# Elasticsearch 安装

> 前提：
>
> 1. Elasticssearch 依赖 Java8+ 环境，需提前安装 JDK，关于 JDK 的安装不作赘叙
> 2. 本文安装的 ElasticSearch 版本是 6.3.2

## 下载和安装

Elasticsearch 提供了 `.zip` 和 `.tar.gz` 软件包下载，这些软件包可用于在任何系统上安装Elasticsearch。

### 下载和安装 `.zip` 包

```bash
# 下载软件包
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.zip
# 比较 SHA
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.zip.sha512
shasum -a 512 -c elasticsearch-6.3.2.zip.sha512 
# 解压软件包
unzip elasticsearch-6.3.2.zip
cd elasticsearch-6.3.2/
```

### 下载和安装 `.tar.gz` 包

```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.tar.gz
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.tar.gz.sha512
shasum -a 512 -c elasticsearch-6.3.2.tar.gz.sha512 
tar -xzf elasticsearch-6.3.2.tar.gz
cd elasticsearch-6.3.2/
```

## 运行

Elasticsearch 运行直接通过如下命令进行启动：

```bash
./bin/elasticsearch
```

通过上述方式启动，默认的是前台启动的，可以通过 `Ctrl-C` 来停止应用。

也可以作为守护进程来启动，如下所示：

```bash
./bin/elasticsearch -d
```

关闭应用可以通过获取进程的 pid，使用命令 `kill -9 <pid>` 来关闭进程

## 验证

验证 Elasticsearch 节点是否成功运行，可通过发送一个 Http 请求来进行验证：

```http
GET http://localhost:9200/
```

成功运行将返回类似如下的响应：

```json
{
  "name" : "Cp8oag6",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "AT69_T_DTp-1qgIJlatQqA",
  "version" : {
    "number" : "6.3.2",
    "build_flavor" : "default",
    "build_type" : "zip",
    "build_hash" : "f27399d",
    "build_date" : "2016-03-30T09:51:41.449Z",
    "build_snapshot" : false,
    "lucene_version" : "7.3.1",
    "minimum_wire_compatibility_version" : "1.2.3",
    "minimum_index_compatibility_version" : "1.2.3"
  },
  "tagline" : "You Know, for Search"
}
```



