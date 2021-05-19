# Hadoop 常用端口配置

## HDFS

|            参数             |             描述              | 默认  |     配置文件     |        例子值         |
| :-------------------------: | :---------------------------: | :---: | :--------------: | :-------------------: |
|      `fs.default.name`      |     Namenode RPC交互端口      | 8020  | `core-site.xml`  | `hdfs://master:8020/` |
|     `dfs.http.address`      |     NameNode web管理端口      | 50070 | `hdfs- site.xml` |    `0.0.0.0:50070`    |
|   `dfs.datanode.address`    |      datanode　控制端口       | 50010 | `hdfs -site.xml` |    `0.0.0.0:50010`    |
| `dfs.datanode.ipc.address`  | datanode的RPC服务器地址和端口 | 50020 | `hdfs-site.xml`  |    `0.0.0.0:50020`    |
| `dfs.datanode.http.address` |  datanode的HTTP服务器和端口   | 50075 | `hdfs-site.xml`  |    `0.0.0.0:50075`    |  |  |

## MapReduce

|                参数                |          描述          | 默认  |     配置文件      |        例子值         |
| :--------------------------------: | :--------------------: | :---: | :---------------: | :-------------------: |
|        `mapred.job.tracker`        |  job-tracker交互端口   | 8021  | `mapred-site.xml` | `hdfs://master:8021/` |
|               `job`                |  tracker的web管理端口  | 50030 | `mapred-site.xml` |    `0.0.0.0:50030`    |
| `mapred.task.tracker.http.address` | task-tracker的HTTP端口 | 50060 | `mapred-site.xml` |    `0.0.0.0:50060`    |

## 其他

|             参数             |   描述    |         默认         | 配置文件 |     例子值      |
| :--------------------------: | :-------: | :------------------: | :------: | :-------------: |
| `dfs.secondary.http.address` | secondary | NameNode web管理端口 |  50090   | `hdfs-site.xml` | `0.0.0.0:50090` |
