# lambda

## 用法

示例：最普遍的一个例子，执行一个线程

`new Thread(() -> System.out.print('hello world')).start();`

- 分析

1. `->` 这个箭头是 lambda 表达式的关键操作符
2. `->` 把表达式分成两截，前面是函数参数，后面是函数体
3. Thread 的构造函数接收的是一个 Runnable 接口对象，而我们这里的用法相当于是把一个函数当作一个接口对象传递进去了，这正是函数式编程的含义所在
4. Runnable 接口有个注解 @FunctionalInterface，它是 jdk8 才引入的，它的含义是函数接口。它是 lambda 表达式的协议注解