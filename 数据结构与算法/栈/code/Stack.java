package com.epoint.superz.datatructures.stack;

/**
 * 2020年03月29日 superz add
 */
public interface Stack
{
    void init();

    void destroy();

    void clear();

    boolean isEmpty();

    /**
     * 若栈存在且非空，返回栈顶元素
     * @return
     */
    Integer get();

    /**
     * 若栈存在，插入新元素 e 到栈中
     * @param e
     * @return
     */
    boolean push(Integer e);

    /**
     * 删除栈顶元素，结果返回栈顶元素
     * @return
     */
    Integer pop();

    int length();
}
