# Broadcast-广播变量

Broadcast 是将数据从一个节点发送到其他各个节点上去。

## 实现

### 1. 分发 task 的时候先分发 bdata 的元信息

Driver 先建一个本地文件夹用以存放需要 broadcast 的 data，并启动一个可以访问该文件夹的 HttpServer。当调用 `val bdata = sc.broadcast(data)` 时就把 data 写入文件夹，同时写入 Driver 的 blockManger 中（StorageLevel 为内存＋磁盘），获得一个 blockId，类型为 BroadcastBlockId。

```scala
//initialize
sparkSession.build()#env.broadcastManager.initialize()
    new TorrentBroadcastFactory.initialize()

//use broadcast
sc.broadcast()
    broadcastManager.newBroadcast()
        //Divide the object into multiple blocks and put those blocks in the block manager.
        new TorrentBroadcast[T](value_, nextBroadcastId.getAndIncrement()).writeBlocks()
            //保存一份到driver上
            SparkEnv.get.blockManager.putSingle(broadcastId, value, MEMORY_AND_DISK, tellMaster = false)
                doPutIterator()#memoryStore.putIteratorAsValues()#diskStore.put(blockId)
            //以4m分别保存block("spark.broadcast.blockSize", "4m"),并得到meta
            block MetaDatas = TorrentBroadcast.blockifyObject(value, blockSize..)
            foreach block MetaData : 
                blockManager.putBytes(BroadcastBlockId, MEMORY_AND_DISK_SER...)
                    doPutBytes()#memoryStore.putIteratorAsValues()#diskStore.putBytes()
                    //异步复制数据,sc.broadcast()应该只会在driver端保留一份数据,replication=1,后面executorfetch数据时才慢慢增加broadcast的副本数量
                    if level.replication > 1 :ThreadUtils.awaitReady(replicate(ByteBufferBlockData(bytes, false)．．．)

//复制副本规则,作为参考
blockManager.replicate()
    //请求获得其他BlockManager的id
    val initialPeers = getPeers(false)
        blockManagerMaster.getPeers(blockManagerId).sortBy(_.hashCode)
            //从driver上获取其他节点
            driverEndpoint.askSync[Seq[BlockManagerId]](GetPeers(blockManagerId))
                //BlockManagerMasterEndpoint中返回非driver和非当前节点的blockManagerId
                blockManagerInfo.keySet.contains(blockManagerId)#blockManagerIds.filterNot { _.isDriver }.filterNot { _ == blockManagerId }.toSeq
             foreach block replicate replication-1 nodes: blockTransferService.uploadBlockSync()
                //后面就是发送信息给blockManager,再保存数据通知driver
                blockManager.putBytes()#reportBlockStatus(blockId, putBlockStatus)
                    blockManagerMasterEndpoint.updateBlockInfo() //driver端更新信息
```

当调用 `rdd.transformation(func)` 时，如果 func 用到了 bdata，那么 Driver `submitTask()` 的时候会将 bdata 一同 func 进行序列化得到 serialized task，注意序列化的时候不会序列化 bdata 中包含的 data。

```scala
//TorrentBroadcast.scala 序列化的时候不会序列化 bdata 中包含的 data
// @transient表明不序列化_value
@transient private lazy val _value: T = readBroadcastBlock()
/** Used by the JVM when serializing this object. */
private def writeObject(out: ObjectOutputStream): Unit = Utils.tryOrIOException {
    assertValid()
    out.defaultWriteObject()
}
```

在 Executor 反序列化 task 的时候，会同时反序列化 task 中的 bdata 对象，这时候会调用 bdata 的 `readObject()` 方法。该方法先去本地 blockManager 那里询问 bdata 的 data 在不在 blockManager 里面，如果不在就使用下面的两种 fetch 方式之一去将 data fetch 过来。得到 data 后，将其存放到 blockManager 里面，这样后面运行的 task 如果需要 bdata 就不需要再去 fetch data 了。如果在，就直接拿来用了。

```scala
//runjob()
dagScheduler.submitMissingTasks(stage: Stage, jobId: Int)
    val taskIdToLocations = getPreferredLocs(stage.rdd, id)
        getCacheLocs()//从本地或者driver获取缓存rdd位置
        rdd.preferredLocations()//也会从checkpointrdd中寻找
    var taskBinary: Broadcast[Array[Byte]] = null
    try {
      // For ShuffleMapTask, serialize and broadcast (rdd, shuffleDep).
      // For ResultTask, serialize and broadcast (rdd, func).
      val taskBinaryBytes: Array[Byte] = stage match {
        case stage: ShuffleMapStage =>
          JavaUtils.bufferToArray(
            closureSerializer.serialize((stage.rdd, stage.shuffleDep): AnyRef))
        case stage: ResultStage => //把func也序列化了,func里面包含broadcast变量
            //不会序列化 broadcast变量 中包含的 data
          JavaUtils.bufferToArray(closureSerializer.serialize((stage.rdd, stage.func): AnyRef))
      }
    taskBinary = sc.broadcast(taskBinaryBytes)//广播task
    taskScheduler.submitTasks(new TaskSet(...))
    ...
```

```scala
//TorrentBroadcast.scala
//使用lazy方式,真正反序列化使用_value才调用方法读值
 @transient private lazy val _value: T = readBroadcastBlock()
 TorrentBroadcast.readBroadcastBlock()
     blockManager.getLocalValues()//本地读取
        memoryStore.getValues(blockId)#diskStore.getBytes(blockId)
     readBlocks() //本地无则从driver/其他executor读取
        foreach block : 
            blockManager.getRemoteBytes(BroadcastBlockId(id, "piece" + pid))
            blockManager.putBytes()//保存在本地
    //整个broadcast保存在本地
    blockManager.putSingle(broadcastId, obj, storageLevel, tellMaster = false)
    blocks.foreach(_.dispose()) //去重,把之前分开保存的block删除
```


### 2. HttpBroadcast

> spark 2.2 的Broadcast package中已经去除了HttpBroadcast,只留下了TorrentBroadcast。

HttpBroadcast 就是每个 executor 通过的 http 协议连接 driver 并从 driver 那里 fetch data。

HttpBroadcast 最大的问题就是 driver 所在的节点可能会出现网络拥堵，因为 worker 上的 executor 都会去 driver 那里 fetch 数据。

### 3. TorrentBroadcast

为了解决 HttpBroadast 中 driver 单点网络瓶颈的问题，Spark 又设计了一种 broadcast 的方法称为 TorrentBroadcast，这个类似于大家常用的 BitTorrent 技术。基本思想就是将 data 分块成 data blocks，然后假设有 executor fetch 到了一些 data blocks，那么这个 executor 就可以被当作 data server 了，随着 fetch 的 executor 越来越多，有更多的 data server 加入，data 就很快能传播到全部的 executor 那里去了。


## F&Q

### 问题：为什么只能 broadcast 只读的变量？

这就涉及一致性的问题，如果变量可以被更新，那么一旦变量被某个节点更新，其他节点要不要一块更新？如果多个节点同时在更新，更新顺序是什么？怎么做同步？还会涉及 fault-tolerance 的问题。为了避免维护数据一致性问题，Spark 目前只支持 broadcast 只读变量。

### 问题：broadcast 到节点而不是 broadcast 到每个 task？

因为每个 task 是一个线程，而且同在一个进程运行 tasks 都属于同一个 application。因此每个节点（executor）上放一份就可以被所有 task 共享。

