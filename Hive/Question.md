### 简述Hive桶表是什么，什么作用，举例

### Hive分区是什么，什么作用，该怎么分区

### Hive动态分区和静态分区

### Hive分区重命名

### 说出Hive导入数据的过程（表有多个分区、桶）

### Hive排序

### 什么是Hive join

### 说说你所理解的Hive视图操作

### Hive序列函数

### 简述Hive自定义函数

### Hive UDF是什么，什么作用，为什么要用

### 简述Hive优缺点

### 说说Hive内部表和外部表分别是什么？为什么要建外部表

内部表：建表时会在HDFS创建一个表的存储目录，增加分区的时候，会将数据复制到此location下，删除数据的时候，将表的数据和元数据一起删除

外部表：一般会建立分区，增加分区的时候不会将数据移到此表的location下，删除数据的时候，只删除了表的元数据的信息，表的数据不会删除。

### Hive内部表和外部表的区别

### 什么是数据倾斜

### Hive表类型有哪些

### MapReduce和Hive的区别和联系

### Hive如何调优，提升效率

### 说出Hive数据清洗的过程

### Hive与RDBMS关系型数据库的区别

### Hive分析窗口函数

### Hive数据倾斜类

### Hive取前10条数据

### Hive取最小成绩记录和最大的记录

### Hive四种排序

### Hive时间函数

### Mysql和Hive的区别

### Hive的SQL语句和Mysql的SQL语句有什么不同

### Hive存储格式

### Hive工作原理

[Hive工作原理](./Hive原理.md)