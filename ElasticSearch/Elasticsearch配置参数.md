# Elasticsearch 配置参数

## `elasticsearch.yml`

| 参数                               | 描述                                                         |
| :--------------------------------- | ------------------------------------------------------------ |
| `cluster.name`                     | 配置的集群名称，默认是 elasticsearch。Elasticsearch 服务会通过广播方式自动连接在同一网段下的 Elasticsearch 服务，通过多播方式进行通信，同一网段下可以有多个集群，通过集群名称这个属性来区分不同的集群。 |
| `node.name`                        | 当前 Elasticsearch 节点的名称。当创建 Elasticsearch 集群时，保证同一个集群中的节点的 `cluster.name` 是相同的，`node.name` 节点名称是不同的。 |
| `node.master`                      | 指定该节点是否有资格被选举为 master，默认是 true。           |
| `node.data`                        | 指定该节点是否存储索引数据，默认为 true。                    |
| `path.data`                        | 设置索引数据的存储路径，默认是 Elasticsearch 安装目录下的 data 文件夹，可以设置多个存储路径，用逗号隔开，如 `path.data:/var/es/data1,/var/es/data2`。 |
| `path.log`                         | 设置日志文件的存储路径，默认是 Elasticsearch 安装目录下的 logs 文件夹。 |
| `bootstrap.mlockall`               | 设置为 true 锁住内存不进行 swapping。该配置设置为 true 可以提高 Elasticsearch 的效率。 |
| `http.port`                        | 设置对外服务的 http 端口，默认为 9200。                      |
| `action.auto_create_index`         | 是否自动创建索引                                             |
| `action.destructive_requires_name` | 删除是否只限于特定名称指向的数据，不允许使用通配符来进行删除 |

### `jvm.options`

| 参数 | 描述 |
| ---- | ---- |
|      |      |

