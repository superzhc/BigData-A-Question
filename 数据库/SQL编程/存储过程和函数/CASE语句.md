<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-17 16:42:26
 * @LastEditTime : 2020-12-17 16:42:38
 * @Copyright 2020 SUPERZHC
-->
CASE 语句可实现比 IF 语句更复杂的条件判断，其语法的基本形式如下：

```sql
CASE case_value
    WHEN when_value THEN statement_list
    [WHEN when_value THEN statement_list ...]
    ELSE statement_list
END CASE 
```

其中，参数 case_value 表示条件判断的变量；参数 when_value 表示变量的取值；参数 statement_list 表示不同 when_value 值的执行语句。

CASE 语句还有另一种形式，该形式的语法如下：

```sql
CASE
    WHEN search_condition THEN statement_list
    WHEN search_condition THEN statement_list ...
    ELSE statement_list 
END CASE
```

参数 search_condition 表示条件判断语句；参数 statement_list 表示不同条件的执行语句。

**示例**

```sql
CASE age
    WHEN 20 THEN SET @count1=@count1+1;
    ELSE SET @count2=@count2+1;
END CASE;
```