> 引言：一般使用 `poll()` 方法都是从各个分区的最新偏移量处开始处理消息，但有时候也需要从特定偏移量处开始获取数据。

需要从特定偏移量处开始消费可以使用 `seek()` 方法，该方法的定义如下：

```java
public void seek(TopicPartition partition, long offset)
public void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata)
```

注：在使用 `subscribe()` 订阅方式的时候，需要先调用一次 `poll()` 方法，完成消费者被自动分配分区，不然直接使用 `seek()` 会报错，因为直接使用的时候，还没有进行分区，这时是不能进行特定偏移量处的消费的。

KafkaConsumer 针对想从分区的起始位置开始读取消息，提供了更加简洁的方法，定义如下：

```java
public void seekToBeginning(Collection<TopicPartition> partitions)
```

对应的想从分区的消息的最新偏移量处开始消费数据，也提供了简洁的方法，定义如下：

```java
public void seekToEnd(Collection<TopicPartition> partitions)
```

