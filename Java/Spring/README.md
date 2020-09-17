# Spring

## 官网相关

Spring 官网：<https://spring.io/>

Spring 系列主要项目：<https://spring.io/projects>

从配置到安全性，Web应用到大数据-无论您的应用程序的基础架构需求如何，都有一个Spring Project来帮助您构建它。从小处助手，根据需要使用 。Spring 是通过设计模块化的。

Spring官网指南：<https://spring.io/guides> 【备注：此为英文文档】

Spring 官方文档翻译（1~6章）：<https://blog.csdn.net/tangtong1/article/details/51326887#hibernate>

## 概述

Spring框架是一个轻量级的解决方案，可以一站式地构建企业级应用。Spring是**模块化**的，所以可以只使用其中需要的部分。可以在任何web框架上使用控制反转（IoC），也可以只使用Hibernate集成代码或JDBC抽象层。它支持声明式事务管理、通过RMI或web服务实现远程访问，并可以使用多种方式持久化数据。它提供了功能全面的MVC框架，可以透明地集成AOP到软件中。

Spring被设计为非侵入式的，这意味着你的域逻辑代码通常不会依赖于框架本身。在集成层（比如数据访问层），会存在一些依赖同时依赖于数据访问技术和Spring，但是这些依赖可以很容易地从代码库中分离出来。

## 简介

Spring 框架是基于 Java 平台的，它为开发 Java 应用提供了全方位的基础设施支持，并且它很好地处理了这些基础设施，所以可以只需要关注应用本身即可。

### 依赖注入（DI）和控制反转（IoC）

一个Java应用程序，从受限制的嵌入式应用到n层的服务端应用，典型地是由相互合作的对象组成的，因此，一个应用程序中的对象是相互依赖的。

Java平台虽然提供了丰富的应用开发功能，但是它并没有把这些基础构建模块组织成连续的整体，而是把这项任务留给了架构师和开发者。你可以使用设计模式，比如工厂模式、抽象工厂模式、创建者模式、装饰者模式以及服务定位器模式等，来构建各种各样的类和对象实例，从而组成整个应用程序。这些设计模式是很简单的，关键在于它们根据最佳实践起了很好的名字，它们的名字可以很好地描述它们是干什么的、用于什么地方、解决什么问题，等等。这些设计模式都是最佳实践的结晶，所以你应该在你的应用程序中使用它们。

Spring的控制反转解决了上述问题，它提供了一种正式的解决方案，你可以把不相干组件组合在一起，从而组成一个完整的可以使用的应用。Spring根据设计模式编码出了非常优秀的代码，所以可以直接集成到自己的应用中。因此，大量的组织机构都使用Spring来保证应用程序的健壮性和可维护性。

### 模块

Spring 框架是一个分层架构。Spring 总共大约有20个模块，由1300多个不同的文件构成。而这些组件被分别整合在核心容器（Core Container）、Aop（Aspect Oriented Programming）和设备支持（Instrmentation）、数据访问及集成（Data Access/Integeration）、Web、报文发送（Messaging）、Test，6个模块集合中。Spring 模块构建在核心容器上。

以下是 Spring 4 的系统架构图。

![系统架构图](images/Spring4.png)

组成 Spring 框架的每个模块集合或者模块都可以单独存在，也可以一个或多个模块联合实现。每个模块的组成和功能如下：

1. 核心容器：由 `spring-beans` 、 `spring-core` 、 `spring-context` 和 `spring-expression`(Spring Expression Language, 简称：SpEL) 4个模块组成。
    - `spring-beans` 和 `spring-core` 模块是Spring框架的核心模块，包含了控制反转(Inversion of Control, IoC)和依赖注入(Dependency Injection, DI)。`BeanFactory` 接口是 Spring 框架中的核心接口，它是工厂模式的具体实现。`BeanFactory` 使用控制反转对应用程序的配置和依赖性规范与实际的应用程序代码进行了分离。但 `BeanFactory` 容器实例化后并不会自动实例化 Bean，只有当 Bean 被使用时 `BeanFactory` 容器才会对该 Bean 进行实例化与依赖关系的装配。
    - `spring-context` 模块构建于核心模块之上，它扩展了 `BeanFactory`，为它添加了Bean生命周期控制、框架事件体系以及资源加载透明化等功能。此外该模块还提供了许多企业级支持，如邮件访问、远程访问、任务调度等，`ApplicationContext` 是该模块的核心接口，它是 `BeanFactory` 的子类，与 `BeanFactory` 不同，`ApplicationContext` 容器实例化后会自动对所有的单实例 Bean 进行实例化与依赖关系的装配，使之处于待用状态。
    - `spring-expression` 模块是统一表达式语言(unified EL)的扩展模块，可以查询、管理运行中的对象，同时也方便的可以调用对象方法、操作数组、集合等。它的语法类似于传统EL，但提供了额外的功能，最出色的要数函数调用和简单字符串的模板函数。这种语言的特性是基于 Spring 产品的需求而设计，它可以非常方便地同Spring IoC进行交互。
2. Aop和设备支持：由 `spring-aop` 、 `spring-aspects` 和 `spring-instrumentation` 3个模块组成。
    - `spring-aop` 是Spring的另一个核心模块，是Aop主要的实现模块。作为继OOP后，对程序员影响最大的编程思想之一，Aop极大地开拓了人们对于编程的思路。在Spring中，它是以JVM的动态代理技术为基础，然后设计出了一系列的Aop横切实现，比如前置通知、返回通知、异常通知等，同时，Pointcut接口来匹配切入点，可以使用现有的切入点来设计横切面，也可以扩展相关方法根据需求进行切入。
    - `spring-aspects` 模块集成自AspectJ框架，主要是为Spring Aop提供多种Aop实现方法。
    - `spring-instrumentation` 模块是基于JAVA SE中的"java.lang.instrument"进行设计的，应该算是Aop的一个支援模块，主要作用是在JVM启用时，生成一个代理类，程序员通过代理类在运行时修改类的字节，从而改变一个类的功能，实现Aop的功能。【在Spring 官方文档里对这个地方也有点含糊不清，暂放置在Aop模块下。】
3. 数据访问及集成：由 `spring-jdbc` 、 `spring-tx` 、 `spring-orm` 、 `spring-jms` 和 `spring-oxm` 5个模块组成。
    - `spring-jdbc` 模块是Spring 提供的JDBC抽象框架的主要实现模块，用于简化Spring JDBC。主要是提供JDBC模板方式、关系数据库对象化方式、SimpleJdbc方式、事务管理来简化JDBC编程，主要实现类是JdbcTemplate、SimpleJdbcTemplate以及NamedParameterJdbcTemplate。
    - `spring-tx` 模块是Spring JDBC事务控制实现模块。使用Spring框架，它对事务做了很好的封装，通过它的Aop配置，可以灵活的配置在任何一层；但是在很多的需求和应用，直接使用JDBC事务控制还是有其优势的。其实，事务是以业务逻辑为基础的；一个完整的业务应该对应业务层里的一个方法；如果业务操作失败，则整个事务回滚；所以，事务控制是绝对应该放在业务层的；但是，持久层的设计则应该遵循一个很重要的原则：保证操作的原子性，即持久层里的每个方法都应该是不可以分割的。所以，在使用Spring JDBC事务控制时，应该注意其特殊性。
    - `spring-orm` 模块是ORM框架支持模块，主要集成 Hibernate, Java Persistence API (JPA) 和 Java Data Objects (JDO) 用于资源管理、数据访问对象(DAO)的实现和事务策略。
    - `spring-jms` 模块（Java Messaging Service）能够发送和接受信息，自Spring Framework 4.1以后，它还提供了对spring-messaging模块的支撑。
    - `spring-oxm` 模块主要提供一个抽象层以支撑OXM（OXM是Object-to-XML-Mapping的缩写，它是一个O/M-mapper，将java对象映射成XML数据，或者将XML数据映射成java对象），例如：JAXB, Castor, XMLBeans, JiBX 和 XStream等。
4. Web：由 `spring-web` 、`spring-webmvc` 、 `spring-websocket` 和 `spring-webmvc-portlet` 4个模块组成。
    - `spring-web` 模块为Spring提供了最基础Web支持，主要建立于核心容器之上，通过Servlet或者Listeners来初始化IoC容器，也包含一些与Web相关的支持。
    - `spring-webmvc` 模块众所周知是一个的Web-Servlet模块，实现了Spring MVC（model-view-controller）的Web应用。
    - `spring-websocket` 模块主要是与Web前端的全双工通讯的协议。
    - `spring-webmvc-portlet` 模块是知名的Web-Portlets模块（Portlets在Web门户上管理和显示的可插拔的用户界面组件。Portlet产生可以聚合到门户页面中的标记语言代码的片段，如HTML，XML等），主要是为SpringMVC提供Portlets组件支持。
5. 报文发送：即 `spring-messaging` 模块。
    - `spring-messaging` 是Spring4 新加入的一个模块，主要职责是为Spring 框架集成一些基础的报文传送应用。
6. Test：即spring-test模块。
    - `spring-test` 模块主要为测试提供支持的，毕竟在不需要发布（程序）到你的应用服务器或者连接到其他企业设施的情况下能够执行一些集成测试或者其他测试对于任何企业都是非常重要的。

### 依赖关系

#### [core](./SpringCore/核心容器.md)

![Core的依赖关系](images/SpringCore.png)

因为 `spring-core` 依赖了 `commons-logging`，而其他模块都依赖了 `spring-core`，所以整个 Spring 框架都依赖了 `commons-logging`，如果有自己的日志实现如 log4j，可以排除对 `commons-logging` 的依赖，没有日志实现而排除了 `commons-logging` 依赖，编译报错

#### aop

![Aop的依赖关系](images/SpringAop.png)

#### data access

![数据访问的依赖关系](images/Spring数据访问.png)

#### web

![Web的依赖关系](images/SpringWeb.png)

#### test

![Test的依赖关系](images/SpringTest.png)

#### 其他

![webSocket&message的依赖关系](images/SpringOther.png)

### 依赖管理和命名约定

| groupId             |        artifactId        | 描述                                       |
| ------------------- | :----------------------: | ------------------------------------------ |
| org.springframework |        spring-aop        | 基于代理的AOP                              |
| org.springframework |      spring-aspects      | 基于切面的AspectJ                          |
| org.springframework |       spring-beans       | bean支持，包括Groovy                       |
| org.springframework |      spring-context      | 运行时上下文，包括调度和远程调用抽象       |
| org.springframework |  spring-context-support  | 包含用于集成第三方库到Spring上下文的类     |
| org.springframework |       spring-core        | 核心库，被许多其它模块使用                 |
| org.springframework |    spring-expression     | Spring表达式语言                           |
| org.springframework |    spring-instrument     | JVM引导的检测代理                          |
| org.springframework | spring-instrument-tomcat | tomcat的检测代理                           |
| org.springframework |       spring-jdbc        | JDBC支持包，包括对数据源设置和JDBC访问支持 |
| org.springframework |        spring-jms        | JMS支持包，包括发送和接收JMS消息的帮助类   |
| org.springframework |     spring-messaging     | 消息处理的架构和协议                       |
| org.springframework |        spring-orm        | 对象关系映射，包括对JPA和Hibernate支持     |
| org.springframework |        spring-oxm        | 对象XML映射                                |
| org.springframework |       spring-test        | 单元测试和集成测试组件                     |
| org.springframework |        spring-tx         | 事务基础，包括对DAO的支持及JCA的集成       |
| org.springframework |        spring-web        | web支持包，包括客户端及web远程调用         |
| org.springframework |      spring-webmvc       | REST web服务及web应用的MVC实现             |
| org.springframework |  spring-webmvc-portlet   | 用于Portlet环境的MVC实现                   |
| org.springframework |     spring-websocket     | WebSocket和SockJS实现，包括对STOMP的支持   |