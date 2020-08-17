## `hbase-site.xml` 和 `hbase-default.xml` 

在Hadoop中将特定于站点的HDFS配置添加到hdfs-site.xml文件，那么对于HBase，特定于站点的配置文件为 `conf/hbase-site.xml`。有关可配置属性的列表，请参见下面的HBase默认配置或查看src/main/resources的HBase源代码中的原始hbase-default.xml源文件。

并不是所有的配置选项都会将其发送到hbase-default.xml。一些配置只会出现在源代码中；因此识别这些更改的唯一方法是通过代码审查。

目前，这里的更改将需要为HBase重启集群来注意到这个变化。

## HBase 默认配置

以下文档是使用默认的 HBase 配置文件 `hbase-default.xml` 作为源生成的。

|参数|描述|默认值|
|----|-----|--------|
|`hbase.tmp.dir`| 这是本地文件系统上的临时目录。将此设置更改为指向比“/tmp”更持久的位置，这是java.io.tmpdir的常见解决方案，因为在重新启动计算机时清除了“/tmp”目录。| `${java.io.tmpdir}/hbase-${user.name}`|
|`hbase.rootdir`|这个目录是region servers共享的目录，HBase保持不变。该URL应该是“完全限定的”以包括文件系统的scheme。例如，要指定HDFS实例的"/hbase"目录，namenode运行在namenode.example.org的9000端口，请将此值设置为：hdfs：//namenode.example.org：9000 / hbase。默认情况下，我们会写$ {hbase.tmp.dir}，通常是/tmp - 所以改变这个配置，否则所有的数据在计算机重启时都会丢失。|`${hbase.tmp.dir}/hbase`|
|`hbase.cluster.distributed`| 群集所处的模式。对于独立模式，可能的值为false，对于分布式模式，可能的值为true。如果为false，启动将在一个JVM中一起运行所有HBase和ZooKeeper守护程序。|false|
|`hbase.zookeeper.quorum`| 使用逗号分隔的ZooKeeper集合中的服务器列表（这个配置应该被命名为hbase.zookeeper.ensemble）。例如，“host1.mydomain.com，host2.mydomain.com，host3.mydomain.com”。默认情况下，对于本地和伪分布式操作模式，将其设置为localhost。对于完全分布式安装，应将其设置为ZooKeeper集成服务器的完整列表。如果在hbase-env.sh中设置HBASE_MANAGES_ZK，这是hbase将作为群集启动/停止的一部分来启动/停止ZooKeeper的服务器列表。客户端，我们将把这个集合成员的列表，并把它与hbase.zookeeper.property.clientPort配置放在一起。并将其作为connectString参数传递给zookeeper构造函数。|localhost|
|`zookeeper.recovery.retry.maxsleeptime`|在重试 zookeeper操作之前的最大睡眠时间（以毫秒为单位），这里需要最大时间，以便睡眠时间不会无限增长。|60000|
| `hbase.local.dir`|将本地文件系统上的目录用作本地存储。|`${hbase.tmp.dir}/local/`|
|`hbase.master.port`|HBase Master应该绑定的端口。|16000|
|`hbase.master.info.port`|HBase Master Web UI的端口。如果您不想运行UI实例，请将其设置为-1。|16010|
|`hbase.master.info.bindAddress`|HBase Master Web UI的绑定地址|`0.0.0.0`|
|`hbase.master.logcleaner.plugins`|由LogsCleaner服务调用的BaseLogCleanerDelegate的逗号分隔列表。这些WAL清理是按顺序调用的。要实现您自己的BaseLogCleanerDelegate，只需将其放入HBase的类路径中，并在此添加完全限定的类名。始终在列表中添加上面的默认日志清理工具。|`org.apache.hadoop.hbase.master.cleaner.TimeToLiveLogCleaner,org.apache.hadoop.hbase.master.cleaner.TimeToLiveProcedureWALCleaner`|
|`hbase.master.logcleaner.ttl`|WAL在归档（{hbase.rootdir} / oldWALs）目录中保留多久，之后将由主线程清除。该值以毫秒为单位。|600000|
|`hbase.master.procedurewalcleaner.ttl`|程序WAL将在归档目录中保留多久，之后将由主线程清除。该值以毫秒为单位。|604800000|
|`hbase.master.hfilecleaner.plugins`|由HFileCleaner服务调用的BaseHFileCleanerDelegate的逗号分隔列表。这些HFile清理器按顺序调用。要实现您自己的BaseHFileCleanerDelegate，只需将其放入HBase的类路径中，并在此添加完全限定的类名。总是在列表中添加上面的默认日志清除程序，因为它们将被覆盖在hbase-site.xml中。|`org.apache.hadoop.hbase.master.cleaner.TimeToLiveHFileCleaner`|
|`hbase.master.infoserver.redirect`|Master是否监听Master Web UI端口（hbase.master.info.port）并将请求重定向到由Master和RegionServer共享的Web UI服务器。配置，当主服务区域（而不是默认）时是有意义的。|true|
|`hbase.master.fileSplitTimeout`|分割一个区域，在放弃尝试之前等待文件分割步骤需要多长时间。默认值：600000。这个设置在hbase-1.x中被称为hbase.regionserver.fileSplitTimeout。Split现在运行主端，因此重命名（如果找到'hbase.master.fileSplitTimeout'设置，将使用它来填充当前'hbase.master.fileSplitTimeout'配置。|600000|
|`hbase.regionserver.port`|HBase RegionServer绑定的端口。|16020|
|`hbase.regionserver.info.port`|HBase RegionServer Web UI的端口如果您不希望RegionServer UI运行，请将其设置为-1。|16030|
|`hbase.regionserver.info.bindAddress`|HBase RegionServer Web UI的地址|`0.0.0.0`|
|`hbase.regionserver.info.port.auto`|Master或RegionServer UI是否应搜索要绑定的端口。如果hbase.regionserver.info.port已被使用，则启用自动端口搜索。用于测试，默认关闭。 |false|
|`hbase.regionserver.handler.count`|在RegionServers上启动RPC Listener实例的计数。Master使用相同的属性来处理主处理程序的数量。太多的处理者可能会适得其反。使其成为CPU数量的倍数。如果主要是只读的，处理程序计数接近CPU计数做得很好。从CPU数量的两倍开始，并从那里调整。|30|
|`hbase.ipc.server.callqueue.handler.factor`|确定呼叫队列数量的因素。值为0表示在所有处理程序之间共享单个队列。值为1意味着每个处理程序都有自己的队列。|0.1|
|`hbase.ipc.server.callqueue.read.ratio`|将调用队列分成读写队列。指定的时间间隔（应该在0.0到1.0之间）将乘以调用队列的数量。值为0表示不分割调用队列，这意味着读取和写入请求将被推送到相同的一组队列中。低于0.5的值意味着将比写入队列更少的读取队列。值为0.5意味着将有相同数量的读写队列。大于0.5的值意味着将有更多的读队列而不是写入队列。值为1.0意味着除了一个之外的所有队列都用于发送读取请求。示例：假设调用队列的总数为10，则read.ratio为0意味着：10个队列将同时包含读/写请求。0.3的读取比例意味着：3个队列将只包含读取请求，7个队列将只包含写入请求。0.5的read.ratio表示：5个队列将只包含读取请求，5个队列将只包含写入请求。0.8的read.ratio意味着：8个队列将只包含读取请求，2个队列将只包含写入请求。1的read.ratio表示：9个队列将只包含读取请求，1个队列将只包含写入请求。|0|
|`hbase.ipc.server.callqueue.scan.ratio`|考虑到读取的调用队列的数量（根据调用队列的总数乘以callqueue.read.ratio计算），scan.ratio属性将把读取的调用队列拆分为小读取和长读取队列。低于0.5的值意味着长读队列比短读队列少。值为0.5意味着将有相同数量的短读取和长读取队列。大于0.5的值意味着将会有比长读取队列更多的长读取队列。值0或1表示使用同一组队列进行获取和扫描。示例：给定读取调用队列的总数为8，scan.ratio为0或1意味着：8个队列将包含长读请求和短读请求。0.3的scan.ratio表示：2个队列只包含长读请求，6个队列只包含短读请求。0.5的scan.ratio表示：4个队列只包含长读请求，4个队列只包含短读请求。0.8的scan.ratio意味着：6个队列只包含长读请求，2个队列只包含短读请求。|0|
|`hbase.regionserver.msginterval`|从RegionServer到Master的消息间隔（以毫秒为单位）。|3000|
|`hbase.regionserver.logroll.period`|无论有多少次编辑，我们将滚动提交日志的时间段。|3600000|
|`hbase.regionserver.logroll.errors.tolerated`|

  在触发服务器中止之前，我们将允许连续的WAL关闭错误的数量。如果在日志滚动过程中关闭当前WAL书写器失败，则设置为0将导致区域服务器中止。即使是一个很小的值（2或3）也会让区域服务器承担瞬间的HDFS错误。|2|
|`hbase.regionserver.hlog.reader.impl`|

  WAL文件读取器的实现。|`org.apache.hadoop.hbase.regionserver.wal.ProtobufLogReader`|
|`hbase.regionserver.hlog.writer.impl`|

  WAL文件编写器的实现。|`org.apache.hadoop.hbase.regionserver.wal.ProtobufLogWriter`
|`hbase.regionserver.global.memstore.size`|

  在新更新被阻止并刷新之前，区域服务器中所有存储区的最大大小。默认为堆的40％（0.4）。更新被阻止，强制刷新直到区域服务器中的所有内存大小都达到hbase.regionserver.global.memstore.size.lower.limit。此配置中的默认值已被故意留空，以便兑现旧的hbase.regionserver.global.memstore.upperLimit属性（如果存在）。没有默认值。||
|`hbase.regionserver.global.memstore.size.lower.limit`|

  强制刷新之前，区域服务器中所有存储区的最大大小。默认为hbase.regionserver.global.memstore.size（0.95）的95％。当由于内存限制而导致更新被阻塞时，此值的100％会导致最小可能的刷新。此配置中的默认值已被故意留空，以便兑现旧的hbase.regionserver.global.memstore.lowerLimit属性（如果存在）。没有默认值。||
|`hbase.systemtables.compacting.memstore.type`|

  确定用于系统表（如META，名称空间表等）的memstore的类型。默认情况下，NONE是类型，因此我们对所有系统表使用默认的memstore。如果我们需要为系统表使用压缩存储器，那么将这个属性设置为：BASIC / EAGE|NONE|
|`hbase.regionserver.optionalcacheflushinterval`|

  在自动刷新之前，编辑在内存中的最长时间。默认为1小时。将其设置为0将禁用自动刷新。|3600000|
|`hbase.regionserver.dns.interface`|

  区域服务器应从中报告其IP地址的网络接口的名称。|default|
|`hbase.regionserver.dns.nameserver`|

  域名服务器应使用的名称服务器（DNS）的主机名或IP地址，以确定主机用于通信和显示的主机名。|default|
|`hbase.regionserver.region.split.policy`|

  分割策略决定了一个区域应该何时拆分。当前可用的各种其他拆分策略是：BusyRegionSplitPolicy，ConstantSizeRegionSplitPolicy，DisabledRegionSplitPolicy，DelimitedKeyPrefixRegionSplitPolicy，KeyPrefixRegionSplitPolicy和SteppingSplitPolicy。DisabledRegionSplitPolicy会阻止手动区域分割。|`org.apache.hadoop.hbase.regionserver.SteppingSplitPolicy`
|`hbase.regionserver.regionSplitLimit`|

  限制区域数量，之后不再发生区域分割。这并不是硬性限制区域数量，而是作为区域服务商在一定限度之后停止分裂的指导方针。默认设置为1000。|1000|
|`zookeeper.session.timeout`|

  ZooKeeper会话超时（以毫秒为单位）。它使用两种不同的方式。首先，这个值用于HBase用来连接到集合的ZK客户端。当它启动一个ZK服务器时它也被HBase使用，并且它被作为'maxSessionTimeout'传递。请参阅<http://hadoop.apache.org/zookeeper/docs/current/zookeeperProgrammers.html#ch_zkSessions>。例如，如果HBase区域服务器连接到也由HBase管理的ZK集合，那么会话超时将是由此配置指定的。但是，连接到以不同配置管理的集成的区域服务器将受到该集合的maxSessionTimeout的限制。所以，尽管HBase可能会建议使用90秒，但是整体的最大超时时间可能会低于此值，并且会优先考虑。ZK目前的默认值是40秒，比HBase的低。|90000|
|`zookeeper.znode.parent`|

  ZooKeeper中用于HBase的Root ZNode。所有配置了相对路径的HBase的ZooKeeper文件都会在这个节点下。默认情况下，所有的HBase的ZooKeeper文件路径都被配置为一个相对路径，所以它们将全部进入这个目录下，除非被改变。|`/hbase`|
|`zookeeper.znode.acl.parent`|

  Root ZNode用于访问控制列表。|acl|
|`hbase.zookeeper.dns.interface`|

  ZooKeeper服务器应从中报告其IP地址的网络接口的名称。|default|
|`hbase.zookeeper.dns.nameserver`|

  名称服务器（DNS）的主机名或IP地址，ZooKeeper服务器应使用该名称服务器来确定主机用于通信和显示的主机名。|default|
|`hbase.zookeeper.peerport`|

  ZooKeeper同伴使用的端口进行彼此会话。有关更多信息，请参阅<http://hadoop.apache.org/zookeeper/docs/r3.1.1/zookeeperStarted.html#sc_RunningReplicatedZooKeeper>。|2888|
|`hbase.zookeeper.leaderport`|

  ZooKeeper用于leader选举的端口。有关更多信息，请参阅<http://hadoop.apache.org/zookeeper/docs/r3.1.1/zookeeperStarted.html#sc_RunningReplicatedZooKeeper>。|3888|
|`hbase.zookeeper.property.initLimit`|

  来自ZooKeeper的配置zoo.cfg的属性。初始同步阶段可以采用的时钟（ticks）周期数。|10|
|`hbase.zookeeper.property.syncLimit`|

  来自ZooKeeper的配置zoo.cfg的属性。发送请求和获取确认之间可以传递的时钟（ticks）数量。|5|
|`hbase.zookeeper.property.dataDir`|

  来自ZooKeeper的配置zoo.cfg的属性。快照存储的目录。|`${hbase.tmp.dir}/zookeeper`|
|`hbase.zookeeper.property.clientPort`|

  来自ZooKeeper的配置zoo.cfg的属性。客户端将连接的端口。|2181|
|`hbase.zookeeper.property.maxClientCnxns`|

  来自ZooKeeper的配置zoo.cfg的属性。限制由IP地址标识的单个客户端的并发连接数量（在套接字级别）可能会对ZooKeeper集合的单个成员产生影响。设置为高，以避免独立运行和伪分布式运行的zk连接问题。|300|
|`hbase.client.write.buffer`|

  BufferedMutator写入缓冲区的默认大小（以字节为单位）。一个更大的缓冲区需要更多的内存 - 在客户端和服务器端，因为服务器实例化传递的写入缓冲区来处理它 - 但更大的缓冲区大小减少了RPC的数量。对于估计使用的服务器端内存，计算：hbase.client.write.buffer * hbase.regionserver.handler.count|2097152|
|`hbase.client.pause`|

  一般客户端pause值。在运行失败的get，region lookup等的重试之前，主要用作等待的值。|100|
|`hbase.client.pause.cqtbe`|

  是否为CallQueueTooBigException（cqtbe）使用特殊的客户端pause。如果您观察到来自同一个RegionServer的频繁的CQTBE，并且其中的调用队列保持充满，则将此属性设置为比hbase.client.pause更高的值没有默认值。||
|`hbase.client.retries.number`|

  最大重试次数。用作所有可重试操作（如获取单元格值，启动行更新等）的最大值。重试间隔是基于hbase.client.pause的粗略函数。首先，我们在这段时间重试，但后来退后，我们很快就达到每十秒钟重试一次。请参阅HConstants＃RETRY_BACKOFF了解备份如何提升。改变这个设置和hbase.client.pause来适应你的工作负载。|15|
|`hbase.client.max.total.tasks`|

  单个HTable实例发送到集群的最大并发突变任务数。|100|
|`hbase.client.max.perserver.tasks`|

  单个HTable实例将发送到单个区域服务器的并发突变任务的最大数量。|2|
|`hbase.client.max.perregion.tasks`|

  客户端将维护到单个Region的最大并发突变任务数。也就是说，如果已经有hbase.client.max.perregion.tasks写入这个区域，那么新的放入将不会被发送到这个区域，直到一些写入完成。|1|
|`hbase.client.perserver.requests.threshold`|

  所有客户端线程（进程级别）中一个服务器的并发未决请求的最大数量。超过请求将立即抛出ServerTooBusyException，以防止用户的线程被占用和只被一个缓慢的区域服务器阻止。如果使用固定数量的线程以同步方式访问HBase，请将此值设置为与线程数量相关的适当值，这些值将对您有所帮助。|2147483647|
|`hbase.client.scanner.caching`|

  如果从本地，客户端内存中未提供，则在扫描程序上调用next时尝试获取的行数。此配置与hbase.client.scanner.max.result.size一起使用，可以有效地使用网络。缺省值默认为Integer.MAX_VALUE，这样网络将填充由hbase.client.scanner.max.result.size定义的块大小，而不受特定行数的限制，因为行的大小随表格的不同而不同。如果您事先知道扫描中不需要超过一定数量的行，则应通过扫描＃setCaching将此配置设置为该行限制。缓存值越高，扫描器的速度越快，但是会占用更多的内存，而当缓存空置时，下一次调用的时间可能会越来越长。请勿设置此值，以便调用之间的时间大于扫描器超时；即hbase.client.scanner.timeout.period|2147483647|
|`hbase.client.keyvalue.maxsize`|

  指定KeyValue实例的组合的最大允许大小。这是为保存在存储文件中的单个条目设置上限。由于它们不能被分割，所以有助于避免因为数据太大而导致地区不能被分割。将此设置为最大区域大小的一小部分似乎是明智的。将其设置为零或更少将禁用检查。|10485760|
|`hbase.server.keyvalue.maxsize`|

  单个单元格的最大允许大小，包括值和所有关键组件。值为0或更小将禁用检查。默认值是10MB。这是保护服务器免受OOM情况的安全设置。|10485760|

|`hbase.client.scanner.timeout.period`|

  客户端扫描程序的租期以毫秒为单位。|60000|

|`hbase.client.localityCheck.threadPoolSize`|

  |2|

|`hbase.bulkload.retries.number`|

  最大重试次数，这是在面对分裂操作时尝试原子批量加载的最大迭代次数，0意味着永不放弃。|10|

|`hbase.master.balancer.maxRitPercent`|

  平衡时转换区域的最大百分比。默认值是1.0。所以没有平衡器节流。如果将此配置设置为0.01，则意味着在平衡时转换中最多有1％的区域。那么当平衡时，集群的可用性至少为99％。|1.0|

|`hbase.balancer.period`|

  区域平衡器在主站运行的时间段。|300000|

|`hbase.normalizer.period`|

  区域标准化程序在主程序中运行的时段。|300000|

|`hbase.regions.slop`|

  如果任何区域服务器具有平均值+（平均\*斜率）区域，则重新平衡。StochasticLoadBalancer（默认负载均衡器）中此参数的默认值为0.001，其他负载均衡器（即SimpleLoadBalancer）中的默认值为0.2。|0.001|

|`hbase.server.thread.wakefrequency`|

  在两次搜索之间休息的时间（以毫秒为单位）。用作日志滚筒等服务线程的睡眠间隔。|10000|

|`hbase.server.versionfile.writeattempts`|

  在放弃之前重试尝试写入版本文件的次数。每个尝试都由hbase.server.thread.wake频率毫秒分隔。|3|

|`hbase.hregion.memstore.flush.size`|

  如果memstore的大小超过此字节数，Memstore将被刷新到磁盘。值由每个hbase.server.thread.wakefrequency运行的线程检查。|134217728|

|`hbase.hregion.percolumnfamilyflush.size.lower.bound.min`|

  如果使用了FlushLargeStoresPolicy，并且有多个列族，那么每当我们达到完全的memstore限制时，我们就会找出所有memstore超过“下限”的列族，只有在保留其他内存的同时刷新它们。默认情况下，“下限”将是“<hbase.hregion.memstore.flush.size/column_family_number>”，除非该属性的值大于该值。如果没有一个族的memstore大小超过下限，所有的memstore都将被刷新（就像往常一样）。|16777216|

|`hbase.hregion.preclose.flush.size`|

  如果我们关闭时某个区域的存储空间大于或等于这个大小，则可以运行“预先刷新（pre-flush）”来清除存储区，然后再放置区域关闭标记并使区域脱机。关闭时，在关闭标志下运行刷新以清空内存。在此期间，该地区处于离线状态，我们没有进行任何写入。如果memstore内容很大，则此刷新可能需要很长时间才能完成。这个预刷新是为了清理大部分的memstore，然后把关闭标志放到离线区域，这样在关闭标志下运行的刷新没有什么用处。|5242880|

|`hbase.hregion.memstore.block.multiplier`|

  如果memstore具有hbase.hregion.memstore.block.multiplier乘以hbase.hregion.memstore.flush.size个字节，则阻止更新。在更新通信高峰期间有用的防止失控的memstore。如果没有上限，memstore就会填满，当刷新生成的flush文件需要很长时间才能压缩或拆分。|4|

|`hbase.hregion.memstore.mslab.enabled`|

  启用MemStore-Local分配缓冲区，该功能可用于在繁重的写入负载下防止堆碎片。这可以减少在大堆停止全局GC pause的频率。|true|

|`hbase.hregion.max.filesize`|

  最大HFile大小。如果一个地区的HFiles的总和已经超过了这个数值，这个地区就会被分成两部分。|10737418240|

|`hbase.hregion.majorcompaction`|

  主要压缩之间的时间，以毫秒表示。设置为0可禁用基于时间的自动重要压缩。用户请求的和基于大小的主要压缩将仍然运行。这个值乘以hbase.hregion.majorcompaction.jitter，使压缩在一个给定的时间窗口内稍微随机的时间开始。默认值是7天，以毫秒表示。如果主要压缩导致您的环境中断，则可以将它们配置为在部署的非高峰时间运行，或者通过将此参数设置为0来禁用基于时间的主要压缩，并在cron作业或另一个外部机制。|604800000|

|`hbase.hregion.majorcompaction.jitter`|

  应用于hbase.hregion.majorcompaction的乘数会导致压缩发生在给定的时间量的任何一侧的hbase.hregion.majorcompaction。数字越小，压缩将越接近hbase.hregion.majorcompaction时间间隔。|0.50|

|`hbase.hstore.compactionThreshold`|

  如果任何一个Store中存在超过此数量的StoreFiles（每个MemStore刷新一个StoreFile），则会执行压缩以将所有StoreFile重写为单个StoreFile。较大的值会延迟压实，但是当压缩发生时，需要较长时间才能完成。|3|

|`hbase.hstore.flusher.count`|

  刷新线程的数量。用更少的线程，MemStore刷新将排队。随着线程数量的增加，刷新将并行执行，增加了HDFS的负载，并可能导致更多的压缩。|2|

|`hbase.hstore.blockingStoreFiles`|

  如果任何一个Store中存在超过此数量的StoreFiles（每次刷新MemStore时将写入一个StoreFile），则会阻止该区域的更新，直到压缩完成或超出hbase.hstore.blockingWaitTime。|16|

|`hbase.hstore.blockingWaitTime`|

  在达到hbase.hstore.blockingStoreFiles定义的StoreFile限制后，区域将阻止更新的时间。经过这段时间后，即使压缩尚未完成，该地区也将停止阻止更新。|90000|

|`hbase.hstore.compaction.min`|

  压缩可以运行之前，必须有符合进行压缩条件的最小StoreFiles数量。调整hbase.hstore.compaction.min的目标是避免使用太多的小型StoreFiles来压缩。如果将此值设置为2，则每次在Store中有两个StoreFiles时会导致轻微的压缩，这可能不合适。如果将此值设置得太高，则需要相应调整所有其他值。对于大多数情况下，默认值是适当的。在以前的HBase版本中，参数hbase.hstore.compaction.min被命名为hbase.hstore.compactionThreshold。|3|

|`hbase.hstore.compaction.max`|

  无论符合条件的StoreFiles的数量如何，将为单个次要压缩选择的StoreFiles的最大数量。有效地，hbase.hstore.compaction.max的值控制单个压缩完成所需的时间长度。将其设置得更大意味着更多的StoreFiles包含在压缩中。对于大多数情况下，默认值是适当的。|10|

|`hbase.hstore.compaction.min.size`|

  StoreFile（或使用ExploringCompactionPolicy时选择的StoreFiles）小于此大小将始终有资格进行轻微压缩。这个大小或更大的HFile通过hbase.hstore.compaction.ratio进行计算，以确定它们是否合格。由于此限制表示所有StoreFiles的“自动包含”限制小于此值，因此在需要刷新多个StoreFile（1-2 MB范围内的许多StoreFiles）的写入繁重环境中可能需要降低此值，因为每个StoreFile都将作为目标，对于压缩而言，所得到的StoreFile可能仍然在最小尺寸下，并且需要进一步的压缩。如果此参数降低，比率检查会更快地触发。这解决了在早期版本的HBase中看到的一些问题，但是在大多数情况下不再需要更改此参数。|134217728|

|`hbase.hstore.compaction.max.size`|

  StoreFile（或使用ExploringCompactionPolicy时选择的StoreFiles）大于此大小将被排除在压缩之外。提高hbase.hstore.compaction.max.size的效果较少，较大的StoreFiles不经常压缩。如果你觉得压缩过于频繁而没有太多好处，你可以尝试提高这个价值。默认值：LONG.MAX_VALUE的值，以字节表示。|9223372036854775807|

|`hbase.hstore.compaction.ratio`|

  对于轻微压缩，此比率用于确定大于hbase.hstore.compaction.min.size的给定StoreFile是否适合压缩。其作用是限制大型StoreFiles的压缩。hbase.hstore.compaction.ratio的值以浮点小数表示。一个很大的比例，如10，将产生一个大型的StoreFile。相反，低值（如0.25）会产生类似于BigTable压缩算法的行为，产生四个StoreFiles。推荐使用1.0到1.4之间的中等数值。在调整此值时，您要平衡写入成本与读取成本。提高价值（如1.4）会有更多的写入成本，因为你会压缩更大的StoreFiles。然而，在读取期间，HBase将需要通过更少的StoreFiles来完成读取。如果您不能利用Bloom过滤器，请考虑使用这种方法。否则，可以将此值降低到1.0以降低写入的背景成本，并使用Bloom过滤器来控制读取期间触摸的StoreFiles的数量。对于大多数情况下，默认值是适当的。|1.2F|

|`hbase.hstore.compaction.ratio.offpeak`|

  允许您设置不同（默认情况下，更积极）的比率，以确定在非高峰时段是否包含较大的StoreFiles。以与hbase.hstore.compaction.ratio相同的方式工作。仅当hbase.offpeak.start.hour和hbase.offpeak.end.hour也被启用时才适用。|5.0F|

|`hbase.hstore.time.to.purge.deletes`|

  使用未来的时间戳延迟清除标记的时间。如果未设置，或设置为0，则将在下一个主要压缩过程中清除所有删除标记（包括具有未来时间戳的标记）。否则，将保留一个删除标记，直到在标记的时间戳之后发生的主要压缩加上此设置的值（以毫秒为单位）。|0|

|`hbase.offpeak.start.hour`|

  非高峰时段开始，以0到23之间的整数表示，包括0和23之间的整数。设置为-1以禁用非高峰。|-1|

|`hbase.offpeak.end.hour`|

  非高峰时段结束，以0到23之间的整数表示，包括0和23之间的整数。设置为-1以禁用非高峰。|-1|

|`hbase.regionserver.thread.compaction.throttle`|

  有两个不同的线程池用于压缩，一个用于大型压缩，另一个用于小型压缩。这有助于保持精简表（如hbase：meta）的快速压缩。如果压缩度大于此阈值，则会进入大型压缩池。在大多数情况下，默认值是适当的。默认值：2 x hbase.hstore.compaction.max x hbase.hregion.memstore.flush.size（默认为128MB）。值字段假定hbase.hregion.memstore.flush.size的值与默认值相同。|2684354560|

|`hbase.regionserver.majorcompaction.pagecache.drop`|

  指定是否通过主要压缩删除读取/写入系统页面缓存的页面。将其设置为true有助于防止重大压缩污染页面缓存，这几乎总是要求的，特别是对于具有低/中等内存与存储率的群集。|true|

|`hbase.regionserver.minorcompaction.pagecache.drop`|

  指定是否通过较小的压缩删除读取/写入系统页面缓存的页面。将其设置为true有助于防止轻微压缩污染页面缓存，这对于内存与存储比率较低的群集或写入较重的群集是最有利的。当大部分读取位于最近写入的数据上时，您可能希望在中等到低写入工作负载下将其设置为false。|true|

|`hbase.hstore.compaction.kv.max`|

  刷新或压缩时要读取并批量写入的KeyValues的最大数量。如果你有较大的KeyValues，并且Out Of Memory Exceptions有问题，请将它设置得更低。|10|

|`hbase.storescanner.parallel.seek.enable`|

  在StoreScanner中启用StoreFileScanner并行搜索功能，该功能可以在特殊情况下减少响应延迟。|false|

|`hbase.storescanner.parallel.seek.threads`|

  如果启用了并行查找功能，则默认线程池大小。|10|

|`hfile.block.cache.size`|

  StoreFile使用的最大堆（-Xmx设置）分配给块缓存的百分比。默认值为0.4意味着分配40％。设置为0禁用，但不建议；您至少需要足够的缓存来保存存储文件索引。|0.4|

|`hfile.block.index.cacheonwrite`|

  这允许在索引被写入时将非根多级索引块放入块高速缓存中。|false|

|`hfile.index.block.max.size`|

  当多级块索引中叶级，中级或根级索引块的大小增长到这个大小时，块将被写出并启动一个新块。|131072|

|`hbase.bucketcache.ioengine`|

  在哪里存储bucketcache的内容。其中之一：offheap、文件或mmap。如果有文件，则将其设置为file(s)：PATH_TO_FILE。mmap意味着内容将在一个mmaped文件中。使用mmap：PATH_TO_FILE。没有默认值。||

|`hbase.bucketcache.size`|

  EITHER表示缓存的总堆内存大小的百分比（如果小于1.0），则表示BucketCache的总容量（兆字节）。|0.0|

|`hbase.bucketcache.bucket.sizes`|

  用于bucketcache的存储区大小的逗号分隔列表。可以是多种尺寸。列出从最小到最大的块大小。您使用的大小取决于您的数据访问模式。必须是256的倍数，否则当你从缓存中读取时，你会遇到“java.io.IOException：Invalid HFile block magic”。如果您在此处未指定任何值，那么您可以选取代码中设置的默认bucketsizes。没有默认值。||

|`hfile.format.version`|

  用于新文件的HFile格式版本。版本3添加了对hfiles中标签的支持（请参阅<http://hbase.apache.org/book.html#hbase.tags>）。另请参阅配置“hbase.replication.rpc.codec”。|3|

|`hfile.block.bloom.cacheonwrite`|

  为复合Bloom过滤器的内联块启用写入缓存。|false|

|`io.storefile.bloom.block.size`|

  复合Bloom过滤器的单个块（“chunk”）的字节大小。这个大小是近似的，因为Bloom块只能被插入到数据块的边界处，而每个数据块的key的个数也不相同。|131072|

|`hbase.rs.cacheblocksonwrite`|

  块完成后，是否应将HFile块添加到块缓存中。|false|

|`hbase.rpc.timeout`|

  这是为了让RPC层定义一个远程调用超时（毫秒）HBase客户端应用程序超时。它使用ping来检查连接，但最终会抛出TimeoutException。|60000|

|`hbase.client.operation.timeout`|

  操作超时是一个顶级的限制（毫秒），确保表格中的阻止操作不会被阻止超过这个限制。在每个操作中，如果rpc请求由于超时或其他原因而失败，则将重试直到成功或抛出RetriesExhaustedException。但是，如果总的阻塞时间在重试耗尽之前达到操作超时，则会提前中断并抛出SocketTimeoutException。|1200000|

|`hbase.cells.scanned.per.heartbeat.check`|

  在heartbeat检查之间扫描的单元格的数量。在扫描处理过程中会发生heartbeat检查，以确定服务器是否应该停止扫描，以便将heartbeat消息发送回客户端。heartbeat消息用于在长时间运行扫描期间保持客户端 |服务器连接的活动。较小的值意味着heartbeat检查将更频繁地发生，因此将对扫描的执行时间提供更严格的界限。数值越大意味着heartbeat检查发生的频率越低。|10000|

|`hbase.rpc.shortoperation.timeout`|

  这是“hbase.rpc.timeout”的另一个版本。对于集群内的RPC操作，我们依靠此配置为短操作设置短超时限制。例如，区域服务器试图向活动主服务器报告的短rpc超时可以更快地进行主站故障转移过程。|10000|

|`hbase.ipc.client.tcpnodelay`|

  在rpc套接字连接上设置没有延迟。|true|

|`hbase.regionserver.hostname`|

  这个配置适用于对HBase很熟悉的人：除非你真的知道你在做什么，否则不要设定它的价值。当设置为非空值时，这表示底层服务器的（面向外部）主机名。没有默认值。||

|`hbase.regionserver.hostname.disable.master.reversedns`|

  这个配置适用于对HBase很熟练的人：除非你真的知道你在做什么，否则不要设定它的价值。当设置为true时，regionserver将使用当前节点主机名作为服务器名称，HMaster将跳过反向DNS查找并使用regionserver发送的主机名。请注意，此配置和hbase.regionserver.hostname是互斥的。|false|

|`hbase.master.keytab.file`|

  用于登录配置的HMaster服务器主体的kerberos密钥表文件的完整路径。没有默认值。||

|`hbase.master.kerberos.principal`|

  “hbase/\_HOST@EXAMPLE.COM”，应该用来运行HMaster进程的Kerberos主体名称。主体名称的格式应为：user/hostname @ DOMAIN。如果使用“_HOST”作为主机名部分，它将被替换为正在运行的实例的实际主机名。没有默认值。||

|`hbase.regionserver.keytab.file`|

  用于登录配置的HRegionServer服务器主体的kerberos密钥表文件的完整路径。没有默认值。||

|`hbase.regionserver.kerberos.principal`|

  “hbase/\_HOST@EXAMPLE.COM”。应该用来运行HRegionServer进程的kerberos主体名称。主体名称的格式应为：user/hostname @ DOMAIN。如果使用“_HOST”作为主机名部分，它将被替换为正在运行的实例的实际主机名。此主体的条目必须存在于hbase.regionserver.keytab.file中指定的文件中 没有默认值。||

|`hadoop.policy.file`|

  RPC服务器使用策略配置文件对客户端请求进行授权决策。仅在启用HBase安全性时使用。|`hbase-policy.xml`|

|`hbase.superuser`|

  用户或组列表（以逗号分隔），允许在整个集群中拥有完全权限（不管存储的ACL）。仅在启用HBase安全性时使用。没有默认值。||

|`hbase.auth.key.update.interval`|

  服务器中认证令牌的主密钥的更新间隔（以毫秒为单位）。仅在启用HBase安全性时使用。|86400000|

|`hbase.auth.token.max.lifetime`|

  验证令牌过期的最长生存时间（以毫秒为单位）。仅在启用HBase安全性时使用。|604800000|

|`hbase.ipc.client.fallback-to-simple-auth-allowed`|

  当客户端配置为尝试安全连接，但尝试连接到不安全的服务器时，该服务器可能会指示客户端切换到SASL SIMPLE（不安全）身份验证。此设置控制客户端是否接受来自服务器的此指令。如果为false（默认值），则客户端将不允许回退到SIMPLE身份验证，并会中止连接。|false|

|`hbase.ipc.server.fallback-to-simple-auth-allowed`|

  当服务器配置为需要安全连接时，它将拒绝来自使用SASL SIMPLE（不安全）身份验证的客户端的连接尝试。此设置允许安全服务器在客户端请求时接受来自客户端的SASL SIMPLE连接。如果为false（默认值），服务器将不允许回退到SIMPLE身份验证，并将拒绝连接。警告：只有在将客户端转换为安全身份验证时，才应将此设置用作临时措施。必须禁止它才能进行安全操作。|false

|`hbase.display.keys`|

  当它被设置为true时，webUI等将显示所有开始/结束键作为表格细节，区域名称等的一部分。当这被设置为假时，键被隐藏。|true|

|`hbase.coprocessor.enabled`|

  启用或禁用协处理器加载。如果'false'（禁用），任何其他协处理器相关的配置将被忽略。|true|

|`hbase.coprocessor.user.enabled`|

  启用或禁用用户（又名表）协处理器加载。如果'false'（禁用），则表格描述符中的任何表协处理器属性将被忽略。如果“hbase.coprocessor.enabled”为“false”，则此设置无效。|true|

|`hbase.coprocessor.region.classes`||

  在所有表上默认加载的区域观察者或端点协处理器的逗号分隔列表。对于任何覆盖协处理器方法，这些类将按顺序调用。在实现自己的协处理器之后，将其添加到HBase的类路径中，并在此处添加完全限定的类名称。协处理器也可以通过设置HTableDescriptor或者HBase shell来按需加载。没有默认值。||

|`hbase.coprocessor.master.classes`|

  在活动的HMaster进程中默认加载的org.apache.hadoop.hbase.coprocessor.MasterObserver协处理器的逗号分隔列表。对于任何实施的协处理器方法，列出的类将按顺序调用。在实现你自己的MasterObserver之后，把它放在HBase的类路径中，并在这里添加完全限定的类名称。没有默认值。||

|`hbase.coprocessor.abortonerror`|

  如果协处理器加载失败，初始化失败或引发意外的Throwable对象，则设置为true将导致托管服务器（主服务器或区域服务器）中止。将其设置为false将允许服务器继续执行，但所涉及的协处理器的系统范围状态将变得不一致，因为它只能在一部分服务器中正确执行，所以这对于仅调试是非常有用的。|true|

|`hbase.rest.port`|

  HBase REST服务器的端口。|8080|

|`hbase.rest.readonly`|

  定义REST服务器将启动的模式。可能的值有：false：此时，所有的HTTP方法都是允许的 |GET / PUT / POST / DELETE。true：此时只允许GET方法。|false|

|`hbase.rest.threads.max`|

  REST服务器线程池的最大线程数。池中的线程被重用来处理REST请求。这将控制同时处理的最大请求数。这可能有助于控制REST服务器使用的内存以避免OOM问题。如果线程池已满，则传入的请求将排队并等待一些空闲的线程。|100

|`hbase.rest.threads.min`|

  REST服务器线程池的最小线程数。线程池总是至少有这么多的线程，所以REST服务器已经准备好为传入的请求提供服务。|2|

|`hbase.rest.support.proxyuser`|

  启用运行REST服务器以支持代理用户模式。|false|

|`hbase.defaults.for.version.skip`|

  设置为true可以跳过“hbase.defaults.for.version”检查。将其设置为true可以在除maven生成的另一侧之外的上下文中有用；即运行在IDE中。你需要设置这个布尔值为true以避免看到RuntimeException：“hbase-default.xml文件似乎是HBase（\ $ {hbase.version}）的旧版本，这个版本是XXX-SNAPSHOT”|false|

|`hbase.table.lock.enable`|

  设置为true以启用锁定zookeeper中的表以进行模式更改操作。从主服务器锁定表可以防止并发的模式修改损坏表状态。|true|

|`hbase.table.max.rowsize`|

  单行字节的最大大小（默认值为1 Gb），用于Get-ing或Scan'ning，不设置行内扫描标志。如果行大小超过此限制RowTooBigException被抛出到客户端。|1073741824|

|`hbase.thrift.minWorkerThreads`|

  线程池的“核心大小”。在每个连接上创建新线程，直到创建了许多线程。|16|

|`hbase.thrift.maxWorkerThreads`|

  线程池的最大大小。待处理的请求队列溢出时，将创建新线程，直到其号码达到此数字。之后，服务器开始丢弃连接。|1000|

|`hbase.thrift.maxQueuedRequests`|

  在队列中等待的最大等待节点连接数。如果池中没有空闲线程，则服务器将请求排队。只有当队列溢出时，才会添加新的线程，直到hbase.thrift.maxQueuedRequests线程。|1000|

|`hbase.regionserver.thrift.framed`|

  在服务器端使用Thrift TFramedTransport。对于thrift服务器，这是推荐的传输方式，需要在客户端进行类似的设置。将其更改为false将选择默认传输，当由于THRIFT-601发出格式错误的请求时，容易受到DoS的影响。|false|

|`hbase.regionserver.thrift.framed.max_frame_size_in_mb`|

  使用成帧传输时的默认帧大小，以MB为单位。|2|

|`hbase.regionserver.thrift.compact`|

  使用Thrift TCompactProtocol二进制序列化协议。|false|

|`hbase.rootdir.perms`|

  安全（kerberos）安装程序中根数据子目录的FS Permissions。主服务器启动时，会使用此权限创建rootdir，如果不匹配则设置权限。 

  |700|

|`hbase.wal.dir.perms`|

  安全（kerberos）安装程序中的根WAL目录的FS Permissions。当主服务器启动时，它将使用此权限创建WAL目录，如果不匹配则设置权限。|700|

|`hbase.data.umask.enable`|

  如果启用，则启用该文件权限应分配给区域服务器写入的文件|false|

|`hbase.data.umask`|

  当hbase.data.umask.enable为true时，应该用来写入数据文件的文件权限|000|

|`hbase.snapshot.enabled`|

  设置为true以允许taken/restored/cloned。|true|

|`hbase.snapshot.restore.take.failsafe.snapshot`|

  设置为true以在还原操作之前得到快照。所得到的快照将在失败的情况下使用，以恢复以前的状态。在还原操作结束时，此快照将被删除|true|

|`hbase.snapshot.restore.failsafe.name`|

  restore操作所采用的故障安全快照的名称。您可以使用{snapshot.name}，{table.name}和{restore.timestamp}变量根据要恢复的内容创建一个名称。|`hbase-failsafe-{snapshot.name}-{restore.timestamp}`|

|`hbase.server.compactchecker.interval.multiplier`|

  这个数字决定了我们扫描的频率，看是否需要压缩。通常情况下，压缩是在某些事件（如memstore flush）之后完成的，但是如果区域在一段时间内没有收到大量的写入，或者由于不同的压缩策略，则可能需要定期检查。检查之间的时间间隔是`hbase.server.compactchecker.interval.multiplier` 乘以 `hbase.server.thread.wakefrequency`。|1000|

|`hbase.lease.recovery.timeout`|

  在放弃之前，我们等待dfs lease的总恢复时间。|900000|

|`hbase.lease.recovery.dfs.timeout`|

  dfs恢复lease调用之间的时间间隔。应该大于namenode为datanode的一部分发出块恢复命令所需的时间总和；dfs.heartbeat.interval和主数据节点所花费的时间，在死数据节点上执行数据块恢复到超时；通常是dfs.client.socket-timeout。|64000|

|`hbase.column.max.version`|

  新的列族描述符将使用此值作为要保留的默认版本数。|1|

|`dfs.client.read.shortcircuit`|

  如果设置为true，则此配置参数启用short-circuit本地读取。|false|

|`dfs.domain.socket.path`|

  如果将dfs.client.read.shortcircuit设置为true，则这是一个UNIX域套接字的路径，该套接字将用于DataNode与本地HDFS客户端之间的通信。如果该路径中存在字符串“\_PORT”，则会被DataNode的TCP端口替换。请注意托管共享域套接字的目录的权限。|none|

|`hbase.dfs.client.read.shortcircuit.buffer.size`|

  如果未设置DFSClient配置dfs.client.read.shortcircuit.buffer.size，我们将使用此处配置的内容作为short-circuit读取默认直接字节缓冲区大小。DFSClient本机默认值是1MB；HBase保持HDFS文件的打开状态，所以文件块\*1MB的数量很快就开始累积起来，并由于直接内存不足而威胁OOME。所以，我们从默认设置下来。使它大于在HColumnDescriptor中设置的默认hbase块大小，通常是64k。|131072|

|`hbase.regionserver.checksum.verify`|

  如果设置为true（默认），HBase将验证hfile块的校验和。当HBase写出hfiles时，HBase将校验和写入数据。HDFS（在此写入时）将校验和写入单独的文件，而不是需要额外查找的数据文件。设置这个标志可以节省一些I/O。设置此标志时，HDFS的校验和验证将在hfile流内部禁用。如果hbase-checksum验证失败，我们将切换回使用HDFS校验和（所以不要禁用HDFS校验！除此功能外，还适用于hfiles，而不适用于WAL）。如果这个参数设置为false，那么hbase将不会验证任何校验和，而是取决于HDFS客户端中的校验和验证。|true|

|`hbase.hstore.bytes.per.checksum`|

  新创建的校验和块中的字节数，用于hfile块中的HBase级校验和。|16384|

|`hbase.hstore.checksum.algorithm`|

  用于计算校验和的算法的名称。可能的值是NULL，CRC32，CRC32C。|CRC32C|

|`hbase.client.scanner.max.result.size`|

  调用扫描器的下一个方法时返回的最大字节数。请注意，当单个行大于此限制时，行仍然完全返回。默认值是2MB，这对于1ge网络是有好处的。有了更快和/或更高的延迟网络，这个值应该增加。|2097152|

|`hbase.server.scanner.max.result.size`|

  调用扫描器的下一个方法时返回的最大字节数。请注意，当单个行大于此限制时，行仍然完全返回。默认值是100MB。这是保护服务器免受OOM情况的安全设置。|104857600|

|`hbase.status.published`|

  该设置激活了主控发布区域服务器的状态。当一台区域服务器死亡并开始恢复时，主服务器会将这些信息推送到客户端应用程序，让他们立即切断连接，而不是等待超时。|false|

|`hbase.status.publisher.class`|

  用multicast消息实现状态发布。|`org.apache.hadoop.hbase.master.ClusterStatusPublisher$MulticastPublisher`|

|`hbase.status.listener.class`|

  使用multicast消息实现状态监听器。|`org.apache.hadoop.hbase.client.ClusterStatusListener$MulticastListener`|

|`hbase.status.multicast.address.ip`|

  用于multicase状态发布的multicase地址。|`226.1.1.3`|

|`hbase.status.multicast.address.port`|

  用于multicase状态发布的multicase端口。|16100|

|`hbase.dynamic.jars.dir`|

  自定义过滤器JAR的目录可以由区域服务器动态加载，而无需重新启动。但是，已加载的过滤器/协处理器类将不会被卸载。不适用于协处理器。|`${hbase.rootdir}/lib`|

|`hbase.security.authentication`|

  控制是否为HBase启用安全身份验证。可能的值是“simple”（不认证）和“Kerberos”。|simple|

|`hbase.rest.filter.classes`|

  用于REST服务的Servlet过滤器。|`org.apache.hadoop.hbase.rest.filter.GzipFilter`|

|`hbase.master.loadbalancer.class`|

  用于在期间发生时执行区域平衡的类。它将DefaultLoadBalancer替换为默认值（因为它被重命名为SimpleLoadBalancer ）。|`org.apache.hadoop.hbase.master.balancer.StochasticLoadBalancer`|

|`hbase.master.loadbalance.bytable`|

  平衡器运行时的因子表名称。|false|

|`hbase.master.normalizer.class`|

  用于执行期间发生时的区域标准化的类。|`org.apache.hadoop.hbase.master.normalizer.SimpleRegionNormalizer`|

|`hbase.rest.csrf.enabled`|

  设置为true以启用对跨站点请求forgery（CSRF）的保护。|false|

|`hbase.rest-csrf.browser-useragents-regex`|

  通过将hbase.rest.csrf.enabled设置为true来启用为REST服务器，针对跨站点请求伪造（CSRF）的防护时，用于匹配HTTP请求的User-Agent标头的正则表达式的逗号分隔列表。如果传入的用户代理与这些正则表达式中的任何一个相匹配，则认为该请求被浏览器发送，因此CSRF预防被强制执行。如果请求的用户代理与这些正则表达式中的任何一个都不匹配，则该请求被认为是由除浏览器以外的其他东西发送的，例如脚本自动化。在这种情况下，CSRF不是一个潜在的攻击向量，所以预防没有被执行。这有助于实现与尚未更新以发送CSRF预防报头的现有自动化的向后兼容性。|Mozilla.\*\*\,Opera.\*\*|

|`hbase.security.exec.permission.checks`|

  如果启用此设置，并且基于ACL的访问控制处于活动状态（AccessController协处理器作为系统协处理器安装，或作为表协处理器安装在表上），则必须授予所有相关用户EXEC权限（如果需要执行协处理器端点调用。像任何其他权限一样，EXEC权限可以在全局范围内授予用户，也可以授予每个表或命名空间的用户。有关协处理器端点的更多信息，请参阅HBase联机手册的协处理器部分。有关使用AccessController授予或撤消权限的更多信息，请参阅HBase联机手册的安全性部分。|false|

|`hbase.procedure.regionserver.classes`|

  在活动HRegionServer进程中默认加载的org.apache.hadoop.hbase.procedure.RegionServerProcedureManager过程管理器的逗号分隔列表。生命周期方法（init / start / stop）将由活动的HRegionServer进程调用，以执行特定的全局barriered过程。在实现你自己的RegionServerProcedureManager之后，把它放在HBase的类路径中，并在这里添加完全限定的类名称。||

|`hbase.procedure.master.classes`|

  在活动HMaster进程中默认加载的`org.apache.hadoop.hbase.procedure.MasterProcedureManager` 过程管理器的逗号分隔列表。程序通过其签名进行标识，用户可以使用签名和即时名称来触发全局程序的执行。在实现你自己的MasterProcedureManager之后，把它放在HBase的类路径中，并在这里添加完全限定的类名称。||

|`hbase.coordinated.state.manager.class`|

  协调状态管理员的完全合格的名字。|`org.apache.hadoop.hbase.coordination.ZkCoordinatedStateManager`|

|`hbase.regionserver.storefile.refresh.period`|

  用于刷新辅助区域的存储文件的时间段（以毫秒为单位）。0意味着此功能被禁用。辅助区域在次要区域刷新区域中的文件列表时会看到来自主要文件的新文件（来自刷新和压缩）（没有通知机制）。但是频繁刷新可能会导致额外的Namenode压力。如果文件的刷新时间不能超过HFile TTL（hbase.master.hfilecleaner.ttl），请求将被拒绝。此设置还建议将HFile TTL配置为较大的值。|0|

|`hbase.region.replica.replication.enabled`|

  是否启用对辅助区域副本的异步WAL复制。如果启用了此功能，则会创建一个名为“region_replica_replication”的复制对等项，它将对日志进行尾随处理，并将突变复制到区域复制大于1的区域复制的区域复制。如果启用一次，禁用此复制也需要禁用复制对等使用shell或Admin java类。复制到辅助区域副本可以在标准群集间复制上工作。|false|

|`hbase.http.filter.initializers`|

  一个以逗号分隔的类名列表。列表中的每个类都必须扩展org.apache.hadoop.hbase.http.FilterInitializer。相应的过滤器将被初始化。然后，过滤器将应用于所有面向jsp和servlet网页的用户。列表的排序定义了过滤器的排序。默认的StaticUserWebFilter添加hbase.http.staticuser.user属性定义的用户主体。|`org.apache.hadoop.hbase.http.lib.StaticUserWebFilter`|

|`hbase.security.visibility.mutations.checkauths`|

  如果启用此属性，将检查可见性表达式中的标签是否与发出突变的用户相关联|false|

|`hbase.http.max.threads`|

  HTTP服务器将在其ThreadPool中创建的最大线程数。|16|

|`hbase.replication.rpc.codec`|

  启用复制时要使用的编解码器，以便标签也被复制。这与支持标签的HFileV3一起使用。如果标签未被使用或者所使用的hfile版本是HFileV2，则可以使用KeyValueCodec作为复制编解码器。请注意，在没有标签时使用KeyValueCodecWithTags进行复制不会造成任何伤害。|`org.apache.hadoop.hbase.codec.KeyValueCodecWithTags`|

|`hbase.replication.source.maxthreads`|

  任何复制源将用于并行传送编辑到接收器的最大线程数。这也限制了每个复制批次被分解成的块的数量。较大的值可以提高主群集和从群集之间的复制吞吐量。默认值为10，很少需要改变。|10|

|`hbase.serial.replication.waitingMs`|

  默认情况下，在复制中，我们不能确定slave集群中的操作顺序与master集群中的顺序相同。如果将REPLICATION_SCOPE设置为2，我们将按照写入顺序进行编辑。这个配置是设置在下一次检查之前，我们将等待多长时间（以毫秒为单位），如果日志不能被推送，因为有一些日志写在它之前还没有被推入。较大的等待将减少hbase：meta上的查询数量，但会增加复制的延迟。此功能依赖于zk-less分配，因此用户必须将hbase.assignment.usezk设置为false来支持它。|10000|

|`hbase.http.staticuser.user`|

  要在呈现内容时在静态网页过滤器上过滤的用户名称。一个示例使用是HDFS Web UI（用于浏览文件的用户）。|dr.stack|

|`hbase.regionserver.handler.abort.on.error.percent`|

  区域服务器RPC线程的百分比无法中止RS。-1表示禁用中止；0表示即使单个处理程序已经死亡也会中止；0.x表示只有当这个百分比的处理程序死亡时才中止；1表示只中止所有的处理程序已经死亡。|0.5|

|`hbase.mob.file.cache.size`|

  要缓存的已打开文件处理程序的数量。更大的值将通过为每个移动文件缓存提供更多的文件处理程序来减少频繁的文件打开和关闭，从而有利于读取。但是，如果设置得太高，则可能导致“打开的文件处理程序太多”。默认值为1000。|1000|

|`hbase.mob.cache.evict.period`|

  mob高速缓存驱逐高速缓存的mob文件之前的时间（秒）。默认值是3600秒。|3600|

|`hbase.mob.cache.evict.remain.ratio`|

  当缓存的移动文件数量超过hbase.mob.file.cache.size时，触发驱逐后保留的文件的比率（介于0.0和1.0之间）会被触发。|0.5f|

|`hbase.master.mob.ttl.cleaner.period`|

  ExpiredMobFileCleanerChore运行的时间段。该单位是秒。默认值是一天。MOB文件名仅使用文件创建时间的日期部分。我们使用这个时间来决定文件的TTL到期时间。所以删除TTL过期的文件可能会被延迟。最大延迟可能是24小时。|86400|

|`hbase.mob.compaction.mergeable.threshold`|

  如果一个mob文件的大小小于这个值，那么它被认为是一个小文件，需要在mob compaction中合并。默认值是1280MB。|1342177280|

|`hbase.mob.delfile.max.count`|

  mob压缩中允许的最大del文件数。在mob压缩中，当现有的del文件的数量大于这个值时，它们被合并，直到del文件的数量不大于该值。默认值是3。|3|

|`hbase.mob.compaction.batch.size`|

  在一批mob压缩中所允许的mob文件的最大数量。mob压缩合并小的mob文件到更大的。如果小文件的数量非常大，则可能导致合并中的“打开的文件处理程序太多”。合并必须分成批次。此值限制在一批mob压缩中选择的mob文件的数量。默认值是100。|100|

|`hbase.mob.compaction.chore.period`|

  MobCompactionChore运行的时间。该单位是秒。默认值是一个星期。|604800|

|`hbase.mob.compactor.class`|

  执行mob compactor，默认一个是PartitionedMobCompactor。|`org.apache.hadoop.hbase.mob.compactions.PartitionedMobCompactor`|

|`hbase.mob.compaction.threads.max`|

  MobCompactor中使用的最大线程数。|1|

|`hbase.snapshot.master.timeout.millis`|

  主快照程序执行的超时。|300000|

|`hbase.snapshot.region.timeout`|

  区域服务器将线程保持在快照请求池中等待超时。|300000|

|`hbase.rpc.rows.warning.threshold`|

  批处理操作中的行数，超过该值将记录警告。|5000|

|`hbase.master.wait.on.service.seconds`|

  默认是5分钟。做30秒的测试。有关上下文，请参见HBASE-19794。|30|