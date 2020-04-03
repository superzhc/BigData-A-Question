跟其他程序设计语言一样，Bash 中的条件语句让用户可以决定一个操作是否被执行。结果取决于一个包在 `[[ ]]` 里的表达式。

由 `[[ ]]`(sh 中是 `[ ]`) 包起来的表达式被称作 **检测命令** 或 **基元**，这些表达式检测一个条件的结果。

### if

if 语句语法格式：

```sh
if condition
then
    command1 
    command2
    ...
    commandN 
fi
```

写成一行（适用于终端命令提示符）：

```sh
if [ $(ps -ef | grep -c "ssh") -gt 1 ]; then echo "true"; fi
```

末尾的fi就是if倒过来拼写，后面还会遇到类似的。

### if else

if else 语法格式：

```sh
if condition
then
    command1 
    command2
    ...
    commandN
else
    command
fi
```

### if else-if else

if else-if else 语法格式：

```sh
if condition1
then
    command1
elif condition2 
then 
    command2
else
    commandN
fi
```

以下实例判断两个变量是否相等：

```sh
a=10
b=20
if [ $a == $b ]
then
   echo "a 等于 b"
elif [ $a -gt $b ]
then
   echo "a 大于 b"
elif [ $a -lt $b ]
then
   echo "a 小于 b"
else
   echo "没有符合的条件"
fi
```

输出结果：

```sh
a 小于 b
```

if else语句经常与test命令结合使用，如下所示：

```sh
num1=$[2*3]
num2=$[1+5]
if test $[num1] -eq $[num2]
then
    echo '两个数字相等!'
else
    echo '两个数字不相等!'
fi
```

输出结果：

```sh
两个数字相等!
```