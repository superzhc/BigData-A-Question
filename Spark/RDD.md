RDD，全称为 Resilient Distributed Datasets，是一个容错的、并行的数据结构，可以让用户显式地将数据存储到磁盘和内存中，并能控制数据的分区。同时，RDD 还提供了一组丰富的操作来操作这些数据。在这些操作中，诸如 map、flatMap、filter 等转换操作实现了 monad 模式，很好地契合了 Scala 的集合操作。除此之外，RDD 还提供了诸如 join、groupBy、reduceByKey 等更为方便的操作（注意，reduceByKey 是 action，而非 transformation），以支持常见的数据运算。

通常来讲，针对数据处理有几种常见模型，包括：Iterative Algorithms，Relational Queries，MapReduce，Stream Processing。例如 Hadoop MapReduce 采用了 MapReduces 模型，Storm 则采用了 Stream Processing 模型。RDD 混合了这四种模型，使得 Spark 可以应用于各种大数据处理场景。

RDD 作为数据结构，本质上是一个只读的分区记录集合。一个 RDD 可以包含多个分区，每个分区就是一个 dataset 片段。RDD 可以相互依赖。如果 RDD 的每个分区最多只能被一个 Child RDD 的一个分区使用，则称之为 narrow dependency；若多个 Child RDD 分区都可以依赖，则称之为 wide dependency。不同的操作依据其特性，可能会产生不同的依赖。例如 map 操作会产生 narrow dependency，而 join 操作则产生 wide dependency。

Spark 之所以将依赖分为 narrow 与 wide，基于两点原因。

首先，narrow dependencies 可以支持在同一个 cluster node 上以管道形式执行多条命令，例如在执行了 map 后，紧接着执行 filter。相反，wide dependencies 需要所有的父分区都是可用的，可能还需要调用类似 MapReduce 之类的操作进行跨节点传递。

其次，则是从失败恢复的角度考虑。narrow dependencies 的失败恢复更有效，因为它只需要重新计算丢失的 parent partition 即可，而且可以并行地在不同节点进行重计算。而 wide dependencies 牵涉到 RDD 各级的多个 Parent Partitions。下图说明了 narrow dependencies 与 wide dependencies 之间的区别：

![img](D:\superz\BigData-A-Question\Spark\images\6a7e4a801dfc0d37efca7f5bd1431174.jpg)

本图来自 Matei Zaharia 撰写的论文 An Architecture for Fast and General Data Processing on Large Clusters。图中，一个 box 代表一个 RDD，一个带阴影的矩形框代表一个 partition。

## RDD 如何保障数据处理效率？

RDD 提供了两方面的特性 persistence 和 patitioning，用户可以通过 persist 与 patitionBy 函数来控制 RDD 的这两个方面。RDD 的分区特性与并行计算能力 (RDD 定义了 parallerize 函数)，使得 Spark 可以更好地利用可伸缩的硬件资源。若将分区与持久化二者结合起来，就能更加高效地处理海量数据。例如：

```scala
input.map(parseArticle _).partitionBy(partitioner).cache()
```

partitionBy 函数需要接受一个 Partitioner 对象，如：

```scala
val partitioner = new HashPartitioner(sc.defaultParallelism)
```

RDD 本质上是一个内存数据集，在访问 RDD 时，指针只会指向与操作相关的部分。例如存在一个面向列的数据结构，其中一个实现为 Int 的数组，另一个实现为 Float 的数组。如果只需要访问 Int 字段，RDD 的指针可以只访问 Int 数组，避免了对整个数据结构的扫描。

RDD 将操作分为两类：transformation 与 action。无论执行了多少次 transformation 操作，RDD 都不会真正执行运算，只有当 action 操作被执行时，运算才会触发。而在 RDD 的内部实现机制中，底层接口则是基于迭代器的，从而使得数据访问变得更高效，也避免了大量中间结果对内存的消耗。

在实现时，RDD 针对 transformation 操作，都提供了对应的继承自 RDD 的类型，例如 map 操作会返回 MappedRDD，而 flatMap 则返回 FlatMappedRDD。当我们执行 map 或 flatMap 操作时，不过是将当前 RDD 对象传递给对应的 RDD 对象而已。例如：

```scala
def map[U: ClassTag](f: T => U): RDD[U] = new MappedRDD(this, sc.clean(f))
```

这些继承自 RDD 的类都定义了 compute 函数。该函数会在 action 操作被调用时触发，在函数内部是通过迭代器进行对应的转换操作：

```scala
private[spark] class MappedRDD[U: ClassTag, T: ClassTag](prev: RDD[T], f: T => U) extends RDD[U](prev) {

  override def getPartitions: Array[Partition] = firstParent[T].partitions

  override def compute(split: Partition, context: TaskContext) =
    firstParent[T].iterator(split, context).map(f)
}
```

## RDD 对容错的支持

支持容错通常采用两种方式：数据复制或日志记录。对于以数据为中心的系统而言，这两种方式都非常昂贵，因为它需要跨集群网络拷贝大量数据，毕竟带宽的数据远远低于内存。

RDD 天生是支持容错的。首先，它自身是一个不变的 (immutable) 数据集，其次，它能够记住构建它的操作图（Graph of Operation），因此当执行任务的 Worker 失败时，完全可以通过操作图获得之前执行的操作，进行重新计算。由于无需采用 replication 方式支持容错，很好地降低了跨网络的数据传输成本。

不过，在某些场景下，Spark 也需要利用记录日志的方式来支持容错。例如，在 Spark Streaming 中，针对数据进行 update 操作，或者调用 Streaming 提供的 window 操作时，就需要恢复执行过程的中间状态。此时，需要通过 Spark 提供的 checkpoint 机制，以支持操作能够从 checkpoint 得到恢复。

针对 RDD 的 wide dependency，最有效的容错方式同样还是采用 checkpoint 机制。不过，似乎 Spark 的最新版本仍然没有引入 auto checkpointing 机制。

## 总结

RDD 是 Spark 的核心，也是整个 Spark 的架构基础。它的特性可以总结如下：

- 它是不变的数据结构存储
- 它是支持跨集群的分布式数据结构
- 可以根据数据记录的 key 对结构进行分区
- 提供了粗粒度的操作，且这些操作都支持分区
- 它将数据存储在内存中，从而提供了低延迟性