# Hive 命令行

**命令行界面，也就是 CLI，是和 Hive 交互的最常用的方式**。使用 CLI，用户可以创建表，检查模式以及查询表等等。

> CLI 是胖客户端，其需要本地具有所有的 Hive 组件，包括配置，同时还需要一个 Hadoop 客户端及其配置。同时，其可作为 HDFS 客户端、MapReduce 客户端以及 JDBC 客户端（用于访问元数据库）进行使用。

### CLI选项

下面的命令显示了CLI所提供的选项列表。（这里显示的是Hive v0.8.\*和Hive v0.9.\*版本的输出）：

```sh
$ hive --help --service cli
log4j:WARN No such property [maxFileSize] in org.apache.log4j.DailyRollingFileAppender.
usage: hive
 -d,--define <key=value>          Variable subsitution to apply to hive
                                  commands. e.g. -d A=B or --define A=B
    --database <databasename>     Specify the database to use
 -e <quoted-query-string>         SQL from command line
 -f <filename>                    SQL from files
 -H,--help                        Print help information
    --hiveconf <property=value>   Use value for given property
    --hivevar <key=value>         Variable subsitution to apply to hive
                                  commands. e.g. --hivevar A=B
 -i <filename>                    Initialization SQL file
 -S,--silent                      Silent mode in interactive shell
 -v,--verbose                     Verbose mode (echo executed SQL to the
                                  console)
```

这个命令的一个简化版表示方式是`hive -h`。

对于Hive v0.7.\*版本，不支持`-d`、`--hivevar`和`-p`选项。

### 变量和属性

`--define key=value` 实际上和 `--hivevar key=value` 是等价的。二者都可以让用户在命令行定义用户自定义变量以便在Hive脚本种引用，以满足不同情况的执行。

当用户使用这个功能时，Hive会将这些键值对放到hivevar命名空间，这样可以和其他3种内置命名空间（hiveconf、system和env）进行区分。

Hive种变量和属性命名空间，如下表：

| 命名空间 | 使用权限  | 描述                                        |
| :------: | :-------: | ------------------------------------------- |
| hivevar  | 可读/可写 | （Hive v0.8.0以及之后的版本）用户自定义变量 |
| hiveconf | 可读/可写 | Hive相关的配置属性                          |
|  system  | 可读/可写 | Java定义的配置属性                          |
|   env    |  只可读   | Shell环境（例如bash）定义的环境变量         |

Hive变量内部是以Java字符串的方式存储的。用户可以在查询种引用变量。**Hive会先使用变量值替换掉查询的变量引用，然后才会将查询语句提交给查询处理器**。

在CLI种，可以**使用SET命令显示或者修改变量值**。

```sh
hive> set env:HOME;
env:HOME=/root

hive> set;
...非常多的输出信息，基本是大部分配置信息

hive> set -v;
...更多的输出信息
```

如果不加`-v`标记，set命令会打印出命令空间hivevar、hiveconf、system和env中所有的变量。使用`-v`标记则还会打印Hadoop中所定义的所有属性。

set命令还可用于给变量赋新的值。自定义变量赋值如下：

```sh
$ hive --define foo=bar

hive> set foo;
foo=bar

hive> set hivevar:foo;
hivevar:foo=bar

hive> set hivevar:foo=bar2;

hive> set foo;
foo=bar2

hive> set hivevar:foo;
hivevar:foo=bar2
```

前缀hivevar:是可选的。`--hivevar`标记和`--define`标记是相同的。

和hivevar变量不同，**用户必须使用`system:`或者`env:`前缀来指定系统属性和环境变量**。

### Hive中“一次使用”命令

用户可能有时期望执行一个或多个查询（使用分号分隔），执行结束后hive CLI立即退出。Hive提供了这样的功能，因为CLI可以接受`-e`命令这种形式。如下所示：

```sh
hive -e "select * from superz_test;"
```

增加`-S`选项可以开启静默模式，这样可去除一些无关紧要的输出信息，使用方式如下：

```sh
hive -S -e "select * from superz_test;"
```

**重要技巧**

> 当用户不能完整记清楚某个属性名时，可以通过如下的方式来模糊获取这个属性名而无需滚动set命令的输出结果进行查找，示例如下：
> ```sh
> hive -S -e "set" | grep warehouse
> ```

### 从文件中执行Hive查询

Hive中可以使用`-f`文件名方式执行指定文件中的一个或者多个查询语句。按照惯例，一般把这些Hive查询文件保存为以`.q`或者`.hql`后缀名的文件。

示例：

```sh
$ cat /user/superz/superz.hql
select * from superz_test;

$ hive -f /user/superz/superz.hql
# 或者在Hive Shell中用户可以使用source命令来执行一个脚本文件
hive> source /user/superz/superz.hql
```

Hive中约定使用src（代表“源表”）作为表名，这个约定来源于Hive源码中的单元测试中的写法，它会在所有测试开始前先创建一个名为src的表。

例如，在测试某个内置函数的时候，通常会写个查询语句，然后往这个函数中传入参数，如下面的示例所示：

```sh
hive> select xpath(\'<a><b id="foo">b1</b><b id="bar">b2</b></a>\',\'//@id\') from src limit 1;
[foo,bar]
```

上面的例子中src代表一个已经创建号的或者虚拟地创建了的名为src的表。

### hiverc文件

CLI选项中有个`-i`，这个选项允许用户指定一个文件，当CLI启动时，在提示符出现前会先执行这个文件。

Hive会自动在HOME目录下寻找 `.hiverc` 文件，而且会自动执行这个文件中的命令（如果文件中有的话）。对于用户需要频繁执行命令，使用这个文件时非常方便的。

示例：【一个`#HOME/.hiverc`文件中的内容】

```
ADD JAR /path/to/custom_hive_extensions.jar;
set hive.cli.print.current.db=true;
set hive.exec.mode.local.auto=true;
```

### 执行shell命令

用户不需要退出hive CLI就可以执行简单的bash shell命令。只要在命令前加上！并且以分号（;）结尾就可以。

Hive CLI中不能使用需要用户进行输入的交互式命令，而且不支持shell的“管道”功能和文件名的自动补全功能

### 在Hive内使用Hadoop的dfs命令

用户可以在Hive CLI中执行Hadoop的dfs命令，只需要将hadoop命令中的关键字hadoop关键字去掉，然后以分号结尾就可以了。

这种使用hadoop命令的方式实际上比其等价的在bash sehll中执行的hadoop dfs命令要更高效。因为后者每次都会启动一个新的JVM实例。而Hive会在同一个进程中执行这个命令。

用户可以通过如下命令查看dfs所提供的所有功能选项列表：

```sh
hive> dfs -help;
```