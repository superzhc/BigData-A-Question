### 简介

流应用程序通常是24/7运行的，因此必须对与应用程序逻辑无关的故障（例如系统故障，JVM 崩溃等）具有弹性的容错能力。为此， Spark Streaming 需要将足够的信息 `checkpoint` 到容错存储系统(比如 HDFS)，以便可以从故障中恢复。检查点包括两种类型：

- **元数据检查点**

  元数据检查点可以保证从 Driver 程序失败中恢复。即如果运行 Driver 的节点失败时，可以查看最近的 checkpoint 数据获取最新的状态。典型的应用程序元数据包括：

- - **配置** ：用于创建流应用程序的配置。
  - **DStream 操作** ：定义流应用程序的 DStream 操作。
  - **未完成的 batch** ：当前运行 batch 对应的 job 在队列中排队，还没有计算到该 batch 的数据。

- **数据检查点**

  将生成的 RDD 保存到可靠的存储中。在某些*有状态*转换中，需要合并多个批次中的数据，所以需要开启检查点。在此类转换中，生成的 RDD 依赖于先前批次的 RDD，这导致依赖链的长度随时间不断增加。为了避免恢复时间无限制的增加（与依赖链成比例），有状态转换的中间 RDD 定期 *checkpoint* 到可靠的存储（例如 HDFS），以切断依赖链，功能类似于持久化，只需要从当前的状态恢复，而不需要重新计算整个 lineage。

总而言之，从 Driver 程序故障中恢复时，主要需要元数据检查点。而如果使用有状态转换，则需要数据或 RDD 检查点。

### 什么时候启用检查点

必须为具有以下类型的应用程序启用检查点：

- **使用了有状态转换转换操作**

  如果在应用程序中使用 `updateStateByKey` 或 `reduceByKeyAndWindow`，则必须提供检查点目录以允许定期进行 RDD 检查点。

- **从运行应用程序的 Driver 程序故障中恢复**

  元数据检查点用于恢复进度信息。

注意，没有前述状态转换的简单流应用程序可以在不启用检查点的情况下运行。在这种情况下，从驱动程序故障中恢复也将是部分的（某些丢失但未处理的数据可能会丢失）。这通常是可以接受的，并且许多都以这种方式运行 Spark Streaming 应用程序。预计将来会改善对非 Hadoop 环境的支持。

### 如何配置检查点

可以通过具有容错的、可靠的文件系统（例如HDFS，S3等）中设置目录来启用检查点，将检查点信息保存到该目录中。开启检查点，需要开启下面的两个配置：

- `streamingContext.checkpoint()`：配置检查点的目录，比如 HDFS 路径
- `dstream.checkpoint()`：检查点的频率

其中配置检查点的时间间隔是可选的。如果不设置，会根据 DStream 的类型选择一个默认值。对于 MapWithStateDStream，默认的检查点间隔是 batch interval 的10倍。对于其他的 DStream，默认的检查点间隔是 `10s`，或者是 batch interval 的间隔时间。**需要注意的是：checkpoint 的频率必须是 batch interval 的整数倍，否则会报错**。

此外，如果要使应用程序从Driver程序故障中恢复，则需要使用下面的方式创建StreamingContext：

```scala
def createStreamingContext (conf: SparkConf,checkpointPath: String):StreamingContext = {
    val ssc = new StreamingContext( <ConfInfo> )
    // .... other code ...
    ssc.checkPoint(checkpointDirectory)
    ssc
}

// 创建一个新的StreamingContext或者从最近的checkpoint获取
val context = StreamingContext.getOrCreate(checkpointDirectory,createStreamingContext _)

// 启动
context.start()
context.awaitTermination()
```

- 程序首次启动时，它将创建一个新的 StreamingContext，然后调用 `start()`。
- 失败后重新启动程序时，它将根据检查点目录中的检查点数据重新创建 StreamingContext。

> **注意：**
>
> RDD 的检查点需要将数据保存到可靠存储上，由此带来一些成本开销。这可能会导致 RDD 获得检查点的那些批次的处理时间增加。因此，需要设置一个合理的检查点的间隔。在 batch interval 较小时(例如1秒），每个 batch interval 都进行检查点可能会大大降低吞吐量。相反，检查点时间间隔太长会导致 lineage和任务规模增加，这可能会产生不利影响。对于需要RDD检查点的有状态转换，默认间隔为batch interval的倍数，至少应为10秒。可以使用 **`dstream.checkpoint(checkpointInterval)`** 进行配置。通常， DStream 的 5-10 个 batch interval 的检查点间隔是一个较好的选择。

### 检查点和持久化之间的区别

- 持久化

- - 当我们将 RDD 保持在 DISK_ONLY 存储级别时，RDD 将存储在一个位置，该 RDD 的后续使用将不会重新计算 lineage。
  - 在调用 `persist()` 之后，Spark 会记住 RDD 的 lineage，即使它没有调用它。
  - 作业运行完成后，将清除缓存并销毁文件。

- 检查点

- - 检查点将 RDD 存储在 HDFS 中，将会删除 lineage 血缘关系。
  - 在完成作业运行后，与持计划不同，不会删除检查点文件。
  - 当 checkpoint 一个 RDD 时，将导致双重计算。即该操作在完成实际的计算工作之前，首先会调用持久化方法，然后再将其写入检查点目录。