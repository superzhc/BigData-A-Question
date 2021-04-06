# Operator State

每个 Operator state（non-keyed state） 都绑定到一个并行的算子实例上。

## 托管的 Operator State

Operator state 的数据结构不像 Keyed state 丰富，只支持 List，可以认为是可序列化对象的列表，彼此独立。这些对象在动态扩展时是可以重新分配 non-keyed state 的最小单元。目前支持几种动态扩展方式：

- Even-split redistribution：算子并发度发生改变的时候，并发的每个实例取出 State 列表，合并到一个新的列表上，形成逻辑上完整的 State。然后根据列表元素的个数，均匀分配给新的并发实例（Task）。
    例如，如果并行度为 1，算子的 State checkpoint 包含数据元 element1 和 element2，当并行度增加到 2 时，element1 会在算子实例 0 中，而 element2 在算子实例 1 中。
- Union redistribution：相比于平均分配更加灵活，把完整 State 划分的方式交给用户去做。并发度发生改变的时候，按同样的方式取到完整的 State 列表，然后直接交给每个实例。

使用托管的 Operator State，有状态函数需要实现 CheckpointedFunction 接口（更通用的）或 `ListCheckpointed<T extends Serializable>` 接口。

## 示例

> Flink 中的 Kafka Connector 就使用了 Operate State，它会在每个 Connector 实例中保存该实例消费 Topic 的所有 `(partition,offset)` 映射

**注意**：只保留了 Operate State 相关代码

```java
public abstract class FlinkKafkaConsumerBase<T> extends RichParallelSourceFunction<T> implements CheckpointListener, ResultTypeQueryable<T>, CheckpointedFunction {
    //...

    private Map<KafkaTopicPartition, Long> subscribedPartitionsToStartOffsets;

    //...

    private transient ListState<Tuple2<KafkaTopicPartition, Long>> unionOffsetStates;

    //...

    @Override
    public final void initializeState(FunctionInitializationContext context) throws Exception {

        OperatorStateStore stateStore = context.getOperatorStateStore();

        this.unionOffsetStates =
                stateStore.getUnionListState(
                        new ListStateDescriptor<>(
                                OFFSETS_STATE_NAME,
                                createStateSerializer(getRuntimeContext().getExecutionConfig())));

        if (context.isRestored()) {
            restoredState = new TreeMap<>(new KafkaTopicPartition.Comparator());

            // populate actual holder for restored state
            for (Tuple2<KafkaTopicPartition, Long> kafkaOffset : unionOffsetStates.get()) {
                restoredState.put(kafkaOffset.f0, kafkaOffset.f1);
            }

            LOG.info(
                    "Consumer subtask {} restored state: {}.",
                    getRuntimeContext().getIndexOfThisSubtask(),
                    restoredState);
        } else {
            LOG.info(
                    "Consumer subtask {} has no restore state.",
                    getRuntimeContext().getIndexOfThisSubtask());
        }
    }

    @Override
    public final void snapshotState(FunctionSnapshotContext context) throws Exception {
        if (!running) {
            LOG.debug("snapshotState() called on closed source");
        } else {
            unionOffsetStates.clear();

            final AbstractFetcher<?, ?> fetcher = this.kafkaFetcher;
            if (fetcher == null) {
                // the fetcher has not yet been initialized, which means we need to return the
                // originally restored offsets or the assigned partitions
                for (Map.Entry<KafkaTopicPartition, Long> subscribedPartition :
                        subscribedPartitionsToStartOffsets.entrySet()) {
                    unionOffsetStates.add(
                            Tuple2.of(
                                    subscribedPartition.getKey(), subscribedPartition.getValue()));
                }

                if (offsetCommitMode == OffsetCommitMode.ON_CHECKPOINTS) {
                    // the map cannot be asynchronously updated, because only one checkpoint call
                    // can happen
                    // on this function at a time: either snapshotState() or
                    // notifyCheckpointComplete()
                    pendingOffsetsToCommit.put(context.getCheckpointId(), restoredState);
                }
            } else {
                HashMap<KafkaTopicPartition, Long> currentOffsets = fetcher.snapshotCurrentState();

                if (offsetCommitMode == OffsetCommitMode.ON_CHECKPOINTS) {
                    // the map cannot be asynchronously updated, because only one checkpoint call
                    // can happen
                    // on this function at a time: either snapshotState() or
                    // notifyCheckpointComplete()
                    pendingOffsetsToCommit.put(context.getCheckpointId(), currentOffsets);
                }

                for (Map.Entry<KafkaTopicPartition, Long> kafkaTopicPartitionLongEntry :
                        currentOffsets.entrySet()) {
                    unionOffsetStates.add(
                            Tuple2.of(
                                    kafkaTopicPartitionLongEntry.getKey(),
                                    kafkaTopicPartitionLongEntry.getValue()));
                }
            }

            if (offsetCommitMode == OffsetCommitMode.ON_CHECKPOINTS) {
                // truncate the map of pending offsets to commit, to prevent infinite growth
                while (pendingOffsetsToCommit.size() > MAX_NUM_PENDING_CHECKPOINTS) {
                    pendingOffsetsToCommit.remove(0);
                }
            }
        }
    }
}
```