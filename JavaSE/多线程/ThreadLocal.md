# ThreadLocal

当使用 ThreadLocal 维护变量时，ThreadLocal 为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不影响其他线程所对应的副本。

示例：

```java
/**
 * 2020年03月22日 superz add
 */
public class ThreadLocalDemo
{
    private static ThreadLocal<Integer> seqNum = new ThreadLocal<Integer>()
    {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public ThreadLocal<Integer> getThreadLocal() {
        return seqNum;
    }

    public Integer getNextNum() {
        seqNum.set(seqNum.get() + 1);
        return seqNum.get();
    }

    public static void main(String[] args) {
        ThreadLocalDemo sn = new ThreadLocalDemo();
        Thread a = new ThreadLocalClient(sn);
        Thread b = new ThreadLocalClient(sn);
        Thread c = new ThreadLocalClient(sn);
        a.start();
        b.start();
        c.start();

    }

    private static class ThreadLocalClient extends Thread
    {
        private ThreadLocalDemo sn;

        public ThreadLocalClient(ThreadLocalDemo sn) {
            this.sn = sn;
        }

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                System.out.println("[" + Thread.currentThread().getName() + "]" + sn.getNextNum());
            }
            sn.getThreadLocal().remove();
        }
    }
}
```

## ThreadLocal 实现原理

ThreadLocal 通过内部的一个 ThreadLocalMap 来保存每个线程的变量。ThreadLocalMap 是 ThreadLocal 类的一个静态内部类，它实现了键值对的设置和获取，每个线程都有独立的 ThreadLocalMap 副本，它所存储的值，只能被当前线程读取和修改。ThreadLocal 通过操作每一个线程特有的 ThreadLocalMap 副本，从而实现了变量访问在不同线程中的隔离。