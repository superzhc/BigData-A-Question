# Hive 分区和分桶

## 分区

按照数据表的某列或某些列分为多个分区，分区从形式上可以理解为**文件夹**，比如我们要收集某个大型网站的日志数据，一个网站每天的日志数据存在同一张表上，由于每天会生成大量的日志，导致数据表的内容巨大，在查询时进行全表扫描耗费的资源非常多。那其实这个情况下，我们可以按照日期对数据表进行分区，不同日期的数据存放在不同的分区，在查询时只要指定分区字段的值就可以直接从该分区查找。分区是以字段的形式在表结构中存在，通过 `describe table` 命令可以查看到字段存在，但是该字段不存放实际的数据内容，仅仅是分区的标识。

### 创建分区

```sql
CREATE TABLE partition_table(
    userid BIGINT,
    name STRING COMMENT 'IP Address of the User')
PARTITIONED BY(age INT, day STRING)
STORED AS SEQUENCEFILE;
```

### 删除分区

```sql
ALERT TABLE <table_name> DROP PARTITION(col_name=col_value[,...])
```

### 查看分区相关信息

- 使用命令 `show partitions <table_name>;` 查看表的所有分区
- 使用 `desc formatted table_name partition(col_name=col_value[,...])` 查看该分区的详细信息，包括该分区在 HDFS 上的路径

### 添加分区

#### 静态分区

**使用 `INSERT` 添加分区**

往分区中追加数据：

```sql
INSERT INTO TABLE partition_table PARTITION (age=20,day ='03-02')
    SELECT id, name from partition_demo;
```

覆盖分区数据:

```sql
INSERT OVERWRITE TABLE partition_table PARTITION (age=20,day ='03-02')
    SELECT id, name from partition_demo;
```

**使用 `ALERT TABLE` 添加分区**

```sql
ALTER TABLE partition_table ADD PARTITION (age=20,day ='03-02') location 'hdfs://namenode/tmp/partition_table/age=20/day=03-02/';
```

#### 动态分区

Hive 提供了动态分区的功能，其可以基于查询参数推断出需要创建的分区的名称。

动态分区使用的分区字段值跟源表字段值之间的关系是根据位置而不是根据命名来匹配的。

用户可以混合使用动态和静态分区，但**静态分区键必须出现在动态分区键之前**。

动态分区功能默认情况下是没有开启的。开启后，默认是以`strict`模式执行的，在这种模式下要求至少有一列分区字段是静态的。这有助于因设计错误导致查询产生大量的分区，例如，用户可能错误地使用时间错作为分区字段，然后导致每秒都对应一个分区，而用户也许是期望按照天或者按照小时进行划分地。

动态分区属性，如下表：

| 属性名称                                   | 缺省值 | 描述                                                                                                                       |
| :----------------------------------------- | :----: | -------------------------------------------------------------------------------------------------------------------------- |
| `hive.exec.dynamic.partition`              | false  | 设置成true，表示开启动态分区功能                                                                                           |
| `hive.exec.dynamic.partition.mode`         | strict | 设置成nonstrict，表示允许所有分区都是动态                                                                                  |
| `hive.exec.max.dynamic.partitions.pernode` |  100   | 每个mapper或reducer可以创建的最大动态分区个数。如果某个mapper或reducer尝试创建大于这个值的分区的话则会抛出一个致命错误信息 |
| `hive.exec.max.dynamic.partitions`         | +1000  | 一个动态分区创建语句可以创建的最大动态分区个数。如果超过这个值则会抛出一个致命错误信息                                     |
| `hive.exec.max.created.files`              | 100000 | 全局可以创建的最大文件个数。有一个Hadoop计数器会跟踪记录创建了多少个文件，如果超过这个值则会抛出一个致命错误信息           |

**使用示例**

```sql
-- 设置为true标识开启动态分区功能（默认为false）
set hive.exec.dynamic.partition=true;
-- 设置为nonstrict，标识允许所有分区都是动态的（默认为strict）
set hive.exec.dynamic.partition.mode=nonstric;

-- insert overwrite是覆盖，insert into是追加
insert overwrite table xxxx.wy partition(age)
    select id, name, tel, age from xxxx.wy;
```

### 静态分区和动态分区的区别

静态分区与动态分区的主要区别在于静态分区是手动指定，而动态分区是通过数据来进行判断。详细来说：

**静态分区**

- 静态分区是在编译期间指定的指定分区名。
- 支持 load 和 insert 两种插入方式。
- 适用于分区数少，分区名可以明确的数据。

**动态分区**

- 根据分区字段的实际值，动态进行分区
- 是在 SQL 执行的时候进行分区
- 需要先将动态分区设置打开 `set hive.exec.dynamic.partition.mode=nonstrict`
- 只能用 insert 方式
- 通过普通表选出的字段包含分区字段，分区字段放置在最后，多个分区字段按照分区顺序放置。

## 分桶

对指定的列计算其 hash，根据 hash 值切分数据，目的是为了并行，每一个桶对应一个文件。

```sql
CREATE TABLE bucketed_user(id INT, name STRING) CLUSTERED BY (id) INTO 4 BUCKETS;
```

对于每一个表（table）或者分区，Hive 可以进一步组织成桶，也就是说桶是更为细粒度的数据范围划分。Hive 也是针对某一列进行桶的组织。Hive 采用对列值进行哈希，然后除以桶的个数求余的方式决定该条记录存放在哪个桶当中。把表（或者分区）组织成桶（Bucket）有两个理由：

1. 获得更高的查询处理效率
   桶为表加上了额外的结构，Hive 在处理有些查询时能够利用这个结构。具体而言，连接两个在（包含连接列的）相同列上划分了桶的表，可以使用 Map 端连接（Map-Side join）高效的实现。比如 join 操作，对于 join 操作的两个表有一个相同的列，如果对这两个表都进行了桶操作。那么将保存相同列值的桶进行 join 操作就可以，可以大大减少 join 的数据量。
2. 使取样（samping）更高效
   在处理大规模数据集时，在开发和修改查询的阶段，如果能在数据集的一小部分数据上试运行查询，会带来很多方便