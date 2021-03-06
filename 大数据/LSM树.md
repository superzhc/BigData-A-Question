# LSM 树

~~LSM 树组织数据的方式：输入数据首先被存储在日志文件，这些文件内的数据完全有序；当有日志文件被修改时，对应的更新会被先保存在内存中来加速查询。当系统经历过许多次数据修改，且内存空间被逐渐占满后，LSM 树会把有序的“键-记录”对写到磁盘中，同时创建一个新的数据存储文件。此时，因为最近的修改都被持久化了，内存中保存的最近更新就可以被丢弃了。~~

LSM 树（Log-structured Merge-tree）的本质是将大量的随机写操作转换成批量的序列写，这样可以极大的提升磁盘数据写入速度，所以 LSM 树非常适合对写操作效率有高要求的应用场景，但是其对应付出的代价是读效率有所降低，这往往可以引入 Bloom Filter 或者缓存等优化措施来对读性能进行改善。

