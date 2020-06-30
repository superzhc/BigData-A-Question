Hive的存储格式主要有：

- TextFile
- SequenceFile
- RCFile
- ORC
- 自定义格式

### TextFile

Hive的默认存储格式

存储方式：行存储

磁盘开销大，数据解析开销大

压缩的text文件Hive无法进行合并和拆分

### SequenceFile

二进制文件以<key,value>的形式序列化到文件中

存储方式：行存储

可分割压缩

一般选择block压缩

优势是文件和Hadoop api中的mapfile是相互兼容的

### RCFile

存储方式：数据按行分块每块按照列存储

压缩快，快速列存取

读记录尽量涉及到的block最少

读取需要的列只需要读取每个row group的头部定义

读取全量数据的操作性能可能比SequenceFile没有明显的优势

### ORC

存储方式：数据按行分块每块按照列存储

压缩快、快速列存取

效率比RCFile高，是RCFile的改良版本

### 自定义格式

用户可以通过实现inputformat和outputformat来自定义输入输出格式