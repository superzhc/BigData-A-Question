## 为何要处理数据倾斜（Data Skew）

### 什么是数据倾斜

对 Spark/Hadoop 这样的大数据系统来讲，数据量大并不可怕，可怕的是数据倾斜。

何谓数据倾斜？数据倾斜指的是，并行处理的数据集中，某一部分（如 Spark 或 Kafka 的一个 Partition）的数据显著多于其它部分，从而使得该部分的处理速度成为整个数据集处理的瓶颈。

### 数据倾斜是如何造成的

在 Spark 中，同一个 Stage 的不同 Partition 可以并行处理，而具体依赖关系的不同 Stage 之间是串行处理的。假设某个 Spark Job 分为 Stage 0 和 Stage 1 两个 Stage，且 Stage 1 依赖于 Stage 0，那 Stage 0 完全处理结束之前不会处理 Stage 1。而 Stage 0 可能包含 N 个 Task，这 N 个 Task 可以并行进行。如果其中 N-1 个 Task 都在 10 秒内完成，而另外一个 Task 却耗时 1 分钟，那该 Stage 的总时间至少为 1 分钟。换句话说，一个 Stage 所耗费的时间，主要由最慢的那个 Task 决定。

由于同一个 Stage 内的所有 Task 执行相同的计算，在排除不同计算节点计算能力差异的前提下，不同 Task 之间耗时的差异主要由该 Task 所处理的数据量决定。

Stage 的数据来源主要分为如下两类

- 从数据源直接读取。如读取 HDFS，Kafka
- 读取上一个 Stage 的 Shuffle 数据

## 如何缓解 / 消除数据倾斜

### 尽量避免数据源的数据倾斜

以 Spark Stream 通过 DirectStream 方式读取 Kafka 数据为例。由于 Kafka 的每一个 Partition 对应 Spark 的一个 Task（Partition），所以 Kafka 内相关 Topic 的各 Partition 之间数据是否平衡，直接决定 Spark 处理该数据时是否会产生数据倾斜。

如《[ Kafka 设计解析（一）- Kafka 背景及架构介绍](http://www.jasongj.com/2015/03/10/KafkaColumn1/#Producer消息路由)》一文所述，Kafka 某一 Topic 内消息在不同 Partition 之间的分布，主要由 Producer 端所使用的 Partition 实现类决定。如果使用随机 Partitioner，则每条消息会随机发送到一个 Partition 中，从而从概率上来讲，各 Partition 间的数据会达到平衡。此时源 Stage（直接读取 Kafka 数据的 Stage）不会产生数据倾斜。

但很多时候，业务场景可能会要求将具备同一特征的数据顺序消费，此时就需要将具有相同特征的数据放于同一个 Partition 中。一个典型的场景是，需要将同一个用户相关的 PV 信息置于同一个 Partition 中。此时，如果产生了数据倾斜，则需要通过其它方式处理。

### 调整并行度分散同一个 Task 的不同 Key

#### 原理

Spark 在做 Shuffle 时，默认使用 HashPartitioner（非 Hash Shuffle）对数据进行分区。如果并行度设置的不合适，可能造成大量不相同的 Key 对应的数据被分配到了同一个 Task 上，造成该 Task 所处理的数据远大于其它 Task，从而造成数据倾斜。

如果调整 Shuffle 时的并行度，使得原本被分配到同一 Task 的不同 Key 发配到不同 Task 上处理，则可降低原 Task 所需处理的数据量，从而缓解数据倾斜问题造成的短板效应。

![img](D:\superz\BigData-A-Question\Spark\images\a20aa0e91fe63c743b4e2375e758d248.png)

### 案例

现有一张测试表，名为 student_external，内有 10.5 亿条数据，每条数据有一个唯一的 id 值。现从中取出 id 取值为 9 亿到 10.5 亿的共 1.5 条数据，并通过一些处理，使得 id 为 9 亿到 9.4 亿间的所有数据对 12 取模后余数为 8（即在 Shuffle 并行度为 12 时该数据集全部被 HashPartition 分配到第 8 个 Task），其它数据集对其 id 除以 100 取整，从而使得 id 大于 9.4 亿的数据在 Shuffle 时可被均匀分配到所有 Task 中，而 id 小于 9.4 亿的数据全部分配到同一个 Task 中。处理过程如下

```sql
INSERT OVERWRITE TABLE test
SELECT CASE WHEN id < 940000000 THEN (9500000  + (CAST (RAND() * 8 AS INTEGER)) * 12 )
       ELSE CAST(id/100 AS INTEGER)
       END,
       name
FROM student_external
WHERE id BETWEEN 900000000 AND 1050000000;
```

通过上述处理，一份可能造成后续数据倾斜的测试数据即以准备好。接下来，使用 Spark 读取该测试数据，并通过`groupByKey(12)`对 id 分组处理，且 Shuffle 并行度为 12。代码如下

```scala
public class SparkDataSkew {
  public static void main(String[] args) {
    SparkSession sparkSession = SparkSession.builder()
      .appName("SparkDataSkewTunning")
      .config("hive.metastore.uris", "thrift://hadoop1:9083")
      .enableHiveSupport()
      .getOrCreate();

    Dataset<Row> dataframe = sparkSession.sql( "select * from test");
    dataframe.toJavaRDD()
      .mapToPair((Row row) -> new Tuple2<Integer, String>(row.getInt(0),row.getString(1)))
      .groupByKey(12)
      .mapToPair((Tuple2<Integer, Iterable<String>> tuple) -> {
        int id = tuple._1();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        tuple._2().forEach((String name) -> atomicInteger.incrementAndGet());
        return new Tuple2<Integer, Integer>(id, atomicInteger.get());
      }).count();

      sparkSession.stop();
      sparkSession.close();
  }

}
```

本次实验所使用集群节点数为 4，每个节点可被 Yarn 使用的 CPU 核数为 16，内存为 16GB。使用如下方式提交上述应用，将启动 4 个 Executor，每个 Executor 可使用核数为 12（该配置并非生产环境下的最优配置，仅用于本文实验），可用内存为 12GB。

```
spark-submit --queue ambari --num-executors 4 --executor-cores 12 --executor-memory 12g --class com.jasongj.spark.driver.SparkDataSkew --master yarn --deploy-mode client SparkExample-with-dependencies-1.0.jar
```

GroupBy Stage 的 Task 状态如下图所示，Task 8 处理的记录数为 4500 万，远大于（9 倍于）其它 11 个 Task 处理的 500 万记录。而 Task 8 所耗费的时间为 38 秒，远高于其它 11 个 Task 的平均时间（16 秒）。整个 Stage 的时间也为 38 秒，该时间主要由最慢的 Task 8 决定。

[![img](https://static001.infoq.cn/resource/image/57/3d/57c86fbe069a87e69eaa1e751938993d.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/1.png)

在这种情况下，可以通过调整 Shuffle 并行度，使得原来被分配到同一个 Task（即该例中的 Task 8）的不同 Key 分配到不同 Task，从而降低 Task 8 所需处理的数据量，缓解数据倾斜。

通过`groupByKey(48)`将 Shuffle 并行度调整为 48，重新提交到 Spark。新的 Job 的 GroupBy Stage 所有 Task 状态如下图所示。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/df/64/df6e1f07e0ea63a7d22866c938014764.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/2.png)

从上图可知，记录数最多的 Task 20 处理的记录数约为 1125 万，相比于并行度为 12 时 Task 8 的 4500 万，降低了 75% 左右，而其耗时从原来 Task 8 的 38 秒降到了 24 秒。

在这种场景下，调整并行度，并不意味着一定要增加并行度，也可能是减小并行度。如果通过`groupByKey(11)`将 Shuffle 并行度调整为 11，重新提交到 Spark。新 Job 的 GroupBy Stage 的所有 Task 状态如下图所示。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/8d/78/8d92d7177053849137eafbd5218ffd78.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/3.png)

从上图可见，处理记录数最多的 Task 6 所处理的记录数约为 1045 万，耗时为 23 秒。处理记录数最少的 Task 1 处理的记录数约为 545 万，耗时 12 秒。

## 总结

**适用场景**

大量不同的 Key 被分配到了相同的 Task 造成该 Task 数据量过大。

**解决方案**

调整并行度。一般是增大并行度，但有时如本例减小并行度也可达到效果。

**优势**

实现简单，可在需要 Shuffle 的操作算子上直接设置并行度或者使用`spark.default.parallelism`设置。如果是 Spark SQL，还可通过`SET spark.sql.shuffle.partitions=[num_tasks]`设置并行度。可用最小的代价解决问题。一般如果出现数据倾斜，都可以通过这种方法先试验几次，如果问题未解决，再尝试其它方法。

**劣势**

适用场景少，只能将分配到同一 Task 的不同 Key 分散开，但对于同一 Key 倾斜严重的情况该方法并不适用。并且该方法一般只能缓解数据倾斜，没有彻底消除问题。从实践经验来看，其效果一般。

## 自定义 Partitioner

## 原理

使用自定义的 Partitioner（默认为 HashPartitioner），将原本被分配到同一个 Task 的不同 Key 分配到不同 Task。

## 案例

以上述数据集为例，继续将并发度设置为 12，但是在`groupByKey`算子上，使用自定义的`Partitioner`（实现如下）

```
.groupByKey(new Partitioner() {
  @Override
  public int numPartitions() {
    return 12;
  }

  @Override
  public int getPartition(Object key) {
    int id = Integer.parseInt(key.toString());
    if(id >= 9500000 && id <= 9500084 && ((id - 9500000) % 12) == 0) {
      return (id - 9500000) / 12;
    } else {
      return id % 12;
    }
  }
})
```

由下图可见，使用自定义 Partition 后，耗时最长的 Task 6 处理约 1000 万条数据，用时 15 秒。并且各 Task 所处理的数据集大小相当。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/2d/ab/2d565d63ab1c221a454a481cbb73b6ab.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/4.png)

## 总结

**适用场景**

大量不同的 Key 被分配到了相同的 Task 造成该 Task 数据量过大。

**解决方案**

使用自定义的 Partitioner 实现类代替默认的 HashPartitioner，尽量将所有不同的 Key 均匀分配到不同的 Task 中。

**优势**

不影响原有的并行度设计。如果改变并行度，后续 Stage 的并行度也会默认改变，可能会影响后续 Stage。

**劣势**

适用场景有限，只能将不同 Key 分散开，对于同一 Key 对应数据集非常大的场景不适用。效果与调整并行度类似，只能缓解数据倾斜而不能完全消除数据倾斜。而且需要根据数据特点自定义专用的 Partitioner，不够灵活。

## 将 Reduce side Join 转变为 Map side Join

## 原理

通过 Spark 的 Broadcast 机制，将 Reduce 侧 Join 转化为 Map 侧 Join，避免 Shuffle 从而完全消除 Shuffle 带来的数据倾斜。

![img](https://static001.infoq.cn/resource/image/05/bc/05d3442dedd3a4eb37ee1d99dfc812bc.png)

## 案例

通过如下 SQL 创建一张具有倾斜 Key 且总记录数为 1.5 亿的大表 test。

```
INSERT OVERWRITE TABLE test
SELECT CAST(CASE WHEN id < 980000000 THEN (95000000  + (CAST (RAND() * 4 AS INT) + 1) * 48 )
       ELSE CAST(id/10 AS INT) END AS STRING),
       name
FROM student_external
WHERE id BETWEEN 900000000 AND 1050000000;
```

使用如下 SQL 创建一张数据分布均匀且总记录数为 50 万的小表 test_new。

```
INSERT OVERWRITE TABLE test_new
SELECT CAST(CAST(id/10 AS INT) AS STRING),
       name
FROM student_delta_external
WHERE id BETWEEN 950000000 AND 950500000;
```

直接通过 Spark Thrift Server 提交如下 SQL 将表 test 与表 test_new 进行 Join 并将 Join 结果存于表 test_join 中。

```
INSERT OVERWRITE TABLE test_join
SELECT test_new.id, test_new.name
FROM test
JOIN test_new
ON test.id = test_new.id;
```

该 SQL 对应的 DAG 如下图所示。从该图可见，该执行过程总共分为三个 Stage，前两个用于从 Hive 中读取数据，同时二者进行 Shuffle，通过最后一个 Stage 进行 Join 并将结果写入表 test_join 中。

![img](https://static001.infoq.cn/resource/image/2c/9d/2c4652e16481aa1763248fd2e4891e9d.png)

从下图可见，最近 Join Stage 各 Task 处理的数据倾斜严重，处理数据量最大的 Task 耗时 7.1 分钟，远高于其它无数据倾斜的 Task 约 2s 秒的耗时。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/cd/63/cdcec00e5f545fb1f97c12fe37e00a63.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/7.png)

接下来，尝试通过 Broadcast 实现 Map 侧 Join。实现 Map 侧 Join 的方法，并非直接通过`CACHE TABLE test_new`将小表 test_new 进行 cache。现通过如下 SQL 进行 Join。

```
CACHE TABLE test_new;
INSERT OVERWRITE TABLE test_join
SELECT test_new.id, test_new.name
FROM test
JOIN test_new
ON test.id = test_new.id;
```

通过如下 DAG 图可见，该操作仍分为三个 Stage，且仍然有 Shuffle 存在，唯一不同的是，小表的读取不再直接扫描 Hive 表，而是扫描内存中缓存的表。

![img](https://static001.infoq.cn/resource/image/87/7b/87c0a35182cfb9b500a1471ad4edf17b.png)

并且数据倾斜仍然存在。如下图所示，最慢的 Task 耗时为 7.1 分钟，远高于其它 Task 的约 2 秒。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/f6/6b/f61ec6705a0e264d64ba56d66c09f46b.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/9.png)

正确的使用 Broadcast 实现 Map 侧 Join 的方式是，通过`SET spark.sql.autoBroadcastJoinThreshold=104857600;`将 Broadcast 的阈值设置得足够大。

再次通过如下 SQL 进行 Join。

```
SET spark.sql.autoBroadcastJoinThreshold=104857600;
INSERT OVERWRITE TABLE test_join
SELECT test_new.id, test_new.name
FROM test
JOIN test_new
ON test.id = test_new.id;
```

通过如下 DAG 图可见，该方案只包含一个 Stage。

![img](https://static001.infoq.cn/resource/image/7a/6f/7a4de15815c70b56e0fc1406c8c5ac6f.png)

并且从下图可见，各 Task 耗时相当，无明显数据倾斜现象。并且总耗时为 1.5 分钟，远低于 Reduce 侧 Join 的 7.3 分钟。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/8c/69/8c586ca5a701223c553442266f011a69.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/11.png)

## 总结

**适用场景**

参与 Join 的一边数据集足够小，可被加载进 Driver 并通过 Broadcast 方法广播到各个 Executor 中。

**优势**

避免了 Shuffle，彻底消除了数据倾斜产生的条件，可极大提升性能。

**劣势**

要求参与 Join 的一侧数据集足够小，并且主要适用于 Join 的场景，不适合聚合的场景，适用条件有限。

## 为 skew 的 key 增加随机前 / 后缀

## 原理

为数据量特别大的 Key 增加随机前 / 后缀，使得原来 Key 相同的数据变为 Key 不相同的数据，从而使倾斜的数据集分散到不同的 Task 中，彻底解决数据倾斜问题。Join 另一则的数据中，与倾斜 Key 对应的部分数据，与随机前缀集作笛卡尔乘积，从而保证无论数据倾斜侧倾斜 Key 如何加前缀，都能与之正常 Join。

![img](https://static001.infoq.cn/resource/image/0b/87/0bd0554709fc644c1688cbe5d4068c87.png)

## 案例

通过如下 SQL，将 id 为 9 亿到 9.08 亿共 800 万条数据的 id 转为 9500048 或者 9500096，其它数据的 id 除以 100 取整。从而该数据集中，id 为 9500048 和 9500096 的数据各 400 万，其它 id 对应的数据记录数均为 100 条。这些数据存于名为 test 的表中。

对于另外一张小表 test_new，取出 50 万条数据，并将 id（递增且唯一）除以 100 取整，使得所有 id 都对应 100 条数据。

```
INSERT OVERWRITE TABLE test
SELECT CAST(CASE WHEN id < 908000000 THEN (9500000  + (CAST (RAND() * 2 AS INT) + 1) * 48 )
  ELSE CAST(id/100 AS INT) END AS STRING),
  name
FROM student_external
WHERE id BETWEEN 900000000 AND 1050000000;

INSERT OVERWRITE TABLE test_new
SELECT CAST(CAST(id/100 AS INT) AS STRING),
  name
FROM student_delta_external
WHERE id BETWEEN 950000000 AND 950500000;
```

通过如下代码，读取 test 表对应的文件夹内的数据并转换为 JavaPairRDD 存于 leftRDD 中，同样读取 test 表对应的数据存于 rightRDD 中。通过 RDD 的 join 算子对 leftRDD 与 rightRDD 进行 Join，并指定并行度为 48。

```
public class SparkDataSkew{
  public static void main(String[] args) {
    SparkConf sparkConf = new SparkConf();
    sparkConf.setAppName("DemoSparkDataFrameWithSkewedBigTableDirect");
    sparkConf.set("spark.default.parallelism", parallelism + "");
    JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

    JavaPairRDD<String, String> leftRDD = javaSparkContext.textFile("hdfs://hadoop1:8020/apps/hive/warehouse/default/test/")
      .mapToPair((String row) -> {
        String[] str = row.split(",");
        return new Tuple2<String, String>(str[0], str[1]);
      });

    JavaPairRDD<String, String> rightRDD = javaSparkContext.textFile("hdfs://hadoop1:8020/apps/hive/warehouse/default/test_new/")
      .mapToPair((String row) -> {
        String[] str = row.split(",");
          return new Tuple2<String, String>(str[0], str[1]);
      });

    leftRDD.join(rightRDD, parallelism)
      .mapToPair((Tuple2<String, Tuple2<String, String>> tuple) -> new Tuple2<String, String>(tuple._1(), tuple._2()._2()))
      .foreachPartition((Iterator<Tuple2<String, String>> iterator) -> {
        AtomicInteger atomicInteger = new AtomicInteger();
          iterator.forEachRemaining((Tuple2<String, String> tuple) -> atomicInteger.incrementAndGet());
      });

    javaSparkContext.stop();
    javaSparkContext.close();
  }
}
```

从下图可看出，整个 Join 耗时 1 分 54 秒，其中 Join Stage 耗时 1.7 分钟。

![img](D:\superz\BigData-A-Question\Spark\images\9a7e22df356736d2b19b5f77d4a0de20.png)

通过分析 Join Stage 的所有 Task 可知，在其它 Task 所处理记录数为 192.71 万的同时 Task 32 的处理的记录数为 992.72 万，故它耗时为 1.7 分钟，远高于其它 Task 的约 10 秒。这与上文准备数据集时，将 id 为 9500048 为 9500096 对应的数据量设置非常大，其它 id 对应的数据集非常均匀相符合。

![img](https://static001.infoq.cn/resource/image/bc/1e/bccb15b47c7c7cf2bf7502dc73b7f31e.png)

现通过如下操作，实现倾斜 Key 的分散处理

- 将 leftRDD 中倾斜的 key（即 9500048 与 9500096）对应的数据单独过滤出来，且加上 1 到 24 的随机前缀，并将前缀与原数据用逗号分隔（以方便之后去掉前缀）形成单独的 leftSkewRDD
- 将 rightRDD 中倾斜 key 对应的数据抽取出来，并通过 flatMap 操作将该数据集中每条数据均转换为 24 条数据（每条分别加上 1 到 24 的随机前缀），形成单独的 rightSkewRDD
- 将 leftSkewRDD 与 rightSkewRDD 进行 Join，并将并行度设置为 48，且在 Join 过程中将随机前缀去掉，得到倾斜数据集的 Join 结果 skewedJoinRDD
- 将 leftRDD 中不包含倾斜 Key 的数据抽取出来作为单独的 leftUnSkewRDD
- 对 leftUnSkewRDD 与原始的 rightRDD 进行 Join，并行度也设置为 48，得到 Join 结果 unskewedJoinRDD
- 通过 union 算子将 skewedJoinRDD 与 unskewedJoinRDD 进行合并，从而得到完整的 Join 结果集

具体实现代码如下

```
public class SparkDataSkew{
    public static void main(String[] args) {
      int parallelism = 48;
      SparkConf sparkConf = new SparkConf();
      sparkConf.setAppName("SolveDataSkewWithRandomPrefix");
      sparkConf.set("spark.default.parallelism", parallelism + "");
      JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

      JavaPairRDD<String, String> leftRDD = javaSparkContext.textFile("hdfs://hadoop1:8020/apps/hive/warehouse/default/test/")
        .mapToPair((String row) -> {
          String[] str = row.split(",");
            return new Tuple2<String, String>(str[0], str[1]);
        });

        JavaPairRDD<String, String> rightRDD = javaSparkContext.textFile("hdfs://hadoop1:8020/apps/hive/warehouse/default/test_new/")
          .mapToPair((String row) -> {
            String[] str = row.split(",");
              return new Tuple2<String, String>(str[0], str[1]);
          });

        String[] skewedKeyArray = new String[]{"9500048", "9500096"};
        Set<String> skewedKeySet = new HashSet<String>();
        List<String> addList = new ArrayList<String>();
        for(int i = 1; i <=24; i++) {
            addList.add(i + "");
        }
        for(String key : skewedKeyArray) {
            skewedKeySet.add(key);
        }

        Broadcast<Set<String>> skewedKeys = javaSparkContext.broadcast(skewedKeySet);
        Broadcast<List<String>> addListKeys = javaSparkContext.broadcast(addList);

        JavaPairRDD<String, String> leftSkewRDD = leftRDD
          .filter((Tuple2<String, String> tuple) -> skewedKeys.value().contains(tuple._1()))
          .mapToPair((Tuple2<String, String> tuple) -> new Tuple2<String, String>((new Random().nextInt(24) + 1) + "," + tuple._1(), tuple._2()));

        JavaPairRDD<String, String> rightSkewRDD = rightRDD.filter((Tuple2<String, String> tuple) -> skewedKeys.value().contains(tuple._1()))
          .flatMapToPair((Tuple2<String, String> tuple) -> addListKeys.value().stream()
          .map((String i) -> new Tuple2<String, String>( i + "," + tuple._1(), tuple._2()))
          .collect(Collectors.toList())
          .iterator()
        );

        JavaPairRDD<String, String> skewedJoinRDD = leftSkewRDD
          .join(rightSkewRDD, parallelism)
          .mapToPair((Tuple2<String, Tuple2<String, String>> tuple) -> new Tuple2<String, String>(tuple._1().split(",")[1], tuple._2()._2()));

        JavaPairRDD<String, String> leftUnSkewRDD = leftRDD.filter((Tuple2<String, String> tuple) -> !skewedKeys.value().contains(tuple._1()));
        JavaPairRDD<String, String> unskewedJoinRDD = leftUnSkewRDD.join(rightRDD, parallelism).mapToPair((Tuple2<String, Tuple2<String, String>> tuple) -> new Tuple2<String, String>(tuple._1(), tuple._2()._2()));

        skewedJoinRDD.union(unskewedJoinRDD).foreachPartition((Iterator<Tuple2<String, String>> iterator) -> {
          AtomicInteger atomicInteger = new AtomicInteger();
          iterator.forEachRemaining((Tuple2<String, String> tuple) -> atomicInteger.incrementAndGet());
        });

        javaSparkContext.stop();
        javaSparkContext.close();
    }
}
```

从下图可看出，整个 Join 耗时 58 秒，其中 Join Stage 耗时 33 秒。

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/a6/41/a604bd2773ac94302981d15495a6eb41.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/15.png)

通过分析 Join Stage 的所有 Task 可知

- 由于 Join 分倾斜数据集 Join 和非倾斜数据集 Join，而各 Join 的并行度均为 48，故总的并行度为 96
- 由于提交任务时，设置的 Executor 个数为 4，每个 Executor 的 core 数为 12，故可用 Core 数为 48，所以前 48 个 Task 同时启动（其 Launch 时间相同），后 48 个 Task 的启动时间各不相同（等待前面的 Task 结束才开始）
- 由于倾斜 Key 被加上随机前缀，原本相同的 Key 变为不同的 Key，被分散到不同的 Task 处理，故在所有 Task 中，未发现所处理数据集明显高于其它 Task 的情况

(点击放大图像)

[![img](https://static001.infoq.cn/resource/image/52/3b/523dbe3a795629bf09b94cab7929fb3b.png)](https://www.infoq.cn/mag4media/repositories/fs/articles//zh/resources/16.png)

实际上，由于倾斜 Key 与非倾斜 Key 的操作完全独立，可并行进行。而本实验受限于可用总核数为 48，可同时运行的总 Task 数为 48，故而该方案只是将总耗时减少一半（效率提升一倍）。如果资源充足，可并发执行 Task 数增多，该方案的优势将更为明显。在实际项目中，该方案往往可提升数倍至 10 倍的效率。

## 总结

**适用场景**

两张表都比较大，无法使用 Map 则 Join。其中一个 RDD 有少数几个 Key 的数据量过大，另外一个 RDD 的 Key 分布较为均匀。

**解决方案**

将有数据倾斜的 RDD 中倾斜 Key 对应的数据集单独抽取出来加上随机前缀，另外一个 RDD 每条数据分别与随机前缀结合形成新的 RDD（相当于将其数据增到到原来的 N 倍，N 即为随机前缀的总个数），然后将二者 Join 并去掉前缀。然后将不包含倾斜 Key 的剩余数据进行 Join。最后将两次 Join 的结果集通过 union 合并，即可得到全部 Join 结果。

**优势**

相对于 Map 则 Join，更能适应大数据集的 Join。如果资源充足，倾斜部分数据集与非倾斜部分数据集可并行进行，效率提升明显。且只针对倾斜部分的数据做数据扩展，增加的资源消耗有限。

**劣势**

如果倾斜 Key 非常多，则另一侧数据膨胀非常大，此方案不适用。而且此时对倾斜 Key 与非倾斜 Key 分开处理，需要扫描数据集两遍，增加了开销。

## 大表随机添加 N 种随机前缀，小表扩大 N 倍

## 原理

如果出现数据倾斜的 Key 比较多，上一种方法将这些大量的倾斜 Key 分拆出来，意义不大。此时更适合直接对存在数据倾斜的数据集全部加上随机前缀，然后对另外一个不存在严重数据倾斜的数据集整体与随机前缀集作笛卡尔乘积（即将数据量扩大 N 倍）。

![img](https://static001.infoq.cn/resource/image/45/8d/450c0bea237de9b49fd0b3a7ba4fbe8d.png)

## 案例

这里给出示例代码，读者可参考上文中分拆出少数倾斜 Key 添加随机前缀的方法，自行测试。

```
public class SparkDataSkew {
  public static void main(String[] args) {
    SparkConf sparkConf = new SparkConf();
    sparkConf.setAppName("ResolveDataSkewWithNAndRandom");
    sparkConf.set("spark.default.parallelism", parallelism + "");
    JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

    JavaPairRDD<String, String> leftRDD = javaSparkContext.textFile("hdfs://hadoop1:8020/apps/hive/warehouse/default/test/")
      .mapToPair((String row) -> {
        String[] str = row.split(",");
        return new Tuple2<String, String>(str[0], str[1]);
      });

    JavaPairRDD<String, String> rightRDD = javaSparkContext.textFile("hdfs://hadoop1:8020/apps/hive/warehouse/default/test_new/")
      .mapToPair((String row) -> {
        String[] str = row.split(",");
        return new Tuple2<String, String>(str[0], str[1]);
    });

    List<String> addList = new ArrayList<String>();
    for(int i = 1; i <=48; i++) {
      addList.add(i + "");
    }

    Broadcast<List<String>> addListKeys = javaSparkContext.broadcast(addList);

    JavaPairRDD<String, String> leftRandomRDD = leftRDD.mapToPair((Tuple2<String, String> tuple) -> new Tuple2<String, String>(new Random().nextInt(48) + "," + tuple._1(), tuple._2()));

    JavaPairRDD<String, String> rightNewRDD = rightRDD
      .flatMapToPair((Tuple2<String, String> tuple) -> addListKeys.value().stream()
      .map((String i) -> new Tuple2<String, String>( i + "," + tuple._1(), tuple._2()))
      .collect(Collectors.toList())
      .iterator()
    );

    JavaPairRDD<String, String> joinRDD = leftRandomRDD
      .join(rightNewRDD, parallelism)
      .mapToPair((Tuple2<String, Tuple2<String, String>> tuple) -> new Tuple2<String, String>(tuple._1().split(",")[1], tuple._2()._2()));

    joinRDD.foreachPartition((Iterator<Tuple2<String, String>> iterator) -> {
      AtomicInteger atomicInteger = new AtomicInteger();
      iterator.forEachRemaining((Tuple2<String, String> tuple) -> atomicInteger.incrementAndGet());
    });

    javaSparkContext.stop();
    javaSparkContext.close();
  }
}
```

## 总结

**适用场景**

一个数据集存在的倾斜 Key 比较多，另外一个数据集数据分布比较均匀。

**优势**

对大部分场景都适用，效果不错。

**劣势**

需要将一个数据集整体扩大 N 倍，会增加资源消耗。

## 总结

对于数据倾斜，并无一个统一的一劳永逸的方法。更多的时候，是结合数据特点（数据集大小，倾斜 Key 的多少等）综合使用上文所述的多种方法。