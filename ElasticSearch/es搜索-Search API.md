# Search API

es 提供了两种搜索方式：**字符串查询（Query String Syntax）搜索** 和 **DSL 查询**

## Query String Syntax

Query string 搜索通过命令非常方便地进行临时性地即席搜索，但它有自身的局限性

- 使用 `q` 指定查询字符串
- `Query String Syntax`：K/V 键值对

示例如下：

```http
GET http://localhost:9200/_search?q=last_name:Smith
```

查询的结果如下：

```json
{
  "took": 156,
  "timed_out": false,
  "num_reduce_phases": 2,
  "_shards": {
    "total": 666,
    "successful": 557,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": 2,
    "max_score": 0.2876821,
    "hits": [
      {
        "_index": "megacorp",
        "_type": "employee",
        "_id": "2",
        "_score": 0.2876821,
        "_source": {
          "first_name": "Jane",
          "last_name": "Smith",
          "age": 32,
          "about": "I like to collect rock albums",
          "interests": [
            "music"
          ]
        }
      },
      {
        "_index": "megacorp",
        "_type": "employee",
        "_id": "1",
        "_score": 0.2876821,
        "_source": {
          "first_name": "John",
          "last_name": "Smith",
          "age": 25,
          "about": "I love to go rock climbing",
          "interests": [
            "sports",
            "music"
          ]
        }
      }
    ]
  }
}
```

## DSL Query【推荐使用】

ElasticSearch 针对 Query-string 搜索的局限性，提供了一个非常丰富灵活的查询语言叫做*查询表达式*，它支持构建更加复杂和健壮的查询。

领域特定语言（DSL），使用 JSON 构建一个请求。示例如下：

```http
GET http://192.168.186.50:9200/_search
Content-Type: application/json;charset=UTF-8

{
  "query": {
    "match": {
      "last_name": "Smith"
    }
  }
}
```

查询到的结果跟使用 Query-string 的结果是一样的，此处就不贴结果了。