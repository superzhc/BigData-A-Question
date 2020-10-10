# Spark Shuffle

Spark Shuffle 分为两个步骤：`Shuffle Write` 和 `Shuffle Read`。

- `Shuffle Write`：上一个 stage 的每个 map task 就必须保证将自己处理的当前分区中的数据相同的 key 写入一个分区文件中，可能会写入多个不同的分区文件中
- `Shuffle Read`：reduce task 就会从上一个 stage 的所有 task 所在的机器上寻找属于自己的那些分区文件，这样就可以保证每一个 key 所对应的 value 都会汇聚到同一个节点上去处理和聚合

## Shuffle Write

### 普通的 HashShuffle

Spark 中需要 shuffle 输出的 map 任务会为每个 reduce 创建对应的 bucket，map 产生的结果会根据设置的 partitioner 得到对应的 bucketId，然后填充到相应的 bucket 中去。每个 map 的输出结果可能包含所有的 reduce 所需要的数据，所以每个 map 会创建 R 个 bucket（R 是 reduce 的个数），M 个 map 总共会创建 M*R 个 bucket。

Map 创建的 bucket 其实对应磁盘上的一个文件，map 的结果写到每个 bucket 中其实就是写到那个磁盘文件中，这个文件也被称为 blockFile，是 DiskBlockManager 管理器通过文件名的 hash 值对应到本地目录的子目录中创建的。每个 map 要在节点上创建 R 个磁盘文件用于结果输出，map 的结果是直接输出到磁盘文件上的，100KB 的内存缓冲是用来创建 FastBufferedOutputStream 输出流。这种方式一个问题就是磁盘小文件和缓存过多，造成耗时且低效的IO操作，可能造成OOM。

### 优化的 HashShuffle

针对上述 shuffle 过程产生的文件过多问题，Spark 有另外一种改进的 shuffle 过程：consolidation shuffle，以期显著减少 shuffle 文件的数量。在 consolidation shuffle 中每个 bucket 并非对应一个文件，而是对应文件中的一个 segment 部分。Job 的 map 在某个节点上第一次执行，为每个 reduce 创建 bucket 对应的输出文件，把这些文件组织成 ShuffleFileGroup，当这次 map 执行完之后，这个 ShuffleFileGroup 可以释放为下次循环利用；当又有 map 在这个节点上执行时，不需要创建新的 bucket 文件，而是在上次的 ShuffleFileGroup 中取得已经创建的文件继续追加写一个 segment；当前次 map 还没执行完，ShuffleFileGroup 还没有释放，这时如果有新的 map 在这个节点上执行，无法循环利用这个 ShuffleFileGroup，而是只能创建新的 bucket 文件组成新的 ShuffleFileGroup 来写输出。

因此，文件数量为：假如总共有 c 个 core， r 个 reduce，最终会产生 c*r 个小文件，因为复用 buffer 后，每个 core 执行的所有 map task 产生 r 个小文件

*该优化通过设置一个参数 `spark.shuffle.consolidateFiles`，该参数默认值为false，将其设置为true即可开启优化机制。通常来说，如果使用HashShuffleManager，那么都建议开启这个选项。*

### 普通的 SortShuffle

在该模式下，数据会先写入一个内存数据结构中，此时根据不同的 shuffle 算子，可能选用不同的数据结构。如果是 reduceByKey 这种**聚合类的 shuffle 算子，那么会选用 Map 数据结构，一边通过 Map 进行聚合，一边写入内存**；如果是 join 这种普通的 shuffle 算子，那么会选用 Array 数据结构，直接写入内存。接着，每写一条数据进入内存数据结构之后，就会判断一下，是否达到了某个临界阈值。如果达到临界阈值的话，那么就会尝试将内存数据结构中的数据溢写到磁盘，然后清空内存数据结构。

1. 每个 map task 将计算结果写入内存数据结构中，这个内存默认大小为 5M
2. 会有一个“监控器”来不定时的检查这个内存的大小，如果写满了5M，比如达到了5.01M，那么再给这个内存申请5.02M（5.01M * 2 – 5M = 5.02）的内存，此时这个内存空间的总大小为10.02M
3. 当“定时器”再次发现数据已经写满了，大小10.05M，会再次给它申请内存，大小为 10.05M * 2 – 10.02M = 10.08M
4. 假如此时总的内存只剩下5M，不足以再给这个内存分配10.08M，那么这个内存会被锁起来，把里面的数据按照相同的key为一组，进行排序后，分别写到不同的缓存中，然后溢写到不同的小文件中，而 map task 产生的新的计算结果会写入总内存剩余的5M中
5. buffer中的数据（已经排好序）溢写的时候，会分批溢写，默认一次溢写10000条数据，假如最后一部分数据不足10000条，那么剩下多少条就一次性溢写多少条
6. 每个map task产生的小文件，最终合并成一个大文件来让reduce拉取数据，合成大文件的同时也会生成这个大文件的索引文件，里面记录着分区信息和偏移量（比如：key为hello的数据在第5个字节到第8097个字节）
7. 最终产生的小文件数为2*m（map task的数量）

## Shuffle Read

Reduce 去拖 map 的输出数据，Spark 提供了两套不同的拉取数据框架：

1. 通过 socket 连接去取数据；
2. 使用 netty 框架去取数据。

每个节点的 Executor 会创建一个 BlockManager，其中会创建一个 BlockManagerWorker 用于响应请求。当 reduce 的 GET_BLOCK 的请求过来时，读取本地文件将这个 blockId 的数据返回给 reduce。如果使用的是 Netty 框架，BlockManager 会创建 ShuffleSender 用于发送 shuffle 数据。

并不是所有的数据都是通过网络读取，对于在本地节点的 map 数据，reduce 直接去磁盘上读取而不再通过网络框架。

## Shuffle 调优

### 配置参数的三种方式

1. 在程序中硬编码，例如 `sparkConf.set("spark.shuffle.file.buffer","64k")`
2. 提交 application 时在命令行指定，例如 `spark-submit --conf spark.shuffle.file.buffer=64k --conf 配置信息=配置值 ...`；推荐该种方式
3. 修改 `SPARK_HOME/conf/spark-default.conf` 配置文件

| 参数                                    | 默认值 | 参数说明                                                                                                                                                                                                                                                                                  | 调优建议                                                                                                                                                                                                                                                                                                                                                   |
| --------------------------------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| spark.shuffle.file.buffer               | 32K    | 该参数用于设置shuffle write task的BufferedOutputStream的buffer缓冲大小。将数据写到磁盘文件之前，会先写入buffer缓冲中，待缓冲写满之后，才会溢写到磁盘。                                                                                                                                    | 如果作业可用的内存资源较为充足的话，可以适当增加这个参数的大小（比如64k），从而减少shuffle write过程中溢写磁盘文件的次数，也就可以减少磁盘IO次数，进而提升性能。在实践中发现，合理调节该参数，性能会有1%~5%的提升。                                                                                                                                        |
| spark.reducer.maxSizeInFlight           | 48M    | 该参数用于设置shuffle read task的buffer缓冲大小，而这个buffer缓冲决定了每次能够拉取多少数据。                                                                                                                                                                                             | 如果作业可用的内存资源较为充足的话，可以适当增加这个参数的大小（比如96M），从而减少拉取数据的次数，也就可以减少网络传输的次数，进而提升性能。在实践中发现，合理调节该参数，性能会有1%~5%的提升。                                                                                                                                                           |
| spark.shuffle.io.maxRetries             | 3      | shuffle read task从shuffle write task所在节点拉取属于自己的数据时，如果因为网络异常导致拉取失败，是会自动进行重试的。该参数就代表了可以重试的最大次数。如果在指定次数之内拉取还是没有成功，就可能会导致作业执行失败。                                                                     | 对于那些包含了特别耗时的shuffle操作的作业，建议增加重试最大次数（比如60次），以避免由于JVM的full gc或者网络不稳定等因素导致的数据拉取失败。在实践中发现，对于针对超大数据量（数十亿~上百亿）的shuffle过程，调节该参数可以大幅度提升稳定性。                                                                                                                |
| spark.shuffle.io.retryWait              | 5s     | 具体解释同上，该参数代表了每次重试拉取数据的等待间隔，默认是5s。                                                                                                                                                                                                                          | 建议加大间隔时长（比如60s），以增加shuffle操作的稳定性。                                                                                                                                                                                                                                                                                                   |
| spark.shuffle.memoryFraction            | 0.2    | 该参数代表了Executor内存中，分配给shuffle read task进行聚合操作的内存比例，默认是20%。                                                                                                                                                                                                    | 在资源参数调优中讲解过这个参数。如果内存充足，而且很少使用持久化操作，建议调高这个比例，给shuffle read的聚合操作更多内存，以避免由于内存不足导致聚合过程中频繁读写磁盘。在实践中发现，合理调节该参数可以将性能提升10%左右。                                                                                                                                |
| spark.shuffle.manager                   | sort   | 该参数用于设置ShuffleManager的类型。Spark 1.5以后，有三个可选项：hash、sort和tungsten-sort。HashShuffleManager是Spark 1.2以前的默认选项，但是Spark 1.2以及之后的版本默认都是SortShuffleManager了。tungsten-sort与sort类似，但是使用了tungsten计划中的堆外内存管理机制，内存使用效率更高。 | 由于SortShuffleManager默认会对数据进行排序，因此如果你的业务逻辑中需要该排序机制的话，则使用默认的SortShuffleManager就可以；而如果你的业务逻辑不需要对数据进行排序，那么建议参考后面的几个参数调优，通过bypass机制或优化的HashShuffleManager来避免排序操作，同时提供较好的磁盘读写性能。这里要注意的是，tungsten-sort要慎用，因为之前发现了一些相应的bug。 |
| spark.shuffle.sort.bypassMergeThreshold | 200    | 当ShuffleManager为SortShuffleManager时，如果shuffle read task的数量小于这个阈值（默认是200），则shuffle write过程中不会进行排序操作，而是直接按照未经优化的HashShuffleManager的方式去写数据，但是最后会将每个task产生的所有临时磁盘文件都合并成一个文件，并会创建单独的索引文件。         | 当你使用SortShuffleManager时，如果的确不需要排序操作，那么建议将这个参数调大一些，大于shuffle read task的数量。那么此时就会自动启用bypass机制，map-side就不会进行排序了，减少了排序的性能开销。但是这种方式下，依然会产生大量的磁盘文件，因此shuffle write性能有待提高。                                                                                   |
| spark.shuffle.consolidateFiles          | false  | 如果使用HashShuffleManager，该参数有效。如果设置为true，那么就会开启consolidate机制，会大幅度合并shuffle write的输出文件，对于shuffle read task数量特别多的情况下，这种方法可以极大地减少磁盘IO开销，提升性能。                                                                           | 如果的确不需要SortShuffleManager的排序机制，那么除了使用bypass机制，还可以尝试将spark.shffle.manager参数手动指定为hash，使用HashShuffleManager，同时开启consolidate机制。在实践中尝试过，发现其性能比开启了bypass机制的SortShuffleManager要高出10%~30%。                                                                                                   |
