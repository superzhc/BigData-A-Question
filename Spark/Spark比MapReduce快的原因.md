1. **Spark基于内存迭代，而MapReduce基于磁盘迭代**
   - MapReduce的设计：中间结果保存到文件，可以提高可靠性，减少内存占用，但是牺牲了性能。
   - Spark的设计：数据在内存中进行交换，要快一些，但是内存这个东西，可靠性比不过MapReduce。
2. **DAG计算模型在迭代计算上比MR的更有效率**
   - 在图论中，如果一个有向图无法从某个顶点出发经过若干条边回到该点，则这个图是一个**有向无环图**（DAG）
   - Spark 计算比 MapReduce 快的根本原因在于 DAG 计算模型。一般而言，DAG 相比 MapReduce 在大多数情况下可以减少shuffle 次数。Spark 的 DAGScheduler 相当于一个改进版的 MapReduce，如果计算不涉及与其他节点进行数据交换，Spark可以在内存中一次性完成这些操作，也就是中间结果无须落盘，减少了磁盘IO的操作。但是，如果计算过程中涉及数据交换，Spark也是会把shuffle的数据写磁盘的！有一个误区，Spark是基于内存的计算，所以快，这不是主要原因，要对数据做计算，必然得加载到内存，Hadoop也是如此，只不过Spark支持将需要反复用到的数据给Cache到内存中，减少数据加载耗时，所以Spark跑机器学习算法比较在行（需要对数据进行反复迭代）。Spark基于磁盘的计算也是比Hadoop快。刚刚提到了Spark的DAGScheduler是个改进版的MapReduce，所以Spark天生适合做批处理的任务。Hadoop的MapReduce虽然不如spark性能好，但是HDFS仍然是业界的大数据存储标准。
3. **Spark是粗粒度的资源调度，而MR是细粒度的资源调度**