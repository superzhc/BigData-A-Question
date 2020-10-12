# Spark 任务调度

## spark 任务调度源码解析

之前的资源调度源码已经跟进到了 Executor 创建完毕，并且在 Executor 中创建了 ThreadPool，此时就是开始了执行调度，我们随机找一个 Action 算子 `count()` 来分析源码：

```scala
def count(): Long = sc.runJob(this, Utils.getIteratorSize _).sum
```

sc 是 SparkContext 的对象，进入 runJob 方法：

```scala
def runJob[T, U: ClassTag](rdd: RDD[T], func: Iterator[T] => U): Array[U] = {
  runJob(rdd, func, 0 until rdd.partitions.length)
}

def runJob[T, U: ClassTag](
    rdd: RDD[T],
    func: Iterator[T] => U,
    partitions: Seq[Int]): Array[U] = {
  val cleanedFunc = clean(func)
  runJob(rdd, (ctx: TaskContext, it: Iterator[T]) => cleanedFunc(it), partitions)
}

def runJob[T, U: ClassTag](
   rdd: RDD[T],
   func: (TaskContext, Iterator[T]) => U,
   partitions: Seq[Int]): Array[U] = {
   val results = new Array[U](partitions.size)
   runJob[T, U](rdd, func, partitions, (index, res) => results(index) = res)
   results
}

def runJob[T, U: ClassTag](
    rdd: RDD[T],
    func: (TaskContext, Iterator[T]) => U,
    partitions: Seq[Int],
    resultHandler: (Int, U) => Unit): Unit = {
  if (stopped.get()) {
    throw new IllegalStateException("SparkContext has been shutdown")
  }
  val callSite = getCallSite
  val cleanedFunc = clean(func)
  logInfo("Starting job: " + callSite.shortForm)
  if (conf.getBoolean("spark.logLineage", false)) {
    logInfo("RDD's recursive dependencies:\n" + rdd.toDebugString)
  }
  dagScheduler.runJob(rdd, cleanedFunc, partitions, callSite, resultHandler, localProperties.get)
  progressBar.foreach(_.finishAll())
  rdd.doCheckpoint()
}
```

进入方法后继续跟进 rdd 这个参数，在 submitJob 中调用，跟进 submitJob 方法中的 rdd 参数在何时使用：

```scala
//提交任务。eventProcessLoop 是 DAGSchedulerEventProcessLoop 对象
eventProcessLoop.post(JobSubmitted(
  jobId, rdd, func2, partitions.toArray, callSite, waiter,
  SerializationUtils.clone(properties)))
```

rdd 传进了 eventProcessLoop.post 方法，进入 post 方法，该方法内部调用了 eventQueue.put(event)，功能是将

提交的任务放入队列：

```scala
//将提交的任务放入队列
def post(event: E): Unit = {
  eventQueue.put(event)
}
```

观察 eventQueue 类型：

private val eventQueue: BlockingQueue[E] = new LinkedBlockingDeque[E]()
eventQueue 存在于 EventLoop 类中，该类有一个 run() 方法，内部逻辑是处理刚刚被 eventProcessLoop.post 进去的新的任务，处理逻辑被封装到 onReceive(event) 方法中，进入该方法无法看到代码逻辑，此时去找 eventProcessLoop 的父类，跟进代码后发现 eventProcessLoop 是 DAGSchedulerEventProcessLoop 实例对象：

private[scheduler] val eventProcessLoop = new DAGSchedulerEventProcessLoop(this)
在 DAGSchedulerEventProcessLoop 类中找到 onReceive 方法，观察逻辑：

override def onReceive(event: DAGSchedulerEvent): Unit = {
  val timerContext = timer.time()
  try {
    doOnReceive(event)
  } finally {
    timerContext.stop()
  }
}
在 doOnReceive(event) 中运行 handleJobSubmitted 方法：

// 运行handleJobSubmitted 方法
dagScheduler.handleJobSubmitted(jobId, rdd, func, partitions, callSite, listener, properties)
在 handleJobSubmitted 方法中通过 submitStage(finalStage) 根据宽窄依赖递归寻找 Stage 并提交：

```scala
//递归寻找stage
private def submitStage(stage: Stage) {
  val jobId = activeJobForStage(stage)
  if (jobId.isDefined) {
    logDebug("submitStage(" + stage + ")")
    if (!waitingStages(stage) && !runningStages(stage) && !failedStages(stage)) {
      val missing = getMissingParentStages(stage).sortBy(_.id)
      logDebug("missing: " + missing)
      if (missing.isEmpty) {
        logInfo("Submitting " + stage + " (" + stage.rdd + "), which has no missing parents")
        //最终会执行 submitMissingTasks 方法
        submitMissingTasks(stage, jobId.get)
      } else {
        for (parent <- missing) {
          submitStage(parent)
        }
        waitingStages += stage
      }
    }
  } else {
    abortStage(stage, "No active job for stage " + stage.id, None)
  }
}
```

划分完 Stage 之后，submitMissingTasks(stage, jobId.get) 方法会将 stage 划分成 task 发送到 Exeuctor 中执行，并以以 TaskSet 形式提交任务：

//以taskSet形式提交任务
taskScheduler.submitTasks(new TaskSet(
  tasks.toArray, stage.id, stage.latestInfo.attemptNumber, jobId, properties))
提交task, 最后会执行 backend.reviveOffers() 调用的是 CoarseGrainedSchedulerBackend 对象中的方法：

backend.reviveOffers()
给 Driver 提交 task，在当前类中的 DriverEndpoint 中有 receive 方法来接收数据：

override def reviveOffers() {
  //给Driver 提交task,在当前类中的DriverEndpoint中 有receive方法来接收数据
  driverEndpoint.send(ReviveOffers)
}
找到 makeOffers()：

case ReviveOffers =>
    makeOffers()
在该方法内部会去执行 launchTasks(taskDescs) 就是去 Executor 中启动Task：

private def makeOffers() {
  // Make sure no executor is killed while some task is launching on it
  val taskDescs = CoarseGrainedSchedulerBackend.this.synchronized {
    // Filter out executors under killing
    val activeExecutors = executorDataMap.filterKeys(executorIsAlive)
    val workOffers = activeExecutors.map {
      case (id, executorData) =>
        new WorkerOffer(id, executorData.executorHost, executorData.freeCores)
    }.toIndexedSeq
    scheduler.resourceOffers(workOffers)
  }
  if (!taskDescs.isEmpty) {
    //去Executor中启动Task
    launchTasks(taskDescs)
  }
}
在 launchTasks(taskDescs) 中就是给 Executor 发送 task 执行:

executorData.executorEndpoint.send(LaunchTask(new SerializableBuffer(serializedTask)))
在 CoarseGrainedExecutorBackend 中会有receive方法匹配 LaunchTask：

//启动Task
case LaunchTask(data) =>
  if (executor == null) {
    exitExecutor(1, "Received LaunchTask command but executor was null")
  } else {
    val taskDesc = TaskDescription.decode(data.value)
    logInfo("Got assigned task " + taskDesc.taskId)
    //Executor 启动Task
    executor.launchTask(this, taskDesc)
  }
得到 task，在线程池中之行：

// 得到task 在线程池中执行
def launchTask(context: ExecutorBackend, taskDescription: TaskDescription): Unit = {
  val tr = new TaskRunner(context, taskDescription)
  runningTasks.put(taskDescription.taskId, tr)
  threadPool.execute(tr)
}