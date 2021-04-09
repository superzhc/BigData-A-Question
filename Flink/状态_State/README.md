# State

有状态的计算是流处理框架要实现的重要功能，因为稍复杂的流处理场景都需要记录状态，然后在新流入数据的基础上不断更新状态。下面的几个场景都需要使用流处理的状态功能：

- 数据流中的数据有重复，想对重复数据去重，需要记录哪些数据已经流入过应用，当新数据流入时，根据已流入过的数据来判断去重。
- 检查输入流是否符合某个特定的模式，需要将之前流入的元素以状态的形式缓存下来。比如，判断一个温度传感器数据流中的温度是否在持续上升。
- 对一个时间窗口内的数据进行聚合分析，分析一个小时内某项指标的 75 分位或 99 分位的数值。
- 在线机器学习场景下，需要根据新流入数据不断更新机器学习的模型参数。

## Flink 的状态分类

### Managed State 和 Raw State

Flink 有两种基本类型的状态：托管状态（Managed State）和原生状态（Raw State）：

|              | Managed State                                    | Raw State          |
| :----------: | ------------------------------------------------ | ------------------ |
| 状态管理方式 | Flink Runtime 托管，自动存储，自动恢复，自动伸缩 | 用户自己管理       |
| 状态数据结构 | Flink 提供的常用数据结构，如 ListState、MapState | 字节数组：`byte[]` |
|   使用场景   | 绝大多数 Flink 算子                              | 用户自定义算子     |

所有数据流函数都可以使用托管状态（Managed State），而原始状态（Raw State）只能在具体实现算子时使用。建议使用托管状态（而不是原始状态），因为在托管状态下，Flink 能够在并行度改变时自适应地重新分配 State，并且在内存管理方面可以做的更好。

### Keyed State 和 Operator State

**Keyed State**

Keyed State，顾名思义就是基于 KeyedStream 上的状态，这个状态是跟特定的 Key 绑定的。KeyedStream 流上的每一个 Key，都对应一个 State。

**Operator State**

Operator State 与 Key 无关，而是与 Operator 绑定，整个 Operator 只对应一个 State。

Flink 针对 Operator State 提供了以下可以保存 State 的数据结构。

```java
ListState<T>
```