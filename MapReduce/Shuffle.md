Shuffle 的本义是洗牌、混洗，把一组有一定规则的数据尽量转换成一组无规则的数据，越随机越好。MapReduce 中的 shuffle 更像是洗牌的逆过程，把一组无规则的数据尽量转换成一组具有一定规则的数据。

为什么 MapReduce 计算模型需要 shuffle 过程？我们都知道 MapReduce 计算模型一般包括两个重要的阶段：map 是映射，负责数据的过滤分发；reduce 是规约，负责数据的计算归并。Reduce 的数据来源于 map，map 的输出即是 reduce 的输入，reduce 需要通过 shuffle 来获取数据。

从 map 输出到 reduce 输入的整个过程可以广义地称为 shuffle。Shuffle 横跨 map 端和 reduce 端，在 map 端包括 spill 过程，在 reduce 端包括 copy 和 sort 过程，如图所示：

![腾讯大数据之TDW计算引擎解析——Shuffle](D:\superz\BigData-A-Question\MapReduce\images\7b3e19f91e797adcb31e807a912cb7ad.jpg)

### Spill过程

Spill 过程包括输出、排序、溢写、合并等步骤，如图所示：

![腾讯大数据之TDW计算引擎解析——Shuffle](D:\superz\BigData-A-Question\MapReduce\images\454e822119c8da2e8ed753fd71fba982.png)

### **Collect**

每个 Map 任务不断地以 <key, value> 对的形式把数据输出到在内存中构造的一个环形数据结构中。使用环形数据结构是为了更有效地使用内存空间，在内存中放置尽可能多的数据。

这个数据结构其实就是个字节数组，叫 kvbuffer，名如其义，但是这里面不光放置了 <key, value> 数据，还放置了一些索引数据，给放置索引数据的区域起了一个 kvmeta 的别名，在 kvbuffer 的一块区域上穿了一个 IntBuffer（字节序采用的是平台自身的字节序）的马甲。<key, value> 数据区域和索引数据区域在 kvbuffer 中是相邻不重叠的两个区域，用一个分界点来划分两者，分界点不是亘古不变的，而是每次 spill 之后都会更新一次。初始的分界点是 0，<key, value> 数据的存储方向是向上增长，索引数据的存储方向是向下增长，如图所示：

![腾讯大数据之TDW计算引擎解析——Shuffle](D:\superz\BigData-A-Question\MapReduce\images\cff3a3ad014fe4d451c2ff0dabff924d.png)

Kvbuffer 的存放指针 bufindex 是一直闷着头地向上增长，比如 bufindex 初始值为 0，一个 Int 型的 key 写完之后，bufindex 增长为 4，一个 Int 型的 value 写完之后，bufindex 增长为 8。

索引是对 <key, value> 在 kvbuffer 中的索引，是个四元组，包括：value 的起始位置、key 的起始位置、partition 值、value 的长度，占用四个 Int 长度，kvmeta 的存放指针 kvindex 每次都是向下跳四个“格子”，然后再向上一个格子一个格子地填充四元组的数据。比如 kvindex 初始位置是 -4，当第一个 <key, value> 写完之后，(kvindex+0) 的位置存放 value 的起始位置、(kvindex+1) 的位置存放 key 的起始位置、(kvindex+2) 的位置存放 partition 的值、(kvindex+3) 的位置存放 value 的长度，然后 kvindex 跳到 -8 位置，等第二个 <key, value> 和索引写完之后，kvindex 跳到 -32 位置。

Kvbuffer 的大小虽然可以通过参数设置，但是总共就那么大，<key, value> 和索引不断地增加，加着加着，kvbuffer 总有不够用的那天，那怎么办？把数据从内存刷到磁盘上再接着往内存写数据，把 kvbuffer 中的数据刷到磁盘上的过程就叫 spill，多么明了的叫法，内存中的数据满了就自动地 spill 到具有更大空间的磁盘。

关于 spill 触发的条件，也就是 kvbuffer 用到什么程度开始 spill，还是要讲究一下的。如果把 kvbuffer 用得死死得，一点缝都不剩的时候再开始 spill，那 map 任务就需要等 spill 完成腾出空间之后才能继续写数据；如果 kvbuffer 只是满到一定程度，比如 80% 的时候就开始 spill，那在 spill 的同时，map 任务还能继续写数据，如果 spill 够快，map 可能都不需要为空闲空间而发愁。两利相衡取其大，一般选择后者。

Spill 这个重要的过程是由 spill 线程承担，spill 线程从 map 任务接到“命令”之后就开始正式干活，干的活叫 sortAndSpill，原来不仅仅是 spill，在 spill 之前还有个颇具争议性的 sort。

### **Sort**

先把 kvbuffer 中的数据按照 partition 值和 key 两个关键字升序排序，移动的只是索引数据，排序结果是 kvmeta 中数据按照 partition 为单位聚集在一起，同一 partition 内的按照 key 有序。

### **Spill**

Spill 线程为这次 spill 过程创建一个磁盘文件：从所有的本地目录中轮训查找能存储这么大空间的目录，找到之后在其中创建一个类似于“spill12.out”的文件。Spill 线程根据排过序的 kvmeta 挨个 partition 的把 <key, value> 数据吐到这个文件中，一个 partition 对应的数据吐完之后顺序地吐下个 partition，直到把所有的 partition 遍历完。一个 partition 在文件中对应的数据也叫段 (segment)。

所有的 partition 对应的数据都放在这个文件里，虽然是顺序存放的，但是怎么直接知道某个 partition 在这个文件中存放的起始位置呢？强大的索引又出场了。有一个三元组记录某个 partition 对应的数据在这个文件中的索引：起始位置、原始数据长度、压缩之后的数据长度，一个 partition 对应一个三元组。

然后把这些索引信息存放在内存中，如果内存中放不下了，后续的索引信息就需要写到磁盘文件中了：从所有的本地目录中轮训查找能存储这么大空间的目录，找到之后在其中创建一个类似于“spill12.out.index”的文件，文件中不光存储了索引数据，还存储了 crc32 的校验数据。(spill12.out.index 不一定在磁盘上创建，如果内存（默认 1M 空间）中能放得下就放在内存中，即使在磁盘上创建了，和 spill12.out 文件也不一定在同一个目录下。)

每一次 spill 过程就会最少生成一个 out 文件，有时还会生成 index 文件，spill 的次数也烙印在文件名中。索引文件和数据文件的对应关系如下图所示：

![腾讯大数据之TDW计算引擎解析——Shuffle](D:\superz\BigData-A-Question\MapReduce\images\950957150ebc291eaedaf9d06aa1b67b.png)

话分两端，在 spill 线程如火如荼的进行 sortAndSpill 工作的同时，map 任务不会因此而停歇，而是一无既往地进行着数据输出。Map 还是把数据写到 kvbuffer 中，那问题就来了：<key, value> 只顾着闷头按照 bufindex 指针向上增长，kvmeta 只顾着按照 kvindex 向下增长，是保持指针起始位置不变继续跑呢，还是另谋它路？如果保持指针起始位置不变，很快 bufindex 和 kvindex 就碰头了，碰头之后再重新开始或者移动内存都比较麻烦，不可取。Map 取 kvbuffer 中剩余空间的中间位置，用这个位置设置为新的分界点，bufindex 指针移动到这个分界点，kvindex 移动到这个分界点的 -16 位置，然后两者就可以和谐地按照自己既定的轨迹放置数据了，当 spill 完成，空间腾出之后，不需要做任何改动继续前进。分界点的转换如下图所示：

![腾讯大数据之TDW计算引擎解析——Shuffle](D:\superz\BigData-A-Question\MapReduce\images\9a7956817712a852b61f145c66666a56.png)

Map 任务总要把输出的数据写到磁盘上，即使输出数据量很小在内存中全部能装得下，在最后也会把数据刷到磁盘上。

### **Merge**

Map 任务如果输出数据量很大，可能会进行好几次 spill，out 文件和 index 文件会产生很多，分布在不同的磁盘上。最后把这些文件进行合并的 merge 过程闪亮登场。

Merge 过程怎么知道产生的 spill 文件都在哪了呢？从所有的本地目录上扫描得到产生的 spill 文件，然后把路径存储在一个数组里。Merge 过程又怎么知道 spill 的索引信息呢？没错，也是从所有的本地目录上扫描得到 index 文件，然后把索引信息存储在一个列表里。到这里，又遇到了一个值得纳闷的地方。在之前 spill 过程中的时候为什么不直接把这些信息存储在内存中呢，何必又多了这步扫描的操作？特别是 spill 的索引数据，之前当内存超限之后就把数据写到磁盘，现在又要从磁盘把这些数据读出来，还是需要装到更多的内存中。之所以多此一举，是因为这时 kvbuffer 这个内存大户已经不再使用可以回收，有内存空间来装这些数据了。（对于内存空间较大的土豪来说，用内存来省却这两个 I/O 步骤还是值得考虑的。）

然后为 merge 过程创建一个叫 file.out 的文件和一个叫 file.out.index 的文件用来存储最终的输出和索引。

一个 partition 一个 partition 的进行合并输出。对于某个 partition 来说，从索引列表中查询这个 partition 对应的所有索引信息，每个对应一个段插入到段列表中。也就是这个 partition 对应一个段列表，记录所有的 spill 文件中对应的这个 partition 那段数据的文件名、起始位置、长度等等。

然后对这个 partition 对应的所有的 segment 进行合并，目标是合并成一个 segment。当这个 partition 对应很多个 segment 时，会分批地进行合并：先从 segment 列表中把第一批取出来，以 key 为关键字放置成最小堆，然后从最小堆中每次取出最小的 <key, value> 输出到一个临时文件中，这样就把这一批段合并成一个临时的段，把它加回到 segment 列表中；再从 segment 列表中把第二批取出来合并输出到一个临时 segment，把其加入到列表中；这样往复执行，直到剩下的段是一批，输出到最终的文件中。

最终的索引数据仍然输出到 index 文件中。

![腾讯大数据之TDW计算引擎解析——Shuffle](D:\superz\BigData-A-Question\MapReduce\images\4f6760e31d02437478a0b7788a1fd68c.png)

Map 端的 shuffle 过程到此结束。

### **Copy**

Reduce 任务通过 http 向各个 map 任务拖取它所需要的数据。每个节点都会启动一个常驻的 http server，其中一项服务就是响应 reduce 拖取 map 数据。当有 mapOutput 的 http 请求过来的时候，http server 就读取相应的 map 输出文件中对应这个 reduce 部分的数据通过网络流输出给 reduce。

Reduce 任务拖取某个 map 对应的数据，如果在内存中能放得下这次数据的话就直接把数据写到内存中。Reduce 要向每个 map 去拖取数据，在内存中每个 map 对应一块数据，当内存中存储的 map 数据占用空间达到一定程度的时候，开始启动内存中 merge，把内存中的数据 merge 输出到磁盘上一个文件中。

如果在内存中不能放得下这个 map 的数据的话，直接把 map 数据写到磁盘上，在本地目录创建一个文件，从 http 流中读取数据然后写到磁盘，使用的缓存区大小是 64K。拖一个 map 数据过来就会创建一个文件，当文件数量达到一定阈值时，开始启动磁盘文件 merge，把这些文件合并输出到一个文件。

有些 map 的数据较小是可以放在内存中的，有些 map 的数据较大需要放在磁盘上，这样最后 reduce 任务拖过来的数据有些放在内存中了有些放在磁盘上，最后会对这些来一个全局合并。

### **Merge Sort**

这里使用的 merge 和 map 端使用的 merge 过程一样。Map 的输出数据已经是有序的，merge 进行一次合并排序，所谓 reduce 端的 sort 过程就是这个合并的过程。一般 reduce 是一边 copy 一边 sort，即 copy 和 sort 两个阶段是重叠而不是完全分开的。

Reduce 端的 shuffle 过程至此结束。