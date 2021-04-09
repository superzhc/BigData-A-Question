# Source

## 内置实现

Flink 针对 DataStream 提供了大量的已经实现的 DataSource（数据源）接口，比如下面 4 种：

1. 基于文件
   > 适合监听文件修改并读取其内容
2. 基于 Socket
   > 监听主机的 host port，从 Socket 中获取数据
3. 基于集合
   > 有界数据集，更偏向于本地测试用
4. 自定义输入
   ```java
   //addSource 可以实现读取第三方数据源的数据
   env.addSource(sourceFunction)
   ```

### 基于文件

Flink 系统支持将文件内容读取到系统中，并转换成分布式数据集 DataStream 进行数据处理。

1. `readTextFile(path)` - 读取文本文件，即符合 TextInputFormat 规范的文件，并将其作为字符串返回。
2. `readFile(fileInputFormat, path)` - 根据指定的文件输入格式读取文件（一次）。
3. `readFile(fileInputFormat, path, watchType, interval, pathFilter, typeInfo)` - 这是上面两个方法内部调用的方法。它根据给定的 fileInputFormat 和读取路径读取文件。根据提供的 watchType，这个 source 可以定期（每隔 interval 毫秒）监测给定路径的新数据（FileProcessingMode.PROCESS_CONTINUOUSLY），或者处理一次路径对应文件的数据并退出（FileProcessingMode.PROCESS_ONCE）。你可以通过 pathFilter 进一步排除掉需要处理的文件。

示例：

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

// 读取文本文件
DataStream<String> text = env.readTextFile("file:///path/to/file");

// 根据指定的fileInputFormat读取文件
DataStream<MyEvent> stream = env.readFile(
        myFormat, myFilePath, FileProcessingMode.PROCESS_CONTINUOUSLY, 100,
        FilePathFilter.createDefaultFilter(), typeInfo);
```

实现:

在具体实现上，Flink 把文件读取过程分为两个子任务，即目录监控和数据读取。每个子任务都由单独的实体实现。目录监控由单个非并行（并行度为1）的任务执行，而数据读取由并行运行的多个任务执行。后者的并行性等于作业的并行性。单个目录监控任务的作用是扫描目录（根据 watchType 定期扫描或仅扫描一次），查找要处理的文件并把文件分割成切分片（splits），然后将这些切分片分配给下游 reader。reader 负责读取数据。每个切分片只能由一个 reader 读取，但一个 reader 可以逐个读取多个切分片。

重要注意：

- 如果 watchType 设置为 `FileProcessingMode.PROCESS_CONTINUOUSLY`，则当文件被修改时，其内容将被重新处理。这会打破“exactly-once”语义，因为在文件末尾附加数据将导致其所有内容被重新处理。
- 如果 watchType 设置为 `FileProcessingMode.PROCESS_ONCE`，则 source 仅扫描路径一次然后退出，而不等待 reader 完成文件内容的读取。当然 reader 会继续阅读，直到读取所有的文件内容。关闭 source 后就不会再有检查点。这可能导致节点故障后的恢复速度较慢，因为该作业将从最后一个检查点恢复读取。

### 基于 Socket

- `socketTextStream(String hostname, int port)` - 从 socket 读取。元素可以用分隔符切分。

示例：

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

DataStream<String> dataStream = env.socketTextStream("localhost", 9999); // 监听 localhost 的 9999 端口过来的数据
```

### 基于集合

1. `fromCollection(Collection)` - 从 Java 的 `Java.util.Collection` 创建数据流。集合中的所有元素类型必须相同。
2. `fromCollection(Iterator, Class)` - 从一个迭代器中创建数据流。Class 指定了该迭代器返回元素的类型。
3. `fromElements(T …)` - 从给定的对象序列中创建数据流。所有对象类型必须相同。
4. `fromParallelCollection(SplittableIterator, Class)` - 从一个迭代器中创建并行数据流。Class 指定了该迭代器返回元素的类型。
5. `generateSequence(from, to)` - 创建一个生成指定区间范围内的数字序列的并行数据流。

示例：

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

DataStream<Event> input = env.fromElements(
	new Event(1, "barfoo", 1.0),
	new Event(2, "start", 2.0),
	new Event(3, "foobar", 3.0),
	...
);
```

### 自定义输入

Flink 也提供了一批内置的 Connector（连接器）。连接器会提供对应的 Source 支持，如下表所示：

| 连接器                      | 是否提供 Source 支持 | 是否提供 Sink 支持 |
| --------------------------- | -------------------- | ------------------ |
| Apache Kafka                | 是                   | 是                 |
| Apache Cassandra            | 否                   | 是                 |
| Amazon Kinesis Data Streams | 是                   | 是                 |
| Elasticsearch               | 否                   | 是                 |
| HDFS                        | 否                   | 是                 |
| RabbitMQ                    | 是                   | 是                 |
| Apache NiFi                 | 是                   | 是                 |
| Twitter Streaming API       | 是                   | 否                 |

Flink 通过 Apache Bahir 组件提供了对这些连接器的支持，如下表所示：

| 连接器          | 是否提供 Source 支持 | 是否提供 Sink 支持 |
| --------------- | -------------------- | ------------------ |
| Apache ActiveMQ | 是                   | 是                 |
| Apache Flume    | 否                   | 是                 |
| Redis           | 否                   | 是                 |
| Akka            | 否                   | 是                 |
| Netty           | 是                   | 否                 |

用户也可以自定义数据源，有两种实现方式：

- 通过实现 SourceFunction 接口来自定义无并行度（也就是并行度只能为 1）的数据源。
- 通过实现 ParallelSourceFunction 接口或者继承 RichParallelSourceFunction 来自定义有并行度的数据源。

## JDBC

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