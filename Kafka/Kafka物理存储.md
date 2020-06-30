## 文件目录布局

Kafka 中的消息是以主题为基本单位进行归类的，各个主题在逻辑上相互独立。每个主题又可以分为一个或多个分区，分区的数量可以在主题创建的时候指定，也可以在之后修改。每条消息在发送的时候会根据分区规则被追加到指定的分区中，分区中的每条消息都会被分配一个唯一的序列号，也就是通常所说的偏移量（offset）。

不考虑多副本的情况，一个分区对应一个日志（Log），为了防止 Log 过大，Kafka 又引入了日志分段（LogSegment）的概念，将 Log 切分为多个 LogSegment，相当于一个巨型文件被平均分配为多个相对较小的文件，这样也便于消息的维护和清理。事实上，Log 和 LogSegment 也不是纯粹物理意义上的概念，Log 在物理上只以文件夹的形式存储，而每个 LogSegment 对应于磁盘上的一个日志文件和两个索引文件，以及可能的其他文件（比如以 `.txnindex` 为后缀的事务索引文件），其关系图如下所示：

![1571732230790](../images/1571732230790.png)

向 Log 中追加消息时是顺序写入的，只有最后一个 LogSegment 才能执行写入操作，在此之前所有的 LogSegment 都不能写入数据。为了方便描述，将最后一个 LogSegment 称为 `activeSegment`，即表示当前活跃的日志分段。随着消息的不断写入，当 activeSegment 满足一定的条件时，就需要创建新的 activeSegment，之后追加的消息将写入新的 activeSegment。

为了便于消息的检索，每个 LogSegment 中的日志文件（以 `.log` 为文件后缀）都有对应的两个索引文件：偏移量索引文件（以 `.index` 为文件后缀）和时间戳索引文件（以 `.timeindex` 为文件后缀）。每个 LogSegment 都有一个基准偏移量 baseOffset，用来表示当前 LogSegment 中第一条消息的 offset。偏移量是一个 64 位的长整型数，日志文件和两个索引文件都是根据基准偏移量（baseOffset）命名的，名称固定为 20 位数字，没有达到的位数则用 0 填充。比如第一个 LogSegment 的基准偏移量为 0，对应的日志文件为 00000000000000000000.log。

注：每个 LogSegment 中不只包含 `.log` `.index` `.timeindex` 这 3 中文件，还可能包含 `.deleted` `.cleaned` `.swap` 等临时文件，以及可能的 `.snapshot` `.txnindex` `leader-epoch-checkpoint` 等文件。

## 日志索引

在每个日志分段文件中都对应了两个索引文件，主要用来提高查询消息的效率。偏移量索引文件用来建立消息偏移量（offset）到物理地址之间的映射关系，方便快速定位消息所在的物理文件位置；时间戳索引文件则根据指定的时间戳（timestamp）来查找对应的偏移量信息。

Kafka 中的索引文件以稀疏索引（sparse index）的方式构造消息的索引，它并不保证每个消息在索引文件中都有对应的索引项。每当写入一定量（由 broker 端参数 `log.index.interval.bytes` 指定，默认值为 4096，即 4KB）的消息时，偏移量索引文件和时间戳索引文件分别增加一个偏移量索引项和时间戳索引项，增大或减小 `log.index.interval.bytes` 的值，对应地可以增加或缩小索引项的密度。

稀疏索引通过 MappedByteBuffer 将索引文件映射到内存中，以加快索引的查询速度。偏移量索引文件中偏移量是单调递增的，查询指定偏移量时，使用二分查找法来快速定位偏移量的位置，如果指定的偏移量不在索引文件中，则会返回小于指定偏移量的最大偏移量。时间戳索引文件中的时间戳也保持严格的单调递增，查询指定时间戳时，也根据二分查找法来查找不大于该时间戳的最大偏移量，至于要找到对应的物理文件位置还需要根据偏移量索引文件来进行再次定位。稀疏索引的方式是在磁盘空间、内存空间、查找时间等多方面之间的一个折中。

日志分段文件切分包含以下几个条件，满足其一即可：

1. 当前日志分段文件的大小超过 broker 端参数 `log.segment.bytes` 配置的值。`log.segment.bytes` 参数的默认值为1073741824，即 1G
2. 当前日志分段中消息的最大时间戳与当前系统的时间戳的差值大于 `log.roll.ms` 或 `log.roll.hours` 参数配置的值。如果同时配置了 `log.roll.ms` 和 `log.roll.hours` 参数，那么 `log.roll.ms` 的优先级高，默认情况下，只配置了 `log.roll.hours` 参数，其值为 168，即 7 天
3. 偏移量索引文件或时间戳索引文件的大小达到了 broker 端参数 `log.index.size.max.bytes` 配置的值。`log.index.size.max.bytes` 的默认值为 10485760，即 10MB
4. 追加的消息的偏移量与当前日志分段的偏移量之间的差值大于 Integer。MAX_VALUE，即要追加的消息的偏移量不能转变为相对偏移量（offset-baseOffset>Integer.MAX_VALUE）.

对非当前活跃的日志分段而言，其对应的索引文件内容已经固定而不需要再写入索引项，所以会被设定为只读。而对当前活跃的日志分段（activeSegment）而言，索引文件还会追加更多的索引项，所以被设定为可读写。在索引文件切分的时候，Kafka 会关闭当前正在写入的索引文件并设置为只读模式，同时以可读写的模式创建新的索引文件，索引文件的大小由 broker 端参数 `log.index.size.max.bytes` 配置。Kafka 在创建索引文件的时候会为其预分配 `log.index.size.max.bytes` 大小的文件，注意这一点与日志分段文件不同，只有当索引文件进行切分的时候，Kafka 才会把该索引文件裁剪到实际的数据大小。也就是说，与当前活跃的日志分段对应的索引文件的大小固定为 `log.index.size.max.bytes`，而其余日志分段对应的索引文件的大小为实际的占用空间。

## 日志清理

Kafka 将消息存储在磁盘中，为了控制磁盘占用空间的不断增加就需要对消息做一定的清理操作。Kafka 中每一个分区副本都对应一个 Log，而 Log 又可以分为多个日志分段，这样也便于日志的清理操作。Kafka 提供了两种日志清理策略：

1. 日志删除（Log Retention）：按照一定的保留策略直接删除不符合条件的日志分段
2. 日志压缩（Log Compaction）：针对每个消息的 key 进行整合，对于有相同 key 的不同 value 值，只保留最后一个版本

可以通过 broker 端参数 `log.cleanup.policy` 来设置日志清理策略，此参数的默认值为 `delete`，即采用日志删除的策略。如果要采用日志压缩的清理策略，就需要将 `log.cleanup.policy` 设置为 `compact`，并且还需要将 `log.cleaner.enable`（默认值为 true）设定为 true。通过将 `log.cleanup.policy` 参数设置为 `delete,compact` ，还可以同时支持日志删除和日志压缩两种策略。日志清理的粒度可以控制到主题级别。

### 日志删除

在 Kafka 的日志管理器中会有一个专门的日志删除任务来周期性地检测和删除不符合保留条件的日志分段文件，这个周期可以通过broker 端参数 `log.retention.check.interval.ms` 来配置，默认值为 300000 ，即 5 分钟。当前日志分段的保留策略有3 种：基于时间的保留策略、基于日志大小的保留策略和基于日志起始偏移量的保留策略。

**基于时间**

> 日志删除任务会检查当前日志文件中是否有保留时间超过设定的阈值（retentionMs）来寻找可删除的日志分段文件集合（ de letableSegments），如下图所示。retentionMs 可以通过 broker 端参数 `log.retention.hours` 、`log.retention.minutes` 和 `log.retention.ms` 来配置， 其中 `log.retention.ms` 的优先级最高，`log.retention.minutes`次之，`log.retention.hours` 最低。默认情况下只配置了 `log.retention.hours` 参数， 其值为 168，故默认情况下日志分段文件的保留时间为 7 天。
>
> ![1571763000890](../images/1571763000890.png)
>
> 查找过期的日志分段文件，并不是简单地根据日志分段的最近修改时间 lastModifiedTime 来计算的， 而是根据日志分段中最大的时间戳 largestTimeStamp 来计算的。因为日志分段的 lastModifiedTime 可以被有意或无意地修改，比如执行了 touch 操作，或者分区副本进行了重新分配，lastModifiedTime 并不能真实地反映出日志分段在磁盘的保留时间。要获取日志分段中的最大时间戳 largestTimeStamp 的值，首先要查询该日志分段所对应的时间戳索引文件，查找时间戳索引文件中最后一条索引项，若最后一条索引项的时间戳字段值大于0，则取其值，否则才设置为最近修改时间 lastModifiedTime 。
> 若待删除的日志分段的总数等于该日志文件中所有的日志分段的数量，那么说明所有的日志分段都己过期， 但该日志文件中还要有一个日志分段用于接收消息的写入，即**必须要保证有一个活跃的日志分段 activeSegment**，在此种情况下，会先切分出一个新的日志分段作为 activeSegment，然后执行删除操作。
> 删除日志分段时， 首先会从 Log 对象中所维护日志分段的跳跃表中移除待删除的日志分段，以保证没有线程对这些日志分段进行读取操作。然后将日志分段所对应的所有文件添加上 `.deleted` 的后缀（当然也包括对应的索引文件）。最后交由一个以 `delete-file` 命名的延迟任务来删除这些以 `.deleted ` 为后缀的文件，这个任务的延迟执行时间可以通过`file.delete.delay.ms` 参数来调配，此参数的默认值为 60000，即 1 分钟。

**基于日志大小**

> 日志删除任务会检查当前日志的大小是否超过设定的阈值（ retentionSize ）来寻找可删除的日志分段的文件集合（ deletableSegments ），如下图所示。retentionSize 可以通过 broker 端参数 `log.retention.bytes` 来配置，默认值为 -1，表示无穷大。注意 `log.retention.bytes` 配置的是 Log 中所有日志文件的总大小，而不是单个日志分段（确切地说应该为 .log 日志文件）的大小。单个日志分段的大小由 broker 端参数 `log.segment.bytes` 来限制，默认值为1073741824, 即 1GB 。
>
> ![1571764536668](../images/1571764536668.png)
>
> 基于日志大小的保留策略与基于时间的保留策略类似，首先计算日志文件的总大小 size 和 retentionSize 的差值diff，即计算需要删除的日志总大小，然后从日志文件中的第一个日志分段开始进行查找可删除的日志分段的文件集合 deletableSegments。查找出 deletableSegments 之后就执行删除操作，这个删除操作和基于时间的保留策略的删除操作相同。

**基于日志起始偏移量**

> 一般情况下，日志文件的起始偏移量 logStartOffset 等于第一个日志分段的 baseOffset，但这并不是绝对的，logStartOffset 的值可以通过 DeleteRecordsRequest 请求（比如使用 KafkaAdminClient 的 deleteRecords() 方法、使用 kafka-delete-records.sh 脚本）、日志的清理和截断等操作进行修改。
> 基于日志起始偏移量的保留策略的判断依据是某日志分段的下一个日志分段的起始偏移量 baseOffset 是否小于等于logStartOffset，若是则可以删除此日志分段。如下图所示，假设 logStartOffset 等于25，日志分段 1 的起始偏移量为0，日志分段 2 的起始偏移量为 11，日志分段 3 的起始偏移量为 23，通过如下动作收集可删除的日志分段的文件集合 deletableSegments :
>
> 1. 从头开始遍历每个日志分段，日志分段1的下一个日志分段的起始偏移量为 11，小于 logStartOffset  的大小，将日志分段1 加入 deletableSegments 
> 2. 日志分段2的下一个日志偏移量的起始偏移量为 23，也小于 logStartOffset 的大小，将日志分段2也加入deletableSegments
> 3. 日志分段3的下一个日志偏移量在 logStartOffset 的右侧，故从日志分段3开始的所有日志分段都不会加入deletableSegments
>
> ![1571765119347](../images/1571765119347.png)
>
> 收集完可删除的日志分段的文件集合之后的删除操作同基于日志大小的保留策略和基于时间的保留策略相同































