### 引入依赖

**Maven**

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka_2.12</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```gr
compile group: 'org.apache.kafka', name: 'kafka_2.12', version: '1.0.0'
```

### 创建主题

```java
AdminUtils.createTopic(zkClient, topic, partitions, replication, properties);
```

### 删除主题

```java
AdminUtils.deleteTopic(zkClient, topic);
```

### 修改主题

```java
Properties properties = AdminUtils.fetchTopicConfig(zkClient, topic);
// 修改topic的属性
// 分为：
// 1、新增：props.put("min.cleanable.dirty.ratio", "0.3");
// 2、删除：props.remove("max.message.bytes");
// 3、修改：props.put("min.cleanable.dirty.ratio", "0.3");
// 遍历修改的属性
for (Object key : newproperties.keySet()) {
    Object value = newproperties.get(key);
    if (properties.containsKey(key)) {// 已经存在属性
        if (null == value)// 删除
            properties.remove(key);
        else// 修改
            properties.put(key, value);
    }
    else {
        if (null != value)// 新增
            properties.put(key, value);
    }
}
AdminUtils.changeTopicConfig(zkClient, topic, properties);
```

### 获取所有主题

```java
List<String> topics = scala.collection.JavaConversions.seqAsJavaList(ZkUtils.getAllTopics(zkClient));
```

### 判断主题是否存在

```java
AdminUtils.topicExists(zkClient, topic);
```

### 获取主题的描述信息

```java
AdminUtils.fetchTopicConfig(zkClient, topic);
```

### 获取所有分区

```java
ZkUtils.getPartitionsForTopics(zkClient);
```

### 添加分区数

```java
AdminUtils.addPartitions(zkClient, topic, numPartitions, "", true, new Properties());
```

### 获取所有订阅某 Topic 的所有消费者组

```java
/**
* get all subscribing consumer group names for a given topic
* @param brokerListUrl localhost:9092 for instance
* @param topic         topic name
* @return
*/
public static Set<String> getAllGroupsForTopic(String brokerListUrl, String topic) {
    AdminClient client = AdminClient.createSimplePlaintext(brokerListUrl);

    try {
        List<GroupOverview> allGroups = scala.collection.JavaConversions.seqAsJavaList(client.listAllGroupsFlattened().toSeq());
        Set<String> groups = new HashSet<>();
        for (GroupOverview overview: allGroups) {
            String groupID = overview.groupId();
            Map<TopicPartition, Object> offsets = scala.collection.JavaConversions.mapAsJavaMap(client.listGroupOffsets(groupID));
            Set<TopicPartition> partitions = offsets.keySet();
            for (TopicPartition tp: partitions) {
                if (tp.topic().equals(topic)) {
                    groups.add(groupID);
                }
            }
        }
        return groups;
    } finally {
        client.close();
    }
}　
```

