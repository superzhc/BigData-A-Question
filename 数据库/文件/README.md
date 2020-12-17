<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-05-08 10:12:30
 * @LastEditTime : 2020-12-17 17:38:19
 * @Copyright 2020 SUPERZHC
-->
# 文件

## 参数文件

当 MySQL 实例启动时，数据库会先去读取一个配置参数文件，用来寻找**数据库的各种文件所在位置以及指定某些初始化参数，这些参数通常定义了某种内存结构有多大等**。

在默认情况下，MySQL 实例会按照一定的顺序在指定的位置进行读取。

MySQL 数据库的参数文件是以文本方式进行存储的。

从 MySQL 5.1 版本开始，可以通过 `information_schema` 数据库下的 `GLOBAL_VARIABLES` 视图来进行查找，如下所示：

```sql
use information_schema;
SELECT * FROM GLOBAL_VARIABLES Where VARIABLE_NAME like 'innodb_%'
```

也可以直接通过如下语句进行查询：

```sql
SHOW VARIABLES like 'innodb_%';
```

MySQL 数据库中的参数可以分为两类：
- 动态（dynamic）参数
- 静态（static）参数

动态参数意味着可以在 MySQL 实例运行中进行更改，静态参数说明在整个实例生命周期内都不得进行更改。

可以通过 SET 命令对动态的参数值进行修改，SET 语法如下：

```sql
SET
 | [global | session] system_var_name=expr
 | [@@global. | @@session. | @@] system_var_name=expr
```

global 和 session 关键字表明参数的修改是基于当前会话还是整个实例的生命周期。

## 日志文件

用来记录 MySQL 实例对某种条件做出响应时写入的文件，如错误日志文件、二进制日志文件、慢查询日志文件、查询日志文件等

详细见：[日志文件](./日志文件.md)

## socket 文件

当用 UNIX 域套接字方式进行连接时需要的文件

查看套接字文件的位置：

```sql
SHOW VARIABLES LIKE 'socket';
```

## pid 文件

当 MySQL 实例启动时，会将自己的进程 ID 写入一个文件中——该文件即为 pid 文件。该文件可由参数 `pid_file` 控制，默认位于数据库目录下，文件名为 **`主机名.pid`**：

```sql
SHOW VARIABLES LIKE 'pid_file';
```

## MySQL 表结构文件

因为 MySQL 插件式存储引擎的体系结构关系，MySQL 数据的存储是根据表进行的，每个表都会有与之对应的文件。但不论表采用何种存储引擎，MySQL 都有一个以 frm 为后缀名的文件，这个文件记录了该表的表结构定义。

frm 还用来存放视图的定义，如用户创建了一个 v_a 视图，那么对应地会产生一个 v_a.frm 文件，用来记录视图的定义，该文件是文本文件，可以直接使用 cat 命令进行查看。

## 存储引擎文件

因为 MySQL 表存储引擎的关系，每个存储引擎都会有自己的文件来保存各种数据。这些存储引擎真正存储了记录和索引等数据。

详细见：[InnoDB存储引擎文件](../InnoDB/InnoDB存储引擎文件.md) 