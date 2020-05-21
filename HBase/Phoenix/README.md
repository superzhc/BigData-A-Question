## 简介

Apache Phoenix 是一个 HBase 的开源 SQL 引擎。可以使用标准的 JDBC API 代替 HBase 客户端 API 来创建表，插入数据，查询HBase数据。

Phoenix 提供了比自己手写的方式相同或更好的性能，通过以下方法：

- 编译查询的 SQL 为原生 HBase 的 scan 语句；
- 检测 scan 语句最佳的开始和结束的 key；
- 精心编排 scan 语句让他们并行执行；
- 让计算去接近数据通过；
- 推送编写的 WHERE 子句的谓词到服务端过滤器处理；
- 执行聚合查询通过服务端钩子（称为协同处理器）。

Phoenix 还提供了以下增强功能来更多的优化性能：

- 实现了二级索引来提升非主键字段查询的性能；
- 统计相关数据来提高并行化水平，并帮助选择最佳优化方案；
- 跳过扫描过滤器来优化 IN，LIKE，OR 查询；
- 优化主键的来均匀分布写压力。

> Apache Phoenix 官方站点：https://phoenix.apache.org/
> Phoenix 支持的sql语句： https://phoenix.apache.org/language/index.html
> Phoenix 支持的DataTypes：https://phoenix.apache.org/language/datatypes.html
> Phoenix 支持的函数：https://phoenix.apache.org/language/functions.html

## 安装

### 0、前提条件

Hadoop 集群、Zookeeper、HBase 都安装成功

### 1、 下载安装包

下载地址：<http://phoenix.apache.org/download.html>

> 注：根据 HBase 的版本下载对应版本的 Phoenix

### 2、配置

#### 2.1、将 `phoenix-core-4.x-HBase-1.x.jar` 复制到 HBase 集群中

将 Phoenix 目录下的 `phoenix-core-4.13.1-HBase-1.3.jar`、~~`phoenix-4.13.1-HBase-1.3-client.jar`~~ 拷贝到 HBase 集群各个节点 hbase 安装目录 lib 中

#### 2.2、设置 `HADOOP_CONF_DIR`

Phoenix 需要读取 HBase 集群所使用的 HDFS 环境。若 Phoenix 所在的服务器在 HDFS 的 node 上，则可直接指定为 `$HADOOP_HOME/conf` 目录，主要是读取该目录下的 `core-site.xml`、`hdfs-site.xml` 这两个文件。若不在 HDFS 的 node 上，需要到服务器上将这 2 个配置文件拷贝下来，配置所需要的参数。

#### 2.3、设置 `HBASE_CONF_DIR`

Phoenix 需要配置相关参数才能访问 HBase 集群。若 Phoenix 所在的服务器在 HBase 的 Master/RegionServer 上，则可直接配置 `$HBASE_HOME/conf` 目录，读取的是 `hbase-site.xml` 文件，该文件配置了访问 HBase 服务相关参数。若不在 HBase 的节点上，需要单独添加 `hbase-site.xml` 文件，添加 HBase 以及 HDFS 的相关配置项即可

#### 2.4、设置 `LD_LIBRARY_PATH`【可选】

如果HBase开启了Lzo/Snappy等压缩方式，需要指定客户端上的LD_LIBRARY_PATH环境变量来加载本地库，否则会出现数据读写异常。

#### 2.5、使用 Phoenix 的客户端 Jar 包

若用户需要使用 Phoenix 的 JDBC 方式来访问 HBase，只需要 `phoenix-4.x-HBase-1.x-client.jar` 这个 Jar 就可以了。这个Jar 包就在 Phoenix 的发行包下，将其添加到你的 Java 应用程序的 ClassPath 即可，访问 HBase 所需要的其他类都已经被包含在其中。需要注意的是，若直接使用 Hadoop Jar 方式来调用，在 Hadoop 版本不一致的情况下，可能会存在某些 Jar 包版本冲突的风险。这时可以使用 Java 直接来调用。

## 使用

一般可以使用以下三种方式访问 Phoenix：

- JDBC API
- 使用 Python 编写的命令行工具（`sqlline`，`sqlline-thin` 和 `psql` 等）
- SQuirrel