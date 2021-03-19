# CentOS7 下配置静态路由

## route 命令

> 注：若 route 命令不存在，如下安装：
> 
> ```bash
> yum install net-tools
> ```

**参数**：

```
add     增加路由
del     删除路由
-net    设置到某个网段的路由
-host   设置到某台主机的路由
gw      出口网关 IP地址
dev     出口网关 物理设备名
```

**示例**：

```bash
# 查看路由信息
ip route show
ip route show | cloumn -t #格式化后的路由

# 加入到主机的路由
route add -host 192.168.1.123 dev eth0
route add -host 192.168.1.123 gw 192.168.1.1

# 加入到网络的路由
route add -net 192.168.1.0 netmask 255.255.255.0 eth0
route add -net 192.168.1.0 netmask 255.255.255.0 gw 192.168.1.1
route add -net 192.168.1.0 netmask 255.255.255.0 gw 192.168.1.1 eth1

# 加入默认网关
route add default gw 192.168.1.1

# 删除路由
route del -host 192.168.1.11 dev eth0
route del -net 192.168.1.0 netmask 255.255.255.0
```

## 显示路由表

```bash
[root@linux-node1 ~]# ip route show 
default via 192.168.56.2 dev eth0 
169.254.0.0/16 dev eth0  scope link  metric 1002 
192.168.56.0/24 dev eth0  proto kernel  scope link  src 192.168.56.11

[root@linux-node1 ~]# ip route show | column -t       # 格式化一下
default          via  192.168.56.2  dev    eth0
169.254.0.0/16   dev  eth0          scope  link    metric  1002
192.168.56.0/24  dev  eth0          proto  kernel  scope   link  src  192.168.56.1
```

## 新增路由

**示例**

> 需求：将访问 `10.2.80.50` 的数据包通过 eth2 发送到 `192.168.127.1` 实现内网访问 `10.2.80.50`

命令如下：

```bash
route add –host 10.2.80.50 gw 192.168.127.1 dev eth2
```

**注**：

```bash
# 示例1：-net 只能指定网段
route add -net 10.2.80.50 netmask 255.255.255.0 gw 192.168.127.1 dev eth2
## 执行报错：route: netmask doesn't match route address
## 正确用法如下：
route add -net 10.2.80.0 netmask 255.255.255.0 gw 192.168.127.1 dev eth2

# 示例2：-host 不用指定掩码
route add -host 10.2.80.50 netmask 255.255.255.0 gw 192.168.127.1 dev eth2
## 执行报错：route: netmask 000000ff doesn't make sense with host route
## 正确用法如下：
route add -host 10.2.80.50 gw 192.168.127.1 dev eth2
```

## 测试 IP 的路由情况

```bash
ip route get 10.2.80.50
```

## 添加永久路由

```bash
vi /etc/sysconfig/network-scripts/route-[interface] # 注意这个文件本身不存在，需要创建。interface是硬件文件

192.168.150.0/24 via 192.168.1.253 dev enp0s3
```

之后重启