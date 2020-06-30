> **通过隐式转换，程序员可以在编写Scala程序时故意漏掉一些信息，让编译器去尝试在编译期间自动推导出这些信息来，这种特性可以极大的减少代码量，忽略那些冗长，过于细节的代码**

`implicit`是Scala中的一个关键字，`implicit`用法有如下几种：

1. 隐式转换函数
    ```scala
    implicit def int2str(x:Int):String=x.toString
    ```
2. 隐式类
    ```scala
    implicit class Box(x:Int){
    }
    ```
3. 隐式参数
    ```scala
    def compare[T](x:T,y:T)(implicit ordered:Ordering[T]):Int={
        order.compare(x,y)
    }
    ```
4. 隐式值
    ```scala
    implicit val x:Int=0
    ```
5. 隐式对象
    ```scala
    implicit object obj{
    }
    ```
6. context bound
    ```scala
    def compare2[T:Ordering](x:T,y:T)={
        val ord=implicit[Ordering[T]]
        ord.compare(x,y)
    }
    ```

## 隐式转换函数

```scala
implicit def int2str(x:Int):String=x.toString
```

在函数int2str前加上一个关键字`implicit`，**它告诉编译器，这个函数是一个隐式转换函数，能够把`Int`类型的值转换成`String`类型的值**。

**这种隐式转换的意义在于，如果在进行一个对`Int`类型的操作时不合法，编译器会在当前作用域寻找合适的隐式转换，来尝试使这种操作合法**。

隐式转换发生在以下两种情景：

- `e`是一个`S`类型的表达式，而需要的却是`T`类型，编译器会寻找`S=>T`的隐式转换
- `e`是一个`S`类型的表达式，使用点号访问`e.m`时，`m`不是类型`S`的成员，编译器会寻找合适的隐式转换使`e.m`合法

隐式转换最常用的用途就是扩展已有的类，在不修改原有类的基础上为其添加新的方法、成员。

上面的示例，在为Int类型定义好到String类型的隐式转换后，所有String类型支持的操作都可以直接在Int类型的值上使用，如：

```scala
10.concat("hello")
10.length

// 接受String类型的函数也可以接受Int类型：
def hi(s:String)=println("hi"+s)
hi(123)
```

注意：

1. **对于隐式转换函数，编译器最关心的是它的类型签名，即它将哪一种类型转换到另一种类型，也就是说它应该只接受一个参数，对于接受多参数的隐式函数来说就没有隐式转换的功能了**
    ```Scala
    implicit def int2str(x:Int):String=x.toString       // √
    implicit def int2str(x:Int,y:Int):String=x.toString // ×
    ```
2. 不支持嵌套的隐式转换
    ```scala
    class A{
        def hi=println("hi")
    }
    
    implicit def int2str(x:Int):String=x.toString
    implicit def str2A(s:String):A=new A

    "str".hi()  // √
    10.hi()     // ×
    ```
3. 不能存在二义性，即同一个作用域不能定义两个相同类型的隐式转换函数，这样编译器将无法决定使用哪个转换
    ```scala
    /* 错误 */
    implicit def int2str(x:Int):String=x.toString
    
    implicit def anotherInt2str(x:Int):String=x.toString
    /* 错误 */
    ```
4. 代码能够在不使用隐式转换的前提下能编译通过，就不会进行隐式转换

## 隐式类

隐式转换最重要的应用是扩展已存在的类，示例：【对已有的Int类型添加一个sayhi方法】

```scala
class SayhiImpl(ivalue:Int){
    val value=ivalue
    def sayhi=println(s"hi $value!")
}

implicit def int2Sayhi(x:Int)=new SayhiImpl(x)
```

那么调用`123.sayhi`，将会输出：`hi 123！`

上面的示例是先实现一个支持`sayhi`方法的类，再写一个隐式转换函数，使得Int类型也支持`sayhi`。但是这种写法过于啰嗦了，可以使用隐式类实现等价的功能：

```scala
implicit class SayhiImpl(ivalue:Int){
    val value=ivalue
    def sayhi=println(s"hi $value!")
}

123.sayhi
```

**隐式类就是在类定义前加一个`implicit`关键字，这表示它的构造函数是一个隐式转换函数，能够将参数的类型转换成自己的类型**，上例的构造函数`SayhiImpl(ivalue:Int)`定义了Int到SayhiImpl的隐式转换。

在使用隐式类时需要注意以下限制条件：

1. 只能在别的trait/类/对象内部定义
    ```scala
    object Helpers{
        implicit class RichInt(x:Int)   //正确
    }
    implicit class RichDouble(x:Double) //错误
    ```
2. 构造函数只能携带一个非隐式参数
    ```scala
    implicit class RichDate(date:java.util.Date)                       //正确
    implicit class Indexer[T](collection:Seq[T],index:Int)             //错误
    implicit class Indexer[T](collection:Seq[T])(implicit index Index) //正确
    ```
3. 在同一作用域内，不能又任何方法、成员或对象与隐式类
    注意：**`implicit`关键字不能用于case类**
    ```scala
    object Bar
    implicit class Bar(x:Int)      //错误

    val x=5
    implicit class x(y:Int)        //错误

    implicit case class Baz(x:Int) //错误
    ```

## 隐式参数

```scala
def compare[T](x:T,y:T)(implicit ordered:Ordering[T]):Int={
    ordered.compare(x,y)
}
```

在函数定义的时候，支持在最后一组参数使用`implicit`，表明这是一组隐式参数。在调用该函数的时候，可以不用传递隐式参数，而编译器会自动寻找一个`implicit`标记过的合适的值作为该参数。

例如上面的函数，调用compare时不需要显式提供ordered，而只需要直接`compare(1,2)`这样使用即可。

```scala
object Test{
    trait Adder[T]{
        def add(x:T,y:T):T
    }

    implicit val a=new Adder[Int]{
        override def add(x:Int,y:Int):Int=x+y
    }

    def addTest(x:Int,y:Int)(implicit adder:Adder[Int])={
        adder.add(x,y)
    }

    addTest(1,2)    // 正确，3
    addTest(1,2)(a) // 正确，3
    addTest(1,2)(new Adder[Int]{
        override def add(x:Int,y:Int):Int=x-y
    })              // 正确，-1
}
```

## 隐式值和隐式对象

隐式值：

```scala
implicit val x:Int=0
```

隐式对象：

```scala
implicit object obj{
}
```

> **在调用含有隐式参数的函数时，编译器会自动寻找合适的隐式值当作隐式参数，而只有用implict标记过的值、对象、函数才能被找到**。

例如自动寻找隐式对象：

```scala
implicit object Obj{
    def hello(s:String)=println(s"hello $s!")
}

def test2(s:String)(implicit o:Obj.type)={
    o.hello(s)
}

test2("world") //Hello World
```

自动寻找隐式函数：

```scala
implicit def int2str(x:Int):String=x.toString

def test1(x:Int,func:String=>Unit)(implicit helper:Int=>String)={
    func("\""+helper(x)+"\"")
}

test1(12,println) //"12"
```

## context bound

隐式作为泛型类型的限制：

```scala
def compare2[T:Ordering](x:T,y:T)={
    val ord=implicitly[Ordering[T]]
    ord.compare(x,y)
}
```

上面compare2是一个泛型函数，其有一个类型参数T，在这里`T:Ordering`对T类型做出了限制，要求必须存在一个`Ordering[T]`类型的隐式值，这种限制就叫做context bound

这其实是隐式参数的语法糖，它等价于：

```scala
def compare2[T](x:T,y:T)(implicit ord:Ordering[T])={
    ord.compare(x,y)
}
```

注意到前面的函数体用到了`implicitly`函数。这是因为在使用`[T:Ordering]`这样的类型限制时，我们没有能接触到具体的`Ordering[T]`类型的隐式值ord，这时候调用`implicitly`函数，就可以拿到这个隐式值，进而进行下一步操作了。没有这个函数的话，需要这样写：

```scala
def compare2[T:Ordering](x:T,y:T)={
    def helper(implicit ord:Ordering[T])==ord.compare(x,y)
    helper()
}
```

## 隐式转换的时机

> 1. 当方法中的参数的类型与目标类型不一致时
> 2. 当对象调用类中不存在的方法或成员时，编译器会自动将对象进行隐式转换

## 隐式解析机制

> 即编译器是如何查找到缺失信息的，解析具有以下两种规则：
> 
> 1. 首先会在当前代码作用域下查找隐式实体（隐式方法、隐式类和隐式对象）
> 2. 如果第一条规则查找隐式实体失败，会继续在隐式参数的类型的作用域里查找

> 类型的作用域是指该类型相关联的全部伴生模块，一个隐式实体的类型T，它的查找范围如下：
> 
> 1. 如果T被定义为`T with A with B with C`，那么A，B，C都是T的部分，在T的隐式解析过程中，它们的伴生对象都会被搜索
> 2. 如果T是参数化类型，那么类型参数和与类型参数相关联的部分都算作T的部分，比如`List[String]`的隐式搜索会搜索List的伴生对象和String的伴生对象
> 3. 如果T是一个单例类型p.T，即T是属于某个p对象内，那么这个p对象也会被搜索
> 4. 如果T是个类型注入S#T，那么S和T都会被搜索