> `kafka-console-producer.sh`命令可以将标准输入的内容发送到 Kafka 集群。

该命令参数如下：

```sh
Option                                   Description
------                                   -----------
--batch-size <Integer: size>             Number of messages to send in a single
                                           batch if they are not being sent
                                           synchronously. (default: 200)
--broker-list <String: broker-list>      REQUIRED: The broker list string in
                                           the form HOST1:PORT1,HOST2:PORT2.
--compression-codec [String:             The compression codec: either 'none',
  compression-codec]                       'gzip', 'snappy', or 'lz4'.If
                                           specified without value, then it
                                           defaults to 'gzip'
--key-serializer <String:                The class name of the message encoder
  encoder_class>                           implementation to use for
                                           serializing keys. (default: kafka.
                                           serializer.DefaultEncoder)
--line-reader <String: reader_class>     The class name of the class to use for
                                           reading lines from standard in. By
                                           default each line is read as a
                                           separate message. (default: kafka.
                                           tools.
                                           ConsoleProducer$LineMessageReader)
--max-block-ms <Long: max block on       The max time that the producer will
  send>                                    block for during a send request
                                           (default: 60000)
--max-memory-bytes <Long: total memory   The total memory used by the producer
  in bytes>                                to buffer records waiting to be sent
                                           to the server. (default: 33554432)
--max-partition-memory-bytes <Long:      The buffer size allocated for a
  memory in bytes per partition>           partition. When records are received
                                           which are smaller than this size the
                                           producer will attempt to
                                           optimistically group them together
                                           until this size is reached.
                                           (default: 16384)
--message-send-max-retries <Integer>     Brokers can fail receiving the message
                                           for multiple reasons, and being
                                           unavailable transiently is just one
                                           of them. This property specifies the
                                           number of retires before the
                                           producer give up and drop this
                                           message. (default: 3)
--metadata-expiry-ms <Long: metadata     The period of time in milliseconds
  expiration interval>                     after which we force a refresh of
                                           metadata even if we haven't seen any
                                           leadership changes. (default: 300000)
--old-producer                           Use the old producer implementation.
--producer-property <String:             A mechanism to pass user-defined
  producer_prop>                           properties in the form key=value to
                                           the producer.
--producer.config <String: config file>  Producer config properties file. Note
                                           that [producer-property] takes
                                           precedence over this config.
--property <String: prop>                A mechanism to pass user-defined
                                           properties in the form key=value to
                                           the message reader. This allows
                                           custom configuration for a user-
                                           defined message reader.
--queue-enqueuetimeout-ms <Integer:      Timeout for event enqueue (default:
  queue enqueuetimeout ms>                 2147483647)
--queue-size <Integer: queue_size>       If set and the producer is running in
                                           asynchronous mode, this gives the
                                           maximum amount of  messages will
                                           queue awaiting sufficient batch
                                           size. (default: 10000)
--request-required-acks <String:         The required acks of the producer
  request required acks>                   requests (default: 1)
--request-timeout-ms <Integer: request   The ack timeout of the producer
  timeout ms>                              requests. Value must be non-negative
                                           and non-zero (default: 1500)
--retry-backoff-ms <Integer>             Before each retry, the producer
                                           refreshes the metadata of relevant
                                           topics. Since leader election takes
                                           a bit of time, this property
                                           specifies the amount of time that
                                           the producer waits before refreshing
                                           the metadata. (default: 100)
--socket-buffer-size <Integer: size>     The size of the tcp RECV size.
                                           (default: 102400)
--sync                                   If set message send requests to the
                                           brokers are synchronously, one at a
                                           time as they arrive.
--timeout <Integer: timeout_ms>          If set and the producer is running in
                                           asynchronous mode, this gives the
                                           maximum amount of time a message
                                           will queue awaiting sufficient batch
                                           size. The value is given in ms.
                                           (default: 1000)
--topic <String: topic>                  REQUIRED: The topic id to produce
                                           messages to.
--value-serializer <String:              The class name of the message encoder
  encoder_class>                           implementation to use for
                                           serializing values. (default: kafka.
                                           serializer.DefaultEncoder)

-----------------------------------------------------
Read data from standard input and publish it to Kafka.
-----------------------------------------------------

Command Usage:
-----------------------------------------------------
 old-producer:
     kafka-console-producer.sh --broker-list <IP1:PORT, IP2:PORT,...> --topic <topic name> --old-producer --sync
-----------------------------------------------------
 new-producer:
     kafka-console-producer.sh --broker-list <IP1:PORT, IP2:PORT,...> --topic <topic name> --producer.config <config file>
-----------------------------------------------------
```

其中 `--broker-list` 和 `--topic` 是两个必须提供的参数

使用标准输入方式：

```sh
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
```

注：使用 `ctrl+D`完成消息的发送

从文件读取：

```sh
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test < file-input.txt
```

