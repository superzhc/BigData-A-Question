# fuzzy

fuzzy 是模糊查询

fuzzy 的搜索请求参数如下：

- value ：查询的关键字
- boost：设置查询的权值，默认是 1.0
- min_similarity：设置匹配的最小相似度，默认值为 0.5；对于字符串，取值为 0~1（包括 0 和 1）；对于数值，取值可能大于 1；对于日期类型，取值为 1d，2d，1m 这样，1d 表示一天
- prefix_length：指明区分词项的共同前缀长度，默认是 0
- max_expansions：指明查询中的词项可扩展的数目，默认可以无限大

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "fuzzy":{
            "k1":{
                "value":"zhangsnn",
                "fuzziness":2
            }
        }
    }
}
```