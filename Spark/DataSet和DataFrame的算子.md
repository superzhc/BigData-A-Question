## `show()`

```scala
show()//默认显示20行，字段值超过20，默认截断
show(numRows: Int)//指定显示行数
show(truncate: Boolean)//指定是否截断超过20的字符
show(numRows: Int, truncate: Boolean)//指定行数，和是否截断
show(numRows: Int, truncate: Int)//指定行数，和截断的字符长度
```

## `na()`：返回包含null值的行

```scala
val df=spark.read.json("D:\\test\\people.json")
df.show()
// 2019年12月20日 这种写法好像有问题吧
df.na.drop().show()
```






















