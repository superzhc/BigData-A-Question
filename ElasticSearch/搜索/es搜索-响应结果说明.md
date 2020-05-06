# 响应结果说明

示例：

```http
GET http://localhost:9200/superz/_search
```

得到的响应结果一般如下所示：

```json
{
  "took": 2,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 1.0,
    "hits": [
      {
        "_index": "superz",
        "_type": "test",
        "_id": "FSkX53ABV-8UrARgh4Dt",
        "_score": 1.0,
        "_source": {
          "name": "superz1",
          "sex": "男",
          "note": "this is a demo1"
        }
      }
    ]
  }
}
```

## took

> took 值反映执行整个搜索请求耗费了多少毫秒

## timed_out

> timed_out 值反映查询是否超时。默认情况下，搜索请求不会超时。如果低响应时间比完成结果更重要，则可以指定 timeout：
>
> ```http
> GET http://localhost:9200/superz/_search?timeout=10ms
> ```
>
> 在请求超时之前，elasticsearch 将会返回已经成功从每个分片获取的结果。

**注意**：应当注意的是 `timeout` 不是停止执行查询，它仅仅是告知正在协调的节点返回到目前为止收集的结果并且关闭连接。在后台，其他的分片可能仍在执行查询即使是结果已经被发送了。

## _shards

> _shards 部分返回的是在查询中参与分片的总数，以及这些分片成功了多少个，失败了多少个。正常情况下用户不希望分片失败，但是分片失败是可能发生的。如果用户遭遇到一种灾难级别的故障，在这个故障中丢失了相同分片的原始数据和副本，那么对这个分片将没有可用副本来对搜索请求作出响应。假若这样，Elasticsearch 将报告这个分片是失败的，但是会继续返回剩余分片的结果。

## hits

> 返回结果中最重要的部分就是 hits，它包含的 total 字段来表示匹配到的文档总数，并且一个 hits 数组包含所查询结果的前十个文档（*在未使用分页的情况下*）。
>
> 在 hits 数组中每个结果包含文档的 `_index` 、 `_type` 、 `_id` ，加上 `_source` 字段。这意味着用户可以直接从返回的搜索结果中使用整个文档。
>
> 每个结果还有一个 `_score` ，它衡量了文档与查询的匹配程度。默认情况下，首先返回最相关的文档结果，就是说，返回的文档是按照 `_score` 降序排列的。
>
> `max_score` 值是与查询所匹配文档的 `_score` 的最大值。