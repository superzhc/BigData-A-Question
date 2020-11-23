# `yarn-session.sh`

`yarn-session.sh` 一般用来创建 Flink 集群。

**参数列表**

| 参数                              | 描述                                                                |
| --------------------------------- | ------------------------------------------------------------------- |
| `-n,--container <arg>`            | **必填项**，表示分配容器的数量（也就是 TaskManager 的数量）         |
| `-D <arg>`                        | 动态属性                                                            |
| `-d,--detached`                   | 在后台独立运行                                                      |
| `-jm,--jobManagerMemory <arg>`    | 设置 JobManager 的内存，单位是 MB                                   |
| `-nm,--name`                      | 在 YARN 上为一个自定义的应用设置一个名字                            |
| `-q,--query`                      | 显示 YARN 中可用的资源（内存、cpu 核数）                            |
| `-qu,--queue <arg>`               | 指定 YARN 队列                                                      |
| `-s,--slots <arg>`                | 每个 TaskManager 使用的 Slot 数量                                   |
| `-tm,--taskManagerMemory <arg>`   | 每个 TaskManager 的内存，单位是 MB                                  |
| `-z,--zookeeperNamespace <arg>`   | 针对 HA 模式在 ZooKeeper 上创建 NameSpace                           |
| `-id,--applicationId <yarnAppId>` | 指定 YARN 集群上的任务 ID，附着到一个后台独立运行的 yarn session 中 |