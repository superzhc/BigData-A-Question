<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:39:57
 * @LastEditTime : 2020-12-17 16:40:11
 * @Copyright 2020 SUPERZHC
-->
REPEAT 语句是有条件控制的循环语句。当满足特定条件时，就会跳出循环语句。REPEAT 语句的基本语法形式如下：

```sql
[begin_label:]REPEAT 
    statement_list
    UNTIL search_condition
END REPEAT [end_label]
```

其中，参数 statement_list 表示循环的执行语句；参数 search_condition 表示结束循环的条件，满足该条件时循环结束。

**示例**

```sql
REPEAT
    SET @count1=@count1+1;
    UNTIL @count1=100
END REPEAT;
```