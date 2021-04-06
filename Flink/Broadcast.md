# Broadcast

Broadcast 在 DataStream 和 DataSet 中有区别的，DataStream 使用是广播状态，在 DataSet 中使用是广播变量。

## 广播变量

广播变量允许编程人员在每台机器上保持一个**只读**的缓存变量，而不是传送变量的副本给 Task。广播变量创建后，它可以运行在集群中的任何 Function 上，而不需要多次传递给集群节点。另外请记住，不要修改广播变量，这样才能确保每个节点获取到的值都是一致的。

用一句话解释，Broadcast 可以理解为一个公共的共享变量。可以把一个 DataSet（数据集）广播出去，不同的 Task 在节点上都能够获取到它，这个数据集在每个节点上只会存在一份。如果不使用 Broadcast，则在各节点的每个 Task 中都需要复制一份 DataSet 数据集，比较浪费内存（也就是一个节点中可能会存在多份 DataSet 数据）。

Broadcast 的使用步骤如下：

1. 初始化数据
   ```java
   DataSet<Integer> toBroadcast=env.fromElements(1,2,3);
   ```
2. 广播数据
   ```java
   .withBroadcastSet(toBroadcast,"broadcastSetName");
   ```
3. 获取变量
   ```java
   Collection<Integer> broadcastSet=getRuntimeContext().getBroadcastVariable("broadcastSetName");
   ```

在使用 Broadcast 的时候需要注意以下事项：

- 广播变量存在于每个节点的内存中，它的数据量不能太大，因为广播出去的数据常驻内存，除非程序执行结束。
- 广播变量在初始化广播以后不支持修改，这样才能保证每个节点的数据都是一致的。
- 如果多个算子需要使用同一份数据集，那么需要在对应的多个算子后面分别注册广播变量。

**示例**

```java

```

## 广播状态

Flink 在 1.5 版本引入了广播状态（broadcast State），可以视为 Operator State 的一种特殊情况。它能够将一个流中的数据（通常是较少量的数据）广播到下游算子的所有并发实例中，实现真正的低延迟动态更新。

Flink 直接使用了 MapStateDescriptor 作为广播的状态描述符，方便存储多种不同的广播数据。示例：

```java
MapStateDescriptor<String, String> broadcastStateDesc = new MapStateDescriptor<>(
   "broadcast-state-desc",
   String.class,       // 广播数据的key类型
   String.class        // 广播数据的value类型
);
```

接下来在广播流 controlStream 上调用 `broadcast()` 方法，将它转换成广播流 BroadcastStream。

```java
BroadcastStream<String> broadcastStream = controlStream
  .setParallelism(1)
  .broadcast(broadcastStateDesc);
```

然后在 DataStream 上调用 `connect()` 方法，将它与广播流连接起来，生成 BroadcastConnectedStream。

```java
BroadcastConnectedStream<String, String> connectedStream = sourceStream.connect(broadcastStream);
```

最后就要调用 `process()` 方法对连接起来的流进行处理了。如果 DataStream 是一个普通的流，需要定义BroadcastProcessFunction，反之，如果该 DataStream 是一个 KeyedStream，就需要定义 KeyedBroadcastProcessFunction。并且与之前常见的 ProcessFunction 不同的是，它们都多了一个专门处理广播数据的方法 `processBroadcastElement()`。

```java
connectedStream.process(new BroadcastProcessFunction<String, String, String>() {
      private static final long serialVersionUID = 1L;

      @Override
      public void processElement(String value, ReadOnlyContext ctx, Collector<String> out) throws Exception {
        ReadOnlyBroadcastState<String, String> state = ctx.getBroadcastState(broadcastStateDesc);
        for (Entry<String, String> entry : state.immutableEntries()) {
          String bKey = entry.getKey();
          String bValue = entry.getValue();
          // 根据广播数据进行原数据流的各种处理
        }
        out.collect(value);
      }

      @Override
      public void processBroadcastElement(String value, Context ctx, Collector<String> out) throws Exception {
        BroadcastState<String, String> state = ctx.getBroadcastState(broadcastStateDesc);
        // 如果需要的话，对广播数据进行转换，最后写入状态
        state.put("some_key", value);
      }
    });
```
