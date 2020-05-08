package com.epoint.superz.datatructures.stack;

/**
 * 2020年03月29日 superz add
 */
public class SqStack implements Stack
{
    private static final int MAXSIZE = 20;
    // region 数据结构的定义
    Integer[] data = new Integer[MAXSIZE];
    int top = -1;
    // endregion

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
        return top == -1 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Integer get() {
        return top == -1 ? null : data[top];
    }

    @Override
    public boolean push(Integer e) {
        if (top == MAXSIZE - 1) {
            throw new RuntimeException("栈满");
        }

        // 插入数据
        top++;
        data[top] = e;

        return true;
    }

    @Override
    public Integer pop() {
        if (top == -1)
            throw new RuntimeException("这是一个空栈");

        Integer e = data[top];
        data[top] = null;
        top--;

        return e;
    }

    @Override
    public int length() {
        return top + 1;
    }
}
