# Apache Kylin 在携程的实践

> 在近期的 Apache Kylin Meetup 上，携程大数据资深研发工程师张巍分享了 Kylin 在携程的应用。本文为大家介绍携程当前的架构以及使用 Kylin 过程中的挑战与心得。

携程在 2016 年左右开始应用 Kylin 的解决方案。在 2018 年的 5、6 月份，我作为小白接手了 Kylin，逐渐琢磨、踩坑，折腾折腾就过来了。我将介绍 Kylin 在携程这一年的发展历程，碰到的挑战，以及解决的问题。

## 背景

### 1. 早期架构

下图是携程早期的 OLAP 结构，比较简单。有两个应用，一个是 BI 分析报表工具，另一个是自助分析的 Adhoc 平台，下层主要是 Hive，技术比较单一。Hive 是比较慢的运行引擎，但是很稳定。期间我们也使用过 Shark，但 Shark 维护成本比较高，所以后面也被替换掉了。文件存储用的是 HDFS。整个架构是比较简单的，搭建过程中成本也比较低。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\a1ec363b6edfffd57d0e0472333659bf.png)

早期架构的特点：一个字**慢！** 两字**很慢！\**三个字\**非常慢！！！**

### 2. 技术选型

随着业务需求的多样化发展，我们团队引入了许多 OLAP 引擎，其中也包括了 Kylin。这里我们重点介绍下选择 Kylin 所考虑的几个方面：

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\e065afbec6aae14982fe66679b2682b1.png)

**百亿数据集支持：**

首先对我们来说，海量数据的支持必不可少的。因为很多的用户向我们抱怨，由于携程早期都是采用微软的解决方案，几乎没办法支撑百亿级的数据分析，即便使用 Hive，也需要等待很长时间才能得到结果。

**SQL** **支持：**

很多的分析人员之前使用的 SQL Server, 所以即使迁移到新的技术也希望能保留使用 SQL 的习惯。

**亚秒级响应：**

还有很多的用户反馈，他们需要更快的响应速度，Hive、Spark SQL 响应只能达到分钟级别，MPP 数据库像 Presto、ClickHouse 也只能做到秒级，毫秒级是很困难的。

**高并发：**

在一定的用户规模下，并发查询的场景非常普遍。仅仅通过扩容是非常消耗机器资源的，一定规模下维护成本也很高。而且，传统的 MPP 会随着并发度升高，性能出现急剧的下降。就拿 Presto 来说，一般单个查询消耗 10 s，如果同时压 100 个并发，就出不了结果。Kylin 在这一方面的表现好很多。

**HBase** **的技术储备：**

携程有大量使用 HBase 的场景，在我们大数据团队中有精通 HBase 的开发人员，而 Kylin 的存储采用 HBase，所以运维起来我们会更得心应手。

**离线多：**

携程目前离线分析的场景比较多，Kylin 在离线分析场景下属于比较成熟的解决方案，所以我们选择了 Kylin。

## 当前架构

首先我们先看一下目前 Kylin 在携程的使用规模。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\161e0f2810799ea18cc8b25f1a8fcc92.png)

Cube 的数量现在稳定在 300 多个，覆盖 7 个业务线，其中最大的业务线是度假玩乐。目前单份数据存储总量是 56 T，考虑到 HDFS 三份拷贝，所以总存储量大约 182 T，数据规模达到 300 亿条左右。最大的 Cube 来自于火车票业务，一天最大的数据是 28 亿，一天次构建最大的结果集在 13 T 左右。查询次数比较固定，基本上是 20 万查询 / 天，通过 Kylin 的查询日志分析下来，90% 的查询可以达到 300 ms 左右。

下图是 OLAP 的架构图，在携程主打的 OLAP 采用 Spark、Presto 和 Kylin，Hive 慢慢被 Spark 给替代。Kylin 服务两个业务产品，一块是 Artnova BI 分析工具，还有其他的业务部门报表产品，也会接入 Kylin。存储层是 HBase、HDFS 等等。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\47da84969142720863c233af5d56b63f.png)

这里顺便提到元数据的管理，目前正在做这块的开发，其中包含字段的血缘分析、表的特征分析，后期 Kylin 会根据分析的结果自动替用户构建 Cube。打个比方：某些表，用户频繁的访问，或者在维度很固定的情况下，Kylin 就会自动配置一个对应的 Cube。前端的报表自动就匹配了 Kylin 作为查询引擎，对用户来说，之前每次要 20 s 才能展示的报表，突然有一天只需要 500 ms 就可以展示了。

另外，我们计划将 Kylin 接入规则引擎，从而给数据产品提供统一的入口。并且提供查询自动降级等对用户友好的功能。

我们再看下 Kylin 部署图：

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\049c8153e9f7833893c128339364cef8.png)

### 1. 两套集群

主要考虑到 MR 构建的性能。目前在大集群上跑 MR, 由于 Hadoop 在高峰时期持续到中午都非常繁忙，计算资源基本上是满负荷。一次 MR 任务的调度需要等待 20 秒左右才跑起来，对于准实时的构建性能影响非常大，很难满足用户的准实时数据落地的需求。虽然目前暂时把任务优先级别调高，但提升也是比较有限的。

### 2. 负载均衡

负载均衡是为了防止单机宕机对用户产生的影响，是出于稳定性的考虑。

### 3. 独立的 HBase + HDFS 查询集群

脱离线上大集群，单独构建 HBase + HDFS 集群, 可以大大减少其他因素对于查询性能的影响。

### 4. 共享的计算集群

在我们接收 Kylin 之前，整个 Kylin 的部署是 7 个业务线，7 个 Kylin 的 Instance，比方说火车票这个 Instance 只有 2 个 Cube，每天的查询量几百次。单个节点放在那里很多时间都是空闲的，浪费资源不说，维护成本也高。

为什么要读写分离？ 因为 Kylin 是典型的适用于一次写、多次查询的场景，对于查询，最好是其他不相关的干扰因素越少越好。在之前的构建和查询混杂在一起的情况下，查询性能受制于构建任务，互相制约，难以保证服务的稳定性。

## 监控

我们自己开发了一套 Kylin 集群的监控系统。

### 1. Kylin HBase 集群的监控，把握 HBase 的负载

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\b9b6f1ac5d0eb1dbbc25a464c7b025ab.png)

这里我们说个故事。几个月前，Kylin 出现了一个很大的与 Kylin HBase 相关的问题。因为 HBase Master 进程和 ZooKeeper 失联了，然后 HMaster 两个都失联，全部挂了之后，整个 HBase Master 出现宕机状态。我们最后是通过上图的页面发现当时 HMaster 系统的 CPU 特别高，然后顺藤摸瓜，找出问题的根源。

### 2. 查询性能监控

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\fe2ac14df2070a0ea3cf6dec906b9b94.png)

可以更加实时地看到各个时段平均查询的响应速度，我们每次优化之后都能通过这个页面看到优化的效果是否明显。

### 3. 清理合并任务的运行状态监控告警

通过这套监控系统，我们可以实时把握 Kylin 的垃圾清理状态。Kylin 中数据垃圾的堆积是灾难性的, 如果不监控，积少成多的失败会导致灾难性的后果。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\07545d340c33d4754442b8719d929d2f.png)

这里说个故事。之前由于合并清理任务没有及时地被监控，导致长时间的失败，我们都没有在意，后果是什么呢？HBase 的 Region 数量暴增到 5 万以上，导致 HBase 的压力特别大，最后无法支撑，从而挂机，这是个惨痛的代价。

## 经验分享

### 1. 维度组合优化

像度假部门，他们很多的场景需要 20、30 个维度。在这种场景下，我们需要确定强制维度。为什么使用强制维度？比方说：用户门票分析，有些维度是必需的，比如性别、姓名、身份证。通过优化其实可以将原先 30 个维度的场景减少到 14 个以内。

### 2. 高基字典

如果在 Kylin 上面配高基 dict，fact distinct 这个步骤就会对于高基的字典生成一个很大的如下 Sequence File，如果这个文件达到 700 MB，Hadoop 初始化 StreamBuffer 时就会抛出异常。

1.4G hdfs://ns/kylin/kylin_metadata/kylin-a2696e1c-1516-4ff1-800e-5f7b940d203a/C_PaymentCR_Flat_clone/fact_distinct_columns/V_OLAPPAY_CONVERTRATE_FLAT.SITE

[异常详情与解决方法](https://plumbr.io/outofmemoryerror/requested-array-size-exceeds-vm-limit)见这里。

### 3. MR 内存分配

精确去重时，Kylin 会做全局字典，在 MR 构建过程中，全局字典在 Kylin 里会被切分成很多 Slice，构建过程中这些 Slice 存放在缓存中。由于缓存需要控制大小，所以通过不停地换入换出，保证缓存的大小可控。由于每个数据其实都需要访问其中一个 Slice，我们遇到的问题是：因为内存太少，整个全局字典 Slice 换入换出太频繁，一条数据过来之后，找不到 Slice，会从 HDFS 中加载。整个过程非常缓慢，当我们把内存调高了一倍之后，构建速度明显改善。

### 4. 构建调度缓慢

Kylin 依赖 Scheduler 的实现去调度 Job 构建任务，在 Streaming 的构建场景下，会积累大量的历史 Job 信息。Scheduler 在每个步骤调度的间隔会去扫描 Job 历史，从而获取哪些 Job 需要继续被构建下去。在这个过程中，Scheduler 会一个一个 RPC 请求访问 HBase。如果 Job 历史超过 1 万个，1 万次 RPC 的请求耗时大概有 1 分钟左右，这样大大影响 Scheduler 的调度性能。我们这里通过缓存需要被调度的 Job 信息，减少了 99% 的无用的 HBase RPC 调用，从而提高了整体调度间隔消耗的时间。

### 5. Merge 上传无效字典

用过 Kylin 的人都知道，在 Kylin 构建过程中，有很多的 Segments 之后，要把它们 merge。Streaming 产生新 Segments 的频率很高，因此，可能 5 分钟就出现一个 Segment。一天里，一个 Cube 就可以产生 1440 个 Segments。在 merge 的过程中，即使 merge 两个 Segments，Kylin 也会上传 1440 个 Segments 的元信息。这块我们已经进行优化，并且成功合并到了 [Kylin 2.6.0 之后的版本](https://issues.apache.org/jira/browse/KYLIN-3826)。

### 6. 数据安全

当我们迁移到统一的公用集群之后，我们要考虑数据安全，每一个用户仅仅对自己的 Cube 可见，所以我们在 Kylin 2.3.1 版本上，加了用户隔离的逻辑。对于用户来说，这样可以直接看到自己业务相关的 Cube。

### 7. 节点自动探知

去年，我们出现了一个事故。当时，凌晨一个查询节点出现岩机器宕机故障。由于 Kylin 配置中 kylin.rest.servers 指明了所有同步节点的 Host，导致即使出现一个节点宕机，同步请求依然会不停地发向这个节点，最后所有同步的线程被阻塞。读写分离的集群出现了灾难性的数据不同步情况。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\ce5309b35dac06b7abe0387b1c2fae5c.png)

我们的方案：[引进了服务发现组建 ZooKeeper ](https://issues.apache.org/jira/browse/KYLIN-3810)。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\bb3ca7fc0f518d2bd03d2daef9c5bdfd.png)

### 8. 独立 HBase 的 HA 模式

去年，我们 HBase NameNode 主备切换导致 Kylin 无法正常工作的问题。我们实现了通过 namespace 的方式来访问 HDFS，并将相关的改进提交到[社区](https://issues.apache.org/jira/browse/KYLIN-3811)。

### 9. 设置 kylin.storage.hbase.max-region-count

控制 hbase.max-region-count 其实可以有效地控制生成 HFile 过程中对于 DataNode 的写压力。比如说：在 HDFS 集群比较有限的情况下，大量的 MR 写操作，会给 HDFS 系统带来很大的压力，减少这个值可以有效地控制 MR 写 HFile 的并发度，但也会影响构建性能，这个需要权衡。

## 案例分享

### 离线分析案例

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\4911e84484b9d73a7b3d08835c999220.png)

携程之前使用的是 OpenTSDB+Hive。采用 Kylin 前，先从 Hive 先生成聚合表，然后导入 HBase，通过 OpenTSDB 去分析，现在积累了接近百亿的数据，随着数据的增长，老的方案已经无法满足业务需求了，而且同步数据成本高，OpenTSDB 没办法支持精准去重响应时间也很差。用了 Kylin 之后，现在的业务规模已经可以支撑上百亿了，目前已经配有 200 个左右的线上活跃的 Cube。

**实时分析案例**

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\4a5207004c35777094eaf1e1d9742843.png)

这个是去年 3、4 月份用户提的新需求。Kylin 现在是上图所示的 Streaming-Cube 的架构，Kylin 接入的是携程的 Hermes，Hermes 是 Kafka 的一个封装。我们现在支持原生 Kafka 接入和 Hermes 接入，底层沿用 MR，因为我们测试过 Spark，其实很多的场景上和 MR 相当，效果不是特别明显。

![Apache Kylin 在携程的实践](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\40b064df538e71ac625817619bcc9718.png)

这部分主要是用于度假预订状态告警，度假团队需要去分析用户预订的情况，准确实时地发送给客服人员任何预订失败等错误状况，所以这块对于数据构建落地的时间敏感度比较高。目前，通过一系列优化，Streaming 的构建基本保持在 5 分钟左右，可以满足一部分业务的需求。但是，更大的挑战是达到一分钟以内，也就是说秒级构建，所以对于我们来说 Streaming-realtime 会是一个值得尝试的方向。

## 展望

携程针对 Kylin 主要有两方面的展望。

### 1. 支持自动构建 Cube

这块我们目前在调研，通过分析应用采集的元数据、SQL 特征，可以自动地为用户构建 Cube，为用户节约 Kylin 的学习成本，同时减少重复查询对于 MPP 的压力。

### 2. Real-time Streaming 的调研和落地

为了能够更加丰富 Kylin 的使用场景，我们打算对 eBay 为 Kylin 贡献的[实时流处理技术](https://mp.weixin.qq.com/s?__biz=MzAwODE3ODU5MA==&mid=2653079081&idx=1&sn=7a5088406e01f6bfa62ffb02d7254d2f&scene=21#wechat_redirect)做进一步调研和落地工作。

## Q&A

Q：演讲中提到的构建的 Cube 有 20 个指标，这种情况下去重，是精准去重还是近似去重？有多少个指标呢？

A：用户配的是精确。精确去重指标不会太多。

Q：演讲中提到 20 个维度的响应时间是亚秒级，有 20 个维度。请问你们做了哪些优化的工作来达到如此快的响应时间？

A：我们构建的时候，对于这种维度多的情况，建议当用户采取了以下 3 种措施来优化查询：

§ 使用 Mandatory Dimension；

§ 实现分布式缓存；

§ 配置高基维度的时候，会建议他们把高基维度往前移，这样会更高效地命中 Cube，并减小扫描的数据范围）。

Q：配了 20 个维度，最终产生的 Cube 单日有多大？

A：最大的 Cube 日产生 13 T 的数据。

Q：刚刚提到的监控方案是你们自主研发的，还是有开源的方案可以用？

A：监控是我们自主研发的。我们接入了公司已经成熟的监控平台，避免反复造轮子。

Q：分享里提到的实时 5 分钟构建一次，我理解是采用批操作，并不是真正的流，而是把流几分钟拆成一个批次。是吗？

A：对的。

Q：前面讲到底层用的 MR，没用 Spark，因为觉得时间上并没有什么节省。这个是 Spark 本身的原因，还是因为你们的任务还不是很大的量？因为每次 Spark 启任务的时间和 MR 相比有差别？

A：离线这块目前可以达到要求，所以还没有转成 Spark。我们在实时这块用 Spark 的过程中，就是像你说的，每次提交任务就很慢，达不到要求。

Q：是因为频繁提交的问题？不是因为它本身？

A：对，不是因为它本身。我们也在调研如何避免每个构建过程都启动一次 driver。

Q：在我之前的应用场景里，有一个维度特别的高基维，每天增量就很大，我们查询机制里这个维度是必选的。比如说是人的工号，里面放了很多人，然后我们要去预计算，如果说这个维度非常高，数据量会非常大，这种情况下你们会采取什么办法呢？

A：高基字段可以设置下 shard by。

Q：携程每天预计算的集群大概是有多大？

A：离线集群是 2 台物理机，每台 100 多 G 的物理机，查询节点放了 4 台虚机。实时这块，因为用户量目前不多，所以都是建在虚机上，所以内存也不大。

Q：在维度特别大，数据量又很大的情况下，剪枝的话，Cuboid 大概会控制在多少？

A：维度特别大的情况，我们最多是 4096 个 Cuboid。