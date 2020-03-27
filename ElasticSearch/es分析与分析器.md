# 分析与分析器

分析器的工作流程如下：

- 首先，将一块文本分成适合于倒排索引的独立的词条
- 之后，将这些词条统一化为标准格式以提高它们的可搜索行或者 recall

分析器实际上是将三个功能封装到了一个包里：

**字符过滤器**

首先，字符串按顺序通过每个字符过滤器。它们的任务是在分词前整理字符串。一个字符过滤器可以用来去掉 HTML，或者将 `&` 转换成 `and`

**分词器**

其次，字符串被分词器分成单个的词条。一个简单的分词器遇到空格和标点的时候，可能会将文本拆分成词条

**Token 过滤器**

最后，词条按顺序通过每个 token 过滤器。这个过程可能会改变词条（例如，小写化 `Quick`），删除词条（例如，像 `a`,`and`,`the` 等无用助词），或者增加词条（例如，像 `jump` 和 `leap` 这种同义词）

~~Elasticsearch 提供了开箱即用的字符过滤器、分词器和 Token 过滤器。这些可以组合起来形成自定义的分析器以用于不同的目的。~~

## 内置分析器

Elasticsearch 附带了多种可以直接使用的预包装的分析器。

为了证明它们的差异，分析下面的字符串在不同的分析器下得到那些词条：

```txt
Set the shape to semi-transparent by calling set_trans(5)
```

**标准分析器**

标准分析器是 Elasticsearch 默认使用的分析器。它是分析各种语言文本最常用的选择。它根据 Unicode 联盟定义的单词边界划分文本，删除绝大部分标点，最后将词条小写。

它会产生：

```txt
set, the, shape, to, semi, transparent, by, calling, set_trans, 5
```

**简单分析器**

简单分析器在任何不是字母的地方分隔文本，将词条小写。

它会产生：

```txt
set, the, shape, to, semi, transparent, by, calling, set, trans
```

**空格分析器**

空格分析器在空格的地方划分文本。

它会产生：

```txt
Set, the, shape, to, semi-transparent, by, calling, set_trans(5)
```

**语言分析器**

特定语言分析器可用于很多语言，它们可以考虑指定语言的特点。例如，英语分析器附带了一组英语无用词（常用单词，例如 `and` 或者 `the`，它们对相关性没有什么影响），它们会被删除。由于理解英语语法的规则，这个分词器可以提取英语单词的词干。

英语分词器会产生下面的词条：

```txt
set, shape, semi, transpar, call, set_tran, 5
```

注意：`transparent`、`calling` 和 `set_trans` 已经变为词根格式。

## 什么时候使用分词器

当用户索引一个文档，它的全文域被分析成词条以用来创建倒排索引。但是，当用户在全文域搜索的时候，需要将查询字符串通过相同的分析过程，以保证搜索的词条格式预索引中的词条格式一致。

全文查询，理解每个域是如何定义的，因此它们可以做正确的事：

- 当你查询一个 *全文* 域时， 会对查询字符串应用相同的分析器，以产生正确的搜索词条列表。
- 当你查询一个 *精确值* 域时，不会分析查询字符串，而是搜索你指定的精确值。

## 测试分析器

`analyze` API 提供了查看文本的分词结果，如下：

```http
GET /_analyze
{
	"analyzer":"standard",
	"text":"Text to analyze"
}
```

上面的请求中，在消息体里**指定分析器**和**要分析的文本**

返回结果如下：

```json
{
  "tokens": [
    {
      "token": "text",
      "start_offset": 0,
      "end_offset": 4,
      "type": "<ALPHANUM>",
      "position": 0
    },
    {
      "token": "to",
      "start_offset": 5,
      "end_offset": 7,
      "type": "<ALPHANUM>",
      "position": 1
    },
    {
      "token": "analyze",
      "start_offset": 8,
      "end_offset": 15,
      "type": "<ALPHANUM>",
      "position": 2
    }
  ]
}
```

- `token`：实际存储到索引中的词条
- `position`：指明词条在原始文本中出现的位置
- `start_offset`/`end_offset`：指明字符串在原始字符串中的位置

