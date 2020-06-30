### `classOf`、`isInstanceOf`、`asInstanceOf` 三个预定义方法

**示例**

```scala
object PredefineTest{  
  def main(args: Array[String]):Unit = {  
    val c : Char = 97.asInstanceOf[Char]  
    "hello".asInstanceOf[String]  
    1.asInstanceOf[Long]  
    val it: Seq[String] = List("a", "b")  
    it.asInstanceOf[List[String]]  
  
    "hello".isInstanceOf[String]  
  
    classOf[String]  
  }  
}
```

使用 `scalac -Xprint:cleanup PredefineTest.scala`，Scala 编译器输出的 main 方法体内代码的抽象树（AST）信息如下：

```scala
val c: Char = 97.toChar();  
("hello": java.lang.String);  
1.toLong();  
val it: Seq = immutable.this.List.apply(scala.this.Predef.wrapRefArray(Array[java.lang.String]{"a", "b"}.$asInstanceOf[Array[java.lang.Object]]()));  
it.$asInstanceOf[List]();  
"hello".$isInstanceOf[java.lang.String]();  
{  
  classOf[java.lang.String];  
  ()  
}  
```

使用jd反编译工具查看对应代码如下：

```java
char c = (char)97;  
"hello";  
1;  
Seq it = List..MODULE$.apply(Predef..MODULE$.wrapRefArray((Object[])new String[] { "a", "b" }));  
((List)it);  
  
("hello" instanceof String);  
String.class;
```

`classOf[T]`

获取类型 T 的 Class 对象

classOf 方法定义在`scala.Predef`

```scala
object Predef extends LowPriorityImplicits {  
      /** Return the runtime representation of a class type.  This is a stub method.  
       *  The actual implementation is filled in by the compiler.  
       */  
      def classOf[T]: Class[T] = null  
    ...  
```

classOf的注释翻译过来的意思是：**返回类型的运行时呈现状态。这是一个存根方法。实际的实现是由编译器填补（自动生成）**

Predef object是默认导入的，所以classOf方法相当于一个**全局**方法。

`isInstanceOf[T]`

判断对象是否为T类型的实例。

`isInstanceOf` 和 `asInstanceOf` 由 `scala.Any` 类定义，Scala类层级的根类；其中class scala.AnyRef 继承自Any，是所有应引用类型的基类;trait scala.AnyVal 也继承自Any，是所有基本类型的实现的trait。**所以所有对象都自动拥有isInstanceOf和asInstanceOf这两个方法**。

特别注意的是 **Any 和AnyRef 这两个类属于“编译时类型”（虚拟类型？）**，不存在于运行时。所以这两者在Scala中都未提供源码，其语义由编译器在编译时构建。

`asInstanceOf[T]`

将对象类型强制转换为T类型。

还是由于泛型存在类型擦除的原因,1.asInstanceOf[String]在运行时会抛出ClassCastException异常，而List(1).asInstanceOf[List[String]]将不会。

结论

总而言之，**把classOf[T]看成Java里的T.class, obj.isInstanceOf[T]看成 obj instanceof T, obj.asInstanceOf[T]看成(T)obj**就对了。scala为用户提供了语法糖，但也免不了类型擦除问题的影响。

