YARN 中的应用程序在运行失败的时候有几次尝试机会，就像 MapReduce 任务在遇到硬件或网络故障时要进行几次尝试一样。运行 MapReduce application master 的最多尝试次数由 `mapreduce.am.max-attempts` 属性控制。默认值是 2，即如果 MapReduce application master 失败两次，便不会再进行尝试，作业将失败。

YARN 对集群上运行的 YARN application master 的最大尝试次数加以了限制，单个的应用程序不可以超过这个限制。该限制由 `yarn.resourcemanager.am.max-attemps` 属性设置，默认值是 2，这样如果想增加 MapReduce application master 的尝试次数，也必须增加集群上 YARN 的设置。

恢复的过程如下：

application master 向资源管理器发送周期性的心跳，当 application master 失败时，资源管理器将检测到该失败并在一个新的容器（由节点管理器管理）中开始一个新的 master 实例。对于 MapReduce application master，它将使用作业历史来恢复失败的应用程序所运行任务的状态，使其不必重新运行。默认情况下恢复功能是开启的，但可以通过设置 `yarn.app.mapreduce.am.job.recovery.enable` 为 false 来关闭这个功能。

MapReduce 客户端向 application master 轮询进度报告，但是如果它的 application master 运行失败，客户端就需要定位新的实例。在作业初始化期间，客户端向资源管理器询问并缓存 application master 的地址，使其每次需要向 application master 查询时不必重载资源管理器。但是，如果 application master 运行失败，客户端就会在发出状态更新请求时经历超时，这时客户端会折回向资源管理器请求新的 application master 的地址。这个过程对用户是透明的。

