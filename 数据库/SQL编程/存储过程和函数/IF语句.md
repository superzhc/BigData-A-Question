<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:43:05
 * @LastEditTime : 2020-12-17 16:43:17
 * @Copyright 2020 SUPERZHC
-->
IF 语句用来进行条件判断，根据条件执行不同的语句。其语法的基本形式如下：

```sql
IF search_condition THEN statement_list
    [ELSEIF search_condition THEN statement_list] ...
    [ELSE statement_list] 
END IF
```

参数 search_condition 表示条件判断语句；参数 statement_list 表示不同条件的执行语句。

**示例**

```sql
IF age>20 THEN SET @count1=@count1+1;
    ELSEIF age=20 THEN @count2=@count2+1;
    ELSE @count3=@count3+1;
END IF;
```