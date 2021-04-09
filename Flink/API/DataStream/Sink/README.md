# Sink

Flink 针对 DataStream 提供了大量的已经实现的数据目的地（Sink）。

## writeAsText

> 将元素以字符串形式逐行写入，这些字符串通过调用每个元素的 `toString()` 方法来获取

## `print/printToErr`

> 打印每个元素的 `toString()` 方法的值到标准输出或者标准错误输出流中

## 自定义输出

> 通过 addSink 可以实现把数据输出到第三方存储介质中

系统提供了一批内置的 Connector，它们会提供对应的 Sink 支持，如下表所示：

| 连接器                      | 是否提供 Source 支持 | 是否提供 Sink 支持 |
| --------------------------- | -------------------- | ------------------ |
| Apache Kafka                | 是                   | 是                 |
| Apache Cassandra            | 否                   | 是                 |
| Amazon Kinesis Data Streams | 是                   | 是                 |
| Elasticsearch               | 否                   | 是                 |
| HDFS                        | 否                   | 是                 |
| RabbitMQ                    | 是                   | 是                 |
| Apache NiFi                 | 是                   | 是                 |
| Twitter Streaming API       | 是                   | 否                 |

Flink 通过 Apache Bahir 组件也提供了对这些连接器的支持，如下表所示：

| 连接器          | 是否提供 Source 支持 | 是否提供 Sink 支持 |
| --------------- | -------------------- | ------------------ |
| Apache ActiveMQ | 是                   | 是                 |
| Apache Flume    | 否                   | 是                 |
| Redis           | 否                   | 是                 |
| Akka            | 否                   | 是                 |
| Netty           | 是                   | 否                 |

用户也可以自定义 Sink 的实现，有如下两种实现：

- 实现 SinkFunction 接口
- 继承 RichSinkFunction 类