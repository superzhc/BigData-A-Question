<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-05-08 10:12:30
 * @LastEditTime : 2020-12-17 17:42:21
 * @Copyright 2020 SUPERZHC
-->
# 多版本并发控制（MVCC）

> 目前 MVCC 设计没有统一的实现标准，因此 MySQL、Oracle、PostgreSQL 等其他数据库都有各自的实现，但各自的实现机制不尽相同。

可以认为 MVCC 是行级锁的一个变种，但是它在很多情况下避免了加锁操作，因此开销更低。

MVCC 的实现是通过保存数据在某个时间点的快照来实现的。也就是说。不管需要执行多长时间，每个事务看到的数据都是一致的。根据事务开始的时间不同，每个事务对同一张表，同一时刻看到的数据可能是不一样的。