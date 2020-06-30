## Kafka单条消息过大

查看kafka配置，**默认单条消息最大为1M**，当单条消息长度超过1M时，就会出现发送到broker失败，从而导致消息在producer的队列中一直累积，直到撑爆生产者的内存。

修改kafka配置可以解决此问题。主要修改步骤如下：

1. 修改kafka的broker配置：`message.max.bytes`（默认:1000000B），这个参数表示单条消息的最大长度。在使用kafka的时候，应该预估单条消息的最大长度，不然导致发送失败。
2. 修改kafka的broker配置：`replica.fetch.max.bytes` (默认: 1MB)，broker可复制的消息的最大字节数。这个值应该比`message.max.bytes`大，否则broker会接收此消息，但无法将此消息复制出去，从而造成数据丢失。
3. 修改消费者客户端配置：`fetch.message.max.bytes` (默认 1MB) – 消费者能读取的最大消息。这个值应该大于或等于`message.max.bytes`。*如果不调节这个参数，就会导致消费者无法消费到消息*，并且不会爆出异常或者警告，导致消息在broker中累积。

Kafka单条消息大小的性能：~~[kafka中处理超大消息的一些考虑](http://www.mamicode.com/info-detail-453907.html)~~ [kafka中处理超大消息的一些考虑](./Kafka中处理大消息的思考.md)