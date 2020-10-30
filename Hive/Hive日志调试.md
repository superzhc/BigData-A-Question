# Hive 日志调试

在 Hive 中使用的是 Log4j 来输出日志，默认情况下，CLI 是不能将日志信息输出到控制台的。在 Hive0.13.0 之前的版本，默认的日志级别是 WARN；从 Hive0.13.0 开始，默认的日志级别是 INFO。默认的日志存放在  `/tmp/<user.name>/hive.log` 文件中。

在默认的日志级别下，是不能将 DEBUG 信息输出的，可以通过以下两种方式修改 log4j 的日志输出级别，从而利用这些调试日志进行错误定位，具体做法如下：

```bash
hive --hiveconf hive.root.logger=DEBUG,console
```

或者在 `${HIVE_HOME}/conf/hive-log4j.properties` 文件中找到 `hive.root.logger` 属性，并将其修改为下面的设置：

```properties
hive.root.logger=DEBUG,console
```

> 注：**在 HiveQL 中设定日志的级别参数是无效的**，因为设定log的参数读取在会话建立以前已经完成了。这也就说，不能通过下面的方法来修改 log4j 的日志输出级别：
>
> ```bash
> hive> set hiveconf:hive.root.logger=DEBUG,console;
> ```