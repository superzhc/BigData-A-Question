# MySQL 连接器

在 `${PRESTO_HOME}/etc/catalog` 目录中创建一个文件 `mysql.properties`，文件内容如下：

```properties
connector.name = mysql
#连接器URL
connection-url = jdbc:mysql://localhost:3306 
#连接器用户名
connection-user = root
#连接器密码
connection-password = pwd 
```