惰性函数（尽可能延迟表达式求值）是许多函数式编程语言的特性。

但函数的返回值被声明为 lazy 时，函数的执行将被推迟，直到用户首次对此取值，该函数才会执行，这种函数称之为惰性函数。

示例：

```scala
def sum(n1:Int,n2:Int):Int={
    println("sum()被执行了")
    n1+n2
}

def main(args:Array[String]):Unit={
    lazy val res=sum(10,20)
    println("-------------------------")
}
```

**注意事项和细节**：

1. lazy 不能修饰 var 类型的变量