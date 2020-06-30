Kafka 使用 Zookeeper 保存集群的元数据信息和消费者信息。

![1570779081633](../images/1570779081633.png)

| Registry                | Zookeeper节点                                                | 行为                                                         |
| ----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| BrokerNode Registry     | `/brokers/ids/[0...N]`->host:port                            | 每个Broker节点启动时在 /brokers 下创建子节点，读取 /brokers 就可以获取 Kafka 的集群信息。这些 Broker 节点会选举出一个 Leader 作为整个集群的 Controller |
| BrokerTopic Registry    | `/brokers/topics/[topic]`->{version:1,partitions:{}}         | 创建主题时指定分区数量，Kafka 为每个分区分配的 AR 分布在不同的 Broker 上。分区的 key 是分区编号，value 是所在的 Broker 节点列表，列表数量跟副本数量有关 |
| PartitionState Registry | `/brokers/topics/[topic]/partitions/[partition]/state`->{leaderAndlsr} | 分区的状态信息，每个分区要可用必须要在 AR 中选举出 Leader 副本，同时也有 ISR 保持喝 Leader 副本的同步 |
| ConsumerId Registry     | `/consumers/[group_id]/ids/[consumer_id]`->topic1...topicN   | 消费者订阅主题，消费者都有所属的消费组。读取消费组节点下的所有子节点就可以获取消费组的消费者成员列表 |
| ConsumerOffset Tracking | `/consumers/[group_id]/offsets/[topic]/[partition_id]`->offset_counter_value | 消费者记录分区的消费进度，偏移量以消费组为级别，是为了保证即使消费者挂掉，分区也会被分配给其他的消费者，使得分区被透明地消费 |
| PartitionOwner Registry | `/consumer/[group_id]/owners/[topic]/[partition_id]`->consumer_node_id | 分区的所有者，一个分区只会被分区给消费者组中的一个消费者。所有者不像偏移量，记录的是消费者，如果用消费组，就无法区分不同的消费者 |

