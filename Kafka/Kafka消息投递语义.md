# Kafka消息投递语义-消息不丢失，不重复，不丢不重

## 介绍

Kafka 支持 3 种消息投递语义：

- `At most once`——最多一次，消息可能会丢失，但不会重复
- `At least once`——最少一次，消息不会丢失，可能会重复
- `Exactly once`——只且一次，消息不丢失不重复，有且消费一次。

但是整体的消息投递语义需要 Producer 端和 Consumer 端两者来保证。

## Producer 消息生产者端

> 一个场景例子：
>
> 当 Producer 向 Broker 发送一条消息，这时网络出错了，Producer 无法得知 Broker 是否接受到了这条消息。网络出错可能是发生在消息传递的过程中，也可能发生在 Broker 已经接受到了消息，并返回 ACK 给 Producer 的过程中。

这时，Producer 只能进行重发，消息可能会重复，但是保证了 At least once。

0.11.0 的版本通过给每个 Producer 一个唯一 ID，并且在每条消息中生成一个 sequence num，这样就能对消息去重，达到 Producer 端的 exactly once。

这里还涉及到 Producer 端的 acks 设置和 Broker 端的副本数量，以及 `min.insync.replicas` 的设置。

```
比如 Producer 端的 acks 设置如下：
- acks=0 //消息发了就发了，不等任何响应就认为消息发送成功
- acks=1 //leader分片写消息成功就返回响应给producer
- acks=all（-1） //当acks=all， min.insync.replicas=2，就要求 INSRNC 列表中必须要有 2 个副本都写成功，才返回响应给 Producer，如果 INSRNC 中已同步副本数量不足 2，就会报异常，如果没有 2 个副本写成功，也会报异常，消息就会认为没有写成功。
```

## Broker 消息接收端

上文说过 `acks=1`，表示当 leader 分片副本写消息成功就返回响应给 Producer，此时认为消息发送成功。

如果 leader 写成功但马上挂了，还没有将这个写成功的消息同步给其他的分片副本，那么这个分片此时的 ISR 列表为空，如果`unclean.leader.election.enable=true`，就会发生 log truncation（日志截取），同样会发生消息丢失；如果`unclean.leader.election.enable=false`，那么这个分片上的服务就不可用了，Producer 向这个分片发消息就会抛异常。

所以设置 `min.insync.replicas=2`，`unclean.leader.election.enable=false`，Producer 端的 `acks=all`，这样发送成功的消息就绝不会丢失。

## Consumer 消息消费者端

所有分片的副本都有自己的 log 文件（保存消息）和相同的 offset 值。当 Consumer 没挂的时候，offset 直接保存在内存中，
如果挂了，就会发生负载均衡，需要 Consumer group 中另外的 Consumer 来接管并继续消费。

Consumer 消费消息的方式有以下 2 种：

1. consumer 读取消息，保存 offset，然后处理消息。
   现在假设一个场景：保存offset成功，但是消息处理失败，consumer 又挂了，这时来接管的 consumer 就只能从上次保存的 offset继续消费，这种情况下就有可能丢消息，但是保证了 at most once 语义。
2. consumer 读取消息，处理消息，处理成功，保存 offset。
   如果消息处理成功，但是在保存 offset 时，consumer 挂了，这时来接管的 consumer 也只能从上一次保存的 offset 开始消费，这时消息就会被重复消费，也就是保证了 at least once 语义。

以上这些机制的保证都不是直接一个配置可以解决的，而是你的 consumer 代码来完成的，只是一个处理顺序先后问题。

第一种对应的代码：

```java
List<String> messages = consumer.poll();
consumer.commitOffset();
processMsg(messages);
```

第二种对应的代码：

```java
List<String> messages = consumer.poll();
processMsg(messages);
consumer.commitOffset();
```

## Exactly Once 实现原理

### Producer 端的消息幂等性保证

每个 Producer 在初始化的时候都会被分配一个唯一的 PID，Producer 向指定的 Topic 的特定 Partition 发送的消息都携带一个 sequence number（简称seqNum），从零开始的单调递增的。

Broker 会将 Topic-Partition 对应的 seqNum 在内存中维护，每次接受到 Producer 的消息都会进行校验；只有 seqNum 比上次提交的seqNum 刚好大一，才被认为是合法的。比它大的，说明消息有丢失；比它小的，说明消息重复发送了。

以上说的这个只是针对单个 Producer 在一个 session 内的情况，假设 Producer 挂了，又重新启动一个 Producer 被而且分配了另外一个 PID，这样就不能达到防重的目的了，所以 Kafka 又引进了 Transactional Guarantees（事务性保证）。

### Transactional Guarantees 事务性保证

Kafka 的事务性保证说的是：同时向多个 Topic-Partitions 发送消息，要么都成功，要么都失败。

为什么搞这么个东西出来？我想了下有可能是这种例子：

用户定了一张机票，付款成功之后，订单的状态改了，飞机座位也被占了，这样相当于是2条消息，那么保证这个事务性就是：向订单状态的 Topic 和飞机座位的 Topic 分别发送一条消息，这样就需要 Kafka 的这种事务性保证。

这种功能可以使得 consumer offset 的提交（也是向 Broker 产生消息）和 Producer 的发送消息绑定在一起。用户需要提供一个唯一的全局性 TransactionalId，这样就能将 PID 和 TransactionalId 映射起来，就能解决 Producer 挂掉后跨 session 的问题，应该是将之前 PID 的 TransactionalId 赋值给新的 Producer。

### Consumer 端

以上的事务性保证只是针对的 Producer 端，对 Consumer 端无法保证，有以下原因：

1. 压实类型的 Topics，有些事务消息可能被新版本的 Producer 重写
2. 事务可能跨坐2个 log segments，这时旧的 segments 可能被删除，就会丢消息
3. 消费者可能寻址到事务中任意一点，也会丢失一些初始化的消息
4. 消费者可能不会同时从所有的参与事务的 Topic-Partitions 分片中消费消息

如果是消费 Kafka 中的 Topic，并且将结果写回到 Kafka中另外的 Topic，可以将消息处理后结果的保存和 offset 的保存绑定为一个事务，这时就能保证消息的处理和 offset 的提交要么都成功，要么都失败。

如果是将处理消息后的结果保存到外部系统，这时就要用到两阶段提交（tow-phase commit），但是这样做很麻烦，较好的方式是 offset 自己管理，将它和消息的结果保存到同一个地方，整体上进行绑定，