# awk

## 简介

awk 是一个强大的文本分析工具。awk 是把文件逐行的读入，以空格为默认分隔符将每行切片，切开的部分再进行各种分析处理。

> 注：在平时可能会看到 gawk 命令，gawk 程序是 Unix 中原 awk 程序的 GUN 版本，现在平常使用的 awk 其实就是 gawk，可以通过查看 awk 命令存放的文职，gawk 是 awk 建立的一个软连接所指向的，所以在系统中使用 awk 或是 gawk 都是一样的。

## 使用方法

```sh
awk '/pattern/ + {action}' {filename}
```

尽管操作可能会很复杂，但语法总是这样，其中 pattern 表示 awk 在数据中查找的内容，而 action 是在找到匹配内容时所执行的一系列命令。花括号(`{}`)不需要在程序中始终出现，但它们用于根据特定的模式对一系列指令进行分组。

> - pattern 表示正则表达式，用斜杠括起来
> - 若 action 中有多个语句，以 `;` 进行分隔

awk 语言的最基本功能是在文件或者字符串中基于指定规则浏览和抽取信息，awk 抽取信息后，才能进行其他文本操作。完整的 awk 脚本通常用来格式化文本文件中的信息。

通常，awk 是以文件的一行为处理单位的。awk 每接收文件的一行，然后执行相应的命令，来处理文本。

**命令行使用方式**

```sh
awk [-F  field-separator]  'commands'  input-file(s)
# 其中，commands 是真正awk命令，[-F 域分隔符]是可选的。 input-file(s) 是待处理的文件。
# 在awk中，文件的每一行中，由域分隔符分开的每一项称为一个域。通常，在不指名-F域分隔符的情况下，默认的域分隔符是空格。
```

**awk 内置变量**

awk有许多内置变量用来设置环境信息，这些变量可以被改变，下面给出了最常用的一些变量：

```txt
ARGC               命令行参数个数
ARGV               命令行参数排列
ENVIRON            支持队列中系统环境变量的使用
FILENAME           awk浏览的文件名
FNR                浏览文件的记录数
FS                 设置输入域分隔符，等价于命令行 -F选项
NF                 浏览记录的域的个数
NR                 已读的记录数
OFS                输出域分隔符
ORS                输出记录分隔符
RS                 控制记录分隔符
```

**print 和 printf**

awk 中同时提供了 print 和 printf 两种打印输出的函数。

其中 print 函数的参数可以是变量、数值或者字符串。字符串必须用双引号引用，参数用逗号分隔。如果没有逗号，参数就串联在一起而无法区分。这里，逗号的作用与输出文件的分隔符的作用是一样的，只是后者是空格而已。

printf 函数，其用法和 c 语言中 printf 基本相似,可以格式化字符串,输出复杂时，printf 更加好用，代码更易懂。

## 示例

**入门示例**

![image-20200327171145043](https://i.loli.net/2020/03/27/GplO3TbLEnJNkHU.png)

awk 工作流程是这样的：读入有 `\n` 换行符分割的一条记录，然后将记录按指定的域分隔符划分域，填充域，`$0` 则表示所有域,​`$1`表示第一个域,​`$n` 表示第 n 个域。默认域分隔符是"**空白键**" 或 "**[tab]键**",所以`$1`表示登录用户，​`$3` 表示登录用户ip,以此类推。

**指定域分隔符**

```sh
cat /etc/passwd |awk  -F ':'  '{print $1}'  
```

这种是 `awk + action` 的示例，每行都会执行 `action{print $1}`

**指定多个域，并用 tab 键进行分隔**

```sh
cat /etc/passwd |awk  -F ':'  '{print $1"\t"$7}'
```

**action 操作添加前后操作**

```sh
cat /etc/passwd |awk  -F ':'  'BEGIN {print "name,shell"}  {print $1","$7} END {print "blue,/bin/nosh"}'
```

awk 工作流程是这样的：先执行 BEGING，然后读取文件，读入有 `\n` 换行符分割的一条记录，然后将记录按指定的域分隔符划分域，填充域，`$0` 则表示所有域,​`$1` 表示第一个域,​`$n` 表示第 n 个域,随后开始执行模式所对应的动作action。接着开始读入第二条记录······直到所有的记录都读完，最后执行END操作。 

**搜索指定关键字的所有行**

```sh
awk -F: '/root/' /etc/passwd
```

这种是pattern的使用示例，匹配了pattern(这里是root)的行才会执行action(没有指定action，默认输出每行的内容)

搜索指定关键字的所有行，并显示对应的 shell

```sh
awk -F: '/root/ {print $7}' /etc/passwd
```

**使用 awk 环境变量，打印相关信息**

```sh
awk  -F ':'  '{print "filename:" FILENAME ",linenumber:" NR ",columns:" NF ",linecontent:"$0}' /etc/passwd
```