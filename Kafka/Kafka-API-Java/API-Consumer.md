### 1、引入依赖

maven 引入依赖，如下:

```xml
<dependency>
	<groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2、Consumer 初始化

Consumer 使用一个 Properties file 来进行构建。以下提供了使用 Consumer Group 所需要的最小配置

```java
Properties props = new Properties();
props.put("bootstrap.servers", "192.168.186.48:6667");
props.put("group.id", "superz");
props.put("key.deserializer", StringDeserializer.class.getName());
props.put("value.deserializer", StringDeserializer.class.getName());
Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);
```

- 需要为 Consumer 提供一个 broker 的初始化列表，以用于发现集群中其他的 broker，这个配置并不需要提供所有的 broker，client 会从给定的配置中发现当前所有存活的 broker。
- 为了加入 Consumer Group，需要配置一个 Group Id
- Consumer 需要配置反序列化 message key 和 value

### 3、Topic 订阅

为了开始消费，必须指定应用需要订阅的 topics，订阅代码如下：

```java
consumer.subscribe(Arrays.asList(“foo”, “bar”));
```

**订阅之后，Consumer 会与组内其他 Consumer 协调来获取其分区分配**。

**subscribe 方法并不是递增的：必须包含所有想要消费的 topics。用户可以在任何时刻改变想要消费的 topic 集，当调用 subscribe 时，之前订阅的 topics 列表会被新的列表取代**。

### 4、基本的 Poll 事件循环

Consumer 需要能够**并行获取数据**，在众多 brokers 中**获取多个 topic 的多个分区消息**。为了实现这个目的，Consumer API 被设计成风格**类似 unix 中的 poll 或者 select 调用**：一旦 topic 注册了，所有的 coordination，rebalancing 和 data fetch 都是由一个处于循环中的 poll 调用来驱动。

Consumer 客户端 jar 包已完成大量复杂的工作，用户只需要调用 poll 方法来获取消息。每一次 poll 调用都会返回从所分配的 partition 获取的一组消息（也许是空的）。

示例：展示一个基本的 poll 循环，打印获取的 records 的 offset 和 value

```java
try {
  while (running) {
    ConsumerRecords<String, String> records = Consumer.poll(1000); // 超时时间1000毫秒
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + ": " + record.value());
  }
} finally {
  Consumer.close();
}
```

poll API 根据当前的位置返回 records，当 Group 第一次创建时，消费开始的位置会被根据 reset policy（一般设置成从每个分区的最早的 offset 或者最新的 offset 开始）来设置。只要 Consumer 开始提交 offset ，那么之后的 rebalance 都会重置消费开始位置到最新的被提交的 offset。

传递给 poll 的参数是 Consumer 在当前位置等待有 record 返回时需要被阻塞的时间。一旦有 record 时，Consumer 会立即返回，如果没有 record，它将会等待直到超时。

### 5、【可选】消费者存活

每个 Consumer 都是被分配它所订阅的 topics 中所有 partitions 的一部分 partition。**只要 Consumer 持有的这部分 partition，组内其他成员就无法从这些分区中读取数据**，这样就能够避免重复消费，但是如果消费者因为机器或者应用的故障挂掉了，Kafka 是通过什么方法将分区分配给其他 Consumer 的？`Kafka group coordination 协议`通过心跳机制来解决这个问题的。在每次重新分配之后，当前所有的成员会周期性发送心跳给 Group coordinator。只要 coordinator 接收到心跳，它就会假设成员是存活的。每次接收到心跳，coordinator 都会开始（或重置）一个 timer。当 timer 过期时，如果还没有接收到心跳，coordinator 会将这个成员标记为挂掉，然后通知其他组内成员需要重新分配分区。timer 的时间被称为 `session timeout`，由客户端的 `session.timeout.ms` 进行设置。

```java
props.put("session.timeout.ms","60000");
```

session timeout 能够保证，当机器或者应用崩溃，或者网络分区导致 Consumer 与 coordinator 分隔开来时，会去释放出现问题的 Consumer 持有的 partition。

注：如果用户处理消息的时间比 session timeout 要长的话，那么会使得 coordinator 认为 Consumer 已经挂掉了，导致 partition 重新分配。用户可已经将 session timeout 设置的足够大，以使得这种情况不会发生。默认 session timeout 是 30s，但是将它设置成几分钟是不可行的，这样就会导致 coordinator 需要更长的时间来检测真正的 Consumer 崩溃。

### 6、【可选】offset 提交

Kafka 为每个分区的每条消息保持偏移量的值，这个偏移量是该分区中一条消息的唯一标识符。也表示消费者在这个分区的位置。

当一个 Consumer Group 被建立时，最开始消费的 offset 位置是由 `auto.offset.reset` 配置来控制的，一旦 Consumer 开始消费，它会根据应用的需要自动提交 offset。**每次 rebalance，消费开始的 position 都会被设置成分区最后提交的 offset。如果 Consumer 在为已经成功处理的 message 提交 offset 之前崩溃了，那么重新分配到这个分区的 Consumer 会重复消费这些消息。**所以，提交的频率越频繁，那么因为奔溃而造成的重复消费就会越少。

当用户将 `enable.auto.commit` 设置为 ture（默认是 true），Consumer 会根据 `auto.commit.interval.ms` 的设置来周期性自动触发 offset 提交。通过减少 commit 间隔，可以减少在崩溃之后消费者需要重新处理的 message 数量。

### 7、【可选】使用手动分区分配

用户可以只需要分配想要读取数据的 partion，然后开始 poll data 就可以了。

示例：将一个 topic 的所有分区都分配给一个 Consumer

```java
List<TopicPartition> partitions = new ArrayList<>();
for (PartitionInfo partitionInfo : consumer.partitionsFor(TOPIC)) {
    partitions.add(new TopicPartition(TOPIC, partitionInfo.partition()));
}
consumer.assign(partitions);
```

和 subscribe 方法类似，assign 方法需要传递想要读取的 partition 的列表，一旦 partition 被分配了，poll 的工作方式和之前一样。

