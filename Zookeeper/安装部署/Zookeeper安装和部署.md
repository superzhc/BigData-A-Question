# Zookeeper 安装部署

> 官网下载地址：<https://archive.apache.org/dist/zookeeper/>

**本文的版本是基于 3.4.5**

Zookeeper 即可以配置成单机模式，也可以配置成集群模式。

## 单机模式

下载 Zookeeper 的安装包后，解压到合适的位置。进入 zookeeper 目录下的 conf 子目录，创建 `zoo.cfg`:

```cfg
tickTime=2000
dataDir=/usr/local/data/zookeeper/data
dataLogDir=/usr/local/data/zookeeper/logs
clientPort=2181
```

参数说明：

- `tickTime`：zookeeper 中使用的基本时间单位，毫秒值
- `dataDir`：数据目录，可以是任意目录
- `dataLogDir`：log 目录，同样可以是任意目录。如果没有设置该参数，将使用和 dataDir 相同的设置
- `clientPort`：监听 client 连接的端口号

注意事项：

1. dataDir 目录不需要提前创建
2. dataLogDir 目录需要提前创建完成
3. 注意 `zoo.cfg` 文件内容中的空格

至此，zookeeper 的单机模式已经配置好了。启动 server 只需运行脚本:

```sh
bin/zkServer.sh start  
```

Server 启动之后，就可以启动 client 连接 server 了，执行脚本：

```sh
bin/zkCli.sh -server localhost:4180
```

## 伪集群模式

所谓伪集群, 是指在单台机器中启动多个zookeeper进程, 并组成一个集群. 以启动3个zookeeper进程为例。

将zookeeper的目录拷贝2份:

```sh
|--zookeeper0  
|--zookeeper1  
|--zookeeper2
```

更改zookeeper0/conf/zoo.cfg文件为:

```sh
tickTime=2000    
initLimit=5    
syncLimit=2    
dataDir=/Users/apple/zookeeper0/data    
dataLogDir=/Users/apple/zookeeper0/logs    
clientPort=4180  
server.0=127.0.0.1:8880:7770    
server.1=127.0.0.1:8881:7771    
server.2=127.0.0.1:8882:7772
```

新增几个参数，其含义如下：

- `initLimit`： zookeeper集群中的包含多台server, 其中一台为leader, 集群中其余的server为follower. initLimit参数配置初始化连接时, follower和leader之间的最长心跳时间. 此时该参数设置为5, 说明时间限制为5倍tickTime, 即5*2000=10000ms=10s.
- `syncLimit`：该参数配置leader和follower之间发送消息, 请求和应答的最大时间长度. 此时该参数设置为2, 说明时间限制为2倍tickTime, 即4000ms.
- `server.X=A:B:C`：其中X是一个数字, 表示这是第几号server. A是该server所在的IP地址. B配置该server和集群中的leader交换消息所使用的端口. C配置选举leader时所使用的端口. 由于配置的是伪集群模式, 所以各个server的B, C参数必须不同.

参照`zookeeper0/conf/zoo.cfg`, 配置 `zookeeper1/conf/zoo.cfg`, 和 `zookeeper2/conf/zoo.cfg` 文件. 只需更改dataDir, dataLogDir, clientPort参数即可.

在之前设置的dataDir中新建**`myid`**文件, 写入一个数字, 该数字表示这是第几号server. 该数字必须和 `zoo.cfg` 文件中的`server.X`中的X一一对应.
`/Users/apple/zookeeper0/data/myid` 文件中写入0, `/Users/apple/zookeeper1/data/myid` 文件中写入1, `/Users/apple/zookeeper2/data/myid` 文件中写入2.

分别进入`/Users/apple/zookeeper0/bin`,`/Users/apple/zookeeper1/bin`, `/Users/apple/zookeeper2/bin`三个目录, 启动server.
任意选择一个server目录, 启动客户端:

```sh
bin/zkCli.sh -server localhost:4180
```

## 集群模式

集群模式的配置和伪集群基本一致.

由于集群模式下, 各server部署在不同的机器上, 因此各server的 `conf/zoo.cfg` 文件可以完全一样.

```sh
tickTime=2000    
initLimit=5    
syncLimit=2    
dataDir=/home/zookeeper/data    
dataLogDir=/home/zookeeper/logs    
clientPort=4180  
server.43=10.1.39.43:2888:3888  
server.47=10.1.39.47:2888:3888    
server.48=10.1.39.48:2888:3888  
```

示例中部署了3台zookeeper server, 分别部署在`10.1.39.43`, `10.1.39.47`, `10.1.39.48`上. 需要注意的是, 各server的dataDir目录下的myid文件中的数字必须不同.

`10.1.39.43` server的myid为43, `10.1.39.47` server的myid为47, `10.1.39.48` server的myid为48.

## 验证

**通过 Jps 查看进程是否存在**

```bash
jps

# QuorumPeerMain
```

**通过 zk 的状态命令来查看**

```bash
${ZOOKEEPER_HOME}/bin/zkServer.sh status
```