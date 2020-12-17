<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:40:34
 * @LastEditTime : 2020-12-17 16:40:44
 * @Copyright 2020 SUPERZHC
-->
ITERATE 语句也是用来跳出循环的语句，但是 ITERATE 语句是跳出本次循环，然后直接进入下一次循环。ITERATE 语句的语法形式如下：

```sql
ITERATE label
```

其中，参数 label 表示循环的标志。

**示例**

```sql
add_num:LOOP
    SET @count1=@count1+1;
    IF @count1=100 THEN
        LEAVE add_num;
    ELSEIF MOD(@count1,3)=0 THEN
        ITERATE add_num;
END LOOP add_num;
```