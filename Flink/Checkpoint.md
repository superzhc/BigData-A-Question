# Checkpoint

为了保证State的容错性，Flink需要对State进行CheckPoint。CheckPoint是Flink实现容错机制的核心功能，它能够根据配置周期性地基于Stream中各个Operator/Task的状态来生成快照，从而将这些状态数据定期持久化存储下来。Flink程序一旦意外崩溃，重新运行程序时可以有选择地从这些快照进行恢复，从而修正因为故障带来的程序数据异常。

Flink的CheckPoint机制可以与Stream和State持久化存储交互的前提有以下两点：

- 需要有持久化的Source，它需要支持在一定时间内重放事件，这种Source的典型例子就是持久化的消息队列（如Apache Kafka、RabbitMQ等）或文件系统（如HDFS、S3、GFS等）。
- 需要有用于State的持久化存储介质，比如分布式文件系统（如HDFS、S3、GFS等）。

默认情况下，CheckPoint功能是Disabled（禁用）的，使用时需要先开启它。

通过如下代码即可开启：

```java
// duration：Checkpoint的周期
env.enableCheckpointing(duration);
```



