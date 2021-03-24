# scp 命令

Linux scp 命令用于 Linux 之间复制文件和目录。

scp 是 secure copy 的缩写， scp 是 linux 系统下基于 ssh 登陆进行安全的远程文件拷贝命令。

scp 是加密的，rcp 是不加密的，scp 是 rcp 的加强版。

## 语法

```bash
scp 
    [-1246BCpqrv] 
    [-c cipher] 
    [-F ssh_config] 
    [-i identity_file]
    [-l limit] 
    [-o ssh_option] 
    [-P port] 
    [-S program]
[[user@]host1:]file1 
    [...] 
[[user@]host2:]file2
```

**简易写法**

```bash
scp [可选参数] file_source file_target 
```

**参数说明**:

- `-1`： 强制 scp 命令使用协议 ssh1
- `-2`： 强制 scp 命令使用协议 ssh2
- `-4`： 强制 scp 命令只使用 IPv4 寻址
- `-6`： 强制 scp 命令只使用 IPv6 寻址
- `-B`： 使用批处理模式（传输过程中不询问传输口令或短语）
- `-C`： 允许压缩。（将 `-C` 标志传递给 ssh，从而打开压缩功能）
- `-p`：保留原文件的修改时间，访问时间和访问权限。
- `-q`： 不显示传输进度条。
- `-r`： 递归复制整个目录。
- `-v`：详细方式显示输出。scp 和 ssh(1) 会显示出整个过程的调试信息。这些信息用于调试连接，验证和配置问题。
- `-c cipher`： 以 cipher 将数据传输进行加密，这个选项将直接传递给 ssh。
- `-F ssh_config`： 指定一个替代的 ssh 配置文件，此参数直接传递给 ssh。
- `-i identity_file`： 从指定文件中读取传输时使用的密钥文件，此参数直接传递给 ssh。
- `-l limit`： 限定用户所能使用的带宽，以 Kbit/s 为单位。
- `-o ssh_option`： 如果习惯于使用 ssh_config(5) 中的参数传递方式
- `-P port` ：注意是大写的 P， port是指定数据传输用到的端口号
- `-S program`： 指定加密传输时所使用的程序。此程序必须能够理解 ssh(1) 的选项。

## 实例

### 从本地复制到远程

**命令格式**

```bash
scp local_file remote_username@remote_ip:remote_folder 
# 或者 
scp local_file remote_username@remote_ip:remote_file 
# 或者 
scp local_file remote_ip:remote_folder 
# 或者 
scp local_file remote_ip:remote_file 
```

- 第 1，2 个指定了用户名，命令执行后需要再输入密码，第 1 个仅指定了远程的目录，文件名字不变，第 2 个指定了文件名；
- 第 3，4 个没有指定用户名，命令执行后需要输入用户名和密码，第 3 个仅指定了远程的目录，文件名字不变，第 4 个指定了文件名；

**示例**

```bash
scp /home/space/music/1.mp3 root@www.runoob.com:/home/root/others/music 
scp /home/space/music/1.mp3 root@www.runoob.com:/home/root/others/music/001.mp3 
scp /home/space/music/1.mp3 www.runoob.com:/home/root/others/music 
scp /home/space/music/1.mp3 www.runoob.com:/home/root/others/music/001.mp3 
```

**复制目录命令格式**

```bash
scp -r local_folder remote_username@remote_ip:remote_folder 
# 或者 
scp -r local_folder remote_ip:remote_folder 
```

- 第 1 个指定了用户名，命令执行后需要再输入密码；
- 第 2 个没有指定用户名，命令执行后需要输入用户名和密码；

**示例**

```bash
scp -r /home/space/music/ root@www.runoob.com:/home/root/others/ 
scp -r /home/space/music/ www.runoob.com:/home/root/others/ 
```

### 从远程复制到本地

从远程复制到本地，只要将从本地复制到远程的命令的后 2 个参数调换顺序即可，如下示例：

```bash
scp root@www.runoob.com:/home/root/others/music /home/space/music/1.mp3 
scp -r www.runoob.com:/home/root/others/ /home/space/music/
```