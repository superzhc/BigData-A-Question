# Apache Kylin 的快速数据立方体算法——概述

Apache Kylin（麒麟）是由 eBay 贡献给开源社区的大数据分析引擎，支持在超大数据集上进行秒级别的 SQL 及 OLAP 查询，目前是 Apache 基金会的孵化项目 [1]。本文是一系列介绍快速数据立方体计算（Fast Cubing）的第一篇，将从概念上介绍新算法与旧算法的区别以及分析它的优劣。该算法目前正在内部进行测试和改进，将在 Apache Kylin 后续版本中发布。源代码已经公开在 Kylin 的 Git 代码库中 [2]，感兴趣的读者可以到相应分支查看。

背景：Kylin 使用 Hadoop 结合数据立方体（Cube）技术实现多维度快速 OLAP 分析能力的。关于数据立方体概念，请参考 [3]。

## 逐层算法

在介绍快速 Cube 算法之前，我们先简单回顾一下现有的算法，也称之为“逐层算法”（By Layer Cubing）。

我们知道，一个 N 维的完全 Cube，是由：1 个 N 维子立方体（Cuboid）， N 个（N-1）维 Cuboid, N*(N-1)/2 个 (N-2) 维 Cuboid …, N 个 1 维 Cuboid, 1 个 0 维 Cuboid，总共 2^N 个子立方体组成的；在“逐层算法”中，按维度数逐渐减少来计算，每个层级的计算（除了第一层，它是从原始数据聚合而来），是基于它上一层级的结果来计算的。

举例子来说，[Group by A, B] 的结果，可以基于 [Group by A, B, C] 的结果，通过去掉 C 后聚合得来的；这样可以减少重复计算；当 0 维度 Cuboid 计算出来的时候，整个 Cube 的计算也就完成了。

图 1 展示了用该算法计算一个四维 Cube 的流程。

![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\ed38c518cc133e153b5344ea68fff38a.png)

图 1 逐层算法

此算法的 Mapper 和 Reducer 都比较简单。Mapper 以上一层 Cuboid 的结果（Key-Value 对）作为输入。由于 Key 是由各维度值拼接在一起，从其中找出要聚合的维度，去掉它的值成新的 Key，然后把新 Key 和 Value 输出，进而 Hadoop MapReduce 对所有新 Key 进行排序、洗牌（shuffle）、再送到 Reducer 处；Reducer 的输入会是一组有相同 Key 的 Value 集合，对这些 Value 做聚合计算，再结合 Key 输出就完成了一轮计算。

每一轮的计算都是一个 MapReduce 任务，且串行执行； 一个 N 维的 Cube，至少需要 N 次 MapReduce Job。

### 算法优点

- 此算法充分利用了 MapReduce 的能力，处理了中间复杂的排序和洗牌工作，故而算法代码清晰简单，易于维护；
- 受益于 Hadoop 的日趋成熟，此算法对集群要求低，运行稳定；在内部维护 Kylin 的过程中，很少遇到在这几步出错的情况；即便是在 Hadoop 集群比较繁忙的时候，任务也能完成。

### 算法缺点

- 当 Cube 有比较多维度的时候，所需要的 MapReduce 任务也相应增加；由于 Hadoop 的任务调度需要耗费额外资源，特别是集群较庞大的时候，反复递交任务造成的额外开销会相当可观；
- 由于 Mapper 不做预聚合，此算法会对 Hadoop MapReduce 输出较多数据 ; 虽然已经使用了 Combiner 来减少从 Mapper 端到 Reducer 端的数据传输，所有数据依然需要通过 Hadoop MapReduce 来排序和组合才能被聚合，无形之中增加了集群的压力 ;
- 对 HDFS 的读写操作较多：由于每一层计算的输出会用做下一层计算的输入，这些 Key-Value 需要写到 HDFS 上；当所有计算都完成后，Kylin 还需要额外的一轮任务将这些文件转成 HBase 的 HFile 格式，以导入到 HBase 中去；
- 总体而言，该算法的效率较低，尤其是当 Cube 维度数较大的时候；时常有用户问，是否能改进 Cube 算法，缩短时间。

## 快速 Cube 算法

快速 Cube 算法（Fast Cubing）是麒麟团队对新算法的一个统称，它还被称作“逐段”(By Segment) 或“逐块”(By Split) 算法。

该算法的主要思想是，对 Mapper 所分配的数据块，将它计算成一个完整的小 Cube 段（包含所有 Cuboid）；每个 Mapper 将计算完的 Cube 段输出给 Reducer 做合并，生成大 Cube，也就是最终结果；图 2 解释了此流程。

![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\13b065d1501c7f46a2dacf6b1a6d6486.png)

图 2 逐块 Cube 算法

### **Mapper****的预聚合**

与旧算法相比，快速算法主要有两点不同：

- Mapper 会利用内存做预聚合，算出所有组合；Mapper 输出的每个 Key 都是不同的，这样会减少输出到 Hadoop MapReduce 的数据量，Combiner 也不再需要；
- 一轮 MapReduce 便会完成所有层次的计算，减少 Hadoop 任务的调配。

我们看一个例子：某个 Cube 有四个维度：A、B、C、D；每个 Mapper 分配到的数据块有约一百万条记录；在这一百万条记录中，每个维度的基数 (Cardinality) 分别是 Card(A), Card(B), Card(C), Card(D)。

当从原始数据计算四维 Cuboid（ID： 1111）的时候：旧算法的 Mapper 会简单地对每条记录去除不相关的维度，然后输出到 Hadoop，所以输出量依然是一百万条；新算法的 Mapper，由于做了聚合，它只输出 [count distinct A, B, C, D] 条记录到 Hadoop，此数目肯定小于原始条数；在很多时候下，它会是原来的 1/10 甚至 1/1000。

当从四维 Cuboid 1111 计算三维 Cuboid 如 0111 的时候，维度 A 会被聚合掉；假定 A 维度的值均匀分布，那么聚合后的记录数会是四维 Cuboid 记录数的 1/ Card(A),；而旧算法的 Mapper 输出数跟四维 Cuboid 记录数相同。

可以看到，在 Cuboid 的推算过程中的每一步，新算法都会比旧算法产生更少数据；总的加起来，新算法中的 Mapper 对 Hadoop 的输出，会比老算法少一个或几个数量级，具体数字取决于用户数据的特性；越少的数据，意味着越少的 I/O 和 CPU，从而使得性能得以提升。

### 子立方体生成树的遍历

值得一提的还有一个改动，就是子立方体生成树 (Cuboid Spanning Tree) 的遍历次序；在旧算法中，Kylin 按照层级，也就是广度优先遍历 (Broad First Search) 的次序计算出各个 Cuboid；在快速 Cube 算法中，Mapper 会按深度优先遍历（Depth First Search）来计算各个 Cuboid。深度优先遍历是一个递归方法，将父 Cuboid 压栈以计算子 Cuboid，直到没有子 Cuboid 需要计算时才出栈并输出给 Hadoop；最多需要暂存 N 个 Cuboid，N 是 Cube 维度数。

采用 DFS，是为了兼顾 CPU 和内存：

- 从父 Cuboid 计算子 Cuboid，避免重复计算；
- 只压栈当前计算的 Cuboid 的父 Cuboid，减少内存占用。

![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\e6cd0322668edccdb732cbb45ac69410.png)

图 3 子立方体生成树的遍历

图 3 是一个四维 Cube 的完整生成树；按照 DFS 的次序，在 0 维 Cuboid 输出前的计算次序是 ABCD -> BCD -> CD -> D -> *， ABCD, BCD, CD 和 D 需要被暂存；在 * 被输出后，D 可被输出，内存得到释放；在 C 被计算并输出后，CD 就可以被输出； ABCD 最后被输出。

采用 DFS，Mapper 的输出会是排序的（某些特殊情况除外）：Cube 行键 (row key) 是由 [Cuboid ID + 维度值] 组成；DFS 访问的结果，恰好是按照 Cuboid ID 从小到大输出；而在同一个 Cuboid 内，维度值也是升序排序；所以总的输出是排序的，请看如下示例。

```
0000 
0001[D0] 
0001[D1] 
.... 
0010[C0] 
0010[C1] 
.... 
0011[C0][D0] 
0011[C0][D1] 
.... 
.... 
1111[A0][B0][C0][D0] 
....
```

注: 这里 [D0] 代表 D 维度的最小值，[D1] 代表次小值，以此类推。

由于每个 Mapper 的输出都是排序的，Hadoop 对这些输出进行归并排序的效率也会更高。

### OutOfMemory error

在新算法的开发和测试初期，我们发现 Mapper 常常会遇到 OutOfMemory 而异常终止；总结下来，以下情况往往会导致该异常:

a) Hadoop Mapper 所分配的堆内存较小 ;­­­­­­­

b) Cube 中使用了"Distinct count" (HyperLogLog 会占用较大内存);

c) Cube 的维度较多，导致生成树较深；

d) 分配到 Mapper 的数据块过大；

简单的增大 Mapper 的 JVM heap size 可以暂时解决该问题；但是不是每个用户的 Hadoop 机器都有大内存；算法需要足够的健壮性和适应性，否则用户会很头疼；我们花了不少努力来优化该算法，例如主动探测 OOM 的发生，将堆栈中的 Cuboid 缓存到本地磁盘等；这一系列优化在 eBay 内部测试的结果非常好，OOM 的发生率大大降低，而性能没有明显的下降。

下面我们对快速 Cube 算法做一个总结。

### 算法优点

- 比老算法性能更好；下图是一个新老算法在两个案例上的所耗时间对比（分钟），能减少约 30% 到 50%；

  ![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\662e8d1ad8373bbd32e604befc2de029.jpg)

- Mapper 内的 Cube 计算逻辑可以被其它 Cube 引擎重用，例如流数据 (Streaming) 和 Spark; 实际上 Kylin 已经在这么做了。

### 算法缺点

- 新算法略复杂，学习曲线更陡；
- 虽然新算法会在内存不足时会把数据暂存到本地磁盘，要获取最佳性能，最好给 Mapper 以足够内存，用户要在输入数据块大小、Mapper 配置、Cube 复杂度之间找到平衡，需具备更多知识和经验。

## 快速算法的其它改进

本文概述了快速 Cube 算法的主要思想；其实 Kylin 在引入此算法的同时，还引入了其它一些改进，例如基于采样的 Region 切分，一步直接生成 HFile，基于 HBase 表的 Cube 合并等；这些改变都影响了 Cube 的构建，是 Kylin 管理员所需要了解的，我们将在后续文章中做详细阐述，敬请关注。

如果你对 Apache Kylin 项目感兴趣，欢迎访问项目主页：

[http://kylin.incubator.apache.org](http://kylin.incubator.apache.org/)

或订阅邮件列表：

[user@kylin.incubator.apache.org ](mailto:user@kylin.incubator.apache.org)和 [dev@kylin.incubator.apache.org](mailto:dev@kylin.incubator.apache.org)

或订阅微信公众号:ApacheKylin

项目地址：[ http://kylin.io](http://kylin.io/)

## 参考

[1] Apache Kylin 主页: https://kylin.incubator.apache.org/

[2] Apache Kylin Git 镜像: https://github.com/apache/incubator-kylin

[3] Data Cubes： http://www2.cs.uregina.ca/~dbd/cs831/notes/dcubes/dcubes.html