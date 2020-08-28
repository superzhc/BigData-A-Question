# Bean 使用

- [Bean 使用](#bean-使用)
  - [Bean 定义](#bean-定义)
    - [bean 定义的属性](#bean-定义的属性)
    - [Spring 配置元数据](#spring-配置元数据)
  - [Bean 作用域](#bean-作用域)
  - [Bean 生命周期](#bean-生命周期)
  - [Bean 后置处理器](#bean-后置处理器)
  - [Bean 定义继承](#bean-定义继承)
  - [依赖注入](#依赖注入)
    - [Spring 基于构造函数的依赖注入](#spring-基于构造函数的依赖注入)
    - [Spring 基于 Setter 方法的依赖注入](#spring-基于-setter-方法的依赖注入)
  - [注入内部 Beans](#注入内部-beans)
  - [注入集合](#注入集合)
  - [Beans 自动装配](#beans-自动装配)
  - [基于注解的配置](#基于注解的配置)
    - [**`@Required` 注解**](#required-注解)
    - [**`@Autowired` 注解**](#autowired-注解)
    - [**`@Qualifier` 注解**](#qualifier-注解)
    - [**`JSR-250` 注解**](#jsr-250-注解)
  - [基于 Java 的配置](#基于-java-的配置)
    - [`@Configuration`和`@Bean`注解](#configuration和bean注解)
  - [`@Import` 注解](#import-注解)
  - [Spring 中的事件处理](#spring-中的事件处理)

## Bean 定义

被称作 Bean 的对象是构成应用程序的支柱也是由 Spring IoC 容器管理的。bean 是一个被实例化，组装，并通过 Spring IoC 容器所管理的对象。这些 bean 是由容器提供的**配置元数据**创建的。

bean 定义包含称为**配置元数据**的信息，配置元数据包含以下定义：
- 如何创建一个 bean
- bean 的生命周期的详细信息
- bean 的依赖关系

### bean 定义的属性

|           属性           | 描述                                                           |
|:------------------------:|--------------------------------------------------------------|
|          class           | 这个属性是强制的，并且指定用来创建 bean 的实现类                |
|           name           | 这个属性指定 bean 标识符                                       |
|          scope           | 这个属性指定由特定的 bean 定义创建的对象的作用域               |
|     constructor-arg      | 它是用来注入依赖关系的，构造函数注入                            |
|        properties        | 它是用来注入依赖关系的，setter方法注入                          |
|     autowiring mode      | 它是用来注入依赖关系的 ???                                     |
| lazy-initialization mode | 延迟初始化 bean，此属性告诉容器只有第一次请求才去创建 bean 实例 |
|   initialization 方法    | 在 bean 的所有必需的属性被容器设置之后，调用回调方法            |
|     destruction 方法     | 当包含该 bean 的容器被销毁时，调用回调方法                      |
|          parent          | bean定义的继承                                                 |

### Spring 配置元数据

Spring IoC 容器完全由实际编写的配置元数据的格式解耦，有下面三个重要的方法把配置元数据提供给 Spring 容器：
- 基于 XML 的配置文件
- 基于注解的配置
- 基于 Java 的配置

## Bean 作用域

Spring 框架支持以下五个作用域，如果使用 web-aware ApplicationContext 时，其中三个是可用的。

|    作用域     | 描述                                                                          |
|:-------------:|-----------------------------------------------------------------------------|
|   singleton   | 该作用域将 bean 的定义限制在每一个 Spring IoC 容器中只有一个单一实例【注：默认】 |
|   prototype   | 该作用于将单一 bean 的定义限制在任意数量的对象实例                            |
|    request    | 该作用域将 bean 的定义限制为 HTTP 请求，只有在 web 的上下文中有效              |
|    session    | 该作用域将 bean 的定义限制为 HTTP 会话，只有在 web 的上下文中有效              |
| globa-session | 该作用域将 bean 的定义限制为全局 HTTP 会话，只有在 web 的上下文中有效          |

## Bean 生命周期

为了定义安装和拆卸一个 bean，只要声明带有 **`init-method`**、**`destroy-method`** 参数的。`init-method`属性指定一个方法，在实例化 bean 时，立即调用该方法，同样，`destroy-method` 指定一个方法，只要从容器中移除 bean 之后，才能调用该方法。 

- **默认的初始化和销毁方法**

如果具有太多相同名称的初始化或销毁方法的 bean，可以不需要再每个 bean 上声明**初始化方法**和**销毁方法**。框架使用元素中的**`default-init-method`**、**`default-destroy-method`**属性提供了灵活地配置这种情况，如下所示：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd"
    default-init-method="init" 
    default-destroy-method="destroy">

   <bean id="..." class="...">
       <!-- collaborators and configuration for this bean go here -->
   </bean>
</beans>
```

## Bean 后置处理器

**BeanPostProcessor** 接口定义回调方法，可以通过实现该接口方法来实现自己的实例化逻辑，依赖解析逻辑等。也可以在 Spring 容器通过插入一个或多个 BeanPostProcessor 的实现来完成实例化，配置和初始化一个 bean 之后实现一些自定义逻辑回调方法。

可以配置多个 BeanPostProcessor 接口，通过设置 BeanPostProcessor 实现的 **Ordered** 接口提供的 order 属性来控制这些 BeanPostProcessor 接口的执行顺序。

BeanPostProcessor 可以对 bean（或对象）实例进行操作，这意味着 Spring IoC 容器实例化一个 bean 实例后 BeanPostProcessor 接口开始进行它们的工作。

**ApplicationContext** 会自动检测由 **BeanPostProcessor** 接口实现定义的 bean，注册这些 bean 为后置处理器，然后通过在容器中创建 bean ，在适当的时候调用它。

## Bean 定义继承

bean 定义可以包含很多的配置信息，包括构造函数的参数，属性值，容器的具体信息例如初始化方法，静态工厂方法名等等。

子 bean 的定义继承父定义的配置数据。子定义可以根据需要重写一些值，或者添加其他值。

Spring Bean 定义的继承与 Java 类的继承无关，但是继承的概念是一样的。可以定义一个父 bean 的定义作为模板和其他子 bean 就可以从父 bean 中继承所需的配置。

当使用基于 XML 的配置元数据时，通过使用父属性，指定父 bean 作为该属性的值来表明子 bean 的定义。

- **Bean 定义模板**

在定义一个 Bean 定义模板时，不应该指定类的属性，而应该制定带有 **true** 值的**抽象**属性，如下所示：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">

   <bean id="beanTeamplate" abstract="true">
      <property name="message1" value="Hello World!"/>
      <property name="message2" value="Hello Second World!"/>
      <property name="message3" value="Namaste India!"/>
   </bean>

   <bean id="helloIndia" class="com.tutorialspoint.HelloIndia" parent="beanTeamplate">
      <property name="message1" value="Hello India!"/>
      <property name="message3" value="Namaste India!"/>
   </bean>

</beans>
```

父 bean 自身不能被实例化，因为它是不完整的，而且它也被明确地标记为抽象的。当一个定义是抽象的，它仅仅作为一个纯粹的模板 bean 定义来使用的，充当子定义的父定义使用。

## 依赖注入

- **类构造函数**注入
- **Setter 方法**注入

### Spring 基于构造函数的依赖注入

当容器调用带有一组参数的类构造函数时，基于构造函数的 DI 就完成了，其中每个参数代表一个对其他类的依赖。

- **构造函数参数解析**：

如果存在不止一个参数时，当把参数传递给构造函数时，可能会存在歧义，要解决这个问题，那么参数值按照构造函数的参数在 bean 定义中的顺序即可。

如果使用 type 属性显式地指定了构造函数参数的类型，容器也可以使用与简单类型匹配的类型，如下：

```xml
<beans>

   <bean id="exampleBean" class="examples.ExampleBean">
      <constructor-arg type="int" value="2001"/>
      <constructor-arg type="java.lang.String" value="Zara"/>
   </bean>

</beans>
```

最好还是使用 index 属性来显式地指定构造函数参数的索引。

注：<font color="red">**一个对象传递一个引用，需要使用标签的 ref 属性，直接传递值得话，应该使用 value 属性**</font>

### Spring 基于 Setter 方法的依赖注入

当容器调用一个无参的构造函数或一个无参的静态 factory 方法来初始化 bean 后，通过容器在 bean 上调用 Setter 方法，基于 Setter 方法的 DI 就完成了。

- **使用 p-namespace 实现 XML 配置**

使用 p-namespace 可以实现更简洁的写法。

## 注入内部 Beans

内部 Bean（inner beans）是在其他 bean 的范围内定义的 bean。如下所示：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">

   <bean id="outerBean" class="...">
      <property name="target">
         <bean id="innerBean" class="..."/>
      </property>
   </bean>

</beans>
```

## 注入集合

Spring 提供了四种类型的集合的配置元素，如下所示：

|   元素    | 描述                                           |
|:---------:|----------------------------------------------|
| \<list\>  | 注入列表值                                     |
|  \<set\>  | 注入set                                        |
|  \<map\>  | 注入名称-值对的集合，其中名称和值可以是任何类型 |
| \<props\> | 注入名称-值对的集合，其中名称和值都是字符串类型 |

## Beans 自动装配

- **自动装配模式**

下列自动装配模式，它们可用于指示 Spring 容器来使用自动装配进行依赖注入。可以使用元素的 **autowire** 属性为一个 bean 定义制定自动装配模式。

|    模式     | 描述                                                                                                                                                                                                                                         |
|:-----------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|     no      | 这是默认的设置，它意味着没有自动装配，应该使用显式地 bean 引用来依赖注入。无需为了依赖注入做特殊的事情                                                                                                                                          |
|   byName    | 由属性名自动装配。Spring 容器看到在 XML 配置文件中的 bean 的*自动装配*的属性为 *byName* 。然后尝试匹配，并且将它的属性与配置文件中被定义为相同名称的 beans 的属性进行连接                                                                       |
|   byType    | 由属性数据类型自动装配。Spring 容器看到在 XML 配置文件中 bean 的*自动装配*的属性设置为 *byType* 。然后如果它的**类型**匹配配置文件中的一个确切的 bean 名称，它将尝试匹配和连接属性的类型。如果存在不止一个这样的 bean，则一个致命的异常将会被抛出 |
| constructor | 类似于 byType，但该类型适用于构造函数参数类型。如果在容器中没有一个构造函数参数类型的 bean，则一个致命错误将会发生                                                                                                                              |
| autodetect  | Spring 首先尝试通过 *constructor* 使用自动装配来连接，如果它不执行，Spring 尝试通过 *byType* 来自动装配                                                                                                                                        |

可以使用 **byType** 或者 **constructor** 自动装配模式来连接数组和其他类型的集合。

- **自动装配的局限性**

当自动装配始终在同一个项目中使用时，它的效果最好。如果通常不使用自动装配，它可能会使开发人员混淆的使用它来连接只有一个或两个 bean 定义。不过，自动装配可以**显著的减少需要指定的属性或构造器参数，但应该在使用它们之前考虑到自动装配的局限性和缺点**

| 限制         | 描述                                                                          |
|------------|-----------------------------------------------------------------------------|
| 重写的可能性 | 可以使用总是重写自动装配的`<constructor-arg>`和`<property>`设置来指定以来关系 |
| 原始数据类型 | 不能自动装配所谓的简单类型包括基本类型，字符串和类                             |
| 混乱的本质   | 自动装配不如显式装配精准，所以如果可能的话尽可能使用显式装配                   |

## 基于注解的配置

从 Spring 2.5 开始就可以使用注解来配置以来注入，而不是采用 XML 来描述一个 bean 的依赖注入，可以使用相关类，方法或字段声明的注解，将 bean 配置移动到组件类本身。

注解依赖注入默认情况下在 Spring 容器中是不开启的。因此，在可以使用基于注解的依赖注入之前，需要在 Spring 配置文件中启用它。开启配置如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd">

   <context:annotation-config/>
   <!-- bean definitions go here -->

</beans>
```

|         注解          | 描述                                                                                             |
|:---------------------:|------------------------------------------------------------------------------------------------|
|      `@Required`      | `@Required`注解应用于 bean 属性的 setter 方法                                                    |
|     `@Autowired`      | @Autowired注解可以应用到 bean 属性的 setter 方法，非 setter 方法，构造函数和属性                   |
|     `@Qualifier`      | `@Qualifier`通过指定确切的将被依赖注入的 bean，`@Autowired` 和 `@Qualifier` 注解可以用来删除混乱  |
| `JSR-250` Annotations | Spring 支持 `JSR-250` 的基础的注解，其中包括了 `@Resource`，`@PostConstruct` 和 `@PreDestory` 注解 |

### **`@Required` 注解**

**@Required** 注解应用于 bean 属性的 setter方法，它表明使用此注解的 bean 的属性在配置时必须放在 XML 配置文件中，否则容器就会抛出一个 BeanInitializationException 异常。

### **`@Autowired` 注解**

**@Autowired** 注解对在哪里和如何完成自动连接提供了更多的细微的控制。

@Autowired 注解可以在 setter 方法中被用于自动连接 bean，就像 @Autowired 注解，容器，一个属性或者任意命名的可能带有多个参数的方法。

- **Setter 方法中的 @Autowired**

可以在 XML 文件中的 setter 方法中使用 @Autowired 注解来除去`<property>`元素。当 Spring遇到一个在 setter 方法中使用的 @Autowired 注解，它会在方法中视图执行 byType 自动连接。

- **属性中的 @Autowired**

可以在属性中使用 @Autowired 注解来除去 setter 方法。当使用自动连接属性传递的时候，Spring 会将这些传递过来的值或者引用自动分配给那些属性。

- **构造函数中的 @Autowired**

可以在构造函数中使用 @Autowired。一个构造函数使用 @Autowired 注解，说明当创建 bean 时，即使在 XML 文件中没有使用`<constructor>`元素配置 bean ，构造函数也会被自动连接。

- **@Autowired 的（required=false）选项**

默认情况下，@Autowired 注解意味着依赖是必须的，它类似于 @Required 注解，然而，可以使用 @Autowired 的 `required=false` 选项关闭默认行为。

### **`@Qualifier` 注解**

可能会有这样一种情况，当创建多个具有相同类型的 bean 时，并且想要用一个属性只为它们其中的一个进行装配，在这种情况下，可以使用 `@Qualifier` 注解和 `@Autowired` 注解来指定哪一个真正的 bean 将会被装配来消除混乱

### **`JSR-250` 注解**

Spring还使用基于 `JSR-250` 注解，它包括 `@PostConstruct`， `@PreDestroy` 和 `@Resource` 注解。

- **@PostConstruct 和 @PreDestroy 注解**

为了定义一个 bean 的安装和卸载，一般使用 `init-method`、`destroy-method` 参数简单的声明一下 。`init-method` 属性指定了一个方法，该方法在 bean 的实例化阶段会立即被调用。同样地，`destroy-method` 指定了一个方法，该方法只在一个 bean 从容器中删除之前被调用。

注解情况下可以使用 `@PostConstruct` 注解作为初始化回调函数的一个替代，`@PreDestroy` 注解作为销毁回调函数的一个替代

## 基于 Java 的配置

基于 Java 配置的常用注解：

### `@Configuration`和`@Bean`注解

带有 **`@Configuration`** 的注解类表示这个类可以作为 Spring IoC 容器，用来定义 Bean 使用。**`@Bean`** 注解用于定义一个 bean，返回一个对象，该对象被注册在 Spring 应用程序上下文中。

示例如下：

```java
package com.tutorialspoint;
import org.springframework.context.annotation.*;
@Configuration
public class HelloWorldConfig {
   @Bean 
   public HelloWorld helloWorld(){
      return new HelloWorld();
   }
}
```

上面的代码等同于下面的 XML 配置：

```xml
<beans>
   <bean id="helloWorld" class="com.tutorialspoint.HelloWorld" />
</beans>
```

带有 `@Bean` 注解的方法名称作为 bean 的ID。它创建并返回实际的 bean，在配置类中可以声明多个 `@Bean`。一旦定义了配置类，就可以使用 `AnnotationConfigApplicationContext` 来加载并它们提供给 Spring 容器，如下所示：

```java
public static void main(String[] args) {
   ApplicationContext ctx = new AnnotationConfigApplicationContext(HelloWorldConfig.class); 
   HelloWorld helloWorld = ctx.getBean(HelloWorld.class);
   helloWorld.setMessage("Hello World!");
   helloWorld.getMessage();
}
```

也可以加载各种配置类，如下所示：

```java
public static void main(String[] args) {
   AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
   ctx.register(AppConfig.class, OtherConfig.class);
   ctx.register(AdditionalConfig.class);
   ctx.refresh();
   MyService myService = ctx.getBean(MyService.class);
   myService.doStuff();
}
```

- **注入 Bean 的依赖性**

当 `@Bean` 依赖对象时，表达这种依赖性很简单，只要哟个 bean 方法调用另一个，如下所示：

```java
package com.tutorialspoint;
import org.springframework.context.annotation.*;
@Configuration
public class AppConfig {
   @Bean
   public Foo foo() {
      return new Foo(bar());
   }
   @Bean
   public Bar bar() {
      return new Bar();
   }
}
```

- **生命周期回调**

`@Bean` 注解支持指定任意的初始化和销毁的回调方法，就像在 bean 元素中 Spring 的 XML 的初始化方法和销毁方法的属性：

```java
public class Foo {
   public void init() {
      // initialization logic
   }
   public void cleanup() {
      // destruction logic
   }
}

@Configuration
public class AppConfig {
   @Bean(initMethod = "init", destroyMethod = "cleanup" )
   public Foo foo() {
      return new Foo();
   }
}
```

- **指定 Bean 的范围**

默认范围是单实例，但是可以使用 `@Scope` 注解配置方法，如下所示：

```java
@Configuration
public class AppConfig {
   @Bean
   @Scope("prototype")
   public Foo foo() {
      return new Foo();
   }
}
```

## `@Import` 注解

**`@Import`** 注解允许从另一个配置类中加载 @Bean 定义。

## Spring 中的事件处理

<font color="red">实际应用场景</font>

通过 ApplicationEvent 类和 ApplicationListener 接口来提供在 ApplicationContext 中处理事件。如果一个 bean 实现 ApplicationLister，那么每次 ApplicationEvent 被发布到 ApplicationContext 上，那个 bean 会被通知。

Spring 提供了以下的标准事件：

|Spring 内置事件|描述|
|---|------|
|`ContextRefreshedEvent`|ApplicationContext 被初始化或刷新时，该事件被发布。这也可以在ConfigurableApplicationContext 接口中使用 `refresh()` 方法来发生|
|`ContextStartedEvent`|当使用 ConfigurableApplicationContext 接口中的 start() 方法启动 ApplicationContext 时，该事件被发布。你可以调查你的数据库，或者你可以在接受到这个事件后重启任何停止的应用程序。|
|`ContextStoppedEvent`|当使用 ConfigurableApplicationContext 接口中的 stop() 方法停止 ApplicationContext 时，发布这个事件。你可以在接受到这个事件后做必要的清理的工作。|
|`ContextClosedEvent`|当使用 ConfigurableApplicationContext 接口中的 close() 方法关闭 ApplicationContext 时，该事件被发布。一个已关闭的上下文到达生命周期末端；它不能被刷新或重启。|
|`RequestHandledEvent`|这是一个 web-specific 事件，告诉所有 bean HTTP 请求已经被服务。|

由于 Spring 的事件处理是单线程的，所以如果一个事件被发布，直至并且除非所有的接收者得到的该消息，该进程被阻塞并且流程将不会继续。因此，如果事件处理被使用，在设计应用程序时应注意。