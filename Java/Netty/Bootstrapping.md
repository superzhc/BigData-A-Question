Bootstrapping（引导）是出现在 Netty 配置程序的过程中，Bootstrapping 在给服务器绑定指定窗口或者要连接客户端的时候会使用到。

Bootstrapping 有以下两种类型：

- 一种是用于客户端的 Bootstrap
- 一种是用于服务端的 ServerBootstrap

不管程序使用哪种协议，创建的是一个客户端还是服务器，Bootstrapping 都是必须要使用到的。

两种 Bootstrapping 之间有一些相似之处，也有一些不同。Bootstrap 和 ServerBootstrap 之间的差异如下：

| 分类                | Bootstrap            | ServerBootstrap |
| ------------------- | -------------------- | --------------- |
| 网络功能            | 连接到远程主机和端口 | 绑定本地端口    |
| EventLoopGroup 数量 | 1                    | 2               |