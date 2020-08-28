# IoC/DI

**IoC:Inversion of Control** [控制反转]

**DI:Dependency Injection** [依赖注入]

## 简介

IoC容器：最主要是完成 **对象的创建** 和 **依赖的管理注入** 等等。

所谓控制反转，就是把原先我们代码里面需要实现的对象创建、依赖的代码，反转给容器来帮忙实现。那么必然的我们需要创建一个容器，同时需要一种描述来让容器知道需要创建的对象与对象的关系。这个描述最具体表现就是我们可配置的文件。

对象和对象关系的表示可以用 `xml`,`properties` 文件等语义化配置文件表示。

描述对象关系的文件存放的地方可能是 `classpath`,`filesystem`,`URL 网络资源`,`servletContext` 等。

## IoC 体系结构

### BeanFactory

Spring Bean的创建是典型的**工厂模式**，其中 `BeanFactory` 作为最顶层的一个接口类，它定义了IOC容器的基本功能规范，BeanFactory 这个容器接口在`org.springframework.beans.factory.BeanFactor` 中被定义 ，BeanFactory 有三个子类：`ListableBeanFactory`、`HierarchicalBeanFactory` 和`AutowireCapableBeanFactory`。

```java
public interface BeanFactory {
    //对FactoryBean的转义定义，因为如果使用bean的名字检索FactoryBean得到的对象是工厂生成的对象，
    //如果需要得到工厂本身，需要转义
    String FACTORY_BEAN_PREFIX = "&";

    //根据bean的名字，获取在IOC容器中得到bean实例
    Object getBean(String name) throws BeansException;

    //根据bean的名字和Class类型来得到bean实例，增加了类型安全验证机制。
    Object getBean(String name, Class requiredType) throws BeansException;

    //提供对bean的检索，看看是否在IOC容器有这个名字的bean
    boolean containsBean(String name);

    //根据bean名字得到bean实例，并同时判断这个bean是不是单例
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

    //得到bean实例的Class类型
    Class getType(String name) throws NoSuchBeanDefinitionException;

    //得到bean的别名，如果根据别名检索，那么其原名也会被检索出来
    String[] getAliases(String name);
}
```

在BeanFactory里只对IOC容器的基本行为作了定义，根本不关心bean是如何定义怎样加载的。正如我们只关心工厂里得到什么的产品对象，至于工厂是怎么生产这些对象的，这个基本的接口不关心。而要知道工厂是如何产生对象的，我们需要看具体的IoC容器实现， **spring提供了许多IOC容器的实现，比如`XmlBeanFactory`,`ClasspathXmlApplicationContext`,`ApplicationContext`等**。其中`XmlBeanFactory` 就是针对最基本的IoC容器的实现，这个IOC容器可以读取XML文件定义的`BeanDefinition`（XML文件中对bean的描述）。

`ApplicationContext` 是Spring提供的一个高级的IoC容器，它除了能够提供IoC容器的基本功能外，还为用户提供了以下的附加服务。

从`ApplicationContext`接口的实现，我们看出其特点：

1. 支持信息源，可以实现国际化。（实现`MessageSource`接口）
2. 访问资源。(实现`ResourcePatternResolver`接口)
3. 支持应用事件。(实现`ApplicationEventPublisher`接口)

### BeanDefinition

Spring IOC容器管理定义的各种Bean对象及其相互的关系，Bean对象在Spring实现中是以`BeanDefinition`来描述的。

## IoC 容器初始化

IoC容器的初始化包括`BeanDefinition`的Resource**定位**、**载入**和**注册**这三个基本的过程。

## IoC 容器的依赖注入

### 依赖注入发生的时间

当Spring IoC容器完成了Bean定义资源的定位、载入和解析注册以后，IoC容器中已经管理类Bean定义的相关数据，但是此时IoC容器还没有对所管理的Bean进行依赖注入，依赖注入在以下两种情况发生：

- 用户第一次通过getBean方法向IoC容器索要Bean时，IoC容器触发依赖注入。
- 当用户在Bean定义资源中为`<Bean>`元素配置了`lazy-init`属性，即让容器在解析注册Bean定义时进行预实例化，触发依赖注入。