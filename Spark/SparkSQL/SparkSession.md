# SparkSession

Spark SQL 模块的编程主入口是 **SparkSession**，SparkSession 对象不仅为用户提供了创建 DataFrame 对象、读取外部数据源并转化为 DataFrame 对象以及执行 sql 查询的 API，还负责记录着用户希望 Spark 应用如何在 Spark 集群运行的控制、调优参数，是 Spark SQL 的上下文环境，是运行的基础。

> 在 `spark-shell` 中，SparkSession 已经被系统默认创建以供用户使用，变量为 spark。

可以通过 `SparkSession.builder()` 创建一个基本 SparkSession 对象，示例代码如下：

```scala
import org.apache.spark.sql.SparkSession

val sparkSession=SparkSession
    .builder()
    .appName("Spark SQL应用实例")
    .config("spark.some.config.option","some-value")
    .getOrCreate()

// 引入 spark.implicits._，以便于RDDs和DataFrame之间的隐式转换
import sparkSession.implicits._
```

> 注：**Java 版本的创建方式和 Scala 版本的创建方式是一样的，Spark 官网并没有专门提供类似 JavaSparkContext 的 JavaSparkSession**

sparkSession的一些重要的变量和方法：

![sparkSession的变量和方法](../images/1576825462_20191220150412570_4950.png)

Spark SQL内部使用DataFrame和DataSet来表示一个数据集合，然后就可以再这个数据集合上应用各种统计函数和算子。DataFrame和Dataset的关系是，**DataFrame就是一种类型为Row的Dataset**，即:

```scala
type DataFrame=Dataset[Row]
```


执行SQL查询

SparkSession为用户提供了直接执行sql语句的`SparkSession.sql(sqlText:String)`方法，sql语句可直接作为字符串传入`sql()`方法中，sql查询所得到的结果依然为DataFrame对象。在Spark SQL模块上直接执行sql语句的查询需要首先将标志着结构化数据源的DataFrame对象注册成临时表，进而在sql语句中对该临时表进行查询操作，具体的步骤如下：

```scala
df.createOrReplaceTempView("student")
val sqlDF=sparkSession.sql("select name,age from student")
sqlDF.show()
```