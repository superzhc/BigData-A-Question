## 收集器设置参数

| 参数                      | 新生代                   | 老年代              |
| ------------------------- | ------------------------ | ------------------- |
| `-XX:+UseSerialGC`        | Serial 收集器            | Serial Old 收集器   |
| `-XX:+UseParNewGC`        | ParNew 收集器            | Serial Old 收集器   |
| `-XX:+UseParallelGC`      | Parallel Scavenge 收集器 | Serial Old 收集器   |
| `-XX:+UseConcMarkSweepGC` | ParNew 收集器            | CMS 收集器          |
| `-XX:+UseParallelOldGC`   | Parallel Scavenge 收集器 | Parallel Old 收集器 |
| `-XX:+UseG1GC`            | G1 收集器                | G1 收集器           |

