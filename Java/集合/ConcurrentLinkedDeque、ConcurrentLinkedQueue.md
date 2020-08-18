# ConcurrentLinkedDeque、ConcurrentLinkedQueue

基于链表实现的无界队列，添加元素不会堵塞。但是这就要求这个集合的消费者工作速度至少要和生产这一样快，不然内存就会耗尽。严重依赖于CAS(compare-and-set)操作。
