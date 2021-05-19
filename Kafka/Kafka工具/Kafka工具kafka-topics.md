> `kafka-topics` 用于创建、删除、描述或修改一个主题

|     日期     | 版本           | 备注                                                                   |
| :----------: | -------------- | ---------------------------------------------------------------------- |
| 2021年5月8日 | 2.2.1-cdh6.3.2 | 经测试，连接Kafka的参数已支持 `--bootstrap-server`，等同 `--zookeeper` |

该命令包含以下参数：

```bash
Option                                   Description
------                                   -----------
--alter                                  Alter the number of partitions,
                                           replica assignment, and/or
                                           configuration for the topic.
--config <String: name=value>            A topic configuration override for the
                                           topic being created or altered.The
                                           following is a list of valid
                                           configurations:

                                         See the Kafka documentation for full
                                           details on the topic configs.
--create                                 Create a new topic.
--delete                                 Delete a topic
--delete-config <String: name>           A topic configuration override to be
                                           removed for an existing topic (see
                                           the list of configurations under the
                                           --config option).
--describe                               List details for the given topics.
--disable-rack-aware                     Disable rack aware replica assignment
--force                                  Suppress console prompts
--help                                   Print usage information.
--if-exists                              if set when altering or deleting
                                           topics, the action will only execute
                                           if the topic exists
--if-not-exists                          if set when creating topics, the
                                           action will only execute if the
                                           topic does not already exist
--list                                   List all available topics.
--partitions <Integer: partitions>  The number of partitions for the topic
                                           being created or altered (WARNING:
                                           If partitions are increased for a
                                           topic that has a key, the partition
                                           logic or ordering of the messages
                                           will be affected
--replica-assignment <String:            A list of manual partition-to-broker
  broker_id_for_part1_replica1 :           assignments for the topic being
  broker_id_for_part1_replica2 ,           created or altered.
  broker_id_for_part2_replica1 :
  broker_id_for_part2_replica2 , ...>
--replication-factor <Integer:           The replication factor for each
  replication factor>                      partition in the topic being created.
--topic <String: topic>                  The topic to be create, alter or
                                           describe. Can also accept a regular
                                           expression except for --create option
--topics-with-overrides                  if set when describing topics, only
                                           show topics that have overridden
                                           configs
--unavailable-partitions                 if set when describing topics, only
                                           show partitions whose leader is not
                                           available
--under-replicated-partitions            if set when describing topics, only
                                           show under replicated partitions
--zookeeper <String: urls>               REQUIRED: The connection string for
                                           the zookeeper connection in the form
                                           host:port. Multiple URLS can be
                                           given to allow fail-over.

-----------------------------------------------------
Create, delete, describe, or change a topic.
-----------------------------------------------------

Command Usage:
-----------------------------------------------------
 create:
   enable rack strategy:
     kafka-topics.sh --create --topic <topic name> --partitions <Integer: the number of partitions> --replication-factor <Integer: replication factor> --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka>
   disable rack strategy:
     kafka-topics.sh --create --topic <topic name> --partitions <Integer: the number of partitions> --replication-factor <Integer: replication factor> --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka> --disable-rack-aware
-----------------------------------------------------
 delete:
     kafka-topics.sh --delete --topic <topic name> --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka>
-----------------------------------------------------
 alter:
     kafka-topics.sh --alter --topic <topic name> --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka> --config <name=value>
-----------------------------------------------------
 list:
     kafka-topics.sh --list --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka>
-----------------------------------------------------
 describe:
     kafka-topics.sh --describe --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka>
-----------------------------------------------------
```

| Option                           | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `--alter`                        | 更改分区数、副本配置和(/或)主题的配置                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `--config <String: name=value>`  | 覆盖主题的配置信息。<br>可覆盖的配置项有如下：<br>`cleanup.policy`,`compression.type`,`delete.retention.ms`,`file.delete.delay.ms`,`flush.messages`,`flush.ms`,`follower.replication.throttled.replicas`,`index.interval.bytes`,`leader.replication.throttled.replicas`, `max.message.bytes`,`message.format.version`,`message.timestamp.difference.max.ms`,`message.timestamp.type`,`min.cleanable.dirty.ratio`,`min.compaction.lag.ms`,`min.insync.replicas`,`preallocate,retention.bytes`,`retention.ms`,`segment.bytes`,`segment.index.bytes`,`segment.jitter.ms`,`segment.ms`,`unclean.leader.election.enable` |
| `--create`                       | 创建一个新的主题                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `--delete`                       | 删除一个主题                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `--delete-config <String: name>` | 删除已有主题中的配置（详细的配置见`--config`中的选项）                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `--describe`                     | 列出给定主题的详细信息                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `--disable-rack-aware`           |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `--force`                        |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |

**描述主题的配置**

```sh
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --describe --entity-type topics --entity-name xumaosheng
```

**设置保留时间**

```sh
# Deprecated way
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --alter --topic xumaosheng --config retention.ms=1000

# Modern way
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --alter --entity-type topics --entity-name xumaosheng --add-config retention.ms=1000
```

如果需要删除主题中所有消息，则可以利用保留时间。首先将保留时间设置为非常低，等待几秒，然后将保留时间恢复为上一个值。

注意：默认保留时间为24h（86400000毫秒）

**删除主题**

```sh
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --delete --topic xumaosheng
```

注意：需要在 Broker 的配置文件 `server.peoperties` 中配置 `delete.topic.enable=true` 才能删除主题

**主题信息**

```sh
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --describe --topic xumaosheng
```

**添加分区**

```sh
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --alter --topic xumaosheng --partitions 3
```

**创建主题**

```sh
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --create --replication-factor 1 --partitions 3 --topic xumaosheng
```

**列出主题**

```sh
bin/kafka-topics.sh --zookeeper localhost:2181/kafka --list
```

