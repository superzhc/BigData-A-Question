# Spark 资源调度

Spark 的 Executor 是在 Application 执行之前将所有的资源申请完毕，然后再进行任务调度，直到最后一个 Task 执行完毕，才会释放资源。

- **优点**：每一个 Task 执行之前不需要自己去申请资源，直接使用资源就可以，每一个 Task 的启动时间就变短了，Task 执行时间缩短，使得整个 Application 执行的速度较快。
- **缺点**：无法充分利用集群的资源，比如总共有 10 万的 Task，就要申请 10 万个 Task 的资源，即使只剩下一个 Task 要执行，也得等它执行完才释放资源，在这期间 99999 个 Task 的资源没有执行任何 Task，但也不能被其他需要的进程或线程使用。

> 注：Application 执行之前申请的 Executor 可以被这个 Application 中的所有 Job 共享。

## 资源调度源码解析

### [单独]一、Spark 集群启动：

首先启动 Spark 集群，查看 sbin 下的 `start-all.sh` 脚本，会先启动 Master：

```bash
# Start Master
"${SPARK_HOME}/sbin"/start-master.sh

# Start Workers
"${SPARK_HOME}/sbin"/start-slaves.sh
```

查看 `sbin/start-master.sh` 脚本，发现会去执行 `org.apache.spark.deploy.master.Master` 类，开始在源码中跟进 Master，从 main 方法开始：

```scala
//主方法
def main(argStrings: Array[String]) {
  Thread.setDefaultUncaughtExceptionHandler(new SparkUncaughtExceptionHandler(exitOnUncaughtException = false))
  Utils.initDaemon(log)
  val conf = new SparkConf
  val args = new MasterArguments(argStrings, conf)
  /**
    * 创建RPC环境和Endpoint（RPC 远程过程调用）,在Spark中Driver，Master，Worker角色都有各自的Endpoint，相当于各自的通信邮箱。
    */
  val (rpcEnv, _, _) = startRpcEnvAndEndpoint(args.host, args.port, args.webUiPort, conf)
  rpcEnv.awaitTermination()
}
```

在 main 方法中执行了 startRpcEnvAndEndpoint 方法，创建 RPC 环境和 Endpoint（RPC 远程过程调用），详细的说 RpcEnv 是用于接收消息和处理消息的远程通信调用环境，Master 向 RpcEnv 中去注册，不管是 Master，Driver，Worker，Executor 等都有自己的 Endpoint，相当于是邮箱，其他人想跟我通信先要找到我的邮箱才可以。Master 启动时会将 Endpoint 注册在 RpcEnv 里面，用于接收，处理消息。跟进 startRpcEnvAndEndpoint 方法：

```scala
/**
  * 创建RPC(Remote Procedure Call)环境
  * 这里只是创建准备好Rpc的环境，后面会向RpcEnv中注册 角色【Driver,Master,Worker,Executor】
  */
val rpcEnv = RpcEnv.create(SYSTEM_NAME, host, port, conf, securityMgr)
/**
  * 向RpcEnv 中 注册Master
  *
  * rpcEnv.setupEndpoint(name,new Master)
  * 这里new Master的Master是一个伴生类，继承了ThreadSafeRpcEndpoint，归根结底继承到了Trait接口RpcEndpoint
  * 什么是Endpoint?
  * EndPoint中存在
  *     onstart() :启动当前Endpoint
  *     receive() :负责收消息
  *     receiveAndReply():接受消息并回复
  *  Endpoint还有各自的引用，方便其他Endpoint发送消息，直接引用对方的EndpointRef即可找到对方的Endpoint
  *  以下masterEndpoint就是Master的Endpoint引用RpcEndpointRef 。
  * RpcEndpointRef中存在：
  *     send():发送消息
  *     ask() :请求消息，并等待回应。
  */
val masterEndpoint: RpcEndpointRef = rpcEnv.setupEndpoint(ENDPOINT_NAME,
    new Master(rpcEnv, rpcEnv.address, webUiPort, securityMgr, conf))
val portsResponse = masterEndpoint.askSync[BoundPortsResponse](BoundPortsRequest)
(rpcEnv, portsResponse.webUIPort, portsResponse.restPort)
```

继续跟进 create 方法：

```scala
//创建NettyRpc环境
new NettyRpcEnvFactory().create(config)

/* NettyRpcEnvFactory 的对象，并调用 create 方法，代码如下：*/
/**
  * 创建nettyRPC通信环境。
  * 该方法的作用是创建nettyRPC通信环境，并且在 new NettyRpcEnv 时会做一些初始化：
  * Dispatcher：这个对象中有存放消息的队列和消息的转发
  * TransportContext：可以创建NettyRpcHandler
  */
val nettyEnv = new NettyRpcEnv(sparkConf, javaSerializerInstance, config.advertiseAddress, config.securityManager, config.numUsableCores)

/* NettyRpcEnv 代码如下：*/
private val dispatcher: Dispatcher = new Dispatcher(this, numUsableCores)
private val transportContext = new TransportContext(transportConf, new NettyRpcHandler(dispatcher, this, streamManager))

/* Dispatcher.scala*/
// Dispatcher 的作用是存放消息的队列和消息的转发，首先看 Dispatcher 实例化时执行的逻辑：
private val threadpool: ThreadPoolExecutor = {
  val availableCores =
    if (numUsableCores > 0) numUsableCores else Runtime.getRuntime.availableProcessors()
  val numThreads = nettyEnv.conf.getInt("spark.rpc.netty.dispatcher.numThreads",
    math.max(2, availableCores))
  val pool = ThreadUtils.newDaemonFixedThreadPool(numThreads, "dispatcher-event-loop")
  for (i <- 0 until numThreads) {
    pool.execute(new MessageLoop)
  }
  pool
}

/* MessageLoop.scala */
// 在 Dispatcher 实例化的过程中会创建一个 threadpool，在 threadpool 中会执行 MessageLoop:
private class MessageLoop extends Runnable {
  override def run(): Unit = {
    try {
      while (true) {
        try {
          //take 出来消息一直处理
          val data: EndpointData = receivers.take()
          if (data == PoisonPill) {
            // Put PoisonPill back so that other MessageLoops can see it.
            receivers.offer(PoisonPill)
            return
          }
          //调用process方法处理消息
          data.inbox.process(Dispatcher.this)
        } catch {
          case NonFatal(e) => logError(e.getMessage, e)
        }
      }
    } catch {
      case ie: InterruptedException => // exit
    }
  }
}
```

在 MessageLoop 中的 `receivers.take()` 会一直向 receivers 消息队列中去数据，而 receivers 是在 Dispatcher 初始化时创建的，至此接收消息的程序已经启动起来：

```scala
private val receivers = new LinkedBlockingQueue[EndpointData]
```

其中会传入一个 EndpointData 对象，实例化时会实例化一个 Inbox 对象：

```scala
private class EndpointData(
    val name: String,
    val endpoint: RpcEndpoint,
    val ref: NettyRpcEndpointRef) {
  //将endpoint封装到Inbox中
  val inbox = new Inbox(ref, endpoint)
}
```

实例化 Inbox 对象，当注册 endpoint 时都会调用一个异步方法，messages 中放一个 OnStart 样例类(消息队列)，所以早默认情况下都会调用 OnStart 的匹配方法：

```scala
inbox.synchronized {
  messages.add(OnStart)
}
```

在实例化 MessageLoop 时还会调用 process 方法处理消息：

```scala
//调用process 方法处理消息
data.inbox.process(Dispatcher.this)
```

在 process 中就会找到与发送消息所匹配的 case 去执行逻辑，例如：

```scala
case OnStart =>
  //调用Endpoint 的onStart方法
  endpoint.onStart()
  if (!endpoint.isInstanceOf[ThreadSafeRpcEndpoint]) {
    inbox.synchronized {
      if (!stopped) {
        enableConcurrent = true
      }
    }
  }
```

下面来分析 transportContext 对象的作用，在创建完该类的实例之后，会调用 `transportContext.createServer` 方法去启动 NettyRpc 的服务，在创建 Rpc 服务的过程中，会创建将处理消息的对象 createChannelHandler：

```scala
server = transportContext.createServer(bindAddress, port, bootstraps)
```

在 createServer 方法中会实例化 TransportServer 的对象，在 try 中会调用 init 方法进行初始化：

```scala
try {
  //运行初始化init方法
  init(hostToBind, portToBind);
}
```

在 init 方法中，Rpc 的远程通信对象 bootstrap 会调用 childHandler 方法，会初始化网络通信管道：

```scala
//初始化网络通信管道
context.initializePipeline(ch, rpcHandler);
```

在初始化网络通信管道的过程中，创建处理消息的 channelHandler 对象，该对象的作用是创建并处理客户端的请求消息和服务消息

```scala
//创建处理消息的 channelHandler
TransportChannelHandler channelHandler = createChannelHandler(channel, channelRpcHandler);

private TransportChannelHandler createChannelHandler(Channel channel, RpcHandler rpcHandler) {
  TransportResponseHandler responseHandler = new TransportResponseHandler(channel);
  TransportClient client = new TransportClient(channel, responseHandler);
  TransportRequestHandler requestHandler = new TransportRequestHandler(channel, client,
    rpcHandler, conf.maxChunksBeingTransferred());

  return new TransportChannelHandler(client, responseHandler, requestHandler, conf.connectionTimeoutMs(), closeIdleConnections);
}
```

TransportChannelHandler 由以上 responseHandler，client，requestHandler 三个 handler 构建，并且这个对象中有 channelRead 方法，用于读取接收到的消息：

```scala
@Override
public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {
  //判断当前消息是请求的消息还是回应的消息
  if (request instanceof RequestMessage) {
    requestHandler.handle((RequestMessage) request);
  } else if (request instanceof ResponseMessage) {
    responseHandler.handle((ResponseMessage) request);
  } else {
    ctx.fireChannelRead(request);
  }
}
```

以 requestHandler 为例，调用 `headle —> processRpcRequest((RpcRequest) request)`，会看到 rpcHandler.receive，此时调用的是 NettyRpcHandler 的 receive：

```scala
try {
  /**
   *  rpcHandler 是一直传过来的 NettyRpcHandler
   *  这里的receive 方法 是 NettyRpcHandler 中的方法
      */
    rpcHandler.receive(reverseClient, req.body().nioByteBuffer(), new RpcResponseCallback() {

    @Override
    public void onSuccess(ByteBuffer response) {
      respond(new RpcResponse(req.requestId, new NioManagedBuffer(response)));
    }
    
    @Override
    public void onFailure(Throwable e) {
      respond(new RpcFailure(req.requestId, Throwables.getStackTraceAsString(e)));
    }
  });
}

------------------------------------------------------------------------

override def receive(
  client: TransportClient,
  message: ByteBuffer,
  callback: RpcResponseCallback): Unit = {
  val messageToDispatch = internalReceive(client, message)
  //dispatcher负责发送远程的消息，都最终调到postMessage 方法
  dispatcher.postRemoteMessage(messageToDispatch, callback)
}
```

继续调用 dispatcher.postRemoteMessage 方法：

```scala
def postRemoteMessage(message: RequestMessage, callback: RpcResponseCallback): Unit = {
  val rpcCallContext =
    new RemoteNettyRpcCallContext(nettyEnv, callback, message.senderAddress)
  val rpcMessage = RpcMessage(message.senderAddress, message.content, rpcCallContext)
  postMessage(message.receiver.name, rpcMessage, (e) => callback.onFailure(e))
}
```

在 postRemoteMessage 中，无论是请求消息还是回应消息，都最终会执行到这个 postMessage：

```scala
private def postMessage(
    endpointName: String,
    message: InboxMessage,
    callbackIfStopped: (Exception) => Unit): Unit = {
  val error = synchronized {
    //获取消息的通信邮箱名称
    val data = endpoints.get(endpointName)
    if (stopped) {
      Some(new RpcEnvStoppedException())
    } else if (data == null) {
      Some(new SparkException(s"Could not find $endpointName."))
    } else {
      //将消息放入通信端的消息队列中
      data.inbox.post(message)
      //添加到消息队列中
      receivers.offer(data)
      None
    }
  }
```

在该方法中会将 message 放入 inbox 中，在处理消息的程序启动后，处理消息的程序也已经启动，至此 RpcEnv环境启动完毕，紧接着 Master 要在RpcEnv 中注册：

```scala
val masterEndpoint: RpcEndpointRef = rpcEnv.setupEndpoint(ENDPOINT_NAME,
  new Master(rpcEnv, rpcEnv.address, webUiPort, securityMgr, conf))
```

Master 注册了自己 EndPoint，可以接受处理消息，Master启动成功。

Worker 同理，Spark 集群启动成功。

### 二、Spark 提交任务向 Master 申请启动 Driver

执行 `./spark-submit…`，找到 `spark-submit.sh` 脚本，找到 SparkSubmit 主类：

```scala
//提交任务主类运行
override def main(args: Array[String]): Unit = {
  // Initialize logging if it hasn't been done yet. Keep track of whether logging needs to
  // be reset before the application starts.
  val uninitLog = initializeLogIfNecessary(true, silent = true)
  //设置参数
  val appArgs = new SparkSubmitArguments(args)
  if (appArgs.verbose) {
    // scalastyle:off println
    printStream.println(appArgs)
    // scalastyle:on println
  }
  appArgs.action match {
    //任务提交匹配 submit
    case SparkSubmitAction.SUBMIT => submit(appArgs, uninitLog)
    case SparkSubmitAction.KILL => kill(appArgs)
    case SparkSubmitAction.REQUEST_STATUS => requestStatus(appArgs)
  }
}
```

因为我们是提交任务，所以会匹配到 SparkSubmitAction.SUBMIT 的类型，继续执行 submit 方法。

```scala
val (childArgs, childClasspath, sparkConf, childMainClass) = prepareSubmitEnvironment(args)
```

在 submit 中，prepareSubmitEnvironment 会返回一个四元组，重点注意 childMainClass 类，是最后启动 Driver 的类，这里以 standalone-cluster 为例。

```scala
//准备提交任务的环境
doPrepareSubmitEnvironment(args, conf)
```

进入 prepareSubmitEnvironment，找到准备提交任务的环境的方法 doPrepareSubmitEnvironment：

```scala
//正常提交方式
// In legacy standalone cluster mode, use Client as a wrapper around the user class
childMainClass = STANDALONE_CLUSTER_SUBMIT_CLASS
```

因为这里是以 standalone-cluster 为例，所以找到匹配的case，并进入 STANDALONE_CLUSTER_SUBMIT_CLASS 看看代表什么：

```scala
private[deploy] val STANDALONE_CLUSTER_SUBMIT_CLASS = classOf[ClientApp].getName()
```

这里的 clientApp 就是我们要启动的 Driver 部分。

在 submit 方法继续向下走，因为方法不会直接执行，所以代码向下走回执行到 doRunMain()：

```scala
//运行
runMain(childArgs, childClasspath, sparkConf, childMainClass, args.verbose)
继续执行 runMain 方法，这里将 childMainClass 作为参数传了进来：

//加载类
mainClass = Utils.classForName(childMainClass)

//------------------------------------------------------------

val app: SparkApplication = if (classOf[SparkApplication].isAssignableFrom(mainClass))
```

这里首先将 childMainClass 加载出来，赋给变量 mainClass，之后会将 mainClass 映射成 SparkApplication。

app.start(childArgs.toArray, sparkConf)
接下来调用start方法，这里调用的是 ClientApp 的 start 方法，因为 childMainClass 是 clientApp 的类型：

```scala
rpcEnv.setupEndpoint("client", new ClientEndpoint(rpcEnv, driverArgs, masterEndpoints, conf))
```

在 rpc 中设置提交当前任务的 Endpoint，只要设置肯定会运行 new ClientEndpoint 类的 onStart 方法：

```scala
sval mainClass = "org.apache.spark.deploy.worker.DriverWrapper"
//将DriverWrapper 这个类封装到Command中
val command = new Command(mainClass,
Seq("{{WORKER_URL}}", "{{USER_JAR}}", driverArgs.mainClass) ++ driverArgs.driverOptions,
sys.env, classPathEntries, libraryPathEntries, javaOpts)

val driverDescription = new DriverDescription(
driverArgs.jarUrl,
driverArgs.memory,
driverArgs.cores,
driverArgs.supervise,
command)
//向Master申请启动Driver,Master中的 receiveAndReply 方法会接收此请求消息
asyncSendToMasterAndForwardReply[SubmitDriverResponse](
RequestSubmitDriver(driverDescription))
```

这里将 org.apache.spark.deploy.worker.DriverWrapper 封装成 command，并且将 command 封装到 driverDescription 中，然后向 Master 申请启动 Driver，Master 中的 receiveAndReply 方法会接收此请求消息：

```scala
case RequestSubmitDriver(description) =>
  //判断Master状态
  if (state != RecoveryState.ALIVE) {
    val msg = s"${Utils.BACKUP_STANDALONE_MASTER_PREFIX}: $state. " +
      "Can only accept driver submissions in ALIVE state."
    context.reply(SubmitDriverResponse(self, false, None, msg))
  } else {
    logInfo("Driver submitted " + description.command.mainClass)
    val driver = createDriver(description)
    persistenceEngine.addDriver(driver)
    waitingDrivers += driver
    drivers.add(driver)
    schedule()

    // TODO: It might be good to instead have the submission client poll the master to determine
    //       the current status of the driver. For now it's simply "fire and forget".
    
    context.reply(SubmitDriverResponse(self, true, Some(driver.id),
      s"Driver successfully submitted as ${driver.id}"))
  }
```

这里首先会判断 Master 的状态，如果符合要求的话，会使用之前封装的 description 对象创建 driver，driver 其实是一个 DriverInfo 的类型，里面封装了一些 Driver 的信息。之后会在 waitingDrivers (private val waitingDrivers = new ArrayBuffer[DriverInfo]) 中添加刚才创建完的 DriverInfo对象，进入 schedule() 调度方法：

```scala
/**
  * schedule() 方法是通用的方法
  * 这个方法中当申请启动Driver的时候也会执行，但是最后一行的startExecutorsOnWorkers 方法中 waitingApp是空的，只是启动Driver。
  * 在提交application时也会执行到这个scheduler方法，这个时候就是要启动的Driver是空的，但是会直接运行startExecutorsOnWorkers 方法给当前的application分配资源
    *
    */
private def schedule(): Unit = {
    //判断Master状态
    if (state != RecoveryState.ALIVE) {
    return
    }
    // Drivers take strict precedence over executors 这里是打散worker
    val shuffledAliveWorkers = Random.shuffle(workers.toSeq.filter(_.state == WorkerState.ALIVE))
    //可用的worker数量
    val numWorkersAlive = shuffledAliveWorkers.size
    var curPos = 0
    for (driver <- waitingDrivers.toList) { // iterate over a copy of waitingDrivers
    // We assign workers to each waiting driver in a round-robin fashion. For each driver, we
    // start from the last worker that was assigned a driver, and continue onwards until we have
    // explored all alive workers.
    var launched = false
    var numWorkersVisited = 0
    while (numWorkersVisited < numWorkersAlive && !launched) {
      //拿到curPos位置的worker
      val worker = shuffledAliveWorkers(curPos)
      numWorkersVisited += 1
      if (worker.memoryFree >= driver.desc.mem && worker.coresFree >= driver.desc.cores) {
        //这里是启动Driver,启动Driver之后会为当前的application 申请资源
        launchDriver(worker, driver)
        waitingDrivers -= driver
        launched = true
      }
      //curPos 就是一直加一的往后取 Worker  ,一直找到满足资源的worker
      curPos = (curPos + 1) % numWorkersAlive
    }
    }
    startExecutorsOnWorkers()
}
```

这里会找到满足条件的 Worker 节点去启动 Driver，调用 launchDriver(worker, driver) 方法：

```scala
private def launchDriver(worker: WorkerInfo, driver: DriverInfo) {
  logInfo("Launching driver " + driver.id + " on worker " + worker.id)
  worker.addDriver(driver)
  driver.worker = Some(worker)
  //给Worker发送消息启动Driver,这里在Worker中会有receive方法一直匹配LaunchDriver
  worker.endpoint.send(LaunchDriver(driver.id, driver.desc))
  driver.state = DriverState.RUNNING
}
```

这里会向 Worker 发送消息启动 Driver，这里在 Worker 中会有 receive 方法一直匹配 LaunchDriver：

```scala
case LaunchDriver(driverId, driverDesc) =>
  logInfo(s"Asked to launch driver $driverId")
  val driver = new DriverRunner(
    conf,
    driverId,
    workDir,
    sparkHome,
    driverDesc.copy(command = Worker.maybeUpdateSSLSettings(driverDesc.command, conf)),
    self,
    workerUri,
    securityMgr)
  drivers(driverId) = driver
  //启动Driver,会初始化 org.apache.spark.deploy.worker.DriverWrapper ，运行main方法
  driver.start()
```

这里说的启动的 Driver 就是刚才说的 val mainClass = "org.apache.spark.deploy.worker.DriverWrapper"，Driver 启动的就是 DriverWrapper 类的启动，DriverWrapper 的启动就是在 Worker 中创建一个 Driver 进程。启动Driver，会初始化 org.apache.spark.deploy.worker.DriverWrapper，运行 main 方法：

```scala
//下面的mainClass就是我们真正提交的application
case workerUrl :: userJar :: mainClass :: extraArgs =>
在 DriverWrapper 的 main 方法中的 mainClass，就是我们真正提交的 Application

// Delegate to supplied main class
val clazz = Utils.classForName(mainClass)
//得到提交application的主方法
val mainMethod = clazz.getMethod("main", classOf[Array[String]])

/**
  * 启动提交的application 中的main 方法。
  * 这里启动application，会先创建SparkConf和SparkContext
  *   SparkContext中 362行try块中会创建TaskScheduler(492)
    */
mainMethod.invoke(null, extraArgs.toArray[String])
```

### 三、Spark Driver 启动完毕，并且向 Master 注册 Applciation

当在 Worker 启动完 Driver 之后，程序走到在 Driver 端我们手写的代码上，首先就是启动 SparkContext，在与源码中找到 SparkContext 类，创建 Scala 对象时只有方法不执行，剩下的都执行，找到如下代码：

```scala
/**
  * 启动调度程序，这里(sched,ts) 分别对应 StandaloneSchedulerBackend 和 TaskSchedulerImpl 两个对象
  *   master 是提交任务写的 spark://node1:7077
    */
    val (sched, ts) = SparkContext.createTaskScheduler(this, master, deployMode)
    _schedulerBackend = sched
    _taskScheduler = ts
    _dagScheduler = new DAGScheduler(this)
    _heartbeatReceiver.ask[Boolean](TaskSchedulerIsSet)

// start TaskScheduler after taskScheduler sets DAGScheduler reference in DAGScheduler's
// constructor
//TaskSchedulerImpl 对象的start方法
_taskScheduler.start()
```

我们能看到 createTaskScheduler 方法返回一个元组，且在 SparkContext 中已经创建了 DAGScheduler对象，进入 createTaskScheduler 方法：

```scala
//standalone 提交任务都是以 “spark://”这种方式提交
case SPARK_REGEX(sparkUrl) =>
  //scheduler 创建TaskSchedulerImpl 对象
  val scheduler = new TaskSchedulerImpl(sc)
  val masterUrls = sparkUrl.split(",").map("spark://" + _)
  /**
          * 这里的 backend 是StandaloneSchedulerBackend 这个类型
          */
  val backend = new StandaloneSchedulerBackend(scheduler, sc, masterUrls)
  //这里会调用 TaskSchedulerImpl 对象中的  initialize 方法将 backend 初始化,一会要用到
  scheduler.initialize(backend)
  //返回了  StandaloneSchedulerBackend 和 TaskSchedulerImpl 两个对象
  (backend, scheduler)
```

因为是以Standalone的方式提交任务，所以找到匹配的 case。在这里创建了 TaskSchedulerImpl 的对象scheduler，并将其传入了 StandaloneSchedulerBackend(scheduler, sc, masterUrls)中，得知，返回的 backend 是一个 Standalone 大环境的任务调度器，scheduler 则是 TaskScheduler 的调度器。

```scala
/*
  * 在TaskScheduler中设置进来backend ，
  *  这里的backend 是 StandaloneSchedulerBackend
  *  StandaloneSchedulerBackend 继承了CoarseGrainedSchedulerBackend 类
    */
    def initialize(backend: SchedulerBackend) {
    this.backend = backend
    schedulableBuilder = {
      schedulingMode match {
        case SchedulingMode.FIFO =>
          new FIFOSchedulableBuilder(rootPool)
        case SchedulingMode.FAIR =>
          new FairSchedulableBuilder(rootPool, conf)
        case _ =>
          throw new IllegalArgumentException(s"Unsupported $SCHEDULER_MODE_PROPERTY: " +
          s"$schedulingMode")
      }
    }
    schedulableBuilder.buildPools()
    }
```

在 scheduler.initialize(backend) 中的作用就是将 backend 设置为 scheduler 的属性，且StandaloneSchedulerBackend 继承了CoarseGrainedSchedulerBackend 类。

```scala
//TaskSchedulerImpl 对象的start方法
_taskScheduler.start()
ts 调用的是 TaskSchedulerImpl 中的 start() 方法：

/**
  * TaskScheduler 启动
    */
override def start() {
    //StandaloneSchedulerBackend 启动
    backend.start()

    if (!isLocal && conf.getBoolean("spark.speculation", false)) {
      logInfo("Starting speculative execution thread")
        speculationScheduler.scheduleWithFixedDelay(new Runnable {
          override def run(): Unit = Utils.tryOrStopSparkContext(sc) {
            checkSpeculatableTasks()
          }
        }, SPECULATION_INTERVAL_MS, SPECULATION_INTERVAL_MS, TimeUnit.MILLISECONDS)
    }
}
```

进入 backend.start() 方法，因为 backend 是 StandaloneSchedulerBackend 类型，所有调用的是 StandaloneSchedulerBackend 中的 start() 方法：

```scala
override def start() {
  /**
    * super.start()中有创建Driver的通信邮箱也就是Driver的引用
    * 未来Executor就是向 StandaloneSchedulerBackend中父类 CoarseGrainedSchedulerBackend 中反向注册信息的.
    */
  super.start()
```
进入 super.start()，在方法中向 RpcEnv 注册了 Driver 端的 Endpoint：

```scala
override def start() {
  val properties = new ArrayBuffer[(String, String)]
  for ((key, value) <- scheduler.sc.conf.getAll) {
    if (key.startsWith("spark.")) {
      properties += ((key, value))
    }
  }

  // TODO (prashant) send conf instead of properties
  /**
    * 创建Driver的Endpoint ,就是创建Driver的通信邮箱，向Rpc中注册当前DriverEndpoint
    * 未来Executor就是向DriverEndpoint中反向注册信息，这里Driver中会有receiveAndReply方法一直监听匹配发过来的信息
    */
  driverEndpoint = createDriverEndpointRef(properties)
}
```

注意在 backend.start() 方法执行创建完 DriverEndpoint 之后，还执行了如下代码，功能是向 Driver 注册application 的信息 ：

```scala
val command = Command("org.apache.spark.executor.CoarseGrainedExecutorBackend", args, sc.executorEnvs, classPathEntries ++ testingClassPath, libraryPathEntries, javaOpts)
......
  val appDesc: ApplicationDescription = ApplicationDescription(sc.appName, maxCores,   sc.executorMemory, command, webUrl, sc.eventLogDir, sc.eventLogCodec, coresPerExecutor,    initialExecutorLimit)
  //提交应用程序的描述信息
  //封装 appDesc,这里已经传入了StandaloneAppClient 中
  client = new StandaloneAppClient(sc.env.rpcEnv, masters, appDesc, this, conf)
  //启动StandaloneAppClient，之后会向Driver注册application的信息
  client.start()
进入 client.start() 之后就是给 rpcEnv 上注册信息

def start() {
  // Just launch an rpcEndpoint; it will call back into the listener.
  /**
    *  这里就是给空的 endpoint[AtomicReference] 设置下 信息，
    *  主要是rpcEnv.setupEndpoint 中创建了 ClientEndpoint 只要设置Endpoint 肯定会调用 ClientEndpoint的onStart方法
    */
  endpoint.set(rpcEnv.setupEndpoint("AppClient", new ClientEndpoint(rpcEnv)))
}
```

然后读取 ClientEndpoint 的 onStart() 方法

```scala
//onStart 方法
override def onStart(): Unit = {
  try {
    //向Master 注册当前application的信息
    registerWithMaster(1)
  } catch {
    case e: Exception =>
      logWarning("Failed to connect to master", e)
      markDisconnected()
      stop()
  }
}
```

向 Master 注册当前application的信息的 registerWithMaster() 方法，因为我们的 Master 会有高可用，所以要给所有的 Master 注册，进入后看到 tryRegisterAllMasters()：

```scala
private def registerWithMaster(nthRetry: Int) {
  //tryRegisterAllMasters 尝试向所有的Master 注册application信息
  registerMasterFutures.set(tryRegisterAllMasters())
  registrationRetryTimer.set(registrationRetryThread.schedule(new Runnable {
    override def run(): Unit = {
      if (registered.get) {
        registerMasterFutures.get.foreach(_.cancel(true))
        registerMasterThreadPool.shutdownNow()
      } else if (nthRetry >= REGISTRATION_RETRIES) {
        markDead("All masters are unresponsive! Giving up.")
      } else {
        registerMasterFutures.get.foreach(_.cancel(true))
        registerWithMaster(nthRetry + 1)
      }
    }
  }, REGISTRATION_TIMEOUT_SECONDS, TimeUnit.SECONDS))
}
```

进入 tryRegisterAllMasters() 之后，会获取 Master 的 Endpoint，向 Master 注册 application，Master 类中receive 方法中会匹配接收 RegisterApplication 类型，随机在 Master 中匹配 RegisterApplication 的 case：

```scala
//向所有的Master去注册Application的信息
private def tryRegisterAllMasters(): Array[JFuture[_]] = {
  //遍历所有的Master地址
  for (masterAddress <- masterRpcAddresses) yield {
    registerMasterThreadPool.submit(new Runnable {
      override def run(): Unit = try {
        if (registered.get) {
          return
        }
        logInfo("Connecting to master " + masterAddress.toSparkURL + "...")
        //获取Master的通信邮箱
        val masterRef = rpcEnv.setupEndpointRef(masterAddress, Master.ENDPOINT_NAME)
        //向Master注册application，Master类中receive方法中会匹配接收 RegisterApplication类型
        masterRef.send(RegisterApplication(appDescription, self))
      } catch {
        case ie: InterruptedException => // Cancelled
        case NonFatal(e) => logWarning(s"Failed to connect to master $masterAddress", e)
      }
    })
  }
}
```

Master 匹配 RegisterApplication 类型消息的处理流程：

```scala
//Driver 端提交过来的要注册Application
case RegisterApplication(description, driver) =>
  // TODO Prevent repeated registrations from some driver
  //如果Master状态是standby 忽略不提交任务
  if (state == RecoveryState.STANDBY) {
    // ignore, don't send response
  } else {
    logInfo("Registering app " + description.name)
    //这里封装application信息，注意，在这里可以跟进去看到默认一个application使用的core的个数就是 Int.MaxValue
    val app = createApplication(description, driver)
    //注册app ,这里面有向 waitingApps中加入当前application
    registerApplication(app)
    logInfo("Registered app " + description.name + " with ID " + app.id)
    persistenceEngine.addApplication(app)
    driver.send(RegisteredApplication(app.id, self))
    //最终又会执行通用方法schedule()
    schedule()
  }
```

至此 Driver 向 Master 注册 Application 流程结束。

### 四、Master 发送消息启动 Executor

在执行 schedule() 之前会向 Driver(StandaloneAppClient) 端发送 接收到Application已经被注册 的消息，最终又会执行通用方法 schedule() 方法：

```scala
/**
  * schedule() 方法是通用的方法
  * 这个方法中当申请启动Driver的时候也会执行，但是最后一行的startExecutorsOnWorkers 方法中 waitingApp是空的，只是启动Driver。
  * 在提交application时也会执行到这个scheduler方法，这个时候就是要启动的Driver是空的，但是会直接运行startExecutorsOnWorkers 方法给当前的application分配资源
    *
    */
private def schedule(): Unit = {
    //判断Master状态
    if (state != RecoveryState.ALIVE) {
    return
    }
    // Drivers take strict precedence over executors 这里是打散worker
    val shuffledAliveWorkers = Random.shuffle(workers.toSeq.filter(_.state == WorkerState.ALIVE))
    //可用的worker数量
    val numWorkersAlive = shuffledAliveWorkers.size
    var curPos = 0
    for (driver <- waitingDrivers.toList) { // iterate over a copy of waitingDrivers
    // We assign workers to each waiting driver in a round-robin fashion. For each driver, we
    // start from the last worker that was assigned a driver, and continue onwards until we have
    // explored all alive workers.
    var launched = false
    var numWorkersVisited = 0
    while (numWorkersVisited < numWorkersAlive && !launched) {
      //拿到curPos位置的worker
      val worker = shuffledAliveWorkers(curPos)
      numWorkersVisited += 1
      if (worker.memoryFree >= driver.desc.mem && worker.coresFree >= driver.desc.cores) {
        //这里是启动Driver,启动Driver之后会为当前的application 申请资源
        launchDriver(worker, driver)
        waitingDrivers -= driver
        launched = true
      }
      //curPos 就是一直加一的往后取 Worker  ,一直找到满足资源的worker
      curPos = (curPos + 1) % numWorkersAlive
    }
    }
    startExecutorsOnWorkers()
}
```

此时我们又回到了 schedule() 方法，之前是从 launchDriver(worker, driver) 进去的，现在又出来继续调用 startExecutorsOnWorkers() 方法：

```scala
private def startExecutorsOnWorkers(): Unit = {
  // Right now this is a very simple FIFO scheduler. We keep trying to fit in the first app
  // in the queue, then the second app, etc.
  //从waitingApps中获取提交的app
  for (app <- waitingApps) {
    //coresPerExecutor 在application中获取启动一个Executor使用几个core 。参数--executor-core可以指定，下面指明不指定就是1
    val coresPerExecutor = app.desc.coresPerExecutor.getOrElse(1)
    // If the cores left is less than the coresPerExecutor,the cores left will not be allocated
    //判断是否给application分配够了core,因为后面每次给application 分配core后 app.coresLeft 都会相应的减去分配的core数
    if (app.coresLeft >= coresPerExecutor) {
      // Filter out workers that don't have enough resources to launch an executor
      //过滤出可用的worker
      val usableWorkers = workers.toArray.filter(_.state == WorkerState.ALIVE)
        .filter(worker => worker.memoryFree >= app.desc.memoryPerExecutorMB &&
          worker.coresFree >= coresPerExecutor)
        .sortBy(_.coresFree).reverse

      /**
        * 下面就是去worker中划分每个worker提供多少core和启动多少Executor,注意：spreadOutApps 是true
        * 返回的 assignedCores 就是每个worker节点中应该给当前的application分配多少core
        */
      val assignedCores = scheduleExecutorsOnWorkers(app, usableWorkers, spreadOutApps)
    
      // Now that we've decided how many cores to allocate on each worker, let's allocate them
      for (pos <- 0 until usableWorkers.length if assignedCores(pos) > 0) {
        //在worker中给Executor划分资源
        allocateWorkerResourceToExecutors(
          app, assignedCores(pos), app.desc.coresPerExecutor, usableWorkers(pos))
      }
    }
  }
}
```

在此处应该注意的是 scheduleExecutorsOnWorkers(app, usableWorkers, spreadOutApps) 方法，最后返回最后返回每个Worker上分配多少core，其他解释已加注释：

```scala
private def scheduleExecutorsOnWorkers(
      app: ApplicationInfo,
      usableWorkers: Array[WorkerInfo],
      spreadOutApps: Boolean): Array[Int] = {
    //启动一个Executor使用多少core,这里如果提交任务没有指定 --executor-core这个值就是None
    val coresPerExecutor : Option[Int]= app.desc.coresPerExecutor
    //这里指定如果提交任务没有指定启动一个Executor使用几个core，默认就是1
    val minCoresPerExecutor = coresPerExecutor.getOrElse(1)
    //oneExecutorPerWorker 当前为true
    val oneExecutorPerWorker :Boolean= coresPerExecutor.isEmpty
    //默认启动一个Executor使用的内存就是1024M，这个设置在SparkContext中464行
    val memoryPerExecutor = app.desc.memoryPerExecutorMB
    //可用worker的个数
    val numUsable = usableWorkers.length
    //创建两个重要对象
    val assignedCores = new Array[Int](numUsable) // Number of cores to give to each worker
    val assignedExecutors = new Array[Int](numUsable) // Number of new executors on each worker
    /**
      * coresToAssign 指的是当前要给Application分配的core是多少？ app.coresLeft 与集群所有worker剩余的全部core 取个最小值
      * 这里如果提交application时指定了 --total-executor-core 那么app.coresLeft  就是指定的值
      */
    var coresToAssign = math.min(app.coresLeft, usableWorkers.map(_.coresFree).sum)

    /** Return whether the specified worker can launch an executor for this app. */
    //判断某台worker节点是否还可以启动Executor
    def canLaunchExecutor(pos: Int): Boolean = {
      //可分配的core是否大于启动一个Executor使用的1个core
      val keepScheduling = coresToAssign >= minCoresPerExecutor
      //是否有足够的core
      val enoughCores = usableWorkers(pos).coresFree - assignedCores(pos) >= minCoresPerExecutor
    
      // If we allow multiple executors per worker, then we can always launch new executors.
      // Otherwise, if there is already an executor on this worker, just give it more cores.
      //assignedExecutors(pos) == 0 为true,launchingNewExecutor就是为true
      val launchingNewExecutor = !oneExecutorPerWorker || assignedExecutors(pos) == 0
      //启动新的Executor
      if (launchingNewExecutor) {
        val assignedMemory = assignedExecutors(pos) * memoryPerExecutor
        //是否有足够的内存
        val enoughMemory = usableWorkers(pos).memoryFree - assignedMemory >= memoryPerExecutor
        //这里做安全判断，说的是要分配启动的Executor和当前application启动的使用的Executor总数是否在Application总的Executor限制之下
        val underLimit = assignedExecutors.sum + app.executors.size < app.executorLimit
        keepScheduling && enoughCores && enoughMemory && underLimit
      } else {
        // We're adding cores to an existing executor, so no need
        // to check memory and executor limits
        keepScheduling && enoughCores
      }
    }
    
    // Keep launching executors until no more workers can accommodate any
    // more executors, or if we have reached this application's limits
//    var freeWorkers = (0 until numUsable).filter(one=>canLaunchExecutor(one))
    var freeWorkers = (0 until numUsable).filter(canLaunchExecutor)
    while (freeWorkers.nonEmpty) {
      freeWorkers.foreach { pos =>
        var keepScheduling = true
        while (keepScheduling && canLaunchExecutor(pos)) {
          coresToAssign -= minCoresPerExecutor
          assignedCores(pos) += minCoresPerExecutor

          // If we are launching one executor per worker, then every iteration assigns 1 core
          // to the executor. Otherwise, every iteration assigns cores to a new executor.
          if (oneExecutorPerWorker) {
            assignedExecutors(pos) = 1
          } else {
            assignedExecutors(pos) += 1
          }
    
          // Spreading out an application means spreading out its executors across as
          // many workers as possible. If we are not spreading out, then we should keep
          // scheduling executors on this worker until we use all of its resources.
          // Otherwise, just move on to the next worker.
          if (spreadOutApps) {
            keepScheduling = false
          }
        }
      }
      freeWorkers = freeWorkers.filter(canLaunchExecutor)
    }
    //最后返回每个Worker上分配多少core
    assignedCores
  }
```

在上述代码中还有几个注意点：


为每个worker节点分配完资源后就开始启动Executor了，接着执行 Master.schedule() 方法下的 allocateWorkerResourceToExecutors 方法，该方法传入了当前 application，每个 worker 分配的核数，每个核上要启动的 Executor 数量以及可用的 usableWorkers ：

```scala
//在worker中给Executor划分资源
allocateWorkerResourceToExecutors(
  app, assignedCores(pos), app.desc.coresPerExecutor, usableWorkers(pos))
点进入之后 Master 会计算每个 Executor 要分配多少个 core，紧接着去 worker 启动Executor：

private def allocateWorkerResourceToExecutors(
    app: ApplicationInfo,
    assignedCores: Int,
    coresPerExecutor: Option[Int],
    worker: WorkerInfo): Unit = {
  // If the number of cores per executor is specified, we divide the cores assigned
  // to this worker evenly among the executors with no remainder.
  // Otherwise, we launch a single executor that grabs all the assignedCores on this worker.
  val numExecutors = coresPerExecutor.map { assignedCores / _ }.getOrElse(1)
  //每个Executor要分配多少个core
  val coresToAssign = coresPerExecutor.getOrElse(assignedCores)
  for (i <- 1 to numExecutors) {
    val exec: ExecutorDesc = app.addExecutor(worker, coresToAssign)
    //去worker中启动Executor
    launchExecutor(worker, exec)
    app.state = ApplicationState.RUNNING
  }
}
```

在 launchExecutor(worker, exec) 方法中，会获取 Worker 的通信邮箱，给 Worker 发送启动 Executor 的消息，具体就是启动Executor需要多少个 core 和 内存，然后在 Worker 中有 receive 方法一直匹配 LaunchExecutor 类型：

```scala
//启动Executor
private def launchExecutor(worker: WorkerInfo, exec: ExecutorDesc): Unit = {
  logInfo("Launching executor " + exec.fullId + " on worker " + worker.id)
  worker.addExecutor(exec)

  /**
    *  获取Worker的通信邮箱，给Worker发送启动Executor【多少core和多少内存】
    *  在Worker中有receive 方法一直匹配 LaunchExecutor 类型
    */
  worker.endpoint.send(LaunchExecutor(masterUrl,
    exec.application.id, exec.id, exec.application.desc, exec.cores, exec.memory))
  exec.application.driver.send(
    ExecutorAdded(exec.id, worker.id, worker.hostPort, exec.cores, exec.memory))
}
```

在 worker 的处理方法中，会创建 ExecutorRunner 类对象，参数 appDesc 中有 Command("org.apache.spark.executor.CoarseGrainedExecutorBackend",…)，其中第一个参数就是Executor类

```scala
//创建ExecutorRunner
val manager = new ExecutorRunner(
  appId,
  execId,
  /**
    * appDesc 中有 Command("org.apache.spark.executor.CoarseGrainedExecutorBackend",.......) 中
    * 第一个参数就是Executor类
    */
  appDesc.copy(command = Worker.maybeUpdateSSLSettings(appDesc.command, conf)),
  cores_,
  memory_,
  self,
  workerId,
  host,
  webUi.boundPort,
  publicAddress,
  sparkHome,
  executorDir,
  workerUri,
  conf,
  appLocalDirs, ExecutorState.RUNNING)
```

紧接着就是启动通过 manager 启动Executor，启动的就是 CoarseGrainedExecutorBackend 类，下面看 CoarseGrainedExecutorBackend 类中的 main 方法有反向注册给Driver：

```scala
//注册Executor的通信邮箱，会调用CoarseGrainedExecutorBackend的onstart方法
env.rpcEnv.setupEndpoint("Executor", new CoarseGrainedExecutorBackend(
  env.rpcEnv, driverUrl, executorId, hostname, cores, userClassPath, env))
会调用 CoarseGrainedExecutorBackend 类的 onStart() 方法：

override def onStart() {
  logInfo("Connecting to driver: " + driverUrl)
  //从RPC中拿到Driver的引用，给Driver反向注册Executor
  rpcEnv.asyncSetupEndpointRefByURI(driverUrl).flatMap { ref =>
    // This is a very fast action so we can use "ThreadUtils.sameThread"
    //拿到Driver的引用
    driver = Some(ref)
    /**
      * 给Driver反向注册Executor信息，这里就是注册给之前看到的 CoarseGrainedSchedulerBackend 类中的DriverEndpoint
      * DriverEndpoint类中会有receiveAndReply 方法来匹配RegisterExecutor
      */
    ref.ask[Boolean](RegisterExecutor(executorId, self, hostname, cores, extractLogUrls))
  }(ThreadUtils.sameThread).onComplete {
    // This is a very fast action so we can use "ThreadUtils.sameThread"
    case Success(msg) =>
      // Always receive `true`. Just ignore it
    case Failure(e) =>
      exitExecutor(1, s"Cannot register with driver: $driverUrl", e, notifyDriver = false)
  }(ThreadUtils.sameThread)
}
```

在该方法中从 RPC 中拿到 Driver 的引用，将 Executor 反向注册给 Driver，方法中的 ref 就是 CoarseGrainedSchedulerBackend 类的引用，之后在 CoarseGrainedSchedulerBackend 中找到匹配 RegisterExecutor 的 case，用于反向注册：

```scala
/**
  * 拿到Execuotr的通信邮箱，发送消息给ExecutorRef 告诉 Executor已经被注册。
  * 在 CoarseGrainedExecutorBackend 类中 receive方法一直监听有没有被注册，匹配上就会启动Executor
    *
    */
executorRef.send(RegisteredExecutor)
```

在 Driver 端告诉 Execuotr 端已经被注册，匹配上就会启动 Executor。去看 Execuotr 端匹配 RegisteredExecutor 的 case，用于启动 Executor：

```scala
//匹配上Driver端发过来的消息，已经接受注册Executor了，下面要启动Executor
case RegisteredExecutor =>
  logInfo("Successfully registered with driver")
  try {
    //下面创建Executor，Executor真正的创建Executor,Executor中有线程池用于task运行
    executor = new Executor(executorId, hostname, env, userClassPath, isLocal = false)
  } catch {
    case NonFatal(e) =>
      exitExecutor(1, "Unable to create executor due to " + e.getMessage, e)
  }
```

Executor 在被告知反向注册成功之后，开始真正的创建 Executor，Executor 中有线程池用于task运行：

```scala
/**
  * Executor 中的线程池
    */
private val threadPool = {
    val threadFactory = new ThreadFactoryBuilder()
    .setDaemon(true)
    .setNameFormat("Executor task launch worker-%d")
    .setThreadFactory(new ThreadFactory {
      override def newThread(r: Runnable): Thread =
        // Use UninterruptibleThread to run tasks so that we can allow running codes without being
        // interrupted by `Thread.interrupt()`. Some issues, such as KAFKA-1894, HADOOP-10622,
        // will hang forever if some methods are interrupted.
        new UninterruptibleThread(r, "unused") // thread name will be set by ThreadFactoryBuilder
    })
    .build()
    Executors.newCachedThreadPool(threadFactory).asInstanceOf[ThreadPoolExecutor]
}
```

至此，Executor 创建完毕，开始 Spark 的任务调度过程。