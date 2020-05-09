## 简介

Lucene最初由鼎鼎大名Doug Cutting开发，2000年开源，现在也是开源全文检索方案的不二选择，它的特点概述起来就是：全Java实现、开源、高性能、功能完整、易拓展，功能完整体现在对分词的支持、各种查询方式（前缀、模糊、正则等）、打分高亮、列式存储（DocValues）等等。

而且Lucene虽已发展10余年，但仍保持着一个活跃的开发度，以适应着日益增长的数据分析需求，最新的6.0版本里引入block k-d trees，全面提升了数字类型和地理位置信息的检索性能，另基于Lucene的Solr和ElasticSearch分布式检索分析系统也发展地如火如荼。

Lucene整体使用如图所示：

![lucene角色](D:\superz\BigData-A-Question\ElasticSearch\Lucene\images\20170103091620316.png)

## 索引原理

全文检索技术由来已久，绝大多数都基于倒排索引来做的。倒排索引相当于一篇文章包含了哪些词，它从词出发，记载了这个词在哪些文档中出现过，由两部分组成——**词典**和**倒排表**。

![这里写图片描述](D:\superz\BigData-A-Question\ElasticSearch\Lucene\images\20170103094805234.png)

其中词典结构尤为重要，有很多种词典结构，各有各的优缺点，最简单如排序数组，通过二分查找来检索数据，更快的有哈希表，磁盘查找有B树、B+树，但一个能支持TB级数据的倒排索引结构需要在时间和空间上有个平衡，下图列了一些常见词典的优缺点：

![这里写图片描述](D:\superz\BigData-A-Question\ElasticSearch\Lucene\images\20170103100752086.png)

Lucene 现在使用的索引结构是 FST 数据结构。

### FST

![FST](D:\superz\BigData-A-Question\ElasticSearch\Lucene\images\20170103104515602.png)

## 索引实现

Lucene经多年演进优化，现在的一个索引文件结构如图所示，基本可以分为三个部分：词典、倒排表、正向文件、列式存储DocValues。

![Lucene索引实现](D:\superz\BigData-A-Question\ElasticSearch\Lucene\images\20170105081126996.png)

### 词典索引结构

TODO

### 倒排表结构

倒排表就是文档号集合，Lucene现使用的倒排表结构叫Frame of reference,它主要有两个特点：

1. 数据压缩
2. 跳跃表加速合并，因为布尔查询时，and 和or 操作都需要合并倒排表，这时就需要快速定位相同文档号，所以利用跳跃表来进行相同文档号查找

### 正向文件

正向文件指的就是原始文档，Lucene对原始文档也提供了存储功能，它存储特点就是分块+压缩。

- fnm中为元信息存放了各列类型、列名、存储方式等信息
- fdt文件就是存放原始文档的文件，它占了索引库90%的磁盘空间
- fdx文件为索引文件，通过文档号（自增数字）快速得到文档位置

### DocValues

虽然倒排索引能够解决从词到文档的快速映射，但需要对检索结果进行分类、排序、数学计算等聚合操作是需要文档号到文档值得快速映射，而原先不管是倒排索引还是行式存储的文档都无法满足要求，因此在4.0后Lucene推出了DocValues来解决这一问题（注：4.0版本之前，用的是一种FieldCache来实现的）。

## 参考

- [Lucene底层原理和优化经验分享(1)-Lucene简介和索引原理](https://blog.csdn.net/njpjsoftdev/article/details/54015485)
- [Lucene底层原理和优化经验分享(2)-Lucene优化经验总结](https://blog.csdn.net/njpjsoftdev/article/details/54133548)