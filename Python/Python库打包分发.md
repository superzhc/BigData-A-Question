Python 有非常丰富的第三方库可以使用，很多开发者会向 pypi 上提交自己的 Python 包。要想向 pypi 包仓库提交自己开发的包，首先要将自己的代码打包，才能上传分发。

### distutils简介

distutils是标准库中负责建立 Python 第三方库的安装器，使用它能够进行 Python 模块的安装和发布。distutils 对于简单的分发很有用，但功能缺失。大部分 Python 用户会使用更先进的 steptools 模块。

### setuptools简介

setuptools 是 distutils 增强版，不包括在标准库中。其扩展了很多功能，能够帮助开发者更好的创建和分发 Python 包。大部分 Python 用户都会使用更先进的 setuptools 模块。

setuptools有一个fork分支是distribute。它们共享相同的命名空间，因此如果安装了distribute，`import setuptools`时实际上将导入使用distribute创建的包。

> distribute已经合并回setuptools

~~还有一个打包分发工具时distutils2，其试图尝试充分利用distutil，setuptools和distribute并成Python标准库中的标准工具。但该计划并没有达到预期的目的，且已经是一个废弃的项目。~~

因此，setuptools是一个优秀的，可靠的Python包安装与分发工具。以下涉及到包的安装和分发均针对setuptools，并不保证distutils可用。

### 包格式

Python库打包的格式包括`Wheel`和`Egg`。Egg格式是由setuptools在2004年引入，而Wheel格式是由PEP427在2012年定义。使用Wheel和Egg安装都不需要重新构建和编译，其在发布之前就应该完成测试和构建。

Egg和Wheel本质上都是一个zip格式包，Egg文件使用`.egg`扩展名，Wheel使用`.whl`扩展名。

Wheel的出现是为了替代Egg，其现在被认为是Python的二进制包的标准格式。

以下是Wheel和Egg的主要区别：

- Wheel有一个官方的PEP427来定义，而Egg没有PEP定义
- Wheel是一种分发格式，即打包格式。而Egg既是一种分发格式，也是一种运行时安装的格式，并且是可以被直接import
- Wheel文件不会包含`.pyc`文件
- Wheel使用和PEP376兼容的`.dist-info`目录，而Egg使用`.egg-info`目录
- Wheel有着更丰富的命名规则。
- Wheel是有版本的。每个Wheel文件都包含wheel规范的版本和打包的实现
- Wheel在内部被`sysconfig path type`管理，因此转向其他格式也更容易

### setup.py文件

Python 库打包分发的关键在于编写`setup.py`文件。`setup.py`文件编写的规则是从setuptools或者distuils模块导入**setup函数**，并传入各类参数进行调用。

```py
# coding:utf-8

from setuptools import setup
# or
# from distutils.core import setup  

setup(
    name='demo',     # 包名字
    version='1.0',   # 包版本
    description='This is a test of the setup',   # 简单描述
    author='huoty',  # 作者
    author_email='sudohuoty@163.com',  # 作者邮箱
    url='https://www.konghy.com',      # 包的主页
    packages=['demo'],                 # 包
)
```

#### 参数概述

`setup`函数常用的参数如下：

|          参数           |                          说明                          |
| ---------------------- | ----------------------------------------------------- |
| name                   | 包名称                                                 |
| version                | 包版本                                                 |
| author	             | 程序的作者                                             |
| author_email           | 程序的作者的邮箱地址                                    |
| maintainer             | 维护者                                                 |
| maintainer_email	     | 维护者的邮箱地址                                        |
| url	                 | 程序的官网地址                                          |
| license	             | 程序的授权信息                                          |
| description	         | 程序的简单描述                                          |
| long_description	     | 程序的详细描述                                          |
| platforms              | 程序适用的软件平台列表                                  |
| classifiers	         | 程序的所属分类列表                                      |
| keywords               | 程序的关键字列表                                       |
| packages               | 需要处理的包目录(通常为包含 `__init__.py` 的文件夹)       |
| py_modules	         | 需要打包的 Python 单文件列表                            |
| download_url	         | 程序的下载地址                                          |
| cmdclass	             | 添加自定义命令                                          |
| package_data	         | 指定包内需要包含的数据文件                               |
| include_package_data   | 自动包含包内所有受版本控制(cvs/svn/git)的数据文件        |
| exclude_package_data	 | 当 include_package_data 为 True 时该选项用于排除部分文件 |
| data_files             | 打包时需要打包的数据文件，如图片，配置文件等              |
| ext_modules	         | 指定扩展模块                                            |
| scripts	             | 指定可执行脚本,安装时脚本会被安装到系统 PATH 路径下       |
| package_dir            | 指定哪些目录下的文件被映射到哪个源码包                   |
| requires	             | 指定依赖的其他包                                        |
| provides	             | 指定可以为哪些模块提供依赖                               |
| install_requires       | 安装时需要安装的依赖包                                  |
| entry_points           | 动态发现服务和插件，下面详细讲                           |
| setup_requires	     | 指定运行 setup.py 文件本身所依赖的包                     |
| dependency_links	     | 指定依赖包的下载地址                                    |
| extras_require         | 当前包的高级/额外特性需要依赖的分发包                    |
| zip_safe	             | 不压缩包，而是以目录的形式安装                           |

#### find_packages

对于简单工程来说，手动增加packages参数是容易。而对于复杂的工程来说，可能添加很多的包，这是手动添加就变得麻烦。Setuptools模块提供了一个**find_packages函数**,它默认在与`setup.py`文件同一目录下搜索各个含有`__init__.py`的目录做为要添加的包。

```py
find_packages(where='.', exclude=(), include=('*',))
```

find_packages函数的第一个参数用于指定在哪个目录下搜索包，参数exclude用于指定排除哪些包，参数include指出要包含的包。

默认默认情况下`setup.py`文件只在其所在的目录下搜索包。如果不用find_packages，想要找到其他目录下的包，也可以设置 package_dir参数，其指定哪些目录下的文件被映射到哪个源码包，如: `package_dir={'': 'src'}`表示“root package”中的模块都在 src 目录中。




















