# SparkCore-Overview

## 部署图

![Spark部署图](images/1630214c239dc419)

从部署图中可以看到

- 整个集群分为 Master 节点和 Worker 节点，相当于 Hadoop 的 Master 和 Slave 节点。
- Master 节点上常驻 Master 守护进程，负责管理全部的 Worker 节点。
- Worker 节点上常驻 Worker 守护进程，负责与 Master 节点通信并管理 Executors。
- Driver 官方解释是 `The process running the main() function of the application and creating the SparkContext`。Application 就是用户自己写的 Spark 程序（driver program），比如 `WordCount.scala`。如果 driver program 在 Master 上运行，比如在 Master 上运行
    ```bash
    ./bin/run-example SparkPi 10
    ```
    那么 SparkPi 就是 Master 上的 Driver。如果是 YARN 集群，那么 Driver 可能被调度到 Worker 节点上运行（比如上图中的 Worker Node 2）。另外，如果直接在自己的 PC 上运行 driver program，比如在 Eclipse 中运行 driver program，使用
    ```scala
    //val sc = new SparkContext("spark://master:7077", "AppName")

    //spark 2.x后用SparkSession作为统一入口
    val spark = SparkSession
        .builder()
        .appName("AppName")
        .master("spark://master:7077")
        .config("spark.sql.warehouse.dir", "D:\\spark-warehouse")
        .getOrCreate()
    ```
    去连接 master 的话，driver 就在自己的 PC 上，但是不推荐这样的方式，因为 PC 和 Workers 可能不在一个局域网，driver 和 executor 之间的通信会很慢。
- 每个 Worker 上存在一个或者多个 ExecutorBackend 进程。每个进程包含一个 Executor 对象，该对象持有一个线程池，每个线程可以执行一个 task。
    ```scala
    //Executor.scala 使用线程池处理执行任务
    private val threadPool = {
        val threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Executor task launch worker-%d")
            .setThreadFactory(new ThreadFactory {
                override def newThread(r: Runnable): Thread = new UninterruptibleThread(r, "unused") // thread name will be set by ThreadFactoryBuilder
            })
            .build()
        //无界线程池
        Executors.newCachedThreadPool(threadFactory).asInstanceOf[ThreadPoolExecutor]
    }
    //执行任务时
    def launchTask(context: ExecutorBackend, taskDescription: TaskDescription): Unit = {
        //class TaskRunner extends Runnable
        val tr = new TaskRunner(context, taskDescription)
        runningTasks.put(taskDescription.taskId, tr)
        //线程池中执行
        threadPool.execute(tr)
    }
    ```
- 每个 application 包含一个 driver 和多个 executors，每个 executor 里面运行的 tasks 都属于同一个 application。
- 在 Standalone 版本中，ExecutorBackend 被实例化成 CoarseGrainedExecutorBackend 进程。
    > Spark Executor Driver 资源调度小结几个要点:
    > 1. worker 每次 LaunchExecutor 都会创建一个 CoarseGrainedExecutorBackend 进程(Executor 和 CoarseGrainedExecutorBackend 是 1 对 1 的关系)
    > 2. executor 分配模式:<br/>
    > 1)spreadOutApps 分散分配 executor，最大化负载均衡和高并行;<br/>
    > 2)!spreadOutApps 集中分配，尽可能的满足App，让其尽快执行，而忽略了其并行效率和负载均衡
- Worker 通过持有 ExecutorRunner 对象来控制 CoarseGrainedExecutorBackend 的启停。
    ```scala
    //worker.scala
    //Worker 通过持有 ExecutorRunner 对象来控制 CoarseGrainedExecutorBackend 的启停
    val executors = new HashMap[String, ExecutorRunner]

    def receive = {
        case LaunchExecutor
            val manager = new ExecutorRunner(...)   //创建ExecutorRunner
            executors(appId + "/" + execId) = manager
            manager.start() //起线程，线程内起进程处理
            coresUsed += cores_
            memoryUsed += memory_
            //更新完本地worker状态,启动executor线程后就通知master
            //向Master发送ExecutorStateChanged信号
            sendToMaster(ExecutorStateChanged(appId, execId, manager.state, None, None))
            ...
    }
    ```

> Spark 集群启动后会有 Master 进程和 Worker 进程
> 当提交任务后，才在 Worker上启动 Driver(一个 Application 对应一个 Driver)，Driver 在 Worker 上启动 ExecutorBackend 进程，用于分配 Executor

```scala
//master.scala master在worker上启动driver
//等待启动的多个 driver
private val waitingDrivers = new ArrayBuffer[DriverInfo]

/**
 * schedule()方法是来调度当前可用资源的调度方法，它管理还在排队等待的Apps资源的分配，
 * 这个方法是每次在集群资源发生变动的时候都会调用，根据当前集群最新的资源来进行Apps的资源分配。
 * 变动包括注册driver,在worker上启动driver,在worker上启动executor
 */
private def schedule(): Unit = {
    ...
    //启动Driver 内部实现是发送启动Driver命令给指定Worker，Worker来启动Driver(master向worker发送LaunchDriver消息)
    launchDriver(worker, driver) => worker.endpoint.send(LaunchDriver(driver.id, driver.desc))
    startExecutorsOnWorkers()
}
```

```scala
//worker.scala
 def receive = {
    case LaunchDriver(driverId, driverDesc) =>   //worker接收master信息启动driver
      val driver = new DriverRunner(...)
      drivers(driverId) = driver  //HashMap[String, DriverRunner]
      driver.start()    //内部起线程来启动driver进程
      coresUsed += driverDesc.cores
      memoryUsed += driverDesc.mem
}
```

## Master 与 Worker 启动

启动命令

```bash
start-all.sh
    #内部调用
    spark-config.sh #加载配置
    start-master.sh #会启动Master,new Master对象
    start-slaves.sh #读取slave启动slave,new worker对象
```

### Master

```scala
// Master.scala
main()
    new MasterArguments(argStrings, conf) //设置配置参数
    RpcEnv.create() //创建RPC通信,启动Netty Server
    startRpcEnvAndEndpoint()//启动master
        receivers.offer(data) //receivers:LinkedBlockingQueue[EndpointData]
        MessageLoop.run()   //线程中消费receivers的信息
            //消费线程根据消息做相应处理,这里调用master的onStart方法
            endpoint.onStart()

Master.onStart()
    new MasterWebUI(this, webUiPort).bind()//启动master UI
    //起一个线程给自己定时发送CheckForWorkerTimeOut信号去掉heartbeat超时的Worker
    Runnable.run(){self.send(CheckForWorkerTimeOut)}    
    //根据RECOVERY_MODE(ZOOKEEPER/FILESYSTEM/CUSTOM)，来生成持久化引擎persistenceEngine和选举代理 leaderElectionAgent
    //生成zk目录,保存app,driver和worker信息
    zkFactory.createPersistenceEngine()
    zkFactory.createLeaderElectionAgent(this)
        //启动leader的竞争与选举
        new LeaderLatch(zk, WORKING_DIR).start()

//Master的角色发生变化时会受到通知，Master的electedLeader()方法或者revokedLeadership()方法会被调用
Master.electedLeader()
    self.send(ElectedLeader)//给自己发送ElectedLeader

receive()
    case ElectedLeader
        //去取app,driver和worker信息
        zk.getChildren.forPath(WORKING_DIR).asScala.filter(_.startsWith(prefix)).flatMap(deserializeFromFile[T])
        registerApplication(app)    //重新注册所有从zk上读取到的app
        driver.send(MasterChanged(self, masterWebUiUrl))    //通知driver maste改变
        self.send(CompleteRecovery)//定时给自己发送CompleteRecovery信息
    
    case CompleteRecovery      
        workers.filter & apps.filter & drivers.filter //过滤/移除无效组件
        schedule()
            launchDriver()
            startExecutorsOnWorkers()
```

### worker

```scala
start-slaves.sh //读取slaves文件,启动worker

//worker.scala 启动流程与master一致
main()
    new WorkerArguments(argStrings, conf) //读取配置
    startRpcEnvAndEndpoint()    //启动worker
        receivers.offer(data) //receivers:LinkedBlockingQueue[EndpointData]
        MessageLoop.run()   //线程中消费receivers的信息
            //消费线程根据消息做相应处理,这里调用worker的onStart方法
            endpoint.onStart()

worker.onStart()
    new ExternalShuffleService().startIfEnabled() //新进程,对其他计算节点提供本节点上面的所有shuffle map输出
    new WorkerWebUI(this, workDir, webUiPort).bind() //web ui
    registerWithMaster()
        tryRegisterAllMasters()
            new Runnable {run(){sendRegisterMessageToMaster()}} //给master发送注册信息
        new Runnable {run(){Option(self).foreach(_.send(RegisterWorker))}} //定时给自己发信息注册worker
            cancelLastRegistrationRetry() //已注册就取消定时注册线程任务
            sendRegisterMessageToMaster(RegisterWorker) //否则在重试次数内继续异步发送注册信息

//master.scala
receive()
     case RegisterWorker
        workerRef.send(MasterInStandby) //master在Standby状态下直接返回信息,worker暂时不执行操作
        workerRef.send(RegisterWorkerFailed("Duplicate worker ID")) //通知重复注册了
        persistenceEngine.addWorker(new WorkerInfo) //将worker信息持久化(如zookeeper)
        workerRef.send(RegisteredWorker()) //通知worker注册成功
        schedule() //重新调度,分配资源
        
//worker.scala
 case RegisteredWorker
    changeMaster() //更新master信息
    self.send(SendHeartbeat) //线程异步给自己发送SendHeartbeat信号
        //转发给master
        sendToMaster(Heartbeat(workerId, self) 
master=>    workerInfo.lastHeartbeat = System.currentTimeMillis()
master=>    worker.send(ReconnectWorker(masterUrl)) //worker重启/新加入就会在master中找不到信息,重新注册
    self.send(WorkDirCleanup) //如果开启了spark.worker.cleanup.enabled=true,则会发信息清除非running状态的app的工作目录
    //通知masterworker当前状态,master收到后会一一比对信息
    //若Executor/driver不一致,就返回信息让worker杀掉对应Executor/driver
    masterRef.send(WorkerLatestState)
```

## spark-提交流程

```scala
spark-submit
    exec "${SPARK_HOME}"/bin/spark-class org.apache.spark.deploy.SparkSubmit "$@"
        SparkSubmit.main()
             submit()   //这里按脚本可以调用kill,requestStatus等方法
                //反射调用自定义类的main方法
                mainClass.getMethod("main", new Array[String](0).getClass).invoke(null, childArgs.toArray)
                
//自定义类main方法内sparkSession会新建SparkContext
sparkSession.getOrCreate(SparkContext.getOrCreate(sparkConf))
    val (backend, ts) = SparkContext.createTaskScheduler(this, master, deployMode)
        //这里会根据spark-submit的 master参数选择模式,以standalone为例
        new StandaloneSchedulerBackend(new TaskSchedulerImpl(sc), sc, masterUrls)
        TaskSchedulerImpl.initialize()  //初始化任务调度池FIFO/FAIR
    ts.start() 
        backend.start()
            new StandaloneAppClient().start()
                new ClientEndpoint().onStart()  // 启动异步的netty RPC通信
                    tryRegisterAllMasters()     
                        registerMasterThreadPool.submit(new Runnable {run(masterRef.send(RegisterApplication(appDescription, self)))})
        //推测执行
        speculationScheduler.scheduleWithFixedDelay(new Runnable { run(checkSpeculatableTasks())}
    
//Master接收注册app信息
receive()
     case RegisterApplication  
        //返回通知已注册app
         driver.send(RegisteredApplication(app.id, self))
        schedule()  //资源调度
            launchDriver(worker, driver)  //启动Driver 内部实现是发送启动Driver命令给指定Worker，Worker来启动Driver(master向worker发送LaunchDriver消息)
                worker.new DriverRunner().start()  //worker收到信息启动DriverRunner
            startExecutorsOnWorkers()
                //计算出对应worker需要分配的cores,spreadOutApps是否集中非配executor
                scheduleExecutorsOnWorkers(app, usableWorkers, spreadOutApps)
                allocateWorkerResourceToExecutors() //真正分配executor
                    launchExecutor()
                        worker.endpoint.send(LaunchExecutor())
                            Worker.new ExecutorRunner().start() //worker收到信息启动ExecutorRunner
                        exec.application.driver.send(ExecutorAdded())
          
//StandaloneAppClient接收注册成功信息
receive()
    case RegisteredApplication()
        //记录已注册该app
```

## Job 例子

使用 Spark 自带的 examples 包中的 GroupByTest，假设在 Master 节点运行，命令是

```bash
/* Usage: GroupByTest [numMappers] [numKVPairs] [valSize] [numReducers] */
bin/run-example GroupByTest 100 10000 1000 36
```

GroupByTest 具体代码如下

```scala
package org.apache.spark.examples
import java.util.Random
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
/**
  * Usage: GroupByTest [numMappers] [numKVPairs] [valSize] [numReducers]
  */
object GroupByTest {
  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("GroupBy Test")
    var numMappers = 100
    var numKVPairs = 10000
    var valSize = 1000
    var numReducers = 36

    val sc = new SparkContext(sparkConf)

    val pairs1 = sc.parallelize(0 until numMappers, numMappers).flatMap { p =>
      val ranGen = new Random
      var arr1 = new Array[(Int, Array[Byte])](numKVPairs)
      for (i <- 0 until numKVPairs) {
        val byteArr = new Array[Byte](valSize)
        ranGen.nextBytes(byteArr)
        arr1(i) = (ranGen.nextInt(Int.MaxValue), byteArr)
      }
      arr1
    }.cache
    // Enforce that everything has been calculated and in cache
    pairs1.count

    println(pairs1.groupByKey(numReducers).count)

    sc.stop()
  }
}
```

阅读代码后，用户头脑中 job 的执行流程是这样的：

![deploy](images/163021694745c320)

具体流程很简单，这里来估算下 data size 和执行结果：

1. 初始化 SparkConf()。
2. 初始化 numMappers=100, numKVPairs=10,000, valSize=1000, numReducers= 36。
3. 初始化 SparkContext。这一步很重要，是要确立 driver 的地位，里面包含创建 driver 所需的各种 actors 和 objects。

> spark 2.x 用的是基于Netty的Rpc通信方式,Akka由于不适用package/stream的数据传输而弃用了,见[Spark为何使用Netty通信框架替代Akka](http://www.aboutyun.com/thread-21115-1-1.html)

1. 每个 mapper 生成一个 `arr1: Array[(Int, Byte[])]`，length 为 numKVPairs。每一个 Byte[] 的 length 为 valSize，Int 为随机生成的整数。`Size(arr1) = numKVPairs * (4 + valSize) = 10MB`，所以 `Size(pairs1) = numMappers * Size(arr1) ＝1000MB`。这里的数值计算结果都是*约等于*。
2. 每个 mapper 将产生的 arr1 数组 cache 到内存。
3. 然后执行一个 action 操作 count()，来统计所有 mapper 中 arr1 中的元素个数，执行结果是 `numMappers * numKVPairs = 1,000,000`。这一步主要是为了将每个 mapper 产生的 arr1 数组 cache 到内存。
4. 在已经被 cache 的 paris1 上执行 groupByKey 操作，groupByKey 产生的 reducer （也就是 partition） 个数为 numReducers。理论上，如果 hash(Key) 比较平均的话，每个 reducer 收到的 <Int, Array[Byte]> record 个数为 `numMappers * numKVPairs / numReducer ＝ 27,777`，大小为 `Size(pairs1) / numReducer = 27MB`。
5. reducer 将收到的 `<Int, Byte[]>` records 中拥有相同 Int 的 records 聚在一起，得到 `<Int, list(Byte[], Byte[], ..., Byte[])>`。
6. 最后 count 将所有 reducer 中 records 个数进行加和，最后结果实际就是 pairs1 中不同的 Int 总个数。

### Job 逻辑执行图

Job 的实际执行流程比用户头脑中的要复杂，需要先建立逻辑执行图（或者叫数据依赖图），然后划分逻辑执行图生成 DAG 型的物理执行图，然后生成具体 task 执行。分析一下这个 job 的逻辑执行图：

使用 `RDD.toDebugString` 可以看到整个 logical plan （RDD 的数据依赖关系）如下

```scala
  MapPartitionsRDD[3] at groupByKey at GroupByTest.scala:51 (36 partitions)
    ShuffledRDD[2] at groupByKey at GroupByTest.scala:51 (36 partitions)
      FlatMappedRDD[1] at flatMap at GroupByTest.scala:38 (100 partitions)
        ParallelCollectionRDD[0] at parallelize at GroupByTest.scala:38 (100 partitions)
```

用图表示就是：

![deploy](images/1630217645a335b4)

> 需要注意的是 data in the partition 展示的是每个 partition 应该得到的计算结果，并不意味着这些结果都同时存在于内存中。

根据上面的分析可知：

- 用户首先 init 了一个0-99 的数组： `0 until numMappers`
- parallelize() 产生最初的 ParrallelCollectionRDD，每个 partition 包含一个整数 i。
- 执行 RDD 上的 transformation 操作（这里是 flatMap）以后，生成 FlatMappedRDD，其中每个 partition 包含一个 Array[(Int, Array[Byte])]。
- 第一个 count() 执行时，先在每个 partition 上执行 count，然后执行结果被发送到 driver，最后在 driver 端进行 sum。

```scala
/**
 * RDD.scala
 * Return the number of elements in the RDD.
 * Utils.getIteratorSize _:是在partition中执行的
 */
def count(): Long = sc.runJob(this, Utils.getIteratorSize _).sum
```

- 由于 FlatMappedRDD 被 cache 到内存，因此这里将里面的 partition 都换了一种颜色表示。
- groupByKey 产生了后面两个 RDD，为什么产生这两个在后面章节讨论。
- 如果 job 需要 shuffle，一般会产生 ShuffledRDD。该 RDD 与前面的 RDD 的关系类似于 Hadoop 中 mapper 输出数据与 reducer 输入数据之间的关系。
- MapPartitionsRDD 里包含 groupByKey() 的结果。
- 最后将 MapPartitionsRDD 中的每个value（也就是Array[Byte]）都转换成 Iterable 类型。
- 最后的 count 与上一个 count 的执行方式类似。

**可以看到逻辑执行图描述的是 job 的数据流：job 会经过哪些 transformation()，中间生成哪些 RDD 及 RDD 之间的依赖关系。**

### Job 物理执行图

逻辑执行图表示的是数据上的依赖关系，不是 task 的执行图。在 Hadoop 中，用户直接面对 task，mapper 和 reducer 的职责分明：一个进行分块处理，一个进行 aggregate。Hadoop 中整个数据流是固定的，只需要填充 map() 和 reduce() 函数即可。Spark 面对的是更复杂的数据处理流程，数据依赖更加灵活，很难将数据流和物理 task 简单地统一在一起。因此 Spark 将数据流和具体 task 的执行流程分开，并设计算法将逻辑执行图转换成 task 物理执行图，转换算法后面的章节讨论。

针对这个 job，我们先画出它的物理执行 DAG 图如下：

![deploy](images/1630217fb6bf8099)

可以看到 GroupByTest 这个 application 产生了两个 job，第一个 job 由第一个 action（也就是 `pairs1.count`）触发产生，分析一下第一个 job：

- 整个 job 只包含 1 个 stage（不明白什么是stage没关系，后面章节会解释，这里只需知道有这样一个概念）。
- Stage 0 包含 100 个 ResultTask。
- 每个 task 先计算 flatMap，产生 FlatMappedRDD，然后执行 action() 也就是 count()，统计每个 partition 里 records 的个数，比如 partition 99 里面只含有 9 个 records。
- 由于 pairs1 被声明要进行 cache，因此在 task 计算得到 FlatMappedRDD 后会将其包含的 partitions 都 cache 到 executor 的内存。
- task 执行完后，driver 收集每个 task 的执行结果，然后进行 sum()。
- job 0 结束。

第二个 job 由 `pairs1.groupByKey(numReducers).count` 触发产生。分析一下该 job：

- 整个 job 包含 2 个 stage。
- Stage 1 包含 100 个 ShuffleMapTask，每个 task 负责从 cache 中读取 pairs1 的一部分数据并将其进行类似 Hadoop 中 mapper 所做的 partition，最后将 partition 结果写入本地磁盘。
- Stage 0 包含 36 个 ResultTask，每个 task 首先 shuffle 自己要处理的数据，边 fetch 数据边进行 aggregate 以及后续的 mapPartitions() 操作，最后进行 count() 计算得到 result。
- task 执行完后，driver 收集每个 task 的执行结果，然后进行 sum()。
- job 1 结束。

可以看到物理执行图并不简单。与 MapReduce 不同的是，Spark 中一个 application 可能包含多个 job，每个 job 包含多个 stage，每个 stage 包含多个 task。**怎么划分 job，怎么划分 stage，怎么划分 task 等等问题会在后面的章节介绍。**

## 参考文档

1. [SparkInternals](https://github.com/JerryLead/SparkInternals/blob/master/markdown/1-Overview.md)
2. [Spark Executor Driver资源调度小结](https://blog.csdn.net/oopsoom/article/details/38763985)
3. [Spark为何使用Netty通信框架替代Akka](http://www.aboutyun.com/thread-21115-1-1.html)
4. [深入理解Spark 2.1 原理与源码分析](https://blog.csdn.net/column/details/14162.html)