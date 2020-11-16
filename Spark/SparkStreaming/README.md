# Spark Streaming

Spark Streaming 是 Spark 核心 API 的一个扩展，它对实时流式数据的处理具有可扩展性、高吞吐量、可容错性等特点。用户可以从 kafka、flume、Twitter、 ZeroMQ、Kinesis 等源获取数据，也可以通过由高阶函数 map、reduce、join、window 等组成的复杂算法计算出数据。最后，处理后的数据可以推送到文件系统、数据库、实时仪表盘中。

![img](images/20170331163803416)

**注意点**：

1. 只要一个 StreamingContext 启动之后，就不能再往其中添加任何计算逻辑了；
2. 一个 StreamingContext 停止之后，是肯定不能重启的。调用 `stop()` 之后，不能再调用 `start()`；
3. 一个 JVM 同时只能有一个 StreamingContext 启动(和 SparkContext 一样)；
4. 调用 `stop()` 方法时，会同时停止内部的 SparkContext，如果希望后面继续使用 SparkContext 创建其他类型的 Context，比如 SQLContext，可以用 `stop(false)`；
5. 一个 SparkContext 可以创建多个 StreamingContext，只要上一个先用 `stop(false)` 停止，再创建下一个即可。

## Spark Streaming VS Storm

| 对比点          | Storm                            | Spark Streaming                                               |
| --------------- | -------------------------------- | ------------------------------------------------------------- |
| 实时计算模型    | 纯实时，来一条数据，处理一条数据 | 准实时，对一个**时间段**内的数据收集起来，作为一个RDD，再处理 |
| 实时计算延迟度  | 毫秒级                           | 秒级                                                          |
| 吞吐量          | 低                               | 高                                                            |
| 事务机制        | 支持完善                         | 支持，但不够完善                                              |
| 健壮性 / 容错性 | ZooKeeper，Acker，非常强         | Checkpoint，WAL，一般                                         |
| 动态调整并行度  | 支持                             | 不支持                                                        |

## [DStream](Spark/SparkStreaming/Dstream.md)

Spark Streaming 接收实时的输入数据流，然后将这些数据切分为批数据供 Spark 引擎处理，Spark 引擎将数据生成最终的结果数据。

![img](images/20170331163814967)

Spark Streaming 支持一个高层的抽象，叫做离散流(`discretized stream`)或者 `DStream`，它代表连续的数据流。DStream 既可以利用从 Kafka、Flume 和 Kinesis 等源获取的输入数据流创建，也可以在其他 DStream 的基础上通过高阶函数获得。在内部，DStream 是由一系列RDD组成。

## Receiver

除了文件数据流、Direct 模式的 Kafka 数据源之外，所有的输入 DStream 都会绑定一个 Receiver 对象，Receiver 用来从数据源接收数据，并将其存储在 Spark 的内存中，以供后续处理。

Receiver 是一个长时间在 executor 中运行的任务，**一个Receiver会独占一个cpu core**，必须保证分配给 Spark Streaming 的每个 executor 分配的 `core > 1`，保证分配到 executor 上运行的输入 DStream，有两条线程并行，一条运行 Receiver 接收数据；一条处理数据。否则的话，只会接收数据，不会处理数据。

Receiver 在 blockInterval 毫秒生成一个新的数据块，总共创建 `N = batchInterval/blockInterval` 的数据块,并由当前 executor 的 BlockManager 分发给其他执行程序的 BlockManagers，所有这些块组成一个 RDD (在 batchInterval 期间生成的块是 RDD 的 partitions)

## 特性

**优点**：

- 吞吐量大、速度快。
- 容错：SparkStreaming在没有额外代码和配置的情况下可以恢复丢失的工作。checkpoint。
- 社区活跃度高。生态圈强大。
- 数据源广泛。

**缺点**：

- 延迟。500毫秒已经被广泛认为是最小批次大小，这个相对storm来说，还是大很多。所以实际场景中应注意该问题，就像标题分类场景，设定的0.5s一批次，加上处理时间，分类接口会占用1s的响应时间。实时要求高的可选择使用其他框架。
