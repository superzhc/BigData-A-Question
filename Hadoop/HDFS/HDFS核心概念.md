# HDFS 核心概念

## Block 数据块

在 HDFS 系统中，为了便于文件的管理和备份，引入分块概念（Block）。这里的**块是HDFS存储系统当中的最小单位**，HDFS 默认定义一个块的大小在 Hadoop1.0 中为 64MB，Hadoop2.0 中为 128MB。当有文件上传到 HDFS 上时，若文件大小大于设置的块大小，则该文件会被切分存储为多个块，多个块可以存放在不同的DataNode上。但值得注意的是如果某文件大小没有到达 64/128MB，该文件并不会占据整个块空间 。（例如当一个 1MB 的文件存储在 128MB 的块中，文件只使用1MB的硬盘空间，而不是 128MB）。

> 默认副本数为3。

## namenode

namenode 是管理文件系统的命名空间。它维护着文件系统树及所有的文件、目录的元数据。这些信息以两个文件形式永久保存在本地磁盘上：**fsimage文件**和**edits编辑日志文件**。NameNode 也记录着每个文件中各个块所在的数据节点信息，但它并不永久保存块的位置信息，因为这些信息会在系统启动时根据数据节点信息重建。

namenode 中仅仅存储目录树信息，而关于 Block 的位置信息则是从各个 datanode 上传到 namenode 上的。

Namenode 的目录树信息的物理存储在 fsimage 文件中的，当 Namenode 启动的时候会首先读取 fsimage 这个文件，将目录树信息装在到内存中。而 edits 存储的是日志信息，在 Namenode 启动后所有对目录结构的增加、删除、修改等操作都会记录到 edits 文件中，并不会同步的记录在 fsimage 中。

当 Namenode 节点关闭的时候，并不会将 fsimage 和 edits 文件进行合并，这个合并过程实际上发生在 Namenode 启动的过程中。

也就是说，当 Namenode 启动的时候，首先装载 fsimage 文件，然后再应用 edits 文件，最后还会将最新的目录树信息更新到新的 fsimage 文件中，然后启用新的 edits 文件。

## datanode

datanode 是文件系统的工作节点。它们根据需要存储并检索数据块，并定期向 namenode 发送它们所存储的块的列表。

## SecondNamenode

SecondNamenode 是对主 Namenode 的一个补充，它会周期的执行对 HDFS 元数据的检查点。

当前的设计仅仅允许每个HDFS只有单个 SecondNamenode 结点。

SecondNamenode是有一个后台的进程，会定期的被唤醒（唤醒的周期依赖相关配置）执行检查点任务，然后继续休眠。

它使用 ClientProtocol 协议与主 Namenode 通信。

- **1、检查点的作用**

在 Namenode 的操作流程中，如果 Namenode 运行期间文件发生的改动较多的话，会造成 edits 文件变得非常大，这样在下一次 Namenode 启动的过程中，读取 fsimage 文件后，会应用这个非常大的 edits 文件，导致启动时间变长，并不可控。

Namenode 的 edits 文件过大的问题，也就是 SecondNamenode 要解决的主要问题。

**SecondNamenode 会按照一定的规则被唤醒，然后进行 fsimage 文件与 edits 文件合并，防止 edits 文件过大，导致 Namenode 启动时间过长**

- **2、检查点被唤醒的条件**

控制检查点的参数有两个，分别是：

1. `fs.checkpoint.period`：单位秒，默认值 3600，检查点的间隔时间，当距离上次检查点执行超过该时间后启动检查点
2. `fs.checkpoint.size`：单位字节，默认值67108864，当 edits 文件超过该大小后，启动检查点

上面两个条件是**或**的关系，只要满足一个启动条件，检查点即被唤醒

- **3、检查点执行的过程**
  1. 初始化检查点
  2. 通知 Namenode 启用新的 edits 文件
  3. 从 Namenode 下载 fsimage 和 edits 文件
  4. 调用 loadFSImage 装载 fsimage
  5. 调用 loadFSEdits 应用 edits 日志
  6. 保存合并后的目录树信息到新的 fsimage 中
  7. 将新产生的 fsimage 上传到 Namenode 中，替换原来的 fsimage 文件
  8. 结束检查点

SecondeNamenode 最好于 Namenode 部署到不同的服务器，因为在 merge 的过程中，SecondNamenode 对内存的需求与 Namenode 是相同的，所以对于那些大型的生产系统中，如果将两者部署在同一台服务器上，在内存上会出现瓶颈。

## Client

客户端，系统使用者

- 调用 HDFS API 操作文件
- 与NN交互获取文件元数据
- 与DN交互进行数据读写