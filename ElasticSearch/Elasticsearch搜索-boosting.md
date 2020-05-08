# boosting：提升某字段得分的检索

由于多个字段进行搜索，可能希望提高某一字段的得分。如下面示例，将“摘要”字段的得分提高了 3 倍，以增加“摘要”字段的重要性：

```http
POST http://127.0.0.1:9200/bookdb_index/book/_search
{
    "query": {
        "multi_match" : {
            "query" : "elasticsearch guide",
            "fields": ["title", "summary^3"]
        }
    },
    "_source": ["title", "summary", "publish_date"]
}
```

