# Aware接口

Aware 接口定义如下：

```java
/**
 * Marker superinterface indicating that a bean is eligible to be
 * notified by the Spring container of a particular framework object
 * through a callback-style method. Actual method signature is
 * determined by individual subinterfaces, but should typically
 * consist of just one void-returning method that accepts a single
 * argument.
 *
 * <p>Note that merely implementing {@link Aware} provides no default
 * functionality. Rather, processing must be done explicitly, for example
 * in a {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}.
 * Refer to {@link org.springframework.context.support.ApplicationContextAwareProcessor}
 * and {@link org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory}
 * for examples of processing {@code *Aware} interface callbacks.
 *
 * @author Chris Beams
 * @since 3.1
 */
public interface Aware {

}
```

Aware接口为Spring容器的核心接口，是一个具有标识作用的超级接口，实现了该接口的bean是具有被Spring容器通知的能力，通知的方式是采用回调方式。

**Aware接口是一个空接口，实际的方法签名由各个子接口来确定，且该接口通常只会有一个接收单参数的set方法，该set方法的命名为`set+去掉接口名中的Aware后缀`，即XxxAware接口，则方法定义为`setXxx()`**，例如`BeanNameAware(setBeanName)`、`ApplicationContextAware(setApplicationContext)`

Aware的子接口需要提供一个`setXxx`方法，因set是设置属性值的方法，即Aware类接口的`setXxx`方法其实就是设置xxx属性的。

Spring容器设置属性是通过调用方法`invokeAwareMethods()`来实现的，代码如下：

```java
private void invokeAwareMethods(final String beanName, final Object bean) {
    if (bean instanceof Aware) {
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if (bean instanceof BeanClassLoaderAware) {
            ClassLoader bcl = getBeanClassLoader();
            if (bcl != null) {
                ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
            }
        }
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
        }
    }
}
```

首先判断 bean 实例是否属于 Aware 接口的范畴，如果是的话，则调用实例的 `setXxx()` 方法给实例设置 xxx 属性值，在 `invokeAwareMethods()` 方法主要是设置 beanName，beanClassLoader、BeanFactory 中三个属性值。

Spring提供了一系列的Aware接口，如下图（部分）：

![](./images/AwareImpl.png)

```java
public interface BeanClassLoaderAware extends Aware {
    /**
    * 将 BeanClassLoader 提供给 bean 实例回调
    * 在 bean 属性填充之后、初始化回调之前回调，
    * 例如InitializingBean的InitializingBean.afterPropertiesSet()方法或自定义init方法
    */
    void setBeanClassLoader(ClassLoader classLoader);
}

public interface BeanFactoryAware extends Aware {
    /**
    * 将 BeanFactory 提供给 bean 实例回调
    * 调用时机和 setBeanClassLoader 一样
    */
    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}

public interface BeanNameAware extends Aware {
    /**
    * 在创建此 bean 的 bean工厂中设置 beanName
    */
    void setBeanName(String name);
}

public interface ApplicationContextAware extends Aware {
    /**
    * 设置此 bean 对象的 ApplicationContext，通常，该方法用于初始化对象
    */
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
```

Spring容器在初始化主动检测当前bean是否实现了Aware接口，如果实现了则回调其set方法将相应的参数设置给该bean，这个时候该bean就从Spring容器中取得相应的资源。

部分常用的Aware子接口，便于日后查询：

- `LoadTimeWeaverAware`：加载Spring Bean时织入第三方模块，如AspectJ
- `BeanClassLoaderAware`：加载Spring Bean的类加载器
- `BootstrapContextAware`：资源适配器BootstrapContext，如JCA,CCI
- `ResourceLoaderAware`：底层访问资源的加载器
- `BeanFactoryAware`：声明BeanFactory
- `PortletConfigAware`：PortletConfig
- `PortletContextAware`：PortletContext
- `ServletConfigAware`：ServletConfig
- `ServletContextAware`：ServletContext
- `MessageSourceAware`：国际化
- `ApplicationEventPublisherAware`：应用事件
- `NotificationPublisherAware`：JMX通知
- `BeanNameAware`：声明Spring Bean的名字