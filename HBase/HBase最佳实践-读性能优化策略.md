## HBase 读优化

### HBase 客户端优化

#### Scan.setCaching 是否设置合理？

**优化原理**

通常一次 scan 会返回大量数据，因此客户端发起一次请求，实际上并不会一次就将所有数据加载到本地，而是分成多次 RPC 请求进行加载，这样设计一方面是因为大量数据请求可能会导致网络带宽严重损耗而影响其他业务，另一方面也有可能因为数据量太大导致本地客户端发生 OOM。在这样的设计体系下用户会首先加载一部分数据到本地，然后遍历处理，再加载下一部分数据到本地处理，如此往复，直到所有数据都加载完成。数据加载到本地就存放在 scan 缓存中，默认 100 条数据大小。

通常情况下，默认的 scan 缓存设置就可以正常工作的。但是一些大 scan（一次 scan 可能需要查询几万甚至几十万行数据）来说，每次请求 100 条数据意味着一次 scan 需要几百甚至 几千次 RPC 请求，这种交互的代价无疑是很大的。因此可以考虑将 scan 缓存设置增大，比如设为 500 或者 1000 就可能更加合适。

经一些实验表明，在一次 scan 扫描 10w+ 条数据量的条件下，将 scan 缓存从 100 增加到 1000，可以有效降低 scan 请求的总体延迟，延迟基本降低了 25% 左右。

**优化建议**

大 scan 场景下将 scan 缓存从 100 增大到 500 或者 1000，用以减少 RPC 次数

#### Get 是否使用批量请求？

**优化原理**

HBase 分别提供了单条 get 以及批量 get 的 API 接口，使用批量 get 接口可以减少客户端到 RegionServer 之间的 RPC 连接数，提高读取性能。另外需要注意的是，批量 get 请求要么成功返回所有请求数据，要么抛出异常。

**优化建议**

使用批量 get 进行读取请求

#### 请求是否可以显式指定列族或者列

**优化原理**

HBase 是典型的列族数据库，意味着同一列族的数据存储在一起，不同列族的数据分开存储在不同的目录下。如果一个表有多个列族，只是根据 RowKey 而不指定列族进行索引的话不同列族的数据需要独立进行检索，性能必然会比指定列族的查询差很多，很多情况下甚至会有2倍~3倍的性能损失

**优化建议**

可以指定列族或者列进行精确查找的尽量指定查找

#### 离线批量读取请求是否设置禁止缓存

**优化原理**

通常离线批量读取数据会进行一次性全表扫描，一方面数据量很大，另一方面请求只会执行一次。这种场景下如果使用 scan 默认设置，就会将数据从 HDFS 加载出来之后放到缓存。可想而知，大量数据进入缓存必将其他实时业务热点数据挤出，其他业务不得不从 HDFS 加载，进而会造成明显的读延迟毛刺。

**优化建议**

离线批量读取请求设置禁用缓存，`scan.setBlockCache(false)`

### Hbase 服务器优化

一般服务器端问题一旦导致业务读请求延迟较大的话，通常是集群级别的，即整个集群的业务都会反映读延迟较大。

#### 随机读请求是否均衡

**优化原理**

极端情况下假如所有的读亲求都落在一台 RegionServer 的某几个 Region 上，这一方面不能发挥整个集群的并发处理能力，另一方面势必造成此台 RegionServer 资源严重消耗（比如 IO 耗尽、handler 耗尽等），落在该台 RegioonServer 上的其他业务会因此受到很大的波及。可见，读请求不均衡不仅会造成本身业务性能很差，还会严重影响其它业务。当然，写请求不均衡也会造成类似的问题，可见负载不均衡是 HBase 中的严重问题

**观察确认**

观察所有 RegionServer 的读请求 QPS 曲线，确认是否存在读请求不均衡现象

**优化建议**

RowKey 必须进行散列化处理（比如 MD5 散列），同时建表必须进行预分区处理

#### BlockCache是否设置合理？

**优化原理**

BlockCache 作为读缓存，对于读性能来说至关重要。默认情况下 BlockCache 和 Memstore 的配置相对比较均衡（各占40%），可以根据集群业务进行修正，比如读多写少业务可以将 BlockCache 占比调大。另一方面，BlockCache的策略选择也很重要，不同策略对读性能来说影响并不是很大，但是对GC的影响却相当显著，尤其 BucketCache 的 offheap 模式下 GC 表现很优越。另外，HBase 2.0对offheap的改造（HBASE-11425）将会使HBase的读性能得到2～4倍的提升，同时GC表现会更好！

**观察确认**

观察所有 RegionServer 的缓存未命中率、配置文件相关配置项一级 GC 日志，确认 BlockCache 是否可以优化

**优化建议**

JVM内存配置量 < 20G，BlockCache策略选择LRUBlockCache；否则选择BucketCache策略的offheap模式。

#### HFile 文件是否太多？

**优化原理**

HBase 读取数据通常首先会到 Memstore 和 BlockCache 中检索（读取最近写入数据&热点数据），如果查找不到就会到文件中检索。HBase的类LSM结构会导致每个store包含多数HFile文件，文件越多，检索所需的IO次数必然越多，读取延迟也就越高。文件数量通常取决于Compaction的执行策略，一般和两个配置参数有关：`hbase.hstore.compactionThreshold`和`hbase.hstore.compaction.max.size`，前者表示一个store中的文件数超过多少就应该进行合并，后者表示参数合并的文件大小最大是多少，超过此大小的文件不能参与合并。这两个参数不能设置太’松’（前者不能设置太大，后者不能设置太小），导致Compaction合并文件的实际效果不明显，进而很多文件得不到合并。这样就会导致HFile文件数变多。

**观察确认**

观察 RegionServer 级别以及 Region 级别的 storefile 数，确认HFile文件是否过多

**优化建议**

`hbase.hstore.compactionThreshold`设置不能太大，默认是3个；设置需要根据Region大小确定，通常可以简单的认为`hbase.hstore.compaction.max.size = RegionSize / hbase.hstore.compactionThreshold`

#### Compaction 是否消耗系统资源过多？

**优化原理**

Compaction 是将小文件合并为大文件，提高后续业务随机读性能，但是也会带来 IO 放大以及带宽消耗问题（数据远程读取以及三副本写入都会消耗系统带宽）。正常配置情况下 Minor Compaction 并不会带来很大的系统资源消耗，除非因为配置不合理导致 Minor Compaction 太过频繁，或者 Region 设置太大情况下发生 Major Compaction。

**观察确认**

观察系统 IO 资源以及带宽资源使用情况，再观察 Compaction 队列长度，确认是否是由于 Compaction 导致系统资源消耗过多

**优化建议**

1. Minor Compaction设置：`hbase.hstore.compactionThreshold`设置不能太小，又不能设置太大，因此建议设置为5～6；`hbase.hstore.compaction.max.size = RegionSize / hbase.hstore.compactionThreshold`
2. Major Compaction设置：大Region读延迟敏感业务（ 100G以上）通常不建议开启自动Major Compaction，手动低峰期触发。小Region或者延迟不敏感业务可以开启Major Compaction，但建议限制流量；
3. 期待更多的优秀Compaction策略，类似于stripe-compaction尽早提供稳定服务

### HBase 列族设计优化

#### BloomFilter 过滤器是否设置？设置是否合理？

**优化原理**

Bloomfilter 主要用来过滤不存在待检索 RowKey 或者 Row-Col 的 HFile 文件，避免无用的 IO 操作。它会告诉你在这个 HFile 文件中是否可能存在待检索的 KV，如果不存在，就可以不用消耗 IO  打开文件进行  seek。很显然，通过设置 Bloomfilter 可以提升随机读写的性能。

Bloomfilter 取值有两个，row以及rowcol，需要根据业务来确定具体使用哪种。如果业务大多数随机查询仅仅使用 row 作为查询条件，Bloomfilter 一定要设置为 row，否则如果大多数随机查询使用 row+cf 作为查询条件，Bloomfilter 需要设置为 rowcol。如果不确定业务查询类型，设置为 row。

**优化建议**

任何业务都应该设置 Bloomfilter，通常设置为 row 就可以，除非确认业务随机查询类型为 row+cf，可以设置为 rowcol。

### HDFS 相关优化

HDFS 作为 HBase 最终数据存储系统，通常会使用三副本策略存储 HBase 数据文件以及日志文件。从 HDFS 的角度来看，HBase 既是它的客户端，HBase 通过调用它的客户端进行数据读写操作，因此 HDFS 的相关优化也会影响 HBase 的读写性能。

#### HDFS 数据本地化率是不是很低？

**数据本地率**

HDFS数据通常存储三份，假如当前RegionA处于Node1上，数据a写入的时候三副本为(Node1,Node2,Node3)，数据b写入三副本是(Node1,Node4,Node5)，数据c写入三副本(Node1,Node3,Node5)，可以看出来所有数据写入本地Node1肯定会写一份，数据都在本地可以读到，因此数据本地率是100%。现在假设RegionA被迁移到了Node2上，只有数据a在该节点上，其他数据（b和c）读取只能远程跨节点读，本地率就为33%（假设a，b和c的数据大小相同）。

**优化原理**

数据本地率太低很显然会产生大量的跨网络IO请求，必然会导致读请求延迟较高，因此提高数据本地率可以有效优化随机读性能。数据本地率低的原因一般是因为Region迁移（自动balance开启、RegionServer宕机迁移、手动迁移等）,因此一方面可以通过避免Region无故迁移来保持数据本地率，另一方面如果数据本地率很低，也可以通过执行major_compact提升数据本地率到100%。

**优化建议**

避免Region无故迁移，比如关闭自动balance、RS宕机及时拉起并迁回飘走的Region等；在业务低峰期执行major_compact提升数据本地率

#### Hedged Read 功能是否开启？

**优化原理**

HBase 数据在 HDFS 中一般都会存储三份，而且优先会通过 Short-Circuit Local Read 功能尝试本地读。但是在某些特殊情况下，有可能会出现因为磁盘问题或者网络问题引起的短时间本地读取失败，为了应对这类问题，社区开发者提出了补偿重试机制-Hedged Read。该机制基本工作原理为：客户端发起一个本地读，一旦一段时间之后还没有返回，客户端将会向其他 DataNode 发送相同数据的请求。哪一个请求先返回，另一个就会被丢弃。

**优化建议**

开启 Hedged Read 功能

#### Short-Circuit Local Read  功能是否开启？

**优化原理**

当前 HDFS 读取数据都需要经过 DataNode，客户端会向 DataNode 发送读取数据的请求，DataNode接受到请求之后从硬盘中将文件读出来，再通过 TCP 发送给客户端。Short-Circuit 策略允许客户端绕过 DataNode 直接读取 本地数据。

**优化建议**

开启 Short-Circuit Local Read  功能

## HBase 读性能优化归纳

读延迟较大无非三种常见的表象：

- 单个业务慢
- 集群随机读慢
- 某个业务随机读之后其他业务受到影响导致随机读延迟很大

将这些问题进行如下归纳：

![img](../images/in7uz6ldm2.jpeg)

![img](../images/9x2ozhb0f1.jpeg)

![img](../images/g2elht51oa.png)