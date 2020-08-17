> MySQL 提供了一个 **EXPLAIN** 命令，它可以对 `SELECT` 语句进行分析，并输出 `SELECT` 执行的详细信息，以供开发人员针对性优化。
>
> EXPLAIN 命令用法是直接在 SELECT 语句前加上 EXPLAIN 就可以了，如：
>
> ```sql
> EXPLAIN SELECT * FROM user_info where id<10
> ```

## EXPLAIN 输出格式

EXPLAIN 命令的输出格式内容大致如下：

```sql
mysql> explain select * from user_info where id = 2
*************************** 1. row ***************************
           id: 1
  select_type: SIMPLE
        table: user_info
   partitions: NULL
         type: const
possible_keys: PRIMARY
          key: PRIMARY
      key_len: 8
          ref: const
         rows: 1
     filtered: 100.00
        Extra: NULL
1 row in set, 1 warning (0.00 sec)
```

### id

`SELECT` 查询的序列号，包含一组数字，表示查询中执行select子句或者操作表的顺序

id分为三种情况：

1. 如果id相同，那么执行顺序从上到下:
    ```sql
    explain select * from emp e join dept d on e.deptno = d.deptno join salgrade sg on e.sal between sg.losal and sg.hisal;
    ```
2. 如果id不同，如果是子查询，id的序号会递增，id值越大优先级越高，越先被执行
    ```sql
    explain select * from emp e where e.deptno in (select d.deptno from dept d where d.dname = 'SALES');
    ```
3. id相同和不同的，同时存在：相同的可以认为是一组，从上往下顺序执行，在所有组中，id值越大，优先级越高，越先执行
    ```sql
    explain select * from emp e join dept d on e.deptno = d.deptno join salgrade sg on e.sal between sg.losal and sg.hisal where e.deptno in (select d.deptno from dept d where d.dname = 'SALES');
    ```

### select_type

`select_type` 表示查询的类型，它的常用取值有：

| 值                     | 描述                                                         |
| ---------------------- | ------------------------------------------------------------ |
| `SIMPLE`               | 表示此查询不包含 UNION 查询或子查询                          |
| `PRIMARY`              | 表示此查询是最外层的查询                                     |
| `UNION`                | 表示此查询是 UNION 的第二或随后的查询                        |
| `DEPENDENT UNION`      | UNION 中的第二个或后面的查询语句，取决于外面的查询           |
| `UNION RESULT`         | UNION 的结果                                                 |
| `SUBQUERY`             | 子查询中的第一个 SELECT                                      |
| `DEPENDENT SUBQUERY`   | 子查询中的第一个 SELECT，取决于外面的查询。即子查询依赖于外层查询的结果 |
| `DERIVED`              | Derived table                                                |
| `UNCACHEABLE SUBQUERY` | A subquery for which the result cannot be cached and must be re-evaluated for each row of the outer query |
| `UNCACHEABLE UNION`    | The second or later select in a UNION that belongs to an uncacheable subquery (see UNCACHEABLE SUBQUERY) |

```sql
--simple:简单的查询，不包含子查询和union
explain select * from emp;

--primary:查询中若包含任何复杂的子查询，最外层查询则被标记为Primary
explain select staname,ename supname from (select ename staname,mgr from emp) t join emp on t.mgr=emp.empno ;

--union:若第二个select出现在union之后，则被标记为union
explain select * from emp where deptno = 10 union select * from emp where sal >2000;

--dependent union:跟union类似，此处的depentent表示union或union all联合而成的结果会受外部表影响
explain select * from emp e where e.empno  in ( select empno from emp where deptno = 10 union select empno from emp where sal >2000)

--union result:从union表获取结果的select
explain select * from emp where deptno = 10 union select * from emp where sal >2000;

--subquery:在select或者where列表中包含子查询
explain select * from emp where sal > (select avg(sal) from emp) ;

--dependent subquery:subquery的子查询要受到外部表查询的影响
explain select * from emp e where e.deptno in (select distinct deptno from dept);

--DERIVED: from子句中出现的子查询，也叫做派生类，
explain select staname,ename supname from (select ename staname,mgr from emp) t join emp on t.mgr=emp.empno ;

--UNCACHEABLE SUBQUERY：表示使用子查询的结果不能被缓存
 explain select * from emp where empno = (select empno from emp where deptno=@@sort_buffer_size);
 
--uncacheable union:表示union的查询结果不能被缓存：sql语句未验证
```

### table

表示查询涉及的表或衍生表

1. 如果是具体的表名，则表明从实际的物理表中获取数据，当然也可以是表的别名
2. 表名是 derivedN 的形式，表示使用了 id 为 N 的查询产生的衍生表
3. 当有 union result 的时候，表名是 union n1,n2 等的形式，n1,n2 表示参与 union 的 id

### partitions

匹配的分区

### type

`type` 字段比较中，它提供了判断查询是否高效的重要依据。通过 `type` 字段，判断此次查询是*全表扫描*还是*索引扫描*等。

| 值            | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| `system`      | 表中只有一条数据。这个类型是特殊的 `const` 类型              |
| `const`       | 针对主键或唯一索引的等值查询扫描，最多只返回一行数据。`const` 查询速度非常快，因为它仅仅读取一次即可 |
| `eq_ref`      | 此类型通常出现在多表的 join 查询，表示对于前表的每一个结果，都只能匹配到后表的一行结果，并且查询的比较操作通常是 `=`，查询效率较高 |
| `ref`         | 此类型通常出现在多表的 join 查询，针对于非唯一或非主键索引，或者是使用了最左前缀规则索引的查询 |
| `index_merge` | 在查询过程中需要多个索引组合使用                             |
| `range`       | 表示使用索引范围查询，通过索引字段范围获取表中部分数据记录。这个类型通常出现在 `=`、`<>`、`>`、`>=`、`<`、`<=`、`IS NULL`、`<=>`、`BETWEEN IN()` 操作中。<br>当 `type` 是 `range` 时，那么 EXPLAIN 输出的 `ref` 字段为 NULL，并且 `key_len` 字段时此次查询中使用到的索引的最长的那个。 |
| `index`       | 表示全索引扫描（full index scan），和 ALL 类型类似，只不过 ALL 类型是全表扫描，而 index 类型则仅仅扫描所有的索引，而不扫描数据<br>`index` 类型通常出现在：所要查询的数据直接在索引树种就可以直接获取到，而不需要扫描数据，当是这种情况时 Extra 字段会显示 `Using index` |
| `ALL`         | 表示全表扫描，这个类型的查询是最差的查询之一。通常来说，用户的查询不应该出现 ALL 类型的查询，因为这样的查询在数据量大的情况下，对数据库的性能是巨大的灾难。如一个查询是 ALL 类型查询，那么一般来说可以对相应的字段添加索引来避免 |

通常来说，不同的 type 类型的性能关系如下：

```
system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL 
```

`ALL` 类型因为是全表扫描，因此在相同的查询条件下，它的速度是最慢的。而 `index` 类型的查询虽然不是全表扫描，但是它扫描了所有索引，因此比 ALL 类型稍快。

```sql
--all:全表扫描，一般情况下出现这样的sql语句而且数据量比较大的话那么就需要进行优化。
explain select * from emp;

--index：全索引扫描这个比all的效率要好，主要有两种情况，一种是当前的查询时覆盖索引，即我们需要的数据在索引中就可以索取，或者是使用了索引进行排序，这样就避免数据的重排序
explain  select empno from emp;

--range：表示利用索引查询的时候限制了范围，在指定范围内进行查询，这样避免了index的全索引扫描，适用的操作符： =, <>, >, >=, <, <=, IS NULL, BETWEEN, LIKE, or IN() 
explain select * from emp where empno between 7000 and 7500;

--index_subquery：利用索引来关联子查询，不再扫描全表
explain select * from emp where emp.job in (select job from t_job);

--unique_subquery:该连接类型类似与index_subquery,使用的是唯一索引
 explain select * from emp e where e.deptno in (select distinct deptno from dept);
 
--index_merge：在查询过程中需要多个索引组合使用，没有模拟出来

--ref_or_null：对于某个字段即需要关联条件，也需要null值的情况下，查询优化器会选择这种访问方式
explain select * from emp e where  e.mgr is null or e.mgr=7369;

--ref：使用了非唯一性索引进行数据的查找
 create index idx_3 on emp(deptno);
 explain select * from emp e,dept d where e.deptno =d.deptno;

--eq_ref ：使用唯一性索引进行数据查找
explain select * from emp,emp2 where emp.empno = emp2.empno;

--const：这个表至多有一个匹配行，
explain select * from emp where empno = 7369;
 
--system：表只有一行记录（等于系统表），这是const类型的特例，平时不会出现
```

### possible_keys

`possible_keys` 表示 MySQL 在查询时，能够使用到的索引。注意，即使有些索引在 `possible_keys` 中出现，但是并不表示此索引会真正地被 MySQL 使用到。MySQL 在查询时具体使用了哪些索引，由 key字段决定。

### key

此字段时 MySQL 在当前查询时真正使用到的索引

### key_len

表示索引中使用的字节数，可以通过key_len计算查询中使用的索引长度，在不损失精度的情况下长度越短越好。

### ref

显示索引的哪一列被使用了，如果可能的话，是一个常数

### rows

MySQL 查询优化器根据统计信息，估算 SQL 要查找到结果集需要扫描读取地数据行数。

这个值非常直观显示 SQL 的效率好坏，原则上 rows 越少越好。

### filtered

表示此查询条件所过滤的数据的百分比

### Extra

EXPLAIN 中的很多额外的信息会在 Extra 字段显示，常见的有以下几种内容：

| 值                      | 描述                                                         |
| ----------------------- | ------------------------------------------------------------ |
| `Using temporary`       | 查询有使用临时表，一般出现于排序、分组和多表 join 的情况，查询效率不高，建议优化 |
| `Using filesort`        | 表示 MySQL 需额外的排序操作，不能通过索引顺序达到排序效果。<br>一般有 `Using filesort` 都建议优化去掉，因为这样的查询 CPU 资源消耗大。 |
| `Using index`           | 覆盖索引扫描，表示查询在索引树种就可查询所需数据，不用扫描表数据文件，往往说明性能不错 |
| `Using Index Condition` | 会先条件过滤索引，过滤完索引后找到所有符合索引条件的数据行，随后用 WHERE 子句中的其他条件去过滤这些数据行 |
| `Using where`           | 表示MySQL服务器在存储引擎收到记录后进行“后过滤”（Post-filter）,如果查询未能使用索引，Using where 的作用只是提醒我们 MySQL 将用 where 子句来过滤结果集。<br>这个一般发生在MySQL服务器，而不是存储引擎层。一般发生在不能走索引扫描的情况下或者走索引扫描，但是有些查询条件不在索引当中的情况下。 |

```sql
--using filesort:说明mysql无法利用索引进行排序，只能利用排序算法进行排序，会消耗额外的位置
explain select * from emp order by sal;

--using temporary:建立临时表来保存中间结果，查询完成之后把临时表删除
explain select ename,count(*) from emp where deptno = 10 group by ename;

--using index:这个表示当前的查询时覆盖索引的，直接从索引中读取数据，而不用访问数据表。如果同时出现using where 表名索引被用来执行索引键值的查找，如果没有，表面索引被用来读取数据，而不是真的查找
explain select deptno,count(*) from emp group by deptno limit 10;

--using where:使用where进行条件过滤
explain select * from t_user where id = 1;

--using join buffer:使用连接缓存，情况没有模拟出来

--impossible where：where语句的结果总是false
explain select * from emp where empno = 7469;
```
