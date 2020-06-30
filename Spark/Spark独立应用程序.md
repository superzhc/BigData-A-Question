用 Scala 创建一个简单的 Spark 应用程序。

```scala
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object SimpleApp{
    def main(args:Array[String]):Unit={
        // YOUR_SPARK_HOME 修改为本地安装的 Spark 路径
        val logFile="YOUR_SPARK_HOME/README.md"
        val conf=new SparkConf().setAppName("Simple Application")
        val sc = new SparkContext(conf)
    	val logData = sc.textFile(logFile, 2).cache()
    	val numAs = logData.filter(line => line.contains("a")).count()
    	val numBs = logData.filter(line => line.contains("b")).count()
    	println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
    }
}
```

这个程序是为了计算在 Spark README 中行里包含 `a` 和包含 `b` 的次数。不像 Spark Shell，本例子需要初始化自己的 SparkContext，把 SparkContext 初始化作为程序的一部分。

 通过 SparkContext 的构造函数参入 [SparkConf](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.SparkConf) 对象，这个对象包含了一些关于用户程序的信息。

该程序依赖于 Spark API，所以需要包含一个 sbt 文件，`simple.sbt` 解释了 Spark 是一个依赖。这个文件还要补充 Spark 依赖于一个 repository：

```sbt
name := "Simple Project"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.2.0"
```

要让 sbt 正确工作，需要把 `SimpleApp.scala` 和 `simple.sbt` 按照标准的文件目录结构布局。

```
# Your directory layout should look like this
$ find .
.
./simple.sbt
./src
./src/main
./src/main/scala
./src/main/scala/SimpleApp.scala
```

上面的做好之后，可以把程序的代码创建成一个 JAR 包。然后使用 `spark-submit` 来运行程序。

```sh
# Package a jar containing your application
$ sbt package
...
[info] Packaging {..}/{..}/target/scala-2.10/simple-project_2.10-1.0.jar

# Use spark-submit to run your application
$ YOUR_SPARK_HOME/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  target/scala-2.10/simple-project_2.10-1.0.jar
...
Lines with a: 46, Lines with b: 23
```



