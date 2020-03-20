# 映射

## 核心简单域类型

Elasticsearch 支持如下简单域类型：

- 字符串：string
- 整数：byte，short，integer，long
- 浮点数：float，double
- 布尔型：boolean
- 日期：date

当用户索引一个包含新域的文档，Elasticsearch 会使用动态映射，通过 JSON 中基本数据类型，尝试猜测域类型，使用规则如下：

| **JSON type**                  | **域 type** |
| ------------------------------ | ----------- |
| 布尔型: `true` 或者 `false`    | `boolean`   |
| 整数: `123`                    | `long`      |
| 浮点数: `123.45`               | `double`    |
| 字符串，有效日期: `2014-09-15` | `date`      |
| 字符串: `foo bar`              | `string`    |

## 查看映射

通过 `_mapping` 可以查看 Elasticsearch 在一个或多个索引中的一个或多个类型的映射。

示例：

```http
GET /gb/_mapping/tweet
```

Elasticsearch 根据索引的文档，为域（也叫属性）动态生成映射

## 自定义映射

尽管在很多情况下基本域数据类型已经够用了，但用户经常需要为单独域自定义映射，特别是字符串。自定义映射允许执行下面的操作：

- 全文字符串域和精确值字符串域的区别
- 使用特定语言分析器
- 优化域以适应部分匹配
- 指定自定义数据格式

域最重要的属性是 `type`。对于不是 `string` 的域，一般只需要设置 `type`：

```json
{
    "number_of_clicks": {
        "type": "integer"
    }
}
```

默认，`string` 类型域会被认为包含全文，也就是说，它们的值在索引前，会通过一个分析器，针对这个域的查询在搜索前也会经过一个分析器。

`string` 域映射的两个最重要属性是 `index` 和 `analyzer`。

**index**

`index` 属性控制怎么索引字符串，它可以是下面三个值：

- `analyzed`：首先分析字符串，然后索引它。也就是全文索引这个域
- `not_analyzed`：索引这个域，所以它能够被搜索，但索引的是精确值，不会对它进行分析
- `no`：不索引这个域，这个域不会被搜索到。

`string` 域 `index` 属性默认是 `analyzed` 。如果想映射这个字段为一个精确值，需要设置它为 `not_analyzed` ：

```json
{
    "tag": {
        "type":     "string",
        "index":    "not_analyzed"
    }
}
```

**analyzer**

对于 `analyzed` 字符串，用 `analyzer` 属性指定在搜索和索引时使用的分析器。默认，Elasticsearch 使用 `standard` 分析器，但可以指定一个内置的分析器替代它，例如 `whitespace`、`simple` 和 `english`：

```json
{
    "tweet": {
        "type":     "string",
        "analyzer": "english"
    }
}
```

## 更新映射

当首次创建一个索引的时候，可以指定类型的映射。也可以使用 `/_mapping` 为新类型（或者为存在的类型更新映射）增加映射。

> NOTE：尽管可以增加一个存在的映射，**不能修改已经存在的域映射**。如果一个域的映射已经存在，那么该域的数据可能已经被索引，如果意图修改这个域的映射，索引的数据可能会出错，不能被正常的搜索。

可以更新一个映射来添加一个新域，但不能将一个存在的域从 `analyzed` 改为 `not_analyzed`。

示例：

```http
PUT /superz/_mapping/test
{
  "properties": {
    "age": {
      "type": "integer"
    }
  }
}
```

> TODO：新增string类型报错，好像没有这个类型了

## 测试映射

可以使用 `analyze` API 测试字符串域的映射

```http
GET /superz/_analyze
{
  "field": "name",
  "text": "aaa bbb ccc ddd eee fff ggg hhh iii jjjj kkk lll mmm"
}
```

