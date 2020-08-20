# JVM 故障分析及性能优化

在故障定位(尤其是 OutOfMemory)和性能分析的时候，经常会用到一些文件来帮助我们排除代码问题。这些文件记录了 JVM 运行期间的内存占用、线程执行等情况，这就是我们常说的 Dump 文件。常用的有 Heap Dump和Thread Dump（也叫 javacore，或 java dump）。可以这么理解：Heap Dump记录内存信息的，Thread Dump是记录 CPU 信息的。

- Thread Dump：主要记录 JVM 在某一时刻各个线程执行的情况，以栈的形式显示，是一个文本文件。通过对 Thread Dump 文件可以分析出程序的问题出现在什么地方，从而定位具体的代码然后进行修正。Thread Dump 需要结合占用系统资源的线程 ID 进行分析才有意义
- Heap Dump：主要记录了在某一时刻 JVM 堆中对象使用情况，即某个时刻 JVM 堆的快照，是一个二进制文件，主要用于分析哪些对象占用了太大的堆空间，从而发现导致内存泄漏的对象

上面两种 Dump 文件都具有实时性，因此需要在服务器出现问题的时候生成，并且多生成几个文件，方便进行对比分析。

[Heap Dump 使用](JVM/HeadDump/README.md)

[Thread Dump 使用](JVM/ThreadDump/README.md)