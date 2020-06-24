## 配置文件

|    配置文件名     | 配置对象       | 作用                                                         |
| :---------------: | -------------- | ------------------------------------------------------------ |
|  `core-site.xml`  | 全局参数       | 用于定义系统级别的参数，如 HDFS、URL、Hadoop 的临时目录等    |
|  `hdfs-site.xml`  | HDFS参数       | 如名称节点和数据节点的存放位置、文件副本的个数、文件读取权限等 |
| `mapred-site.xml` | MapReduce参数  | 包括 JobHistory Server 和应用程序参数两部分，如 reduce 任务的默认个数、任务所能够使用内存的默认上下限等 |
|  `yarn-site.xml`  | 资源管理器参数 | 配置 ResouceManager，NodeManager 的通信端口，web 监控端口等  |

## 配置参数

### `core-site.xml`

| 参数名                | 默认值                     | 作用               |
| :-------------------- | -------------------------- | ------------------ |
| `fs.defaultFS`        | `file:///`                 | 文件系统主机和端口 |
| `io.file.buffer.size` | 4096                       | 流文件的缓冲区大小 |
| `hadoop.tmp.dir`      | `/tmp/hadoop-${user.name}` | 临时文件夹         |

### `hdfs-site.xml`

| 参数名                                | 默认值                              | 作用                                                         |
| :------------------------------------ | ----------------------------------- | ------------------------------------------------------------ |
| `dfs.namenode.secondary.http-address` | `0.0.0.0:50090`                     | 定义 HDFS  对应的 HTTP 服务器地址和端口                      |
| `dfs.namenode.name.dir`               | `file://${hadoop.tmp.dir}/dfs/name` | 定义 DFS 的名称节点在本地文件系统的位置                      |
| `dfs.datanode.data.dir`               | `file://${hadoop.tmp.dir}/dfs/data` | 定义 DFS 的数据节点存储数据块时存储在本地文件系统的位置      |
| `dfs.replication`                     | 3                                   | 块的副本数量                                                 |
| `dfs.webhdfs.enabled`                 | `true`                              | 是否通过 http 协议读取 hdfs 文件，如果选是，则集群安全性较差 |

### `mapred-site.xml`

| 参数名                                | 默认值          | 作用                                                         |
| ------------------------------------- | --------------- | ------------------------------------------------------------ |
| `mapreduce.framework.name`            | `local`         | 取值 local、classic 或 yarn 其中之一，如果不是 yarn，则不会使用 YARN 集群来实现资源的分配 |
| `mapreduce.jobhistory.address`        | `0.0.0.0:10020` | 定义历史服务器的地址和端口，通过历史服务器查看已经运行完的 MapReduce 作业记录 |
| `mapreduce.jobhistory.webapp.address` | `0.0.0.0:19888` | 定义历史服务器 web 应用访问的地址和端口                      |

### `yarn-site.xml`

| 参数名                                          | 默认值         | 作用                                                         |
| ----------------------------------------------- | -------------- | ------------------------------------------------------------ |
| `yarn.resourcemanager.address`                  | `0.0.0.0:8032` | ResourceManager 提供给客户端访问的地址。客户端通过该地址向 RM 提交应用程序，杀死应用程序等 |
| `yarn.resourcemanager.scheduler.address`        | `0.0.0.0:8030` | ResourceManager 提供给 ApplicationMaster 的访问地址。ApplicationMaster 通过该地址向 RM 申请资源、释放资源等 |
| `yarn.resourcemanager.resource-tracker.address` | `0.0.0.0:8031` | ResourceManager 提供给 NodeManager 的地址。NodeManager 通过该地址向 RM  汇报心跳和领取任务等 |
| `yarn.resourcemanager.admin.address`            | `0.0.0.0:8033` | ResourceManager 提供给管理员的访问地址。管理员通过该地址向 RM 发送管理命令等 |
| `yarn.resourcemanager.webapp.address`           | `0.0.0.0:8088` | ResourceManager 对 web 服务提供地址。用户可通过该地址在浏览器中查看集群各类信息 |
| `yarn.nodemanager.aux-services`                 |                | 通过该配置项，用户可以自定义一些服务，例如 MapReduce 的 shuffle 功能就是采用这种方式实现的，这样就可以在 NodeManager 上扩展自己的服务 |















































