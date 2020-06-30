### 引入依赖

**Maven**

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Gradle**

```
compile group: 'org.apache.kafka', name: 'kafka-clients', version: '2.2.0'
```

### 获取所有订阅某 Topic 的所有消费者组

```java
private static List<String> getGroupsForTopic(String brokerServers, String topic) 
            throws ExecutionException, InterruptedException, TimeoutException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerServers);

        try (AdminClient client = AdminClient.create(props)) {
            List<String> allGroups = client.listConsumerGroups()
                    .valid()
                    .get(10, TimeUnit.SECONDS)
                    .stream()
                    .map(ConsumerGroupListing::groupId)
                    .collect(Collectors.toList());

            Map<String, ConsumerGroupDescription> allGroupDetails =
                    client.describeConsumerGroups(allGroups).all().get(10, TimeUnit.SECONDS);

            final List<String> filteredGroups = new ArrayList<>();
            allGroupDetails.entrySet().forEach(entry -> {
                String groupId = entry.getKey();
                ConsumerGroupDescription description = entry.getValue();
                boolean topicSubscribed = description.members().stream().map(MemberDescription::assignment)
                        .map(MemberAssignment::topicPartitions)
                        .map(tps -> tps.stream().map(TopicPartition::topic).collect(Collectors.toSet()))
                        .anyMatch(tps -> tps.contains(topic));
                if (topicSubscribed)
                    filteredGroups.add(groupId);
            });
            return filteredGroups;
        }
    }
```

注意事项：

1. 假设集群中没有配置安全认证和授权机制或者发起此AdminClient的用户是合法用户且有CLUSTER以及GROUP的DESCRIBE权限
2. 上面这个函数无法获取非运行中的消费者组，即虽然一个group订阅了某topic，但是若它所有的consumer成员都关闭的话这个函数是不会返回该group的