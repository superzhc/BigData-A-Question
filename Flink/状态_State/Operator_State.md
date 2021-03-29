# Operator State

每个 Operator state（non-keyed state） 都绑定到一个并行的算子实例上。

## 托管的 Operator State

Operator state 的数据结构不像 Keyed state 丰富，只支持 List，可以认为是可序列化对象的列表，彼此独立。这些对象在动态扩展时是可以重新分配 non-keyed state 的最小单元。目前支持几种动态扩展方式：

- Even-split redistribution：算子并发度发生改变的时候，并发的每个实例取出 State 列表，合并到一个新的列表上，形成逻辑上完整的 State。然后根据列表元素的个数，均匀分配给新的并发实例（Task）。
    例如，如果并行度为 1，算子的 State checkpoint 包含数据元 element1 和 element2，当并行度增加到 2 时，element1 会在算子实例 0 中，而 element2 在算子实例 1 中。
- Union redistribution：相比于平均分配更加灵活，把完整 State 划分的方式交给用户去做。并发度发生改变的时候，按同样的方式取到完整的 State 列表，然后直接交给每个实例。

使用托管的 Operator State，有状态函数需要实现 CheckpointedFunction 接口（更通用的）或 `ListCheckpointed<T extends Serializable>` 接口。