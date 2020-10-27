# 多个 Job 调度

Job 是一个 Action 操作触发的计算单元，在一个 Spark Application 内部，多个并行的 Job 是可以同时运行的 。

## FIFO 调度

默认情况下，Spark 的调度会使用 FIFO 的方式来调度多个 Job。每个 Job 都会被划分为多个 Stage，而且第一个 Job 会对所有可用的资源获取优先使用权，并且让它的 Stage 的 task 去运行，然后第二个 Job 再获取资源的使用权，以此类推。

## Fair 调度

在公平的资源共享策略下，Spark 会将多个 Job 的 task 使用一种轮询的方式来分配资源和执行，所以所有的 Job 都有一个基本公平的机会去使用集群的资源

```scala
conf.set("spark.scheduler.mode", "FAIR")
```

```bash
--conf spark.scheduler.mode=FAIR
```

## 公平调度资源池

Fair scheduler 也支持将 Job 分成多个组并放入多个池中，以及为每个池设置不同的调度优先级。这个特性对于将重要的和不重要的 Job 隔离运行的情况非常有用，可以为重要的 Job 分配一个池，并给予更高的优先级；为不重要的 Job 分配另一个池，并给予较低的优先级。

在代码中设置 `sparkContext.setLocalProperty("spark.scheduler.pool", "poolName")`，所有在这个线程中提交的 Job 都会进入这个池中,设置是以线程为单位保存的，很容易实现用同一线程来提交同一用户的所有作业到同一个资源池中。设置为null则清空池子。

默认情况下，每个池子都会对集群资源有相同的优先使用权，但是在每个池内，Job 会使用 FIFO 的模式来执行。

可以通过配置文件来修改池的属性

1. schedulingMode：FIFO/FAIR，来控制池中的 Jobs 是否要排队，或者是共享池中的资源
2. weight：控制资源池相对其他资源池，可以分配到资源的比例。默认情况下，所有池子的权重都是 1.如果将某个资源池的 weight 设为 2，那么该资源池中的资源将是其他池子的 2 倍，如果将 weight 设得很高，如 1000，可以实现资源池之间的调度优先级 – weight=1000 的资源池总能立即启动其对应的作业。
3. minShare：每个资源池最小资源分配值（CPU 个数）,公平调度器总是会尝试优先满足所有活跃（active）资源池的最小资源分配值，然后再根据各个池子的 weight 来分配剩下的资源。因此，minShare 属性能够确保每个资源池都能至少获得一定量的集群资源。minShare 的默认值是 0。

配置文件默认地址 `spark/conf/fairscheduler.xml`，自定义文件 `conf.set("spark.scheduler.allocation.file", "/path/to/file")`

```xml
<allocations>
  <pool name="production">
    <schedulingMode>FAIR</schedulingMode>
    <weight>1</weight>
    <minShare>2</minShare>
  </pool>
  <pool name="test">
    <schedulingMode>FIFO</schedulingMode>
    <weight>2</weight>
    <minShare>3</minShare>
  </pool>
</allocations>
```

没有在配置文件中配置的资源池都会使用默认配置:

- schedulingMode : FIFO
- weight : 1
- minShare : 0