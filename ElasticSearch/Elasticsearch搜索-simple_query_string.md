# simple_query_string

简化版的 query_string，语法更适合用户使用

```http
GET http://127.0.0.1:9200/_search
{
  "query": {
    "simple_query_string" : {
        "query": "\"fried eggs\" +(eggplant | potato) -frittata",
        "fields": ["title^5", "body"],
        "default_operator": "and"
    }
  }
}
```

