# 输入 DStreams

输入 DStreams 表示从数据源获取输入数据流的 DStreams。每一个输入流 DStream 和一个 `Receiver` 对象相关联，这个 `Receiver` 从源中获取数据，并将数据存入内存中用于处理。

输入 DStreams 表示从数据源获取的原始数据流。Spark Streaming 拥有两类数据源：

- 基本源（Basic sources）：这些源在 StreamingContext API 中直接可用。例如文件系统、套接字连接、Akka 的 actor 等。
- 高级源（Advanced sources）：这些源包括 Kafka,Flume,Kinesis,Twitter 等等。它们需要通过额外的类来使用。

注意：可以在一个流应用中并行地创建多个输入 DStream 来接收多个数据流，这将创建多个 Receiver 同时接收多个数据流。但是，`Receiver` 作为一个长期运行的任务运行在 Spark worker 或 executor 中。因此，它占有一个核，这个核是分配给 Spark Streaming 应用程序的所有核中的一个。所以，为 Spark Streaming 应用程序分配足够的核（如果是本地运行，那么是线程）用以处理接收的数据并且运行 `Receiver` 是非常重要的。

几点需要注意的地方：

- 如果分配给应用程序的核的数量少于或者等于输入 DStreams 或者 Receivers 的数量，系统只能够接收数据而不能处理它们。
- 当运行在本地，如果用户的 master URL 被设置成了“local”，这样就只有一个核运行任务。这对程序来说是不足的，因为作为 `Receiver` 的输入 DStream 将会占用这个核，这样就没有剩余的核来处理数据了。