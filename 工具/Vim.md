---
title: vim使用
date: 2017-11-10
tags: [vim]
---
# Vim

## vim快捷键

##### vim跳到指定行

在编辑模式下输入：ngg 或者 nG【n为指定的行数(如25)】  
在命令模式下输入行号n  
如果想打开文件即跳转: vim +n FileName

##### vim查找

vim下要查找字符串的时候， 都是输入 / 或者 ？  加需要查找的字符串来进行搜索  

用/和？的区别：  
/后跟查找的字符串。vim会显示文本中第一个出现的字符串。  
?后跟查找的字符串。vim会显示文本中最后一个出现的字符串。  

不管用/还是？查找到第一个字符串后，按回车，vim会高亮所有的匹配文

##### 保存文件

命令模式下，wq