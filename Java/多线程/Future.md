<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-24 17:05:15
 * @LastEditTime : 2020-12-24 17:47:24
 * @Copyright 2020 SUPERZHC
-->
# Future

Future 模式的核心在于：去除了主函数的等待时间，并使得原本需要等待的时间段可以用于处理其他业务逻辑。

## Future 源码

Future 是一个接口，里面定义了五个方法：

```java
// 取消任务
boolean cancel(boolean mayInterruptIfRunning);

// 获取任务执行结果
V get() throws InterruptedException, ExecutionException;

// 获取任务执行结果，带有超时时间限制
V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,  TimeoutException;

// 判断任务是否已经取消
boolean isCancelled();

// 判断任务是否已经结束
boolean isDone();
```

## FutureTask

FutureTask 实现了 RunnableFuture 接口，RunnableFuture 继承了 Runnable 接口和 Future 接口：

```java
public class FutureTask<V> implements RunnableFuture<V>

public interface RunnableFuture<V> extends Runnable, Future<V> {
    void run();
}
```