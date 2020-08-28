# 参考指南

- [参考指南](#参考指南)
  - [简介](#简介)
  - [入门](#入门)
    - [Maven 安装](#maven-安装)
    - [Spring Boot CLI 安装](#spring-boot-cli-安装)
    - [添加 classpath 依赖](#添加-classpath-依赖)
    - [创建可执行 jar](#创建可执行-jar)
  - [使用](#使用)
    - [继承 starter parent](#继承-starter-parent)
    - [在不使用 parent POM 的情况使用 Spring Boot](#在不使用-parent-pom-的情况使用-spring-boot)
    - [使用 Spring Boot Maven 插件](#使用-spring-boot-maven-插件)
  - [Starters](#starters)

## 简介

Spring Boot 简化了基于 Spring 的应用开发，只需要**`run`**就能创建一个独立的、产品级别的 Spring 应用。一般为 Spring 平台及第三方库提供开箱即用的设置，这样就可以有条不紊的开始。多数 Spring Boot 应用只需要很少的 Spring 配置。

可以使用 Spring Boot 创建 Java 应用，并使用 `java -jar` 启动它或次啊用传统的 war 部署方式，同时也提供了一个运行 "**spring 脚本**"的命令行工具。

Spring Boot 的主要目标是：
- 为所有 Spring 开发提供一个从根本上更快，且随处可得的入门体验
- 开箱即用，但通过不采用默认设置可以快速摆脱这种方式
- 提供一系列大型项目常用的非功能性特征，比如：内嵌服务器，安全，指标，健康监测，外部化配置
- 绝对没有代码生成，也不需要 XML 配置

## 入门

<font color="blue">查看spring-boot-*.jar相关信息</font>

对于 java 开发者来说，使用 Spring Boot 就跟使用其他 Java 库一样，只需要在 `classpath` 下引入适当的 `spring-boot-*.jar` 文件。Spring Boot 不需要集成任何特殊的工具，所以可以使用任何IDE或文本编辑器；同时，Spring Boot应用也没有什么特殊之处，可以像对待其他Java程序那样运行，调试它。
尽管可以拷贝 Spring Boot jars，但还是建议使用支持依赖管理的构建工具，比如Maven或Gradle。

### Maven 安装

一般自己搭建的spring boot项目的POM文件会继承 **`spring-boot-starter-parent`** 工程，并声明一个或多个依赖。

### Spring Boot CLI 安装

Spring Boot CLI是一个命令行工具，可用于快速搭建基于 Spring 的原型。它支持运行 Groovy 脚本，这也就意味着可以使用类似 Java 的语法，但不用写很多的模板代码。

Spring Boot不一定非要配合CLI使用，但它绝对是Spring应用取得进展的最快方式。

### 添加 classpath 依赖

Spring Boot 提供很多"**Starter**"，用来简化添加 jars 到 classpath 的操作。一般可以直接在项目的 POM 的 parent 节点使用了 `spring-boot-starter-parent` ，它是一个特殊的 starter ，提供了有用的 Maven 默认设置。同时，它也提供一个 dependency-management 节点，这样对于需要的依赖就可以省略 version 标记。

其他 "Starters" 只简单提供开发特定类型应用所需要的依赖。

### 创建可执行 jar

可执行 jars（有时候被称为胖 jars）是包含编译后的类及代码运行所需依赖 jar 的存档。

**可执行 jars 和 Java**：Java 没有提供任何标准方式，用于加载内嵌 jar 文件（即 jar 文件中还包含 jar 文件），这对分发自包含应用来说是个问题。为了解决该问题，很多开发者采用"共享的"jars。共享的jar只是简单地将所有 jars 的类打包进一个单独的存档，这种方式存在的问题是很难区分应用程序中使用了那些库。在多个 jars 中如果存在相同的文件名（但内容不一样）也会是一个问题。Spring Boot 采取一个不同的方式，允许真正的直接内嵌 jars。

为了创建可执行的 jar，需要将 `spring-boot-maven-plugin` 添加到 `pom.xml` 中，在 dependencies 节点后插入以下内容：

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

查看打包 jar 包的内部结构，可以运行 jar tvf :`jar tvf xxx.jar`

使用 java -jar 命令运行应用程序：`java -jar xxx.jar`

## 使用

### 继承 starter parent

配置项目，继承自 `spring-boot-starter-parent`，只需将 parent 按如下设置：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.1.RELEASE</version>
</parent>
```

注：只需在该依赖上指定 Spring Boot 版本，如果导入其他的 starters，放心的省略版本号。

按照以上设置，可以在自己的项目中通过覆盖属性来覆盖个别的依赖。

### 在不使用 parent POM 的情况使用 Spring Boot

如果不想使用 `spring-boot-starter-parent` POM,通过设置 `scope=import` 的依赖，仍能获取到依赖管理的好处：

```xml
<dependencyManagement>
     <dependencies>
        <dependency>
            <!-- Import dependency management from Spring Boot -->
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>2.1.1.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

以上配置不允许使用属性覆盖个别依赖。

### 使用 Spring Boot Maven 插件

Spring Boot 包含一个 Maven 插件，它可以将项目打包成一个可执行 jar。如果需要使用它，可以将该插件添加到 `<plugins>` 节点处：

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

## Starters

Starters 是一个依赖描述符的集合，可以将它包含进项目中，这样添加依赖就非常方便。可以获取所有 Spring 及相关技术的一站式服务，而不需要翻阅示例代码，拷贝粘贴大量的依赖描述符。

该 Starters 包含很多搭建，快速运行项目所需要的依赖，并提供一致的，可管理传递性的依赖集。

**名字有什么含义**：所有官方 starters 遵循相似的命名模式： `spring-boot-starter-*` 。该命名结构旨在帮助用户找到需要的 starter。

以下应用程序 starters 是 Spring Boot 在 `org.springframework.boot` group 下提供的：

| 名称                                   | 描述                                                         | Pom                                                          |
| -------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| spring-boot-starter-test               | 用于测试Spring Boot应用，支持常用测试类库，包括JUnit, Hamcrest和Mockito | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-test/pom.xml) |
| spring-boot-starter-mobile             | 用于使用Spring Mobile开发web应用                             | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-mobile/pom.xml) |
| spring-boot-starter-social-twitter     | 对使用Spring Social Twitter的支持                            | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-social-twitter/pom.xml) |
| spring-boot-starter-cache              | 用于使用Spring框架的缓存支持                                 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-cache/pom.xml) |
| spring-boot-starter-activemq           | 用于使用Apache ActiveMQ实现JMS消息                           | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-activemq/pom.xml) |
| spring-boot-starter-jta-atomikos       | 用于使用Atomikos实现JTA事务                                  | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-jta-atomikos/pom.xml) |
| spring-boot-starter-aop                | 用于使用Spring AOP和AspectJ实现面向切面编程                  | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-aop/pom.xml) |
| spring-boot-starter-web                | 用于使用Spring MVC构建web应用，包括RESTful。Tomcat是默认的内嵌容器 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-web/pom.xml) |
| spring-boot-starter-data-elasticsearch | 用于使用Elasticsearch搜索，分析引擎和Spring Data Elasticsearch | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-elasticsearch/pom.xml) |
| spring-boot-starter-jdbc               | 对JDBC的支持（使用Tomcat JDBC连接池）                        | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-jdbc/pom.xml) |
| spring-boot-starter-batch              | 对Spring Batch的支持                                         | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-batch/pom.xml) |
| spring-boot-starter-social-facebook    | 用于使用Spring Social Facebook                               | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-social-facebook/pom.xml) |
| spring-boot-starter-web-services       | 对Spring Web服务的支持                                       | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-web-services/pom.xml) |
| spring-boot-starter-jta-narayana       | Spring Boot Narayana JTA Starter                             | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-jta-narayana/pom.xml) |
| spring-boot-starter-thymeleaf          | 用于使用Thymeleaf模板引擎构建MVC web应用                     | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-thymeleaf/pom.xml) |
| spring-boot-starter-mail               | 用于使用Java Mail和Spring框架email发送支持                   | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-mail/pom.xml) |
| spring-boot-starter-jta-bitronix       | 用于使用Bitronix实现JTA事务                                  | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-jta-bitronix/pom.xml) |
| spring-boot-starter-data-mongodb       | 用于使用基于文档的数据库MongoDB和Spring Data MongoDB         | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-mongodb/pom.xml) |
| spring-boot-starter-validation         | 用于使用Hibernate Validator实现Java Bean校验                 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-validation/pom.xml) |
| spring-boot-starter-jooq               | 用于使用JOOQ访问SQL数据库，可使用[spring-boot-starter-data-jpa](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#spring-boot-starter-data-jpa)或[spring-boot-starter-jdbc](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#spring-boot-starter-jdbc)替代 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-jooq/pom.xml) |
| spring-boot-starter-redis              | 用于使用Spring Data Redis和Jedis客户端操作键-值存储的Redis，在1.4中已被[spring-boot-starter-data-redis](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#spring-boot-starter-data-redis)取代 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-redis/pom.xml) |
| spring-boot-starter-data-cassandra     | 用于使用分布式数据库Cassandra和Spring Data Cassandra         | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-cassandra/pom.xml) |
| spring-boot-starter-hateoas            | 用于使用Spring MVC和Spring HATEOAS实现基于超媒体的RESTful web应用 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-hateoas/pom.xml) |
| spring-boot-starter-integration        | 用于使用Spring Integration                                   | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-integration/pom.xml) |
| spring-boot-starter-data-solr          | 通过Spring Data Solr使用Apache Solr搜索平台                  | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-solr/pom.xml) |
| spring-boot-starter-freemarker         | 用于使用FreeMarker模板引擎构建MVC web应用                    | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-freemarker/pom.xml) |
| spring-boot-starter-jersey             | 用于使用JAX-RS和Jersey构建RESTful web应用，可使用[spring-boot-starter-web](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#spring-boot-starter-web)替代 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-jersey/pom.xml) |
| spring-boot-starter                    | 核心starter，包括自动配置支持，日志和YAML                    | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter/pom.xml) |
| spring-boot-starter-data-couchbase     | 用于使用基于文档的数据库Couchbase和Spring Data Couchbase     | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-couchbase/pom.xml) |
| spring-boot-starter-artemis            | 使用Apache Artemis实现JMS消息                                | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-artemis/pom.xml) |
| spring-boot-starter-cloud-connectors   | 对Spring Cloud Connectors的支持，用于简化云平台下（例如Cloud Foundry 和Heroku）服务的连接 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-cloud-connectors/pom.xml) |
| spring-boot-starter-social-linkedin    | 用于使用Spring Social LinkedIn                               | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-social-linkedin/pom.xml) |
| spring-boot-starter-velocity           | 用于使用Velocity模板引擎构建MVC web应用，**从1.4版本过期**   | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-velocity/pom.xml) |
| spring-boot-starter-data-rest          | 用于使用Spring Data REST暴露基于REST的Spring Data仓库        | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-rest/pom.xml) |
| spring-boot-starter-data-gemfire       | 用于使用分布式数据存储GemFire和Spring Data GemFire           | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-gemfire/pom.xml) |
| spring-boot-starter-groovy-templates   | 用于使用Groovy模板引擎构建MVC web应用                        | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-groovy-templates/pom.xml) |
| spring-boot-starter-amqp               | 用于使用Spring AMQP和Rabbit MQ                               | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-amqp/pom.xml) |
| spring-boot-starter-hornetq            | 用于使用HornetQ实现JMS消息，被[spring-boot-starter-artemis](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#spring-boot-starter-artemis)取代 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-hornetq/pom.xml) |
| spring-boot-starter-ws                 | 用于使用Spring Web服务，被[spring-boot-starter-web-services](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#spring-boot-starter-web-services)取代 | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-ws/pom.xml) |
| spring-boot-starter-security           | 对Spring Security的支持                                      | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-security/pom.xml) |
| spring-boot-starter-data-redis         | 用于使用Spring Data Redis和Jedis客户端操作键—值数据存储Redis | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-redis/pom.xml) |
| spring-boot-starter-websocket          | 用于使用Spring框架的WebSocket支持构建WebSocket应用           | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-websocket/pom.xml) |
| spring-boot-starter-mustache           | 用于使用Mustache模板引擎构建MVC web应用                      | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-mustache/pom.xml) |
| spring-boot-starter-data-neo4j         | 用于使用图数据库Neo4j和Spring Data Neo4j                     | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-neo4j/pom.xml) |
| spring-boot-starter-data-jpa           | 用于使用Hibernate实现Spring Data JPA                         | [Pom](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters/spring-boot-starter-data-jpa/pom.xml) |

