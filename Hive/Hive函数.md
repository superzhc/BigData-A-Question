Hive支持以下内置函数：

|    返回类型     |                        签名                        | 描述                                                         |
| :-------------: | :------------------------------------------------: | :----------------------------------------------------------- |
|     BIGINT      |                 `round(double a)`                  | 返回BIGINT最近的double值。                                   |
|     BIGINT      |                 `floor(double a)`                  | 返回最大BIGINT值等于或小于double。                           |
|     BIGINT      |                  `ceil(double a)`                  | 它返回最小BIGINT值等于或大于double。                         |
|     double      |             `rand()`、`rand(int seed)`             | 它返回一个随机数，从行改变到行。                             |
|     string      |          `concat(string A, string B,...)`          | 它返回从A后串联B产生的字符串                                 |
|     string      |           `substr(string A, int start)`            | 它返回一个起始，从起始位置的子字符串，直到A.结束             |
|     string      |     `substr(string A, int start, int length)`      | 返回从给定长度的起始start位置开始的字符串。                  |
|     string      |                 `upper(string A)`                  | 它返回从转换的所有字符为大写产生的字符串。                   |
|     string      |                 `ucase(string A)`                  | 和上面的一样                                                 |
|     string      |                 `lower(string A)`                  | 它返回转换A的所有字符为小写产生的字符串。                    |
|     string      |                 `lcase(string A)`                  | 和上面的一样                                                 |
|     string      |                  `trim(string A)`                  | 它返回字符串从A.两端修剪空格的结果                           |
|     string      |                 `ltrim(string A)`                  | 它返回A从一开始修整空格产生的字符串(左侧)                    |
|     string      |                 `rtrim(string A)`                  | `rtrim(string A)`，它返回A从结束修整空格产生的字符串(右侧)   |
|     string      |   `regexp_replace(string A, string B, string C)`   | 它返回从替换所有子在B结果配合C.在Java正则表达式语法的字符串  |
|       int       |                  `size(Map<K.V>)`                  | 它返回在映射类型的元素的数量。                               |
|       int       |                  `size(Array<T>)`                  | 它返回在数组类型元素的数量。                                 |
| value of <type> |              `cast(<expr> as <type>)`              | 它把表达式的结果expr<类型>如`cast('1' as BIGINT)`代表整体转换为字符串'1'。如果转换不成功，返回的是NULL。 |
|     string      |           `from_unixtime(int unixtime)`            | 转换的秒数从Unix纪元(1970-01-0100:00:00 UTC)代表那一刻，在当前系统时区的时间戳字符的串格式："1970-01-01 00:00:00" |
|     string      |            `to_date(string timestamp)`             | 返回一个字符串时间戳的日期部分：`to_date("1970-01-01 00:00:00") = "1970-01-01"` |
|       int       |                `year(string date)`                 | 返回年份部分的日期或时间戳字符串：`year("1970-01-01 00:00:00") = 1970`, `year("1970-01-01") = 1970` |
|       int       |                `month(string date)`                | 返回日期或时间戳记字符串月份部分：`month("1970-11-01 00:00:00") = 11`, `month("1970-11-01") = 11` |
|       int       |                 `day(string date)`                 | 返回日期或时间戳记字符串当天部分：`day("1970-11-01 00:00:00") = 1`,` day("1970-11-01") = 1` |
|     string      | `get_json_object(string json_string, string path)` | 提取从基于指定的JSON路径的JSON字符串JSON对象，并返回提取的JSON字符串的JSON对象。如果输入的JSON字符串无效，返回NULL。 |

- **聚合函数**

Hive支持以下内置聚合函数。这些函数的用法类似于SQL聚合函数。

| 返回类型 | 签名                            | 描述                                                 |
| :------- | :------------------------------ | :--------------------------------------------------- |
| BIGINT   | `count(*)`, `count(expr)`       | `count(*)` - 返回检索行的总数。                      |
| DOUBLE   | `sum(col)`, `sum(DISTINCT col)` | 返回该组或该组中的列的不同值的分组和所有元素的总和。 |
| DOUBLE   | `avg(col)`, `avg(DISTINCT col)` | 返回上述组或该组中的列的不同值的元素的平均值。       |
| DOUBLE   | `min(col)`                      | 返回该组中的列的最小值。                             |
| DOUBLE   | `max(col)`                      | 返回该组中的列的最大值。                             |

