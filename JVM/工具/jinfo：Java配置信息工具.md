# jinfo：Java 配置信息工具

jinfo(Configuration Info for Java)的作用是实时地查看和调整虚拟机各项参数。它的基本语法为：

```sh
jinfo <option> <pid>
```

其中 option 可以为以下信息：

- `-flag <name>`：打印指定 JVM 的参数值
- `-flag [+|-]<name>`：设置指定 JVM 参数的布尔值
- `-flag <name>=<value>`：设置指定 JVM 参数的值
- `-flags`：输出所有 JVM 参数
- `-sysprops`：输出 Java 的环境变量

> 在很多情况下，Java 应用程序不会指定所有的 JVM 参数。而此时，开发人员可能不知道某一具体的 JVM 参数的默认值。在这种情况下，可能需要通过查找文档获取某个参数的默认值。这个查找过程可能是非常艰难的，但有了 jinfo 工具，开发人员可以很方便的找到 JVM 参数的当前值。

除了查找参数的值，jinfo 也支持修改部分参数的数值，当然，这个修改能力是及其有限的。

