# Flink Scala Shell

Scala Shell 方式支持流处理和批处理。当启动 Scala Shell 命令行之后，两个不同的 ExecutionEnvironments 会被自动创建。使用 senv（Stream）和 benv（Batch）分别去处理流数据和批数据。

命令格式如下：

```bash
${FLINK_HOME}/bin/start-scala-shell.sh [local|remote|yarn] [options] <args>
```

**local 模式**

```bash
# local [options]使用Scala Shell创建一个本地Flink集群

# 指定Flink使用的第三方依赖
-a <path/to/jar> | --addclasspath <path/to/jar>
```

**remote 模式**

```bash
# remote [options] <host> <port> 使用Scala Shell连接一个远程Flink集群。

# 指定远程Flink集群JobManager的主机名或者IP
<host>

# 指定远程Flink集群JobManager的端口号
<port>

# 指定Flink使用的第三方依赖
-a <path/to/jar> | --addclasspath <path/to/jar>
```

**yarn 模式**

```bash
# yarn [options]使用Scala Shell创建一个Flink on Yarn模式的集群
```