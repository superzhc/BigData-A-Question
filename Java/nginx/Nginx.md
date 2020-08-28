# Nginx

## 简介

## 安装和配置

Nginx的安装

```sh
# CentOS
yum install nginx;
# Ubuntu
sudo apt-get install nginx;
# Mac
brew install nginx;
```

nginx操作命令：

```sh
#-s,signal，意思就是向nginx 发送 start|reload|stop 命令

# 启动
nginx -s start;
# 重新启动，热启动，修改配置重启不影响线上
nginx -s reload;
# 关闭
nginx -s stop;
# 修改配置后，可以通过下面的命令测试是否有语法错误
nginx -t;

# 从容停止Nginx
kill -QUIT Nginx主进程号
# 快速停止Nginx
kill -TERM Nginx主进程号
kill -INT Nginx主进程号
# 强制停止所有Nginx进程
pkill -9 nginx

# Nginx的平滑重启
kill -HUP Nginx主进程号

# 为nginx指定一个配置文件，来代替缺省的
nginx -c </path/to/config>

# 不运行，而仅仅测试配置文件。nginx 将检查配置文件的语法的正确性，并尝试打开配置文件中所引用到的文件。
nginx -t

# 显示 nginx 的版本。
nginx -v

# 显示 nginx 的版本，编译器版本和配置参数。
nginx -V
```

### Nginx 的信号控制

信号  | 作用
:----:|:--
TERM  | 快速关闭
INT   | 快速关闭
QUIT  | 从容关闭
HUP   | 平滑重启，重新加载配置文件
USR1  | 重新打开日志文件，在切割日志时用途较大
USR2  | 平滑升级可执行程序
WINCH | 从容关闭工作进程

## 配置文件

nginx 的配置文件默认在nginx程序安装目录的conf 二级目录下，主配置文件为nginx.conf。

nginx.conf的配置文件结构主要由以下几部分构成：

```conf
...
events {
    ...
}

http {
    ...
    # 虚拟主机
    server {
        ...
    }
    # 第二个虚拟主机
    server {
        ...
    }
    ...
}
```

### 语法

nginx是模块化的系统，整个系统是分成一个个模块的。每个模块负责不同的功能。比如http_gzip_static_module就是负责压缩的，http_ssl_module就是负责加密的，如果不用某个模块的话，也可以去掉，可以让整个nginx变得小巧，更适合自己。

nginx模块的使用是通过配置文件来改变功能的。nginx的模块是通过一个叫 **指令(directive)** 的东西来用的。整个配置文件都是由指令来控制的。nginx也有自己内置的指令，比如events, http, server, 和 location等。

### 核心模块

#### 主模块指令

主模块是实现基本功能的指令集，一般写在配置文件的最上面。

##### error_log

语法：`error_log file [debug|info|notice|warn|error|crit]`

默认值：${prefix}/logs/error.log

- file:用来指定记录错误日志的文件路径。错误日志记录了服务器运行期间遇到的各种错误，以及一些普通的诊断信息。
- [debug|info|notice|warn|error|crit]：6个错误级别，设置日志文件记录信息级别的高低，控制日志文件记录信息的数量和类型，debug 的级别最低，记录范围最广，crit 的级别最高。

##### include

语法：include file | *

该指令用于包含配置文件，include 支持文件名匹配，如`include vhosts/*.conf;`

##### pid

语法：pid file

pid 文件内记录着当前 nginx 主进程的ID号，通过kill 命令发送信号给该 ID 号。

##### user

语法：user user [group]

默认值：nobody nobody

示例：user www users

该指令用于指定运行 nginx worker 进程的用户和组，默认的用户名和组名都是nobody。

##### worker_processes

语法：worker_processes [-]n

默认值：1

该指令用于设置nginx 的工作进程数

#### 主模块变量

- $nginx_version：当前运行的nginx 版本号
- $pid：进程 ID 号
- $realpath_root：ROOT 目录的绝对路径

#### 事件模块指令

事件模块是控制nginx 处理访问连接的指令集，跟主模块指令一样，事件模块指令也写在配置文件的最上方区域。

##### worker_connections

语法：worker_connections number

该指令用于设置每个工作进程能够处理的连接数。

### 标准 HTTP 模块

#### HTTP 核心模块

##### alias

语法：alias file-path|directory-path

使用环境：location

该指令用于在URL 和文件系统路径之间实现映射。

示例：

```conf
location /i/ {
    alias /spools/w3/images;
}
# 访问 URL 地址“/i/top.jpg”会返回文件“/spools/w3/images/top.jpg”
```

##### client_body_in_file_only

语法：clent_body_in_file_only on|off

默认值：off

使用环境：http、server、location

该指令允许将一个客户端的请求内容记录到一个文件中，该文件在请求完成后不会被删除。在内置Perl 中，该指令可以用于调试`$r -> request_body_file` 方法

##### client_body_temp_path

语法：client_body_temp_path dir-path [level1|level2|level3]

使用环境：http、server、location

该指令用于指定存放请求内容临时文件的目录。缓存目录最多支持3层子目录。

##### client_body_timeout

语法：clent_body_timeout time

默认值：60

使用环境：http、server、location

该指令用于设置读取客户端请求内容的超时时间，如果超过该指令的时间，将返回“Request time out” 错误信息（HTTP 状态码为408）

##### default_type

语法：default_type MIME-type

默认值：default_type text/plain

使用环境：http、server、location

MIME-type 用来告诉浏览器请求的文件媒体类型

##### error_page

语法：error_page code [code ...] [= !=answer-code] uri

使用环境：http、server、location、if in location

该指令用于设置如果出现指定的 HTTP 错误状态码，则返回客户端显示的对应 URI 地址。

示例：

```conf
# 遇到404错误码，则显示指定的404.html文件内容给客户端
error_page 404 /404.html
# 遇到502、503、504错误码，则显示指定的50x.html文件内容给客户端
error_page 502 503 504 /50x.html
# 遇到403错误码，则跳转地址
error_page 403 http://example.com/forbidden.html
# 更改 HTTP 的错误响应码为别的响应码
error_page 404 =200 /empty.gif
```

##### index

语法：index file [file...]

默认值：index index.html

使用环境：http、server、location

该指令用于设置默认首页文件。

##### listen

语法：listen address:port [default [backlog=num | rcvbuf=size | sndbuf=size | accept_filter=filter | deferrer | bind |ssl]]

默认值：listen 80

使用环境：server

该指令用于设置虚拟主机监听的服务器地址和端口。可以同时设置服务器地址和端口号，也可以只指定一个IP 地址，或者一个端口号，或者一个服务器名称。如果 listen 指令只设置一个服务器名和IP 地址，那么默认的端口号为80。

##### location 配置

语法规则：

```conf
location [=|~|~*|^~] /uri/ { … }

= ：表示精确匹配,这个优先级也是最高的
^~ ：表示uri以某个常规字符串开头，理解为匹配 url路径即可。nginx不对url做编码，因此请求为/static/20%/aa，可以被规则^~ '/static/ /aa'匹配到（注意是空格）。
~ ：表示区分大小写的正则匹配
~* ：表示不区分大小写的正则匹配(和上面的唯一区别就是大小写)
!~和!~*分别为区分大小写不匹配及不区分大小写不匹配的正则
/ 通用匹配，任何请求都会匹配到，默认匹配.
```

使用环境：server

##### server

语法：server{...}

使用环境：http

该指令用于配置虚拟主机。在server{...}中使用 listen 指令来为一个虚拟主机设置监听的 IP 和端口，使用 server_name 指令来设置不同的虚拟主机名称。

##### server_name

语法：server_name name {...}

#### upstream 模块

upstream 指令用于设置一组可以在proxy_pass 指令中使用的代理服务器，默认的负载均衡方式为轮询。upstream 模块中的 server 指令用于指定后端服务器的名称和参数，服务器的名称可以是一个域名、一个IP地址、端口号或UNIX Socket。而在 server{...} 虚拟主机内，可以通过 proxy_pass 指令设置进行反向代理的upstream 服务器集群。

upstream 模块是nginx 负载均衡的主要模块，它提供了一个简单方法来实现在轮询和客户端 IP 之间的后端服务器负载均衡，并可以对后端服务器进行健康检查。

```conf
upstream backend {
    server backend1.example.com weight=5;
    server backend2.example.com:8090;
    server unix:/tmp/backend3;
}

server {
    location / {
        proxy_pass http://backend;
    }
}
```

##### server 指令

语法： `server name [parameters]`

使用环境：upstream

该指令用于指定服务器后端服务器的名称和参数。服务器的名称可以是一个域名、一个IP地址、端口号或UNIX Socket。

在后端服务器名称之后，可以跟一下参数:

- weight=Number:设置服务器的权重，权重数值越高，被分配到的客户端请求数越多，如果没有设置权重，则默认权重为1
- max_fails=Number:在fail_timeout 指定的时间内对后端服务器请求失败的次数，如果检测到后端服务器无法连接及发生服务器错误（404错误除外），则标记为失败。如果没有设置，则默认值1。设置数值0将关闭这项检查。
- fail_timeout=Time：在经历参数max_fails 设置的失败次数后，暂停的时间
- down:标记服务器为永久离线状态，用于 ip_hash 指令
- backup：仅仅在非backup 服务器全部宕机或繁忙的时候才启用

##### upstream 相关变量

- $upstream_addr:处理请求的upstream服务器地址
- $upstream_status:upstream服务器的应答状态
- $upstream_response_time:upstream服务器响应时间（毫秒），多个响应以逗号和冒号分割
- $upstream_http_$HEADER:任意的HTTP协议头信息，例如：`$upstream_http_host`

#### access 模块

access 模块提供了一个简单的基于host名称的访问控制。通过该模块，可以允许或禁止指定的 IP 地址或 IP 段访问某些虚拟主机或目录。

示例：

```conf
location / {
    deny 192.168.1.1;
    allow 192.168.1.0/24;
    allow 10.1.1.0/16;
    deny all;
}
```

##### allow

语法：allow [address|CIDR|all]

允许指定的 IP 地址或 IP 段访问某些虚拟主机或目录。

##### deny

语法：deny [address|CIDR|all]

禁止指定的 IP 地址或 IP 段访问某些虚拟主机或目录。

### 可选 HTTP 模块

### 邮件模块

### 第三方模块和补丁

### 虚拟主机

虚拟主机使用的是特殊的软硬件技术，它把一台运行在因特网上的服务器主机分成一台台“虚拟”的主机，每台虚拟主机都可以是一个独立的网站，可以具有独立的域名，具有完整的Internet服务器功能（WWW、FTP、Email等），同一台主机上的虚拟主机之间是完全独立的。从网站访问者来看，每一台虚拟主机和一台独立的主机完全一样。

利用虚拟主机，不用为每个要运行的网站提供一台单独的 Nginx 服务器或单独运行一组 Nginx 工作进程。虚拟主机提供了在同一台服务器、同一组 Nginx 进程上运行多个网站的功能。

Nginx 可以配置多种类型的虚拟主机：

- 基于 **IP** 的虚拟主机
- 基于 **域名** 的虚拟主机
- 基于 **端口** 的虚拟主机

在nginx 中，一个 server{...} 就是一个虚拟主机，如果要配置多个虚拟主机，建立多段 server{...} 配置即可。监听的 IP 和端口也可不写 IP 地址，只写端口，如配置成 `listen 80`,则表示监听该服务器上所有 IP 的80端口，可通过server_name 区分不同的虚拟主机。

### Rewrite 规则

Rewrite 的主要功能就是实现 URL 重写，Nginx 的 Rewrite 规则采用 PCRE（Perl Compatible Regular Expression）Prel 兼容正则表达式的语法进行规则匹配。若需要Rewrite 的功能需要编译安装PCRE库。

通过 Rewrite 规则，可以实现规范的 URL、根据变量来做 URL 转向及选择配置。

#### 指令

Rewrite 规则的相关指令有 if、retwrite、set、return、break ，其中 rewrite 是最关键的指令。一个简单的 Rewrite 规则语法如下：

`rewrite ^/b(.*)\.html /play.php?video=$1 break;`

##### break 指令

语法：break

默认值：none

使用环境：server,location,if

该指令的作用是完成当前的规则集，不再处理 rewrite 指令

##### if 指令

语法：if(condition){...}

默认值：none

使用环境：server,location

该指令用于检查一个条件是否符合，如果条件符合，则执行大括号内的语句。if 指令不支持嵌套，不支持多个条件&&和||处理。

以下信息可以被指定为条件：

1. 变量名，错误的值包括：空字符串“”，或者任何以0开始的字符串
2. 变量比较可以使用“=”和“!=”运算符
3. 正则表达式模式匹配可以使用“~*”和“~”符号
4. “~”符号表示区分大小写字母的匹配
5. “~*”符号表示不区分大小写字母的匹配，如firefox和FireFox是匹配的
6. “!~”和“!~*”符号的作用跟“~”和“~*”相反，表示不匹配
7. “-f”和“!-f”用来判断文件是否存在
8. “-d”和“!-d”用来判断目录是否存在
9. “-e”和“!-e”用来判断文件或目录是否存在
10. “-x”和“!-x”用来判断文件是否可执行

部分正则表达式可以在圆括号"()"内，其值可以通过后面的变量$1至$9访问

##### return 指令

语法：return code

默认值：none

使用环境：server,location,if

该指令用于结束规则的执行并返回状态码给客户端。状态码可以使用这些值：204，400，402~406，408，410，411，413，416 及 500~504。此外，非标准状态码444将以不发送任何Header 头的方式结束连接。

##### rewrite 指令

语法：rewrite regex replacement flag

默认值：none

使用环境：server,location,if

该指令根据表达式来重定向 URI，或者修改字符串。该指令根据配置文件中的顺序来执行。

rewrite 指令的最后一项参数为 flag 标记，支持 flag 标记有：

- last：表示完成rewrite
- break：本条规则匹配完成后，终止匹配，不在匹配后面的规则
- redirect：返回302临时重定向，浏览器地址栏会显示跳转后的 URL 的地址
- permanent：返回301永久重定向，浏览器地址栏会显示跳转后的 URL 的地址

last 和 break 用于实现 URI 重写，浏览器地址栏的 URL 地址不变，但在服务器端访问的路径发生了变化。rediect 和 permanent 用来实现 URL 跳转，浏览器地址栏会显示跳转后的 URL 的地址

last 和 break 标记的实现功能类似，但二者存在细微的差别，使用 alias 指令时必须用 last 标记，使用 proxy_pass 指令时要使用 break 标记。last 标记在本条 rewrite 规则执行完毕后，会对其所在的 server{...} 标签重新发起请求，而 break 标记则在本条规则匹配完成后，终止匹配，不再匹配后面的规则。

一般在根location中（即 location /{...}）或直接在server标签中编写 rewrite 规则，推荐使用last 标记，在非根中（例如 location /child/{...}），则使用 break 标记。

**注**：对花括号来说，它们既能用在重定向的正则表达式里，也能用在配置文件里分割代码块，为了避免冲突，正则表达式如果带有花括号，应该用双引号（或单引号）包围。

##### set 指令

语法：set variable value

默认值：none

使用环境：server，location，if

该指令用于定义一个变量，并给变量赋值。变量的值可以为文本、变量及文本变量的联合。

示例：`set $varname 'hello'`

##### uninitialized_variable_warn 指令

语法：uninitialized_variable_warn on|off

默认值：uninitialized_variable_warn on

使用环境：http，server，location，if

该指令用于开启或关闭记录关于未初始化变量的警告信息，默认为开启

#### Rewrite 常用的全局变量

在if、location、rewrite 指令中，可以使用以下全局变量：

- $args
- $content_length
- $content_type
- $document_root
- $document_uri
- $host
- $http_user_agent
- $http_cookie
- $limit_rate
- $request_body_file
- $request_method
- $remote_addr
- $remote_port
- $remote_user
- $request_filename
- $request_uri
- $query_string
- $scheme
- $server_protocol
- $server_addr
- $server_name
- $server_port
- $uri

#### PCRE 正则表达式语法

字符  | 描述
:-------:|:------------------------------------------------------------
\\       | 将下一个字符标记为一个特殊字符、或一个原义字符、或一个向后引用、或一个八进制转义符。例如，“n”匹配字符“n”。“\\n”匹配一个换行符。序列“\\”匹配“”，而“\(”则匹配“(”。
^        | 匹配输入字符串的开始位置。如果设置了RegExp对象的Multiline属性，^也匹配“n”或“r”之后的位置。
$        | 匹配输入字符串的结束位置。如果设置了RegExp对象的Multiline属性，$也匹配“n”或“r”之前的位置。
\*       | 匹配前面的子表达式零次或多次。例如，zo\*能匹配“z”以及“zoo”。\*等价于{0,}。
\+       | 匹配前面的子表达式一次或多次。例如，“zo+”能匹配“zo”以及“zoo”，但不能匹配“z”。+等价于{1,}。
?        | 匹配前面的子表达式零次或一次。例如，“do(es)?”可以匹配“do”或“does”中的“do”。?等价于{0,1}。
{n}      | n是一个非负整数。匹配确定的n次。例如，“o{2}”不能匹配“Bob”中的“o”，但是能匹配“food”中的两个o。
{n,}     | n是一个非负整数。至少匹配n次。例如，“o{2,}”不能匹配“Bob”中的“o”，但能匹配“foooood”中的所有o。“o{1,}”等价于“o+”。“o{0,}”则等价于“o*”。
{n,m}    | m和n均为非负整数，其中n<=m。最少匹配n次且最多匹配m次。例如，“o{1,3}”将匹配“fooooood”中的前三个o。“o{0,1}”等价于“o?”。请注意在逗号和两个数之间不能有空格。
?        | 当该字符紧跟在任何一个其他限制符（*，+，?，{n}，{n,}，{n,m}）后面时，匹配模式是非贪婪的。非贪婪模式尽可能少的匹配所搜索的字符串，而默认的贪婪模式则尽可能多的匹配所搜索的字符串。例如，对于字符串“oooo”，“o+?”将匹配单个“o”，而“o+”将匹配所有“o”。
.        | 匹配除“n”之外的任何单个字符。要匹配包括“\\n”在内的任何字符，请使用像“[.\\n]”的模式。
(pattern)| 匹配pattern并获取这一匹配。所获取的匹配可以从产生的Matches集合得到，在VBScript中使用SubMatches集合，在JScript中则使用$0…$9属性。要匹配圆括号字符，请使用“\\(”或“\\)”。
(?:pattern)| 匹配pattern但不获取匹配结果，也就是说这是一个非获取匹配，不进行存储供以后使用。这在使用或字符“(|)”来组合一个模式的各个部分是很有用。例如“industr(?:y|ies)”就是一个比“industry|industries”更简略的表达式。
(?=pattern)| 后置正向预查，在任何匹配pattern的字符串开始处匹配查找字符串。这是一个非获取匹配，也就是说，该匹配不需要获取供以后使用。例如，“Windows(?=95|98|NT|2000)”能匹配“Windows2000”中的“Windows”，但不能匹配“Windows3.1”中的“Windows”。预查不消耗字符，也就是说，在一个匹配发生后，在最后一次匹配之后立即开始下一次匹配的搜索，而不是从包含预查的字符之后开始。
(?!pattern)| 后置负向预查，在任何不匹配pattern的字符串开始处匹配查找字符串。这是一个非获取匹配，也就是说，该匹配不需要获取供以后使用。例如“Windows(?!95|98|NT|2000)”能匹配“Windows3.1”中的“Windows”，但不能匹配“Windows2000”中的“Windows”。预查不消耗字符，也就是说，在一个匹配发生后，在最后一次匹配之后立即开始下一次匹配的搜索，而不是从包含预查的字符之后开始
(?<=pattern)| 前置正向预查，在任何匹配pattern的字符串开始处匹配查找字符串。这是一个非获取匹配，也就是说，该匹配不需要获取供以后使用。例如，“(?<=Windows)2000”能匹配“Windows2000”中的“2000”，但不能匹配“Office2000”中的“2000。预查不消耗字符，也就是说，在一个匹配发生后，在最后一次匹配之后立即开始下一次匹配的搜索，而不是从包含预查的字符之后开始。
(?<\!pattern)| 前置负向预查，在任何不匹配pattern的字符串开始处匹配查找字符串。这是一个非获取匹配，也就是说，该匹配不需要获取供以后使用。例如“(?<\!Windows)2000”能匹配“Office2000”中的“2000”，但不能匹配“Windows2000”中的“2000”。预查不消耗字符，也就是说，在一个匹配发生后，在最后一次匹配之后立即开始下一次匹配的搜索，而不是从包含预查的字符之后开始
x|y     | 匹配x或y。例如，“z|food”能匹配“z”或“food”。“(z|f)ood”则匹配“zood”或“food”。
[xyz]   | 字符集合。匹配所包含的任意一个字符。例如，“[abc]”可以匹配“plain”中的“a”。
[^xyz]  | 负值字符集合。匹配未包含的任意字符。例如，“[^abc]”可以匹配“plain”中的“p”。
[a-z]   | 字符范围。匹配指定范围内的任意字符。例如，“[a-z]”可以匹配“a”到“z”范围内的任意小写字母字符。
[^a-z]  | 负值字符范围。匹配任何不在指定范围内的任意字符。例如，“[^a-z]”可以匹配任何不在“a”到“z”范围内的任意字符。
\b      | 匹配一个单词边界，也就是指单词和空格间的位置。例如，“er\\b”可以匹配“never”中的“er”，但不能匹配“verb”中的“er”。
\B      | 匹配非单词边界。“er\\B”能匹配“verb”中的“er”，但不能匹配“never”中的“er”。
\cx     | 匹配由x指明的控制字符。例如，\\cM匹配一个Control-M或回车符。x的值必须为A-Z或a-z之一。否则，将c视为一个原义的“c”字符。
\d      | 匹配一个数字字符。等价于[0-9]。
\D      | 匹配一个非数字字符。等价于[^0-9]。
\f      | 匹配一个换页符。等价于x0c和\\cL。
\n      | 匹配一个换行符。等价于x0a和\\cJ。
\r      | 匹配一个回车符。等价于x0d和\\cM。
\s      | 匹配任何空白字符，包括空格、制表符、换页符等等。等价于[\\f\\n\\r\\t\\v]。
\S      | 匹配任何非空白字符。等价于[^\\f\\n\\r\\t\\v]。
\t      | 匹配一个制表符。等价于x09和\\cI。
\v      | 匹配一个垂直制表符。等价于x0b和\\cK。
\w      | 匹配包括下划线的任何单词字符。等价于“[A-Za-z0-9_]”。
\W      | 匹配任何非单词字符。等价于“[^A-Za-z0-9_]”。
\xn     | 匹配n，其中n为十六进制转义值。十六进制转义值必须为确定的两个数字长。例如，“x41”匹配“A”。“x041”则等价于“x04&1”。正则表达式中可以使用ASCII编码。.
\num    | 匹配num，其中num是一个正整数。对所获取的匹配的引用。例如，“(.)\1”匹配两个连续的相同字符。
\n      | 标识一个八进制转义值或一个向后引用。如果n之前至少n个获取的子表达式，则n为向后引用。否则，如果n为八进制数字（0-7），则n为一个八进制转义值。
\nm     | 标识一个八进制转义值或一个向后引用。如果nm之前至少有nm个获得子表达式，则nm为向后引用。如果nm之前至少有n个获取，则n为一个后跟文字m的向后引用。如果前面的条件都不满足，若n和m均为八进制数字（0-7），则nm将匹配八进制转义值nm。
\nml    | 如果n为八进制数字（0-3），且m和l均为八进制数字（0-7），则匹配八进制转义值nml。
\un     | 匹配n，其中n是一个用四个十六进制数字表示的Unicode字符。例如，u00A9匹配版权符号（?）。

#### Rewrite 规则编写实例

```conf
# 判断文件是否存在，如果不存在重定向
if(!-e $request_filename){
    rewrite ^/(.*)$ /index.php last;
}

# 多目录转成参数 abc.domain.com/sort/2 => abc.domain.com/index.php?act=sort&name=abc&id=2
if($host ~* (.*)\.domain\.com){
    set $sub_name $1;
    rewrite ^/sort\/(+d)\/?$ /index.php?act=sort&name=$sub_name&id=$1 last;
}

# 目录对换 /123456/xxxx => /xxxx?id=123456
rewrite ^/(\d+)/(.+)/ /$2?id=$1 last;

# 如果客户端使用IE浏览器，则重定向到/nginx-ie 目录下
if($http_user_agent ~ MISE){
    rewrite ^(.*)$ /nginx-ie/$1 break;
}

# 禁止访问多个目录
location ~ ^/(cron/templates)/ {
    deny all;
    break;
}

# 禁止访问以/data 开头的文件：
location ~ ^/data {
    deny all;
}

# 将多级目录下的文件转换成一个文件 /job-123-456-789.html 指向 /job/123/456/789.html
rewrite ^/job-([0-9]+)-([0-9]+)-([0-9]+)\.html$ /job/$1/$2/jobshow_$3.html last;
```

### 日志文件

与日志相关的指令主要有两条：

- log_format:用来设置日志的格式
- access_log:用来指定日志文件的存放路径、格式和缓存大小

两条指令在配置文件中的位置可以在 http{...} 之间，也可在虚拟主机之间，即 server{...} 之间。

#### log_format

语法： `log_format name format [format ...]`

- name：定义的格式名称
- format：定义的格式样式

#### access_log

用 log_format 指令设置了日志格式之后，需要用 access_log 指令指定日志文件存放路径。

access_log 指令的语法如下：

`access_log path [format [buffer=size | off]]`

- path：日志文件的存放路径
- format：使用log_format 指令设置的日志格式的名称
- buffer=size：设置内存区缓冲区的大小
- off：`access_log off` 关闭日志记录

### ngx_http_core_module模块提供的变量

参数名称            | 注释
:-----------------:|:------------------------------------------------------------------------
$arg_PARAMETER     | HTTP 请求中某个参数的值，如/index.php?site=www.ttlsa.com，可以用$arg_site取得www.ttlsa.com这个值.
$args              | HTTP 请求中的完整参数。例如，在请求/index.php?width=400&height=200 中，$args表示字符串width=400&height=200.
$binary_remote_addr| 二进制格式的客户端地址。例如：\x0A\xE0B\x0E
$body_bytes_sent   | 表示在向客户端发送的http响应中，包体部分的字节数
$content_length    | 表示客户端请求头部中的Content-Length 字段
$content_type      | 表示客户端请求头部中的Content-Type 字段
$cookie_COOKIE     | 表示在客户端请求头部中的cookie 字段
$document_root     | 表示当前请求所使用的root 配置项的值
$uri               | 表示当前请求的URI，不带任何参数
$document_uri      | 与$uri 含义相同
$request_uri       | 表示客户端发来的原始请求URI，带完整的参数。$uri和$document_uri未必是用户的原始请求，在内部重定向后可能是重定向后的URI，而$request_uri 永远不会改变，始终是客户端的原始URI.
$host              | 表示客户端请求头部中的Host字段。如果Host字段不存在，则以实际处理的server（虚拟主机）名称代替。如果Host字段中带有端口，如IP:PORT，那么$host是去掉端口的，它的值为IP。$host 是全小写的。这些特性与http_HEADER中的http_host不同，http_host只取出Host头部对应的值。
$hostname          | 表示 Nginx所在机器的名称，与 gethostbyname调用返回的值相同
$http_HEADER       | 表示当前 HTTP请求中相应头部的值。HEADER名称全小写。例如，示请求中 Host头部对应的值 用 $http_host表
$sent_http_HEADER  | 表示返回客户端的 HTTP响应中相应头部的值。HEADER名称全小写。例如，用 $sent_ http_content_type表示响应中 Content-Type头部对应的值
$is_args           | 表示请求中的 URI是否带参数，如果带参数，$is_args值为 ?，如果不带参数，则是空字符串
$limit_rate        | 表示当前连接的限速是多少，0表示无限速
$nginx_version     | 表示当前 Nginx的版本号
$query_string      | 请求 URI中的参数，与 $args相同，然而 $query_string是只读的不会改变
$remote_addr       | 表示客户端的地址
$remote_port       | 表示客户端连接使用的端口
$remote_user       | 表示使用 Auth Basic Module时定义的用户名
$request_filename  | 表示用户请求中的 URI经过 root或 alias转换后的文件路径
$request_body      | 表示 HTTP请求中的包体，该参数只在 proxy_pass或 fastcgi_pass中有意义
$request_body_file | 表示 HTTP请求中的包体存储的临时文件名
$request_completion|当请求已经全部完成时，其值为 “ok”。若没有完成，就要返回客户端，则其值为空字符串；或者在断点续传等情况下使用 HTTP range访问的并不是文件的最后一块，那么其值也是空字符串。
$request_method    | 表示 HTTP请求的方法名，如 GET、PUT、POST等
$scheme            | 表示 HTTP scheme，如在请求 `https://nginx.com/`中表示 https
$server_addr       | 表示服务器地址
$server_name       | 表示服务器名称
$server_port       |表示服务器端口
$server_protocol   | 表示服务器向客户端发送响应的协议，如 HTTP/1.1或 HTTP/1.0

### 缓存模块

## nginx命令

nginx安装后只有一个程序文件，本身并不提供各种管理程序，它是使用参数和信号机制对nginx进程本身进行控制的。

nginx的使用：`nginx [参数]`

参数如下：
- `start`：启动
- `restart`：重启
- `stop`：停止
- `-c <path_to_config>`：使用指定的配置文件而不是conf目录下的`nginx.conf`
- `-t`：测试配置文件是否正确；在运行时需要重新加载配置的时候，需要用此参数来检测所修改的配置文件是否有语法错误。
- `-s reload`：重载
- `-s`：stop停止 

## 负载均衡

负载均衡是由多台服务器以对称的方式组成一个服务器集合，没台服务器都具有等价的地位，都可以单独对外提供服务而无需其他服务器的辅助。通过某种负载分担技术，将外部发送过来的请求均匀分配到对称结构中的某一台服务器上，而接收到请求的服务器独立的回应客户的请求。负载均衡能平均分配客户请求到服务器队列，藉此快速获取重要数据，解决大量并发访问服务问题。这种群集技术可以用最少的投资获得接近于大型主机的性能。

## 反向代理

反向代理（Reverse Proxy）是指以代理服务器来接收Internet 上的连接请求，然后将请求转发给内部网络上的服务器，并经从服务器上得到的结果返回给 Internet 上请求连接的客户端，此时代理服务器对外就表现为一个服务器。