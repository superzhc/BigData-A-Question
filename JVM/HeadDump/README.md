# Heap Dump

> Heap Dump 文件是一个二进制文件，它保存了某一时刻 JVM 堆中对象的使用情况，是指定时刻的 Java 堆栈的快照，是一种镜像文件。

## 生成 Head Dump 文件

### 使用 jmap 命令生成

```sh
jmap -dump:live,format=b,file=<文件地址，如：./heap.hprof> <pid>
```

## 使用 jconsole 生成

可以通过下图的 dumpHeap 按钮生成 Head Dump 文件

![img](D:\superz\BigData-A-Question\JVM\HeadDump\images\11196780-581da91751fb4477.webp)

### 内存溢出自动生成 Heap Dump 文件

在启动 Java 应用程序时，添加参数 `-XX:+HeapDumpOnOutOfMemoryEr

## Heap Dump 文件分析工具

### jhat

jhat 是 JDK 自带的用于分析 JVM Heap Dump 文件的工具，使用下面的命令可以将堆文件的分析结果以 HTML 网页的形式进行展示：

```sh
jhat <heap-dump-file>
```

其中 heap-dump-file 是文件的路径和文件名，可以使用 `-Xms512m` 参数这只命令的内存大小。

### Eclipse Memory Analyzer（MAT）

Eclipse Memory Analyzer(MAT) 是 Eclipse 提供的一款用于 Heap Dump 分析的工具，用来辅助发现内存泄漏减少内存占用，从数以百万计的对象中快速计算出对象的 Retained Size，查看并自动生成一个 Leak Suspect（内存泄露可疑点）报表。

### IBM Heap Analyzer

[IBM Heap Analyzer](https://www.ibm.com/developerworks/community/alphaworks/tech/heapanalyzer) 是IBM公司推出的一款用于分析Heap Dump信息的工具，下载之后是一个jar文件，执行结果如下：
![img](D:\superz\BigData-A-Question\JVM\HeadDump\images\9ab49abb09fb99a66bf2612989e630c2.png)