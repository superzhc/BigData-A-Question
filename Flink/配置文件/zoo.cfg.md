# `zoo.cfg`

```properties
# 每个 tick 的毫秒数
tickTime=2000

# 初始同步阶段可以采用的 tick 数
initLimit=10

# 在发送请求和获取确认之间可以传递的 tick 数
syncLimit=5

# 存储快照的目录
# dataDir=/tmp/zookeeper

# 客户端将连接的端口
clientPort=2181

# ZooKeeper quorum peers
server.1=localhost:2888:3888
# server.2=host:peer-port:leader-port
```

