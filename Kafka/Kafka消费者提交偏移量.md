偏移量的作用，一个消费者组消费一个主题的过程中，如果有消费者发生崩溃或者有新的消费者加入群组，就会触发再均衡，完成再均衡后，每个消费者可能会分配到新的分区，为了能够继续之前的消息读取，消费者需要一个记录上一次读取到的位置的偏移量，之后从这个偏移量指定的位置开始读取。

在新版 Kafka 中，消费者提交偏移量是通过向 `__consumer_offset` 特殊主题发送包含每个分区的偏移量来实现的。

KafkaConsumer API 提供了多种方式来提交偏移量：

- 自动提交
- 提交当前偏移量
- 异步提交
- 同步和异步组合提交
- 提交特定的偏移量

### 自动提交

如果 `enable.auto.commit` 被设为 true，根据 ` auto.commit.interval.ms ` 设置的提交时间间隔，消费者会自动把从 `poll()` 方法接收到的最大偏移量提交上去。

自动提交是在轮询中进行的，消费者每次在进行轮询时会检查是否该提交偏移量了，如果是，就会提交从上一次轮询返回的偏移量。

自动提交方式虽然使用简单，但是因为是周期性的提交时间间隔，可能会发生在某个时间间隔内读取的数据的偏移量未提交，发生了再均衡，这样造成读取的数据重复，且自动提交并没有办法去避免重复处理消息，即使将时间间隔减小，但也没办法完全避免。

### 提交当前偏移量

消费者 API 提供了另一种提交偏移量的方式，开发者可以在必要的时候提交当前偏移量。

需要将 `enable.auto.commit` 设为 false，让应用程序使用 `commitSync()` 来决定何时提交偏移量，这个 API 会提交由 `poll()` 方法返回的最新偏移量，提交成功后马上返回，如果提交失败就抛出异常。

### 异步提交

`commitSync()` 提交有一个不足，在 broker 对提交请求作出响应之前，应用程序会一直阻塞，这样会限制应用程序的吞吐量。这个时候可以使用异步提交 API，只需要发送提交请求，无需等待 broker 的响应。

在成功提交或碰到无法提交的错误之前，`commitSync()` 会一直尝试，但 `commitAsync()` 不会，这是因为如果它在得不到服务器响应的时候，进行重试，可能会覆盖掉已经提交成功的更大的偏移量，这个时候再发生再均衡，就会出现重复消息。

`commitAsync()` 方法也支持回调，在 broker 作出响应时会执行回调，回调经常被用于记录提交错误或生成度量指标。

### 同步和异步组合提交

在关闭消费者或者再均衡前的最后一次提交，必须要确保提交成功，因此，再消费者关闭前一般会组合使用 `commitAsync()` 和 `commitSync()`。它的工作原理如下：

```java
try{
    while(true){
        consumer.poll(Duration.ofSeconds(1));
        // ....        
        consumer.commitAsync();
    }
}catch(Exception e){
    // ...
}finally{
    try{
        consumer.commitSync();
    }finally{
        consumer.close();
    }
}
```

先使用 `commitAsync()` 方法来提交，这样的速度更快，而且即使这次提交失败，但下次可能会成功，直到关闭消费者，没有所谓的下一次提交了，使用 `commitSync()` 会一直重试，知道提交成功或发生无法恢复的错误。

### 提交特定的偏移量

消费者 API 允许在调用 `commitSync()` 和 `commitAsync()` 方法时传入提交的分区和偏移量的 map，方法的定义如下：

```java
public void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets)
public void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets, final Duration timeout)
public void commitAsync(final Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback)
```



