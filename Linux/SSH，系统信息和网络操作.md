[toc]

### `bg`：列出所有被停止或后台运行的任务，或将一个已停止的任务后台运行。

### `cal`：输出当前月份的日历

### `date`：输出当前日期和时间

### `df`：输出磁盘使用统计数据

### `dig`：输出某个域名的 DNS 信息

```sh
dig domain
```

### `du`：输出某些文件或目录的硬盘使用情况

```sh
du [option] [filename|directory]
```

Options:

- `-h` (人类可读) 把结果以 KB、 MB 、GB 为单位输出。
- `-s` (压缩总结) 输出一个目录总的磁盘空间占用情况，总结输出子目录的报告。

示例：

```sh
$ du -sh pictures
1.4M pictures
```

### `fg`:输出前台中最近运行的任务

### `finger`:输出某个用户的信息

```sh
finger username
```

### `jobs`:列出在后台运行的任务，同时给出任务号

### `last`:列出特定用户的登录记录

```sh
last yourUsername
```

### `man`:输出特定命令的使用手册

```sh
man command
```

### `passwd`:让当前登录的用户更改他的密码

### `ping`:ping 某个主机然后输出结果

```sh
ping host
```

### `ps`:列出某个用户的所有进程

```sh
ps -u yourusername
```

### `quota`:显示磁盘使用量和配额

```sh
quota -v
```

### `scp`:在本地主机和远程主机之间或两个远程主机之间传输文件

*从本地主机复制文件到远程主机*

```sh
scp source_file user@host:directory/target_file
```

*从远程主机复制文件到本地主机*

```sh
scp user@host:directory/source_file target_file
scp -r user@host:directory/source_folder target_folder
```

这个命令也接受一个参数 `-P`，用来连接指定端口

```sh
scp -P port user@host:directory/source_file target_file
```

### `ssh`

ssh(SSH 客户端)是一个用来登录到远程主机并执行命令的程序

```sh
ssh user@host
```

这个命令也接受一个可选参数 `-p`，用来指定连接到特定的端口。

```sh
ssh -p port user@host
```

### `top`:动态展示所有活跃的进程

### `uname`:输出内核信息

```sh
uname -a
```

### `uptime`：输出服务器运行了多长时间以及有多少个用户登录

### `w`

输出系统在线用户

### `wget`:下载文件

```
wget file
```

### `whoami`

输出现在登录的用户的用户名

### `whois`

获取某个域名的 whois 信息

```
whois domain
```