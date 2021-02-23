<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-03-08 12:42:47
 * @LastEditTime : 2021-02-05 15:54:04
 * @Copyright 2021 SUPERZHC
-->
# Kafka

## 简介

Apache Kafka 是一个 ==**分布式发布-订阅（pub-sub）消息系统**==。

[Kafka概述](Kafka/Kafka概述.md) 

[术语解释](Kafka/Kafka术语.md)

## 安装配置

[Kafka的安装配置](Kafka/Kafka安装配置.md)

**验证 Kafka 是否启动成功**，使用 jps 命令来查看，如下所示：

```sh
jps

# 结果如下所示：
...
QuorumPeerMain
Kafka
...
# QuorumPeerMain 是 Zookeeper 守护进程，另一个是 Kafka 守护进程
```

**查看 Kafka 版本**

Kafka 并没有提供类似其他命令的选项-version来快速查看版本。可以通过执行以下命令，获得 Kafka 的版本

```sh
# There is nothing like kafka --version at this point. So you should either check the version from $KAFKA_HOME/libs/ folder or you can run

find ./libs/ -name \*kafka_\* | head -1 | grep -o '\kafka[^\n]*'

# from your kafka folder (and it will do the same for you). It will return you something like kafka_2.9.2-0.8.1.1.jar.asc where 0.8.1.1 is your kafka version.
```

## 基本操作

[Kafka工具使用](Kafka/Kafka工具使用.md) 

## Broker

[TODO]

## 生产者

[Producer](Kafka/Kafka之Producer.md) 

[Producer配置](Kafka/Kafka配置\Producer配置.md) 

[Kafka工具kafka-console-producer](Kafka/Kafka工具/Kafka工具kafka-console-producer.md)

## 消费者

 [TODO]

## 实战

- [Kafka 示例代码](Kafka/Kafka示例代码.md)

## Kafka 如何保证数据的可靠性和一致性

- [Kafka 的高可靠性是怎么实现的？](Kafka/Kafka是如何保证数据的可靠性和一致性.md)
- [Kafka 在什么情况下会出现消息丢失？](Kafka/Kafka是如何保证数据的可靠性和一致性.md)
- [怎么尽可能保证 Kafka 的可靠性？](Kafka/Kafka是如何保证数据的可靠性和一致性.md)