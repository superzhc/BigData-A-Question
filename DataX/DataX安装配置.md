### 0、环境要求

- 系统环境 windows、Linux 均可
- JDK (1.8 及以上)
- Python（推荐 2.6.X,一定要为 python2，因为在使用 DataX 的 py 文件是使用 python2 编写的，python3 不兼容 python2，存在语法的差异，造成使用 python3 运行 python2 会出现运行不成功的问题 ）
- Apache Maven 3.X（Compile DataX）

### 1、下载安装包

下载地址：<https://github.com/alibaba/DataX>

### 2、安装

将下载的安装包，解压到本地某个目录，即可运行样例同步作业

示例：使用 Linux 系统来安装 DataX

```sh
$ tar zxvf datax.tar.gz
$ sudo chmod -R 755 {YOUR_DATAX_HOME}
$ cd  {YOUR_DATAX_HOME}/bin
$ python datax.py ../job/job.json
```

### 3、测试

命令使用参考：[DataX使用](./DataX使用.md)

#### ①创建作业的配置文件

获取作业的配置文件模板

#### ②根据配置文件模板填写相关选项

根据上述作业的配置文件模板，填写相关选项

#### ③启动 DataX

如：`python datax.py D:\Software\install\Environment\DataX\datax\job\mysql2mysql.json`