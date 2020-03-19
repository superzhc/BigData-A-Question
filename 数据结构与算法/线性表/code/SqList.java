package com.epoint.superz.datatructures.list;

/**
 * 线性表的顺序存储实现
 * 2020年03月20日 superz add
 */
public class SqList implements List
{
    /* 设置数组的最大容量 */
    private static final int MAXSIZE = 20;
    /**
     * 此处为示例，固定下来了类型，真正的实现该使用泛型
     */
    private Integer[] data;
    private int size = 0;

    @Override
    public void init() {

    }

    @Override
    public boolean empty() {
        return false;
    }

    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public Integer get(int index) {
        if (size == 0 || index < 0 || index > size)
            throw new RuntimeException("索引位置不合理");
        return data[index];
    }

    @Override
    public int locate(Integer e) {
        return 0;
    }

    /**
     * 插入算法的思路：
     * 1. 如果插入位置不合理，抛出异常
     * 2. 如果线性表长度大于等于数组长度，则抛出异常或动态增加容量
     * 3. 从最后一个元素开始向前遍历到第 i 个位置，分别将它们都向后移动一个位置
     * 4. 将要插入元素填入位置 i 处
     * 5. 表长加 1
     * @param index
     * @param e
     */
    @Override
    public void insert(int index, Integer e) {
        if (size == MAXSIZE)
            throw new RuntimeException("more than array's length");
        if (index < 0 || index > size)
            throw new RuntimeException("索引位置不合理");
        if (index < size) {
            for (int i = size - 1; i >= index; i--)
                data[i + 1] = data[i];
        }
        data[index] = e;
        size++;
    }

    /**
     * 删除算法的思路：
     * 1. 如果删除位置不合理，抛出异常
     * 2. 取出删除元素
     * 3. 从删除元素位置开始遍历到最后一个位置，分别将它们向前移动一个位置
     * 4. 表长减1
     * @param index
     * @return
     */
    @Override
    public Integer delete(int index) {
        if (size == 0)
            throw new RuntimeException("no element");
        if (index < 0 || index > size)
            throw new RuntimeException("索引位置不合理");

        Integer e = data[index];
        for (int i = index + 1; i < size; i++) {
            data[i - 1] = data[i];
        }
        size--;
        return e;
    }

    @Override
    public int length() {
        return size;
    }
}
