# es 配置信息

## 索引和文档的存储路径

es 上的所有的索引和文档数据是存储在本地的磁盘中，具体的路径可在 es 的配置文件 `{ES_HOME}/config/elasticsearch.yml` 中配置，配置如下：

```yml
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
path.data: /path/to/data
#
# Path to log files:
#
path.logs: /path/to/logs
```

