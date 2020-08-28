# BeanPostProcessor

Spring 作为优秀的开源框架，它提供了丰富的可扩展点：
- Aware 接口
- BeanPostProcessor

> BeanPostProcessor 的作用：在 Bean 完成实例化后，如果需要对其进行一些配置、增加一些自己的处理逻辑，那么请使用 BeanPostProcessor。

## 基本原理

BeanPostProcessor 接口定义如下：

```java
public interface BeanPostProcessor {
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

`postProcessBeforeInitialization()` 和 `postProcessAfterInitialization()` 两个方法都接收一个 Object 类型的 bean，一个 String 类型的 beanName，其中 bean 是已经实例化了的 instanceBean。这两个方法是初始化bean的前后置处理器，它们应用 `invokeInitMethods()`前后。如下图：

![](./images/BeanPostProcessor.png)

**BeanPostProcessor 可以理解为是 Spring 的一个工厂钩子**（其实 Spring 提供一系列的钩子，如 **Aware** 、**InitializingBean**、**DisposableBean**），**它是 Spring 提供的对象实例化阶段强有力的扩展点，允许 Spring 在实例化 bean 阶段对其进行定制化修改**，比较常见的使用场景是**处理标记接口实现类**或者**为当前对象提供代理实现（例如AOP）**。

一般普通的 BeanFactory 是不支持自动注册 BeanPostProcessor 的，需要手动调用 `addBeanPostProcessor()` 进行注册，注册后的 BeanPostProcessor 适用于所有该 BeanFactory 创建的 bean，但是 ApplicationContext 可以在其 bean 定义中自动检测所有的 BeanPostProcessor 并自动完成注册，同时将它们应用到随后创建的任何 bean 中。

ApplicationContext 实现自动注册的原因在于构造一个 ApplicationContext 实例对象的时候会调用 `registerBeanPostProcessors()` 方法将检测到的 BeanPostProcessor 注入到 ApplicationContext 容器中，同时应用到该容器创建的 bean 中。

```java
/**
* 实例化并调用已经注入的 BeanPostProcessor
* 必须在应用中 bean 实例化之前调用
*/
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}

public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

    // 获取所有的 BeanPostProcessor 的 beanName
    // 这些 beanName 都已经全部加载到容器中去，但是没有实例化
    String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

    // 记录所有的beanProcessor数量
    int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;

    // 注册 BeanPostProcessorChecker，它主要是用于在 BeanPostProcessor 实例化期间记录日志
    // 当 Spring 中高配置的后置处理器还没有注册就已经开始了 bean 的实例化过程，这个时候便会打印 BeanPostProcessorChecker 中的内容
    beanFactory.addBeanPostProcessor(new PostProcessorRegistrationDelegate.BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

    // PriorityOrdered 保证顺序
    List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
    // MergedBeanDefinitionPostProcessor
    List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
    // 使用 Ordered 保证顺序
    List<String> orderedPostProcessorNames = new ArrayList<>();
    // 没有顺序
    List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    for (String ppName : postProcessorNames) {
        // PriorityOrdered
        if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            // 调用 getBean 获取 bean 实例对象
            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
            priorityOrderedPostProcessors.add(pp);
            if (pp instanceof MergedBeanDefinitionPostProcessor) {
                internalPostProcessors.add(pp);
            }
        }
        // Ordered
        else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
            orderedPostProcessorNames.add(ppName);
        }
        else {
            // 无序
            nonOrderedPostProcessorNames.add(ppName);
        }
    }

    // 第一步注册所有实现了 PriorityOrdered 的BeanPostProcessor
    // 先排序
    sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
    // 后注册
    registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

    // 第二步注册所有实现了 Ordered 的 BeanPostProcessor
    List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
    for (String ppName : orderedPostProcessorNames) {
        BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
        orderedPostProcessors.add(pp);
        if (pp instanceof MergedBeanDefinitionPostProcessor) {
            internalPostProcessors.add(pp);
        }
    }
    sortPostProcessors(orderedPostProcessors, beanFactory);
    registerBeanPostProcessors(beanFactory, orderedPostProcessors);

    // 第三步注册所有无序的 BeanPostProcessor
    List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
    for (String ppName : nonOrderedPostProcessorNames) {
        BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
        nonOrderedPostProcessors.add(pp);
        if (pp instanceof MergedBeanDefinitionPostProcessor) {
            internalPostProcessors.add(pp);
        }
    }
    registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

    // 最后，注册所有的 MergedBeanDefinitionPostProcessor 类型的 BeanPostProcessor
    sortPostProcessors(internalPostProcessors, beanFactory);
    registerBeanPostProcessors(beanFactory, internalPostProcessors);

    // 加入ApplicationListenerDetector（探测器）
    // 重新注册 BeanPostProcessor 以检测内部 bean，因为 ApplicationListeners 将其移动到处理器链的末尾
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}

private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
    Comparator<Object> comparatorToUse = null;
    if (beanFactory instanceof DefaultListableBeanFactory) {
        comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
    }
    if (comparatorToUse == null) {
        comparatorToUse = OrderComparator.INSTANCE;
    }
    postProcessors.sort(comparatorToUse);
}

private static void registerBeanPostProcessors(
    ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

    for (BeanPostProcessor postProcessor : postProcessors) {
        beanFactory.addBeanPostProcessor(postProcessor);
    }
}
```

方法首先 beanFactory 获取注册到该 BeanFactory 中所有 BeanPostProcessor 类型的 beanName，其实就是找所有实现了 BeanPostProcessor 接口的 bean ，然后迭代这些 bean，将其按照PriorityOrdered、Ordered、无序的顺序添加至相应的 List 集合中，最后依次调用 `sortPostProcessors()` 进行排序处理和 `registerBeanPostProcessors()` 完成注册。

BeanPostProcessor总结如下：
1. BeanPostProcessor 的作用域是容器级别的，它只和所在的容器相关 ，当 BeanPostProcessor 完成注册后，它会应用于所有跟它在同一个容器内的 bean
2. BeanFactory 和 ApplicationContext 对 BeanPostProcessor 的处理不同，ApplicationContext 会自动检测所有实现了 BeanPostProcessor 接口的 bean，并完成注册，但是使用 BeanFactory 容器时则需要手动调用 `addBeanPostProcessor()` 完成注册
3. ApplicationContext 的 BeanPostProcessor 支持 Ordered，而 BeanFactory 的 BeanPostProcessor 是不支持的，原因在于ApplicationContext 会对 BeanPostProcessor 进行 Ordered 检测并完成排序，而 BeanFactory 中的 BeanPostProcessor 只跟注册的顺序有关。