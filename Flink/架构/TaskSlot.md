# TaskSlot

Flink 的每个 TaskManager 为集群提供 Solt。Solt 的数量通常与每个 TaskManager 节点的可用 CPU 内核数成比例，一般情况下 Slot 的数量就是每个节点的 CPU 的核数，如下图所示：

![image-20201119221428474](../images/image-20201119221428474.png)

![image-20201119221810058](../images/image-20201119221810058.png)