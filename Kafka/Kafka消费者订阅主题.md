## 消费者订阅

消费者通过 `subscribe()` 和 `assign()` 两种方式订阅主题

### `subscribe()`

使用 `subscribe()` 可以订阅一个或多个主题，对于这个方法而言，可以以集合的方式订阅多个主题，也可以以正则表达式的形式订阅特定模式的主题。

subscribe 的几个重载方法如下：

```java
public void subscribe(Collection<String> topics)
public void subscribe(Pattern pattern, ConsumerRebalanceListener listener)
public void subscribe(Pattern pattern)
public void subscribe(Pattern pattern, ConsumerRebalanceListener listener)
```

对于消费者以集合的方式订阅主题，如果前后两次订阅了不同的主题，以最后一次的订阅为准，前面的订阅都会失效，如：

```java
consumer.subscribe(Arrays.asList("topic1"));
consumer.subscribe(Arrays.asList("topic2"));
```

上述的示例，最终订阅的是 topic2，由此可以看出来 topic1 订阅失效了。

如果以正则表达式的方式订阅主题，在之后的过程中，如果新创建了新的主题，并且主题的名称与正则表达式相匹配，那么这个消费者就可以消费到这个新添加的主题中的消息。

示例如下：

```java
consumer.subscribe(Pattern.compile("superz-*"));
```

###  `assign()`

使用 `assign()` 可以让消费者为自己**分配**分区

`assign()` 的方法定义如下：

```java
public void assign(Collection<TopicPartition> partitions)
```

### 两者的区别

分区分配的区别：

- `subscribe()` 是有 Kafka 内部算法为消费者自动分配分区
- `assign()` 则需要开发者手动为消费者指定消费的分区

通过分区分配的区别可以看出来，采用 `subscribe()` 方式的订阅，多个消费者之间消费的消息不会重复，且所有消费者消费的消息是一个主题的全部消息；但使用 `assign()` 方式的订阅，在位移未提交的情况下，多个消费者订阅相同的主题分区，消费到的消息是完全一样的。

建议：`assign()` 与 `subscribe()` 不要混用

因为 `assign()` 、`subscribe()` 订阅并配置 `enable.auto.commit=true` 的情况下，`poll()` 会提交偏移量，这样会造成 `assign()`、`subscribe()` 都会对同一个主题分区提交偏移量，这样的偏移量对其中的一些订阅是有问题的。

## 消费者取消订阅

在 KafkaConsumer 中可以使用 `unsubscribe()` 方法来取消主题的订阅。

这个方法可以取消如下的订阅方式：

- 以 `subscribe(Collection)` 方式实现的订阅
- 以 `subscribe(Pattern)` 方式实现的订阅
- 以 `assign()` 方式实现的订阅

使用方式的示例如下：

```java
consumer.unsubscribe();
```

如果将 `subscribe(Collection)` 或  `assign()` 中的集合参数设置为空集合，也可以实现取消订阅，以下三种方式都可以取消订阅：

```java
consumer.unsubscribe();
consumer.subscribe(new ArrayList<String>());
consumer.assign(new ArrayList<TopicPartition>());
```

