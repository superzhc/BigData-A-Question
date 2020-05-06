# wildcard

wildcard 通配符查询，允许使用通配符 `*` 和 `?` 来进行查询

- `*`：代表一个或多个字符
- `?`：仅代表一个字符

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "wildcard":{
            "k1":"zh*san"
        }
    }
}
```

> 注意：**这个查询很影响性能**