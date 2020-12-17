<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:41:07
 * @LastEditTime : 2020-12-17 16:41:18
 * @Copyright 2020 SUPERZHC
-->
LEAVE 语句主要用于跳出循环控制，其语法形式如下：

```sql
LEAVE label
```

其中，参数 label 表示循环的标志。

**示例**

```sql
add_num:LOOP
    SET @count1=@count1+1;
    IF @count1=100 THEN
        LEAVE add_num;
END LOOP add_num;
```