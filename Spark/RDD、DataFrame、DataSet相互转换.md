RDD

```scala
val rdd=spark.sparkContext.textFile("D:\\test\\people.txt")

// 方法一：先转换成元组，再使用toDF添加Schema信息
val rdd1=rdd.map(ele=>(ele.split(",")(0),ele.split(",")(1).trim.toInt))
rdd1.toDF("name","age")//转df，指定字段名
rdd1.toDS()//转ds

// 方法二：定义样例类，再通过toDF直接转换样例类对象
case class Person(name:String,age:Int)
val rdd2=rdd.map(_.split(","))
.map(ele=>(Person(ele(0),ele(1).trim.toInt))).toDF()//将自定义类转df

// 方法三
val schemaString="name age"
val fields=schemaString.split(" ")
    .map(ele=>StructField(ele,StringType,nullable = true))
val schema=StructType(fields)
val rdd3=rdd.map(_.split(",")).map(ele=>Row(ele(0),ele(1).trim))
val df=spark.createDataFrame(rdd3,schema)//将StructType作用到rdd上，转df
```

DataFrame

```scala
val df=spark.sql("select date,status,api from data.api")

// 转换成RDD
df.rdd

//df转ds就太多了，先列举几个
df.as() map filter flatMap等等吧
```

DataSet

```scala
// 转换成RDD
ds.rdd
```