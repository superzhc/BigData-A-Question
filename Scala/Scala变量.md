变量定义的语法：

```scala
var|val 变量名[:变量类型]=变量值

// 示例
var a:Int=10

// 类型推导，使用 isInstanceOf[Int] 判断
println(a.isInstanceOf[Int])
```

因为 val 没有线程安全的问题，因此效率高，scala 的设计者推荐使用 val 来定义变量

val 修饰的变量在编译后，等同于加上了 final 来进行修饰变量



**注意事项**：

1. 在 scala 中，小数默认为 double 类型，整数为 int 类型
2. 变量的申明时，需要初始化