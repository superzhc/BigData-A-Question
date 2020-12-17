<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:36:12
 * @LastEditTime : 2020-12-17 16:36:28
 * @Copyright 2020 SUPERZHC
-->
WHILE 语句也是有条件控制的循环语句，但 WHILE 语句和 REPEAT 语句是不一样的。WHILE 语句是当满足条件时，执行循环内的语句。WHILE 语句的基本语法形式如下：

```sql
[begin_label:]WHILE search_condition DO
    statement_list
END WHILE [end_label]
```

其中，参数 statement_condition 表示循环执行的条件，满足该条件时循环执行；参数 statement_list 表示循环的执行语句。

**示例**

```sql
WHILE @count1<100 DO
    SET @count1=@count1+1;
END WHILE;
```