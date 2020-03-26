携程 Presto 技术演进之路

> **作者简介**：张巍，携程技术中心大数据资深研发工程师。2017 年加入携程，在大数据平台部门从事基础框架的研发和运维，目前主要负责 Presto，Kylin，StructedStreaming 等大数据组建的运维，优化，设计及调研工作。对资源调度，OLAP 引擎，存储引擎等大数据模块有浓厚的兴趣，对 hdfs，yarn，presto，kylin，carbondata 等大数据组建有相关优化和改造经验。

### 一、背景介绍

携程作为中国在线旅游的龙头，提供酒店，机票，度假等服务，这些服务的背后是基于各个部门每天对海量数据的分析。

随着业务的不断增长，用户体验的不断提升，每个部门对数据处理的响应时间要求越来越短，对各个部门报表的响应速度要求越来越快，对跨部门的数据交互和查询也越来越紧密，所以需要一个统一的快速查询引擎，且这个查询引擎需要从 GB 到 PB 以上的海量数据集中获取有价值的信息。

我们在技术选型上对比了 Presto，Spark，Impala 等 MPP 数据库。综合考量框架本身性能和社区活跃程度，最终选择了 Presto。

Presto 是 Facebook 开源的 MPP 数据库，先简单了解下架构图：

![enter image description here](https://i.loli.net/2020/03/26/6wUmFGTYO28S1De.png)

它是一个 Master-Slave 的架构，由下面三部分组成：

1）一个 Coordinator 节点

2）一个 Discovery Server 节点

3）多个 Worker 节点

Coordinator 负责解析 SQL 语句，生成执行计划，分发执行任务给 Worker 节点执行。

Discovery Server 通常内嵌于 Coordinator 节点中。

Worker 节点负责实际执行查询任务以及负责与 HDFS 交互读取数据。

Worker 节点启动后向 DiscoveryServer 服务注册，Coordinator 从 DiscoveryServer 获得可以正常工作的 Worker 节点。如果配置了 HiveConnector，需要配置一个 Hive MetaStore 服务为 Presto 提供 Hive 元信息。

### 二、携程 Presto 使用的困境

首先来看一下我们 2018 年前遇到的一些问题。

携程在 2014 年探索使用 Presto 去满足用户快速即席查询和报表系统的需求。在 2017 年末，离线团队接手携程 Presto 后，发现当时的 Presto 本身存在一系列问题，那个时候 Presto 的版本是 0.159，也相对较低。

#### 2.1 稳定性差

当时用户反馈最多的是，Presto 又 OOM 了。只能采取重启 Presto 恢复服务，实际上对于用户来说，系统挂掉是最恶劣的一种体验了。

Presto 严格的分区类型检查和表类型检查，导致大量用户在 Presto 上发起的查询以失败告终，对于那些使用老分区重新刷数据的用户简直就是灾难。

一些大数据量的查询经常占用着计算资源，有时运行了 2、3 个小时，累计生成上百万个 split，导致其他小的查询，响应速度受到严重影响。

#### 2.2 认证不规范

很早以前，携程在 Presto 中内部嵌入一个 Mysql 的驱动, 通过在 Mysql 表中存放用户账号和密码访问 Presto 的权限认证。实际上和大数据团队整体使用 Kerberos 的策略格格不入。

#### 2.3 性能浪费

所有的 join 查询默认都是使用 Broadcast join，用户必须指定 join 模式才能做到 Broadcast join 和 Map join 的切换。

数据传输过程中并没有做压缩，从而带来网络资源的极大浪费。

#### 2.4 没有监控

Presto 自身没有监控分析系统，只能通过 Presto 自身提供的短时监控页面看到最近几分钟的用户查询记录，对分析和追踪历史错误查询带来很大的不便。

无法知道用户的查询量和用户的查询习惯，从而无法反馈给上游用户有效的信息，以帮助应用层开发人员更合理的使用 Presto 引擎。

### 三、携程 Presto 引擎上所做的改进

为了提供稳定可靠的 Presto 服务，我们在性能，安全，资源管控，兼容性，监控方面都做了一些改动，以下列出一些主要的改进点。

#### 3.1 性能方面

- 根据 Hive statistic 信息，在执行查询之前分析 hive 扫描的数据，决定 join 查询是否采用 Broadcast join 还是 map join。
- Presto Page 在多节点网络传输中开启压缩，减少 Network IO 的损耗，提高分布计算的性能。
- 通过优化 Datanode 的存储方式，减少 presto 扫描 Datanode 时磁盘 IO 带来的性能影响。
- Presto 自身参数方面的优化。

#### 3.2 安全方面

- 启用 Presto Kerberos 模式，用户只能通过 https 安全协议访问 Presto。
- 实现 Hive Metastore Kerberos Impersonating 功能。
- 集成携程任务调度系统（宙斯）的授权规则。
- 实现 Presto 客户端 Kerberos cache 模式，简化 Kerberos 访问参数，同时减少和 KDC 交互。

#### 3.3 资源管控方面

- 控制分区表最大查询分区数量限制。
- 控制单个查询生成 split 数量上限, 防止计算资源被恶意消耗。
- 自动发现并杀死长时间运行的查询。

#### 3.4 兼容性方面

- 修复对 Avro 格式文件读取时丢失字段的情况。
- 兼容通过 Hive 创建 view，在 Presto 上可以对 Hive view 做查询。（考虑到 Presto 和 Hive 语法的兼容性，目前能支持一些简单的 view）。
- 去除 Presto 对于表字段类型和分区字段类型需要严格匹配的检测。
- 修复 Alter table drop column xxx 时出现 ConcurrentModification 问题。

### 四、携程 Presto 升级之路

升级之初 Presto 的使用场景如图。

![enter image description here](https://i.loli.net/2020/03/26/e8gCY1BsvA3yHdl.png)

#### 4.1 第一阶段，版本升级

对于版本选择，我们关心的几个问题：1）是否很好地解决各类内存泄漏的问题；2）对于查询的性能是否有一定提升。

综上考虑，决定使用 0.190 版本的 Presto 作为目标的升级版本。

通过这个版本的升级，结合对 Presto 的一部分改进，解决了几个主要问题：

- Presto 内存泄漏问题。
- Presto 读取 Avro 文件格式存在字段遗漏的问题。
- Presto 语法上无法支持整数类型相乘。

#### 4.2 第二阶段，权限和性能优化

在第二个版本中，我们主要解决了以下问题：

- Kerberos 替换 Mysql
- Join 模式的自动感知和切换
- 限流（拒绝返回 100 万以上数据量的查询）

**认证机制**

这里简单介绍下 Kerberos 权限替换过程中的一些细节。Presto 的认证流程：

![enter image description here](https://i.loli.net/2020/03/26/HDWPuQbXUnqdVRY.png)

Presto 涉及到认证和权限的部分如上面红色框标注的 3 个部分，Coordinator、HDFS 和 Hive Metastore 这三块。Coordinator 和 HDFS 这两块是比较完善的，重点讲一下 Hive Metastore。

在 Kerberos 模式下，所有 SQL 都是用 Presto 的启动账号访问 Hive Metastore，比如使用 Hive 账号启动 Presto，不论是 flt 账户还是 htl 账户提交 SQL，最终到 Hive Metastore 层面都是 Hive 账号，这样权限太大，存在安全风险。我们增加了 Presto Hive MetastoreImpresonating 机制，这样 htl 在访问 Hive Metastore 时使用的是通过 Hive 账号伪装的 htl 账户。

![enter image description here](https://i.loli.net/2020/03/26/y3xRcnuY7HU1ZV6.png)

新的问题又来了，在认证过程中需要获取 Hive 的 Token，可是 Token 反复的获取都需要一次 Metastore 的交互，这样会给 Metastore 带来压力。于是我们对 Token 做了一个缓存，在其 Token 有效期内缓存在 Presto 内存中。

#### 4.3 第三阶段，资源管控和监控平台

在第三个版本中，我们解决了以下问题：

- 拦截大量生成 split 的查询 SQL
- Presto 监控平台初步搭建
- 限制最大访问的分区数量

##### 4.3.1 数据采集

流程图

![enter image description here](https://i.loli.net/2020/03/26/zBAoZmdqpcJx7eF.png)

程序每一分钟从 Presto Coordinator 采集数据， 分发到多个监听器，同时写入 Mysql 表。

当前入库 5 张监控表。

Basic query：查询基本信息（状态，内存使用，总时间消耗，错误信息等）

Query stats：查询性能信息（每一步的时间消耗，数据输入输出量信息等）

Query info：查询客户端参数信息（发起客户的基本信息，参数信息等）

Query stage info：每个查询中所有 stage 的信息（输入输出量信息，内存使用情况，调用核的数量等）

Query task info：每个 stage 中所有 task 的信息（输入输出信息， 内存信息，调用核数等）

##### 4.3.2 实时健康状况报告

基于以上采集的数据，我们会实时生成 presto 集群的健康报表以及历史运行趋势。这些数据可以用于：

- 集群容量的评估
- 集群健康状态的检测

![enter image description here](https://i.loli.net/2020/03/26/dOeuUlnx6HKJ2YV.png)

![enter image description here](https://i.loli.net/2020/03/26/ux1UWMiRcVHzBN7.jpg)

![enter image description here](https://i.loli.net/2020/03/26/XONJb2Lt1TuaozG.jpg)

##### 4.3.3 问题追踪

除了健康报表之外，对于查询错误和性能问题，我们提供了详细的历史数据，运维人员可以通过报表反应出的异常状况做进一步的排查。

通过报表能够发现某个用户查询时出现了外部异常

![enter image description here](https://i.loli.net/2020/03/26/h4nWoAzNCvPHBwJ.png)

![enter image description here](https://i.loli.net/2020/03/26/syqKGxgUEw3OYBM.png)

![enter image description here](https://i.loli.net/2020/03/26/YRQdOyTvhlBgMpK.png)

通过查看异常堆栈，发现查询是由于 hive metastore 出现短暂重启引起的查询失败。

#### 4.3.4 其他

在 Presto 升级改进的同时，我们也调研了 Presto on Carbondata 的使用场景。

当时 Carbondata 使用的是 1.3.0 版本。在此基础上：

- 修复了一系列 Presto on carbon data 的功能和性能的问题
- 对比了 Presto on carbon data 和 Presto on hive，得出结论：Presto on Carbon 整体性能和 Presto on orc 的整体性能相当。同样的数据量 Carbon snappy 的存储是 ORC zlib 的六倍，就目前存储吃紧的情况下，不适合使用。
- 目前仅在线上提供 Carbondata 连接器，暂未投入业务使用。

当前 Presto 的架构为：

![enter image description here](https://i.loli.net/2020/03/26/BEDukVaiw4brj23.png)

### 五、携程 Presto 未来升级方向

#### 5.1 架构完善和技术改进

启用 Presto 资源队列，规划通过 AppName 划分每个资源队列的最大查询并发数，避免某个应用大量的查询并发同时被 Presto 执行，从而影响其他的 App。

实时告警平台，对于错误的查询，Presto 能够实时的发送异常查询到告警平台，帮助运维人员快速响应和发现错误以便及时处理。

统一的查询引擎，统一的查询引擎可以在 presto，kylin，hive spark-sql 之间匹配最优的查询引擎，做语法转换后路由过去。

#### 5.2 业务方向

未来携程内部 OLAP 报表系统（Art Nova）会更大范围的采用携程 Presto 作为用户自定义报表的底层查询引擎，以用来提高报表的响应速度和用户体验。

部分业务部门的 vdp 也计划调研，实时数据计算采用 Presto 作为其默认的查询引擎的可能性。

下个阶段我们期望的 Presto 整体架构图：

![enter image description here](https://i.loli.net/2020/03/26/d1MWrzRKJNBAUVC.png)

### 六、结束语

随着 Presto 社区的蓬勃发展，最新版本为 0.203，其中包含了大量的优化和 Bug Fix，希望跟大家一起讨论。