## 简介

Hive 最适合数据仓库应用程序，使用该应用程序进行相关的静态数据分析，不需要快速响应给出结果，而且数据本身不会频繁变化。

Hive 不是一个完整的数据库，Hadoop 以及 HDFS 的**设计本身约束和局限性**地限制了 Hive 所能胜任的工作。其中最大的限制就是 **Hive不支持记录级别的更新、插入或者删除操作**，但是用户可以通过查询生成新表或者将查询结果导入到文件中。同时，因为 Hadoop 是一个面向批处理的系统，而 MapReduce 任务（job）的启动过程需要消耗较长的时间，所以 **Hive查询延时比较严重**。传统数据库中在秒级别可以完成的查询，在 Hive 中，即使数据集相对较小，往往也需要执行更长的时间。最后需要说明的是，**Hive不支持事务**。

Hive 是最适合数据仓库应用程序的，其可以维护海量数据，而且可以对数据进行挖掘，然后形成意见和报告等。

> Hive 提供了一个被称为 **Hive查询语言**（简称 HiveQL 或 HQL）的 SQL 方言来查询存储在 Hadoop 集群中的数据。但 HiveQL 并不符合 ANSI SQL 标准，其和 Oracle，Mysql，SQL Server 支持的常规 SQL 方言在很多方面存在差异（注：**HiveQL 和 Mysql 提供的 SQL 方言最接近**）

Hive 是一种建立在 Hadoop 文件系统上的数据仓库架构，并对存储在 HDFS 中的数据进行分析和管理；它可以将结构化的数据文件映射为一张数据库表，并提供完整的 SQL 查询功能，可以将 SQL 语句转换为 MapReduce 任务进行运行，通过自己的 SQL 去查询分析需要的内容，这套 SQL 简称 HiveQL（HQL），使不熟悉 MapReduce 的用户也能很方便地利用 SQL 语言对数据进行查询、汇总、分析。

## 元数据

所有的 Hive 客户端都需要一个 metastoreservice（元数据服务），Hive 使用这个服务来存储表模式信息和其他元数据信息。通常情况下会使用一个关系型数据库中的表来存储这些信息。默认情况下，Hive 会使用内置的Derby SQL服务器，其可以提供有限的、单进程的存储服务。

元数据存储中存储了如表的模式和分区信息等元数据信息。

## 配置参数

[配置参数](Hive配置参数.md)：

- Hive在HDFS中的存储路径配置
- 配置连接元数据存储库
- 配置HiveServer

### Hive 命令

`$HIVE_HOME/bin/hive` 这个shell命令是通向包括命令行界面也就是 CLI 等 Hive 服务的通道。

**命令选项**

如果用户执行下面的命令，那么可以查看到 hive 命令的一个简明说明的选项列表。

```sh
hive --help
Usage ./hive <parameters> --service serviceName <service parameters>
Service List: beeline cleardanglingscratchdir cli help hiveburninclient hiveserver2 hiveserver hwi jar lineage metastore metatool orcfiledump rcfilecat schemaTool version
Parameters parsed:
  --auxpath : Auxillary jars
  --config : Hive configuration directory
  --service : Starts specific service/component. cli is default
Parameters used:
  HADOOP_HOME or HADOOP_PREFIX : Hadoop install directory
  HIVE_OPT : Hive options
For help on a particular service:
  ./hive --service serviceName --help
Debug help:  ./hive --debug --help
```

需要注意 Service List 后面的内容。这里提供了几个服务，包括使用的CLI。用户可以通过 `--service name`服务名称来启用某个服务，尽管其中有几个服务也是有快捷启动方式的。

| 选项        | 名称         | 描述                                                         |
| ----------- | ------------ | ------------------------------------------------------------ |
| cli         | 命令行界面   | 用户定义表，执行查询等。如果没有指定服务（即`--service`），这个是默认的服务。 |
| hiveserver2 | Hive Server  | 监听来自于其他进程的Thrift连接的一个守护进程                 |
| hwi         | Hive Web界面 | 是一个可以执行查询语句和其他命令的简单的Web界面，这样可以不用登录到集群中的某台机器上使用CLI来进行查询 |
| jar         |              | hadoop jar命令的一个扩展， 这样可以执行需要Hive环境的应用    |
| metastore   |              | 启动一个扩展的Hive 元数据服务，可以供多客户端使用            |
| rcfilecat   |              | 一个可以打印出RCFile格式文件内容的工具                       |

[Hive命令行(CLI)](Hive命令行.md) 

## 原理

1. 用户提交查询等任务给 Driver
2. 编译器获得该用户的任务 Plan
3. 编译器 Compiler 根据用户任务去 MetaStore 中获取需要的 Hive 的元数据信息
4. 编译器 Compiler 得到元数据信息，对任务进行编译，先将 HiveQL 转换为抽象语法树，然后将抽象语法树转换为查询块，将查询块转化为逻辑的查询计划，重写逻辑查询计划，将逻辑计划转化为物理的计划（MapReduce），最后选择最佳的策略
5. 将最终的计划提交给 Driver
6. Driver 将计划 Plan 转交给 ExecutionEngine 去执行，获取元数据消息，提交给 JobTracker 或者 SourceManager 执行该任务，任务会直接读取 HDFS 中文件进行相应的操作
7. 获取执行的结果
8. 取得并返回执行结果