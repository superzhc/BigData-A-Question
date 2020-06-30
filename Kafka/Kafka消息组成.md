Message 消息是 Kafka 通信的基本单位，每个 producer 可以向一个 topic（主题）发布一些消息

Kafka 中的 Message 是以 topic 为基本单位组织的，不同的 topic 之间是相互独立的。每个 topic 又可以分成几个不同的 partition（每个 topic 有几个 partition 是在创建 topic 时指定的），每个 partition 存储一部分 Message。

partition 中每条 Message 包含了以下三个属性：

- **offset**：消息唯一标识，对应类型为 long
- **MessageSize**：对应类型 int32
- **data**：是 message 的具体内容

