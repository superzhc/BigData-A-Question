# regexp

regexp 正则表达式查询

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "regexp":{
            "k1":"zh.*san[1-9]*?"
        }
    }
}
```