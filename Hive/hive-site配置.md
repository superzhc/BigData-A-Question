|                         配置项                          | 作用                                                         |         默认值         |
| :-----------------------------------------------------: | ------------------------------------------------------------ | :--------------------: |
| <font color='red'>`hive.metastore.warehouse.dir`</font> | Hive的数据都是存储在 HDFS 上，该配置指定数据存储的位置       | `/user/hive/warehouse` |
|                     动态分区设置：                      |                                                              |                        |
|             **hive.exec.dynamic.partition**             | 是否开启动态分区功能，使用动态分区时候，该参数必须设置成 true |         false          |
|          **hive.exec.dynamic.partition.mode**           | 动态分区的模式，默认strict，表示必须指定至少一个分区为静态分区，nonstrict模式表示允许所有的分区字段都可以使用动态分区。<br>一般需要设置为nonstrict |         strict         |
|      **hive.exec.max.dynamic.partitions.pernode**       | 在每个执行MR的节点上，最大可以创建多少个动态分区。<br/>该参数需要根据实际的数据来设定。<br/>比如：源数据中包含了一年的数据，即day字段有365个值，那么该参数就需要设置成大于365，如果使用默认值100，则会报错。 |          100           |
|          **hive.exec.max.dynamic.partitions**           | 在所有执行MR的节点上，最大一共可以创建多少个动态分区。       |          1000          |
|             **hive.exec.max.created.files**             | 整个MR Job中，最大可以创建多少个HDFS文件。<br>一般默认值足够了，除非你的数据量非常大，需要创建的文件数大于100000，可根据实际情况加以调整。 |         100000         |
|            **hive.error.on.empty.partition**            | 当有空分区生成时，是否抛出异常。                             |         false          |
|                                                         |                                                              |                        |

