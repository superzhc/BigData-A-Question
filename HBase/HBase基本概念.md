## HBase 架构

![52297ef305db9.jpg-500.2kB](./images/52297ef305db9.jpg)

### Client

- 包含访问 HBase 的接口，并维护 cache 来加快对 HBase 的访问
- 通过 RPC 机制和 Master，RegionServer 通信

#### Zookeeper

- 保证任何时候，集群中只有一个 Master
- 存储所有 Region 的寻址入口
- 实时监控 RegionServer 的上线和下线信息。并实时通知给 Master
- 存储 HBase 元数据信息
- HBase 中可以启动多个 HMaster，通过 Zookeeper 的 Master Election 机制保证总有一个 Master 运行

### Master

- 为 RegionServer 分配 region
- 负责 RegionServer 的负载均衡
- 发现挂掉的 RegionServer 并重新分配其上的 region
- 负责表的创建、删除等操作

由于 Master 只维护表和 region 的元数据，而不参与表数据 I/O 的过程，Master 下线仅导致所有的元数据的修改被冻结（无法创建删除表，无法修改表的 schema，无法进行 region 的负载均衡，无法处理 region 上下线，无法进行 region 的合并，唯一例外的是 region 的 split 可以正常进行，因为只有 region server 参与），表的数据读写还可以正常进行。因此 Master 下线短时间内对整个 HBase 集群没有影响

### RegionServer

- RegionServer 维护 region，处理这些 region 的 I/O 请求
- RegionServer 负责切分在运行过程中变得过大的 region
- RegionServer 提供了行锁

## HBase 数据模型

HBase 是一个稀疏、多维度、有序的映射表。

*这张表中每个单元是通过由**行键**、**列簇**、**列限定符**和**时间戳**组成的索引来标识的*。每个单元的值是一个未经解释的字符串，没有数据类型。当用户在表中存储数据时，每一行都有一个唯一的行键和任意多的列。

表的每一行由一个或多个列簇组成，一个列簇中可以包含任意多个列。在同一个表模式下，每行所包含的列簇是相同的，也就是说，列簇的个数与名称都是相同的，但是每一行中的每个列簇中列的个数可以不同，如图 1 所示。

![HBase数据模型示意](./images/5-1Z5091305564M.gif)

HBase 中的同一个列簇里面的数据存储在一起，列簇支持动态扩展，可以随时添加新的列，无须提前定义列的数量。所以，尽管表中的每一行会拥有相同的列簇，但是可能具有截然不同的列。正因为如此，对于整个映射表的每行数据而言，有些列的值就是空的，所以 HBase 的表是稀疏的。

HBase 执行更新操作时，并不会删除数据旧的版本，而是生成一个新的版本，原有的版本仍然保留。

用户可以对 HBase 保留的版本数量进行设置。在查询数据库的时候，用户可以选择获取距离某个时间最近的版本，或者一次获取所有版本。如果查询的时候不提供时间戳，那么系统就会返回离当前时间最近的那一个版本的数据。

HBase 提供了两种数据版本回收方式：一种是保存数据的最后个版本；另一种是保存最近一段时间内的版本，如最近一个月。

#### 数据模型的基本概念

HBase 中的数据被存储在表中，具有行和列，是一个多维的映射结构。

##### 1. 表（Table)

HBase采用表来组织数据，表由许多行和列组成，列划分为多个列簇。

##### 2. 行（Row)

在表里面，每一行代表着一个数据对象。每一行都是由一个**行键**（**Row Key**）和一个或者多个列组成的。

行键是用来表示唯一一行记录的**主键**，HBase的数据是按照RowKey的**字典顺序**进行全局排序的，所有的查询，这一个排序维度。

因为表的行是按照行键顺序来进行存储的，所以行键的设计相当重要。设计行键的一个重要原则就是相关的行键要存储在接近的位置，例如，设计记录网站的表时，行键需要将域名反转（例如，`org.apache.www`、`org.apache.mail`、`org.apache.jira`），这样的设计能使与 apache 相关的域名在表中存储的位置非常接近。

访问表中的行只有 3 种方式：

- 通过单个行键获取单行数据；
- 通过一个行键的区间来访问给定区间的多行数据；
- 全表扫描。

##### 3. 列（Column）

列由**列簇（Column Family）**和**列限定符（Column Qualifier）**联合标识，由`:`进行间隔，如 `family:qualifiero`

##### 4. 列簇（Column Family)

在定义 HBase 表的时候需要提前设置好列簇，表中所有的列都需要组织在列簇里面。列簇一旦确定后，就不能轻易修改，因为它会影响到 HBase 真实的物理存储结构，但是列簇中的列限定符及其对应的值可以动态增删。

表中的每一行都有相同的列簇，但是不需要每一行的列簇里都有一致的列限定符，所以说是一种稀疏的表结构，这样可以在一定程度上避免数据的冗余。

HBase 中的列簇是一些列的集合。一个列簇的所有列成员都有着相同的前缀，例如，`courses:history` 和 `courses:math` 都是列簇 `courses `的成员。`:`是列簇的分隔符，用来区分前缀和列名。列簇必须在表建立的时候声明，列随时可以新建。

##### 5. 列限定符（Column Qualifier）

列簇中的数据通过列限定符来进行映射。列限定符不需要事先定义，也不需要在不同行之间保持一致。列限定符没有特定的数据类型，以二进制字节来存储。

##### 6. 单元（Cell）

行键、列簇和列限定符一起标识一个单元，存储在单元里的数据称为单元数据，没有特定的数据类型，以二进制字节（Byte array）来存储。

##### 7. 时间戳（Timestamp）

默认情况下，每一个单元中的数据插入时都会用时间戳来进行版本标识。

读取单元数据时，如果时间戳没有被指定，则默认返回最新的数据；写入新的单元数据时，如果没有设置时间戳，则默认使用当前时间。每一个列簇的单元数据的版本数量都被 HBase 单独维护，默认情况下，HBase 保留 3 个版本数据。

## HBase 物理模型

### KeyValue 格式

```sh
${HBASE_HOME}/bin/hbase hfile -p -f /hbase/data/default/superz_demo/6073d66131d693a5f924c0f33751c917/baseinfo/8a433657a99042e5b964f2eca93c0575
```

![1563343211817](../images/1563343211817.png)

![KeyValue格式](../images/keyvalue.png)

### HFile 格式

```sh
${HBASE_HOME}/bin/hbase hfile -m -f /hbase/data/default/superz_demo/6073d66131d693a5f924c0f33751c917/baseinfo/8a433657a99042e5b964f2eca93c0575
```

![1563343270999](../images/1563343270999.png)

![HFilev1.png-66.6kB](../images/HFilev1.png)

![a112.png-80.5kB](../images/a112.png)

物理上，表示按列簇分开存储的，每个 Column Family 存储在 HDFS 上的一个单独文件中（因此将具有共同 I/O 特性的列放在一个 Column Family 中）

HBase 为每个值维护了多级索引，即：RowKey、Column Family，Column Name，Timestamp

Table 中的所有行都按照 RowKey 的字典序排列

Table 在行的方向上分割为多个 Region

![hbase è¡¨ç»æ.jpg-135.3kB](../images/hbase 表结构.jpg)

Region 按大小分割的，每个表开始只有一个 region，随着数据增多，region 不断增大，当增大到一个阈值的时候，region 就会等分成两个新的 region，之后会有越来越多的 region。

Region 是 HBase 中分布式存储和负载均衡的最小单元，Region 实际上是 RowKey 排序后的按规则分割的连续的存储空间，不同 Region 分布到不同 RegionServer 上

![17345043259.jpg-28kB](../images/17345043259.jpg)

Region 虽然是分布式存储的最小单元，但并不是存储的最小单元。

1. Region 由一个或者多个 Store 组成，每个 Store 保存一个 Column Family
2. 每个 Store 又由一个 MemStore 和  0 至多个 StoreFile 组成
3. MemStore 存储在内存中，StoreFile 存储在 HDFS 上

