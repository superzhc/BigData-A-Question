> - Shell 是一个用 C 语言编写的程序，它是用户使用 Linux 的桥梁
> - Shell 既是一种命令语言，又是一种程序设计语言
> - Shell 是指一种应用程序，这个应用程序提供了一个界面，用户通过这个界面访问 Linux 内核的服务

Shell 脚本（Shell script），是一种为 shell 编写的脚本程序，一般文件后缀为 `.sh`

本文档写的是 Bash，也就是 Bourne Again Shell（/bin/bash），因 Bash 是大多数 Linux 系统默认的 Shell。

在一般情况下，人们并不区分 Bourne Shell 和 Bourne Again Shell，所以，像 `#!/bin/sh`，它同样也可以改为 `#!/bin/bash`。

**`#!` 是一个约定的标记，它告诉系统这个脚本需要什么解释器来执行，即使用哪一种 Shell**。

## 运行 Shell 脚本的两种方法

**1、作为可执行程序**

```sh
chmod +x ./test.sh  #使脚本具有执行权限
./test.sh  #执行脚本
```

注意，一定要写成 **./test.sh**，而不是 **test.sh**，运行其它二进制的程序也一样，直接写 test.sh，linux 系统会去 PATH 里寻找有没有叫 test.sh 的，而只有 /bin, /sbin, /usr/bin，/usr/sbin 等在 PATH 里，你的当前目录通常不在 PATH 里，所以写成 test.sh 是会找不到命令的，要用 ./test.sh 告诉系统说，就在当前目录找。

**2、作为解释器参数**

这种运行方式是，直接运行解释器，其参数就是 shell 脚本的文件名，如：

```sh
/bin/sh test.sh
/bin/php test.php
```

这种方式运行的脚本，不需要在第一行指定解释器信息，写了也没用。

## 其他

### 解释器

在 Shell 脚本，`#!` 告诉系统其后路径所指定的程序既是解释此脚本文件的 Shell 解释器。`#!` 被称作 shebang（也称为 Hashbang）

`#!` 决定了脚本可以像一个独立的可执行文件一样执行，而不用在终端之前输入 `sh`、`bash`、`python` 等

```sh
# 以下两种方式都可以指定 shell 解释器为 bash，第二种方式更好
#!/bin/bash
#!/usr/bin/env bash
```

### 常见的环境变量

| 变量      | 描述                                               |
| --------- | -------------------------------------------------- |
| `$HOME`   | 当前用户的用户目录                                 |
| `$PATH`   | 用分号分隔的目录列表，shell 会到这些目录中查找命令 |
| `$PWD`    | 当前工作目录                                       |
| `$RANDOM` | 0 到 32767 之间的整数                              |
| `$UID`    | 数值类型，当前用户的用户 ID                        |
| `$PS1`    | 主要系统输入提示符                                 |
| `$PS2`    | 次要系统输入提示符                                 |

### 反引号的使用

会把反引号里面当作一条命令来执行

```sh
test=`date`
echo "The date and time are:$test"
```

## Shell 扩展

### 命令置换

命令置换允许用户对一个命令求值，并将其值置换到另一个命令或者变量赋值表达式中。当一个命令被 \` \` 或者 `$()` 包围时，命令置换将会执行。

举个例子：

```sh
now=`date +%T`
### or
now=$(date +%T)

echo $now ### 19:08:26
```

