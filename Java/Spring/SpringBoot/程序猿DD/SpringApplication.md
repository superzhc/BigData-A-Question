SpringApplication 类提供了一种快捷方式，用于从 `main()` 方法启动 Spring 应用，只需要将该任务委托给 `SpringApplication.run` 静态方法:

```java
public static void main(String[] args){
    SpringApplication.run(MySpringConfiguration.class, args);
}
```



### 启动失败

如果应用启动失败，注册的`FailureAnalyzers`就有机会提供一个特定的错误信息，及具体的解决该问题的动作。

**注**：Spring Boot 提供了很多的 FailureAnalyzer 实现，用户也可以自定义实现。

如果没有可用于处理该异常的失败分析器（failure analyzers），那需要展示完整的 auto-configuration 报告以便更好的查看出问题的地方，因此你需要启用`org.springframework.boot.autoconfigure.logging.AutoConfigurationReportLoggingInitializer`的debug属性，或开启DEBUG日志级别。

### 自定义 Banner

通过在classpath下添加一个 `banner.txt` 或设置 `banner.location` 来指定相应的文件可以改变启动过程中打印的banner。如果这个文件有特殊的编码，可以使用 `banner.encoding` 设置它（默认为UTF-8）。除了文本文件，也可以添加一个 `banner.gif` ， `banner.jpg` 或 `banner.png` 图片，或设置 `banner.image.location` 属性。图片会转换为字符画（ASCII art）形式，并在所有文本banner上方显示。

在 `banner.txt` 中可以使用如下占位符：

| 变量                                                         | 描述                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| ${application.version}                                       | MANIFEST.MF中声明的应用版本号，例如`Implementation-Version: 1.0`会打印`1.0` |
| ${application.formatted-version}                             | MANIFEST.MF中声明的被格式化后的应用版本号（被括号包裹且以v作为前缀），用于显示，例如(`v1.0`) |
| ${spring-boot.version}                                       | 当前Spring Boot的版本号，例如`1.4.1.RELEASE`                 |
| ${spring-boot.formatted-version}                             | 当前Spring Boot被格式化后的版本号（被括号包裹且以v作为前缀）, 用于显示，例如(`v1.4.1.RELEASE`) |
| ${Ansi.NAME}（或${AnsiColor.NAME}，${AnsiBackground.NAME}, ${AnsiStyle.NAME}） | NAME代表一种ANSI编码，具体详情查看[AnsiPropertySource](https://github.com/spring-projects/spring-boot/tree/v1.4.1.RELEASE/spring-boot/src/main/java/org/springframework/boot/ansi/AnsiPropertySource.java) |
| ${application.title}                                         | `MANIFEST.MF`中声明的应用title，例如`Implementation-Title: MyApp`会打印`MyApp` |

**注** 如果想以编程的方式产生一个banner，可以使用`SpringBootApplication.setBanner(…)`方法，并实现`org.springframework.boot.Banner`接口的`printBanner()`方法。

也可以使用`spring.main.banner-mode`属性决定将banner打印到何处，`System.out`（`console`），配置的logger（`log`）或都不输出（`off`)。

打印的banner将注册成一个名为`springBootBanner`的单例bean。