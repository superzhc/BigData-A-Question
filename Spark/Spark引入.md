Spark 1.2.0 使用 Scala 2.10 写应用程序，需要使用一个兼容的 Scala 版本(例如：2.10.X)。

写 Spark 应用程序时，需要添加 Spark 的 Maven 依赖，Spark 可以通过 Maven 中心仓库来获得：

```
groupId = org.apache.spark
artifactId = spark-core_2.10
version = 1.2.0 #根据实际需要指定版本
```

另外，如果需要访问 HDFS 集群，需要根据 HDFS 版本添加 `hadoop-client` 的依赖。一些公共的 HDFS 版本 tags 在第三方发行页面中被列出。

```
groupId = org.apache.hadoop
artifactId = hadoop-client
version = <your-hdfs-version>
```

最后，需要导入一些 Spark 的类和隐式转换到你的程序，添加下面的行就可以了：

```scala
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
```

