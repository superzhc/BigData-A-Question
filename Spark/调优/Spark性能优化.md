# Spark 性能优化

按调优效果排序

1. 分配资源、并行度、RDD 架构与缓存
2. shuffle 调优
3. spark 算子调优
4. JVM 调优、广播大变量等

Spark 的性能优化，主要手段包括：

1. 对 RDD 使用高性能序列化类库
2. 优化数据结构
3. 对多次使用的RDD进行持久化 / Checkpoint
4. 使用序列化的持久化级别
5. Java 虚拟机垃圾回收调优
6. 提高并行度
7. 广播共享数据
8. 数据本地化
9. reduceByKey 和 groupByKey 的合理使用
10. Shuffle调优
11. 算子调优

## 资源分配

整个 spark 性能调优的基础：先确保**资源充足**，提高并行度和内存

```bash
/usr/local/spark/bin/spark-submit \
--class xxxx \
--num-executors 3 \  #配置executor的数量
--driver-memory 100m \  #配置driver的内存（影响不大）
--executor-memory 100m \  #配置每个executor的内存大小
--executor-cores 3 \  #配置每个executor的cpu core数量
/usr/local/xxx.jar \
```

资源设置原则：将 Executor 相关参数设置成可使用资源最大值，executor数量，cpu和内存

executor和并行度关系:`并行执行task数量 = executor数量 * 每个executor cpu core`

提高executor内存作用:

1. RDD cache不写入磁盘,减少磁盘IO
2. shuffle操作的reduce端,需要内存存放拉取的数据并聚合,可以减少IO
3. task执行,降低JVM GC

## 检测spark程序内存消耗

1. 设置RDD的并行度
   - 在 `parallelize()`、`textFile()` 等方法中，传入第二个参数，设置 RDD 的 task/partition 的数量；
   - 用 `SparkConf.set()` 方法，设置 `spark.default.parallelism`，可以统一设置这个 Application 所有 RDD 的 partition 数量
2. 在程序中将 RDD cache 到内存中:`RDD.cache()`
3. 观察 Driver 的 log：`“BlockManagerInfo: Added rdd_6_0 in memory on 192.168.0.101:12907 (size: 16.6 KB, free: 1443.6 MB)”` 的日志信息。这就显示了每个 partition 占用了多少内存
4. 将这个内存信息乘以 partition 数量，即可得出 RDD 大致的内存占用量。

##  对RDD使用高性能序列化类库

Spark提供了两种序列化机制

1. Java序列化机制(默认)
    Java序列化机制的速度比较慢，而且序列化后的数据占用的内存空间比较大
2. Kryo序列化机制
    Kryo序列化机制比Java序列化机制更快，而且序列化后的数据占用的空间更小，通常比Java序列化的数据占用的空间要小10倍
    Kryo序列化机制之所以不是默认序列化机制的原因是，有些类型虽然实现了Seriralizable接口，但是它也不一定能够进行序列化(不兼容)；此外，如果要得到最佳的性能，Kryo还要求在Spark应用程序中，对所有需要序列化的类型都进行注册(麻烦)

### 使用Kryo序列化机制

使用 Kryo 时，需要序列化的类，是要预先进行注册的，以获得最佳性能，如果不注册的话，那么 Kryo 必须时刻保存类型的全限定名，反而占用内存。Spark 默认是对 Scala 中常用的类型自动注册了 Kryo 的，都在 AllScalaRegistry 类中。除此之外需要注册

```scala
SparkConf sc =  sparkSession.sparkContext.getConf
//将Spark的序列化器设置为KryoSerializer
sc.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
sc.registerKryoClasses(Array(classOf[类名]))
```

序列化作用对象

1. 算子函数中使用到的外部变量(broadcast)
2. 持久化 RDD 时进行序列化`StorageLevel.MEMORY_ONLY_SER`
3. shuffle 操作

### 优化点

1. 优化缓存大小
    若要序列化的对象过大，Kryo 内部的缓存可能不够存放那么大的 class 对象，此时可以设置 kryo 缓存大小 `sc.set("spark.kryoserializer.buffer.mb", "20")` 设置成 20m,默认是 2m
2. 预先注册自定义类型
    `sc.registerKryoClasses(Array(classOf[类名]))`:避免全限定类名

### 应用场景

如算子函数使用到了外部的大对象的情况，使用 java 序列化可能会慢,占内存

## 优化数据结构

优化算子函数内部用到的数据

1. 尽量使用数组
    `int[]` 优于 `List<Integer>`:在大量数据的情况下,可以节省对象类型的占用空间,包括引用地址等
2. 原始数据类型优于类
    `Map<String,Student>`可以转换成一个长串字符:`id:name,class|id:name,class|...`,这样计算量会增大,但内存占用量减少
3. 避免使用多层嵌套对象结构
   对象引用也会占用大量空间
   ```java
   public class Teacher { 
     private List<Student> students = new ArrayList<Student>(); 
   }
   ```
   可以改成json字符串格式
   ```json
   {"teacherId": 1, "teacherName": "leo", students:[{"studentId": 1, "studentName": "tom"},{"studentId":2, "studentName":"marry"}]}
   ```
4. int 优于 string
    int比string占用更少内存
5. 指针压缩
    如果RAM 小于32 GB，设置 `-XX:+UseCompressedOops`使指针为4个字节而不是8个字节

## 对多次使用的RDD进行持久化 / CheckPoint

复用时直接从 BlockManager 中拿持久化的数据，拿不到就读取 CheckPoint 数据，失败就重新计算

如果持久化数据可能丢失，第一次计算 RDD 时可以 CheckPoint 数据(CheckPoint 数据更加不易丢失)

避免不同 Action 重复计算 RDD 方式

1. RDD 复用:将不同功能的 RDD 抽取为一个 RDD
2. RDD 持久化:公用 RDD 持久化到内存/磁盘
3. 持久化:内存序列化 > 内存+磁盘(无序列化) > 内存+磁盘(序列化)

> 内存资源非常充足时:持久化时采用双副本机制,避免宕机丢失副本

```scala
//按优先顺序排列
persist(StorageLevel.MEMORY_ONLY())
StorageLevel.MEMORY_ONLY_SER()
StorageLevel.MEMORY_AND_DISK()
StorageLevel.MEMORY_AND_DISK_SER()
StorageLevel.DISK_ONLY()
```

## 使用序列化的持久化级别

如果内存大小不是特别充足，完全可以使用序列化的持久化级别，比如`MEMORY_ONLY_SER、MEMORY_AND_DISK_SER`等。使用`RDD.persist(StorageLevel.MEMORY_ONLY_SER)`这样的语法即可

对RDD持久化序列化后，**RDD的每个partition的数据，都是序列化为一个巨大的字节数组**

## JVM调优

spark-submit脚本中设置GC信息

```bash
--conf "spark.executor.extraJavaOptions=-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
```

### GC调优

Spark 中 GC 调优的目的是确保只有长寿命的 RDD 存储在 Old 版本中，并且 Young 版本的大小足够存储短命期的对象

1. full GC 次数过多：增大JVM内存
2. minor GC 多，major GC 少：为 Eden 分配更多的内存 `-Xmn=xxx`
3. OldGen 近满：减少用于缓存的内存量 `spark.memory.fraction` 缓存较少的对象比减慢任务执行更好；也可以减少 YoungGen 的大小 `-Xmn=xxx`；可以改变 NewRatio 比例
4. 使用 G1GC 回收器 `-XX:+UseG1GC`，当 executor 堆大小很大时，提高 G1 region 大小是很有用的 `-XX:G1HeapRegionSize`

### 优化 Executor 堆内存比例

spark中堆内存划分成了两部分

1. 储存：给RDD的cache、persist操作进行RDD数据缓存用；
2. 执行：给spark算子函数的运行使用的，存放函数中创建的对象

可以web ui界面上查询到stage中task的运行时间、gc时间,如果发现gc太频繁，时间太长,就可以适当调整堆内存分布比例,降低cache内存占比(可以将一部分缓存RDD写入磁盘/序列化),此时算子可用内存就增加了

默认情况下，Spark使用每个executor 60%的内存空间来缓存RDD，而task执行期间创建的对象，只有40%的内存空间来存放
 若task执行的40%的内存空间不够，就会触发Java虚拟机的垃圾回收操作 `SparkConf().set("spark.storage.memoryFraction", "0.4")`:设置RDD缓存空间40%,task空间60%
 当RDD缓存空间降低,可以持久化RDD
 task空间又分为老年代和新生代,这部分空间和jvm调优一致`--conf "spark.executor.extraJavaOptions=-verbose:gc -Xmn=500M"`

> `spark.memory.fraction`:expresses the fraction of the (JVM heap space - 300MB) (default 0.6). The rest of the space (40%) is reserved for user data structures, internal metadata in Spark, and safeguarding against OOM errors in the case of sparse and unusually large records.

> 看官网的解释,应该是
>  执行+储存=JVM size*spark.memory.fraction
>  储存=(执行+储存)*spark.memory.storageFraction

### Executor 堆外内存

场景：当 Spark 作业处理的数据量特别大，运行时不时的报错 `shuffle output file not found；resubmitting task；executor lost,task lost，out of memory` （内存溢出）等错误,最后 Spark 作业彻底崩溃。

可能原因:executor的堆外内存不足，导致executor内存溢出(挂掉)；当后续的stage的task要从一些executor中去拉取shuffle map output文件，但由于executor已经挂掉了，关联的block manager消失,导致出错

![executor堆外内存不足](images/16185314afd36d04)

当blockManager挂掉,task无法通过blockManager获取数据,报错shuffle output file not found,而DAGScheduler会重复提交stage,TaskScheduler重复提交task(resubmitting task),最后作业失败

默认时executor堆外内存大小为300多M,容易OOM

```bash
# 在spark-submit中设置,不在代码中设置
# 针对yarn模式,设置为2G
--conf spark.yarn.executor.memoryOverhead=2048
```

### executor连接等待时长

executor，优先从自己本地关联的BlockManager中获取某份数据,若本地block manager没有,则会通过TransferService去远程连接其他节点上executor的block manager去获取

当远程executor的block manager正在进行JVM GC操作,则获取不到响应,当超过默认的60s未响应时,就宣告失败

当fileid xxxx not found,file lost时,可能就是远程数据拉取失败导致,重复失败就可能导致DAGScheduler会重复提交stage,TaskScheduler重复提交task(resubmitting task)

可以将等待连接的时长设置长一点

```bash
# 在spark-submit中设置,不在代码中设置
--conf spark.core.connection.ack.wait.timeout=100
```

## 提高并行度

并行度：可以同时执行的 task 数量(stage 的 task 数量,不过实际并行执行时受 cpu core 数量限制，`实际并行执行= min(executor数量*executor cpu core,task 数量)`) 推荐设置task数量(并行度) 为总 cpu core 数量2~3倍

`sparkContext.set("spark.default.parallelism", "5")`：将 RDD 分成 5 份 partition，一个 partition 由一个 task 计算，所有的算子操作只有 5 个 task 去运行，此时在 spark-submit 脚本设置的 executor 不合理，就会浪费资源

shuffle 操作时，前一个 stage 中的每个 task 会将数据写入多个 file 中，默认是下一个 stage task 数量和每个 task 的 file 数量对应，一个 task 处理上一个 task 中的一个 file，由于上一个 stage 有多个 task，所以是一个 task 处理多个 file；相同的 key 必存在同一个 file 中

![task-file-task](images/161d27fc0c74a71b)

> 对于上图，比如在 reduceByKey 时，每个 stage0 task 都会将 key=a 数据放到对应 file 中，则会有三个 file 有该数据，在stage1读取时，这三个file必为某一个task读取，不会被多个 task 同时读取

### 设置方式

1. `SparkSession.builder().config("spark.default.parallelism", 500)`:统一设置所有RDD
2. `sparkContext.textFile()` `sparkContext.parallelize()`:单个RDD并行度设置
3. spark自动设置,会自动按文件输入源数量设置并行度
    如HDFS,会给每个InputSplit创建一个partition;
    对于reduceByKey等会发生shuffle的操作,则会将并行度设置成最大的父RDD的并行度

### Spark SQL并行度不生效

Spark SQL会根据hive表对应的hdfs文件的block，自动设置Spark SQL查询所在的stage的并行度,该stage并行度自定义设置失效,但其他stage并行度设置有效

可以将Spark SQL查询出来的RDD，使用repartition算子去重分区,可以得到多个partition,避免了和Spark SQL绑定在一个stage中的算子，只能使用少量的task去处理大量数据以及复杂的算法逻辑

## 广播共享数据

让共享数据存在在节点上,而不是每个task都存一份 `sparkContext.broadcast(object)`

若不广播变量,则复制变量到每个task中.广播变量后变成每个executor中有一个副本(不是每个worker都有一个副本)

每个executor对应一个blockManager,blockManager负责管理某个executor对应的内存和磁盘上的数据

> BlockManager 是管理整个Spark运行时的数据读写的，当然也包含数据存储本身，在这个基础之上进行读写操作，由于 Spark 本身是分布式的，所以 BlockManager 也是分布式的 task数据读写都是通过blockManager完成

task读取广播变量值时,先到本地blockManager中寻找,若blockManager中找不到,则由blockManager寻找到其他blockManager或driver中寻找,并保存到本地blockManager中

## 数据本地化

本地化级别：

1. PROCESS_LOCAL：数据和计算它的代码在同一个JVM进程中,即在同一个executor中——task由executor执行,数据存储在executor对应的blockManager中。
2. NODE_LOCAL：跨进程,数据和计算它的代码在一个节点上，但是不在一个进程中，比如在不同的executor进程中，或者是数据在HDFS文件的block中。
3. NO_PREF：数据从哪里过来，性能都是一样的。
4. RACK_LOCAL：数据和计算它的代码在一个机架上,数据需要在网络间传输。
5. ANY：数据可能在任意地方，比如其他网络环境内，或者其他机架上,性能最差。

可以设置参数，spark.locality系列参数，来调节Spark等待task可以进行数据本地化的时间 默认都是3s

```
spark.locality.wait:本地化
spark.locality.wait.process:进程
spark.locality.wait.node:结点
spark.locality.wait.rack:机架
```

`SparkConf.set("spark.locality.wait","10")`:设置成10s
 TaskSchedulerImpl会优先用最好的本地化级别去启动task,即会优先在包含了要处理的partition的executor中启动task,若executor已经满负载执行task,没有空闲资源执行新的task,则会按照设置的等待时间等待,超时则放大一个级别去尝试

### 调节时机

日志中会显示数据本地化级别,PROCESS_LOCAL,NODE_LOCAL etc,如果大多数task是PROCESS_LOCAL级别,可以不用调节 若大多数级别不是PROCESS_LOCAL,则可以调节本地化等待时长

调节完后再次查看日志,需要确认数据本地化级别提升,**spark作业时间缩短**

## reduceByKey和groupByKey的合理使用

```scala
val counts = pairs.reduceByKey(_ + _)
val counts = pairs.groupByKey().map(wordCounts => (wordCounts._1, wordCounts._2.sum))
```

![reduceByKey :一个workcount例子](images/161d27a1e300095b)

如果能用reduceByKey，那就用reduceByKey，因为它会在map端，**先进行本地combine**，可以大大减少要传输到reduce端的数据量，**减小网络传输**的开销。
 只有在reduceByKey处理不了时，才用`groupByKey().map()`来替代。
 groupByKey不会进行本地聚合,而是将shuffleMapTask输出拉取到ResultTask内存中,导致大量的数据传输

> aggregateByKey和reduceByKey原理一样

## Shuffle调优

![Shuffle调优](images/160651d72ba34699)

> 这里的内容大部分是针对HashShuffleManager的,SortShuffleManager和HashShuffleManager类似,只是将map端一个task只生成一个文件而已,而不是一个task生成reduce端task数量的file

### shuffle manager

spark 1.2以前版本默认用HashShuffleManager,新版默认使用SortShuffleManager,还有tungsten-sort（自己实现内存管理）

```scala
SparkConf().set("spark.shuffle.manager", "hash") #hash、sort、tungsten-sort
```

#### HashShuffleManager

![task-file-task](images/161d27fc0c74a71b)

特点:map端创建大量file
 在Shuffle的过程中写数据时不做排序操作，只是将数据根据Hash的结果，将各个Reduce分区的数据写到各自的磁盘文件中

#### SortShuffleManager

![SortShuffleManager图示](images/161d2814b3af83c3)

特点:

1. SortShuffleManager会对每个reduce task要处理的数据，进行排序（默认的）。
2. SortShuffleManager会避免像HashShuffleManager那样，默认就去创建多份磁盘文件。一个task，只会写入一个磁盘文件，不同reduce task的数据，用**offset来划分界定**。

`spark.shuffle.sort.bypassMergeThreshold`
 这个参数仅适用于SortShuffleManager，SortShuffleManager在处理不需要排序的Shuffle操作时，由于排序带来性能的下降。这个参数决定了在这种情况下，当Reduce分区的数量小于多少的时候，在SortShuffleManager内部**不使用Merge Sort的方式处理数据**，而是与Hash Shuffle类似，直接将分区文件写入单独的文件
 不同的是，在**最后一步还是会将这些文件合并成一个单独的文件**。这样通过去除Sort步骤来加快处理速度，代价是需要并发打开多个文件，所以内存消耗量增加，本质上是相对HashShuffleMananger的一个折衷方案。 这个参数的默认值是200个分区，如果内存GC问题严重，可以降低这个值。

#### tungsten-sort

使用了自己实现的一套内存管理机制，性能上有很大的提升， 而且可以避免shuffle过程中产生的大量的OOM，GC，等等内存相关的异常

参见[Spark性能优化第八季之Spark Tungsten-sort Based Shuffle](http://blog.csdn.net/u011007180/article/details/52389268)

#### ShuffleManager如何选择hash、sort、tungsten-sort

1. 不需要排序
    对于不需要进行排序的Shuffle操作来说，如repartition等，如果文件数量不是特别巨大，HashShuffleManager面临的内存问题不大，而SortShuffleManager需要额外的根据Partition进行排序，显然HashShuffleManager的效率会更高
2. 需要排序
    而对于本来就需要在Map端进行排序的Shuffle操作来说，如ReduceByKey等，使用HashShuffleManager虽然在写数据时不排序，但在其它的步骤中仍然需要排序，而SortShuffleManager则可以将写数据和排序两个工作**合并在一起执行**，因此即使不考虑HashShuffleManager的内存使用问题，SortShuffleManager依旧可能更快

### consolidation机制

```scala
SparkConf.set("spark.shuffle.consolidateFiles", "true")
```

- 未开启consolidation机制的问题

  大量文件IO

  ![HashShuffleManager Shuffle过程-no consolidation](images/1606502fc9ecc0e0)

  HashShuffleManager Shuffle过程-no consolidation

**HashShuffleManager**

1. 每一个Mapper会根据Reducer的数量创建出相应的bucket，bucket的数量是M×R，其中M是Map的个数，R是Reduce的个数。
2. Mapper产生的结果会根据设置的partition算法填充到每个bucket中去。这里的partition算法是可以自定义的，当然默认的算法是根据key哈希到不同的bucket中去。
3. 当Reducer启动时，它会根据自己task的id和所依赖的Mapper的id从远端或是本地的block manager中取得相应的bucket作为Reducer的输入进行处理。

假定该job有4个Mapper和4个Reducer，有2个core，也就是能并行运行两个task。可以算出Spark的shuffle write共需要16个bucket
 在shuffle consolidation中**每一个bucket并非对应一个文件，而是对应文件中的一个segment**，同时shuffle consolidation所产生的shuffle文件数量与Spark core的个数也有关系。在上面的图例中，job的4个Mapper分为两批运行，在第一批2个Mapper运行时会申请8个bucket，产生8个shuffle文件；而在第二批Mapper运行时，申请的8个bucket并不会再产生8个新的文件，而是追加写到之前的8个文件后面，这样一共就只有8个shuffle文件，而在文件内部这有16个不同的segment

shuffleMapTask任务运行时,task并行数量是按照分配的cpu core 数量k运行的,同一时刻只有k个shuffleMapTask同时执行,每个task都会产生一批磁盘文件,但当下一批shuffleMapTask执行时,会**复用**上一批shuffleMapTask中产生的文件,将数据插入到上一批产生的文件上,并不会新建文件
 最后只会有k*m个文件(cpu core数量一般不多)

### 设置bucket缓存大小

默认情况下，shuffle的map task，输出到磁盘文件会先写入每个task自己关联的一个内存缓冲区,缓冲区大小默认是32kb。 当内存缓冲区满溢之后，才会spill溢写到磁盘文件中 `spark.shuffle.file.buffer`：map task的写磁盘缓存，默认32k,设置越大,写入到对应bucket文件次数越少

### 设置拉取缓存大小

`spark.reducer.maxSizeInFlight`:reduce task的拉取缓存，默认48m
 ReduceTask中将 MapTask拉取数据到内存中时,每次最大拉取48m(可能拉取小于48M),拉取完就聚合处理,然后再拉取
 当内存足够时,可以设置更大的拉取缓存大小,减少拉取次数

> Reduce端是多次重复向map端拉取数据,而不是等到map端都将数据输入文件才拉取

另一方面,增大拉取缓存时,可能会导致OOM:每个task拉取的数据量大,所有task拉取满额数据,加上reduce端执行的聚合函数的代码，可能会创建大量对象,最终导致OOM,此时只能设置更小的拉取值

只有当应用**资源充**足,map端数据不是非常大时,可以设置更大的拉取缓存

### 拉取失败重试设置

当blockManager挂掉/GC,task无法通过blockManager获取数据,报错shuffle output file not found
 若是blockManager挂掉,则应用会失败
 若是blockManager GC,则DAGScheduler会重复提交stage,恢复正常

`spark.shuffle.io.maxRetries`：拉取失败的最大重试次数，默认3次
 `spark.shuffle.io.retryWait`：拉取失败的重试间隔，默认5s
 默认时,若3*5=15s内没有拉取到数据,则会报shuffle output file not found

reduce task拉取数据时,可能会遇到map task executor的jvm full GC,导致工作线程暂停,拉取超时失败

### 设置reduce内存比例

`spark.shuffle.memoryFraction`：在拉取到数据之后，会用hashmap对各个key对应的values进行汇聚,该参数用于设置executor中reduce端聚合的内存比例，默认0.2，超过比例就会溢出到磁盘上.

> bucket缓存大小和reduce内存比例有相关,一端出问题,另一端也容易出问题:都是磁盘IO频繁,影响性能

### 场景

可以从spark UI 查看总体上task shuffle write和shuffle read信息,读写量大,则可以增大相关参数

```
spark.shuffle.file.buffer #可以每次增大一倍查看效果
spark.shuffle.memoryFraction #每次提高0.1查看效果
复制代码
```

### shuffle参数

| 属性名称                                | 默认值 | 属性说明                                                                                                                                                                                                                                                |
| --------------------------------------- | :----: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| spark.reducer.maxSizeInFlight           |  48m   | reduce task的buffer缓冲，代表了每个reduce task每次能够拉取的map side数据最大大小，如果内存充足，可以考虑加大大小，从而减少网络传输次数，提升性能                                                                                                        |
| spark.shuffle.blockTransferService      | netty  | shuffle过程中，传输数据的方式，两种选项，netty或nio，spark 1.2开始，默认就是netty，比较简单而且性能较高，spark 1.5开始nio就是过期的了，而且spark 1.6中会去除掉                                                                                          |
| spark.shuffle.compress                  |  true  | 是否对map side输出的文件进行压缩，默认是启用压缩的，压缩器是由spark.io.compression.codec属性指定的，默认是snappy压缩器，该压缩器强调的是压缩速度，而不是压缩率                                                                                          |
| spark.shuffle.consolidateFiles          | false  | 默认为false，如果设置为true，那么就会合并map side输出文件，对于reduce task数量特别多的情况下，可以极大减少磁盘IO开销，提升性能                                                                                                                          |
| spark.shuffle.file.buffer               |  32k   | map side task的内存buffer大小，写数据到磁盘文件之前，会先保存在缓冲中，如果内存充足，可以适当加大大小，从而减少map side磁盘IO次数，提升性能                                                                                                             |
| spark.shuffle.io.maxRetries             |   3    | 网络传输数据过程中，如果出现了网络IO异常，重试拉取数据的次数，默认是3次，对于耗时的shuffle操作，建议加大次数，以避免full gc或者网络不通常导致的数据拉取失败，进而导致task lost，增加shuffle操作的稳定性                                                 |
| spark.shuffle.io.retryWait              |   5s   | 每次重试拉取数据的等待间隔，默认是5s，建议加大时长，理由同上，保证shuffle操作的稳定性                                                                                                                                                                   |
| spark.shuffle.io.numConnectionsPerPeer  |   1    | 机器之间的可以重用的网络连接，主要用于在大型集群中减小网络连接的建立开销，如果一个集群的机器并不多，可以考虑增加这个值                                                                                                                                  |
| spark.shuffle.io.preferDirectBufs       |  true  | 启用堆外内存，可以避免shuffle过程的频繁gc，如果堆外内存非常紧张，则可以考虑关闭这个选项                                                                                                                                                                 |
| spark.shuffle.manager                   |  sort  | ShuffleManager，Spark 1.5以后，有三种可选的，hash、sort和tungsten-sort，sort-based ShuffleManager会更高效实用内存，并且避免产生大量的map side磁盘文件，从Spark 1.2开始就是默认的选项，tungsten-sort与sort类似，但是内存性能更高                         |
| spark.shuffle.memoryFraction            |  0.2   | 如果spark.shuffle.spill属性为true，那么该选项生效，代表了executor内存中，用于进行shuffle reduce side聚合的内存比例，默认是20%，如果内存充足，建议调高这个比例，给reduce聚合更多内存，避免内存不足频繁读写磁盘                                           |
| spark.shuffle.service.enabled           | false  | 启用外部shuffle服务，这个服务会安全地保存shuffle过程中，executor写的磁盘文件，因此executor即使挂掉也不要紧，必须配合spark.dynamicAllocation.enabled属性设置为true，才能生效，而且外部shuffle服务必须进行安装和启动，才能启用这个属性                    |
| spark.shuffle.service.port              |  7337  | 外部shuffle服务的端口号，具体解释同上                                                                                                                                                                                                                   |
| spark.shuffle.sort.bypassMergeThreshold |  200   | 对于sort-based ShuffleManager，如果没有进行map side聚合，而且reduce task数量少于这个值，那么就不会进行排序，如果你使用sort ShuffleManager，而且不需要排序，那么可以考虑将这个值加大，直到比你指定的所有task数量都大，以避免进行额外的sort，从而提升性能 |
| spark.shuffle.spill                     |  true  | 当reduce side的聚合内存使用量超过了spark.shuffle.memoryFraction指定的比例时，就进行磁盘的溢写操作                                                                                                                                                       |
| spark.shuffle.spill.compress            |  true  | 同上，进行磁盘溢写时，是否进行文件压缩，使用spark.io.compression.codec属性指定的压缩器，默认是snappy，速度优先                                                                                                                                          |

## 算子调优

### MapPartitions

#### Map VS MapPartitions

普通的map操作，若partition中有1万条数据,那么function要执行和计算1万次。当内存不足时,可以将已经处理的数据内存里面垃圾回收掉,避免OOM

而使用MapPartitions操作之后，一个task仅仅会执行一次function，function一次接收所有的partition数据,只要执行一次就可以了.内存不足以一次处理一个partition的数据,会OOM

#### 适用场景

数据量不大时,估算每个partition的量和每个executor的内存资源,内存是否能容纳所有的partition数据

### coalesce配合filter使用

filter操作之后

1. 每个partition数据量变少，但是但后续处理的task不变,浪费计算资源
2. partition数据量不同,出现数据倾斜,task处理数据量不均

用coalesce压缩partition数量

> local模式下，不用去设置分区和并行度的数量,local模式自己本身就是进程内模拟的集群来执行，本身性能就很高,对并行度、partition数量都有一定的内部的优化

### 使用repartitionAndSortWithinPartitions替代repartition与sort类操作

repartitionAndSortWithinPartitions是Spark官网推荐的一个算子，官方建议，如果需要在repartition重分区之后，还要进行排序，建议直接使用repartitionAndSortWithinPartitions算子。因为该算子可以一边进行重分区的shuffle操作，一边进行排序。shuffle与sort两个操作同时进行，比先shuffle再sort来说，性能可能是要高的

