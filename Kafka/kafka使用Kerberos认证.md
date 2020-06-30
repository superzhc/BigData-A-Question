配置 Kafka Broker

1. 添加一个 JAAS 文件，例如，JAAS 文件命名为`kafka_server_jaas.conf`（注意每个 broker 都应该由自己的 keytab）

   ```conf
   KafkaServer{
   	com.sun.security.auth.module.Krb5LoginModule required
   	useKeyTab=true
   	storeKey=true
   	keyTab="/etc/security/keytabs/kafka_server.keytab"
   	principal="kafka/kafka1.hostname.com@EXAMPLE.COM";
   };
   
   // Zookeeper client authentication
   Client {
   	com.sun.security.auth.module.Krb5LoginModule required
   	useKeyTab=true
   	storeKey=true
   	keyTab="/etc/security/keytabs/kafka_server.keytab"
   	principal="kafka/kafka1.hostname.com@EXAMPLE.COM";
   };
   ```

   JAAS 文件中的 KafkaServer 告诉 broker 哪个 principal 要使用，以及存储该 principal 的 keytab 的位置。它允许 broker 使用指定的 keytab 进行登录

2. 通过 JAAS 和 krb5 文件位置（可选的）作为 JVM 参数传递到每个 broker

   ```
   -Djava.security.krb5.conf=/etc/kafka/krb5.conf
   -Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf
   ```

3. 确保在 JAAS 文件的 keytab 配置文件可被启动的 broker 的用户读取

4. 在 `server.properties` 中配置`SASL的端口`和`SASL机制`，例如：

   ```properties
   listeners=SASL_PLAINTEXT://host.name:port
   security.inter.broker.protocol=SASL_PLAINTEXT
   sasl.mechanism.inter.broker.protocol=GSSAPI
   sasl.enabled.mechanisms=GSSAPI
   ```

   还必须在`server.properties`配置服务器名称，应与 broker 的 principal 名匹配，在上面的示例中，principal 是 `kafka/kafka1.hostname.com@EXAMPLE.COM`，所以：

   ```properties
   sasl.kerberos.service.name=kafka
   ```



配置 Kafka Client

1. 客户端（生产者，消费者，connect 等等）用自己的 principal 进行集群认证（通常用相同名称作为运行客户端的用户）。因此，获取或根据需要创建这些这些 principal。然后为每个客户端配置 JAAS 配置。JVM 中的不同客户端通过指定不同的 principal 可以作为不同的用户运行。`producer.properties`或`consumer.properties`中的`sasl.jaas.config`描述了像生产者和消费者之类的客户端如何连接到 Kafka broker。以下是使用 keytab 的客户端的示例配置（推荐长时间运行的进程）：

   ```properties
    sasl.jaas.config=com.sun.security.auth.module.Krb5LoginModule required \
        useKeyTab=true \
        storeKey=true  \
        keyTab="/etc/security/keytabs/kafka_client.keytab" \
        principal="kafka-client-1@EXAMPLE.COM";
   ```

   对于像`kafka-console-consumer`或`kafka-console-producer`这样的命令行工具，kinit 可以与`useTicketCache=true`一起使用，如：

   ```properties
    sasl.jaas.config=com.sun.security.auth.module.Krb5LoginModule required \
        useTicketCache=true;
   ```

   客户端的 JAAS 配置可以作为 JVM 参数，类似 broker。客户端使用名为`KafkaClient`的 login 部分。此选项允许 JVM 中所有客户端连接的一个用户

2. 确保 JAAS 配置中的 keytab 配置文件能被操作 Kafka 客户端的用户读取

3. 可以将 krb5 文件位置作为 JVM 参数传递给每个客户端 JVM：

   ```
    -Djava.security.krb5.conf=/etc/kafka/krb5.conf
   ```

4. 在`producer.properties`或`consumer.properties`中配置以下属性：

   ```properties
    security.protocol=SASL_PLAINTEXT (or SASL_SSL)
    sasl.mechanism=GSSAPI
    sasl.kerberos.service.name=kafka
   ```

   