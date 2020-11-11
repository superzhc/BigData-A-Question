# Hive 存储格式

Apache Hive 支持的存储格式主要有：

- TextFile
- SequenceFile
- RCFile
- ORC
- Parquet
- 自定义格式

在建表时使用如下语句来指定存储格式：

```sql
STORED AS (
    TextFile|RCFile|SequenceFile|AVRO|ORC|Parquet
)
```

## TextFile

每一行都是一条记录，每行都以换行符（`\n`）结尾。

数据不做压缩，磁盘开销大，数据解析开销大。可结合 Gzip、Bzip2 使用（系统自动检查，执行查询时自动解压），但使用这种方式，Hive 不会对数据进行切分，从而无法对数据进行并行操作。

> 注：Hive 的默认存储格式

## SequenceFile

Hadoop API 提供的一种二进制文件，以 `<key,value>` 的形式序列化到文件中，其具有使用方便、可分割、可压缩的特点。支持三种压缩选择：NONE, RECORD, BLOCK。 Record 压缩率低，一般建议使用 BLOCK 压缩。

## RCFile

存储方式：一种行列存储相结合的存储方式。首先，其将数据按行分块，保证同一个 record 在一个块上，避免读一个记录需要读取多个 block。其次，块数据列式存储，有利于数据压缩和快速的列存取。

压缩快，快速列存取

读取需要的列只需要读取每个 row group 的头部定义

读取全量数据的操作性能可能比 SequenceFile 没有明显的优势

### RCFile 的设计和实现

> RCFile 文件格式是 FaceBook 开源的一种 Hive 的文件存储格式，首先将表分为几个行组，对每个行组内的数据进行按列存储，每一列的数据都是分开存储，正是先水平划分，再垂直划分的理念

![img](images/20160830235545048)

- **在存储结构上**：HDFS 内 RCFile 的存储结构是首先对表进行行划分，分成多个行组。一个行组主要包括：16 字节的 HDFS 同步块信息，主要是为了区分一个 HDFS 块上的相邻行组；元数据的头部信息主要包括该行组内的存储的行数、列的字段信息等等；数据部分 RCFile 将每一行，存储为一列，将一列存储为一行，因为当表很大，字段很多的时候，往往只需要取出固定的一列就可以。在一般的列存储中，会将不同的列分开存储，这样在查询的时候会跳过某些列，但是有时候存在一个表的有些列不在同一个 HDFS 块上，所以在查询的时候，hive 重组列的过程会浪费很多 IO 开销。

  ![img](images/20160904195200078)

- **数据的布局**： 首先根据 HDFS 的结构，一个表可以由多个 HDFS 块构成。在每个 HDFS 块中，RCFile 以 row group 为基本单位组织数据，一个表多所有 row group 大小一致，一个 HDFS 块中可以包含多个 row group。每个 row group 包含三个部分，第一部分是 sync marker，用来区分一个 HDFS 块中两个连续多 row group。第二部分是 row group 的 metadata header，记录每个 row group 中有多少行数据，每个列数据有多少字节，以及每列一行数据的字节数。第三部分就是 row group 中的实际数据，这里是按列存储的。

- **数据的压缩**： 在 metadata header 部分用 RLE (Run Length Encoding) 方法压缩，因为对于记录每个列数据中一行数据的字节数是一样的，这些数字重复连续出现，因此用这种方法压缩比比较高。在压缩实际数据时，每列单独压缩。

- **数据的追加写**： RCFile 会在内存里维护每个列的数据，叫 column holder，当一条记录加入时，首先会被打散成多个列，人后追加到每列对应的 column holder，同时更新 metadata header 部分。可以通过记录数或者缓冲区大小控制内存中 column holder 的大小。当记录数或缓冲大小超过限制，就会先压缩 metadata header，再压缩 column holder，然后写到 HDFS。

- **数据的读取和惰性解压（lazy decompression）**：RCFile 在处理一个 row group 时，只会读取 metadata header 和需要的列，合理利用列存储在 I/O 方面的优势。而且即使在查询中出现的列技术读进内存也不一定会被解压缩，只有但确定该列数据需要用时才会去解压，也就是惰性解压（lazy decompression）。例如对于 select a from tableA where b = 1，会先解压 b 列，如果对于整个 row group 中的 b 列，值都不为 1，那么就没必要对这个 row group 对 a 列去解压，因为整个 row group 都跳过了。

- **row group 的大小**： row group 太小肯定是不能充分发挥列存储的优势，但是太大也会有问题。首先，论文中实验指出，当 row group 超过某一阈值时，很难再获得更高当压缩比。其次，row group 太大会降低 lazy decompression 带来的益处，还是拿 select a from tableA where b = 1 来说，如果一个 row group 里有一行 b = 1，我们还是要解压 a 列，从而根据 metadata header 中的信息找到 b = 1 的那行的 a 列的值，如果此时我们把 row group 设小一点，假设就设成 1，这样对于 b <> 1 的行都不需要解压 a 列。最后论文中给出一个一般性的建议，建议将 row group 设成 4MB。

## ORC

存储方式：数据按行分块每块按照列存储

压缩快、快速列存取

效率比 RCFile 高，是 RCFile 的改良版本

### 详解

![img](images/20160904201046983)

ORC File（Optimized Row Columnar file），对 RCFile 做了一些优化，克服 RCFile 的一些限制。和 RCFile 格式相比，ORC 格式有以下优点：

- 每个 task 只输出单个文件，这样可以减少 NameNode 的负载；
- 支持各种复杂的数据类型，比如： datetime, decimal, 以及一些复杂类型(struct, list, map, and union)；
- 在文件中存储了一些轻量级的索引数据；
- 基于数据类型的块模式压缩；
- integer 类型的列用游程编码(run-length encoding)；
- String 类型的列用字典编码(dictionary encoding)；
- 用多个互相独立的 RecordReaders 并行读相同的文件；
- 无需扫描 markers 就可以分割文件；
- 绑定读写所需要的内存；
- metadata 的存储是用 Protocol Buffers 的，所以它支持添加和删除一些列。

**存储结构上**

ORC File 在 RCFile 基础上引申出来 Stripe 和 Footer 等。每个 ORC 文件首先会被横向切分成多个 Stripe，而每个 Stripe 内部以列存储，所有的列存储在一个文件中，而且每个 stripe 默认的大小是 250MB，相对于 RCFile 默认的行组大小是 4MB，所以比 RCFile 更高效。

**文件结构**

ORC File 包含一组组的行数据，称为 stripes，除此之外，ORC File 的 file footer 还包含一些额外的辅助信息。在 ORC File 文件的最后，有一个被称为 postscript 的区，它主要是用来存储压缩参数及压缩页脚的大小。

在默认情况下，一个 stripe 的大小为 250MB。大尺寸的 stripes 使得从 HDFS 读数据更高效。

在 file footer 里面包含了该 ORC File 文件中 stripes 的信息，每个 stripe 中有多少行，以及每列的数据类型。当然，它里面还包含了列级别的一些聚合的结果，比如：count, min, max, and sum。

```
Postscripts中存储该表的行数，压缩参数，压缩大小，列等信息
Stripe Footer中包含该stripe的统计结果，包括Max，Min，count等信息
FileFooter中包含该表的统计结果，以及各个Stripe的位置信息
IndexData中保存了该stripe上数据的位置信息，总行数等信息
RowData以stream的形式保存了数据的具体信息
```

![这里写图片描述](images/20160904201433666)

**存储空间上**

ORC File 扩展了 RCFile 的压缩，除了Run-length（游程编码），引入了字典编码和Bit编码。采用字典编码，最后存储的数据便是字典中的值，每个字典值得长度以及字段在字典中的位置。至于Bit编码，对所有字段都可采用Bit编码来判断该列是否为null，如果为null则Bit值存为0，否则存为1，对于为null的字段在实际编码的时候不需要存储，也就是说字段若为null，是不占用存储空间的。

**Stripe结构**

每个Stripe都包含index data、row data以及stripe footer，Stripe footer包含流位置的目录，Row data在表扫描的时候会用到。Index data包含每列的最大和最小值以及每列所在的行。行索引里面提供了偏移量，它可以跳到正确的压缩块位置。通过行索引，可以在stripe中快速读取的过程中可以跳过很多行，尽管这个stripe的大小很大。在默认情况下，最大可以跳过10000行。因为可以通过过滤预测跳过很多行，因而可以在表的 secondary keys 进行排序，从而可以大幅减少执行时间。比如你的表的主分区是交易日期，那么你可以对次分区（state、zip code以及last name）进行排序。

Hive 读取数据的时候，根据FileFooter读出Stripe的信息，根据IndexData读出数据的偏移量从而读取出数据。

## Parquet

一个面向列的二进制文件格式。Parquet 对于大型查询的类型是高效的。对于扫描特定表格中的特定列的查询，Parquet 特别有用。

## 自定义格式

用户可以通过实现 inputformat 和 outputformat 来自定义输入输出格式

## 总结

### 存储格式对比

| 存储格式     | 存储方式                    | 特点                                                                                                                                                                                                                                  |
| ------------ | --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| TextFile     | 行存储                      | 存储空间消耗比较大，并且压缩的 text，无法分割和合并，查询的效率最低，可以直接存储，加载数据的速度最高                                                                                                                                 |
| SequenceFile | 行存储                      | 存储空间消耗最大，压缩的文件可以分割和合并，查询效率高，需要通过 text 文件转化来加载                                                                                                                                                  |
| RCFile       | 数据按行分块 每块按照列存储 | 存储空间最小，查询的效率最高，需要通过 text 文件转化来加载，加载的速度最低。压缩快 快速列存取。读记录尽量涉及到的 block 最少，读取需要的列只需要读取每个row group 的头部定义。读取全量数据的操作 性能可能比sequencefile没有明显的优势 |
| ORCFile      | 数据按行分块 每块按照列存储 | 压缩快,快速列存取 ,效率比rcfile高,是rcfile的改良版本                                                                                                                                                                                  |
| Parquet      | 列存储                      | 相对于 ORC，Parquet 压缩比较低，查询效率较低，不支持 update、insert 和 ACID，但是 Parquet 支持 Impala 查询引擎                                                                                                                        |
