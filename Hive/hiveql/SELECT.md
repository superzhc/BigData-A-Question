# SELECT

Hive 中的 SELECT 基础语法和标准 SQL 语法基本一致，支持 WHERE、DISTINCT、GROUP BY、ORDER BY、HAVING、LIMIT、子查询等；

```sql
[WITH CommonTableExpression (, CommonTableExpression)*]  
SELECT [ALL | DISTINCT] select_expr, select_expr, ...
FROM table_reference
[WHERE where_condition]
[GROUP BY col_list]
[CLUSTER BY col_list
  | [DISTRIBUTE BY col_list] [SORT BY | ORDER BY col_list]
]
[LIMIT number]

-- 使用 ALL 和 DISTINCT 选项区分对重复记录的处理。默认是 ALL，表示查询所有记录。DISTINCT 表示去掉重复的记录
-- ORDER BY 与 SORD BY的不同：
-- ORDER BY 全局排序，只有一个 Reduce 任务
-- SORT BY 只做本机排序
```

## select_expr

1. 表中的列名
2. 使用正则表达式来指定列
   ```sql
   SELECT symbol, `price.*` FROM stocks;
   ```
3. 使用函数调用和算数表达式来计算列

### 列别名

使用关键字 AS 给列起别名

```sql
SELECT userid as id FROM customer;
```

### `CASE ... WHEN ... THEN` 语句

`CASE … WHEN … THEN` 语句和 if 条件语句类似，用于处理单个列的查询结果。

```sql
SELECT name, salary,
  CASE
    WHEN salary < 50000.0 THEN 'low'
    WHEN salary >= 50000.0 AND salary < 70000.0 THEN 'middle'
    WHEN salary >= 70000.0 AND salary < 100000.0 THEN 'high'
    ELSE 'very high'
  END AS bracket 
FROM employees;
```

## WHERE

> WHERE语句用于过滤条件

## GROUP BY

GROUP BY 语句通常会和聚合函数一起使用，按照一个或者多个列对结果进行分组，然后对每个组执行聚合操作。

## JOIN

Hive 支持通常的 SQL JOIN 语句，但是只支持等值连接。

### INNER JOIN

内连接（INNER JOIN）中，只有进行连接的两个表中都存在与连接标准相匹配的数据才会被保留下来。

ON子句指定了两个表间数据进行连接的条件。

注：Hive 目前还不支持在 ON 子句中的谓词间使用 OR。

**示例**

```sql
SELECT a.ymd, a.price_close, b.price_close , c.price_close
    FROM stocks a JOIN stocks b ON a.ymd = b.ymd
                  JOIN stocks c ON a.ymd = c.ymd
    WHERE a.symbol = 'AAPL' AND b.symbol = 'IBM' AND c.symbol = 'GE';
```

大多数情况下，Hive 会对每对 JOIN 连接对象启动一个 MapReduce 任务。本例中，会首先启动一个 MapReduce job 对表 a 和表 b 进行连接操作，然后会再启动一个 MapReduce job 将第一个 MapReduce job 的输出和表 c 进行连接操作。

> Hive 的执行顺序总是按照从左到右的顺序执行的。

**优化**

在上面的例子中，每个 ON 子句都使用到了 ymd 作为其中一个 JOIN 连接键。在这种情况下，Hive 通过一个优化可以在同一个 MapReduce job 中连接 3 张表。因此，*当对 3 个或者更多个表进行 JOIN 连接时，如果每个 ON 子句都使用相同的连接键的话，那么只会产生一个 MapReduce job*。

Hive 同时假定查询中最后一个表是最大的那个表。在对每行记录进行连接操作时，它会尝试将其他表缓存起来，然后扫描最后那个表进行计算。因此，*用户需要保证连续查询中的表的大小从左到右是依次增加的*。

用户也可以显式地通过标记机制来告诉查询优化器哪张表是大表，使用方式如下：

```sql
SELECT /*+STREAMTABLE(s)*/s.ymd,s.symbol,s.price_close,d.dividend
    FROM stocks s JOIN dividends d ON s.ymd = d.ymd AND s.symbol = d.symbol
    WHERE s.symbol = 'AAPL';
```

### LEFT OUTER JOIN

左外连接通过关键字 LEFT OUTER 进行标识，在这种 JOIN 连接操作中，JOIN 操作符左边表中符合WHERE子句的所有记录将会被返回。JOIN 操作符右边表中如果没有符合 ON 后面连接条件的记录时，那么从右边表指定选择的列的值将会是NULL。

### RIGHT OUTER JOIN

右外连接（RIGHT OUTER JOIN）会返回右边表所有符合 WHERE 语句的记录。左表中匹配不上的字段值用 NULL 代替。

### FULL OUTER JOIN

完全外连接（FULL OUTER JOIN）将会返回所有表中符合 WHERE 语句条件的所有记录。如果任一表的指定字段没有符合条件的值的话，那么就使用 NULL 值替代。

### LEFT SEMI-JOIN

左半开连接（LEFT SEMI-JOIN）会返回左边表的记录，前提是其记录对于右边表满足 ON 语句中的判定条件。

注：Hive 不支持右半开连接（RIGHT SEMI-JOIN）

> SEMI-JOIN 比通常的 INNER JOIN 要更高效，原因如下：对于左表中一条指定的记录，在右边表中一旦找到匹配的记录，Hive 就会立即停止扫描。从这点来看，左边表中选择的列是可以预测的。

### 笛卡尔积 JOIN

笛卡尔积是一种连接，表示左边表的行数乘以右边表的行数等于笛卡尔结果集的大小。

注：笛卡尔积会产生大量的数据。和其他连接类型不同，笛卡尔积不是并行执行的，而且使用 MapReduce 计算架构的话，任何方式都无法进行优化。

## ORDER BY 和 SORT BY

Hive 中 ORDER BY 语句和其他的 SQL 方言中的定义是一样的。其会对查询结果集执行一个全局排序。这也就是说会有一个所有的数据都通过一个 reducer 进行处理的过程。对于大数据集，这个过程可能会消耗太过漫长的时间来执行。

Hive 增加了一个可供选择的方式，也就是 SORT BY，其只会在每个 reducer 中对数据进行排序，也就是执行一个局部排序过程。这可以保证每个 reducer 的输出数据都是有序的（但并非全局有序）。这样可以提高后面进行的全局排序的效率。

对于这两种情况，语法区别仅仅是，一个关键字是 ORDER，另一个关键字是 SORT。用户可以指定任意期望进行排序的字段，并可以在字段后面加上 ASC 关键字（默认的），表示按升序排序，或加 DESC 关键字，表示按降序排序。

## 含有 SORT BY 的 DISTRIBUTE BY

DISTRIBUTE BY 控制 map 的输出在 reducer 中是如何划分的。MapReduce job 中传输的所有数据都是按照键-值对的方式进行组织的，因此 Hive 在将用户的查询语句转换成 MapReduce job 时，其必须在内部使用这个功能。

默认情况下，MapReduce 计算框架会依据 map 输入的键计算相应的哈希值，然后按照得到的哈希值将键-值对均匀分发到多个 reducer 中去。~~不过不幸的是，这也就意味着当我们使用 SORT BY 时，不同 reducer 的输出内容会有明显的重叠，至少对于排列顺序而言是这样，即使每个 reducer 的输出的数据都是有序的~~。【个人理解就是存在哈希冲突的原因，可能造成多个key的值都被分发到同一个 reducer 中了，造成数据是按照多个 key 的值来排序的，这不符合我们想要的那种，相同的key下的数据是在一块的且有序的】

注意：Hive 要求 DISTRIBUTE BY 语句要写在 SORT BY 语句之前。

## CLUSTER BY

CLUSTER BY 相当于 DISTRIBUTE BY 和 SORT BY 的简写，形如：

```sql
DISTRIBUTE BY col1 SORT BY col1
-- 等价于
CLUSTER BY col1
```

使用 `DISTRIBUTE BY ... SORT BY` 语句或其简化版的 CLUSTER BY 语句会剥夺 SORT BY 的并行性，然而这样可以实现输出文件的数据是全局排序的。

## LIMIT

LIMIT 用于限制数据返回的行数。

## 其他

### 类型转换

用户可以使用 `cast(...)` 函数对指定列的值进行显式的类型转换。

类型转换函数的语法是 `cast(COLUMN as TYPE)`，且在 Hive 中如果无法进行成功转换的值，Hive 会返回 NULL。