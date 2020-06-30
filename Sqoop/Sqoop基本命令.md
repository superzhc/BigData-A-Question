### 基本操作

可以使用 `sqoop help`来查看，sqoop 支持哪些命令

![1563873359956](../images/1563873359956.png)

查询到这些支持的命令之后，如果不知道使用方式，可以使用 `sqoop help <command>` 的方式来查看某条具体命令的使用方式，比如：

![1563873692344](../images/1563873692344.png)

#### 列出数据库有哪些库

```sh
sqoop list-databases \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111
```

#### 列出某个数据库有哪些表

```sh
sqoop list-tables \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111
```

### Sqoop的数据导入

```sh
# 语法格式
sqoop import (generic-args) (import-args)
```

相关参数见[参数列表](./参数列表.md)文档

#### 将 RDBMS 导入到 HDFS

**1、普通导入：导入 Oracle 数据库中的 SFZK 的数据到 HDFS 上**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111 \
--table SFZK \
-m 1
```

**2、指定分隔符和导入路径**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111 \
--table SFZK \
--target-dir /user/hadoop/sfzk2 \
--fields-terminated-by '\t' \
-m 2
```

**3、带where条件**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd --password 11111 \
--where "gmsfhm like '32058219930302%'" \
--table SFZK \
--target-dir /user/hadoop/sfzk3 \
-m 1
```

**4、查询指定列**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd --password 11111 \
--columns GMSFZK,XM \
--where "gmsfhm like '32058219930302%'" \
--table SFZK \
--target-dir /user/hadoop/sfzk4 \
-m 1

# --columns：指定导出哪几列
```

**5、指定自定义查询 SQL**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd --password 11111 \
--target-dir /user/hadoop/sfzk5 \
--query 'select gmsfhm,xm from sfzk where $CONDITIONS and gmsfhm like "32058219930302%"'  \
--split-by gmsfhm
--fields-terminated-by '\t'  \
-m4

# --query：导入过程使用SQL语句 
# --split-by：表中的那一列作为任务分配列
# 在以上需要按照自定义SQL语句导出数据到HDFS的情况下：
# 1、引号问题，要么外层使用单引号，内层使用双引号，$CONDITIONS的$符号不用转义， 要么外层使用双引号，那么内层使用单引号，然后$CONDITIONS的$符号需要转义
# 2、自定义的SQL语句中必须带有WHERE \$CONDITIONS
```

**6、Select语句导入空值处理**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd --password 11111 \
--target-dir /user/hadoop/sfzk5 \
--query 'select gmsfhm,xm from sfzk where $CONDITIONS and gmsfhm like "32058219930302%"'  \
--split-by gmsfhm  \
-m 2 \
--null-string '' \
--null-non-string ''

# 参数解析： 
# 当从数据库中拉取过来的数据存在空值，默认Sqoop会设置为null，通过下面参数，来替换null值。 
# 举例语句中，null值，设置为了空： 
# --null-string：string类型替换为第一个''中指定的值 
# --null-non-string：非string类型替换为第二个''中指定的值
```

**7、导入库下所有语句**

```sh
sqoop import-all-tables \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111

# import-all-tables：导入指定库下面所有的表
```

#### 将 RDBMS 导入到 Hive

Sqoop 导入关系型数据到 Hive 的过程是先导入到 HDFS，然后再 `load` 进 Hive

**普通导入：数据存储在默认的 `default` Hive库中，表名就是对应的mysql的表名：**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111 \
--table SFZK  \
--hive-import  \
-m 1

# --hive-import：该次导入任务为导向hive
```

导入过程：

> 1. 导入表 SFZK 的数据到 HDFS 的默认路径
> 2. 自动仿造 SFZK 去创建一张 Hive 表，创建在默认的 default 库中
> 3. 把临时目录中的数据导入到 Hive 表中

**指定行分隔符和列分隔符，指定 `hive-import`，指定覆盖导入，指定自动创建 Hive 表，指定表名，指定删除中间结果数据目录**

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111 \
--table SFZK  \
--fields-terminated-by "\t"  \
--lines-terminated-by "\n"  \
--hive-import  \
--hive-overwrite  \
--create-hive-table  \
--delete-target-dir \
--hive-database  mydb_test \
--hive-table new_help_keyword

# 注：sqoop会自动给创建hive的表。 但是不会自动创建不存在的库

# 等价于上面的语句
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111 \
--table SFZK  \
--fields-terminated-by "\t"  \
--lines-terminated-by "\n"  \
--hive-import  \
--hive-overwrite  \
--create-hive-table  \ 
--hive-table  mydb_test.new_help_keyword  \
--delete-target-dir

# --hive-table：要导入的hive表 
# --hive-database：导入的hive库 
```

#### 将 RDBMS 导入到 HBase

```sh
sqoop import \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL \
--username epoint_zjg_pbd \
--password 11111 \
--table SFZK  \
--hbase-table new_help_keyword \
--column-family person \
--hbase-row-key help_keyword_id

# --hbase-table：要导入的hbase表 
# --column-family：指定hbase表中列族 
# --hbase-create-table：如果HBase表没有创建，自行创建 
# 注：导入到 HBase 中，需要先创建表，再执行导入语句
```

**Mysql bulk load方式导入到hbase**

```sh
sqoop import --connect jdbc:mysql://master:3306/tmpbase --username root --password password --table user_info --hbase-table user_info --column-family f --hbase-createtable --hbase-bulkload

# 参数解析: 
# --hbase-table：要导入的hbase表 
# --column-family：指定hbase表中列族 
# --hbase-create-table：如果HBase表没有创建，自劢创建 
# --hbase-bulkload：以bulkload方式导入HBase，而非直接put
```

### Sqoop 增量导入

将 Sqoop 导入语句执行两次，在执行第二次时会出现错误：

```sh
ERROR tool.ImportTool: Encountered IOException running import job: org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory hdfs://node190:8020/user/hive/warehouse/anqi_wang already exists
```

这表示 HDFS 中已经存在相应存储，此时需要执行 Sqoop-Hive 的增量导入语句

注：**由于Hive没有rowkey，其hdfs存储决定了Sqoop-Hive只能添加，update更新导入无法进行。**

#### Append 增量模式

> Append 模式：追加模式，必须是数值字段（1,2,3,4....）
>
> 适用范围：有自增序列
>
> 注：
>
> 1. **incremental append 模式不支持数据导入到 Hive**

```sh
sqoop import \
--connect jdbc:mysql://mysql-serverip:3306/sqoop \
--username root \
--password root \
--query 'select * from user_info where $CONDITIONS' \
--split-by id \
--target-dir /data/sqoop/ \
-m 1 \
--incremental append \
--check-column grade \
--last-value 80

# 参数解析： 
# --incremental：指定sqoop增量模式 
# --check-column：指定增量的列 
# --last-value：指定列值从那一行开始
```

#### Lastmodified 增量模式

> Lastmodified模式：根据时间戳更新模式，这种模式要求**标识字段必须是日期值（适合类型有date,time,datetime,timestamp）**
>
> 更新数据是指在数据表中发生变化的数据，指定一个标识字段，并指定一个更新值，当目的路径已存在该目录时，需要添加一个 `--append` 或 `--merge-key`

```sh
sqoop import \
--connect jdbc:mysql://msyql-serverip:3306/sqoop \
--username root --password root \
--query 'select * from user_info where $CONDITIONS' \
--split-by id \
--target-dir /data/sqoop \
-m 1 \
--incremental lastmodified \
--check-column c_date \
--append \
--last-value '2015-03-05 01:16:18'

# 参数解析： 
# --incremental：指定sqoop增量模式 
# --check-column：指定增量的列（datatime类型） 
# --last-value：指定从那一段时间到当前开始 
# 上面的语句将到处表 user_info 中字段 c_date 的值 >'2015-03-05 01:16:18' 且 <'当前系统时间' 的所有数据记录，不同之处在于 --append 参数表示根据更新要求直接将抽取出来的数据附加至目标目录，而 --merg-key 参数则表示此更新操作将分成 2 个 MR Job，Job1 将表中的更新数据导入到 HDFS 中一个临时目录下，Job2 将结合新数据和已经存在 HDFS 上的旧数据按照 merge-key 指定的字段进行合并（类似于去重），并输出到目标目录，每一行仅保留最新的值
# 注：此模式支持导入至 Hive表，但是即使使用了 merge-key 参数也无法使新数据和旧数据进行合并去重
```

#### 保存上次导入的值

 无论 incremental 是 append 模式还是 lastmodified 模式，都需要指定标识字段和上次更新值，如果手动写命令更新导入，则需要记录每一次的导入后打印的值。

可以利用 sqoop metastore 保存导入导出的参数及其值

1. 创建 sqoop job

   ```sh
   sqoop job --create test -- import --connect jdbc:oracle:thin:@//myoracle:1521/pdbORCL --username DM --password-file sqoop.pwd --table WE --hive-import --hive-table we1 --incremental lastmodified --check-column LT --last-value '2016-04-13 19:11:10' --merge-key ID
   ```

2. 执行 sqoop job

   ```sh
   sqoop job --exec test
   ```

每次执行 `sqoop job --exec test`，sqoop metastore 会保存此次 job 中 last-value 的最新值（即每次执行的系统时间），无需手动记录，以便于自动增量导入。

### Sqoop的数据导出

#### 数据导出到 mysql 表

```sh
sqoop export --connect jdbc:mysql://mysql-serverip:3306/test --username root --password root --table user_info_ --fields-terminated-by ',' --export-dir /user/root/mysql/user_info --input-null-string '' --input-null-non-string ''

# 参数解析： 
# export：从HDFS导出mysql（或其他RDBMS） 
# --table：mysql当中表 
# --fields-terminated-by：源数据字段分隔符 
# --export-dir：源数据HDFS上位置
```

#### Batch 模式数据导入到 mysql 表

```sh
sqoop export -Dsqoop.export.records.per.statement=10 -- connect jdbc:mysql://mysql-server-ip:3306/sqoop --username root --password root --table user_info_bak --fields-terminatedby ',' --export-dir /user/root/mysql/user_info
```

参数解析 
`--Dsqoop.export.records.per.statement=10`指定每10条数据执行一次insert类似 `INSERT INTO xxx VALUES (), (), (), ... `
或是 
`--Dsqoop.export.records.per.transaction=10`指定每次事务多少条记录被insert，类似BEGIN; INSERT, INSERT, .... COMMIT

#### HDFS数据对 mysql 表做更新

```sh
sqoop export --connect jdbc:mysql://mysql-serverip:3306/sqoop --username sqoop --password sqoop --table user_info_bak --update-key id --fields-terminated-by ',' --export-dir /user/root/mysql/user_info
```

参数解析 
`--update-key id`指定根据那个列进行更新，也可指定多列，用逗号分隔。 

相当于：

```sql
update user_info_bak set name=‘xxx’… where id=“1’;
update user_info_bak set name=‘ yyy’… where id=“2’;
-- …
```

#### HDFS导入 mysql 表空值处理

```sh
sqoop export --connect jdbc:mysql://mysql-serverip:3306/sqoop --username sqoop --password sqoop --table user_info --input-null-string '' --input-null-non-string ''
```

参数解析 
同导入的含义相同 
`--input-null-string`：会被翻译成数据库中string列NULL的值 
`--input-null-non-string`：被翻译成数据库中非string列NULL的值

### Sqoop Eval

`sqoop eval` 允许用户执行用户定义的查询，对各自的数据库服务器和在控制台中预览结果。

- **查询数据**

```sh
sqoop eval \
--connect jdbc:oracle:thin:@192.168.186.36:1521:ORCL --username epoint_zjg_pbd --password 11111 \
--query "select * from SUPERZ_SQOOP"
```

### Sqoop Job

#### 定义 Job

```sh
sqoop job --create user_info -- import --connect jdbc:mysql://mysql-server-ip:3306/sqoop --username root --password root --table user_info -m 1

# 参数解析： 
# sqoop job --create：将创建一个Job名字为user_info
```

#### 执行 Job 及修改Job参数

```sh
sqoop job --exec user_info

# 参数解析： 
# sqoop job --exec：将执行已经定义好的user_info覆盖定义Job的默认参数 
# sqoop job --exec user_info -m 3：覆盖之前存在的参数-m 1为-m 3
```

#### 删除某个 Job

```sh
sqoop job --delete user_info
```

#### 查看当前可用的 Job

```sh
sqoop job --list
```

#### 查看某个具体 Job 的信息

```sh
sqoop job --show user_info
```

