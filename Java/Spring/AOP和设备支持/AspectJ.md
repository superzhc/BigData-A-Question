# AspectJ

## 简介

AspectJ是一个易用的、功能强大的AOP编程语言。

### 官网地址

<http://www.eclipse.org/aspectj/>

### 官网AspectJ的定义

- a seamless aspect-oriented extension to the Java programming language（一种基于Java平台的面向切面编程的语言）
- Java platform compatible（兼容Java平台，可以无缝扩展）
- easy to learn and use（易学易用）

### 官网关于AspectJ的功能

clean modularization of crosscutting concerns, such as error checking and handling, synchronization, context-sensitive behavior, performance optimizations, monitoring and logging, debugging support, and multi-object protocols

大意是说：干净的模块化横切关注点（也就是说单纯，基本上无侵入），如错误检查和处理，同步，上下文敏感的行为，性能优化，监控和记录，调试支持，多目标的协议。

### 其他常用的AOP

- Jboss AOP
- Spring AOP：Spring自己原生的AOP。需要实现大量的接口，继承大量的类，所以spring aop一度被千夫所指。这于它的无侵入，低耦合完全冲突。不过Spring对开源的优秀框架，组建向来是采用兼容，并入的态度。所以，后来的Spring 就提供了Aspectj支持，也就是后来所说的基于纯POJO的AOP。
    - 区别：Spring Aop采用的动态织入，而Aspectj是静态织入。静态织入：指在编译时期就织入，即：编译出来的class文件，字节码就已经被织入了。动态织入又分静动两种，静则指织入过程只在第一次调用时执行；动则指根据代码动态运行的中间状态来决定如何操作，每次调用Target的时候都执行。

## AspectJ和AJDT的安装

### AspectJ安装

到官网下载aspectj，双击下载下来的jar文件，完成aspectj的安装；然后把aspectj安装目录下的lib中的“aspectjrt.jar”复制到jre安装目录下的“lib\ext”目录中。配置环境变量：PATH中添加aspect的安装路径下的bin路径。

至此，已经可以通过使用命令行的方式编写aspectj程序了——aspectj安装目录下的bin中的“ajc”为编译器（对应java的“javac”）。

### Eclipse插件AJDT的安装

选择对应Eclipse版本的AJDT插件下载，下载完成后解压文件，分别将其中的feature和plugins目录下的内容加入到 eclipse的相应同名目录中即可完成ajdt的安装。

## 概念

### 核心关注点

### 横切关注点

横切关注点是跨软件特定部分使用的一种行为，通常也是一种数据。它可能是一种约束，作为软件本身的一种特征，或者只是所有类都必须执行的一种行为。

### 方面

方面（aspect）是横切关注点的另一种称呼。在面向方面中，方面提供了一种机制，利用该机制，可以用一种模块化的方式指定横切关注点。

- 以模块化的方式定义方面
- 动态的应用方面
- 根据一组规则应用方面
- 提供一种机制和一种环境，用于指定将为特定方面执行的代码

面向方面方法提供了一组语义和语法构造来满足这些要求，使得无论编写的是哪一种软件，都可以一般的应用方面。这些构造就是通知、连接点和切入点。

### 通知

## 语法

### aspect的定义

- 定义一个切面使用关键字aspect
- 定义切入点（pointcut）：［修饰符(public,protected...)］　pointcut  poincut名字()　:  表达式;
- 定义通知（advice）：通知类型()：pointcut名字(){// todo something}

一个最基本的aspect，就是这样组成的。aspect支持很多类型的pointcut，最基本的就是method call pointcut（方法级别），而Spring的AOP仅支持方法基本

### pointcut

#### 方法和构造函数（Methods and Constructors）

- call(Signature)：在调用匹配特定签名的方法时；例：调用Log.e(),通过切入点进入通知
    - 在方法调用上触发通知，其环境是调用类
    - Signature可以包含通配符，用于选择不同类和方法上的一系列连接点
- execution(Signature)：方法和构造函数的执行

**注：** method call是调用某个函数的地方，而execution是某个函数执行的内部。

语法结构：execution([修饰符]　返回值类型　方法名(参数)　［异常模式)

**注：** 中括号为可选部分。

例子：【call的使用方式和execution一致】

- execution(public *.*(..))：所有的public方法
- execution(* hello(..))：所有的hello(..)方法
- execution(String hello(..)):所有返回值为String的hello方法
- execution(* hello(String)):所有参数为String类型的hello()
- execution(* hello(String..)):至少有一个参数，且第一个参数类型为String的hello方法
- execution(* com.aspect..*(..)):所有com.aspect包，以及子孙包下的所有方法
- execution(* com..*.*Dao.find*(..)):com包下的所有一Dao结尾的类的一find开头的方法

绑定方法调用上传递的参数值
> 创建一个切入点，用于指定想在其签名中作为标识符捕获的参数。使用call(Signature)和args([TypePatterns|Identifiers])切入点来捕获对方法的调用，然后把需要标识符绑定到方法的参数值上。

#### 字段（Fields）

- get(Signature)：属性的读操作；例：读取DemoActivity.debug成员
- set(Signature)：属性的写操作，传入的值可已作为一个切入点的参数；

#### Advice

adviceexecution()：Advice执行

语法结构：pointcut \<pointcut name\>(): adviceexecution();

adviceexecution()切入点用于捕捉在应用程序内执行任何通知的连接点。使用adviceexecution()切入点在其他通知开始执行的同时应用通知。

#### 异常处理（Exception Handlers）

handler(TypePattern)：匹配TypePattern的异常类型，将进入通知，同时可以将异常值作为切入点的一个参数

handler(TypePatter)切入点具有5个关键特征：

- 在捕获异常的作用域内选择连接点
- 切入点的通知仅用于类型模式指定Throwable获取子类的地方
- TypePattern声明无论何时捕捉到异常或其子类的匹配类型，都会应用相应的通知。
- 切入点只支持before()形式的通知。这意味着不能使用像arround()这样的通知来重写catch块的正常行为
- TypePattern可以包含通配符，用于选择不同类上的一系列连接点

捕获抛出的异常
> 结合使用args([Types | Identifiers])切入点与handler(TypePattern)切入点，将捕捉的异常展示为切入点上的标识符，可将其传递给相应的通知。

#### 初始化（Initialization）

- staticinitialization(TypePattern)：类初始化
- initialization(Signature)：对象初始化
- preinitialization(Signature)：对象预先初始化

initialization(Signature)切入点的语法格式：pointcut \<pointcut name\>(\<any values to be picked up\>): initialization(\<optional modifier\> \<class\>.new (\<parameter types\>));

initialization(Signature)具有5个关键的特性：

1. 切入点必须包含new关键字
2. 切入点捕捉连接点发生在任何超类的初始化之后，以及从构造函数方法返回之前。
3. Signature必须解析成特定类的构造函数，而不是一个简单的方法。
4. 切入点提供了编译时检查，用于检查构造函数是否正在被引用
5. 由于AspectJ编译器中的编译器限制，当与around()通知关联时，不能使用initialization(Signature)切入点。

#### Lexical

- within(TypePattern)：捕获在指定类或者方面中的程序体中的所有连接点，包括内部类
- withincode(Signature)：用于捕获在构造器或者方法中的所有连接点，包括在其中的本地类

#### Instanceof checks and context exposure

- this(Type or Id)：所有Type or id 的实例的执行点，匹配所有的连接点，如方法调用，属性设置，当前的执行对象为Account，或者其子类。
- target(Type or Id)：配所有的连接点，目标对象为Type或Id
- args(Type or Id, ...)：参数类型为Type

#### Control Flow

- cflow(Pointcut)：捕获所有的连接点在指定的方法执行中，包括执行方法本身
- cflowbelow(Pointcut)：捕获所有的连接点在指定的方法执行中，除了执行方法本身

#### Conditional

- if(Expression)：逻辑／结合操作
- (! Pointcut)：非操作
- (Pointcut0 && Pointcut1)：并操作
- (Pointcut0 || Pointcut1)：或操作

pointcut基于正则的语法，那么肯定也支持通配符，含义如下：

- * 表示任何数量的字符，除了(.)
- .. 表示任何数量的字符包括任何数量的(.)
- + 描述指定类型的任何子类或者子接口

### 通知（advice）

AspectJ有五种类型的advice：

- before( Formals )
- after( Formals ) returning [ ( Formal ) ]
- after( Formals ) throwing [ ( Formal ) ]
- after( Formals )
- Type around( Formals )

## 编译（织入）

### 编译一个方面和多个Java文件

创建一个名为\<appname\>.lst的Aspect的构建配置文件，其中包含要包括在编译中的所有类的文件和方面名称，使用命令指示ajc编译器把这些方面应用类。`ajc -argfile file.lst -classpath %My_ClassPath% -d %My_Destination_Directory%`

**注：** ajc编译器不会搜索要编译的文件的源或类路径，必须告诉它编译中涉及哪些文件；这意味着将与方面一起编译的所有源都必须直接输入到ajc编译器中。

有三种方式可用于把待编译的文件提供给ajc编译器（其中两种在语义上等价的）：

- argfiles 选项
    - 可以在ajc命令上利用这个选项指定文件名，从而在一个.lst文件中提供所有的文件
- \@ 选项
    - 这个选项等价argfiles选项
- 直接列出文件

### 织入方面到jar中

在运行ajc命令时，使用- inpath命令选项。

## 例子

```java
// HelloWorld.java
public class HelloWorld {
    public static void main (String[] args) {
        System.out.println("Hello World!");
    }
}
```

```aj
// Tracing.aj
public aspect Tracing {

    private pointcut mainMethod () :
        execution(public static void main(String[]));

    before () : mainMethod() {
        System.out.println("> " + thisJoinPoint);
    }

    after () : mainMethod() {
        System.out.println("< " + thisJoinPoint);
    }
}
```

- 执行命令 javac HelloWorld.java 将 HelloWorld.java 编译为 HelloWorld.class
- 执行命令 ajc -outjar Tracing.jar -outxml Tracing.aj 将 Tracing.aj 编译为 Tracing.jar
- 使用命令 java HelloWorld 运行 HelloWorld 类，输出 Hello World!。
- 使用命令 aj5 -classpath "Tracing.jar;%CLASSPATH%" HelloWorld 运行 HelloWorld 类，结果为
    ```
    > execution(void HelloWorld.main(String[]))
    Hello World!
    < execution(void HelloWorld.main(String[]))
    ```

## AspectJ的Maven插件

使用 aspectj-maven-plugin 编绎打包 使用aspectJ 的maven项目

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.4</version>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
    </configuration>
</plugin>
```