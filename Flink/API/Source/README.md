# Source

## ~~JDBC~~

**引入依赖**

```xml
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-jdbc_2.11</artifactId>
	<version>${flink.version}</version>
</dependency>
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>8.0.15</version>
</dependency>
```

**代码示例**

```java

```

## [Kafka](Flink/API/Source/Kafka连接器.md)

**引入依赖**

```xml
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-kafka_2.11</artifactId>
	<version>${flink.version}</version>
</dependency>
```

**代码示例**

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, jsdz_brokers);
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 5000);
props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-flink-user:superz");
FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>("superz-test", new SimpleStringSchema(), props);
//setStartFromEarliest()会从最早的数据开始进行消费，忽略存储的offset信息
consumer.setStartFromEarliest();
//Flink从topic中指定的时间点开始消费，指定时间点之前的数据忽略
//consumer.setStartFromTimestamp(1559801580000L);
//Flink从topic中最新的数据开始消费
//consumer.setStartFromLatest();
//Flink从topic中指定的group上次消费的位置开始消费，所以必须配置group.id参数
//consumer.setStartFromGroupOffsets();
DataStream<String> sourceStream = env.addSource(consumer);
```

## Redis

**引入依赖**

```xml
<dependency>
	<groupId>org.apache.bahir</groupId>
	<artifactId>flink-connector-redis_2.11</artifactId>
	<version>1.0</version>
</dependency>
```

**代码示例**

```java

```

## RabbitMQ

**引入依赖**

```xml
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-rabbitmq_2.11</artifactId>
	<version>${flink.version}</version>
</dependency>
```

**代码示例**

```java

```

## Elasticsearch6

**引入依赖**

```xml
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-connector-elasticsearch6_2.11</artifactId>
	<version>${flink.version}</version>
</dependency>
<dependency>
	<groupId>org.apache.flink</groupId>
	<artifactId>flink-sql-connector-elasticsearch6_2.11</artifactId>
	<version>${flink.version}</version>
</dependency>
```

**代码示例**

```java

```