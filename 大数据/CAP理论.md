# CAP 理论

1. C **一致性**（Consistency）：分布式环境中，一致性是指多个副本之间，在同一时刻能否有同样的值
2. A **可用性**（Availability）：系统提供的服务必须一直处于可用的状态。即使集群中一部分结点故障
3. P **分区容错性**（Partition tolerance）：系统在遇到结点故障或者网络分区时，任然能对外提供一致性和可用性的服务。以实际效果而言，分区相当于通信的时限要求，系统如果不能在一定时限内达成数据一致性，也就意味着发生了分区的情况，必须就当前操作在 C 和 A之前作出选择。

## CAP不能同时满足的证明

假设系统中有 5 个节点，n1~n5。n1，n2，n3 在A物理机房。n4，n5 在 B 物理机房。现在发生了网络分区，A 机房和 B 机房网络不通。 **保证一致性**：此时客户端在 A 机房写入数据，不能同步到B机房。写入失败。此时失去了可用性。 **保证可用性**：数据在 A 机房的 n1~n3 节点都写入成功后返回成功。数据在 B 机房的 n4~n5 节点也写入数据，返回成功。同一份数据在 A 机房和 B 机房出现了数据不一致的情况。聪明如你，可以想到 zookeeper，当一个节点 down 掉，系统会将其剔出节点，然其它一半以上的节点写入成功即可。是不是 zookeeper 同时满足了 CAP 呢。其实这里有一个误区，系统将其剔出节点。有一个隐含的条件是，系统引入了一个调度者，一个踢出坏节点的调度者。当调度者和 zookeeper 节点出现网络分区，整个系统还是不可用的。

## 常见场景

CA without P：这种情况在分布式系统中几乎是不存在的。首先在分布式环境下，网络分区是一个自然的事实。因为分区是必然的，所以如果舍弃 P，意味着要舍弃分布式系统。那也就没有必要再讨论 CAP 理论了。这也是为什么在前面的 CAP 证明中，我们以系统满足 P 为前提论述了无法同时满足 C 和 A。
CP without A：相当于每个写请求都须在Server之前强一致。P (分区)会导致同步时间无限延长。这个是可以保证的。例如数据库的分布式事务，两阶段提交，三阶段提交等
AP without C：当网络分区发生，A 和 B 集群失去联系。为了保证高可用，系统在写入时，系统写入部分节点就会返回成功，这会导致在一定时间之内，客户端从不同的机器上面读取到的是数据是不一样的。例如 redis 主从异步复制架构，当 master down 掉，系统会切换到 slave，由于是异步复制，salve 不是最新的数据，会导致一致性的问题。

## 两阶段提交协议（2PC）

> 二阶段提交 (Two-phaseCommit) 是指，在计算机网络以及数据库领域内，为了使基于分布式系统架构下的所有节点在进行事务提交时保持一致性而设计的一种算法 (Algorithm)。通常，二阶段提交也被称为是一种协议 (Protocol)。在分布式系统中，每个节点虽然可以知晓自己的操作时成功或者失败，却无法知道其他节点的操作的成功或失败。当一个事务跨越多个节点时，为了保持事务的 ACID 特性，需要引入一个作为协调者的组件来统一掌控所有节点(称作参与者)的操作结果并最终指示这些节点是否要把操作结果进行真正的提交(比如将更新后的数据写入磁盘等等)。因此，二阶段提交的算法思路可以概括为：参与者将操作成败通知协调者，再由协调者根据所有参与者的反馈情报决定各参与者是否要提交操作还是中止操作。

**两种角色**

- 协调者
- 参与者

**处理阶段**

- 询问投票阶段：事务协调者给每个参与者发送 Prepare 消息，参与者收到消息后，要么在本地写入 redo 或 undo 日志成功后，返回同意的消息或者一个终止事务的消息
- 执行初始化（执行提交）：协调者在收到所有参与者的消息后，如果有一个返回终止事务，那么协调者给每个参与者发送回滚的指令，否则发送 commit 消息。

**异常情况处理**

- 协调者故障：备用协调者接管，并查询参与者执行到什么地址
- 参与者故障：协调者会等待他重启然后执行
- 协调者和参与者同时故障：协调者故障，然后参与者也故障。例如：有机器 1，2，3，4。其中 4 是协调者，1，2，3是参与者，4 给1，2 发完提交事务后故障了，正好3这个时候也故障了，注意这时 3 是没有提交事务数据的。在备用协调者启动了，去询问参与者，由于3死掉了，一直不知道它处于什么状态（接受了提交事务，还是反馈了能执行还是不能执行 3 个状态）。面对这种情况，2PC，是不能解决的，要解决需要下文介绍的 3PC

**缺点**

- **同步阻塞问题**：由于所有参与的节点都是事务阻塞型的，例如 `update table set status=1 where current_day=20181103`，那么参与者`table`表的`current_day=20181103`的记录都会被锁住，其他的要修改`current_day=20181103`行的事务，都会被阻塞
- **单点故障阻塞其他事务**：协调者再执行提交的阶段 down 掉，所有的参与者出于锁定事务资源的状态中。无法完成相关的事务操作。
- **参与者和协调者同时 down 掉**：协调者在发送完 commit 消息后 down 掉，而唯一接受到此消息的参与者也 down 掉了。新协调者接管，也是一个懵逼的状态，不知道此条事务的状态。无论提交或者回滚都是不合适的。**这个是两阶段提交无法改变的**

## 三阶段提交协议（3PC）

2PC 当时只考虑如果单机故障的情况，是可以勉强应付的。当遇到协调者和参与者同时故障的话，2PC 的理论是不完善的。此时 3PC 登场。
3PC 就是对 2PC 漏洞的补充协议。主要改动两点

1. 在 2PC 的第一阶段和第二阶段插入一个准备阶段，做到就算参与者和协调者同时故障也不阻塞，并且保证一致性。
2. 在协调者和参与者之间引入超时机制

**处理的三个阶段**

- 事务询问阶段 (can commit 阶段)：协调者向参与者发送 commit 请求，然后等待参与者反应。这个和 2PC 阶段不同的是，此时参与者没有锁定资源，没有写 redo，undo，执行回滚日志。**回滚代价低**
- 事务准备阶段 (pre commit)：如果参与者都返回 ok，那么就发送 Prepare 消息，参与者本地执行 redo 和 undo 日志。否者就向参与者提交终止 (abort) 事务的请求。如果再发送 Prepare 消息的时候，等待超时，也会向参与者提交终止事务的请求。
- 执行事务阶段 (do commit)：如果所有发送Prepare都返回成功，那么此时变为执行事务阶段，向参与者发送 commit 事务的消息。否者回滚事务。在此阶段参与者如果在一定时间内没有收到 do commit 消息，触发超时机制，会自己提交事务。此番处理的逻辑是，能够进入此阶段，说明在事务询问阶段所有节点都是好的。即使在提交的时候部分失败，有理由相信，此时大部分节点都是好的。是可以提交的

**缺点**

- 不能解决网络分区的导致的数据不一致的问题：例如 1~5 五个参与者节点，1，2，3 个节点在A机房，4，5 节点在 B 机房。在 `pre commit` 阶段，1~5 个节点都收到 Prepare 消息，但是节点1执行失败。协调者向`1~5`个节点发送回滚事务的消息。但是此时A，B机房的网络分区。`1~3` 号节点会回滚。但是 `4~5` 节点由于没收到回滚事务的消息，而提交了事务。待网络分区恢复后，会出现数据不一致的情况。

- 不能解决 fail-recover 的问题：

  由于 3PC 有超时机制的存在，2PC 中未解决的问题，参与者和协调者同时 down 掉，也就解决了。一旦参与者在超时时间内没有收到协调者的消息，就会自己提交。这样也能避免参与者一直占用共享资源。但是其在网络分区的情况下，不能保证数据的一致性

## 寻求平衡

**对于分布式系统而言，分区容错性是最基本的要求，而不是可选项**。

放弃可用性：遇到节点或网络故障时，为了保证一致性，服务将降级或不可用

放弃一致性：为了保证可用性，不再追求数据的实时一致性，只承诺最终的一致性

## BASE 理论

基本可用(Basically Available)：发生故障时损失部分功能或响应时间
软状态(Soft state)：允许数据存在中间状态，即允许数据同步过程存在延时
最终一致性(Eventually consistent)：所有数据副本经过同步最终达到一致状态

## NWR 理论

- N：数据副本的数量
- W：写请求需要写入的副本数量
- R：读请求需要读取的副本数量

通常要求 `W>N/2`

当 `W+R>N` 时，可以认为系统是强一致性的
当 `W+R<=N` 时，可以认为系统是弱一致性的