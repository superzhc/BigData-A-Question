![img](../images/4252059524-5b9731766ca16_articlex.png)

1. Zookeeper 分为服务器端（Server）和客户端（Client），客户端可以连接到整个 Zookeeper 服务的任意服务器上（除非 LeaderServer 参数被显式设置，Leader 不允许接受客户端连接）
2. 客户端使用并维护一个TCP 连接，通过这个连接发送请求、接受响应、获取观察的事件以及发送心跳。如果这个 TCP 连接中断，客户端将自动尝试连接到另外的 Zookeeper 服务器。客户端第一次连接到 Zookeeper 服务时，接受这个连接的 Zookeeper 服务器会为这个客户端建立一个会话。当这个客户端连接到另外的服务器时，这个会话会被新的服务器重新建立连接
3. 上图的每一个 Server 代表一个安装 Zookeeper 服务的机器，既是整个提供 Zookeeper 服务的集群（或者是伪集群组成）
4. 组成 Zookeeper 服务的服务器之间是相互通信的。它们维护一个内存中的状态图像，以及持久存储中的事务日志和快照；只要大多数服务器可用，Zookeeper 服务就可用
5. Zookeeper 启动时，将从实例中选举一个 Leader，Leader 负责处理数据更新等操作，一个更新操作成功的标志是当且仅当大多数 Server 在内存中成功修改数据。每个 Server 在内存中存储了一份数据
6. Zookeeper 是可以集群复制的，集群间通过 Zab 协议（Zookeeper Atomic Broadcast）来保持数据的一致性
7. Zab 协议包括两个阶段：`leader election` 阶段和 `Atomic Brodcast` 阶段
	1. 集群中将选举出一个 leader，其他的机器则称为 follower，所有的写操作都被传送给 leader，并通过 brocast 将所有的更新告诉给 follower
	2. 当 leader 崩溃或者 leader 失去大多数的 follower 时，需要重新选举出一个新的 leader，让所有的服务器都恢复到一个正确的状态
	3. 当 leader 被选举出来，且大多数服务器完成了和 leader 的状态同步后，leader election 的过程就结束了，就将会进入到 Atomic Brodcast 的过程
	4. Atomic Brodcast 同步 leader 和 follower 之间的信息，保证 leader 和 follower 具有形同的系统状态