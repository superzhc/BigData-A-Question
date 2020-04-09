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
> |   `-permstat`    | 以 ClassLoader 为统计口径显示永久代内存状态。                |
> |       `-F`       | 当虚拟机进程对 -dump 选项没有响应时，可使用这个选项强制生成 dump 快照。 |
>

