### Meta 表作用

HBase Meta 表记录了每个表的所在的 RegionServer 以及 Region 的范围。这样，当用户从客户端读取、写入数据的时候，通过 Meta 表就可以知道数据的 Rowkey 是在那个 Region 的范围以及 Region 所在的 RegionServer。 

### Mete表的Rowkey

region所在的表名+region的StartKey+时间戳。而这三者的MD5值也是HBase在HDFS上存储的region的名字。

### Mete表的列族和列

表中最主要的列簇：`info`

`info`里面包含三个Column：`regioninfo`, `server`, `serverstartcode`。

其中regioninfo就是Region的详细信息，包括StartKey, EndKey 以及每个Family的信息等。server存储的就是管理这个Region的RegionServer的地址。

所以当Region被拆分、合并或者重新分配的时候，都需要来修改这张表的内容。

### Region的定位

第一次读取： 

1. 步骤1：读取ZooKeeper中META表的位置

2. 步骤2：读取.META表中用户表的位置
3. 步骤3：读取数据。

如果已经读取过一次，则root表和`.META`都会缓存到本地，直接去用户表的位置读取数据。