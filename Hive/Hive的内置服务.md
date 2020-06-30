执行`hive --service [服务名]`启动Hive的内置服务。

Hive的内置服务中`help`可获取帮助信息，如下：

```sh
[master@master1 hive]$ bin/hive --service help
Usage ./hive <parameters> --service serviceName <service parameters>
Service List: beeline cli help hiveburninclient hiveserver2 hiveserver hwi jar lineage metastore metatool orcfiledump rcfilecat schemaTool version 
Parameters parsed:
  --auxpath : Auxillary jars 
  --config : Hive configuration directory
  --service : Starts specific service/component. cli is default
Parameters used:
  HADOOP_HOME or HADOOP_PREFIX : Hadoop install directory
  HIVE_OPT : Hive options
For help on a particular service:
  ./hive --service serviceName --help
Debug help:  ./hive --debug --help
```

上面输出项 Service List 里面显示出 Hive 支持的列表，beeline cli help hiveburninclient hiveserver2 hiveserver hwi jar lineage metastore metatool orcfiledump rcfilecat schemaTool version

常用的一些服务：

- `cli`：是 Command Line Interface 的缩写，是 Hive 的命令行界面，是默认服务，直接可以在命令行里使用
- `hiveserver`：这个可以让 Hive 以提供 Thrift 服务的服务器形式运行，可以允许多个不同语言编写的客户端进行通信
- `hwi`：是 Hive Web Interface 的缩写，它是 Hive 的 Web 接口，是 Hive cli 的一个 web 替代方案
- `jar`：与 Hadoop Jar 等价的 Hive 接口，这是运行类路径中同时包含 Hadoop 和 Hive 类的 Java 应用程序的简便方式
- `metastore`：在默认的情况下，metastore 和 hive 服务运行在同一个进程中，使用这个服务，可以让 metastore 作为一个单独的进程运行，可以通过 METASTORE-PORT 来指定监听的端口号

