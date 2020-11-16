# StreamingContext <!-- {docsify-ignore} -->

为了初始化 Spark Streaming 程序，一个 StreamingContext 对象必需被创建，它是 Spark Streaming 所有流操作的主要入口。

在创建 StreamingContext 时需要指明 sparkConf 和 batchDuration(批次时间)，Spark 流处理本质是将流数据拆分为一个个批次，然后进行微批处理，batchDuration 就是批次拆分的时间间隔。这个时间可以根据业务需求和服务器性能进行指定，如果业务要求低延迟并且服务器性能也允许，则这个时间可以指定得很短。

一个 StreamingContext 对象可以用 SparkConf 对象创建：

**Scala 版本**

```scala
import org.apache.spark._
import org.apache.spark.streaming._
val conf = new SparkConf().setAppName(appName).setMaster(master)
val ssc = new StreamingContext(conf, Seconds(1))
```

**Java 版本**

```java
import org.apache.spark.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;

// Create a local StreamingContext with two working thread and batch interval of 1 second
SparkConf conf = new SparkConf().setAppName(appName).setMaster(master);
JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
```

> 注意：StreamingContext 在内部创建了一个 SparkContext 对象，可以通过 `ssc.sparkContext` 访问这个 SparkContext 对象。

也可以利用已经存在的 `SparkContext` 对象创建 `StreamingContext` 对象：

**Scala 版本**

```scala
import org.apache.spark.streaming._
val sc = ...                // existing SparkContext
val ssc = new StreamingContext(sc, Seconds(1))
```

**Java 版本**

```java
import org.apache.spark.streaming.api.java.*;

JavaSparkContext sc = ...   //existing JavaSparkContext
JavaStreamingContext ssc = new JavaStreamingContext(sc, Durations.seconds(1));
```

当一个上下文（StreamingContext）定义之后，用户必须按照以下几步进行操作：

- 定义输入源；
- 准备好流计算指令；
- 利用 `streamingContext.start()` 方法接收和处理数据；
- 处理过程将一直持续，直到 `streamingContext.stop()` 方法被调用。

## 注意事项

- 一旦一个 StreamingContext 已经启动，就不能有新的流算子建立或者是添加到 StreamingContext 中。
- 一旦一个 StreamingContext 已经停止，它就不能再重新启动
- 在 JVM 中，同一时间只能有一个 StreamingContext 处于活跃状态
- 在 StreamingContext 上调用 `stop()` 方法，也会关闭 SparkContext 对象。如果只想仅关闭 StreamingContext 对象，设置 `stop()` 的可选参数为 false
- 一个 SparkContext 对象可以重复利用去创建多个 StreamingContext 对象，前提条件是前面的 StreamingContext 在后面 StreamingContext 创建之前关闭（不关闭 SparkContext）。