# Source

## Kafka

### 引入依赖

```xml
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-kafka-0.11_2.11</artifactId>
	<version>${flink.version}</version>
</dependency>
```

### 代码示例

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

DataStream<KafkaEvent> input = env
		.addSource(
			new FlinkKafkaConsumer011<>(
				parameterTool.getRequired("input-topic"), //从参数中获取传进来的 topic 
				new KafkaEventSchema(),
				parameterTool.getProperties())
			.assignTimestampsAndWatermarks(new CustomWatermarkExtractor()));
```