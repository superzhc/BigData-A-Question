# ScheduledExecutorService

ScheduledExecutorService 是一个线程池，用来在指定延时之后执行或者以固定的频率周期性的执行提交的任务。它包含了 4 个方法：

```java
public interface ScheduledExecutorService extends ExecutorService {

    /**
     * 在指定delay（延时）之后，执行提交Runnable的任务，返回一个ScheduledFuture，
     * 任务执行完成后ScheduledFuture的get()方法返回为null，ScheduledFuture的作用是可以cancel任务
     */
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, 
                                       TimeUnit unit);

    /**
     * 在指定delay（延时）之后，执行提交Callable的任务，返回一个ScheduledFuture
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, 
                                           TimeUnit unit);

    /**
     * 提交一个Runnable任务延迟了initialDelay时间后，开始周期性的执行该任务，每period时间执行一次
     * 如果任务异常则退出。如果取消任务或者关闭线程池，任务也会退出。
     * 如果任务执行一次的时间大于周期时间，则任务执行将会延后执行，而不会并发执行
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);

    /**
     * 提交一个Runnable任务延迟了initialDelay时间后，开始周期性的执行该任务，以后
       每两次任务执行的间隔是delay
     * 如果任务异常则退出。如果取消任务或者关闭线程池，任务也会退出。
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit);

}
```