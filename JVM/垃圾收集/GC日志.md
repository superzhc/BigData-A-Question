# GC 日志

> JVM 提供了 `-XX:+PrintGCDetails` 这个收集器日志参数，告诉虚拟机在发生垃圾收集行为时打印内存回收日志，并且在进程退出的时候输出当前的内存各区域分配情况。
>

每一种收集器的日志形式都是由它们自身的实现所决定的，换而言之，每个收集器的日志格式都可以不一样。但虚拟机设计者为了方便用户阅读，将各个收集器的日志都维持一定的共性。

示例：

```txt
[GC (System.gc()) [PSYoungGen: 4465K->792K(33280K)] 4465K->800K(110080K), 0.0073845 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[Full GC (System.gc()) [PSYoungGen: 792K->0K(33280K)] [ParOldGen: 8K->608K(76800K)] 800K->608K(110080K), [Metaspace: 3223K->3223K(1056768K)], 0.0062587 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
```

GC 日志开头的 `[GC` 和 `[Full GC` 说明了这次垃圾收集的停顿类型，用来区分是 Minor GC 还是 Full GC 的标志(Flag)，GC 表明发生的是 Minor GC。括号里面的 `System.gc()` 是引起垃圾回收的原因。

接下来的 `[PSYoungGen`、`[ParOldGen` 表示 GC 发生的区域，这里显示的区域名称与使用的 GC 收集器是密切相关的，例如上面的示例所使用的 Parallel Scavenge 收集器的新生代显示的是 `[PSYoungGen`。

> 各种垃圾收集器对应的显示内容，如下：
>
> - Serial 收集器 ->`DefNew`
> - ParNew 收集器 ->`ParNew`
> - Parallel Scavenge  收集器 ->`PSYoungGen`
> - Serial Old 收集器 ->`Tenured`
> - Parallel Old 收集器 ->`ParOldGen`
> - CMS 收集器 ->`CMS`、`CMS...`
> - G1 收集器 ->``

后面方括号内部的 `4465K->792K(33280K)` 含义是 `GC前该内存区域已使用容量->GC后该内存区域已使用容量(该内存区域的总容量)`，而在方括号之外的 `4465K->792K(33280K)` 表示 `GC前Java堆已使用的容量->GC后Java堆已使用容量(Java堆总容量)`。

再往后，`0.0073845 secs` 表示该内存区域 GC 所占用的时间，单位是秒。有的收集器会给出更具体的时间数据，如 `[Times: user=0.00 sys=0.00, real=0.01 secs]`，这里面的 user、sys 和 real 与 Linux 的 time 命令所输出的时间含义一致，分别代表用户态消耗的 CPU 时间、内核态消耗的 CPU 事件和操作从开始到结束所经历的墙钟时间（Wall Clock Time）。CPU 时间与墙钟时间的区别是，墙钟时间包括各种非运算的等待耗时，例如等待磁盘 I/O、等待线程阻塞，而 CPU 时间不包括这些耗时，但当系统有多 CPU 或者多核的话，多线程操作会叠加这些 CPU 时间，所以 user 或 sys 时间超过 real 时间是完全正常的。