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
```

## 4. yum 安装的 Mysql 的 root 有默认密码，执行如下命令查看密码

```bash
grep 'temporary password' /var/log/mysqld.log
```

## 5. 修改用户密码

```bash
ALTER USER 'username'@'localhost' IDENTIFIED WITH mysql_native_password BY 'new_password';
```

注：如果密码修改报错，一般是密码策略的问题，可修改密码策略，执行如下语句：

```bash
set global validate_password_policy=0;
```

执行完上面的语句后，再重新执行修改用户密码。
