# Kafka 示例代码

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
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.184.42:9092");
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
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.186.142:9092");
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