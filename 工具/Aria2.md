# Aria2

aria2 是一个轻量级的支持多种协议，支持多种下载源的命令行下载工具，它支持HTTP/HTTPS, FTP, SFTP, BitTorrent（bt） 和 Metalink（磁力）。

aria2 能够在第三方的图形界面上使用内建的JSON-RPC 和 XML-RPC 来控制，有非常好的扩展性。

官网地址：<http://aria2.sourceforge.net>
github地址：<https://github.com/aria2/aria2>

## 安装

从<https://github.com/aria2/aria2/releases>上找到适合的版本来下载aria2，下载完成后解压到自己指定的位置，aria2下的目录如下：

```
aria2
  |
  |--aria2c.exe
  |--AUTHORS
  |--ChangeLog
  |--COPYING
  |--LICENSE.OpenSSL
  |--NEWS
  |--README.html
  |--README.mingw
```

需要新建如下几个文件：

1. `aria2.log`：日志，空文件就行
2. `aria2.session`：下载历史，空文件就行
3. `aria2.conf`：配置文件
4. `aria2.vbs`：隐藏cmd窗口运行用到的

**设置aria2的配置：`aria2.conf`**

```conf
## '#'开头为注释内容, 选项都有相应的注释说明, 根据需要修改 ##
## 被注释的选项填写的是默认值, 建议在需要修改时再取消注释  ##

## 文件保存相关 ##
# 文件的保存路径(可使用绝对路径或相对路径), 默认: 当前启动位置
dir=D:\download\aria2
# 启用磁盘缓存, 0为禁用缓存, 需1.16以上版本, 默认:16M
disk-cache=32M
# 文件预分配方式, 能有效降低磁盘碎片, 默认:prealloc
# 预分配所需时间: none < falloc ? trunc < prealloc
# falloc和trunc则需要文件系统和内核支持
# NTFS建议使用falloc, EXT3/4建议trunc, MAC 下需要注释此项
file-allocation=falloc
# 断点续传
continue=true

## 下载连接相关 ##
# 最大同时下载任务数, 运行时可修改, 默认:5
max-concurrent-downloads=10
# 同一服务器连接数, 添加时可指定, 默认:1
max-connection-per-server=5
# 最小文件分片大小, 添加时可指定, 取值范围1M -1024M, 默认:20M
# 假定size=10M, 文件为20MiB 则使用两个来源下载; 文件为15MiB 则使用一个来源下载
min-split-size=10M
# 单个任务最大线程数, 添加时可指定, 默认:5
split=20
# 整体下载速度限制, 运行时可修改, 默认:0
#max-overall-download-limit=0
# 单个任务下载速度限制, 默认:0
#max-download-limit=0
```

**设置aria2的启动程序：`aria2.vbs`**:

```sh
CreateObject("WScript.Shell").Run "D:\soft\aria2\aria2c.exe --conf-path=aria2.conf",0
```

## 使用说明

**从网页下载**：

```sh
$ aria2c http://example.org/mylinux.iso
```

**从两个源头链接下载**：

```sh
$ aria2c http://a/f.iso ftp://b/f.iso
```

**在一个源头上面使用两个连接下载**：

```sh
$ aria2c -x2 http://a/f.iso
```

**BitTorrent BT下载**:

```sh
$ aria2c http://example.org/mylinux.torrent
```

**BitTorrent Magnet URI BT磁力下载**:

```sh
$ aria2c 'magnet:?xt=urn:btih:248D0A1CD08284299DE78D5C1ED359BB46717D8C'
```

**Metalink 磁力下载**:

```sh
$ aria2c http://example.org/mylinux.metalink
```

**从文件下载**（把要下载的链接都写到一下文件里面）

```sh
$ aria2c -i uris.txt
```