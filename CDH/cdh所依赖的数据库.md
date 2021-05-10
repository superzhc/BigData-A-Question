官方文档中也提供了安装cdh所需依赖的数据库信息
其中数据库<database>和<user>相关信息如下表格：

| Service                            | Database  | User   |
| ---------------------------------- | --------- | ------ |
| Cloudera Manager Server            | scm       | scm    |
| Activity Monitor                   | amon      | amon   |
| Reports Manager                    | rman      | rman   |
| Hue                                | hue       | hue    |
| Hive Metastore Server              | metastore | hive   |
| Sentry Server                      | sentry    | sentry |
| Cloudera Navigator Audit Server    | nav       | nav    |
| Cloudera Navigator Metadata Server | navms     | navms  |
| Oozie                              | oozie     | oozie  |

- 连接数据库
```shell
mysql -u root -p
```
- 创建数据库
```sql
# scm
CREATE DATABASE scm DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL ON scm.* TO 'scm'@'%' IDENTIFIED BY 'scm';

# amon
CREATE DATABASE amon DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL ON amon.* TO 'amon'@'%' IDENTIFIED BY 'amon';

# rman
CREATE DATABASE rman DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL ON rman.* TO 'rman'@'%' IDENTIFIED BY 'rman';

# hue
CREATE DATABASE hue DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci; 
GRANT ALL ON hue.* TO 'hue'@'%' IDENTIFIED BY 'hue';

# hive
CREATE DATABASE metastore DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL ON metastore.* TO 'hive'@'%' IDENTIFIED BY 'hive';

# sentry
CREATE DATABASE sentry DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;   
GRANT ALL ON sentry.* TO 'sentry'@'%' IDENTIFIED BY 'sentry';

# nav
CREATE DATABASE nav DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;      
GRANT ALL ON nav.* TO 'nav'@'%' IDENTIFIED BY 'nav';

# navms
CREATE DATABASE navms DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL ON navms.* TO 'navms'@'%' IDENTIFIED BY 'navms';

# oozie
CREATE DATABASE oozie DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL ON oozie.* TO 'oozie'@'%' IDENTIFIED BY 'oozie';

# flush
FLUSH PRIVILEGES;
```