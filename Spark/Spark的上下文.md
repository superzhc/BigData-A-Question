# SparkContext

SparkContext 是 Spark 编程的主入口点，SparkContext 负责与 Spark 集群的连接，可以被用于在集群上创建 RDDs、累加器（accumulators）和广播变量（broadcast variables）。

> 在 `spark-shell` 中，SparkContext 已经被系统默认创建以供用户使用，变量为 sc。

**Scala 版本**

```scala
val conf:SparkConf = new SparkConf().setAppName("Demo").setMaster("local")
val sc:SparkContext=new SparkContext(conf)
```

**Java 版本**

```java
SparkConf conf=new SparkConf().setAppName("Demo").setMaster("local");
JavaSparkContext jsc=new JavaSparkContext(conf);
```

[SparkSession](SparkSQL/SparkSession.md ':include')

[StreamingContext](SparkStreaming/StreamingContext.md ':include')