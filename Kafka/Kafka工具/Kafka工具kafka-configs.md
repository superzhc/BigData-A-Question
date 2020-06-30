> `kafka-config.sh` 提供命令提示的功能。

使用步骤如下：

1. 在命令行中输入 `bin/kafka-config.sh`，然后会输出下面的命令提示信息

   ```sh
   Option                      Description
   ------                      -----------
   --add-config <String>       Key Value pairs of configs to add. Square brackets
                                 can be used to group values which contain commas:
                                 'k1=v1,k2=[v1,v2,v2],k3=v3'. The following is a
                                 list of valid configurations: For entity_type
                                 'topics':
                                   cleanup.policy
                                   compression.type
                                   delete.retention.ms
                                   file.delete.delay.ms
                                   flush.messages
                                   flush.ms
                                   follower.replication.throttled.replicas
                                   index.interval.bytes
                                   leader.replication.throttled.replicas
                                   max.message.bytes
                                   message.format.version
                                   message.timestamp.difference.max.ms
                                   message.timestamp.type
                                   min.cleanable.dirty.ratio
                                   min.compaction.lag.ms
                                   min.insync.replicas
                                   preallocate
                                   retention.bytes
                                   retention.ms
                                   segment.bytes
                                   segment.index.bytes
                                   segment.jitter.ms
                                   segment.ms
                                   unclean.leader.election.enable
                               For entity_type 'brokers':
                                   follower.replication.throttled.rate
                                   leader.replication.throttled.rate
                               For entity_type 'users':
                                   request_percentage
                                   producer_byte_rate
                                   SCRAM-SHA-256
                                   SCRAM-SHA-512
                                   consumer_byte_rate
                               For entity_type 'clients':
                                   request_percentage
                                   producer_byte_rate
                                   consumer_byte_rate
                               Entity types 'users' and 'clients' may be specified
                                 together to update config for clients of a
                                 specific user.
   --alter                     Alter the configuration for the entity.
   --delete-config <String>    config keys to remove 'k1,k2'
   --describe                  List configs for the given entity.
   --entity-default            Default entity name for clients/users (applies to
                                 corresponding entity type in command line)
   --entity-name <String>      Name of entity (topic name/client id/user principal
                                 name/broker id)
   --entity-type <String>      Type of entity (topics/clients/users/brokers)
   --force                     Suppress console prompts
   --help                      Print usage information.
   --zookeeper <String: urls>  REQUIRED: The connection string for the zookeeper
                                 connection in the form host:port. Multiple URLS
                                 can be given to allow fail-over.
   
   -----------------------------------------------------
   Add/Remove entity config for a topic, client, user or broker
   -----------------------------------------------------
   
   Command Usage:
   -----------------------------------------------------
    Update the configuration properties:
        kafka-configs.sh --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka> --alter --add-config <The KV pair of the configuration attributes to be added, like k1 = v1, k2 = v2,...> --entity-name <topic name or client id> --entity-type <topics or clients>
   -----------------------------------------------------
    View the configuration properties:
        kafka-configs.sh --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka> --describe --entity-name <topic name or client id> --entity-type <topics or clients>
   -----------------------------------------------------
   ```

2. 从输出的 `Add/Remove entity config for a topic, client, user or broker` 可以看到这个命令可以配置 topic，client，user 或 broker。

   如果要设置 topic，就需要设置 `entity-type` 为 `topic`，输入如下命令：

   ```sh
   bin/kafka-configs.sh --entity-type topics
   # Command must include exactly one action: --describe, --alter
   ```

3. 命令提示需要指定一个操作，增加一个 `--describe` 来测试：

   ```sh
   kafka-configs.sh --entity-type topics --describe
   #Missing required argument "[zookeeper]"
   ```

4. 根据提示增加 `--zookeeper`:

   ```sh
   kafka-configs.sh --entity-type topics --describe --zookeeper 192.168.186.40:24002/kafka
   # 输出结果如下：
   Configs for topic 'epointlog' are
   Configs for topic 'xuamosheng1' are
   Configs for topic '__default_metrics' are retention.ms=86400000,segment.ms=14400000,min.insync.replicas=2,retention.bytes=-1
   Configs for topic 'xumaosheng' are
   Configs for topic 'xumaosheng1' are
   Configs for topic 'loganalyseloadrunner' are
   Configs for topic 'bingo' are
   Configs for topic 'epoint' are
   Configs for topic 'epointlog1' are
   Configs for topic '__consumer_offsets' are segment.bytes=104857600,cleanup.policy=compact,compression.type=producer
   Configs for topic 'epointlog3' are
   ```

5. 由于没有指定主题名，会将所有的主题信息都给显示出来，可以指定一个 topics：

   ```sh
   kafka-configs.sh --entity-type topics --describe --zookeeper 192.168.186.40:24002/kafka --entity-name xumaosheng
   # Configs for topic 'xumaosheng' are
   ```

   此时显示了 `xumaosheng` 主题的信息，这里是空

通过 Kafka 命令提示，可以很方便的通过提示信息来进行下一步操作。