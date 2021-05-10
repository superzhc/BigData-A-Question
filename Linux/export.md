# export 命令

**export命令**用于将shell变量输出为环境变量，或者将shell函数输出为环境变量。

一个变量创建时，它不会自动地为在它之后创建的shell进程所知。而命令export可以向后面的shell传递变量的值。当一个shell脚本调用并执行时，它不会自动得到原为脚本（调用者）里定义的变量的访问权，除非这些变量已经被显式地设置为可用。export命令可以用于传递一个或多个变量的值到任何后继脚本。

**语法**

```sh
export (选项) (参数)
```

**选项**

```sh
-f：代表[变量名称]中为函数名称；
-n：删除指定的变量。变量实际上并未删除，只是不会输出到后续指令的执行环境中；
-p：列出所有的shell赋予程序的环境变量。
```

**参数**

变量：指定要输出或者删除的环境变量。

**实例**

一般来说，配置交叉编译工具链的时候需要指定编译工具的路径，此时就需要设置环境变量。查看已经存在的环境变量：

```sh
[root@localhost ~]# export
declare -x HOME="/root"
declare -x PATH="/usr/kerberos/sbin:/usr/kerberos/bin:/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:/root/bin"
declare -x pwd="/root"
declare -x USER="root"
...
```