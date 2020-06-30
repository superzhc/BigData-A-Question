AdminClient 配置：<http://kafka.apachecn.org/documentation.html#adminclientconfigs>

| NAME                                     | DESCRIPTION                                                  | TYPE     | DEFAULT               | VALID VALUES  | IMPORTANCE |
| :--------------------------------------- | :----------------------------------------------------------- | :------- | :-------------------- | :------------ | :--------- |
| bootstrap.servers                        | host/port,用于和kafka集群建立初始化连接。因为这些服务器地址仅用于初始化连接，并通过现有配置的来发现全部的kafka集群成员（集群随时会变化），所以此列表不需要包含完整的集群地址（但尽量多配置几个，以防止配置的服务器宕机）。 | list     |                       |               | high       |
| ssl.key.password                         | 密钥仓库文件中的私钥密码。对于客户端是可选的。               | password | null                  |               | high       |
| ssl.keystore.location                    | 密钥仓库文件的位置。这对于客户端是可选的，可以用于客户端的双向认证。 | string   | null                  |               | high       |
| ssl.keystore.password                    | 密钥仓库文件的仓库密钥。这对于客户端是可选的，只有配置了ssl.keystore.location才需要。 | password | null                  |               | high       |
| ssl.truststore.location                  | 信任存储文件的位置。                                         | string   | null                  |               | high       |
| ssl.truststore.password                  | 信任存储文件的密码。如果未设置密码，对信任库的访问仍然可用，但是完整性检查将被禁用。 | password | null                  |               | high       |
| client.id                                | 在发出请求时传递给服务器的id字符串。这样做的目的是通过允许在服务器端请求日志记录中包含逻辑应用程序名称来跟踪请求源的ip/port。 | string   | ""                    |               | medium     |
| connections.max.idle.ms                  | 关闭闲置连接的时间。                                         | long     | 300000                |               | medium     |
| receive.buffer.bytes                     | 读取数据时使用的TCP接收缓冲区（SO_RCVBUF）的大小。如果值为-1，则将使用OS默认值。 | int      | 65536                 | [-1,...]      | medium     |
| request.timeout.ms                       | 配置控制客户端等待请求响应的最长时间。如果在超时之前未收到响应，客户端将在必要时重新发送请求，如果重试耗尽，则该请求将失败。 | int      | 120000                | [0,...]       | medium     |
| sasl.jaas.config                         | JAAS配置文件使用的格式的SASL连接的JAAS登录上下文参数。这里描述JAAS配置文件格式。该值的格式为：' (=)*;' | password | null                  |               | medium     |
| sasl.kerberos.service.name               | Kafka运行的Kerberos principal名。可以在Kafka的JAAS配置或Kafka的配置中定义。 | string   | null                  |               | medium     |
| sasl.mechanism                           | 用于客户端连接的SASL机制。安全提供者可用的任何机制。GSSAPI是默认机制。 | string   | GSSAPI                |               | medium     |
| security.protocol                        | 与broker通讯的协议。有效的值有: PLAINTEXT, SSL, SASL_PLAINTEXT,SASL_SSL. | string   | PLAINTEXT             |               | medium     |
| send.buffer.bytes                        | 发送数据时时使用TCP发送缓冲区（SO_SNDBUF）的大小。如果值为-1，则使用OS默认值。 | int      | 131072                | [-1,...]      | medium     |
| ssl.enabled.protocols                    | 启用SSL连接的协议列表。                                      | list     | TLSv1.2,TLSv1.1,TLSv1 |               | medium     |
| ssl.keystore.type                        | 密钥仓库文件的文件格式。对于客户端是可选的。                 | string   | JKS                   |               | medium     |
| ssl.protocol                             | 用于生成SSLContext的SSL协议。默认设置是TLS，这对大多数情况都是适用的。最新的JVM中允许的值是TLS,TLSv1.1和TLSv1.2。较旧的JVM可能支持SSL,SSLv2和SSLv3,但由于已知的安全漏洞,不建议使用。 | string   | TLS                   |               | medium     |
| ssl.provider                             | 用于SSL连接的安全提供程序的名称。 默认值是JVM的默认安全提供程序。 | string   | null                  |               | medium     |
| ssl.truststore.type                      | 信任仓库文件的文件格式                                       | string   | JKS                   |               | medium     |
| metadata.max.age.ms                      | 我们强制更新元数据的时间段（以毫秒为单位），即使我们没有任何分区leader发生变化，主动发现任何新的broker或分区。 | long     | 300000                | [0,...]       | low        |
| metric.reporters                         | 用作指标记录的类的列表。实现MetricReporter接口，以允许插入将被通知新的度量创建的类。JmxReporter始终包含在注册JMX统计信息中。 | list     | ""                    |               | low        |
| metrics.num.samples                      | 用于计算度量维护的样例数。                                   | int      | 2                     | [1,...]       | low        |
| metrics.recording.level                  | The highest recording level for metrics.                     | string   | INFO                  | [INFO, DEBUG] | low        |
| metrics.sample.window.ms                 | 时间窗口计算度量标准。                                       | long     | 30000                 | [0,...]       | low        |
| reconnect.backoff.max.ms                 | 重新连接到重复无法连接的broker程序时等待的最大时间（毫秒）。如果提供，每个主机的回退将会连续增加，直到达到最大值。 计算后退增加后，增加20％的随机抖动以避免连接风暴。 | long     | 1000                  | [0,...]       | low        |
| reconnect.backoff.ms                     | 尝试重新连接到给定主机之前等待的基本时间量。这避免了在频繁的重复连接主机。此配置适用于client对broker的所有连接尝试。 | long     | 50                    | [0,...]       | low        |
| retries                                  | 在失败之前重试调用的最大次数                                 | int      | 5                     | [0,...]       | low        |
| retry.backoff.ms                         | 尝试重试失败的请求之前等待的时间。这样可以避免在某些故障情况下以频繁的重复发送请求。 | long     | 100                   | [0,...]       | low        |
| sasl.kerberos.kinit.cmd                  | Kerberos kinit命令路径。                                     | string   | /usr/bin/kinit        |               | low        |
| sasl.kerberos.min.time.before.relogin    | 刷新尝试之间的登录线程睡眠时间。                             | long     | 60000                 |               | low        |
| sasl.kerberos.ticket.renew.jitter        | 添加到更新时间的随机抖动百分比。                             | double   | 0.05                  |               | low        |
| sasl.kerberos.ticket.renew.window.factor | 登录线程将休眠，直到从上次刷新到“票”到期时间的指定窗口为止，此时将尝试续订“票”。 |          |                       |               |            |
|                                          | double                                                       | 0.8      |                       | low           |            |
| ssl.cipher.suites                        | 密码套件列表。是TLS或SSL网络协议来协商用于网络连接的安全设置的认证，加密，MAC和密钥交换算法的命名组合。默认情况下，支持所有可用的密码套件。 | list     | null                  |               | low        |
| ssl.endpoint.identification.algorithm    | 使用服务器证书验证服务器主机名的端点识别算法。               | string   | null                  |               | low        |
| ssl.keymanager.algorithm                 | 用于SSL连接的密钥管理工厂算法。默认值是Java虚拟机配置的密钥管理器工厂算法。 | string   | SunX509               |               | low        |
| ssl.secure.random.implementation         | 用于SSL加密操作的SecureRandom PRNG实现。                     | string   | null                  |               | low        |
| ssl.trustmanager.algorithm               | 用于SSL连接的信任管理工厂算法，默认是Java虚拟机机制。        | string   | PKIX                  |               | low        |