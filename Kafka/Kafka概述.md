## 简介

Apache Kafka 是一个**分布式发布-订阅（pub-sub）消息系统**，它一般被称为`分布式提交日志`或者`分布式流平台`，它以高吞吐、持久化、易水平扩展、支持流数据处理等多种特性而被广泛使用。目前越来越多的开源分布式处理系统如 Strom、Spark、Flink 等都支持与 Kafka 集成。

Kafka 具备如下特点：

- 高吞吐量、低延迟：Kafka 每秒可以处理几十万条消息，它的延迟最低只有几毫秒，每个 topic 可以分多个 partition；
- 可扩展性：Kafka 集群支持热扩展；
- 持久性、可靠性：消息被持久化到本地磁盘，并且支持数据备份防止数据丢失；
- 容错性：允许集群中节点失败（若副本数量为n，则允许n-1个节点失败）；
- 高并发：支持数千个客户端同时读写。

## 基本概念与术语

### 消息

Kafka 的数据单元被称为**消息**。Kafka 中的消息格式由多个字段组成，其中很多字段都是用于管理消息的元数据字段，对用户是完全透明的。

消息由消息头部、key 和 value 组成，消息头部包括 消息版本号、属性、时间戳、键长度和消息体长度等信息，一般需要掌握以下 3 个重要字段的含义：

- key：消息键，对消息做分区时使用，即决定消息被保存在某个主题的哪个分区下
- value：消息体，保存实际的消息数据
- timestamp：消息发送的时间戳，用于流式处理及其他依赖时间的处理语义，如果不指定则取当前时间

### 主题和分区

Kafka 的消息通过**主题**进行分类，用来实现区分实际业务。

如下图 Kafka 官网提供的主题和分区的关系图所示，主题可以被分为若干个分区，一个分区就是一个提交日志。消息以追加的方式写入分区，然后以顺序的方式进行读取。Kafka 通过分区来实现数据冗余和伸缩性。分区可以分布在不同的服务器上，即一个主题的数据可能分布在多个服务器上，以此提供比单个服务器更高的性能。

![](https://raw.githubusercontent.com/superzhc/GraphBed/master/publish/%E4%B8%BB%E9%A2%98%E7%9A%84%E5%88%86%E5%8C%BA%E6%97%A5%E5%BF%97.png)

注意事项：

1. 由于主题一般包含多个分区，因此无法在整个主题范围内保证消息的顺序，但可以保证消息在单个分区的顺序性。

### 生产者

生产者（Producer）用于将数据流发布到 Kafka 的主题。

### 消费者

消费者订阅一个或多个主题，并按照消息的顺序读取消息。

### 偏移量

偏移量是一种元数据，它是一个不断递增的整数值，在创建消息时，Kafka 会把它添加到消息里。在给定的分区里，每个消息的偏移量都是唯一的。消费者通过检查消息的偏移量来区分已经读取过的消息。

### Broker

broker 可以看作一个独立的 Kafka 服务器或者 Kafka 服务实例。broker 接收来自生产者的消息，对消息设置偏移量，并将消息保存到磁盘中。broker 为消费者提供服务，对客户端订阅的分区的请求做出响应，返回消息给客户端。

## 使用场景

### 活动跟踪

Kafka 最初的使用场景是跟踪用户的活动。用于记录用户与网站的前端应用发生交互生成的用户活动相关的信息。

### 消息传输

Kafka 具有非常高的吞吐量特性，其内置的分区机制和副本机制即实现了高性能的消息传输，同时还提供了高可靠性和高容错性，因此 Kafka 特别适合用于实现消息处理应用。

### 度量指标和日志记录

### 提交日志

Kafka 的基本概念来源于提交日志，所以使用 Kafka 作为提交日志是件顺理成章的事情。

### 流数据的处理

