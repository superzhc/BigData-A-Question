### 显示命令

```sql
show databases;
show tables;
show partitioins;
show functions;
describe extended table_name.col_name -- 这个不知道是干啥的~~
```

### 数据库

#### 查看所有库

默认情况下，hive安装完成之后，会有一个`default`库

```sql
SHOW DATABASES;
-- 切换到某个数据库下
use <databasename>;
```

 <font color='red'>Hive中的数据库在HDFS上的存储路径为：`${hive.metastore.warehouse.dir}/databasename.db`</font>

#### 创建数据库

```sql
CREATE (DATABASE|SCHEMA) [IF NOT EXISTS] database_name
  	[COMMENT database_comment]
  	[LOCATION hdfs_path] -- 创建的时候指定数据库在 HDFS 上的存储位置
  	[WITH DBPROPERTIES (property_name=property_value, ...)];
-- 示例：
-- 创建一个库
CREATE DATABASE IF NOT EXISTS testdb;
```

hive中的库的概念对应于在 `hive-site.xml` 中配置项 `hive.metastore.warehouse.dir` 指定目录的一个子目录。

#### 修改数据库

修改数据库属性：

```sql
ALTER (DATABASE|SCHEMA) database_name SET DBPROPERTIES (property_name=property_value, …);
```

修改数据库属主：

```sql
ALTER (DATABASE|SCHEMA) database_name SET OWNER [USER|ROLE] user_or_role;
```

#### 删除数据库

```sql
-- DROP DATABASE|SCHEMA [IF EXISTS] <database_name> [RESTRICT|CASCADE];
-- 删除库
DROP DATABASE testdb;
-- 如果库中存在表的表，直接删除会出错，可以加上cascade强制删除
DROP DATABASE testdb CASCADE;
```

### 表

#### 查看所有表

```sql
show tables;
-- 按正则表达式显示表
show tables 'superz*';
```

表的存储路径：

- 默认情况下，表的存储路径为：`${hive.metastore.warehouse.dir}/databasename.db/tablename/`

- 可以使用 <font color='red'>`desc formatted <tablename>;`</font> 命令查看表的详细信息，其中包括了存储路径：`Location:               hdfs://cdh5/hivedata/warehouse/lxw1234.db/lxw1234`


#### 创建表

```sql
CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.] table_name
[(col_name data_type [COMMENT col_comment], ...)]
[COMMENT table_comment]
[PARTITIONED BY(partcol_name data_type [COMMENT partcol_comment],...)] -- 表示该表为分区表
[ROW FORMAT row_format] -- 指定表的分隔符
----delimited fields terminated by '\t'  通过'\t'分割字段
----lines terminated by '\n'            通过'\n'结束一行字段
[STORED AS file_format] -- 指定表在 HDFS 上的文件存储格式
[LOCATION path] -- 指定表在 HDFS 上的存储位置

-- 示例
-- 创建外部表，关键字 external table
CREATE EXTERNAL TABLE page_view(
    userid BIGINT,
    ip STRING COMMENT 'IP Address of the User')
COMMENT 'This is the staging page view table'
FIELDS TERMINATED BY '\054'
STORED AS TEXTFILE
LOCATION '<hdfs_location>';

-- 创建分区表，关键字 partitioned
CREATE TABLE par_table(
    userid BIGINT,
    ip STRING COMMENT 'IP Address of the User')
COMMENT 'This is the page view table'
PARTITIONED BY(date STRING, pos STRING)
ROW FORMAT DELIMITED ‘\t’
FIELDS TERMINATED BY '\n'
STORED AS SEQUENCEFILE;

-- 建Bucket表
CREATE TABLE par_table(
    userid BIGINT,
    ip STRING COMMENT 'IP Address of the User')
COMMENT 'This is the page view table'
PARTITIONED BY(date STRING, pos STRING)
CLUSTERED BY(userid) SORTED BY(viewTime) INTO 32 BUCKETS
ROW FORMAT DELIMITED ‘\t’
   FIELDS TERMINATED BY '\n'
STORED AS SEQUENCEFILE;

-- 创建表并创建索引字段 ds
CREATE TABLE invites(foo INT, bar STRING) PARTITIONED BY (ds STRING); 

-- 复制一个空表
CREATE TABLE empty_key_value_store LIKE key_value_store;
```

#### 修改表

```sql
-- 重命名表
ALTER TABLE name RENAME TO new_name
ALTER TABLE events RENAME TO new_event

-- 表添加一列
ALTER TABLE name ADD COLUMNS (col_name data_type [COMMETN col_comment],...)
ALTER TABLE pokes ADD COLUMNS (new_col INT)
ALTER TABLE invites ADD COLUMNS (new_col2 INT COMMENT 'a comment') -- 添加一列并增加列字段注释 

-- 更新列
ALTER TABLE name REPLACE COLUMNS (col_name data_type [COMMETN col_comment],...)

-- ADD 是代表新增一字段，字段位置在所有列后面（partition 列前）
-- REPLACE 则是表示替换表中所有字段

-- 更改声明，修改列的名字、类型、位置、注释
ALTER TABLE table_name CHANGE [COLUMN] col_old_name col_new_name column_type [COMMENT col_comment] [FIRST|AFTER column_name]

-- 删除列
ALTER TABLE name DROP [COLUMN] column_name

-- 添加表的元数据信息
ALTER TABLE table_name SET TBLPROPERTIES table_properties
-- table_properties:[property_name=property_value,...]
-- 用户可以用这个命令向表中添加 metadata

-- 改变表文件格式与组织
ALTER TABLE table_name SET FILEFORMAT file_format
ALTER TABLE table_name CLUSTERED BY(userid) SORTED BY(viewTime) INTO num_buckets BUCKETS
-- 这个命令修改了表的物理存储属性

-- 增加分区
ALTER TABLE table_name ADD [IF NOT EXISTS] partition_spec [LOCATION 'location1'] ...
-- 注，partition_spec:PARTITION (partition_col = partition_col_value,...)

-- 删除分区
ALTER TABLE table_name DROP partition_spec, partition_spec,...
```

#### 删除表

```sql
DROP TABLE [IF EXISTS] table_name;
```

### 视图

#### 创建视图

```sql
CREATE VIEW [IF NOT EXISTS] [db_name.]view_name 
[(column_name [COMMENT column_comment], …) ]
[COMMENT view_comment]
[TBLPROPERTIES (property_name = property_value, …)]
AS SELECT …;

-- 如果只提供视图名，无列名，则视图列的名字将由定义的 SELECT 表达式自动生成
-- 如果修改基本表的属性，视图中不会体现，无效查询将会失败
-- 视图是只读的，不能用 LOAD/INSERT/ALTER
```

#### 删除视图

```sql
DROP VIEW IF EXISTS view_name;
```

#### 修改视图

```sql
ALTER VIEW view_name AS SELECT ...
```

### 分区

#### 创建分区表

在[创建表](#创建表)的添加 `PARTITIONED BY (col_name data_type [COMMENT col_comment],...)` 来指定该表为分区表，后面括号中指定了分区的字段和类型，分区字段可以有多个，在 HDFS 中对应多级目录

#### 添加分区

##### 使用 `INSERT` 添加分区

往分区中追加数据：

```sql
INSERT INTO TABLE t_lxw1234 PARTITION (month = ‘2015-06′,day = ‘2015-06-15′)
```

覆盖分区数据:

```sql
INSERT overwrite TABLE t_lxw1234 PARTITION (month = ‘2015-06′,day = ‘2015-06-15′)
```

##### 使用 `ALERT TABLE` 添加分区

```sql
ALTER TABLE t_lxw1234 ADD PARTITION (month = ‘2015-06′,day = ‘2015-06-15′) location ‘hdfs://namenode/tmp/lxw1234/month=2015-06/day=2015-06-15/';
```

#### 查看分区对应的 HDFS 路径

- 使用命令 `show partitions table_name;` 查看表的所有分区
- 使用 `desc formatted table_name partition(col_name=col_value[,...])` 查看该分区的详细信息，包括该分区在 HDFS 上的路径

#### 删除分区

可以使用 `ALERT TABLE <table_name> DROP PARTITION(col_name=col_value[,...])` 删除一个分区

### INSERT

#### 向数据表内加载文件

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
LOAD DATA LOCAL INPATH ‘/home/lxw1234/t_lxw1234/’ INTO TABLE t_lxw1234;
-- 加载HDFS文件
LOAD DATA INPATH ‘/user/lxw1234/t_lxw1234/’ INTO TABLE t_lxw1234;
-- 加载本地文件，同时给定分区信息
LOAD DATA LOCAL INPATH './examples/files/kv2.txt' OVERWRITE INTO TABLE invites PARTITION (ds='2008-08-15');
```

#### 将查询结果插入 Hive 表

```sql
-- 基本模式
INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1 FROM from_statement
-- 多插入模式
FROM from_statement
INSERT OVERWRITE TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1
[INSERT OVERWRITE TABLE tablename2 [PARTITION ...] select_statement2] ...
-- 自动分区模式
INSERT OVERWRITE TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) select_statement FROM from_statement

INSERT OVERWRITE TABLE t_lxw1234 PARTITION (day = ‘2015-06-15’) SELECT day,url from source_table;
```

#### 将查询结果写入 HDFS 文件系统

```sql
INSERT OVERWRITE [LOCAL] DIRECTORY directory1
  [ROW FORMAT row_format] [STORED AS file_format] 
  SELECT ... FROM ...
```

如果指定了**LOCAL**关键字，则为导出到本地文件系统，否则，导出到**HDFS**。使用**ROW FORMAT**关键字可以指定导出的文件分隔符。

#### `INSERT INTO`

```sql
INSERT INTO  TABLE tablename1 [PARTITION (partcol1=val1, partcol2=val2 ...)] select_statement1 FROM from_statement
```

### Select

##### 基本查询操作

Hive中的SELECT基础语法和标准SQL语法基本一致，支持WHERE、DISTINCT、GROUP BY、ORDER BY、HAVING、LIMIT、子查询等；

```sql
[WITH CommonTableExpression (, CommonTableExpression)*]  
SELECT [ALL | DISTINCT] select_expr, select_expr, ...
FROM table_reference
[WHERE where_condition]
[GROUP BY col_list]
[CLUSTER BY col_list
  | [DISTRIBUTE BY col_list] [SORT BY | ORDER BY col_list]
]
[LIMIT number]

-- 使用 ALL 和 DISTINCT 选项区分对重复记录的处理。默认是 ALL，表示查询所有记录。DISTINCT 表示去掉重复的记录
-- ORDER BY 与 SORD BY的不同：
-- ORDER BY 全局排序，只有一个 Reduce 任务
-- SORT BY 只做本机排序

-- 示例
-- 按条件查询
select a.foo from invites a where a.ds='<DATE>';

-- 将查询数据输出至目录
INSERT OVERWRITE DIRECTORY '/tmp/hdfs_out' SELECT a.* FROM invites a WHERE a.ds='<DATE>';

-- 将查询结果输出至本地目录
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/local_out' SELECT a.* FROM pokes a;

-- 选择所有列进行输出
INSERT OVERWRITE TABLE events SELECT a.* FROM profiles a;
INSERT OVERWRITE TABLE events SELECT a.* FROM profiles a WHERE a.key < 100;
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/reg_3' SELECT a.* FROM events a;
INSERT OVERWRITE DIRECTORY '/tmp/reg_4' select a.invites, a.pokes FROM profiles a;
INSERT OVERWRITE DIRECTORY '/tmp/reg_5' SELECT COUNT(1) FROM invites a WHERE a.ds='<DATE>';
INSERT OVERWRITE DIRECTORY '/tmp/reg_5' SELECT a.foo, a.bar FROM invites a;
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/sum' SELECT SUM(a.pc) FROM pc1 a;

-- 将一个表的统计结果插入另一个表中
FROM invites a INSERT OVERWRITE TABLE events SELECT a.bar, count(1) WHERE a.foo > 0 GROUP BY a.bar;
INSERT OVERWRITE TABLE events SELECT a.bar, count(1) FROM invites a WHERE a.foo > 0 GROUP BY a.bar;
JOIN
FROM pokes t1 JOIN invites t2 ON (t1.bar = t2.bar) INSERT OVERWRITE TABLE events SELECT t1.bar, t1.foo, t2.foo;

-- 将同一个表的数据插入到多个地方
FROM src
INSERT OVERWRITE TABLE dest1 SELECT src.* WHERE src.key < 100
INSERT OVERWRITE TABLE dest2 SELECT src.key, src.value WHERE src.key >= 100 and src.key < 200
INSERT OVERWRITE TABLE dest3 PARTITION(ds='2008-04-08', hr='12') SELECT src.key WHERE src.key >= 200 and src.key < 300
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/dest4.out' SELECT src.value WHERE src.key >= 300;
```

#### 基于 Partition 的查询

一般 SELECT 查询会扫描整个表，使用 PARTITIONED BY 子句建表，查询就可以利用分区剪枝（input pruing）的特性

