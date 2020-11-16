# DataFrame/Dataset 创建和存储

DataFrame 的定义与 RDD 类似，即都是 Spark 平台用以分布式并行计算的不可变分布式数据集合。但 DataFrame 只是针对结构化数据源的高层数据抽象，且在 DataFrame 对象的创建过程中必须指定数据集的结构信息（Schema）。

DataFrame 是 Spark SQL 模块所需处理的结构化数据的核心对象，即在 Spark 程序中若想要使用简易的 SQL 接口对数据进行分析，首先需要将所处理的数据源转化为 DataFrame 对象，进而在 DataFrame 对象上调用各种 API 来实现需求。

DataFrame 可以从许多结构化数据源加载并构造得到，如：结构化数据文件，Hive 中的表，外部数据库等等。

> 官方文档中，在 Java 和 Scala 中，DataFrame 其实就是 `DataSet[Row]`，即表示每一行内容的 Row 对象组成的 DataSet 对象，因此 DataSet 的 API 是适用于 DataFrame 的。

## 从 RDD 转换成 DataFrame/DataSet

**Scala 版本 **

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
```

**Java 版本**

```java
import java.util.Arrays;
import java.util.Collections;
import java.io.Serializable;
 
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
 
public static class Person implements Serializable {
  private String name;
  private int age;
 
  public String getName() {
    return name;
  }
 
  public void setName(String name) {
    this.name = name;
  }
 
  public int getAge() {
    return age;
  }
 
  public void setAge(int age) {
    this.age = age;
  }
}
 
// 创建一个Person对象
Person person = new Person();
person.setName("Andy");
person.setAge(32);
 
// 创建Java beans的Encoders
Encoder<Person> personEncoder = Encoders.bean(Person.class);
Dataset<Person> javaBeanDS = spark.createDataset(
  Collections.singletonList(person),
  personEncoder
);
javaBeanDS.show();
// +---+----+
// |age|name|
// +---+----+
// | 32|Andy|
// +---+----+
 
// Encoders类提供了常见类型的Encoders
Encoder<Integer> integerEncoder = Encoders.INT();
Dataset<Integer> primitiveDS = spark.createDataset(Arrays.asList(1, 2, 3), integerEncoder);
Dataset<Integer> transformedDS = primitiveDS.map(value -> value + 1, integerEncoder);
transformedDS.collect(); // 返回 [2, 3, 4]
```

## 通用 load/save 函数

### 默认文件的读取

Spark SQL 的默认数据源格式为 **parquet** 格式。数据源为 Parquet 文件时，Spark SQL 可以方便地进行读取，甚至可以直接在 Parquet 文件上执行查询操作。修改配置项 **`spark.sql.sources.default`**，可以修改默认数据源格式。

示例：【通过通用的load/save方法对parquet文件进行读取、存储】

```scala
// 读取
val df=sparkSeesion.read.load("/home/user/users.parquet")
// 存储
df.select("name","age").write.save("/home/user/nameAndAge.parquest")
```

### 指定文件格式的读取

当数据源不是配置的默认格式（如：Parquet）的文件时，需要手动指定数据源的格式。数据源格式需指定全名（如：`org.apache.spark.sql.parquet`），如果数据源为内置格式，则只需指定简称（json，parquet，jdbc，orc，libsvm，csv，text）即可。通过指定数据源格式名，还可以对DataFrame进行类型转换操作。

示例：【将原为JSON格式的数据源转储为Parquet格式文件】

```scala
val df=spark.read.format("json").load("/home/user/user.json")

df.select("name","age").write.format("parquet").save("/home/user/userandages.parquet")
```

## JSON 文件读取

**Scala 版本**

```scala
val df = spark.read.json("examples/src/main/resources/people.json")
```

**Java 版本**

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

Dataset<Row> df = spark.read().json("examples/src/main/resources/people.json");
```

## ~~在文件上直接进行SQL查询~~

相比于使用read API将文件加载到DataFrame并对其进行查询，还可以使用SQL直接查询该文件。

示例：

```scala
val df=sparkSession.sql("select name,age from parquet.`/home/user/user.parquet`")
```

注：在使用SQL直接查询Parquet文件时，需加`parquet.`标识符和Parquet文件所在路径

## 运行 SQL 语句生成 DataFrame

SparkSession 的 sql 函数可以让应用程序以编程的方式运行 SQL 查询, 并将结果作为一个 DataFrame 返回。

**Scala 版本**

```scala
// Register the DataFrame as a SQL temporary view
df.createOrReplaceTempView("people")

val sqlDF = spark.sql("SELECT * FROM people")
sqlDF.show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

**Java 版本**

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// Register the DataFrame as a SQL temporary view
df.createOrReplaceTempView("people");

Dataset<Row> sqlDF = spark.sql("SELECT * FROM people");
sqlDF.show();
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

## 存储模式

保存操作可以选择使用**存储模式**（SaveMode），从而指定如何处理现有数据（如果存在），存储模式如下：

| Scala/Java                      | Any Language     | Meaning                                                                                                    |
| ------------------------------- | ---------------- | ---------------------------------------------------------------------------------------------------------- |
| SaveMode.ErrorIfExists(default) | "error"(default) | 将DataFrame保存到数据源时，如果数据已经存在，则会抛出异常                                                  |
| SaveMode.Append                 | "append"         | 将DataFrame保存到数据源时，如果数据/表已经存在，则DataFrame的内容将被附加到现有数据中                      |
| SaveMode.Overwrite              | "overwrite"      | 覆盖模式意味着将DataFrame保存到数据源时，如果数据表已经存在，则预期DataFrame的内容将覆盖现有数据           |
| SaveMode.Ignore                 | "ignore"         | 忽略模式意味着当将DataFrame保存到数据源时，如果数据已经存在，则不会保存DataFrame的数据，并且不更改现有数据 |

通过`mode()`方法设置数据写入指定文件的存储模式。

示例：

```scala
df.select("name","age").write().mode(SaveMode.Append).save("/home/user/userAndParquet.parquet")
```