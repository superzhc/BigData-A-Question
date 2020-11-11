# Hive 整合 HBase
配置 Hive 与 HBase 整合的目的是利用 HQL 语法实现对 HBase 数据库的增删改查操作，基本原理就是利用两者本身对外的 API 接口互相进行通信，两者通信主要是依靠 `hive_hbase-handler.jar` 工具类。 但请注意：使用 Hive 操作 HBase 中的表，只是提供了便捷性，`hiveQL` 引擎使用的是 MapReduce，对于性能上，表现比较糟糕，在实际应用过程中可针对不同的场景酌情使用。

## 原理

Hive 与 HBase 利用两者本身对外的 API 来实现整合，主要是靠 HBaseStorageHandler 进行通信，利用 HBaseStorageHandler，Hive 可以获取到 Hive 表对应的 HBase 表名，列簇以及列，InputFormat 和 OutputFormat 类，创建和删除 HBase 表等。 

Hive 访问 HBase 中表数据，实质上是通过 MapReduce 读取 HBase 表数据，其实现是在 MR 中，使用 HiveHBaseTableInputFormat 完成对 HBase 表的切分，获取 RecordReader 对象来读取数据。 

对 HBase 表的切分原则是一个 Region 切分成一个 Split，即表中有多少个 Regions，MR 中就有多少个 Map； 

读取 HBase 表数据都是通过构建 Scanner，对表进行全表扫描，如果有过滤条件，则转化为 Filter。当过滤条件为 rowkey 时，则转化为对 rowkey 的过滤；Scanner 通过 RPC 调用 RegionServer 的 `next()` 来获取数据。

## 配置

### 1、拷贝 HBase 相关 jar 包

将 HBase 相关 jar 包拷贝到 Hive 的 lib 目录下

```
hbase-client-0.98.13-hadoop2.jar
hbase-common-0.98.13-hadoop2.jar
hbase-server-0.98.13-hadoop2.jar
hbase-common-0.98.13-hadoop2-tests.jar
hbase-protocol-0.98.13-hadoop2.jar
htrace-core-2.04.jar
hive-hbase-handler-1.0.0.jar
zookeeper-3.4.5.jar
```

### 2、修改 `hive-site.xml` 配置文件

- ~~将上述 jar 包 添加到 `hive.aux.jars.path` 属性中~~

- ~~修改 `hive.zookeeper.quorum` 属性~~

- 添加 `hbase.zookeeper.quorum` 属性

  ```xml
  <property>      
      <name>hbase.zookeeper.quorum</name>
      <value>192.168.186.40,192.168.186.41,192.168.186.42</value>
  </property>
  ```

### 3、~~修改 `hive-env.sh` 配置文件~~

### 4、启动 Hive

## F&Q

### 1、hive连接hbase外部表错误，`Can't get the locations`

在hive中执行创建hbase的外部表，执行创建脚本：

```sql
hive> CREATE EXTERNAL TABLE ANALYSELOG(
        key          string,
        apiguid      string,
        apiid        string,
        apiname      string,
        clientip     string,
        consumer     string,
        context      string,
        forwardtime  int,
        method       string,
        requestsize  double,
        requesttime  int,
        requesturl   string,
        responsesize double,
        rowguid      string,
        startat      date,
        status       int)
      STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
      WITH SERDEPROPERTIES("hbase.columns.mapping"=":key,default:apiguid,default:apiid,default:apiname,default:clientip,default:consumer,default:context,default:forwardtime,default:method,default:requestsize,default:requesttime,default:requesturl,default:responsesize,default:rowguid,default:startat,default:status")
      TBLPROPERTIES("hbase.table.name" = "ANALYSELOG");
```

报错如下：

```sh
FAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask. MetaException(message:org.apache.hadoop.hbase.client.RetriesExhaustedException: Can't get the location for replica 0
        at org.apache.hadoop.hbase.client.RpcRetryingCallerWithReadReplicas.getRegionLocations(RpcRetryingCallerWithReadReplicas.java:354)
        at org.apache.hadoop.hbase.client.ScannerCallableWithReplicas.call(ScannerCallableWithReplicas.java:159)
        at org.apache.hadoop.hbase.client.ScannerCallableWithReplicas.call(ScannerCallableWithReplicas.java:61)
        at org.apache.hadoop.hbase.client.RpcRetryingCaller.callWithoutRetries(RpcRetryingCaller.java:211)
        at org.apache.hadoop.hbase.client.ClientScanner.call(ClientScanner.java:327)
        at org.apache.hadoop.hbase.client.ClientScanner.nextScanner(ClientScanner.java:302)
        at org.apache.hadoop.hbase.client.ClientScanner.initializeScannerInConstruction(ClientScanner.java:167)
        at org.apache.hadoop.hbase.client.ClientScanner.<init>(ClientScanner.java:162)
        at org.apache.hadoop.hbase.client.HTable.getScanner(HTable.java:799)
        at org.apache.hadoop.hbase.MetaTableAccessor.fullScan(MetaTableAccessor.java:602)
        at org.apache.hadoop.hbase.MetaTableAccessor.tableExists(MetaTableAccessor.java:366)
        at org.apache.hadoop.hbase.client.HBaseAdmin.tableExists(HBaseAdmin.java:415)
        at org.apache.hadoop.hbase.client.HBaseAdmin.tableExists(HBaseAdmin.java:425)
        at org.apache.hadoop.hive.hbase.HBaseStorageHandler.preCreateTable(HBaseStorageHandler.java:214)
        at org.apache.hadoop.hive.metastore.HiveMetaStoreClient.createTable(HiveMetaStoreClient.java:731)
        at org.apache.hadoop.hive.metastore.HiveMetaStoreClient.createTable(HiveMetaStoreClient.java:724)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.apache.hadoop.hive.metastore.RetryingMetaStoreClient.invoke(RetryingMetaStoreClient.java:178)
        at com.sun.proxy.$Proxy5.createTable(Unknown Source)
        at org.apache.hadoop.hive.ql.metadata.Hive.createTable(Hive.java:778)
        at org.apache.hadoop.hive.ql.exec.DDLTask.createTable(DDLTask.java:4465)
        at org.apache.hadoop.hive.ql.exec.DDLTask.execute(DDLTask.java:318)
        at org.apache.hadoop.hive.ql.exec.Task.executeTask(Task.java:162)
        at org.apache.hadoop.hive.ql.exec.TaskRunner.runSequential(TaskRunner.java:89)
        at org.apache.hadoop.hive.ql.Driver.launchTask(Driver.java:1756)
        at org.apache.hadoop.hive.ql.Driver.execute(Driver.java:1497)
        at org.apache.hadoop.hive.ql.Driver.runInternal(Driver.java:1294)
        at org.apache.hadoop.hive.ql.Driver.run(Driver.java:1161)
        at org.apache.hadoop.hive.ql.Driver.run(Driver.java:1151)
        at org.apache.hadoop.hive.cli.CliDriver.processLocalCmd(CliDriver.java:217)
        at org.apache.hadoop.hive.cli.CliDriver.processCmd(CliDriver.java:169)
        at org.apache.hadoop.hive.cli.CliDriver.processLine(CliDriver.java:380)
        at org.apache.hadoop.hive.cli.CliDriver.executeDriver(CliDriver.java:740)
        at org.apache.hadoop.hive.cli.CliDriver.run(CliDriver.java:685)
        at org.apache.hadoop.hive.cli.CliDriver.main(CliDriver.java:625)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.apache.hadoop.util.RunJar.run(RunJar.java:233)
        at org.apache.hadoop.util.RunJar.main(RunJar.java:148)
```

看错误应该是连不上 HBase，HBase是用 Zookeeper 管理的，进行如下测试：

1. 测试单节点 HBase 的连接

```bash
$ hive -hiveconf hbase.master=master:60000
```

进入 Hive 的 CLI 后，执行创建外部表的脚本，发现还是报错。

2. 测试集群 HBase 的连接

```bash
hive -hiveconf hbase.zookeeper.quorum=slave1,slave2,master,slave4,slave5,slave6,slave7
```

进入 Hive 的 CLI 后，执行创建外部表的脚本，发现创建成功。

由此可见，是 Hive 读取 HBase 的 Zookeeper 时出错了。查看 `hive-site.xml` 文件中，有个名为 `hive.zookeeper.quorum` 的属性，复制一份改为 `hbase.zookeeper.quorum` 的属性。如下：

```xml
  <property>
    <name>hbase.zookeeper.quorum</name>
    <value>slave1,slave2,master,slave4,slave5,slave6,slave7</value>
    <description>
    </description>
  </property>
```

至此，问题解决，创建 HBase 外部表成功。