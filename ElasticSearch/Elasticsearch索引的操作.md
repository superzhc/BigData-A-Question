# Elasticsearch 索引操作

## 创建索引

索引的创建可以在请求体里面传入设置和类型映射，也可以什么配置都不写，直接使用默认的配置，如下所示：

```http
PUT http://localhost:9200/index_name
{
	"settings":{ ... any settings ... }
	"mappings":{
		"_doc":{ ... any mappings ... }
	}
}
```

> 如果禁止自动创建索引，可以通过在 `config/elasticsearch.yml` 的每个节点下添加如下的配置：
>
> ```yml
> action.auto_create_index: false
> ```

**注意**：索引的名称必须都是小写，否则报 400 错误

## 删除索引

```http
// 删除单个索引
DELETE http://localhost:9200/superz1
// 删除多个索引
DELETE http://localhost:9200/superz1,superz2
// 使用通配符删除多个索引
DELETE http://localhost:9200/superz*
// 删除全部索引
DELETE http://localhost:9200/_all
DELETE http://localhost:9200/*
```

> 对于能够用单个命令删除所有数据可能会导致可怕的后果，若要避免意味的大量删除，可以在 `config/elasticsearch.yml` 进行如下配置：
>
> ```yml
> action.destructive_requires_name : true
> ```

## 索引设置

可以通过修改配置来自定义索引。下面是两个最重要的设置：

- `number_of_shards`：每个索引的主分片数，默认值是 `5`。这个配置在索引创建后不能修改。
- `number_of_replicas`：每个主分片的副本数，默认值是 `1`。对于活动的索引库，这个配置可以随时修改。

示例：创建一个 5 个主分片，副本数为 2 的小索引：

```http
PUT http://localhost:9200/superz1
{
  "settings": {
    "number_of_shards": 5,
    "number_of_replicas": 2
  }
}
```

动态修改副本数：

```http
PUT http://localhost:9200/superz/_settings
{
	"number_of_replicas": 1
}
```

> 注：**不能动态修改索引的分片数**。