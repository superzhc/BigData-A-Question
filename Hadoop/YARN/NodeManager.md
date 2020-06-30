NodeManager是Hadoop Yarn在每个计算节点上的代理，它根据YARN应用程序的要求，使用节点上的物理资源来运行Container。**NodeManager本质上是YARN的工作守护进程**，主要有以下职责：

- 保持与ResourceManager的同步
- 跟踪节点的健康状况
- 管理各个Container的生命周期，监控每个Container的资源使用情况（如内存，CPU）
- 管理分布式缓存（对Container所需的Jar、库等文件的本地文件系统缓存）
- 管理各个Container生成的日志
- 不同的YARN应用可能需要的辅助服务

> Container的管理是NodeManager的核心功能。