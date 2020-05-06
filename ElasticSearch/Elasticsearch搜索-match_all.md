# match_all

通过 match_all 查询，查询指定索引下的所有文档

```http
GET http://localhost:9200/superz/_search
{
  "query": {
    "match_all": {}
  }
}
```