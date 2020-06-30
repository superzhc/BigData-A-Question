### 大数据环境要求

- Hadoop
- Hive
- Hbase
- Spark（可选）
- Kafka（可选）
- JDK：1.8+

Kylin 依赖于 Hadoop 集群处理大量的数据集，需要准备一个配置好 HDFS、YARN，MapReduce、Hive、HBase、Zookeeper 和其他服务的 Hadoop 集群供 Kylin 运行。

Kylin 可以在 Hadoop 集群的任意节点上启动。

运行 Kylin 的 Linux 账号要有访问 Hadoop 集群的权限，包括创建/写入 HDFS 文件夹，Hive表，HBase表和提交 MapReduce 任务的权限。

### Kylin 安装

1. 从 [Apache Kylin下载网站](https://kylin.apache.org/download/) 下载一个适用于您 Hadoop 版本的二进制文件。例如，适用于 HBase 1.x 的 Kylin 2.5.0 可通过如下命令行下载得到：

   ```sh
   cd /usr/local/
   wget http://mirror.bit.edu.cn/apache/kylin/apache-kylin-2.5.0/apache-kylin-2.5.0-bin-hbase1x.tar.gz
   ```

2. 解压 tar 包，配置环境变量 `$KYLIN_HOME` 指向 Kylin 文件夹

   ```sh
   tar -zxvf apache-kylin-2.5.0-bin-hbase1x.tar.gz
   cd apache-kylin-2.5.0-bin-hbase1x
   export KYLIN_HOME=`pwd`
   ```

3. 检查 Kylin 运行环境：`$KYLIN_HOME/bin/check-env.sh`

4. 启动 Kylin：`$KYLIN_HOME/bin/kylin.sh start`

5. 通过浏览器 `http://hostname:7070/kylin` 查看，初始用户名和密码为 `ADMIN/KYLIN`

6. 运行 `$KYLIN_HOME/bin/kylin.sh stop` 即可停止 Kylin

### Kylin 目录

- `bin`: shell 脚本，用于启动／停止 Kylin，备份／恢复 Kylin 元数据，以及一些检查端口、获取 Hive/HBase 依赖的方法等；
- `conf`: Hadoop 任务的 XML 配置文件，这些文件的作用可参考[配置页面](http://kylin.apache.org/docs/install/configuration.html)
- `lib`: 供外面应用使用的 jar 文件，例如 Hadoop 任务 jar, JDBC 驱动, HBase coprocessor 等.
- `meta_backups`: 执行 `bin/metastore.sh backup` 后的默认的备份目录;
- `sample_cube` 用于创建样例 Cube 和表的文件。
- `spark`: 自带的 spark。（*从v2.6.1开始，Kylin不再包含Spark二进制包*）
- `tomcat`: 自带的 tomcat，用于启动 Kylin 服务。
- `tool`: 用于执行一些命令行的jar文件。

### Kylin 配置

#### 配置文件一览

| 组件名 |           文件名           | 描述                                                         |
| ------ | :------------------------: | ------------------------------------------------------------ |
| Kylin  |     `kylin.properties`     | Kylin 使用的全局配置文件                                     |
| Kylin  |   `kylin_hive_conf.xml`    | Hive 任务的配置项，在构建 Cube 的第一步通过 Hive 生成中间表时，会根据该文件的设置调整 Hive 的配置参数 |
| Kylin  | `kylin_job_conf_inmem.xml` | 包含 MR 任务的配置项，当 Cube 构建算法是 Fast Cubing 时，会根据该文件的设置调整构建任务中的 MR 参数 |
| Kylin  |    `kylin_job_conf.xml`    | MR 任务的配置项，当`kylin_job_conf_inmem.xml`不存在，或 Cube 构建算法是 Layer Cubing 时，会根据该文件的设置调整构建任务中的 MR 参数 |
| Hadoop |      `core-site.xml`       | Hadoop 使用的全局配置文件，用于定义系统级别的参数，如HDFS URL、Hadoop临时目录等 |
| Hadoop |      `hdfs-site.xml`       | 用于配置HDFS参数，如 NameNode 与 DataNode 存放位置、文件副本个数、文件读取权限等 |
| Hadoop |      `yarn-site.xml`       | 用于配置 Hadoop 集群资源管理系统参数，如 ResourceManager 与 NodeManager 的通信端口，web监控端口等 |
| Hadoop |     `mapred-site.xml`      | 用于配置 MR 参数，如 reduce 任务的默认个数，任务所能够使用内存的默认上下限等 |
| HBase  |      `hbase-site.xml`      | 用于配置 Hbase 运行参数，如 master 机器名与端口号，根数据存放位置等 |
| Hive   |      `hive-site.xml`       | 用于配置 Hive 运行参数，如 hive 数据存放目录，数据库地址等   |

#### Hadoop 参数配置

- `yarn.nodemanager.resource.memory-mb` 配置项的值不小于 8192MB
- `yarn.scheduler.maximum-allocation-mb` 配置项的值不小于 4096MB
- `mapreduce.reduce.memory.mb` 配置项的值不小于 700MB
- `mapreduce.reduce.java.opts` 配置项的值不小于 512MB
- `yarn.nodemanager.resource.cpu-vcores` 配置项的值不小于 8

#### kylin.properties 核心参数

| 配置名                                             | 默认值                 | 说明                                      |
| -------------------------------------------------- | ---------------------- | ----------------------------------------- |
| kylin.metadata.url                                 | `kylin_metadata@hbase` | Kylin 元数据库路径                        |
| kylin.env.hdfs-working-dir                         | `/kylin`               | Kylin 服务所用的 HDFS 路径                |
| kylin.server.mode                                  | `all`                  | 运行模式，可以是 all，job，query 中的一个 |
| kylin.source.hive.database-for-flat-table          | `default`              | Hive 中间表保存在哪个 Hive 数据库中       |
| kylin.storage.hbase.compression-codec              | `none`                 | HTable 所采用的压缩算法                   |
| kylin.storage.hbase.table-name-prefix              | `kylin_`               | HTable 表名的前缀                         |
| kylin.storage.hbase.namespace                      | `default`              | HTable 默认表空间                         |
| kylin.storage.hbase.region-cut-gb                  | `5`                    | region 分割的大小                         |
| kylin.storage.hbase.hfile-size-gb                  | `2`                    | hfile 大小                                |
| kylin.storage.hbase.min-region-count               | `1`                    | 最小 region 个数                          |
| kylin.storage.hbase.max-region-count               | `500`                  | 最大 region 个数                          |
| kylin.query.force-limit                            | `-1`                   | 为`select *`语句强制添加 LIMIT 分句       |
| kylin.query.pushdown.update-enabled                | `false`                | 是否开启查询下压                          |
| kylin.query.pushdown.cache-enabled                 | `false`                | 开启查询是否缓存                          |
| kylin.cube.is-automerge-enabled                    | `true`                 | segment 自动合并功能                      |
| kylin.metadata.hbase-client-scanner-timeout-period | `10000`                | HBase 扫描数据的超时时间                  |
| kylin.metadata.hbase-rpc-timeout                   | `5000`                 | 执行 RPC 操作的超时时间                   |
| kylin.metadata.hbase-client-retries-number         | `1`                    | HBase 重试次数                            |

对上述参数的一些说明：

- `kylin.query.force-limit` 默认是没有限制，推荐设置为 1000；
- `kylin.storage.hbase.hfile-size-gb` 可以设置为 1，有助于加快 MR 速度；
- `kylin.storage.hbase.min-region-count` 可以设置为 HBase 节点数，强制数据分散在 N 个节点；
- `kylin.storage.hbase.compression-codec` 默认没有进行压缩，推荐在环境运行情况下配置压缩算法。

#### Spark 相关配置

所有使用 `kylin.engine.spark-conf.` 作为前缀的 Spark 配置属性都能在 `$KYLIN_HOME/conf/kylin.properties` 中进行管理，当然这些参数支持在 Cube 的高级配置中进行覆盖。下面是推荐的 Spark 动态资源分配配置：

```properties
# 运行在yarn-cluster模式，当然可以配置为独立 Spark 集群：spark://ip:7077
kylin.engine.spark-conf.spark.master=yarn
kylin.engine.spark-conf.spark.submit.deployMode=cluster 

# 启动动态资源分配
kylin.engine.spark-conf.spark.dynamicAllocation.enabled=true
kylin.engine.spark-conf.spark.dynamicAllocation.minExecutors=2
kylin.engine.spark-conf.spark.dynamicAllocation.maxExecutors=1000
kylin.engine.spark-conf.spark.dynamicAllocation.executorIdleTimeout=300
kylin.engine.spark-conf.spark.shuffle.service.enabled=true
kylin.engine.spark-conf.spark.shuffle.service.port=7337

# 内存设置
kylin.engine.spark-conf.spark.driver.memory=2G

# 数据规模较大或者字典较大时可以调大 executor 内存
kylin.engine.spark-conf.spark.executor.memory=4G 
kylin.engine.spark-conf.spark.executor.cores=2

# 心跳超时
kylin.engine.spark-conf.spark.network.timeout=600

# 分区大小
kylin.engine.spark.rdd-partition-cut-mb=100
```

#### Cube Planner 相关配置

Cube Planner 是 V2.3 后添加的新功能，使用该功能可以在 Cube 创建成功后即可看到全部 Cuboid 的数目及组合情况；此外配置成功后，可以看到线上的 Query 与 Cuboid 的匹配情况，使得可以查看到热门、冷门甚至没有使用到的 Cuboid，借助这些可以指导我们对 Cube 构建进行二次优化；关于 Cube Planner 的使用，可以参考官方文档：[kylin.apache.org/cn/docs/tut…](https://link.juejin.im?target=http%3A%2F%2Fkylin.apache.org%2Fcn%2Fdocs%2Ftutorial%2Fuse_cube_planner.html)。

![img](../images/166923b9235cf14e)

