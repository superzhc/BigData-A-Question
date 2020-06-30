DataFrame 是一种跨语言的、通用的数据科学抽象。

## 操作

### 增加列

**SQL自定义函数**

```scala
val topic ="topic123"
tempDataFrame.createOrReplaceTempView("temp")
sqlContex.sqlContext.udf.register("replaceCol", (str:String) => topic)
val addDf =sqlContex.sqlContext.sql(s"select *,replaceCol(content) as topicName from temp")
addDf.show()
```

**`withColumn` 自定义函数**

```scala
val topic ="topic123"
val replace = (x:String)=>{topic}
val replaceCol = udf(replace)
val data = tempDataFrame.withColumn("topicName",replaceCol(tempDataFrame("content")))
data.show()
```

### 修改列值

重新构造 DataFrame,DataFrame 先取不用改变的列，编写自定义函数构造新列，再加到新 DataFrame

```scala
tempDataFrame.createOrReplaceTempView("temp")
// 创建自定义函数
sqlContex.udf.register("Time2Time", Time2Time _)

// 获取colName
val linkedColNames = getLinkedColNames(tempDataFrame.schema.fieldNames)

val addDf = sqlContex.sqlContext.sql(s"select $linkedColNames,Time2Time(update_time) AS update_time,Time2Time(create_time) AS create_time,from temp where $conditon")
//addDf.saveToEs("ods_wj_scenes_detail/docs")
addDf.select("update_time").show(50)
```

