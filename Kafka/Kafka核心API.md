Apache Kafka 引入一个新的 Java 客户端（在 `org.apache.kafka.clients` 包中），代替老的 Scala 客户端，但是为了兼容，将会共存一段时间。为了减少依赖，这些客户端都有一个独立的 jar，而旧的 Scala 客户端继续与服务端保留在同个包下。

### Kafka 有 5 个核心 API

#### Producer API

作用：允许应用程序发送数据流到 Kafka 集群中的 topic

 [Producer](Kafka/Kafka-API-Java/API-Producer.md) 

#### Consumer API

作用：允许应用程序从 Kafka 集群的 topic 中读取数据流

 [Consumer](Kafka/Kafka-API-Java/API-Consumer.md) 

#### Streams API

作用：允许从输入 topic 转换数据流到输出 topic

 [Streams](Kafka/Kafka-API-Java/API-Streams.md) 

#### Connect API

作用：通过实现连接器（connecter），不断地从一些源系统或应用程序中拉取数据到 Kafka，或从 Kafka 提交数据到宿系统（sink system）或应用程序。

 [Connect](Kafka/Kafka-API-Java/API-Connect.md) 

#### AdminClient

 [AdminClient](Kafka/Kafka-API-Java/API-AdminClient.md) 