数据的存储文件叫 HFile，HFile 中存储的是经过排序的键值映射结构，结构如下所示：

![HFile结构](D:\superz\BigData-A-Question\HBase\images\HFilev1.png)

文件内部由连续的块组成，块的索引信息存储在文件的尾部。当把 HFile 打开并加载到内存中时，索引信息会优先加载到内存中，每个块的默认大小是 64 KB，可以根据需要配置不同的块大小。

每一个 HFile 都有一个块索引，通过一个磁盘查找就可以实现查询。首先，在内存的块索引中进行二分查找，确定可能包含给定键的块，然后读取磁盘块找到实际要找的键。

## 命令行解析

```sh
${HBASE_HOME}/bin/hbase hfile -m -f <HDFS中HFile文件地址>
# 示例如下：
${HBASE_HOME}/bin/hbase hfile -m -f /hbase/data/default/superz_demo/6073d66131d693a5f924c0f33751c917/baseinfo/8a433657a99042e5b964f2eca93c0575
```

上述命令执行结果如下：

![返回结果](D:\superz\BigData-A-Question\HBase\images\1563343270999.png)

