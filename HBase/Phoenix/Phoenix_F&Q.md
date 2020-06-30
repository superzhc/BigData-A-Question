## Phoenix 映射已存在的 HBase 表，却查询不到数据

#### 问题描述

Phoenix 映射已存在的 HBase 表，表映射正常，但是使用phoenix查询，却查询不到数据

#### 示例

##### 1、`hbase shell` 插入数据

```sh
create 'test1','i'
put 'test1','1','i:name','zhangsan'
put 'test1','2','i:name','lisi'
put 'test1','1','i:age','15'
put 'test1','2','i:age','2'

hbase(main):006:0> scan 'test1'
ROW                                          COLUMN+CELL
 1                                           column=i:age, timestamp=1523416240312, value=15
 1                                           column=i:name, timestamp=1523416227940, value=zhangsan
 2                                           column=i:age, timestamp=1523416249281, value=2
 2                                           column=i:name, timestamp=1523416234516, value=lisi
```

##### 2、phoenix 创建表

```sh
  create table "test1"(
    pk VARCHAR PRIMARY KEY
    ,"i"."name" VARCHAR
    ,"i"."age" VARCHAR);
```

##### 3、phoenix 查询，却查询不到数据

```sh
0: jdbc:phoenix:> select * from "test1";
+-----+-------+------+
| PK  | name  | age  |
+-----+-------+------+
+-----+-------+------+
No rows selected (0.238 seconds)
```

#### 问题原因

> 经过阅读官方文档发现，phoenix 4.10 版本后，对列映射做了优化，采用一套新的机制，不在基于列名方式映射到 hbase。

#### 解决办法

1. 如果只做查询，强烈建议使用 phoenix 视图方式映射，删除视图不影响 hbase 源数据，语法如下：

```sql
 create view "test1"(
    pk VARCHAR PRIMARY KEY
    ,"i"."name" VARCHAR
    ,"i"."age" VARCHAR);
```

2. 必须要表映射，需要禁用列映射规则（会降低查询性能），如下：

```sql
  create table "test1"(
    pk VARCHAR PRIMARY KEY
    ,"i"."name" VARCHAR
    ,"i"."age" VARCHAR)
 column_encoded_bytes=0;
```

## `Inconsistent namespace mapping properites..`

**详细报错信息**：

```log
Exception in thread "main" java.sql.SQLException: ERROR 726 (43M10):  Inconsistent namespace mapping properites.. Cannot initiate connection as SYSTEM:CATALOG is found but client does not have phoenix.schema.isNamespaceMappingEnabled enabled
    at org.apache.phoenix.exception.SQLExceptionCode$Factory$1.newException(SQLExceptionCode.java:454)
    at org.apache.phoenix.exception.SQLExceptionInfo.buildException(SQLExceptionInfo.java:145)
    at org.apache.phoenix.query.ConnectionQueryServicesImpl$13.call(ConnectionQueryServicesImpl.java:2342)
    at org.apache.phoenix.query.ConnectionQueryServicesImpl$13.call(ConnectionQueryServicesImpl.java:2300)
    at org.apache.phoenix.util.PhoenixContextExecutor.call(PhoenixContextExecutor.java:78)
    at org.apache.phoenix.query.ConnectionQueryServicesImpl.init(ConnectionQueryServicesImpl.java:2300)
    at org.apache.phoenix.jdbc.PhoenixDriver.getConnectionQueryServices(PhoenixDriver.java:231)
    at org.apache.phoenix.jdbc.PhoenixEmbeddedDriver.createConnection(PhoenixEmbeddedDriver.java:144)
    at org.apache.phoenix.jdbc.PhoenixDriver.connect(PhoenixDriver.java:202)
    at java.sql.DriverManager.getConnection(DriverManager.java:664)
    at java.sql.DriverManager.getConnection(DriverManager.java:208)
    at org.apache.phoenix.util.QueryUtil.getConnection(QueryUtil.java:340)
    at org.apache.phoenix.util.QueryUtil.getConnection(QueryUtil.java:332)
    at org.apache.phoenix.mapreduce.AbstractBulkLoadTool.loadData(AbstractBulkLoadTool.java:209)
    at org.apache.phoenix.mapreduce.AbstractBulkLoadTool.run(AbstractBulkLoadTool.java:183)
    at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:70)
    at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:84)
    at org.apache.phoenix.mapreduce.CsvBulkLoadTool.main(CsvBulkLoadTool.java:101)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
```

**解决办法**：

1. **服务端配置**

   在运行load数据的节点上的`hbase-site.xml`（这里就是服务端）里添加如下配置：

   ```xml
   <property>
       <name>phoenix.schema.isNamespaceMappingEnabled</name>
       <value>true</value>
   </property>
   ```

2. **客户端配置**

   在客户端目录下`hbase-site.xml`（这里就是客户端）里也需要有上面的配置项

## 创建同步索引超时[TODO]

在客户端配置文件 `hbase-site.xml` 中，把超时参数设置大一些，足够 build 索引数据的时间。

```xml
<property>
    <name>hbase.rpc.timeout</name>
    <value>60000000</value>
</property>
<property>
    <name>hbase.client.scanner.timeout.period</name>
    <value>60000000</value>
</property>
<property>
    <name>phoenix.query.timeoutMs</name>
    <value>60000000</value>
</property>
```

