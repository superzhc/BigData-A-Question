# Kafka 的设计架构？

简单架构如下：

![](../images/producer_consumer.png)

详细如下：

![](../images/kafka_arch-iteblog.png)

Kafka 架构分为以下几个部分：

- Producer：生产者，向 Kafka Broker 发送/生产消息
- Consumer：消费者，从 Kafka Broker 中获取消息
- Topic：主题，一个 Topic 可以分为一个或多个分区
- Consumer Group：这是 Kafka 用来实现一个 Topic 消息的广播（发给所有的consumer）和单播（发给任意一个consumer）的手段。一个 Topic 可以有多个 Consumer Group
- Broker：一个 Kafka 服务器就是一个 Broker。一个集群由多个 Broker 组成。一个 Broker 可以容纳多个 Topic
- Partition：为了实现扩展性，一个非常大的 Topic 可以分不到多个 Broker 上，每个 partition 是一个有序的队列。partition 中的每条消息都会被分配一个有序的 id（offset）。将消息发给 consumer，Kafka 只保证一个 partition 中的消息的顺序，不保证一个 Topic 的整体（多个 partition 间）的顺序
- Offset：Kafka 的存储文件都是按照 `offset.kafka` 来命名，用 offset 做名字的好处是方便查找。例如你想找位于 2049 的位置，只要找到 `2048.kafka` 的文件即可。