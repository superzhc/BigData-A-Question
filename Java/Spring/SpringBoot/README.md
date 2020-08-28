---
title: Spring Boot学习笔记
date: 2017-11-25 00:50:40
tags: spring
---
# Spring Boot基础

## 介绍

SpringBoot 是 Spring 框架对“**约定优先于配置**(**Convention Over Configuration**)”理念的最佳实践产物，一个典型的SpringBoot应用本质上就是一个基于Spring框架的应用。

首先Spring Boot不是一个框架，它是一种用来轻松创建具有最小或零配置的独立应用程序的方式。这是方法用来开发基于Spring的应用，但只需非常少的配置。它提供了默认的代码和注释配置，快速启动新的Spring项目而不需要太多时间。它利用现有的Spring项目以及第三方项目来开发生产就绪(投入生产)的应用程序。它提供了一组Starter Pom或gradle构建文件，可以使用它们添加所需的依赖项，并且还便于自动配置。

Spring Boot根据其类路径上的库自动配置所需的类。假设应用程序想要与数据库交互，如果在类路径上有Spring数据库，那么它会自动建立与数据源类的连接。

Spring Boot的主要动机是简化配置和部署spring应用程序的过程。

Spring Boot为开发提供一个具有最小功能的Spring应用程序，并提供了一个新的范例。使用Spring Boot将能够以更灵活的方式开发Spring应用程序，并且能够通过最小(或可能没有)配置Spring来专注于解决应用程序的功能需求。它使用全新的开发模型，通过避免一些繁琐的开发步骤和样板代码和配置，使Java开发非常容易。

Spring Boot可以轻松创建单独的，生产级的基于Spring的应用程序，我们只管“运行”。查看Spring平台和第三方库。大多数Spring Boot应用程序只需要很少的Spring配置。

### Spring Boot的主要特点

- 创建独立的Spring应用程序
- 直接嵌入Tomcat，Jetty或Undertow（无需部署WAR文件）
- 提供“初始”的POM文件内容，以简化Maven配置
- 尽可能时自动配置Spring
- 提供生产就绪的功能，如指标，健康检查和外部化配置
- 绝对无代码生成，也不需要XML配置

### Spring Boot核心

- Spring Boot不是编写应用程序的框架，它可以帮助我们以最少的配置或零配置开发和构建，打包和部署应用程序。
- 它不是应用程序服务器。但是它是提供应用程序服务器功能的嵌入式servlet容器，而不是Spring Boot本身。
- 类似地，Spring Boot不实现任何企业Java规范，例如JPA或JMS。 例如，Spring Boot不实现JPA，但它通过为JPA实现(例如Hibernate)自动配置适当的bean来支持JPA。
- 最后，Spring Boot不使用任何形式的代码生成来完成它的功能。它是利用Spring 4的条件配置功能，以及Maven和Gradle提供的传递依赖关系解析，以在Spring应用程序上下文中自动配置bean。

简而言之，Spring Boot它的核心就是Spring。

### Spring Boot特性

主要是一下四个方面：

- 自动配置：针对很多Spring应用程序常见的应用功能，Spring Boot能自动提供相关配置。
- 起步依赖：告诉Spring Boot需要什么功能，它就能引入需要的库。
- 命令行界面：这是Spring Boot的可选特性，借此你只需写代码就能完成完整的应用程序，无需传统项目构建。
- Actuator：让你能够深入运行中的Spring Boot应用程序

每一个特性都在通过自己的方式简化Spring应用程序开发。

### Spring Boot优点

- 使用Java或Groovy开发基于Spring的应用程序非常容易。
- 它减少了大量的开发时间并提高了生产力。
- 它避免了编写大量的样板代码，注释和XML配置。
- Spring Boot应用程序与其Spring生态系统(如Spring JDBC，Spring ORM，Spring Data，Spring Security等)集成非常容易。
- 它遵循“自用默认配置”方法，以减少开发工作量。
- 它提供嵌入式HTTP服务器，如Tomcat，Jetty等，以开发和测试Web应用程序非常容易。
- 它提供CLI(命令行界面)工具从命令提示符，非常容易和快速地开发和测试Spring Boot(Java或Groovy)应用程序。
- 它提供了许多插件来开发和测试Spring启动应用程序非常容易使用构建工具，如Maven和Gradle。
- 它提供了许多插件，以便与嵌入式和内存数据库工作非常容易。

### Spring Boot的限制

将现有或传统的Spring Framework项目转换为Spring Boot应用程序是一个非常困难和耗时的过程。它仅适用于全新Spring项目。

## 入门

Spring Boot项目归根只是一个常规的Spring项目，只是利用了Spring Boot启动程序和自动配置。要创建Spring Boot Java应用程序的方法，有两种种方法：

- 使用Spring STS IDE
- 使用Spring Initializr

### 纯手动安装

**注：** 此处仅使用maven安装，还存在其他安装方式

Spring引导依赖项groupId使用org.springframework.boot。 通常Maven POM文件将继承自spring-boot-starter-parent项目，并将依赖性声明为一个或多个“Starters”

一个典型的pom.xml文件内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>myproject</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <!-- Inherit defaults from Spring Boot -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.4.3.RELEASE</version>
    </parent>

    <!-- Add typical dependencies for a web application -->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <!-- Package as an executable jar -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

spring-boot-starter-parent是使用Spring Boot的一个很好的方法，但它可能并不适合所有的时候。 有时可能需要从不同的父POM继承，或者可能只是不喜欢默认设置。

### Spring Initializr 安装

进入<http://start.spring.io>网站，填写相关项目信息，即可下载到一个项目压缩包，解压导入到ide中

## 应用程序开发入门

### POM文件

首先，创建Maven pom.xml文件，pom.xml是将用于构建项目的配置，将它放到项目文件夹下，初始化的pom.xml文件内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>myproject</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.4.3.RELEASE</version>
    </parent>

    <!-- Additional lines to be added here... -->

</project>
```

pom.xml给出了一个工作构建，可以通过运行mvn package(可以忽略“jar will be empty - no content was marked for inclusion!”现在警告)测试它。此时，可以将项目导入到IDE中。

### 添加类路径依赖项

Spring Boot提供了一些“启动器”，使得容易添加 jar 到你的类路径。spring-boot-starter-parent是一个特殊的启动器，提供了有用的 Maven 默认值。 它还提供了一个依赖关系管理部分，以便您可以省略“blessed”依赖关系的 version 标签。

其他“Starters”只是提供了在开发特定类型的应用程序时可能需要的依赖关系。

web应用程序创建，将添加一个spring-boot-starter-web依赖关系。

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

可见，Spring Boot 极大地简化了 Spring 应用从搭建到开发的过程，做到了「开箱即用」的方式。Spring Boot 已经提供很多「开箱即用」的依赖，如上面开发 web 应用使用的 spring-boot-starter-web ，都是以 spring-boot-starter-xx 进行命名的。

Spring Boot 「开箱即用」 的设计，对开发者非常便利。简单来说，只要往 Spring Boot 项目加入相应的 spring-boot-starter-xx 依赖，就可以使用对应依赖的功能，比如加入 spring-boot-starter-data-jpa 依赖，就可以使用数据持久层框架 Spring Data JPA 操作数据源。相比 Spring 以前需要大量的XML配置以及复杂的依赖管理，极大的减少了开发工作量和学习成本。

**注：** 其他的Artifact ID都有 spring-boot-starter- 前缀。这些都是Spring Boot起步依赖，它们都有助于Spring Boot应用程序的构建。

### 编写代码

代码示例：

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// 这个注解告诉Spring Boot“猜测”将如何配置Spring，它是基于添加的jar依赖。
// 由于spring-boot-starter-web添加了Tomcat和Spring MVC，因此自动配置将假设正在开发一个Web应用程序并相应地设置Spring。
@EnableAutoConfiguration
public class Example
{

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    // 这只是一个遵循Java约定的应用程序入口点的标准方法。
    // main方法通过调用run来委托Spring Boot SpringApplication类。
    // SpringApplication将引导应用程序，启动Spring，从而启动自动配置Tomcat Web服务器。
    // 需要传递Example.class作为run方法的参数来告诉SpringApplication，这是主要的Spring组件。args数组也被传递以暴露任何命令行参数。
    public static void main(String[] args) throws Exception {
        //SpringApplication的静态run方法，首先会创建一个SpringApplication对象实例，然后调用这个创建好的SpringApplication的实例run方法。
        SpringApplication.run(Example.class, args);
    }

    // 运行：
    // 由于 POM 中使用了spring-boot-starter-parent，有一个有用的 run 目标，用它来启动应用程序。输入 mvn spring-boot:run 从根项目目录启动应用程序
}
```

### 创建可执行jar

创建一个完全自包含的可执行jar文件，可以在生产中运行。可执行jar(有时称为“fat jar”)是包含编译的类以及需要运行的所有jar依赖性的代码存档。

>可执行jar和Java
>Java不提供任何标准方法来加载嵌套的jar文件(即包含在jar中的jar文件)。如果想要分发一个自包含的应用程序，这可能是有问题的。
>
>为了解决这个问题，许多开发人员使用“uber” jar。 一个uber jar简单地将所有类，从所有jar到一个单一的归档。这种方法的问题是，很难看到实际上在应用程序中使用哪些库。如果在多个jar中使用相同的文件名(但是具有不同的内容)，它也可能是有问题的。

Spring Boot采用不同的方法，并允许直接嵌套JAR。

要创建可执行的jar，需要将spring-boot-maven-plugin添加到pom.xml文件中。在dependencies部分下面插入以下行：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

保存pom.xml文件并从命令行运行命令 mvn package,在 target 目录中，应该看到一个myproject-0.0.1-SNAPSHOT.jar文件。文件大小应为10 Mb左右。如果想看里面内容，可以使用jar tvf:`jar tvf target/myproject-0.0.1-SNAPSHOT.jar`。

应该在target目录中看到一个名为myproject-0.0.1-SNAPSHOT.jar.original的文件。这是Maven在Spring Boot重新打包之前创建的原始jar文件。

要运行该应用程序，请使用java -jar命令：`java -jar target/myproject-0.0.1-SNAPSHOT.jar`。

## 配置文件

Spring Boot使用了一个全局的配置文件application.properties或application.yml，放在src/main/resources目录下或者类路径的/config下。Sping Boot的全局配置文件的作用是对一些默认配置的配置值进行修改。

### 自定义属性

```java
//my:
//  name: superz

//读取配置文件的值，只需要加@Value("${属性名}")
@Value("${my.name}")
private String name;
```

### 将配置文件的属性赋给实体类

```java
//my:
//    name: superz
//    age: 24
//    sex: 男

//需要加上注解@ConfigurationProperties,并加上它的prefix，@Component可加可不加
@ConfigurationProperties(prefix = "my")
@Component
public class ConfigBean {
    private String name;
    private int age;
    private String sex;

    //省略getter setter...
}
```

最后，需要在应用类或者Application类，加EnableConfigurationProperties注解，`@EnableConfigurationProperties({ConfigBean.class})`；且注入应用程序类，`@Autowired ConfigBean configBean;`

### 自定义配置文件

自定义配置文件test.properties:

```java
//test.properties配置文件如下：
//com.superz.name=superz
//com.superz.age=24

@Configuration
@PropertySource(value = "classpath:test.properties")
@ConfigurationProperties(prefix = "com.suprez")
public class User {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```

调用同上，在应用程序中加上EnableConfigurationProperties注解，并将类注入到类中。

### 多个环境配置文件

配置不同的环境，配置文件的格式为application-{profile}.properties，其中{profile}对应的是环境标识，比如：

- application-test.properties：测试环境
- application-dev.properties：开发环境
- application-prod.properties：生产环境

使用的话，直接在application.yml中加：

```yml
 spring:
  profiles:
    active: dev #环境标识
```

### 改变JDK编译版本

```xml
<properties>
    <java.version>1.8</java.version>
</properties>
```

### 修改端口号

SpringBoot默认端口是8080，如果想要修改，直接修改配置文件即可，在配置文件中加入：

```yml
server:
    port: 9000
    #常用配置
    #address= # bind to a specific NIC
    #session-timeout: # session timeout in seconds
    #the context path, defaults to '/'
    #context-path: /spring-boot
    #servlet-path:  # the servlet path, defaults to '/'
    #tomcat:
        #access-log-pattern: # log pattern of the access log
        #access-log-enabled:false # is access logging enabled
        #protocol-header:x-forwarded-proto # ssl forward headers
        #remote-ip-header:x-forwarded-for
        #basedir:/tmp # base dir (usually not needed, defaults totmp)
        #background-processor-delay:30; # in seconds
        #max-threads : 0 # number of threads in protocol handler
        #uri-encoding : UTF-8 # character encoding to use for URLdecoding
```

### 配置ContextPath

Spring boot默认是\/，这样直接通过http:\/\/ip:port\/就可以访问到index页面，如果要修改为http:\/\/ip:port\/path\/访问的话，那么需要在Application.properties文件中加入server.context-path= \/你的path,比如：spring-boot,那么访问地址就是http:\/\/ip:port\/spring\-boot路径。

`server: context-path: /spring-boot`

## 处理静态资源

Spring Boot 默认为我们提供了静态资源处理，使用WebMvcAutoConfiguration中的配置各种属性。建议直接使用SpringBoot的默认配置方式，如果需要特殊处理的再通过配置进行修改。如果想要自己完全控制WebMVC，就需要在@Configuration注解的配置类上增加@EnableWebMvc（@SpringBootApplication注解的程序入口类已经包含@Configuration），增加该注解以后WebMvcAutoConfiguration中配置就不会生效，你需要自己来配置需要的每一项。这种情况下的配置还是要多看一下WebMvcAutoConfiguration类。

资源文件的约定目录结构

- Maven的资源文件目录：/src/java/resources
- spring-boot项目静态文件目录：/src/java/resources/static
- spring-boot项目模板文件目录：/src/java/resources/templates
- spring-boot静态首页的支持，即index.html放在以下目录结构会直接映射到应用的根目录下：

```java
classpath:/META-INF/resources/index.html
classpath:/resources/index.html
classpath:/static/index.html
calsspath:/public/index.html
```

在spring-boot下，默认约定了Controller试图跳转中thymeleaf模板文件的的前缀prefix是”classpath:/templates/”,后缀suffix是”.html”

在application.properties配置文件中是可以修改的，配置的前缀和后缀：

```yml
spring:
    thymeleaf:
        prefix: /templates/
        suffix: .html
```

### 默认资源映射

默认配置的 /** 映射到 /static （或/public、/resources、/META-INF/resources）

**注：** static、public、resources等目录都在 classpath，优先级顺序为：META/resources > resources > static >public

### 自定义默认资源

#### 自定义目录

#### 使用外部目录

#### 通过配置文件配置

```yml
spring:
    mvc:
        static-path-pattern: #默认值为classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/;spring.resources.static-locations=这里设置要指向的路径，多个使用英文逗号隔开，使用spring.mvc.static-path-pattern可以重新定义pattern;使用 spring.resources.static-locations可以重新定义 pattern所指向的路径，支持 classpath:和 file:
```

**注意**  spring.mvc.static-path-pattern只可以定义一个，目前不支持多个逗号分割的方式。

## SpringApplication执行流程

 1. 如果我们使用的是SpringApplication的静态run方法，那么，这个方法里面首先要创建一个SpringApplication对象实例，然后调用这个创建好的SpringApplication的实例方法。在SpringApplication实例初始化的时候，它会提前做几件事情：
    - 根据classpath里面是否存在某个特征类（org.springframework.web.context.ConfigurableWebApplicationContext）来决定是否应该创建一个为Web应用使用的ApplicationContext类型。
    - 使用SpringFactoriesLoader在应用的classpath中查找并加载所有可用的ApplicationContextInitializer。
    - 使用SpringFactoriesLoader在应用的classpath中查找并加载所有可用的ApplicationListener。
    - 推断并设置main方法的定义类。
2. SpringApplication实例初始化完成并且完成设置后，就开始执行run方法的逻辑了，方法执行伊始，首先遍历执行所有通过SpringFactoriesLoader可以查找到并加载的SpringApplicationRunListener。调用它们的started()方法，告诉这些SpringApplicationRunListener，“嘿，SpringBoot应用要开始执行咯！”。
3. 创建并配置当前Spring Boot应用将要使用的Environment（包括配置要使用的PropertySource以及Profile）。
4. 遍历调用所有SpringApplicationRunListener的environmentPrepared()的方法，告诉他们：“当前SpringBoot应用使用的Environment准备好了咯！”。
5. 如果SpringApplication的showBanner属性被设置为true，则打印banner。
6. 根据用户是否明确设置了applicationContextClass类型以及初始化阶段的推断结果，决定该为当前SpringBoot应用创建什么类型的ApplicationContext并创建完成，然后根据条件决定是否添加ShutdownHook，决定是否使用自定义的BeanNameGenerator，决定是否使用自定义的ResourceLoader，当然，最重要的，将之前准备好的Environment设置给创建好的ApplicationContext使用。
7. ApplicationContext创建好之后，SpringApplication会再次借助Spring-FactoriesLoader，查找并加载classpath中所有可用的ApplicationContext-Initializer，然后遍历调用这些ApplicationContextInitializer的initialize（applicationContext）方法来对已经创建好的ApplicationContext进行进一步的处理。
8. 遍历调用所有SpringApplicationRunListener的contextPrepared()方法。
9. 最核心的一步，将之前通过@EnableAutoConfiguration获取的所有配置以及其他形式的IoC容器配置加载到已经准备完毕的ApplicationContext。
10. 遍历调用所有SpringApplicationRunListener的contextLoaded()方法。
11. 调用ApplicationContext的refresh()方法，完成IoC容器可用的最后一道工序。
12. 查找当前ApplicationContext中是否注册有CommandLineRunner，如果有，则遍历执行它们。
13. 正常情况下，遍历执行SpringApplicationRunListener的finished()方法、（如果整个过程出现异常，则依然调用所有SpringApplicationRunListener的finished()方法，只不过这种情况下会将异常信息一并传入处理）

去除事件通知点后，整个流程如下：
![](../images/springboot.jpg)

## 定制与优化内置的Tomcat容器

### 通过配置文件

配置的核心内容参考org.springframework.boot.autoconfigure.web.ServerProperties这个服务属性类，下面展示部分对tomcat的配置：

```yml
server:
  port: 8081
  # tomcat设置
  tomcat:
    accesslog:
    # 开启日志访问
      enabled: true
    # 日志保存路径
      directory: e:/tmp/logs
```

### 实现EmbeddedServletContainerCustomizer接口

```java
@Component
public class MyEmbeddedServletContainerCustomizer implements EmbeddedServletContainerCustomizer {
    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        //org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory 
        //说明默认是的Tomcat容器
        System.out.println(container.getClass());
        TomcatEmbeddedServletContainerFactory factory = (TomcatEmbeddedServletContainerFactory) container;
        //设置端口
        factory.setPort(8088);
        //设置Tomcat的根目录
        factory.setBaseDirectory(new File("d:/tmp/tomcat"));
        //设置访问日志存放目录
        factory.addContextValves(getLogAccessLogValue());
        //设置Tomcat线程数和连接数
        factory.addConnectorCustomizers(new MyTomcatConnectorCustomizer());
        //初始化servletContext对象
        factory.addInitializers((servletContext) -> {
            System.out.println(" = = = = 获取服务器信息 = = " + servletContext.getServerInfo());
        });

    }
    private AccessLogValve getLogAccessLogValue() {
        AccessLogValve accessLogValve = new AccessLogValve();
        accessLogValve.setDirectory("d:/tmp/tomcat/logs");
        accessLogValve.setEnabled(true);
        accessLogValve.setPattern(Constants.AccessLog.COMMON_PATTERN);
        accessLogValve.setPrefix("springboot-access-log");
        accessLogValve.setSuffix(".txt");
        return accessLogValve;
    }
}

/**
 * 定制tomcat的连接数与线程数
 */
class MyTomcatConnectorCustomizer implements TomcatConnectorCustomizer {
    @Override
    public void customize(Connector connector) {
        //连接协议 HTTP/1.1
        System.out.println(connector.getProtocol());
        //连接协议处理器 org.apache.coyote.http11.Http11NioProtocol
        System.out.println(connector.getProtocolHandler().getClass());
        //Http11NioProtocol
        Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
        // 设置最大连接数
        protocolHandler.setMaxConnections(2000);
        // 设置最大线程数
        protocolHandler.setMaxThreads(500);
    }
}
```

### 在Spring容器中配置EmbeddedServletContainerFactory实现类

```java
@SpringBootConfiguration
public class WebServerConfiguration {
    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        //设置端口
        factory.setPort(8089);
        //设置404错误界面
        factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/404.html"));
        //设置在容器初始化的时候触发
        factory.addInitializers((servletContext) -> {
            System.out.println(" = = = = 获取服务器信息 = = " + servletContext.getServerInfo());
        });
        //设置最大连接数和最大线程数
        factory.addConnectorCustomizers((connector) -> {
            Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
            protocolHandler.setMaxConnections(2000);
            protocolHandler.setMaxThreads(500);
        });
        //设置访问日志记录文件的目录
        factory.addContextValves(getLogAccessLogValue());
        return factory;
    }

    private AccessLogValve getLogAccessLogValue() {
        AccessLogValve accessLogValve = new AccessLogValve();
        accessLogValve.setDirectory("d:/tmp/logs");
        accessLogValve.setEnabled(true);
        accessLogValve.setPattern(Constants.AccessLog.COMMON_PATTERN);
        accessLogValve.setPrefix("SpringBoot-Access-Log");
        accessLogValve.setSuffix(".txt");
        return accessLogValve;
    }
}
```

## Web应用中统一异常处理

Spring Boot提供了一个默认的映射：/error，当处理中抛出异常之后，会转到该请求中处理，并且该请求有一个全局的错误页面用来展示异常内容。

创建自定义的统一异常处理：

- 创建全局异常处理类：通过使用@ControllerAdvice定义统一的异常处理类，而不是在每个Controller中逐个定义。@ExceptionHandler用来定义函数针对的异常类型，最后将Exception对象和请求URL映射到error.html中
    ```java
    @ControllerAdvice
    class GlobalExceptionHandler {

        public static final String DEFAULT_ERROR_VIEW = "error";

        @ExceptionHandler(value = Exception.class)
        public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
            ModelAndView mav = new ModelAndView();
            mav.addObject("exception", e);
            mav.addObject("url", req.getRequestURL());
            mav.setViewName(DEFAULT_ERROR_VIEW);
            return mav;
        }
    }
    ```
- 实现error.html页面展示：在templates目录下创建error.html，将请求的URL和Exception对象的message输出。
    ```html
    <!DOCTYPE html>
    <html>
    <head lang="en">
        <meta charset="UTF-8" />
        <title>统一异常处理</title>
    </head>
    <body>
        <h1>Error Handler</h1>
        <div th:text="${url}"></div>
        <div th:text="${exception.message}"></div>
    </body>
    </html>
    ```

通过实现上述内容之后，只需要在Controller中抛出Exception，当然我们可能会有多种不同的Exception。然后在@ControllerAdvice类中，根据抛出的具体Exception类型匹配@ExceptionHandler中配置的异常类型来匹配错误映射和处理。

### 返回JSON格式

通过@ControllerAdvice统一定义不同Exception映射到不同错误处理页面。而当要实现RESTful API时，返回的错误是JSON格式的数据，而不是HTML页面，只需在@ExceptionHandler之后加入@ResponseBody，就能让处理函数return的内容转换为JSON格式。

## 使用Fastjson解析Json数据

### 引入fastjson依赖库：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.15</version>
</dependency>
```

### 配置Fastjson

支持两种方法：

第一种方法：

1. 启动类继承extends WebMvcConfigurerAdapter
2. 覆盖方法configureMessageConverters

```java
@SpringBootApplication
public class ApiCoreApp  extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(fastConverter);
    }
}
```

第二种方法：

在App.java启动类中，注入Bean : HttpMessageConverters。

```java
@SpringBootApplication
public class ApiCoreApp {
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
       FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
       FastJsonConfig fastJsonConfig = new FastJsonConfig();
       fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
       fastConverter.setFastJsonConfig(fastJsonConfig);
       HttpMessageConverter<?> converter = fastConverter;
       return new HttpMessageConverters(converter);
    }

    public static void main(String[] args) {
       SpringApplication.run(ApiCoreApp.class, args);
    }
}
```

## 使用@Scheduled创建定时任务

创建定时任务：

- 在Spring Boot的主类中加入@EnableScheduling注解，启用定时任务的配置
    ```java
    @SpringBootApplication
    @EnableScheduling
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    ```
- 创建定时任务实现类
    ```java
    @Component
    public class ScheduledTasks {

        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        //5秒打印一次当前时间
        @Scheduled(fixedRate = 5000)
        public void reportCurrentTime() {
            System.out.println("现在时间：" + dateFormat.format(new Date()));
        }
    }
    ```

### @Scheduled详解

- @Scheduled(fixedRate = 5000) ：上一次开始执行时间点之后5秒再执行
- @Scheduled(fixedDelay = 5000) ：上一次执行完毕时间点之后5秒再执行
- @Scheduled(initialDelay=1000, fixedRate=5000) ：第一次延迟1秒后执行，之后按fixedRate的规则每5秒执行一次
- @Scheduled(cron="\*/5 \* \* \* \* \*") ：通过cron表达式定义规则

## 使用log4j记录日志

### 引入log4j依赖

在创建Spring Boot工程时，我们引入了spring-boot-starter，其中包含了spring-boot-starter-logging，该依赖内容就是Spring Boot默认的日志框架Logback，所以我们在引入log4j之前，需要先排除该包的依赖，再引入log4j的依赖。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <exclusions>
        <exclusion> 
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j</artifactId>
</dependency>
```

### 配置log4j.properties

在引入了log4j依赖之后，只需要在src/main/resources目录下加入log4j.properties配置文件，就可以开始对应用的日志进行配置使用。

#### 控制台输出

通过如下配置，设定root日志的输出级别为INFO，appender为控制台输出stdout。

```properties
# LOG4J配置
log4j.rootCategory=INFO, stdout

# 控制台输出
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %5p %c{1}:%L - %m%n
```

#### 输出到文件

在开发环境，我们只是输出到控制台没有问题，但是到了生产或测试环境，或许持久化日志内容，方便追溯问题原因。可以通过添加如下的appender内容，按天输出到不同的文件中去，同时还需要为log4j.rootCategory添加名为file的appender，这样root日志就可以输出到logs/all.log文件中了。

```properties
log4j.rootCategory=INFO, stdout, file

# root日志输出
log4j.appender.file=org.apache.log4j.DailyRollingFileAppender
log4j.appender.file.file=logs/all.log
log4j.appender.file.DatePattern='.'yyyy-MM-dd
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %5p %c{1}:%L - %m%n
```

#### 分类输出

##### 按不同package进行输出

通过定义输出到logs/my.log的appender，并对com.didispace包下的日志级别设定为DEBUG级别、appender设置为输出到logs/my.log的名为didifile的appender。

```properties
# com.didispace包下的日志配置
log4j.category.com.didispace=DEBUG, didifile

# com.didispace下的日志输出
log4j.appender.didifile=org.apache.log4j.DailyRollingFileAppender
log4j.appender.didifile.file=logs/my.log
log4j.appender.didifile.DatePattern='.'yyyy-MM-dd
log4j.appender.didifile.layout=org.apache.log4j.PatternLayout
log4j.appender.didifile.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %5p %c{1}:%L ---- %m%n
```

##### 不同级别进行分类

比如对ERROR级别输出到特定的日志文件中，具体配置可以如下。

```properties
log4j.logger.error=errorfile
# error日志输出
log4j.appender.errorfile=org.apache.log4j.DailyRollingFileAppender
log4j.appender.errorfile.file=logs/error.log
log4j.appender.errorfile.DatePattern='.'yyyy-MM-dd
log4j.appender.errorfile.Threshold = ERROR
log4j.appender.errorfile.layout=org.apache.log4j.PatternLayout
log4j.appender.errorfile.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %5p %c{1}:%L - %m%n
```

## 使用AOP统一处理Web请求日志

AOP为Aspect Oriented Programming的缩写，意为：面向切面编程，通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。AOP是Spring框架中的一个重要内容，它通过对既有程序定义一个切入点，然后在其前后切入不同的执行内容，比如常见的有：打开数据库连接/关闭数据库连接、打开事务/关闭事务、记录日志等。基于AOP不会破坏原来程序逻辑，因此它可以很好的对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率。

### 引入AOP依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

在完成了引入AOP依赖包后，一般来说并不需要去做其他配置。也许在Spring中使用过注解配置方式的人会问是否需要在程序主类中增加@EnableAspectJAutoProxy来启用，实际并不需要。

### 实现Web层的日志切面

- 使用@Aspect注解将一个java类定义为切面类
- 使用@Pointcut定义一个切入点，可以是一个规则表达式，比如下例中某个package下的所有函数，也可以是一个注解等。
- 根据需要在切入点不同位置的切入内容
    - 使用@Before在切入点开始处切入内容
    - 使用@After在切入点结尾处切入内容
    - 使用@AfterReturning在切入点return内容之后切入内容（可以用来对处理返回值做一些加工处理）
    - 使用@Around在切入点前后切入内容，并自己控制何时执行切入点自身的内容
    - 使用@AfterThrowing用来处理当切入内容部分抛出异常之后的处理逻辑

```java
@Aspect
@Component
public class WebLogAspect {

    private Logger logger = Logger.getLogger(getClass());

    @Pointcut("execution(public * com.didispace.web..*.*(..))")
    public void webLog(){}

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
        logger.info("URL : " + request.getRequestURL().toString());
        logger.info("HTTP_METHOD : " + request.getMethod());
        logger.info("IP : " + request.getRemoteAddr());
        logger.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        logger.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));

    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        logger.info("RESPONSE : " + ret);
    }
}
```

## SpringBoot和Mybatis

### 添加POM依赖

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

MyBatis-Spring-Boot-Starter依赖将会提供如下：

- 自动检测现有的DataSource
- 将创建并注册SqlSessionFactory的实例，该实例使用SqlSessionFactoryBean将该DataSource作为输入进行传递
- 将创建并注册从SqlSessionFactory中获取的SqlSessionTemplate的实例。
- 自动扫描您的mappers，将它们链接到SqlSessionTemplate并将其注册到Spring上下文，以便将它们注入到您的bean中。

就是说，使用了该Starter之后，只需要定义一个DataSource即可（application.properties中可配置），它会自动创建使用该DataSource的SqlSessionFactoryBean以及SqlSessionTemplate。会自动扫描你的Mappers，连接到SqlSessionTemplate，并注册到Spring上下文中。

### 数据源配置

```properties
spring.datasource.url = jdbc:mysql://localhost:3306/spring?useUnicode=true&characterEncoding=utf-8
spring.datasource.username = root
spring.datasource.password = root
spring.datasource.driver-class-name = com.mysql.jdbc.Driver
```

### 自定义数据源配置

Spring Boot默认使用tomcat-jdbc数据源，如果你想使用其他的数据源，比如这里使用了阿里巴巴的数据池管理，添加druid的依赖，配置application.properties或application.yml文件。

此处显示为在启动类中配置:

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private Environment env;

    //destroy-method="close"的作用是当数据库连接不使用的时候,就把该连接重新放到数据池中,方便下次使用调用.
    @Bean(destroyMethod =  "close")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));//用户名
        dataSource.setPassword(env.getProperty("spring.datasource.password"));//密码
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setInitialSize(2);//初始化时建立物理连接的个数
        dataSource.setMaxActive(20);//最大连接池数量
        dataSource.setMinIdle(0);//最小连接池数量
        dataSource.setMaxWait(60000);//获取连接时最大等待时间，单位毫秒。
        dataSource.setValidationQuery("SELECT 1");//用来检测连接是否有效的sql
        dataSource.setTestOnBorrow(false);//申请连接时执行validationQuery检测连接是否有效
        dataSource.setTestWhileIdle(true);//建议配置为true，不影响性能，并且保证安全性。
        dataSource.setPoolPreparedStatements(false);//是否缓存preparedStatement，也就是PSCache
        return dataSource;
    }
}
```

### 创建接口Mapper和对应的Mapper.xml文件

### 创建实体

### 在程序启动类处添加@MapperScan注解，扫描Mapper接口，使Spring来控制管理

也可在接口处添加@Mapper注解，等同在程序启动类处添加@MapperScan注解，扫描Mapper接口

### 修改application.yml 配置文件

```yml
mybatis:
  mapperLocations: classpath:mapper/*.xml
  typeAliasesPackage: com.epoint.superz.domain
```

### 在Controller或Service调用方法测试

## 使用Druid和监控配置

Spring Boot默认的数据源是：org.apache.tomcat.jdbc.pool.DataSource。

### 添加maven依赖

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.0.18</version>
</dependency>
```

### 配置数据源相关信息

```properties
# 数据库访问配置
# 主数据源，默认的
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/test
spring.datasource.username=root
spring.datasource.password=123456

# 下面为连接池的补充设置，应用到上面所有数据源中
# 初始化大小，最小，最大
spring.datasource.initialSize=5
spring.datasource.minIdle=5
spring.datasource.maxActive=20
# 配置获取连接等待超时的时间
spring.datasource.maxWait=60000
# 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
spring.datasource.timeBetweenEvictionRunsMillis=60000
# 配置一个连接在池中最小生存的时间，单位是毫秒
spring.datasource.minEvictableIdleTimeMillis=300000
spring.datasource.validationQuery=SELECT 1 FROMDUAL
spring.datasource.testWhileIdle=true
spring.datasource.testOnBorrow=false
spring.datasource.testOnReturn=false
# 打开PSCache，并且指定每个连接上PSCache的大小
spring.datasource.poolPreparedStatements=true
spring.datasource.maxPoolPreparedStatementPerConnectionSize=20
# 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
spring.datasource.filters=stat,wall,log4j
# 通过connectProperties属性来打开mergeSql功能；慢SQL记录
spring.datasource.connectionProperties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
# 合并多个DruidDataSource的监控数据
#spring.datasource.useGlobalDataSourceStat=true
```

### 配置监控统计功能

#### 配置方式一

```java
//配置Servlet
@WebServlet(urlPatterns="/druid/*",
            initParams={
                @WebInitParam(name="allow",value="192.168.1.72,127.0.0.1"),// IP白名单 (没有配置或者为空，则允许所有访问)
                @WebInitParam(name="deny",value="192.168.1.73"),// IP黑名单 (存在共同时，deny优先于allow)
                @WebInitParam(name="loginUsername",value="admin"),//用户名
                @WebInitParam(name="loginPassword",value="123456"),//密码
                @WebInitParam(name="resetEnable",value="false")//禁用HTML页面上的“Reset All”功能
            }
)
publicclass DruidStatViewServlet extends StatViewServlet{
       privatestatic finallong serialVersionUID = 1L;
}
```

```java
//配置Filter
@WebFilter(filterName="druidWebStatFilter",urlPatterns="/*",
            initParams={
                @WebInitParam(name="exclusions",value="*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*")//忽略资源
            }
)
publicclass DruidStatFilter extends WebStatFilter{
}
```

最后需要在启动类中添加注解：@ServletComponentScan，是spring能够扫描到编写的servlet和filter。

#### 配置方式二

```java
@Configuration
publicclass DruidConfiguration {
    /**
    *注册一个StatViewServlet
    *@return
    */
    @Bean
    public ServletRegistrationBean DruidStatViewServle2(){
        //org.springframework.boot.context.embedded.ServletRegistrationBean提供类的进行注册.
        ServletRegistrationBean servletRegistrationBean =new ServletRegistrationBean(new StatViewServlet(),"/druid2/*");
        //添加初始化参数：initParams
        //白名单：
        servletRegistrationBean.addInitParameter("allow","127.0.0.1");
        //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
        servletRegistrationBean.addInitParameter("deny","192.168.1.73");
        //登录查看信息的账号密码.
        servletRegistrationBean.addInitParameter("loginUsername","admin2");
        servletRegistrationBean.addInitParameter("loginPassword","123456");
        //是否能够重置数据.
        servletRegistrationBean.addInitParameter("resetEnable","false");
        returnservletRegistrationBean;
    }

    /**
    *注册一个：filterRegistrationBean
    *@return
    */
    @Bean
    publicFilterRegistrationBean druidStatFilter2(){
        FilterRegistrationBeanfilterRegistrationBean =new FilterRegistrationBean(new WebStatFilter());
        //添加过滤规则.
        filterRegistrationBean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息.
        filterRegistrationBean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
        returnfilterRegistrationBean;
    }
}
```

### 代码注入DataSource

```java
/**
*注册dataSouce，这里只是一个简单的例子，只注入了部分参数，其它自行注入。
* @param driver
* @param url
* @param username
* @param password
* @param maxActive
* @return
*/
@Bean
public DataSourcedruidDataSource(@Value("${spring.datasource.driverClassName}") Stringdriver,
                                 @Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}")String username,
                                 @Value("${spring.datasource.password}") String password,
                                 @Value("${spring.datasource.maxActive}") int maxActive) {
    DruidDataSource druidDataSource = new DruidDataSource();
    druidDataSource.setDriverClassName(driver);
    druidDataSource.setUrl(url);
    druidDataSource.setUsername(username);
    druidDataSource.setPassword(password);
    druidDataSource.setMaxActive(maxActive);

    System.out.println("DruidConfiguration.druidDataSource(),url="+url+",username="+username+",password="+password);
    try {
        druidDataSource.setFilters("stat, wall");
    } catch(SQLException e) {
        e.printStackTrace();
    }
    returndruidDataSource;
}
```

## Thymeleaf模板

Thymeleaf 是一个跟 Velocity、FreeMarker 类似的模板引擎，它可以完全替代 JSP 。相较与其他的模板引擎，它有如下三个极吸引人的特点：

1. Thymeleaf 在有网络和无网络的环境下皆可运行，即它可以让美工在浏览器查看页面的静态效果，也可以让程序员在服务器查看带数据的动态页面效果。这是由于它支持 html 原型，然后在 html 标签里增加额外的属性来达到模板+数据的展示方式。浏览器解释 html 时会忽略未定义的标签属性，所以 thymeleaf 的模板可以静态地运行；当有数据返回到页面时，Thymeleaf 标签会动态地替换掉静态内容，使页面动态显示。
2. Thymeleaf 开箱即用的特性。它提供标准和spring标准两种方言，可以直接套用模板实现JSTL、 OGNL表达式效果，避免每天套模板、该jstl、改标签的困扰。同时开发人员也可以扩展和创建自定义的方言。
3. Thymeleaf 提供spring标准方言和一个与 SpringMVC 完美集成的可选模块，可以快速的实现表单绑定、属性编辑器、国际化等功能。

### 应用

maven依赖：

```xml
<dependency>
    <groupId>org.thymeleaf</groupId>
    <artifactId>thymeleaf</artifactId>
    <version>2.1.4</version>
</dependency>
```

在application.properties中的默认参数配置

```properties
# THYMELEAF (ThymeleafAutoConfiguration)
#开启模板缓存（默认值：true）
spring.thymeleaf.cache=true 
#Check that the template exists before rendering it.
spring.thymeleaf.check-template=true 
#检查模板位置是否正确（默认值:true）
spring.thymeleaf.check-template-location=true
#Content-Type的值（默认值：text/html）
spring.thymeleaf.content-type=text/html
#开启MVC Thymeleaf视图解析（默认值：true）
spring.thymeleaf.enabled=true
#模板编码
spring.thymeleaf.encoding=UTF-8
#要被排除在解析之外的视图名称列表，用逗号分隔
spring.thymeleaf.excluded-view-names=
#要运用于模板之上的模板模式。另见StandardTemplate-ModeHandlers(默认值：HTML5)
spring.thymeleaf.mode=HTML5
#在构建URL时添加到视图名称前的前缀（默认值：classpath:/templates/）
spring.thymeleaf.prefix=classpath:/templates/
#在构建URL时添加到视图名称后的后缀（默认值：.html）
spring.thymeleaf.suffix=.html
#Thymeleaf模板解析器在解析器链中的顺序。默认情况下，它排第一位。顺序从1开始，只有在定义了额外的TemplateResolver Bean时才需要设置这个属性。
spring.thymeleaf.template-resolver-order=
#可解析的视图名称列表，用逗号分隔
spring.thymeleaf.view-names=
```

增加头文件:`<html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">`

### 标准表达式语法

#### 变量

- 可用值表达式(后台设置): ${...}
- 所有可用值表达式*{...}

>比如*{name} 从可用值中查找name，如果有上下文，比如上层是object，则查object中的name属性。

#### 消息表达式

使用#{...}获取消息表达式，国际化时使用，也可以使用内置的对象，比如date格式化数据

#### URL链接表达式

Thymeleaf对于URL的处理是通过语法@{...}来处理的。

#### 片段表达式

~{...},用来引入公共部分代码片段，并进行传值操作使用的语法

#### 运算符

在表达式中可以使用各类算术运算符，例如+, -, *, /, %，逻辑运算符>, <, <=,>=，==,!=都可以使用，唯一需要注意的是使用<,>时需要用它的HTML转义符

### 标签

#### th:text

th:text="${data}",将data的值替换该属性所在标签的body，字符常量要用引号。

#### th:utext

th:utext，和th:text的区别是"unescaped text"。

#### th:with

th:with,定义变量，th:with="isEven=${prodStat.count}%2==0"，定义多个变量可以用逗号分隔。

#### th:attr

设置标签属性，多个属性可以用逗号分隔，比如th:attr="src=@{/image/aa.jpg},title=#{logo}"，此标签不太优雅，一般用的比较少。

#### th:[tagAttr]

设置标签的各个属性，比如th:value,th:action等。可以一次设置两个属性，比如：th:alt-title="#{logo}"；对属性增加前缀和后缀，用th:attrappend，th:attrprepend,比如：th:attrappend="class=${' '+cssStyle}"；对于属性是有些特定值的，比如checked属性，thymeleaf都采用bool值，比如th:checked=${user.isActive}

#### 循环

使用th:each标签

#### 条件求值

##### If/Unless

Thymeleaf中使用th:if和th:unless属性进行条件判断，例：\<a\>标签只有在th:if中条件成立时才显示，th:unless于th:if恰好相反，只有表达式中的条件不成立，才会显示其内容。

##### Switch

**注：** 默认属性default可以用*表示。

## SpringBoot和Swagger2

在Spring Boot中使用Swagger2构建强大的RESTful API文档。

RESTful API在面对多个开发人员或多个开发团队时，传统的做法是创建一份RESTful APIs文档来记录所有接口细节，然而这样的做法有以下几个问题：

- 由于接口众多，并且细节复杂（需要考虑不同的HTTP请求类型、HTTP头部信息、HTTP请求内容等），高质量地创建这份文档本身就是件非常吃力的事，下游的抱怨声不绝于耳。
- 随着时间推移，不断修改接口实现的时候都必须同步修改接口文档，而文档与代码又处于两个不同的媒介，除非有严格的管理机制，不然很容易导致不一致现象。

为了解决以上的问题，使用Swagger2来管理RESTful APIs，它可以轻松的整合到Spring Boot中，并与Spring MVC程序配合组织出强大RESTful API文档。它既可以减少我们创建文档的工作量，同时说明内容又整合入实现代码中，让维护文档和修改代码整合为一体，可以让我们在修改代码逻辑的同时方便的修改文档说明。另外Swagger2也提供了强大的页面测试功能来调试每个RESTful API。

### 添加Swagger2依赖

```xml
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.2.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.2.2</version>
</dependency>
```

### 创建Swagger2配置类

在Application.java同级创建Swagger2的配置类Swagger2。

```java
/**
 * 创建Swagger2配置类
 *
 * @author superz
 *
 */
@Configuration // 通过@Configuration注解，让Spring来加载该类配置。
@EnableSwagger2 // 通过@EnableSwagger2注解来启用Swagger2。
public class Swagger2 {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)//
                .apiInfo(apiInfoBuild())//
                .select()//select()函数返回一个ApiSelectorBuilder实例用来控制哪些接口暴露给Swagger来展现，本例采用指定扫描的包路径来定义，Swagger会扫描该包下所有Controller定义的API，并产生文档内容（除了被@ApiIgnore指定的请求）。
                .apis(RequestHandlerSelectors.basePackage("com.epoint.superz.restful"))//
                .paths(PathSelectors.any())//
                .build();
    }

    /**
        * 用来创建该Api的基本信息（这些基本信息会展现在文档页面中）
        * 
        * @return
        */
    public ApiInfo apiInfoBuild() {
        return new ApiInfoBuilder() //
                .title("Spring Boot中使用Swagger2构建RESTful APIs").description("第一次使用Swagger2构建Restful APIs")
                .contact("superz")//
                .version("1.0")//
                .build();
    }
}
```

### 添加文档内容

在完成了上述配置后，其实已经可以生产文档内容，但是这样的文档主要针对请求本身，而描述主要来源于函数等命名产生，对用户并不友好，通常需要自己增加一些说明来丰富文档内容。通过@ApiOperation注解来给API增加说明、通过@ApiImplicitParams、@ApiImplicitParam注解来给参数增加说明。

### API文档访问与调试