如果节点管理器由于崩溃或运行非常缓慢而失败，就会停止向资源管理器发送心跳信息（或发送频率很低）。如果 10 分钟内（可以通过属性 `yarn.resourcemanager.nm.liveness-monitor.expiry-interval-ms` 设置）没有收到一条心跳信息，资源管理器将会通知停止发送心跳信息的节点管理器，并且将其从自己的节点池中移除以调度启用容器。

对于那些曾经在失败的节点管理器上运行且成功完成的 map 任务，如果属于未完成的作业，那么 application master 会安排它们重新运行。这是由于这些任务的中间输出驻留在失败的节点管理器的本地文件系统中，可能无法被 reduce 任务访问的缘故。

如果应用程序的运行失败次数过高，那么节点管理器可能会被拉黑，即使节点管理自己并没有失败过。由 application master 管理黑名单，对于 MapReduce，如果一个节点管理器上有超过三个任务失败，application master 就会尽量将任务调度到不同的节点上。用户可以通过作业属性 `mapreduce.job.maxtaskfailures.per.tracker` 设置该阈值。

