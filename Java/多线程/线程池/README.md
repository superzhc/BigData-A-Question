# 线程池

在并发的线程数量很多，且每个线程都是执行一个时间很短的任务就结束了，这样频繁创建线程就会大大降低系统的效率，因为频繁创建线程和销毁线程需要时间。在 Java 中可以通过线程池来使线程得以复用。

## ThreadPoolExecutor

`java.util.concurrent.ThreadPoolExecutor` 类是线程池中最核心的一个类。

在 ThreadPoolExecutor 类中提供了四个构造方法：

```java
public class ThreadPoolExecutor extends AbstractExecutorService {
    //.....
    public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue);

    public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory);

    public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue,RejectedExecutionHandler handler);

    public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory,RejectedExecutionHandler handler);
    //...
}
```

ThreadPoolExecutor 继承了 AbstractExecutorService类，并提供了四个构造器，事实上，通过观察每个构造器的源码具体实现，发现前面三个构造器都是调用的第四个构造器进行的初始化工作。

构造器中各个参数的含义：

- corePoolSize：核心池的大小。在创建了线程池后，默认情况下，线程池中并没有任何线程，而是等待有任务到来才创建线程去执行任务，除非调用了 `prestartAllCoreThreads()` 或者 `prestartCoreThread()` 方法，从这 2 个方法的名字就可以看出来，是预创建线程的意思，即在没有任务到来之前就创建 corePoolSize 个线程或者一个线程。默认情况下，在创建了线程池后，线程池中线程数为 0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到 corePoolSize 后，就会把到达的任务放到缓存队列中；
- maximumPoolSize：线程池最大线程数，它表示在线程池中最多能创建多少个线程；
- keepAliveTime：表示线程没有任务执行时最多保持多久时间会终止。默认情况下，只有当线程池中的线程数大于 corePoolSize 时，keepAliveTime 才会起作用，直到线程池中的线程数不大于 corePoolSize，即当线程池中的线程数大于 corePoolSize 时，如果一个线程空闲的时间达到 keepAliveTime，则会终止。但是如果调用了 `allowCoreTreadTimeOut(boolean)` 方法，在线程池中的线程数不大于 corePoolSize 时，keepAliveTime 参数也会起作用，直到线程池中的线程数为 0；
- unit：参数keepAliveTime的时间单位，有7种取值，在 TimeUnit 类中有7种静态属性：
    - `TimeUnit.DAYS`：天
    - `TimeUnit.HOURS`：小时
    - `TimeUnit.MINUTES`：分钟
    - `TimeUnit.SECONDS`：秒
    - `TimeUnit.MILLISECONDS`：毫秒
    - `TimeUnit.MICROSECONDS`：微秒
    - `TimeUnit.NANOSECONDS`：纳秒
- workQueue：一种阻塞队列，用来存储等待执行的任务，会对线程池的运行过程产生重大影响，一般来说，这里的阻塞队列有以下几种选择：
    - ArrayBlockQueue
    - LinkedBlockQueue
    - SynchronousQueue
- threadFactory：线程工厂，主要用来创建线程；
- headler：表示当拒绝处理任务时的策略，有以下四种取值：
    - `ThreadPoolExecutor.AbortPolicy`：丢弃任务并抛出 RejectedExecutionException 异常。
    - `ThreadPoolExecutor.DiscardPolicy`：也是丢弃任务，但是不抛出异常。
    - `ThreadPoolExecutor.DiscardOldestPolicy`：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
    - `ThreadPoolExecutor.CallerRunsPolicy`：由调用线程处理该任务

ThreadPoolExecutor 类中一些重要的成员变量

```java
private final BlockingQueue<Runnable> workQueue;              //任务缓存队列，用来存放等待执行的任务
private final ReentrantLock mainLock = new ReentrantLock();   //线程池的主要状态锁，对线程池状态（比如线程池大小、runState等）的改变都要使用这个锁
private final HashSet<Worker> workers = new HashSet<Worker>();  //用来存放工作集

private volatile long  keepAliveTime;    //线程存货时间
private volatile boolean allowCoreThreadTimeOut;   //是否允许为核心线程设置存活时间
private volatile int   corePoolSize;     //核心池的大小（即线程池中的线程数目大于这个参数时，提交的任务会被放进任务缓存队列）
private volatile int   maximumPoolSize;   //线程池最大能容忍的线程数

private volatile int   poolSize;       //线程池中当前的线程数

private volatile RejectedExecutionHandler handler; //任务拒绝策略

private volatile ThreadFactory threadFactory;   //线程工厂，用来创建线程

private int largestPoolSize;   //用来记录线程池中曾经出现过的最大线程数

private long completedTaskCount;   //用来记录已经执行完毕的任务个数
```

## 线程池实现原理

### 线程池状态

在 ThreadPoolExecutor 中定义了一个 `volatile `变量，另外定义了几个 `static final` 变量表示线程池的各个状态：

```java
volatile int runState;
static final int RUNNING    = 0;
static final int SHUTDOWN   = 1;
static final int STOP       = 2;
static final int TERMINATED = 3
```

runState 表示当前线程池的状态，它是一个 `volatile` 变量用来保证线程之间的可见性；

下面的几个常量表示 runState 可能的几个取值。

当创建线程池后，初始时，线程池处于 `RUNNING` 状态；如果调用了 `shutdown()` 方法后，则线程池处于 `SHUTDOWN` 状态，此时线程池不能够接受新的任务，它会等待所有的任务执行完毕；如果调用了 `shutdownNow()` 方法，则线程处于 `STOP` 状态，此时线程池不能接受新的任务，并且会去尝试终止正在执行的任务；当线程处于 `SHUTDOWN` 或 `STOP` 状态，并且所有工作线程已经销毁，任务缓存队列已经清空或执行结束后，线程池被设置为 `TERMINATED` 状态。

### 任务的执行

在 ThreadPoolExecutor 类中，最核心的任务提交方法是 `execute()` 方法，虽然通过 submit 也可以提交任务，但是实际上 submit 方法里面最终调用的还是 `execute()` 方法， `execute()` 的实现原理如下：

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    if (poolSize >= corePoolSize || !addIfUnderCorePoolSize(command)) {
        if (runState == RUNNING && workQueue.offer(command)) {
            if (runState != RUNNING || poolSize == 0)
                ensureQueuedTaskHandled(command);
        }
        else if (!addIfUnderMaximumPoolSize(command))
            reject(command); // is shutdown or saturated
    }
}
```

### 线程池中的线程初始化

默认情况下，创建线程池之后，线程池中是没有线程的，需要提交任务之后才会创建线程。

在实际中如果需要线程池创建之后立即创建线程，可以通过以下两个方法办到：

- `prestartCoreThread()`：初始化一个核心线程；
- `prestartAllCoreThreads()`：初始化所有核心线程

### 任务缓存队列及排队策略

workQueue，任务缓存队列，它用来存放等待执行的任务。

workQueue 的类型为 `BlockingQueue<Runnable>`，通常可以取下面三种类型：

- ArrayBlockingQueue：基于数组的先进先出队列，此队列创建时必须指定大小；
- LinkedBlockingQueue：基于链表的先进先出队列，如果创建时没有指定此队列大小，则默认为 `Integer.MAX_VALUE`；
- SynchronousQueue：这个队列比较特殊，它不会保存提交的任务，而是将直接新建一个线程来执行新来的任务。

### 任务拒绝策略

当线程池的任务缓存队列已满并且线程池中的线程数目达到 maximumPoolSize，如果还有任务到来就会采取任务拒绝策略，通常有以下四种策略：

- `ThreadPoolExecutor.AbortPolicy`：丢弃任务并抛出 RejectedExecutionException 异常。
- `ThreadPoolExecutor.DiscardPolicy`：也是丢弃任务，但是不抛出异常。
- `ThreadPoolExecutor.DiscardOldestPolicy`：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
- `ThreadPoolExecutor.CallerRunsPolicy`：由调用线程处理该任务

### 线程池的关闭

ThreadPoolExecutor提供了两个方法，用于线程池的关闭，分别是 `shutdown()` 和 `shutdownNow()`，其中：

- `shutdown()`：不会立即终止线程池，而是要等所有任务缓存队列中的任务都执行完后才终止，但再也不会接受新的任务
- `shutdownNow()`：立即终止线程池，并尝试打断正在执行的任务，并且清空任务缓存队列，返回尚未执行的任务

### 线程池容量的动态调整

ThreadPoolExecutor 提供了动态调整线程池容量大小的方法：`setCorePoolSize()` 和 `setMaximumPoolSize()`

- `setCorePoolSize()`：设置核心池大小
- `setMaximumPoolSize()`：设置线程池最大能创建的线程数目大小