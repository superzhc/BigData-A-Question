# HiveQL

HiveQL 是 Hive 查询语言。和普遍使用的所有 SQL 方言一样，它不完全遵守任一种 ANSI SQL 标准的修订版。HiveQL 可能和 Mysql 的方言最接近，但是两者存在显著性差异的。Hive 不支持行级插入操作、更新操作和删除操作。Hive 也不支持事务。Hive 增加了在 Hadoop 背景下的可以提供更高性能的扩展，以及一些个性化的扩展，甚至还增加了一些外部程序。

## 显示命令总结

```sql
/* 数据库 */
-- 显示所有数据库
SHOW DATABASES;
-- 可以使用正则表达式匹配来帅选出需要的数据库名，如列出所有以字母h开头，以其他字符结尾的数据库
SHOW DATABASES LIKE 'h.*';

/* 表 */
-- 显示所有数据表
SHOW TABLES;
-- 显示指定数据库下的所有数据表
SHOW TABLES IN mydb;
-- 按正则表达式显示表
SHOW TABLES 'h*'
--查看表的详细信息
desc formatted <tablename>;

/* 分区 */
-- 显示表的所有分区
show partitioins <table_name>;

/* 函数 */
-- 显示所有函数
show functions;

describe extended table_name.col_name;
```

## DDL

[数据库](Hive/HiveQL/DDL/数据库.md)

[表](Hive/HiveQL/DDL/表.md)

[视图](Hive/HiveQL/DDL/视图.md)

## [DML](Hive/HiveQL/DML/README.md)

[Hive 数据导入和导出](Hive/HiveQL/DML/Hive数据导入和导出.md)

## SELECT

[HiveQL 总体](Hive/HiveQL/SELECT.md)

## 内置函数

[HiveQL 总体](Hive/HiveQL/Hive内置函数.md)