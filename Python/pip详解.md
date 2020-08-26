pip为包管理器，跟Linux上众多的包管理器的功能大致相同，就是对包进行管理，使得包的安装，更新和卸载更容易。

pip包管理器下载的python库来自：[PyPI](https://pypi.org/)

pip在python2.7.9以上和3.4以上自带，通过venv,virtualenv和pyvenv创建的虚拟环境默认也会安装，不过最好通过以下命令更新到最新版

Windows:

```sh
python -m pip install -U pip
```

Linux或Mac

```sh
pip install -U pip
```

## pip使用

### 验证pip是否安装成功

```sh
pip --version
```

### 包的安装

```sh
pip install PackageName                # 安装最新版本
pip install PackageName==1.0.4         # 安装指定具体的某个版本
pip install PackageName>=1.0.4         # 指定最小版本
```

通过使用`==`, `>=`, `<=`, `>`, `<`来指定一个版本号

### 通过requirements.txt进行安装

```sh
pip install -r requirements.txt
```

### 显示包文件

```sh
pip show --files PackageName
```

该命令能够显示包的简介，包安装的位置，以及整个包包含的文件

### 显示所有的包

列出当前python环境下安装的所有的包

```sh
pip list
```

### 显示过期的包

列出所有可以更新的包：

```sh
pip list --outdated
```

### 包的更新

```sh
pip install -U PackageName
```

该命令将指定包更新到仓库最新版

### 包的卸载

```sh
pip uninstall PackageName
```

## pip help

```sh
C:\Users\superz>pip help

Usage:
  pip <command> [options]

Commands:
  install                     Install packages.
  download                    Download packages.
  uninstall                   Uninstall packages.
  freeze                      Output installed packages in requirements format.
  list                        List installed packages.
  show                        Show information about installed packages.
  check                       Verify installed packages have compatible dependencies.
  config                      Manage local and global configuration.
  search                      Search PyPI for packages.
  wheel                       Build wheels from your requirements.
  hash                        Compute hashes of package archives.
  completion                  A helper command used for command completion.
  help                        Show help for commands.

General Options:
  -h, --help                  Show help.
  --isolated                  Run pip in an isolated mode, ignoring environment variables and user configuration.
  -v, --verbose               Give more output. Option is additive, and can be used up to 3 times.
  -V, --version               Show version and exit.
  -q, --quiet                 Give less output. Option is additive, and can be used up to 3 times (corresponding to
                              WARNING, ERROR, and CRITICAL logging levels).
  --log <path>                Path to a verbose appending log.
  --proxy <proxy>             Specify a proxy in the form [user:passwd@]proxy.server:port.
  --retries <retries>         Maximum number of retries each connection should attempt (default 5 times).
  --timeout <sec>             Set the socket timeout (default 15 seconds).
  --exists-action <action>    Default action when a path already exists: (s)witch, (i)gnore, (w)ipe, (b)ackup,
                              (a)bort).
  --trusted-host <hostname>   Mark this host as trusted, even though it does not have valid or any HTTPS.
  --cert <path>               Path to alternate CA bundle.
  --client-cert <path>        Path to SSL client certificate, a single file containing the private key and the
                              certificate in PEM format.
  --cache-dir <dir>           Store the cache data in <dir>.
  --no-cache-dir              Disable the cache.
  --disable-pip-version-check
                              Don't periodically check PyPI to determine whether a new version of pip is available for
                              download. Implied with --no-index.
  --no-color                  Suppress colored output
```

## FAQ

### 方括号在pip安装中的含义是什么

在日常安装包的过程中，会看到如下的命令：

```sh
pip install "splinter[django]"
```

上述pip安装命令使用的语法是：

```sh
pip install "project[extra]"
```

来自`@chetner`的解释：

> 命令`pip install splinter django`将安装两个名为`splinter`和`django`的包。另一方面，`splinter[django]`会安装`splinter`包的变体，其中包含`django`的支持。请注意，它与`django`包本身无关，而只是由`splinter`包定义的字符串，用于启用的特定功能集。

### 从Github的分支进行安装

从Github的分支安装，格式为：`pip install git+github的https地址@分支`

```sh
pip install git+https://github.com/shadowsocks/shadowsocks.git@master
```

从Github下的压缩包安装，格式为：`pip install zip地址包`

```sh
pip install https://github.com/shadowsocks/shadowsocks/archive/master.zip
```











