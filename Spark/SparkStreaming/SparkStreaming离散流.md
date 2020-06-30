# Spark Streaming离散流

离散流或者 DStreams 是 Spark Streaming 提供的基本的抽象，它代表一个连续的数据流。它要么是从源中获取的输入流，要么是输入流通过转换算子生成的处理后的数据流。

在内部，DStreams 由一系列连续的 RDD 组成。

DStreams 中的每个 RDD 都包含确定时间间隔内的数据，如下图所示：

![DStreams](images/2015-08-16_55d04e98b664c.png)

任何对 DStreams 的操作都转换成了对 DStreams 隐含的 RDD 的操作。