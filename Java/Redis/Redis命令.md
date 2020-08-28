# Redis 命令

## 命令表格

| 序号 | 命令                 | 描述                                                         | 时间复杂度 |
| ---- | -------------------- | ------------------------------------------------------------ | ---------- |
| 1    | keys [pattern]       | 列出系统中所有的 key                                         | O(n)       |
| 2    | dbsize               | Redis 系统所有 key 的个数                                    | O(1)       |
| 3    | exists key           | Redis 系统是否存在该 key，存在返回1，否则返回0               | O(1)       |
| 4    | del key...           | 删除 Redis 中的 key，删除不存在的key返回0，否则返回key个数   | O(1)       |
| 5    | expire key seconds   | 设置 key 的过期时间，单位是秒，pexpire 单位是毫秒            | O(1)       |
| 6    | ttl key              | 显示 key 剩余的过期时间，返回 -1 表示没有设置过期时间，-2表示key已删除，pttl，单位是毫秒 | O(1)       |
| 7    | persist key          | 去掉 key 的过期时间                                          | O(1)       |
| 8    | type key             | 查看 key 的数据类型                                          | O(1)       |
| 9    | randomkey            | 随机返回系统中存在的key                                      |            |
| 10   | rename key1 newKey   | 给 key 重命名                                                |            |
| 11   | renamenx key1 newKey | 给 key 重命名，如果newKey已存在则重命名失败，返回0，否则返回1 |            |
| 12   | move key DBNO        | 将指定的key移动到指定的数据库，redis默认16个库，默认使用 0号库 |            |
| 13   | select DBNO          | 使用 redis 的某个数据库，类似于 mysql 的 use dbname          |            |

## 命令详解

### keys

`keys`这个命令的时间复杂度是O(N)，而且redis又是单线程执行，在执行`keys`时即使是时间复杂度只有O(1)例如SET或者GET这种简单命令也会阻塞，从而导致这个时间点性能抖动，设置可能出现timeout。

### scan

官方文档用法如下：

```bash
SCAN cursor [MATCH pattern] [COUNT count]
```

### slowlog

查看慢日志，通过使用命令`slowlog`，用法如下：

```sh
SLOWLOG subcommand [argument]
```

subcommand 主要有：
- **get**：用法：`slowlog get [argument]`，获取 argument 参数指定数量的慢日志。
- **len**：用法：`slowlog len`，总慢日志数量。
- **reset**：用法：`slowlog reset`，清空慢日志。

> 命令耗时超过多少才会保存到 slowlog 中，可以通过命令 `config set slowlog-log-slower-than 2000` 配置并且不需要重启 redis 。注意：单位是微妙，2000 微妙即 2 毫秒。

### rename-command

为了防止把问题带到生产环境，可以通过配置文件重命名一些危险命令，例如 keys 等一些高危命令。操作非常简单，只需要在 conf 配置文件增加如下所示配置即可：

```sh
rename-command flushdb flushddbb
rename-command flushall flushallall
rename-command keys keysys
```

### bigkeys

检查生产环境上一些有问题的数据。

`bigkeys`的用法如下：

```sh
redis-cli -p 6380 --bigkeys
```

### monitor

### info

### config

### set

## Key(键)

### DEL

**DEL key [key ...]**

删除给定的一个或多个 key。

不存在的 key 会被忽略。

**返回值**：被删除 key 的数量。

### DUMP

**DUMP KEY**

序列化给定 key，并返回被序列化的值，使用 RESTORE 命令可以将这个值反序列化为 Redis 键。

序列化生成的值有以下几个特点：
- 它带有 64 位的校验和，用于检测错误，RESTORE 在进行反序列化之前会先检查校验和
- 值的编码格式和 RDB 文件保持一致
- RDB 版本会被编码在序列化值当中，如果因为 Redis 的版本不同造成 RDB 格式不兼容，那么 Redis 会拒绝对这个值进行反序列化操作

序列化的值不包括任何生存时间信息

### EXISTS

**EXISTS key**

检查给定 key 是否存在。

**返回值**：若 key 存在，返回 1 ，否则返回 0 。

### EXPIRE

**EXPIRE key seconds**

为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。

### EXPIREAT

### KEYS

### MIGRATE

### MOVE

### OBJECT

### PERSIST

### PEXPIREAT

### PTTL

### RANDOMKEY

### RENAME

### RENAMENX

### RESTORE

### SORT

### TYPE

### SCAN