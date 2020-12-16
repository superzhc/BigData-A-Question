<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-05-08 10:12:30
 * @LastEditTime : 2020-12-16 17:10:20
 * @Copyright 2020 SUPERZHC
-->
# CASE 表达式

## CASE 表达式的语法

CASE 表达式的语法分为 **简单 CASE 表达式** 和 **搜索 CASE 表达式** 两种。

简单 CASE 表达式的语法：

```sql
CASE <表达式>
	WHEN <表达式> THEN <表达式>
	WHEN <表达式> THEN <表达式>
	WHEN <表达式> THEN <表达式>
	·
	·
	·
	ELSE <表达式>
END
```

搜索 CASE 表达式的语法：

```sql
CASE WHEN <求值表达式> THEN <表达式>
	WHEN <求值表达式> THEN <表达式>
	WHEN <求值表达式> THEN <表达式>
	·
	·
	·
	ELSE <表达式>
END
```