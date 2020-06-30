Spark 提供了两种常见的创建 RDD 的方式：

- 调用 SparkContext 的 `parallelize()` 方法将数据并行化生成 RDD
- 从外部存储系统（如 HDFS、共享文件系统、HBase 或提供 Hadoop InputFormat 的任何数据源）中引用数据生成 RDD

### 程序内部数据作为数据源

把程序中一个已有的集合传给 SparkContext 的 `parallelize()` 方法，对集合并行化，从而创建 RDD。

通过调用 SparkContext 的 parallelize 方法将驱动程序已经存在的数据集转化为**并行化集合（Parallelize Collections）**。集合的元素被复制以形成可并行操作的分布式数据集。

例如，下面创建一个包含数字 1~5 的并行集合：

```scala
val data = Array(1,2,3,4,5)
val distData = sc.parallelize(data)
```

一旦创建，分布式数据集（distData）就可以进行并行操作。例如，可以调用 `distData.reduce((a,b)=>a+b)`将数组的元素相加。

并行集合的一个重要参数是将数据集切割到的分区数（partitions）。Spark 将为集群的每个 RDD 分区进行一个计算任务，即 RDD 每一个分区是计算任务的基本分配单位，而非整个 RDD。通常，Spark 会根据集群实际情况自动设置分区数。但是，也可以通过将其作为第二个参数传递给 parallelize 来手动设置，如下实例将 data 数据集切分为 10 个分区：

```scala
sc.parallelize(data,10)
```

### 外部数据源

Spark 支持多种数据源，比如 HDFS、Cassandra、HBase、Amazon S3或者其他支持Hadoop的数据源。Spark支持多种文件格式，比如普通文本文件、SequenceFiles、Parquet、CSV、JSON、对象文件、Hadoop的输入输出文件等。

文本文件可以使用 SparkContext 的 `textFile(path:String,minPartitions:Int=defaultMinPartitions)` 方法创建 RDD。此方法需要一个文件的 URI（本地路径的机器上，或一个 `hdfs://`、`s3n://`等 URI），另外可以通过第二个参数 minPartitions 设置 RDD 分区数，返回值为由 String 对象组成的 `RDD[String]`。

示例：从一个本地文件系统读取文本文件作为外部数据源

```scala
val distFile = sc.textFile("file:///usr/local/data.txt",10)
```

#### **`textFile()`**

![1570525167703](../images/1570525167703.png)

参数分析：

- `path:String`，path 用来表示 RDD 外部数据源路径信息的 URI，这个 URI 可以是 HDFS、本地文件系统，以及任何一个 Hadoop 支持的文件系统的 URI
- `minPartitions:Int=defaultMinPartitions`，minPartitions 参数用来指定生成的 RDD 的分区（partition）数，需要注意的是 RDD 的 partition 个数其实是在逻辑上将数据集进行划分，RDD 各分区的实质是记录着数据源的各个文件块（block）在 HDFS 位置的信息集合，并不是数据源本身的集合，因此 RDD partitions 数目也受 HDFS 的 split size 影响，HDFS 默认文件块（block）大小为 128M，这就意味着当数据源文件小于 128M 时，RDD 分区数并不会按照 minPartitions 进行指定分区，而只有一个分区。

注意事项：

- 如果需要从本地文件系统读取文件作为外部数据源，则文件必须确保集群上的所有工作节点可访问。可以将文件复制到所有工作节点或使用集群上的共享文件系统
- Spark 所有的基于文件的读取方法，包括 textFile 支持读取某个目录下多个指定文件，支持部分的压缩文件和通配符
- textFile 方法还采用可选的第二个参数来控制文件的分区数。默认情况下，Spark 为文件的每个块创建一个分区，但也可以通过传递更大的值来请求更高数量的分区。注意，不能有比块少的分区。

#### **Spark 的 Scala API 还支持其他几种数据格式**

除了文本文件，Spark 的 Scala API 支持其他几种数据格式：

- `SparkContext.wholeTextFiles` 可用于读取包含多个小文本文件的目录，并将其作为 `(filename,content)`表示的 `(文件名，文件内容)`键值对组成的 RDD 返回。这与 textFile 每个文件中的每行返回一条记录不同。
- 对于 [SequenceFiles](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html)，可以使用 SparkContext 的 `sequenceFile[K, V]` 方法创建，K 和 V 分别对应的是 key 和 values 的类型。像 [IntWritable](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/io/IntWritable.html) 与 [Text](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/io/Text.html) 一样，它们必须是 Hadoop 的 [Writable](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/io/Writable.html) 接口的子类。另外，对于几种通用的 Writables，Spark 允许你指定原声类型来替代。例如： `sequenceFile[Int, String]` 将会自动读取 IntWritables 和 Text。
- 对于其他的 Hadoop InputFormats，你可以使用 `SparkContext.hadoopRDD` 方法，它可以指定任意的 `JobConf`，输入格式(InputFormat)，key 类型，values 类型。你可以跟设置 Hadoop job 一样的方法设置输入源。你还可以在新的 MapReduce 接口(org.apache.hadoop.mapreduce)基础上使用 `SparkContext.newAPIHadoopRDD`(译者注：老的接口是 `SparkContext.newHadoopRDD`)。
- `RDD.saveAsObjectFile` 和 `SparkContext.objectFile` 支持保存一个RDD，保存格式是一个简单的 Java 对象序列化格式。这是一种效率不高的专有格式，如 Avro，它提供了简单的方法来保存任何一个 RDD。

