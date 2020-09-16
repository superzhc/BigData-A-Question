## `hbase-site.xml` 和 `hbase-default.xml` 

在Hadoop中将特定于站点的HDFS配置添加到hdfs-site.xml文件，那么对于HBase，特定于站点的配置文件为 `conf/hbase-site.xml`。有关可配置属性的列表，请参见下面的HBase默认配置或查看src/main/resources的HBase源代码中的原始hbase-default.xml源文件。

并不是所有的配置选项都会将其发送到hbase-default.xml。一些配置只会出现在源代码中；因此识别这些更改的唯一方法是通过代码审查。

目前，这里的更改将需要为HBase重启集群来注意到这个变化。

## `hbase-default.xml` 默认配置

```
hbase.tmp.dir
```

这是本地文件系统上的临时目录。将此设置更改为指向比“/tmp”更持久的位置，这是 java.io.tmpdir 的常见解决方案，因为在重新启动计算机时清除了“/tmp”目录。

默认: `${java.io.tmpdir}/hbase-${user.name}`

```
hbase.rootdir
```

这个目录是 region servers 共享的目录，HBase 保持不变。该 URL 应该是“完全限定的”以包括文件系统的 scheme。例如，要指定 HDFS 实例的"/hbase"目录，namenode 运行在 namenode.example.org 的 9000 端口，请将此值设置为：hdfs：//namenode.example.org：9000 / hbase。默认情况下，我们会写$ {hbase.tmp.dir}，通常是/tmp - 所以改变这个配置，否则所有的数据在计算机重启时都会丢失。

默认: `${hbase.tmp.dir}/hbase`

```
hbase.cluster.distributed
```

群集所处的模式。对于独立模式，可能的值为 false，对于分布式模式，可能的值为 true。如果为 false，启动将在一个 JVM 中一起运行所有 HBase 和 ZooKeeper 守护程序。

默认: `false`

```
hbase.zookeeper.quorum
```

使用逗号分隔的 ZooKeeper 集合中的服务器列表（这个配置应该被命名为 hbase.zookeeper.ensemble）。例如，“host1.mydomain.com，host2.mydomain.com，host3.mydomain.com”。默认情况下，对于本地和伪分布式操作模式，将其设置为 localhost。对于完全分布式安装，应将其设置为 ZooKeeper 集成服务器的完整列表。如果在 hbase-env.sh 中设置 HBASE_MANAGES_ZK，这是 hbase 将作为群集启动/停止的一部分来启动/停止 ZooKeeper 的服务器列表。客户端，我们将把这个集合成员的列表，并把它与 hbase.zookeeper.property.clientPort 配置放在一起。并将其作为 connectString 参数传递给 zookeeper 构造函数。

默认: `localhost`

```
zookeeper.recovery.retry.maxsleeptime
```

在重试 zookeeper 操作之前的最大睡眠时间（以毫秒为单位），这里需要最大时间，以便睡眠时间不会无限增长。

默认: `60000`

```
hbase.local.dir
```

将本地文件系统上的目录用作本地存储。

默认:`${hbase.tmp.dir}/local/`

```
hbase.master.port
```

HBase Master 应该绑定的端口。

默认: `16000`

```
hbase.master.info.port
```

HBase Master Web UI 的端口。如果您不想运行 UI 实例，请将其设置为-1。

默认: `16010`

```
hbase.master.info.bindAddress
```

HBase Master Web UI 的绑定地址

默认: `0.0.0.0`

```
hbase.master.logcleaner.plugins
```

由 LogsCleaner 服务调用的 BaseLogCleanerDelegate 的逗号分隔列表。这些 WAL 清理是按顺序调用的。要实现您自己的 BaseLogCleanerDelegate，只需将其放入 HBase 的类路径中，并在此添加完全限定的类名。始终在列表中添加上面的默认日志清理工具。

默认: `org.apache.hadoop.hbase.master.cleaner.TimeToLiveLogCleaner,org.apache.hadoop.hbase.master.cleaner.TimeToLiveProcedureWALCleaner`

```
hbase.master.logcleaner.ttl
```

WAL 在归档（{hbase.rootdir} / oldWALs）目录中保留多久，之后将由主线程清除。该值以毫秒为单位。

默认: `600000`

```
hbase.master.procedurewalcleaner.ttl
```

过程 WAL 将在归档目录中保留多久，之后将由主线程清除。该值以毫秒为单位。

默认: `604800000`

```
hbase.master.hfilecleaner.plugins
```

由 HFileCleaner 服务调用的 BaseHFileCleanerDelegate 的逗号分隔列表。这些 HFile 清理器按顺序调用。要实现您自己的 BaseHFileCleanerDelegate，只需将其放入 HBase 的类路径中，并在此添加完全限定的类名。总是在列表中添加上面的默认日志清除程序，因为它们将被覆盖在 hbase-site.xml 中。

默认: `org.apache.hadoop.hbase.master.cleaner.TimeToLiveHFileCleaner`

```
hbase.master.infoserver.redirect
```

Master 是否监听 Master Web UI 端口（hbase.master.info.port）并将请求重定向到由 Master 和 RegionServer 共享的 Web UI 服务器。配置，当主服务区域（而不是默认）时是有意义的。

默认: `true`

```
hbase.master.fileSplitTimeout
```

分割一个区域，在放弃尝试之前等待文件分割步骤需要多长时间。默认值：600000。这个设置在 hbase-1.x 中被称为 hbase.regionserver.fileSplitTimeout。Split 现在运行主端，因此重命名（如果找到'hbase.master.fileSplitTimeout'设置，将使用它来填充当前'hbase.master.fileSplitTimeout'配置。

默认: `600000`

```
hbase.regionserver.port
```

HBase RegionServer 绑定的端口。

默认: `16020`

```
hbase.regionserver.info.port
```

HBase RegionServer Web UI 的端口如果您不希望 RegionServer UI 运行，请将其设置为-1。

默认: `16030`

```
hbase.regionserver.info.bindAddress
```

HBase RegionServer Web UI 的地址

默认: `0.0.0.0`

```
hbase.regionserver.info.port.auto
```

Master 或 RegionServer UI 是否应搜索要绑定的端口。如果 hbase.regionserver.info.port 已被使用，则启用自动端口搜索。用于测试，默认关闭。

默认: `false`

```
hbase.regionserver.handler.count
```

在 RegionServers 上启动 RPC Listener 实例的计数。Master 使用相同的属性来处理主处理程序的数量。太多的处理者可能会适得其反。使其成为 CPU 数量的倍数。如果主要是只读的，处理程序计数接近 CPU 计数做得很好。从 CPU 数量的两倍开始，并从那里调整。

默认: `30`

```
hbase.ipc.server.callqueue.handler.factor
```

确定呼叫队列数量的因素。值为 0 表示在所有处理程序之间共享单个队列。值为 1 意味着每个处理程序都有自己的队列。

默认: `0.1`

```
hbase.ipc.server.callqueue.read.ratio
```

将调用队列分成读写队列。指定的时间间隔（应该在 0.0 到 1.0 之间）将乘以调用队列的数量。值为 0 表示不分割调用队列，这意味着读取和写入请求将被推送到相同的一组队列中。低于 0.5 的值意味着将比写入队列更少的读取队列。值为 0.5 意味着将有相同数量的读写队列。大于 0.5 的值意味着将有更多的读队列而不是写入队列。值为 1.0 意味着除了一个之外的所有队列都用于发送读取请求。示例：假设调用队列的总数为 10，则 read.ratio 为 0 意味着：10 个队列将同时包含读/写请求。0.3 的读取比例意味着：3 个队列将只包含读取请求，7 个队列将只包含写入请求。0.5 的 read.ratio 表示：5 个队列将只包含读取请求，5 个队列将只包含写入请求。0.8 的 read.ratio 意味着：8 个队列将只包含读取请求，2 个队列将只包含写入请求。1 的 read.ratio 表示：9 个队列将只包含读取请求，1 个队列将只包含写入请求。

默认: `0`

```
hbase.ipc.server.callqueue.scan.ratio
```

考虑到读取的调用队列的数量（根据调用队列的总数乘以 callqueue.read.ratio 计算），scan.ratio 属性将把读取的调用队列拆分为小读取和长读取队列。低于 0.5 的值意味着长读队列比短读队列少。值为 0.5 意味着将有相同数量的短读取和长读取队列。大于 0.5 的值意味着将会有比长读取队列更多的长读取队列。值 0 或 1 表示使用同一组队列进行获取和扫描。示例：给定读取调用队列的总数为 8，scan.ratio 为 0 或 1 意味着：8 个队列将包含长读请求和短读请求。0.3 的 scan.ratio 表示：2 个队列只包含长读请求，6 个队列只包含短读请求。0.5 的 scan.ratio 表示：4 个队列只包含长读请求，4 个队列只包含短读请求。0.8 的 scan.ratio 意味着：6 个队列只包含长读请求，2 个队列只包含短读请求。

默认: `0`

```
hbase.regionserver.msginterval
```

从 RegionServer 到 Master 的消息间隔（以毫秒为单位）。

默认: `3000`

```
hbase.regionserver.logroll.period
```

无论有多少次编辑，我们将滚动提交日志的时间段。

默认: `3600000`

```
hbase.regionserver.logroll.errors.tolerated
```

在触发服务器中止之前，我们将允许连续的 WAL 关闭错误的数量。如果在日志滚动过程中关闭当前 WAL 书写器失败，则设置为 0 将导致区域服务器中止。即使是一个很小的值（2 或 3）也会让区域服务器承担瞬间的 HDFS 错误。

默认: `2`

```
hbase.regionserver.hlog.reader.impl
```

WAL 文件读取器的实现。

默认: `org.apache.hadoop.hbase.regionserver.wal.ProtobufLogReader`

```
hbase.regionserver.hlog.writer.impl
```

WAL 文件编写器的实现。

默认: `org.apache.hadoop.hbase.regionserver.wal.ProtobufLogWriter`

```
hbase.regionserver.global.memstore.size
```

在新更新被阻止并刷新之前，区域服务器中所有存储区的最大大小。默认为堆的 40％（0.4）。更新被阻止，强制刷新直到区域服务器中的所有内存大小都达到 hbase.regionserver.global.memstore.size.lower.limit。此配置中的默认值已被故意留空，以便兑现旧的 hbase.regionserver.global.memstore.upperLimit 属性（如果存在）。

默认: none

```
hbase.regionserver.global.memstore.size.lower.limit
```

强制刷新之前，区域服务器中所有存储区的最大大小。默认为 hbase.regionserver.global.memstore.size（0.95）的 95％。当由于内存限制而导致更新被阻塞时，此值的 100％会导致最小可能的刷新。此配置中的默认值已被故意留空，以便兑现旧的 hbase.regionserver.global.memstore.lowerLimit 属性（如果存在）。

默认: none

```
hbase.systemtables.compacting.memstore.type
```

确定用于系统表（如 META，名称空间表等）的 memstore 的类型。默认情况下，NONE 是类型，因此我们对所有系统表使用默认的 memstore。如果我们需要为系统表使用压缩存储器，那么将这个属性设置为：BASIC / EAGER

默认: `NONE`

```
hbase.regionserver.optionalcacheflushinterval
```

在自动刷新之前，编辑在内存中的最长时间。默认为 1 小时。将其设置为 0 将禁用自动刷新。

默认: `3600000`

```
hbase.regionserver.dns.interface
```

区域服务器应从中报告其 IP 地址的网络接口的名称。

默认: `default`

```
hbase.regionserver.dns.nameserver
```

域名服务器应使用的名称服务器（DNS）的主机名或 IP 地址，以确定主机用于通信和显示的主机名。

默认: `default`

```
hbase.regionserver.region.split.policy
```

分割策略决定了一个区域应该何时拆分。当前可用的各种其他拆分策略是：BusyRegionSplitPolicy，ConstantSizeRegionSplitPolicy，DisabledRegionSplitPolicy，DelimitedKeyPrefixRegionSplitPolicy，KeyPrefixRegionSplitPolicy 和 SteppingSplitPolicy。DisabledRegionSplitPolicy 会阻止手动区域分割。

默认: `org.apache.hadoop.hbase.regionserver.SteppingSplitPolicy`

```
hbase.regionserver.regionSplitLimit
```

限制区域数量，之后不再发生区域分割。这并不是硬性限制区域数量，而是作为区域服务商在一定限度之后停止分裂的指导方针。默认设置为 1000。

默认: `1000`

```
zookeeper.session.timeout
```

ZooKeeper 会话超时（以毫秒为单位）。它使用两种不同的方式。首先，这个值用于 HBase 用来连接到集合的 ZK 客户端。当它启动一个 ZK 服务器时它也被 HBase 使用，并且它被作为'maxSessionTimeout'传递。请参http://hadoop.apache.org/zookeeper/docs/current/zookeeperProgrammers.html#ch_zkSessions。例如，如果 HBase 区域服务器连接到也由 HBase 管理的 ZK 集合，那么会话超时将是由此配置指定的。但是，连接到以不同配置管理的集成的区域服务器将受到该集合的 maxSessionTimeout 的限制。所以，尽管 HBase 可能会建议使用 90 秒，但是整体的最大超时时间可能会低于此值，并且会优先考虑。ZK 目前的默认值是 40 秒，比 HBase 的低。

默认: `90000`

```
zookeeper.znode.parent
```

ZooKeeper 中用于 HBase 的 Root ZNode。所有配置了相对路径的 HBase 的 ZooKeeper 文件都会在这个节点下。默认情况下，所有的 HBase 的 ZooKeeper 文件路径都被配置为一个相对路径，所以它们将全部进入这个目录下，除非被改变。

默认: `/hbase`

```
zookeeper.znode.acl.parent
```

Root ZNode 用于访问控制列表。

默认: `acl`

```
hbase.zookeeper.dns.interface
```

ZooKeeper 服务器应从中报告其 IP 地址的网络接口的名称。

默认: `default`

```
hbase.zookeeper.dns.nameserver
```

名称服务器（DNS）的主机名或 IP 地址，ZooKeeper 服务器应使用该名称服务器来确定主机用于通信和显示的主机名。

默认: `default`

```
hbase.zookeeper.peerport
```

ZooKeeper 同伴使用的端口进行彼此会话。有关更多信息，请参阅 http://hadoop.apache.org/zookeeper/docs/r3.1.1/zookeeperStarted.html#sc_RunningReplicatedZooKeeper

默认: `2888`

```
hbase.zookeeper.leaderport
```

ZooKeeper 用于 leader 选举的端口。有关更多信息，请参阅 http://hadoop.apache.org/zookeeper/docs/r3.1.1/zookeeperStarted.html#sc_RunningReplicatedZooKeeper

默认: `3888`

```
hbase.zookeeper.property.initLimit
```

来自 ZooKeeper 的配置 zoo.cfg 的属性。初始同步阶段可以采用的时钟（ticks）周期数。

默认: `10`

```
hbase.zookeeper.property.syncLimit
```

来自 ZooKeeper 的配置 zoo.cfg 的属性。发送请求和获取确认之间可以传递的时钟（ticks）数量。

默认: `5`

```
hbase.zookeeper.property.dataDir
```

来自 ZooKeeper 的配置 zoo.cfg 的属性。快照存储的目录。

默认: `${hbase.tmp.dir}/zookeeper`

```
hbase.zookeeper.property.clientPort
```

来自 ZooKeeper 的配置 zoo.cfg 的属性。客户端将连接的端口。

默认: `2181`

```
hbase.zookeeper.property.maxClientCnxns
```

来自 ZooKeeper 的配置 zoo.cfg 的属性。限制由 IP 地址标识的单个客户端的并发连接数量（在套接字级别）可能会对 ZooKeeper 集合的单个成员产生影响。设置为高，以避免独立运行和伪分布式运行的 zk 连接问题。

默认: `300`

```
hbase.client.write.buffer
```

BufferedMutator 写入缓冲区的默认大小（以字节为单位）。一个更大的缓冲区需要更多的内存 - 在客户端和服务器端，因为服务器实例化传递的写入缓冲区来处理它 - 但更大的缓冲区大小减少了 RPC 的数量。对于估计使用的服务器端内存，计算：hbase.client.write.buffer * hbase.regionserver.handler.count

默认: `2097152`

```
hbase.client.pause
```

一般客户端 pause 值。在运行失败的 get，region lookup 等的重试之前，主要用作等待的值。 hbase.client.retries.number 有关如何取消此初始暂停量以及此重试数量说明。

默认: `100`

```
hbase.client.pause.cqtbe
```

是否为 CallQueueTooBigException（cqtbe）使用特殊的客户端 pause。如果您观察到来自同一个 RegionServer 的频繁的 CQTBE，并且其中的调用队列保持充满，则将此属性设置为比 hbase.client.pause 更高的值

默认: none

```
hbase.client.retries.number
```

最大重试次数。用作所有可重试操作（如获取单元格值，启动行更新等）的最大值。重试间隔是基于 hbase.client.pause 的粗略函数。首先，我们在这段时间重试，但后来退后，我们很快就达到每十秒钟重试一次。请参阅 HConstants＃RETRY_BACKOFF 了解备份如何提升。改变这个设置和 hbase.client.pause 来适应你的工作负载。

默认: `15`

```
hbase.client.max.total.tasks
```

单个 HTable 实例发送到集群的最大并发突变任务数。

默认: `100`

```
hbase.client.max.perserver.tasks
```

单个 HTable 实例将发送到单个区域服务器的并发突变任务的最大数量。

默认: `2`

```
hbase.client.max.perregion.tasks
```

客户端将维护到单个 Region 的最大并发突变任务数。也就是说，如果已经有 hbase.client.max.perregion.tasks 写入这个区域，那么新的放入将不会被发送到这个区域，直到一些写入完成。

默认: `1`

```
hbase.client.perserver.requests.threshold
```

所有客户端线程（进程级别）中一个服务器的并发未决请求的最大数量。超过请求将立即抛出 ServerTooBusyException，以防止用户的线程被占用和只被一个缓慢的区域服务器阻止。如果使用固定数量的线程以同步方式访问 HBase，请将此值设置为与线程数量相关的适当值，这些值将对您有所帮助。详见:https://issues.apache.org/jira/browse/HBASE-16388

默认: `2147483647`

```
hbase.client.scanner.caching
```

如果从本地，客户端内存中未提供，则在扫描程序上调用 next 时尝试获取的行数。此配置与 hbase.client.scanner.max.result.size 一起使用，可以有效地使用网络。缺省值默认为 Integer.MAX_VALUE，这样网络将填充由 hbase.client.scanner.max.result.size 定义的块大小，而不受特定行数的限制，因为行的大小随表格的不同而不同。如果您事先知道扫描中不需要超过一定数量的行，则应通过扫描＃setCaching 将此配置设置为该行限制。缓存值越高，扫描器的速度越快，但是会占用更多的内存，而当缓存空置时，下一次调用的时间可能会越来越长。请勿设置此值，以便调用之间的时间大于扫描器超时；即 hbase.client.scanner.timeout.period

默认: `2147483647`

```
hbase.client.keyvalue.maxsize
```

指定 KeyValue 实例的组合的最大允许大小。这是为保存在存储文件中的单个条目设置上限。由于它们不能被分割，所以有助于避免因为数据太大而导致地区不能被分割。将此设置为最大区域大小的一小部分似乎是明智的。将其设置为零或更少将禁用检查。

默认: `10485760`

```
hbase.server.keyvalue.maxsize
```

单个单元格的最大允许大小，包括值和所有关键组件。值为 0 或更小将禁用检查。默认值是 10MB。这是保护服务器免受 OOM 情况的安全设置。

默认: `10485760`

```
hbase.client.scanner.timeout.period
```

客户端扫描程序的租期以毫秒为单位。

默认: `60000`

```
hbase.client.localityCheck.threadPoolSize
```

默认: `2`

```
hbase.bulkload.retries.number
```

最大重试次数，这是在面对分裂操作时尝试原子批量加载的最大迭代次数，0 意味着永不放弃。

默认: `10`

```
hbase.master.balancer.maxRitPercent
```

平衡时转换区域的最大百分比。默认值是 1.0。所以没有平衡器节流。如果将此配置设置为 0.01，则意味着在平衡时转换中最多有 1％的区域。那么当平衡时，集群的可用性至少为 99％。

默认: `1.0`

```
hbase.balancer.period
```

区域平衡器在主站运行的时间段。

默认: `300000`

```
hbase.normalizer.period
```

区域标准化程序在主程序中运行的时段。

默认: `300000`

```
hbase.normalizer.min.region.count
```

区域标准化程序最小数量

默认: `3`

```
hbase.regions.slop
```

如果任何区域服务器具有平均值+（平均*斜率）区域，则重新平衡。StochasticLoadBalancer（默认负载均衡器）中此参数的默认值为 0.001，其他负载均衡器（即 SimpleLoadBalancer）中的默认值为 0.2。

默认: `0.001`

```
hbase.server.thread.wakefrequency
```

在两次搜索之间休息的时间（以毫秒为单位）。用作日志滚筒等服务线程的睡眠间隔。

默认: `10000`

```
hbase.server.versionfile.writeattempts
```

在放弃之前重试尝试写入版本文件的次数。每个尝试都由 hbase.server.thread.wake 频率毫秒分隔。

默认: `3`

```
hbase.hregion.memstore.flush.size
```

如果 memstore 的大小超过此字节数，Memstore 将被刷新到磁盘。值由每个 hbase.server.thread.wakefrequency 运行的线程检查。

默认: `134217728`

```
hbase.hregion.percolumnfamilyflush.size.lower.bound.min
```

如果使用了 FlushLargeStoresPolicy，并且有多个列族，那么每当我们达到完全的 memstore 限制时，我们就会找出所有 memstore 超过“下限”的列族，只有在保留其他内存的同时刷新它们。默认情况下，“下限”将是“hbase.hregion.memstore.flush.size/column_family_number”，除非该属性的值大于该值。如果没有一个族的 memstore 大小超过下限，所有的 memstore 都将被刷新（就像往常一样）。

默认: `16777216`

```
hbase.hregion.preclose.flush.size
```

如果我们关闭时某个区域的存储空间大于或等于这个大小，则可以运行“预先刷新（pre-flush）”来清除存储区，然后再放置区域关闭标记并使区域脱机。关闭时，在关闭标志下运行刷新以清空内存。在此期间，该地区处于离线状态，我们没有进行任何写入。如果 memstore 内容很大，则此刷新可能需要很长时间才能完成。这个预刷新是为了清理大部分的 memstore，然后把关闭标志放到离线区域，这样在关闭标志下运行的刷新没有什么用处。

默认: `5242880`

```
hbase.hregion.memstore.block.multiplier
```

如果 memstore 具有 hbase.hregion.memstore.block.multiplier 乘以 hbase.hregion.memstore.flush.size 个字节，则阻止更新。在更新通信高峰期间有用的防止失控的 memstore。如果没有上限，memstore 就会填满，当刷新生成的 flush 文件需要很长时间才能压缩或拆分。

默认: `4`

```
hbase.hregion.memstore.mslab.enabled
```

启用 MemStore-Local 分配缓冲区，该功能可用于在繁重的写入负载下防止堆碎片。这可以减少在大堆停止全局 GC pause 的频率。

默认: `true`

```
hbase.hregion.max.filesize
```

最大 HFile 大小。如果一个地区的 HFiles 的总和已经超过了这个数值，这个地区就会被分成两部分。

默认: `10737418240`

```
hbase.hregion.majorcompaction
```

主要压缩之间的时间，以毫秒表示。设置为 0 可禁用基于时间的自动重要压缩。用户请求的和基于大小的主要压缩将仍然运行。这个值乘以 hbase.hregion.majorcompaction.jitter，使压缩在一个给定的时间窗口内稍微随机的时间开始。默认值是 7 天，以毫秒表示。如果主要压缩导致您的环境中断，则可以将它们配置为在部署的非高峰时间运行，或者通过将此参数设置为 0 来禁用基于时间的主要压缩，并在 cron 作业或另一个外部机制。

默认: `604800000`

```
hbase.hregion.majorcompaction.jitter
```

应用于 hbase.hregion.majorcompaction 的乘数会导致压缩发生在给定的时间量的任何一侧的 hbase.hregion.majorcompaction。数字越小，压缩将越接近 hbase.hregion.majorcompaction 时间间隔。

默认: `0.50`

```
hbase.hstore.compactionThreshold
```

如果任何一个 Store 中存在超过此数量的 StoreFiles（每个 MemStore 刷新一个 StoreFile），则会执行压缩以将所有 StoreFile 重写为单个 StoreFile。较大的值会延迟压实，但是当压缩发生时，需要较长时间才能完成。

默认: `3`

```
hbase.regionserver.compaction.enabled
```

开启/关闭 压缩 通过设置 true/false.也可以通过 compaction_switch shell 命令

默认: `true`

```
hbase.hstore.flusher.count
```

刷新线程的数量。用更少的线程，MemStore 刷新将排队。随着线程数量的增加，刷新将并行执行，增加了 HDFS 的负载，并可能导致更多的压缩。

默认: `2`

```
hbase.hstore.blockingStoreFiles
```

如果任何一个 Store 中存在超过此数量的 StoreFiles（每次刷新 MemStore 时将写入一个 StoreFile），则会阻止该区域的更新，直到压缩完成或超出 hbase.hstore.blockingWaitTime。

默认: `16`

```
hbase.hstore.blockingWaitTime
```

在达到 hbase.hstore.blockingStoreFiles 定义的 StoreFile 限制后，区域将阻止更新的时间。经过这段时间后，即使压缩尚未完成，该地区也将停止阻止更新。

默认: `90000`

```
hbase.hstore.compaction.min
```

压缩可以运行之前，必须有符合进行压缩条件的最小 StoreFiles 数量。调整 hbase.hstore.compaction.min 的目标是避免使用太多的小型 StoreFiles 来压缩。如果将此值设置为 2，则每次在 Store 中有两个 StoreFiles 时会导致轻微的压缩，这可能不合适。如果将此值设置得太高，则需要相应调整所有其他值。对于大多数情况下，默认值是适当的。在以前的 HBase 版本中，参数 hbase.hstore.compaction.min 被命名为 hbase.hstore.compactionThreshold。

默认: `3`

```
hbase.hstore.compaction.max
```

无论符合条件的 StoreFiles 的数量如何，将为单个次要压缩选择的 StoreFiles 的最大数量。有效地，hbase.hstore.compaction.max 的值控制单个压缩完成所需的时间长度。将其设置得更大意味着更多的 StoreFiles 包含在压缩中。对于大多数情况下，默认值是适当的。

默认: `10`

```
hbase.hstore.compaction.min.size
```

StoreFile（或使用 ExploringCompactionPolicy 时选择的 StoreFiles）小于此大小将始终有资格进行轻微压缩。这个大小或更大的 HFile 通过 hbase.hstore.compaction.ratio 进行计算，以确定它们是否合格。由于此限制表示所有 StoreFiles 的“自动包含”限制小于此值，因此在需要刷新多个 StoreFile（1-2 MB 范围内的许多 StoreFiles）的写入繁重环境中可能需要降低此值，因为每个 StoreFile 都将作为目标，对于压缩而言，所得到的 StoreFile 可能仍然在最小尺寸下，并且需要进一步的压缩。如果此参数降低，比率检查会更快地触发。这解决了在早期版本的 HBase 中看到的一些问题，但是在大多数情况下不再需要更改此参数。

默认: `134217728`

```
hbase.hstore.compaction.max.size
```

StoreFile（或使用 ExploringCompactionPolicy 时选择的 StoreFiles）大于此大小将被排除在压缩之外。提高 hbase.hstore.compaction.max.size 的效果较少，较大的 StoreFiles 不经常压缩。如果你觉得压缩过于频繁而没有太多好处，你可以尝试提高这个价值。默认值：LONG.MAX_VALUE 的值，以字节表示。

默认: `9223372036854775807`

```
hbase.hstore.compaction.ratio
```

对于轻微压缩，此比率用于确定大于 hbase.hstore.compaction.min.size 的给定 StoreFile 是否适合压缩。其作用是限制大型 StoreFiles 的压缩。hbase.hstore.compaction.ratio 的值以浮点小数表示。一个很大的比例，如 10，将产生一个大型的 StoreFile。相反，低值（如 0.25）会产生类似于 BigTable 压缩算法的行为，产生四个 StoreFiles。推荐使用 1.0 到 1.4 之间的中等数值。在调整此值时，您要平衡写入成本与读取成本。提高价值（如 1.4）会有更多的写入成本，因为你会压缩更大的 StoreFiles。然而，在读取期间，HBase 将需要通过更少的 StoreFiles 来完成读取。如果您不能利用 Bloom 过滤器，请考虑使用这种方法。否则，可以将此值降低到 1.0 以降低写入的背景成本，并使用 Bloom 过滤器来控制读取期间触摸的 StoreFiles 的数量。对于大多数情况下，默认值是适当的。

默认: `1.2F`

```
hbase.hstore.compaction.ratio.offpeak
```

允许您设置不同（默认情况下，更积极）的比率，以确定在非高峰时段是否包含较大的 StoreFiles。以与 hbase.hstore.compaction.ratio 相同的方式工作。仅当 hbase.offpeak.start.hour 和 hbase.offpeak.end.hour 也被启用时才适用。

默认: `5.0F`

```
hbase.hstore.time.to.purge.deletes
```

使用未来的时间戳延迟清除标记的时间。如果未设置，或设置为 0，则将在下一个主要压缩过程中清除所有删除标记（包括具有未来时间戳的标记）。否则，将保留一个删除标记，直到在标记的时间戳之后发生的主要压缩加上此设置的值（以毫秒为单位）。

默认: `0`

```
hbase.offpeak.start.hour
```

非高峰时段开始，以 0 到 23 之间的整数表示，包括 0 和 23 之间的整数。设置为-1 以禁用非高峰。

默认: `-1`

```
hbase.offpeak.end.hour
```

非高峰时段结束，以 0 到 23 之间的整数表示，包括 0 和 23 之间的整数。设置为-1 以禁用非高峰。

默认: `-1`

```
hbase.regionserver.thread.compaction.throttle
```

有两个不同的线程池用于压缩，一个用于大型压缩，另一个用于小型压缩。这有助于保持精简表（如 hbase：meta）的快速压缩。如果压缩度大于此阈值，则会进入大型压缩池。在大多数情况下，默认值是适当的。默认值：2 x hbase.hstore.compaction.max x hbase.hregion.memstore.flush.size（默认为 128MB）。值字段假定 hbase.hregion.memstore.flush.size 的值与默认值相同。

默认: `2684354560`

```
hbase.regionserver.majorcompaction.pagecache.drop
```

指定是否通过主要压缩删除读取/写入系统页面缓存的页面。将其设置为 true 有助于防止重大压缩污染页面缓存，这几乎总是要求的，特别是对于具有低/中等内存与存储率的群集。

默认: `true`

```
hbase.regionserver.minorcompaction.pagecache.drop
```

指定是否通过较小的压缩删除读取/写入系统页面缓存的页面。将其设置为 true 有助于防止轻微压缩污染页面缓存，这对于内存与存储比率较低的群集或写入较重的群集是最有利的。当大部分读取位于最近写入的数据上时，您可能希望在中等到低写入工作负载下将其设置为 false。

默认: `true`

```
hbase.hstore.compaction.kv.max
```

刷新或压缩时要读取并批量写入的 KeyValues 的最大数量。如果你有较大的 KeyValues，并且 Out Of Memory Exceptions 有问题，请将它设置得更低。

默认: `10`

```
hbase.storescanner.parallel.seek.enable
```

在 StoreScanner 中启用 StoreFileScanner 并行搜索功能，该功能可以在特殊情况下减少响应延迟。

默认: `false`

```
hbase.storescanner.parallel.seek.threads
```

如果启用了并行查找功能，则默认线程池大小。

默认: `10`

```
hfile.block.cache.size
```

StoreFile 使用的最大堆（-Xmx 设置）分配给块缓存的百分比。默认值为 0.4 意味着分配 40％。设置为 0 禁用，但不建议；您至少需要足够的缓存来保存存储文件索引。

默认: `0.4`

```
hfile.block.index.cacheonwrite
```

这允许在索引被写入时将非根多级索引块放入块高速缓存中。

默认: `false`

```
hfile.index.block.max.size
```

当多级块索引中叶级，中级或根级索引块的大小增长到这个大小时，块将被写出并启动一个新块。

默认: `131072`

```
hbase.bucketcache.ioengine
```

在哪里存储 bucketcache 的内容。其中之一：offheap、文件或 mmap。如果有文件，则将其设置为 file(s)：PATH_TO_FILE。mmap 意味着内容将在一个 mmaped 文件中。使用 mmap：PATH_TO_FILE。详见: http://hbase.apache.org/book.html#offheap.blockcache

默认: none

```
hbase.bucketcache.size
```

EITHER 表示缓存的总堆内存大小的百分比（如果小于 1.0），则表示 BucketCache 的总容量（兆字节）。默认值：0.0

默认: none

```
hbase.bucketcache.bucket.sizes
```

用于 bucketcache 的存储区大小的逗号分隔列表。可以是多种尺寸。列出从最小到最大的块大小。您使用的大小取决于您的数据访问模式。必须是 256 的倍数，否则当你从缓存中读取时，你会遇到“java.io.IOException：Invalid HFile block magic”。如果您在此处未指定任何值，那么您可以选取代码中设置的默认 bucketsizes。

默认: none

```
hfile.format.version
```

用于新文件的 HFile 格式版本。版本 3 添加了对 hfiles 中标签的支持（请参阅 http://hbase.apache.org/book.html#hbase.tags）。另请参阅配置“hbase.replication.rpc.codec”。

默认: `3`

```
hfile.block.bloom.cacheonwrite
```

为复合 Bloom 过滤器的内联块启用写入缓存。

默认: `false`

```
io.storefile.bloom.block.size
```

复合 Bloom 过滤器的单个块（“chunk”）的字节大小。这个大小是近似的，因为 Bloom 块只能被插入到数据块的边界处，而每个数据块的 key 的个数也不相同。

默认: `131072`

```
hbase.rs.cacheblocksonwrite
```

块完成后，是否应将 HFile 块添加到块缓存中。

默认: `false`

```
hbase.rpc.timeout
```

这是为了让 RPC 层定义一个远程调用超时（毫秒）HBase 客户端应用程序超时。它使用 ping 来检查连接，但最终会抛出 TimeoutException。

默认: `60000`

```
hbase.client.operation.timeout
```

操作超时是一个顶级的限制（毫秒），确保表格中的阻止操作不会被阻止超过这个限制。在每个操作中，如果 rpc 请求由于超时或其他原因而失败，则将重试直到成功或抛出 RetriesExhaustedException。但是，如果总的阻塞时间在重试耗尽之前达到操作超时，则会提前中断并抛出 SocketTimeoutException。

默认: `1200000`

```
hbase.cells.scanned.per.heartbeat.check
```

在 heartbeat 检查之间扫描的单元格的数量。在扫描处理过程中会发生 heartbeat 检查，以确定服务器是否应该停止扫描，以便将 heartbeat 消息发送回客户端。heartbeat 消息用于在长时间运行扫描期间保持客户端 - 服务器连接的活动。较小的值意味着 heartbeat 检查将更频繁地发生，因此将对扫描的执行时间提供更严格的界限。数值越大意味着 heartbeat 检查发生的频率越低。

默认: `10000`

```
hbase.rpc.shortoperation.timeout
```

这是“hbase.rpc.timeout”的另一个版本。对于集群内的 RPC 操作，我们依靠此配置为短操作设置短超时限制。例如，区域服务器试图向活动主服务器报告的短 rpc 超时可以更快地进行主站故障转移过程。

默认: `10000`

```
hbase.ipc.client.tcpnodelay
```

在 rpc 套接字连接上设置没有延迟。详见: [http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#getTcpNoDelay(](http://docs.oracle.com/javase/1.5.0/docs/api/java/net/Socket.html#getTcpNoDelay())

默认: `true`

```
hbase.regionserver.hostname
```

这个配置适用于对 HBase 很熟悉的人：除非你真的知道你在做什么，否则不要设定它的价值。当设置为非空值时，这表示底层服务器的（面向外部）主机名。

详见: https://issues.apache.org/jira/browse/HBASE-12954

默认: none

```
hbase.regionserver.hostname.disable.master.reversedns
```

这个配置适用于对 HBase 很熟练的人：除非你真的知道你在做什么，否则不要设定它的价值。当设置为 true 时，regionserver 将使用当前节点主机名作为服务器名称，HMaster 将跳过反向 DNS 查找并使用 regionserver 发送的主机名。请注意，此配置和 hbase.regionserver.hostname 是互斥的。详见: https://issues.apache.org/jira/browse/HBASE-18226

默认: `false`

```
hbase.master.keytab.file
```

用于登录配置的 HMaster 服务器主体的 kerberos 密钥表文件的完整路径。

默认: none

```
hbase.master.kerberos.principal
```

Ex. "hbase/[_HOST@EXAMPLE.COM](mailto:_HOST@EXAMPLE.COM)"应该用来运行 HMaster 进程的 Kerberos 主体名称。主体名称的格式应为：user/hostname @ DOMAIN。如果使用“_HOST”作为主机名部分，它将被替换为正在运行的实例的实际主机名。

默认: none

```
hbase.regionserver.keytab.file
```

用于登录配置的 HRegionServer 服务器主体的 kerberos 密钥表文件的完整路径。

默认: none

```
hbase.regionserver.kerberos.principal
```

Ex. "hbase/[_HOST@EXAMPLE.COM](mailto:_HOST@EXAMPLE.COM)"应该用来运行 HRegionServer 进程的 kerberos 主体名称。主体名称的格式应为：user/hostname @ DOMAIN。如果使用“_HOST”作为主机名部分，它将被替换为正在运行的实例的实际主机名。此主体的条目必须存在于 hbase.regionserver.keytab.file 中指定的文件中

默认: none

```
hadoop.policy.file
```

RPC 服务器使用策略配置文件对客户端请求进行授权决策。仅在启用 HBase 安全性时使用。

默认: `hbase-policy.xml`

```
hbase.superuser
```

用户或组列表（以逗号分隔），允许在整个集群中拥有完全权限（不管存储的 ACL）。仅在启用 HBase 安全性时使用。

默认: none

```
hbase.auth.key.update.interval
```

服务器中认证令牌的主密钥的更新间隔（以毫秒为单位）。仅在启用 HBase 安全性时使用。

默认: `86400000`

```
hbase.auth.token.max.lifetime
```

验证令牌过期的最长生存时间（以毫秒为单位）。仅在启用 HBase 安全性时使用。

默认: `604800000`

```
hbase.ipc.client.fallback-to-simple-auth-allowed
```

当客户端配置为尝试安全连接，但尝试连接到不安全的服务器时，该服务器可能会指示客户端切换到 SASL SIMPLE（不安全）身份验证。此设置控制客户端是否接受来自服务器的此指令。如果为 false（默认值），则客户端将不允许回退到 SIMPLE 身份验证，并会中止连接。

默认: `false`

```
hbase.ipc.server.fallback-to-simple-auth-allowed
```

当服务器配置为需要安全连接时，它将拒绝来自使用 SASL SIMPLE（不安全）身份验证的客户端的连接尝试。此设置允许安全服务器在客户端请求时接受来自客户端的 SASL SIMPLE 连接。如果为 false（默认值），服务器将不允许回退到 SIMPLE 身份验证，并将拒绝连接。警告：只有在将客户端转换为安全身份验证时，才应将此设置用作临时措施。必须禁止它才能进行安全操作。

默认: `false`

```
hbase.display.keys
```

当它被设置为 true 时，webUI 等将显示所有开始/结束键作为表格细节，区域名称等的一部分。当这被设置为假时，键被隐藏。

默认: `true`

```
hbase.coprocessor.enabled
```

启用或禁用协处理器加载。如果'false'（禁用），任何其他协处理器相关的配置将被忽略。

默认: `true`

```
hbase.coprocessor.user.enabled
```

启用或禁用用户（又名表）协处理器加载。如果'false'（禁用），则表格描述符中的任何表协处理器属性将被忽略。如果“hbase.coprocessor.enabled”为“false”，则此设置无效。

默认: `true`

```
hbase.coprocessor.region.classes
```

在所有表上默认加载的区域观察者或端点协处理器的逗号分隔列表。对于任何覆盖协处理器方法，这些类将按顺序调用。在实现自己的协处理器之后，将其添加到 HBase 的类路径中，并在此处添加完全限定的类名称。协处理器也可以通过设置 HTableDescriptor 或者 HBase shell 来按需加载。

默认: none

```
hbase.coprocessor.master.classes
```

在活动的 HMaster 进程中默认加载的 org.apache.hadoop.hbase.coprocessor.MasterObserver 协处理器的逗号分隔列表。对于任何实施的协处理器方法，列出的类将按顺序调用。在实现你自己的 MasterObserver 之后，把它放在 HBase 的类路径中，并在这里添加完全限定的类名称。

默认: none

```
hbase.coprocessor.abortonerror
```

如果协处理器加载失败，初始化失败或引发意外的 Throwable 对象，则设置为 true 将导致托管服务器（主服务器或区域服务器）中止。将其设置为 false 将允许服务器继续执行，但所涉及的协处理器的系统范围状态将变得不一致，因为它只能在一部分服务器中正确执行，所以这对于仅调试是非常有用的。

默认: `true`

```
hbase.rest.port
```

HBase REST 服务器的端口。

默认: `8080`

```
hbase.rest.readonly
```

定义 REST 服务器将启动的模式。可能的值有：false：此时，所有的 HTTP 方法都是允许的 - GET / PUT / POST / DELETE。true：此时只允许 GET 方法。

默认: `false`

```
hbase.rest.threads.max
```

REST 服务器线程池的最大线程数。池中的线程被重用来处理 REST 请求。这将控制同时处理的最大请求数。这可能有助于控制 REST 服务器使用的内存以避免 OOM 问题。如果线程池已满，则传入的请求将排队并等待一些空闲的线程。

默认: `100`

```
hbase.rest.threads.min
```

REST 服务器线程池的最小线程数。线程池总是至少有这么多的线程，所以 REST 服务器已经准备好为传入的请求提供服务。

默认: `2`

```
hbase.rest.support.proxyuser
```

启用运行 REST 服务器以支持代理用户模式。

默认: `false`

```
hbase.defaults.for.version.skip
```

设置为 true 可以跳过“hbase.defaults.for.version”检查。将其设置为 true 可以在除 maven 生成的另一侧之外的上下文中有用；即运行在 IDE 中。你需要设置这个布尔值为 true 以避免看到 RuntimeException：“hbase-default.xml 文件似乎是 HBase（\ $ {hbase.version}）的旧版本，这个版本是 XXX-SNAPSHOT”

默认: `false`

```
hbase.table.lock.enable
```

设置为 true 以启用锁定 zookeeper 中的表以进行模式更改操作。从主服务器锁定表可以防止并发的模式修改损坏表状态。

默认: `true`

```
hbase.table.max.rowsize
```

单行字节的最大大小（默认值为 1 Gb），用于 Get-ing 或 Scan'ning，不设置行内扫描标志。如果行大小超过此限制 RowTooBigException 被抛出到客户端。

默认: `1073741824`

```
hbase.thrift.minWorkerThreads
```

线程池的“核心大小”。在每个连接上创建新线程，直到创建了许多线程。

默认: `16`

```
hbase.thrift.maxWorkerThreads
```

线程池的最大大小。待处理的请求队列溢出时，将创建新线程，直到其号码达到此数字。之后，服务器开始丢弃连接。

默认: `1000`

```
hbase.thrift.maxQueuedRequests
```

在队列中等待的最大等待节点连接数。如果池中没有空闲线程，则服务器将请求排队。只有当队列溢出时，才会添加新的线程，直到 hbase.thrift.maxQueuedRequests 线程。

默认: `1000`

```
hbase.regionserver.thrift.framed
```

在服务器端使用 Thrift TFramedTransport。对于 thrift 服务器，这是推荐的传输方式，需要在客户端进行类似的设置。将其更改为 false 将选择默认传输，当由于 THRIFT-601 发出格式错误的请求时，容易受到 DoS 的影响。

默认: `false`

```
hbase.regionserver.thrift.framed.max_frame_size_in_mb
```

使用成帧传输时的默认帧大小，以 MB 为单位。

默认: `2`

```
hbase.regionserver.thrift.compact
```

使用 Thrift TCompactProtocol 二进制序列化协议。

默认: `false`

```
hbase.rootdir.perms
```

安全（kerberos）安装程序中根数据子目录的 FS Permissions。主服务器启动时，会使用此权限创建 rootdir，如果不匹配则设置权限。

默认: `700`

```
hbase.wal.dir.perms
```

安全（kerberos）安装程序中的根 WAL 目录的 FS Permissions。当主服务器启动时，它将使用此权限创建 WAL 目录，如果不匹配则设置权限。

默认: `700`

```
hbase.data.umask.enable
```

如果启用，则启用该文件权限应分配给区域服务器写入的文件

默认: `false`

```
hbase.data.umask
```

当 hbase.data.umask.enable 为 true 时，应该用来写入数据文件的文件权限

默认: `000`

```
hbase.snapshot.enabled
```

设置为 true 以允许 taken/restored/cloned。

默认: `true`

```
hbase.snapshot.restore.take.failsafe.snapshot
```

设置为 true 以在还原操作之前得到快照。所得到的快照将在失败的情况下使用，以恢复以前的状态。在还原操作结束时，此快照将被删除

默认: `true`

```
hbase.snapshot.restore.failsafe.name
```

restore 操作所采用的故障安全快照的名称。您可以使用{snapshot.name}，{table.name}和{restore.timestamp}变量根据要恢复的内容创建一个名称。

默认: `hbase-failsafe-{snapshot.name}-{restore.timestamp}`

```
hbase.snapshot.working.dir
```

快照过程将发生的位置。已完成快照的位置不会更改，但快照进程发生的临时目录将设置为此位置。为了提高性能，它可以是一个独立的文件系统，而不是根目录。有关详细信息，请参阅 HBase-21098

默认: none

```
hbase.server.compactchecker.interval.multiplier
```

这个数字决定了我们扫描的频率，看是否需要压缩。通常情况下，压缩是在某些事件（如 memstore flush）之后完成的，但是如果区域在一段时间内没有收到大量的写入，或者由于不同的压缩策略，则可能需要定期检查。检查之间的时间间隔是 hbase.server.compactchecker.interval.multiplier 乘以 hbase.server.thread.wakefrequency。

默认: `1000`

```
hbase.lease.recovery.timeout
```

在放弃之前，我们等待 dfs lease 的总恢复时间。

默认: `900000`

```
hbase.lease.recovery.dfs.timeout
```

dfs 恢复 lease 调用之间的时间间隔。应该大于 namenode 为 datanode 的一部分发出块恢复命令所需的时间总和；dfs.heartbeat.interval 和主数据节点所花费的时间，在死数据节点上执行数据块恢复到超时；通常是 dfs.client.socket-timeout。详见:HBASE-8389

默认: `64000`

```
hbase.column.max.version
```

新的列族描述符将使用此值作为要保留的默认版本数。

默认: `1`

```
dfs.client.read.shortcircuit
```

如果设置为 true，则此配置参数启用 short-circuit 本地读取。

默认: `false`

```
dfs.domain.socket.path
```

如果将 dfs.client.read.shortcircuit 设置为 true，则这是一个 UNIX 域套接字的路径，该套接字将用于 DataNode 与本地 HDFS 客户端之间的通信。如果该路径中存在字符串“_PORT”，则会被 DataNode 的 TCP 端口替换。请注意托管共享域套接字的目录的权限。

默认: `none`

```
hbase.dfs.client.read.shortcircuit.buffer.size
```

如果未设置 DFSClient 配置 dfs.client.read.shortcircuit.buffer.size，我们将使用此处配置的内容作为 short-circuit 读取默认直接字节缓冲区大小。DFSClient 本机默认值是 1MB；HBase 保持 HDFS 文件的打开状态，所以文件块*1MB 的数量很快就开始累积起来，并由于直接内存不足而威胁 OOME。所以，我们从默认设置下来。使它大于在 HColumnDescriptor 中设置的默认 hbase 块大小，通常是 64k。

默认: `131072`

```
hbase.regionserver.checksum.verify
```

如果设置为 true（默认），HBase 将验证 hfile 块的校验和。当 HBase 写出 hfiles 时，HBase 将校验和写入数据。HDFS（在此写入时）将校验和写入单独的文件，而不是需要额外查找的数据文件。设置这个标志可以节省一些 I/O。设置此标志时，HDFS 的校验和验证将在 hfile 流内部禁用。如果 hbase-checksum 验证失败，我们将切换回使用 HDFS 校验和（所以不要禁用 HDFS 校验！除此功能外，还适用于 hfiles，而不适用于 WAL）。如果这个参数设置为 false，那么 hbase 将不会验证任何校验和，而是取决于 HDFS 客户端中的校验和验证。

默认: `true`

```
hbase.hstore.bytes.per.checksum
```

新创建的校验和块中的字节数，用于 hfile 块中的 HBase 级校验和。

默认: `16384`

```
hbase.hstore.checksum.algorithm
```

用于计算校验和的算法的名称。可能的值是 NULL，CRC32，CRC32C。

默认: `CRC32C`

```
hbase.client.scanner.max.result.size
```

调用扫描器的下一个方法时返回的最大字节数。请注意，当单个行大于此限制时，行仍然完全返回。默认值是 2MB，这对于 1ge 网络是有好处的。有了更快和/或更高的延迟网络，这个值应该增加。

默认: `2097152`

```
hbase.server.scanner.max.result.size
```

调用扫描器的下一个方法时返回的最大字节数。请注意，当单个行大于此限制时，行仍然完全返回。默认值是 100MB。这是保护服务器免受 OOM 情况的安全设置。

默认: `104857600`

```
hbase.status.published
```

该设置激活了主控发布区域服务器的状态。当一台区域服务器死亡并开始恢复时，主服务器会将这些信息推送到客户端应用程序，让他们立即切断连接，而不是等待超时。

默认: `false`

```
hbase.status.publisher.class
```

用 multicast 消息实现状态发布。

默认: `org.apache.hadoop.hbase.master.ClusterStatusPublisher$MulticastPublisher`

```
hbase.status.listener.class
```

使用 multicast 消息实现状态监听器。

默认: `org.apache.hadoop.hbase.client.ClusterStatusListener$MulticastListener`

```
hbase.status.multicast.address.ip
```

用于 multicase 状态发布的 multicase 地址。

默认: `226.1.1.3`

```
hbase.status.multicast.address.port
```

用于 multicase 状态发布的 multicase 端口。

默认: `16100`

```
hbase.dynamic.jars.dir
```

自定义过滤器 JAR 的目录可以由区域服务器动态加载，而无需重新启动。但是，已加载的过滤器/协处理器类将不会被卸载。不适用于协处理器。详见:HBASE-1936

默认: `${hbase.rootdir}/lib`

```
hbase.security.authentication
```

控制是否为 HBase 启用安全身份验证。可能的值是“simple”（不认证）和“Kerberos”。

默认: `simple`

```
hbase.rest.filter.classes
```

用于 REST 服务的 Servlet 过滤器。

默认: `org.apache.hadoop.hbase.rest.filter.GzipFilter`

```
hbase.master.loadbalancer.class
```

用于在期间发生时执行区域平衡的类。它将 DefaultLoadBalancer 替换为默认值（因为它被重命名为 SimpleLoadBalancer ）。详见: http://hbase.apache.org/devapidocs/org/apache/hadoop/hbase/master/balancer/StochasticLoadBalancer.html

默认: `org.apache.hadoop.hbase.master.balancer.StochasticLoadBalancer`

```
hbase.master.loadbalance.bytable
```

平衡器运行时的因子表名称。默认：false。

默认: `false`

```
hbase.master.normalizer.class
```

用于执行期间发生时的区域标准化的类。详见: http://hbase.apache.org/devapidocs/org/apache/hadoop/hbase/master/normalizer/SimpleRegionNormalizer.html

默认: `org.apache.hadoop.hbase.master.normalizer.SimpleRegionNormalizer`

```
hbase.rest.csrf.enabled
```

设置为 true 以启用跨站请求伪造的保护。

默认: `false`

```
hbase.rest-csrf.browser-useragents-regex
```

通过将 hbase.rest.csrf.enabled 设置为 true 来启用为 REST 服务器，针对跨站点请求伪造（CSRF）的防护时，用于匹配 HTTP 请求的 User-Agent 标头的正则表达式的逗号分隔列表。如果传入的用户代理与这些正则表达式中的任何一个相匹配，则认为该请求被浏览器发送，因此 CSRF 预防被强制执行。如果请求的用户代理与这些正则表达式中的任何一个都不匹配，则该请求被认为是由除浏览器以外的其他东西发送的，例如脚本自动化。在这种情况下，CSRF 不是一个潜在的攻击向量，所以预防没有被执行。这有助于实现与尚未更新以发送 CSRF 预防报头的现有自动化的向后兼容性。

默认: `<sup>Mozilla.**,**</sup>**Opera.**`

```
hbase.security.exec.permission.checks
```

如果启用此设置，并且基于 ACL 的访问控制处于活动状态（AccessController 协处理器作为系统协处理器安装，或作为表协处理器安装在表上），则必须授予所有相关用户 EXEC 权限（如果需要执行协处理器端点调用。像任何其他权限一样，EXEC 权限可以在全局范围内授予用户，也可以授予每个表或命名空间的用户。有关协处理器端点的更多信息，请参阅 HBase 联机手册的协处理器部分。有关使用 AccessController 授予或撤消权限的更多信息，请参阅 HBase 联机手册的安全性部分。

默认: `false`

```
hbase.procedure.regionserver.classes
```

在活动 HRegionServer 进程中默认加载的 org.apache.hadoop.hbase.procedure.RegionServerProcedureManager 过程管理器的逗号分隔列表。生命周期方法（init / start / stop）将由活动的 HRegionServer 进程调用，以执行特定的全局 barriered 过程。在实现你自己的 RegionServerProcedureManager 之后，把它放在 HBase 的类路径中，并在这里添加完全限定的类名称。

默认: none

```
hbase.procedure.master.classes
```

在活动 HMaster 进程中默认加载的 org.apache.hadoop.hbase.procedure.MasterProcedureManager 过程管理器的逗号分隔列表。程序通过其签名进行标识，用户可以使用签名和即时名称来触发全局程序的执行。在实现你自己的 MasterProcedureManager 之后，把它放在 HBase 的类路径中，并在这里添加完全限定的类名称。

默认: none

```
hbase.coordinated.state.manager.class
```

协调状态管理员的完全合格的名字。

默认: `org.apache.hadoop.hbase.coordination.ZkCoordinatedStateManager`

```
hbase.regionserver.storefile.refresh.period
```

用于刷新辅助区域的存储文件的时间段（以毫秒为单位）。0 意味着此功能被禁用。辅助区域在次要区域刷新区域中的文件列表时会看到来自主要文件的新文件（来自刷新和压缩）（没有通知机制）。但是频繁刷新可能会导致额外的 Namenode 压力。如果文件的刷新时间不能超过 HFile TTL（hbase.master.hfilecleaner.ttl），请求将被拒绝。此设置还建议将 HFile TTL 配置为较大的值。

默认: `0`

```
hbase.region.replica.replication.enabled
```

是否启用对辅助区域副本的异步 WAL 复制。如果启用了此功能，则会创建一个名为“region_replica_replication”的复制对等项，它将对日志进行尾随处理，并将突变复制到区域复制大于 1 的区域复制的区域复制。如果启用一次，禁用此复制也需要禁用复制对等使用 shell 或 Admin java 类。复制到辅助区域副本可以在标准群集间复制上工作。

默认: `false`

```
hbase.http.filter.initializers
```

一个以逗号分隔的类名列表。列表中的每个类都必须扩展 org.apache.hadoop.hbase.http.FilterInitializer。相应的过滤器将被初始化。然后，过滤器将应用于所有面向 jsp 和 servlet 网页的用户。列表的排序定义了过滤器的排序。默认的 StaticUserWebFilter 添加 hbase.http.staticuser.user 属性定义的用户主体。

默认: `org.apache.hadoop.hbase.http.lib.StaticUserWebFilter`

```
hbase.security.visibility.mutations.checkauths
```

如果启用此属性，将检查可见性表达式中的标签是否与发出突变的用户相关联

默认: `false`

```
hbase.http.max.threads
```

HTTP 服务器将在其 ThreadPool 中创建的最大线程数。

默认: `16`

```
hbase.replication.rpc.codec
```

启用复制时要使用的编解码器，以便标签也被复制。这与支持标签的 HFileV3 一起使用。如果标签未被使用或者所使用的 hfile 版本是 HFileV2，则可以使用 KeyValueCodec 作为复制编解码器。请注意，在没有标签时使用 KeyValueCodecWithTags 进行复制不会造成任何伤害。

默认: `org.apache.hadoop.hbase.codec.KeyValueCodecWithTags`

```
hbase.replication.source.maxthreads
```

任何复制源将用于并行传送编辑到接收器的最大线程数。这也限制了每个复制批次被分解成的块的数量。较大的值可以提高主群集和从群集之间的复制吞吐量。默认值为 10，很少需要改变。

默认: `10`

```
hbase.http.staticuser.user
```

要在呈现内容时在静态网页过滤器上过滤的用户名称。一个示例使用是 HDFS Web UI（用于浏览文件的用户）。

默认: `dr.stack`

```
hbase.regionserver.handler.abort.on.error.percent
```

区域服务器 RPC 线程的百分比无法中止 RS。-1 表示禁用中止；0 表示即使单个处理程序已经死亡也会中止；0.x 表示只有当这个百分比的处理程序死亡时才中止；1 表示只中止所有的处理程序已经死亡。

默认: `0.5`

```
hbase.mob.file.cache.size
```

要缓存的已打开文件处理程序的数量。更大的值将通过为每个移动文件缓存提供更多的文件处理程序来减少频繁的文件打开和关闭，从而有利于读取。但是，如果设置得太高，则可能导致“打开的文件处理程序太多”。默认值为 1000。

默认: `1000`

```
hbase.mob.cache.evict.period
```

mob 高速缓存驱逐高速缓存的 mob 文件之前的时间（秒）。默认值是 3600 秒。

默认: `3600`

```
hbase.mob.cache.evict.remain.ratio
```

当缓存的移动文件数量超过 hbase.mob.file.cache.size 时，触发驱逐后保留的文件的比率（介于 0.0 和 1.0 之间）会被触发。默认值是 0.5f。

默认: `0.5f`

```
hbase.master.mob.ttl.cleaner.period
```

ExpiredMobFileCleanerChore 运行的时间段。该单位是秒。默认值是一天。MOB 文件名仅使用文件创建时间的日期部分。我们使用这个时间来决定文件的 TTL 到期时间。所以删除 TTL 过期的文件可能会被延迟。最大延迟可能是 24 小时。

默认: `86400`

```
hbase.mob.compaction.mergeable.threshold
```

如果一个 mob 文件的大小小于这个值，那么它被认为是一个小文件，需要在 mob compaction 中合并。默认值是 1280MB。

默认: `1342177280`

```
hbase.mob.delfile.max.count
```

mob 压缩中允许的最大 del 文件数。在 mob 压缩中，当现有的 del 文件的数量大于这个值时，它们被合并，直到 del 文件的数量不大于该值。默认值是 3。

默认: `3`

```
hbase.mob.compaction.batch.size
```

在一批 mob 压缩中所允许的 mob 文件的最大数量。mob 压缩合并小的 mob 文件到更大的。如果小文件的数量非常大，则可能导致合并中的“打开的文件处理程序太多”。合并必须分成批次。此值限制在一批 mob 压缩中选择的 mob 文件的数量。默认值是 100。

默认: `100`

```
hbase.mob.compaction.chore.period
```

MobCompactionChore 运行的时间。该单位是秒。默认值是一个星期。

默认: `604800`

```
hbase.mob.compactor.class
```

执行 mob compactor，默认一个是 PartitionedMobCompactor。

默认: `org.apache.hadoop.hbase.mob.compactions.PartitionedMobCompactor`

```
hbase.mob.compaction.threads.max
```

MobCompactor 中使用的最大线程数。

默认: `1`

```
hbase.snapshot.master.timeout.millis
```

主快照程序执行的超时。

默认: `300000`

```
hbase.snapshot.region.timeout
```

区域服务器将线程保持在快照请求池中等待超时。

默认: `300000`

```
hbase.rpc.rows.warning.threshold
```

批处理操作中的行数，超过该值将记录警告。

默认: `5000`

```
hbase.master.wait.on.service.seconds
```

默认是 5 分钟。做 30 秒的测试。有关上下文，请参见 HBASE-19794。

默认: `30`

## `hbase-env.sh`

hbase-env.sh 文件用来设置 HBase 环境变量。比如包括在启动 HBase 守护程序（如堆大小和垃圾回收器配置）时传递 JVM 的选项。您还可以设置 HBase 配置、日志目录、niceness、ssh 选项，定位进程 pid 文件的位置等的配置。打开 *conf/hbase-env.sh* 文件并仔细阅读其内容。每个选项都有相当好的记录。如果希望在启动时由 HBase 守护进程读取，请在此处添加您自己的环境变量。

此处的更改将需要重启 HBase 才能注意到更改。

