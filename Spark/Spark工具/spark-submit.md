# spark-submit

> `spark-submit`脚本用于在集群上启动应用程序，它位于Spark的bin目录中。这种启动方式可以通过统一的界面使用所有的Spark支持的集群管理功能，因此不必为每个应用程序专门配置应用程序。

## Spark 支持三种集群管理方式

- Standalone：Spark自带的一种集群管理方式，易于构建集群
- Mesos：通用的集群管理，可以在其上运行Hadoop MapReduce和一些服务应用
- Yarn：Hadoop2.0中的资源管理器

## spark-submit 详细参数说明

| 参数名                | 参数说明                                                                                                             |
| --------------------- | -------------------------------------------------------------------------------------------------------------------- |
| —master               | master 的地址，提交任务到哪里执行，例如 `spark://host:port`, yarn, local。具体指可参考下面关于Master_URL的列表       |
| —deploy-mode          | 在本地 (client) 启动 driver 或在 cluster 上启动，默认是 client                                                       |
| —class                | 应用程序的主类，仅针对 java 或 scala 应用                                                                            |
| —name                 | 应用程序的名称                                                                                                       |
| --jars                | 用逗号分隔的本地 jar 包，设置后，这些 jar 将包含在 driver 和 executor 的 classpath 下。【注：不支持通配符添加jar包】 |
| —packages             | 包含在driver 和executor 的 classpath 中的 jar 的 maven 坐标                                                          |
| —exclude-packages     | 为了避免冲突 而指定不包含的 package                                                                                  |
| —repositories         | 远程 repository                                                                                                      |
| —conf PROP=VALUE      | 指定 spark 配置属性的值， 例如 `-conf spark.executor.extraJavaOptions='-XX:MaxPermSize=256m'`                        |
| —properties-file      | 加载的配置文件，默认为 `conf/spark-defaults.conf`                                                                    |
| —driver-memory        | Driver内存，默认 1G                                                                                                  |
| —driver-java-options  | 传给 driver 的额外的 Java 选项                                                                                       |
| —driver-library-path  | 传给 driver 的额外的库路径                                                                                           |
| —driver-class-path    | 传给 driver 的额外的类路径                                                                                           |
| —driver-cores         | Driver 的核数，默认是1。在 yarn 或者 standalone 下使用                                                               |
| —executor-memory      | 每个 executor 的内存，默认是1G                                                                                       |
| —total-executor-cores | 所有 executor 总共的核数。仅仅在 mesos 或者 standalone 下使用                                                        |
| —num-executors        | 启动的 executor 数量。默认为2。在 yarn 下使用                                                                        |
| —executor-core        | 每个 executor 的核数。在yarn或者standalone下使用                                                                     |

### Master URL

| Master URL        | 含义                                                                                               |
| ----------------- | -------------------------------------------------------------------------------------------------- |
| local             | 使用1个worker线程在本地运行Spark应用程序                                                           |
| local[K]          | 使用K个worker线程在本地运行Spark应用程序                                                           |
| local[*]          | 使用所有剩余worker线程在本地运行Spark应用程序                                                      |
| spark://HOST:PORT | 连接到Spark Standalone集群，以便在该集群上运行Spark应用程序                                        |
| mesos://HOST:PORT | 连接到Mesos集群，以便在该集群上运行Spark应用程序                                                   |
| yarn-client       | 以client方式连接到YARN集群，集群的定位由环境变量HADOOP_CONF_DIR定义，该方式driver在client运行。    |
| yarn-cluster      | 以cluster方式连接到YARN集群，集群的定位由环境变量HADOOP_CONF_DIR定义，该方式driver也在集群中运行。 |

## 配置文件

`spark-submit`脚本可以从属性文件加载默认的spark配置值，并将他们传递到应用程序。默认情况下，它将从Spark目录中的`conf/spark-default.conf`中读取选项。

以这种方式加载默认Spark配置可以避免需要某些标志来引发提交。通常，在SparkConf上显式设置的配置值具有最高优先级，然后将标志传递给`spark-submit`，然后将该值设置为默认值。

如果不清楚配置选项的来源，可以使用`--verbose`选项运行`spark-submit`来打印出细粒度的调试信息

## Spark 应用程序的依赖

如果用户的代码依赖于其他项目、第三方包，则需要将它们与应用程序一起打包，才能将代码分发到Spark集群。为此需要创建一个包含代码及其依赖关系的程序集jar。

> sbt和Maven都有集成插件。

创建程序集jar时，列出Spark和Hadoop作为提供的依赖项，这些不需要捆绑，因为它们在运行时由集群管理器提供。

> 对于Python，可以使用`spark-submit`的`--py-files`参数来添加`.py`、`.zip`和`.egg`文件以与应用程序一起发布。如果依赖多个Python文件，建议将它们打包成`.zip`或`.egg`

### 高级依赖管理

当使用`spark-submit`时，应用程序jar以及`-jars`选项中包含的任何jar将被自动上传到集群上。`--jars`之后提供的URL必须用逗号分隔。该列表包含在驱动程序和执行器类路径上。目录扩展不适用于`--jars`。

**Spark使用以下URL方案来允许不同的策略来传播jar**：

- `file:`：绝对路径和`file:/`URI由驱动程序的HTTP文件服务器提供，每个执行程序从驱动程序HTTP服务器提取文件。
- `hdfs:`,`http:`,`https:`,`ftp:`：这些按照预期从URI中下拉文件和JAR
- `local:`：以`local:/`开头的URI预计作为每个工作节点上的本地文件存在。这意味着不会出现网络IO，并且适用于推送到每个工作者的大型文件/JAR，或通过NFS，GlusterFS等共享。

请注意，JAR和文件将复制到执行程序节点上每个SparkContext的工作目录。这可能会随着时间的推移占用大量空间，并需要清理。使用YARN，清理将自动进行处理，并且通过Spark standalone，可以使用`spark.worker.cleanup.appDataTtl`属性配置自动清理。

## spark-submit 提交应用程序示例

```sh
# 在本地运行(本地模式8核)
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master local[8] \
  /path/to/examples.jar \
  100 # 参数

# Run on a Spark standalone cluster in client deploy mode(standalone client模式)
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master spark://207.184.161.138:7077 \
  --executor-memory 20G \
  --total-executor-cores 100 \
  /path/to/examples.jar \
  1000

# Run on a Spark standalone cluster in cluster deploy mode with supervise(standalone cluster模式使用supervise)
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master spark://207.184.161.138:7077 \
  --deploy-mode cluster \
  --supervise \
  --executor-memory 20G \
  --total-executor-cores 100 \
  /path/to/examples.jar \
  1000

# Run on a YARN cluster(YARN cluster模式)
export HADOOP_CONF_DIR=XXX
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master yarn \
  --deploy-mode cluster \  # can be client for client mode
  --executor-memory 20G \
  --num-executors 50 \
  /path/to/examples.jar \
  1000

# Run on a Mesos cluster in cluster deploy mode with supervise(Mesos cluster模式使用supervise)
./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master mesos://207.184.161.138:7077 \
  --deploy-mode cluster \
  --supervise \
  --executor-memory 20G \
  --total-executor-cores 100 \
  http://path/to/examples.jar \
  1000

# Run a Python application on a Spark standalone cluster(standalone cluster模式提交python application)
./bin/spark-submit \
  --master spark://207.184.161.138:7077 \
  examples/src/main/python/pi.py \
  1000
```