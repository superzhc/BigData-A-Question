Spark 核心的概念是 **弹性分布式数据集（Resilient Distributed Dataset,RDD）**：**一个可并行操作的有容错机制的数据集合**。

有两种方式创建 RDDs：第一种是在用户的驱动程序中并行化一个已经存在的集合；另外一种是引用一个外部存储系统的数据集，例如共享的文件系统，HDFS，HBase 或其他 Hadoop 数据格式的数据源。

### Spark 并行集合

并行集合（Parallelized collections）的创建是通过在一个已有的集合（Scala `Seq`）上调用 SparkContext 的 `parallelize` 方法实现的。集合中的元素被复制到一个可并行操作的分布式数据集中。

例如，这里演示了如何在一个包含 1 到 5 的数组中创建并行集合：

```scala
val data = Array(1, 2, 3, 4, 5)
val distData = sc.parallelize(data)
```

一旦创建完成，这个分布式数据集(`distData`)就可以被并行操作。例如，我们可以调用 `distData.reduce((a, b) => a + b)` 将这个数组中的元素相加。

并行集合一个很重要的参数是**切片数**(*slices*)，表示一个数据集切分的份数。Spark 会在集群上为每一个切片运行一个任务。你可以在集群上为每个 CPU 设置 2-4 个切片(slices)。正常情况下，Spark 会试着基于你的集群状况自动地设置切片的数目。然而，你也可以通过 `parallelize` 的第二个参数手动地设置(例如：`sc.parallelize(data, 10)`)。

### Spark 外部数据集

Spark 可以从任何一个 Hadoop 支持的存储源创建分布式数据集，包括本地文件系统，HDFS，Cassandra，HBase，[Amazon S3](http://wiki.apache.org/hadoop/AmazonS3)等。 Spark 支持文本文件(text files)，[SequenceFiles](http://hadoop.apache.org/docs/current/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html) 和其他 Hadoop [InputFormat](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/InputFormat.html)。

文本文件 RDDs 可以使用 SparkContext 的 `textFile` 方法创建。 在这个方法里传入文件的 URI (机器上的本地路径或 `hdfs://`，`s3n://` 等)，然后它会将文件读取成一个行集合。这里是一个调用例子：

```shell
scala> val distFile = sc.textFile("data.txt")
distFile: RDD[String] = MappedRDD@1d4cee08
```

一旦创建完成，`distFiile` 就能做数据集操作。

例如，可以用下面的方式使用 `map` 和 `reduce` 操作将所有行的长度相加：`distFile.map(s => s.length).reduce((a, b) => a + b)`。

注意，Spark 读文件时：

- 如果使用本地文件系统路径，文件必须能在 work 节点上用相同的路径访问到。要么复制文件到所有的 workers，要么使用网络的方式共享文件系统。
- 所有 Spark 的基于文件的方法，包括 `textFile`，能很好地支持文件目录，压缩过的文件和通配符。例如，可以使用 `textFile("/my/文件目录")`，`textFile("/my/文件目录/*.txt")` 和 `textFile("/my/文件目录/*.gz")`。
- `textFile` 方法也可以选择第二个可选参数来控制切片(*slices*)的数目。默认情况下，Spark 为每一个文件块(HDFS 默认文件块大小是 64M)创建一个切片(*slice*)。但是你也可以通过一个更大的值来设置一个更高的切片数目。注意，你不能设置一个小于文件块数目的切片值。

### Spark RDD 操作

RDDs 支持 2 种类型的操作：

- *转换(transformations)* 从已经存在的数据集中创建一个新的数据集；
- *动作(actions)* 在数据集上进行计算之后返回一个值到驱动程序。

在 Spark 中，所有的转换(transformations)都是惰性(lazy)的，它们不会马上计算它们的结果。相反的，它们仅仅记录转换操作是应用到哪些基础数据集(例如一个文件)上的。转换仅仅在这个时候计算：当动作(action) 需要一个结果返回给驱动程序的时候。这个设计能够让 Spark 运行得更加高效。

默认情况下，每一个转换过的 RDD 会在每次执行动作(action)的时候重新计算一次。然而，也可以使用 `persist` (或 `cache`)方法持久化(`persist`)一个 RDD 到内存中。在这个情况下，Spark 会在集群上保存相关的元素，在下次查询的时候会变得更快。在这里也同样支持持久化 RDD 到磁盘，或在多个节点间复制。

### Spark RDD 持久化

Spark 最重要的一个功能是它可以通过各种操作（operations）持久化（或者缓存）一个集合到内存中。当持久化一个RDD的时候，每一个节点都将参与计算的所有分区数据存储到内存中，并且这些数据可以被这个集合（以及这个集合衍生的其他集合）的动作（action）重复利用。这个能力使后续的动作速度更快（通常快10倍以上）。对应迭代算法和快速的交互使用来说，缓存是一个关键的工具。

通过`persist()`或者`cache()`方法持久化一个rdd。首先，在action中计算得到rdd；然后，将其保存在每个节点的内存中。Spark 的缓存是一个容错的技术-如果RDD的任何一个分区丢失，它可以通过原有的转换（transformations）操作自动的重复计算并且创建出这个分区。

此外，用户可以利用不同的存储级别存储每一个被持久化的RDD。例如，它允许用户持久化集合到磁盘上、将集合作为序列化的Java对象持久化到内存中、在节点间复制集合或者存储集合到[Tachyon](http://tachyon-project.org/)中。用户可以通过传递一个`StorageLevel`对象给`persist()`方法设置这些存储级别。`cache()`方法使用了默认的存储级别—`StorageLevel.MEMORY_ONLY`。完整的存储级别介绍如下所示：

| Storage Level                          | Meaning                                                      |
| -------------------------------------- | ------------------------------------------------------------ |
| MEMORY_ONLY                            | 将RDD作为非序列化的Java对象存储在jvm中。如果RDD不适合存在内存中，一些分区将不会被缓存，从而在每次需要这些分区时都需重新计算它们。这是系统默认的存储级别。 |
| MEMORY_AND_DISK                        | 将RDD作为非序列化的Java对象存储在jvm中。如果RDD不适合存在内存中，将这些不适合存在内存中的分区存储在磁盘中，每次需要时读出它们。 |
| MEMORY_ONLY_SER                        | 将RDD作为序列化的Java对象存储（每个分区一个byte数组）。这种方式比非序列化方式更节省空间，特别是用到快速的序列化工具时，但是会更耗费cpu资源—密集的读操作。 |
| MEMORY_AND_DISK_SER                    | 和MEMORY_ONLY_SER类似，但不是在每次需要时重复计算这些不适合存储到内存中的分区，而是将这些分区存储到磁盘中。 |
| DISK_ONLY                              | 仅仅将RDD分区存储到磁盘中                                    |
| MEMORY_ONLY_2, MEMORY_AND_DISK_2, etc. | 和上面的存储级别类似，但是复制每个分区到集群的两个节点上面   |
| OFF_HEAP (experimental)                | 以序列化的格式存储RDD到[Tachyon](http://tachyon-project.org/)中。相对于MEMORY_ONLY_SER，OFF_HEAP减少了垃圾回收的花费，允许更小的执行者共享内存池。这使其在拥有大量内存的环境下或者多并发应用程序的环境中具有更强的吸引力。 |

NOTE:在python中，存储的对象都是通过Pickle库序列化了的，所以是否选择序列化等级并不重要。

Spark也会自动持久化一些shuffle操作（如`reduceByKey`）中的中间数据，即使用户没有调用`persist`方法。这样的好处是避免了在shuffle出错情况下，需要重复计算整个输入。如果用户计划重用计算过程中产生的RDD，我们仍然推荐用户调用`persist`方法。

### 如何选择存储级别

Spark的多个存储级别意味着在内存利用率和cpu利用效率间的不同权衡。我们推荐通过下面的过程选择一个合适的存储级别：

- 如果你的RDD适合默认的存储级别（MEMORY_ONLY），就选择默认的存储级别。因为这是cpu利用率最高的选项，会使RDD上的操作尽可能的快。
- 如果不适合用默认的级别，选择MEMORY_ONLY_SER。选择一个更快的序列化库提高对象的空间使用率，但是仍能够相当快的访问。
- 除非函数计算RDD的花费较大或者它们需要过滤大量的数据，不要将RDD存储到磁盘上，否则，重复计算一个分区就会和重磁盘上读取数据一样慢。
- 如果你希望更快的错误恢复，可以利用重复(replicated)存储级别。所有的存储级别都可以通过重复计算丢失的数据来支持完整的容错，但是重复的数据能够使你在RDD上继续运行任务，而不需要重复计算丢失的数据。
- 在拥有大量内存的环境中或者多应用程序的环境中，OFF_HEAP具有如下优势：
- 它运行多个执行者共享Tachyon中相同的内存池
- 它显著地减少垃圾回收的花费
- 如果单个的执行者崩溃，缓存的数据不会丢失

### 删除数据

Spark自动的监控每个节点缓存的使用情况，利用最近最少使用原则删除老旧的数据。如果你想手动的删除RDD，可以使用`RDD.unpersist()`方法