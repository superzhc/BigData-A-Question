ThreadPoolExecutor

参数最全的构造方法

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler)
```

- **corePoolSize：**线程池的核心线程数，说白了就是，即便是线程池里没有任何任务，也会有 corePoolSize 个线程在候着等任务。
- **maximumPoolSize：**最大线程数，不管提交多少任务，线程池里最多工作线程数就是 maximumPoolSize。
- **keepAliveTime：**线程的存活时间。当线程池里的线程数大于 corePoolSize 时，如果等了 keepAliveTime 时长还没有任务可执行，则线程退出。
- **unit：**这个用来指定 keepAliveTime 的单位，比如秒:TimeUnit.SECONDS。
- **workQueue：**一个阻塞队列，提交的任务将会被放到这个队列里。
- **threadFactory：**线程工厂，用来创建线程，主要是为了给线程起名字，默认工厂的线程名字：pool-1-thread-3。
- **handler：**拒绝策略，当线程池里线程被耗尽，且队列也满了的时候会调用。

