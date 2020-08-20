| 组件                                                  | 说明                                                         |
| ----------------------------------------------------- | ------------------------------------------------------------ |
| [Topics（主题）](Kafka/Kafka之Topic和Partition.md)    | 由用户定义并配置在 Kafka 服务器 <br>用于建立生产者和消费者之间的订阅关系：生产者发送消息到指定 Topic 下，消费者从这个 Topic 下消费消息 |
| [Partition（分区）](Kafka/Kafka之Topic和Partition.md) | 一个 Topic 下面会分为很多分区 <br>例如：“*kafka-test*”这个*Topic*下可以分为*6*个分区，分别由两台服务器提供，那么通常可以配置为让每台服务器提供*3*个分区，假如服务器*ID*分别为*0*、*1*，则所有的分区为*0-0*、*0-1*、*0-2*和*1-0*、*1-1*、*1-2* <br>Topic 物理上的分组，一个 Topic 可以分为多个 partition，每个 partition 是一个有序的队列。 <br>partition 中每条消息都会被分配一个有序的 id（offset） |
| **Offset（偏移量）**                                  | 消息存储在 Kafka 的 Broker 上，消费者拉取消息数据的过程中需要知道消息在文件中的偏移量（Offset） |
| [Broker](Kafka/Kafka之Broker.md)                      | 已发布的消息保存在一组服务器中，称之为 Kafka 集群。集群中的每一个服务器都是一个代理（Broker）。消费者可以订阅一个或多个主题（topic），并从 Broker 拉数据，从而消费这些已发布的消息 |
| [Producers（生产者）](Kafka/Kafka之Producer.md)       | 消息产生的源头，负责生成消息并发送到 Kafka <br> 消息是**追加方式**添加到分区的 |
| [Consumers（消费者）](Kafka/Kafka之Consumer.md)       | 消息的使用方，负责消费 Kafka 服务器上的消息                  |
| [Group（消费者分组）](Kafka/Kafka之Consumer.md)       | 用于归组同类消费者，<br>在 Kafka 中，多个消费者可以共同消费一个 Topic 下的消息，每个消费者消费其中的部分消息，这些消费者就组成了一个分组，拥有同一个分组名称，通常也被称为消费者集群 |
| **Leader（领导者）**                                  | “Leader” 是负责给定分区的所有读取和写入节点。每个分区都有一个服务器充当 Leader |
| **Follower（追随者）**                                | 跟随领导者指令的节点被称为 Follower。如果领导失败，一个追随者将自动成为新的领导者。跟随着作为正常的消费者，拉取消息并更新其自己的数据存储。 |

