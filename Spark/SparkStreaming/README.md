# Spark Streaming

Spark Streaming 是 Spark 核心 API 的一个扩展，它对实时流式数据的处理具有可扩展性、高吞吐量、可容错性等特点。用户可以从 kafka、flume、Twitter、 ZeroMQ、Kinesis 等源获取数据，也可以通过由高阶函数 map、reduce、join、window 等组成的复杂算法计算出数据。最后，处理后的数据可以推送到文件系统、数据库、实时仪表盘中。

![img](images/20170331163803416)

## DStream

Spark Streaming 接收实时的输入数据流，然后将这些数据切分为批数据供 Spark 引擎处理，Spark引擎将数据生成最终的结果数据。

![img](images/20170331163814967)

Spark Streaming 支持一个高层的抽象，叫做离散流(`discretized stream`)或者 `DStream`，它代表连续的数据流。DStream 既可以利用从 Kafka、Flume 和 Kinesis 等源获取的输入数据流创建，也可以在其他 DStream 的基础上通过高阶函数获得。在内部，DStream 是由一系列RDD组成。

## 特性

**优点**：

- 吞吐量大、速度快。
- 容错：SparkStreaming在没有额外代码和配置的情况下可以恢复丢失的工作。checkpoint。
- 社区活跃度高。生态圈强大。
- 数据源广泛。

**缺点**：

- 延迟。500毫秒已经被广泛认为是最小批次大小，这个相对storm来说，还是大很多。所以实际场景中应注意该问题，就像标题分类场景，设定的0.5s一批次，加上处理时间，分类接口会占用1s的响应时间。实时要求高的可选择使用其他框架。
