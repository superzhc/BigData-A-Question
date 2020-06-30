在 DataX 的逻辑模型中包括 Job 和 Task 两个维度，通过将 Job 进行 Task 拆分，然后将 Task 合并到 TaskGroup 进行运行。

Job 实例运行在 JobContainer 容器中，它是所有任务的 master，负责初始化、拆分、调度、运行、回收、监控和汇报，但它并不做实际的数据同步操作。

- Job：Job 是 DataX 用以描述从一个源头到一个目的端的同步作业，是 DataX 数据同步的最小业务单元。比如：从一张 mysql 表同步到 odps 的一个表的特定分区
- Task：Task 是为最大化而把 Job 拆分得到的最小执行单元。比如：读一张有 1024 个分表的 mysql 分库分表的 Job，拆分成 1024 个读 Task，用若干个并发执行
- TaskGroup：描述的是一组 Task 集合。在同一个 TaskGroupContainer 执行下的 Task 集合称之为 TaskGroup
- JobContainer：Job 执行器，负责 Job 全局拆分、调度、前置语句和后置语句等工作的工作单元
- TaskGroupContainer：TaskGroup 执行器，负责执行一组 Task 的工作单元。

简而言之，Job 拆分成 Task，分别在框架提供的容器中执行，插件只需要实现 Job 和 Task 两部分逻辑。