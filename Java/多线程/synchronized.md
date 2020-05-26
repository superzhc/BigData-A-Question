# synchronized

**定义**

> Java 中具有通过 synchronized 实现的内置锁，内置锁获取锁和释放锁的过程是隐式的，进入 synchronized 修饰的代码就获得锁，离开相应的代码就释放锁。

**作用**

> 当它用来修饰一个方法或一个代码块时，能够保证在同一时刻最多只有一个线程执行该代码。

**使用**

> synchronized 主要有两种使用方法：synchronized 方法和 synchronized 代码块。

**基本原则**

- 当一个线程访问“某对象”的synchronized方法“或者”synchronized代码块“时，其他线程对”该对象“的”synchronized方法“或者”synchronized代码块“将被阻塞。

- 当一个线程访问”某对象“的”synchronized方法“或者”synchronized方法块“时，其他线程可以访问”该对象“的非同步代码块。

  