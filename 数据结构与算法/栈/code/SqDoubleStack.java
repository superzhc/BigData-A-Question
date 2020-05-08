package com.epoint.superz.datatructures.stack;

/**
 * 两栈共享空间
 * 2020年03月29日 superz add
 */
public class SqDoubleStack
{
    private static final int MAXSIZE = 40;
    // region 两栈共享空间的数据结构
    Integer[] data = new Integer[MAXSIZE];
    int top1 = -1;
    int top2 = MAXSIZE - 1;
    // endregion

    /**
     * 入栈
     * @param e 数据
     * @param stackNumber 入哪个栈的编号
     * @return
     */
    public boolean push(Integer e, int stackNumber) {
        if (top1 == top2)
            throw new RuntimeException("栈满");

        if (stackNumber == 1)
            data[++top1] = e;
        else if (stackNumber == 2)
            data[--top2] = e;

        return true;
    }
}
