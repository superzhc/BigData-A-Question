Apache Phoenix 是一个 HBase 的开源 SQL 引擎。可以使用标准的 JDBC API 代替 HBase 客户端 API 来创建表，插入数据，查询HBase数据。

Phoenix提供了比自己手写的方式相同或更好的性能，通过以下方法

- 编译查询的SQL为原生HBase的scan语句；
- 检测scan语句最佳的开始和结束的key；
- 精心编排scan语句让他们并行执行；
- 让计算去接近数据通过；
- 推送编写的WHERE子句的谓词到服务端过滤器处理；
- 执行聚合查询通过服务端钩子（称为协同处理器）。

Phoenix 还提供了以下增强功能来更多的优化性能：

- 实现了二级索引来提升非主键字段查询的性能；
- 统计相关数据来提高并行化水平，并帮助选择最佳优化方案；
- 跳过扫描过滤器来优化IN，LIKE，OR查询；
- 优化主键的来均匀分布写压力。

Apache Phoenix 官方站点：https://phoenix.apache.org/
Phoenix支持的sql语句： https://phoenix.apache.org/language/index.html
Phoenix 支持的DataTypes：https://phoenix.apache.org/language/datatypes.html
Phoenix 支持的函数：https://phoenix.apache.org/language/functions.html

## 安装

### 0、前提条件

Hadoop 集群、Zookeeper、HBase 都安装成功

### 1、 下载安装包

官网地址:<http://phoenix.apache.org/>

注：根据 HBase 的版本下载对应版本的 Phoenix

下载页面：http://phoenix.apache.org/download.html

### 2、配置

1. 将 Phoenix 目录下的 `phoenix-core-4.13.1-HBase-1.3.jar`、~~`phoenix-4.13.1-HBase-1.3-client.jar`~~ 拷贝到 hbase 集群各个节点 hbase 安装目录 lib 中。
2. 将 hbase 集群中的配置文件 `hbase-site.xml` 拷贝到 Phoenix 的 bin 目录下，覆盖原有的配置文件。
3. 将 hdfs 集群中的配置文件 `core-site.xml`、`hdfs-site.xml` 拷贝到 Phoenix 的 bin 目录下

详细配置见：[环境配置](./环境配置.md)

## 常规使用

一般可以使用以下三种方式访问 Phoenix：

- JDBC API
- 使用 Python 编写的命令行工具（`sqlline`，`sqlline-thin` 和 `psql` 等）
- SQuirrel

### [命令行工具使用](./工具.md)

- [sqlline.py](./Phoenix_SQL操作)
- `psql`
- BulkLoadTool相关工具类

### JDBC 使用

#### 说明

需要引入包`phoenix-xxx-client.jar`到项目中，将HBase集群的`hbase-site.xml`配置文件也加到项目中。

#### 获取连接

```java
package cn.com.dimensoft.hadoop.phoenix.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

public class BaseDB {

    public static Connection getConnection() {
        try {
            // load driver
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");

            // get connection
            // jdbc 的 url 类似为 jdbc：phoenix [ ：<zookeeper quorum> [ ：<端口号> [ ：<根节点> [ ：<principal> [ ：<keytab file> ]]]]]
            // 需要引用三个参数：hbase.zookeeper.quorum、hbase.zookeeper.property.clientPort、and zookeeper.znode.parent，
            // 这些参数可以缺省不填而在 hbase-site.xml 中定义。
            return DriverManager.getConnection("jdbc:phoenix:zk1,zk2,zk3:2181:/znode:test/dev:/home/jinchuan/test.keytab",props);
            //其中props是可选属性，可能包括Phoenix和HBase配置属性，以及由以下内容组成的连接字符串：
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
```

## [Phoenix 数据类型](./数据类型.md)

目前Phoenix支持24种简单数据类型和1个一维Array的复杂类型。

## Phoenix 二级索引系统

在 HBase 中只有一个单一的按照字典序排序的 rowkey 索引，当使用 rowkey 来进行数据查询的时候速度较快，但是如果不使用 rowkey 来查询的话就会使用 filter 来对全表进行扫描，很大程度上降低了检索的性能。而 Phoenix 提供了二级索引技术来应对这种使用 rowkey 之外的条件进行检索的场景。

~~Phoenix 支持两种类型的索引技术：Global Indexing 和 Local Indexing ，这两种索引技术分别适用于不同的业务场景（主要是偏重于读还是偏重于写）。~~

### Covered Indexes

只需要通过索引就能返回所要查询的数据，所有索引的列必须包含所需查询的列（SELECT的列和WHERE的列）

覆盖索引的特点是把原数据存储在索引数据表中，这样在查询到索引数据时就不需要再次返回到原表查询，可以直接拿到查询结果。

### Functional Indexes

从 Phoenix4.3 以上就支持函数索引，其索引不局限于列，可以合适任意的表达式来创建索引，当在查询时用到了这些表达式时就直接返回表达式结果。

### Global Indexes

Global indexing适用于***多读少写***的业务场景。使用Global indexing的话在写数据的时候会消耗大量开销，因为所有对数据表的更新操作（DELETE, UPSERT VALUES and UPSERT SELECT）,会引起索引表的更新，而索引表是分布在不同的数据节点上的，跨节点的数据传输带来了较大的性能消耗。在读数据的时候Phoenix会选择索引表来降低查询消耗的时间。在默认情况下如果想查询的字段不是索引字段的话索引表不会被使用，也就是说不会带来查询速度的提升。

Global indexing 创建的索引对应一张独立的 HBase 表

### Local Indexes

Local indexing适用于***写操作频繁***的场景。与Global indexing一样，Phoenix会自动判定在进行查询的时候是否使用索引。使用Local indexing时，索引数据和数据表的数据是存放在相同的服务器中的避免了在写操作的时候往不同服务器的索引表中写索引带来的额外开销。使用Local indexing的时候即使查询的字段不是索引字段索引表也会被使用，这会带来查询速度的提升，这点跟Global indexing不同。一个数据表的所有索引数据都存储在一个单一的独立的可共享的表中。在读数据的时候因为存储数据的region的位置无法预测导致性能有一定损耗。

本地索引和原数据

### 创建二级索引

#### 1、配置 `hbase-site.xml`

在 HBase 集群的每个 RegionServer 节点的 `hbase-site.xml` 中加入如下配置并重启 HBase 集群：

```xml
<!-- 使用 Global Indexing 的配置 -->
<property>
    <name>hbase.regionserver.wal.codec</name>
    <value>org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec</value>
</property>

<!-- 使用 Local Indexing 的配置 -->
<property>
    <name>hbase.master.loadbalancer.class</name>
    <value>org.apache.phoenix.hbase.index.balancer.IndexLoadBalancer</value>
</property>
<property>
    <!-- 配置这个参数的目的是确保数据表与索引表协同定位。 -->
    <name>hbase.coprocessor.master.classes</name>
    <value>org.apache.phoenix.hbase.index.master.IndexMasterObserver</value>
</property>
```

#### 2、创建表

进入 phoenix 的 CLI 的界面映射 HBase 的表或视图

```sql
CREATE TABLE fronthis(ROW varchar primary key,"default"."age" int,"default"."log_timestamp" varchar,"default"."name" varchar,"default"."offset" varchar,"default"."operation" varchar);
```

<font color="red">~~网上对于映射HBase的表的主键要默认为 ROW，但实际上不可设置为 ROW，可设置成其他的值，测试设置成 rowguid、guid等都是可以的~~</font>

**查看表的索引**

```sh
!indexes fronthis
```

#### 3、创建索引

对表的某个字段创建索引，如：对fronthis表的operation字段创建索引，索引的名字为 operation_index：

```sql
-- 创建 Global Indexing 
CREATE INDEX operation_index ON fronthis(operation)
-- 创建 Local Indexing 的索引语句
CREATE LOCAL INDEX operation_index ON company(name);
-- 创建覆盖索引
CREATE INDEX IDX_COL1_COVER_COL2 on test(col1) include(col2)
-- 创建函数索引
CREATE INDEX CONCATE_IDX ON TEST(UPPER(COL1|COL2))
-- 查询函数索引
-- SELECT * FROM TEST WHERE UPPER(COL1|COL2)='23'
```

查看当前所有表会发现多出一张索引表（表名为创建的索引名），查询该表数据。

**创建异步索引（批量建立索引）**

在表的数据量较多的时候，直接使用上面的语句进行索引的创建，很容易发生执行超时的问题，一般考虑使用批量建立索引，创建异步索引语法和同步索引相差一个关键字:`ASYNC`

```sh
# 1、在 phoenix 中建立索引表信息
CREATE INDEX fronthis_test ON fronthis("DEFAULT"."log_timestamp","DEFAULT"."operation") ASYNC
# 索引语句加 ANSYC 来进行异步索引建立

# 2、在 HBase 的目录下，启动批量建立索引的 mapreduce 任务
${HBASE_HOME}/bin/hbase org.apache.phoenix.mapreduce.index.IndexTool --data-table FRONTHIS  --index-table FRONTHIS_TEST --output-path ASYNC_INDEX_HFILES
```

#### 4、其他

##### 查询索引中的半索引问题

栗子：

```sql
-- 字段name创建了索引，address是没有创建索引的
select name,address from company where name='dimensoft';
```

这样的查询语句是不会用到索引表的。name字段虽然是索引字段但是address字段并不是索引字段，也就是说*需要查询出来的字段必须都是索引字段*，如`select name from company where name='dimensoft'`。如果希望使用索引表进行查询的话可以使用以下三种方式来解决这个**半索引问题**：

- 强制使用索引表：在进行查询的时候通过sql语句强制使用索引查询 `SELECT /*+ INDEX(company my_index) */ name,address FROM company WHERE name = 'dimensoft';`这样的查询会导致二次检索数据表，第一次检索是去索引表中查询符合name为dimensoft的数据，这时候发现address字段并不在索引字段中，会去company表中第二次扫描，因此只有当用户明确知道符合检索条件的数据较少的时候才适合使用，否则会造成全表扫描，对性能影响较大。
- 创建 covered index：创建索引的时候指定一个 covered 字段，先删除 my_index 索引 `drop index my_index on company;`创建 covered index `create index my_index on company(name) include(address);`使用这种方式创建的索引会导致address字段的值被拷贝到索引中，缺点就是会导致索引表大小有一定的增加。
- 使用 Local Indexing 创建索引：与Global Indexing不同，当使用Local Indexing的时候即使查询的所有字段都不在索引字段中时也会用到索引进行查询（这是由Local Indexing自动完成的）

## F&Q

[F&Q](./Phoenix_F&Q.md)





















