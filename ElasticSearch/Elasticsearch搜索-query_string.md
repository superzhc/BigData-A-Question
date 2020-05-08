# query_string

支持复杂的 Lucene query string 语法，不推荐使用

```http
GET http://127.0.0.1:9200/_search
{
    "query": {
        "query_string" : {
            "default_field" : "content",
            "query" : "this AND that OR thus"
        }
    }
}
```

- `default_field`：查询的字段，默认是 `_all` 字段，即对所有字段进行查询
- `query`：需要查询的具体内容

