## Spark 本地运行模式

`local` 模式：测试使用

## Spark运行集群的三种部署方式

Spark应用程序在集群上部署运行时，可以由不同的组件为其提供资源管理调度服务（资源包括CPU、内存等）。比如，可以使用自带的**独立集群管理器（standalone）**，或者**使用 YARN**，也可以**使用 Mesos**。因此，Spark包括三种不同类型的集群部署方式，包括 `standalone`、`Spark on Mesos` 和 `Spark on YARN`。

1. `standalone`  模式
   与MapReduce1.0框架类似，Spark框架本身也自带了完整的资源调度管理服务，可以独立部署到一个集群中，而不需要依赖其他系统来为其提供资源管理调度服务。在架构的设计上，Spark与MapReduce1.0完全一致，都是由一个Master和若干个Slave构成，并且以槽（slot）作为资源分配单位。不同的是，Spark中的槽不再像MapReduce1.0那样分为 Map 槽和 Reduce 槽，而是只设计了统一的一种槽提供给各种任务来使用。
2. `Spark on Mesos` 模式
   Mesos是一种资源调度管理框架，可以为运行在它上面的Spark提供服务。`Spark on Mesos` 模式中，Spark程序所需要的各种资源，都由Mesos负责调度。由于Mesos和Spark存在一定的血缘关系，因此，Spark这个框架在进行设计开发的时候，就充分考虑到了对Mesos的充分支持，因此，相对而言，Spark运行在Mesos上，要比运行在YARN上更加灵活、自然。目前，Spark官方推荐采用这种模式，所以，许多公司在实际应用中也采用该模式。
3. `Spark on YARN`模式
   Spark可运行于YARN之上，与Hadoop进行统一部署，即“Spark on YARN”，其架构如下图所示，资源管理和调度依赖YARN，分布式存储则依赖HDFS。

![Spark on YARN架构](../images/图9-13-Spark-on-Yarn架构.jpg)
