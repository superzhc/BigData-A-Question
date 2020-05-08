package com.epoint.superz.datatructures.list;

/**
 * 线性表
 * 2020年03月20日 superz add
 */
public interface List
{
    // region 操作

    /**
     * 初始化操作，建立一个空的线性表 L
     */
    void init();

    /**
     * 若线性表为空，返回true，否则返回 false
     * @return
     */
    boolean empty();

    /**
     * 将线性表清空
     * @return
     */
    boolean clear();

    /**
     * 将线性表中的第 i 个位置的元素返回给e
     * @param index
     * @return
     */
    Integer get(int index);

    /**
     * 在线性表中查找与给定值 e 相等的元素，如果查找成功，返回该元素在表中序号表示成功；否则返回 0 表示失败
     * @param e
     * @return
     */
    int locate(Integer e);

    /**
     * 在线性表中第 i 个位置插入新的元素 e
     * @param index
     * @param e
     */
    void insert(int index,Integer e);

    /**
     * 删除线性表中第 i 个位置元素，并用 e 返回其值
     * @param index
     * @return
     */
    Integer delete(int index);

    /**
     * 返回线性表的元素个数
     * @return
     */
    int length();
    // endregion
}
