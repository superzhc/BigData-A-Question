![img](D:\superz\BigData-A-Question\JVM\images\16fff2fc2a7504d2)

Java 堆是 JVM 所管理的内存中最大的一块，也是垃圾收集器的管理区域。大多数垃圾收集器都会将堆内存划分为上图所示的几个区域，**整体分为新生代和老年代，比例为 1 : 2，新生代又进一步分为 Eden、From Survivor 和 To Survivor，默认比例为 8 : 1 : 1**，请注意，可通过 SurvivorRatio 参数进行设置。请注意，从 JDK 8 开始，JVM 中已经不再有永久代的概念了，Java 堆上的无论哪个区域，存储的都只能是对象的实例，将Java 堆细分的目的只是为了更好地回收内存，或者更快地分配内存。