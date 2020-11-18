# Hive 数据导入和导出方式

## 数据导入

### 从文件系统中导入数据到 Hive 表

```sql
LOAD DATA [LOCAL] INPATH 'filepath' [OVERWRITE] INTO TABLE tablename [PARTITION(partcol1=val1,...)]
-- LOAD 操作只是单纯的复制/移动操作，将数据文件移动到 Hive 表对应的位置。

-- filepath：相对路径 or 绝对路径 or 包含模式的完整URI
-- 相对路径：如project/data1，如果发现是相对路径，则路径会被解释为相对于当前用户的当前路径
-- 绝对路径：如/user/hive/project/data1
-- 包含模式的完整URI：如hdfs://namenode:9000/user/hive/project/data1
-- filepath 可以引用一个文件（这种情况下，Hive 会将文件移动到表所对应的目录中）或者是一个目录（在这种情况下，Hive会将目录中的所有文件移动至表所对应的目录中）

-- LOCAL关键字：指定了 LOCAL，即本地文件
-- 如果没有指定 LOCAL，但 filepath 指向一个完整的 URI，hive会直接使用这个 URI
-- 若未指定 LOCAL，且 filepath 并不是一个完整的 URI，hive会根据 hadoop 配置文件的配置项 fs.default.name 指定的 NameNode 的 URI 来计算出完整的 URI。
-- 如果没有指定 schema 或者 authority，Hive 会使用在 hadoop 配置文件中定义的 schema 和 authority

-- OVERWRITE关键字：
-- 目标表（或者分区）中的内容（如果有）会被删除，然后再将 filepath 指向文件/目录中的内容添加到表/分区中

-- 加载的目标可以是一个表或者分区。如果表包含分区，必须指定每一个分区的分区名

-- 加载本地文件
LOAD DATA LOCAL INPATH 'customer.txt' INTO TABLE customer;
-- 加载HDFS文件
LOAD DATA INPATH 'customer.txt' INTO TABLE customer;
-- 加载本地文件，同时给定分区信息
LOAD DATA LOCAL INPATH './examples/files/kv2.txt' OVERWRITE INTO TABLE invites PARTITION (ds='2008-08-15');
```

### 从别的表中查询出相应的数据导入到 Hive 表

```sql
-- 基本模式
INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1 FROM from_statement

-- 多插入模式：MULTI_TABLE_INSERT
FROM from_statement
INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1
[INSERT OVERWRITE TABLE tablename2 [PARTITION ...] select_statement2] ...

-- 自动分区模式
INSERT OVERWRITE TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) select_statement FROM from_statement
```

**示例**

```sql
INSERT OVERWRITE TABLE customer PARTITION (day = '2020-11-10') SELECT day,url from source_table;

FROM customer a INSERT OVERWRITE TABLE events SELECT a.bar, count(1) WHERE a.foo > 0 GROUP BY a.bar;

-- 将同一个表的数据插入到多个地方
FROM src
INSERT OVERWRITE TABLE dest1 SELECT src.* WHERE src.key < 100
INSERT OVERWRITE TABLE dest2 SELECT src.key, src.value WHERE src.key >= 100 and src.key < 200
INSERT OVERWRITE TABLE dest3 PARTITION(ds='2008-04-08', hr='12') SELECT src.key WHERE src.key >= 200 and src.key < 300
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/dest4.out' SELECT src.value WHERE src.key >= 300;
```

### 在创建表的时候通过从别的表中查询出相应的记录并插入到所创建的表中

在实际情况中，表的输出结果可能太多，不适于显示在控制台上，这时候将 Hive 的查询输出结果直接存在一个新的表中是非常方便的，我们称这种情况为 CTAS（`create table .. as select`）如下：

```sql
create table customer 
as 
select id, name, tel from customer_temp;
```

## 数据导出

```sql
INSERT OVERWRITE [LOCAL] DIRECTORY directory1
  [ROW FORMAT row_format] [STORED AS file_format] 
  SELECT ... FROM ...
```

如果指定了**LOCAL**关键字，则为导出到本地文件系统，否则，导出到**HDFS**。使用**ROW FORMAT**关键字可以指定导出的文件分隔符。

### 导出到本地文件系统

```sql
insert overwrite local directory '/home/user1/customer'
    select * from customer;
```

> 注：和导入数据到 Hive 不一样，不能用 `insert into` 来将数据导出

### 导出到 HDFS 中

```sql
insert overwrite directory '/home/user1/customer'
    select * from customer;
```