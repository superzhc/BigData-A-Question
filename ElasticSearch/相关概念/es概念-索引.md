# 索引

> 索引是指向一个或者多个*物理**分片***的逻辑命名空间。
>
> 一个分片是一个底层的工作单元，它仅保存了全部数据中的一部分
>
> Elasticsearch 是利用分片将数据分发到集群内各处的。分片是数据的容器，文档保存在分片内，分片又被分配到集群内的各个节点里。当集群规模扩大或者缩小时，Elasticsearch 会自动的在各节点中迁移分片，使得数据仍然均匀分布在集群里。

一个副本分片只是一个主分片的拷贝。副本分片作为硬件故障时保护数据不丢失的冗余备份，并为搜索和返回文档等读操作提供服务。

**在索引建立的时候就已经确定了主分片数，但是副本分片数可以随时修改**

## 操作索引

### 创建索引

索引的创建可以在请求体里面传入设置或类型映射，如下所示：

```http
PUT http://localhost:9200/superz1
{
	"settings":{ ... any settings ... }
	"mappings":{
		"_doc":{ ... any mappings ... }
	}
}
```

> 如果想禁止自动创建索引，可以通过在 `config/elasticsearch.yml` 的每个节点下添加如下的配置
>
> ```yml
> action.auto_create_index : false
> ```

**注意**：索引的名称必须都是小写，否则报 400 错误

### 删除索引

用以下的请求来删除索引：

```http
DELETE http://localhost:9200/superz1
```

也可以删除多个索引：

```http
DELETE http://localhost:9200/superz,superz1
DELETE http://localhost:9200/superz*
```

甚至删除全部索引

```http
DELETE http://localhost:9200/_all
DELETE http://localhost:9200/*
```

> 对于能够用单个命令删除所有数据可能会导致可怕的后果。如果想要避免意外的大量删除，可以在 `config/elasticsearch.yml` 进行如下的设置：
>
> ```yml
> action.destructive_requires_name : true
> ```
>
> 这个设置使删除只限于特定名称指向的数据，而不允许通过指定 `_all` 或通配符来删除指定索引库。

## 索引设置

可以通过修改配置来自定义索引行为。

下面是两个最重要的设置：

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

## 配置分析器

> 索引设置中 `analysis` 模块可以用来配置已经存在的分析器或针对用户的索引创建新的自定义分析器。

示例：创建一个新的分析器，叫做 `es_std`，并使用预定义的西班牙语停用词列表

```http
PUT /spanish_doc
{
	"settings":{
		"analysis":{
			"analyzer":{
				"es_std":{
					"type":"standard",
					"stopwords":"_spanish_"
				}
			}
		}
	}
}
```

`es_std` 分析器不是全局的，它仅仅存在于上例定义的 `spanish_doc` 索引中。为了使用 `analyze` API 来对它进行测试，必须使用特定的索引名：

```http
GET /spanish_doc/_analyze?analyzer=es_std
El veloz zorro marrón
```

## 自定义分析器

虽然 Elasticsearch 已经内置了一些分析器，然而在分析器上 Elasticsearch 真正强大之处在于，用户可以通过在一个适合用户的特定数据的设置之中组合字符过滤器、分词器、词汇单元过滤器来创建自定义的分析器。