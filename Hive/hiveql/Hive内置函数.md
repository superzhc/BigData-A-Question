# 内置函数

## 数学函数

| 函数                                         | 返回值类型 | 描述                                                  |
| :------------------------------------------- | :--------: | ----------------------------------------------------- |
| `round(DOUBLE D)`                            |   BIGINT   | 返回DOUBLE类型d的BIGINT类型的近似值                   |
| `round(DOUBLE d,INT n)`                      |   DOUBLE   | 返回DOUBLE类型d的保留n位小数的DOUBLE类型的近似值      |
| `floor(DOUBLE d)`                            |   BIGINT   | d是DOUBLE类型的，返回`<=d`的最大BIGINT型值            |
| `ceil(DOUBLE d)/ceiling(DOUBLE d)`           |   BIGINT   | d是DOUBLE类型的，返回`>=d`的最小BIGINT型值            |
| `rand()/rand(INT seed)`                      |   DOUBLE   | 每行返回一个DOUBLE型随机数，整数seed是随机因子        |
| `exp(DOUBLE d)`                              |   DOUBLE   | 返回e的d幂次方，返回的是个DOUBLE型值                  |
| `ln(DOUBLE d)`                               |   DOUBLE   | 以自然数为底d的对数，返回DOUBLE型值                   |
| `log10(DOUBLE d)`                            |   DOUBLE   | 以10为底d的对数，返回DOUBLE型值                       |
| `log2(DOUBLE d)`                             |   DOUBLE   | 以2为底d的对数，返回DOUBLE型值                        |
| `log(DOUBLE base,DOUBLE d)`                  |   DOUBLE   | 以base为底的对数，返回DOUBLE型值，base和d都是DOUBLE型 |
| `pow(DOUBLE d,DOUBLE p)`                     |   DOUBLE   | 计算d的p次幂，返回DOUBLE型值，其中d和p都是DOUBLE型的  |
| `power(DOUBLE d,DOUBLE p)`                   |   DOUBLE   | 计算d的p次幂，返回DOUBLE型值，其中d和p都是DOUBLE型的  |
| `sqrt(DOUBLE d)`                             |   DOUBLE   | 计算d的平方根，其中d是DOUBLE类型                      |
| `bin(DOUBLE i)`                              |   STRING   | 计算二进制值i的STRING类型值，其中i是BIGINT类型        |
| `hex(BIGINT i)`                              |   STRING   | 计算十六进制值i的STRING类型值，其中i是BIGINT类型      |
| `hex(STRING str)`                            |   STRING   | 计算十六进制表达的值str的STRING类型值                 |
| `hex(BINARY b)`                              |   STRING   | 计算二进制表达的值b的STRING类型值（Hive 0.12.0新增）  |
| `unhex(STRING i)`                            |   STRING   | `hex(STRING str)`的逆方法                             |
| `conv(BIGINT num,INT from_base,INT to_base)` |   STRING   | 将BIGINT类型的num从from_base进制转换成to_base进制     |
| `conv(BIGINT num,INT from_base,INT to_base)` |   STRING   | 将STRING类型的num从from_base进制转换成to_base进制     |
| `abs(DOUBLE d)`                              |   DOUBLE   | 计算DOUBLE型值d的绝对值                               |
| `pmod(INT i1,INT i2)`                        |    INT     | INT值i1对INT值i2取模                                  |
| `pmod(DOUBLE d1,DOUBLE d2)`                  |   DOUBLE   | DOUBLE值d1对DOUBLE值d2取模                            |
| `sin(DOUBLE d)`                              |   DOUBLE   | 正弦值                                                |
| `asin(DOUBLE d)`                             |   DOUBLE   | 反正弦值                                              |
| `cos(DOUBLE d)`                              |   DOUBLE   | 余弦值                                                |
| `acos(DOUBLE d)`                             |   DOUBLE   | 反余弦值                                              |
| `tan(DOUBLE d)`                              |   DOUBLE   | 正切值                                                |
| `atan(DOUBLE d)`                             |   DOUBLE   | 反正切值                                              |
| `degrees(DOUBLE d)`                          |   DOUBLE   | 将DOUBLE型弧度值d转换成角度值                         |
| `radians(DOUBLE d)`                          |   DOUBLE   | 将DOUBLE型角度值d转换成弧度值                         |
| `positive(INT i)`                            |    INT     | 返回+i                                                |
| `positive(DOUBLE d)`                         |   DOUBLE   | 返回+d                                                |
| `negative(INT i)`                            |    INT     | 返回-i                                                |
| `negative(DOUBLE d)`                         |   DOUBLE   | 返回-d                                                |
| `sign(DOUBLE d)`                             |   FLOAT    |                                                       |
| `e()`                                        |   DOUBLE   | 数学常数e，也就是超越数                               |
| `pi()`                                       |   DOUBLE   | 数学常数pi，也就是圆周率                              |

## 聚合函数

| 函数                                            |       返回值类型       | 描述                                 |
| :---------------------------------------------- | :--------------------: | ------------------------------------ |
| `count(*)`                                      |         BIGINT         | 计算总函数，包括含有NULL值的行       |
| `count(expr)`                                   |         BIGINT         | 计算expr表达式的值非NULL的行数       |
| `count(DISTINCT expr[,expr_.])`                 |         BIGINT         | 计算expr表达式的值排重后非NULL的行数 |
| `sum(col)`                                      |         DOUBLE         | 计算指定行的值的和                   |
| `sum(DISTINCT col)`                             |         DOUBLE         | 计算排重后值的和                     |
| `avg(col)`                                      |         DOUBLE         | 计算指定行的值的平均值               |
| `avg(DISTINCT col)`                             |         DOUBLE         | 计算排重后的值的平均值               |
| `min(col)`                                      |         DOUBLE         | 计算指定行的最小值                   |
| `max(col)`                                      |         DOUBLE         | 计算指定行的最大值                   |
| `variance(col)`                                 |         DOUBLE         | 返回集合col中的一组数值的方差        |
| `var_pop(col)`                                  |         DOUBLE         |                                      |
| `var_samp(col)`                                 |         DOUBLE         |                                      |
| `stddev_pop(col)`                               |         DOUBLE         |                                      |
| `stddev_samp(col)`                              |         DOUBLE         |                                      |
| `convar_pop(col1,col2)`                         |         DOUBLE         |                                      |
| `convar_samp(col1,col2)`                        |         DOUBLE         |                                      |
| `corr(col1,col2)`                               |         DOUBLE         |                                      |
| `percentile(BIGINT int_expr,p)`                 |         DOUBLE         |                                      |
| `percentile(BIGINT int_expr,ARRAY(P1[,P2...]))` |     ARRAY<DOUBLE>      |                                      |
| `percentile_approx(DOUBLE col,p[,NB])`          |         DOUBLE         |                                      |
| `percentile_approx(DOUBLE col)`                 |     ARRAY<DOUBLE>      |                                      |
| `histogram_numeric(col,NB)`                     | ARRAY<STRUCT{'x','y'}> |                                      |
| `collect_set(col)`                              |         ARRAY          |                                      |

## 其他内置函数

| 返回值类型                   | 使用方式                                                     | 描述                                                         |
| :--------------------------- | :----------------------------------------------------------- | ------------------------------------------------------------ |
| STRING                       | `ascii(STRING s)`                                            | 返回字符串s中首个ASCII字符的整数值                           |
| STRING                       | `base64(BINARY bin)`                                         | 将二进制值bin转换成基于64位的字符串(Hive 0.12.0版本新增)     |
| BINARY                       | `binary(STRING s)`<br/>`binary(BINARY b)`                    | 将输入的值转换成二进制值（Hive0.12.0版本新增                 |
| 返回类型就是type定义的类型   | `cast(<expr> as <type>)`                                     | 将expr转换成type类型的。<br/>例如`cast('1' as BIGINT)`将会将字符串‘1’转换成BIGINT数值类型。<br/>如果转换过程失败，则返回NULL |
| STRING                       | `concat(BINARY s1,BINARY s2, …)`                             | 将二进制字节码按次序拼接成一个字符串（Hive 0.12.0版本新增）  |
| STRING                       | `concat(STRING s1,STRING s2, …)`                             | 将字符串s1,s2等拼接成一个字符串。<br>例如，`concat('ab','cd')`的结果是`abcd` |
| STRING                       | `concat_ws(STRING separator, STRING s1,STRING　s2,…)`        | 和concat类似，不过是使用指定的分隔符进行拼接的               |
| STRING                       | `concat_ws(BINARY separator, BINARY s1,STRING　s2,…)`        | 和concat类似，不过是使用指定的分隔符进行拼接的（Hive 0.12.0版本新增） |
| ARRAY<STRUCT<STRING,DOUBLE>> | `context_ngrams(array <array<string>>,array <string>,int K, int pf)` | 和ngrams类似，但是从每个外层数组的第二个单词数组来查找前K个字尾 |
| STRING                       | `decode(BINARY bin,STRING charset)`                          | 使用指定的字符集charset将二进制值bin解码成字符串(支持的字符集有:'US_ASCII', 'ISO-8859-1', 'UTF-8', 'UTF-16BE', 'UTF-16LE', 'UTF-16').如果任一输入参数为NULL，则结果为NULL(Hive 0.12.0版本新增) |
| BINARY                       | `encode(STRING src,STRING charset)`                          | 使用指定的字符集charset将字符串src编码成二进制值(支持的字符集有:'US_ASCII', 'ISO-8859-1', 'UTF-8', 'UTF-16BE', 'UTF-16LE', 'UTF-16').如果任一输入参数为NULL，则结果为NULL(Hive 0.12.0版本新增) |
| INT                          | `find_in_set(STRING s,STRING commaSepa-ratedString)`         | 返回在以逗号分隔的字符串中s出现的位置，如果没有找到则返回NULL |
| STRING                       | `format_number(NUMBER x,INT d)`                              | 将数值x转换成‘#,###,###.##’格式字符串，并保留d位小数。如果d为0，那么输出值就没有小数点后面的值 |
| STRING                       | `get_json_object(STRING json_string,STRING path)`            | 从给定路径上的JSON字符串中抽取出JSON对象，并返回这个对象的JSON字符串形式。如果输入的JSON字符串是非法的，则返回NULL |
| BOOLEAN                      | `in`                                                         | 例如，`test in (val1, val2, …)`,其表示如果test值等于后面列表中的任一值的话，则返回true |
| BOOLEAN                      | `in_file(STRING s,STRING filename)`                          | 如果文件名为filename的文件中有完整一行数据和字符串s完全匹配的话，则返回true |
| INT                          | `instr(STRING str,STRING substr)`                            | 查找字符串str中子字符串substr第一次出现的位置                |
| INT                          | `length(STRING s)`                                           | 计算字符串s的长度                                            |
| INT                          | `locate(STRING substr,STRING str [,INT pos])`                | 查找在字符串str中的pos位置后字符串substr第一次出现的位置     |
| STRING                       | `lower(STRING s)`                                            | 将字符串中所有字母转换成小写字母。<br>例如，`upper('hIvE')`的结果是`'hive'` |
| STRING                       | `lcase(STRING s)`                                            | 和`lower()`一样                                              |
| STRING                       | `lpad(STRING s,INT len,STRING pad)`                          | 从左边开始对字符串s使用字符串pad进行填充，最终达到len长度为止。如果字符串s本身长度比len大的话，那么多余的部分会被去除掉 |
| STRING                       | `ltrim(STRING s)`                                            | 将字符串s前面出现的空格全部去除掉。例如`trim(' hive ')`的结果是`'hive '` |
| ARRAY<STRUCT<STRING,DOUBLE>> | `ngrams(ARRAY<ARRAY<string>>,INT N, INT K,INT pf)`           | 估算文件中前K个字尾。pf是精度系数                            |
| STRING                       | `parse_url(STRING url,STRING partname[,STRING key])`         | 从URL中抽取指定部分的内容。<br>参数url表示一个URL字符串，<br>参数partname表示要抽取的部分名称，其是大小写敏感的，可选的值有：HOST, PATH, QUERY, REF, PROTOCOL,AUTHORITY, FILE, USERINFO, QUERY:`<key>`。 如果partname是QUERY的话，那么还需要指定第三个参数key。<br>可以和表中的parse_url_tuple对比下 |
| STRING                       | `printf(STRING format, Obj … args)`                          | 按照printf风格格式化输出输入的字符串（Hive 0.9.0版本新增）   |
| STRING                       | `regexp_extract(STRING subject, STRING regex_pattern,STRING index)` | 抽取字符串subject中符合正则表达式regex_pattern的第index个部分的子字符串 |
| STRING                       | `regexp_replace(STRING s,STRING　regex,STRING replacement)`  | 按照Java正则表达式regex将字符串s中符合条件的部分替换成replacement所指定的字符串a。如果replacement部分是空的话，那么符合正则的部门就会被去除掉。<br>例如`regexp_replace('hive', '[ie]', 'z')`的结果是‘hzvz’ |
| STRING                       | `repeat(STRING s,INT n)`                                     | 重复输出n次字符串s                                           |
| STRING                       | `reverse(STRING s)`                                          | 反转字符串                                                   |
| STRING                       | `rpad(STRING s,INT len, STRING pad)`                         | 从右边开始对字符串s使用字符串pad进行填充，最终达到len长度为止。如果字符串s本身长度比len大的话，那么多余的部分会被去除掉 |
| STRING                       | `rtrim(STRING s)`                                            | 将字符串s后面出现的空格全部去除掉。<br>例如`trim(' hive ')`的结果是`' hive'` |
| ARRAY<ARRAY<STRING>>         | `sentences(STRING s,STRING lang,STRING locale)`              | 将输入字符串s转换成句子数组，每个句子又由一个单词数组构成。参数lang和locale是可选的，如果没有使用的，则使用默认的本地化信息 |
| INT                          | `size(MAP<K.V>)`                                             | 返回MAP中元素的个数                                          |
| INT                          | `size(ARRAY<T>)`                                             | 返回数组ARRAY的元素个数                                      |
| STRING                       | `space(INT n)`                                               | 返回n个空格                                                  |
| ARRAY<STRING>                | `split(STRING s,STRING pattern)`                             | 按照正则表达式pattern分割字符串s，并将分割后的部分以字符串数组的方式返回 |
| MAP<STRING,STRING>           | `str_to_map(STRING s,STRING delim1,STRING delim2)`           | 将字符串s按照指定分隔符转换成Map，第一个参数是输入的字符串，第二个参数是键值对之间的分隔符，第三个分隔符是键和值之间的分隔符 |
| STRING                       | `substr(STRING s,STRINGstart_index)`<br>`substring(STRING s,STRING start_index)` | 对于字符串s，从start位置开始截取length长度的字符串,作为子字符串。<br>例如`substr(‘abcdefgh’,3,2)`的结果是‘cd’ |
| STRING                       | `substr(BINARY s,STRING start_index)`<br>`substring(BINARY s,STRING start_index)` | 对于二进制字节值s，从start位置开始截取length长度的字符串,作为子字符串（Hive 0.12.0新增） |
| STRING                       | `translate(STRING input,STRING from, STRING to)`             |                                                              |
| STRING                       | `trim(STRING A)`                                             | 将字符串s前后出现的空格全部去除掉。<br>例如`trim(' hive ')`的结果是`hive` |
| BINARY                       | `unbase64(STRING str)`                                       | 将基于64位的字符串str转换成二进制值（Hive 0.12.0版本新增）   |
| STRING                       | `upper(STRING A)`<br>`ucase(STRING A)`                       | 将字符串中所有字母转换成大写字母。<br>例如，`upper('hIvE')`的结果是‘HIVE’ |
| STRING                       | `from_unixtime(BIGINT unixtime[,STRING format])`             | 将时间戳秒数转换成UTC时间，并用字符串表示,可以通过format规定的时间格式，指定输出的时间格式 |
| BIGINT                       | `unix_timestamp()`                                           | 获取当前本地时区下的当前时间戳                               |
| BIGINT                       | `unix_timestamp(STRING date)`                                | 输入的时间字符串格式必须是`yyyy-MM-dd HH:mm:ss`，如果不符合则返回0，如果符合则将此时间字符串转换成Unix时间戳。<br>例如：`unix_timestamp('2009- 03-20 11:30:01') = 1237573801` |
| BIGINT                       | `unix_timestamp(STRING date, STRING pattern)`                | 将指定时间字符串格式字符串转换成Unix时间戳，如果格式不对则返回0。<br>例如：`unix_timestamp('2009-03-20', 'yyyy-MM-dd') = 1237532400` |
| STRING                       | `to_date(STRING timestamp)`                                  | 返回时间字符串的日期部分，<br>例如：`to_date("1970-01-01 00:00:00") = "1970-01-01"` |
| INT                          | `year(STRING date)`                                          | 返回时间字符串中的年份并使用INT类型表示。<br>例如:`year("1970-01-0100:00:00") = 1970`, `year("1970-01-01") = 1970` |
| INT                          | `month(STRING date)`                                         | 返回时间字符串中的月份并使用INT类型表示。<br>例如: `month("1970-11-01 00:00:00") = 11`, `month("1970-11-01") = 11` |
| INT                          | `day(STRING date)`<br>`dayofmonth(STRING date)`              | 返回时间字符串中的天并使用INT类型表示。<br>例如:`day("1970-11-01 00:00:00") =1`, `day("1970-11-01") = 1` |
| INT                          | `hour(STRING date)`                                          | 返回时间戳字符串中的小时并使用INT类型表示。<br>例如：`hour('2009-07-30 12:58:59') = 12`, `hour('12:58:59') = 12` |
| INT                          | `minute(STRING date)`                                        | 返回时间字符串中的分钟数                                     |
| INT                          | `second(STRING date)`                                        | 返回时间字符串中的秒数                                       |
| INT                          | `weekofyear(STRING date)`                                    | 返回时间字符串位于一年中第几个周内。<br>例如：`weekofyear("1970-11-01 00:00:00") = 44`, `weekofyear("1970-11-01") = 44` |
| INT                          | `datediff(STRING enddate, STRING startdate)`                 | 计算开始时间startdata到结束时间enddata相差的天数。<br>例如：`datediff('2009-03-01', '2009-02-27') = 2` |
| STRING                       | `date_add(STRING startdate, INT days)`                       | 为开始时间startdata增加days天。<br>例如：`date_add('2008-12-31', 1) = '2009-01-01'` |
| STRING                       | `date_sub(STRING startdate, INT days)`                       | 从开始时间startdata中减去days天。<br>例如：`date_sub('2008-12-31', 1) = '2008-12-30'` |
| TIMESTAMP                    | `from_utc_timestamp(TIMESTAMP timestamp,STRING timezone)`    | 如果给定的时间戳并非UTC，则将其转化成指定的时区下的时间戳（Hive 0.8.0版本新增） |
| TIMESTAMP                    | `to_utc_timestamp(TIMESTAMP timestamp,STRING timezone)`      | 如果给定的时间戳是指定的时区下的时间戳，则将其转化成UTC下的时间戳（Hive0.8.0版本新增） |