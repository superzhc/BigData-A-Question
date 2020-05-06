# term/terms

## term

term 查询被用于精确值匹配，这些精确值可以时数字、日期、布尔值、未经过分析的字符串；term 查询对于输入的文本不分析，所以它将给定的值进行精确查询

示例：查询数据中年龄字段为27的文档

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "term":{
            "age":27
        }
    }
}
```

示例：查询数据中k1字段为v1的文档

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "term":{
            "k1":"v1"
        }
    }
}
```

示例：查询数据中k1字段为"hello elasticsearch"的文档

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "term":{
            "k1":"hello elasticsearch"
        }
    }
}
```

## terms

terms 查询和 term 查询是一样的，但它允许指定多值进行匹配。

如果这个字段包含了指定值中的任何一个值，那么这个文档满足条件和 term 查询一样，terms 查询对于输入的文本不分析。

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "terms":{
            "k1":["v1","v2","v3"]
        }
    }
}
```