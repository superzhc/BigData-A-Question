<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-08-31 14:56:30
 * @LastEditTime : 2020-12-04 11:49:14
 * @Copyright 2020 SUPERZHC
-->
# Aria2

aria2 是一个轻量级的支持多种协议，支持多种下载源的命令行下载工具，它支持 HTTP/HTTPS, FTP, SFTP, BitTorrent（bt） 和 Metalink（磁力）。

aria2 能够在第三方的图形界面上使用内建的 JSON-RPC 和 XML-RPC 来控制，有非常好的扩展性。

- 官网地址：<http://aria2.sourceforge.net>
- github地址：<https://github.com/aria2/aria2>

## 安装

从 <https://github.com/aria2/aria2/releases> 上找到适合的版本来下载 aria2，下载完成后解压到自己指定的位置，aria2 下的目录如下：

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
4. `aria2.vbs`：隐藏 cmd 窗口运行用到的

**设置 aria2 的配置：`aria2.conf`**

完整选项说明请参考 [Aria2 Manual](https://aria2.github.io/manual/en/html/aria2c.html)

配置示例，可根据需要修改各项参数：

```conf
## '#'开头为注释内容, 选项都有相应的注释说明, 根据需要修改 ##
## 被注释的选项填写的是默认值, 建议在需要修改时再取消注释  ##

## 文件保存相关 ##
# 文件的保存路径(可使用绝对路径或相对路径), 默认: 当前启动位置
dir=~/downloads
# 启用磁盘缓存, 0为禁用缓存, 需1.16以上版本, 默认:16M
#disk-cache=32M
# 文件预分配方式, 能有效降低磁盘碎片, 默认:prealloc
# 预分配所需时间: none < falloc ? trunc < prealloc
# falloc和trunc则需要文件系统和内核支持
# NTFS建议使用falloc, EXT3/4建议trunc, MAC 下需要注释此项
#file-allocation=none
# 断点续传
continue=true

## 下载连接相关 ##
# 最大同时下载任务数, 运行时可修改, 默认:5
#max-concurrent-downloads=5
# 同一服务器连接数, 添加时可指定, 默认:1
max-connection-per-server=5
# 最小文件分片大小, 添加时可指定, 取值范围1M -1024M, 默认:20M
# 假定size=10M, 文件为20MiB 则使用两个来源下载; 文件为15MiB 则使用一个来源下载
min-split-size=10M
# 单个任务最大线程数, 添加时可指定, 默认:5
#split=5
# 整体下载速度限制, 运行时可修改, 默认:0
#max-overall-download-limit=0
# 单个任务下载速度限制, 默认:0
#max-download-limit=0
# 整体上传速度限制, 运行时可修改, 默认:0
#max-overall-upload-limit=0
# 单个任务上传速度限制, 默认:0
#max-upload-limit=0
# 禁用IPv6, 默认:false
#disable-ipv6=true
# 连接超时时间, 默认:60
#timeout=60
# 最大重试次数, 设置为0表示不限制重试次数, 默认:5
#max-tries=5
# 设置重试等待的秒数, 默认:0
#retry-wait=0

## 进度保存相关 ##
# 从会话文件中读取下载任务
input-file=/etc/aria2/aria2.session
# 在Aria2退出时保存`错误/未完成`的下载任务到会话文件
save-session=/etc/aria2/aria2.session
# 定时保存会话, 0为退出时才保存, 需1.16.1以上版本, 默认:0
#save-session-interval=60

## RPC相关设置 ##
# 启用RPC, 默认:false
enable-rpc=true
# 允许所有来源, 默认:false
rpc-allow-origin-all=true
# 允许非外部访问, 默认:false
rpc-listen-all=true
# 事件轮询方式, 取值:[epoll, kqueue, port, poll, select], 不同系统默认值不同
#event-poll=select
# RPC监听端口, 端口被占用时可以修改, 默认:6800
#rpc-listen-port=6800
# 设置的RPC授权令牌, v1.18.4新增功能, 取代 --rpc-user 和 --rpc-passwd 选项
#rpc-secret=<TOKEN>
# 设置的RPC访问用户名, 此选项新版已废弃, 建议改用 --rpc-secret 选项
#rpc-user=<USER>
# 设置的RPC访问密码, 此选项新版已废弃, 建议改用 --rpc-secret 选项
#rpc-passwd=<PASSWD>
# 是否启用 RPC 服务的 SSL/TLS 加密,
# 启用加密后 RPC 服务需要使用 https 或者 wss 协议连接
#rpc-secure=true
# 在 RPC 服务中启用 SSL/TLS 加密时的证书文件,
# 使用 PEM 格式时，您必须通过 --rpc-private-key 指定私钥
#rpc-certificate=/path/to/certificate.pem
# 在 RPC 服务中启用 SSL/TLS 加密时的私钥文件
#rpc-private-key=/path/to/certificate.key

## BT/PT下载相关 ##
# 当下载的是一个种子(以.torrent结尾)时, 自动开始BT任务, 默认:true
#follow-torrent=true
# BT监听端口, 当端口被屏蔽时使用, 默认:6881-6999
listen-port=51413
# 单个种子最大连接数, 默认:55
#bt-max-peers=55
# 打开DHT功能, PT需要禁用, 默认:true
enable-dht=false
# 打开IPv6 DHT功能, PT需要禁用
#enable-dht6=false
# DHT网络监听端口, 默认:6881-6999
#dht-listen-port=6881-6999
# 本地节点查找, PT需要禁用, 默认:false
#bt-enable-lpd=false
# 种子交换, PT需要禁用, 默认:true
enable-peer-exchange=false
# 每个种子限速, 对少种的PT很有用, 默认:50K
#bt-request-peer-speed-limit=50K
# 客户端伪装, PT需要
peer-id-prefix=-TR2770-
user-agent=Transmission/2.77
peer-agent=Transmission/2.77
# 当种子的分享率达到这个数时, 自动停止做种, 0为一直做种, 默认:1.0
seed-ratio=0
# 强制保存会话, 即使任务已经完成, 默认:false
# 较新的版本开启后会在任务完成后依然保留.aria2文件
#force-save=false
# BT校验相关, 默认:true
#bt-hash-check-seed=true
# 继续之前的BT任务时, 无需再次校验, 默认:false
bt-seed-unverified=true
# 保存磁力链接元数据为种子文件(.torrent文件), 默认:false
bt-save-metadata=true
```

**设置 aria2 的启动程序：`aria2.vbs`**:

```sh
CreateObject("WScript.Shell").Run "D:\soft\aria2\aria2c.exe --conf-path=aria2.conf",0
```

## 使用说明

**从网页下载**：

```bash
$ aria2c http://example.org/mylinux.iso
```

**从两个源头链接下载**：

```bash
$ aria2c http://a/f.iso ftp://b/f.iso
```

**在一个源头上面使用两个连接下载**：

```bash
$ aria2c -x2 http://a/f.iso
```

**BitTorrent BT 下载**:

```bash
$ aria2c http://example.org/mylinux.torrent
```

**BitTorrent Magnet URI BT 磁力下载**:

```bash
$ aria2c 'magnet:?xt=urn:btih:248D0A1CD08284299DE78D5C1ED359BB46717D8C'
```

**Metalink 磁力下载**:

```bash
$ aria2c http://example.org/mylinux.metalink
```

**从文件下载**（把要下载的链接都写到一下文件里面）

```bash
$ aria2c -i uris.txt
```

**断点续传规则**

aria2 对断点续传有很好的支持，只需要重新运行一次同样的下载命令即可， 加 `-c` 参数，开启断点续传功能。

```bash
# aria2c -c http://mirrors.kernel.org/gnu/gcc/gcc-5.1.0/gcc-5.1.0.tar.gz
[#575536 12MiB/116MiB(10%) CN:1 DL:878KiB ETA:2m1s]                                                                  
04/24 20:50:31 [NOTICE] Shutdown sequence commencing... Press Ctrl-C again for emergency shutdown.
04/24 20:50:31 [NOTICE] Download GID#5755367ab0bce05b not complete: /tmp/gcc-5.1.0.tar.gz
Download Results:
gid   |stat|avg speed  |path/URI
======+====+===========+=======================================================
575536|INPR|   816KiB/s|/tmp/gcc-5.1.0.tar.gz
Status Legend:
(INPR):download in-progress.
aria2 will resume download if the transfer is restarted.
If there are any errors, then see the log file. See '-l' option in help/man page for details.
```

## 开启 RPC 功能

后续的图形化界面都是基于 aria2 开启 RPC 功能才能使用的，开启 RPC 配置如下：

```conf
## RPC
#允许rpc
enable-rpc=true
#允许所有来源, web界面跨域权限需要
rpc-allow-origin-all=true
#允许非外部访问
rpc-listen-all=true
#RPC端口, 仅当默认端口被占用时修改
#rpc-listen-port=6800
```

## 图形化界面：AriaNg

[AriaNg 中文文档](http://ariang.mayswind.net/zh_Hans/)