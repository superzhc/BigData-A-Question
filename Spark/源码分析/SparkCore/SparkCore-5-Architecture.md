# SparkCore-Architecture

# 架构

前三章从 job 的角度介绍了用户写的 program 如何一步步地被分解和执行。这一章主要从架构的角度来讨论 master，worker，driver 和 executor 之间怎么协调来完成整个 job 的运行。

> 实在不想在文档中贴过多的代码，这章贴这么多，只是为了方面自己回头 debug 的时候可以迅速定位，不想看代码的话，直接看图和描述即可。

## 部署图

重新贴一下 Overview 中给出的部署图：



![deploy](images/1638803e1229f6bc)



接下来分阶段讨论并细化这个图。

## Job 提交

下图展示了driver program（假设在 master node 上运行）如何生成 job，并提交到 worker node 上执行。



![JobSubmission](images/163880c2079238f8)



Driver 端的逻辑如果用代码表示：

```
finalRDD.action()
=> sc.runJob()

// generate job, stages and tasks
=> dagScheduler.runJob()
=> dagScheduler.submitJob()
//将任务提交JobSubmitted放置在event队列当中，eventThread后台线程将对该任务提交进行处理
=>    dagSchedulerEventProcessLoop.post(JobSubmitted)

//实际上并不是接收外部消息,而是线程从阻塞队列里获取结果并匹配执行
DAGSchedulerEventProcessLoop.onReceive()
=>  dagScheduler.handleJobSubmitted(jobId, ...)
=>      finalStage = createResultStage(finalRDD...)
            getShuffleDependencies()
                //用stack做深度优先遍历
                toVisit.dependencies.foreach {
                  //如果是ShuffleDependency,则为parent
                  case shuffleDep: ShuffleDependency[_, _, _] =>
                    parents += shuffleDep
                    //同一个stage,加入寻找队列,要一直找到所有的parent stage
                  case dependency =>
                    waitingForVisit.push(dependency.rdd)
                }
              getOrCreateShuffleMapStage()    
                 createShuffleMapStage()
                    new ShuffleMapStage()
                    mapOutputTracker.registerShuffle(shuffleDep.shuffleId, rdd.partitions.length)
=>      submitStage(finalStage)
复制代码
//父stage就递归调用,若没有父stage就提交当前stage,这种逻辑就要找到顶层stage提交
dagScheduler.submitStage(stage: Stage)
    //获取父stage,如果 parentStages 都可能已经执行过了，那么就为空了
    val missing = getMissingParentStages(stage) 
        //stage 划分算法,基于stack操作,宽依赖创建ShuffleMapStage,
        case shufDep: ShuffleDependency[_, _, _] => getOrCreateShuffleMapStage(shufDep, stage.firstJobId)
        //窄依赖加入stack,一定要追溯到其依赖的ShuffleDependency/none
        case narrowDep: NarrowDependency[_] =>  waitingForVisit.push(narrowDep.rdd)
    submitMissingTasks(stage, jobId.get)//没有父stage就提交task
    submitStage(parent)//若有父stage就递归提交所有父stage,然后再提交子stage
复制代码
dagScheduler.submitMissingTasks(stage: Stage, jobId: Int)
    taskBinary = sc.broadcast(taskBinaryBytes)//广播task
    //按照stage的类型,生成相应的ShuffleMapTask/ResultTask
    val tasks: Seq[Task[_]] = new ShuffleMapTask / new ResultTask
    //提交taskset
=>  taskScheduler.submitTasks(new TaskSet(...))
       val manager = createTaskSetManager(taskSet, maxTaskFailures)
       //将TaskSetManager加入rootPool调度池中，由schedulableBuilder决定调度顺序
       schedulableBuilder.addTaskSetManager(manager, manager.taskSet.properties)
       //调用SchedulerBackend的reviveOffers方法对Task进行调度，决定task具体运行在哪个Executor中
       //对Task进行调度，决定task具体运行在哪个Executor中
=>      schedulerBackend.reviveOffers()
          driverEndpoint.send(ReviveOffers) //给driver发送调度信息

//driver端调度执行task          
CoarseGrainedSchedulerBackend.receive()
    case ReviveOffers =>
=>      makeOffers()
            //考虑 locality 等因素来确定 task 的全部信息 TaskDescription
            scheduler.resourceOffers(workOffers)
=>          launchTasks()
                 //向executor发送消息,在executor上启动task
                 foreach task 
                    executorEndpoint.send(LaunchTask())
复制代码
```

代码的文字描述：

当用户的 program 调用 `val sc = new SparkContext(sparkConf)` 时，这个语句会帮助 program 启动诸多有关 driver 通信、job 执行的对象、线程等，**该语句确立了 program 的 driver 地位。**

### 生成 Job 逻辑执行图

Driver program 中的 transformation() 建立 computing chain（一系列的 RDD），每个 RDD 的 compute() 定义数据来了怎么计算得到该 RDD 中 partition 的结果，getDependencies() 定义 RDD 之间 partition 的数据依赖。

### 生成 Job 物理执行图

每个 action() 触发生成一个 job，在 dagScheduler.runJob() 的时候进行 stage 划分，在 submitStage() 的时候生成该 stage 包含的具体的 ShuffleMapTasks 或者 ResultTasks，然后将 tasks 打包成 TaskSet 交给 taskScheduler，如果 taskSet 可以运行就将 tasks 交给 CoarseGrainedSchedulerBackend 去分配执行。

### 分配 Task

CoarseGrainedSchedulerBackend 接收到 taskSet 后，会通 将 serialized tasks 发送到调度器指定的 worker node 上的 CoarseGrainedExecutorBackend Endpoint上。

## Job 接收

Worker 端接收到 tasks 后，执行如下操作

```
//driver端先发LaunchTask信息给executor
executorEndpoint.send(LaunchTask(serializedTask)
=> executor.launchTask()
    // Executors.newCachedThreadPool,用的是无界队列
=> executor.threadPool.execute(new TaskRunner(context, taskDescription))
复制代码
```

**executor 将 task 包装成 taskRunner，并从线程池中抽取出一个空闲线程运行 task。一个 CoarseGrainedExecutorBackend 进程有且仅有一个 executor 对象。**

## Task 运行

下图展示了 task 被分配到 worker node 上后的执行流程及 driver 如何处理 task 的 result。



![TaskExecution](images/16388129748a08c9)



Executor 收到 serialized 的 task 后，先 deserialize 出正常的 task，然后运行 task 得到其执行结果 directResult，这个结果要送回到 driver 那里。但是通过 Actor 发送的数据包不宜过大，**如果 result 比较大（比如 groupByKey 的 result）先把 result 存放到本地的“内存＋磁盘”上，由 blockManager 来管理，只把存储位置信息（indirectResult）发送给 driver**，driver 需要实际的 result 的时候，会通过 HTTP 去 fetch。如果 result 不大（小于`spark.akka.frameSize = 10MB`），那么直接发送给 driver。

上面的描述还有一些细节：如果 task 运行结束生成的 directResult > akka.frameSize，directResult 会被存放到由 blockManager 管理的本地“内存＋磁盘”上。**BlockManager 中的 memoryStore 开辟了一个 LinkedHashMap 来存储要存放到本地内存的数据**。LinkedHashMap 存储的数据总大小不超过 `Runtime.getRuntime.maxMemory * spark.storage.memoryFraction(default 0.6)` 。如果 LinkedHashMap 剩余空间不足以存放新来的数据，就将数据交给 diskStore 存放到磁盘上，但前提是该数据的 storageLevel 中包含“磁盘”。

> 这里directResult传输的大小标准已经改为Math.min(("spark.rpc.message.maxSize", 128m),("spark.task.maxDirectResultSize", 1L << 20))

> 上文的内存空间管理需要再验证

```
In TaskRunner.run()
=> coarseGrainedExecutorBackend.statusUpdate(TaskState.RUNNING)
        driverRef.send(StatusUpdate)
=> updateDependencies(addedFiles, addedJars) //下载依赖资源
=> task = ser.deserialize(serializedTask)
=> value = task.run(taskId) //会调用子类ShuffleMapTask/ResultTask的runTask方法
=> directResult = new DirectTaskResult(ser.serialize(value),accumUpdates)   //Accumulator
=> if( resultSize > maxResultSize )  //默认maxResultSize是1G
       //IndirectTaskResult是woker BlockManager中存储DirectTaskResult的引用
        ser.serialize(new IndirectTaskResult[Any](TaskResultBlockId(taskId), resultSize))
   else if (resultSize > maxDirectResultSize) {  //若task返回结果大于128M(默认的rpc传输消息大小) < 1G
        ser.serialize(new IndirectTaskResult[Any](blockId, resultSize))
    }else{
        ser.serialize(directResult)
    }
=> coarseGrainedExecutorBackend.statusUpdate(TaskState.FINISHED,result)
=>      driverRef.send(StatusUpdate,result)
复制代码
```

ShuffleMapTask 和 ResultTask 生成的 result 不一样。**ShuffleMapTask 生成的是 MapStatus**，MapStatus 包含两项内容：一是该 task 所在的 BlockManager 的 BlockManagerId（实际是 executorId + host, port, nettyPort），二是 task 输出的每个 FileSegment 大小。**ResultTask 生成的 result 的是 func 在 partition 上的执行结果**。比如 count() 的 func 就是统计 partition 中 records 的个数。由于 ShuffleMapTask 需要将 FileSegment 写入磁盘，因此需要输出流 writers，这些 writers 是由 blockManger 里面的 shuffleBlockManager 产生和控制的。

```
In task.run(taskId) //会调用子类的runTask方法
// if the task is ShuffleMapTask
=> shuffleMapTask.runTask(context)
=> shuffleManager.getWriter.write(rdd.iterator(partition, context))
//MapStatus包含了task将shuffle文件的写入地址
=> return MapStatus(blockManager.blockManagerId, Array[compressedSize(fileSegment)])

//If the task is ResultTask,直接运行
=> return func(context, rdd.iterator(split, context))
复制代码
```

Driver 收到 task 的执行结果 result 后会进行一系列的操作：首先告诉 taskScheduler 这个 task 已经执行完，然后去分析 result。由于 result 可能是 indirectResult，需要先调用 blockManager.getRemoteBytes() 去 fech 实际的 result，这个过程下节会详解。得到实际的 result 后，需要分情况分析，**如果是 ResultTask 的 result，那么可以使用 ResultHandler 对 result 进行 driver 端的计算（比如 count() 会对所有 ResultTask 的 result 作 sum）**，如果 result 是 ShuffleMapTask 的 MapStatus，那么需要将 MapStatus（ShuffleMapTask 输出的 FileSegment 的位置和大小信息）**存放到 mapOutputTrackerMaster 中的 mapStatuses 数据结构中以便以后 reducer shuffle 的时候查询**。如果 driver 收到的 task 是该 stage 中的最后一个 task，那么可以 submit 下一个 stage，如果该 stage 已经是最后一个 stage，那么告诉 dagScheduler job 已经完成。

```
After driver receives StatusUpdate(result)
=> taskSchedulerImpl.statusUpdate(taskId, state, result.value)
//TaskState.isFinished(state) && state == TaskState.FINISHED
=> taskResultGetter.enqueueSuccessfulTask(taskSet, tid, result)
        TaskResultExecutor.execute.(new Runnable().run())
            if result is directResult
                directResult.value(serializer)
            if result is IndirectResult
                serializedTaskResult = blockManager.getRemoteBytes(blockId)
=>          taskSchedulerImpl.handleSuccessfulTask(taskSetManager, tid, result)
                //Marks a task as successful and notifies the DAGScheduler that the task has ended.
=>              taskSetManager.handleSuccessfulTask(tid, taskResult)
                    sched.backend.killTask() //杀死所有其他与之相同的task的尝试
                    //通知dagScheduler该task完成
=>                  dagScheduler.taskEnded(result.value, result.accumUpdates)
                        eventProcessLoop.post(CompletionEvent)  //起线程处理的

dagScheduler.doOnReceive()
    dagScheduler.handleTaskCompletion(completion)
=>      if task Success
            if task is ResultTask
                updateAccumulators(event)
                if (job.numFinished == job.numPartitions) 
                    markStageAsFinished(resultStage)
                    //Removes state for job and any stages that are not needed by any other job
                    cleanupStateForJobAndIndependentStages(job)
                    listenerBus.post(SparkListenerJobEnd(job.jobId, JobSucceeded))
                job.listener.taskSucceeded(outputId, result)//通知 JobWaiter 有任务成功
                    jobWaiter.taskSucceeded(index, result)
                    resultHandler(index, result)

             if task is ShuffleMapTask
                updateAccumulators(event)
                 shuffleStage.pendingPartitions -= task.partitionId
                 shuffleStage.addOutputLoc(smt.partitionId, mapStatus)
                if (all tasks in current stage have finished)
                    mapOutputTracker.registerMapOutputs(shuffleId, Array[MapStatus])
                        mapStatuses.put(shuffleId, Array[MapStatus])
=>              submitWaitingChildStages(stage)
                    waitingStages.filter(_.parents.contains(parent)).foreach.submitStage(_)
        
        //补充下其他可能的情况            
=>      if task Resubmitted      
            pendingPartitions += task.partitionId //TaskSetManagers只对ShuffleMapStage Resubmitted
            
=>      if task FetchFailed
            if fail times > (spark.stage.maxConsecutiveAttempts,4)
                abortStage(failedStage, abortMessage)
            else new Runnable.run(){eventProcessLoop.post(ResubmitFailedStages)}
            handleExecutorLost(executorId)//有多次fetch failures 就标记executor丢失
                blockManagerMaster.removeExecutor(execId)
                foreach ShuffleMapStage in executor
                    stage.removeOutputsOnExecutor(execId)
                    mapOutputTracker.registerMapOutputs(shuffleId,Array[MapStatus])
        
=>      if task exceptionFailure  
            // 异常还要更新accumulator~~
             updateAccumulators(event)
复制代码
```

## Shuffle read

上一节描述了 task 运行过程及 result 的处理过程，这一节描述 reducer（需要 shuffle 的 task ）是如何获取到输入数据的。关于 reducer 如何处理输入数据已经在上一章的 shuffle read 中解释了。

**问题：reducer 怎么知道要去哪里 fetch 数据？**



![readMapStatus](images/163881a5e49b93fe)

reducer 首先要知道 parent stage 中 ShuffleMapTask 输出的 FileSegments 在哪个节点。**这个信息在 ShuffleMapTask 完成时已经送到了 driver 的 mapOutputTrackerMaster，并存放到了 mapStatuses: HashMap<stageId, Array[MapStatus]> 里面**，给定 stageId，可以获取该 stage 中 ShuffleMapTasks 生成的 FileSegments 信息 Array[MapStatus]，通过 Array(taskId) 就可以得到某个 task 输出的 FileSegments 位置（blockManagerId）及每个 FileSegment 大小。



当 reducer 需要 fetch 输入数据的时候，会首先调用 blockStoreShuffleFetcher 去获取输入数据（FileSegments）的位置。blockStoreShuffleFetcher 通过调用本地的 MapOutputTrackerWorker 去完成这个任务，MapOutputTrackerWorker 使用 mapOutputTrackerMasterActorRef 来与 mapOutputTrackerMasterActor 通信获取 MapStatus 信息。blockStoreShuffleFetcher 对获取到的 MapStatus 信息进行加工，提取出该 reducer 应该去哪些节点上获取哪些 FileSegment 的信息，这个信息存放在 blocksByAddress 里面。之后，blockStoreShuffleFetcher 将获取 FileSegment 数据的任务交给 basicBlockFetcherIterator。

```
rdd.iterator()
=> rdd(e.g., ShuffledRDD/CoGroupedRDD).compute()
=> SparkEnv.get.shuffleFetcher.fetch(shuffledId, split.index, context, ser)
=> blockStoreShuffleFetcher.fetch(shuffleId, reduceId, context, serializer)
=> statuses = MapOutputTrackerWorker.getServerStatuses(shuffleId, reduceId)

=> blocksByAddress: Seq[(BlockManagerId, Seq[(BlockId, Long)])] = compute(statuses)
=> basicBlockFetcherIterator = blockManager.getMultiple(blocksByAddress, serializer)
=> itr = basicBlockFetcherIterator.flatMap(unpackBlock)
复制代码
```



![blocksByAddress](images/163881be10a4f34c)



basicBlockFetcherIterator 收到获取数据的任务后，会生成一个个 fetchRequest，**每个 fetchRequest 包含去某个节点获取若干个 FileSegments 的任务**。图中展示了 reducer-2 需要从三个 worker node 上获取所需的白色 FileSegment (FS)。总的数据获取任务由 blocksByAddress 表示，要从第一个 node 获取 4 个，从第二个 node 获取 3 个，从第三个 node 获取 4 个。

为了加快任务获取过程，显然要将总任务划分为子任务（fetchRequest），然后为每个任务分配一个线程去 fetch。Spark 为每个 reducer 启动 5 个并行 fetch 的线程（Hadoop 也是默认启动 5 个）。由于 fetch 来的数据会先被放到内存作缓冲，因此一次 fetch 的数据不能太多，Spark 设定不能超过 `spark.reducer.maxSizeInFlight＝48MB`。**注意这 48MB 的空间是由这 5 个 fetch 线程共享的**，因此在划分子任务时，尽量使得 fetchRequest 不超过`48MB / 5 = 9.6MB`。如图在 node 1 中，Size(FS0-2) + Size(FS1-2) < 9.6MB 但是 Size(FS0-2) + Size(FS1-2) + Size(FS2-2) > 9.6MB，因此要在 t1-r2 和 t2-r2 处断开，所以图中有两个 fetchRequest 都是要去 node 1 fetch。**那么会不会有 fetchRequest 超过 9.6MB**？当然会有，如果某个 FileSegment 特别大，仍然需要一次性将这个 FileSegment fetch 过来。另外，如果 reducer 需要的某些 FileSegment 就在本节点上，那么直接进行 local read。最后，将 fetch 来的 FileSegment 进行 deserialize，将里面的 records 以 iterator 的形式提供给 rdd.compute()，整个 shuffle read 结束。

```
//Spark shuffle read for spark 2.x
ShuffledRDD.compute()
    BlockStoreShuffleReader.read()   //fetch数据
        // 通过消息发送获取 ShuffleMapTask 存储数据位置的元数据
=>       mapOutputTracker.getMapSizesByExecutorId(shuffleId, startPartition, endPartition)
            val statuses = getStatuses(shuffleId)  // 得到元数据Array[MapStatus]
            	// 从driver的MapOutputTrackerMasterEndpoint远程获取,实际上是另起线程,通过LinkedBlockingQueue发送消息
            	val fetchedBytes = askTracker[Array[Byte]](GetMapOutputStatuses(shuffleId))
            	// 返回格式为：Seq[BlockManagerId,Seq[(shuffle block id, shuffle block size)]]
            MapOutputTracker.convertMapStatuses(shuffleId, startPartition, endPartition, statuses)
         //设置每次传输的大小
         SparkEnv.get.conf.getSizeAsMb("spark.reducer.maxSizeInFlight", "48m") * 1024 * 1024
         //最大远程请求抓取block次数
         SparkEnv.get.conf.getInt("spark.reducer.maxReqsInFlight", Int.MaxValue)
=>       new ShuffleBlockFetcherIterator().initialize() 
            splitLocalRemoteBlocks()//划分本地和远程的blocks
                val targetRequestSize = math.max(maxBytesInFlight / 5, 1L)//每批次请求的最大字节数,运行5个请求并行
                address.executorId != blockManager.blockManagerId.executorId //若 executorId 与本 blockManagerId.executorId不同,则从远程获取
                //集合多个block,达到targetRequestSize批次大小才构建一次请求
                remoteRequests += new FetchRequest(address, curBlocks)
            fetchUpToMaxBytes()//发送远程请求获取blocks
                sendRequest(fetchRequests.dequeue())    //一个个发送请求
                    //请求数据太大,会写入磁盘shuffleFiles,否则不写入
=>                  shuffleClient.fetchBlocks(blockIds.toArray)
                        new OneForOneBlockFetcher().start() //发送fetch请求
            fetchLocalBlocks()// 获取本地的Blocks
                foreach block {blockManager.getBlockData(blockId)}
                    //如果是shuffle block.则获取经过shuffle的bolck
=>                  ExternalShuffleBlockResolver.getSortBasedShuffleBlockData()
                        //new file()的方式读取索引文件
                        File indexFile = getFile("shuffle_" + shuffleId + "_" + mapId + "_0.index")
                        File data = getFile("shuffle_" + shuffleId + "_" + mapId + "_0.data")
                        //FileSegmentManagedBuffer(data,offset,length)//从indexFile中获取文件offset和length,这里只要获取文件中的一部分数据
                            //通过管道流channel读取指定长度字节文件
                    //如果不是shuffle block,则从内存或者磁盘中直接读取
                    diskStore.getBytes(blockId)
                        //还是通过管道流的方式读取文件的指定字节内容
                        new FileInputStream(diskManager.getFile(new file(blockId.name))).getChannel()
                    memoryStore.getValues(blockId)
                        entries.get(blockId) //直接从LinkedHashMap[BlockId, MemoryEntry[_]]获取
         
         // 若dep已进行Map端合并,就直接用mergeCombiners替代mergeValue(已经mergeValue过了)
=>       aggregator.combineCombinersByKey(combinedKeyValuesIterator)
            new ExternalAppendOnlyMap[K, C, C](identity, mergeCombiners, mergeCombiners).insertAll(iter)
                //估算集合大小,遍历mapEntry,若内存不足就放磁盘
                maybeSpill(currentMap, estimatedSize)
                //直接在map上运算,update是个闭包,包含了mergeValue,createCombiner函数
                currentMap.changeValue(curEntry._1, update)
        // 若dep未进行Map端合并,还是需要对单个的vaule合并的
=>       aggregator.combineValuesByKey(keyValuesIterator) 
            new ExternalAppendOnlyMap[K, V, C](createCombiner, mergeValue, mergeCombiners).insertAll(iter)
        //对数据进行排序并写入内存缓冲区,若排序中计算结果超出阈值，则将其溢写到磁盘数据文件
=>       new ExternalSorter().insertAll(aggregatedIter) //如果需要排序keyOrdering
            //map对应需要shouldCombine(aggregator!=none),buffer是个key和value连续放的数组
            if (shouldCombine)  foreach record {map.changeValue((getPartition(key), update)}
            else foreach record{ buffer.insert(getPartition(key))
            maybeSpillCollection()  //超过阈值时写入磁盘
                maybeSpill(collection, estimateSize()) //使用采样方式估算出来的大小
                    spill(collection)//将内存中的集合spill到一个有序文件中
                        val spillFile = spillMemoryIteratorToDisk(inMemoryIterator)
                            //以批次将记录刷入文件
                        spills += spillFile //spill出来的文件集合
        
        //sorter.iterator触发了排序逻辑
        CompletionIterator[Product2[K, C], Iterator[Product2[K, C]]](sorter.iterator, sorter.stop())    
             ExternalSorter.merge() //合并一组排序文件,未写入,返回(partition,Iterator)
                    (0 until numPartitions).iterator.map { p =>
                         (p, mergeWithAggregation(mergeCombiners))  //需要聚合时,内部还是mergeSort
                         (p, mergeSort(ordering.get))    //需要排序时
                         (p, iterators.iterator.flatten)   //都不需要,直接返回
复制代码
```

下面再讨论一些细节问题：

**reducer 如何将 fetchRequest 信息发送到目标节点？目标节点如何处理 fetchRequest 信息，如何读取 FileSegment 并回送给 reducer？**

> 这部分spark 2中有改动,需要再对比



![fetchrequest](images/1638827cc87a9afc)



rdd.iterator() 碰到 ShuffleDependency 时会调用 BasicBlockFetcherIterator 去获取 FileSegments。BasicBlockFetcherIterator 使用 blockManager 中的 connectionManager 将 fetchRequest 发送给其他节点的 connectionManager。connectionManager 之间使用 NIO 模式通信。其他节点，比如 worker node 2 上的 connectionManager 收到消息后，会交给 blockManagerWorker 处理，blockManagerWorker 使用 blockManager 中的 diskStore 去本地磁盘上读取 fetchRequest 要求的 FileSegments，然后仍然通过 connectionManager 将 FileSegments 发送回去。如果使用了 FileConsolidation，diskStore 还需要 shuffleBlockManager 来提供 blockId 所在的具体位置。如果 FileSegment 不超过 `spark.storage.memoryMapThreshold=8KB` ，那么 diskStore 在读取 FileSegment 的时候会直接将 FileSegment 放到内存中，否则，会使用 RandomAccessFile 中 FileChannel 的内存映射方法来读取 FileSegment（这样可以将大的 FileSegment 加载到内存）。

当 BasicBlockFetcherIterator 收到其他节点返回的 serialized FileSegments 后会将其放到 fetchResults: Queue 里面，并进行 deserialization，所以 **fetchResults: Queue 就相当于在 Shuffle details 那一章提到的 softBuffer。**如果 BasicBlockFetcherIterator 所需的某些 FileSegments 就在本地，会通过 diskStore 直接从本地文件读取，并放到 fetchResults 里面。最后 reducer 一边从 FileSegment 中边读取 records 一边处理。

```
After the blockManager receives the fetch request

=> connectionManager.receiveMessage(bufferMessage)
=> handleMessage(connectionManagerId, message, connection)

// invoke blockManagerWorker to read the block (FileSegment)
=> blockManagerWorker.onBlockMessageReceive()
=> blockManagerWorker.processBlockMessage(blockMessage)
=> buffer = blockManager.getLocalBytes(blockId)
=> buffer = diskStore.getBytes(blockId)
=> fileSegment = diskManager.getBlockLocation(blockId)
=> shuffleManager.getBlockLocation()
=> if(fileSegment < minMemoryMapBytes)
     buffer = ByteBuffer.allocate(fileSegment)
   else
     channel.map(MapMode.READ_ONLY, segment.offset, segment.length)
复制代码
```

每个 reducer 都持有一个 BasicBlockFetcherIterator，一个 BasicBlockFetcherIterator 理论上可以持有 48MB 的 fetchResults。每当 fetchResults 中有一个 FileSegment 被读取完，就会一下子去 fetch 很多个 FileSegment，直到 48MB 被填满。

```
BasicBlockFetcherIterator.next()
=> result = results.task()
=> while (!fetchRequests.isEmpty &&
        (bytesInFlight == 0 || bytesInFlight + fetchRequests.front.size <= maxBytesInFlight)) {
        sendRequest(fetchRequests.dequeue())
      }
=> result.deserialize()
复制代码
```

## Discussion

这一章写了三天，也是我这个月来心情最不好的几天。Anyway，继续总结。

架构部分其实没有什么好说的，就是设计时尽量功能独立，模块独立，松耦合。BlockManager 设计的不错，就是管的东西太多（数据块、内存、磁盘、通信）。

这一章主要探讨了系统中各个模块是怎么协同来完成 job 的生成、提交、运行、结果收集、结果计算以及 shuffle 的。贴了很多代码，也画了很多图，虽然细节很多，但远没有达到源码的细致程度。如果有地方不明白的，请根据描述阅读一下源码吧。

如果想进一步了解 blockManager，可以参阅 Jerry Shao 写的 [Spark源码分析之-Storage模块](http://jerryshao.me/architecture/2013/10/08/spark-storage-module-analysis/)。