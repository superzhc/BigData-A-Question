集合代表了一组对象（和数组一样，但数组长度不能变，而集合能）。Java中的集合框架定义了一套规范，用来表示操作集合，使具体操作与实现细节解耦。

## Collection接口

Collection 接口是最基本的集合接口，它不提供直接的实现，Java SDK 提供的类都是继承自 Collection 的*子接口*如 List 和 Set。Collection 所代表的是一种规则，它所包含的元素都必须遵循一条或者多条规则。如有些允许重复而有些则不能重复、有些必须要按照顺序插入而有些则是散列，有些支持排序但是有些则不支持。

在 Java 中所有实现了 Collection 接口的类都必须提供两套标准的构造函数，一个是无参，用于创建一个空的 Collection，一个是带有 Collection 参数的有参构造函数，用于创建一个新的 Collection，这个新的 Collection 与传入进来的 Collection 具备相同的元素。

### List

List接口直接继承Collection接口。List所代表的是有序的Collection，即它用某种特定的插入顺序来维护元素顺序。用户可以对列表中每个元素的插入位置进行精确地控制，同时可以根据元素的整数索引（在列表中的位置）访问元素，并搜索列表中的元素。实现List接口的集合主要有：ArrayList、LinkedList、Vector、Stack。

List使用规则如下：

- 对于需要快速插入、删除元素，则需使用LinkedList。
- 对于需要快速访问元素，则需使用ArrayList。
- 对于“单线程环境”或者“多线程环境，但是List仅被一个线程操作”，需要考虑使用非同步的类，如果是“多线程环境，切List可能同时被多个线程操作”，考虑使用同步的类（如Vector）。

### Queues/deques

队列，它主要分为两大类，一类是阻塞式队列，队列满了以后再插入元素则会抛出异常，主要包括ArrayBlockQueue、PriorityBlockingQueue、LinkedBlockingQueue。另一种队列则是双端队列，支持在头、尾两端插入和移除元素，主要包括：ArrayDeque、LinkedBlockingDeque、LinkedList。

### Stack

### Map

Map与List、Set接口不同，它是由一系列键值对组成的集合，提供了key到Value的映射。同时它也没有继承Collection。在Map中它保证了key与value之间的一一对应关系。也就是说一个key对应一个value，所以它不能存在相同的key值，当然value值可以相同。实现map的有：HashMap、TreeMap、Hashtable、Properties、EnumMap。

几乎所有通用Map都使用哈希映射技术。哈希映射技术是一种就元素映射到数组的非常简单的技术。由于哈希映射采用的是数组结果，那么必然存在一种用于确定任意键访问数组的索引机制，该机制能够提供一个小于数组大小的整数，将该机制称之为哈希函数。在Java中不必为寻找这样的整数而大伤脑筋，因为每个对象都必定存在一个返回整数值的hashCode方法，需要做的就是将其转换为整数，然后再将该值除以数组大小取余即可。

在哈希映射表中，内部数组中的每个位置称作“存储桶”(bucket)，而可用的存储桶数（即内部数组的大小）称作容量 (capacity)，为了使Map对象能够有效地处理任意数的元素，将Map设计成可以调整自身的大小。如果开始就知道Map的预期大小值，将Map调整的足够大，则可以大大减少甚至不需要重新调整大小，这很有可能会提高速度。

负载因子本身就是在控件和时间之间的折衷。当我使用较小的负载因子时，虽然降低了冲突的可能性，使得单个链表的长度减小了，加快了访问和更新的速度，但是它占用了更多的控件，使得数组中的大部分控件没有得到利用，元素分布比较稀疏，同时由于Map频繁的调整大小，可能会降低性能。但是如果负载因子过大，会使得元素分布比较紧凑，导致产生冲突的可能性加大，从而访问、更新速度较慢。所以我们一般推荐不更改负载因子的值，采用默认值0.75。

### Set

Set是一种不包括重复元素的Collection。它维持它自己的内部排序，所以随机访问没有任何意义。与List一样，它同样运行null的存在但是仅有一个。由于Set接口的特殊性，所有传入Set集合中的元素都必须不同，同时要注意任何可变对象，如果在对集合中元素进行操作时，导致e1.equals(e2)==true，则必定会产生某些问题。实现了Set接口的集合有：EnumSet、HashSet、TreeSet。

## 异同点

### Vector和ArrayList

1. vector是线程同步的，所以它也是线程安全的，而arraylist是线程异步的，是不安全的。如果不考虑到线程的安全因素，一般用arraylist效率比较高。
2. 如果集合中的元素的数目大于目前集合数组的长度时，vector增长率为目前数组长度的100%,而arraylist增长率为目前数组长度的50%.如过在集合中使用数据量比较大的数据，用vector有一定的优势。 
3. 如果查找一个指定位置的数据，vector和arraylist使用的时间是相同的，都是0(1),这个时候使用vector和arraylist都可以。而如果移动一个指定位置的数据花费的时间为0(n-i)n为总长度，这个时候就应该考虑到使用linklist,因为它移动一个指定位置的数据所花费的时间为0(1),而查询一个指定位置的数据时花费的时间为0(i)。

ArrayList 和Vector是采用数组方式存储数据，此数组元素数大于实际存储的数据以便增加和插入元素，都允许直接序号索引元素，但是插入数据要设计到数组元素移动等内存操作，所以索引数据快插入数据慢，Vector由于使用了synchronized方法（线程安全）所以性能上比ArrayList要差，LinkedList使用双向链表实现存储，按序号索引数据需要进行向前或向后遍历，但是插入数据时只需要记录本项的前后项即可，所以插入数度较快！

### Aarraylist和Linkedlist

1. ArrayList是实现了基于动态数组的数据结构，LinkedList基于链表的数据结构。
2. 对于随机访问get和set，ArrayList觉得优于LinkedList，因为LinkedList要移动指针。
3. 对于新增和删除操作add和remove，LinedList比较占优势，因为ArrayList要移动数据。这一点要看实际情况的。若只对单条数据插入或删除，ArrayList的速度反而优于LinkedList。但若是批量随机的插入删除数据，LinkedList的速度大大优于ArrayList. 因为ArrayList每插入一条数据，要移动插入点及之后的所有数据。

### HashMap与TreeMap

1. HashMap通过hashcode对其内容进行快速查找，而TreeMap中所有的元素都保持着某种固定的顺序，如果你需要得到一个有序的结果你就应该使用TreeMap（HashMap中元素的排列顺序是不固定的）。
2. 集合框架”提供两种常规的Map实现：HashMap和TreeMap (TreeMap实现SortedMap接口)。
3. 在Map 中插入、删除和定位元素，HashMap 是最好的选择。但如果您要按自然顺序或自定义顺序遍历键，那么TreeMap会更好。使用HashMap要求添加的键类明确定义了hashCode()和 equals()的实现。 这个TreeMap没有调优选项，因为该树总处于平衡状态。

### Hashtable与HashMap

1. 历史原因:Hashtable是基于陈旧的Dictionary类的，HashMap是Java 1.2引进的Map接口的一个实现 。
2. 同步性:Hashtable是线程安全的，也就是说是同步的，而HashMap是线程序不安全的，不是同步的。
3. 值：只有HashMap可以让你将空值作为一个表的条目的key或value。

## 对集合的选择

### 对List的选择

1. 对于随机查询与迭代遍历操作，数组比所有的容器都要快。所以在随机访问中一般使用ArrayList
2. LinkedList使用双向链表对元素的增加和删除提供了非常好的支持，而ArrayList执行增加和删除元素需要进行元素位移。
3. 对于Vector而已，我们一般都是避免使用。
4. 将ArrayList当做首选，毕竟对于集合元素而已我们都是进行遍历，只有当程序的性能因为List的频繁插入和删除而降低时，再考虑LinkedList。

### 对Set的选择

1. HashSet由于使用HashCode实现，所以在某种程度上来说它的性能永远比TreeSet要好，尤其是进行增加和查找操作。
2. 虽然TreeSet没有HashSet性能好，但是由于它可以维持元素的排序，所以它还是存在用武之地的。

### 对Map的选择

1. HashMap与HashSet同样，支持快速查询。虽然Hashtable速度的速度也不慢，但是在HashMap面前还是稍微慢了些，所以HashMap在查询方面可以取代Hashtable。
2. 由于TreeMap需要维持内部元素的顺序，所以它通常要比HashMap和Hashtable慢。

## `java.util.Collections`

就像有专门的java.util.Arrays来处理数组，Java中对集合也有java.util.Collections来处理。

第一组方法主要返回集合的各种数据：

- Collections.checkedCollection / checkedList / checkedMap / checkedSet / checkedSortedMap / checkedSortedSet：检查要添加的元素的类型并返回结果。任何尝试添加非法类型的变量都会抛出一个ClassCastException异常。这个功能可以防止在运行的时候出错。
- Collections.emptyList / emptyMap / emptySet ：返回一个固定的空集合，不能添加任何元素。
- Collections.singleton / singletonList / singletonMap：返回一个只有一个入口的 set/list/map 集合。
- Collections.synchronizedCollection / synchronizedList / synchronizedMap / synchronizedSet / synchronizedSortedMap / synchronizedSortedSet：获得集合的线程安全版本（多线程操作时开销低但不高效，而且不支持类似put或update这样的复合操作）
- Collections.unmodifiableCollection / unmodifiableList / unmodifiableMap / unmodifiableSet / unmodifiableSortedMap / unmodifiableSortedSet：返回一个不可变的集合。当一个不可变对象中包含集合的时候，可以使用此方法。

第二组方法中，其中有一些方法因为某些原因没有加入到集合中：

- Collections.addAll：添加一些元素或者一个数组的内容到集合中。
- Collections.binarySearch：和数组的Arrays.binarySearch功能相同。
- Collections.disjoint：检查两个集合是不是没有相同的元素。
- Collections.fill：用一个指定的值代替集合中的所有元素。
- Collections.frequency：集合中有多少元素是和给定元素相同的。
- Collections.indexOfSubList / lastIndexOfSubList：和String.indexOf(String) / lastIndexOf(String)方法类似——找出给定的List中第一个出现或者最后一个出现的子表。
- Collections.max / min：找出基于自然顺序或者比较器排序的集合中，最大的或者最小的元素。
- Collections.replaceAll：将集合中的某一元素替换成另一个元素。
- Collections.reverse：颠倒排列元素在集合中的顺序。如果你要在排序之后使用这个方法的话，在列表排序时，最好使用Collections.reverseOrder比较器。
- Collections.rotate：根据给定的距离旋转元素。
- Collections.shuffle：随机排放List集合中的节点，可以给定你自己的生成器——例如 java.util.Random / java.util.ThreadLocalRandom or java.security.SecureRandom。
- Collections.sort：将集合按照自然顺序或者给定的顺序排序。
- Collections.swap：交换集合中两个元素的位置（多数开发者都是自己实现这个操作的）。
