<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-02-19 17:51:21
 * @LastEditTime : 2021-02-19 17:56:02
 * @Copyright 2021 SUPERZHC
-->
# 大文件处理

## 已经 `git commit` 的文件，当 `git push` 报错

去掉大文件的上传，使用如下的命令：

```bash
git filter-branch -f --index-filter 'git rm --cached --ignore-unmatch  大文件所在文件夹/大文件的名称.zip'
```