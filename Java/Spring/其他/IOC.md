---
title: Spring IoC
date: 2017-11-14
tags: spring
---
## IoC基础

IoC(Inversion of Control)即*控制反转*，不是一种技术，而是一种思想。IoC主要是将设计好的对象交给容器控制，而不是传统的在对象内部直接控制。

在传统Java SE程序设计中，一般都是直接在对象内部通过new进行创建对象，是程序主动去创建依赖对象；而IoC是有专门一个容器去创建这些对象，即由IoC容器来控制对象的创建，当然IoC容器控制对象主要控制了外部资源的获取（不只是对象包括比如文件等）。

反转：传统应用程序是在对象中主动控制去直接获取依赖对象，而反转则是由容器来帮忙创建及注入依赖对象；为何是反转？因为由容器来查找及注入依赖对象，对象只是被动的接受依赖对象，所以是反转；那些方面反转了？依赖的对象的获取被反转了。

DI(Dependency Injection)即*依赖注入*，是组件之间依赖关系由容器在运行期间决定，形象的说，即由容器动态的将某个依赖关系注入到组件中。依赖注入的目的并非为软件系统代入更多的功能，而是为了提升组件重用的频率，并为系统搭建了一个灵活可扩展的平台，通过依赖注入机制，只需要通过简单的配置，而无需任何代码就可指定目标需要的资源，完成资审的逻辑，而不需要关心具体的资源来自何处，由谁实现。

理解DI的关键是：“谁依赖谁，为什么需要依赖，谁注入谁，注入了什么”，那我们来深入分析一下：
 
- 谁依赖于谁：当然是应用程序依赖于IoC容器；
- 为什么需要依赖：应用程序需要IoC容器来提供对象需要的外部资源；
- 谁注入谁：很明显是IoC容器注入应用程序某个对象，应用程序依赖的对象；
- 注入了什么：就是注入某个对象所需要的外部资源（包括对象、资源、常量数据）。

## IoC容器基本原理

### IoC容器的概念

IoC容器就是具有依赖注入功能的容器，IoC容器负责实例化、定位、配置应用程序中的对象及建立这些对象间的依赖。应用程序无需直接在代码中new相关的对象，应用程序由IoC容器进行组装。在Spring中BeanFactory是IoC容器的实际代表者。

Spring IoC容器如何知道哪些是它管理的对象呢？这就需要配置文件，Spring IoC容器通过读取配置文件中的配置元数据，通过元数据对应用中的各个对象进行实例化及装配。一般使用基于xml配置文件进行配置元数据，而且Spring与配置文件完全解耦的，可以使用其他任何可能的方式进行配置元数据，比如注解、基于java文件的、基于属性文件的配置都可以。

### Bean的概念

由IoC容器管理的组成应用程序的对象叫作Bean，Bean就是由Spring容器初始化、装配及管理的对象，除此之外，bean就与应用程序中的其他对象没有什么区别了。那IoC怎样确定如何实例化Bean、管理Bean之间的依赖关系以及管理Bean呢？这就需要配置元数据，在Spring中由BeanDefinition代表。

### 详解IoC容器

在Spring Ioc容器的代表就是org.springframework.beans包中的BeanFactory接口，BeanFactory接口提供了IoC容器最基本功能；而org.springframework.context包下的ApplicationContext接口扩展了BeanFactory，还提供了与Spring AOP集成、国际化处理、事件传播及提供不同层次的context实现 (如针对web应用的WebApplicationContext)。简单说， BeanFactory提供了IoC容器最基本功能，而 ApplicationContext 则增加了更多支持企业级功能支持。ApplicationContext完全继承BeanFactory，因而BeanFactory所具有的语义也适用于ApplicationContext。

IoC容器的工作方式，以xml配置方式为例，
1. 准备配置文件：在配置文件中声明Bean定义也就是为Bean配置元数据。
2. 由IoC容器进行解析元数据： IoC容器的Bean Reader读取并解析配置文件，根据定义生成BeanDefinition配置元数据对象，IoC容器根据BeanDefinition进行实例化、配置及组装Bean。
3. 实例化IoC容器：由客户端实例化容器，获取需要的Bean。

执行过程如图：![](http://ozchbp0v3.bkt.clouddn.com/SpringIoC.jpg)

## IoC的配置

### XML配置的结构

一般配置文件结构如下：

```xml
<beans>  
    <import resource="resource1.xml"/>  
    <bean id="bean1" class=""></bean>  
    <bean id="bean2" class=""></bean>  
<bean name="bean2" class=""></bean>  
    <alias alias="bean3" name="bean2"/>  
    <import resource="resource2.xml"/>  
</beans> 
```

1. <bean>标签主要用来进行Bean定义；
2. alias用于定义Bean别名的；
3. import用于导入其他配置文件的Bean定义，这是为了加载多个配置文件，当然也可以把这些配置文件构造为一个数组（new String[] {"config1.xml", "config2.xml"}）传给ApplicationContext实现进行加载多个配置文件。<import>标签可以放在<beans>下的任何位置，没有顺序关系。

### Bean的配置

Spring IoC容器目的就是管理Bean，这些Bean将根据配置文件中的Bean定义进行创建，而Bean定义在容器内部由BeanDefinition对象表示，该定义主要包含以下信息：

- 全限定类名（FQN）：用于定义Bean的实现类;
- Bean行为定义：这些定义了Bean在容器中的行为；包括作用域（单例、原型创建）、是否惰性初始化及生命周期等；
- Bean创建方式定义：说明是通过构造器还是工厂方法创建Bean；
- Bean之间关系定义：即对其他bean的引用，也就是依赖关系定义，这些引用bean也可以称之为同事bean 或依赖bean，也就是依赖注入。

Bean定义只有“全限定类名”在当使用构造器或静态工厂方法进行实例化bean时是必须的，其他都是可选的定义。难道Spring只能通过配置方式来创建Bean吗？回答当然不是，某些SingletonBeanRegistry接口实现类实现也允许将那些非BeanFactory创建的、已有的用户对象注册到容器中，这些对象必须是共享的，比如使用DefaultListableBeanFactory 的registerSingleton() 方法。不过建议采用元数据定义。

### Bean的命名

每个Bean可以有一个或多个id（或称之为标识符或名字），在这里我们把第一个id称为“标识符”，其余id叫做“别名”；这些id在IoC容器中必须唯一。

如何为Bean指定id呢，有以下几种方式：

1. 不指定id，只配置必须的全限定类名，由IoC容器为其生成一个标识，客户端必须通过接口“T getBean(Class<T> requiredType)”获取Bean；
2. 指定id，必须在Ioc容器中唯一；
3. 指定name，这样name就是“标识符”，必须在Ioc容器中唯一；
4. 指定id和name，id就是标识符，而name就是别名，必须在Ioc容器中唯一；
5. 指定多个name，多个name用“,”、“;”、“ ”分割，第一个被用作标识符，其他的（alias1、alias2、alias3）是别名，所有标识符也必须在Ioc容器中唯一；
6. 使用<alias>标签指定别名，别名也必须在IoC容器中唯一；

从定义来看，name或id如果指定它们中的一个时都作为“标识符”，那为什么还要有id和name同时存在呢？这是因为当使用基于XML的配置元数据时，在XML中id是一个真正的XML id属性，因此当其他的定义来引用这个id时就体现出id的好处了，可以利用XML解析器来验证引用的这个id是否存在，从而更早的发现是否引用了一个不存在的bean，而使用name，则可能要在真正使用bean时才能发现引用一个不存在的bean。

**Bean命名约定：** Bean的命名遵循XML命名规范，但最好符合Java命名规范，由“字母、数字、下划线组成“，而且应该养成一个良好的命名习惯， 比如采用“驼峰式”，即第一个单词首字母开始，从第二个单词开始首字母大写开始，这样可以增加可读性。

### 实例化Bean

Spring IoC容器则需要根据Bean定义里的配置元数据使用反射机制来创建Bean。在Spring IoC容器中根据Bean定义创建Bean主要有以下几种方式：

1. 使用构造器实例化Bean：这是最简单的方式，Spring IoC容器即能使用默认空构造器也能使用有参数构造器两种方式创建Bean，如以下方式指定要创建的Bean类型：

    1. 使用空构造器进行定义，使用此种方式，class属性指定的类必须有空构造器
    2. 使用有参数构造器进行定义，使用此中方式，可以使用< constructor-arg >标签指定构造器参数值，其中index表示位置，value表示常量值，也可以指定引用，指定引用使用ref来引用另一个Bean定义

2. 使用静态工厂方式实例化Bean，使用这种方式除了指定必须的class属性，还要指定factory-method属性来指定实例化Bean的方法，而且使用静态工厂方法也允许指定方法参数，spring IoC容器将调用此属性指定的方法来获取Bean
3. 使用实例工厂方法实例化Bean，使用这种方式不能指定class属性，此时必须使用factory-bean属性来指定工厂Bean，factory-method属性指定实例化Bean的方法，而且使用实例工厂方法允许指定方法参数，方式和使用构造器方式一样

## DI的配置使用

- 构造器注入：就是容器实例化Bean时注入那些依赖，通过在在Bean定义中指定构造器参数进行注入依赖，包括实例工厂方法参数注入依赖，但静态工厂方法参数不允许注入依赖；
- setter注入：通过setter方法进行注入依赖；
- 方法注入：能通过配置方式替换掉Bean方法，也就是通过配置改变Bean方法功能。

### 构造器注入

使用构造器注入通过配置构造器参数实现，构造器参数就是依赖。除了构造器方式，还有静态工厂、实例工厂方法可以进行构造器注入。构造器注入可以根据参数索引注入、参数类型注入或Spring3支持的参数名注入，但参数名注入是有限制的，需要使用在编译程序时打开调试模式（即在编译时使用“javac –g:vars”在class文件中生成变量调试信息，默认是不包含变量调试信息的，从而能获取参数名字，否则获取不到参数名字）或在构造器上使用@ConstructorProperties（java.beans.ConstructorProperties）注解来指定参数名。

```xml
<!-- 通过构造器参数索引方式依赖注入 -->  
<bean id="byIndex" class="cn.javass.spring.chapter3.HelloImpl3">  
    <constructor-arg index="0" value="Hello World!"/>  
    <constructor-arg index="1" value="1"/>  
</bean>  
<!-- 通过构造器参数类型方式依赖注入 -->  
<bean id="byType" class="cn.javass.spring.chapter3.HelloImpl3">  
   <constructor-arg type="java.lang.String" value="Hello World!"/>  
   <constructor-arg type="int" value="2"/>  
</bean>  
<!-- 通过构造器参数名称方式依赖注入 -->  
<bean id="byName" class="cn.javass.spring.chapter3.HelloImpl3">  
   <constructor-arg name="message" value="Hello World!"/>  
   <constructor-arg name="index" value="3"/>  
</bean>  
```

静态工厂注入方式：

```java
//静态工厂类  
package cn.javass.spring.chapter3;  

import cn.javass.spring.chapter2.helloworld.HelloApi;

public class DependencyInjectByStaticFactory {

       public static HelloApi newInstance(String message, int index) {  
              return new HelloImpl3(message, index);  
       }  
}  
```

配置文件：

```xml
<bean id="byIndex" class="cn.javass.spring.chapter3.DependencyInjectByStaticFactory" factory-method="newInstance">  
    <constructor-arg index="0" value="Hello World!"/>  
    <constructor-arg index="1" value="1"/>  
</bean>  
<bean id="byType" class="cn.javass.spring.chapter3.DependencyInjectByStaticFactory" factory-method="newInstance">  
    <constructor-arg type="java.lang.String" value="Hello World!"/>  
    <constructor-arg type="int" value="2"/>  
</bean>  
<bean id="byName" class="cn.javass.spring.chapter3.DependencyInjectByStaticFactory" factory-method="newInstance">  
    <constructor-arg name="message" value="Hello World!"/>  
    <constructor-arg name="index" value="3"/>  
</bean> 
```

实例工厂注入方式：

```java
//实例工厂类  
package cn.javass.spring.chapter3;  

import cn.javass.spring.chapter2.helloworld.HelloApi;

public class DependencyInjectByInstanceFactory {  

    public HelloApi newInstance(String message, int index) {  
        return new HelloImpl3(message, index);  
    }  
}
```

配置xml：

```xml
<bean id="instanceFactory" class="cn.javass.spring.chapter3.DependencyInjectByInstanceFactory"/>  
   
<bean id="byIndex" factory-bean="instanceFactory"  factory-method="newInstance">  
    <constructor-arg index="0" value="Hello World!"/>  
    <constructor-arg index="1" value="1"/>  
</bean>  
<bean id="byType" factory-bean="instanceFactory" factory-method="newInstance">  
    <constructor-arg type="java.lang.String" value="Hello World!"/>  
    <constructor-arg type="int" value="2"/>  
</bean>  
<bean id="byName" factory-bean="instanceFactory" factory-method="newInstance">  
    <constructor-arg name="message" value="Hello World!"/>  
    <constructor-arg name="index" value="3"/>  
</bean>  
```

### setter注入

setter注入，是通过在通过构造器、静态工厂或实例工厂实例好Bean后，通过调用Bean类的setter方法进行注入依赖。

```java
package cn.javass.spring.chapter3;  
import cn.javass.spring.chapter2.helloworld.HelloApi;  
public class HelloImpl4 implements HelloApi {  
    private String message;  
    private int index;  
    //setter方法  
    public void setMessage(String message) {  
        this.message = message;  
    }  
    public void setIndex(int index) {  
        this.index = index;  
    }  
    @Override  
    public void sayHello() {  
        System.out.println(index + ":" + message);  
    }  
} 
```

配置xml：

```xml
<!-- 通过setter方式进行依赖注入 -->  
    <bean id="bean" class="cn.javass.spring.chapter3.HelloImpl4">  
        <property name="message" value="Hello World!"/>  
        <property name="index">  
            <value>1</value>  
        </property>  
    </bean>  
```

### 注入常量

### 注入Bean ID

### 注入集合、数组和字典

Spring不仅能注入简单类型数据，还能注入集合（Collection、无序集合Set、有序集合List）类型、数组(Array)类型、字典(Map)类型数据、Properties类型数据。

1. 注入集合类型：包括Collection类型、Set类型、List类型数据：

    1. List类型：需要使用<list>标签来配置注入
    2. Set类型：需要使用<set>标签来配置注入，其配置参数及含义和<lsit>标签完全一样
    3. Collection类型：因为Collection类型是Set和List类型的基类型，所以使用<set>或<list>标签都可以进行注入

2. 注入数组类型：需要使用<array>标签来配置注入，其中标签属性“value-type”和“merge”和<list>标签含义完全一样
3. 注入字典（Map）类型：字典类型是包含键值对数据的数据结构，需要使用<map>标签来配置注入，其属性“key-type”和“value-type”分别指定“键”和“值”的数据类型，其含义和<list>标签的“value-type”含义一样，并使用<key>子标签来指定键数据，<value>子标签来指定键对应的值数据
4. Properties注入：Spring能注入java.util.Properties类型数据，需要使用<props>标签来配置注入，键和值类型必须是String，不能变，子标签<prop key=”键”>值</prop>来指定键值对

### 引用其它Bean

### 内部Bean定义

### 处理null值

 Spring通过<value>标签或value属性注入常量值，所有注入的数据都是字符串，那如何注入null值呢？通过“null”值吗？当然不是因为如果注入“null”则认为是字符串。Spring通过<null/>标签注入null值。

 ## 延迟初始化Bean

 延迟初始化也叫做惰性初始化，指不提前初始化Bean，而是只有在真正使用时才创建及初始化Bean。

配置方式很简单只需在<bean>标签上指定 “lazy-init” 属性值为“true”即可延迟初始化Bean。

Spring容器会在创建容器时提前初始化“singleton”作用域的Bean，“singleton”就是单例的意思即整个容器每个Bean只有一个实例，后边会详细介绍。Spring容器预先初始化Bean通常能帮助我们提前发现配置错误，所以如果没有什么情况建议开启，除非有某个Bean可能需要加载很大资源，而且很可能在整个应用程序生命周期中很可能使用不到，可以设置为延迟初始化。

延迟初始化的Bean通常会在第一次使用时被初始化；或者在被非延迟初始化Bean作为依赖对象注入时在会随着初始化该Bean时被初始化，因为在这时使用了延迟初始化Bean。

容器管理初始化Bean消除了编程实现延迟初始化，完全由容器控制，只需在需要延迟初始化的Bean定义上配置即可，比编程方式更简单，而且是无侵入代码的。 

## 使用depends-on

depends-on是指指定Bean初始化及销毁时的顺序，使用depends-on属性指定的Bean要先初始化完毕后才初始化当前Bean，由于只有“singleton”Bean能被Spring管理销毁，所以当指定的Bean都是“singleton”时，使用depends-on属性指定的Bean要在指定的Bean之后销毁。

## 自动装配

自动装配就是指由Spring来自动地注入依赖对象，无需人工参与。

目前Spring3.0支持“no”、“byName ”、“byType”、“constructor”四种自动装配，默认是“no”指不支持自动装配的，其中Spring3.0已不推荐使用之前版本的“autodetect”自动装配，推荐使用Java 5+支持的（@Autowired）注解方式代替；如果想支持“autodetect”自动装配，请将schema改为“spring-beans-2.5.xsd”或去掉。

自动装配的好处是减少构造器注入和setter注入配置，减少配置文件的长度。自动装配通过配置<bean>标签的“autowire”属性来改变自动装配方式。接下来让我们挨着看下配置的含义。

1. default：表示使用默认的自动装配，默认的自动装配需要在<beans>标签中使用default-autowire属性指定，其支持“no”、“byName ”、“byType”、“constructor”四种自动装配
2. no：意思是不支持自动装配，必须明确指定依赖。
3. byName：通过设置Bean定义属性autowire="byName"，意思是根据名字进行自动装配，只能用于setter注入。比如我们有方法“setHelloApi”，则“byName”方式Spring容器将查找名字为helloApi的Bean并注入，如果找不到指定的Bean，将什么也不注入。此处注意了，*在根据名字注入时，将把当前Bean自己排除在外*
4. “byType”：通过设置Bean定义属性autowire="byType"，意思是指根据类型注入，用于setter注入，比如如果指定自动装配方式为“byType”，而“setHelloApi”方法需要注入HelloApi类型数据，则Spring容器将查找HelloApi类型数据，如果找到一个则注入该Bean，如果找不到将什么也不注入，如果找到多个Bean将优先注入<bean>标签“primary”属性为true的Bean，否则抛出异常来表明有个多个Bean发现但不知道使用哪个。
5. “constructor”：通过设置Bean定义属性autowire="constructor"，功能和“byType”功能一样，根据类型注入构造器参数，只是用于构造器注入方式。
6. autodetect：自动检测是使用“constructor”还是“byType”自动装配方式，已不推荐使用。如果Bean有空构造器那么将采用“byType”自动装配方式，否则使用“constructor”自动装配方式。此处要把3.0的xsd替换为2.5的xsd，否则会报错。

不是所有类型都能自动装配：

- 不能自动装配的数据类型：Object、基本数据类型（Date、CharSequence、Number、URI、URL、Class、int）等；
- 通过“<beans>”标签default-autowire-candidates属性指定的匹配模式，不匹配的将不能作为自动装配的候选者，例如指定“*Service，*Dao”，将只把匹配这些模式的Bean作为候选者，而不匹配的不会作为候选者；
- 通过将“<bean>”标签的autowire-candidate属性可被设为false，从而该Bean将不会作为依赖注入的候选者。

数组、集合、字典类型的根据类型自动装配和普通类型的自动装配是有区别的：

- 数组类型、集合（Set、Collection、List）接口类型：将根据泛型获取匹配的所有候选者并注入到数组或集合中，如“List<HelloApi> list”将选择所有的HelloApi类型Bean并注入到list中，而对于集合的具体类型将只选择一个候选者，“如 ArrayList<HelloApi> list”将选择一个类型为ArrayList的Bean注入，而不是选择所有的HelloApi类型Bean进行注入；
- 字典（Map）接口类型：同样根据泛型信息注入，键必须为String类型的Bean名字，值根据泛型信息获取，如“Map<String, HelloApi> map” 将选择所有的HelloApi类型Bean并注入到map中，而对于具体字典类型如“HashMap<String, HelloApi> map”将只选择类型为HashMap的Bean注入，而不是选择所有的HelloApi类型Bean进行注入。

**注：** 自动装配注入和配置注入方式一同工作时，*配置注入的数据会覆盖自动装配注入的数据*。

## 依赖检查

用于检查Bean定义的属性都注入数据了，不管是自动装配的还是配置方式注入的都能检查，如果没有注入数据将报错，从而提前发现注入错误，只检查具有setter方法的属性。

Spring3+也不推荐配置方式依赖检查了，建议采用Java5+ @Required注解方式。

## Bean作用域

什么是作用域呢？即“scope”，在面向对象程序设计中一般指对象或变量之间的可见范围。而在Spring容器中是指其创建的Bean对象相对于其他Bean对象的请求可见范围。

Spring提供“singleton”和“prototype”两种基本作用域，另外提供“request”、“session”、“global session”三种web作用域；Spring还允许用户定制自己的作用域。

### 基本的作用域

1. singleton：指“singleton”作用域的Bean只会在每个Spring IoC容器中存在一个实例，而且其完整生命周期完全由Spring容器管理。对于所有获取该Bean的操作Spring容器将只返回同一个Bean。
2. prototype：即原型，指每次向Spring容器请求获取Bean都返回一个全新的Bean，相对于“singleton”来说就是不缓存Bean，每次都是一个根据Bean定义创建的全新Bean。

### Web应用中的作用域

在Web应用中，我们可能需要将数据存储到request、session、global session。因此Spring提供了三种Web作用域：request、session、globalSession。

1. request作用域：表示每个请求需要容器创建一个全新Bean。比如提交表单的数据必须是对每次请求新建一个Bean来保持这些表单数据，请求结束释放这些数据。
2. session作用域：表示每个会话需要容器创建一个全新Bean。比如对于每个用户一般会有一个会话，该用户的用户信息需要存储到会话中，此时可以将该Bean配置为web作用域。
3. globalSession：类似于session作用域，只是其用于portlet环境的web应用。如果在非portlet环境将视为session作用域。