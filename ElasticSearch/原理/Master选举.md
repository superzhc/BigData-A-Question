> 前提条件：
>
> 1. 只有节点配置了 `node.master:true` 的候选主节点才能成为主节点
> 2. 最小主节点数(`min_master_nodes`)的目的是防止脑裂

选举流程大致如下：

1. 确认候选主节点数达标，即判断 `elasticsearch.yml` 设置的参数 `discovery.zen.minimum_master_nodes` 的值
2. 比较：先判定是否具备 master 资格，具备候选主节点资格的优先返回；若两节点都为候选节点，则 id 小的值会成为主节点