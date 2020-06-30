消息堆积是消费滞后（Lag）的一种表现形式，消息中间件服务端中所留存的消息与消费掉的消息之间的差值即为消息堆积量，也称之为消费滞后量。对于 Kafka 而言，消息被发送至 Topic 中，而 Topic 又分成了多个分区（Partition），每一个 Partition 都有一个预写式的日志文件，虽然 Partition 可以继续细分为若干个段文件（Segment），但是对于上层应用来说可以将 Partition 看成最小的存储单元（一个由多个 Segment 文件拼接的 "巨型文件"）。每个 Partition 都由一系列有序的、不可变的消息组成，这些消息被连续的追加到 Partition 中。

 ![img](../images/20180416011820831.png) 

上图有四个概念：

1. **LogStartOffset**：表示一个 Partition 的起始位移，初始为 0，虽然消息的增加以及日志清除策略的影响，这个值会阶段性的增大
2. **ConsumerOffset**：消费位移，表示 Partition 的某个消费者消费到的位移位置
3. **HighWatermark**：简称 HW，代表消费端所能”观察“到的 Partition 的最高日志位移，`HW>=ConsumerOffset`
4. **LogEndOffset**：简称 LEO，代表 Partition 的最高日志位移，其值对消费者不可见。比如再 ISR（In-Sync-Replicas）副本数等于 3 的情况下（如下图所示），消息发送到 Leader A 之后会更新 LEO 的值，Follower B 和 Follower C 也会实时拉取 Leader A 中的消息来更新自己，HW 就表示 A、B、C 三者同时达到的日志位移，也就是 A、B、C 三者中 LEO 最小的那个值。由于 B、C拉取 A 消息之间延时问题，所以 HW 必然不会一直与 Leader 的 LEO 相等，即 `LEO>=HW`

 ![img](../images/20180416011835658.png) 

要计算 Kafka 中某个消费者的滞后量很简单，首先获取消费者消费的 Topics 的数量，然后针对每个 Topic 来计算其中每个 Partition 的 Lag，每个 Partition 的 Lag 计算如下图所示：

 ![img](../images/20180416011844356.png) 

由图可知 `Lag=HW-ConsumerOffset`。对于这里可能有个误区，就是认为 Lag 应该是 LEO 与 ConsumerOffset 之间的差值，但 LEO 是对消费者不可见的，既然不可见何来消费滞后一说。





































