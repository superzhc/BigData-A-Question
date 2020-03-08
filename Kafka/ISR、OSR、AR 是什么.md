# ISR、OSR、AR 是什么？

- ISR：In-Sync Replicas 副本同步队列
- OSR：Out-of-Sync Replicas
- AR：Assigned Replicas 所有副本

ISR 是由 Leader 维护，follower 从 Leader 同步数据有一些延迟，超过相应的阈值会把 follower 剔除出 ISR，存入 OSR 列表，新加入的 follower 也会先存放在 OSR 中。

`AR = ISR + OSR`