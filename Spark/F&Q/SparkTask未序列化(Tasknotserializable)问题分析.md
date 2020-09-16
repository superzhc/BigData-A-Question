# SparkTask未序列化(Tasknotserializable)问题分析

问题描述及原因分析

> 在编写Spark程序中，由于在map等算子内部使用了外部定义的变量和函数，从而引发Task未序列化问题。然而，Spark算子在计算过程中使用外部变量在许多情形下确实在所难免，比如在filter算子根据外部指定的条件进行过滤，map根据相应的配置进行变换等。为了解决上述Task未序列化问题，这里对其进行了研究和总结。

出现 `org.apache.spark.SparkException: Task not serializable` 这个错误，一般是因为在 map、filter 等的参数使用了外部的变量，但是这个变量不能序列化（不是说不可以引用外部变量，只是要做好序列化工作，具体后面详述）。其中最普遍的情形是：当引用了某个类（经常是当前类）的成员函数或变量时，会导致这个类的所有成员（整个类）都需要支持序列化。虽然许多情形下，当前类使用了 `extends Serializable` 声明支持序列化，但是由于某些字段不支持序列化，仍然会导致整个类序列化时出现问题，最终导致出现Task未序列化问题。

引用成员变量的实例分析

如上所述，由于 Spark 程序中的 map、filter 等算子内部引用了类成员函数或变量导致需要该类所有成员都需要支持序列化，又由于该类某些成员变量不支持序列化，最终引发 Task 无法序列化问题。为了验证上述原因，我们编写了一个实例程序，如下所示。该类的功能是从域名列表中（rdd）过滤得到特定顶级域名（rootDomain，如.com,.cn,.org）的域名列表，而该特定顶级域名需要函数调用时指定。

```scala
class MyTest1(conf:String) extends Serializable{
  val list = List("a.com", "www.b.com", "a.cn", "a.com.cn", "a.org");
  private val sparkConf = new SparkConf().setAppName("AppName");
  private val sc = new SparkContext(sparkConf);
  val rdd = sc.parallelize(list);

  private val rootDomain = conf

  def getResult(): Array[(String)] = {
    val result = rdd.filter(item => item.contains(rootDomain))
    result.take(result.count().toInt)
  }
}
````

依据上述分析的原因，由于依赖了当前类的成员变量，所以导致当前类全部需要序列化，由于当前类某些字段未做好序列化，导致出错。实际情况与分析的原因一致，运行过程中出现错误，如下所示。分析下面的错误报告得到错误是由于sc（SparkContext）引起的。

```log
Exception in thread "main" org.apache.spark.SparkException: Task not serializable
    at org.apache.spark.util.ClosureCleaner$.ensureSerializable(ClosureCleaner.scala:166)
    at org.apache.spark.util.ClosureCleaner$.clean(ClosureCleaner.scala:158)
    at org.apache.spark.SparkContext.clean(**SparkContext**.scala:1435) 
……
Caused by: java.io.NotSerializableException: org.apache.spark.SparkContext
    - field (class "com.ntci.test.MyTest1", name: "sc", type: "class org.apache.spark.SparkContext")
    - object (class "com.ntci.test.MyTest1", com.ntci.test.MyTest1@63700353)
    - field (class "com.ntci.test.MyTest1$$anonfun$1", name: "$outer", type: "class com.ntci.test.MyTest1")
```

为了验证上述结论，将不需要序列化的的成员变量使用关键字“@transent”标注，表示不序列化当前类中的这两个成员变量，再次执行函数，同样报错。

```log
Exception in thread "main" org.apache.spark.SparkException: Task not serializable
    at org.apache.spark.util.ClosureCleaner$.ensureSerializable(ClosureCleaner.scala:166)
……
 Caused by: java.io.NotSerializableException: org.apache.spark.SparkConf
    - field (class "com.ntci.test.MyTest1", name: "sparkConf", type: "class org.apache.spark.**SparkConf**")
    - object (class "com.ntci.test.MyTest1", com.ntci.test.MyTest1@6107799e)
```

虽然错误原因相同，但是这次导致错误的字段是sparkConf（SparkConf）。使用同样的“@transent”标注方式，将sc（SparkContext）和sparkConf（SparkConf）都标注为不需序列化，再次执行时，程序正常执行。

```scala
class MyTest1(conf:String) extends Serializable{
  val list = List("a.com", "www.b.com", "a.cn", "a.com.cn", "a.org");
  @transient
  private val sparkConf = new SparkConf().setAppName("AppName");
  @transient
  private val sc = new SparkContext(sparkConf);
  val rdd = sc.parallelize(list);

  private val rootDomain = conf

  def getResult(): Array[(String)] = {

    val result = rdd.filter(item => item.contains(rootDomain))
    result.take(result.count().toInt)
  }
}
```

所以，通过上面的例子我们可以得到结论：由于Spark程序中的map、filter等算子内部引用了类成员函数或变量导致该类所有成员都需要支持序列化，又由于该类某些成员变量不支持序列化，最终引发Task无法序列化问题。相反地，对类中那些不支持序列化问题的成员变量标注后，使得整个类能够正常序列化，最终消除Task未序列化问题。
引用成员函数的实例分析

成员变量与成员函数的对序列化的影响相同，即引用了某类的成员函数，会导致该类所有成员都支持序列化。为了验证这个假设，我们在map中使用了当前类的一个成员函数，作用是如果当前域名没有以“www.”开头，那么就在域名头添加“www.”前缀（注：由于rootDomain是在getResult函数内部定义的，就不存在引用类成员变量的问题，也就不存在和排除了上一个例子所讨论和引发的问题，因此这个例子主要讨论成员函数引用的影响；此外，不直接引用类成员变量也是解决这类问题的一个手段，如本例中为了消除成员变量的影响而在函数内部定义变量的这种做法，这类问题具体的规避做法此处略提，在下一节作详细阐述）。下面的代码同样会报错，同上面的例子一样，由于当前类中的sc（SparkContext）和sparkConf（SparkConf）两个成员变量没有做好序列化处理，导致当前类的序列化出现问题。

```scala
class MyTest1(conf:String)  extends Serializable{
  val list = List("a.com", "www.b.com", "a.cn", "a.com.cn", "a.org");
  private val sparkConf = new SparkConf().setAppName("AppName");
  private val sc = new SparkContext(sparkConf);
  val rdd = sc.parallelize(list);

  def getResult(): Array[(String)] = {
    val rootDomain = conf
    val result = rdd.filter(item => item.contains(rootDomain))
    .map(item => addWWW(item))
    result.take(result.count().toInt)
  }
  def addWWW(str:String):String = {
    if(str.startsWith("www."))
      str
    else
      "www."+str
  }
}
```

如同前面的做法，将sc（SparkContext）和sparkConf（SparkConf）两个成员变量使用“@transent”标注后，使当前类不序列化这两个变量，则程序可以正常运行。此外，与成员变量稍有不同的是，由于该成员函数不依赖特定的成员变量，因此可以定义在scala的object中（类似于Java中的static函数），这样也取消了对特定类的依赖。如下面例子所示，将addWWW放到一个object对象（UtilTool）中去，filter操作中直接调用，这样处理以后，程序能够正常运行。

```scala
def getResult(): Array[(String)] = {
    val rootDomain = conf
    val result = rdd.filter(item => item.contains(rootDomain))
    .map(item => UtilTool.addWWW(item))
    result.take(result.count().toInt)
  }
object UtilTool {
  def addWWW(str:String):String = {
    if(str.startsWith("www."))
      str
    else
      "www."+str
  }
}
```

对全类序列化要求的验证

如上所述，引用了某类的成员函数，会导致该类及所有成员都需要支持序列化。因此，对于使用了某类成员变量或函数的情形，首先该类需要序列化（extends Serializable），同时需要对某些不需要序列化的成员变量标记以避免为序列化造成影响。对于上面两个例子，由于引用了该类的成员变量或函数，导致该类以及所有成员支持序列化，为了消除某些成员变量对序列化的影响，使用“@transent”进行标注。 

为了进一步验证关于整个类需要序列化的假设，这里在上面例子使用“@transent”标注后并且能正常运行的代码基础上，将类序列化的相关代码删除（去掉extends Serializable），这样程序执行会报该类为序列化的错误，如下所示。所以通过这个实例说明了上面的假设。

```log
Caused by: java.io.NotSerializableException: com.ntci.test.MyTest1
    - field (class "com.ntci.test.MyTest1$$anonfun$1", name: "$outer", type: "class com.ntci.test.MyTest1")
```

所以通过以上例子可以说明：map等算子内部可以引用外部变量和某类的成员变量，但是要做好该类的序列化处理。首先是该类需要继承Serializable类，此外，对于类中某些序列化会出错的成员变量做好处理，这也是Task未序列化问题的主要原因。对于出现这类问题，首先查看未能序列化的成员变量是哪个，对于可以不需要序列化的成员变量可使用“@transent”标注。 

此外，也不是map操作所在的类必须序列化不可（继承Serializable类），对于不需要引用某类成员变量或函数的情形，就不会要求相应的类必须实现序列化，如下面的例子所示，filter操作内部没有引用任何类的成员变量或函数，因此当前类不用序列化，程序可正常执行。

```scala
class MyTest1(conf:String) {
  val list = List("a.com", "www.b.com", "a.cn", "a.com.cn", "a.org");
  private val sparkConf = new SparkConf().setAppName("AppName");
  private val sc = new SparkContext(sparkConf);
  val rdd = sc.parallelize(list);

  def getResult(): Array[(String)] = {
    val rootDomain = conf
    val result = rdd.filter(item => item.contains(rootDomain))
    result.take(result.count().toInt)
  }
}
```

解决办法与编程建议

承上所述，这个问题主要是引用了某类的成员变量或函数，并且相应的类没有做好序列化处理导致的。因此解决这个问题无非以下两种方法：

- 不在（或不直接在）map等闭包内部直接引用某类（通常是当前类）的成员函数或成员变量
- 如果引用了某类的成员函数或变量，则需对相应的类做好序列化处理

**不在（或不直接在）map等闭包内部直接引用某类成员函数或成员变量**

1. 对于依赖某类成员变量的情形

如果程序依赖的值相对固定，可取固定的值，或定义在map、filter等操作内部，或定义在scala 
object对象中（类似于Java中的static变量）
如果依赖值需要程序调用时动态指定（以函数参数形式），则在map、filter等操作时，可不直接引用该成员变量，而是在类似上面例子的getResult函数中根据成员变量的值重新定义一个局部变量，这样map等算子就无需引用类的成员变量。
2. 对于依赖某类成员函数的情形
如果函数功能独立，可定义在scala object对象中（类似于Java中的static方法），这样就无需一来特定的类。

**如果引用了某类的成员函数或变量，则需对相应的类做好序列化处理**

对于这种情况，则需对该类做好序列化处理，首先该类继承序列化类，然后对于不能序列化的成员变量使用“@transent”标注，告诉编译器不需要序列化。 

此外如果可以，可将依赖的变量独立放到一个小的class中，让这个class支持序列化，这样做可以减少网络传输量，提高效率。 