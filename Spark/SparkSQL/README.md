# Spark SQL

Spark SQL 是 Spark 中用于处理结构化数据的模块。它与基本的 Spark RDD API 不同的地方在于其接口提供了更多关于结构化数据的信息，能够更好地应用于计算过程。这些额外的信息也能够帮助系统进行优化，从而提高计算的性能。

Spark SQL 允许 Spark 执行用 SQL, HiveQL 或者 Scala 表示的关系查询。这个模块的核心是一个新类型的 RDD-[SchemaRDD](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SchemaRDD)。SchemaRDDs 由[行](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.package@Row:org.apache.spark.sql.catalyst.expressions.Row.type)对象组成，行对象拥有一个模式（scheme）来描述行中每一列的数据类型。SchemaRDD与关系型数据库中的表很相似。可以通过存在的RDD、一个[Parquet](http://parquet.io/)文件、一个JSON数据库或者对存储在[Apache Hive](http://hive.apache.org/)中的数据执行HiveSQL查询中创建。

Spark中所有相关功能的入口点是[SQLContext](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext)类或者它的子类，创建一个SQLContext的所有需要仅仅是一个SparkContext。

```scala
val sc: SparkContext // An existing SparkContext.
val sqlContext = new org.apache.spark.sql.SQLContext(sc)

// createSchemaRDD is used to implicitly convert an RDD to a SchemaRDD.
import sqlContext.createSchemaRDD
```

除了一个基本的 SQLContext，也能够创建一个 HiveContext，它支持基本 SQLContext 所支持功能的一个超集。它的额外的功能包括用更完整的 HiveQL 分析器写查询去访问 HiveUDFs 的能力、从 Hive 表读取数据的能力。用 HiveContext 不需要一个已经存在的 Hive 开启，SQLContext可用的数据源对HiveContext也可用。HiveContext分开打包是为了避免在Spark构建时包含了所有的Hive依赖。如果对你的应用程序来说，这些依赖不存在问题，Spark 1.2推荐使用HiveContext。以后的稳定版本将专注于为SQLContext提供与HiveContext等价的功能。

用来解析查询语句的特定SQL变种语言可以通过`spark.sql.dialect`选项来选择。这个参数可以通过两种方式改变，一种方式是通过`setConf`方法设定，另一种方式是在SQL命令中通过`SET key=value`来设定。对于SQLContext，唯一可用的方言是“sql”，它是Spark SQL提供的一个简单的SQL解析器。在HiveContext中，虽然也支持"sql"，但默认的方言是“hiveql”。这是因为HiveQL解析器更完整。在很多用例中推荐使用“hiveql”。

