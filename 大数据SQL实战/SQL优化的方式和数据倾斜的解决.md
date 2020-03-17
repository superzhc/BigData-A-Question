# SQL优化的方式和数据倾斜的解决

## join 语句中 where 条件的位置

当两个表进行 join 操作时，主表的 where 限制可以写在最后面，但从表分区限制条件不要写在 where 条件中，建议写在 on 条件或子查询中。主表的分区条件可以写在 where 条件中（最好先用子查询过滤）。

示例如下：

```sql
select * from A join (select * from B where dt=20150301) B on B.id=A.id where A.dt=20150301;
select * from A join B on B.id=A.id where B.dt=20150301; --不允许
select * from (select * from A where dt=20150301)A join (select * from B where dt=20150301)B on B.id=A.id;
```

第二个语句会先 join，后进行分区裁剪，数据量变大，性能下降。在实际使用过程中，应该尽量避免。

## 数据倾斜

产生数据倾斜的根本原因是有少数 worker 处理的数据量远远超过其他 worker 处理的数据量，从而导致少数 worker 的运行时长远远超过其他 worker 的平均运行时长，从而导致整个任务运行时间超长，造成任务延迟。

### join 造成的数据倾斜

> 造成 join 数据倾斜的原因是 join on 的 key 分布不均匀。

假设还是上述示例语句，现在将大表 A 和小表 B 进行 join 操作，运行如下语句：

```sql
select * from A join B on A.value= B.value
```

可以通过如下的方法进行优化：

- 由于表 B 是个小表并且没有超过 512MB，可将上述语句优化为 mapjoin 语句再执行，语句如下：
    ```sql
    select /*+ MAPJOIN(B) */ * from A join B on A.value= B.value;
    ```
- 将倾斜的 key 用单独的逻辑来处理，例如经常发生两边的 key 中有大量 null 数据导致了倾斜。则需要在 join 前先过滤掉 null 的数据或者补上随机数，然后再进行 join，示例如下：
    ```sql
    select * from A join B
        on case 
            when A.value is null then concat('value',rand() ) 
            else A.value 
            end = B.value;
    ```
- 在实际场景中，如果知道数据倾斜了，但无法获取导致数据倾斜的 key 信息，那么可以使用一个通用的方案，查看数据倾斜，如下所示：
    例如：`select * from a join b on a.key=b.key;` 产生数据倾斜。 可以执行： 
    ```sql
    select left.key, left.cnt * right.cnt from 
        (select key, count(*) as cnt from a group by key) left 
        join
        (select key, count(*) as cnt from b group by key) right
        on left.key=right.key;
    ```
    查看key的分布，可以判断 `a join b` 时是否会有数据倾斜。

### group by 倾斜

> 造成group by倾斜的原因是group by的key分布不均匀。

[TODO]

### 错误使用动态分区造成的数据倾斜

[TODO]

### 窗口函数的优化

[TODO]

### 子查询改用 join

例如有一个子查询，如下所示：

```sql
SELECT * FROM table_a a WHERE a.col1 IN (SELECT col1 FROM table_b b WHERE xxx);
```

当此语句中的 table_b 子查询返回的 col1 的个数超过 1000 个时，系统会报错为：`records returned from subquery exceeded limit of 1000`，此时可以使用 join 语句来代替，如下所示：

```sql
SELECT a.* FROM table_a a JOIN (SELECT DISTINCT col1 FROM table_b b WHERE xxx) c ON (a.col1 = c.col1);
```