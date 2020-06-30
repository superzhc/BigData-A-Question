DataX 支持单机多线程模式完成同步作业运行，如下是一个 DataX 作业生命周期的时序图：

![img](../images/aa6c95a8-6891-11e6-94b7-39f0ab5af3b4.png)

**核心模块介绍**：

1. DataX 完成单个数据同步的作业，称之为 Job，DataX 接受到一个 Job 之后，将启动一个进程来完成整个作业同步过程。DataX Job 模块是单个作业的中枢管理，承担了数据清理、子任务划分（将单一作业计算转化为多个子 Task）、TaskGroup 管理等功能
2. DataX Job 启动后，会根据不同的源端切分策略，将 Job 切分成多个小的 Task（子任务），以便于并发执行。Task 便是 DataX 作业的最小单元，每一个 Task 都会负责一部分数据的同步工作
3. 切分多个 Task 之后，DataX Job 会调用 Scheduler 模块，根据配置的并发数据量，将拆分成的 Task 重新组合，组装成 TaskGroup（任务组）。每一个 TaskGroup 负责以一定的并发运行完毕分配好的所有 Task，默认单个任务组的并发数量为 5
4. 每一个 Task 都由 TaskGroup 负责启动，Task 启动后，会固定启动 `Reader`-->`Channel`-->`Writer` 的线程来完成任务同步工作
5. DataX 作业运行起来之后，Job 监控并等待多个 TaskGroup 模块任务完成，等待所有 TaskGroup 任务完成后 Job 成功退出。否则，异常退出，进程退出值非 0

**DataX 调度流程**：

