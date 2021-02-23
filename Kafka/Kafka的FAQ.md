<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-07-01 00:57:27
 * @LastEditTime : 2021-02-05 16:05:22
 * @Copyright 2021 SUPERZHC
-->
## Kafka单条消息过大

查看kafka配置，**默认单条消息最大为1M**，当单条消息长度超过1M时，就会出现发送到broker失败，从而导致消息在producer的队列中一直累积，直到撑爆生产者的内存。

修改kafka配置可以解决此问题。主要修改步骤如下：

1. 修改kafka的broker配置：`message.max.bytes`（默认:1000000B），这个参数表示单条消息的最大长度。在使用kafka的时候，应该预估单条消息的最大长度，不然导致发送失败。
2. 修改kafka的broker配置：`replica.fetch.max.bytes` (默认: 1MB)，broker可复制的消息的最大字节数。这个值应该比`message.max.bytes`大，否则broker会接收此消息，但无法将此消息复制出去，从而造成数据丢失。
3. 修改消费者客户端配置：`fetch.message.max.bytes` (默认 1MB) – 消费者能读取的最大消息。这个值应该大于或等于`message.max.bytes`。*如果不调节这个参数，就会导致消费者无法消费到消息*，并且不会爆出异常或者警告，导致消息在broker中累积。

Kafka单条消息大小的性能：~~[kafka中处理超大消息的一些考虑](http://www.mamicode.com/info-detail-453907.html)~~ [kafka中处理超大消息的一些考虑](Kafka/Kafka中处理大消息的思考.md)

## Kafka Consumer assign 和 subscribe 模式的差异分析

api文档地址：<http://kafka.apachecn.org/10/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html>

在`Usage Examples`->`Manual Partition Assignment`中有如下一段话：

> The group that the consumer specifies is still used for committing offsets, but now the set of partitions will only change with another call to assign. Manual partition assignment does not use group coordination, so consumer failures will not cause assigned partitions to be rebalanced. Each consumer acts independently even if it shares a groupId with another consumer. To avoid offset commit conflicts, you should usually ensure that the groupId is unique for each consumer instance.

上面一段话的翻译是：

> 使用者所在的组仍用于提交偏移量，但是现在分区将**仅随**着其他的手动分配(assign)调用而更改。手动分配分区不使用组协调，因此使用者在遇到故障的后不会导致重新分配分区。即使每个使用者与另一个使用者共享一个groupId，每个使用者也独立执行操作。为了避免偏移量提交冲突，通常应确保groupId对于每个使用者实例都是唯一的

在assign的API中有如下一段介绍：

> Manual topic assignment through this method does not use the consumer's group management functionality. As such, there will be no rebalance operation triggered when group membership or cluster and topic metadata change.

翻译如下：

> **通过assign方法手动分配主题是不使用消费者组的组管理功能**。这样，当消费者组的成员或集群和主题的元数据发生更改时，将不会触发任何再平衡操作的。