# State

在 Flink 架构体系中，有状态计算可以说是 Flink 非常重要的特性之一。有状态计算是指在程序计算过程中，在 Flink 程序内部存储计算产生的中间结果，并提供给后续 Function 或算子计算结果使用。

和状态计算不同的是，无状态计算不会存储计算过程中产生的结果，也不会将结果用于下一步计算过程中，程序只会在当前的计算流程中实行计算，计算完成就输出结果，然后下一条数据接入，然后再处理。

无状态计算实现的复杂度相对较低，实现起来较容易，但是无法完成提到的比较复杂的业务场景，例如下面的例子：

- 用户想实现 CEP（复杂事件处理），获取符合某一特定事件规则的事件，状态计算就可以将接入的事件进行存储，然后等待符合规则的事件触发；
- 用户想按照分钟、小时、天进行聚合计算，求取当前的最大值、均值等聚合指标，这就需要利用状态来维护当前计算过程中产生的结果，例如事件的总数、总和以及最大，最小值等；
- 用户想在 Stream 上实现机器学习的模型训练，状态计算可以帮助用户维护当前版本模型使用的参数；
- 用户想使用历史的数据进行计算，状态计算可以帮助用户对数据进行缓存，使用户可以直接从状态中获取相应的历史数据。

## 状态类型

在 Flink 中根据数据集是否根据 Key 进行分区，将状态分为 Keyed State 和 Operator State（Non-keyed State）两种类型。

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