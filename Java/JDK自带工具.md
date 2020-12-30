<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-07-01 00:52:24
 * @LastEditTime : 2020-12-23 15:56:14
 * @Copyright 2020 SUPERZHC
-->
# JDK 的 bin 目录下 exe 的用途

## appletviewer

主要用于运行并浏览applet小程序。

![](http://ozchbp0v3.bkt.clouddn.com/jdk-appletviewer.png)

## extcheck

extcheck是JDK自带的jar文件检测工具，主要用于检测指定的jar文件与Java SDK安装的任何扩展之间是否存在版本冲突。在安装一个扩展之前，你可以使用此工具来检测是否已经安装该扩展的相同版本或最新版本。

extcheck工具通过将指定jar文件中清单文件(manifest)的Specification-title和Specification-version头信息与扩展目录中所有的jar文件对应的头信息进行比较，从而检测是否存在版本冲突。

> JDK(或JRE)默认的扩展安装目录为jre/lib/ext。extcheck工具通过使用与方法java.lang.Package.isCompatibleWith相同的方式来比较版本号。

如果没有检测出冲突问题，则返回代码为0。

如果扩展目录中任何jar文件的清单文件与指定jar文件有相同的Specification-title以及相同(或更新)的Specification-version版本号，将返回一个非0的错误码。如果指定jar文件的清单文件中没有Specification-title或Specification-version属性，也将返回非0的错误码。

extcheck工具的命令行用法为：

> extcheck [-verbose] [-J\<runtime flag\>] target.jar

其中：

- 可选参数-verbose表示显示详细的检测信息；
- 可选参数-J\<runtime flag\>可以有多个，用于指定可选的运行时参数，例如：-J-Xms48m。
- 参数target.jar表示指定的jar文件。

## idlj

## jabswitch

jabswitch，就是Java Access Bridge Switch的简称，用于控制Java访问桥的开/关。Java访问桥是一种技术，让Java应用程序实现Accessibility API，以供Microsoft Windows系统的辅助技术访问。

## jar

多用途的存档及压缩工具，是个java应用程序，可将多个文件合并为单个JAR归档文件。

用法：jar {ctxu}[vfm0M] [jar-文件] [manifest-文件] [-C 目录] 文件名 ...

选项：

- c 创建新的存档
- t 列出存档内容的列表
- x 展开存档中的命名的（或所有的〕文件
- u 更新已存在的存档
- v 生成详细输出到标准输出上
- f 指定存档文件名
- m 包含来自标明文件的标明信息
- 0 只存储方式；未用ZIP压缩格式
- M 不产生所有项的清单（manifest〕文件
- i 为指定的jar文件产生索引信息
- C 改变到指定的目录，并且包含下列文件：如果一个文件名是一个目录，它将被递归处理。清单（manifest〕文件名和存档文件名都需要被指定，按'm' 和 'f'标志指定的相同顺序。

示例1：将两个class文件存档到一个名为 'classes.jar' 的存档文件中：

> jar cvf classes.jar Foo.class Bar.class

示例2：用一个存在的清单（manifest）文件 'mymanifest' 将 foo/ 目录下的所有文件存档到一个名为 'classes.jar' 的存档文件中：

> jar cvfm classes.jar mymanifest -C foo/ .

## jarsigner

jar文件的签名和验证工具，主要用于为jar文件生成签名，并且验证已签名的jar文件的签名信息。

jarsigner的主要用法为：

```sh
#生成jar文件的签名
jarsigner [选项] jar文件 别名
#验证已签名的jar文件的签名信息和完整性
jarsigner -verify [选项] jar文件
```

JAR 功能使得类文件、图像、声音和其它数字化数据可打包到一个文件中，以便更方便快捷地发布。名为 jar 的工具使开发者可以生成 JAR 文件(从技术上来说，任何 zip 文件都可看作为 JAR 文件，尽管由 jar 创建或由 jarsigner 处理时，JAR 文件也包含一个 META-INF/MANIFEST.MF文件)。

## java

Java解释器，直接从类文件执行Java应用程序代码。

## javac

Java编译器，将Java源代码换成字节代。

用法：javac <选项> <源文件>

可能的选项包括：

- g,生成所有调试信息
- g:none,生成无调试信息
- g:{lines,vars,source},生成只有部分调试信息
- O,优化；可能妨碍调试或者增大类文件
- nowarn,生成无警告
- verbose,输出关于编译器正在做的信息
- deprecation,输出使用了不鼓励使用的API的源程序位置
- classpath <路径>,指定用户类文件的位置
- sourcepath <路径>,指定输入源文件的位置
- bootclasspath <路径>,覆盖自举类文件的位置
- extdirs <目录(多个)>,覆盖安装的扩展类的位置
- d <目录>,指定输出类文件的位置
- encoding <编码>,指定源文件中所用的字符集编码
- target <版本>,生成指定虚拟机版本的类文件
- help,Print a synopsis of standard options

## javadoc

根据Java源代码及其说明语句生成的HTML文档。

## javafxpackager

## javah

产生可以调用Java过程的C过程，或建立能被Java程序调用的C过程的头文件。

## javap

是JDK自带的反汇编工具，用于将Java字节码文件反汇编为Java源代码。

```
用法:javap <options> <classes>
可能的选项包括:
-help  --help  -?        打印用法信息
-version                 版本信息
-v  -verbose             打印附加信息
-l                       打印行号和本地变量表
-public                  仅显示public类和成员
-protected               显示protected/public类和成员
-package                 显示package/protected/public类和成员(默认值)
-p  -private             显示所有的类和成员
-c                       反汇编代码
-s                       打印内部类型签名
-sysinfo                 显示将被处理的类的系统信息(路径，大小，日期，MD5 哈希值)
-constants               显示static final常量
-classpath <path>        指定查找用户类文件的位置
-bootclasspath <path>    覆盖由引导类加载器所加载的类文件的位置
```

## javapackager

## java-rmi

## javaw

## javaws

## jcmd

用于向正在运行的JVM发送诊断信息请求。

## jconsole

JConsole是一个遵循JMX(Java Management Extensions)规范的图形用户界面的监测工具，主要用于监控并提供运行于Java平台的应用程序的性能和资源占用信息。

## jdb

jdb,意即Java Debugger,Java调试器，可以逐行地执行程序、设置断点和检查变量。

## jdeps

## jhat

jhat(Java Heap Analysis Tool)，是JDK自带的Java堆内存分析工具。

用法：

> jhat [ options ] \<heap-dump-file\>

## jinfo

jinfo(Java Configuration Information)，主要用于查看指定Java进程(或核心文件、远程调试服务器)的Java配置信息。配置信息包括Java系统属性、Java虚拟机命令行标识参数。

```sh
#指定进程号(pid)的进程
jinfo [ option ] pid
#指定核心文件
jinfo [ option ] <executable <core>
#指定远程调试服务器
jinfo [ option ] [server-id@]<remote-hostname-or-IP>
```

## jjs

## jli

## jmap

jmap是JDK自带的工具软件，主要用于打印指定Java进程(或核心文件、远程调试服务器)的共享对象内存映射或堆内存细节。

```sh
#指定进程号(pid)的进程
jmap [ option ] <pid>
#指定核心文件
jmap [ option ] <executable <core>
#指定远程调试服务器
jmap [ option ] [server-id@]<remote-hostname-or-IP>
```

## jmc

## jps

jps(JVM Process Status Tool)，主要用于显示Java进程的状态信息。

## jrunscript

## jsadebugd

## jstack

jstack，是一个堆栈跟踪工具，主要用于打印指定Java进程、核心文件或远程调试服务器的Java线程的堆栈跟踪信息。

```sh
#指定进程号(pid)的进程
jstack [ option ] <pid>
#指定核心文件
jstack [ option ] <executable <core>
#指定远程调试服务器
jstack [ option ] [server-id@]<remote-hostname-or-IP>
```

## jstat

jstat，是Java虚拟机的统计监测工具，主要用于显示JVM的性能统计性能。

## jstatd

jstatd，即虚拟机的jstat守护进程，主要用于监控JVM的创建与终止，并提供一个接口允许远程监控工具依附到在本地主机上运行的JVM。

## jvisualvm

jvisualvm，即Java VisualVM，是一个图形化界面的Java虚拟机监控工具。jvisualvm主要提供在Java虚拟机上运行的Java应用程序的详细信息。

使用jvisualvm，你可以详细查看虚拟机中每个Java应用程序，甚至每个线程、每个类、每个实例的相关信息，包括堆栈、字段、类型、方法值，以及启动时间、方法调用次数等。

## keytool

## kinit

## klist

## ktab

## native2ascii

将含有不是Unicode或Latinl字符的的文件转换为Unicode编码字符的文件。

## orbd

## pack200

## policytool

## rmic

## rmid

## rmiregistry

## schemagen

## serialver

返回serialverUID。语法：serialver [show] 命令选项show是用来显示一个简单的界面，输入完整的类名按Enter键或"显示"按钮，可显示serialverUID。

## servertool

## tnameserv

## unpack200

## wsgen

## wsimport

## xjc