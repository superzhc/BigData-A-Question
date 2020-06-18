# YARN

> Apache YARN（Yet Another Resource Negotiator 的缩写）是 Hadoop 的集群资源管理系统。

## YARN 应用运行机制

YARN 通过两类长期运行的守护进程提供自己的核心服务：管理集群上资源使用的资源管理器（resource manager）、运行在集群中所有节点上且能够启动和监控容器（container）的节点管理器（node manager）。容器用于执行特定应用程序的进程，每个容器都有资源限制（内存、CPU等）。一个容器可以是一个 Unix 进程，也可以是一个 Linux cgroup，取决于 YARN 的配置。

![YARN应用的运行机制](D:\superz\BigData-A-Question\Hadoop\YARN\images\image-20200618013151973.png)

为了在 YARN 上运行一个应用，首先，客户端联系资源管理器，要求它运行一个 application master 进程（步骤1）。然后，资源管理器找到一个能够在容器中启动 application master 的节点管理器（步骤 2a 和 2b）。准确的说，application master 一旦运行起来后能够做些什么依赖于应用本身。有可能是在所处的容器中简单地运行一个计算，并将结果返回给客户端；或是向资源管理器请求更多的容器（步骤3），以用于运行一个分布式计算（步骤 4a 和 4b）。

## 调度

> YARN 中有三种调度器可用：**FIFO调度器（FIFO Scheduler）**、**容量调度器（Capacity Scheduler）**和**公平调度器（Fair Scheduler）**。

FIFO 调度器将应用放置在一个队列中，然后按照提交的顺序（先进先出）运行应用。首先为队列中第一个应用的请求分配资源，第一个应用的请求被满足后再依次为队列中下一个应用服务。

FIFO 调度器的优点是，简单易懂，不需要任何配置，但是不适合共享集群。大的应用会占用集群中的所有资源，所以每个应用必须等待直到轮到自己运行。在一个共享集群中，更适合使用容器调度器或公平调度器。这两种调度器都允许长时间运行的作业能及时完成，同时也允许正在进行较小临时查询的用户能够在合理时间内得到返回结果。

使用 Capactity 调度器时，一个独立的专门队列保证小作业一提交就可以启动，由于队列容量是为那个队列中的作业所保留，因此这种策略是以整个集群的利用率为代价的。

使用公平调度器时，不需要预留一定量的资源，因为调度器会在所有运行的作业之间动态平衡资源。

## 配置

### 配置调度器

通过查看 `yarn.resourcemanager.scheduler.class` 配置项来配置调度器，如配置公平调度器：

```xml
<property>
    <name>yarn.resourcemanager.scheduler.class</name>
    <value>org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler</value>
</property>
```





























