# Spark 集群中的节点个数、RDD 分区个数、cpu 内核个数三者与并行度的关系

![img](images/160d4a2cc03d76f4)

1. 每个 file 包含多个 block
2. Spark 读取输入文件时，会根据具体数据格式对应的 InputFormat 进行解析，一般是将若干个 block 合并成一个输入分片，称为 InputSplit，注意**InputSplit不能跨越文件**
3. 一个 InputSplit 生成一个 task
4. 每个 Executor 由若干 core 组成，每个 Executor 的每个 core 一次只能执行一个 Task
5. 每个 Task 执行后生成了目标 RDD 的一个 partition

> 如果 partition 的数量多，能起实例的资源也多，那自然并发度就多
> 
> 如果 partition 的数量少，但是资源多，则 task 数量不足，它也不会有很多并发
> 
> 如果 partition 的数量多，但是资源少（如 core），那么并发也不大，会算完一批再继续下一批

```
Task被执行的并发度 = Executor数目 * 每个Executor核数
```

> 每个 Executor 的 core 数目通过 `spark.executor.cores` 参数设置。这里 cores 其实是指的工作线程。一般来说 Spark 作业配置的 Executor 核数不应该超过机器的物理核数。

**partition 的数目**

1. 数据读入阶段，如 `sc.textFile`，输入文件被划分为多少 InputSplit 就会需要多少初始 Task
2. Map 阶段 partition 数目保持不变
3. Reduce 阶段，RDD 的聚合会触发 Shuffle 操作，聚合后的 RDD 的 partition 数目跟具体操作有关，例如 repartition 操作会聚合成指定分区数，还有一些算子是可配置的