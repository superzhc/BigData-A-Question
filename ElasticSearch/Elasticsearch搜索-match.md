# match

match 查询可接收文字、数字、日期等数据类型。

match 查询的时候，elasticsearch 会根据给定的字段提供合适的分词器，而 term 查询不会有分析器分析的过程。

```http
GET http://localhost:9200/superz/_search
{
 "query": {
  "match": {"name": "superz1"}
 }
}
```

> 注意：
>
> - 对于搜索的是词组，如 "superz xxx"，匹配查询默认的使用 or 操作，即只要包含 superz 或 xxx 都会被检索到，可以指定匹配查询为 and 操作。