<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2019-10-08 17:26:38
 * @LastEditTime : 2021-02-05 16:46:21
 * @Copyright 2021 SUPERZHC
-->
# RDD 操作

RDD支持两种操作：

- 转换（Transformation）：即从现有地数据集创建一个新的数据集
- 行动（Action）：即在数据集上进行计算后，返回一个值给Driver程序

## 转换操作

对应RDD而言，每一次转换操作都会产生不同的RDD，供给下一个“转换”使用。转换得到的RDD是惰性求值，也就是说，整个转换过程只是记录了转换的轨迹，并不会发生真正的计算，只有遇到行动操作时，才会发生真正的计算，开始从血缘关系源头开始，进行物理的转换操作。

下面列出一些常见的转换操作（Transformation API）：

| 操作                           | 描述                                                                                                            |
| ------------------------------ | --------------------------------------------------------------------------------------------------------------- |
| `filter(func)`                 | 筛选出满足函数func的元素，并返回一个新的数据集                                                                  |
| `map(func)`                    | 将每个元素传递给函数func中，并将结果返回给一个新的数据集                                                        |
| `flatMap(func)`                | 与`map()`相似，但每个输入元素都可以映射到0或多个输出结果                                                        |
| `union(otherDataset)`          | 由源数据集和参数数据集联合返回一个新的数据集                                                                    |
| `distinct([numTasks])`         | 返回一个包含数据源集中所有不重复元素的新数据集                                                                  |
| `groupByKey([numTasks])`       | 应用于`(K,V)`键值对的数据集时，返回一个新的`(K,Iterable)`形式的数据集                                           |
| `reduceByKey(func[,numTasks])` | 应用于`(K,V)`键值对的数据集时，返回一个新的`(K,V)`形式的数据集，其中的每个值是将每个key传递到函数func中进行聚合 |

## 行动操作

行动操作是真正触发计算的地方。Spark程序执行到行动操作时，才会执行真正的计算，从文件中加载数据，完成一次又一次的转换操作，最终完成行动操作得到结果。

下面列出一些常见的行动操作（Action API）：

| 操作                       | 描述                                                                                                                                                                                                            |
| -------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `count()`                  | 返回数据集中的元素个数                                                                                                                                                                                          |
| `collect()`                | 以数组的形式返回数据集中的所有元素                                                                                                                                                                              |
| `first()`                  | 返回数据集中的第一个元素                                                                                                                                                                                        |
| `take(n)`                  | 以数组的形式返回数据集中的前n个元素                                                                                                                                                                             |
| `reduce(func)`             | 通过函数func（输入两个参数并返回一个值）聚合数据集中的元素                                                                                                                                                      |
| `foreach(func)`            | 将数据集中的每个元素传递到函数func中进行运行                                                                                                                                                                    |
| `saveAsTextFile(path)`     | 将数据集的元素以textfile的形式保存到本地文件系统（HDFS或者任何其他Hadoop支持的文件系统）。对于每个元素，Spark将会调用toString方法，将它转换为文件中的文本行                                                     |
| `saveAsSequenceFile(path)` | 将数据集的元素以Hadoop sequencefile的格式保存到指定的目录下，可以是本地系统、HDFS或者任何其他Hadoop支持的文件系统，这个只限于由key-value对组成，并实现了Hadoop的Writable接口，或者可以隐式地转换为Writable地RDD |
| `countByKey()`             | 应用于`(K,V)`，返回一个`(K,Int)`对的map，表示每一个key对应的元素个数                                                                                                                                            |

## 惰性机制

Spark的惰性机制，转换操作并不会立即执行计算，需要等到行动操作才会真正执行计算