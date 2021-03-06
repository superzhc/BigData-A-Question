企业的生产活动会产生各种各样的数据，数据作为企业最重要的资产之一，价值巨大，数据价值的获取需要对其进行不断访问（或读或写），不同的数据访问需求就构成了相互区别的数据访问场景，只有按照场景定制数据的存储、检索、传输以及计算加工方案，才有可能提供整体最优的数据访问性能。

滴滴 OLAP 引擎细分多种场景，如灵活分析、固化分析、热点数据等，针对不同场景的特性，使用不同的场景引擎。Apache Kylin 作为滴滴 OLAP 引擎内部的固化分析场景引擎，间接服务了滴滴下游多个重要的数据产品，覆盖了十多条业务线，为用户提供了稳定、可靠、高效的数据分析性能。

数据访问场景

 2.1 重视区分数据访问场景

为什么需要重视区分数据访问场景？怎么区分数据访问场景？

本质上，数据访问场景是一类数据访问需求，数据访问需求可以通过考察以下几个方面进行归类识别：

1. 期望进行的查询以及各个查询的查询频度和占查询总量的比例；
2. 每类查询（行、列、字节等）结果的数据量级分布；
3. 读取和更新数据的关系，如读写比例、数据更新粒度等；
4. 数据的工作规模以及在本机使用数据的方式；
5. 事务以及事务隔离级别的需求；
6. 数据副本和逻辑完整性的需求；
7. 对每类查询的延迟和吞吐量的需求；

等等，针对不同的数据访问场景，需要定制不同的数据存储、检索、传输以及计算加工方案，只有这样，才能借助场景特性，设计更有针对性的实现方案，以更加契合该场景下的数据使用，最大化整体数据访问性能。

 2.2 典型的数据访问场景

典型的数据访问场景有：OLTP（On-Line Transaction Processing）场景和 OLAP（On-Line Analysis Processing）场景，OLAP 场景下至少又可细分为以探索分析为目的的灵活分析和有严格数据访问模式的固化分析两类场景，这里提到的严格数据访问模式是指在数据产出前就已确定可对该数据执行哪些具体分析，其有明确的边界，更具体地是指该数据存在固定的分析维度，固定的分析指标以及在指标之上有固定的聚合方式。

表 2.2-1 对灵活分析和固化分析进行了对比总结：

![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\640.jpg)

在互联网企业，尤其是以数据驱动为核心的互联网企业，灵活分析和固化分析二者不可或缺，其中固化分析的一种主要应用就是报表类数据产品（当然，并非唯一，其他应用方式也不可忽略）。灵活分析和固化分析二者的整合催生了滴滴支持多场景的 OLAP 引擎的诞生。

支持多场景的 OLAP 引擎

 3.1 OLAP 引擎现状

当前，OLAP 引擎众多。在大数据领域，如 Hive，依赖 Hadoop 提供的可靠存储和计算能力，为用户提供稳定的分析服务，再比如 Presto、Spark SQL，突出内存计算，可以满足用户快速的分析需求。这些产品虽然侧重不同，但都能覆盖灵活分析和固化分析场景需求。

但从严格意义上讲，这些产品在设计上都没有细分两种场景，而是将二者抽象为一类分析需求，即认为只有一种场景，这样设计的好处是可以统一领域问题处理，但因为模糊了两种场景各自的特点，在两种使用场景共存的应用中，总体数据访问性能上存在较大的提升空间。

 3.2 滴滴 OLAP 引擎

滴滴 OLAP 引擎，区分灵活分析和固化分析，同时对于热点数据，也作为一种特有场景进行处理。

![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\640.webp)

图 3.2-1 OLAP 引擎方案（忽略部分组件关联连线）

如图 3.2-1，整体方案上，查询请求首先进入 OLAP 引擎，查询路由会基于元数据选择合适的场景引擎，然后根据所选择的场景引擎重写查询，完成重写的查询会被发往场景引擎完成执行并返回查询结果。

此外，OLAP 引擎还包含了一个数据转移决策模块，可以根据用户提供的先验知识及自身决策将数据转移到更契合的场景引擎。调度模块根据这一决策触发数据在场景引擎间的“复制”，以及无效“副本”的清除。新生成的“复制副本”通常会提供比老旧“副本”更好的访问性能，即需要花费更小的访问 Cost（代价）。

 3.3 固化分析场景实现方案

章节 2.2 中已经分析了固化分析场景的特点，不难发现，该场景下非常适合对数据进行聚合预计算并缓存聚合结果，即聚合缓存。

固化分析有固定的分析维度、指标及其聚合方式，查询边界明确，对原始明细数据执行聚合预计算，并将结果缓存到 Key-Value 存储，不仅可以缓解每次分析查询给集群带来的计算压力，Key-Value 存储也可以提升查询性能。通常地，聚合预计算结果相对于原始的明细数据在规模上会有明显的量级下降，这也是 OLAP 场景的一个特性。

Apache Kylin 作为固化分析场景引擎

 4.1 固化分析场景引擎选型

章节 3.3 分析了固化分析场景的实现方案，在技术选型时选择使用 Apache Kylin，主要因为 Kylin 提供了以下三个特性：

1. 针对明细数据进行聚合计算；
2. 将聚合结果存储到 HBase；
3. 针对明细和聚合结果支持标准的 SQL 访问；

上述三个特性不仅提供了完备的聚合缓存能力，同时也支持 SQL 访问，因此接入成本相对较低，另外，Kylin 也提供了成熟的优化查询的方法，如 HBase RowKey 序、字典编码等等，在一定程度上方便了使用优化。

 4.2 Apache Kylin 在场景引擎中承担的工作职责

Kylin 作为固化分析场景引擎，主要负责对有聚合缓存需求的表进行查询加速。什么样的表会有这样的需求呢？

1. 报表类产品使用的表；
2. 经 OLAP 引擎数据转移决策识别认为需要进行聚合缓存的表；

前者不难理解，后者则如引擎中的表，表数据规模较大，且被频繁执行某种聚合分析，在一段时间内达到一定的频次，引擎会识别并认为该表需要执行聚合缓存，进而触发调度将数据“复制”到 Kylin。这样，下次针对该表的聚合分析如果可被 Kylin 的聚合缓存覆盖，就会直接查询 Kylin 中的聚合数据“副本”而非原始的明细数据“副本”。

 4.3 使用 Apache Kylin 遇到的挑战

滴滴使用 Kylin 的方式与传统方式有异，Kylin 在架构设计上与业务紧耦合，传统方式中业务分析人员基于 Kylin 建模、构建立方体（Cube），然后执行分析查询。但滴滴将 Kylin 作为固化分析场景下的引擎使用，提供针对表的聚合缓存服务，这样作为一个通用数据组件的 Kylin 就剥离了业务属性，且与用户相割裂，对外透明。

在最初的使用中，由于没有控制 OLAP 引擎的内部并发，来自调度的聚合缓存任务会在某些情况下高并发地执行 Kylin 的表加载、模型和立方体的创建，因为 Kylin Project 元数据的更新机制导致操作存在失败的可能。当前，我们通过在 OLAP 引擎内部使用队列在一定程度上缓解了问题的发生，此外，结合重试机制，基本可以保证操作的成功完成。最后我们也注意到，该问题在最新的 Kylin 版本中已经进行了修复。

另外，Kylin 默认地，在删除立方体时不会卸载 HBase 中的 Segment 表，而需定期执行脚本进行清理。这样，就导致引擎运行时及时卸载无效的立方体无法级联到 HBase，给 HBase 造成了较大的运维压力。因此我们也对源码进行了调整，在立方体删除时增加了 HBase Segment 表清理的功能，等等。

 4.4 Apache Kylin 在场景引擎中的使用效果

图 4.4-1 为 Kylin 在滴滴 OLAP 引擎中的部署情况，Kylin 集群包含 2 台分布式构建节点、8 台查询节点，其中 2 台查询节点作为集群接口承接 REST 请求，REST 请求主要包含两类：构建作业状态查询和创建类操作，创建类操作如装载表、建模、创建立方体以及对等的删除操作等等。对于构建作业状态查询轮询请求两台节点，而对创建类操作则请求其中固定的一台节点，另一台作为 Standby 存在，这样设计的主要目的是避免集群接口的单点问题，同时解决因 Kylin 集群元数据同步机制导致的可能出现的创建类操作失败问题。

![img](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\640.webp)

图 4.4-1 Kylin 集群部署

目前，Kylin 集群维护了 700+ 的立方体，每日运行 2000+ 的构建作业，平均构建时长 37 分钟，立方体存储总量 30+TB（已去除 HDFS 副本影响）；对未使用缓存的查询进行统计，80% 的查询耗时小于 500ms，90% 的查询耗时小于 2.8 秒。需要说明的是，由于 OLAP 引擎中“数据转移决策”模块会根据查询场景触发数据“复制”到 Kylin 中来，在近期的统计中，立方体数量一度会达到 1100+。

在业务方面，Kylin 间接服务了下游数易（面向全公司的报表类数据产品）、开放平台（面向全公司的查询入口）等多个重要数据产品，覆盖了快车、专车等十多个业务线的分析使用，间接用户 3000+，Kylin 的引入为用户提供了稳定、可靠、高效的固化分析性能。

总结

滴滴 OLAP 引擎细分了灵活分析、固化分析、热点数据等几类场景， Kylin 作为固化分析场景引擎在滴滴 OLAP 引擎中承担了重要角色。引擎使用 Kylin 提供的：针对明细数据进行聚合计算，将聚合结果存储到 HBase，针对明细和聚合结果支持标准的 SQL 访问三个特性透明地服务用户。当前，引擎服务了下游多个重要数据产品，覆盖了十多个业务线，为用户提供了稳定、可靠、高效的数据分析性能。

Kylin 作为中国人主导的第一个 Apache 顶级开源项目，覆盖场景重要，社区活跃，资料丰富，未来我们将更加专注于 Kylin 在滴滴 OLAP 引擎应用场景下的稳定性以及查询、构建优化。

 作者介绍

郑秋野，滴滴数据平台高级专家工程师，滴滴数据系统团队负责人。

靳国卫，滴滴数据平台专家工程师，主要负责 OLAP 引擎建设。

张晓东，滴滴数据平台高级工程师，主要负责 OLAP 引擎元数据设计开发以及 Apache Kylin 集群运维。

 关于滴滴数据平台团队

滴滴基础平台部数据平台团队负责公司数据仓库建设、数据治理以及基础数据工具研发等，团队提供 OLAP 引擎、Adhoc 工具、数据地图、数据分析平台、建模平台等系列产品稳定、高效地服务用户的数据需求。