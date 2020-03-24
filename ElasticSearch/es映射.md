# 映射

> Elasticsearch 中的映射（Mapping）用来定义一个文档，可以定义所包含的字段以及字段的类型、分词器及属性等。

映射可以分为**动态映射**和**静态映射**：

- 动态映射

  > Elasticsearch 中可以不需要事先定义映射（Mapping），文档写入 Elasticsearch 时，会根据文档字段自动识别类型，这种机制称之为动态映射

- 静态映射

  > 在 Elasticsearch 也可以事先定义好映射，包含文档及其类型等，这种方式称之为静态映射

## 字段类型

每个字段都有一个数据类型，类型如下：

- 简单类型，如：[`text`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/text.html), [`keyword`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/keyword.html), [`date`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/date.html), [`long`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/number.html), [`double`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/number.html), [`boolean`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/boolean.html) or [`ip`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/ip.html)
- 支持子 JSON 数据对象类型，如：[`object`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/object.html) or [`nested`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/nested.html)
- 特殊类型，如： [`geo_point`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/geo-point.html), [`geo_shape`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/geo-shape.html), or [`completion`](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/search-suggesters-completion.html)

### 字符串类型

有`text` 和 `keyword`2种 。其中 `text` 支持分词，用于全文搜索；`keyword` 不支持分词，用于聚合和排序。~~在旧的ES里这两个类型由`string`表示~~。

如果安装了 IK 分词插件，可以为 `text` 类型指定 IK 分词。一般来说，对于字符串类型，如果：

1. **模糊搜索+精确匹配**，一般是name或者title字段：

```
"name": {
        "type": "text",
        "analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      }
```

2. **模糊搜索**，一般是内容详情字段：

```
"content": {
        "type": "text",
        "analyzer": "ik_smart"
      }
```

3. **精确匹配**：

```
"name": {
        "type": "keyword"
      }
```

4. **不需要索引**：

```
"url": {
        "type": "keyword",
        "index": false
      }
```

#### 数字类型

支持 long，integer，short，byte，double，float，half_float，scaled_float。具体说明如下：

- long：带符号的64位整数，其最小值为`-2^63`，最大值为`(2^63)-1`。
- integer：带符号的32位整数，其最小值为`-2^31`，最大值为`(23^1)-1`。
- short：带符号的16位整数，其最小值为-32,768，最大值为32,767。
- byte：带符号的8位整数，其最小值为-128，最大值为127。
- double：双精度64位IEEE 754浮点数。
- float：单精度32位IEEE 754浮点数。
- half_float：半精度16位IEEE 754浮点数。
- scaled_float：缩放类型的的浮点数。需同时配置缩放因子(scaling_factor)一起使用。

对于整数类型（byte，short，integer和long）而言，应该选择这是足以使用的最小的类型。这将有助于索引和搜索更有效。

对于浮点类型（float、half_float和scaled_float），`-0.0`和`+0.0`是不同的值，使用`term`查询查找`-0.0`不会匹配`+0.0`，同样`range`查询中上边界是`-0.0`不会匹配`+0.0`，下边界是`+0.0`不会匹配`-0.0`。

其中`scaled_float`，比如价格只需要精确到分，`price`为`57.34`的字段缩放因子为`100`，存起来就是`5734`。优先考虑使用带缩放因子的`scaled_float`浮点类型。

#### 日期类型

类型为 `date`。

JSON 本身是没有日期类型的，因此 Elasticsearch 中的日期可以是：

- 包含格式化日期的字符串
- 一个13位long类型表示的毫秒时间戳（ milliseconds-since-the-epoch）
- 一个integer类型表示的10位普通时间戳（seconds-since-the-epoch）

在 Elasticsearch 内部，日期类型会被转换为 UTC（如果指定了时区）并存储为 long 类型表示的毫秒时间戳。

日期类型可以使用使用`format`自定义，默认缺省值：`"strict_date_optional_time||epoch_millis"`：

```
"postdate": {
      "type": "date",
      "format": "strict_date_optional_time||epoch_millis"
    }
```

`format` 有很多内置类型，这里列举部分说明：

- strict_date_optional_time, date_optional_time：通用的ISO日期格式，其中日期部分是必需的，时间部分是可选的。例如 "2015-01-01"或"2015/01/01 12:10:30"。其中`strict_`开头的表示严格的日期格式，这意味着，年、月、日部分必须具有前置0。
- epoch_millis：13位毫秒时间戳
- epoch_second：10位普通时间戳

当然也可以自定义日期格式，例如：

```
"postdate":{
      "type":"date",
      "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd"
    }
```

注意：如果新文档的字段的值与format里设置的类型不兼容，ES会返回失败。

#### 复杂类型

- 数组数据类型
  在 ElasticSearch 中，没有专门的数组（Array）数据类型，但是，在默认情况下，任意一个字段都可以包含0或多个值，这意味着每个字段默认都是数组类型，只不过，数组类型的各个元素值的数据类型必须相同。在ElasticSearch 中，数组是开箱即用的（out of box），不需要进行任何配置，就可以直接使用。，例如：
  - 字符型数组: `[ "one", "two" ]`
  - 整型数组：`[ 1, 2 ]`
  - 数组型数组：`[ 1, [ 2, 3 ]]` 等价于`[ 1, 2, 3 ]`

- 对象数据类型 object 对于单个JSON对象。JSON天生具有层级关系，文档可以包含嵌套的对象。
- 嵌套数据类型 nested 对于JSON对象的数组

#### Geo数据类型

- 地理点数据类型 geo_point 对于纬度/经度点
- Geo-Shape数据类型 geo_shape 对于像多边形这样的复杂形状

#### 专用数据类型

- IP数据类型 ip 用于IPv4和IPv6地址
- 完成数据类型 completion 提供自动完成的建议
- 令牌计数数据类型 token_count 计算字符串中的标记数
- mapper-murmur3 murmur3 在索引时计算值的哈希值并将它们存储在索引中
- 过滤器类型 接受来自query-dsl的查询
- join 数据类型 为同一索引中的文档定义父/子关系

## 查看映射

通过 `_mapping` 可以查看 Elasticsearch 在一个或多个索引中的一个或多个类型的映射。

示例：

```http
GET /superz,superz630/_mapping
```

## 动态映射

**动态映射规则**

| JSON数据    | 自动推测的类型         |
| ----------- | ---------------------- |
| null        | 没有字段被添加         |
| true或false | boolean型              |
| 小数        | float型                |
| 数字        | long型                 |
| 日期        | date或text             |
| 字符串      | text                   |
| 数组        | 由数组第一个非空值决定 |
| JSON对象    | object类型             |

## 静态映射

动态映射的自动类型推测功能并不是完全的正确，这就需要**静态映射**机制。静态映射需要事先指定字段类型。相比于动态映射，静态映射可以添加更加详细字段类型，更精准的配置信息等。

> 注意：mapping 生成后是不允许修改（包括删除）的，所以需要提前合理的定义 mapping。

### 新建映射

在首次创建一个索引的时候，可以指定字段来进行类型映射。

```http
PUT /superz630
{
  "mappings": {
    "_doc": {
      "properties": {
        "id": {
          "type": "integer"
        },
        "content": {
          "type": "text"
        },
        "created": {
          "type": "date",
          "format": "strict_date_optional_time||epoch_millis"
        }
      }
    }
  }
}
```

### 更新映射

对于已经添加了的索引，可以使用 `/_mapping` 为已存在的索引增加映射。

> NOTE：尽管可以增加一个已存在的索引的字段映射，**但不能修改索引中已经存在的字段映射**。如果一个字段的映射已经存在，那么该字段的数据可能已经被索引，如果意图修改这个字段的映射，索引的数据可能会出错，不能被正常的搜索。

```http
PUT /superz630/_mapping/_doc
{
  "properties": {
    "id": {
      "type": "integer"
    },
    "content": {
      "type": "text"
    },
    "created": {
      "type": "date",
      "format": "strict_date_optional_time||epoch_millis"
    }
  }
}
```

## 测试分析

### 测试索引下的字段的分词

可以使用 `_analyze` API 测试字符串域的映射

```http
GET /superz/_analyze
{
  "field": "name",
  "text": "aaa bbb ccc ddd eee fff ggg hhh iii jjjj kkk lll mmm"
}
```

### 测试分词器

```http
GET /superz630/_analyze
{
  "analyzer": "ik_max_word",
  "text": "天地不仁，万物皆为刍狗"
}
```

