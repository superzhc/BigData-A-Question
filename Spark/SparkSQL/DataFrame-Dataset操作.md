# DataFrame/DataSet 的操作

Spark SQL 中的 DataFrame 类似于一张关系型数据表。在关系型数据库中对单表或进行的查询操作，在DataFrame中都可以通过调用其API接口来实现。

## Action

### `show`：展示数据

> 以表格的形式在输出中展示 DataFrame 中的数据，类似于 `select * from spark_sql_test` 的功能。

show方法有四种调用方式，分别为:

1. `show()`：只显示前20条记录

   ```scala
   df.show
   ```

2. `show(numRows: Int)`：显示numRows条

   ```scala
   df.show(3)
   ```

3. `show(truncate: Boolean)`：是否最多只显示20个字符，默认为true。

   ```scala
   df.show(false)
   df.show(true)
   ```

4. `show(numRows: Int, truncate: Boolean)`：综合前面的显示记录条数，以及对过长字符串的显示格式。

   ```scala
   df.show(3,false)
   ```

### `collect`：获取所有数据到数组

> 将 DataFrame 中的所有数据都获取到，并返回一个Array对象.

```scala
df.collect()
```

### `collectAsList`：获取所有数据到List

> 功能和collect类似，只不过将返回结构变成了List对象

```scala
df.chollectAsList()
```

### `describe(cols: String*)`：获取指定字段的统计信息

> 这个方法可以动态的传入一个或多个String类型的字段名，结果仍然为DataFrame对象，用于统计数值类型字段的统计值，比如count, mean, stddev, min, max等。

```scala
df.describe("c1" , "c2", "c4" ).show()
```

### `first, head, take, takeAsList`：获取若干行记录

这里列出的四个方法比较类似，其中：

1. `first`获取第一行记录
2. `head`获取第一行记录，`head(n: Int)`获取前n行记录
3. `take(n: Int)`获取前n行数据
4. `takeAsList(n: Int)`获取前n行数据，并以List的形式展现

以`Row`或者`Array[Row]`的形式返回一行或多行数据。`first`和`head`功能相同。

`take`和`takeAsList`方法会将获得到的数据返回到Driver端，所以使用这两个方法时需要注意数据量，以免Driver发生OutOfMemoryError

## Transformation

### `select`：获取指定字段值

> 根据传入的String/Column类型字段名，获取指定字段的值，以DataFrame类型返回

**Scala 版本**

```scala
df.select("name").show()

// This import is needed to use the $-notation
import spark.implicits._
df.select($"name", $"age" + 1).show()
```

**Java 版本**

```java
// Select only the "name" column
df.select("name").show();
// +-------+
// |   name|
// +-------+
// |Michael|
// |   Andy|
// | Justin|
// +-------+

// Select everybody, but increment the age by 1
df.select(col("name"), col("age").plus(1)).show();
// +-------+---------+
// |   name|(age + 1)|
// +-------+---------+
// |Michael|     null|
// |   Andy|       31|
// | Justin|       20|
// +-------+---------+
```

### `selectExpr`：可以对指定字段进行特殊处理

> 可以直接对指定字段调用UDF函数，或者指定别名等。传入String类型参数，得到DataFrame对象。

```scala
df.selectExpr("id" , "c3 as time" , "round(c4)" ).show(false)
```

### `where(conditionExpr: String)`：SQL语言中where关键字后的条件

> 传入筛选条件表达式，可以用and和or，得到DataFrame类型的返回结果

```scala
df.where("col1 = 1 and col2 = 2 or col3=4").show()
```

### `filter`：根据字段进行筛选

> 传入筛选条件表达式，得到DataFrame类型的返回结果。和where使用条件相同

**Scala 版本**

```scala
// This import is needed to use the $-notation
import spark.implicits._
df.filter($"age" > 21).show()
```

**Java 版本**

```java
// Select people older than 21
df.filter(col("age").gt(21)).show();
// +---+----+
// |age|name|
// +---+----+
// | 30|Andy|
// +---+----+
```

### `withColumnRenamed`：重命名DataFrame中的指定字段名

> 如果指定的字段名不存在，不进行任何操作。

```scala
df.withColumnRenamed("id","idx")
```

### `withColumn`：往当前DataFrame中新增一列

> `whtiColumn(colName:String,col:Column)` 方法根据指定 colName 往 DataFrame 中新增一列，如果 colName 已存在，则会覆盖当前列。

**Scala 版本**

```scala
df.withColumn("id2", df("id")).show()
```

**Java 版本**

```java
Dataset<Row> newDs = ds.withColumn("new_col",org.apache.spark.sql.functions.lit(1));
```

### `drop`：去除指定字段，保留其他字段

> 返回一个新的DataFrame对象，其中不包含去除的字段，一次只能去除一个字段。

```scala
df.drop("id")
df.drop(df("id"))
```

### `limit`：获取前n行数据

> limit方法获取指定DataFrame的前n行记录，得到一个新的DataFrame对象。和take与head不同的是，limit方法不是Action操作。

```scala
df.limit(3).show()
```

### `groupBy`：根据字段进行分组操作

> 根据字段进行group by操作
>
> groupBy方法有两种调用方式，可以传入String类型的字段名，也可传入Column类型的对象。

**Scala 版本**

```scala
df.groupBy("c1" )
df.groupBy(df("c1"))
```

**Java 版本**

```java
df.groupBy("c1" )
```

该方法得到的是`GroupedData`类型对象，在GroupedData的API中提供了group by之后的操作，比如:

- `max(colNames: String*)`方法，获取分组中指定字段或者所有的数字类型字段的最大值，只能作用于数字型字段
- `min(colNames: String*)`方法，获取分组中指定字段或者所有的数字类型字段的最小值，只能作用于数字型字段
- `mean(colNames: String*)`方法，获取分组中指定字段或者所有的数字类型字段的平均值，只能作用于数字型字段
- `sum(colNames: String*)`方法，获取分组中指定字段或者所有的数字类型字段的和值，只能作用于数字型字段
- `count()`方法，获取分组中的元素个数

### `orderBy`、`sort`：指定字段排序

> 按指定字段排序，默认为升序

```scala
df.orderBy("id").show()
df.orderBy(df("id").desc).show()
```

### `sortWithinPartitions`：按Partition根据指定字段排序

> 和sort方法功能类似，区别在于sortWithinPartitions方法返回的是按Partition排好序的DataFrame对象。

### `distinct`：返回一个不包含重复记录的DataFrame

> 返回当前DataFrame中不重复的Row记录。

```scala
df.distinct().show()
```

### `dropDuplicates`：根据指定字段去重

> 根据指定字段去重。类似于 `select distinct a, b` 操作。
>
> 注：当不传入参数的时候，跟distinct方法一样的效果

```scala
df.dropDuplicates(Seq("c1")).show()
```

### `agg`：聚合操作

> 聚合操作调用的是agg方法，该方法有多种调用方式。一般与groupBy方法配合使用。

```scala
df.agg("id" -> "max", "c4" -> "sum").show()
```

### `union`：对两个DataFrame进行组合

> 对两个DataFrame进行组合

```scala
df.union(df2).show()
```

### `join`：联接操作

对于没有joinType的方法，默认使用内连接（inner join）

joinType(连接类型)：

- inner
- cross
- out
- full
- full_outer
- left
- left_outer
- right
- right_outer

**笛卡尔积**

```scala
df.join(df2)
```

**使用一个或多个字段作为联接条件**

注：需要两个DataFrame中有相同的列名

```scala
df.join(df2,"c1")
df.join(df2,Seq("c1","c2"))
```

**指定join类型**

> 两个 DataFrame 的 join 操作有 inner, outer, left_outer, right_outer, leftsemi 类型。

*Scala 版本*

```scala
df.join(df2,Seq("c1","c2"),"inner")
```

*Java 版本*

```java
// 此种方式进行join操作会保存两个进行关联的列
df.join(df2,df.col("c1").equalTo(top5GenreDF.col("c1")),"inner");

// 如果两个表的列明相同，且是内连接，第二个参数可以直接用String
df.join(df2,"c1");
```

**使用Column类型来join**

```scala
df.join(df2,df("id")===df2("t_id"))
```

### `intersect`：获取两个DataFrame中共有的记录

```scala
df.intersect(df.limit(1)).show()
```

### `except`：获取一个DataFrame中有另一个DataFrame中没有的记录

```scala
df.except(df.limit(1)).show()
```

## 其他

### `printSchema`：打印 schema

**Scala 版本**

```scala
// This import is needed to use the $-notation
import spark.implicits._
// Print the schema in a tree format
df.printSchema()
// root
// |-- age: long (nullable = true)
// |-- name: string (nullable = true)
```

**Java 版本**

```java
// Print the schema in a tree format
df.printSchema();
// root
// |-- age: long (nullable = true)
// |-- name: string (nullable = true)
```

### `col,apply`：获取指定字段

> 只能获取一个字段，返回对象为Column类型。

```scala
df.col("id")
df.apply("id")
df("id")
```

### 随机数

在 Spark SQL 中，`org.apache.spark.sql.functions` 包提供了一些实用的函数，其中就包含了产生随机数的函数。它们可以从一个特定的分布中提取出独立同分布值，例如产生均匀分布随机数的函数 `rand()` 和产生服从正态分布的随机数的函数 `randn()` 。

### Java写法下引用 `col,sum,avg` 等方法

需要保证静态引用了类，这样类中的静态方法即可被引用，如下所示：

```java
import static org.apache.spark.sql.functions.*;

// 或者用到一个引用一个
// col("...") is preferable to df.col("...")
import static org.apache.spark.sql.functions.col;
```