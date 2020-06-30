**用户提交的每个应用程序均包含一个AM**，主要功能包括：

- 与RM调度器协商以获取资源（用Container表示）
- 将得到的任务进一步分配给内部的任务
- 与NM通信以启动/停止任务
- 监控所有任务运行状态，并在任务运行失败时重新为任务申请资源以重启任务

每个应用程序的ApplicationMaster都是一个引导进程，一旦应用程序的提交通过了，且自身加载完成，它就开始启动所有工作。

一旦应用程序被提交后，应用程序在ResourceManager中的~~代表~~代理将申请一个Container来启动该引导进程。

![ApplicationMaster与YARN交互](https://gitee.com/superzchao/GraphBed/raw/master/1576034834_20191211112702465_29112.png)

上述的过程以应用程序提交一个请求到ResourceManager开始。接着，ApplicationMaster启动，向ResourceManager注册。ApplicationMaster向ResourceManager请求Container执行实际的工作。将分配的Container告知NodeManager以便ApplicationMaster使用。计算过程在Container中执行，这些Container将与ApplicationMaster（~~不是ResourceManager~~）保持通信，并告知任务过程。当程序完成后，Container被停止，ApplicationMaster从ResourceManager中注销。

当前YARN自带两个AM实现，一个是用于演示AM编写方法的实例程序distributeshell，它可以申请一定数目的Container以并行运行一个shell命令或者shell脚本；另一个是运行MapReduce应用程序的AM-MRAppMaster。














