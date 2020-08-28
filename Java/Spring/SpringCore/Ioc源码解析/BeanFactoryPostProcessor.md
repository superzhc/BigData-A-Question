# BeanFactoryPostProcessor

BeanFactoryPostProcessort为Spring容器启动阶段，提供了一种容器扩展机制，它允许在容器实例化Bean之前对注册到该容器的BeanDefinition做出修改。

BeanFactoryPostProcess的机制提供了bean实例化之前最后一次修改BeanDefinition。

定义如下：

```java
public interface BeanFactoryPostProcessor {

/**
* 1、Modify the application context's internal bean factory after its standard initialization.
*  
* 2、All bean definitions will have been loaded, but no beans will have been instantiated yet. This allows for overriding or adding properties even to eager-initializing beans.
*
* @param beanFactory the bean factory used by the application context
* @throws org.springframework.beans.BeansException in case of errors
*/
void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
```

BeanFactoryPostProcessor 接口仅有一个 postProcessBeanFactory 方法，该方法接收一个 ConfigurableListableBeanFactory 类型的 beanFactory 参数。上面有两行注释：
1. 表示了该方法的作用：在 standard initialization（实在是不知道这个怎么翻译：标准初始化？） 之后（已经就是已经完成了 BeanDefinition 的加载）对 bean factory 容器进行修改。其中参数 beanFactory 应该就是已经完成了 standard initialization 的 BeanFactory。
2. 表示作用时机：所有的 BeanDefinition 已经完成了加载即加载至 BeanFactory 中，但是还没有完成初始化。

`postProcessBeanFactory()` 工作与 BeanDefinition 加载完成之后，Bean 实例化之前，其主要作用是对加载 BeanDefinition 进行修改。

注：在 `postProcessBeanFactory()` 中千万不能进行 Bean 的实例化工作，因为这样会导致 bean 过早实例化，会产生严重后果，始终需要注意的是 BeanFactoryPostProcessor 是与 BeanDefinition 打交道的，如果想要与 Bean 打交道，请使用 BeanPostProcessor。

与 BeanPostProcessor 一样，BeanFactoryPostProcessor 同样支持排序，一个容器可以同时拥有多个 BeanFactoryPostProcessor，如果需要排序的话，可以实现 Ordered 接口。

与 BeanPostProcessor 一样，对于一般 BeanFactory，需要容器主动去进行注册调用，ApplicationContext会自动识别配置文件中BeanFactoryPostProcessor并且完成注册和调用。

