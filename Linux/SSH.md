# SSH

> Secure Shell（缩写为SSH），由IETF的网络工作小组（Network Working Group）所制定；SSH为一项创建在应用层和传输层基础上的安全协议，为计算机上的Shell（壳层）提供安全的传输和使用环境。
> 
> -- 来自维基百科

## 使用

**语法**

```bash
ssh [-1246AaCfgKkMNnqsTtVvXxYy] [-b bind_address] [-c cipher_spec]
    [-D [bind_address:]port] [-e escape_char] [-F configfile]
    [-i identity_file] [-L [bind_address:]port:host:hostport]
    [-l login_name] [-m mac_spec] [-O ctl_cmd] [-o option] [-p port]
    [-R [bind_address:]port:host:hostport] [-S ctl_path]
    [-W host:port] [-w local_tun[:remote_tun]]
    [user@]hostname [command]
```

**示例**

```
1.登录                   
    ssh -p 22 omd@192.168.25.137               
2.直接执行命令  -->最好全路径                   
    ssh root@192.168.25.137 ls -ltr /backup/data                       
        ==>ssh root@192.168.25.137 /bin/ls -ltr /backup/data               
3.查看已知主机                    
    cat /root/.ssh/known_hosts
4.ssh远程执行sudo命令
    ssh -t omd@192.168.25.137 sudo rsync hosts /etc/

5.scp               
    1.功能   -->远程文件的安全(加密)拷贝                   
        scp -P22 -r -p /home/omd/h.txt omd@192.168.25.137:/home/omd/               
    2.scp知识小结                   
        scp是加密远程拷贝，cp为本地拷贝                   
        可以推送过去，也可以拉过来                   
        每次都是全量拷贝(效率不高，适合第一次)，增量拷贝用rsync

6.ssh自带的sftp功能               
    1.Window和Linux的传输工具                   
        wincp   filezip                   
        sftp  -->基于ssh的安全加密传输                   
        samba   
    2.sftp客户端连接                   
        sftp -oPort=22 root@192.168.25.137                   
        put /etc/hosts /tmp                   
        get /etc/hosts /home/omd   
    3.sftp小结：                   
        1.linux下使用命令： sftp -oPort=22 root@x.x.x.x                   
        2.put加客户端本地路径上传                  
        3.get下载服务器端内容到本地                   
        4.远程连接默认连接用户的家目录
```

**后台 ssh 服务相关**

```bash
# 查询openssl软件
    rpm -qa openssh openssl
# 查询sshd进程
    ps -ef | grep ssh
        --> /usr/sbin/sshd
# 查看ssh端口
    netstat -lntup | grep ssh  
    ss | grep ssh                (效果同上，同下，好用)
    netstat -a | grep ssh(记住这个)
    netstat -lnt | grep 22    ==>  查看22端口有没有开/ssh服务有没有开启
    技巧： netstat -lnt | grep ssh | wc -l -->只要大于2个就是ssh服务就是好的
# 查看ssh的秘钥目录
    ll /root/.ssh/known_hosts  # 当前用户家目录的.ssh目录下
# ssh的配置文件
    cat /etc/ssh/sshd_config   
# ssh服务的关闭
    service sshd stop
# ssh服务的开启：
    service sshd start
# ssh服务的重启
    service sshd reload    [停止进程后重启] ==> 推荐
    service sshd restart   [干掉进程后重启] ==> 不推荐
# ssh远程登录
    ssh 192.168.1.100      # 默认利用当前宿主用户的用户名登录
    ssh omd@192.168.1.100  # 利用远程机的用户登录
    ssh omd@192.168.1.100  -o stricthostkeychecking=no # 首次登陆免输yes登录
    ssh omd@192.168.1.100 "ls /home/omd"  # 当前服务器A远程登录服务器B后执行某个命令
    ssh omd@192.168.1.100 -t "sh /home/omd/ftl.sh"  # 当前服务器A远程登录服务器B后执行某个脚本
```

**ssh 免密设置**

```bash
# 1. 根据RSA算法生成私钥和公钥
ssh-keygen -t rsa     # 一路回车即可
#   id_dsa         -->私钥(钥匙) 
#   id_dsa.pub     -->公钥(锁)
# 2. 拷贝公钥给目标服务器
ssh-copy-id -i id_dsa.pub omd@192.168.25.110             #【使用ssh登录的默认端口22】
# ssh-copy-id -i id_dsa.pub –p 666 omd@192.168.25.120    #【使用ssh登录设置的端口666】
```