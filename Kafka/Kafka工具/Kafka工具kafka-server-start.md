用法：

```sh
bin/kafka-server-start.sh [-daemon] server.properties [--override property]*
```

这个命令后面可以有很多参数，第一个是可选参数，该参数可以让当前命令以后台服务方式执行，第二个必须是 Kafka 的配置文件。后面还可以有多个 `--override` 开头的参数，其中的 property 可以是 Broker Configs 中提供的所有参数。这些额外的参数会覆盖配置文件中的设置

示例：

```sh
bin/kafka-server-start.sh -daemon config/server.properties --override broker.id=0 --override log.dirs=/tmp/kafka-logs-1 --override listeners=PLAINTEXT://:9092 --override advertised.listeners=PLAINTEXT://192.168.16.150:9092

bin/kafka-server-start.sh -daemon config/server.properties --override broker.id=1 --override log.dirs=/tmp/kafka-logs-2 --override listeners=PLAINTEXT://:9093 --override advertised.listeners=PLAINTEXT://192.168.16.150:9093
```

示例的这种用法只是用于演示，真正要启动多个 Broker 应该针对不同的 Broker 创建相应的 `server.peoperties` 配置。

