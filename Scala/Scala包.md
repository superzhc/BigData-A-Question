和 Java 一样，Scala 中管理项目可以使用包，但 **Scala 中的包的功能更加强大**，使用也**相对复杂些**。

## 特点概述

**基本语法**

```
package 包名
```

**Scala 包的三大作用**（和 Java 一样）

1. 区分相同名字的类
2. 当类很多时，可以很好的管理类
3. 控制访问范围
4. 可以对类的功能进行扩展

注：**Scala 中包名和源码所在的系统文件目录结构可以不一致，但是编译后的字节码文件路径和包名会保持一致**（这个工作有编译器完成）

**命名方式**

同 java 相同

**Scala 自动引入的常用包**

- `java.lang.*`
- `scala包`
- `Predef包`

**使用细节和注意事项**：

```scala
// 方法一
package com.one.two

object packagedemo{
   def hello():Unit={
       println("hello world")
   }
}

// 方法二
package com.one
package two

object packagedemo{
   def hello():Unit={
       println("hello world")
   }
}

// 方法三
package com.one{
    package two{
        object packagedemo{
           def hello():Unit={
               println("hello world")
           }
        }
    }
}
```

上述的三个方法的包使用都是等价的

由方法三可以知道，在 scala 中的一个文件中，可以同时创建多个包，以及给各个包创建 class，trait 和 object

