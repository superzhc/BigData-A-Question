sqoop是一款开源的关系型数据库到Hadoop的迁移工具，对于通用的数据类型，如数值类型、字符类型、日期类型等sqoop可以提供无缝地迁移到Hadoop平台。但对于特殊类型，如LOB，使用sqoop迁移则有所限制。
对于CLOB，如xml文本，sqoop可以迁移到Hive表，对应字段存储为字符类型。
对于BLOB，如jpg图片，sqoop无法直接迁移到Hive表，只能先迁移到HDFS路径，然后再使用Hive命令加载到Hive表。迁移到HDFS后BLOB字段存储为16进制形式。本文我们介绍如果使用sqoop迁移Oracle中带blob字段的表到相应地Hive表。

1. 首先，假设我们已经有现成的Oracle环境，且Oracle中有一张带BLOB（存储图片）字段的表，如t_blob。

2. 其次，假设我们有现成的Hadoop集群且装有sqoop，我们验证sqoop可以正常访问Oracle环境并能访问上述带BLOB的表。
	```sh
	sqoop-list-tables --driver oracle.jdbc.OracleDriver --connect jdbc:oracle:thin:@10.10.10.7:1521/orcl --username itlr --password itlr
	```
	
3. 现在我们可以使用sqoop的import命令来把带BLOB的表抽取到指定的HDFS路径下
	```sh
	sqoop-import --connect jdbc:oracle:thin:@10.10.10.7:1521/orcl --username itlr --password itlr --table T_BLOB --columns "a,b,c" --split-by A -m 4 --inline-lob-limit=16777126 --target-dir /tmp/t_lob
	```
	
4. 验证sqoop不能直接导入带BLOB的表到Hive表
	```sh
	sqoop-import --connect jdbc:oracle:thin:@10.10.10.7:1521/orcl --username itlr --password itlr --table T_BLOB --split-by A -m 4 --hive-import --create-hive-table
	```
	输出错误如下
    ```sh
       ...
       19/02/20 17:51:11 ERROR tool.ImportTool: Import failed: java.io.IOException: Hive does not support the SQL type for column C
            at org.apache.sqoop.hive.TableDefWriter.getCreateTableStmt(TableDefWriter.java:181)
            at org.apache.sqoop.hive.HiveImport.importTable(HiveImport.java:189)
            at org.apache.sqoop.tool.ImportTool.importTable(ImportTool.java:530)
            at org.apache.sqoop.tool.ImportTool.run(ImportTool.java:621)
            at org.apache.sqoop.Sqoop.run(Sqoop.java:147)
            at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:70)
            at org.apache.sqoop.Sqoop.runSqoop(Sqoop.java:183)
            at org.apache.sqoop.Sqoop.runTool(Sqoop.java:234)
            at org.apache.sqoop.Sqoop.runTool(Sqoop.java:243)
            at org.apache.sqoop.Sqoop.main(Sqoop.java:252)
    ```
5. 从HDFS路径中查看导入的文件，可以看到对于BLOB字段存储为16进制形式
	```sh
	[hdfs@p08 ~]$ hadoop fs -ls /tmp/t_lob              
Found 2 items
-rw-r--r--   3 hdfs supergroup          0 2019-02-20 17:29 /tmp/t_lob/_SUCCESS
-rw-r--r--   3 hdfs supergroup     129237 2019-02-20 17:29 /tmp/t_lob/part-m-00000
	```
	![img](../images/20190222181510910.png)
6. 创建Hive外表，指向上述文件，并查询对应Hive表是否有数据
	通过以下输出，Hive外表t_blob有一条记录，对应blob字段长度为129230
    ```sh
    create external table t_blob(a string, b string, c string)
    row format delimited
    fields terminated by ','
    location '/tmp/t_lob';

    hive> select a,b,length(c) from t_blob;
    Query ID = hdfs_20190220174141_292e9bc8-55de-4a42-ad46-ef507252e3ca
    Total jobs = 1
    Launching Job 1 out of 1
    Number of reduce tasks is set to 0 since there's no reduce operator
    Starting Job = job_1550649972092_0003, Tracking URL = http://p13.esgyncn.local:8088/proxy/application_1550649972092_0003/
    Kill Command = /opt/cloudera/parcels/CDH-5.13.0-1.cdh5.13.0.p0.29/lib/hadoop/bin/hadoop job  -kill job_1550649972092_0003
    Hadoop job information for Stage-1: number of mappers: 1; number of reducers: 0
    2019-02-20 17:41:24,240 Stage-1 map = 0%,  reduce = 0%
    2019-02-20 17:41:30,471 Stage-1 map = 100%,  reduce = 0%, Cumulative CPU 3.71 sec
    MapReduce Total cumulative CPU time: 3 seconds 710 msec
    Ended Job = job_1550649972092_0003
    MapReduce Jobs Launched: 
    Stage-Stage-1: Map: 1   Cumulative CPU: 3.71 sec   HDFS Read: 133288 HDFS Write: 13 SUCCESS
    Total MapReduce CPU Time Spent: 3 seconds 710 msec
    OK
    1       ABC     129230
    Time taken: 14.027 seconds, Fetched: 1 row(s)
    ```

至此，我们已经知道如何用sqoop迁移blob到Hive表，后续我们继续介绍迁移带blob的表到Trafodion表。
