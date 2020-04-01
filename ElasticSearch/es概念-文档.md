# 文档

> Elasticsearch 是分布式的**文档存储**。它能存储和检索复杂的数据结构（序列化成为 JSON 文档）以实时的方式。换句话说，一旦一个文档被存储在 Elasticsearch 中，它就可以被集群中的任意节点检索到。

其实文档就是类似数据库里面的一条记录（或一行数据）。文档（Document）是索引信息的基本单位。

文档被序列化化成为 JSON 格式，物理存储在一个索引重

**在 Elasticsearch 中，每个字段的所有数据都是默认被索引的**。

## 文档元数据

Elasticsearch 的文档的元数据必须有以下三个：

- `_index`：文档所属索引名称
- ~~`_type`：文档的对象类型~~
  - 6.0.0 以前，一个索引可以设置多个 types
  - 6.0.0开始，被 Deprecated 了。一个索引只能包含一个 type，首选的类型名称为 `_doc`，这样索引 API 具有与 7.0 版本相同的路径
- `_id`：文档的唯一标识

> 根据上述的三个必须元数据元素就可以唯一确定 Elasticsearch 中的一个文档。

## 文档操作

### 新增文档

```http
PUT /superz2/_doc/1
{
    "first_name" : "John",
    "last_name" :  "Smith",
    "age" :        25,
    "about" :      "I love to go rock climbing",
    "interests": [ "sports", "music" ]
}
```

注意，路径 `/superz2/employee/1` 包含了三部分的信息：

- `superz2`：索引名称
- `1`：数据的唯一标识

### 更新文档

```http
POST /superz2/_doc/1/_update
{
	"doc":{"first_name":"Jane","age:20"}
}
```

### 判断文档是否存在

```http
HEAD /superz2/_doc/1
```

### 获取文档

```http
GET /superz2/_doc/1
```

返回结果包含了文档的一些元数据，以及 `_source` 属性。

### 删除文档

```http
DELETE /superz2/_doc/1
```

### 获取多个文档

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

如果想检索的数据都在相同的 `_index` 中（~~甚至相同的 `_type` 中~~），则可以在 URL 中指定默认的 `/_index` ~~或者默认的 `/_index/_type`~~，如下：

```http
GET /website/_doc/_mget
{
   "ids" : [ "2", "1" ]
}
```

### 批量操作

> `bulk` API 允许在单个步骤中进行多次 `create`、`index`、`update` 或 `delete` 请求。
>
> `bulk` 的请求体格式如下所示：
>
> ```json
> { action: { metadata }}\n
> { request body        }\n
> { action: { metadata }}\n
> { request body        }\n
> ...
> ```
>
> 这种格式类似一个有效的单行 JSON 文档 *流* ，它通过换行符(`\n`)连接到一起。注意两个要点：
>
> - 每行一定要以换行符(`\n`)结尾， 包括最后一行 。这些换行符被用作一个标记，可以有效分隔行
> - 这些行不能包含未转义的换行符，因为它们将会对解析造成干扰。这意味着这个 JSON 不能使用 pretty 参数打印
>
> `action/metadata` 行指定哪一个文档做什么操作。
>
> `action` 必须是以下选项之一：
>
> - `create`：如果文档不存在，那么就创建它
> - `index`：创建一个新文档或者替换一个现有的文档
> - `update`：部分更新一个文档
> - `delete`：删除一个文档
>
> `metadata` 应该指定被索引、创建、更新或者删除的文档的 `_index`、`_type` 和 `_id`
>
> `request body` 行由文档的 `_source` 本身组成，它是 `index`、`create` 和 `update` 操作所必须的，`delete` 操作是不需要的

示例：

```http
POST /_bulk
{ "delete": { "_index": "website", "_type": "blog", "_id": "123" }} 
{ "create": { "_index": "website", "_type": "blog", "_id": "123" }}
{ "title":    "My first blog post" }
{ "index":  { "_index": "website", "_type": "blog" }}
{ "title":    "My second blog post" }
{ "update": { "_index": "website", "_type": "blog", "_id": "123", "_retry_on_conflict" : 3} }
{ "doc" : {"title" : "My updated blog post"} }
```

> 对于操作的文档都是有相同的 `_index`  ~~和`_type`~~，可以类似 `_mget` 一样在 URL 中设置默认的 `_index`  ~~或者 `/_index/_type`~~

## 文档存储

当新增一个文档的时候，文档会被存储到一个主分片中。

文档分配到哪个分片是根据下面的公式决定的：

```txt
shard = hash(routing) % number_of_primary_shards
```

`routing`  是一个可变值，默认是文档的 `_id`，也可以设置成一个自定义的值。`routing` 通过 hash 函数生成一个数字，然后这个数字再除以 `number_of_primary_shards`（主分片的数量）后得到余数。这个分布在 `0` 到 `number_of_primary_shards - 1` 之间的余数就是所寻求的文档所在分片的位置。

> 根据上面可知，为什么要在创建索引的时候就确定好主分片的数量并且永远不会改变这个数量，因为如果数量变化了，那么之前路由的值都会失效，文档再也找不到了。

所有的文档 API（`get`、`index`、`delete`、`bulk`、`update` 以及 `mget`）都接受一个叫做 routing 的路由参数，通过这个参数用户可以自定义文档到分片的映射。

