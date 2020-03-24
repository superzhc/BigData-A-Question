package com.epoint.superz.datatructures.list;

/**
 * 线性表的链式存储
 * 2020年03月25日 superz add
 */
public class LinkList implements List
{
    private Node start;
    private int size = 0;

    @Override
    public void init() {
        start = null;
    }

    @Override
    public boolean empty() {
        return size == 0;
    }

    @Override
    public boolean clear() {
        start = null;
        return true;
    }

    /**
     * 获取链表第 i 个数据的算法思路：
     * 1. 声明一个结点 p 指向链表第一个结点，初始化 j 从 0 开始；
     * 2. 当 j<i，就遍历链表，让 p 的指针向后移动，不断指向下一个结点，j 累加 1；
     * 3. 若到链表末尾 p 为空，则说明第 i 个元素不存在
     * 4. 否则查找成功，返回结点 p 的数据
     * @param index
     * @return
     */
    @Override
    public Integer get(int index) {
        if (index > size - 1)
            throw new RuntimeException("查询索引越界");

        Node p = start;
        for (int j = 0; j <= index; j++) {
            p = p.next;
        }

        return p.data;
    }

    @Override
    public int locate(Integer e) {
        Node p = start;
        for (int j = 0; j < size; j++) {
            if (p.data == e)
                return j;
        }
        return -1;
    }

    /**
     * 单链表第 i 个数据插入结点的算法思路：
     * 1. 声明一个结点 p 指向链表第一个结点，初始化 j 从 0 开始；
     * 2. 当 j<i 时，就遍历链表，让 p 的指针向后移动，不断指向下一个结点，j 累加 1；
     * 3. 若到链表末尾 p 为空，则说明第 i 个元素不存在；
     * 4. 否则查找成功，在系统中生成一个空结点 s；
     * 5. 将数据元素 e 赋值给 s
     * 6. 单链表的插入标准语句 s.next=p.next; p.next=s
     * 7. 返回成功
     * @param index
     * @param e
     */
    @Override
    public void insert(int index, Integer e) {
        Node s = new Node();
        s.data = e;

        // 直接在开头插入
        if (index == 0) {
            s.next = start;
            start = s;
            size += 1;
            return;
        }
        else if (index > size) {
            throw new RuntimeException("查询索引越界");
        }

        Node p = start;
        // 获取 index-1 处的结点位置
        for (int j = 0; j < index; j++) {
            p = p.next;
        }

        // Node s = new Node();
        // s.data = e;
        s.next = p.next;
        p.next = s;

        // 数据的个数+1
        size += 1;
    }

    /**
     * 单链表第 i 个数据删除结点的算法思路：
     * 1. 声明一个结点 p 指向链表第一个结点，初始化 j 从 0 开始；
     * 2. 当 j<i 时，就遍历链表，让 p 的指针向后移动，不断指向下一个结点，j 累加 1；
     * 3. 若到链表末尾 p 为空，则说明第 i 个元素不存在；
     * 4. 否则查找成功，将预删除的结点 p.next 赋值给 q；
     * 5. 单链表的删除标准语句 p.next=q.next;
     * 6. ~~将 q 结点中的数据赋值给 e，作为返回~~
     * 7. 释放 q 结点，即 q.next=null
     * 8. 返回 q 结点的数据
     * @param index
     * @return
     */
    @Override
    public Integer delete(int index) {
        Node p = start;
        if (index == 0) {
            start = p.next;
            p.next = null;
            return p.data;
        }
        else if (index >= size) {
            throw new RuntimeException("查询索引越界");
        }
        // 获取index-1处的结点
        for (int j = 0; j < index; j++) {
            p = p.next;
        }

        // 则第 i 个结点的信息
        Node q = p.next;

        // 删除结点的标准语句
        p.next = q.next;
        // 释放 q 结点
        q.next = null;

        // 数据的个数-1
        size -= 1;

        return q.data;
    }

    @Override
    public int length() {
        return size;
    }

    /**
     * 定义结点的数据结构
     */
    public static class Node
    {
        public Integer data;
        public Node next;
    }
}
