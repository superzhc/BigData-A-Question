# 第三方 connector

## Kafka

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
dss.addSink(new FlinkKafkaProducer011<Metrics>(
        parameterTool.get("kafka.sink.brokers"),
        parameterTool.get("kafka.sink.topic"),
        new MetricSchema()
        )).name("flink-connectors-kafka")
        .setParallelism(parameterTool.getInt("stream.sink.parallelism"));
```

## Elasticsearch

**引入依赖**

```xml
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-connector-elasticsearch6_${scala.binary.version}</artifactId>
    <version>${flink.version}</version>
</dependency>
```

**代码示例**

```java
ElasticsearchSink.Builder<T> esSinkBuilder = new ElasticsearchSink.Builder<>(hosts, 
    (Metrics metric, RuntimeContext runtimeContext, RequestIndexer requestIndexer) -> {
        requestIndexer.add(Requests.indexRequest()
                .index("test_" + metric.getName())  //es 索引名
                .type("_doc") //es type
                .source(GsonUtil.toJSONBytes(metric), XContentType.JSON)); 
    });
esSinkBuilder.setBulkFlushMaxActions(bulkFlushMaxActions);
dss.addSink(esSinkBuilder.build()).setParallelism(parallelism);
```

**常用的配置项**

1. `bulk.flush.backoff.enable`: 用来表示是否开启重试机制
2. `bulk.flush.backoff.type`: 重试策略，有两种：EXPONENTIAL 指数型（表示多次重试之间的时间间隔按照指数方式进行增长）、CONSTANT 常数型（表示多次重试之间的时间间隔为固定常数）
3. `bulk.flush.backoff.delay`: 进行重试的时间间隔
4. `bulk.flush.backoff.retries`: 失败重试的次数
5. `bulk.flush.max.actions`: 批量写入时的最大写入条数
6. `bulk.flush.max.size.mb`: 批量写入时的最大数据量
7. `bulk.flush.interval.ms`: 批量写入的时间间隔，配置后则会按照该时间间隔严格执行，无视上面的两个批量写入配置

## Redis

**代码示例**

```java
// 创建Redis的配置
FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("localhost").setPort(6379).build();
// 创建Redis的Sink
RedisSink<Tuple2<String,String>> redisSink = new RedisSink<>(conf,new RedisMapper<Tuple2<String,String>>{
    ...
});
dss.addSink(redisSink);
```