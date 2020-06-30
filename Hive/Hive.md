## 简介

Hive首页地址：<https://cwiki.apache.org/confluence/display/Hive/Home>

Hive是建立在 Hadoop 上的数据仓库基础构架。它提供了一系列的工具，可以用来对数据进行**提取/转化/加载（ETL）**，这是一种可以存储、查询和分析存储在 Hadoop 中的大规模数据的机制。

Hive 定义了简单的类 SQL 查询语言，称为 Hive SQL（简称为**HQL**），它允许熟悉 SQL 的用户查询数据。同时，这个语言也允许熟悉 MapReduce 开发者的开发自定义的 mapper 和 reducer 来处理内建的 mapper 和 reducer 无法完成的复杂的分析工作。

Hive 与关系型数据库 SQL 略有所不同，但支持了绝大多数的语句，如 DDL、DML以及常见的聚合函数、连接哈寻、条件查询。

Hive 不适合用于 OLTP，也不提供实时查询功能。它最适合应用在基于大量不可变数据的批处理作业。

## Hive 系统架构

[Hive 架构](./Hive架构.md)

### 元数据（MetaStore）

## Hive 配置文件

`hive-env.sh`

`hive-site.xml`

`hive-log4j.properties`

## Hive 命令行

#### `hive --config`

#### [hive shell](./Hive命令行.md)

#### [Hive的内置服务](./Hive的内置服务.md)

### Hive 的启动方式

- **hive  命令行模式**

直接输入 `${HIVE_HOME}/bin/hive` 的执行程序，或者输入 `hive --service cli`

用于 Linux 平台命令查询

- **hive  Web UI**的启动方式

`hive --service hwi `

用于通过浏览器来访问 hive

- **hive  远程服务**(默认端口号10000) 启动方式

`hive --service hiveserver2  &`（& 表示后台运行）

用 java、python 等程序通过 jdbc 等驱动访问 hive，需要用这种启动方式

[HiveServer和HiveServer2的对比](./HiveServer.md)

## HiveQL（HQL）

#### [HQL 语法](./HiveQL.md)

#### [数据类型](./Hive数据类型.md)

#### [运算符](./Hive运算符.md)

#### [内置函数](./Hive函数.md)

#### 表

Hive中的表分为**内部表(MANAGED_TABLE)**和**外部表(EXTERNAL_TABLE)**。

- 内部表和外部表最大的区别
  - 内部表DROP时候**会删除**HDFS上的数据;
  - 外部表DROP时候**不会删除**HDFS上的数据;
- 内部表适用场景：Hive中间表、结果表、一般不需要从外部（如本地文件、HDFS上load数据）的情况。
- 外部表适用场景：源表，需要定期将外部数据映射到表中。
- 使用场景：
  - 每天将收集到的网站日志定期流入HDFS文本文件，一天一个目录；
  - 在Hive中建立外部表作为源表，通过添加分区的方式，将每天HDFS上的原始日志映射到外部表的天分区中；
  - 在外部表（原始日志表）的基础上做大量的统计分析，用到的中间表、结果表使用内部表存储，数据通过SELECT+INSERT进入内部表。

#### 表查询

单表查询

inner join

outer join

semi join

map join

子查询

视图

#### [HiveQL与SQL区别](./HiveQL与SQL区别.md)

## 数据表设计

## Hive 优化

[Hive性能优化](./Hive性能优化.md)

表分区Partitions

表存储桶buckets

表压缩

索引

执行计划

控制 Mapper、Reducer 数量

## 访问方式

#### [Hive Shell](./Hive命令行.md)

#### [Java JDBC API](./Hive_Java_API.md)

#### [Beeline](./Beeline.md)

## 自定义函数

## Hive 安全

### 认证

### 授权

### 权限模型

## 安装和配置

[Hive的安装配置](./Hive的安装配置.md)

## Hive的数据单元

Hive 没有专门的数据存储格式，也没有为数据建立索引，用户可以非常自由的组织 Hive 中的表，只需要在创建表的时候告诉 Hive 数据中的列分隔符和行分隔符，Hive 就可以解析数据。

Hive 中所有数据都存储在 HDFS 中，Hive 包含以下数据模型：`DataBase`，`Table`，`External Table`，`Partition`，`Bucket`。

- `DataBase/Schema`:数据库
- `Table`:Hive 中的 Table 和数据库中的 Table 在概念上是类似的，每一个 Table 在 Hive 中都有一个相应的目录存储数据。
- `Partition`:对应于数据库中的 Partition 列的密集索引，但是 Hive 中 Partition 的组织方式和数据库中的很不相同。在 Hive 中，表中的一个Partition 对应于表下的一个目录，所有的 Partition 的数据都存储在对应的目录中。
- `Bucket`:对指定列计算 hash，根据 hash 值切分数据，目的是为了并行，每一个 Bucket 对应一个文件。
- `External Table`:指向已经在 HDFS 中存在的数据，可以创建 Partition。它和 Table 在元数据的组织上是相同的，而实际数据的存储则有较大的差异。 

### 视图

和关系型数据库一样，Hive中也提供了视图的功能，注意Hive中视图的特性，和关系型数据库中的稍有区别：

- 只有逻辑视图，没有物化视图；
- 视图只能查询，不能 Load/Insert/Update/Delete 数据；
- 视图在创建时候，只是保存了一份元数据，当查询视图的时候，才开始执行视图对应的那些子查询；

### 分区

Hive中的表分区比较简单，就是将同一组数据放到同一个HDFS目录下，当查询中过滤条件指定了某一个分区值时候，只将该分区对应的目录作为Input，从而减少MapReduce的输入数据，提高查询效率。

## 使用

## 其他

### Hive 的日志

Hive的日志一般存放在操作系统本地的 `/tmp/${user.name}/hive.log` 中，该文件路径由 `$HIVE_HOME/conf/hive-log4j.properties` 中指定，可自己修改该路径。