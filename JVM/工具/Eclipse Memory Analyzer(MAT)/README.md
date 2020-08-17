# Eclipse Memory Analyzer(MAT)

Eclipse Memory Analyzer（简称MAT）是一个功能丰富且操作简单的JVM Heap Dump分析工具，可以用来辅助发现内存泄漏减少内存占用。
使用 Memory Analyzer 来分析生产环境的 Java 堆转储文件，可以从数以百万计的对象中快速计算出对象的 Retained Size，查看是谁在阻止垃圾回收，并自动生成一个 Leak Suspect（内存泄露可疑点）报表。

### 下载与安装

Eclipse Memory Analyzer（MAT）支持两种安装方式，一是Eclipse插件的方式，另外一个就是独立运行的方式，建议使用独立运行的方式。
在 http://www.eclipse.org/mat/downloads.php 下载安装MAT，启动之后打开 `File - Open Heap Dump...` 菜单，然后选择生成的 Heap DUmp 文件，选择 "Leak Suspects Report"，然后点击 "Finish" 按钮。
![img](images/7ac5ffafea8488aa36df72fd0a8a2df6.png)

### 主界面

第一次打开因为需要分析dump文件，所以需要等待一段时间进行分析，分析完成之后dump文件目录下面的文件信息如下：
![img](images/56ae33d76a59bff56b92de705f3e0fc1.png)
上图中 heap-27311.bin 文件是原始的Heap Dump文件，zip文件是生成的html形式的报告文件。

打开之后，主界面如下所示：
![img](images/47fb011d433ddf4c295f4718adc8b4b5.png)

接下来介绍界面中常用到的功能：
![img](images/843eb07db5728ab786cfbcb016d6809f.png)

### ![img](images/1bd31f8d4331c0cf4467c38b016118cd.png) Overview

Overview视图，即概要界面，显示了概要的信息，并展示了MAT常用的一些功能。

- Details 显示了一些统计信息，包括整个堆内存的大小、类（Class）的数量、对象（Object）的数量、类加载器（Class Loader)的数量。
- Biggest Objects by Retained Size 使用饼图的方式直观地显示了在JVM堆内存中最大的几个对象，当光标移到饼图上的时候会在左边Inspector和Attributes窗口中显示详细的信息。
- Actions 这里显示了几种常用到的操作，算是功能的快捷方式，包括 Histogram、Dominator Tree、Top Consumers、Duplicate Classes，具体的含义和用法见下面；
- Reports 列出了常用的报告信息，包括 Leak Suspects和Top Components，具体的含义和内容见下；
- Step By Step 以向导的方式引导使用功能。

### ![Histogram](images/fe892b246b43fa64b866790a2f256859.png) Histogram

直方图，可以查看每个类的实例（即对象）的数量和大小。

### ![img](images/5931b0544ead20fc50aba5efd828385e.png) Dominator Tree

支配树，列出Heap Dump中处于活跃状态中的最大的几个对象，默认按 retained size进行排序，因此很容易找到占用内存最多的对象。

### ![img](images/1c997cbcaac271fbef1a6768ec1485ed.png) OQL

MAT提供了一个对象查询语言（OQL），跟SQL语言类似，将类当作表、对象当作记录行、成员变量当作表中的字段。通过OQL可以方便快捷的查询一些需要的信息，是一个非常有用的工具。

### ![img](images/b16c2f91008e885eb9fd8aeb93d4bb31.png) Thread Overview

此工具可以查看生成Heap Dump文件的时候线程的运行情况，用于线程的分析。

### ![img](images/5243d42aa462821596a6bc6348d3ead0.png) Run Expert System Test

可以查看分析完成的HTML形式的报告，也可以打开已经产生的分析报告文件，子菜单项如下图所示：
![img](images/22d20d2d807b24c859b795846f587922.png)
常用的主要有Leak Suspects和Top Components两种报告：

- Leak Suspects 可以说是非常常用的报告了，该报告分析了 Heap Dump并尝试找出内存泄漏点，最后在生成的报告中对检测到的可疑点做了详细的说明；
- Top Components 列出占用总堆内存超过1%的对象。

### ![img](images/ea77da743bec2e5c8883a87f8e181a48.png) Open Query Browser

提供了在分析过程中用到的工具，通常都集成在了右键菜单中，在后面具体举例分析的时候会做详细的说明。如下图：
![img](images/c9f7a5b311c473627ddd281f52907e0a.png)

这里仅针对在 Overview 界面中的 Acations中列出的两项进行说明：

- Top Consumers 按类、类加载器和包分别进行查询，并以饼图的方式列出最大的几个对象。菜单打开方式如下：
  ![img](images/5e7a1762e03b51c263cb713f64461135.png)
- Duplicate Classes 列出被加载多次的类，结果按类加载器进行分组，目标是加载同一个类多次被类加载器加载。使用该工具很容易找到部署应用的时候使用了同一个库的多个版本。菜单打开方式如下图：
  ![img](images/cc8cbe62f8e41bb9da95edeba1a25a1b.png)

### ![img](images/8a21e1700c75e4e0acfb6dd2db536f1c.png) Find Object by address

通过十六进制的地址查找对应的对象，见下图：
![img](images/27e4a4532a75590d7e3a66f7101c9240.png)