package com.epoint.superz.datatructures.stack;

/**
 * 链表栈
 * 2020年03月30日 superz add
 */
public class LinkStack implements Stack
{
    StackNode top = null;
    int count = 0;

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isEmpty() {
        return count == 0 ? true : false;
    }

    @Override
    public Integer get() {
        return null == top ? null : top.data;
    }

    @Override
    public boolean push(Integer e) {
        StackNode node = new StackNode();
        node.data = e;
        node.next = top;

        // 切换栈顶为新插入的结点
        top = node;

        count++;

        return false;
    }

    @Override
    public Integer pop() {
        if (top == null)
            throw new RuntimeException("空栈");

        StackNode cur = top;

        top = cur.next;
        cur.next = null;

        count--;

        return cur.data;
    }

    @Override
    public int length() {
        return count;
    }

    /**
     * 栈结点的数据结构
     */
    private static class StackNode
    {
        Integer data;// 数据
        StackNode next;
    }
}
