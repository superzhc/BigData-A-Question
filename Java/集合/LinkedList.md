# LinkedList

## LinkedList概述

同样实现List接口的LinkedList与ArrayList不同，ArrayList是一个动态数组，而LinkedList是一个双向链表。所以它除了有ArrayList的基本操作方法外还额外提供了get，remove，insert方法在LinkedList的首部或尾部。

由于实现的方式不同，LinkedList不能随机访问，它所有的操作都是要按照双重链表的需要执行。在列表中索引的操作将从开头或结尾遍历列表（从靠近指定索引的一端）。这样做的好处就是可以通过较低的代价在List中进行插入和删除操作。

与ArrayList一样，LinkedList也是非同步的。如果多个线程同时访问一个List，则必须自己实现访问同步。一种解决方法是在创建List时构造一个同步的List：`List list = Collections.synchronizedList(new LinkedList(...));`

此类实现Deque接口，为add、poll提供先进先出队列操作，以及其他堆栈和双端队列操作。

Deque实现：每一个节点都保存着上一个节点和下一个节点的指针。这就意味着数据的存取和更新具有线性复杂度（这也是一个最佳化的实现，每次操作都不会遍历数组一半以上，操作成本最高的元素就是数组中间的那个）。如果想写出高效的LinkedList代码可以使用 ListIterators 。如果你想用一个Queue/Deque实现的话（你只需读取第一个和最后一个元素就行了）——考虑用ArrayDeque代替。

## LinkedList源码分析

### LinkedList定义

```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
```

其中AbstractSequentialList提供了 List 接口的骨干实现，从而最大限度地减少了实现受“连续访问”数据存储（如链接列表）支持的此接口所需的工作,从而以减少实现List接口的复杂度。Deque一个线性 collection，支持在两端插入和移除元素，定义了双端队列的操作。

### LinkedList属性

在LinkedList中提供了两个基本属性size、header。

```java
//其中size表示LinkedList的大小，header表示链表的表头，Entry为节点对象
private transient Entry<E> header = new Entry<E>(null, null, null);
private transient int size = 0;

// Entry为LinkedList的内部类，它定义了存储的元素，该元素的前一个元素和后一个元素
private static class Entry<E> {
    E element;        //元素节点
    Entry<E> next;    //下一个元素
    Entry<E> previous;  //上一个元素

    Entry(E element, Entry<E> next, Entry<E> previous) {
        this.element = element;
        this.next = next;
        this.previous = previous;
    }
}
```

### LinkedList构造函数

LinkedList提供了两个构造方法：LinkedList()和LinkedList(Collection<? extends E> c)。

```java
/**
*  构造一个空列表。
*/
public LinkedList() {
    header.next = header.previous = header;
}

/**
*  构造一个包含指定 collection 中的元素的列表，这些元素按其 collection 的迭代器返回的顺序排列。
*/
public LinkedList(Collection<? extends E> c) {
    //先构造出一个空列表
    this();
    addAll(c);
}

/**
*  添加指定 collection 中的所有元素到此列表的结尾，顺序是指定 collection 的迭代器返回这些元素的顺序。
*/
public boolean addAll(Collection<? extends E> c) {
    return addAll(size, c);
}

/**
* 将指定 collection 中的所有元素从指定位置开始插入此列表。其中index表示在其中插入指定collection中第一个元素的索引
*/
public boolean addAll(int index, Collection<? extends E> c) {
    //若插入的位置小于0或者大于链表长度，则抛出IndexOutOfBoundsException异常
    if (index < 0 || index > size)
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    Object[] a = c.toArray();
    int numNew = a.length;    //插入元素的个数
    //若插入的元素为空，则返回false
    if (numNew == 0)
        return false;
    //modCount:在AbstractList中定义的，表示从结构上修改列表的次数
    modCount++;
    //获取插入位置的节点，若插入的位置在size处，则是头节点，否则获取index位置处的节点
    Entry<E> successor = (index == size ? header : entry(index));
    //插入位置的前一个节点，在插入过程中需要修改该节点的next引用：指向插入的节点元素
    Entry<E> predecessor = successor.previous;
    //执行插入动作
    for (int i = 0; i < numNew; i++) {
        //构造一个节点e，这里已经执行了插入节点动作同时修改了相邻节点的指向引用
        Entry<E> e = new Entry<E>((E) a[i], successor, predecessor);
        //将插入位置前一个节点的下一个元素引用指向当前元素
        predecessor.next = e;
        //修改插入位置的前一个节点，这样做的目的是将插入位置右移一位，保证后续的元素是插在该元素的后面，确保这些元素的顺序
        predecessor = e;
    }
    successor.previous = predecessor;
    //修改容量大小
    size += numNew;
    return true;
}

/**
* 返回指定位置(若存在)的节点元素
*/
private Entry<E> entry(int index) {
    if (index < 0 || index >= size)
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                + size);
    //头部节点
    Entry<E> e = header;
    //判断遍历的方向
    if (index < (size >> 1)) {
        for (int i = 0; i <= index; i++)
            e = e.next;
    } else {
        for (int i = size; i > index; i--)
            e = e.previous;
    }
    return e;
}
```

### LinkedList新增

add(E e): 将指定元素添加到此列表的结尾。

```java
public boolean add(E e) {
    addBefore(e, header);
        return true;
}

private Entry<E> addBefore(E e, Entry<E> entry) {
    //利用Entry构造函数构建一个新节点 newEntry，
    Entry<E> newEntry = new Entry<E>(e, entry, entry.previous);
    //修改newEntry的前后节点的引用，确保其链表的引用关系是正确的
    newEntry.previous.next = newEntry;
    newEntry.next.previous = newEntry;
    //容量+1
    size++;
    //修改次数+1
    modCount++;
    return newEntry;
}
```

LinkedList还提供了其他的增加方法：

- add(int index, E element)：在此列表中指定的位置插入指定的元素。
- addAll(Collection<? extends E> c)：添加指定 collection 中的所有元素到此列表的结尾，顺序是指定 collection 的迭代器返回这些元素的顺序。
- addAll(int index, Collection<? extends E> c)：将指定 collection 中的所有元素从指定位置开始插入此列表。
- addFirst(E e): 将指定元素插入此列表的开头。
- addLast(E e): 将指定元素添加到此列表的结尾。

### LinkedList移除

移除的方法：

- clear()： 从此列表中移除所有元素。
- remove()：获取并移除此列表的头（第一个元素）。
- remove(int index)：移除此列表中指定位置处的元素。
- remove(Objec o)：从此列表中移除首次出现的指定元素（如果存在）。
- removeFirst()：移除并返回此列表的第一个元素。
- removeFirstOccurrence(Object o)：从此列表中移除第一次出现的指定元素（从头部到尾部遍历列表时）。
- removeLast()：移除并返回此列表的最后一个元素。
- removeLastOccurrence(Object o)：从此列表中移除最后一次出现的指定元素（从头部到尾部遍历列表时）。

### LinkedList查找

通过迭代来返回查找的数据，方法如下：

- get(int index)：返回此列表中指定位置处的元素。
- getFirst()：返回此列表的第一个元素。
- getLast()：返回此列表的最后一个元素。
- indexOf(Object o)：返回此列表中首次出现的指定元素的索引，如果此列表中不包含该元素，则返回 -1。
- lastIndexOf(Object o)：返回此列表中最后出现的指定元素的索引，如果此列表中不包含该元素，则返回 -1。