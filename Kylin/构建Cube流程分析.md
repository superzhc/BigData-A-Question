Build 操作是构建一个 Cube 指定的时间区间的数据，Kylin 是基于预计算获得高效的查询速度，因此需要通过 Build，将原始数据（比如 Hive）转化为目标数据（比如 HBase）。

![【Kylin教程】（四）Build Cube流程分析](../images/20180701112428.png)

#### Step1：生成中间临时数据（Create Intermediate Flat Hive Table）

根据 Cube 定义生成的原始数据，会新创建一个 Hive 外部表，然后根据 Cube 定义的星型模型，查询出维度（Derived 类型的维度是使用的外键）和度量的值插入到新创建的表中。

- **创建外部表**

表名根据 Cube 名称和 Segment 的 uuid（增量 cube 为时间范围）生成的

![1564558155727](../images/1564558155727.png)

- **插入数据**

此步选择出事实表和维度表按照 Join 方式之后出现在维度或者度量参数中的列，然后再加上用户设置的 where 条件和 partition 时间条件

![1564558308990](../images/1564558308990.png)

#### Step2：重新分配（Redistribute intermediate table）

经过上一个步骤之后，Hive 会在 HDFS 的目录中生成一些数据文件，但是一些文件可能会很大，而另外一些文件可能会很小甚至是空的。文件大小分布的不均衡也会导致后续的 MR 任务执行的不平衡：一些 mapper 任务会执行的很快，而其他的 mapper 可能会执行的很慢。为了使这些数据分布的更均匀一些，Kylin 增加了该步骤用来重新分配各个数据文件中的数据。

![1564558603252](../images/1564558603252.png)

#### Step3：创建事实表的 Distinct Column 文件（Extract Fact Table Distinct Columns）

计算出现在表中每一个维度和度量的 Distinct 值，如果某一个维度列的 distinct 值比较大，那么可能导致 MR 任务 OOM 。所以如果此步骤不能在合理的时间内完成，请重新对 Cube 进行设计，因为真正的 Build 过程会花费更长的时间。

![1564559011150](../images/1564559011150.png)

#### Step4：构建维度词典（Build Dimension Dictionary）

根据上一步中已经获得了所有维度列的 distinct 值得文件，接着 Kylin 将会在内存中构建词典。词典是为了节约存储而设计的，用于将一个成员值编码成一个整数类型并且可以通过整数值获取到原始成员的值。每一个 Cuboid 成员是以 key-value 存储在 HBase 中，但是 key 一般是 string，将它转为整数值可以减少内存占用。通常这一步会很快，但是如果 distinct 值得集合很大，Kylin 可能会报错，例如，`Too high cardinality is not suitable for dictionary`

![1564559285532](../images/1564559285532.png)

#### Step5：保存 Cuboid 统计信息（Save Cuboid Statistics）

#### Step6：创建 HTable（Create HTable）

#### Step7：计算生成 Base Cuboid 数据文件（Build Base Cuboid）

首先清楚Base Cuboid是什么：假如一个Cube有四个维度A,B,C,D 那么这四种维度的所有可能组合就是Base Cuboid，类似于在查询中`select count(1) from table group by A,B,C,D` 这个查询结果的个数就是Base Cuboid的成员数。也是通过MR任务完成的，输入的是第一步中的输出文件。

#### Step8：计算第 N 层的 Cuboid 文件（Build N-Dimension Cuboid）

该流程是由多个步骤组成，逐层算法的处理过程，步骤的数量是根据维度组合的Cuboid总数决定的。每一步都使用前一步的输出作为输入，然后去除某个维度进行聚合，生成一个子cuboid。例如，对于cuboid ABCD，去除维度A可以获得cuboid BCD，去除维度B可以获得cuboid ACD等。

![【Kylin教程】（四）Build Cube流程分析](../images/2018070207114455.png)

我们可以看一下该流程生成的HDFS数据文件：

```sh
[root@cm-master apache-kylin-2.3.1-bin]# hadoop fs -ls -R /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/
drwxr-xr-x   - root supergroup          0 2018-06-30 13:17 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_1_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:17 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_1_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup     117125 2018-06-30 13:17 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_1_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:18 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_2_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:18 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_2_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup     727188 2018-06-30 13:18 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_2_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:19 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_3_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:19 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_3_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup    1927350 2018-06-30 13:19 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_3_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:21 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_4_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:21 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_4_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup    2752555 2018-06-30 13:21 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_4_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:22 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_5_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:22 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_5_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup    2115698 2018-06-30 13:22 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_5_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:23 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_6_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:23 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_6_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup     740767 2018-06-30 13:23 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_6_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:24 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_7_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:24 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_7_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup      61947 2018-06-30 13:24 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_7_cuboid/part-r-00000
drwxr-xr-x   - root supergroup          0 2018-06-30 13:16 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_base_cuboid
-rw-r--r--   2 root supergroup          0 2018-06-30 13:16 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_base_cuboid/_SUCCESS
-rw-r--r--   2 root supergroup     119175 2018-06-30 13:16 /kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/level_base_cuboid/part-r-00000
```

#### Step9：基于内存构建 Cube（Build Cube In-Mem）

如果聚合都在 Reduce 端，那么通过 Shuffle 传输到Reduce端会造成很大的网络压力。那么可不可以把聚合放到Map端来做呢？Reduce端再做最后的聚合。这样Reduce收到的数据就会变小，网络压力得以减轻。

该步骤在执行的时候会使用 `conf/kylin_job_conf_inmem.xml` 中的相关配置项，我们会发现增大了Mapper端的内存，因为部分聚合放到了Mapper端。

#### Step10：将 Cuboid 转为 HFile（Convert Cuboid Data to HFile）

将 Cuboid 数据转化为 HFile。输入数据包括所有的 Cuboid文件，输出 HFile 文件

```sh
-input hdfs://cm-master:8020/kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/cuboid/* 
-output hdfs://cm-master:8020/kylin/kylin_metadata/kylin-b9454c80-57a4-4124-adc1-1c3a5a81f73b/bdstar_cube/hfile 
-htablename KYLIN_UU1M5PEI5C 
```

#### Step11：将 HFile 导入到 HBase 表中（Load HFile to HBase Table）

这一步使用了 HBase API 将 HFile 导入到 HBase 的 region 中

#### Step12：更新 Cube 信息（Update Cube Info）

将数据导入 HBase 中之后，Kylin 会将新生成的 Segment 在元数据中的状态修改为 Ready

#### Step13：清理中间表（Hive Cleanup）

将之前生成的临时中间表删除

