## 直接import

这种方式有个大前提，python执行文件和模块在同一层级目录，如下：

```tree
python
--|pkg
  --|__init__.py
  --|module1.py
  --|module2.py
--|main.py
```

> 使用模块方式为：先导入-》接着输入模块.变量|函数

## 通过sys模块导入自定义模块的path

如果执行文件和模块不在同一目录，这时候直接import是找不到自定义模块的。如下所示：

```tree
python
--|pkg
  --|__init__.py
  --|module1.py
  --|module2.py
--|main
  --|main.py
```

sys模块是python内置的，因此导入自定义模块的步骤如下：

1. 先导入sys模块
2. 然后通过`sys.path.append(path)`函数来导入自定义模块所在的目录
3. 导入自定义模块

`main.py`的代码如下：

```py
# main.py
# -*- coding: utf-8 -*-
import sys
sys.path.append(r"C:\xxx\python")
from pkg import module1
module1.hi()
```

## 通过pth文件找到自定义模块

这个方法原理就是利用了系统变量，python会扫描path变量的路径来导入模块，可以在系统path里面添加。但还是推荐使用pth文件添加。

模块和执行文件目录结构跟上面一致：

```tree
python
--|pkg
  --|__init__.py
  --|module1.py
  --|module2.py
--|main
  --|main.py
```

在Python安装目录的`$PYTHON_HOME/Lib/site-packages`下创建`superz.pth`文件，文件内容写入pkg模块的地址。

完成pth的写入后就可以在项目中引入自定义模块了