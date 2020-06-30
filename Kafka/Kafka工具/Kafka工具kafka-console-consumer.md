> `kafka-console-consumer.sh`命令只是简单的将消息输出到标准输出中。

该命令支持的参数如下：

```sh
Option                                   Description
------                                   -----------
--blacklist <String: blacklist>          Blacklist of topics to exclude from
                                           consumption.
--bootstrap-server <String: server to    REQUIRED (unless old consumer is
  connect to>                              used): The server to connect to.
--consumer-property <String:             A mechanism to pass user-defined
  consumer_prop>                           properties in the form key=value to
                                           the consumer.
--consumer.config <String: config file>  Consumer config properties file. Note
                                           that [consumer-property] takes
                                           precedence over this config.
--csv-reporter-enabled                   If set, the CSV metrics reporter will
                                           be enabled
--delete-consumer-offsets                If specified, the consumer path in
                                           zookeeper is deleted when starting up
--enable-systest-events                  Log lifecycle events of the consumer
                                           in addition to logging consumed
                                           messages. (This is specific for
                                           system tests.)
--formatter <String: class>              The name of a class to use for
                                           formatting kafka messages for
                                           display. (default: kafka.tools.
                                           DefaultMessageFormatter)
--from-beginning                         If the consumer does not already have
                                           an established offset to consume
                                           from, start with the earliest
                                           message present in the log rather
                                           than the latest message.
--isolation-level <String>               Set to read_committed in order to
                                           filter out transactional messages
                                           which are not committed. Set to
                                           read_uncommittedto read all
                                           messages. (default: read_uncommitted)
--key-deserializer <String:
  deserializer for key>
--max-messages <Integer: num_messages>   The maximum number of messages to
                                           consume before exiting. If not set,
                                           consumption is continual.
--metrics-dir <String: metrics           If csv-reporter-enable is set, and
  directory>                               this parameter isset, the csv
                                           metrics will be output here
--new-consumer                           Use the new consumer implementation.
                                           This is the default.
--offset <String: consume offset>        The offset id to consume from (a non-
                                           negative number), or 'earliest'
                                           which means from beginning, or
                                           'latest' which means from end
                                           (default: latest)
--partition <Integer: partition>         The partition to consume from.
                                           Consumption starts from the end of
                                           the partition unless '--offset' is
                                           specified.
--property <String: prop>                The properties to initialize the
                                           message formatter.
--skip-message-on-error                  If there is an error when processing a
                                           message, skip it instead of halt.
--timeout-ms <Integer: timeout_ms>       If specified, exit if no message is
                                           available for consumption for the
                                           specified interval.
--topic <String: topic>                  The topic id to consume on.
--value-deserializer <String:
  deserializer for values>
--whitelist <String: whitelist>          Whitelist of topics to include for
                                           consumption.
--zookeeper <String: urls>               REQUIRED (only when using old
                                           consumer): The connection string for
                                           the zookeeper connection in the form
                                           host:port. Multiple URLS can be
                                           given to allow fail-over.

-----------------------------------------------------
The console consumer is a tool that reads data from Kafka and outputs it to standard output.
-----------------------------------------------------

Command Usage:
-----------------------------------------------------
 old-consumer:
     kafka-console-consumer.sh --zookeeper <ZK_IP1:ZK_PORT,ZK_IP2:ZK_PORT,.../kafka> --topic <topic name> --from-beginning
-----------------------------------------------------
 new-consumer:
     kafka-console-consumer.sh --topic <topic name> --bootstrap-server <IP1:PORT, IP2:PORT,...> --new-consumer --consumer.config <config file>
-----------------------------------------------------
```

`--bootstrap-server` 必须指定，通常 `--topic` 也要指定查看主题。如果想要从头查看消息，还可以指定 `--from-beginning` 参数。一般使用的命令如下：

```sh
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```

还可以通过下面的命令指定分区查看：

```sh
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning --partition 0
```

