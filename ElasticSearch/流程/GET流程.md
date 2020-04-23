# GET 流程

GET/MGET 流程必须指定三元组：`_index`、`_type`、`_id`

![image-20200421105153829](D:\superz\BigData-A-Question\ElasticSearch\流程\images\image-20200421105153829.png)

## GET 基本流程

读取单个文档的流程如下图所示：

![image-20200421105255334](D:\superz\BigData-A-Question\ElasticSearch\流程\images\image-20200421105255334.png)

1. 客户端向 NODE1 发送读请求
2. NODE1 使用文档 ID 来确定文档属于分片 0，通过集群状态中的内容路由表信息获知分片 0 有三个副本数据，位于所有的三个节点中，此时它可以将请求发送到任意节点，这里它将请求转发到 NODE2
3. NODE2 将文档返回给 NODE1，NODE1 将文档返回给客户端

NODE1 作为协调节点，会将客户端请求轮询发送到集群的所有副本来实现负载均衡