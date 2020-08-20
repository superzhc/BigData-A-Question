kafka安装目录下的bin目录包含了很多运维可操作的shell脚本，列举如下：

| 脚本名称                            | 用途描述                                      |
| ----------------------------------- | --------------------------------------------- |
| connect-distributed.sh              | 连接kafka集群模式                             |
| connect-standalone.sh               | 连接kafka单机模式                             |
| kafka-acls.sh                       | todo                                          |
| kafka-broker-api-versions.sh        | todo                                          |
| kafka-configs.sh                    | 配置管理脚本                                  |
| kafka-console-consumer.sh           | kafka消费者控制台                             |
| kafka-console-producer.sh           | kafka生产者控制台                             |
| kafka-consumer-groups.sh            | kafka消费者组相关信息                         |
| kafka-consumer-perf-test.sh         | kafka消费者性能测试脚本                       |
| kafka-delegation-tokens.sh          | todo                                          |
| kafka-delete-records.sh             | 删除低水位的日志文件                          |
| kafka-log-dirs.sh                   | kafka消息日志目录信息                         |
| kafka-mirror-maker.sh               | 不同数据中心kafka集群复制工具                 |
| kafka-preferred-replica-election.sh | 触发preferred replica选举                     |
| kafka-producer-perf-test.sh         | kafka生产者性能测试脚本                       |
| kafka-reassign-partitions.sh        | 分区重分配脚本                                |
| kafka-replay-log-producer.sh        | todo                                          |
| kafka-replica-verification.sh       | 复制进度验证脚本                              |
| kafka-run-class.sh                  | todo                                          |
| kafka-server-start.sh               | 启动kafka服务                                 |
| kafka-server-stop.sh                | 停止kafka服务                                 |
| kafka-simple-consumer-shell.sh      | deprecated，推荐使用kafka-console-consumer.sh |
| kafka-streams-application-reset.sh  | todo                                          |
| kafka-topics.sh                     | topic管理脚本                                 |
| kafka-verifiable-consumer.sh        | 可检验的kafka消费者                           |
| kafka-verifiable-producer.sh        | 可检验的kafka生产者                           |
| trogdor.sh                          | todo                                          |
| zookeeper-security-migration.sh     | todo                                          |
| zookeeper-server-start.sh           | 启动zk服务                                    |
| zookeeper-server-stop.sh            | 停止zk服务                                    |
| zookeeper-shell.sh                  | zk客户端                                      |

### `kafka-server-start.sh`

 [kafka-server-start](Kafka/Kafka工具/Kafka工具kafka-server-start.md) 

### `kafka-topics.sh`

 [kafka-topics](Kafka/Kafka工具/Kafka工具kafka-topics.md) 

### `kafka-console-producer.sh`

 [kafka-console-producer](Kafka/Kafka工具/Kafka工具kafka-console-producer.md) 

### `kafka-console-consumer.sh`

 [kafka-console-consumer](Kafka/Kafka工具/Kafka工具kafka-console-consumer.md) 

### `kafka-configs.sh`

 [kafka-configs](Kafka/Kafka工具/Kafka工具kafka-configs.md) 

### `kafka-consumer-groups.sh`

当一个 Consumer Group 时，可以在命令行中使用 `kafka-consumer-group.sh` 脚本获取 partition 分配和消费进度

```sh
bin/kafka-consumer-groups.sh --bootstrap-server 192.168.186.48:6667 --describe --group superz  --new-consumer

# 结果显示如下：
GROUP     TOPIC              PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG     OWNER
superz    Hello-Kafka        0          85              85              0       consumer-1_/192.168.150.31
```

上面显示了 Consumer Group中 partition 的分配，那个消费者实例拥有那个分区，以及最新的 committed offset（这里的 current-offset）。LAG 表示 Log-End-Offset 与最新的 committed offset 的差值。管理员可以使用这个命令来监控以保证 Consumer Group 跟上了 Producer 的速度

