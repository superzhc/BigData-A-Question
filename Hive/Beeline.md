hiveserver2 提供了一种新的命令行工具 Beeline，它是 Hive 0.11 引入的新的交互式 CLI，基于 SQLLine，可以作为 Hive JDBC Client 端访问 Hive Server2，启动一个 Beeline 就是维护一个 session。

Beeline 工作模式有两种，即本地嵌入模式和远程模式，嵌入模式情况下，返回一个嵌入式的 Hive，类似于 Hive CLI，而远程模式则是通过 Thrift 协议与某个单独的 hiveserver2 进程进行连接通信。

示例：

```sh
-bash-4.2# beeline
Beeline version 1.2.1000.2.6.3.0-235 by Apache Hive
beeline> !connect jdbc:hive2://192.168.186.49:10000               (连接 hiveserver2)
Connecting to jdbc:hive2://192.168.186.49:10000
Enter username for jdbc:hive2://192.168.186.49:10000: hdfs        (用户名)
Enter password for jdbc:hive2://192.168.186.49:10000: **********  (密码)
Connected to: Apache Hive (version 1.2.1000.2.6.3.0-235)
Driver: Hive JDBC (version 1.2.1000.2.6.3.0-235)
Transaction isolation: TRANSACTION_REPEATABLE_READ
0: jdbc:hive2://192.168.186.49:10000> show databases;             (查询语句，要以分号结尾)
+----------------+--+
| database_name  |
+----------------+--+
| default        |
+----------------+--+
1 row selected (0.608 seconds)
0: jdbc:hive2://192.168.186.49:10000>
```



```sh
beeline -u jdbc:hive2://ep-002:10000/default -n hdfs
```

