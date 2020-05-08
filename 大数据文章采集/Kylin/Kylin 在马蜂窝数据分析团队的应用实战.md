# Kylin 在马蜂窝数据分析团队的应用实战

**AI 前线导读：**马蜂窝大数据平台自 2017 年下半年引入 Apache Kylin 以来，极大的提升了数据分析师对于数据探索的效率。因为使用了 Apache Kylin，数据分析师可以直接查询大数据、无需排队、亚秒级响应，整体开发效率提高了 10 倍以上。

## 为什么 Apache Kylin 是分析师的标配技能

说到 Apache Kylin（以下简称 Kylin），对于做大数据开发，尤其是数据仓库开发的同学，即使没用过，至少一定或多或少听过，但对于数据分析师而言，可能不一定十分熟悉，在马蜂窝，利用 Kylin，自己动手搭建所负责业务的数据仓库，已经成为数据分析师日常工作的一部分，是分析师的标配技能。

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\6eeec0568be84dd844831edeeeceb4ea.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/7681-1534669935184.png)

传统的根据数据流进行分层的数据团队组织架构中，数据分析团队大多是作为数据平台的使用者，通过各种数据后台，提取数据进行分析工作，这更多是沿用了大数据技术兴起前的组织架构。

从业务角度看：随着业务复杂性及业务发展速度越来越快，尤其是马蜂窝的业务从最初的社区、到攻略、再到近两年逐渐发力的酒店和电商平台等商业化业务线，涉及用户旅行的行前、行中、行后的所有环节，做整个旅游行业的闭环。马蜂窝内部更像是一个集团公司，各个团队间的业务情况、数据需求及发展阶段有很大不同，结合自身业务的复杂性，传统的按部就班，层层堆叠的组织结构和做事方式，已经不足以适应当下的业务发展要求。

从数据分析角度来看，由于组织结构的分层，也往往容易出现踢皮球，数据项目周期拉长，甚至因层层传递导致的理解偏差，所带来的潜在问题等。

从技术角度看：随着 Kylin 等相关大数据技术的日趋成熟，各公司数据架构大同小异，重心已从从基本架构和功能实现，逐渐变为如何充分吸收各种大数据相关技术，如何充分发挥技术与数据的价值。

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\6a6b89ef4bbca50cd327d3c495a909fb.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/6092-1534669935423.png)![Kylin在马蜂窝数据分析团队的应用实战](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

通过数据平台产品化，赋能给数据分析师为代表的非数据开发小伙伴，共同进行数据平台建设。

作为研发为主的数据平台团队，由封闭的数据流的开发者，转为开放的数据平台产品的设计与实现者，充分将数据流各环节产品化，将环节中的的数据与技术能力通过数据产品开放出来，允许分析师等数据使用者加入进来，开放共建平台。

作为对接数据与业务的主力军，数据分析师从一个最上层的数据使用者，转变为数据全生命周期的管理者和建设者，能够对数据做到端到端的把控，一头控制数据源头，一头控制数据需求，中间通过数据平台各个产品自助完成数据流，职责覆盖数据埋点定义、清洗规则设立，数据仓库设计与实现，离线分析、看板配置，API 输出、推动数据项目落地等。

Kylin 作为一个成熟的 OLAP 引擎，被引入到数据平台的产品建设中，通过简单的二次开发与集成，使得数据仓库的建设能力得以开放出来，大大降低了数据仓库的建设门槛，使得数据分析师能够很好的在其上进行数据仓库的定义与构建，带来了秒级的查询响应速度，大大提升了分析师日常数据查询效率，及相关数据在业务场景中的落地。

## Kylin 在马蜂窝分析师团队的日常

以马蜂窝客服服务质量统计需求为例，业务部门希望能够从每天即兴跑 SQL 这种临时查询的方式，升级为日常可使用的后台。后台中主要统计指标为：从店铺维度，管家维度，商品维度和目的地维度分别统计销售业绩和服务质量。销售业绩和服务质量具体需求如下图：

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\6c077ccacc77b4a8e79ecba5249de64d.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/16image003-1534669474298.png)

![Kylin在马蜂窝数据分析团队的应用实战](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\67d31e039e728bccdea277e2adc8b474.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/15image005-1534669472593.png)

在 Kylin 没有成为数据分析师标配之前，这样的一个后台需要前后端开发 / 数据分析师 / 数据库工程师和数据开发工程师协作，大约需耗时 1 个月，还容易因为开发对业务不理解容易导致指标统计出问题。

现在在马蜂窝，只需要分析师和需求方理清需求，确定好统计主题，各主题下分别统计哪些维度，各个维度下又有哪些度量，以及度量的统计粒度和统计口径，就可以依托数据产品，独立完成统计后台搭建，耗时约一周，整体开发效率提升 10 倍以上，具体工作流程如下图。

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\f030200ea450fbe2f050df0def702f86.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/4983-1534669934851.png)![Kylin在马蜂窝数据分析团队的应用实战](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

数据分析师在理清需求后，从三个方面对基础数据进行有效性验证：  

- 一是依据日志设计文档逐一校对日志是否按照指定的要求与逻辑上报；
- 二是抽样对基础数据做描述性统计，包括各个字段的均值 / 中位数 / 四分位数 / 极值 / 缺省值个数进行统计，做到在统计层面上对数据心中有数；
- 三是对关键节点数据和开发测试一起，进行逻辑校对，制作数据质量看板，并针对关键数据，由测试团队编写自动化测试用例进行数据校验，双重保证数据质量，尽可能做到数据在产生的时候不出问题，出了问题，也能在第一时间感知并修复。

分析师对数据的有效性验证完后，采用 HIVE 视图的方式，依据统计逻辑对数据进行清洗和整理。在本例中，需要将 IM 会话信息，产品基础信息，客服服务时间信息，产品订单信息分别按照对应的主题，创建事实表和维度表。有了事实表和维度表后，可以据此快速用 Kylin 搭建 CUBE。

在使用 Kylin 的时候有三个容易出现的问题：  

- 一是分析师对数据仓库的星型模型，雪花模型，或者星系模型等理解不够，很容易抽离不出来维度表，做出来的是一张业务大宽表，据此创建的 CUBE 膨胀率较高，只是利用了 Kylin 的预计算能力，快速出统计结果；
- 二是分析师很难平衡计算时间和存储空间的关系，往往会根据业务方的需求，过分强调响应速度，把一些不太容易用到的维度也放在 CUBE 里，导致 CUBE 数量多，膨胀率高，占用大量计算和存储资源；
- 三是分析师对 Kylin 底层算法不理解，优化 CUBE 能力有限。目前采用的方案是，分析师创建 CUBE 后由专人负责审核和优化。

分析师创建完 CUBE 后，在 MDW（注：马蜂窝统计指标库）中用 SQL 创建指标并定义维度。MDW 不存储由 CUBE 作为数据源的数据，只是保留了计算指标的 SQL（计算逻辑），并在需要的时候去 Kylin 中实时获取数据。

MDW 有标准的 API 接口，可以无缝接入马蜂窝的数据看板系统。分析师利用数据看板系统，将 MDW 指标按照主题 / 维度等组合成特定的看板组，并配置好看板和看板相互之间的跳转关系，看板内部依托 MDW 内对同一指标不同维度的管理实现下钻和上卷。此时，马蜂窝客服服务质量统计后台就基本搭建完成（如下图）。

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\80cd7e96b2e272e9738cf842ab7bf4ff.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/4image008-1534669473403.png)![Kylin在马蜂窝数据分析团队的应用实战](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

## Kylin 集成到数据平台的经验

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\4414271e3a2959e8854dc9296caaa651.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/6image010-1534669472882.png)

如上图所示, 数据分析师日常工作中大部分的即兴查询是基于 MQL(我们的 OLAP 平台) 来探索数据，MQL 内置支持 Presto,Hive,Kylin,Phoenix 等不同 SQL 引擎。

随着业务发展，数据分析师提交的 Presto SQL 任务，往往受限于 Presto 集群的并发性及一些大资源开销的 SQL 影响，我们通过引入分集群队列的排队调度，尽可能保证分析师的 SQL 执行成功率，但却增加了等待的时间。

我们每天有 160+ 个用户在使用 MQL, 每天约 2k+ 次 SQL 查询。在上图的任务状态中，我们可以看到随着 Presto SQL 的提交，分析师们的数据探索时间出现线性式的增长，他们需要等待半小时，甚至一小时的时间才能执行他们的 SQL 任务。

马蜂窝大数据平台自 2017 年下半年引入 Kylin 以来，其亚秒级的响应速度，极大的提升了数据分析师对于数据的探索的效率。现在数据分析师在 MQL 进行提交 Kylin SQL 后，无需排队，亚秒级响应，相比于之前的 Presto SQL 任务，Kylin 给分析师们减少了几十倍，甚至几百倍的等待时间，给数据分析师的工作带来了很大的效率。

当前在马蜂窝 Kylin 平台，我们有 80+ 个 cube 在 Kylin 上运转，90% 的 cube 在 5s 内响应，每天约 5w+ 次 Kylin 调用。我们的 cube 已经覆盖了马蜂窝所有的业务线，如电商，酒店，搜索，推送，用户增长等等业务线。

下面将分成 3 个部分来讲下 Kylin 在马蜂窝的实战。

### 1. Kylin 在马蜂窝数据平台的应用

![Kylin在马蜂窝数据分析团队的应用实战](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\fc842fb293a202d6fc98405a85e8b947.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/4image012-1534669473149.png)

如上图所示，我们规范了数据分析师数据探索的统一入口。MQL 作为马蜂窝的 OLAP 平台，所有数据分析师通过 MQL 提交 SQL 之后，系统根据分析师所选的引擎来将 SQL 分发到不同的 Presto 平台和 Kylin 平台。

除此之外, 系统根据 SQL 解析器去获取分析师 SQL 里的字段列和条件列, 以便后续我们能有针对性的对 Kylin cube 进行优化。MQL-T-API 是对外输出的数据获取 API，分析师通过 MQL Template 进行模板创建后，将模板链接分享给其他工程师，工程师通过程序调用能在极短的时间内，无缝的接入到各个数据报表后台及业务系统（例如定向推送）当中。

在图中所示，我们有个探针模块，利用探针来去跟踪并评估分析师所提交的 SQL，按照规范，我们会生成探针模块报表，输送给分析师，来让他们把 Presto SQL 渐渐的转化成 Kylin Cube。这样一来，通过探针模块，我们也能不断去发现并挖掘 Kylin Cube 的增长点, 同时提升分析师的工作效率。

### 2. Kylin 在马蜂窝数据平台调度流程

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\53bca3de09bf51e937ed0673d255ef3e.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/4image014-1534669473754.png)

如上图所示，在数据仓库中，我们按不同主题建立分层，每一层都会有不同的数据表，不同数据表中会有依赖关系。在马蜂窝数据平台，引入 AirFlow 完成基于 DAG 的数据血缘依赖的调度系统。

在 Kylin 平台，确保 cube 的就绪时间，正确的 build 数据并触发下游指标平台的计算，是非常关键的一步。

由于 Kylin 数据源都来自 Hive 仓库，而表既有物理表和视图，我们会按照 Kylin Project 分项目下去加载各自的数据表，通过解析器去获取表视图所依赖的物理表，然后在 AirFlow 调度平台去检测所有物理表或视图的状态，都准备就绪后，才会触发 Kylin Cube 的构建。

在整个数据血缘平台中，我们也需要去监控各个表的正常状态，通过系统从而能自动的二次构建 Kylin Cube，以确保数据正确统计与使用。

### 3. Kylin 在马蜂窝数据平台上线的标准流程

[![Kylin在马蜂窝数据分析团队的应用实战](D:\superz\BigData-A-Question\大数据文章采集\Kylin\images\ea461759dcfe85a383924efce5f96ffe.png)](https://s3.amazonaws.com/infoq.content.live.0/articles/kylin-in-mafengwo-data-analysis/zh/resources/2image016-1534669471905.png)

如上图所示，这是新 cube 在上线之前的一个标准流程。随着马蜂窝业务的不断发展，我们数据分析师团队的阵容越来越大。需要制定一套标准流程，来确保线上的 cube 足够优秀和健壮。当前我们拆分了两套集群，一个测试集群和生产集群。

数据分析师在测试集群上按照需求进行 cube 设计之后，我们会对新的 cube 按照标准 cube 设计法则进行评审，在判断 cube 足够好之后，我们只会迁移 mode 和 cube 元数据到生产集群，并加入调度系统每天正常 build。在生产集群，我们也会对每一个 cube 查询进行二次观察，按照其查询条件规则来对 cube 进行二次优化。

另外, 我们也不断的汲取行业经验，并按照自己实战的经验，来规范并制定一套 cube 设计的基本法则，并把法则输送给每一个数据分析师，使其成为一个标准的 cube 管理者。

## 结语

Kylin 在马蜂窝的正式使用不到一年的时间，而作为数据分析师的标配技能，也是在使用过程中逐步积累总结经验形成的，期间需要数据分析团队和平台研发团队大量的沟通协作，优化平台产品与工作流程，让更多的数据分析师能够驾驭 Kylin 这个强大的工具，服务好更多的业务场景，提升马蜂窝各个岗位的小伙伴数据使用的效率。

我们会继续关注 Kylin 社区的发展，也希望更多的人能了解并参与进来，早点驾驭和感受这只强大神兽的力量。

### 作者简介

韩鑫：马蜂窝大数据团队负责人，从理论物理跨界到技术研发，现在当半个产品经理

邵黎明：马蜂窝数据分析团队负责人，从车间主任到情报分析师，人生不设限

汪木铃：马蜂窝大数据平台研发技术负责人，从应用开发到底层源码，现在痴迷于各种大数据组件

### 马蜂窝简介

马蜂窝从中国最大的中文在线旅行社区出发，为旅行者提供从攻略到预订的一站式解决方案。以攻略为核心，通过对海量 UGC 信息的大数据应用，马蜂窝提供覆盖全球 6 万个目的地的交通、酒店、景点、餐饮、购物、当地玩乐等全方位旅游资讯及产品预订服务，是中国领先的自由行服务平台。

### 马蜂窝数据团队简介

由于马蜂窝的业务涉及旅行的所有环节，各业务对数据的需求差异巨大，平台研发团队吸收引进各种最新的大数据技术，将内部的各种数据处理需求抽象，形成平台化的通用数据产品，将大数据能力充分赋予每一个分析师，降低重复性的工作，端到端掌控数据全生命周期，真正成为数据的掌控者，从传统的表哥表姐，成长为各业务的数据合伙人。