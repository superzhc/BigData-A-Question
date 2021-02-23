# Scala 匹配模式

Scala没有类似Java的switch关键字，但Scala有一个更强的模式匹配，可以看作是switch的加强版。

模式匹配是检查某个值（value）是否匹配某一个模式的机制，一个成功的匹配同时会将匹配值**解构**为其组成部分。

## 语法

一个模式匹配语句包括一个待匹配的值，`match`关键字，以及至少一个`case`语句。

```scala
变量 match {
  case 值1 => 代码
  case 值2 => 代码
  ...
  case 值N if(...) => 代码 //if(...)是守卫条件
  case _ => 代码
}
```

## 模式匹配类型

Scala的模式匹配可以支持常量模式、变量模式、序列模式、元组模式、变量绑定模式等等

### 常量匹配

```scala
object MatchDemo {
  def matchConstant(x: Any) = x match {
    case 1 => "One"
    case "two" => "Two"
    case "3" => "Three"
    case true => "True"
    case null => "null value"
    case Nil => "empty list"
    case _ => "other value"
  }

  def main(args: Array[String]): Unit = {
    println(matchConstant(1)) // One
    println(matchConstant(true)) // True
    println(matchConstant(null)) // null value
    println(matchConstant(List())) // empty list
    println(matchConstant(false)) // other value
  }
}
```

### 变量匹配

case后面的值是变量

```scala
def matchVariable(x: Any) = x match {
  case x if (x == 1) => x
  case x if (x == "Tony") => x
  case x: String => "other value:" + x
  case _ => "unexpected value:" + x
}

println(matchVariable(1)) //1
println(matchVariable("Tony")) //Tony
println(matchVariable("Scala")) //other value:Scala
println(matchVariable(2)) //unexpected value:2
```

### 序列匹配

case后面的值是数组、List、Range等集合

```scala
def matchSeq(x: Any) = x match {
  case List("Tony", _, _*) => "Tony is in the list"
  case x :: y :: Nil => s"$x $y"
  case List(_, second, _*) => s"The second is:$second"
  case Array(first, second, _*) => s"first:$first,second:$second"
  case _ => "Other seq"
}

val list1 = List("Tony", "Cafei", "Aaron")
val list2 = "android" :: "ios" :: "H5" :: Nil
val list3 = List(1, 2)
val array1 = Array("Hadoop", "Spark", "ES")
val array2 = Array("Scala")
println(matchSeq(list1)) //Tony is in the list
println(matchSeq(list2)) //The second is:ios
println(matchSeq(list3)) //1 2
println(matchSeq(array1)) //first:Hadoop,second:Spark
println(matchSeq(array2)) //Other seq
```

### 元组匹配

case 后面的值是元组类型

```scala
def matchTuple(x: Any) = x match {
  case (first, _, _) => first
  case _ => "Something else"
}

val t = ("Tony", "Cafei", "Aaron")
println(matchTuple(t)) //Tony
```

注：在元组模式中不能使用`_*`来匹配剩余的元素，`_*`只适用于序列模式

### 类型匹配

它可以匹配输入待匹配变量的类型

```scala
def matchType(x: Any) = x match {
  case s: String => s"the string length is:${s.length}"
  case m: Map[_, _] => s"the map size is:${m.size}"
  case _: Int | _: Double => s"the number is:$x"
  case _ => s"unexpected value:$x"
}

println(matchType("test")) //the string length is:4
println(matchType(1)) //the number is:1
println(matchType(1.0d)) //the number is:1.0
println(matchType(true)) //unexpected value:true
val map = Map("one" -> 1, "two" -> 2, "three" -> 3)
println(matchType(map)) //the map size is:3
```

> case子句支持“或”逻辑，使用`|`即可

### 变量绑定匹配

可以将匹配的对象绑定到变量上。首先写一个变量名，然后写一个`@`符号，最后写入该匹配的对象。如果匹配成功，则将变量设置为匹配的对象

```scala
case class Person(name: String, age: Int)

val person = Person("Tony", 18)
person match {
  case p@Person(_, age) => println(s"${p.name},age is $age")
  case _ => println("Not a person")
}
//Tony,age is 18
```

## 样例类（case classes）匹配

样例类非常适合模式匹配

```scala

```

## Option类的模式匹配

Scala语言中包含一个标准类型Option类型，代表可选值。Option类型的值有两个可能的值，一个为`Some(x)`其中x为有效值，另一个为None对象，代表空值

```scala
def matchOption(x: Option[Int]) = x match {
  case Some(x) => x
  case None => "?"
}

val books = Map("hadoop" -> 1, "spark" -> 2, "hbase" -> 3)
println(matchOption(books.get("hadoop"))) //1
println(matchOption(books.get("hive"))) //?
```

## 异常处理的模式匹配

Scala抛出异常的语法和Java中的抛出异常语法是一致的，但Scala的catch语句与Java是有区别的，Scala是通过case语句来捕获对应的异常。

```scala
catch {
  case e: IllegalArgumentException => println("illegal arg. exception");
  case e: IllegalStateException    => println("illegal state exception");
  case e: IOException              => println("IO exception");
}
```