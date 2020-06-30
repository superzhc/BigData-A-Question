## 构建映射

示例：

```scala
val scores=Map("Alice"->10,"Bob"->3,"Cindy"->8)
```

上述代码构造出一个不可变的Map[String,Int]，其值不能被改变。

在Scala中，映射是对偶的集合。

## 获取映射中的值

在Scala中，映射是通过使用`()`表示法来查找某个键对应的值

```scala
val bobScore=scores("Bob")
```

> 如果映射并不包含请求中使用的键，则会抛出异常

要检查映射中是否有某个指定的键，可以用contains方法来判定

> `映射.get(键)`的调用返回的是一个Option对象























