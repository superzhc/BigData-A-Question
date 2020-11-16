# 运行流程

1. 初始化 StreamingContext 作为程序的统一入口；因为 Spark Streaming 的底层依赖于 Spark Core，因此 StreamingContext 必须依赖于 SparkContext
2. Driver 端初始化好之后，会发送 Receiver 对象到 Executor 上，Receiver 默认只有一个
3. Receiver 启动后可以视为一个 Task 任务，这个任务本质上是一个不断接收数据，并封装成 Block，之后将 Block 写到 Executor 的内存中
4. Receiver 会把这些 Block 的信息返回给 Driver
5. Driver 会根据一定的时间间隔，把这些 Block 组织成一个 RDD，其实一个 Block 就是一个 partition

> 注：Spark Streaming 默认是 200ms 形成一个 Block，通过参数 `spark.streaming.blockInterval` 来进行设置的。