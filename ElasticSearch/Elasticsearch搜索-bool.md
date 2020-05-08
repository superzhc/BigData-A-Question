# bool

bool 查询可以将多查询组合在一起，成为用户想要的布尔查询。它接收以下参数：

| 操作符   | 描述                                                         |
| -------- | ------------------------------------------------------------ |
| must     | AND 关系，文档 ***必须*** 匹配这些条件才能检索出来           |
| must_not | NOT 关系，文档 ***必须不*** 匹配这些条件才能检索出来         |
| should   | OR 关系，如果满足这些语句中的任意语句，将增加 `_score`，否则，无任何影响。它们主要用于修正每个文档的相关性得分 |
| filter   | ***必须*** 匹配，但它以不评分、过滤模式来进行。这些语句对评分没有贡献，只是根据过滤标准来排除或包含文件 |

```http
GET http://localhost:9200/superz/_search
{
	"query":{
        "bool": {
            "must":     { "match": { "title": "how to make millions" }},
            "must_not": { "match": { "tag":   "spam" }},
            "should": [
                { "match": { "tag": "starred" }},
                { "range": { "date": { "gte": "2014-01-01" }}}
            ],
            "filter":{
            	"range":{"price":{"lte":29.99}}
            }
        }
    }
}
```

> **TIP**：如果没有 must 语句，那么至少需要能够匹配其中的一条 should 语句。但如果存在至少一条 must 语句，则对 should 语句的匹配没有要求。