# StateBackend

默认情况下，State 会保存在 TaskManager 的内存中，CheckPoint 会存储在 JobManager 的内存中。State 和 CheckPoint 的存储位置取决于 StateBackend 的配置。Flink 一共提供了 3 种 StateBackend：

- MemoryStateBackend
  State 数据保存在 Java 堆内存中，执行 CheckPoint 的时候，会把 State 的快照数据保存到 JobManager 的内存中。基于内存的 StateBackend 在生产环境下不建议使用。
- FsStateBackend
  State 数据保存在 TaskManager 的内存中，执行 CheckPoint 的时候，会把 State 的快照数据保存到配置的文件系统中，可以使用 HDFS 等分布式文件系统。
- RocksDBStateBackend
  RocksDB 跟上面的都略有不同，它会在本地文件系统中维护状态，State 会直接写入本地 RocksDB 中。同时它需要配置一个远端的 FileSystem URI（一般是 HDFS），在进行 CheckPoint 的时候，会把本地的数据直接复制到远端的 FileSystem 中。Fail Over（故障切换）的时候直接从远端的 Filesystem 中恢复数据到本地。RocksDB 克服了 State 受内存限制的缺点，同时又能够持久化到远端文件系统中，推荐在生产中使用。

## 使用

### 全局配置

修改 `flink-conf.yaml` 配置文件，主要修改下面两个参数：

```yaml
state.backend: filesystem
state.checkpoints.dir: hdfs://namenode:9000/flink/checkpoints
```

`state.backend` 的值可以是下面这 3 种:

- `jobmanager` 表示使用 MemoryStateBackend
- `filesystem` 表示使用 FsStateBackend
- `rocksdb` 表示使用 RocksDBStateBackend

### 单任务调整

```java
env.setStateBackend(new MemoryStateBackend());
env.setStateBackend(new FsStateBackend("hdfs://namenode:9000/flink/checkpoints"));
env.setStateBackend(new RocksDBStateBackend(filebackend,true));
```

**注意**：在使用 RocksDBStateBackend 时需要引入第三方依赖：

```xml
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-statebackend-rocksdb_2.11</artifactId>
    <version>1.12.2</version>
</dependency>
```
