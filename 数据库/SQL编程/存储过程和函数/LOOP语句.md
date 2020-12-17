<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:41:51
 * @LastEditTime : 2020-12-17 16:42:01
 * @Copyright 2020 SUPERZHC
-->
LOOP 语句可以使某些特定的语句重复执行，实现一个简单的循环。但是 LOOP 语句本身没有停止循环，必须遇到 LEAVE 语句等才能停止循环。LOOP 语句的语法形式如下：

```sql
[begin_label:]LOOP
    statement_list
END LOOP [end_label]
```

其中，参数 begin_label 和参数 end_label 分别表示循环开始和结束的标志，这两个标志必须相同，而且都可以省略；参数 statement_list 表示需要循环执行的语句。

**示例**

```sql
add_num:LOOP
    SET @count1=@count1+1;
END LOOP add_num;
```