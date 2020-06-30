Spark Streaming 是 Spark 核心 API 的一个扩展，它对实时流式数据的处理具有可扩展性、高吞吐量、可容错性等特点。用户可以从 kafka、flume、Twitter、 ZeroMQ、Kinesis 等源获取数据，也可以通过由高阶函数 map、reduce、join、window 等组成的复杂算法计算出数据。最后，处理后的数据可以推送到文件系统、数据库、实时仪表盘中。

![img](images/20170331163803416)

Spark Streaming 接收实时的输入数据流，然后将这些数据切分为批数据供 Spark 引擎处理，Spark引擎将数据生成最终的结果数据。

![img](images/20170331163814967)

Spark Streaming 支持一个高层的抽象，叫做离散流(`discretized stream`)或者 `DStream`，它代表连续的数据流。DStream 既可以利用从 Kafka、Flume 和 Kinesis 等源获取的输入数据流创建，也可以在其他 DStream 的基础上通过高阶函数获得。在内部，DStream 是由一系列RDDs组成。

目前而言 SparkStreaming 主要支持以下三种业务场景：
- 无状态操作：只关注当前批次中的实时数据，例如：
  - 商机标题分类，分类http请求端 -> kafka -> Spark Streaming -> http请求端Map -> 响应结果
  - 网库Nginx访问日志收集，flume->kafka -> Spark Streaming -> hive/hdfs
  - 数据同步，网库主站数据通过“主站”->kafka->Spark Streaming -> hive/hdfs
- 有状态操作：对有状态的DStream进行操作时,需要依赖之前的数据 除了当前新生成的小批次数据，但还需要用到以前所生成的所有的历史数据。新生成的数据与历史数据合并成一份流水表的全量数据。例如:
  - 实时统计网库各个站点总的访问量
  - 实时统计网库每个商品的总浏览量，交易量，交易额。
- 窗口操作：定时对指定时间段范围内的DStream数据进行操作，例如：
   - 网库主站的恶意访问、爬虫，每10分钟统计30分钟内访问次数最多的用户。

## 特性

**优点**：

- 吞吐量大、速度快。
- 容错：SparkStreaming在没有额外代码和配置的情况下可以恢复丢失的工作。checkpoint。
- 社区活跃度高。生态圈强大。
- 数据源广泛。

**缺点**：

- 延迟。500毫秒已经被广泛认为是最小批次大小，这个相对storm来说，还是大很多。所以实际场景中应注意该问题，就像标题分类场景，设定的0.5s一批次，加上处理时间，分类接口会占用1s的响应时间。实时要求高的可选择使用其他框架。













