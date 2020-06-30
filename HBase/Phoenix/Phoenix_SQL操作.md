### 查看当前所有表

```sql
!tables
```

注：HBase 中已经存在的表不会自动映射过来，需要手动创建系统结果的数据表

### 创建表/视图

```sql
CREATE TABLE user (id varchar PRIMARY KEY,account varchar ,passwd varchar);
CREATE TABLE my_schema.my_table ( id BIGINT not null primary key, date)
CREATE TABLE my_table ( id INTEGER not null primary key desc, date DATE not null,m.db_utilization DECIMAL, i.db_utilization) m.DATA_BLOCK_ENCODING='DIFF'
CREATE TABLE stats.prod_metrics ( host char(50) not null, created_date date not null,txn_count bigint CONSTRAINT pk PRIMARY KEY (host, created_date) )
CREATE TABLE IF NOT EXISTS "my_case_sensitive_table" ( "id" char(10) not null primary key, "value" integer)
    DATA_BLOCK_ENCODING='NONE',VERSIONS=5,MAX_FILESIZE=2000000 split on (?, ?, ?)
CREATE TABLE IF NOT EXISTS my_schema.my_table (org_id CHAR(15), entity_id CHAR(15), payload binary(1000),CONSTRAINT pk PRIMARY KEY (org_id, entity_id) )TTL=86400

CREATE VIEW "my_hbase_table"( k VARCHAR primary key, "v" UNSIGNED_LONG) default_column_family='a';
CREATE VIEW my_view ( new_col SMALLINT ) AS SELECT * FROM my_table WHERE k = 100;
CREATE VIEW my_view_on_view AS SELECT * FROM my_view WHERE new_col > 70;
```

Phoenix 会自动将表名和字段名转换为大写字母，如果不想转换的话可以使用双括号括起来

Phoenix 创建的表，在 HBase 中的列簇默认是 0，主键字段对应到 hbase 的 ROW 字段。

如果想指定列簇与列的话使用如下语句建表

```sql
CREATE TABLE user (id varchar PRIMARY KEY,INFO.account varchar ,INFO.passwd varchar);
```

#### 映射 HBase 的数据表

安装好phoenix后对于HBase中已经存在的数据表不会自动进行映射，所以想要再phoenix中操作HBase已有数据表就需要手动进行配置。

根据 HBase 表，在 Phoenix 中创建相同结构的数据表【注：HBase 数据表默认主键列名是 ROW】

```sql
create table "phoenix"(ROW varchar primary key, "info"."name" varchar);
```

注：**这里一定要注意的是表名和列族以及列名需要用双引号括起来，因为HBase是区分大小写的，如果不用双引号括起来的话Phoenix在创建表的时候会自动将小写转换为大写字母，这样HBase中会创建另外一张表PHOENIX。**

映射成功，以后就可以直接从通过phoenix来操作HBase中的“phoenix”这张表。

### 删除表/视图

`drop` 关键字删除表或视图。如果删除表，表中数据也会被删除，如果是视图则不受影响。

```sql
DROP TABLE my_schema.my_table;
DROP TABLE IF EXISTS my_table;
DROP TABLE my_schema.my_table CASCADE;

-- 删除视图
DROP VIEW my_view
DROP VIEW IF EXISTS my_schema.my_view
DROP VIEW IF EXISTS my_schema.my_view CASCADE
```

### 修改表结构

alter关键字可以添加或删除一列或更新表属性。被移除的列，其上数据会被删除；如果该列是主键，不能被移除；如果移除列的是一个视图，数据是不会受影响的。

```sql
ALTER TABLE my_table ADD dept_name varchar(50)
ALTER TABLE my_table DROP COLUMN parent_id
ALTER TABLE my_table SET IMMUTABLE_ROWS=true

ALTER TABLE my_schema.my_table ADD d.dept_id char(10) VERSIONS=10
ALTER TABLE my_table ADD dept_name char(50), parent_id char(15) null primary key
ALTER TABLE my_table DROP COLUMN d.dept_id, parent_id;
ALTER VIEW my_view DROP COLUMN new_col;
ALTER TABLE my_table SET IMMUTABLE_ROWS=true,DISABLE_WAL=true;
```

### 插入/更新数据

更新数据的语法语插入数据相同

```sql
UPSERT INTO TEST VALUES('foo','bar',3);
UPSERT INTO TEST(NAME,ID) VALUES('foo',123);

-- UPSERT SELECT
UPSERT INTO test.targetTable(col1, col2) SELECT col3, col4 FROM test.sourceTable WHERE col5 < 100
UPSERT INTO foo SELECT * FROM bar;
```

插入数据的命令与 RDBMS 中插入数据语法稍有差别，使用 upsert

### 查询数据

```sql
SELECT * FROM TEST LIMIT 1000;
-- 分页查询
SELECT * FROM TEST LIMIT 1000 OFFSET 100;
SELECT full_name FROM SALES_PERSON WHERE ranking >= 5.0 UNION ALL SELECT reviewer_name FROM CUSTOMER_REVIEW WHERE score >= 8.0
```

### 删除数据

```sql
DELETE FROM TEST;
DELETE FROM TEST WHERE ID=123;
DELETE FROM TEST WHERE NAME LIKE 'foo%';
```

### 函数

#### 创建函数

```sql
CREATE FUNCTION my_reverse(varchar) returns varchar as 'com.mypackage.MyReverseFunction' using jar 'hdfs:/localhost:8080/hbase/lib/myjar.jar'
CREATE FUNCTION my_reverse(varchar) returns varchar as 'com.mypackage.MyReverseFunction'
CREATE FUNCTION my_increment(integer, integer constant defaultvalue='10') returns integer as 'com.mypackage.MyIncrementFunction' using jar '/hbase/lib/myincrement.jar'
CREATE TEMPORARY FUNCTION my_reverse(varchar) returns varchar as 'com.mypackage.MyReverseFunction' using jar 'hdfs:/localhost:8080/hbase/lib/myjar.jar'
```

#### 删除函数

```sql
DROP FUNCTION IF EXISTS my_reverse
DROP FUNCTION my_reverse
```

### 索引

#### 创建索引

```sql
CREATE INDEX my_idx ON sales.opportunity(last_updated_date DESC)
CREATE INDEX my_idx ON log.event(created_date DESC) INCLUDE (name, payload) SALT_BUCKETS=10
CREATE INDEX IF NOT EXISTS my_comp_idx ON server_metrics ( gc_time DESC, created_date DESC ) DATA_BLOCK_ENCODING='NONE',VERSIONS=?,MAX_FILESIZE=2000000 split on (?, ?, ?)
CREATE INDEX my_idx ON sales.opportunity(UPPER(contact_name))
```

#### 删除索引

```sql
DROP INDEX my_idx ON sales.opportunity
DROP INDEX IF EXISTS my_idx ON server_metrics
```

#### 修改索引

```sql
ALTER INDEX my_idx ON sales.opportunity DISABLE
ALTER INDEX IF EXISTS my_idx ON server_metrics REBUILD
```

### 执行计划

在数据库中，执行计划就是表示一条 SQL 将要执行的步骤，这些步骤按照不同的数据库运算符号（算子）组成，具体的组成和执行方式由数据库中的查询优化器来决定。换而言之，执行计划决定了SQL的执行效率。

```sql
-- 查看执行计划
EXPLAIN SELECT NAME, COUNT(*) FROM TEST GROUP BY NAME HAVING COUNT(*) > 2;
EXPLAIN SELECT entity_id FROM CORE.CUSTOM_ENTITY_DATA WHERE organization_id='00D300000000XHP' AND SUBSTR(entity_id,1,3) = '002' AND created_date < CURRENT_DATE()-1;
```

**执行计划的作用**：

- 将要扫描的CHUNK数量
- 客户端并发线程数量
- 执行模式（并行或串行）
- 查询过滤字段或者扫描范围
- 将会查询的表名
- 估算扫描数据bytes大小（依赖stats信息）
- 估算扫描数据量大小（依赖stats信息）
- 估算数量bytes大小和数据量时间
- 操作符被执行在客户端或者服务端
- 涉及的查询operations（sort、filter, scan, merge, join, limit等）

**性能良好的执行计划应该满足以下条件**：

1. 除了必要的客户端操作外, 大多数操作应该尽可能的都在服务端, 因为服务端是一个集群, 处理速度快, 而客户端只是一个单独的机器

2. 尽可能使用 `RANGE SCAN` 或 `SKIP SCAN` 而不是 `FULL SCAN`, 也就是最好不要出现全表扫描

3. 查询SQL中, where条件最好是包含所有的主键, 最次也要包含前几个,  前几个不能省略, 否则会大大的降低性能, 出现全表扫描的情况

4. 最好使用索引查询, 索引可以明显的提升查询速度

5. 如果你的SQL包含了索引字段, 但是Phoenix协处理器并没有在执行SQL的时候使用索引, 可以强制指定索引, 语法为: 

    `SELECT /*+ INDEX(my_table my_index) */ v2 FROM my_table WHERE v1 = 'foo'`

**执行计划内容解释**：

- `AGGREGATE INTO ORDERED DISTINCT ROWS`：使用诸如加法之类的操作聚合返回的行。使用 `ORDERED` 时，`GROUP BY` 操作应用于主键约束的前导部分，这允许聚合在适当的位置完成，而不是将所有不同的组保留在服务器端的内存中。使用有GROUP BY子句的聚合函数将结果聚合为不同的多行
- `AGGREGATE INTO SINGLE ROW`：使用没有 `GROUP BY` 子句的聚合函数将结果聚合为单行。例如，`count(*)` 语句返回一行，其中包含与查询匹配的总行数。
- `CLIENT`：操作将在客户端执行。在服务器端执行大多数操作的速度更快，因此您应该考虑是否有办法重写SQL查询语句, 以便让更多的操作都在服务器执行, 提高查询效率
- `FILTER BY 表达式`：仅返回与表达式匹配的结果, 就是过滤器
- `INNER-JOIN`：该操作将在满足连接条件的行上连接多个表
- `MERGE SORT`：对结果执行合并排序
- `RANGE SCAN OVER tableName [ … ]`：方括号中的信息表示查询中使用的每个主键的开始和停止
- `ROUND ROBIN`：当查询不包含 `ORDER BY` 并因此可以按任何顺序返回行时，`ROUND ROBIN` 命令最大化客户端的并行化
- `SKIP SCAN`：Phoenix实现的一种扫描方式，通常能比 `Range Scan` 获得更好的性能
- `FULL SCAN OVER tableName`：全表扫描, 出现了这种情况, 就要修改SQL了,  不然性能会很慢
- `LIMIT`:对查询结果取 TOP N
- `x-CHUNK`：执行SQL的线程数。最大并行度仅限于线程池中的线程数, 是一个客户端配置, 和hbase没有直接关系。最小并行化对应于表在扫描的开始行和停止行之间具有的region数量, 也就是一个region至少有一个scan扫描。这个并行度还和 guidepost width 有关,  如果路标宽度为100M, 也就意味着, 1G的region, 会有10个扫描并行执行。简单说, chunk数就是scan数
- `PARALLEL x-WAY`：表示SQL执行期间最终将会合并成多少个并行度。简单说就是, chunk 个 scan 并行执行完, 结果将保存到 way 个 List 集合中, 我们获取结果就是遍历 way 个 List 集合
- `SERIAL`：一些查询以串行方式运行。例如，单行查找或前几个主键上过滤的查询，并将结果限制在可配置的阈值以下
- `EST_BYTES_READ`：预估扫描的总字节数
- `EST_ROWS_READ`：预估扫描的总行数
- `EST_INFO_TS`：收集统计信息的时间点（毫秒），其实就是时间戳

### 执行脚本

phoenix自带了执行sql脚本的功能，这样方便了希望能够直接将一些关系型数据库的数据进行迁移到HBase（也可以直接使用sqoop进行导入）。

执行脚本语句如下：

```sh
-- 192.168.187.128,192.168.187.129,192.168.187.130:2181这一串参数是HBase集群的zookeeper集群ip与端口号
-- user.sql 是脚本文件
./bin/psql.py 192.168.187.128,192.168.187.129,192.168.187.130:2181 user.sql
```