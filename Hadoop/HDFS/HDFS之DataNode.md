# DataNode

DataNode 是 HDFS 中的 worker 节点，它负责**存储数据块**，也负责**为系统客户端提供数据块的读写服务**，同时还会根据 NameNode 的指示来进行创建、删除、和复制等操作。此外，它还会通过心跳定期向 NameNode 发送所存储文件块列表信息。当对 HDFS 文件系统进行读写时，NameNode 告知客户端每个数据驻留在哪个 DataNode，客户端直接与 DataNode 进行通信，DataNode 还会与其它 DataNode 通信，复制这些块以实现冗余。

