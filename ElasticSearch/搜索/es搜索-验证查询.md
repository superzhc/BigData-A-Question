# 验证查询

查询可以变得非常的复杂，尤其和不同的分析器与不同的字段映射结合时，理解起来就有点困难了。此时可以借助 `validate-query` API 来验证查询是否合法。

```http
GET http://192.168.186.50:9200/superz/_validate/query
{
  "query": {
    "name": {
      "match": "superz1"
    }
  }
}
```

以上 validate 请求的应答会返回这个查询是不合法的：

```json
{
  "valid": false
}
```

**理解错误信息**

为了找出查询不合法的原因，可以将 explain 参数加到查询字符串中：

```http
GET http://192.168.186.50:9200/superz/_validate/query?explain
{
  "query": {
    "name": {
      "match": "superz1"
    }
  }
}
```

返回的响应信息如下：

```json
{
  "valid": false,
  "error": "org.elasticsearch.common.ParsingException: no [query] registered for [name]"
}
```

由上面的返回信息可知，查询类型（match）与字段名称（name）搞混了。

**理解查询语句**

对于合法的查询，使用 explain 参数将返回可读的描述，如下所示：

```http
GET http://192.168.186.50:9200/superz/_validate/query?explain
{
  "query": {
    "match": {
      "name": "superz1"
    }
  }
}
```

返回响应信息：

```json
{
  "_shards": {
    "total": 1,
    "successful": 1,
    "failed": 0
  },
  "valid": true,
  "explanations": [
    {
      "index": "superz",
      "valid": true,
      "explanation": "name:superz1"
    }
  ]
}
```

查询的每一个 index 都会返回对应的 explanation，因为每一个 index 都有自己的映射和分析器 。