<font color='red'>官网安装文档</font>：https://cwiki.apache.org/confluence/display/Hive/AdminManual+Installation

#### 0、环境要求

JDK版本：HIVE 1.1要求安装的JDK版本大于1.7或者更高，HIVE版本从0.14-1.1可以运行在JDK1.6上。HIVE官方文档强烈建议JDK使用1.8的版本

Hadoop版本:建议使用Hadoop2.x。在安装Hive的机器上，必须要安装了Hadoop。很容易理解，因为查询会转换成MapReduce Job执行。因为需要转换，所以必须要有Hadoop

#### 1、下载

```sh
su hadoop
http://archive.cloudera.com/cdh5/cdh/5/hive-1.1.0-cdh5.4.7.tar.gz
```

#### 2、解压安装

```sh
tar -xvf hive-1.1.0-cdh5.4.7.tar.gz -C /usr/local
mv /usr/local/hive-1.1.0-cdh5.4.7 /usr/local/hive
```

#### 3、设置环境变量 HIVE_HOME(`/etc/profile`)

```sh
export HIVE_HOME=/usr/local/hive
export PATH=$HIVE_HOME/bin:$PATH
source /etc/profile
```

注：非 root 用户，配置自己的 `~/.bash_profile` 即可

#### 4、创建 Hive 运行时目录并赋予权限

```sh
$HADOOP_HOME/bin/hadoop fs -mkdir       /tmp
$HADOOP_HOME/bin/hadoop fs -mkdir  -p   /user/hive/warehouse
$HADOOP_HOME/bin/hadoop fs -chmod g+w   /tmp
$HADOOP_HOME/bin/hadoop fs -chmod g+w   /user/hive/warehouse
```

#### 5、修改配置文件

```sh
cd $HIVE_HOME/conf
cp hive-default.xml.template hive-default.xml
touch hive-site.xml
cp hive-exec-log4j.properties.template hive-exec-log4j.properties
cp hive-log4j.properties.template hive-log4j.properties
cp beeline-log4j.properties.template beeline-log4j.properties
```

注：**hive- default.xml.template这个要复制二份，一个是hive-default.xml，另一个是hive-site.xml，其中 hive-site.xml为用户自定义配置，hive-default.xml为全局配置，**hive启动时，-site.xml自定义配置会覆盖 -default.xml全局配置的相同配置项。

`hive-site.xml` 中配置保存元数据为mysql数据库

```xml
<configuration>
    <property>
        <name>javax.jdo.option.ConnectionURL</name>
        <value>jdbc:mysql://localhost:3306/hive_metastore</value>
    </property>

    <property>
        <name>javax.jdo.option.ConnectionDriverName</name>
        <value>com.mysql.jdbc.Driver</value>
    </property>

    <property>
        <name>javax.jdo.option.ConnectionUserName</name>
        <value>hive</value>
    </property>

    <property>
        <name>javax.jdo.option.ConnectionPassword</name>
        <value>hive</value>
    </property>
</configuration>
```

#### 6、验证安装是否成功

在命令行中输入"hive"，出现类似以下界面表示安装成功！

```sh
[hadoop@iZ28csbxcf3Z conf]$ hive
Logging initialized using configuration in file:/usr/local/hive/conf/hive-log4j.properties
WARNING: Hive CLI is deprecated and migration to Beeline is recommended.
hive>
```

