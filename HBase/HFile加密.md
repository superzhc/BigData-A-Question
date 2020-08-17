为了避免第三方非法访问 HBase 的数据，可以给 HBase 配置加密算法，目前 **HBase 只支持使用 AES 加密算法，用于保护静态的 HFile 和 WAL 文件数据**。

HBase 配置的 AES 加密是一种端到端的加密模式，其中的加/解密过程对于客户端来说是完全透明的。数据在客户端读操作的时候被解密，当数据被客户端写的时候被加密。这个功能作用就是保证处于加密空间内的数据不被非法查询，只有经过认证的客户端才能查看解密内容。

## 配置

> 在配置文件 `hbase-site.xml` 添加相关配置

1、使用 keytool 程序为 AES 加密创建适当长度的密钥

```sh
cd $HBASE_HOME/conf
# 生成 hbase.jks
keytool -keystore hbase.jks -storetype jceke -storepass admin123 -genseckey -keyalg AES -keysize 128 -alias hbase
# hbase.jks：表示生成的jks文件存储路径
# admin123：表示存储的密码
# AES：表示加密的类型，目前仅支持 AES
# 128：表示密钥的长度，AES支持128位长度
# hbase：为密钥文件的别名
```

在密钥文件上设置适当的权限，并将其分发给所有的 HBase 服务器。

2、配置HBase Daemons

在集群的 `hbase-site.xml` 中设置以下属性，配置 HBase 守护程序以使用由 KeyStore 文件支持的密钥提供程序或检索集群主密钥。

```xml
<property>
  <name>hbase.crypto.keyprovider</name>
  <value>org.apache.hadoop.hbase.io.crypto.KeyStoreKeyProvider</value>
</property>
<property>
  <name>hbase.crypto.keyprovider.parameters</name>
  <!--密码是keytool中使用的storepass-->
  <value>jceks:///${HBASE_HOME}/conf/hbase.jks?password=admin123</value>
</property>
```

默认情况下，HBase 服务帐户名称将用于解析群集主密钥。但是，可以使用任意别名（在keytool命令中）存储它。在这种情况下，请将以下属性设置为用户使用的别名。 

```xml
<property>
  <name>hbase.crypto.master.key.name</name>
  <value>hbase</value>
</property>
```

还需要确保用户的 HFile 使用 **HFile v3**，以便使用透明加密。这是 HBase 1.0 以后的默认配置。对于以前的版本，请在 `hbase-site.xml` 文件中设置以下属性。

```xml
<property>
  <name>hfile.format.version</name>
  <value>3</value>
</property>
```

3、配置 WAL 加密

通过设置以下属性，在每个 RegionServer 的 `hbase-site.xml` 中配置 WAL 加密。也可以将这些包含在 HMaster 的 `hbase-site.xml` 中，但是 HMaster 没有 WAL 并且不会使用它们。 

```xml
<property>
  <name>hbase.regionserver.hlog.reader.impl</name>
  <value>org.apache.hadoop.hbase.regionserver.wal.SecureProtobufLogReader</value>
</property>
<property>
  <name>hbase.regionserver.hlog.writer.impl</name>
  <value>org.apache.hadoop.hbase.regionserver.wal.SecureProtobufLogWriter</value>
</property>
<property>
  <name>hbase.regionserver.wal.encryption</name>
  <value>true</value>
</property>
```

4、重启 HBase 服务

5、创建加密类型为 AES 的表

```sh
create 'superz',{NAME=>'cf1',ENCRYPTION=>'AES'},{NAME=>'cf2'}
```



## 加密读写性能对比

利用YCSB的读写基准测试，来进行明文和密文的读写性能对比。HBase单表单列族，写入和读取5000w条数据。

![img](images/2018090409541442)

![img](images/20180904095734292)