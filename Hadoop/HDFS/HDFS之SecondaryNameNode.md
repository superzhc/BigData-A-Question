# SecondaryNameNode

需要注意，SecondaryNameNode 并不是 NameNode 的备份。所有HDFS文件的元信息都保存在 NameNode 的内存中。在 NameNode 启动时，它首先会加载 `fsimage` 到内存中，在系统运行期间，所有对 NameNode 的操作也都保存在了内存中，同时为了防止数据丢失，这些操作又会不断被持久化到本地 `edits` 文件中。

`edits` 文件存在的**目的是为了提高系统的操作效率**，NameNode 在更新内存中的元信息之前都会先将操作写入 `edits` 文件。在NameNode 重启的过程中，`edits` 会和 `fsimage` 合并到一起，但是合并的过程会影响到 Hadoop 重启的速度，SecondaryNameNode 就是为了解决这个问题而诞生的。

SecondaryNameNode 的角色就是**定期的合并 `edits` 和 `fsimage` 文件**，合并的步骤：

1. 合并之前告知 NameNode 把所有的操作写到新的 `edits` 文件并将其命名为 `edits.new`。
2. SecondaryNameNode 从 NameNode 请求 `fsimage` 和 `edits` 文件
3. SecondaryNameNode 把 `fsimage` 和 `edits` 文件合并成新的 `fsimage` 文件
4. NameNode 从 SecondaryNameNode 获取合并好的新的 `fsimage` 并将旧的替换掉，并把 `edits` 用第一步创建的 `edits.new` 文件替换掉
5. 更新 `fstime` 文件中的检查点

最后再总结一下整个过程中涉及到 NameNode 中的相关文件

- `fsimage` ：保存的是上个检查点的 HDFS 的元信息
- `edits` ：保存的是从上个检查点开始发生的 HDFS 元信息状态改变信息
- `fstime`：保存了最后一个检查点的时间戳



