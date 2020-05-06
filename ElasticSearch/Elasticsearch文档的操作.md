# Elasticsearch 文档的操作

> 在 6.x 版本及以后的版本，type 的概念已经被废弃了，一个索引只能包含一个 type，且首选的类型名称是 `_doc`，因此为了兼容后续的版本，统一使用 `_doc` 作为 type 名称

## 新增文档

```http
PUT http://localhost:9200/superz2/_doc/1
{
    "first_name" : "John",
    "last_name" :  "Smith",
    "age" :        25,
    "about" :      "I love to go rock climbing",
    "interests": [ "sports", "music" ]
}
```

- `superz2`：索引的名称
- `1`：索引的唯一标识，可不填，那样会自动生成索引的唯一标识

## 删除文档

```http
DELETE http://localhost:9200/superz2/_doc/1
```

## 更新文档

```http
POST http://localhost:9200/superz2/_doc/1/_update
{
	"doc":{"first_name":"Jane","age:20"}
}
```

## 获取文档

```http
GET http://localhost:9200/superz2/_doc/1
```

返回结果包含了文档的元数据，以及 `_source` 属性

## 判断文档是否存在

```http
HEAD http://localhost:9200/superz2/_doc/1
```

## 获取多个文档

```http
GET /_mget
{
   "docs" : [
      {
         "_index" : "website",
         "_type" :  "_doc",
         "_id" :    2
      },
      {
         "_index" : "website",
         "_type" :  "_doc",
         "_id" :    1,
         "_source": "views"
      }
   ]
}
```

若在同一个索引下获取多个文档，则可以在 URL 中指定默认的索引，如下所示：

```http
GET /website/_doc/_mget
{
	"ids":[2,1]
}
```

## 批量操作

`bulk` API 允许在单个步骤中进行多次 `create`、`index`、`update` 或 `delete` 请求。

`bulk` 的请求体格式如下所示：

```json
{ action: { metadata }}\n
{ request body        }\n
{ action: { metadata }}\n
{ request body        }\n
...
```

这种格式类似一个有效的单行 JSON 文档 *流* ，它通过换行符(`\n`)连接到一起。注意两个要点：

- 每行一定要以换行符(`\n`)结尾， 包括最后一行 。这些换行符被用作一个标记，可以有效分隔行
- 这些行不能包含未转义的换行符，因为它们将会对解析造成干扰。

`action/metadata` 行指定哪一个文档做什么操作。

`action` 必须是以下选项之一：

- `create`：如果文档不存在，那么就创建它
- `index`：创建一个新文档或者替换一个现有的文档
- `update`：部分更新一个文档
- `delete`：删除一个文档

`metadata` 指定被索引、创建、更新或者删除的文档的 `_index` 和 `_id`

`request body` 行由文档的 `_source` 本身组成，它是 `index`、`create` 和 `update` 操作所必须的，`delete` 操作是不需要的

示例：

```http
POST /_bulk
{ "delete": { "_index": "website", "_type": "_doc", "_id": "123" }} 
{ "create": { "_index": "website", "_type": "_doc", "_id": "123" }}
{ "title":    "My first blog post" }
{ "index":  { "_index": "website", "_type": "_doc" }}
{ "title":    "My second blog post" }
{ "update": { "_index": "website", "_type": "_doc", "_id": "123", "_retry_on_conflict" : 3} }
{ "doc" : {"title" : "My updated blog post"} }
```

同样的，若操作的是同一个索引，可在 URL 中设置默认索引。