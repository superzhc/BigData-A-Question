# Apache Kylin 在 4399 大数据平台的应用

## 背 景

在开始案例分享前，先简单介绍一下 4399 以及 4399 的大数据团队

- 4399 是中国最早的和领先的在线休闲小游戏平台，日活跃达 2000 多万
- 4399 游戏盒是 4399 旗下的手游分发平台，日活过 350w
- 4399 的大数据团队规模在 15 人左右，主要工作内容为游戏推荐、游戏搜索、竞价广告，多维分析、大数据平台等等

4399 从 Kylin v1.5 版本开始使用，使用版本也随着官方版本的升级在升级，现生产系统两个版本同时在运行：Kylin v2.0.0、Kylin v2.3.0，共有 20 个 Cube 为我们的大数据平台提供分析服务，如漏斗模型分析等。其中最大的 Cube，每到周末需要构建 2.5 亿条的数据，18 个维度，9 个指标，构建耗时 80 分钟左右。Kylin 的引入使用主要帮我们解决了三大问题：

- 提供 ANSI-SQL 接口，让统计分析由繁杂变得简单。
- 解决口径不一致问题。原先每个需求过来，需要重写统计逻辑，编写人员不同就会导致口径不一致，数据出入较大，校准工作量大。现在统一整理一张事实表，相关需求通过 SQL 查询同一张表，口径一致，校准简易。
- 增加维度或者指标时，大大降低了所需工作量。

解决这三大问题的同时，Kylin 还保证了快速的响应时间。Kylin 维度组合设计的合理性特点，不仅能够减少 Cube 构建时间，还能让我们获得合理的查询响应时间。现生产最大的事实表，包含 18 个维度、9 个指标，95% 的 SQL 能在 3 秒以内返回正确结果。

## 4399 大数据平台介绍

随着业务的增加，4399 的数据规模呈爆炸式增长，想要完整的收集数据，并从数据中挖掘出商业价值，大数据平台的引入势在必行。我司在前几年开始引入大数据平台，使用流行的开源的组件，搭建出符合公司业务需求的平台。为了保证数据落盘幂等操作、写入消费的 Exactly-One，我们相应地开发一些小工具。

[![Apache Kylin在4399大数据平台的应用](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\7c21d4658e6378177c9122b0d58153a6.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apache-kylin-at-4399-big-data-platform/zh/resources/6581-1529509616889.png)![Apache Kylin在4399大数据平台的应用](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

发展到现在，公司大数据平台已经拥有 50 多个节点，主要职责包含三大块：

- 收集原始日志，每天新增日质量在 5T 左右
- OLAP - 对日志做多维数据分析，这部分使用的是 Kylin
- 用户画像，机器学习。发掘用户价值

## Apache Kylin 上线应用

在 4399 的大数据平台中，Hadoop 为我们提供了数据管理功能，但是现有的业务分析工具（如 Tableau、Microstrategy 等）存在很大的局限性，例如难以水平扩展、无法处理超大规模数据、缺少对 Hadoop 的支持等。数据仓库 Hive 虽然也提供了 SQL 查询接口，但是响应时间差强人意。在这样的背景下，Kylin 能够在亚秒级查询巨大的 Hive 表，并支持高并发, 可以说是应运而生了。

### a) Kylin 平台架构

如图 2-1 Kylin 在 4399 应用的技术架构图，主要包含查询和构建服务器。

[![Apache Kylin在4399大数据平台的应用](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\0db64a2e80a6df8b6e5c80941aedde5e.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apache-kylin-at-4399-big-data-platform/zh/resources/5082-1529509618200.png)![Apache Kylin在4399大数据平台的应用](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

图表 2-1 Kylin 平台架构图

**i. 部署情况**

为了保证查询服务的稳定性，我们使用 Nginx 配置负载均衡。

- 生产环境：三台查询、一台构建；HBase 集群包含 23 节点。
- 测试环境：一台查询、一台构建和查询。

多版本同时部署，需要修改配置，使得 Zookeeper 的 znode 路径以及 kylin_metadata 分开，避免相互影响。

**ii. 数据流向**

以我们应用平台 4399 游戏盒的漏斗模型分析（从展示到点击下载启动留存）为例，分析数据流向。

[![Apache Kylin在4399大数据平台的应用](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\c70f45ff0421502e198b3af4570a5da8.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apache-kylin-at-4399-big-data-platform/zh/resources/4073-1529509618797.png)![Apache Kylin在4399大数据平台的应用](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

图表 2-2 漏斗模型数据流向图

如图表 2-2，首先要整理好事实表和维度表，构成星型模型（或雪花模型），分析所需的纬度和指标，配置 Kylin 模型，接着配置相应的 cube。经过 Kylin 的构建，就能使用 SQL 语句查询 Kylin。

### b) 展示页面

为了方便运营人员的查看数据，体现出 Apache Kylin 的多维分析引擎优势，自己开发了一套多维分析展示页面。（下列图表使用了模拟数据。）

如图表 2-3，这是下载完成的分析模型。上面是维度相关的条件筛选和分组展开。左下角是行为路径统计树状图，各路径下载总量和占比一目了然，右下角指标的走势图和指标维度下钻列表。所有的维度都支持条件筛选和分组展开，各维度的指标对比，使得分析能更直观的感受数据趋势的内在原因。

![Apache Kylin在4399大数据平台的应用](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)[![Apache Kylin在4399大数据平台的应用](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\f6c633b713bde07e0f65857e52c86489.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apache-kylin-at-4399-big-data-platform/zh/resources/3444-1529509617382.png)

图表 2-3 多维分析界面

按照游戏维度分组展开的效果如图表 2-4。

![Apache Kylin在4399大数据平台的应用](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)[![Apache Kylin在4399大数据平台的应用](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\cb2fea3095b3a5efee0c840ee7ded5fc.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/apache-kylin-at-4399-big-data-platform/zh/resources/2865-1529509617854.png)

图表 2-4 游戏维度分组展开

### c) Apache Kylin 优化建议

众所周知，Kylin 的核心思想是预计算，即对多维分析可能用到的度量值进行预计算，将计算好的结果保存成 Cube，提供查询。所以涉及到两个方面的性能问题：

- 查询时的响应时间。
- 预计算花费的时间和空间。

**i. 查询时间优化**

Kylin 的查询过程主要包含四个步骤：解析 SQL，从 HBase 获取数据，二次聚合运算，返回结果。显然优化的重点就落在如何加快 HBase 获取数据的速度和减少二次聚合预算。

- 提高 HBase 响应时间：修改配置，修改 Cache 的策略，增加 Block Cache 的容量
- 减少二次聚合运算：合理设计纬度，使查询时尽量能精确命中 Cuboid。去重值使用有损算法。

### d) 预计算优化

预计算的优化，主要考虑有何缩短构建花费的时间，以及中间结果和最终结果占用的空间。每个业务单独一个 Cube，避免每个 Cube 大而全，减少不必要的计算。

**i.Cube 优化**

随着维度数目的增加，Cuboid 的数量成指数级增长。为了缓解 Cube 的构建压力，Kylin 提供了 Cube 的高级设置。这些高级设置包括聚合组（Aggregation Group）、联合维度（Joint Dimension）、层级维度（Hierarchy Dimension）和必要维度（Mandatory Dimension）等。

合理调整纬度配置，对需构建的 Cuboid 进行剪枝，刷选出真正需要的 Cuboid，优化构建性能，降低构建时间，大大提高了集群资源的利用效率。优化前后的效果对比如表 2-5：

![Apache Kylin在4399大数据平台的应用](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)![Apache Kylin在4399大数据平台的应用](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\c3cb230687ba46e0ae2ee120d53e942d.png)

图表 2-5 优化后效果对比

\1. 必须维度

查询时，经常使用的维度，以及低基数纬度。如该维度基数 <10，可以考虑作为必须维度。

\2. 层级维度

维度关系有一定层级性、基数有小到大情况可以使用层级维度。

3.Joint 维度

维度之间是同时出现的关系，及查询时，绝大部分情况都是同时出现的。可以使用 joint 维。

\4. 维度组合组

将为维度进行分组，查询时组与组之间的维度不会同时出现。

**ii. 配置优化**

配置优化，包括 Kylin 资源的配置以及 Hadoop 集群配置相关修改。

\1. 构建资源

每个 Cube 构建时，所需的资源不太一样，需要进行相应的资源调整。

\2. 调整副本

集群默认的文件副本数为 3，Cube 构建时，将副本数调为 2，个别中间任务还可以调整为 1，这样可以降低构建任务时集群 IO。为了保证查询的稳定性，HBase 副本数依然为 3。

\3. 启用压缩

Hadoop 集群启用 Snappy 压缩，HBase 也启用 Snappy，最终生成的 HFILE，最大压缩率可达 70% 左右，大大降低了集群 IO 负载。

## 后记

在 4399 大数据平台上现在还存在几个问题：HBase 集群不够稳定，查询响应时间不够稳定，个别语句响应时间不理想，Cube segment 重新构建原本 HBase 表不会自动删除。围绕这几个问题，我们后续会再进行一些优化。

- HBase 集群独立出来，避免被集群其他任务所影响，调整配置优化查询，增加查询的 cache 内存比例。
- 个别准实时构建任务结果调整为 HBase 的内存表，减少响应时间，能大幅度提高响应时间的稳定性。
- 修改源码，重新构建结束后自动清理过期 HBase 表，降低 HBase 索引的压力。

## 作者介绍

**林兴财**，毕业于厦门大学计算机科学与技术专业。有多年的嵌入式开发、系统运维经验，现就职于四三九九网络股份有限公司，担任大数据开发工程师，主要负责大数据平台的规划建设。