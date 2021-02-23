<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-02-18 17:15:54
 * @LastEditTime : 2021-02-18 17:19:27
 * @Copyright 2021 SUPERZHC
-->
# Git 报文件名过长的错误

## 方法一：设置全局或系统

```bash
git config --global core.longpaths true

# 或者

git config --system core.longpaths true
```

## 方法二：只设置当前工程

```bash
git config core.longpaths true
```