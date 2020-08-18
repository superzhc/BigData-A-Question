# Stack

Stack继承自Vector，实现一个后进先出（LIFO）的对象堆栈。栈是一种非常常见的数据结构，它采用典型的先进后出的操作方式完成的。每个栈都包含一个栈顶，每次出栈是将栈顶的数据取出。Stack提供5个额外的方法使得Vector得以被当作堆栈使用。基本的push和pop 方法，还有peek方法得到栈顶的元素，empty方法测试堆栈是否为空，search方法检测一个元素在堆栈中的位置。Stack刚创建后是空栈。

Stack继承Vector，它对Vector进行了简单的扩展：`public class Stack<E> extends Vector<E>`

Stack的方法源码：

```java
/**
* 构造函数，仅一个构造函数
*/
public Stack() {
}

/**
*  push函数：将元素存入栈顶
*/
public E push(E item) {
    // 将元素存入栈顶。
    // addElement()的实现在Vector.java中
    addElement(item);

    return item;
}

/**
* pop函数：返回栈顶元素，并将其从栈中删除
*/
public synchronized E pop() {
    E obj;
    int len = size();

    obj = peek();
    // 删除栈顶元素，removeElementAt()的实现在Vector.java中
    removeElementAt(len - 1);

    return obj;
}

/**
* peek函数：返回栈顶元素，不执行删除操作
*/
public synchronized E peek() {
    int len = size();

    if (len == 0)
        throw new EmptyStackException();
    // 返回栈顶元素，elementAt()具体实现在Vector.java中
    return elementAt(len - 1);
}

/**
* 栈是否为空
*/
public boolean empty() {
    return size() == 0;
}

/**
*  查找“元素o”在栈中的位置：由栈底向栈顶方向数
*/
public synchronized int search(Object o) {
    // 获取元素索引，elementAt()具体实现在Vector.java中
    int i = lastIndexOf(o);

    if (i >= 0) {
        return size() - i;
    }
    return -1;
}
```

一种后进先出的队列。不要在生产代码中使用，使用别的Deque来代替（ArrayDeque比较好）。