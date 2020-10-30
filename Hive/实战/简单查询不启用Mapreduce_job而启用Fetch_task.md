# 简单查询不启用 Mapreduce Job 而启用 Fetch Task

当需要查询某个表的某列时，Hive 默认是会启用 MapReduce Job 来完成这个任务。我们知道，启用 MapReduce Job 是会消耗系统开销的。对于这个问题，从 Hive0.10.0 版本开始，对于简单的不需要聚合的类似 `SELECT <col> from <table> LIMIT n` 语句，不需要起 MapReduce Job，直接通过 Fetch Task 获取数据，可以通过下面几种方法实现：

**方法一**

在 hive cli 中进行参数的配置，如下：

```bash
set hive.fetch.task.conversion=more;
SELECT id, money FROM m limit 10;
```

**方法二**

```bash
bin/hive --hiveconf hive.fetch.task.conversion=more
```

**方法三**

上面的两种方法都可以开启 Fetch 任务，但作用域都是临时的；如果想一直启用这个功能，可以在 `${HIVE_HOME}/conf/hive-site.xml` 里面加入以下的配置：

```xml
<property>
  <name>hive.fetch.task.conversion</name>
  <value>more</value>
  <description>
    Some select queries can be converted to single FETCH task 
    minimizing latency.Currently the query should be single 
    sourced not having any subquery and should not have
    any aggregations or distincts (which incurrs RS), 
    lateral views and joins.
    1. minimal : SELECT STAR, FILTER on partition columns, LIMIT only
    2. more    : SELECT, FILTER, LIMIT only (+TABLESAMPLE, virtual columns)
  </description>
</property>
```

这样就可以长期启用 Fetch 任务。