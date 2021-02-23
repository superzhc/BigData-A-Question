<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-02-19 14:26:16
 * @LastEditTime : 2021-02-19 16:10:29
 * @Copyright 2021 SUPERZHC
-->
# `.gitignore` 忽略文件使用

## 忽略文件的原则

- 忽略操作系统自动生成的文件，比如缩略图等；
- 忽略编译生成的中间文件、可执行文件等，也就是如果一个文件是通过另一个文件自动生成的，那自动生成的文件就没必要放进版本库，比如Java编译产生的.class文件；
- 忽略你自己的带有敏感信息的配置文件，比如存放口令的配置文件。

## 使用方法

1. 首先，在你的工作区新建一个名称为 `.gitignore` 的文件。
2, 然后，把要忽略的文件名填进去，Git 就会自动忽略这些文件。

不需要从头写 `.gitignore` 文件，GitHub 已经为我们准备了各种配置文件，只需要组合一下就可以使用了。所有配置文件可以直接在线浏览：<https://github.com/github/gitignore>

## 查看规则

如果你发现 `.gitignore` 写得有问题，需要找出来到底哪个规则写错了，可以用 `git check-ignore` 命令检查：

```bash
$ git check-ignore -v HelloWorld.class
.gitignore:1:*.class    HelloWorld.class
```

可以看到 `HelloWorld.class` 匹配到了第一条 `*.class` 的忽略规则所以文件被忽略了。

## 忽略规则文件语法

**忽略指定文件/目录**

```bash
# 忽略指定文件
HelloWrold.class

# 忽略指定文件夹
bin/
bin/gen/
```

**通配符忽略规则**

```bash
# 忽略.class的所有文件
*.class

# 忽略名称中末尾为ignore的文件夹
*ignore/

# 忽略名称中间包含ignore的文件夹
*ignore*/
```