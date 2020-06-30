DataFrame的定义与RDD类似，即都是Spark平台用以分布式并行计算的不可变分布式数据集合。但DataFrame只是针对结构化数据源的高层数据抽象，且在DataFrame对象的创建过程中必须指定数据集的结构信息（Schema）。

DataFrame是Spark SQL模块所需处理的结构化数据的核心对象，即在Spark程序中若想要使用简易的SQL接口对数据进行分析，首先需要将所处理的数据源转化为DataFrame对象，进而在DataFrame对象上调用各种API来实现需求。

DataFrame可以从许多结构化数据源加载并构造得到，如：结构化数据文件，Hive中的表，外部数据库等等。

> 官方文档中，在Java和Scala中，DataFrame其实就是`DataSet[Row]`，即表示每一行内容的Row对象组成的DataSet对象，因此DataSet的API是适用于DataFrame的。

### 将RDDs转化为DataFrame


> 注意：并不是由任意类型对象组成的RDD都可以转化为DataFrame对象，只有当组成`RDD[T]`的每一个T对象内部具有公有且鲜明的字段结构时，才能隐式或显式地总结出创建DataFrame对象所必要的结构信息（Schema）进行转化。

Spark SQL支持将现有RDDs转换为DataFrame的两种不同方法，其实也就是**隐式推断**或者**显式指定DataFrame对象的Schema**。

1. 使用反射机制（Reflection）推理出Schema（结构信息）
    采用这种方式转化为DataFrame对象，要求被转化的RDD[T]的类型T具有典型一维表的字段结构对象。
    Spark SQL的Scala接口支持自动将包含样例类对象的RDD转换为DataFrame对象。在样例类的声明中已预先定义了表的结构信息，内部通过反射机制即可读取样例类的参数的名称、类型，转化为DataFrame对象的Schema。
2. 由开发者指定Schema
    先构建一个Schema，然后将其应用到现有的RDD[Row]。这种方式可以根据需求和数据结构构建Schema，而且需要将RDD[T]转化为Row对象组成的RDD(RDD[Row])，这种方法的代码虽然多了点，但也提供了更高的自由度和灵活度。

自定义指定Schema的步骤：

1. 根据需求从源RDD转化为RDD[Row]
2. 创建由符合在步骤1中创建的RDD中的Rows结构的StructType表示的模式
3. 通过SparkSession提供的createDataFrame方法将模式应用于行的RDD

示例：

```scala
import org.apache.spark.sql.type._

val peopleRDD=sparkSession.sparkContext.textFile("/home/test/people.txt")

// 创建Schema
val schemaString="name age"
// 封装成Array[StructField]
val fields=schemaString.split(" ").map(fieldName=>StructField(fieldName,StringType,nullable=true))
// 将fields强制转换成StructType对象，形成了真正可用于构建DataFrame对象的Schema
val schema=StructType(fields)

// 将RDD[String]转换为RDD[Row]
val rowRDD=peopleRDD.map(_.split(",")).map(attribute=>Row(attribute(0),attribute(1)))

// 将schema应用到rowRDD上，完成DataFrame的转换
val peopleDF=sparkSession.createDataFrame(rowRDD,schema)

// 对DataFrame进行操作
peopleDF.createOrReplaceTempView("people")
val result=sparkSession.sql("select name,age from people")
result.show()
```