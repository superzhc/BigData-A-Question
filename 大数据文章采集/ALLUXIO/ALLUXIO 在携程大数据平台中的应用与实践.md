ALLUXIO 在携程大数据平台中的应用与实践

> **作者简介**：郭建华，携程技术中心软件研发工程师，2016 年加入携程，在大数据平台部门从事基础框架的研究与运维，主要负责 HDFS、Alluxio 等离线平台的研发运维工作。

进入大数据时代，实时作业有着越来越重要的地位，并且部分实时和离线作业存在数据共享。实践中使用统一的资源调度平台能够减少运维工作，但同时也会带来一些问题。

本文将介绍携程大数据平台是如何引入 Alluxio 来解决 HDFS 停机维护影响实时作业的问题，并在保证实时作业不中断的同时，减少对 HDFSNameNode 的压力，以及加快部分 Spark SQL 作业的处理效率。

### 一、背景

携程作为中国旅游业的龙头，早在 2003 年就在美国上市，发展到现在，携程对外提供酒店、机票、火车票、度假、旅游等线上服务产品，每天线上有上亿的访问量，与此同时，海量的用户和访问也产生了大量的数据，这些数据包括日志以及访问记录，这些数据都需要落地到大数据平台上。

为了对这些数据进行分析, 我们在大数据方面有着大量的离线和实时作业。主集群已突破千台的规模, 有着超过 50PB 的数据量，每日的增量大概在 400TB。巨大的数据量且每天的作业数达到了 30 万，给存储和计算带来了很大的挑战。

HDFS NameNode 在存储大量数据的同时，文件数和 block 数给单点的 NameNode 处理能力带来了压力。因为数据存在共享，所以只能使用一套 HDFS 来存储数据，实时落地的数据也必须写入 HDFS。

为了缓解和优化 NameNode 的压力，我们会对 NameNode 进行源码优化，并进行停机维护。而 HDFS 的停机会导致大量的需要数据落地到 HDFS 的 Spark Streaming 作业出错，对那些实时性要求比较高的作业，比如实时推荐系统，这种影响是需要极力避免的。

![enter image description here](https://i.loli.net/2020/03/26/UibN5WZLjEDxOkh.png)

图 1 携程大数据平台架构图

图 1 为携程大数据平台架构图，DataSource 为不同的数据源，有日志信息、订单信息。它们通过携程自己研发的中间件或者直接落地到 HDFS 或者被 Spark Streaming 消费之后再落地到 HDFS。

Streaming 计算的结果有的直接落地到 Redis 或者 ElasticSearch 等快速查询平台，而有些 Streaming 计算的实时数据需要和历史数据进行再计算，则需要落地到 HDFS 上。

按照业务层不同的需求，我们提供了不同的执行引擎来对 HDFS 上的数据进行计算。执行快速的 Spark SQL 和 Kylin 主要用在 OLAP 上，Hive 和 Spark SQL 同时用在 ETL 作业上，Presto 主要用在 adhoc 查询。

上述架构能够满足大部分的工作要求，但是随着集群规模的增大，业务作业的增多，集群面临了很大的挑战，其中也存在着诸多不足。

上述架构存在以下几个问题：

1）SparkStreaming 依赖于 HDFS，当 HDFS 进行停机维护的时候，将会导致大量的 Streaming 作业出错。

2）SparkStreaming 在不进行小文件合并的情况下会生成大量的小文件，假设 Streaming 的 batch 时间为 10s，那么使用 Append 方式落地到 HDFS 的文件数在一天能达到 8640 个文件，如果用户没有进行 Repartition 来进行合并文件，那么文件数将会达到 Partition*8640。我们具有接近 400 个 Streaming 作业，每天落地的文件数量达到了 500 万，而目前我们集群的元数据已经达到了 6.4 亿，虽然每天会有合并小文件的作业进行文件合并，但太大的文件增量给 NameNode 造成了极大的压力。

3）SparkStreaming 长时间占用上千 VCores 会对高峰时期的 ETL 作业产生影响，同时，在高峰期如果 Streaming 出错，作业重试可能会出现长时间分配不到资源的情况。

为了解决上述问题，我们为 SparkStreaming 搭建了一套独立的 Hadoop 集群，包括独立的 HDFS、Yarn 等组件。

虽然上述问题得到了很好的解决，但这个方案仍然会带来一些问题。如果主集群想访问实时集群中的数据时，需要用户事先将数据 DistCp 到主集群，然后再进行数据分析。架构如图 2 所示。除了 DistCp 能够跨集群传输数据之外，我们第一个想到的就是 Alluxio。

![enter image description here](https://i.loli.net/2020/03/26/ufal1k2HE7RShgZ.png)

图 2 独立集群架构：HDFS2 独立与主集群 HDFS1 以提供资源隔离

Alluxio 作为全球第一个基于内存级别的文件系统，具有高效的读写性能，同时能够提供统一的 API 来访问不同的存储系统。它架构在传统分布式文件系统和分布式计算框架之间，为上层计算框架提供了内存级别的数据读写服务。

如图 3 所示，Alluxio 可以支持目前几乎所有的主流分布式存储系统，可以通过简单配置或者 Mount 的形式将 HDFS、S3 等挂载到 Alluxio 的一个路径下。这样我们就可以统一的通过 Alluxio 提供的 Schema 来访问不同存储系统的数据，极大的方便了客户端程序开发。

同时，对于存储在云端的数据或者计算与存储分离的场景，可以通过将热点数据 load 到 Alluxio，然后再使用计算引擎进行计算，这极大的提高了计算的效率，而且减少了每次计算需要从远程拉去数据的所导致的网络 IO。而我们利用 Alluxio 统一入口的特性，挂载了两个 HDFS 集群，从而实现了从 Alluxio 一个入口读取两个集群的功能，而具体访问哪个底层集群，完全由 Alluxio 帮我们实现了。

![enter image description here](https://i.loli.net/2020/03/26/vHAhFmGT9Ct2siW.png)

图 3 Alluxio 统一存储和抽象

### 二、解决方案

为了解决数据跨集群共享的问题，我们引入了国际知名并且开源的 Alluxio。部署的 Alluxio1.4 具有良好的稳定性和高效性，在引入 Alluxio 之后，架构如图 4 所示。

![enter image description here](https://i.loli.net/2020/03/26/XmSuUfQidLKzrjh.png)

图 4 改进后架构图

从图 4 可以看到，Spark Streaming 数据直接落地到 Alluxio，Alluxio 通过将 HDFS1 和 HDFS2 分别挂载到两个路径下。简单的通过命令：`$ alluxiofs mount /path/on/alluxio hdfs://namenode:port/path/on/hdfs` 就能分别挂载这两个 HDFS 集群。

HDFS-2 集群专门负责存储流计算的数据。数据收集到 Kafka 之后，Spark Streaming 对其进行消费，计算后的数据直接写挂载了 HDFS-2 集群的路径。

Alluxio 很友好的为 Client 提供了三种写策略，分别是：MUST_CACHE、CACHE_THROUGH、THROUGH，这三种策略分别是只写 Alluxio，同步到 HDFS，只写 HDFS。这里可以根据数据的重要性，采用不同的策略来写 Alluxio，重要的数据需要同步到 HDFS，允许数据丢失的可以采用只写 Alluxio 策略。

采用上述策略方案之后，我们发现 Alluxio 在一定程度上减少了 NameNode 的压力。部分热点数据并且多次使用的数据，我们会通过定时作业将该部分数据加载到 Alluxio，一方面加快了计算引擎加载数据的速度，另外一方面减少了对 NameNode 的数据访问请求数。

此外，Alluxio 自身实现了一个叫做 TTL（Time To Live）的功能，只要对一个路径设置了 TTL，Alluxio 内部会对这部分数据进行检测，当前时间减去路径的创建时间大于 TTL 数值的路径会触发 TTL 功能。

考虑到实用性，Alluxio 为我们提供了 Free 和 Delete 两种 Action。Delete 会将底层文件一同删除，Free 只删 Alluxio 而不删底层文件系统。为了减少 Alluxio 内存压力，我们要求写到 Alluxio 中的数据必须设置一个 TTL，这样 Alluxio 会自动将过期数据删除（通过设置 Free Action 策略，可以删除 Alluxio 而不删除 HDFS）。对于从 Alluxio 内存中加载数据的 Spark Sql 作业，我们拿取了线上的作业和从 HDFS 上读数据进行了对比，普遍提高了 30% 的执行效率。

### 三、后记

从调研 Alluxio 到落地上线 Alluxio，整个过程下来，我们碰到过一系列的问题，针对这些问题以及业务需求，开发了一系列的功能并回馈了 Alluxio 社区。

1、Alluxio 在写 HDFS 的时候，需要使用 HDFS 的 Root 账号权限，对于带 Kerberos 的 HDFS 集群，会出现无权限写。为了解决这个问题，我们为 Alluxio 单独建了一个 Hadoop 账号，所有落地到 HDFS 的数据通过该账号来写。

2、1.4 版本的 Alluxio 不支持以文件夹的形式进行 TTL 的设置，我们进行了功能的完善并贡献给社区（出现在 1.5 以及后续版本中）。

3、1.4 版本不支持 TTL 使用 Free 策略来删除数据，我们对该功能进行了完善并贡献给社区（出现在 1.5 以及后续版本中）。

4、1.4 版本底层文件发生修改，对于 Alluxio 来说是不感知的，而通过 Alluxio 读取的数据可能出现不准确（1.7 版本得到了彻底解决），我们开发了一个 shell 命令 checkConsistency 和 repairConsistency 来解决这个问题。