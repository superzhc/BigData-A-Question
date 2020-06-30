## Sqoop Import 原理

![img](../images/855959-20170317111255385-1909693524.png)

Sqoop 在 import 时，需要指定 `split-by` 参数。Sqoop 根据不同的 `split-by` 参数值来进行切分，然后将切分出来的区域分配到不同 map 中。

每个 map 中再处理数据库中获取的一行一行的值，写入到 HDFS 中（由此可知，导入导出的事务是以 Mapper 任务为单位）。

同时 `split-by` 根据不同的参数类型有不同的切分方法，如 Int 类型，Sqoop 会取最大和最小 `split-by` 字段值，然后根据传入的 **num-mappers** 来确定划分几个区域。

比如 `select max(split_by),min(split-by) from table` 得到的 `max(split-by)` 和 `min(split-by)` 分别为1000和1，而 num-mappers 为2的话，则会分成两个区域(1,500)和(501-1000),同时也会分成2个 sql 给2个 map 去进行导入操作，分别为 `select XXX from table where split-by>=1 and split-by<500` 和 `select XXX from table where split-by>=501 and split-by<=1000`。最后每个map各自获取各自SQL中的数据进行导入工作。