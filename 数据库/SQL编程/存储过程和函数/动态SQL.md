<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-18 09:32:00
 * @LastEditTime : 2020-12-18 09:51:41
 * @Copyright 2020 SUPERZHC
-->
# 动态 SQL

Mysql 5.0 以后，支持了动态 sql 语句，可以通过传递不同的参数得到想要的值。

**方式一**

```sql
SET @sqlstatement = (预处理的sql语句，可以使用 concat 拼接的语句)
PREPARE stmt_name FROM @sqlstatement;
EXECUTE stmt_name;
{DEALLOCATE|DROP} PREPARE stmt_name;
```

**方式二**

```sql
SET @sqlstatement = (预处理的sql语句，可以使用 concat 拼接的语句，参数用 ? 代替)
PREPARE stmt_name FROM @sqlstatement;
SET @var_name='xxx'
EXECUTE stmt_name [USING @var_name[,@var_name ...]];
{DEALLOCATE|DROP} PREPARE stmt_name;
```