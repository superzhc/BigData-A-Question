# RDD 存储结构

RDD 实现的数据结构核心如下：

| 属性                            | 说明                               |
| ------------------------------- | ---------------------------------- |
| 分区列表-partitions             | 每个分区为 RDD 的一部分数据        |
| 依赖列表-dependencies           | 父 RDD，即依赖 RDD                 |
| 计算函数-compute                | 利用父分区计算 RDD 各分区的值      |
| 分区器-partitioner              | 指明 RDD 的分区方式（Hash、Range） |
| 分区位置列表-preferredLocations | 指明分区优先存放的节点位置         |

RDD 的 5 个特性会对应源码中的 4 个方法和 1 个属性。`RDD.scala` 是一个总的抽象，不同的子类会对 RDD 中的方法进行重载。

```scala
//该方法只会被调用一次。由子类实现，返回这个RDD的所有partition。
protected def getPartitions: Array[Partition]
//该方法只会被调用一次。计算该RDD和父RDD的依赖关系
protected def getDependencies: Seq[Dependency[_]] = deps
// 对分区进行计算，返回一个可遍历的结果
def compute(split: Partition, context: TaskContext): Iterator[T]
//可选的，指定优先位置，输入参数是split分片，输出结果是一组优先的节点位置
protected def getPreferredLocations(split: Partition): Seq[String] = Nil
//可选的，分区的方法，针对第4点，类似于mapreduce当中的Paritioner接口，控制key分到哪个reduce
@transient val partitioner: Option[Partitioner] = None
```

## Partition

> 一份待处理的原始数据会被按照相应的逻辑(例如 jdbc 和 hdfs 的 split 逻辑)切分成 n 份，每份数据对应到 RDD 中的一个 Partition ，Partition 的数量决定了 task 的数量，影响着程序的并行度。

```scala
package org.apache.spark

/**
 * An identifier for a partition in an RDD.
 */
trait Partition extends Serializable {
  /**
   * Get the partition's index within its parent RDD
   */
  def index: Int

  // A better default implementation of HashCode
  override def hashCode(): Int = index

  override def equals(other: Any): Boolean = super.equals(other)
}
```

Partition 和 RDD 是伴生的，即每一种 RDD 都有其对应的 Partition 实现，所以分析 Partition 主要是分析其子类。这里关注两个常用的子类，JdbcPartition 和 HadoopPartition。

### JdbcPartition

```scala
private[spark] class JdbcPartition(idx: Int, val lower: Long, val upper: Long) extends Partition {
  override def index: Int = idx
}
```

查看 JdbcPartition 实现，相比 Partition，主要多了 lower 和 upper 这两个字段。

见 [JdbcRDD](#jdbcrdd) 类的 `getPartitions` 和 `compute` 函数可知，这两个参数应用到查询的 SQL 语句的分区中，如 `select * from test where id>=? and id<?`，需要有两个占位符。

### HadoopPartition

```scala
/**
 * A Spark split class that wraps around a Hadoop InputSplit.
 */
private[spark] class HadoopPartition(rddId: Int, override val index: Int, s: InputSplit)
  extends Partition {

  val inputSplit = new SerializableWritable[InputSplit](s)

  override def hashCode(): Int = 31 * (31 + rddId) + index

  override def equals(other: Any): Boolean = super.equals(other)

  /**
   * Get any environment variables that should be added to the users environment when running pipes
   * @return a Map with the environment variables and corresponding values, it could be empty
   */
  def getPipeEnvVars(): Map[String, String] = {
    val envVars: Map[String, String] = if (inputSplit.value.isInstanceOf[FileSplit]) {
      val is: FileSplit = inputSplit.value.asInstanceOf[FileSplit]
      // map_input_file is deprecated in favor of mapreduce_map_input_file but set both
      // since it's not removed yet
      Map("map_input_file" -> is.getPath().toString(),
        "mapreduce_map_input_file" -> is.getPath().toString())
    } else {
      Map()
    }
    envVars
  }
}
```

## RDD 子类

### JdbcRDD

> An RDD that executes a SQL query on a JDBC connection and reads results.
> 
> 一个RDD，它在JDBC连接上执行SQL查询并读取结果

```scala
class JdbcRDD[T: ClassTag](
    sc: SparkContext,
    getConnection: () => Connection,
    sql: String,
    lowerBound: Long,
    upperBound: Long,
    numPartitions: Int,
    mapRow: (ResultSet) => T = JdbcRDD.resultSetToObjectArray _)
  extends RDD[T](sc, Nil) with Logging {

  override def getPartitions: Array[Partition] = {
    // bounds are inclusive, hence the + 1 here and - 1 on end
    val length = BigInt(1) + upperBound - lowerBound
    (0 until numPartitions).map { i =>
      val start = lowerBound + ((i * length) / numPartitions)
      val end = lowerBound + (((i + 1) * length) / numPartitions) - 1
      new JdbcPartition(i, start.toLong, end.toLong)
    }.toArray
  }

  override def compute(thePart: Partition, context: TaskContext): Iterator[T] = new NextIterator[T]
  {
    context.addTaskCompletionListener{ context => closeIfNeeded() }
    val part = thePart.asInstanceOf[JdbcPartition]
    val conn = getConnection()
    val stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

    val url = conn.getMetaData.getURL
    if (url.startsWith("jdbc:mysql:")) {
      // setFetchSize(Integer.MIN_VALUE) is a mysql driver specific way to force
      // streaming results, rather than pulling entire resultset into memory.
      // See the below URL
      // dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-implementation-notes.html

      stmt.setFetchSize(Integer.MIN_VALUE)
    } else {
      stmt.setFetchSize(100)
    }

    logInfo(s"statement fetch size set to: ${stmt.getFetchSize}")

    stmt.setLong(1, part.lower)
    stmt.setLong(2, part.upper)
    val rs = stmt.executeQuery()

    override def getNext(): T = {
      if (rs.next()) {
        mapRow(rs)
      } else {
        finished = true
        null.asInstanceOf[T]
      }
    }

    override def close() {
      try {
        if (null != rs) {
          rs.close()
        }
      } catch {
        case e: Exception => logWarning("Exception closing resultset", e)
      }
      try {
        if (null != stmt) {
          stmt.close()
        }
      } catch {
        case e: Exception => logWarning("Exception closing statement", e)
      }
      try {
        if (null != conn) {
          conn.close()
        }
        logInfo("closed connection")
      } catch {
        case e: Exception => logWarning("Exception closing connection", e)
      }
    }
  }
}
```

### HadoopRDD

> HadoopRDD 是一个专为 Hadoop(HDFS、Hbase、S3) 设计的RDD。

HadoopRDD 主要重写了三个方法:

```scala
/**
 * getPartitions方法最后是返回了一个array。它调用的是inputFormat自带的getSplits方法来计算分片，然后把分片信息放到array中
 **/
override def getPartitions: Array[Partition] = {
  val jobConf = getJobConf()
  // add the credentials here as this can be called before SparkContext initialized
  SparkHadoopUtil.get.addCredentials(jobConf)
  val inputFormat = getInputFormat(jobConf)
  val inputSplits = inputFormat.getSplits(jobConf, minPartitions)
  val array = new Array[Partition](inputSplits.size)
  for (i <- 0 until inputSplits.size) {
    array(i) = new HadoopPartition(id, i, inputSplits(i))
  }
  array
}

/**
 * compute方法的作用主要就是根据输入的partition信息生成一个InterruptibleIterator
 **/
override def compute(theSplit: Partition, context: TaskContext): InterruptibleIterator[(K, V)] = {

 val iter = new NextIterator[(K, V)] {

      //将compute的输入theSplit，转换为HadoopPartition
      val split = theSplit.asInstanceOf[HadoopPartition]
      ......
      //重写getNext方法
      override def getNext(): (K, V) = {
        try {
          finished = !reader.next(key, value)
        } catch {
          case _: EOFException if ignoreCorruptFiles => finished = true
        }
        if (!finished) {
          inputMetrics.incRecordsRead(1)
        }
        (key, value)
      }
     }
}

override def getPreferredLocations(split: Partition): Seq[String] = {
  val hsplit = split.asInstanceOf[HadoopPartition].inputSplit.value
  val locs: Option[Seq[String]] = HadoopRDD.SPLIT_INFO_REFLECTIONS match {
    case Some(c) =>
      try {
        val lsplit = c.inputSplitWithLocationInfo.cast(hsplit)
        val infos = c.getLocationInfo.invoke(lsplit).asInstanceOf[Array[AnyRef]]
        Some(HadoopRDD.convertSplitLocationInfo(infos))
      } catch {
        case e: Exception =>
          logDebug("Failed to use InputSplitWithLocations.", e)
          None
      }
    case None => None
  }
  locs.getOrElse(hsplit.getLocations.filter(_ != "localhost"))
}
```

### MapPartitionsRDD

> An RDD that applies the provided function to every partition of the parent RDD.
> 
> 将所提供的函数应用于父RDD的每个分区的RDD。

它重写了父类 RDD 的 `partitioner`、`getPartitions` 和 `compute`:

```scala
private[spark] class MapPartitionsRDD[U: ClassTag, T: ClassTag](
    var prev: RDD[T],
    f: (TaskContext, Int, Iterator[T]) => Iterator[U],  // (TaskContext, partition index, iterator)
    preservesPartitioning: Boolean = false)
  extends RDD[U](prev) {
  override val partitioner = if (preservesPartitioning) firstParent[T].partitioner else None
  override def getPartitions: Array[Partition] = firstParent[T].partitions
  override def compute(split: Partition, context: TaskContext): Iterator[U] =
    f(context, split.index, firstParent[T].iterator(split, context))
  override def clearDependencies() {
    super.clearDependencies()
    prev = null
  }
}
```

可以看出在 MapPartitionsRDD 里面都用到一个 firstParent 函数。且可以发现在 MapPartitionsRDD 其实没有重写 partition 和 compute 逻辑，只是从 firstParent 中取了出来。

firstParent 的源码在 `RDD.scala` 中，代码如下：

```scala
/** Returns the first parent RDD */
protected[spark] def firstParent[U: ClassTag]: RDD[U] = {
  dependencies.head.rdd.asInstanceOf[RDD[U]]
}
```