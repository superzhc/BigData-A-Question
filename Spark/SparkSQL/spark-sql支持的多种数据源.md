## 通用load/save函数

Spark SQL的默认数据源格式为**parquet**格式。数据源为Parquet文件时，Spark SQL可以方便地进行读取，甚至可以直接在Parquet文件上执行查询操作。修改配置项**`spark.sql.sources.default`**，可以修改默认数据源格式。

示例：【通过通用的load/save方法对parquet文件进行读取、存储】

```scala
// 读取
val df=sparkSeesion.read.load("/home/user/users.parquet")
// 存储
df.select("name","age").write.save("/home/user/nameAndAge.parquest")
```

## 手动指定选项

当数据源不是配置的默认格式（如：Parquet）的文件时，需要手动指定数据源的格式。数据源格式需指定全名（如：`org.apache.spark.sql.parquet`），如果数据源为内置格式，则只需指定简称（json，parquet，jdbc，orc，libsvm，csv，text）即可。通过指定数据源格式名，还可以对DataFrame进行类型转换操作。

示例：【将原为JSON格式的数据源转储为Parquet格式文件】

```scala
val df=spark.read.format("json").load("/home/user/user.json")
df.select("name","age").write.format("parquet").save("/home/user/userandages.parquet")
```

## 在文件上直接进行SQL查询

相比于使用read API将文件加载到DataFrame并对其进行查询，还可以使用SQL直接查询该文件。

示例：

```scala
val df=sparkSession.sql("select name,age from parquet.`/home/user/user.parquet`")
```

注：在使用SQL直接查询Parquet文件时，需加`parquet.`标识符和Parquet文件所在路径

## 存储模式

保存操作可以选择使用**存储模式**（SaveMode），从而指定如何处理现有数据（如果存在），存储模式如下：

|           Scala/Java            |   Any Language   |                                              Meaning                                               |
| ------------------------------- | ---------------- | -------------------------------------------------------------------------------------------------- |
| SaveMode.ErrorIfExists(default) | "error"(default) | 将DataFrame保存到数据源时，如果数据已经存在，则会抛出异常                                              |
| SaveMode.Append                 | "append"         | 将DataFrame保存到数据源时，如果数据/表已经存在，则DataFrame的内容将被附加到现有数据中                    |
| SaveMode.Overwrite              | "overwrite"      | 覆盖模式意味着将DataFrame保存到数据源时，如果数据表已经存在，则预期DataFrame的内容将覆盖现有数据          |
| SaveMode.Ignore                 | "ignore"         | 忽略模式意味着当将DataFrame保存到数据源时，如果数据已经存在，则不会保存DataFrame的数据，并且不更改现有数据 |

通过`mode()`方法设置数据写入指定文件的存储模式。

示例：

```scala
df.select("name","age").write().mode(SaveMode.Append).save("/home/user/userAndParquet.parquet")
```