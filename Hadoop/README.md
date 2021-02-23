<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-05-21 14:34:26
 * @LastEditTime : 2021-02-05 15:55:39
 * @Copyright 2021 SUPERZHC
-->
# Hadoop

Hadoop 是一个由 Apache 基金会所开发的分布式系统基础框架。

主要解决的是**海量数据的存储**和**海量数据的分析计算问题**

> 官网地址：<https://hadoop.apache.org/>
>
> Github地址：<https://github.com/apache/hadoop>

**Hadoop 的优势**

- **高可靠性**：Hadoop 底层维护多个数据副本，所以即使 Hadoop 某个计算元素或存储出现故障，也不会导致数据丢失
- **高扩展性**：在集群间分配任务数据，可方便的扩展数以千计的节点
- **高效性**：在 MapReduce 的思想下，Hadoop 是并行工作的，以加快任务处理速度
- **高容错性**：能够自动将失败的任务重新分配

> Hadoop1.x和Hadoop2.x区别?
>
> Hadoop1.x 中 MapReduce 同时处理业务逻辑运算和资源的调度，耦合性较大；在 Hadoop2.x 时代，增加了 Yarn 组件，单独负责资源调度，MapReduce 也只负责运算

## Hadoop 的安全模式

官方地址：<http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/SecureMode.html#Permissions_for_both_HDFS_and_local_fileSystem_paths>