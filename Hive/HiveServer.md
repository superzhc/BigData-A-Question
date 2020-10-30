# Hive Server

## 简介

HiveServer 可以让 Hive 以提供 Trift 服务的服务器形式来运行，可以允许许多不同语言（如：C、python、Java）编写的客户端进行通信。

HiveServer 的启动指令

```sh
# 前台命令，会驻留到控制台
${HIVE_HOME}/bin/hive --servie hiveserver2

# 前台转后台，启动完成，按回车可以继续进行其他操作
# & 表示后台运行
${HIVE_HOME}/bin/hive --servie hiveserver2 &
```

默认的端口为 `10000`，可以通过设置`HIVE_PORT` 环境变量来设置服务器所监听的端口号。

## HiveServer vs HiveServer2

两者都允许远程客户端使用多种编程语言，通过 HiveServer 或 HiveServer2，客户端可以在不启动 CLI 的情况下对 Hive 中的数据进行操作。

HiveServer 和 HiveServer2 都是基于 Thrif。

既然已经存在 HiveServer，为什么还需要 HiveServer2 呢？这是因为 HiveServer 不能处理多于一个客户端的并发请求，这是由于 HiveServer 使用的 Thrift 接口所导致的限制，不能通过修改 HiveServer 的代码修正，因此在 Hive-0.11.0 版本中重写了 HiveServer 代码得到了 HiveServer2，进而解决了该问题。HiveServer2 支持多客户端的并发和认证，为开放 API 客户端如 JDBC、ODBC 提供更好的支持。

HiveServer 和 HiveServer2 的 JDBC 区别：

| 名称        | URL             | Driver                                   |
| ----------- | --------------- | ---------------------------------------- |
| HiveServer2 | `jdbc:hive2://` | `org.apache.hive.jdbc.HiveDriver`        |
| HiveServer  | `jdbc:hive://`  | `org.apache.hadoop.hive.jdbc.HiveDriver` |

## 使用

### ~~HiveServer~~

在命令行输入 `hive --service hiveserver --help` 查看 hiveserver 的帮助信息：

```sh
[hadoop@hadoop~]$ hive --service hiveserver --help
Starting Hive Thrift Server
usage:hiveserver
-h,--help                        Print help information
    --hiveconf <property=value>   Use value for given property
    --maxWorkerThreads <arg>      maximum number of worker threads,
                                 default:2147483647
    --minWorkerThreads <arg>      minimum number of worker threads,
                                  default:100
-p <port>                        Hive Server portnumber, default:10000
-v,--verbose                     Verbose mode
```

启动 hiveserver 服务，可以得知默认 hiveserver 运行在端口 **10000**，最小 100 工作线程，最大 2147483647 工作线程

> 注：HiveServer 计划从 Hive 0.15开始从版本中删除，需要切换到 HiveServer2

### HiveServer2

Hiveserver2 允许在配置文件 `hive-site.xml` 中进行配置管理，具体的参数为：

```
hive.server2.thrift.min.worker.threads– 最小工作线程数，默认为5。
hive.server2.thrift.max.worker.threads – 最小工作线程数，默认为500。
hive.server2.thrift.port– TCP 的监听端口，默认为10000。
hive.server2.thrift.bind.host– TCP绑定的主机，默认为localhost
```

也可以设置环境变量 `HIVE_SERVER2_THRIFT_BIND_HOST` 和 `HIVE_SERVER2_THRIFT_PORT` 覆盖 `hive-site.xml` 设置的主机和端口号。

从Hive-0.13.0开始，HiveServer2支持通过HTTP传输消息，该特性当客户端和服务器之间存在代理中介时特别有用。与HTTP传输相关的参数如下：

```
hive.server2.transport.mode – 默认值为binary（TCP），可选值HTTP。
hive.server2.thrift.http.port– HTTP的监听端口，默认值为10001。
hive.server2.thrift.http.path – 服务的端点名称，默认为 cliservice。
hive.server2.thrift.http.min.worker.threads– 服务池中的最小工作线程，默认为5。
hive.server2.thrift.http.max.worker.threads– 服务池中的最小工作线程，默认为500。
```

**启动 HiveServer2 有两种方式，一种是`hive --service hiveserver2`，另一种是`hiveserver2`**

默认情况下，HiveServer2 以提交查询的用户执行查询，如果 `hive.server2.enable.doAs` 设置为 false，查询将以运行 hiveserver2 进程的用户运行。为了防止非加密模式下的内存泄露，可以通过设置下面的参数为 true 禁用文件系统的缓存：

```
fs.hdfs.impl.disable.cache – 禁用HDFS文件系统缓存，默认值为false
fs.file.impl.disable.cache – 禁用本地文件系统缓存，默认值为false
```

