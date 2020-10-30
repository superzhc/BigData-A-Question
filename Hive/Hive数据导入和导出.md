# Hive 数据导入和导出方式

## 数据导入

### 从本地文件系统中导入数据到 Hive 表

```sql
create table customer(id int, name string,age int, tel string) 
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' 
    STORED AS TEXTFILE;
load data local inpath 'customer.txt' into table customer;
```

### HDFS 上导入数据到 Hive 表

从本地文件系统中将数据导入到 Hive 表的过程中，其实是先将数据临时复制到 HDFS 的一个目录下，然后再将数据从那个临时目录下移动到对应的 Hive 表的数据目录里面。既然如此，那么 Hive 肯定支持将数据直接从 HDFS 上的一个目录移动到相应 Hive 表的数据目录下

```sql
create table customer(id int, name string,age int, tel string) 
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' 
    STORED AS TEXTFILE;
load data inpath 'hive/customer.txt' into table customer;
```

### 从别的表中查询出相应的数据导入到 Hive 表

```sql
create table customer(id int, name string,age int, tel string) 
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' 
    STORED AS TEXTFILE;
insert into table customer select * from customer_temp;
```

### 在创建表的时候通过从别的表中查询出相应的记录并插入到所创建的表中

在实际情况中，表的输出结果可能太多，不适于显示在控制台上，这时候将 Hive 的查询输出结果直接存在一个新的表中是非常方便的，我们称这种情况为 CTAS（`create table .. as select`）如下：

```sql
create table customer 
as 
select id, name, tel from customer_temp;
```

## 数据导出

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