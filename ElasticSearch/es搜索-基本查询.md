## term

> term 查询被用于精确值匹配，这些精确值可以时数字、日期、布尔值、未经过分析的字符串；term 查询对于输入的文本不分析，所以它将给定的值进行精确查询

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

> terms 查询和 term 查询是一样的，但它允许指定多值进行匹配。
>
> 如果这个字段包含了指定值中的任何一个值，那么这个文档满足条件和 term 查询一样，terms 查询对于输入的文本不分析。

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

## match

> match 查询可接收文字、数字、日期等数据类型
>
> match 查询的时候，elasticsearch 会根据给定的字段提供合适的分词器，而 term 查询不会有分析器分析的过程

```http
GET http://localhost:9200/superz/_search
{
  "query": {
    "match": {"name": "superz1"}
  }
}
```

## match_all

> 通过 match_all 查询，查询指定索引下的所有文档

```http
GET http://localhost:9200/superz/_search
{
  "query": {
    "match_all": {}
  }
}
```

## match_phrase

> 短语查询，slop 定义的是关键词之间隔多少未知单词

```http
GET http://localhost:9200/superz/_search
{
	"query":{
		"match_phrase":{
			"mark":{
				"query":"hello elasticsearch",
				"slop":0
			}
		}
	}
}
```

## multi_match

> 可以指定多个被查询字段

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

## prefix

> prefix 前缀匹配查询

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "prefix":{
            "k1":"v1"
        }
    }
}
```

## range

> range 查询找出那些落在指定区间内的数字或时间
>
> 被允许的操作符如下：
> - `gt`：大于
> - `gte`：大于等于
> - `lt`：小于
> - `lte`：小于等于

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "range":{
            "age":{
                "gte":20,
                "lt":30
            }
        }
    }
}
```

range 范围查询可以定义日期格式

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "range":{
            "d1":{
                "gte":"2020-03-01",
                "lte":"2021",
                "format":"yyyy-MM-dd||yyyy"
            }
        }
    }
}
```

## exists/missing

> `exists` 查询和 `missing` 查询被用于查找那些指定字段中有值（`exists`）或无值（`missing`）的文档。这与SQL中的 `IS_NULL` (`missing`) 和 `NOT IS_NULL` (`exists`) 在本质上具有共性

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "exists":{
            "field":"k1"
        }
    }
}
```

## wildcard

> wildcard 通配符查询，允许使用通配符 `*` 和 `?` 来进行查询
>
> - `*`：代表一个或多个字符
> - `?`：仅代表一个字符
>
> 注意：**这个查询很影响性能**

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

## regexp

> regexp 正则表达式查询

```http
GET http://localhost:9200/superz/_search
{
    "query":{
        "regexp":{
            "k1":"zh.*san"
        }
    }
}
```

## fuzzy

> fuzzy 模糊查询
>
> - value ：查询的关键字
>- boost：设置查询的权值，默认是 1.0
> - min_similarity：设置匹配的最小相似度，默认值为 0.5；对于字符串，取值为 0~1（包括 0 和 1）；对于数值，取值可能大于 1；对于日期类型，取值为 1d，2d，1m 这样，1d 表示一天
> - prefix_length：指明区分词项的共同前缀长度，默认是 0
> - max_expansions：指明查询中的词项可扩展的数目，默认可以无限大

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

## fuzzy_like_this

> 查询得到与给定内容相似的所有文档
>
> - fields：字段组，默认是 `_all`
> - like_text：设置关键词
> - ignore_tf：设置忽略词项的词频，默认是false
> - max_query_terns：指明在生成的查询中查询词项的最大数目，默认是 25
> - min_similarity：指明区分词项最小的相似度，默认是 0.5
> - prefix_length：指明区分词项共同前缀的长度，默认是 0
> - boost：设置权值，默认是 1.0
> - analyze：指明用于分析给定内容的分词器

```http
GET http://localhost:9200/superz/_search
{
	"query":{
		"fuzzy_like_this":{
			"fields":["title","preview"],
			"like_text":"hello elasticsearch",
			"min_similarity":0.5,
			"prefix_length":0.2
		}
	}
}
```

## fuzzy_like_this_field

> 只作用在一个字段里，其他与 fuzzy_like_this 功能一样

```http
GET http://localhost:9200/superz/_search
{
	"query":{
		"fuzzy_like_this_field":{
			"preview":{
                "like_text":"hello elasticsearch",
                "min_similarity":0.5,
                "prefix_length":0.2
			}
		}
	}
}
```

## more_like_this

> - fields：定义字段组，默认是 `_all`
> - like_text：定义要查询的关键词
> - percent_terms_to_match：该参数指明一个文档必须匹配多大比例的词项才被视为相似。默认值是 0.3
> - min_term_freq：该参数指明在生成的查询中查询词项的最大数目。默认为 25
> - stop_words：该参数指明将被忽略的单词集合
> - min_doc_freq：该参数指明词项应至少在多少个文档中出现才不会被忽略。默认是 5
> - max_doc_freq：该参数指明出现词项的最大数目，以避免词项被忽略。默认是无限大
> - min_word_len：该参数指明单个单词的最小长度，低于该值的单词将被忽略，默认值是 0
> - max_word_len：指明单个单词的最大长度，高于该值的单词将被忽略，默认是无限大
> - boost_terms：该参数指明提升每个单词的权重时使用的权值。默认是 1
> - boost：指明提升一个查询的权值，默认是 1.0
> - analyze：指定用于分析的分析器

```http
GET http://localhost:9200/superz/_search
{
	"query":{
		"more_like_this":{
			"fields":["title","preview"],
			"like_text":"hello elasticsearch",
			"min_term_freq":1,
			"min_doc_freq":1
		}
	}
}
```

## more_like_this_field

> 只作用在一个字段里，其他与 more_like_this 功能一样

```http
GET http://localhost:9200/superz/_search
{
	"query":{
		"more_like_this_field":{
			"preview":{
                "like_text":"hello elasticsearch",
                "min_term_freq":1,
                "min_doc_freq":1
			}
		}
	}
}
```

## from 和 size 的使用

> - from：从哪个结果开始返回
> - size：定义返回最大的结果数

```http
GET http://localhost:9200/superz/_search
{
	"from":5,
	"size":10,
	"query":{
		"match":{
			"name":"superz"
		}
	}
}
```

## 指定返回的字段查询

```http
# 注意：只能返回 store 为 yes 的字段
GET http://localhost:9200/superz/_search
{
	"fields":["preview"],
	"query":{
		"match":{
			"preview":"test"
		}
	}
}
```

