# Flink run 命令

Flink run 命令既可以向 Flink 中提交任务，也可以在提交任务的同时创建一个新的 Flink 集群。

Flink run 命令格式如下：

```bash
${FLINK_HOME}/bin/flink run [OPTIONS] <jar-file> <arguments>
```

其中 `[]` 表示是可选参数，`<>` 表示是必填参数。

**OPTIONS 参数**

| 参数                             | 描述                                                                                                                            |
| -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `-c,--class <classname>`         | 如果没有在 JAR 包中指定入口类，则需要在此通过这个参数动态指定 JAR 包的入口类（注意：这个参数一定要放到 `<jar-file>` 参数前面。） |
| `-m,--jobmanager <host:port>`    | 指定需要连接的 JobManager（主节点）地址，可以指定一个不同于配置文件中的 JobManager                                              |
| `-p,--parallelism <parallelism>` | 动态指定任务的并行度，可以覆盖配置文件中的默认值                                                                                |
|`--detached`|任务采用后台运行的方式|