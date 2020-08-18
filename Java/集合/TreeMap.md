# TreeMap

一种基于已排序且带导向信息Map的红黑树。每次插入都会按照自然顺序或者给定的比较器排序。这个Map需要实现equals方法和Comparable/Comparator。compareTo需要前后一致。这个类实现了一个NavigableMap接口：可以带有与键数量不同的入口，可以得到键的上一个或者下一个入口，可以得到另一Map某一范围的键（大致和SQL的BETWEEN运算符相同），以及其他的一些方法。

TreeMap的实现是红黑树算法的实现，通过红黑树算法来分析TreeMap。

##### 红黑树简介

红黑树又称红-黑二叉树，它首先是一颗二叉树，它具体二叉树所有的特性。同时红黑树更是一颗自平衡的排序二叉树。

二叉树都满足一个基本性质：即树中的任何节点的值大于它的左子节点，且小于它的右子节点。按照这个基本性质使得树的检索效率得到了很大的提升。但在生成二叉树的过程是非常容易失衡的，最坏的情况就是一边倒（只有左/右子树），这样势必导致二叉树的检索效率大大降低（O(n)），所以为了维持二叉树的平衡，提出来很多算法，如：AVL，SBT，伸展树，TREAP ，红黑树等等。

平衡二叉树必须具备的特征：它是一颗空树或它的左右两个子树的高度差的绝对值不超过1，并且左右两个子树都是一颗平衡二叉树。

![](http://ozchbp0v3.bkt.clouddn.com/TreeMap.jpg)

红黑树顾名思义就是节点是红色或者黑色的平衡二叉树，它通过颜色的约束来维持着二叉树的平衡。对于一颗有效的红黑树二叉树而言需要如下规范：

1. 每个节点都只能是红色或者黑色
2. 根节点是黑色
3. 每个叶节点（NIL节点、空节点）是黑色的
4. 如果一个节点是红色的，则它的两个子节点都是黑色的。也就是说在一条路径上不能出现相邻的两个红色节点
5. 从任一节点到其每个叶子节点的所有路径都包含相同数目的黑色节点

这些约束强制了红黑树的关键性质: 从根到叶子的最长的可能路径不多于最短的可能路径的两倍长。结果是这棵树大致上是平衡的。因为操作比如插入、删除和查找某个值的最坏情况时间都要求与树的高度成比例，这个在高度上的理论上限允许红黑树在最坏情况下都是高效的，而不同于普通的二叉查找树。所以红黑树它是复杂而高效的，其检索效率O(log n)。

对于红黑二叉树而言，它主要包括三大基本操作：左旋、右旋、着色。

###### 红黑树增加节点

红黑树在新增节点过程中比较复杂，复杂归复杂它同样必须要依据上面提到的五点规范。

对于新节点的插入有如下的三个关键地方：

1. 插入新节点总是红色节点
2. 如果插入节点的父节点是黑色，能维持性质
3. 如果插入节点的父节点是红色，破坏了性质。故插入算法就是通过重新着色或旋转，来维持性质。

插入的节点可能性：

- 为根节点：
    若新插入的节点N没有父节点，则直接当根节点插入即可，同时将颜色设置为黑色。
- 父节点为黑色：
    这种情况新节点N同样是直接插入，同时颜色为红色。
- 父节点P和P的兄弟节点U都为红色【P、U的父节点设为G】：
    P、U节点变黑、G节点变红。这时由于经过节点P、U的路径都必须经过G所以在这些路径上面的黑节点数目还是相同的。但是经过上面的处理，可能G节点的父节点也是红色，这个时候我们需要将G节点当做新增节点递归处理。
- 若父节点P为红色，P的兄弟节点U为黑色或者缺少，且新增节点N为P节点的右孩子：
    对新增节点N、P进行一次左旋转，P节点颜色进行转换。
- 父节点P为红色，叔父节点U（父节点P的兄弟节点）为黑色或者缺少，新增节点N为父节点P左孩子【P、U的父节点设为G】：（ **注：只是简略写下，红黑树需要着重研究** ）
    对于这种情况先已P节点为中心进行右旋转，在旋转后产生的树中，节点P是节点N、G的父节点，再将P、G节点的颜色进行转换。

###### 红黑树删除节点

针对于红黑树的增加节点而言，删除显得更加复杂，使原本就复杂的红黑树变得更加复杂。同时删除节点和增加节点一样，同样是找到删除的节点，删除之后调整红黑树。但是这里的删除节点并不是直接删除，而是通过走了“弯路”通过一种捷径来删除的：找到被删除的节点D的子节点C，用C来替代D，不是直接删除D，因为D被C替代了，直接删除C即可。所以这里就将删除父节点D的事情转变为了删除子节点C的事情，这样处理就将复杂的删除事件简单化了。子节点C的规则是：右分支最左边，或者 左分支最右边的。

红-黑二叉树删除节点，最大的麻烦是要保持各分支黑色节点数目相等。 因为是删除，所以不用担心存在颜色冲突问题——插入才会引起颜色冲突。

红黑树删除节点同样会分成几种情况，这里是按照待删除节点有几个儿子的情况来进行分类：

1. A节点没有儿子，即为叶结点。直接把父结点的对应儿子指针设为NULL，删除A结点就OK了。
2. A节点节点只有一个儿子。那么把父结点的相应儿子指针指向A节点的独生子，删除A结点也OK了。
3. A节点有两个儿子。这种情况比较复杂，但还是比较简单。上面提到过用子节点C替代代替待删除节点D，然后删除子节点C即可。

诚然，既然删除节点比较复杂，那么在这里我们就约定一下规则：

1. 下面要讲解的删除节点一定是实际要删除节点的后继节点（N），如前面提到的C。
2. 下面提到的删除节点的树都是如下结构，该结构所选取的节点是待删除节点的右树的最左边子节点。这里我们规定真实删除节点为N、父节点为P、兄弟节点为W兄弟节点的两个子节点为X1、X2。

##### TreeMap数据结构

TreeMap的定义如下：

```java
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable
```

TreeMap继承AbstractMap，实现NavigableMap、Cloneable、Serializable三个接口。其中AbstractMap表明TreeMap为一个Map即支持key-value的集合， NavigableMap则意味着它支持一系列的导航方法，具备针对给定搜索目标返回最接近匹配项的导航方法 。

TreeMap的几个重要属性：

```java
//比较器，因为TreeMap是有序的，通过comparator接口就可以对TreeMap的内部排序进行精密的控制
private final Comparator<? super K> comparator;
//TreeMap红-黑节点，为TreeMap的内部类
private transient Entry<K,V> root = null;
//容器大小
private transient int size = 0;
//TreeMap修改次数
private transient int modCount = 0;
//红黑树的节点颜色--红色
private static final boolean RED = false;
//红黑树的节点颜色--黑色
private static final boolean BLACK = true;
```

对于叶子节点Entry是TreeMap的内部类，它的几个重要属性：

```java
//键
K key;
//值
V value;
//左孩子
Entry<K,V> left = null;
//右孩子
Entry<K,V> right = null;
//父亲
Entry<K,V> parent;
//颜色
boolean color = BLACK;
```

##### TreeMap的PUT方法

在TreeMap的put()的实现方法中主要分为两个步骤，第一：构建排序二叉树，第二：平衡二叉树。

对于排序二叉树的创建，其添加节点的过程如下：

- 以根节点为初始节点进行检索。
- 与当前节点进行比对，若新增节点值较大，则以当前节点的右子节点作为新的当前节点。否则以当前节点的左子节点作为新的当前节点。
- 循环递归2步骤知道检索出合适的叶子节点为止。
- 将新增节点与3步骤中找到的节点进行比对，如果新增节点较大，则添加为右子节点；否则添加为左子节点。

```java
public V put(K key, V value) {
    //用t表示二叉树的当前节点
    Entry<K,V> t = root;
    //t为null表示一个空树，即TreeMap中没有任何元素，直接插入
    if (t == null) {
        //比较key值，个人觉得这句代码没有任何意义，空树还需要比较、排序？
        compare(key, key); // type (and possibly null) check
        //将新的key-value键值对创建为一个Entry节点，并将该节点赋予给root
        root = new Entry<>(key, value, null);
        //容器的size = 1，表示TreeMap集合中存在一个元素
        size = 1;
        //修改次数 + 1
        modCount++;
        return null;
    }
    int cmp;     //cmp表示key排序的返回结果
    Entry<K,V> parent;   //父节点
    // split comparator and comparable paths
    Comparator<? super K> cpr = comparator;    //指定的排序算法
    //如果cpr不为空，则采用既定的排序算法进行创建TreeMap集合
    if (cpr != null) {
        do {
            parent = t;      //parent指向上次循环后的t
            //比较新增节点的key和当前节点key的大小
            cmp = cpr.compare(key, t.key);
            //cmp返回值小于0，表示新增节点的key小于当前节点的key，则以当前节点的左子节点作为新的当前节点
            if (cmp < 0)
                t = t.left;
            //cmp返回值大于0，表示新增节点的key大于当前节点的key，则以当前节点的右子节点作为新的当前节点
            else if (cmp > 0)
                t = t.right;
            //cmp返回值等于0，表示两个key值相等，则新值覆盖旧值，并返回新值
            else
                return t.setValue(value);
        } while (t != null);
    }
    //如果cpr为空，则采用默认的排序算法进行创建TreeMap集合
    else {
        if (key == null)     //key值为空抛出异常
            throw new NullPointerException();
        /* 下面处理过程和上面一样 */
        Comparable<? super K> k = (Comparable<? super K>) key;
        do {
            parent = t;
            cmp = k.compareTo(t.key);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                return t.setValue(value);
        } while (t != null);
    }
    //将新增节点当做parent的子节点
    Entry<K,V> e = new Entry<>(key, value, parent);
    //如果新增节点的key小于parent的key，则当做左子节点
    if (cmp < 0)
        parent.left = e;
    //如果新增节点的key大于parent的key，则当做右子节点
    else
        parent.right = e;

    /*
     *  上面已经完成了排序二叉树的的构建，将新增节点插入该树中的合适位置
     *  下面fixAfterInsertion()方法就是对这棵树进行调整、平衡，具体过程参考上面红黑树新增的五种情况
     */
    fixAfterInsertion(e);
    //TreeMap元素数量 + 1
    size++;
    //TreeMap容器修改次数 + 1
    modCount++;
    return null;
}

/**
* 新增节点后的修复操作
* x 表示新增节点
*/
private void fixAfterInsertion(Entry<K,V> x) {
    x.color = RED;    //新增节点的颜色为红色

    //循环 直到 x不是根节点，且x的父节点不为红色
    while (x != null && x != root && x.parent.color == RED) {
        //如果X的父节点（P）是其父节点的父节点（G）的左节点
        if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            //获取X的叔节点(U)
            Entry<K,V> y = rightOf(parentOf(parentOf(x)));
            //如果X的叔节点（U） 为红色（情况三）
            if (colorOf(y) == RED) {
                //将X的父节点（P）设置为黑色
                setColor(parentOf(x), BLACK);
                //将X的叔节点（U）设置为黑色
                setColor(y, BLACK);
                //将X的父节点的父节点（G）设置红色
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            }
            //如果X的叔节点（U为黑色）；这里会存在两种情况（情况四、情况五）
            else {
                //如果X节点为其父节点（P）的右子树，则进行左旋转（情况四）
                if (x == rightOf(parentOf(x))) {
                    //将X的父节点作为X
                    x = parentOf(x);
                    //右旋转
                    rotateLeft(x);
                }
                //（情况五）
                //将X的父节点（P）设置为黑色
                setColor(parentOf(x), BLACK);
                //将X的父节点的父节点（G）设置红色
                setColor(parentOf(parentOf(x)), RED);
                //以X的父节点的父节点（G）为中心右旋转
                rotateRight(parentOf(parentOf(x)));
            }
        }
        //如果X的父节点（P）是其父节点的父节点（G）的右节点
        else {
            //获取X的叔节点（U）
            Entry<K,V> y = leftOf(parentOf(parentOf(x)));
            //如果X的叔节点（U） 为红色（情况三）
            if (colorOf(y) == RED) {
                //将X的父节点（P）设置为黑色
                setColor(parentOf(x), BLACK);
                //将X的叔节点（U）设置为黑色
                setColor(y, BLACK);
                //将X的父节点的父节点（G）设置红色
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            }
            //如果X的叔节点（U为黑色）；这里会存在两种情况（情况四、情况五）
            else {
                //如果X节点为其父节点（P）的右子树，则进行左旋转（情况四）
                if (x == leftOf(parentOf(x))) {
                    //将X的父节点作为X
                    x = parentOf(x);
                    //右旋转
                    rotateRight(x);
                }
                //（情况五）
                //将X的父节点（P）设置为黑色
                setColor(parentOf(x), BLACK);
                //将X的父节点的父节点（G）设置红色
                setColor(parentOf(parentOf(x)), RED);
                //以X的父节点的父节点（G）为中心右旋转
                rotateLeft(parentOf(parentOf(x)));
            }
        }
    }
    //将根节点G强制设置为黑色
    root.color = BLACK;
}

private void rotateLeft(Entry<K,V> p) {
    if (p != null) {
        //获取P的右子节点，其实这里就相当于新增节点N（情况四而言）
        Entry<K,V> r = p.right;
        //将R的左子树设置为P的右子树
        p.right = r.left;
        //若R的左子树不为空，则将P设置为R左子树的父亲
        if (r.left != null)
            r.left.parent = p;
        //将P的父亲设置R的父亲
        r.parent = p.parent;
        //如果P的父亲为空，则将R设置为跟节点
        if (p.parent == null)
            root = r;
        //如果P为其父节点（G）的左子树，则将R设置为P父节点(G)左子树
        else if (p.parent.left == p)
            p.parent.left = r;
        //否则R设置为P的父节点（G）的右子树
        else
            p.parent.right = r;
        //将P设置为R的左子树
        r.left = p;
        //将R设置为P的父节点
        p.parent = r;
    }
}

private void rotateRight(Entry<K,V> p) {
    if (p != null) {
        //将L设置为P的左子树
        Entry<K,V> l = p.left;
        //将L的右子树设置为P的左子树
        p.left = l.right;
        //若L的右子树不为空，则将P设置L的右子树的父节点
        if (l.right != null) 
            l.right.parent = p;
        //将P的父节点设置为L的父节点
        l.parent = p.parent;
        //如果P的父节点为空，则将L设置根节点
        if (p.parent == null)
            root = l;
        //若P为其父节点的右子树，则将L设置为P的父节点的右子树
        else if (p.parent.right == p)
            p.parent.right = l;
        //否则将L设置为P的父节点的左子树
        else 
            p.parent.left = l;
        //将P设置为L的右子树
        l.right = p;
        //将L设置为P的父节点
        p.parent = l;
    }
}

private static <K,V> void setColor(Entry<K,V> p, boolean c) {
    if (p != null)
        p.color = c;
}
```

###### TreeMap的deleteEntry方法

```java
private void deleteEntry(Entry<K,V> p) {
    modCount++;      //修改次数 +1
    size--;          //元素个数 -1

    /*
    * 被删除节点的左子树和右子树都不为空，那么就用 p节点的中序后继节点代替 p 节点
    * successor(P)方法为寻找P的替代节点。规则是右分支最左边，或者 左分支最右边的节点
    * ---------------------（1）
    */
    if (p.left != null && p.right != null) {
        Entry<K,V> s = successor(p);
        p.key = s.key;
        p.value = s.value;
        p = s;
    }

    //replacement为替代节点，如果P的左子树存在那么就用左子树替代，否则用右子树替代
    Entry<K,V> replacement = (p.left != null ? p.left : p.right);

    /*
    * 删除节点，分为上面提到的三种情况
    * -----------------------（2）
    */
    //如果替代节点不为空
    if (replacement != null) {
        replacement.parent = p.parent;
        /*
        *replacement来替代P节点
        */
        //若P没有父节点，则跟节点直接变成replacement
        if (p.parent == null)
            root = replacement;
        //如果P为左节点，则用replacement来替代为左节点
        else if (p == p.parent.left)
            p.parent.left  = replacement;
        //如果P为右节点，则用replacement来替代为右节点
        else
            p.parent.right = replacement;

        //同时将P节点从这棵树中剔除掉
        p.left = p.right = p.parent = null;

        /*
        * 若P为红色直接删除，红黑树保持平衡
        * 但是若P为黑色，则需要调整红黑树使其保持平衡
        */
        if (p.color == BLACK)
            fixAfterDeletion(replacement);
    } else if (p.parent == null) {     //p没有父节点，表示为P根节点，直接删除即可
        root = null;
    } else {      //P节点不存在子节点，直接删除即可
        if (p.color == BLACK)         //如果P节点的颜色为黑色，对红黑树进行调整
            fixAfterDeletion(p);

        //删除P节点
        if (p.parent != null) {
            if (p == p.parent.left)
                p.parent.left = null;
            else if (p == p.parent.right)
                p.parent.right = null;
            p.parent = null;
        }
    }
}

static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
    if (t == null)
        return null;
    /*
    * 寻找右子树的最左子树
    */
    else if (t.right != null) {
        Entry<K,V> p = t.right;
        while (p.left != null)
            p = p.left;
        return p;
    }
    /*
    * 选择左子树的最右子树
    */
    else {
        Entry<K,V> p = t.parent;
        Entry<K,V> ch = t;
        while (p != null && ch == p.right) {
            ch = p;
            p = p.parent;
        }
        return p;
    }
}

private void fixAfterDeletion(Entry<K,V> x) {
    // 删除节点需要一直迭代，知道 直到 x 不是根节点，且 x 的颜色是黑色
    while (x != root && colorOf(x) == BLACK) {
        if (x == leftOf(parentOf(x))) {      //若X节点为左节点
            //获取其兄弟节点
            Entry<K,V> sib = rightOf(parentOf(x));

            /*
            * 如果兄弟节点为红色----（情况3.1）
            * 策略：改变W、P的颜色，然后进行一次左旋转
            */
            if (colorOf(sib) == RED) {
                setColor(sib, BLACK);
                setColor(parentOf(x), RED);
                rotateLeft(parentOf(x));
                sib = rightOf(parentOf(x));
            }

            /*
            * 若兄弟节点的两个子节点都为黑色----（情况3.2）
            * 策略：将兄弟节点编程红色
            */
            if (colorOf(leftOf(sib))  == BLACK &&
                colorOf(rightOf(sib)) == BLACK) {
                setColor(sib, RED);
                x = parentOf(x);
            }
            else {
                /*
                * 如果兄弟节点只有右子树为黑色----（情况3.3）
                * 策略：将兄弟节点与其左子树进行颜色互换然后进行右转
                * 这时情况会转变为3.4
                */
                if (colorOf(rightOf(sib)) == BLACK) {
                    setColor(leftOf(sib), BLACK);
                    setColor(sib, RED);
                    rotateRight(sib);
                    sib = rightOf(parentOf(x));
                }
                /*
                *----情况3.4
                *策略：交换兄弟节点和父节点的颜色，
                *同时将兄弟节点右子树设置为黑色，最后左旋转
                */
                setColor(sib, colorOf(parentOf(x)));
                setColor(parentOf(x), BLACK);
                setColor(rightOf(sib), BLACK);
                rotateLeft(parentOf(x));
                x = root;
            }
        }

        /**
        * X节点为右节点与其为做节点处理过程差不多，这里就不在累述了
        */
        else {
            Entry<K,V> sib = leftOf(parentOf(x));

            if (colorOf(sib) == RED) {
                setColor(sib, BLACK);
                setColor(parentOf(x), RED);
                rotateRight(parentOf(x));
                sib = leftOf(parentOf(x));
            }

            if (colorOf(rightOf(sib)) == BLACK &&
                colorOf(leftOf(sib)) == BLACK) {
                setColor(sib, RED);
                x = parentOf(x);
            } else {
                if (colorOf(leftOf(sib)) == BLACK) {
                    setColor(rightOf(sib), BLACK);
                    setColor(sib, RED);
                    rotateLeft(sib);
                    sib = leftOf(parentOf(x));
                }
                setColor(sib, colorOf(parentOf(x)));
                setColor(parentOf(x), BLACK);
                setColor(leftOf(sib), BLACK);
                rotateRight(parentOf(x));
                x = root;
            }
        }
    }

    setColor(x, BLACK);
}
```