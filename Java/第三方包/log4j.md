# log4j

> log4j 是 Apache 的开源项目，是一个功能强大的日志组件，提供方便的日志记录。在 Apache 网站：<[jakarta.apache.org/log4j](http://jakarta.apache.org/log4j)> 可以免费下载到 Log4J 最新版本的软件包。

## 导入及配置

### Maven 导入

```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

### log4j.properties 文件

```properties
### 设置###
log4j.rootLogger = debug,stdout,D,E

### 输出信息到控制抬 ###
log4j.appender.stdout = org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target = System.out
log4j.appender.stdout.layout = org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern = [%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS} method:%l%n%m%n

### 输出DEBUG 级别以上的日志到=/home/duqi/logs/debug.log ###
log4j.appender.D = org.apache.log4j.DailyRollingFileAppender
log4j.appender.D.File = /home/duqi/logs/debug.log
log4j.appender.D.Append = true
log4j.appender.D.Threshold = DEBUG
log4j.appender.D.layout = org.apache.log4j.PatternLayout
log4j.appender.D.layout.ConversionPattern = %-d{yyyy-MM-dd HH:mm:ss}  [ %t:%r ] - [ %p ]  %m%n

### 输出ERROR 级别以上的日志到=/home/admin/logs/error.log ###
log4j.appender.E = org.apache.log4j.DailyRollingFileAppender
log4j.appender.E.File =/home/admin/logs/error.log
log4j.appender.E.Append = true
log4j.appender.E.Threshold = ERROR
log4j.appender.E.layout = org.apache.log4j.PatternLayout
log4j.appender.E.layout.ConversionPattern = %-d{yyyy-MM-dd HH:mm:ss}  [ %t:%r ] - [ %p ]  %m%n
```

## Log4J 基本使用方法

Log4j 由三个重要的组件构成：日志信息的优先级，日志信息的输出目的地，日志信息的输出格式。日志信息的优先级从高到低有`ERROR`、`WARN`、`INFO`、`DEBUG`，分别用来指定这条日志信息的重要程度；日志信息的输出目的地指定了日志将打印到控制台还是文件中；而输出格式则控制了日志信息的显示内容。

### 定义配置文件

Log4j 支持两种配置文件格式，一种是XML格式的文件，一种是Java特性文件(键=值)。

#### XML配置文件

xml格式的log4j配置文件需要使用org.apache.log4j.xml.DOMConfigurator.configure()方法来读入。对xml文件的语法定义可以在log4j的发布包中找到：`org/apache/log4j/xml/log4j.dtd`。

log4j的xml配置文件的树状结构如下所示，注意下图只显示了常用的部分。

```java
xml declaration and DTD
 |
log4j:configuration
 |
 +-- appender (name, class)
 |     |
 |     +-- param (name, value)
 |     +-- layout (class)
 |           |
 |           +-- param (name, value)
 +-- logger (name, additivity)
 |     |
 |     +-- level (class, value)
 |     |     |
 |     |     +-- param (name, value)
 |     +-- appender-ref (ref)
 +-- root
 |
 +-- param (name, class)
 +-- level
 |     |
 |     +-- param (name, value)
 +-- appender-ref (ref)
<!-- xml配置文件的头部包括两个部分：xml声明和DTD声明 -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration xmlns:log4j='http://jakarta.apache.org/log4j/' >
    <!-- 
        输出方式appender一般有5种：
        org.apache.log4j.RollingFileAppender(滚动文件，自动记录最新日志)
        org.apache.log4j.ConsoleAppender (控制台)
        org.apache.log4j.FileAppender (文件)
        org.apache.log4j.DailyRollingFileAppender (每天产生一个日志文件)
        org.apache.log4j.WriterAppender (将日志信息以流格式发送到任意指定的地方)
    -->
    <!-- 定义一个日志输出目的地 -->
    <appender name="myConsole" class="org.apache.log4j.ConsoleAppender">
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="[%d{dd HH:mm:ss,SSS\} %-5p] [%t] %c{2\} - %m%n" />
        </layout>

        <!--过滤器设置输出的级别-->
        <filter class="org.apache.log4j.varia.LevelRangeFilter">
            <param name="levelMin" value="debug" />
            <param name="levelMax" value="warn" />
            <param name="AcceptOnMatch" value="true" />
        </filter>
    </appender>

    <appender name="myFile" class="org.apache.log4j.RollingFileAppender">
        <param name="File" value="D:/output.log" /><!-- 设置日志输出文件名 -->
        <!-- 设置是否在重新启动服务时，在原有日志的基础添加新日志 -->
        <param name="Append" value="true" />
        <param name="MaxBackupIndex" value="10" />
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%p (%c:%L)- %m%n" />
        </layout>
    </appender>

    <appender name="activexAppender" class="org.apache.log4j.DailyRollingFileAppender">
        <param name="File" value="E:/activex.log" />
        <param name="DatePattern" value="'.'yyyy-MM-dd'.log'" />
        <layout class="org.apache.log4j.PatternLayout">
         <param name="ConversionPattern" value="[%d{MMdd HH:mm:ss SSS\} %-5p] [%t] %c{3\} - %m%n" />
        </layout>
    </appender>

    <!--定义一个日志写出器-->
    <!-- 指定logger的设置，additivity指示是否遵循缺省的继承机制-->
    <logger name="com.runway.bssp.activeXdemo" additivity="false">
        <level value ="info"/>
        <!-- 定义该logger的输出目的地 -->
        <appender-ref ref="activexAppender" />
    </logger>

    <!-- 根logger的设置-->
    <root>
        <level value ="debug"/>
        <appender-ref ref="myConsole"/>
        <appender-ref ref="myFile"/>
    </root>

</log4j:configuration>
```

#### Java特性文件

##### 1. 配置根 Logger

语法为：`log4j.rootLogger=[level],appenderName,appenderName,...`

其中，level 是日志记录的优先级，分为`OFF`、`FATAL`、`ERROR`、`WARN`、`INFO`、`DEBUG`、`ALL` 或者自定义级别。Log4j建议只使用四个级别，优先级从高到低分别是`ERROR`、`WARN`、`INFO`、`DEBUG`。通过在这里定义的级别，可以控制到应用程序中相应级别的日志信息的开关。比如定义了`INFO`级别，则应用程序中所有`DEBUG`级别的日志信息将不会打印出来。appenderName 就是指把日志信息输出到哪个地方，可以同时指定多个输出目的地。

##### 2. 配置文件的输出目的地 Appender

配置代码的格式如下：

```properties
log4j.appender.appenderName = fully.qualified.name.of.appender.class  
log4j.appender.appenderName.option1 = value1  
# …  
log4j.appender.appenderName.option = valueN
```

其中，Log4j提供的appender有以下几种：

- `org.apache.log4j.ConsoleAppender`（控制台），
- `org.apache.log4j.FileAppender`（文件），
- `org.apache.log4j.DailyRollingFileAppender`（每天产生一个日志文件），
- `org.apache.log4j.RollingFileAppender`（文件大小到达指定尺寸的时候产生一个新的文件），
- `org.apache.log4j.WriterAppender`（将日志信息以流格式发送到任意指定的地方）

##### 配置日志信息的格式（布局）Layout

其语法为：

```properties
log4j.appender.appenderName.layout = fully.qualified.name.of.layout.class  
log4j.appender.appenderName.layout.option1 = value1  
# …  
log4j.appender.appenderName.layout.option = valueN
```

其中，Log4j提供的layout有以下几种：

- `org.apache.log4j.HTMLLayout`（以HTML表格形式布局），
- `org.apache.log4j.PatternLayout`（可以灵活地指定布局模式），
- `org.apache.log4j.SimpleLayout`（包含日志信息的级别和信息字符串），
- `org.apache.log4j.TTCCLayout`（包含日志产生的时间、线程、类别等等信息）

Log4J采用类似C语言中的printf函数的打印格式格式化日志信息，打印参数如下：

- `%m`:输出代码中指定的消息
- `%p`:输出优先级，即DEBUG，INFO，WARN，ERROR，FATAL
- `%r`:输出自应用启动到输出该log信息耗费的毫秒数
- `%c`:输出所属的类目，通常就是所在类的全名
- `%t`:输出产生该日志事件的线程名
- `%n`:输出一个回车换行符，Windows平台为“rn”，Unix平台为“n”
- `%d`:输出日志时间点的日期或时间，默认格式为ISO8601，也可以在其后指定格式，比如：%d{yyy MMM dd HH:mm:ss,SSS}，输出类似：2002年10月18日 22：10：28，921
- `%l`:输出日志事件的发生位置，包括类目名、发生的线程，以及在代码中的行数。举例：Testlog4.main(TestLog4.java:10)

### 在代码中使用Log4j

#### 获取记录器

使用Log4j，第一步就是获取日志记录器，这个记录器将负责控制日志信息。其语法为：`public static Logger getLogger( String name)`；通过指定的名字获得记录器，如果必要的话，则为这个名字创建一个新的记录器。Name一般取本类的名字，比如：`static Logger logger = Logger.getLogger(ServerWithLog4j.class.getName ())`。

#### 读取配置文件

当获得了日志记录器之后，第二步将配置Log4j环境，其语法为：

- `BasicConfigurator.configure()`：自动快速地使用缺省Log4j环境。
- `PropertyConfigurator.configure(String configFilename)`：读取使用Java的特性文件编写的配置文件。
- `DOMConfigurator.configure(String filename)`：读取XML形式的配置文件。

#### 插入记录信息（格式化日志信息）

其语法如下：

```java
Logger.debug(Object message);  
Logger.info(Object message);  
Logger.warn(Object message);  
Logger.error(Object message);
```

### 日志级别

每个Logger都被了一个日志级别（log level），用来控制日志信息的输出。日志级别从高到低分为：

- `off`:最高等级，用于关闭所有日志记录。
- `fatal`:指出每个严重的错误事件将会导致应用程序的退出。
- `error`:指出虽然发生错误事件，但仍然不影响系统的继续运行。
- `warn`:表明会出现潜在的错误情形。
- `info`:一般和在粗粒度级别上，强调应用程序的运行全程。
- `debug`:一般用于细粒度级别上，对调试应用程序非常有帮助。
- `all`:最低等级，用于打开所有日志记录。

上面这些级别是定义在org.apache.log4j.Level类中。Log4j只建议使用4个级别，优先级从高到低分别是error,warn,info和debug。通过使用日志级别，可以控制应用程序中相应级别日志信息的输出。例如，如果使用info级别，则应用程序中所有低于info级别的日志信息(如debug)将不会被打印出来。

## 源码分析

Log4J将写日志功能抽象成七个核心类或者接口：`Logger`、`LoggerRepository`、`Level`、`LoggingEvent`、`Appender`、`Layout`、`ObjectRender`。

- ```
  Logger
  ```

  :用于对日志记录行为的抽象，提供记录不同级别日志的接口；

  ```java
  public class Logger extends Category
  {
  // Logger继承Category，Category也是一种日志类
  }
  ```

- ```
  Appender
  ```

  :对记录日志形式的抽象；

  ```java
  public interface Appender
  {
  // Appender抽象成了接口，然后主要的实现是WriterAppender，常用的ConsoleAppender，FileAppender都继承了该类。
  // 实际编码中经常会遇到DailyRollingFileAppender，RollingFileAppender都继承于FileAppender。
  }
  ```

- ```
  Layout
  ```

  :是对日志行格式的抽象；

  ```java
  public abstract class Layout implements OptionHandler
  {
  // Layout抽象成 一个模板，比较常用的PatternLayout，HTMLLayout都是该类子类
  }
  ```

- ```
  Level
  ```

  :对日志级别的抽象；

  ```java
  public class Level extends Priority implements Serializable
  {
  // 该类封装一系列日志等级的名字和数字，然后内容封装多个等级的相关枚举
  public final static int INFO_INT = 20000;
  
  
  private static final String INFO_NAME = "INFO";
      
  final static public Level INFO = new Level(INFO_INT, INFO_NAME, 6);
  }
  ```

- ```
  LoggingEvent
  ```

  :对一次日志记录过程中所能取到信息的抽象；

  ```java
  public class LoggingEvent implements java.io.Serializable
  {
  // 该类定义了一堆堆属性，封装了所有的日志信息。
  }
  ```

- ```
  LoggerRepository
  ```

  :Logger实例的容器

  ```java
  public interface LoggerRepository
  {
  // 常见的Hierarchy就是该接口实现，里面封装了框架一堆默认配置，还有Logger工厂。
  // 可以理解该类就是事件源，该类内部封装了以系列的事件
  }
  ```

- ```
  ObjectRender
  ```

  :对日志实例的解析接口，它们主要提供了一种扩展支持。

  ```java
  public interface ObjectRenderer
  {
  
      /**
      * @创建时间： 2016年2月25日
      * @相关参数： @param o
      * @相关参数： @return
      * @功能描述： 解析日志对象，默认实现返回toString()
      */
      public String doRender(Object o);
  }
  ```

Log4J的时序图，如下：

![log4j时序图](images/log4j%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

时序图流程图：获取Logger实例->判断Logger实例对应的日志记录级别是否要比请求的级别低->若是调用forceLog记录日志->创建LoggingEvent实例->将LoggingEvent实例传递给Appender->Appender调用Layout实例格式化日志消息->Appender将格式化后的日志信息写入该Appender对应的日志输出中。

![log4j 详细时序图](images/log4j%E8%AF%A6%E7%BB%86%E6%97%B6%E5%BA%8F%E5%9B%BE.jpg)

