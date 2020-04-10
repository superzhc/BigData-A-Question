上一节里说了如何发现JVM内存中的空集合对象，这对减少JVM内存的浪费有些帮助。这一节要说的内容更有意思一些，可以让我们更深入地了解Java集合的使用。

先说明一点：这里仅仅是通过内存分析来得到一些结论。分析出来的结论通过JDK源码也许可以很轻易地得出，但是请关注分析过程和分析思路，毕竟源码不常有。

在上一节分析空集合对象的过程中难免会产生一些疑问：每个集合的长度有多大？这些集合实例由哪些对象持有？还有一些衍生出的问题：集合空间（capacity）的使用率（或者说填充率）有多高；集合类的hash函数是否足够好；当将元素填充进HashMap实例时是否会发生很多hash冲突？这些都是非常有意义的问题，有助于我们解决具体的任务。在设计开发阶段，这些问题未必都能得到解答，但是在运行测试阶段，借助MAT我们也许可以得到答案。

### 方案说明

这次会使用MAT中的Query Browser对dump文件进行切面式的分析。Query Browser我们之前已经多次使用，它提供了一系列非常有用的功能，就是这个：[![query browser](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\query-browser_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/query-browser.png)。这次主要是用到Java Collections功能组。说一下思路，目前我们有两种方案：

方案一：查询dump中某一个集合类型的全部对象（比如HashMap），然后对结果集进行分组。比如按size进行分组，然后查看size为1的集合对象的持有者。如果您的兴趣在于dump中的全部对象，那么这个方案就值得参考。

方案二：首先缩小要分析的对象的范围，而后使用Java Collection功能组进行分析。举个例子，首先筛选出类名符合“com.zhyea.projects.appname.*”的对象集合，而后在这些对象的histogram视图中再筛选出全部HashMap对象，而后执行查询查看“com.zhyea.projects.appname.*”对象集合中的HashMap对象的size分布信息。如果您的目标是服务器上的某个应用或者是应用中的某个模块，就可以试一下这个方案。

### 集合对象size

采用哪种结构存储数据以及分配多少初始化空间等内容是在开发阶段决定的，但是用来存储数据的集合对象通常是在运行阶段创建的。因此集合的size以及其持有者通常不是很明确。分析现实场景中得到的dump文件是验证之前采用的方案是否正确的一种方式。通过分析dump文件用户可以知道为集合对象分配的初始空间是否太小或者太大。太小了就会导致集合对象在运行时需要不断的重新调整大小，太大了则会导致不必要的内存浪费。

我取了一份公司测试环境中的dump文件，接下来将使用这份文件演示一下分析的过程。

接下来要做的事就是过滤并显示“com.joyxsys.projects.*”包下类的所有实例。通过执行这个操作我们可以只关注要查看的类，而不受dump中大量的其他对象的干扰。

这个需要用到Query Browser中的Show Retained Set功能：

[![show retained set](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\show-retained-set_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/show-retained-set.png)

点击菜单项后，在弹出窗口中输入参数信息：

[![show retained set arguments](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\show-retained-set-arguments_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/show-retained-set-arguments.png)

然后就可以从现有的结果集中进一步过滤出符合“.*HashMap*”的对象：

[![filter hashmap](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\filter-hashmap_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/filter-hashmap.png)

过滤后的结果：

[![filter hashmap result](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\filter-hashmap-result_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/filter-hashmap-result.png)

之后执行右键菜单中的“Collections Grouped By Size”菜单项：

![collection group by size](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\collection-group-by-size_thumb.png)

执行菜单项后得到的结果是一个表格。表格的第一列是HashMap对象的大小，第二列是这样大小的HashMap对象的数量，再就是对应组中对象的Shallow Size和近似Retained Size：

[![shallow retained size](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\shallow-retained-size_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/shallow-retained-size.png)

如果对其中的某组数据感兴趣（比如长度为0的这组数据）就可以使用“Immediate dominators”这个菜单项来找出是谁持有这个组中的对象：

[![0 immedia dominat](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\0-immedia-dominat_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/0-immedia-dominat.png)

就如之前的经验，在这个面板中我们会看到一些类：

[![hashmap objects](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\hashmap-objects_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/hashmap-objects.png)

通过分析我们可以看到，在我的应用中共有104个HashMap对象，其中size为0的对象有93个。DelegatingClassLoader持有了90个size为0的HashMap对象。

### 集合填充率

“Collection Fill Ratio（集合填充率）”查询和刚才执行的查询在某种程度上很类似。不同之处在于Fill Ratio查询只对会为元素预先分配空间的集合类型有效，比如HashMap、ArrayList等等。Fill Ratio查询得到的信息是预分配空间的使用率，这个值通常在0~1之间，计算公式是 Fill Ratio = size / capacity。

对“com.joyxsys.projects.*”包下的HashMap实例执行Collection Fill Ratio：

[![query collection ratio](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\query-collection-ratio_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/query-collection-ratio.png)

查询结果如下图：

[![query collection ratio result](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\query-collection-ratio-result_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/query-collection-ratio-result.png)

可以看到在104个HashMap实例中，93个是空的（和上面得到的结果一致），还有9个Fill Ratio不到20%。所有实例中Fill Ratio最高也不过40%。当然本身数据的总量就不够大，因此也说明不了多少问题。

通常可以把上面提到的两种方式结合起来分析问题。比如可以先执行”Collections Fill Ratio”，而后在得到的分组结果基础上执行”Collections Grouped By Size”操作。

### Hash效率

现在开始讨论下一个问题——hashMap和Hashtable中的hash冲突。一个不甚高明的hash()方法实现会严重影响哈希表的查找速度。最为极端的一种情况就是所有元素返回的hash值都是一样的，这样每次查找就相当于遍历一个LinkedList。

哈希函数是否会导致太多哈希冲突也是一个在开发阶段不容易得到验证的问题，但同时也是通过分析dump文件容易得到结果的一个典型案例。尽管这问题和性能相关更多，但还是在dump中留下了相当多的痕迹，我们可以借助”Map Collision Ratio”来解决这个问题。通过”Map Collision Ratio”可以对HashMap（或Hashtable）的实例按哈希冲突的概率进行分组。哈希冲突的概率是向hash表中插入Entry时发生哈希冲突的概率。

仍然是分析刚才的dump文件，不过这次直接查看全部的HashMap实例，打开Histogram视图，过滤HashMap实例，之后再执行“Map Collision Ratio”查询：

[![query map collision](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\query-map-collision_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/query-map-collision.png)

执行查询后会得到一个表格，其中第一列是Collision Ratio，第二列是相应的对象的数目。

[![query map collision result](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\query-map-collision-result_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/query-map-collision-result.png)

在这里可以很清楚地看到只有一个实例的Collision Ratio介于60%到80%之间。现在可以看一下这个实例，以及发生冲突的所有的key。首先要尝试获取相关的实例：

[![with outgoing reference](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\with-outgoing-reference_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/with-outgoing-reference.png)

执行结果：

[![with outgoing reference result](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\with-outgoing-reference-result_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/with-outgoing-reference-result.png)

在这个里面是看不出什么来的，因为key的类型是String。关于String的hash函数是否够好我想不需多说。但是为什么还会出现这样的情况呢，我能想到的一个解释就是太巧了，碰巧大部分元素都是相同的hash函数。这样的一个HashMap实例会呈现出什么样的特点呢：就是会出现一个非常长的链表，hash表中几乎80%的元素都会在这个链表中。这个可以通过“Grouped By Size”和“List Objects”这两个工具来分析一下。

### 查看HashMap内容

最后介绍一个可以方便查看Map结构内容的工具：“Hash Entries”。在做性能分析工作时这个工具将会经常用到。

通过“List Objects”查看HashMap实例内容通常都不是一件容易的事情，关键是太容易受到干扰了，一大堆内容同时出现，尤其是Hash冲突比较严重的时候，需要不停的展开折叠内容。

[![list objects](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\list-objects_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/list-objects.png)

看看上面这张图体验一下。

这时可以使用“Hash Entries”对一个或多个hashMap实例进行查询，查询结果依然是一个表格：

[![result table](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\result-table_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/result-table.png)

这时候是不是看起来舒服多了。现在可以使用右键菜单继续对目标实例进行分析了。

[![context menu](D:\superz\BigData-A-Question\JVM\工具\Eclipse Memory Analyzer(MAT)\images\context-menu_thumb.png)](http://www.zhyea.com/wp-content/uploads/2016/07/context-menu.png)

### 分析数组

在“Java Collections”功能组还提供了两个数组分析的工具（功能和我们前面介绍的类似，所以不会再重复说明了）：

Arrays Grouped By Size：对直接类型数组和对象数组都有效；

Array Fill Ratio：对直接类型数组无效，统计数组中值不为null的元素的比例。