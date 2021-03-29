# Keyed State

Keyed state 与键（key）相关，只能在 KeyedStream 上应用的函数和算子内使用。可以将 Keyed state 认为是分区后的 Operator state，每个 key 有一个状态的分区（state-partition）。逻辑上，每个 Keyed state 绑定唯一的 `<parallel-operator-instatnce, key>`（算子并行实例和 key 的一对元组），可以将其简单地视为 `<operator, key>`（因为每个 key 属于算子的唯一一个并行实例）。

## 托管的 Keyed State

托管 Keys state 提供多种不同类型 State，作用域都是当前输入数据的键，只能用于 KeyedStream，可以通过 `dss.keyBy(…)` 创建。

Flink 针对 Keyed State 提供了以下可以保存 State 的数据结构。

- `ValueState<T>`：保存了一个值，可以更新和读取（算子操作的每个 key 可能有一个 value）
  ```java
  update(T) //更新
  T value() //取值
  ```
- `ListState<T>`：保存了一个列表 List。可以追加元素，也可以获取到一个包含所有当前存储的元素的迭代器（Iterable）
  ```java
  add(T) 或 addAll(List<T>) //添加到列表
  Iterable<T> get()         //获取迭代器
  update(List<T>)           //使用新的列表覆盖现有列表
  ```
- `ReducingState<T>`：保存一个值，表示添加到 State 的所有值的聚合结果。提供的接口类似于 ListState
  ```java
  add(T) //函数会使用指定的函数（ReduceFunction）对添加的值进行聚合
  ```
- `AggregatingState<IN, OUT>`：保存一个值，表示添加到 State 的所有值的聚合结果。与 ReducingState 不同的是，聚合结果的数据类型可以与添加到 State 的元素的数据类型不同。接口同样类似于 ListState
  ```java
  add(IN) //函数会使用指定的函数（AggregateFunction）对添加的值进行聚合
  ```
- ~~`FoldingState<T, ACC>`~~：保存一个值，表示添加到 State 的所有值的聚合结果。与 ReducingState 不同的是，聚合结果的数据类型可以与添加到 State 的元素的数据类型不同。接口同样类似于 ListState。已经过期的API。
  ```java
  add(IN) //函数会使用指定的函数（FoldFunction）对添加的值进行聚合
  ```
- `MapState<UK, UV>`：保存一个映射表 Map。可以将 key/value 存入 State，也可以获取到一个包含所有当前存储的元素的迭代器（Iterable）
  ```java
  put(UK, UV) 或 putAll(Map<UK, UV>) //添加 key/value 到 Map
  get(UK) //获取与指定 key 的 value
  entries()、keys() 和 values() //对 Map 的元素/键/值遍历访问
  ```

> 所有类型的都有 `clear()` 方法来清除当前状态。FoldingState 和 FoldingStateDescriptor 已在 Flink 1.4 中弃用，未来版本将被完全删除。可以使用 AggregatingState 和 AggregatingStateDescriptor 代替。

操作 State 对象，首先必须创建一个 StateDescriptor，该对象拥有 State 的名称，State 所持有的值的类型，可能还有用户指定的函数。对应不同的 State 类型，有如下类对象：`ValueStateDescriptor`、`ListStateDescriptor`、`ReducingStateDescriptor`、~~`FoldingStateDescriptor`~~ 和 `MapStateDescriptor`。然后使用 `RuntimeContext` 可以才访问到 State，因此只能在 `RichFunction` 中使用，在 `RichFunction` 方法中 `RuntimeContext` 访问不通类型 State 的方法：

- `ValueState<T> getState(ValueStateDescriptor<T>)`
- `ReducingState<T> getReducingState(ReducingStateDescriptor<T>)`
- `ListState<T> getListState(ListStateDescriptor<T>)`
- `AggregatingState<IN, OUT> getAggregatingState(AggregatingState<IN, OUT>)`
- ~~`FoldingState<T, ACC> getFoldingState(FoldingStateDescriptor<T, ACC>)`~~
- `MapState<UK, UV> getMapState(MapStateDescriptor<UK, UV>)`

## 状态生存周期（TTL）

生存期（TTL）可以被指定给任何类型的 Keyed state，如果配置了TTL并且 State value 已过期，State value 会被清除。所有集合类型 State （ListState 和 MapState）都支持为每个条目设置TTL。为了启用 State TTL，首先需要构建 StateTtlConfig 对象，然后通过在 ValueStateDescriptor（其他类型同理）构造中传入该对象来启用 TTL，参考下面的例子：

**Scala 示例**

```scala
// Time.seconds(1) 生存时间 
// 
// StateTtlConfig.UpdateType 更新类型
//   - UpdateType.OnCreateAndWrite - 创建和写入时更新（默认）
//   - UpdateType.OnReadAndWrite - 读取和写入时更新
// 
// StateTtlConfig.StateVisibility 状态可见性，访问时是否返回已经过期的值
//   - StateVisibility.NeverReturnExpired - 永远不会返回过期的值（默认）
//   - StateVisibility.ReturnExpiredIfNotCleanedUp -如果可以读到会返回
// 
val ttlConfig = StateTtlConfig
    .newBuilder(Time.seconds(1))
    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) 
    .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
    .build
// 构建 ValueStateDescriptor
val stateDescriptor = new ValueStateDescriptor[String]("text state", classOf[String])
// 启用TTL
stateDescriptor.enableTimeToLive(ttlConfig)
```