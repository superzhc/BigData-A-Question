# Flink State 管理与恢复

在没有状态管理的 Flink Job 中，如果一个Task在处理过程中挂掉了，那么它在内存中的状态都会丢失，所有的数据都需要重新计算。从容错和消息处理的语义（At-least-once 和 Exactly-once）上来说，Flink引入了State和CheckPoint。

这两个概念的区别如下：

- State一般指一个具体的Task/Operator的状态，State数据默认保存在Java的堆内存中。
- CheckPoint（可以理解为CheckPoint是把State数据持久化存储了）则表示了一个Flink Job在一个特定时刻的一份全局状态快照，即包含了所有Task/Operator的状态。

> 注意：Task是Flink中执行的基本单位，Operator是算子（Transformation）。

State可以被记录，在失败的情况下数据还可以恢复。Flink中有以下两种基本类型的State：

- Keyed State
- Operator State

Keyed State和Operator State以两种形式存在：

- 原始状态（Raw State）：由用户自行管理状态具体的数据结构，框架在做CheckPoint的时候，使用byte[]读写状态内容，对其内部数据结构一无所知。
- 托管状态（Managed State）：由Flink框架管理的状态。

通常在DataStream上推荐使用托管状态，当实现一个用户自定义的Operator时使用到原始状态。

## Keyed State

Keyed State，顾名思义就是基于 KeyedStream 上的状态，这个状态是跟特定的 Key 绑定的。KeyedStream 流上的每一个 Key，都对应一个 State。

`dss.keyBy(……)` 这个代码会返回一个 KeyedStream 对象。

Flink 针对 Keyed State 提供了以下可以保存 State 的数据结构。

- `ValueState<T>`：类型为 T 的单值状态，这个状态与对应的 Key 绑定，是最简单的状态。它可以通过 update 方法更新状态值，通过 `value()` 方法获取状态值。
- `ListState<T>`：Key 上的状态值为一个列表，这个列表可以通过 add 方法往列表中附加值，也可以通过 `get()` 方法返回一个 `Iterable<T>` 来遍历状态值。
- `ReducingState<T>`：每次调用 add 方法添加值的时候，会调用用户传入的 reduceFunction，最后合并到一个单一的状态值。
- `MapState<UK, UV>`：状态值为一个 Map，用户通过 put 或 putAll 方法添加元素。

需要注意的是，以上所述的 State 对象，仅仅用于与状态进行交互（更新、删除、清空等），而真正的状态值有可能存在于内存、磁盘或者其他分布式存储系统中，相当于我们只是持有了这个状态的句柄。

## Operator State

Operator State 与 Key 无关，而是与 Operator 绑定，整个 Operator 只对应一个 State。

Flink 针对 Operator State 提供了以下可以保存 State 的数据结构。

```java
ListState<T>
```

## 容错

当程序出现问题需要恢复Sate数据的时候，只有程序提供支持才可以实现State的容错。

State的容错需要依靠CheckPoint机制，这样才可以保证Exactly-once这种语义，但是注意，它只能保证Flink系统内的Exactly-once，比如Flink内置支持的算子。

针对Source和Sink组件，如果想要保证Exactly-once的话，则这些组件本身应支持这种语义。

**State容错中的CheckPoint机制**

*生成快照*

Flink通过CheckPoint机制可以实现对Source中的数据和Task中的State数据进行存储。如下图所示：

![image-20201119092007571](images/image-20201119092007571.png)

*恢复快照*

Flink还可以通过Restore机制来恢复之前CheckPoint快照中保存的Source数据和Task中的State数据。如下图所示：

![image-20201119092049591](images/image-20201119092049591.png)