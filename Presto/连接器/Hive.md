# Hive 连接器

在 `${PRESTO_HOME}/etc/catalog` 目录中创建一个文件 `hive.properties`，内容如下：

```properties
connector.name = hive-cdh4 
hive.metastore.uri = thrift://localhost:9083
```