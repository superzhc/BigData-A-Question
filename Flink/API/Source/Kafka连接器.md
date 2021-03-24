# Apache Kafka Connector

> Flink 提供了 Apache Kafka 连接器，用于从 Kafka topic 中读取或者向其中写入数据，可提供精确一次的处理语义。

## 依赖

Apache Flink 集成了通用的 Kafka 连接器，它会尽力与 Kafka client 的最新版本保持同步。该连接器使用的 Kafka client 版本可能会在 Flink 版本之间发生变化。 当前 Kafka client 向后兼容 0.10.0 或更高版本的 Kafka broker。

```xml
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-kafka_2.11</artifactId>
	<version>1.12.0</version>
</dependency>
```

## Kafka Consumer

Flink 的 Kafka consumer 称为 FlinkKafkaConsumer。它提供对一个或多个 Kafka topics 的访问。

构造函数接受以下参数：

1. Topic 名称或者名称列表
2. 用于反序列化 Kafka 数据的 DeserializationSchema 或者 KafkaDeserializationSchema
3. Kafka 消费者的属性。需要以下属性：
    - `bootstrap.servers`（以逗号分隔的 Kafka broker 列表）
    - `group.id` 消费组 ID

```java
Properties properties = new Properties();
properties.setProperty("bootstrap.servers", "localhost:9092");
properties.setProperty("group.id", "test");
DataStream<String> stream = env
    .addSource(new FlinkKafkaConsumer<>("topic", new SimpleStringSchema(), properties));
```

### Topic

Flink Kafka Consumer 能够使用正则表达式基于 Topic 名称的模式匹配来读取多个 Topic，如下示例：

```java
final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

Properties properties = new Properties();
properties.setProperty("bootstrap.servers", "localhost:9092");
properties.setProperty("group.id", "test");

FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<>(
    java.util.regex.Pattern.compile("test-topic-[0-9]"),
    new SimpleStringSchema(),
    properties);

DataStream<String> stream = env.addSource(myConsumer);
```

### 反序列化

Flink Kafka Consumer 需要知道如何将 Kafka 中的二进制数据转换为 Java 或者 Scala 对象。KafkaDeserializationSchema 允许用户指定这样的 schema，每条 Kafka 中的消息会调用 `T deserialize(ConsumerRecord<byte[], byte[]> record)` 反序列化。

为了方便使用，Flink 提供了以下几种 schemas：

1. TypeInformationSerializationSchema（和 TypeInformationKeyValueSerializationSchema) 基于 Flink 的 TypeInformation 创建 schema。 如果该数据的读和写都发生在 Flink 中，那么这将是非常有用的。此 schema 是其他通用序列化方法的高性能 Flink 替代方案。
2. JsonDeserializationSchema（和 JSONKeyValueDeserializationSchema）将序列化的 JSON 转化为 ObjectNode 对象，可以使用 `objectNode.get("field").as(Int/String/...)()` 来访问某个字段。 KeyValue objectNode 包含一个含所有字段的 key 和 values 字段，以及一个可选的”metadata”字段，可以访问到消息的 offset、partition、topic 等信息。
3. AvroDeserializationSchema 使用静态提供的 schema 读取 Avro 格式的序列化数据。 它能够从 Avro 生成的类（`AvroDeserializationSchema.forSpecific(...)`）中推断出 schema，或者可以与 GenericRecords 一起使用手动提供的 schema（用 `AvroDeserializationSchema.forGeneric(...)`）。
   <br/>注：要使用此反序列化 schema 必须添加以下依赖：
   ```xml
   <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-avro</artifactId>
        <version>1.12.0</version>
   </dependency>
   ```

当遇到因一些原因而无法反序列化的损坏消息时，反序列化 schema 会返回 null，以允许 Flink Kafka 消费者悄悄地跳过损坏的消息。

**使用 kafka 接口自定义 KafkaDeserializationSchema 获取分区数据**

在使用 SimpleStringSchema 的时候，返回的结果只有 Kafka 的 value，而没有其它信息。很多时候需要获得 Kafka 的 topic 或者其它信息，就需要通过实现 KafkaDeserializationSchema 接口来自定义返回数据的结构，以下代码会让 kafka 消费者返回了 ConsumerRecord 类型的数据，可以通过这个对象获取包括 topic，offset，分区等信息，同时也能获取对应的数据值：

```java
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

public class ObjDeSerializationSchema  implements KafkaDeserializationSchema<ConsumerRecord<String, String>>{
    private static  String encoding = "UTF8";
    @Override
    public boolean isEndOfStream(ConsumerRecord<String, String> nextElement) {
        return false;
    }
    @Override
    public ConsumerRecord<String, String> deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
//        System.out.println("Record--partition::"+record.partition());
//        System.out.println("Record--offset::"+record.offset());
//        System.out.println("Record--timestamp::"+record.timestamp());
//        System.out.println("Record--timestampType::"+record.timestampType());
//        System.out.println("Record--checksum::"+record.checksum());
//        System.out.println("Record--key::"+record.key());
//        System.out.println("Record--value::"+record.value());
        String key=null;
        String value = null;
        if (record.key() != null) {
            key = new String(record.key());
        }
        if (record.value() != null) {
            value = new String(record.value());
        }

        return new ConsumerRecord(
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp(),
                record.timestampType(),
                record.checksum(),
                record.serializedKeySize(),
                record.serializedValueSize(),
                key,
                value);
    }

    @Override
    public TypeInformation<ConsumerRecord<String, String>> getProducedType() {
        return TypeInformation.of(new TypeHint<ConsumerRecord<String, String>>(){});
    }
}
```

### 配置 Kafka Consumer 开始消费的位置

Flink Kafka Consumer 允许通过配置来确定 Kafka 分区的起始位置。

```java
final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<>(...);
myConsumer.setStartFromEarliest();     // 尽可能从最早的记录开始
myConsumer.setStartFromLatest();       // 从最新的记录开始
myConsumer.setStartFromTimestamp(...); // 从指定的时间开始（毫秒）
myConsumer.setStartFromGroupOffsets(); // 默认的方法

DataStream<String> stream = env.addSource(myConsumer);
```

Flink Kafka Consumer 的所有版本都具有上述明确的起始位置配置方法。

- `setStartFromGroupOffsets`（默认方法）：从 Kafka brokers 中的 consumer 组（consumer 属性中的 `group.id` 设置）提交的偏移量中开始读取分区。 如果找不到分区的偏移量，那么将会使用配置中的 `auto.offset.reset` 设置。
- `setStartFromEarliest()` 或者 `setStartFromLatest()`：从最早或者最新的记录开始消费，在这些模式下，Kafka 中的 committed offset 将被忽略，不会用作起始位置。
- `setStartFromTimestamp(long)`：从指定的时间戳开始。对于每个分区，其时间戳大于或等于指定时间戳的记录将用作起始位置。如果一个分区的最新记录早于指定的时间戳，则只从最新记录读取该分区数据。在这种模式下，Kafka 中的已提交 offset 将被忽略，不会用作起始位置。

也可以为每个分区指定 consumer 应该开始消费的具体 offset：

```java
Map<KafkaTopicPartition, Long> specificStartOffsets = new HashMap<>();
specificStartOffsets.put(new KafkaTopicPartition("myTopic", 0), 23L);
specificStartOffsets.put(new KafkaTopicPartition("myTopic", 1), 31L);
specificStartOffsets.put(new KafkaTopicPartition("myTopic", 2), 43L);

myConsumer.setStartFromSpecificOffsets(specificStartOffsets);
```