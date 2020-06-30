## 概述

Sqoop 是 Apache 旗下一款**Hadoop 和关系数据库服务器之间传送数据**的工具。它是用来从关系数据库如：MySQL，Oracle 到 Hadoop 的 HDFS，并从 Hadoop 的文件系统导出数据到关系数据库。

Sqoop：“ SQL 到 Hadoop 和 Hadoop 到 SQL”

核心的功能有两个：`导入、迁入`和`导出、迁出`

**导入数据**：MySQL，Oracle 导入数据到 Hadoop 的 HDFS、HIVE、HBASE 等数据存储系统

**导出数据**：从 Hadoop 的文件系统中导出数据到关系数据库 mysql 等 

Sqoop 的本质还是一个命令行工具

![img](../images/1228818-20180412130640231-449939615.png)

## 工作机制

将导入或导出命令翻译成 MapReduce 程序来实现 

在翻译出的 MapReduce 中主要是对 InputFormat 和 OutputFormat 进行定制

## 安装

==TODO==

## 使用

可以使用 `sqoop help`来查看，sqoop 支持哪些命令，查询到的结果如下所示：

| 命令                | 详细                                                   | 用法                                                         |
| ------------------- | ------------------------------------------------------ | ------------------------------------------------------------ |
| `codegen`           |                                                        |                                                              |
| `create-hive-table` | 将关系型数据库的表结果导入到 Hive 中，在 Hive 中创建表 |                                                              |
| `eval`              | 计算一个 SQL 申明并展示结果                            |                                                              |
| `export`            | 导出 Hadoop 上的数据到 关系型数据库                    |                                                              |
| `help`              | 列举所有可用的命令                                     | `sqoop help [COMMAND]`                                       |
| `import`            | 从数据库导入一个表到 HDFS 上                           | `sqoop import [通用参数] [TOOL-ARGS]`                        |
| `import-all-tables` | 导入所有的表到  HDFS                                   | `sqoop import-all-tables [通用参数] [TOOL-ARGS]`             |
| `import-mainframe`  | 导入所有库中的所有表到 HDFS                            |                                                              |
| `job`               |                                                        | `sqoop job [通用参数] [Job参数] [-- [<tool-name>] [TOOL-ARGS]]` |
| `list-databases`    | 列举一个数据库服务上所有的数据库                       | `sqoop list-databases [通用参数] [TOOL-ARGS]`                |
| `list-tables`       | 列举一个数据库可用的所有表                             | `sqoop list-tables [通用参数] [TOOL-ARGS]`                   |
| `merge`             | 进行增量导入的时候合并结果                             |                                                              |
| `metastore`         | 运行一个独立的 Sqoop 元数据库                          |                                                              |
| `version`           | 显示 Sqoop 的版本信息                                  | `sqoop version`                                              |

查询到这些支持的命令之后，如果不知道使用方式，可以使用 `sqoop help <command>` 的方式来查看某条具体命令的使用方式。

[Sqoop 使用指南](./Sqoop基本命令.md)

相关参数见：[参数列表](./参数列表.md)

## `sqoop import`详解 

Sqoop 在 import 时，需要指定 `split-by` 参数，Sqoop 根据不同的 `split-by` 参数值来进行切分，然后将切分出来的区域分配到不同 map 中。每个 map 中再处理数据库中获取的一行一行的值，写入到 HDFS 中。*同时 `split-by` 根据不同的参数类型有不同的切分方法，但 `-split-by` 对非数字类型的字段支持不好，一般使用主键及数字类型的字段。*

注意事项：

1. 数据类型的兼容性：由于数据源支持的类型和 Hive 本身可能存在不太一样，所以会存在类型转换的问题
2. 分隔符的兼容性

### 全量导入

==TODO==

### 增量导入

增量导入，首先需要知道监控哪一列，这列要从哪个值开始增量

```
--check-column 用来指定一些列
这些被指定的列的类型不能使用任意字符类型，如 char、varchar 等类型都是不可以的，常用的是指定的主键ID
--check-column 可以去指定多个列
```

- `--last-value`：设置从哪个值开始增量
- `--incremental`：增量的模式
  - `append` 获取大于某一列的所有数据
  - `lastmodified` 获取某个时间戳后修改的所有数据
    - `--append`：附加模式
    - `--merge-key`：合并模式

注意：**增量导入不能与 `--delete-target-dir` 一起使用，还有必须指定增量的模式**

### 示例

[示例](./场景验证.md)

## `sqoop export` 详解

==TODO==

## 优化 Sqoop 同步

Sqoop 同步速度大致取决于下面的几个因素：

- 数据源的读取速度
- HDFS 写入速度
- 数据倾斜程度

### 数据源的读取速度

Sqoop 可以通过参数控制并发读取的 Mapper 个数加快读取速度。

```sh
sqoop -m <mapper_num> ...
```

注：`-m` 并不是越大越高，并发数过高会将数据库拖慢，造成同步速度反而变慢。

Sqoop 默认会通过  JDBC 的 API 来读取数据。

### HDFS 写入速度

这个除了刚刚提供的控制并发数，还需要保证 Yarn 分配给 Sqoop 的资源充足，不要让资源成为同步的瓶颈。另外，当我们选择 Parquet/ORC 作为存储格式时，数据在写入的时候需要做大量的预计算，这个过程是比较消耗 CPU 和内存的，我们可以控制 MapReduce 参数，适当提高 Sqoop 的资源配额。

```bash
sqoop -Dmapreduce.map.cpu.vcores=4 -Dmapreduce.map.memory.mb=8192 ...
```

### 数据倾斜程度

Sqoop 默认的导入策略是根据主键进行分区导入的，具体的并发粒度取决于 `-m` 参数。如果主键不连续出现大幅度跳跃，就会导致 Sqoop 导入的时候出现严重的数据倾斜。

## 其他

[抽取工具选型方案](../JSYJZX/抽取工具.md)

