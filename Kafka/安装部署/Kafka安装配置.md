# 安装部署

> Kafka 官网地址：`https://kafka.apache.org/`

**本文是基于 2.2.1 版本**

### 1、安装 JDK

JDK 要求 1.8 版本以上 

### 2、安装 Zookeeper

[Zookeeper的安装和部署](../Zookeeper/Zookeeper安装和部署.md)

### 3、安装 Apache Kafka

#### 下载 Kafka

从官网地址下载 Kafka【本文下载的版本为2.11-2.2.1】

#### 解压 tar 文件

```sh
tar -zxf kafka_2.11-2.2.1.tar.gz
```

#### 重命名解压文件夹

```sh
mv kafka_2.11-2.2.1 kafka
```

#### 进入解压文件

```sh
cd kafka
```

#### 修改 broker 的配置文件 `$KAFKA_HOME/config/server.properties`

```properties
# broker 的编号，如果集群中有多个broker ，则每个broker的编号需要设置的不同
broker.id=0
# broker 对外提供的服务入口地址，一定需要配置，不然客户端会找不到Kafka节点，若外网需要访问的话，需要配置成服务器的地址
listeners= PLAINTEXT://localhost:9092
# 存放消息日志文件的地址
log.dirs= /usr/local/data/kafka/kafka-logs
# Kafka所需的ZooKeeper集群地址，为了方便演示，我们假设Kafka和ZooKeeper都安装在本机
zookeeper.connect=localhost:2181/kafka
```

#### 启动服务器

```sh
# 启动服务器的命令
bin/kafka-server-start.sh config/server.properties

# 响应结果如下所示
[2016-01-02 15:37:30,410] INFO KafkaConfig values:
request.timeout.ms = 30000
log.roll.hours = 168
inter.broker.protocol.version = 0.9.0.X
log.preallocate = false
security.inter.broker.protocol = PLAINTEXT
…………………………………………….
…………………………………………….
```

**后台运行模式**

```bash
bin/kafka-server-start.sh -daemon config/server.properties
```

#### 停止服务器

```sh
bin/kafka-server-stop.sh config/server.properties
```

### 集群配置

broker 的配置文件 `server.properties` 基本一致，但是 `broker.id` 必须设置不同，一般设置成递增的编号就可以了，多节点的 broker 配置完成后，依次启动 Kafka 就可以运行起集群了。