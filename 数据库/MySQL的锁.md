<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-09-02 01:29:08
 * @LastEditTime : 2020-12-17 18:05:18
 * @Copyright 2020 SUPERZHC
-->
# MySQL 的锁

**锁是计算机协调多个进程或线程并发访问某一资源的机制**。在数据库中，除传统的计算资源（如CPU、RAM、I/O等）的争用以外，数据也是一种供许多用户共享的资源。如何保证数据并发访问的一致性、有效性是所有数据库必须解决的一个问题，锁冲突也是影响数据库并发访问性能的一个重要因素。从这个角度来说，锁对数据库而言显得尤其重要，也更加复杂。

相对其他数据库而言，MySQL 的锁机制比较简单，其最显著的特点是不同的 **存储引擎** 支持不同的锁机制。比如，MyISAM 和 MEMORY 存储引擎采用的是表级锁（table-level locking）；InnoDB 存储引擎既支持行级锁（row-level locking），也支持表级锁，但默认情况下是采用行级锁。 

- **表级锁：**开销小，加锁快；不会出现死锁；锁定粒度大，发生锁冲突的概率最高，并发度最低。
- **行级锁：**开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高。

