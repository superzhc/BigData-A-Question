### 从本地文件系统中导入

```sql
load data local inpath 'customer.data' into table customer;
```

### 从HDFS上导入

```sql
load data inpath 'hive/customer.data' into table customer;
```

### 从别的表中查询出相应的数据导入

```sql
insert into table customer select * from customer_temp;
```