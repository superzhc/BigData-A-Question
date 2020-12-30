<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-24 15:36:45
 * @LastEditTime : 2020-12-24 15:48:33
 * @Copyright 2020 SUPERZHC
-->
# Thread.join 详解

在很多情况下，当前线程生成并起动了子线程，如果子线程里要进行大量的耗时的运算，当前线程往往将于子线程之前结束，但是如果当前线程处理完其他的事务后，需要用到子线程的处理结果，也就是当前线程需要等待子线程执行完成之后再结束，这个时候就要用到 `join()` 方法了。

## 使用方式

join 是 Thread 类的一个方法，启动线程后直接调用，例如：

```java
Thread t=new CustomThread();
t.start();
// ...
t.join();
```

## 作用

在 JDK 的 API 中对于 `join()` 方法的注释如下：

> ```
> public final void join() throws InterruptedException Waits for this thread to die. Throws: InterruptedException  - if any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
> ```

简单的说作用就是：**等待该线程终止**。

## 源码

```java
public final void join() throws InterruptedException {
    join(0L);
}

public final synchronized void join(long l) throws InterruptedException
{
    long l1 = System.currentTimeMillis();
    long l2 = 0L;
    if(l < 0L)
        throw new IllegalArgumentException("timeout value is negative");
    if(l == 0L)
        // 如果线程被生成了，但还未被起动，isAlive()将返回false，调用它的join()方法是没有作用的，将直接继续向下执行。
        for(; isAlive(); wait(0L));
    else
        do
        {
            if(!isAlive())
                break;
            long l3 = l - l2;
            if(l3 <= 0L)
                break;
            wait(l3);
            l2 = System.currentTimeMillis() - l1;
        } while(true);
}
```