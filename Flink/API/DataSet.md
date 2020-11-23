# DataSet API

DataSet API 主要可以分为 3 块来分析：DataSource、Transformation 和 Sink。

- DataSource 是程序的数据源输入。
- Transformation 是具体的操作，它对一个或多个输入数据源进行计算处理，比如 map、flatMap、filter 等操作。
- Sink 是程序的输出，它可以把 Transformation 处理之后的数据输出到指定的存储介质中。

## DataSource

对 DataSet 批处理而言，较频繁的操作是读取 HDFS 中的文件数据，因此这里主要介绍两个 DataSource 组件：

1. 基于集合
   ```java
   //主要是为了方便测试使用
   env.fromCollection(Collection)
   ```
2. 基于文件
   ```java
   //基于HDFS中的数据进行计算分析
   env.readTextFile(path)
   ```

## Transformation

Flink 针对 DataSet 提供了大量的已经实现的算子。

### map

> 输入一个元素，然后返回一个元素，中间可以进行清洗转换等操作。

### flatMap

> 输入一个元素，可以返回零个、一个或者多个元素。

### mapPartition

> 类似 map，一次处理一个分区的数据（如果在进行 map 处理的时候需要获取第三方资源连接，建议使用 mapPartition）。

### filter

> 过滤函数，对传入的数据进行判断，符合条件的数据会被留下。

### reduce

> 对数据进行聚合操作，结合当前元素和上一次 reduce 返回的值进行聚合操作，然后返回一个新的值。

### aggregations

> sum、max、min等。

### distinct

> 返回一个数据集中去重之后的元素。

### join

> 内连接。

### outerJoin

> 外链接。

### cross

> 获取两个数据集的笛卡尔积。

### union

> 返回两个数据集的总和，数据类型需要一致。

### first-n

> 获取集合中的前N个元素。

### Sort Partition

> 在本地对数据集的所有分区进行排序，通过 `sortPartition()` 的链接调用来完成对多个字段的排序。

### 数据分区算子

Flink针对DataSet提供了一些数据分区规则，具体如下：

- Rebalance：对数据集进行再平衡、重分区以及消除数据倾斜操作。
- Hash-Partition：根据指定Key的散列值对数据集进行分区。
  ```java
  partitionByHash()
  ```
- Range-Partition：根据指定的Key对数据集进行范围分区。
  ```java
  partitionByRange()
  ```
- Custom Partitioning：自定义分区规则，自定义分区需要实现 Partitioner 接口
  ```java
  dss.partitionCustom(partitioner,"someKey");
  // 或者
  dss.partitionCustom(partitioner,0);
  ```

## Sink

Flink 针对 DataSet 提供了大量的已经实现的 Sink

### writeAsText

> 将元素以字符串形式逐行写入，这些字符串通过调用每个元素的 `toString()` 方法来获取。

### writeAsCsv

> 将元组以逗号分隔写入文件中，行及字段之间的分隔是可配置的，每个字段的值来自对象的toString()方法。

### print

> 打印每个元素的 `toString()` 方法的值到标准输出或者标准错误输出流中。