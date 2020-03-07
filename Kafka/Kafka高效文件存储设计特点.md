# Kafka 高效文件存储设计特点

- Kafka 把 topic 中一个 partition 大文件分成多个小文件段，通过多个小文件段，就容易定期清除或删除已经消费完文件，减少磁盘占用
- 通过索引信息可以快速定位 message 和确定 response 的最大大小
- 通过 index 元数据全部映射到 memory，可以避免 segment file 的 IO 磁盘操作
- 通过索引文件稀疏存储，可以大幅度降低 index 文件元数据占用空间大小