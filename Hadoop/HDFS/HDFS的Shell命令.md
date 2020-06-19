## HDFS 的 Shell 命令

## 命令基本格式

```sh
hadoop fs -cmd <args>
```

### ls

```sh
hadoop fs -ls /
```

列出hdfs文件系统根目录下的目录和文件

```sh
hadoop fs -ls -R /
```

列出hdfs文件系统所有的目录和文件


### put

```sh
hadoop fs -put <local file> <hdfs file>
```

hdfs file的父目录一定要存在，否则命令不会执行

```sh
hadoop fs -put <local file or dir>...<hdfs dir>
```

hdfs dir一定要存在，否则命令不会执行

```sh
hadoop fs -put -<hdfs file>
```

从键盘读取输入到hdfs file中，按`Ctrl+D`结束输入，hdfs file不能存在，否则命令不会执行

#### moveFromLocal

```sh
hadoop fs -moveFromLocal <local src> ... <hdfs dst>
```

与put类似，命令执行后源文件local src被删除，也可以从键盘读取输入到hdfs file中

#### copyFromLocal

```sh
hadoop fs -copyFromLocal <local src> ... <hdfs dst>
```

与put相类似，也可以从键盘读取输入到hdfs file中

### get

```sh
hadoop fs -get <hdfs file> <local file or dir>
```

local file不能和hdfs file名字相同，否则会提示文件已存在，没有重名的文件会复制到本地

```sh
hadoop fs -get <hdfs file or dir> ... <local dir>
```

拷贝多个文件或目录 到本地时，本地要为文件夹路径

#### moveToLocal

#### copyToLocal

```sh
hadoop fs -copyToLocal <hdfs file or dir> <local dir>
```

与get相类似

### rm

```sh
hadoop fs -rm <hdfs file>
hadoop fs -rm -r <hdfs dir>
```

删除一个或多个文件和目录

### mkdir

```sh
hadoop fs -mkdir <hdfs path>
```

只能一级一级的建目录，父目录不存在的话使用这个命令会报错

```sh
hadoop fs -mkdir -p <hdfs path>
```

所创建的目录如果父目录不存在就创建该父目录

### getmerge

```sh
hadoop fs -getmerge <hdfs dir> <local file>
```

将hdfs指定目录下所有文件排序后合并到本地指定的文件中，文件不存在时会自动创建，文件存在时会覆盖里面的内容

```sh
hadoop fs -getmerge -nl <hdfs dir> <local file>
```

加上nl后，合并到local file中的hdfs文件之间会空出一行


### cp

```sh
hadoop fs -cp <hdfs file> <hdfs file>
```

目标文件不能存在，否则命令不能执行，相当于给文件重命名并保存，源文件还存在

### mv

```sh
hadoop fs -mv <hdfs file> <hdfs file>
```

目标文件不能存在，否则命令不能执行，相当于给文件重命名并保存，源文件不存在

```sh
hadoop fs -mv <hdfs file or dir> ... <hdfs dir>
```

源路径有多个时，目标路径必须为目录，且必须存在。

### count

```sh
hadoop fs -count <hdfs path>
```

统计hdfs对应路径下的目录个数，文件个数，文件总计大小

显示为目录个数，文件个数，文件总计大小，输入路径

### du

```sh
hadoop fs -du <hdfs path>
```

显示hdfs对应路径下每个文件夹和文件的大小

```sh
hadoop fs -du -d <hdfs path>
```

显示hdfs对应路径下所有文件和的大小

```sh
hadoop fs -du -h <hdfs path>
```

显示hdfs对应路径下每个文件夹和文件的大小,文件的大小用方便阅读的形式表示，例如用64M代替67108864

### text

```sh
hadoop fs -text <hdfs file>
```

将文本文件或某些格式的非文本文件通过文本格式输出

### setrep

```sh
hadoop fs -setrep -R 3 <hdfs path>
```

改变一个文件在hdfs中的副本个数，上述命令中数字3为所设置的副本个数，-R选项可以对一个人目录下的所有目录+文件递归执行改变副本个数的操作

### stat

```sh
hadoop fs -stat [format] <hdfs path>
```

返回对应路径的状态信息

[format]可选有：

- `%b`：文件大小
- `%o`：Block大小
- `%n`：文件名
- `%r`：副本个数
- `%y`：最后一次修改日期和时间

可以这样书写`hadoop fs -stat %b%n%o <hdfs path>`，不过不建议，这样每个字符输出的结果不是太容易分清楚

### tail

```sh
hadoop fs -tail <hdfs file>
```

在标准输出中显示文件末尾的1KB的数据

### archive

```sh
hadoop archive -archiveName name.har -p <hdfs parent dir> <src>* <hdfs dst>
```

### balancer

```sh
hdfs balancer
```

如果管理员发现某些DataNode保存数据过多，某些DataNode保存数据相对较少，可以使用上述命令手动启动内部的均衡过程

### dfsadmin

```sh
hdfs dfsadmin -help
```

管理员可以通过dfsadmin管理HDFS，用法可以通过上述命令查看

```sh
hdfs dfsadmin -report
```

显示文件系统的基本数据

```sh
hdfs dfsadmin -safemode <enter | leave | get | wait >
```

- `enter`：进入安全模式
- `leave`：离开安全模式
- `get`：获知是否开启安全模式
- `wait`：等待离开安全模式

### distcp

用来在两个HDFS之间拷贝数据