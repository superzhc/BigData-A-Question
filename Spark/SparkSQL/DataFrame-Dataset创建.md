# DataFrame/Dataset 创建

DataFrame 的定义与 RDD 类似，即都是 Spark 平台用以分布式并行计算的不可变分布式数据集合。但 DataFrame 只是针对结构化数据源的高层数据抽象，且在 DataFrame 对象的创建过程中必须指定数据集的结构信息（Schema）。

DataFrame 是 Spark SQL 模块所需处理的结构化数据的核心对象，即在 Spark 程序中若想要使用简易的 SQL 接口对数据进行分析，首先需要将所处理的数据源转化为 DataFrame 对象，进而在 DataFrame 对象上调用各种 API 来实现需求。

DataFrame 可以从许多结构化数据源加载并构造得到，如：结构化数据文件，Hive 中的表，外部数据库等等。

> 官方文档中，在 Java 和 Scala 中，DataFrame 其实就是 `DataSet[Row]`，即表示每一行内容的 Row 对象组成的 DataSet 对象，因此 DataSet 的 API 是适用于 DataFrame的。

## Scala 版本创建 DataFrame

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

## Java 版本创建 DataFrame

> 对于 Java 版本来说，使用的都是 `Dataset<T>`，这个类实质上跟 DataFrame 是一样的

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