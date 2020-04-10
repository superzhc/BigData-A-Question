[toc]

# jmap：Java 内存映像工具

> **jmap(Memory Map for Java)命令用于生成堆转储快照（一般称为 heapdump 或 dump 文件）**。
>
> jmap 的作用并不仅仅是为了获取 dump 文件，它还可以查询 finalize 执行队列、Java 堆和永久代的详细信息，如空间使用率、当前用的是哪种收集器等。
>
> 和 jinfo 命令一样，jmap 有不少功能在 Windows 平台下都是受限的，除了生成 dump 文件的 -dump 选项和用于查看每个类的实例、空间占用统计的 -histo 选项在所有操作系统都提供之外，其余选项只能在 Linux 下使用。
>
> jmap 命令格式：
>
> ```sh
> jmap [option] <pid>
> ```
>
> 选项：
>
> |       选项       | 作用                                                         |
> | :--------------: | ------------------------------------------------------------ |
> |     `-dump`      | 生成 Java 堆转储快照。格式为 `-dump:[live,]format=b,file=<filename>`，其中 live 子参数说明是否只 dump 出存活的对象。 |
> | `-finalizerinfo` | 显示在 `F-Queue` 中等待 Finalizer 线程执行 finalize 方法的对象。 |
> |     `-heap`      | 显示 Java 堆详细信息，如使用哪种收集器、参数配置、分代状况等。 |
> |     `-histo`     | 显示堆中对象统计信息，包括类、实例数量、合计容量。           |
> | ~~`-permstat`~~  | ~~用来打印 Java 堆内存的永久保存区域的类加载器的智能统计信息~~ |
> |    `-clstats`    | `-clstats` 是 `-permstat` 的替代方案。打印类加载信息         |
> |       `-F`       | 当虚拟机进程对 -dump 选项没有响应时，可使用这个选项强制生成 dump 快照。 |
>

## 实战

### 示例 1：`jmap <pid>`

> 查看进程的内存映像信息

使用不带选项参数的 jmap 打印共享对象映射，将会打印目标虚拟机中加载的每个共享对象的起始地址、映射大小以及共享对象文件的路径全称。

![image-20200410095357244](D:\superz\BigData-A-Question\JVM\工具\images\image-20200410095357244.png)

### 示例 2：`jmap -heap <pid>`

> 显示 Java 堆详细信息

打印一个堆的摘要信息，包括 GC 算法、堆配置信息和各内存区域内存使用信息

![image-20200410095721730](D:\superz\BigData-A-Question\JVM\工具\images\image-20200410095721730.png)

### 示例 3：`jmap -histo[:live] <pid>`

> 显示堆中对象的统计信息，其中包括每个 Java 类、对象数量、内存大小（单位字节）、类的完全限定名。

`live` 子选项表示只计算活动的对象。

![image-20200410100514363](D:\superz\BigData-A-Question\JVM\工具\images\image-20200410100514363.png)

### 示例 4：`jmap -clstats <pid>`

> 打印类加载器信息

![image-20200410101226089](D:\superz\BigData-A-Question\JVM\工具\images\image-20200410101226089.png)

### 示例 5：`jmap -finalizerinfo <pid>`

> 打印等待终结的对象信息

![image-20200410101635973](D:\superz\BigData-A-Question\JVM\工具\images\image-20200410101635973.png)

`Number of objects pending for finalization: 0` 说明当前 F-QUEUE 队列中并没有等待 Fializer 线程执行 finalize 函数

### 示例 6：`jmap -dump:format=b,file=<文件地址> <pid>`

> 生成堆转储快照dump文件

