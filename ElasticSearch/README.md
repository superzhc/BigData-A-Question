# Elasticsearch

## 简介

Elasticsearch 是一个分布式的开源搜索和分析引擎，适用于所有类型的数据，包括文本、数字、地理空间、结构化和非结构化数据。Elasticsearch 在 Apache Lucene 的基础上开发而成，以其简单的 REST 风格 API、分布式特性、速度和可扩展性而闻名

### 用途

Elasticsearch 在速度和可扩展性方面都表现出色，而且还能够索引多种类型的内容，这意味着其可用于多种用例：

- 应用程序搜索
- 网站搜索
- 企业搜索
- 日志处理和分析
- 基础设施指标和容器监测
- 应用程序性能监测
- 地理空间数据分析和可视化
- 安全分析
- 业务分析

### 优势

**Elasticsearch 很快。** 由于 Elasticsearch 是在 Lucene 基础上构建而成的，所以在全文本搜索方面表现十分出色。Elasticsearch 同时还是一个近实时的搜索平台，这意味着从文档索引操作到文档变为可搜索状态之间的延时很短，一般只有一秒。因此，Elasticsearch 非常适用于对时间有严苛要求的用例，例如安全分析和基础设施监测。

**Elasticsearch 具有分布式的本质特征。** Elasticsearch 中存储的文档分布在不同的容器中，这些容器称为*分片*，可以进行复制以提供数据冗余副本，以防发生硬件故障。Elasticsearch 的分布式特性使得它可以扩展至数百台（甚至数千台）服务器，并处理 PB 量级的数据。

**Elasticsearch 包含一系列广泛的功能。** 除了速度、可扩展性和弹性等优势以外，Elasticsearch 还有大量强大的内置功能（例如数据汇总和索引生命周期管理），可以方便用户更加高效地存储和搜索数据。

## 相关概念

### 节点(Node)

> 一个 Elasticsearch 的运行实例就是一个节点。

### 集群(Cluster)

> 包含一个或多个拥有相同集群名称的节点，其中包含一个主节点。

### 索引(Index)

业务理解：

> 类似关系型数据中的表（*注：网上可能说是类似数据库，在6.x后，type被废弃，关于这个理解我理解为表更合理*），保存一个类型的数据

底层理解：

> 索引是指向一个或者多个物理分片的逻辑命名空间。

### 文档(Document)

> Elasticsearch使用文档来存储数据，文档使用 JSON 格式，该格式支持复杂的数据结构，可以良好的表达复杂的对象。
>
> 类似于数据库中的一条记录。

### 分片(Shard)

> Elasticsearch 上的索引是分布在一个或多个分片上的，这也就是说，分片是实际的存储单元且每个分片上只保存一部分索引数据，所有的分片的数据合在一起才是一个完整的 Elasticsearch 索引。
>
> Elasticsearch 基于 Lucene，一个 Shard 是一个 Lucene 实例，被 Elasticsearch 自动管理。

在 Elasticsearch 中，Shard 包括**主分片(primary shard)** 和**分本分片(replica shard)**。

### 副本

> 在网络环境中可能随时会遇到故障，为了保证数据的可用性，Elasticsearch 可以设置一个或多个索引的分片的副本，且这些副本是和分片分布在不同的节点上的。

## 安装

[Elasticsearch安装](ElasticSearch/Elasticsearch安装.md) 

## 配置参数

[Elasticsearch配置参数](./Elasticsearch配置参数.md)

## 搜索

Elasticsearch 搜索提供了两种方式：一个是通过使用 REST request URI 发送搜索参数，另一个是通过使用 REST request body 来发送搜索请求。请求体方法是使用一个具有可读性的 JSON 格式中定义搜索，因此本教程中只说明通过请求体的搜索，不对搜索参数的方式做说明。

Elasticsearch 提供的通过 JSON 进行查询的 DSL 被称作 **Query DSL**。该查询语言非常全面，且可读性比较高。

### 搜索请求的基本模块

```json
{
    "query":{
        "match":{
            "_all":"test"
        }
    },
    "from":0,
    "size":10,
    "_source":["name","sex","age","description"],
    "sort":[
        {"created_on":"asc"},
        {"name":"desc"},
        "_score"
    ],
    "highlight":{
        "fields":{
            "高亮的字段名1":{}
        }
    }
}
```

#### query

这是搜索请求中最重要的组成模块，它配置了基于评分返回的最佳文档，也包括了不希望返回哪些文档。

该模块使用查询 DSL 和过滤器 DSL 来配置，如下所示：

- [match_all](./Elasticsearch搜索-match_all.md)
- [term/terms](./Elasticsearch搜索-term-terms.md)
- [match](./Elasticsearch搜索-match.md)
- [multi_match](./Elasticsearch搜索-multi_match.md)
- [query_string](./Elasticsearch搜索-query_string.md)
- [simple_query_string](./Elasticsearch搜索-simple_query_string.md)
- 模糊查询
  - [wildcard](./Elasticsearch搜索-wildcard.md)
  - [regexp](./Elasticsearch搜索-regexp.md)
  - [fuzzy](./Elasticsearch搜索-fuzzy.md)
- [range](./Elasticsearch搜索-range.md)
- [bool](./Elasticsearch搜索-bool.md)

#### size

返回文档的数量

#### from

是 size 一起使用，from 用于分页操作。

#### \_source

指定 `_source` 字段如何返回。默认完整返回的 `_source` 字段，通过配置 `_source`，将过滤返回的字段。如果索引的文档很大，而且无须结果中的全部内容，就使用这个功能。

该配置是对于每个匹配文档而言，ElasticSearch 所应该返回的字段列表。

#### sort

默认的排序是基于文档的得分。可以指定字段进行升序、降序排序。

#### highlight

根据查询的关键字来进行高亮，高亮的结果会显示在返回结果中，关键字会被加上 `<em>` 标签。

#### 响应结果

[响应结果说明](./Elasticsearch搜索-响应结果.md) 