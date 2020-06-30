## F&Q

### Scala+maven项目，导入的项目后项目并不是Scala项目

#### 问题现象

![img](../images/20160809182441946.png)

#### 解决办法

![img](../images/20160809182510164.png)

通过上述操作将maven项目转换成 scala-maven

### 项目存在多个 `scala-library` 库的构建路径

#### 问题现象

![img](../images/20160809182649119.png)

或者

![img](../images/20160809182719729.png)

#### 解决办法

方法1：`Scala Library container`->`Build Path`->`Remove from Build Path`

方法2：移除 maven 的 `pom.xml` 对 scala 的引用