jstack 是 JVM 自带的一种堆栈跟踪工具。jstack 用于打印出给定的 Java 进程 ID 或 core file 或远程调试服务的 Java 堆栈信息。

jstack 用于生成 JVM 当前时刻的线程快照，主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等。线程出现停顿的时候通过 jstack 来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么资源。如果 Java 应用程序崩溃生成 core 文件，jstack 工具可以用来获得 core 文件的 Java Stack 和 Native Stack 的信息，从而可以轻松的知道 Java 应用程序是如何崩溃和在程序何处发生问题。另外，jstack 工具还可以附属到正在运行的 Java 应用程序

