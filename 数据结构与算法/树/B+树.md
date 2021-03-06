B+树是B树的变体，也是一种多路搜索树：

　　1) 其定义基本与B-树相同，除了：

　　2) 非叶子结点的子树指针与关键字个数相同；

　　3) 非叶子结点的子树指针P[i]，指向关键字值属于[K[i], K[i+1])的子树（B-树是开区间）；

　　4) 为所有叶子结点增加一个链指针；

　　5) 所有关键字都在叶子结点出现；

　　B+树的搜索与B树也基本相同，区别是B+树只有达到叶子结点才命中（B树可以在非叶子结点命中），其性能也等价于在关键字全集做一次二分查找；

　　**B+的性质：**

　　1.所有关键字都出现在叶子结点的链表中（稠密索引），且链表中的关键字恰好是有序的；

　　2.不可能在非叶子结点命中；

　　3.非叶子结点相当于是叶子结点的索引（稀疏索引），叶子结点相当于是存储（关键字）数据的数据层；

　　4.更适合文件索引系统。

　　下面为一个B+树创建的示意图：

![img](D:\superz\BigData-A-Question\数据结构与算法\树\images\Bplustreebuild.gif)