## 缺少驱动包

```sh
19/07/23 17:43:05 ERROR sqoop.Sqoop: Got exception running Sqoop: java.lang.RuntimeException: Could not load db driver class: oracle.jdbc.OracleDriver
java.lang.RuntimeException: Could not load db driver class: oracle.jdbc.OracleDriver
        at org.apache.sqoop.manager.OracleManager.makeConnection(OracleManager.java:287)
        at org.apache.sqoop.manager.GenericJdbcManager.getConnection(GenericJdbcManager.java:52)
        at org.apache.sqoop.manager.OracleManager.listDatabases(OracleManager.java:694)
        at org.apache.sqoop.tool.ListDatabasesTool.run(ListDatabasesTool.java:49)
        at org.apache.sqoop.Sqoop.run(Sqoop.java:147)
        at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:76)
        at org.apache.sqoop.Sqoop.runSqoop(Sqoop.java:183)
        at org.apache.sqoop.Sqoop.runTool(Sqoop.java:225)
        at org.apache.sqoop.Sqoop.runTool(Sqoop.java:234)
        at org.apache.sqoop.Sqoop.main(Sqoop.java:243)
```

> `${SQOOP_HOME}/lib/<数据库驱动包>`

## 若从 RDB 导入数据到 HDFS 里，出现中断情况

好比 MapReduce 作业丢失一样，有个容错机制。不需要担心任务中断导致数据重复插入的情况。

Sqoop 导入数据，**要么全部导入成功，要么一条都导入不成功**。

##  错误：`ERROR tool.ImportTool: Error during import: No primary key could be found for table TRANS_GJJY02. Please specify one with --split-by or perform a sequential import with '-m 1'`

根据错误提示可以知道这是因为表中的数据没有设置主键。

针对这个问题有两种解决方案：

- 在关系型数据库中给表设置主键
- 对于无法给关系型数据库的表设置主键的，可以通过以下两个方法来解决
  - 将 map 的个数设置为 1（Sqoop 默认是 4 个），即：`-m 1`
  - 使用 `--split-by <column-name>` ，指定对数据进行分区的列

## 报错：`Hive does not support the SQL type for column ssl_cipher`  

原因：报这个错是因为hive不支持mysql表中某些字段的类型

解决：--map-column-hive ssl_cipher=string,x509_issuer=string,x509_subject=string \  这个是强制转换mysql表中某个字段的类型为string类型，多个字段中间以逗号（`,`）隔开，加这个指令加入到sqoop脚本中

sqoop脚本指令如下：

```sh
sqoop import \
--connect jdbc:mysql://[IP地址]:[端口号]/mysql --username <用户名> --password <密码> \
--table user \
--fields-terminated-by "\t" \
--lines-terminated-by "\n" \
-m 1 \
--hive-import \
--hive-database default --hive-table usertest \
--create-hive-table \
--hive-overwrite \
--map-column-hive ssl_cipher=string,x509_issuer=string,x509_subject=string \  #这个是强制转换mysql表中某个字段的类型为string类型，多个字段中间以逗号隔开
--delete-target-dir
```

## 报错：`ERROR tool.ImportTool: Import failed: java.io.IOException: Generating splits for a textual index column allowed only in case of "-Dorg.apache.sqoop.splitter.allow_text_splitter=true" property passed as a parameter`

原因：主要问题是“--split-by id”这个参数指定的id是一个文本格式，所以需要在命令中加入选项"-Dorg.apache.sqoop.splitter.allow_text_splitter=true"，（这个参数具体是什么意思我也不懂）

解决办法：-Dorg.apache.sqoop.splitter.allow_text_splitter=true把这个指令假如到sqoop指令脚本中

## 报错： `ERROR tool.ImportTool: Imported Failed: Character 8216 is an out-of-range delimiter`

解决办法：仔细检查指令中的字符，看看有没有中文状态的，我的就是中文状态的单引号引起的报错

**4.**报错：

![img](../images/1507938-20181127150610753-1029000116.png)

原因：这种错一般是地址或数据库写错，连接不上

解决办法：仔细检查。

