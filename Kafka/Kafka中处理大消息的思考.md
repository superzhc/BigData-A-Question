Kafka设计的初衷是迅速处理短小的消息，一般<font color="red">**10K**</font>大小的消息吞吐性能最好（可参见LinkedIn的kafka性能测试）。但有时候，需要处理更大的消息，比如XML文档或JSON内容，一个消息差不多有10-100M，这种情况下，Kakfa应该如何处理？

针对这个问题，有以下几个建议：

1. 最好的方法是不直接传送这些大的数据。如果有共享存储，如NAS, HDFS, S3等，可以把这些大的文件存放到共享存储，然后使用Kafka来传送文件的位置信息。
2. 第二个方法是，将大的消息数据切片或切块，在生产端将数据切片为10K大小，使用分区主键确保一个大消息的所有部分会被发送到同一个kafka分区（这样每一部分的拆分顺序得以保留），如此以来，当消费端使用时会将这些部分重新还原为原始的消息。
3. 第三，Kafka的生产端可以压缩消息，如果原始消息是XML，当通过压缩之后，消息可能会变得不那么大。在生产端的配置参数中使用compression.codec和commpressed.topics可以开启压缩功能，压缩算法可以使用GZip或Snappy。

不过如果上述方法都不是你需要的，而你最终还是希望传送大的消息，那么，则可以在kafka中设置下面一些参数：

**broker配置**:

1. `message.max.bytes` (默认:1000000) – broker能接收消息的最大字节数，这个值应该比消费端的`fetch.message.max.bytes`更小才对，否则broker就会因为消费端无法使用这个消息而挂起。
2. `log.segment.bytes` (默认: 1GB) – kafka数据文件的大小，确保这个数值大于一个消息的长度。一般说来使用默认值即可（一般一个消息很难大于1G，因为这是一个消息系统，而不是文件系统）。
3. `replica.fetch.max.bytes` (默认: 1MB) – broker可复制的消息的最大字节数。这个值应该比`message.max.bytes`大，否则broker会接收此消息，但无法将此消息复制出去，从而造成数据丢失。

**Consumer配置**:

- `fetch.message.max.bytes` (默认 1MB) – 消费者能读取的最大消息。这个值应该大于或等于`message.max.bytes`。

所以，如果你一定要选择kafka来传送大的消息，还有些事项需要考虑。要传送大的消息，不是当出现问题之后再来考虑如何解决，而是在一开始设计的时候，就要考虑到大消息对集群和主题的影响。

- **性能**:根据前面提到的性能测试，kafka在消息为10K时吞吐量达到最大，更大的消息会降低吞吐量，在设计集群的容量时，尤其要考虑这点。
- **可用的内存和分区数**：Brokers会为每个分区分配`replica.fetch.max.bytes`参数指定的内存空间，假设`replica.fetch.max.bytes=1M`，且有1000个分区，则需要差不多1G的内存，确保 `分区数*最大的消息`不会超过服务器的内存，否则会报OOM错误。同样地，消费端的`fetch.message.max.bytes`指定了最大消息需要的内存空间，同样，`分区数*最大需要内存空间` 不能超过服务器的内存。所以，如果你有大的消息要传送，则在内存一定的情况下，只能使用较少的分区数或者使用更大内存的服务器。
- **垃圾回收**：到现在为止，我在kafka的使用中还没发现过此问题，但这应该是一个需要考虑的潜在问题。更大的消息会让GC的时间更长（因为broker需要分配更大的块），随时关注GC的日志和服务器的日志信息。如果长时间的GC导致kafka丢失了zookeeper的会话，则需要配置zookeeper.session.timeout.ms参数为更大的超时时间。

一切的一切，都需要在权衡利弊之后，再决定选用哪个最合适的方案。