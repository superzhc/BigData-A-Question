# TreeSet

与HashSet类似。这个类是基于一个TreeMap实例的。这是在单线程部分唯一一个排序的Set。

##### TreeSet定义

TreeSet是一个有序的，它的作用是提供有序的Set集合。TreeSet基础AbstractSet，实现NavigableSet、Cloneable、Serializable接口。其中AbstractSet提供 Set 接口的骨干实现，从而最大限度地减少了实现此接口所需的工作。NavigableSet是扩展的 SortedSet，具有了为给定搜索目标报告最接近匹配项的导航方法，这就意味着它支持一系列的导航方法。比如查找与指定目标最匹配项。Cloneable支持克隆，Serializable支持序列化。

```java
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable
```

TreeSet中定义了如下几个变量。

```java
private transient NavigableMap<E,Object> m;

//PRESENT会被当做Map的value与key构建成键值对
private static final Object PRESENT = new Object();
```

其构造方法：

```java
//默认构造方法，根据其元素的自然顺序进行排序
public TreeSet() {
    this(new TreeMap<E,Object>());
}

//构造一个包含指定 collection 元素的新 TreeSet，它按照其元素的自然顺序进行排序。
public TreeSet(Comparator<? super E> comparator) {
    this(new TreeMap<>(comparator));
}

//构造一个新的空 TreeSet，它根据指定比较器进行排序。
public TreeSet(Collection<? extends E> c) {
    this();
    addAll(c);
}

//构造一个与指定有序 set 具有相同映射关系和相同排序的新 TreeSet。
public TreeSet(SortedSet<E> s) {
    this(s.comparator());
    addAll(s);
}

TreeSet(NavigableMap<E,Object> m) {
    this.m = m;
}
```

##### TreeSet主要方法

```java
// add：将指定的元素添加到此 set（如果该元素尚未存在于 set 中）
public boolean add(E e) {
    return m.put(e, PRESENT)==null;
}

// addAll：将指定 collection 中的所有元素添加到此 set 中
public  boolean addAll(Collection<? extends E> c) {
    // Use linear-time version if applicable
    if (m.size()==0 && c.size() > 0 &&
        c instanceof SortedSet &&
        m instanceof TreeMap) {
        SortedSet<? extends E> set = (SortedSet<? extends E>) c;
        TreeMap<E,Object> map = (TreeMap<E, Object>) m;
        Comparator<? super E> cc = (Comparator<? super E>) set.comparator();
        Comparator<? super E> mc = map.comparator();
        if (cc==mc || (cc != null && cc.equals(mc))) {
            map.addAllForTreeSet(set, PRESENT);
            return true;
        }
    }
    return super.addAll(c);
}

// ceiling：返回此 set 中大于等于给定元素的最小元素；如果不存在这样的元素，则返回 null
public E ceiling(E e) {
    return m.ceilingKey(e);
}

// clear：移除此 set 中的所有元素
public void clear() {
    m.clear();
}

// clone：返回 TreeSet 实例的浅表副本。属于浅拷贝
public Object clone() {
    TreeSet<E> clone = null;
    try {
        clone = (TreeSet<E>) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new InternalError();
    }

    clone.m = new TreeMap<>(m);
    return clone;
}

// comparator：返回对此 set 中的元素进行排序的比较器；如果此 set 使用其元素的自然顺序，则返回 null
public Comparator<? super E> comparator() {
    return m.comparator();
}

// contains：如果此 set 包含指定的元素，则返回 true
public boolean contains(Object o) {
    return m.containsKey(o);
}

// descendingIterator：返回在此 set 元素上按降序进行迭代的迭代器
public Iterator<E> descendingIterator() {
    return m.descendingKeySet().iterator();
}

// descendingSet：返回此 set 中所包含元素的逆序视图
public NavigableSet<E> descendingSet() {
    return new TreeSet<>(m.descendingMap());
}

// first：返回此 set 中当前第一个（最低）元素
public E first() {
    return m.firstKey();
}

// floor：返回此 set 中小于等于给定元素的最大元素；如果不存在这样的元素，则返回 null
public E floor(E e) {
    return m.floorKey(e);
}

// headSet：返回此 set 的部分视图，其元素严格小于 toElement
public SortedSet<E> headSet(E toElement) {
    return headSet(toElement, false);
}

// higher：返回此 set 中严格大于给定元素的最小元素；如果不存在这样的元素，则返回 null
public E higher(E e) {
    return m.higherKey(e);
}

// isEmpty：如果此 set 不包含任何元素，则返回 true
public boolean isEmpty() {
    return m.isEmpty();
}

// iterator：返回在此 set 中的元素上按升序进行迭代的迭代器
public Iterator<E> iterator() {
    return m.navigableKeySet().iterator();
}

// last：返回此 set 中当前最后一个（最高）元素
public E last() {
    return m.lastKey();
}

// lower：返回此 set 中严格小于给定元素的最大元素；如果不存在这样的元素，则返回 null
public E lower(E e) {
    return m.lowerKey(e);
}

// pollFirst：获取并移除第一个（最低）元素；如果此 set 为空，则返回 null
public E pollFirst() {
    Map.Entry<E,?> e = m.pollFirstEntry();
    return (e == null) ? null : e.getKey();
}

// pollLast：获取并移除最后一个（最高）元素；如果此 set 为空，则返回 null
public E pollLast() {
    Map.Entry<E,?> e = m.pollLastEntry();
    return (e == null) ? null : e.getKey();
}

// remove：将指定的元素从 set 中移除（如果该元素存在于此 set 中）
public boolean remove(Object o) {
    return m.remove(o)==PRESENT;
}

// size：返回 set 中的元素数（set 的容量）
public int size() {
    return m.size();
}

// subSet：返回此 set 的部分视图
/**
* 返回此 set 的部分视图，其元素范围从 fromElement 到 toElement。
*/
public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
        E toElement,   boolean toInclusive) {
        return new TreeSet<>(m.subMap(fromElement, fromInclusive,
            toElement,   toInclusive));
}

/**
* 返回此 set 的部分视图，其元素从 fromElement（包括）到 toElement（不包括）。
*/
public SortedSet<E> subSet(E fromElement, E toElement) {
    return subSet(fromElement, true, toElement, false);
}

// tailSet：返回此 set 的部分视图
/**
* 返回此 set 的部分视图，其元素大于（或等于，如果 inclusive 为 true）fromElement。
*/
public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
    return new TreeSet<>(m.tailMap(fromElement, inclusive));
}

/**
* 返回此 set 的部分视图，其元素大于等于 fromElement。
*/
public SortedSet<E> tailSet(E fromElement) {
    return tailSet(fromElement, true);
}
```