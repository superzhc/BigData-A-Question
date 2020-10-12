### Spark任务调度和资源调度


### 资源调度源码分析

#### 分析以集群方式提交命令后的资源调度源码

资源调度的源码（Master.scala）位置：
[![img](http://chant00.com/media/15052101744162.jpg)](http://chant00.com/media/15052101744162.jpg)
1.Worker启动后向Master注册
2.client向Master发送一条消息，为当前的Application启动一个Driver进程

[![img](http://chant00.com/media/15052102502535.jpg)](http://chant00.com/media/15052102502535.jpg)
schedule()方法是对Driver和Executor进行调度的方法，看看启动Driver进程的过程：

[![spark_资源调度源码分析_Driver进程的创建](http://chant00.com/media/spark_%E8%B5%84%E6%BA%90%E8%B0%83%E5%BA%A6%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90_Driver%E8%BF%9B%E7%A8%8B%E7%9A%84%E5%88%9B%E5%BB%BA.png)](http://chant00.com/media/spark_资源调度源码分析_Driver进程的创建.png)

schedule()方法有一些问题：

```
private def schedule(): Unit = {
  if (state != RecoveryState.ALIVE) { return }
  // Drivers take strict precedence over executors
  val shuffledWorkers = Random.shuffle(workers) // Randomization helps balance drivers  for (worker <- shuffledWorkers if worker.state == WorkerState.ALIVE) {
    for (driver <- waitingDrivers) {
      if (worker.memoryFree >= driver.desc.mem && worker.coresFree >= driver.desc.cores) {
        launchDriver(worker, driver)
        waitingDrivers -= driver
      }
    }
  }
  startExecutorsOnWorkers()
}
```



1) 如果是以客户端方式命令执行程序，那么不需要Master来调度Worker创建Driver进程，那么waitingDrivers这个集合中就没有元素，所以也就不需要遍历shuffledWorkers，源码并没有考虑这种情况。应该是有if语句进行非空判定。
2) 如果waitingDrivers中只有一个元素，那么也会一直遍历shuffledWorkers这个集合，实际上是不需要的。

3.Driver进程向Master发送消息：为当前的Application申请一批Executor。

下面看看Executor的创建过程：
[![spark源码分析_资源调度_Executor的启动](http://chant00.com/media/spark%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90_%E8%B5%84%E6%BA%90%E8%B0%83%E5%BA%A6_Executor%E7%9A%84%E5%90%AF%E5%8A%A8.png)](http://chant00.com/media/spark源码分析_资源调度_Executor的启动.png)

通过以上过程，Executor进程就被启动了

#### 资源调度的三个结论

1. 在默认情况下（没有使用`--executor --cores`这个选项）时，每一个Worker节点为当前的Application只启动一个Executor，这个Executor会使用这个Worker管理的所有的core(原因：`assignedCores(pos) += minCoresPerExecutor`)
2. 默认情况下，每个Executor使用1G内存
3. 如果想要在一个Worker节点启动多个Executor，需要使`--executor --cores`这个选项
4. spreadOutApps这个参数可以决定Executor的启动方式，默认轮询方式启动，这样有利于数据的本地化。

#### 验证资源调度的三个结论

集群中总共有6个core和4G内存可用，每个Worker管理3个core和2G内存
[![img](http://chant00.com/media/15052168177467.jpg)](http://chant00.com/media/15052168177467.jpg)

SPARK_HOME/bin下有一个spark-shell脚本文件，执行这个脚本文件就是提交一个application

```
function main() {
……
"${SPARK_HOME}"/bin/spark-submit --class org.apache.spark.repl.Main --name "Spark shell" "$@"
……
}
```



默认情况（不指定任何参数）下启动spark-shell：
`[root@node04 bin]# ./spark-shell --master spark://node01:7077`
[![img](http://chant00.com/media/15052168728002.jpg)](http://chant00.com/media/15052168728002.jpg)
[![img](http://chant00.com/media/15052168987010.jpg)](http://chant00.com/media/15052168987010.jpg)

这个application启动了2个Executor进程，每个Worker节点上启动一个，总共使用了6个core和2G内存，每个Work提供3个core和1G内存。

设置每个executor使用1个core
`[root@node04 bin]# ./spark-shell --master spark://node01:7077 --executor-cores 1`
[![img](http://chant00.com/media/15052169289580.jpg)](http://chant00.com/media/15052169289580.jpg)
[![img](http://chant00.com/media/15052169381307.jpg)](http://chant00.com/media/15052169381307.jpg)

那么每个worker为application启动两个executor，每个executor使用1个core，这是因为启动两个executor后，内存已经用完了，所以即使还有剩余的core可用，也无法再启动executor了

设置每个executor使用2个core
`[root@node04 bin]# ./spark-shell --master spark://node01:7077 --executor-cores 2`
[![img](http://chant00.com/media/15052170871109.jpg)](http://chant00.com/media/15052170871109.jpg)

那么每个worker为application启动1个executor，每个executor使用2个core，这是因为启动两个executor后，每个executor剩余的core为1，已经不够再启动一个exexutor了

设置每个executor使用3G内存
`[root@node04 bin]# ./spark-shell --master spark://node01:7077 --executor-memory 3G`
[![img](http://chant00.com/media/15052171146792.jpg)](http://chant00.com/media/15052171146792.jpg)
提交任务显示为waiting状态，而不是running状态，也不会启动executor

设置每个executor使用1个core，500M内存
`[root@node04 bin]# ./spark-shell --master spark://node01:7077 --executor-cores 1 --executor-memory 500M`
[![img](http://chant00.com/media/15052171461798.jpg)](http://chant00.com/media/15052171461798.jpg)
[![img](http://chant00.com/media/15052171555883.jpg)](http://chant00.com/media/15052171555883.jpg)

设置每个设置每个executor使用1个core，500M内存，集群总共可以使用3个core，集群总共启动3个executor，其中有一个Worker启动了两个executor
`[root@node04 bin]# ./spark-shell --master spark://node01:7077 --executor-cores 1 --executor-memory 500M --total-executor-cores 3`
[![img](http://chant00.com/media/15052171893498.jpg)](http://chant00.com/media/15052171893498.jpg)
[![img](http://chant00.com/media/15052171986395.jpg)](http://chant00.com/media/15052171986395.jpg)

设置每个设置每个executor使用1个core，1.2内存，集群总共可以使用3个core，集群总共启动2个executor，每个Worker启动了1个executor，表面上看起来，两个worker加起来的内存（1.6G）和剩余的core数（1），还够启动一个exexutor，但是这里需要注意的是，两个Worker的内存并不能共用，每个Worker剩余的内存（800M）并不足以启动一个executor
`[root@node04 bin]# ./spark-shell --master spark://node01:7077 --executor-cores 1 --executor-memory 1200M --total-executor-cores 3`
[![img](http://chant00.com/media/15052172249932.jpg)](http://chant00.com/media/15052172249932.jpg)

### 任务调度源码分析

源码位置：core/src/main/scala/rdd/RDD.scala
[![spark任务调度](http://chant00.com/media/spark%E4%BB%BB%E5%8A%A1%E8%B0%83%E5%BA%A6.png)](http://chant00.com/media/spark任务调度.png)

### Spark Standalone集群搭建

#### 角色划分

[![img](http://chant00.com/media/15052175596102.jpg)](http://chant00.com/media/15052175596102.jpg)

##### 1.解压安装包

```
[root@node01 chant]# tar zxf spark-1.6.0-bin-hadoop2.6.tgz
```

##### 2.编辑spark-env.sh文件

```
[root@node01 chant]# mv spark-1.6.0-bin-hadoop2.6 spark-1.6.0
[root@node01 chant]# cd spark-1.6.0/conf/
[root@node01 conf]# cp spark-env.sh.template spark-env.sh
[root@node01 conf]# vi spark-env.sh
# 绑定Master的IP
export SPARK_MASTER_IP=node01
# 提交Application的端口
export SPARK_MASTER_PORT=7077
# 每一个Worker最多可以支配core的个数，注意core是否支持超线程
export SPARK_WORKER_CORES=3
# 每一个Worker最多可以支配的内存
export SPARK_WORKER_MEMORY=2g
```

##### 3.编辑slaves文件

```
[root@node01 conf]# cp slaves.template slaves
[root@node01 conf]# vi slaves
node02
node03
```

##### 4.Spark的web端口默认为8080，与Tomcat冲突，进行修改

```
[root@node01 spark-1.6.0]# cd sbin/
[root@node01 sbin]# vi start-master.sh
if [ "$SPARK_MASTER_WEBUI_PORT" = "" ]; then
  SPARK_MASTER_WEBUI_PORT=8081
fi
```

##### 5.同步配置

```
[root@node01 conf]# cd /opt/chant/
[root@node01 chant]# scp -r spark-1.6.0 node02:`pwd`
[root@node01 chant]# scp -r spark-1.6.0 node03:`pwd`
[root@node01 chant]# scp -r spark-1.6.0 node04:`pwd`
```

##### 6.进入spark安装目录的sbin目录下，启动集群

```
[root@node01 chant]# cd spark-1.6.0/sbin/
[root@node01 sbin]# ./start-all.sh
```

##### 7.访问web界面

[![img](http://chant00.com/media/15052177677901.jpg)](http://chant00.com/media/15052177677901.jpg)

##### 8.提交Application验证集群是否工作正常

以下scala代码是spark源码包自带的例子程序，用于计算圆周率，可传入参数：

```
package org.apache.spark.examples

import scala.math.random

import org.apache.spark._

/** Computes an approximation to pi */
object SparkPi {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Spark Pi")
    val spark = new SparkContext(conf)
    val slices = if (args.length > 0) args(0).toInt else 2
// avoid overflow
    val n = math.min(100000L * slices, Int.MaxValue).toInt 
    val count = spark.parallelize(1 to n, slices).map { i =>
        val x = random * 2 - 1
        val y = random * 2 - 1
        if (x*x + y*y < 1) 1 else 0
      }.reduce((v1,v2) => {v1+v2})
    println("Pi is roughly " + 4.0 * count / n)
    spark.stop()
  }
}
```

此程序的jar包路径为：SPARK_HOME/lib/spark-examples-1.6.0-hadoop2.6.0.jar

#### Standalone模式下提交任务

Standalone模式：提交的任务在spark集群中管理，包括资源调度，计算

##### 客户端方式提交任务

进入客户端所在节点的spark安装目录的bin目录下，提交这个程序：

```
[root@node04 bin]# ./spark-submit --master spark://node01:7077 #指定 master的地址
> --deploy-mode client #指定在客户端提交任务，这个选项可以不写，默认
> --class org.apache.spark.examples.SparkPi  #指定程序的全名
> ../lib/spark-examples-1.6.0-hadoop2.6.0.jar  #指定jar包路径
> 1000  #程序运行时传入的参数
```

说明：
1.客户端提交，Driver进程就在客户端启动，进程名为SparkSubmit

```
# 注意：任务结束后，该进程就关闭了
[root@node04 ~]# jps
1646 Jps
1592 SparkSubmit
```

2.在客户端可以看到task执行情况和执行结果

```
……
17/08/04 02:55:47 INFO DAGScheduler: Job 0 finished: reduce at SparkPi.scala:36, took 6.602662 s
Pi is roughly 3.1409092
17/08/04 02:55:47 INFO SparkUI: Stopped Spark web UI at http://192.168.9.14:4040
……
```

3.适合场景：测试
原因：当提交的任务数量很多时，客户端资源不够用

##### 集群方式提交任务

还是在客户端所在节点的spark安装目录的bin目录下提交程序，只是命令需要修改：

```
[root@node04 bin]# ./spark-submit --master spark://node01:7077
> --deploy-mode cluster #指定在集群中提交任务，这个选项必须写
> --class org.apache.spark.examples.SparkPi
> ../lib/spark-examples-1.6.0-hadoop2.6.0.jar
> 1000
```

说明：
1.集群方式提交任务，Driver进程随机找一个Worker所在的节点启动，进程名为DriverWrapper

```
[root@node02 ~]# jps
1108 Worker
1529 Jps
1514 DriverWrapper
```

2.客户端看不到task执行情况和执行结果，可以在web界面查看
[![img](http://chant00.com/media/15052183410200.jpg)](http://chant00.com/media/15052183410200.jpg)

3.适合场景：生产环境
原因：当task数量很多时，集群方式可以做到负载均衡，解决多次网卡流量激增问题（分摊到集群的Worker节点上），但无法解决单次网卡流量激增问题。

#### Yarn模式下提交任务

yarn模式：把spark任务提交给yarn集群，由yarn集群进行管理，包括资源分配和计算
编辑客户端节点中spark配置文件，加入：
`export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop`,启**动hadoop集群，不需要启动spark集群**

##### 客户端方式提交任务

1.命令
`./spark-submit --master yarn --class org.apache.spark.examples.SparkPi ../lib/spark-examples-1.6.0-hadoop2.6.0.jar 100`
2.流程
[![img](http://chant00.com/media/15052186244693.jpg)](http://chant00.com/media/15052186244693.jpg)

① 在客户端执行提交命令
② 上传应用程序(jar包)及其依赖的jar包到HDFS上，开启Driver进程执行应用程序
③ 客户端会向RS发送请求，为当前的Application启动一个ApplicationMaster进程
④ RS会找一台NM启动ApplicationMaster，ApplicationMaster进程启动成功后，会向RS申请资源（图画的有误，ApplicationMaster应该在NM上）
⑤ RS接受请求后，会向资源充足的NM发送消息：在当前的节点上启动一个Executor进程，去HDFS下载spark-assembly-1.6.0-hadoop2.6.0.jar包，这个jar包中有启动Executor进程的相关类，调用其中的方法就可以启动Executor进程
⑥ Executor启动成功后，Driver开始分发task，在集群中执行任务

3.总结
Driver负责任务的调度
ApplicationMaster负责资源的申请

##### 集群方式提交任务

1.命令

```
./spark-submit --master yarn-cluster --class org.apache.spark.examples.SparkPi ../lib/spark-examples-1.6.0-hadoop2.6.0.jar 100
```

或者

```
./spark-submit --master yarn --deploy-mode cluster --class org.apache.spark.examples.SparkPi ../lib/spark-examples-1.6.0-hadoop2.6.0.jar 100
```

2.流程
[![img](http://chant00.com/media/15052189861132.jpg)](http://chant00.com/media/15052189861132.jpg)

① 在客户端执行提交命令
② 上传应用程序(jar包)及其依赖的jar包到HDFS上
③ 客户端会向RS发送请求，为当前的Application启动一个ApplicationMaster(Driver)进程，这个ApplicationMaster就是driver。
④ RS会找一台NM启动ApplicationMaster，ApplicationMaster(Driver)进程启动成功后，会向RS申请资源（图画的有误，ApplicationMaster应该在NM上），ApplicationMaster(Driver)进程启动成功后，会向RS申请资源
⑤ RS接受请求后，会向资源充足的NM发送消息：在当前的节点上启动一个Executor进程，去HDFS下载spark-assembly-1.6.0-hadoop2.6.0.jar包，这个jar包中有启动Executor进程的相关类，调用其中的方法就可以启动Executor进程
⑥ Executor启动成功后，ApplicationMaster(Driver)开始分发task，在集群中执行任务
3.总结
在cluster提交方式中，ApplicationMaster进程就是Driver进程，任务调度和资源申请都是由一个进程来做的

### Spark HA集群搭建

#### Spark高可用的原理

[![img](http://chant00.com/media/15052194149122.jpg)](http://chant00.com/media/15052194149122.jpg)

说明：
主备切换的过程中，不能提交新的Application。
已经提交的Application在执行过程中，集群进行主备切换，是没有影响的，因为spark是粗粒度的资源调度。

#### 角色划分

[![img](http://chant00.com/media/15052195976097.jpg)](http://chant00.com/media/15052195976097.jpg)

##### 1.修改spark-env.sh配置文件

```
[root@node01 ~]# cd /opt/chant/spark-1.6.0/conf/
[root@node01 conf]# vi spark-env.sh
加入以下配置
export SPARK_DAEMON_JAVA_OPTS="
-Dspark.deploy.recoveryMode=ZOOKEEPER 
-Dspark.deploy.zookeeper.url=node01:2181,node02:2181,node03:2181 
-Dspark.deploy.zookeeper.dir=/spark/ha"
```

##### 2. 同步配置文件

```
[root@node01 conf]# scp spark-env.sh node02:`pwd`
[root@node01 conf]# scp spark-env.sh node03:`pwd`
[root@node01 conf]# scp spark-env.sh node04:`pwd`
```

##### 3. 修改node02的spark配置文件

把master的IP改为node02，把node02的masterUI port改为8082
因为node01的masterUI的port设置为8081，同步后，node02的masterUI的port也为8081，那么在node02启动master进程时，日志中会有警告：
WARN Utils: Service ‘MasterUI’ could not bind on port 8081
导致我们不能通过该port访问node02的masterUI，所以修改的和node01不一样就可以

```
[root@node02 ~]# cd /opt/chant/spark-1.6.0/conf
[root@node02 conf]# vi spark-env.sh
export SPARK_MASTER_IP=node02
[root@node02 conf]# cd ../sbin
[root@node02 sbin]# vi start-master.sh
if [ "$SPARK_MASTER_WEBUI_PORT" = "" ]; then
  SPARK_MASTER_WEBUI_PORT=8082
fi
```

##### 4. 启动Zookeeper集群

```
[root@node02 ~]# zkServer.sh start
[root@node03 ~]# zkServer.sh start
[root@node04 ~]# zkServer.sh start
```

##### 5.在node01上启动Spark集群

```
[root@node01 conf]# cd ../sbin
[root@node01 sbin]# pwd
/opt/chant/spark-1.6.0/sbin
[root@node01 sbin]# ./start-all.sh
```

##### 6.在node02上启动Master进程

```
[root@node02 bin]# cd ../sbin
[root@node02 sbin]# pwd
/opt/chant/spark-1.6.0/sbin
[root@node02 sbin]# ./start-master.sh
```

##### 7.验证集群高可用

[![img](http://chant00.com/media/15052199735375.jpg)](http://chant00.com/media/15052199735375.jpg)
[![img](http://chant00.com/media/15052199860032.jpg)](http://chant00.com/media/15052199860032.jpg)

```
[root@node01 sbin]# jps
1131 Master
1205 Jps
[root@node01 sbin]# kill -9 1131
```

[![img](http://chant00.com/media/15052200135780.jpg)](http://chant00.com/media/15052200135780.jpg)
[![img](http://chant00.com/media/15052200295354.jpg)](http://chant00.com/media/15052200295354.jpg)

再次启动node01的master进程，node01成为standby
`[root@node01 sbin]# ./start-master.sh`
[![img](http://chant00.com/media/15052200673813.jpg)](http://chant00.com/media/15052200673813.jpg)

### Spark History Server配置

提交一个Application：

```
[root@node04 ~]# cd /opt/chant/spark-1.6.0/bin
[root@node04 bin]# ./spark-shell --name "testSparkShell" --master spark://node02:7077
```

[![img](http://chant00.com/media/15052209033503.jpg)](http://chant00.com/media/15052209033503.jpg)
点击ApplicationID
[![img](http://chant00.com/media/15052209140931.jpg)](http://chant00.com/media/15052209140931.jpg)
点击appName查看job信息
[![img](http://chant00.com/media/15052209386045.jpg)](http://chant00.com/media/15052209386045.jpg)

提交一个job

```
scala> sc.textFile("/tmp/wordcount_data")
.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).saveAsTextFile("/tmp/wordcount_result")
```

[![img](http://chant00.com/media/15052210085405.jpg)](http://chant00.com/media/15052210085405.jpg)
点击job查看stage信息
[![img](http://chant00.com/media/15052210190366.jpg)](http://chant00.com/media/15052210190366.jpg)
点击stage查看task信息
[![img](http://chant00.com/media/15052210621811.jpg)](http://chant00.com/media/15052210621811.jpg)

退出Spark-shell后，这个Application的信息是不被保存的，需要做一些配置才会保存历史记录，有两种方法设置保存历史记录
1.提交命令时指定
`./spark-shell --master spark://node02:7077 --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp/spark/historyLog"`
注意：保存历史数据的目录需要先创建好
2.启动history-server
修改conf/spark-defaults.conf文件

```
spark.eventLog.enabled true
spark.eventLog.dir hdfs://node01:9000/spark/historyLog
spark.history.fs.logDirectory hdfs://node01:9000/spark/historyLog
```

spark.eventLog.compress true 可以设置保存历史日志时进行压缩
注意：保存历史数据的目录需要先创建好
然后启动history server：sbin/start-history-server.sh
之后提交的所有的Application的执行记录都会被保存，访问18080端口就可以查看

### [Spark Shuffle](https://tech.meituan.com/spark-tuning-pro.html)


#### SparkShuffle详解

先了解一些角色：

##### MapOutputTracker：管理磁盘小文件的地址

- 主：MapOutputTrackerMaster
- 从：MapOutputTrackerWorker

##### BlockManager：

###### 主：BlockManagerMaster，存在于Driver端

管理范围：RDD的缓存数据、广播变量、shuffle过程产生的磁盘小文件
包含4个重要对象：

1. ConnectionManager：负责连接其他的BlockManagerSlave
2. BlockTransferService：负责数据传输
3. DiskStore：负责磁盘管理
4. Memstore：负责内存管理

###### 从：BlockManagerSlave，存在于Executor端

包含4个重要对象：

1. ConnectionManager：负责连接其他的BlockManagerSlave
2. BlockTransferService：负责数据传输
3. DiskStore：负责磁盘管理
4. Memstore：负责内存管理

[![img](http://chant00.com/media/15052834285291.jpg)](http://chant00.com/media/15052834285291.jpg)

### Spark内存管理

spark1.5之前默认为静态内存管理，之后默认为统一的内存管理，如果对数据比较了解，那么选用静态内存管理可调控的参数多，若想使用静态内存管理，将`spark.memory.useLegacyMode`从默认值`false`改为`true`即可。
这里阐述的是spark1.6版本的内存管理机制，想了解更前卫的版本，请戳[spark2.1内存管理机制](https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-apache-spark-memory-management/index.html?ca=drs-&utm_source=tuicool&utm_medium=referral)

#### 静态内存管理

[![img](http://chant00.com/media/15053969470635.jpg)](http://chant00.com/media/15053969470635.jpg)

###### [Unrolling](https://issues.apache.org/jira/secure/attachment/12765646/unified-memory-management-spark-10000.pdf)

> The memory used for unrolling is borrowed from the storage space. If there are no existing blocks, unrolling can use all of the storage space. Otherwise, unrolling can drop up to M bytes worth of blocks from memory, where M is a fraction of the storage space configurable through `spark.storage.unrollFraction`(default0.2). Note that this sub­region is not staticallyreserved, but dynamically allocated by dropping existing blocks.

所以这里的unrollFraction内存其实是个上限，不是静态固定的0.2，而是动态分配的。

关于Unrolling的详细解读，请戳[RDD缓存的过程](http://www.jianshu.com/p/58288b862030)

RDD在缓存到存储内存之后，Partition被转换成Block，Record在堆内或堆外存储内存中占用一块连续的空间。将Partition由不连续的存储空间转换为连续存储空间的过程，Spark称之为“展开”（Unroll）。Block有序列化和非序列化两种存储格式，具体以哪种方式取决于该RDD的存储级别。非序列化的Block以一种`DeserializedMemoryEntry`的数据结构定义，用一个数组存储所有的Java对象，序列化的Block则以`SerializedMemoryEntry`的数据结构定义，用字节缓冲区（ByteBuffer）来存储二进制数据。每个Executor的Storage模块用一个链式Map结构（`LinkedHashMap`）来管理堆内和堆外存储内存中所有的Block对象的实例[6]，对这个`LinkedHashMap`新增和删除间接记录了内存的申请和释放。

###### Reduce OOM怎么办？

1. 减少每次拉取的数据量
2. 提高shuffle聚合的内存比例
3. 增加executor的内存

#### 统一内存管理

[![img](http://chant00.com/media/15052969860133.jpg)](http://chant00.com/media/15052969860133.jpg)

统一内存管理中互相借用（申请，检查，借用，归还）这一环节会产生额外的计算开销。
其中最重要的优化在于动态占用机制，其规则如下：

- 设定基本的存储内存和执行内存区域（spark.storage.storageFraction 参数），该设定确定了双方各自拥有的空间的范围
- 双方的空间都不足时，则存储到硬盘；若己方空间不足而对方空余时，可借用对方的空间;（存储空间不足是指不足以放下一个完整的 Block）
- 执行内存的空间被对方占用后，可让对方将占用的部分转存到硬盘，然后”归还”借用的空间

[![img](http://chant00.com/media/15053921543814.jpg)](http://chant00.com/media/15053921543814.jpg)
凭借统一内存管理机制，Spark 在一定程度上提高了堆内和堆外内存资源的利用率，降低了开发者维护 Spark 内存的难度，但并不意味着开发者可以高枕无忧。譬如，所以如果存储内存的空间太大或者说缓存的数据过多，反而会导致频繁的全量垃圾回收，降低任务执行时的性能，因为缓存的 RDD 数据通常都是长期驻留内存的 [5] 。所以要想充分发挥 Spark 的性能，需要开发者进一步了解存储内存和执行内存各自的管理方式和实现原理。

### Spark SQL

#### 简介

Spark SQL的前身是shark，Shark是基于Spark计算框架之上且兼容Hive语法的SQL执行引擎，由于底层的计算采用了Spark，性能比MapReduce的Hive普遍快2倍以上，当数据全部load在内存的话，将快10倍以上，因此Shark可以作为交互式查询应用服务来使用。除了基于Spark的特性外，Shark是完全兼容Hive的语法，表结构以及UDF函数等，已有的HiveSql可以直接进行迁移至Shark上。Shark底层依赖于Hive的解析器，查询优化器，但正是由于Shark的整体设计架构对Hive的依赖性太强，难以支持其长远发展，比如不能和Spark的其他组件进行很好的集成，无法满足Spark的一栈式解决大数据处理的需求
Hive是Shark的前身，Shark是SparkSQL的前身，相对于Shark，SparkSQL有什么优势呢？

- SparkSQL产生的根本原因，是因为它完全脱离了Hive的限制
- SparkSQL支持查询原生的RDD，这点就极为关键了。RDD是Spark平台的核心概念，是Spark能够高效的处理大数据的各种场景的基础
- 能够在Scala中写SQL语句。支持简单的SQL语法检查，能够在Scala中写Hive语句访问Hive数据，并将结果取回作为RDD使用

##### Spark和Hive有两种组合

Hive on Spark类似于Shark，相对过时，现在公司一般都采用Spark on Hive。

###### Spark on Hive

Hive只是作为了存储的角色
SparkSQL作为计算的角色

###### Hive on Spark

Hive承担了一部分计算（解析SQL，优化SQL…）的和存储
Spark作为了执行引擎的角色

#### Dataframe

##### 简介

Spark SQL是Spark的核心组件之一，于2014年4月随Spark 1.0版一同面世，在Spark 1.3当中，Spark SQL终于从alpha(内测版本)阶段毕业。Spark 1.3更加完整的表达了Spark SQL的愿景：让开发者用更精简的代码处理尽量少的数据，同时让Spark SQL自动优化执行过程，以达到降低开发成本，提升数据分析执行效率的目的。与RDD类似，DataFrame也是一个分布式数据容器。然而DataFrame更像传统数据库的二维表格，除了数据以外，还掌握数据的结构信息，即schema。同时，与Hive类似，DataFrame也支持嵌套数据类型（struct、array和map）。从API易用性的角度上看，DataFrame API提供的是一套高层的关系操作，比函数式的RDD API要更加友好，门槛更低。

##### RDD VS DataFrame

`DataFrame = SchemaRDD = RDD<ROW>`
[![img](http://chant00.com/media/15052983161313.jpg)](http://chant00.com/media/15052983161313.jpg)
从图虫颜色来区分，DataFrame是列式存储。当要取Age这一列时，RDD必须先取出person再取Age，而DataFrame可以直接取Age这一列。

##### DataFrame底层架构

[![img](http://chant00.com/media/15052983913084.jpg)](http://chant00.com/media/15052983913084.jpg)

#### Predicate Pushdown谓词下推机制

执行如下SQL语句：

```
SELECT table1.name,table2.score
FROM table1 JOIN table2 ON (table1.id=table2.id)
WHERE table1.age>25 AND table2.score>90
```

我们比较一下普通SQL执行流程和Spark SQL的执行流程
[![img](http://chant00.com/media/15052992976209.jpg)](http://chant00.com/media/15052992976209.jpg)

#### DataFrame创建方式

##### 1.读JSON文件(不能嵌套)

```
/**
 * people.json
 * {"name":"Michael"}
   {"name":"Andy", "age":30}
   {"name":"Justin", "age":19}
 */
object DataFrameOpsFromFile {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf() 
    conf.setAppName("SparkSQL")
    conf.setMaster("local")
    val sc = new SparkContext(conf)
val sqlContext = new SQLContext(sc)
//val df = sqlContext.read.format("json").load("people.json")    
val df = sqlContext.read.json("people.json")
    
    //将DF注册成一张临时表，这张表是逻辑上的，数据并不会落地
    //people是临时表的表名，后面的SQL直接FROM这个表名
    df.registerTempTable("people")
    //打印DataFrame的结构
df.printSchema()
/*
     * 结果：nullable=true代表该字段可以为空
     * root
       |-- age: long (nullable = true)
       |-- name: string (nullable = true)
     */
      //查看DataFrame中的数据, df.show(int n)可以指定显示多少条数据
     df.show()
     /*
      * 结果：
      * +----+-------+
        | age|   name|
        +----+-------+
        |null|Michael|
        |  30|   Andy|
        |  19| Justin|
        +----+-------+
      */

      //SELECT name from table
df.select("name").show()
    
//SELECT name,age+10 from table
df.select(df("name"), df("age").plus(10)).show()
    
//SELECT * FROM table WHERE age > 10
df.filter(df("age")>10).show()
    
//SELECT count(*) FROM table GROUP BY age
df.groupBy("age").count.show()

sqlContext.sql("select * from people where age > 20").show()
  }
}
```

##### 2.JSON格式的RDD转为DataFrame

```
public class DataFrameOpsFromJsonRdd {
     public static void main(String[] args) {
         SparkConf conf = new SparkConf()
.setAppName("DataFrameFromJsonRdd").setMaster("local");
         JavaSparkContext sc = new JavaSparkContext(conf);
         //若想使用SparkSQL必须创建SQLContext,必须是传入SparkContext,不能是SparkConf
         SQLContext sqlContext = new SQLContext(sc);
         
         //创建一个本地的集合,类型String,集合中元素的格式为json格式       
         List<String> nameList = Arrays.asList(
                            "{'name':'Tom', 'age':20}",
                            "{'name':'Jed', 'age':30}",
                            "{'name':'Tony', 'age':22}",
                            "{'name':'Jack', 'age':24}");
         List<String> scoreList = Arrays.asList(
                  "{'name':'Tom','score':100}",
                  "{'name':'Jed','score':99}" );
         
         JavaRDD<String> nameRDD = sc.parallelize(nameList);
         JavaRDD<String> scoreRDD = sc.parallelize(scoreList);
         
         DataFrame nameDF = sqlContext.read().json(nameRDD);
         DataFrame scoreDF = sqlContext.read().json(scoreRDD);
         
         /**
          * SELECT nameTable.name,nameTable.age,scoreTable.score
      FROM nameTable JOIN nameTable ON (nameTable.name = scoreTable.name)
          */
         nameDF.join(
scoreDF, nameDF.col("name").$eq$eq$eq(scoreDF.col("name"))
).select(
nameDF.col("name"),nameDF.col("age"),scoreDF.col("score"))
.show();         
         
          nameDF.registerTempTable("name");
         scoreDF.registerTempTable("score");
         String sql = "SELECT name.name,name.age,score.score "
                  + "FROM name join score ON (name.name = score.name)";
         
         sqlContext.sql(sql).show();
         /*
          * +----+---+-----+
            |name|age|score|
            +----+---+-----+
            | Tom| 20|  100|
            | Jed| 30|   99|
            +----+---+-----+
          */
     }
}
```

##### 3.非JSON格式的RDD转为DataFrame

###### 1.反射的方式

Person类

```
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

object RDD2DataFrameByReflectionScala {

  /**
    *  * 使用反射的方式将RDD转换成为DataFrame
    *  * 1.自定义的类必须是public
    *  * 2.自定义的类必须是可序列化的
    *  * 3.RDD转成DataFrame的时候，会根据自定义类中的字段名进行排序
    *  *所以这里直接用case class类
    *  * Peoples.txt内容：
    *      1,Tom,7
    *      2,Tony,11
    *      3,Jack,5
    *  * @author root
    *  */
  case class Person(name: String, age: Int)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf() //创建sparkConf对象
    conf.setAppName("My First Spark App") //设置应用程序的名称，在程序运行的监控页面可以看到名称
    conf.setMaster("local")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    //传入进去Person.class的时候，sqlContext是通过反射的方式创建DataFrame
    //在底层通过反射的方式或得Person的所有field，结合RDD本身，就生成了DataFrame
    val people = sc.textFile("/Users/Chant/Documents/大数据0627/14spark/code/SparkJavaOperator/Peoples.txt")
      .map(_.split(",")).map(p => Person(p(1), p(2).trim.toInt)).toDF()


    people.registerTempTable("people")


    val teenagers = sqlContext.sql("SELECT name, age FROM people WHERE age >= 6 AND age <= 19")

    /**
      * 对dataFrame使用map算子后，返回类型是RDD<Row>
      */
    //    teenagers.map(t => "Name: " + t(0)).foreach(println)
    //    teenagers.map(t => "Name: " + t.get(0)).foreach(println)
    // 不推荐此方法，因为RDD转成DataFrame的时候，他会根据自定义类中的字段名(按字典序)进行排序。
    // 如果列很多，那你就得自己去按字典序排序列名，然后才知道该列对应的索引位。

    // or by field name: 推荐用列名来获取列值
    //    teenagers.map(t => "Name: " + t.getAs[String]("name")).foreach(println)
    //    teenagers.map(t => t.getAs("name")).foreach(println)//这样会报错java.lang.ClassCastException: java.lang.String cannot be cast to scala.runtime.Nothing$
    teenagers.map(t => t.getAs[String]("name")).foreach(println)
  }
}
```

###### 2.动态创建Schema

```
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Chant.
  */
object RDD2DataFrameByProgrammatically {
  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("RDD2DataFrameByProgrammatically"))
    val sQLContext = new SQLContext(sc)
//将数据封装到Row中，RDD[Row]就可以转成DataFrame
    val RowRDD = sc.textFile("/Users/Chant/Documents/大数据0627/14spark/code/SparkScalaOperator/Peoples.txt")
      .map(_.split(",")).map(x => Row(x(0).toInt, x(1), x(2).toInt))

    /**
      * 读取配置文件的方式
      */
    val schemaString = "id:Int name:String age:Int"
    val schema = StructType(schemaString.split(" ")
      .map(x => StructField(x.split(":")(0), if (x.split(":")(1) == "String") StringType else IntegerType, true)))
    //true代表该字段是否可以为空
    //构建StructType，用于最后DataFrame元数据的描述
    //基于已有的MetaData以及RDD<Row> 来构造DataFrame
    
//    //最后一定要写else，不像java可以只有个if。
//    val schema = StructType(schemaString.split(" ")
//      .map(x =>{
//        val pair = x.split(":")
//        if (pair(1) == "String") StructField(pair(0), StringType,true)
//        else if(pair(1) == "Int") StructField(pair(0), IntegerType,true)
//        else  StructField(pair(0), IntegerType,true)
//      }))

    /**
      * 如果列的数据类型比较多，可以用match case
      */
//    val schemaString = "id:Int name:String age:Int"
//
//    val schema = StructType(schemaString.split(" ")
//      .map(x => {
//        val pair = x.split(":")
////        var dataType = null.asInstanceOf[DataType]//巧用这一招
//        var dataType : DataType = null
//        pair(1) match {
//          case "String" => dataType = StringType
//          case "Int"    => dataType = IntegerType
//          case "Double" => dataType = DoubleType
//          case "Long"   => dataType = LongType
//          case _        => println("default, can't match")
//        }
//
//        StructField(pair(0),dataType,true)
//      }))

    /**
      * 傻瓜式
      */
    //    val structField = Array(StructField("id", IntegerType, true), StructField("name", StringType, true), StructField("age", IntegerType, true))
    //    //    val schema = StructType.apply(structField)
    //    val schema = StructType(structField)

    val df = sQLContext.createDataFrame(RowRDD, schema)
    df.printSchema()
    df.show()

    df.registerTempTable("people")
    val res = sQLContext.sql("select * from people where age > 6")
    res.show()
    res.map(x => "Name: " + x.getAs[String]("name") + "\t Age: " + x.getAs[Int]("age")).foreach(println)
  }
}
```

##### 4. 读取MySQL中的数据来创建DataFrame

```
package com.chant.sql.jdbc

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Chant.
  */
object JDBCDataSource2 {

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext(new SparkConf().setAppName("JDBCDataSource2").setMaster("local"))
    val sqlContext = new SQLContext(sc)

    val reader = sqlContext.read.format("jdbc")
    reader.option("url", "jdbc:mysql://node01:3306/testdb")
    reader.option("driver", "com.mysql.jdbc.Driver")
    reader.option("user", "root")
    reader.option("password", "123456")
    reader.option("dbtable", "student_info")

    val stuInfoDF = reader.load()
    reader.option("dbtable","student_score")
    val stuScoreDF = reader.load()
//    分别将mysql中两张表的数据加载并注册为DataFrame
    stuInfoDF.registerTempTable("stuInfos")
    stuScoreDF.registerTempTable("stuScores")

    val sql = "select stuInfos.name, age, score from stuInfos join stuScores " +
      "on (stuInfos.name = stuScores.name) where stuScores.score > 80"
    //执行sql，join两个DF
    val resDF = sqlContext.sql(sql)
    resDF.show()

//    将join后的数据写入的数据库
    resDF.rdd.foreachPartition(p =>{
      Class.forName("com.mysql.jdbc.Driver")
      var conn : Connection = null//这样写，在finnaly的时候才能访问到并将其关闭
      var ps : PreparedStatement = null
      val sql2 = "insert into good_student_info values(?,?,?)"

      try{
        conn = DriverManager.getConnection("jdbc:mysql://node01:3306/testdb", "root", "123456")//获取数据库链接
        conn.setAutoCommit(false)//关闭自动提交
        ps = conn.prepareStatement(sql2)//准备sql
        //迭代添加数据，并添加带batch
        p.foreach(row =>{
          ps.setString(1, row.getAs[String]("name"))
          ps.setInt(2, row.getAs[Int]("age"))
          ps.setInt(3, row.getAs[Int]("score"))
          ps.addBatch()
        })
        //执行并提交
        ps.executeBatch()
        conn.commit()//貌似数据量少的时候，不提交也会有数据写入数据库？？？尚存疑问。但是肯定要写
      }catch{
        case e:Exception => e.printStackTrace()
      }finally {
        if(ps != null) ps.close()
        if(conn != null) conn.close()
      }
    })
    sc.stop()
  }
}
```

##### 5. 读取Hive中的数据创建一个DataFrame（Spark on Hive）

Spark与Hive整合：
1) 编辑spark客户端的配置文件hive-site.xml

```
node04：vi /opt/chant/spark-1.6.0/conf/hive-site.xml
<configuration>
	<property>
  		<name>hive.metastore.uris</name>
    		<value>thrift://node04:9083</value>
      		<description>
Thrift uri for the remote metastore. Used by metastore client to connect to remote metastore.
</description>
      </property>
</configuration>
```

2) 把hadoop的core-site.xml和hdfs-site.xml copy到SPARK_HOME/conf/下
3) node0{1,2,3}：zkServer.sh start
4) node01：start-dfs.sh
5) node01：service mysqld start
6) node04：hive –service metastore
7) node01：/opt/chant/spark-1.6.0/sbin/start-all.sh
8) node02：/opt/chant/spark-1.6.0/sbin/start-master.sh
9) node04：/opt/chant/spark-1.6.0/bin/spark-submit
–master spark://node01:7077,node02:7077
–class com.bjchant.java.spark.sql.hive.HiveDataSource
../TestHiveContext.jar
jar包中的测试代码如下：

```
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext

object HiveDataSource {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("HiveDataSource");
    val sc = new SparkContext(conf);
    val hiveContext = new HiveContext(sc);
    hiveContext.sql("DROP TABLE IF EXISTS student_infos");
    hiveContext.sql("CREATE TABLE IF NOT EXISTS student_infos (name STRING, age INT) row format delimited fields terminated by '\t'");
    hiveContext.sql("LOAD DATA "
      + "LOCAL INPATH '/root/resource/student_infos' "
      + "INTO TABLE student_infos");

    hiveContext.sql("DROP TABLE IF EXISTS student_scores");
    hiveContext.sql("CREATE TABLE IF NOT EXISTS student_scores (name STRING, score INT) row format delimited fields terminated by '\t'");
    hiveContext.sql("LOAD DATA "
      + "LOCAL INPATH '/root/resource/student_scores' "
      + "INTO TABLE student_scores");

    val goodStudentsDF = hiveContext.sql("SELECT si.name, si.age, ss.score "
      + "FROM student_infos si "
      + "JOIN student_scores ss ON si.name=ss.name "
      + "WHERE ss.score>=80");

    hiveContext.sql("DROP TABLE IF EXISTS good_student_infos");
//    goodStudentsDF.saveAsTable("good_student_infos");
    hiveContext.sql("USE result")
    
    //将goodStudentsDF里面的值写入到Hive表中，如果表不存在，会自动创建然后将数据插入到表中
    goodStudentsDF.write.saveAsTable("good_student_infos")

  }
}
```

#### DataFrame数据存储

1. 存储到hive表中
   把hive表读取为dataFrame
   `dataFrame = hiveContext().table("table_name");`
   把dataFrame转为hive表存储到hive中，若table不存在，自动创建
   `dataFrame.write().saveAsTable("table_name");`

2.存储到MySQL/HBase/Redis…中

```
dataFrame.javaRDD().foreachPartition(new VoidFunction<Row>() {
	……
})
```

3.存储到parquet文件(压缩比大，节省空间)中

```
DataFrame usersDF = 
sqlContext.read().format("parquet").load("hdfs://node01:9000/input/users.parquet");
usersDF.registerTempTable("users");
DataFrame resultDF = sqlContext.sql("SELECT * FROM users WHERE name = 'Tom'");
resultDF.write().format("parquet ").mode(SaveMode.Ignore)
.save("hdfs://node01:9000/output/result. parquet ");
resultDF.write().format("json").mode(SaveMode. Overwrite)
.save("hdfs://node01:9000/output/result.json");

public enum SaveMode {
  Append, //如果文件已经存在，追加
  Overwrite, //如果文件已经存在，覆盖
  ErrorIfExists, //如果文件已经存在，报错
  Ignore//如果文件已经存在，不对原文件进行任何修改，即不存储DataFrame
}
```

**parquet数据源会自动推断分区**，类似hive里面的分区表的概念
文件存储的目录结构如下：

```
/users
|/country=US
	|data：id,name
|/country=ZH
	|data：id,name
```

当执行以下代码

```
DataFrame usersDF = sqlContext.read().parquet(
				"hdfs://node01:9000/users"); //路径只写到users
usersDF.printSchema();
/*
*(id int, name string, country string)
*/
usersDF.show();
usersDF.registerTempTable("table1");
sqlContext.sql("SELECT count(0) FROM table1 WHERE country = 'ZH'").show();
//执行这个sql只需要去country=ZH文件夹下去遍历即可
```

#### 自定义函数

##### UDF

```
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType

object UDF {
  
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
        .setMaster("local") 
        .setAppName("UDF")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
  
    val names = Array("yarn", "Marry", "Jack", "Tom") 
    
    
    val namesRDD = sc.parallelize(names, 4) 
    
    
    val namesRowRDD = namesRDD.map { name => Row(name) }
    
    
    val structType = StructType(Array(StructField("name", StringType, true)))  
    
    val namesDF = sqlContext.createDataFrame(namesRowRDD, structType) 
    
    // 注册一张names表
    namesDF.registerTempTable("names")  
    
    sqlContext.udf.register("strLen", (str: String) => str.length()) 
  
    // 使用自定义函数
    sqlContext.sql("select name,strLen(name) from names").show
  }
}
```

##### UDAF：实现对某个字段进行count

```
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.expressions.MutableAggregationBuffer
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.IntegerType

class StringCount extends UserDefinedAggregateFunction {

  //输入数据的类型
  def inputSchema: StructType = {
    StructType(Array(StructField("12321", StringType, true)))
  }

  // 聚合操作时，所处理的数据的类型
  def bufferSchema: StructType = {
    StructType(Array(StructField("count", IntegerType, true)))
  }


  def deterministic: Boolean = {
    true
  }

  // 为每个分组的数据执行初始化值
  def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0
  }

  /**
    * update可以认为是，一个一个地将组内的字段值传递进来实现拼接的逻辑
    * buffer.getInt(0)获取的是上一次聚合后的值
    * 相当于map端的combiner,combiner就是对每一个map task的处理结果进行一次小聚合
    * 大聚和发生在reduce端
    */
  //每个组，有新的值进来的时候，进行分组对应的聚合值的计算
  def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    buffer(0) = buffer.getAs[Int](0) + 1
  }

  /**
    * 合并 update操作,可能是针对一个分组内的部分数据,在某个节点上发生的
    * 但是可能一个分组内的数据，会分布在多个节点上处理
    * 此时就要用merge操作，将各个节点上分布式拼接好的串，合并起来
    * buffer1.getInt(0) : 大聚和的时候上一次聚合后的值      
    * buffer2.getInt(0) : 这次计算传入进来的update的结果
    */

  // 最后merger的时候，在各个节点上的聚合值，要进行merge，也就是合并
  def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getAs[Int](0) + buffer2.getAs[Int](0)
  }

  // 最终函数返回值的类型
  def dataType: DataType = {
    IntegerType
  }

  // 最后返回一个最终的聚合值     要和dataType的类型一一对应
  def evaluate(buffer: Row): Any = {
    buffer.getAs[Int](0)
  }
}
```

#### 开窗函数

row_number()开窗函数的作用：
按照我们每一个分组的数据，按其照顺序，打上一个分组内的行号
id=2016 [111,112,113]
那么对这个分组的每一行使用row_number()开窗函数后，三行数据会一次得到一个组内的行号
id=2016 [111 1,112 2,113 3]
[![img](http://chant00.com/media/15053155944357.jpg)](http://chant00.com/media/15053155944357.jpg)

### SparkStreaming

#### Strom VS SparkStreaming

1. Storm是一个纯实时的流式处理框，SparkStreaming是一个准实时的流式处理框架，（微批处理：可以设置时间间隔）
2. SparkStreaming的吞吐量比Storm高（因为它是微批处理）
3. Storm的事务机制要比SparkStreaming好（每个数据只处理一次）
4. Storm支持动态资源调度（ 可以在数据的低谷期使用更少的资源，而spark的粗粒度资源调度默认是不支持这样的）
5. SparkStreaming的应用程序中可以写SQL语句来处理数据，可以和spark core及sparkSQL无缝结合，所以SparkingStreaming擅长复杂的业务处理，而Storm不擅长复杂的业务处理，它擅长简单的汇总型计算（天猫双十一销量）

#### SparkStreaming执行流程

[![img](http://chant00.com/media/15053158002878.jpg)](http://chant00.com/media/15053158002878.jpg)

总结：
receiver task是7*24h一直在执行，一直接收数据，将接收到的数据保存到batch中，假设batch interval为5s，那么把接收到的数据每隔5s切割到一个batch，因为batch是没有分布式计算的特性的，而RDD有，所以把batch封装到RDD中，又把RDD封装到DStream中进行计算，在第5s的时候，计算前5s的数据，假设计算5s的数据只需要3s，那么第5-8s一边计算任务，一边接收数据，第9-11s只是接收数据，然后在第10s的时候，循环上面的操作。

如果job执行时间大于batch interval，那么未执行的数据会越攒越多，最终导致Spark集群崩溃。
测试：
1.开启scoket server
`[root@node01 ~]# nc -lk 9999`
2.启动spark集群

```
[root@node01 ~]# /opt/chant/spark-1.6.0/sbin /start-all.sh
[root@node02 ~]# /opt/chant/spark-1.6.0/sbin /start-master.sh
```

3.运行测试程序

```
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Durations
import org.apache.spark.storage.StorageLevel

/**
 * 1.local的模拟线程数必须大于等于2,因为一条线程被receiver(接受数据的线程)占用，另外一个线程是job执行
 * 2.Durations时间的设置，就是我们能接受的延迟度，这个我们需要根据集群的资源情况以及监控，要考虑每一个job的执行时间
 * 3.创建StreamingContext有两种方式 (sparkconf、sparkcontext)
 * 4.业务逻辑完成后，需要有一个output operator
 * 5.StreamingContext.start(),straming框架启动之后是不能在次添加业务逻辑
 * 6.StreamingContext.stop()无参的stop方法会将sparkContext一同关闭，如果只想关闭StreamingContext,在stop()方法内传入参数false
 * 7.StreamingContext.stop()停止之后是不能在调用start  
 */

object WordCountOnline {
  def main(args: Array[String]): Unit = {
    
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[2]")
    sparkConf.setAppName("WordCountOnline")

    //在创建streaminContext的时候设置batch Interval
    val ssc = new StreamingContext(sparkConf,Durations.seconds(5))
    
    val linesDStream = ssc.socketTextStream("node01", 9999, StorageLevel.MEMORY_AND_DISK)
    
//    val wordsDStream = linesDStream.flatMap { _.split(" ") }
    val wordsDStream = linesDStream.flatMap(_.split(" "))

    val pairDStream = wordsDStream.map { (_,1) }
    
    val resultDStream = pairDStream.reduceByKey(_+_)
    
    resultDStream.print()
     //outputoperator类的算子 
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }
}
```

结果：
在server 端输入数据，例如，hello world，控制台实时打印wordwount结果：(hello,1)(world,1)

#### Output Operations on DStreams

##### foreachRDD(func)

```
dstream.foreachRDD { rdd =>
  rdd.foreachPartition { partitionOfRecords =>
    // ConnectionPool is a static, lazily initialized pool of connections
    val connection = ConnectionPool.getConnection()
    partitionOfRecords.foreach(record => connection.send(record))
ConnectionPool.returnConnection(connection)  
// return to the pool for future reuse
  }
}
```

##### saveAsTextFiles(prefix, [suffix])

Save this DStream’s contents as text files.

##### saveAsObjectFiles(prefix, [suffix])

Save this DStream’s contents as SequenceFiles of serialized Java objects.

##### saveAsHadoopFiles(prefix, [suffix])

Save this DStream’s contents as Hadoop files.

#### Transformations on Dstreams

##### transform(func)

Return a new DStream by applying a RDD-to-RDD function to every RDD of the source DStream. This can be used to do arbitrary RDD operations on the DStream.

```
val spamInfoRDD = ssc.sparkContext.newAPIHadoopRDD(...) 
// RDD containing spam information
val cleanedDStream = wordCounts.transform(rdd => {
  rdd.join(spamInfoRDD).filter(...) 
// join data stream with spam information to do data cleaning
  ...
})
```

##### updateStateByKey(func)

Return a new “state” DStream where the state for each key is updated by applying the given function on the previous state of the key and the new values for the key. This can be used to maintain arbitrary state data for each key.
UpdateStateByKey的主要功能:
1．Spark Streaming中为每一个Key维护一份state状态，state类型可以是任意类型的，可以是一个自定义的对象，那么更新函数也可以是自定义的。
2．通过更新函数对该key的状态不断更新，对于每个新的batch而言，Spark Streaming会在使用updateStateByKey的时候为已经存在的key进行state的状态更新

```
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Durations, StreamingContext}
import spire.std.option
/** wordcount,实时计算结果，与之前的worCountOnline不同，它是增量计算的。
  *
  * UpdateStateByKey的主要功能:
  * 1、Spark Streaming中为每一个Key维护一份state状态，state类型可以是任意类型的的， 可以是一个自定义的对象，那么更新函数也可以是自定义的。
  * 2、通过更新函数对该key的状态不断更新，对于每个新的batch而言，Spark Streaming会在使用updateStateByKey的时候为已经存在的key进行state的状态更新
  * 第6s的计算结果1-5s
  * hello,3
  * world,2
  *
  * 第11s的时候 6-11秒
  * 接收数据
  * hello 1
  * hello 1
  * world 1
  *计算逻辑
  * hello 3+1+1
  * world 2+1
  *
  * 第16s 11-15s
  * 接收数据
  * hello 1
  * hello 1
  * world 1
  * 计算逻辑
  * hello 5+1+1
  * world 3+1
  * 如果要不断的更新每个key的state，就一定涉及到了状态的保存和容错，这个时候就需要开启checkpoint机制和功能
  *
  * 全面的广告点击分析
  *
  *         有何用？   统计广告点击流量，统计这一天的车流量，统计。。。。点击量
  */
object UpdateStateByKeyOperator {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("UpdateStateByKeyOperator")
    val streamContext = new StreamingContext(conf, Durations.seconds(5))

    /**
    * 上一次的计算结果会保存两份：
      * 1.内存
      * 2.我们设置的checkPoint目录下
      * 因为内存不稳定，放在checkPoint目录下更安全。
      * 多久会将内存中的数据（每一个key所对应的状态）写入到磁盘上一份呢？
      * 	如果你的batch interval小于10s  那么10s会将内存中的数据写入到磁盘一份
      * 	如果bacth interval 大于10s，那么就以bacth interval为准
      */
    streamContext.checkpoint("hdfs://node01:8020/sscheckpoint01")

    val lines = streamContext.socketTextStream("node01",8888)
    val wordsPair = lines.flatMap(_.split(" ")).map((_,1))
//    val wordsPair = streamContext.socketTextStream("node01",8888).flatMap(_.split(" ")).map((_,1))

    //注意这里的各种泛型
    val counts = wordsPair.updateStateByKey[Int]((values:Seq[Int], state:Option[Int]) =>{
      var updateValue = 0
      if(!state.isEmpty) updateValue = state.get

      values.foreach(x =>{updateValue += x})
//      Option.apply[Int](updateValue)
      Some(updateValue)
    })

    counts.print()

    streamContext.start()
    streamContext.awaitTermination()
    streamContext.stop()
  }
}
```

##### Window Operations

[![img](http://chant00.com/media/15053179353512.jpg)](http://chant00.com/media/15053179353512.jpg)

总结：
batch interval：5s
每隔5s切割一次batch封装成DStream
window length：15s
进行计算的DStream中包含15s的数据,这里也就是3个batch。
sliding interval：10s
每隔10s取3个batch封装的DStream，封装成一个更大的DStream进行计算
window length和sliding interval必须是batch interval的整数倍
问题：
time3的RDD被计算两次

```
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Durations, StreamingContext}

object WindowOperator {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WindowOperator").setMaster("local[2]")
    val streamingContext = new StreamingContext(conf, Durations.seconds(5))
    //优化版必须设置cehckpoint目录，因为内存不稳定，保证数据不丢失。
    streamingContext.checkpoint("hdfs://node01:8020/sscheckpoint02")

    val logDStrem = streamingContext.socketTextStream("node01", 8888)

    val wordsPair = logDStrem.flatMap(_.split(" ")).map((_, 1))
    /** batch interval:5s
      * sliding interval:10s
      * window length：60s
      * 所以每隔10s会取12个rdd,在计算的时候会将这12个rdd聚合起来
      * 然后一起执行reduceByKeyAndWindow操作
      * reduceByKeyAndWindow是针对窗口操作的而不是针对DStream操作的
      */
//    val wordCountDStrem = wordsPair.reduceByKeyAndWindow((a, b) => a + b, Durations.seconds(60))
    //为什么这里一定要声明参数类型？？？什么时候需要声明，什么时候不用，什么时候必须不声明？
//    val wordCountDStrem = wordsPair.reduceByKeyAndWindow((a: Int, b: Int) => a+b, Durations.seconds(60), Durations.seconds(10))

    //优化版
    val wordCountDStrem = wordsPair.reduceByKeyAndWindow((a, b) => a+b, (a, b) => a-b, Durations.minutes(1), Durations.seconds(10))
    wordCountDStrem.print()

    streamingContext.start()
    streamingContext.awaitTermination()
    streamingContext.stop()
  }
}
```

优化：
[![img](http://chant00.com/media/15053180739268.jpg)](http://chant00.com/media/15053180739268.jpg)

假设batch=1s，window length=5s，sliding interval=1s，那么每个DStream重复计算了5次，优化后，(t+4)时刻的Window由(t+3)时刻的Window和(t+4)时刻的DStream组成，由于(t+3)时刻的Window包含(t-1)时刻的DStream，而(t+4)时刻的Window中不需要包含(t-1)时刻的DStream，所以还需要减去(t-1)时刻的DStream，所以：
`Window(t+4) = Window(t+3) + DStream(t+4) - DStream(t-1)`

优化后的代码：

```
//优化版必须要设置checkPoint目录
    val wordCountDStrem = wordsPair.reduceByKeyAndWindow((a, b) => a+b, (a, b) => a-b, Durations.minutes(1), Durations.seconds(10))
```

**NOTE:** updateStateByKey和优化版的reduceByKeyAndWindow都必须要设置checkPoint目录。

#### Driver HA

提交任务时设置
`spark-submit –supervise`
Spark standalone or Mesos with cluster deploy mode only:
–supervise If given, restarts the driver on failure.
以集群方式提交到yarn上时，Driver挂掉会自动重启，不需要任何设置
提交任务，在客户端启动Driver，那么不管是提交到standalone还是yarn，Driver挂掉后都无法重启
代码中配置
上面的方式重新启动的Driver需要重新读取application的信息然后进行任务调度，实际需求是，新启动的Driver可以直接恢复到上一个Driver的状态（可以直接读取上一个StreamingContext的DSstream操作逻辑和job执行进度，所以需要把上一个StreamingContext的元数据保存到HDFS上），直接进行任务调度，这就需要在代码层面进行配置。

```
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Durations, StreamingContext}
/**  Spark standalone or Mesos 1with cluster deploy mode only:
  *  在提交application的时候  添加 --supervise 选项  如果Driver挂掉 会自动启动一个Driver
  *  	SparkStreaming
  */
object SparkStreamingOnHDFS2 {
  def main(args: Array[String]): Unit = {
    val checkpointPath = "hdfs://node01:8020/sscheckpoint03"

//    val ssc = new StreamingContext(conf, Durations.seconds(5))

    val ssc = StreamingContext.getOrCreate(checkpointPath,() => {
      println("Creating new context")
      //这里可以设置一个线程，因为不需要一个专门接收数据的线程，而是监控一个目录
      val conf = new SparkConf().setAppName("SparkStreamingOnHDFS").setMaster("local[1]")
      //每隔15s查看一下监控的目录中是否新增了文件
      val ssc  = new StreamingContext(conf, Durations.seconds(15))
      ssc.checkpoint(checkpointPath)
      /** 只是监控文件夹下新增的文件，减少的文件是监控不到的  文件内容有改动也是监控不到 */
      ssc
    })
    val wordCount = ssc.textFileStream("hdfs://node01:8020/hdfs/").flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_)
    wordCount.print()

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }
}
```

执行一次程序后，JavaStreamingContext会在checkpointDirectory中保存，当修改了业务逻辑后，再次运行程序，JavaStreamingContext.getOrCreate(checkpointDirectory, factory);
因为checkpointDirectory中有这个application的JavaStreamingContext，所以不会调用JavaStreamingContextFactory来创建JavaStreamingContext，而是直接checkpointDirectory中的JavaStreamingContext，所以即使业务逻辑改变了，执行的效果也是之前的业务逻辑，如果需要执行修改过的业务逻辑，可以修改或删除checkpointDirectory。

### Kafka

简介
kafka是一个高吞吐的分部式消息系统
[![img](http://chant00.com/media/15053191033367.jpg)](http://chant00.com/media/15053191033367.jpg)
使用kafka和SparkStreaming组合的好处：

1. 解耦，SparkStreaming不用关心数据源是什么，只需要消费数据即可
2. 缓冲，数据提交给kafka消息队列，SparkStreaming按固定时间和顺序处理数据，减轻数据量过大造成的负载
3. 异步通信，kafka把请求队列提交给服务端，服务端可以把响应消息也提交给kafka的消息队列中，互不影响

#### 消息系统特点

1. 生产者消费者模式

2. 可靠性

   自己不丢数据

   当消费者消费一条数据后，这条数据还会保存在kafka中，在一周（默认）后再删除

   消费者不丢数据 

   消费者消费数据的策略，“至少一次”，即消费者至少处理这条数据一次，“严格一次”，即消费者必须且只能消费这条数据一次

#### Kafka架构

[![img](http://chant00.com/media/15053668820845.jpg)](http://chant00.com/media/15053668820845.jpg)

图里还少了个zookeeper，用于存储元数据和消费偏移量（根据偏移量来保证消费至少一次和严格消费一次）
producer：消息生产者
consumer：消息消费者
broker：kafka集群的每一台节点叫做broker，负责处理消息读、写请求，存储消息
topic：消息队列/分类
kafka里面的消息是有topic来组织的，简单的我们可以想象为一个队列，一个队列就是一个topic，然后它把每个topic又分为很多个partition，这个是为了做并行的，在每个partition里面是有序的，相当于有序的队列，其中每个消息都有个序号，比如0到12，从前面读往后面写。一个partition对应一个broker，一个broker可以管多个partition，比如说，topic有6个partition，有两个broker，那每个broker就管理3个partition。这个partition可以很简单想象为一个文件，当数据发过来的时候它就往这个partition上面append，追加就行，kafka和很多消息系统不一样，很多消息系统是消费完了我就把它删掉，而kafka是根据时间策略删除，而不是消费完就删除，在kafka里面没有消费完这个概念，只有过期这个概念

一个topic分成多个partition，每个partition内部消息强有序，其中的每个消息都有一个序号叫offset，一个partition只对应一个broker，一个broker可以管多个partition，消息直接写入文件，并不是存储在内存中，根据时间策略（默认一周）删除，而不是消费完就删除，producer自己决定往哪个partition写消息，可以是轮询的负载均衡，或者是基于hash的partition策略，而这样容易造成数据倾斜。所以**建议使用轮询的负载均衡**。
[![img](http://chant00.com/media/15053673731094.jpg)](http://chant00.com/media/15053673731094.jpg)

consumer自己维护消费到哪个offset，每个consumer都有对应的group，group内部是queue消费模型，各个consumer消费不同的partition，一个消息在group内只消费一次，各个group各自独立消费，互不影响
**partition内部是FIFO的，partition之间不是FIFO的**，当然我们可以把topic设为一个partition，这样就是严格的FIFO（First Input First Output，先入先出队列）

#### kafka特点

- 高性能：单节点支持上千个客户端，百MB/s吞吐

- 持久性：消息直接持久化在普通磁盘上，性能好，直接写到磁盘里面去，就是直接append到磁盘里面去，这样的好处是直接持久话，数据不会丢，第二个好处是顺序写，然后消费数据也是顺序的读，所以持久化的同时还能保证顺序读写

- 分布式：数据副本冗余、流量负载均衡、可扩展

  分布式，数据副本，也就是同一份数据可以到不同的broker上面去，也就是当一份数据，磁盘坏掉的时候，数据不会丢失，比如3个副本，就是在3个机器磁盘都坏掉的情况下数据才会丢。

- 灵活性：消息长时间持久化+Client维护消费状态

  消费方式非常灵活，第一原因是消息持久化时间跨度比较长，一天或者一星期等，第二消费状态自己维护消费到哪个地方了，可以自定义消费偏移量

#### kafka与其他消息队列对比

- RabbitMQ:分布式，支持多种MQ协议，重量级

- ActiveMQ：与RabbitMQ类似

- ZeroMQ：以库的形式提供，使用复杂，无持久化

- redis：单机、纯内存性好，持久化较差

  本身是一个内存的KV系统，但是它也有队列的一些数据结构，能够实现一些消息队列的功能，当然它在单机纯内存的情况下，性能会比较好，持久化做的稍差，当持久化的时候性能下降的会比较厉害

- kafka：分布式，较长时间持久化，高性能，轻量灵活

  天生是分布式的，不需要你在上层做分布式的工作，另外有较长时间持久化，在长时间持久化下性能还比较高，顺序读和顺序写，还通过sendFile这样0拷贝的技术直接从文件拷贝到网络，减少内存的拷贝，还有批量读批量写来提高网络读取文件的性能

#### 零拷贝

[![img](http://chant00.com/media/15053704805816.jpg)](http://chant00.com/media/15053704805816.jpg)
“零拷贝”是指计算机操作的过程中，CPU不需要为数据在内存之间的拷贝消耗资源。而它通常是指计算机在网络上发送文件时，不需要将文件内容拷贝到用户空间（User Space）而直接在内核空间（Kernel Space）中传输到网络的方式。
[![img](http://chant00.com/media/15053705012302.jpg)](http://chant00.com/media/15053705012302.jpg)

#### Kafka集群搭建

node01,node02,node03

##### 1. 解压

```
[root@node01 chant]# tar zxvf kafka_2.10-0.8.2.2.tgz
```

##### 2. 修改server.properties配置文件

```
[root@node01 chant]# cd kafka_2.10-0.8.2.2/config
[root@node01 config]# vi server.properties
broker.id=0  #node01为0，node02为1，node03为2
log.dirs=/var/kafka/logs #真实数据存储路径
auto.leader.rebalance.enable=true  #leader均衡机制开启
zookeeper.connect=node02:2181,node03:2181,node04:2181 #zookeeper集群
```

##### 3. 同步配置，记得修改每台机器的broker.id

##### 4. 启动zookeeper集群

##### 5. 在每台kafka节点上启动kafka集群

```
nohup /opt/chant/kafka_2.10-0.8.2.2/bin/kafka-server-start.sh /opt/chant/kafka_2.10-0.8.2.2/config/server.properties &
```

##### 6. 测试

在node01上
创建topic：

```
/opt/chant/kafka_2.10-0.8.2.2/bin/kafka-topics.sh 
--create 
--zookeeper node02:2181,node03:2181,node04:2181 
--replication-factor 3 
--partitions 3 
--topic test_create_topic
```

生产数据：

```
/opt/chant/kafka_2.10-0.8.2.2/bin/kafka-console-producer.sh 
--broker-list node01:9092,node02:9092,node03:9092 
--topic test_create_topic
```

在node02上启动消费者

```
/opt/chant/kafka_2.10-0.8.2.2/bin/kafka-console-consumer.sh 
--zookeeper node02:2181,node03:2181,node04:2181 
--from-beginning 
--topic test_create_topic
```

在node01输入消息，在node02会接收并打印

查看在集群中有哪些topic：

```
/opt/chant/kafka_2.10-0.8.2.2/bin/kafka-topics.sh 
--list 
--zookeeper node02:2181,node03:2181,node04:2181
```

结果：test_create_topic

查看某个topic信息：

```
/opt/chant/kafka_2.10-0.8.2.2/bin/kafka-topics.sh 
--describe 
--zookeeper node02:2181,node03:2181,node04:2181 --topic test_create_topic

结果：
Topic:test_create_topic	PartitionCount:3	ReplicationFactor:3	Configs:
	Topic: test_create_topic	Partition: 0	Leader: 0	Replicas: 0,1,2	Isr: 0,1,2
	Topic: test_create_topic	Partition: 1	Leader: 1	Replicas: 1,2,0	Isr: 1,2,0
	Topic: test_create_topic	Partition: 2	Leader: 2	Replicas: 2,0,1	Isr: 2,0,1
```

解释一下leader均衡机制(auto.leader.rebalance.enable=true)：
每个partition是有主备结构的，当partition 1的leader，就是broker.id = 1的节点挂掉后，那么leader 0 或leader 2成为partition 1 的leader，那么leader 0 或leader 2 会管理两个partition的读写，性能会下降，当leader 1 重新启动后，如果开启了leader均衡机制，那么leader 1会重新成为partition 1 的leader，降低leader 0 或leader 2 的负载

#### Kafka和SparkStreaming整合

##### Receiver方式

###### 原理：

[![SparkStreaming和Kafka整合原理](http://chant00.com/media/SparkStreaming%E5%92%8CKafka%E6%95%B4%E5%90%88%E5%8E%9F%E7%90%86.png)](http://chant00.com/media/SparkStreaming和Kafka整合原理.png)

###### 获取kafka传递的数据来计算：

```
SparkConf conf = new SparkConf()
    .setAppName("SparkStreamingOnKafkaReceiver")
    .setMaster("local[2]")
    .set("spark.streaming.receiver.writeAheadLog.enable","true");      
JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(5));
//设置持久化数据的目录
jsc.checkpoint("hdfs://node01:8020/spark/checkpoint");
Map<String, Integer> topicConsumerConcurrency = new HashMap<String, Integer>();
//topic名    receiver task数量
topicConsumerConcurrency.put("test_create_topic", 1);
JavaPairReceiverInputDStream<String,String> lines = KafkaUtils.createStream(
    jsc,
    "node02:2181,node03:2181,node04:2181",
    "MyFirstConsumerGroup",
    topicConsumerConcurrency,
    StorageLevel.MEMORY_AND_DISK_SER());
/*
 * 第一个参数是StreamingContext
 * 第二个参数是ZooKeeper集群信息（接受Kafka数据的时候会从Zookeeper中获得Offset等元数据信息）
 * 第三个参数是Consumer Group
 * 第四个参数是消费的Topic以及并发读取Topic中Partition的线程数
 * 第五个参数是持久化数据的级别，可以自定义
 */
//对lines进行其他操作……
```

**注意**

1. 需要spark-examples-1.6.0-hadoop2.6.0.jar
2. kafka_2.10-0.8.2.2.jar和kafka-clients-0.8.2.2.jar版本号要一致

###### kafka客户端生产数据的代码：

```
public class SparkStreamingDataManuallyProducerForKafka extends Thread {
    private String topic; //发送给Kafka的数据的类别
    private Producer<Integer, String> producerForKafka;
    public SparkStreamingDataManuallyProducerForKafka(String topic){
        this.topic = topic;
        Properties conf = new Properties();
        conf.put("metadata.broker.list",
"node01:9092,node02:9092,node03:9092");
        conf.put("serializer.class",  StringEncoder.class.getName());
        producerForKafka = new Producer<Integer, String>(
            new ProducerConfig(conf)) ;
      }

    @Override
    public void run() {
        while(true){
             counter ++;
             String userLog = createUserLog();
//生产数据这个方法可以根据实际需求自己编写
             producerForKafka.send(new KeyedMessage<Integer, String>(topic, userLog));
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
        }
  }

    public static void main(String[] args) {
new SparkStreamingDataManuallyProducerForKafka(
"test_create_topic").start();
//test_create_topic是topic名 
    }
}
```

##### Direct方式

把kafka当作一个存储系统，直接从kafka中读数据，SparkStreaming自己维护消费者的消费偏移量,不再将其存储到zookeeper。
与Receiver方式相比，他有的优点是：

1. one-to-one,直接读取卡夫卡中数据，Partion数与kafka一致。

2. Efficiency,不开启WAL也不会丢失数据，因为事实上kafka的的数据保留时间只要设定合适，可以直接从kafka恢复。

3. Exactly-once semantics,其实这只保证kafka与spark是一致的，之后写数据的时候还需要自己注意才能保证整个事务的一致性。而在receiver模式下，offset交由kafka管理，而kafaka实际交由zookeeper管理，可能出现数据不一致。

   ```
   SparkConf conf = new SparkConf()
            .setAppName("SparkStreamingOnKafkaDirected")
            .setMaster("local[1]");
            
   JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(10));
            
   Map<String, String> kafkaParameters = new HashMap<String, String>();
   kafkaParameters.put("metadata.broker.list", "node01:9092,node02:9092,node03:9092");
            
   HashSet<String> topics = new HashSet<String>();
   topics.add("test_create_topic");
   JavaPairInputDStream<String,String> lines = KafkaUtils.createDirectStream(jsc,
           String.class,
           String.class,
           StringDecoder.class,
           StringDecoder.class,
           kafkaParameters,
           topics);
   //对lines进行其他操作……
   ```

##### 两种方式下提高SparkStreaming并行度的方法

###### Receiver方式调整SparkStreaming的并行度的方法：

- `spark.streaming.blockInterval`

  假设batch interval为5s，Receiver Task会每隔200ms(spark.streaming.blockInterval默认)将接收来的数据封装到一个block中，那么每个batch中包括25个block，batch会被封装到RDD中，所以RDD中会包含25个partition，所以提高接收数据时的并行度的方法是：调低`spark.streaming.blockInterval`的值，建议不低于50ms
  其他配置：

- `spark.streaming.backpressure.enabled` 

  默认false，设置为true后，sparkstreaming会根据上一个batch的接收数据的情况来动态的调整本次接收数据的速度，但是最大速度不能超过`spark.streaming.receiver.maxRate`设置的值（设置为n，那么速率不能超过n/s）

- `spark.streaming.receiver.writeAheadLog.enable` 默认false 是否开启WAL机制

###### Direct方式并行度的设置：

第一个DStream的分区数是由读取的topic的分区数决定的，可以通过增加topic的partition数来提高SparkStreaming的并行度

### 参考资料

1. [Spark Shuffle原理、Shuffle操作问题解决和参数调优](http://www.cnblogs.com/arachis/p/Spark_Shuffle.html)
2. [Spark性能优化指南——高级篇](https://tech.meituan.com/spark-tuning-pro.html)
3. [Spark性能优化指南——基础篇](https://tech.meituan.com/spark-tuning-basic.html)
4. [Unified Memory Management in Spark 1.6](https://issues.apache.org/jira/secure/attachment/12765646/unified-memory-management-spark-10000.pdf)
5. [Garbage Collection Tuning](http://spark.apache.org/docs/latest/tuning.html#garbage-collection-tuning)
6. [spark2.1内存管理机制](https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-apache-spark-memory-management/index.html?ca=drs-&utm_source=tuicool&utm_medium=referral)
7. [Spark内存管理详解（下）——内存管理](http://www.jianshu.com/p/58288b862030)