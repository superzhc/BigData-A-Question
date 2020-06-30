# Classpath 是什么

> classpath就是class的path，也就是类文件(*.class的路径)。

对于 maven 项目，开发期间的 `src/main/java`、`src/main/resources`文件夹中的文件，在编译打包后到了生产包的 **`WEB-INF/classes/`** 目录下；而原来`WEB-INF`下面的`views`和`web.xml`则仍然还是在`WEB-INF`下面。同时由maven引入的依赖都被放入到了`WEB-INF/lib/`下面。最后，编译后的class文件和资源文件都放在了`classes`目录下。

在编译打包项目中，根目录是 `META-INF` 和 `WEB-INF`，其中classes文件夹就是所需的classpath。

注：
- `classpath:`这种前缀，就只能代表一个文件
- `classpath*:`这种前缀，则可以代表多个匹配的文件