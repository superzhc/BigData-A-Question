HDFS 的设计目标是把超大数据集存储到分布在网络中的多台普通商用计算机上，并且能够提供高可靠性和高吞吐量的服务。分布式文件系统要比普通磁盘文件系统复杂，因为它要引入网络编程，分布式文件系统要容忍节点故障也是一个很大的挑战。

### 设计前提和目标

- **专为存储超大文件而设计**：hdfs应该能够支持 GB 级别大小的文件；它应该能够提供很大的数据带宽并且能够在集群中拓展到成百上千个节点；它的一个实例应该能够支持千万数量级别的文件。
- **适用于流式的数据访问**：hdfs适用于批处理的情况而不是交互式处理；它的重点是保证高吞吐量而不是低延迟的用户响应
- **容错性**：完善的冗余备份机制
- **支持简单的一致性模型**：HDFS需要支持一次写入多次读取的模型，而且写入过程文件不会经常变化
- **移动计算优于移动数据**：HDFS提供了使应用计算移动到离它最近数据位置的接口
- **兼容各种硬件和软件平台**

### 不适合的场景

- **大量小文件**：文件的元数据都存储在 NameNode 内存中，大量小文件会占用大量内存。
- **低延迟数据访问**：hdfs是专门针对数据高吞吐量而设计的
- **多用户写入，任意修改文件**

### hdfs架构设计

HDFS主要由3个组件构成，分别是`NameNode`、`SecondaryNameNode`和`DataNode`，HDFS是以 `master/slave` 模式运行的，其中NameNode、SecondaryNameNode 运行在master节点，DataNode运行slave节点。

 [NameNode](Hadoop/HDFS/HDFS之NameNode.md) 

 [DataNode](Hadoop/HDFS/HDFS之DataNode.md) 

 [SecondaryNameNode](Hadoop/HDFS/HDFS之SecondaryNameNode.md) 

NameNode 和 DataNode 架构图

![img](../images/20150918165001520)

### HDFS中的沟通协议

所有的 HDFS 中的沟通协议都是基于`tcp/ip`协议，一个客户端通过指定的 tcp 端口与 NameNode 机器建立连接，并通过ClientProtocol 协议与 NameNode 交互。而 DataNode 则通过 DataNode Protocol 协议与 NameNode 进行沟通。HDFS 的 RCP (远程过程调用)对 ClientProtocol 和 DataNode Protocol 做了封装。按照 HDFS 的设计，NameNode 不会主动发起任何请求，只会被动接受来自客户端或 DataNode 的请求。

### 可靠性保证

可以允许 DataNode 失败。DataNode 会定期（默认3秒）的向 NameNode 发送心跳，若 NameNode 在指定时间间隔内没有收到心跳，它就认为此节点已经失败。此时，NameNode 把失败节点的数据（从另外的副本节点获取）备份到另外一个健康的节点。这保证了集群始终维持指定的副本数。

可以检测到数据块损坏。在读取数据块时，HDFS 会对数据块和保存的校验和文件匹配，如果发现不匹配，NameNode 同样会重新备份损坏的数据块。