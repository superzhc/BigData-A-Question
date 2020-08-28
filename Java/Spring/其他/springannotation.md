---
title: Spring常用注解
date: 2017-12-01
tags: spring
---
# Spring注解

## 概述

注解配置相对于 XML 配置具有很多的优势：

- 它可以充分利用 Java 的反射机制获取类结构信息，这些信息可以有效减少配置的工作。如使用 JPA 注释配置 ORM 映射时，我们就不需要指定 PO 的属性名、类型等信息，如果关系表字段和 PO 属性名、类型都一致，您甚至无需编写任务属性映射信息——因为这些信息都可以通过 Java 反射机制获取。
- 注解和 Java 代码位于一个文件中，而 XML 配置采用独立的配置文件，大多数配置信息在程序开发完成后都不会调整，如果配置信息和 Java 代码放在一起，有助于增强程序的内聚性。而采用独立的 XML 配置文件，程序员在编写一个功能时，往往需要在程序文件和配置文件中不停切换，这种思维上的不连贯会降低开发效率。

## 常用注解

### @Required

依赖检查

### @Value

用于注入SpEL，可以放置在字段、方法参数上

### @Autowired

Spring 2.5 引入了 @Autowired 注释，它可以对类成员变量、方法及构造函数进行标注，完成自动装配的工作。

Spring 通过一个 BeanPostProcessor 对 @Autowired 进行解析，所以要让@Autowired 起作用必须事先在 Spring 容器中声明 AutowiredAnnotationBeanPostProcessor Bean。当 Spring 容器启动时，AutowiredAnnotationBeanPostProcessor 将扫描 Spring 容器中所有 Bean，当发现 Bean 中拥有@Autowired 注释时就找到和其匹配（默认按类型匹配）的 Bean，并注入到对应的地方中去。

在默认情况下使用 @Autowired 注释进行自动注入时，Spring 容器中匹配的候选 Bean 数目必须有且仅有一个。

- 当找不到一个匹配的 Bean 时，Spring 容器将抛出BeanCreationException 异常，并指出必须至少拥有一个匹配的 Bean。当不能确定 Spring 容器中一定拥有某个类的 Bean 时，可以在需要自动注入该类 Bean 的地方可以使用 @Autowired(required = false)，这等于告诉 Spring：在找不到匹配 Bean 时也不报错。
- 如果 Spring 容器中拥有多个候选 Bean，Spring 容器在启动时也会抛出 BeanCreationException 异常。Spring 允许我们通过 @Qualifier 注释指定注入 Bean 的名称，这样歧义就消除了。@Autowired 和@Qualifier 结合使用时，自动注入的策略就从 byType 转变成 byName 了。@Autowired 可以对成员变量、方法以及构造函数进行注释，而@Qualifier 的标注对象是成员变量、方法入参、构造函数入参。@Qualifier 只能和 @Autowired 结合使用，是对 @Autowired 有益的补充。一般来讲，@Qualifier 对方法签名中入参进行注释会降低代码的可读性，而对成员变量注释则相对好一些。

### @Resource

@Resource 的作用相当于 @Autowired，只不过 @Autowired 按 byType 自动注入，面@Resource 默认按 byName 自动注入罢了。@Resource 有两个属性是比较重要的，分别是 name 和 type，Spring 将@Resource 注释的 name 属性解析为 Bean 的名字，而 type 属性则解析为 Bean 的类型。所以如果使用 name 属性，则使用 byName 的自动注入策略，而使用 type 属性时则使用 byType 自动注入策略。如果既不指定 name 也不指定 type 属性，这时将通过反射机制使用 byName 自动注入策略。

一般情况下，我们无需使用类似于 @Resource(type=Test.class) 的注释方式，因为 Bean 的类型信息可以通过 Java 反射从代码中获取。

要让 JSR-250 的注释生效，除了在 Bean 类中标注这些注释外，还需要在 Spring 容器中注册一个负责处理这些注释的 BeanPostProcessor：`<bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"/>`,CommonAnnotationBeanPostProcessor 实现了 BeanPostProcessor 接口，它负责扫描使用了 JSR-250 注释的 Bean，并对它们进行相应的操作。

#### 使用 \<context:annotation-config/\> 简化配置

Spring 2.1 添加了一个新的 context 的 Schema 命名空间，该命名空间对注解驱动、属性文件引入、加载期织入等功能提供了便捷的配置。注解本身是不会做任何事情的，它仅提供元数据信息。要使元数据信息真正起作用，必须让负责处理这些元数据的处理器工作起来。

AutowiredAnnotationBeanPostProcessor 和 CommonAnnotationBeanPostProcessor 就是处理这些注解元数据的处理器。但是直接在 Spring 配置文件中定义这些 Bean 显得比较笨拙。Spring 提供了一种方便的注册这些BeanPostProcessor 的方式，这就是 \<context:annotation-config/\>。

\<context:annotationconfig/\> 将隐式地向 Spring 容器注册 AutowiredAnnotationBeanPostProcessor、CommonAnnotationBeanPostProcessor、PersistenceAnnotationBeanPostProcessor 以及equiredAnnotationBeanPostProcessor 这 4 个 BeanPostProcessor。

### @PostConstruct 和 @PreDestroy

Spring 容器中的 Bean 是有生命周期的，Spring 允许在 Bean 在初始化完成后以及 Bean 销毁前执行特定的操作，您既可以通过实现 InitializingBean/DisposableBean 接口来定制初始化之后 / 销毁之前的操作方法，也可以通过 \<bean\> 元素的 init-method/destroy-method 属性指定初始化之后 / 销毁之前调用的操作方法。

JSR-250 为初始化之后/销毁之前方法的指定定义了两个注释类，分别是 @PostConstruct 和 @PreDestroy，这两个注释只能应用于方法上。标注了 @PostConstruct 注释的方法将在类实例化后调用，而标注了 @PreDestroy 的方法将在类销毁之前调用。

**重点：** 不管是通过实现 InitializingBean/DisposableBean 接口，还是通过 \<bean\> 元素的init-method/destroy-method 属性进行配置，都只能为 Bean 指定一个初始化 / 销毁的方法。但是使用 @PostConstruct 和 @PreDestroy 注释却可以指定多个初始化 / 销毁方法，那些被标注 @PostConstruct 或 @PreDestroy 注释的方法都会在初始化 / 销毁时被执行。

### @Repository

### @Component

@Component 是一个泛化的概念，仅仅表示一个组件 (Bean) ，可以作用在任何层次。

@Component 有一个可选的入参，用于指定 Bean 的名称；一般情况下，Bean 都是 singleton 的，需要注入 Bean 的地方仅需要通过 byType 策略就可以自动注入了，所以大可不必指定 Bean 的名称。

在使用 @Component 注解后，Spring 容器必须启用类扫描机制以启用注解驱动 Bean 定义和注解驱动 Bean 自动注入的策略。

### @Service

@Service 通常作用在业务层，但是目前该功能与 @Component 相同。

### @Constroller

@Constroller 注解标识一个类作为控制器。DispatcherServlet 会扫描所有控制器类，并检测 @RequestMapping 注解配置的方法。Web 自动化配置已经处理完这一步骤。【控制器就是控制请求接收和负责响应到视图的角色。】

**注：** 通过在类上使用 @Repository、@Component、@Service 和 @Constroller 注解，Spring 会自动创建相应的 BeanDefinition 对象，并注册到 ApplicationContext 中。这些类就成了 Spring 受管组件。这三个注解除了作用于不同软件层次的类，其使用方式与 @Repository 是完全相同的。

### RequestMapping

@RequestMapping 注解标识请求 URL 信息，可以映射到整个类或某个特定的方法上。该注解可以表明请求需要的。

- 使用 value 指定特定的 URL ，比如 @RequestMapping(value = "/users”) 和 @RequestMapping(value = "/users/create”) 等。
- 使用 method 指定 HTTP 请求方法，比如 RequestMethod.GET 等
- 还有使用其他特定的参数条件，可以设置 consumes 指定请求时的请求头需要包含的 Content-Type 值、设置 produces 可确保响应的内容类型

### @ResponseBody

@ResponseBody 注解标识该方法的返回值。这样被标注的方法返回值，会直接写入 HTTP 响应体（而不会被视图解析器认为是一个视图对象）。

### @RestController

@RestController 注解，和 @Controller 用法一致，整合了 @Controller 和 @ResponseBody 功能。这样不需要每个 @RequestMapping 方法上都加上 @ResponseBody 注解，这样代码更简明。