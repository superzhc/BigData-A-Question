# YARN 资源参数

|                    参数                    | 描述                                 |  默认值  | 备注                                                                                            |
| :----------------------------------------: | ------------------------------------ | :------: | ----------------------------------------------------------------------------------------------- |
|   `yarn.nodemanager.resource.memory-mb`    | 表示该节点上YARN可使用的物理内存总量 | `8192MB` | 注意，如果你的节点内存资源不够8GB，则需要调减小这个值，而YARN不会智能的探测节点的物理内存总量。 |
|   `yarn.scheduler.minimum-allocation-mb`   | 单个任务可申请的最少内存             | `1024MB` |                                                                                                 |
|   `yarn.scheduler.maximum-allocation-mb`   | 单个任务可申请的最大内存             | `8192MB` |                                                                                                 |
|   `yarn.nodemanager.resource.cpu-vcores`   | 表示该节点上YARN可使用的虚拟CPU个数  |    8     |                                                                                                 |
| `yarn.scheduler.minimum-allocation-vcores` | 单个任务可申请的最小虚拟CPU个数      |    1     |                                                                                                 |
| `yarn.scheduler.maximum-allocation-vcores` | 单个任务可申请的最多虚拟CPU个数      |    32    |                                                                                                 |
|``||||