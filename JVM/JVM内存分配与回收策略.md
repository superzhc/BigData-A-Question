# JVM 内存分配与回收策略

## 对象优先在 Eden 分配

大多数情况下，对象在新生代 Eden 区中分配，当 Eden 区没有足够空间进行分配时，虚拟机将发起依次 Minor GC。

## 大对象直接进入老年代

所谓的大对象是指，需要大量连续内存空间的 Java 对象，最典型的大对象就是那种很长字符串以及数组。

虚拟机提供了一个 `-XX:PretenureSizeThreshold` 参数，令大于这个设置值的对象直接在老年代分配。这样做的目的是避免在 Eden 区及两个 Survivor 区之间发生大量的内存复制。

## 长期存活的对象将进入老年代

虚拟机给每个对象定义了一个对象年龄（Age）计数器，如果对象在 Eden 出生并经过第一次 Minor GC 后仍然存活，并且能被 Survivor 容纳的话，将被移动到 Survivor 空间中，并且对象年龄设为 1。对象在 Survivor 区中每经过依次 Minor GC，年龄将增加 1，当它的年龄增加到一定程度（默认为 15），就将会被晋升到老年代。对象晋升老年代的年龄阈值，可以通过参数 `-XX:MaxTenuringThreshold` 设置。

## 动态对象年龄判定

为了能更好的适应不同程序的内存状况，虚拟机并不是永远地要求对象地年龄必须达到了 MaxTenuringThreshold 才能晋升老年代，如果在 Survivor 空间中相同年龄所有对象大小的总和大于 Survivor 空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代，无需等到 MaxTenuringThreshold 中要求的年龄。

## 空间分配担保

在发生 Minor GC 之前，虚拟机会先检查老年代最大可用的连续空间是否大于新生代所有对象的总空间，如果这个条件成立，那么 Minor GC 可以确保是安全的。如果不成立，则虚拟机会查看 HandlePromotionFailure 设置值是否允许担保失败，如果允许，那么会继续检查老年代最大可用的连续空间是否大于历次晋升到老年代对象的平均大小，如果大于，将尝试着进行一次 Minor GC，尽管这次 Minor GC 是有风险的；如果小于，或者 HandlePromotionFailure 设置不允许冒险，那这时也要改为进行一次 Full GC。

当 Eden 区和 From Survivor 区经过 Minor GC 后的内存仍然大于 To Survivor 内存，这就需要老年代进行分配担保，把 To Survivor 无法容纳的对象直接进入老年代。 