# Hive 配置参数

## Hive 在 HDFS 中的存储路径配置

属性 `hive.metastore.warehouse.dir` 配置文件系统中使用哪个路径来存储 Hive 表中的数据。（这个值会追加到 Hadoop 配置文件中所配置的属性 `fs.default.name` 的值）用户可以根据需要为这个属性指定任意的目录路径。

`hive.metastore.warehouse.dir` 配置项的默认值是 `/usr/hive/warehouse`

也可以为这个属性指定不同的值来允许每个用户定义其自己的数据仓库目录，这样就可以避免影响其他系统用户。因此，用户可能需要使用如下语句来为其自己指定数据仓库目录：

```sql
set hive.metastore.warehouse.dir=/user/myname/hive/warehouse
```

## 配置连接元数据存储库（metastore server）

配置Hive连接元数据存储库的相关属性

| 序号  | 参数项                                  | 描述                                                       |
| :---: | --------------------------------------- | ---------------------------------------------------------- |
|   1   | `javax.jdo.option.ConnectionURL`        | 连接字符串，任何一个适用JDBC进行连接的数据库都作元数据存储 |
|   2   | `javax.jdo.option.ConnectionDriverName` | 驱动包名称                                                 |
|   3   | `javax.jdo.option.ConnectionUserName`   | 用户名                                                     |
|   4   | `javax.jdo.option.ConnectionPassword`   | 密码                                                       |

> 为了使 Hive 能够连接上 MySQL，需要将 JDBC 驱动放置在 Hive 的库路径下，也就是 `$HIVE_HOME/lib` 目录下

## 配置 HiveServer

| 参数项                          | 默认值 | 描述                                         |
| :------------------------------ | :----: | -------------------------------------------- |
| `hive.server2.thrift.bind.host` |        | 绑定要在其上运行HiveServer2 Thrift服务的主机 |
| `hive.server2.thrift.port`      |        | HiveServer2 Thrift的对外端口号               |