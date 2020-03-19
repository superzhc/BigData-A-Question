# 轻量查询

> 轻量搜索是通过**轻量的查询字符串**，此种方式要求在查询字符串中传递所有参数。

查询字符串搜索非常适用于通过命令行做即席查询，但是查询字符串参数会进行 URL 编码，这让 URL 的查看变得更加难懂，如下所示：

```http
GET /_all/tweet/_search?q=+name:john +tweet:mary
# 实际的请求地址
GET /_all/tweet/_search?q=%2Bname%3Ajohn+%2Btweet%3Amary
```

而且这种查询是很脆弱的，一些查询字符串中很小的语法错误，像 `-` ， `:` ， `/` 或者 `"` 不匹配等，将会返回错误而不是搜索结果。

最后，查询字符串搜索允许任何用户在索引的任意字段上执行可能较慢且重量级的查询，这可能会暴露隐私信息，甚至将集群拖垮。

> 因为上述的原因，**对于轻量搜索，仅建议在开发阶段使用，不推荐直接向用户暴露查询字符串搜索功能**。

## `_all` 字段

```http
GET http://localhost:9200/superz/_search?q=value
```

该搜索返回包含 value 的所有文档，在索引一个文档的时候，elasticsearch 取出所有字段的值拼接成一个大的字符串，作为 `_all` 字段进行索引。例如，当索引这个文档时：

```json
{
    "tweet":    "However did I manage before Elasticsearch?",
    "date":     "2014-09-14",
    "name":     "Mary Jones",
    "user_id":  1
}
```

这就增加了一个名叫 `_all` 的额外字段：

```txt
"However did I manage before Elasticsearch? 2014-09-14 Mary Jones 1"
```

除非设置特定字段，否则查询字符串就使用 `_all` 字段进行搜索。

## 单字段全文检索

```
GET http://localhost:9200/superz/_search?q=k1:v1
```

## 条件组合

```http
GET http://localhost:9200/superz/_search?q=+k1:v1 -k2:v2
```

> `+`  前缀表示必须与查询条件匹配；类似地，`-` 前缀表示一定不与查询条件匹配；没有 `+` 或者 `-` 地所有其他条件都是可选的，匹配的越多，文档就越相关。

## 单字段精确检索

```
GET http://localhost:9200/superz/_search?q=k1:"hello elasticsearch"
```

## 多个检索条件的组合

```
GET http://localhost:9200/superz/_search?q=k1:(v1 OR v1') AND NOT k2:v2 
```

## 字段是否存在

```
GET http://localhost:9200/superz/_search?q=_exists_:k1
GET http://localhost:9200/superz/_search?q=NOT exists_:k1
```

## 通配符

用 `?` 表示单字母，`*` 表示任意个字母

```
GET http://localhost:9200/superz/_search?q=k1:zh???san
GET http://localhost:9200/superz/_search?q=k1:zh*san
```

## 近似搜索

用 `~` 表示搜索单词可能有一两个字母写的不对，按照相似度返回结果，最多可以模糊 2 个距离

```
GET http://localhost:9200/superz/_search?q=k1:zhangsnn~
```

## 范围搜索

**对数值和时间，都可以使用范围搜索**

`[]` 表示端点数值包含在范围内，`{}` 表示端点数值不包含在范围内

```
GET http://localhost:9200/superz/_search?q=k1:>20
GET http://localhost:9200/superz/_search?q=k1:[20 TO 30]
GET http://localhost:9200/superz/_search?q=d1:["now-8h" TO "now"]
```

## 正则搜索

**es 中正则性能不高，尽量不要使用**

- 保留字符：`. ? + * | { } [ ] ( ) " \ # @ & < > ~`
- 转义字符用 `\`，例如：`\* \\`


1. 用 `.` 代表一个字符，类似于通配符 `?`
    ```
    GET http://localhost:9200/superz/_search?q=k1:/zh...san/
    ```
2. 用 `.*` 匹配多个，类似于通配符 `*`
    ```
    GET http://localhost:9200/superz/_search?q=k1:/zh.*san/
    ```
3. 用 `*` 匹配0次或多次
    ```
    GET http://localhost:9200/superz/_search?q=k1:/a*b*/
    ```
4. 用 `?` 匹配0次或1次
    ```
    GET http://localhost:9200/superz/_search?q=/aaa?bbb?/
    ```
5. 用 `{}` 表示匹配的次数，格式：`{至少次数,至多次数}`
    ```
    GET http://localhost:9200/superz/_search?q=k1:/a{3}b{3}/
    GET http://localhost:9200/superz/_search?q=k1:/a{2,4}b{2,4}/
    ```
6. 用 `()` 组
    ```
    GET http://localhost:9200/superz/_search?q=k1:/(ab)*/
    GET http://localhost:9200/superz/_search?q=k1:/(ab){3}/
    ```
7. 用 `|` 代表或
    ```
    GET http://localhost:9200/superz/_search?q=k1:/(ab){3}|aaabbb/
    ```
8. 用 `[]` 表示可选字符，用 `^` 代表否定
    ```
    GET http://localhost:9200/superz/_search?q=k1:/[ab]*/
    GET http://localhost:9200/superz/_search?q=k1:/[a-c]*/
    GET http://localhost:9200/superz/_search?q=k1:/[^ab]*/
    ```