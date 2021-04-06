# YARN 中 Flink 任务的内存分配情况

## TaskManager 内存分配原理

通过 `-ytm` 参数传入的内存值实际是 Container 占用的总内存，TaskManager 可使用的内存为：

```
tm_total_memory=container_size - max[containerized.heap-cutoff-ratio-min , taskmanager.heap.size*containerized.heap-cutoff-ratio]
```

其中

```properties
# Container预留的非TM内存占设定的TM内存的比例，默认值0.25；
containerized.heap-cutoff-ratio: 0.25
# Container预留的非TM内存的最小值，默认值600MB； 
ontainerized.heap-cutoff-min: 600mb 
```

