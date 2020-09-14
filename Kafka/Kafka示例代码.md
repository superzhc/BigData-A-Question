# Kafka 示例代码

中文文档：<http://kafka.apachecn.org/>

## 引入依赖包

> 目前我使用的版本是 `2.3.0`，不同的依赖版本的 API 会有所不同，请注意

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.3.0</version>
</dependency>
```

## 生产者

```java
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaProducerDemo
{
    public static void main(String[] args) throws InterruptedException {
        Properties properties = new Properties();

        /*设置集群kafka的ip地址和端口号，可以只写集群中的任一一个broker，会自动寻找其他的broker*/
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        /*对key进行序列化*/
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerSerializer");
        /*对value进行序列化*/
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        /*创建一个kafka生产者*/
        KafkaProducer<Integer, String> kafkaProducer = new KafkaProducer<Integer, String>(properties);
        /*主题*/
        String topic = "test";
        /*循环发送数据*/
        for (int i = 0; i < 20; i++) {
            /*发送的消息*/
            String message = "我是一条信息" + i;
            /*发出消息*/
            kafkaProducer.send(new ProducerRecord<>(topic, message));
            System.out.println(message + "->已发送");
            Thread.sleep(1000);
        }
    }
}
```

## 消费者

```java
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerDemo
{
    public static void main(String[] args) {
        Properties props = new Properties();
        /*设置集群kafka的ip地址和端口号*/
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        /*设置消费者的group.id*/
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerDemo1");
        /*消费信息以后自动提交*/
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        /*控制消费者提交的频率*/
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        /*key反序列化*/
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerDeserializer");
        /*value反序列化*/
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        /*创建kafka消费者对象*/
        KafkaConsumer<Integer, String> consumer = new KafkaConsumer<Integer, String>(props);
        /*订阅主题*/
        String topic = "test";
        consumer.subscribe(Collections.singleton(topic));

        /*每隔一段时间到服务器拉取数据*/
        ConsumerRecords<Integer, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord record : consumerRecords) {
            System.out.println(record.value());
        }
    }
}
```

## 主题

```java
public class EpointKafkaAdminClient implements Closeable
{
    // 客户端实例
    private AdminClient adminClient = null;

    public EpointKafkaAdminClient(String brokerServers) {
        // this.brokerServers = brokerServers;

        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, brokerServers);
        this.adminClient = AdminClient.create(props);
    }

    /**
     * 关闭AdminClient连接
     */
    public void close() {
        if (null != adminClient)
            adminClient.close();
    }

    // regionbroker操作=================================================================================================
    public Integer getBrokerNums() throws Exception {
        return adminClient.describeCluster().nodes().get(timeout, TimeUnit.SECONDS).size();
    }
    // endregionbroker操作=============================================================================================

    // region主题（Topic）增删改查======================================================================================
    /**
     * 创建主题
     * @param topic 主题名
     * @param partitions 分区数
     * @param replication 副本系数
     * @param configs Topic的配置
     * @throws Exception
     */
    public void create(String topic, int partitions, int replication, Map<String, String> configs) throws Exception {
        if (replication > getBrokerNums())
            throw new RuntimeException("副本系数不能大于broker节点数量");

        short replication_short = (short) replication;
        NewTopic newTopic = new NewTopic(topic, partitions, replication_short);
        if (null != configs && configs.size() > 0)
            newTopic.configs(configs);
        CreateTopicsResult result = adminClient.createTopics(Arrays.asList(newTopic));
        result.all().get(timeout, TimeUnit.SECONDS);
    }

    /**
     * 创建主题
     * @param topic 主题名
     * @param partitions 分区数
     * @param replication 副本系数
     * @throws Exception
     */
    public void create(String topic, int partitions, int replication) throws Exception {
        create(topic, partitions, replication, null);
    }

    /**
     * 删除主题
     * @param topic 主题名
     * @throws Exception
     */
    public void delete(String topic) throws Exception {
        // 服务端server.properties需要设置delete.topic.enable=true，才可以使用同步删除，否则只是将主题标记为删除
        DeleteTopicsResult result = adminClient.deleteTopics(Arrays.asList(topic));
        // 同步操作出现超时情况，见上配置是否配置了
        // note 为了更好的兼容性，此处不提供同步获取删除结果
        // result.all().get();
    }

    /**
     * 修改主题
     * @param topic 主题名
     * @param upsertProperties 新增/更新的属性
     * @param deleteProperties 删除的属性
     * @throws Exception
     */
    public void update(String topic, Map<String, String> upsertProperties, List<String> deleteProperties)
            throws Exception {
        List<AlterConfigOp> alterConfigOps = new ArrayList<>();
        if (null != upsertProperties && upsertProperties.size() > 0) {
            for (Map.Entry<String, String> property : upsertProperties.entrySet()) {
                ConfigEntry configEntry = new ConfigEntry(property.getKey(), property.getValue());
                AlterConfigOp alterConfigOp = new AlterConfigOp(configEntry, AlterConfigOp.OpType.SET);
                alterConfigOps.add(alterConfigOp);
            }
        }
        if (null != deleteProperties && deleteProperties.size() > 0) {
            for (String deleteProperty : deleteProperties) {
                ConfigEntry configEntry = new ConfigEntry(deleteProperty, null);
                AlterConfigOp alterConfigOp = new AlterConfigOp(configEntry, AlterConfigOp.OpType.DELETE);
                alterConfigOps.add(alterConfigOp);
            }
        }

        update(topic, alterConfigOps);
    }

    public void update(String topic, List<AlterConfigOp> alterConfigOps) throws Exception {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
        Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();
        configs.put(resource, alterConfigOps);
        adminClient.incrementalAlterConfigs(configs).all().get(timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取当前kafka集群下的所有主题名称
     * @return
     * @throws Exception
     */
    public Set<String> list() throws Exception {
        ListTopicsResult listTopicsResult = adminClient.listTopics();
        Set<String> topics = listTopicsResult.names().get(timeout, TimeUnit.SECONDS);
        return topics;
    }

    /**
     * 判断主题是否存在
     * @param topic 主题名
     * @return
     * @throws Exception
     */
    public boolean exist(String topic) throws Exception {
        Set<String> topics = list();
        return topics.contains(topic);
    }

    /**
     * 获取主题的描述信息
     * @param topic 主题名
     * @return
     * @throws Exception
     */
    public TopicDescription describe(String topic) throws Exception {
        TopicDescription description = adminClient.describeTopics(Arrays.asList(topic)).all()
                .get(timeout, TimeUnit.SECONDS).get(topic);
        return description;
    }
    // endregion主题（Topic）增删改查===================================================================================

    // region主题的分区=================================================================================================
    /**
     * 主题下的所有分区
     * @param topic 主题名
     * @return
     * @throws Exception
     */
    public List<Integer> partitions(String topic) throws Exception {
        List<TopicPartitionInfo> partitionInfos = describe(topic).partitions();
        List<Integer> result = new ArrayList<>();
        for (TopicPartitionInfo partitionInfo : partitionInfos) {
            result.add(partitionInfo.partition());
        }
        return result;
    }

    /**
     * 主题下的所有分区
     * @param topic
     * @return
     * @throws Exception
     */
    public List<TopicPartition> topicPartitions(String topic) throws Exception {
        List<TopicPartitionInfo> partitionInfos = describe(topic).partitions();
        List<TopicPartition> result = new ArrayList<>();
        for (TopicPartitionInfo partitionInfo : partitionInfos) {
            result.add(new TopicPartition(topic, partitionInfo.partition()));
        }
        return result;
    }

    /**
     * 添加分区数
     * @param topic 主题名
     * @param numPartitions 原有的分区数+新增分区数量
     */
    public void addPartitions(String topic, Integer numPartitions) throws Exception {
        NewPartitions newPartitions = NewPartitions.increaseTo(numPartitions);
        Map<String, NewPartitions> map = new HashMap<>(1, 1);
        map.put(topic, newPartitions);
        adminClient.createPartitions(map).all().get(timeout, TimeUnit.SECONDS);
    }
    // endregion主题的分区==============================================================================================
}
```