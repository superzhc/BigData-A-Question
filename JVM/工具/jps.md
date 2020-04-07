# jps

命令 jps 类似于 Linux 下的 ps，但它只用于列出 Java 的进程。

直接运行 jps 不加任何参数，可以列出 Java 程序的进程 ID 等信息。

![image-20200407143451769](D:\superz\BigData-A-Question\JVM\工具\images\image-20200407143451769.png)

jps 提供了一系列参数来控制它的输出内容。

参数 `-q` 指定 jps 只输出进程 ID，而不输出类的短名称：

![image-20200407143708125](D:\superz\BigData-A-Question\JVM\工具\images\image-20200407143708125.png)

参数 `-m` 用于输出传递给 Java 进程的参数：

```sh
jps -m
```

参数 `-l` 用于输出主函数的完整路径：

```sh
jps -l
```

参数 `-v` 可以显示传递给 JVM 的参数：

```sh
jps -v
```

