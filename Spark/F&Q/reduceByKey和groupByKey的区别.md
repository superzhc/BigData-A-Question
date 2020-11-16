# reduceByKey 和 groupbyKey 的区别

官网定义如下：

**`reduceByKey(func, numPartitions=None)`**

> Merge the values for each key using an associative reduce function. This will also perform the merginglocally on each mapper before sending results to a reducer, similarly to a “combiner” in MapReduce. Output will be hash-partitioned with numPartitions partitions, or the default parallelism level if numPartitions is not specified.
> 
> 也就是，reduceByKey 用于对每个 key 对应的多个 value 进行 merge 操作，最重要的是它能够在本地先进行 merge 操作，并且 merge 操作可以通过函数自定义。

**`groupByKey(numPartitions=None)`**

> Group the values for each key in the RDD into a single sequence. Hash-partitions the resulting RDD with numPartitions partitions. 
> 
> Note: If you are grouping in order to perform an aggregation (such as a sum or average) over each key, using reduceByKey or aggregateByKey will provide much better performance.
> 
> 也就是，groupByKey 也是对每个 key 进行操作，但只生成一个 sequence。需要特别注意 “Note” 中的话，它告诉我们：如果需要对 sequence 进行 aggregation 操作（注意，groupByKey 本身不能自定义操作函数），那么，选择 reduceByKey/aggregateByKey 更好。这是因为 groupByKey 不能自定义函数，我们需要先用 groupByKey 生成 RDD，然后才能对此 RDD 通过 map 进行自定义函数操作。

区别如下：

1. 当采用 reduceByKey 时，Spark 可以在每个分区移动数据之前将待输出数据与一个共用的 key 结合。
2. 当采用 groupByKey 时，由于它不接收函数，Spark 只能先将所有的键值对(key-value pair)都移动，这样的后果是集群节点之间的开销很大，导致传输延时。

因此，**在对大数据进行复杂计算时，reduceByKey 优于 groupByKey**。

另外，如果仅仅是 group 处理，那么以下函数应该优先于 groupByKey ：

1. combineByKey 组合数据，但是组合之后的数据类型与输入时值的类型不一样。
2. foldByKey 合并每一个 key 的所有值，在级联函数和“零值”中使用。

