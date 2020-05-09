# Elasticsearch 文档存储

## 路由一个文档到一个分片中

根据如下公式，创建文档的时候确定一个文档存放在哪个一个分片中：

```txt
shard = hash(routing) % number_of_primary_shards
```

routing 是一个可变值，默认是文档的 `_id`，也可以设置成一个自定义的值。 routing 通过 hash 函数生成一个数字，然后这个数字再除以 number_of_primary_shards（主分片的数量）后得到余数 。这个分布在 0 到 `number_of_primary_shards - 1` 之间的余数，就是所寻求的文档所在分片的位置。

## 新建、索引和删除文档

新建、索引和删除请求都是**写操作**，必须在主分片上完成后才能被复制到相关的的副本分片，如下图所示：

![image-20200509111555646](D:\superz\BigData-A-Question\ElasticSearch\images\image-20200509111555646.png)

1. 客户端向 Node1 发送写操作请求
2. 节点使用文档的 `_id` 确定文档属于分片 0。请求会被转发到 Node 3，因为分片 0的主分片目前被分配在 Node 3 上
3. Node 3 在主分片上面执行请求。如果成功了，它将请求并行转发到 Node 1 和 Node 2的副本分片上。一旦所有的副本分片都报告成功，Node 3 将向协调节点报告成功，协调节点向客户端报告成功。

## 读文档

![取回单个文档](D:\superz\BigData-A-Question\ElasticSearch\images\elas_0403.png)

1. 客户端向 Node 1 发送获取请求
2. 节点使用文档的 `_id` 来确定文档属于分片 0 。分片 0 的副本分片存在于所有的三个节点上。 在这种情况下，它将请求转发到 Node 2
3. Node 2 将文档返回给 Node 1，然后将文档返回给客户端

在处理读取请求时，协调结点在每次请求的时候都会通过轮询所有的副本分片来达到负载均衡。