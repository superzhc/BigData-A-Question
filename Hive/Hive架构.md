# Hive 架构

Hive 是建立在 Hadoop 上的数据仓库基础构架。它提供了一系列的工具，可以用来进行数据提取转化加载（ETL），这是一种可以存储、查询和分析存储在 Hadoop 中的大规模数据的机制。Hive 定义了简单的类 SQL 查询语言，称为 `HiveQL`，它允许熟悉 SQL 的用户查询数据。同时，这个语言也允许熟悉 MapReduce 开发者的开发自定义的 mapper 和 reducer 来处理内建的 mapper 和 reducer 无法完成的复杂的分析工作。

![Hive架构图](../images/0602-1.jpg)

Hive 的结构可以分为以下几部分：

- 用户接口：包括 CLI、Client、WUI
- 元数据存储：通常是存储在关系数据库中，如 mysql，derby；Hive 中的元数据包括表的名字，表的列和分区及其属性，表的属性（是否为外部表等），表的数据所在目录等。
- 解释器、编译器、优化器、执行器：完成 `HQL` 查询语句从词法分析、语法分析、编译、优化以及查询计划的生成；生成的查询计划存储在 HDFS 中，并在随后有 MapReduce 调用执行。
- Hadoop：用 HDFS 进行存储，利用 MapReducer 进行计算

Hive 通过给用户提供的一系列交互接口，接收到用户的指令(SQL)，使用自己的 Driver，结合元数据(MetaStore)，将这些指令翻译成 MapReduce，提交到 Hadoop 中执行，最后，将执行返回的结果输出到用户交互接口。

