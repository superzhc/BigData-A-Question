# JobManger

JobManager 负责整个 Flink 集群任务的调度以及资源的管理，从客户端中获取提交的应用，然后根据集群中 TaskManager 上 TaskSlot 的使用情况，为提交的应用分配相应的 TaskSlot 资源并命令 TaskManager 启动从客户端中获取的应用。

JobManager 相当于整个集群的 Master 节点，且整个集群有且只有一个活跃的 JobManager ，负责整个集群的任务管理和资源管理。

JobManager 和 TaskManager 之间通过 Actor System 进行通信，获取任务执行的情况并通过 Actor System 将应用的任务执行情况发送给客户端。

同时在任务执行的过程中，Flink JobManager 会触发 Checkpoint 操作，每个 TaskManager 节点收到 Checkpoint 触发指令后，完成 Checkpoint 操作，所有的 Checkpoint 协调过程都是在 Fink JobManager 中完成。

当任务完成后，Flink 会将任务执行的信息反馈给客户端，并且释放掉 TaskManager 中的资源以供下一次提交任务使用。

## 启动流程

JobManager 的职责主要是接收 Flink 作业，调度 Task，收集作业状态和管理 TaskManager。它包含一个 Actor，并且做如下操作：

- RegisterTaskManager: 它由想要注册到 JobManager 的 TaskManager 发送。注册成功会通过 AcknowledgeRegistration 消息进行 Ack。
- SubmitJob: 由提交作业到系统的 Client 发送。提交的信息是 JobGraph 形式的作业描述信息。
- CancelJob: 请求取消指定 id 的作业。成功会返回 CancellationSuccess，否则返回 CancellationFailure。
- UpdateTaskExecutionState: 由 TaskManager 发送，用来更新执行节点(ExecutionVertex)的状态。成功则返回 true，否则返回 false。
- RequestNextInputSplit: TaskManager 上的 Task 请求下一个输入 split，成功则返回 NextInputSplit，否则返回 null。
- JobStatusChanged： 它意味着作业的状态(RUNNING, CANCELING, FINISHED,等)发生变化。这个消息由 ExecutionGraph 发送。