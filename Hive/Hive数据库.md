Hive中数据库的概念本质上仅仅**是表的一个目录或者命名空间**。

> 如果用户没有显式指定数据库，那么将会使用默认的数据库**default**

Hive会为每个数据库创建一个目录。数据库中的表将会以这个数据库目录的子目录形式存储。有一个例外就是default数据库中的表，因为这个数据库本身没有自己的目录。

数据库所在的目录位于属性`hive.metastore.warehouse.dir`所指定的顶层目录之后，那么当用户创建一个test数据库时，在Hive的存储目录下对应的创建一个目录`${hive.metastore.warehouse.dir}/test.db`。注意，**数据库的文件目录名是以`.db`结尾的**。

用户可以通过如下的命令来修改这个默认的位置：

```sh
create database test location '/myname/hive/test'
```

USE命令用于将某个数据库设置为用户当前的工作数据库，和在文件系统中切换工作目录是一个概念：

```sh
USE test;
```

> 在Hive中可以重复使用`USE ...`命令，这是因为在Hive中并没有嵌套数据库的概念