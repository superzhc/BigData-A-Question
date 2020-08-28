# 动态注入Bean

- 通过`applicationContext`上下文获取`BeanFactory`，有如下两种方式：
1. `DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getParentBeanFactory();`
2. `DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();`

对于第2种方式一般用于存储Spring容器管理之外的Bean，Spring内部很少使用，应用有些情况通过1拿到的beanFactory是null，特别是在应用第三方框架时，dubbo中遇到过。

- 注入一个新的Bean到Spring容器中：

```java
//根据obj的类型、创建一个新的bean、添加到Spring容器中，
//注意BeanDefinition有不同的实现类，注意不同实现类应用的场景 TODO
BeanDefinition beanDefinition = new GenericBeanDefinition();
beanDefinition.setBeanClassName(obj.getClass().getName());
beanFactory.registerBeanDefinition(obj.getClass().getName(), beanDefinition);
```

注意此时放入到Spring容器的Bean并非是单例模式的，可以有重复的bean，取值的时只能通过obj.getClass().getName()获得Bean而不能通过类型获得Bean。这是需要注意的地方。此种方式除了不是单例以外，跟Spring加载一个bean的流程基本一致。包括AOP的逻辑处理

- 将已有的Bean注入到容器中

```java
// 让obj完成Spring初始化过程中所有增强器检验，只是不重新创建obj
applicationContext.getAutowireCapableBeanFactory().applyBeanPostProcessorsAfterInitialization(obj, obj.getClass().getName());
// 将obj以单例的形式入驻到容器中，此时通过obj.getClass().getName()或obj.getClass()都可以拿到放入Spring容器的Bean
beanFactory.registerSingleton(obj.getClass().getName(), obj);
```

## Epoint新增后台代码实现不启动动态注入容器功能

注：7.1.30.2可行

```java
String restControllerName = "tlcQJYKaiPingBiaoNewAction";
Class<?> beanClass = TLCQJYKaiPingBiaoNewAction.class;
ApplicationContext applicationContext = ((SpringContextUtil) ContainerFactory.getContainInfo()
        .getComponent(SpringContextUtil.class)).getApplicationContext();
DefaultListableBeanFactory beanFactory = null;
BeanFactory orignBeanfactory = applicationContext.getParentBeanFactory();
if (orignBeanfactory != null && orignBeanfactory instanceof DefaultListableBeanFactory) {
    beanFactory = (DefaultListableBeanFactory) orignBeanfactory;
}
if (null == beanFactory) {
    beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
}
if (!beanFactory.containsBean(restControllerName)) {
    // 注入Bean到Spring容器
    BeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClassName(beanClass.getName());
    beanDefinition.setScope("request");
    beanFactory.registerBeanDefinition(restControllerName, beanDefinition);// 此处是公司的bean名称（restfulcontroller注解上的内容）
}
```