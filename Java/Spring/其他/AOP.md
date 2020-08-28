---
title: Spring AOP
date: 2017-11-15
tags: spring
---
# Spring AOP

## AOP基础

> 面向方面编程(AOP)：也可称为面向切面编程，是一种编程范式，提供从另一个角度来考虑程序结构从而完善面向对象编程(OOP)。

在进行OOP开发时，都是基于对组件（比如类）进行开发，然后对组件进行组合，OOP最大问题就是无法解耦组件进行开发，而AOP就是为了克服这个问题而出现的，它来进行这种耦合的分离。它利用一种称为“横切”的技术，剖解开封装的对象内部，并将那些影响了多个类的公共行为封装到一个可重用的模块，并将其命名为“Aspect”，即方面。所谓方面，简单的说，就是将那些与业务无关，却为业务模块所公共调用的逻辑或责任封装起来，便于减少系统的重复代码，降低模块间的耦合度，并有利于未来的可操作性和可维护性。

AOP为开发者提供一种进行横切关注点（比如日志关注点横切了支付关注点）分离并织入的机制，把横切关注点分离，然后通过某种技术织入到系统中，从而无耦合的完成了功能。

AOP主要用于横切关注点分离和织入，因此需要理解横切关注点和织入：

- 关注点：也可称为核心关注点，可以认为是所关注的任何东西，一般业务处理的主要流程是核心关注点；
- 横切关注点：一个组件无法完成需要的功能，需要其他组件协作完成，横切关注点的一个特点是，它们经常发生在核心关注点的多处，而且各处都基本相似；
- 关注点分离：将问题细化从而单独部分，即可以理解为不可再分割的组件；
- 织入：横切关注点分离后，需要通过某种技术将横切关注点融合到系统中从而完成需要的功能，因此需要织入，织入可能在编译期、加载期、运行期等进行。

横切关注点可能包含很多，比如非业务的：日志、事务处理、缓存、性能统计、权限控制等等这些非业务的基础功能；还可能是业务的：如某个业务组件横切于多个模块。如下图：

![](http://ozchbp0v3.bkt.clouddn.com/Spring_AOP.jpg)

AOP功能：

- 用于横切关注点的分离和织入横切关注点到系统；
- 完善OOP；
- 降低组件和模块之间的耦合性；
- 使系统容易扩展；
- 而且由于关注点分离从而可以获得组件的更好复用。

AOP的实现技术，主要分为两类：

1. 采用动态代理技术，利用截取消息的方式，对该消息进行装饰，以取代原有对象行为的执行；
2. 采用静态织入的方式，引入特定的语法创建“方面”，从而使得编译器可以在编译期间织入有关“方面”的代码。

### AOP使用场景

AOP用来封装横切关注点，具体可以在下面的场景中使用:

- Authentication 权限
- Caching 缓存
- Context passing 内容传递
- Error handling 错误处理
- Lazy loading　懒加载
- Debugging　　调试
- logging, tracing, profiling and monitoring　记录跟踪　优化　校准
- Performance optimization　性能优化
- Persistence　　持久化
- Resource pooling　资源池
- Synchronization　同步
- Transactions 事务

### AOP的基础概念

- **连接点（Jointpoint）** ：表示需要在程序中插入横切关注点的扩展点，连接点可能是类初始化、方法执行、方法调用、字段调用或处理异常等等，Spring只支持方法执行连接点；
- **切入点（Pointcut）** ：选择一组相关连接点的模式，即可以认为连接点的集合，Spring支持perl5正则表达式和AspectJ切入点模式，Spring默认使用AspectJ语法；
- **通知（Advice）** ：在连接点上执行的行为，通知提供了在AOP中需要在切入点所选择的连接点处进行扩展现有行为的手段；包括前置通知（before advice）、后置通知(after advice)、环绕通知（around advice），在Spring中通过代理模式实现AOP，并通过拦截器模式以环绕连接点的拦截器链织入通知；
- **引入（inter-type declaration）** ：也称为内部类型声明，为已有的类添加额外新的字段或方法，Spring允许引入新的接口（必须对应一个实现）到所有被代理对象（目标对象）；
- **方面/切面（Aspect）** ：横切关注点的模块化。可以认为是通知、引入和切入点的组合；在Spring中可以使用Schema和@AspectJ方式进行组织实现；
- **目标对象（Target Object）** ：需要被织入横切关注点的对象，即该对象是切入点选择的对象，需要被通知的对象，从而也可称为“被通知对象”；由于Spring AOP 通过代理模式实现，从而这个对象永远是被代理对象；
- **AOP代理（AOP Proxy）** ：AOP框架使用代理模式创建的对象，从而实现在连接点处插入通知（即应用切面），就是通过代理来对目标对象应用切面。在Spring中，AOP代理可以用JDK动态代理或CGLIB代理实现，而通过拦截器模型应用切面。
- **织入（Weaving）**：织入是一个过程，是将切面应用到目标对象从而创建出AOP代理对象的过程，织入可以在编译期、类装载期、运行期进行。

在AOP中，通过切入点选择目标对象的连接点，然后在目标对象的相应连接点处织入通知，而切入点和通知就是切面（横切关注点），而在目标对象连接点处应用切面的实现方式是通过AOP代理对象，如下图：

![](http://ozchbp0v3.bkt.clouddn.com/Spring_AOP2.jpg)

Spring的通知类型：

- **前置通知（Before Advice）** :在切入点选择的连接点处的方法之前执行的通知，该通知不影响正常程序执行流程（除非该通知抛出异常，该异常将中断当前方法链的执行而返回）。
- **后置通知（After Advice）** :在切入点选择的连接点处的方法之后执行的通知，包括如下类型的后置通知：
    - **后置返回通知（After returning Advice）** :在切入点选择的连接点处的方法正常执行完毕时执行的通知，必须是连接点处的方法没抛出任何异常正常返回时才调用后置通知。
    - **后置异常通知（After throwing Advice）** : 在切入点选择的连接点处的方法抛出异常返回时执行的通知，必须是连接点处的方法抛出任何异常返回时才调用异常通知。
    - **后置最终通知（After finally Advice）** : 在切入点选择的连接点处的方法返回时执行的通知，不管抛没抛出异常都执行，类似于Java中的finally块。
- **环绕通知（Around Advices）** ：环绕着在切入点选择的连接点处的方法所执行的通知，环绕通知可以在方法调用之前和之后自定义任何行为，并且可以决定是否执行连接点处的方法、替换返回值、抛出异常等等。

### AOP的使用

可以通过配置文件或者编程的方式来使用Spring AOP。

配置可以通过xml文件来进行，大概有四种方式：

1. 配置ProxyFactoryBean，显式地设置advisors, advice, target等
2. 配置AutoProxyCreator，这种方式下，还是如以前一样使用定义的bean，但是从容器中获得的其实已经是代理对象
3. 通过<aop:config>来配置
4. 通过\<aop:aspectj-autoproxy\>来配置，使用AspectJ的注解来标识通知及切入点

也可以直接使用ProxyFactory来以编程的方式使用Spring AOP，通过ProxyFactory提供的方法可以设置target对象, advisor等相关配置，最终通过 getProxy()方法来获取代理对象

### AOP代理对象

AOP代理就是AOP框架通过代理模式创建的对象，Spring使用JDK动态代理或CGLIB代理来实现，Spring缺省使用JDK动态代理来实现，从而任何接口都可别代理，如果被代理的对象实现不是接口将默认使用CGLIB代理，不过CGLIB代理当然也可应用到接口。

AOP代理的目的就是将切面织入到目标对象。

## 基于Schema的AOP

基于Schema的AOP从Spring2.0之后通过“aop”命名空间来定义切面、切入点及声明通知。

在Spring配置文件中，所以AOP相关定义必须放在<aop:config>标签下，该标签下可以有<aop:pointcut>、<aop:advisor>、<aop:aspect>标签，配置顺序不可变。

- <aop:pointcut>：用来定义切入点，该切入点可以重用；
- <aop:advisor>：用来定义只有一个通知和一个切入点的切面；
- <aop:aspect>：用来定义切面，该切面可以包含多个切入点和通知，而且标签内部的通知和切入点定义是无序的；和advisor的区别就在此，advisor只包含一个通知和一个切入点。

![](http://ozchbp0v3.bkt.clouddn.com/Spring_AOP3.JPG)

### 声明切面

切面就是包含切入点和通知的对象，在Spring容器中将被定义为一个Bean，Schema方式的切面需要一个切面支持Bean，该支持Bean的字段和方法提供了切面的状态和行为信息，并通过配置方式来指定切入点和通知实现。

切面使用<aop:aspect>标签指定，ref属性用来引用切面支持Bean。

切面支持Bean“aspectSupportBean”跟普通Bean完全一样使用，切面使用“ref”属性引用它。

### 声明切入点

切入点在Spring中也是一个Bean，Bean定义方式可以有很三种方式：

1. 在<aop:config>标签下使用<aop:pointcut>声明一个切入点Bean，该切入点可以被多个切面使用，对于需要共享使用的切入点最好使用该方式，该切入点使用id属性指定Bean名字，在通知定义时使用pointcut-ref属性通过该id引用切入点，expression属性指定切入点表达式：
    ```xml
    <aop:config>
        <aop:pointcut id="pointcut" expression="execution(* cn.javass..*.*(..))"/>
        <aop:aspect ref="aspectSupportBean">
            <aop:before pointcut-ref="pointcut" method="before"/>
        </aop:aspect>
        </aop:config>
    ```
2. 在<aop:aspect>标签下使用<aop:pointcut>声明一个切入点Bean，该切入点可以被多个切面使用，但一般该切入点只被该切面使用，当然也可以被其他切面使用，但最好不要那样使用，该切入点使用id属性指定Bean名字，在通知定义时使用pointcut-ref属性通过该id引用切入点，expression属性指定切入点表达式：
    ```xml
    <aop:config>
        <aop:aspect ref="aspectSupportBean">
            <aop:pointcut id=" pointcut" expression="execution(* cn.javass..*.*(..))"/>
            <aop:before pointcut-ref="pointcut" method="before"/>
        </aop:aspect>
    </aop:config>
    ```
3. 匿名切入点Bean，可以在声明通知时通过pointcut属性指定切入点表达式，该切入点是匿名切入点，只被该通知使用：
    ```xml
    <aop:config>
        <aop:aspect ref="aspectSupportBean">
            <aop:after pointcut="execution(* cn.javass..*.*(..))" method="afterFinallyAdvice"/>
        </aop:aspect>
    </aop:config>
    ```

### 声明通知

基于Schema方式支持前边介绍的5中通知类型：

1. 前置通知：在切入点选择的方法之前执行，通过<aop:aspect>标签下的<aop:before>标签声明：
    ```xml
    <aop:before pointcut="切入点表达式"
    pointcut-ref="切入点Bean引用"
    method="前置通知实现方法名"
    arg-names="前置通知实现方法参数列表参数名字"/>
    ```
    - pointcut和pointcut-ref：二者选一，指定切入点；
    - method：指定前置通知实现方法名，如果是多态需要加上参数类型，多个用“，”隔开，如beforeAdvice(java.lang.String)；
    - arg-names：指定通知实现方法的参数名字，多个用“,”分隔，可选，类似于构造器注入中的参数名注入限制：在class文件中没生成变量调试信息是获取不到方法参数名字的，因此只有在类没生成变量调试信息时才需要使用arg-names属性来指定参数名，如arg-names="param"表示通知实现方法的参数列表的第一个参数名字为“param”。
2. 后置返回通知：在切入点选择的方法正常返回时执行，通过<aop:aspect>标签下的<aop:after-returning>标签声明：
    ```xml
    <aop:after-returning pointcut="切入点表达式"    pointcut-ref="切入点Bean引用"
    method="后置返回通知实现方法名"
    arg-names="后置返回通知实现方法参数列表参数名字"
    returning="返回值对应的后置返回通知实现方法参数名"
    />
    ```
    - pointcut和pointcut-ref：同前置通知同义；
    - method：同前置通知同义；
    - arg-names：同前置通知同义；
    - returning：定义一个名字，该名字用于匹配通知实现方法的一个参数名，当目标方法执行正常返回后，将把目标方法返回值传给通知方法；returning限定了只有目标方法返回值匹配与通知方法相应参数类型时才能执行后置返回通知，否则不执行，对于returning对应的通知方法参数为Object类型将匹配任何目标返回值。
3. 后置异常通知：在切入点选择的方法抛出异常时执行，通过<aop:aspect>标签下的<aop:after-throwing>标签声明：
    ```xml
    <aop:after-throwing pointcut="切入点表达式"  pointcut-ref="切入点Bean引用"
    method="后置异常通知实现方法名"
    arg-names="后置异常通知实现方法参数列表参数名字"
    throwing="将抛出的异常赋值给的通知实现方法参数名"/>
    ```
    - pointcut和pointcut-ref：同前置通知同义；  
    - method：同前置通知同义；  
    - arg-names：同前置通知同义；  
    - throwing：定义一个名字，该名字用于匹配通知实现方法的一个参数名，当目标方法抛出异常返回后，将把目标方法抛出的异常传给通知方法；throwing限定了只有目标方法抛出的异常匹配与通知方法相应参数异常类型时才能执行后置异常通知，否则不执行，对于throwing对应的通知方法参数为Throwable类型将匹配任何异常。  
4. 后置最终通知：在切入点选择的方法返回时执行，不管是正常返回还是抛出异常都执行，通过<aop:aspect>标签下的<aop:after >标签声明：
    ```xml
    <aop:after pointcut="切入点表达式"  pointcut-ref="切入点Bean引用"
    method="后置最终通知实现方法名"
    arg-names="后置最终通知实现方法参数列表参数名字"/> 
    ```
    - pointcut和pointcut-ref：同前置通知同义；  
    - method：同前置通知同义；  
    - arg-names：同前置通知同义；  
5. 环绕通知：环绕着在切入点选择的连接点处的方法所执行的通知，环绕通知非常强大，可以决定目标方法是否执行，什么时候执行，执行时是否需要替换方法参数，执行完毕是否需要替换返回值，可通过<aop:aspect>标签下的<aop:around >标签声明：
    ```xml
    <aop:around pointcut="切入点表达式" 
    pointcut-ref="切入点Bean引用"
    method="后置最终通知实现方法名"
    arg-names="后置最终通知实现方法参数列表参数名字"/>
    ```
    - pointcut和pointcut-ref：同前置通知同义；  
    - method：同前置通知同义；  
    - arg-names：同前置通知同义；  
    - 环绕通知第一个参数必须是org.aspectj.lang.ProceedingJoinPoint类型，在通知实现方法内部使用ProceedingJoinPoint的proceed()方法使目标方法执行，proceed 方法可以传入可选的Object[]数组，该数组的值将被作为目标方法执行时的参数。

### 引入

Spring引入允许为目标对象引入新的接口，通过在< aop:aspect>标签内使用< aop:declare-parents>标签进行引入，定义方式如下：

```xml
<aop:declare-parents  
    types-matching="AspectJ语法类型表达式"  
    implement-interface="引入的接口"               
    default-impl="引入接口的默认实现"  
    delegate-ref="引入接口的默认实现Bean引用"/>  
```

- types-matching：匹配需要引入接口的目标对象的AspectJ语法类型表达式；  
- implement-interface：定义需要引入的接口；  
- default-impl和delegate-ref：定义引入接口的默认实现，二者选一，default-impl是接口的默认实现类全限定名，而delegate-ref是默认的实现的委托Bean名；

### Advisor

Advisor表示只有一个通知和一个切入点的切面，由于Spring AOP都是基于AOP联盟的拦截器模型的环绕通知的，所以引入Advisor来支持各种通知类型（如前置通知等5种），Advisor概念来自于Spring1.2对AOP的支持，在AspectJ中没有相应的概念对应。

Advisor可以使用<aop:config>标签下的<aop:advisor>标签定义：
```xml
<aop:advisor pointcut="切入点表达式"
pointcut-ref="切入点Bean引用"
advice-ref="通知API实现引用"/>
```
pointcut和pointcut-ref：二者选一，指定切入点表达式；  
advice-ref：引用通知API实现Bean，如前置通知接口为MethodBeforeAdvice；  

## 基于@AspectJ的AOP

Spring除了支持Schema方式配置AOP，还支持注解方式：使用@AspectJ风格的切面声明。

### 启用对@AspectJ的支持

Spring默认不支持@AspectJ风格的切面声明，为了支持需要使用如下配置：
```xml
<aop:aspectj-autoproxy />
```

### 声明切面

使用@Aspect注解进行声明：

```java
@Aspect()  
Public class Aspect{  
……  
}
```
然后将该切面在配置文件中声明为Bean后，Spring就能自动识别并进行AOP方面的配置：
```xml
<bean id="aspect" class="……Aspect" />  
```

### 声明切入点

@AspectJ风格的命名切入点使用org.aspectj.lang.annotation包下的@Pointcut+方法（方法必须是返回void类型）实现。

```java
@Pointcut(value="切入点表达式", argNames = "参数名列表")
public void pointcutName(……) {}
```

- value：指定切入点表达式；
- argNames：指定命名切入点方法参数列表参数名字，可以有多个用“,”分隔，这些参数将传递给通知方法同名的参数，同时比如切入点表达式“args(param)”将匹配参数类型为命名切入点方法同名参数指定的参数类型。  
- pointcutName：切入点名字，可以使用该名字进行引用该切入点表达式。

### 声明通知

@AspectJ风格的声明通知也支持5种通知类型：

1. 前置通知：使用org.aspectj.lang.annotation 包下的@Before注解声明；
    ```java
    @Before(value = "切入点表达式或命名切入点", argNames = "参数列表参数名")
    ```
    - value：指定切入点表达式或命名切入点；  
    - argNames：与Schema方式配置中的同义。  
2. 后置返回通知：使用org.aspectj.lang.annotation 包下的@AfterReturning注解声明；
    ```java
    @AfterReturning(  
    value="切入点表达式或命名切入点",  
    pointcut="切入点表达式或命名切入点",  
    argNames="参数列表参数名",  
    returning="返回值对应参数名") 
    ```
    - value：指定切入点表达式或命名切入点；  
    - pointcut：同样是指定切入点表达式或命名切入点，如果指定了将覆盖value属性指定的，pointcut具有高优先级；  
    - argNames：与Schema方式配置中的同义；  
    - returning：与Schema方式配置中的同义。  
3. 后置异常通知：使用org.aspectj.lang.annotation 包下的@AfterThrowing注解声明；
    ```java
    @AfterThrowing (  
    value="切入点表达式或命名切入点",  
    pointcut="切入点表达式或命名切入点",  
    argNames="参数列表参数名",  
    throwing="异常对应参数名")
    ```
    - value：指定切入点表达式或命名切入点；  
    - pointcut：同样是指定切入点表达式或命名切入点，如果指定了将覆盖value属性指定的，pointcut具有高优先级；  
    - argNames：与Schema方式配置中的同义；  
    - throwing：与Schema方式配置中的同义。
4. 后置最终通知：使用org.aspectj.lang.annotation 包下的@After注解声明；
    ```java
    @After (  
    value="切入点表达式或命名切入点",  
    argNames="参数列表参数名")
    ```
    - value：指定切入点表达式或命名切入点；  
    - argNames：与Schema方式配置中的同义；  
5. 环绕通知：使用org.aspectj.lang.annotation 包下的@Around注解声明；
    ```java
    @Around (  
    value="切入点表达式或命名切入点",  
    argNames="参数列表参数名") 
    ```
    - value：指定切入点表达式或命名切入点；  
    - argNames：与Schema方式配置中的同义；  

### 引入

@AspectJ风格的引入声明在切面中使用org.aspectj.lang.annotation包下的@DeclareParents声明：

```java
@DeclareParents(  
value="AspectJ语法类型表达式",  
defaultImpl=引入接口的默认实现类)  
private Interface interface; 
```

- value：匹配需要引入接口的目标对象的AspectJ语法类型表达式；与Schema方式中的types-matching属性同义；  
- private Interface interface：指定需要引入的接口；  
- defaultImpl：指定引入接口的默认实现类，没有与Schema方式中的delegate-ref属性同义的定义方式；

## AspectJ切入点语法

### Spring AOP支持的AspectJ切入点指示符

切入点指示符用来指示切入点表达式目的，在Spring AOP中目前只有执行方法这一个连接点，Spring AOP支持的AspectJ切入点指示符如下：  

execution：用于匹配方法执行的连接点；  
within：用于匹配指定类型内的方法执行；  
this：用于匹配当前AOP代理对象类型的执行方法；注意是AOP代理对象的类型匹配，这样就可能包括引入接口也类型匹配；  
target：用于匹配当前目标对象类型的执行方法；注意是目标对象的类型匹配，这样就不包括引入接口也类型匹配；  
args：用于匹配当前执行的方法传入的参数为指定类型的执行方法；  
@within：用于匹配所以持有指定注解类型内的方法；  
@target：用于匹配当前目标对象类型的执行方法，其中目标对象持有指定的注解；  
@args：用于匹配当前执行的方法传入的参数持有指定注解的执行；  
@annotation：用于匹配当前执行方法持有指定注解的方法；  
bean：Spring AOP扩展的，AspectJ没有对于指示符，用于匹配特定名称的Bean对象的执行方法；  
reference pointcut：表示引用其他命名切入点，只有@ApectJ风格支持，Schema风格不支持。  

AspectJ切入点支持的切入点指示符还有： call、get、set、preinitialization、staticinitialization、initialization、handler、adviceexecution、withincode、cflow、cflowbelow、if、@this、@withincode；但Spring AOP目前不支持这些指示符，使用这些指示符将抛出IllegalArgumentException异常。这些指示符Spring AOP可能会在以后进行扩展。

#### execution

格式：execution(modifiers-pattern? ret-type-pattern declaring-type-pattern? name-pattern(param-pattern)throws-pattern?)

- modifiers-pattern：修饰器匹配模式
- ret-type-pattern:可以为*表示任何返回值,全路径的类名等。
- name-pattern:指定方法名,\*代表所有,set\*,代表以set开头的所有方法。
- parameters pattern:指定方法参数(声明的类型),(..)代表所有参数,(\*)代表一个参数,(\*,String)代表第一个参数为任何值,第二个为String类型.

returning type pattern,name pattern, and parameters pattern是必须的.

**注：** "?"为正则表达式，最多只可以出现一次（0次或1次）

例子：

- 任意公共方法的执行：execution(public * *(..))
- 任何一个以“set”开始的方法的执行：execution(* set*(..))
- AccountService 接口的任意方法的执行：execution(* com.xyz.service.AccountService.*(..))
- 定义在service包里的任意方法的执行：execution(* com.xyz.service.*.*(..))
- 定义在service包和所有子包里的任意类的任意方法的执行：execution(* com.xyz.service..*.*(..))
- 定义在pointcutexp包和所有子包里的JoinPointObjP2类的任意方法的执行：execution(* com.test.spring.aop.pointcutexp..JoinPointObjP2.*(..))")

**注：** 最靠近(..)的为方法名,靠近.\*(..))的为类名或者接口名

### 命名及匿名切入点

命名切入点可以被其他切入点引用，而匿名切入点是不可以的。

只有@AspectJ支持命名切入点，而Schema风格不支持命名切入点。

如下所示，@AspectJ使用如下方式引用命名切入点：

![](http://ozchbp0v3.bkt.clouddn.com/Spring_AOP4.JPG)

### 类型匹配语法

AspectJ类型匹配的通配符：

- * ：匹配任何数量字符；
- .. ：（两个点）匹配任何数量字符的重复，如在类型模式中匹配任何数量子包；而在方法参数模式中匹配任何数量参数。
- + ：匹配指定类型的子类型；仅能作为后缀放在类型模式后边。

```
java.lang.String    匹配String类型；  
java.*.String       匹配java包下的任何“一级子包”下的String类型；  
                    如匹配java.lang.String，但不匹配java.lang.ss.String  
java..*             匹配java包及任何子包下的任何类型;  
                    如匹配java.lang.String、java.lang.annotation.Annotation  
java.lang.*ing      匹配任何java.lang包下的以ing结尾的类型；  
java.lang.Number+   匹配java.lang包下的任何Number的自类型；  
                    如匹配java.lang.Integer，也匹配java.math.BigInteger
```

### 组合切入点表达式

AspectJ使用 且（&&）、或（||）、非（!）来组合切入点表达式。

在Schema风格下，由于在XML中使用“&&”需要使用转义字符“&amp;&amp;”来代替之，所以很不方便，因此Spring ASP 提供了and、or、not来代替&&、||、!。

### 切入点使用示例

参考地址：<http://jinnianshilongnian.iteye.com/blog/1420691>

## 通知参数

两种获取通知参数的方式：

- 使用JoinPoint获取：Spring AOP提供使用org.aspectj.lang.JoinPoint类型获取连接点数据，任何通知方法的第一个参数都可以是JoinPoint(环绕通知是ProceedingJoinPoint，JoinPoint子类)，当然第一个参数位置也可以是JoinPoint.StaticPart类型，这个只返回连接点的静态部分。
    1. JoinPoint：提供访问当前被通知方法的目标对象、代理对象、方法参数等数据：
    ```java
    package org.aspectj.lang;  
    import org.aspectj.lang.reflect.SourceLocation;  
    public interface JoinPoint {  
        String toString();         //连接点所在位置的相关信息  
        String toShortString();     //连接点所在位置的简短相关信息  
        String toLongString();     //连接点所在位置的全部相关信息  
        Object getThis();         //返回AOP代理对象  
        Object getTarget();       //返回目标对象  
        Object[] getArgs();       //返回被通知方法参数列表  
        Signature getSignature();  //返回当前连接点签名  
        SourceLocation getSourceLocation();//返回连接点方法所在类文件中的位置  
        String getKind();        //连接点类型  
        StaticPart getStaticPart(); //返回连接点静态部分  
    }  
    ```
    2. ProceedingJoinPoint：用于环绕通知，使用proceed()方法来执行目标方法：
    ```java
    public interface ProceedingJoinPoint extends JoinPoint {  
        public Object proceed() throws Throwable;  
        public Object proceed(Object[] args) throws Throwable;  
    } 
    ```
    3. JoinPoint.StaticPart：提供访问连接点的静态部分，如被通知方法签名、连接点类型等：
    ```java
    public interface StaticPart {  
        Signature getSignature();    //返回当前连接点签名  
        String getKind();          //连接点类型  
        int getId();               //唯一标识  
        String toString();         //连接点所在位置的相关信息  
        String toShortString();     //连接点所在位置的简短相关信息  
        String toLongString();     //连接点所在位置的全部相关信息  
    }  
    ```

- 自动获取：通过切入点表达式可以将相应的参数自动传递给通知方法.在Spring AOP中，除了execution和bean指示符不能传递参数给通知方法，其他指示符都可以将匹配的相应参数或对象自动传递给通知方法。

## F&Q

1. 定义了切点，执行方法本应进入通知的，但没有进入？

>Spring AOP要想进入通知，所执行方法的类必须是被Spring IoC容器管理创建。