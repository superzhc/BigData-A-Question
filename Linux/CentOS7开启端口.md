# CentOS7 开启端口

## 使用 firewall

```bash
# 开启端口
firewall-cmd --zone=public --add-port=8081/tcp --permanent

# 重启防火墙
firewall-cmd --reload
```