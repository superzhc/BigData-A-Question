### 1、Hive不支持等值连接

SQL中对两表内联可以写成：`select * from a,b where a.key=b.key`

Hive 中应为：`select * from a join b on a.key=b.key`

### 2、分号符号

分号是 SQL 语句结束标记，在 HiveQL 中也是，但是在 HiveQL 中，对分号识别没有那么智慧

示例：

`select concat(key,concat(';',key)) from dual;`，在 HiveQL 的解析语句时会提示：`FAILED: Parse Error: line 0:-1 mismatched input '<EOF>' expecting ) in function specification`，解决的办法是**使用分号的八进制的ASCII码进行转义**，那么上述语句应写成`select concat(key,concat('\073',key)) from dual;`

### 3、IS [NOT] NULL

SQL 中 null 代表空值，但在 HiveQL 中 String 类型的字段若是空（empty）字符串，即长度为0，那么对它进行 IS NULL 的判断结果是 False。

### 4、Hive不支持 `INSERT INTO table_name VALUES()`，UPDATE，DELETE操作

这样的话，就不要很复杂的锁机制来读写数据。
`INSERT INTO syntax is only available starting in version 0.8`。INSERT INTO就是在表或分区中追加数据。

### 5、Hive 支持嵌入 mapreduce 程序，来处理复杂的逻辑

TODO

### 6、Hive 支持将转换后的数据直接写入不同的表，还能写入分区、hdfs和本地目录

这样能免除多次扫描输入表的开销

```sql
FROM t1  
  
INSERT OVERWRITE TABLE t2  
SELECT t3.c2, count(1)  
FROM t3  
WHERE t3.c1 <= 20  
GROUP BY t3.c2  

INSERT OVERWRITE DIRECTORY '/output_dir'  
SELECT t3.c2, avg(t3.c1)  
FROM t3  
WHERE t3.c1 > 20 AND t3.c1 <= 30  
GROUP BY t3.c2  

INSERT OVERWRITE LOCAL DIRECTORY '/home/dir'  
SELECT t3.c2, sum(t3.c1)  
FROM t3  
WHERE t3.c1 > 30  
GROUP BY t3.c2;  
```

 **实际实例**

- **创建一个表**

```sql
CREATE TABLE u_data (
userid INT,
movieid INT,
rating INT,
unixtime STRING)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '/t'
STORED AS TEXTFILE;
```

下载示例数据文件，并解压缩

```sh
wget http://www.grouplens.org/system/files/ml-data.tar__0.gz # 不可用了
tar xvzf ml-data.tar__0.gz
```

- **加载数据到表中:**

```sql
LOAD DATA LOCAL INPATH 'ml-data/u.data' OVERWRITE INTO TABLE u_data;
```

- **统计数据总量:**  

建一个 `weekday_mapper.py` 文件，作为数据按周进行分割

```py
import sys
import datetime

for line in sys.stdin:
line = line.strip()
userid, movieid, rating, unixtime = line.split('/t')

//生成数据的周信息
weekday = datetime.datetime.fromtimestamp(float(unixtime)).isoweekday()
print '/t'.join([userid, movieid, rating, str(weekday)])
```

- **使用映射脚本**

```sql
-- 创建表，按分割符分割行中的字段值
CREATE TABLE u_data_new (
userid INT,
movieid INT,
rating INT,
weekday INT)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '/t';
//将python文件加载到系统
add FILE weekday_mapper.py;
**将数据按周进行分割**
INSERT OVERWRITE TABLE u_data_new
SELECT
TRANSFORM (userid, movieid, rating, unixtime)
USING 'python weekday_mapper.py'
AS (userid, movieid, rating, weekday)
FROM u_data;

SELECT weekday, COUNT(1)
FROM u_data_new
GROUP BY weekday;
```

- **处理Apache Weblog 数据**

将WEB日志先用正则表达式进行组合，再按需要的条件进行组合输入到表中

添加 jar 包：`add jar ../build/contrib/hive_contrib.jar;`

```sql
CREATE TABLE apachelog (
host STRING,
identity STRING,
user STRING,
time STRING,
request STRING,
status STRING,
size STRING,
referer STRING,
agent STRING)
ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe'
WITH SERDEPROPERTIES (
"input.regex" = "([^ ]*) ([^ ]*) ([^ ]*) (-|//[[^//]]*//]) ([^ /"]*|/"[^/"]*/") (-|[0-9]*) (-|[0-9]*)(?: ([^ /"]*|/"[^/"]*/") ([^ /"]*|/"[^/"]*/"))?",
"output.format.string" = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s"  
)
STORED AS TEXTFILE;
```