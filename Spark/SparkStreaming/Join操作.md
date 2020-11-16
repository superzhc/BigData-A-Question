# Join 操作

Spark Streaming 支持执行不同类型的 join。

## Stream-stream joins

Streams（流）可以非常容易地与其他流进行 join。

**Scala 版本**

```scala
val stream1: DStream[String, String] = ...
val stream2: DStream[String, String] = ...
val joinedStream = stream1.join(stream2)
```

**Java 版本**

```java
JavaPairDStream<String, String> stream1 = ...
JavaPairDStream<String, String> stream2 = ...
JavaPairDStream<String, Tuple2<String, String>> joinedStream = stream1.join(stream2);
```

这里，在每个 batch interval（批间隔）中，由 stream1 生成的 RDD 将与 stream2 生成的 RDD 进行 jion. 也可以做 leftOuterJoin，rightOuterJoin，fullOuterJoin。 此外，在 stream（流）的窗口上进行 join 通常是非常有用的. 这也很容易做到.

**Scala 版本**

```scala
val windowedStream1 = stream1.window(Seconds(20))
val windowedStream2 = stream2.window(Minutes(1))
val joinedStream = windowedStream1.join(windowedStream2)
```

**Java 版本**

```java
JavaPairDStream<String, String> windowedStream1 = stream1.window(Durations.seconds(20));
JavaPairDStream<String, String> windowedStream2 = stream2.window(Durations.minutes(1));
JavaPairDStream<String, Tuple2<String, String>> joinedStream = windowedStream1.join(windowedStream2);
```

## Stream-dataset joins

示例：join window stream（窗口流）与 dataset 的例子

**Scala 版本**

```scala
val dataset: RDD[String, String] = ...
val windowedStream = stream.window(Seconds(20))...
val joinedStream = windowedStream.transform { rdd => rdd.join(dataset) }
```

**Java 版本**

```java
JavaPairRDD<String, String> dataset = ...
JavaPairDStream<String, String> windowedStream = stream.window(Durations.seconds(20));
JavaPairDStream<String, String> joinedStream = windowedStream.transform(rdd -> rdd.join(dataset));
```

实际上，也可以动态更改要加入的 dataset。 提供给 transform 的函数是每个 batch interval（批次间隔）进行评估，因此将使用 dataset 引用指向当前的 dataset。