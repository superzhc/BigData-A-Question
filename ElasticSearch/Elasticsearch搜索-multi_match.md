# multi_match

可以指定多个被查询字段

比如查询 title 和 preview 这两个字段都包含 elasticsearch 关键词的文档

```http
GET http://localhost:9200/superz/_search
{
	"query":{
		"multi_match":{
			"query":"elasticsearch",
			"fields":["title","preview"]
		}
	}
}
```