# AOP

Spring 框架的一个关键组件是**面向方面的编程**(AOP)框架。

## AOP 术语

|       术语        | 描述                                                                                                                               |
| :---------------: | ---------------------------------------------------------------------------------------------------------------------------------- |
|      Aspect       | 一个模块具有一组提供横切需求的 APIs。例如，一个日志模块为了记录日志将被 AOP 方面调用。应用程序可以拥有任意数量的方面，这取决于需求 |
|    Join point     | 在应用程序中它代表一个点，可以在这个点上插入 AOP 方面。也就是说，在实际的应用程序中，其中一个操作将使用 Spring AOP 框架            |
|      Advice       | 这是实际行动之前或之后执行的方法。这是在程序执行期间通过 Spring AOP 框架实际被调用的代码                                           |
| [Pointcut](#切点) | 这是一组一个或多个连接点，通知应该被执行。可以使用表达式或模式指定切入点                                                           |
|   Introduction    | 引用允许添加新方法或属性到现有的类中                                                                                               |
|   Target Object   | 被一个或多个方面所通知的对象，这个对象永远是一个被代理对象，也称为被通知对象                                                       |
|      Weaving      | Weaving 把方面连接到其它的应用程序类型或者对象上，并创建一个被通知的对象，这些可以在编译时，类加载时和运行时完成                   |

## 切点

1. 在定义过滤切入点函数时，直接把**`execution`**已定义匹配表达式作为值传递给通知类型
2. 使用**`@Pointcut`**注解定义切入点表达式

### 切入点指示符

为了方法通知应用到相应过滤的目标方法上，Spring AOP 提供了匹配表达式，这些表达式也叫**切入点指示符**。

#### 通配符

在定义匹配表达式时，通配符几乎随处可见，如`*`、`..` 、`+` ，它们的含义如下：

- **`..`** 

匹配方法定义中的任意数量的参数，此外还匹配类定义中的任意数量包。

```java
//任意返回值，任意名称，任意参数的公共方法
execution(public * *(..))
//匹配com.zejian.dao包及其子包中所有类中的所有方法
within(com.zejian.dao..*)
```

- **`+`**
 
匹配给定类的任意子类。

```java
//匹配实现了DaoUser接口的所有子类的方法
within(com.zejian.dao.DaoUser+)
```

- **`*`**

匹配任意数量的字符。

```java
//匹配com.zejian.service包及其子包中所有类的所有方法
within(com.zejian.service..*)
//匹配以set开头，参数为int类型，任意返回值的方法
execution(* set*(int))
```

#### 类型签名表达式

为了方便类型（如接口、类名、包名）过滤方法，Spring AOP 提供了`within`关键字。其语法格式如下：

```java
within(<type name>)
```

`type name` 则使用包名或者类名替换即可,示例如下：

```java
//匹配com.zejian.dao包及其子包中所有类中的所有方法
@Pointcut("within(com.zejian.dao..*)")

//匹配UserDaoImpl类中所有方法
@Pointcut("within(com.zejian.dao.UserDaoImpl)")

//匹配UserDaoImpl类及其子类中所有方法
@Pointcut("within(com.zejian.dao.UserDaoImpl+)")

//匹配所有实现UserDao接口的类的所有方法
@Pointcut("within(com.zejian.dao.UserDao+)")
```

#### 方法签名表达式

如果想根据方法签名进行过滤，可以使用关键字`execution`，语法表达式如下

```java
//scope ：方法作用域，如public,private,protect
//returnt-type：方法返回值类型
//fully-qualified-class-name：方法所在类的完全限定名称
//parameters 方法参数
execution(<scope> <return-type> <fully-qualified-class-name>.*(parameters))
```

对于给定的作用域、返回值类型、完全限定类名以及参数匹配的方法将会应用切点函数指定的通知，示例如下：

```java
//匹配UserDaoImpl类中的所有方法
@Pointcut("execution(* com.zejian.dao.UserDaoImpl.*(..))")

//匹配UserDaoImpl类中的所有公共的方法
@Pointcut("execution(public * com.zejian.dao.UserDaoImpl.*(..))")

//匹配UserDaoImpl类中的所有公共方法并且返回值为int类型
@Pointcut("execution(public int com.zejian.dao.UserDaoImpl.*(..))")

//匹配UserDaoImpl类中第一个参数为int类型的所有公共的方法
@Pointcut("execution(public * com.zejian.dao.UserDaoImpl.*(int , ..))")
```

#### 其他指示符

- **`bean`**：Spring AOP扩展的，AspectJ没有此指示符，用于匹配特定名称的 Bean 对象的执行方法；
    ```java
    //匹配名称中带有后缀Service的Bean。
    @Pointcut("bean(*Service)")
    private void myPointcut1(){}
    ```
- **`this`** ：用于匹配当前AOP代理对象类型的执行方法；请注意是AOP代理对象的类型匹配，这样就可能包括引入接口也类型匹配
    ```java
    //匹配了任意实现了UserDao接口的代理对象的方法进行过滤
    @Pointcut("this(com.zejian.spring.springAop.dao.UserDao)")
    private void myPointcut2(){}
    ```
- **`target`** ：用于匹配当前目标对象类型的执行方法；
    ```java
    //匹配了任意实现了UserDao接口的目标对象的方法进行过滤
    @Pointcut("target(com.zejian.spring.springAop.dao.UserDao)")
    private void myPointcut3(){}
    ```
- **`@within`**：用于匹配所以持有指定注解类型内的方法；请注意与within是有区别的， within是用于匹配指定类型内的方法执行；
    ```java
    //匹配使用了MarkerAnnotation注解的类(注意是类)
    @Pointcut("@within(com.zejian.spring.annotation.MarkerAnnotation)")
    private void myPointcut4(){}
    ```
- **`@annotation(com.zejian.spring.MarkerMethodAnnotation)`** : 根据所应用的注解进行方法过滤
    ```java
    //匹配使用了MarkerAnnotation注解的方法(注意是方法)
    @Pointcut("@annotation(com.zejian.spring.annotation.MarkerAnnotation)")
    private void myPointcut5(){}
    ```

注：<font color="red">切点指示符可以使用运算符语法进行表达式的混编，如`and`、`or`、`not`（或者`&&`、`||`、`！`）</font>

## 通知函数

Spring 方面可以使用以下五种通知类型：

|      通知      | 描述                                                       |
| :------------: | ---------------------------------------------------------- |
|    前置通知    | 在一个方法执行之前，执行通知                               |
|    后置通知    | 在一个方法执行之后，不考虑其结果，执行通知                 |
|   返回后通知   | 在一个方法执行之后，只有在方法成功完成时，才能执行通知     |
| 抛出异常后通知 | 在一个方法执行之后，只有在方法退出抛出异常时，才能执行通知 |
|    环绕通知    | 在方法调用之前之后，执行通知                               |

### 前置通知 `@Before`

前置通知通过`@Before`注解进行标注，并可直接传入切点表达式的值，该通知在目标函数执行前执行，注意JoinPoint，是Spring提供的静态变量，通过joinPoint参数，可以获取目标对象的信息,如类名称,方法参数,方法名称等，该参数是可选的。

```java
/**
 * 前置通知
 * @param joinPoint 该参数可以获取目标对象的信息,如类名称,方法参数,方法名称等
 */
@Before("execution(* com.zejian.spring.springAop.dao.UserDao.addUser(..))")
public void before(JoinPoint joinPoint){
    System.out.println("我是前置通知");
}
```

### 后置通知 `@AfterReturning`

通过@AfterReturning注解进行标注，该函数在目标函数执行完成后执行，并可以获取到目标函数最终的返回值returnVal，当目标函数没有返回值时，returnVal将返回null，必须通过returning = “returnVal”注明参数的名称而且必须与通知函数的参数名称相同。请注意，在任何通知中这些参数都是可选的，需要使用时直接填写即可，不需要使用时，可以完成不用声明出来。如下:

```java
/**
* 后置通知，不需要参数时可以不提供
*/
@AfterReturning(value="execution(* com.zejian.spring.springAop.dao.UserDao.*User(..))")
public void AfterReturning(){
   System.out.println("我是后置通知...");
}

/**
* 后置通知
* returnVal,切点方法执行后的返回值
*/
@AfterReturning(value="execution(* com.zejian.spring.springAop.dao.UserDao.*User(..))",returning = "returnVal")
public void AfterReturning(JoinPoint joinPoint,Object returnVal){
   System.out.println("我是后置通知...returnVal+"+returnVal);
}
```

### 异常通知 `@AfterThrowing`

该通知只有在异常时才会被触发，并由throwing来声明一个接收异常信息的变量，同样异常通知也用于Joinpoint参数，需要时加上即可，如下：

```java
/**
* 抛出通知
* @param e 抛出异常的信息
*/
@AfterThrowing(value="execution(* com.zejian.spring.springAop.dao.UserDao.addUser(..))",throwing = "e")
public void afterThrowable(Throwable e){
  System.out.println("出现异常:msg="+e.getMessage());
}
```

### 最终通知 `@After`

该通知有点类似于finally代码块，只要应用了无论什么情况下都会执行。

```java
/**
 * 无论什么情况下都会执行的方法
 * joinPoint 参数
 */
@After("execution(* com.zejian.spring.springAop.dao.UserDao.*User(..))")
public void after(JoinPoint joinPoint) {
    System.out.println("最终通知....");
}
```

### 环绕通知 `@Around`

环绕通知既可以在目标方法前执行也可在目标方法之后执行，更重要的是环绕通知可以控制目标方法是否指向执行，但即使如此，我们应该尽量以最简单的方式满足需求，在仅需在目标方法前执行时，应该采用前置通知而非环绕通知。案例代码如下第一个参数必须是ProceedingJoinPoint，通过该对象的proceed()方法来执行目标函数，proceed()的返回值就是环绕通知的返回值。同样的，ProceedingJoinPoint对象也是可以获取目标对象的信息,如类名称,方法参数,方法名称等等。

```java
@Around("execution(* com.zejian.spring.springAop.dao.UserDao.*User(..))")
public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("我是环绕通知前....");
    //执行目标函数
    Object obj= (Object) joinPoint.proceed();
    System.out.println("我是环绕通知后....");
    return obj;
}
```

## 通知传递参数

### 通过 JoinPoint 获取

Spring AOP提供使用`org.aspectj.lang.JoinPoint`类型获取连接点数据，任何通知方法的第一个参数都可以是`JoinPoint`(环绕通知是`ProceedingJoinPoint`，JoinPoint子类)，当然第一个参数位置也可以是`JoinPoint.StaticPart`类型，这个只返回连接点的静态部分。

- **`JoinPoint`**:提供访问当前被通知方法的目标对象、代理对象、方法参数等数据
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
- **`ProceedingJoinPoint`**:用于环绕通知，使用proceed()方法来执行目标方法
    ```java
    public interface ProceedingJoinPoint extends JoinPoint {
        public Object proceed() throws Throwable;
        public Object proceed(Object[] args) throws Throwable;
    }
    ```
- **`JoinPoint.StaticPart`**:提供访问连接点的静态部分，如被通知方法签名、连接点类型等
    ```java
    public interface StaticPart {
        Signature getSignature();    //返回当前连接点签名
        String getKind();          //连接点类型
        int getId();               //唯一标识
        String toString();         //连接点所在位置的相关信息
        String toShortString();     //连接点所在位置的简短相关信息
        String toLongString();     //连接点所在位置的全部相关信息
    }
    ```

### 通过切入点表达式将相应的参数自动传递给通知方法

在Spring AOP中，除了`execution`和`bean`指示符不能传递参数给通知方法，其他指示符都可以将匹配的方法相应参数或对象自动传递给通知方法。获取到匹配的方法参数后通过**`argNames`**属性指定参数名。如下，需要注意的是`args(指示符)`、`argNames`的参数名与`before()`方法中参数名 必须保持一致即`param`。

```java
@Before(value="args(param)", argNames="param") //明确指定了
public void before(int param) {
    System.out.println("param:" + param);
}  
```

当然也可以直接使用`args指示符`不带`argNames`声明参数，如下：

```java
@Before("execution(public * com.zejian..*.addUser(..)) && args(userId,..)")  
public void before(int userId) {  
    //调用addUser的方法时如果与addUser的参数匹配则会传递进来会传递进来
    System.out.println("userId:" + userId);  
}  
```

`args(userId,..)`该表达式会保证只匹配那些至少接收一个参数而且传入的类型必须与userId一致的方法，记住传递的参数可以简单类型或者对象，而且只有参数和目标方法也匹配时才有会有值传递进来。

## 优先级

在不同的切面中，如果有多个通知需要在同一个切点函数指定的过滤目标方法上执行，那些在目标方法前执行(“进入”)的通知函数，最高优先级的通知将会先执行，在执行在目标方法后执行(“退出”)的通知函数，最高优先级会最后执行。而对于在同一个切面定义的通知函数将会根据在类中的声明顺序执行。

如果在不同的切面中定义多个通知响应同一个切点，进入时则优先级高的切面类中的通知函数优先执行，退出时则最后执行，通过实现 **`org.springframework.core.Ordered` 接口**，该接口用于控制切面类的优先级，同时重写 **`getOrder()`方法**，定制返回值，返回值(int 类型)越小优先级越大。

## 实现自定义方面

Spring 支持 `@AspectJ annotation style` 的方法和**基于配置模式**的方法来实现自定义方面。

| 方法             | 描述                                                                  |
| ---------------- | --------------------------------------------------------------------- |
| XML Schema based | 方面是使用常规类以及基于配置的 XML 来实现的                           |
| @AspectJ based   | @AspectJ 引用一种声明方面的风格作为带有 Java 5 注解的常规 Java 类注解 |

### XML模式

需要引入 aop 命名空间标签。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd 
    http://www.springframework.org/schema/aop 
    http://www.springframework.org/schema/aop/spring-aop-3.0.xsd ">

    <!-- bean definition & AOP specific configuration -->
    <bean id="aspectBeanDemo" class="com.superz.demo.AspectBeanDemo"></bean>

    <!--一个 aspect 是使用<aop:aspect>元素声明的，支持的 bean 是使用 ref 属性引用的-->
    <aop:config>
        <aop:aspect id="aspectDemo" ref="aspectBeanDemo">
            <!-- 一个切点可以确定使用不同的通知执行，切点定义如下： -->
            <aop:pointcut id="pointDemo" expression="execution(* com.superz.demo.point..*(..)"/>

            <!-- 声明通知，使用<aop:{ADVICE NAME}>元素在一个aspect中声明五个通知类型中的任何一个 -->
            <aop:before pointcut-ref="pointDemo" method="doRequiredTask"/>
            <aop:after pointcut-ref="pointDemo" method="doRequiredTask"/>
            <aop:after-returning pointcut-ref="pointDemo" returning="retVal" method="doRequiredTask"/>
            <aop:after-throwing pointcut-ref="pointDemo" throwing="ex" method="doRequiredTask"/>
            <aop:around pointcut-ref="pointDemo" method="doRequiredTask"/>
        </aop:aspect>
    </aop:config>
</beans>
```

### `@AspectJ`注解

从 Java 5 开始 `@AspectJ`注解通过标注普通的 Java 类来声明 aspect，这也是一种 aspect 声明的风格。

<font color="red">需要在 XML 文件中配置 `<aop:aspectj-autoproxy>`、`@AspectJ`才可用</font>

[AspectJ的介绍](Java/Spring/AOP和设备支持/Aspect.md)