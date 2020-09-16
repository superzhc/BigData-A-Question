HBase Shell 是 HBase 集群的命令行接口
用户可以通过Shell访问本地或者是远程服务器并与其进行交互,Shell同时提供了客户端和管理功能的操作.
Hbase Shell 是基于Jruby的,其是基于Ruby实现的java虚拟机.
它使用的是交互式的Ruby Shell,输入命令并迅速得到响应

## 常规

### 进入与退出 HBase shell 命令窗口

```sh
# 进入命令
hbase shell

# 退出命令
exit
```

### help

**（1）查看 hbase 中有哪些命令**

![1560821399331](https://raw.githubusercontent.com/superzhc/GraphBed/master/publish/1560821399331.png)

**（2）查看某一组命令下的所有命令的简介和简单示例**

```sh
help 'ddl'
```

**（3）查看某个命令的详细使用**

```sh
help 'create'
```

### 查询服务器状态

```sh
hbase(main):0il:0>status
1 active master,0 backup masters, 1 servers,0 dead,4.0000 average load
```

### 查询版本号

```sh
hbase(main):012:0>version
1.2.1,r8d8a7107dc4ccbf36a92f64675dc60392f85c015,Wed Mar 30 11:19:21 CDT 2016
```

## 表的管理

### 列出所有的表

```sh
hbase(main):014:0>list
TABLE
stu
table
test
3 row(s) in 0.0570 seconds
```

### 获取表的描述

```sh
hbase(main):015:0> describe 'table'
Table table is ENABLED
table
COLUMN FAMILIES DESCRIPTION
{NAME =>'coluran_famaly',DATA_BLOCK_ENCODING =>'NONE',BLOOMFILTER =>'ROW',REPLICATION_SCOPE=>'0',VERSIONS=>'1',COMPRESSION=>'NONE', MIN_VERSIONS =>'0',TTL=>'FOREVER',KEEP_DELETED_CELLS=>'FALSE',BLOCKSIZE=>'65536',IN_MEMORY
=>'false',BLOCKCACHE=>'true}
......
3 row(s) in 0.0430 seconds

# 其中的属性的意义：
NAME：列族名
VERSIONS：最大版本号
MIN_VERSIONS：最小版本号
TTL（Time To Live）：存活时间
IN_MEMORY：是否开启缓存，默认false，应该开启，否则与BLOCKCACHE冲突
BLOCKCACHE：读缓存是否开启，默认开启，64M
```

### 创建一个表

```sh
hbase(main):013:0> create'table','column_famaly','column_famaly1','column_famaly2'
0 row(s) in 94.9160 seconds

# 等价于
create 'table',{NAME=>'column_family1'},{NAME=>'column_family2'}

# 建表时可以指定表属性信息
hbase(main):005:0> create 'user_info',{NAME=>'base_info',VERSIONS=>3 },{NAME=>'extra_info',IN_MEMORY=>'true'} 
```

### 修改表结构

修改一个表结构，如果属性`hbase.online.schema.update.enable=false`，则表**必须先要 disabled，才能开始进行修改**。

```sh
--(1) 增加列族
alter 'table_name', 'add_family'
# 或者
alter 'table_name', {NAME => 'add_family'}
# 当然，新增加的列可以设置属性，比如
alter 'table_name', {NAME => 'add_family', VERSIONS => 3}

--(2) 删除列族
alter 'table_name', {NAME => 'delete_family', METHOD => 'delete'}
或者
alter 'table_name', 'delete' => 'delete_family'

--(3) 添加列族f1同时删除列族f2
alter 'user', {NAME => 'f1'}, {NAME => 'f2', METHOD => 'delete'}

--(4) 修改列族
# 将user表的f1列族版本号改为5
alter 'user', NAME => 'f1', VERSIONS => 5
```

### 删除一个表

1. 首先把表设置为 disable

```sh
hbase(main):020:0>disable 'stu'
0 row(s) in 2.3150 seconds
```

2. 然后删除一个表

```sh
hbase(main):021:0>drop 'stu'
0 row(s) in 1.2820 seconds
```

### 查询表是否存在

```sh
hbase(main):024:0>exists 'table'
Table table does exist
0 row(s) in 0.0280 seconds
```

### 查看表是否可用

```sh
hbase(main):025:0>is_enabled 'table'
true
0 row(s) in 0.0150 seconds
```

## 权限管理

### 分配权限

```sh
# 语法：grant <user> <permissions> <table> <column family> <column qualifier> 参数后面用逗号分割
# 权限用五个字母表示：“RWXCA”
# READ('R'), WRITE('W'), EXEC('X'), CREATE('C'), ADMIN('A')
# 例如，给用户'luanpeng'分配对表t1有读写的权限，
hbase(main)> grant 'luanpeng','RW','t1'
```

### 查看权限

```sh
# 语法：user_permission <table>
# 例如，查看表 t1 的权限列表
hbase(main)> user_permission 't1'
```

### 收回权限

```sh
# 与分配权限类似，语法：revoke <user> <table> <column family> <column qualifier>
# 例如，收回 luanpeng 用户在表 t1 上的权限
hbase(main)> revoke 'luanpeng','t1'
```

## 表数据的增删改查

### 插入/更新数据

```sh
# 插入/更新的语法，完全一致
# 语法：put <table>,<rowkey>,<family:column>,<value>,<timestamp>
hbase(main):031:0>put 'emp','rw1','col_f1:name','tanggao'
0 row(s) in 0.0460 seconds

# (1) put '表名','rowkey','列族名:列名','值'
put 'person','0001','name:firstname', 'Jed'
# (2) 可以指定时间戳，否则默认为系统当前时间
put 'person','0002','info:age',20,1482077777778
```

### 查询数据

```sh
--(1) 查询某行记录
get 'person', '0001'
--(2) 查询某行，指定列名
get 'person', '0001', 'name:firstname'
--(3) 查询某行，添加其他限制条件
# 查询person表中，rowkey为'0001'的这一行，只显示name:firstname这一列，并且只显示最新的3个版本
get 'person', '0001', {COLUMNS => 'name:firstname', VERSIONS => 3}
# 查看指定列的内容，并限定显示最新的3个版本和时间范围 
get 'person', '0001', {COLUMN => 'name:first', VERSIONS => 3, TIMERANGE => [1392368783980, 1392380169184]}
# 查询person表中,rowkey为'rk0001',且某列的内容为'中国'的记录【调用不通过】
scan'person', 'rk0001', {FILTER => "ValueFilter(=, 'binary:中国')"}
```

#### 通过时间戳获取两个版本的数据

```sh
hbase(main):039:0>get 'emp','rw1',{COLUMN=>'col_f1:age',TIMESTAMP=>1463055735107}
COLUMN    CELL
col_f1:age timestamp=1463055735107,value=20
1 row(s) in 0.0340 seconds

hbase(main):040:0>get 'emp','rw1',{COLUMN=>'col_f1:age',TIMESTAMP=>1463055893492}
COLUMN    CELL   
col_f1:age timestamp=1463055893492,value=22
1 row(s) in 0.0140 seconds
```

### 全表扫描

```sh
--(1) 扫描全表
scan 'person'
--(2) 扫描时指定列族
scan 'person', {COLUMNS => 'name'}
--(3) 扫描时指定列族，并限定显示最新的5个版本的内容
scan 'person', {COLUMNS => 'name', VERSIONS => 5}
--(4) 设置开启Raw模式，开启Raw模式会把那些已添加删除标记但是未实际删除的数据也显示出来
scan 'person', {COLUMNS => 'name', RAW => true}
--(5) 列的过滤
# 查询user表中列族为info和data的信息
scan 'user', {COLUMNS => ['info', 'data']}
# 查询user表中列族为info，列名为name、列族为data，列名为pic的信息
scan 'user', {COLUMNS => ['info:name', 'data:pic']}
# 查询user表中列族为info，列名为name的信息，并且版本最新的5个
scan 'user', {COLUMNS => 'info:name', VERSIONS => 5}
# 查询user表中列族为info和data且列名含有a字符的信息
scan 'user', {COLUMNS => ['info', 'data'], FILTER => "(QualifierFilter(=,'substring:a'))"}
# 查询user表中列族为info，rk范围是[rk0001, rk0003)的数据
scan 'people', {COLUMNS => 'info', STARTROW => 'rk0001', ENDROW => 'rk0003'}
# 查询user表中row key以rk字符开头的
scan 'user',{FILTER=>"PrefixFilter('rk')"}
# 查询user表中指定时间范围的数据
scan 'user', {TIMERANGE => [1392368783980, 1392380169184]}
# 查找条数，相当于分页
scan 'user',LIMIT=>n
# 根据时间范围，查找数据
scan 'user',TIMERANGE=>{minStamp,maxStamp}
```

### 统计表中的行数

```sh
hbase(main):045:0>count 'emp'
0 row(s) in 0.0450 seconds
```

### 删除数据

#### 删除行中的某个列数据

```sh
# 语法：delete <table>, <rowkey>,  <family:column> , <timestamp>,必须指定列名
hbase(main):042:0>delete 'emp','rw1','col_f1:age'
0 row(s) in 0.0200 seconds
```

#### 删除行

```sh
# 语法：deleteall <table>, <rowkey>,  <family:column> , <timestamp>，可以不指定列名，删除整行数据
hbase(main) :044:0>deleteall 'emp','rw1'
0 row(s) in 0.0550 seconds
```

#### 删除表中的所有数据

```sh
# 语法： truncate <table>
# 其具体过程是：disable table -> drop table -> create table
hbase(main):007:0>truncate 'emp'
Truncating 'emp' table(it may take a while);
-Disabling table...
-Truncating table...
0 row(s) in 4.1510 seconds
```

## 过滤器

HBase 的过滤器可以让用户**在查询中添加更多的限制条件来减少查询得到的数据量**，从而帮助用户**提高处理表数据的效率**。

所有的**过滤器都在服务端生效**，使被过滤掉的数据不会被传送到客户端。

**比较器**：

1. 二进制比较器：如`binary:abc`，按字段排序跟 `abc` 进行比较 
2. 二进制前缀比较器：如 `binaryprefix:abc`，按字典顺序只跟 `abc` 比较前 3 个字符
3. 正则表达式比较器：如 `regexstring:ab*yz`，按正则表达式匹配以 `ab` 开头，以 `yz` 结尾的值，这个比较器只能使用 `=`、`!=` 两个比较运算符
4. 字串比较器：如 `substring:abc123`，匹配以 `abc123` 开头的值。这个比较也只能使用 `=`、`!=`  两个比较运算c符

**比较运算符**：(`CompareFilter.CompareOp`)

1. EQUAL                                相等
2. GREATER                            大于 
3. GREATER_OR_EQUAL       大于等于
4. LESS                                   小于
5. LESS_OR_EQUAL              小于等于
6. NOT_EQUAL                      不等于

### RowKey 过滤

```sh
# 匹配出RowKey含111的数据
# 示例：查询rowkey包含111的数据
scan 'test',FILTER=>"RowFilter(=,'substring:111')"
# 等价于上面，【注：需要手动引入包】
import org.apache.hadoop.hbase.filter.CompareFilter  
import org.apache.hadoop.hbase.filter.SubstringComparator  
import org.apache.hadoop.hbase.filter.RowFilter  
scan 'test', {FILTER => RowFilter.new(CompareFilter::CompareOp.valueOf('EQUAL'), SubstringComparator.new('111'))}  

# 匹配出RowKey等于0111486816556
scan 'test',FILTER=>"RowFilter(=,'binary:0111486816556')"
# 匹配出RowKey小于等于0111486816556
scan 'test',FILTER=>"RowFilter(<=,'binary:0111486816556')"

# 通过前缀过滤
# 示例：rowkey 以 00000 开头的数据
scan 'test',FILTER=>"PrefixFilter('00000')"
# 等价与上面，【注：需要手动引入包】
import org.apache.hadoop.hbase.filter.RegexStringComparator  
import org.apache.hadoop.hbase.filter.CompareFilter  
import org.apache.hadoop.hbase.filter.SubstringComparator  
import org.apache.hadoop.hbase.filter.RowFilter  
scan 'test', {FILTER => RowFilter.new(CompareFilter::CompareOp.valueOf('EQUAL'),RegexStringComparator.new('^00000\d+\|ts\d+$'))} 
```

### 列过滤

```sh
#####################################列簇过滤#######################################
# 通过列簇过滤，匹配出列簇含 col 的数据
scan 'test',FILTER=>"FamilyFilter(=,'substring:col')"

#####################################列名过滤#######################################
# QualifierFilter：列名限定符过滤器
# 语法：scan <tableName>,{FILTER=>"QualifierFilter(<比较运算符>,'<比较器>')"}
scan 'FRONTHIS',FILTER=>"QualifierFilter(=,'binary:operation')"

# ColumnPrefixFilter：列名前缀过滤器
# 示例：列名以qual开头的列
scan 'test',FILTER=>"ColumnPrefixFilter('qual')"

# MultipleColumnPrefixFilter：多列名前缀过滤器
scan 'test',FILTER=>"MultipleColumnPrefixFilter('c1','c2')"

#####################################列值过滤#######################################
# 过滤列值value,binary表示列值二进制下的精确查询
# 示例：列值=col_qual_value
scan 'test',FILTER=>"ValueFilter(=,'binary:col_qual_value')"

# 过滤列值value,substring表示列值的截取串查询
# 示例：列值包含child_col_qual_value
scan 'test',FILTER=>"ValueFilter(=,'substring:child_col_qual_value')"

# SingleColumValueFilter：列值过滤器
# 需要导入类
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter
import org.apache.hadoop.hbase.filter.CompareFilter
import org.apache.hadoop.hbase.filter.SubstringComparator
scan 'table_name',{FILTER => SingleColumnValueFilter.new(
  Bytes.toBytes('info'),  # 列族
  Bytes.toBytes('column'),    # 字段
  CompareFilter::CompareOp.valueOf('EQUAL'), # 比较器
  Bytes.toBytes('my value')) # 值
}

# FirstKeyOnlyFilter: 一个rowkey可以有多个version,同一个rowkey的同一个column也会有多个的值, 只拿出key中的第一个column的第一个version

# KeyOnlyFilter: 只要key,不要value

######################################实践#########################################
# 以 operation 开头值，并且值等于 D
scan 'FRONTHIS',{FILTER=>"ColumnPrefixFilter('operation') AND ValueFilter(=,'binary:D')"}
# 等价于上面
import org.apache.hadoop.hbase.filter.CompareFilter  
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter  
import org.apache.hadoop.hbase.filter.SubstringComparator  
scan 'FRONTHIS',{FILTER=>SingleColumnValueFilter.new(Bytes.toBytes('DEFAULT'),Bytes.toBytes('operation'),CompareFilter::CompareOp.valueOf('EQUAL'),Bytes.toBytes('D'))}
```

### 起止行查询

```sh
# STARTROW表示从这一行开始，包含这一行
# STOPROW 表示到这行结束，不包含这一样
# 其中，startrow stoprow的数值不一定就是精确值，可以是rowkey里存在的某一个子串
scan 'test',{STARTROW=>'starttime',STOPROW=>'stoptime'}
```

### 组合过滤器

多个过滤器可以通过 `AND`或`OR`连接进行组合过滤

## Region 管理

### 移动 region

```sh
# 语法：move 'encodeRegionName','ServerName'
# encodeRegionName指的regioName后面的编码，ServerName指的是master-status的Region Servers列表
# 示例
hbase(main)>move '4343995a58be8e5bbc739af1e91cd72d', 'db-41.xxx.xxx.org,60020,1390274516739'
```

### 开启/关闭 region

```sh
# 语法：balance_switch true|false
hbase(main)> balance_switch
```

### 手动 split

```sh
# 语法：split 'regionName','splitKey'
```

### 手动触发 major compaction

```sh
#语法：
#Compact all regions in a table:
#hbase> major_compact 't1'
#Compact an entire region:
#hbase> major_compact 'r1'
#Compact a single column family within a region:
#hbase> major_compact 'r1', 'c1'
#Compact a single column family within a table:
#hbase> major_compact 't1', 'c1'
```

## Hbase ZkCli

```sh
# 进入zookeeper的客户端
hbase zkcli

# 列出根目录下的所有节点【注：对获取的信息做了精简，`....` 标识的都是做了精简数据的操作】
[zk: hd3:24002,hd2:24002,hd1:24002(CONNECTED) 0] ls /
# [hbase, hbaseindexer, stormroot, yarn-leader-election, zookeeper ....]
[zk: hd3:24002,hd2:24002,hd1:24002(CONNECTED) 1] ls /hbase
# [splitWAL, switch, table, table-lock, tokenauth .....]
[zk: hd3:24002,hd2:24002,hd1:24002(CONNECTED) 2] ls /hbase/table
# [SUPERZ_HBASEDEMO, SUPERZ_TEST_1, SYSTEM:CATALOG,  kylin_metadata, meta_gb, pentaho_mappings, superz_demo, superz_version, t1, t2, table1, table2, test ....]
[zk: hd3:24002,hd2:24002,hd1:24002(CONNECTED) 3] ls /hbase/table/superz_version
# []
[zk: hd3:24002,hd2:24002,hd1:24002(CONNECTED) 4] get /hbase/table/superz_version
# �master:21300z���O]�PBUF
```

