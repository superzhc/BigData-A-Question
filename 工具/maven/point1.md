---
title: Dependencies 与 DependencyManagement
date: 2017-11-21
tags: maven
---
#### dependencyManagement

在项目的 parent 层，可以通过 dependencyManagement 元素来管理 jar 包的版本，让子项目中引用一个依赖而不用显示的列出版本号。

这样做的好处：统一管理项目的版本号，确保应用的各个项目的依赖和版本一致，才能保证测试的和发布的是相同的成果，因此，在顶层 pom 中定义共同的依赖关系。同时可以避免在每个使用的子项目中都声明一个版本号，这样想升级或者切换到另一个版本时，只需要在父类容器里更新，不需要任何一个子项目的修改；如果某个子项目需要另外一个版本号时，只需要在 dependencies 中声明一个版本号即可。子类就会使用子类声明的版本号，不继承于父类版本号。

#### dependencies

相对于 dependencyManagement，所有声明在父项目中 dependencies 里的依赖都会被子项目自动引入，并默认被所有的子项目继承。

#### 区别

- dependencies 即使在子项目中不写该依赖项，那么子项目仍然会从父项目中继承该依赖项（全部继承）
- dependencyManagement 里只是声明依赖，并不实现引入，因此子项目需要显示的声明需要用的依赖。如果不在子项目中声明依赖，是不会从父项目中继承下来的；只有在子项目中写了该依赖项，并且没有指定具体版本，才会从父项目中继承该项，并且 version 和 scope 都读取自父 pom; 另外如果子项目中指定了版本号，那么会使用子项目中指定的jar版本。

#### 消除多模块插件配置重复

与 dependencyManagement 类似的，也可以使用 pluginManagement 元素管理插件。