以下参数一般直接在请求地址后添加，如`/_cat/health?v`

| 参数 | 描述     |
| ---- | -------- |
| v    | 启动表头 |

在使用 `/_cat/health` 进行查询的时候只显示了一部分信息，当需要查询更多的指标，但是比起查阅文档，可以对任意 API 添加 `?help` 参数来做到这一点。

如 `/_cat/health?help` 显示的结果如下：

```
epoch                 | t,time                                   | seconds since 1970-01-01 00:00:00  
timestamp             | ts,hms,hhmmss                            | time in HH:MM:SS                   
cluster               | cl                                       | cluster name                       
status                | st                                       | health status                      
node.total            | nt,nodeTotal                             | total number of nodes              
node.data             | nd,nodeData                              | number of nodes that can store data
shards                | t,sh,shards.total,shardsTotal            | total number of shards             
pri                   | p,shards.primary,shardsPrimary           | number of primary shards           
relo                  | r,shards.relocating,shardsRelocating     | number of relocating nodes         
init                  | i,shards.initializing,shardsInitializing | number of initializing nodes       
unassign              | u,shards.unassigned,shardsUnassigned     | number of unassigned shards        
pending_tasks         | pt,pendingTasks                          | number of pending tasks            
max_task_wait_time    | mtwt,maxTaskWaitTime                     | wait time of longest task pending  
active_shards_percent | asp,activeShardsPercent                  | active number of shards in percent 

```

第一列显示完整的名称，第二列显示缩写，第三列提供了关于这个参数的简介。现在知道了一些列名了，可以用 `?h` 参数来明确指定显示这些指标：

```http
GET /_cat/nodes?v&h=ip,port,heapPercent,heapMax

ip            port heapPercent heapMax
192.168.1.131 9300          53 990.7mb
```