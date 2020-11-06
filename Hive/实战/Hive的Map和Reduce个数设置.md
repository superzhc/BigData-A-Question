# Hive 的 Map 和 Reduce 个数如何设置

## Map 数量

一般情况下，作业会根据输入目录产生一个或者多个 map 任务。map 任务的个数主要由如下因素决定：

- 输入文件总个数
- 输入文件的大小
- 集群设置的文件块大小（默认为 128M）

**示例1**

> 假设输入目录下有 1 个文件 c，其大小为 680M，Hadoop 会将该文件 c 切分为 6 个块（1 个 40M 的和 5 个 128M 大小的块），对应的 map 数为6。

**示例2**

> 假设输入目录下有 4 个文件 a , b , c, d , 它们的大小分别为 5M, 10M, 128M, 140M，那么 Hadoop 会将其切分为 5 个块（5 个块的大小分别为 5M, 10M, 128M, 128M, 12M) ，对应的 Map 数是 5，即如果文件大于块大小(128M)，会进行拆分；如果小于块大小，则把该文件当成一个块进行处理

### 减少 Map 数量

一般减少 Map 的数量是通过**合并小文件**的方式。

> 备注：
> 
> 如果一个任务包含很多小文件（远远小于所设置的块大小），那么每个小文件都会被被当做一个独立的块且对应一个 map。在上面这种情况下，map 任务启动和初始化的时间远远大于逻辑处理的时间，造成很大的资源浪费。

可以通过如下配置参数来在 map 执行前合并小文件，减少 map 数：

```sql
set mapred.max.split.size=100000000;
set mapred.min.split.size.per.node=100000000;
set mapred.min.split.size.per.rack=100000000;
set hive.input.format=org.apache.hadoop.hive.ql.io.CombineHiveInputFormat;
```

### 增加 Map 数量

将一个文件合理的拆分成多个，既可以实现用多个 Map 任务去完成。

示例如下：

```sql
set mapred.reduce.tasks=10;
create table tmp3 as select * from a distribute by rand(10); 
```

这样会将 a 表的记录，随机的分散到包含 10 个文件的 tmp3 表中，再用 tmp3 代替 a 表进行复杂的查询，则会用 10 个 map 任务去完成。每个map任务只处理部分数据，效率会提高不少。

## Reduce 数量

Reduce 个数的设定极大影响任务执行的效率，不指定 reduce 个数的情况下，Hive 会猜测一个 reduce 个数，基于以下两个设定：

1. `hive.exec.reducers.bytes.per.reducer`：每个 reduce 任务处理的数据量，默认为 `1000^3=1G`
2. `hive.exec.reducers.max`：每个任务最大的 reduce 数，默认为 999

计算公式为：`reduce个数=min(参数2, 总输入数据量/参数1)`

### 调整 reduce 数量

- 调整 `hive.exec.reducers.bytes.per.reducer` 参数的值
  ```sql
  set hive.exec.reducers.bytes.per.reducer=500000000; /*500M*/
  ```
- 命令中明确配置 reduce 的个数，那么 Hive 就不会推测 reduce 的个数
  ```sql
  set mapred.reduce.tasks = 10;
  ```
