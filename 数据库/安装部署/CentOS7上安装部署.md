# CentOS7 上安装部署

## 1. 创建 mysql 镜像源

```bash
vim /etc/yum.repos.d/mysql-community.repo
```

内容如下：

```
[mysql57-community]
name=MySQL 5.7 Community Server
baseurl=https://mirrors.tuna.tsinghua.edu.cn/mysql/yum/mysql57-community-el7/
enabled=1
gpgcheck=0
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-mysql
```

## 2. 下载安装 MySQL5.7

```bash
yum -y install mysql-community-server
```

## 3. 启动 MySQL

```bash
systemctl start mysqld.service
# 设置开机自启动
systemctl enable mysqld.service
```

## 4. yum 安装的 Mysql 的 root 有默认密码，执行如下命令查看密码

```bash
grep 'temporary password' /var/log/mysqld.log
```

## 5. 修改用户密码

使用上面获取的默认密码进行登录 `mysql -u root -p`，进入 mysql 的 shell 进行如下的密码修改操作：

```bash
ALTER USER 'username'@'localhost' IDENTIFIED WITH mysql_native_password BY 'new_password';
```

注：如果密码修改报错，一般是密码策略的问题，可修改密码策略，执行如下语句：

```bash
set global validate_password_policy=0;
set global validate_password_mixed_case_count=0;
set global validate_password_number_count=3;
set global validate_password_special_char_count=0;
set global validate_password_length=3;
```

执行完上面的语句后，再重新执行修改用户密码。

**修改 root 用户相关信息**

```bash
# 修改host
update mysql.user set host = '%' where user = 'root';
# 授权
#GRANT ALL PRIVILEGES ON *.* TO 'username'@'host' IDENTIFIED BY 'password' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION;
# 进行了用户信息的操作，建议一定要刷新一下
flush privileges ;
```