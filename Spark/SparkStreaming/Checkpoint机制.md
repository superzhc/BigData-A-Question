# Checkpoint 机制

## Checkpoint 类型

Spark Streaming 有两种数据需要被进行checkpoint

1. 元数据 Checkpoint：为了从 Driver 失败中进行恢复
    1. 配置信息——创建 Spark Streaming 应用程序的配置信息，比如 SparkConf 中的信息；
    2. DStream 的操作信息——定义了 Spark Streaming 应用程序的计算逻辑的 DStream 操作信息；
    3. 未处理的 batch 信息——那些 Job 正在排队，还没处理的 batch 信息。
2. 数据 Checkpoint：对有状态的 transformation 操作进行快速的失败恢复
    将实时计算过程中产生的 RDD 的数据保存到可靠的存储系统中

## 使用场景

1. 使用了有状态的 transformation 操作
2. 要保证可以从 Driver 失败中进行恢复

除此之外，Spark Streaming 中不应该使用 Checkpoint 机制，降低性能

## Checkpoint 使用方式

将 Checkpoint 间隔设置为窗口操作的滑动间隔的 5~10 倍

- 数据 Checkpoint
    ```scala
    val ssc =  new StreamingContext(spark.sparkContext,Seconds(1))
    ssc.checkpoint("hdfs://spark1:9000/checkpoint")  
    ```
- 从 Driver 失败中进行恢复
  1. 更改程序
    ```scala
    //当Driver从失败中恢复过来时，需要从checkpoint目录中记录的元数据中，恢复出来一个StreamingContext
    def functionToCreateContext(): StreamingContext = {
        val ssc = new StreamingContext(...)  
        val lines = ssc.socketTextStream(...) 
        ssc.checkpoint(checkpointDirectory)   
        ssc
    }
    val context = StreamingContext.getOrCreate(checkpointDirectory, functionToCreateContext _)
    context.start()
    context.awaitTermination()
        ```
  2. 更改spark-submit
   1. `--deploy-mode`:
       默认其值为 client，表示在提交应用的机器上启动 Driver；要能够自动重启 Driver，就必须将其值修改为 cluster；
   2. `--supervise`:
       对于 Spark 自身的 standalone 模式，需要进行一些配置去 supervise driver，监控Driver运行的过程，在它失败时将其重启。