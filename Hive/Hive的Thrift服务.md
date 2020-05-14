> Hive 具有一个可选的组件叫做 HiveServer 或者 HiveThrift，其允许通过指定端口访问 Hive。
>
> Thrift是一个软件框架，其用于跨语言的服务开发。Thrift允许客户端使用包括Java、C++、Ruby和其他很多种语言，通过编程的方式远程访问Hive。

## 启动 Thrift Server

如果想启用 HiveServer，可以在后台启动执行这个 Hive 服务：

```sh
$ cd $HIVE_HOME
$ bin/hive --service hiveserver &
Starting Hive Thrift Server
```

检查 HiveServer 是否启动成功的最快捷方法就是使用 netstat 命令查看 10000 端口是否打开并监听连接：

```sh
$ netstat -nl | grep 10000
tcp 0 0 :::10000　　　　:::*　　　　LISTEN
```
