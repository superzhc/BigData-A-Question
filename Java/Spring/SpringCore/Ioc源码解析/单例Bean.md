# 从单例缓存中获取单例 bean

加载 bean 的第一个步骤是从单例缓存中获取 bean，代码片段如下：

```java
Object sharedInstance = getSingleton(beanName);
if (sharedInstance != null && args == null) {
    if (logger.isDebugEnabled()) {
        if (isSingletonCurrentlyInCreation(beanName)) {
            logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                    "' that is not fully initialized yet - a consequence of a circular reference");
        }
        else {
            logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
        }
    }
    bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
}
```

首先调用`getSingleton()`从缓存中获取bean，在Spring中对单例模式的bean只会创建一次，后续如果再获取该bean则是直接从单例缓存中获取，该过程就体现在`getSingleton()`中。如下：

```java
public Object getSingleton(String beanName) {
    return getSingleton(beanName, true);
}

protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    // 从单例缓冲中加载 bean
    Object singletonObject = this.singletonObjects.get(beanName);

    // 缓存中的 bean 为空，且当前 bean 正在创建
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        // 加锁
        synchronized (this.singletonObjects) {
            // 从 earlySingletonObjects 获取
            singletonObject = this.earlySingletonObjects.get(beanName);
            // earlySingletonObjects 中没有，且允许提前创建
            if (singletonObject == null && allowEarlyReference) {
                // 从 singletonFactories 中获取对应的 ObjectFactory
                ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                // ObjectFactory 不为空，则创建 bean
                if (singletonFactory != null) {
                    singletonObject = singletonFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
    }
    return singletonObject;
}
```

上述代码的总体逻辑就是根据beanName依次检测三个Map（singletonObjects、earlySingletonObjects、singletonFactories），若为空，继续下一个，否则返回。这三个Map存放的都有各自的功能，如下：
- `singletonObjects`:存放的是单例bean，对应关系为`bean name` --> `bean instance`
- `earlySingletonObjects`:存放的是早期的bean，对应关系也是`bean name` --> `bean instance`。它与singletonObjects区别在于earlySingletonObjects中存放的bean不一定是完整的，从上面的代码中可以知道，bean在创建过程中就已经加入到earlySingletonObjects中了，所以当在bean的创建过程中就可以通过 `getBean()` 方法获取。这个Map也是解决循环依赖的关键所在
- `singletonFactories`:存放的是ObjectFactory，可以理解为创建单例bean的factory，对应关系是`bean name`-->`ObjectFactory`

在缓存中获取bean后，若其不为空且args为空，则会调用`getObjectForBeanInstance()`处理，该方法是为了处理因在缓存中获取的bean是最原始的bean而不一定是最终的bean所提供的处理方法，方法代码如下：

```java
protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

    // 若为工厂类引用（name 以 & 开头）
    if (BeanFactoryUtils.isFactoryDereference(name)) {
        // 如果是 NullBean，则直接返回
        if (beanInstance instanceof NullBean) {
            return beanInstance;
        }
        // 如果 beanInstance 不是 FactoryBean 类型，则抛出异常
        if (!(beanInstance instanceof FactoryBean)) {
            throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
        }
    }

    // 到这里我们就有了一个 bean 实例，当然该实例可能是会是是一个正常的 bean 又或者是一个 FactoryBean
    // 如果是 FactoryBean，我我们则创建该 bean
    if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
        return beanInstance;
    }

    // 加载 FactoryBean
    Object object = null;
    // 若 BeanDefinition 为 null，则从缓存中加载
    if (mbd == null) {
        object = getCachedObjectForFactoryBean(beanName);
    }
    // 若 object 依然为空，则可以确认，beanInstance 一定是 FactoryBean
    if (object == null) {
        FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
        //
        if (mbd == null && containsBeanDefinition(beanName)) {
            mbd = getMergedLocalBeanDefinition(beanName);
        }
        // 是否是用户定义的而不是应用程序本身定义的
        boolean synthetic = (mbd != null && mbd.isSynthetic());
        // 核心处理类
        object = getObjectFromFactoryBean(factory, beanName, !synthetic);
    }
    return object;
}
```

该方法主要是进行检测工作的，主要如下：
- 若 name 为工厂相关的（以 & 开头），且 beanInstance 为 NullBean 类型则直接返回，如果 beanInstance 不为 FactoryBean 类型则抛出 BeanIsNotAFactoryException 异常。这里主要是校验 beanInstance 的正确性。
- 如果 beanInstance 不为 FactoryBean 类型或者 name 也不是与工厂相关的，则直接返回。这里主要是对非 FactoryBean 类型处理。
- 如果 BeanDefinition 为空，则从 factoryBeanObjectCache 中加载，如果还是空，则可以断定 beanInstance 一定是 FactoryBean 类型，则委托 `getObjectFromFactoryBean()` 方法处理

从上面代码可以看出`getObjectForBeanInstance()`主要是**返回给定的bean实例对象**，当然该实例对象为非 FactoryBean 类型，对于 FactoryBean 类型的 bean，则是委托`getObjectFromFactoryBean()`从FactoryBean获取bean实例对象。

```java
protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
    // 为单例模式且缓存中存在
    if (factory.isSingleton() && containsSingleton(beanName)) {
        synchronized (getSingletonMutex()) {
            // 从缓存中获取指定的 factoryBean
            Object object = this.factoryBeanObjectCache.get(beanName);

            if (object == null) {
                // 为空，则从 FactoryBean 中获取对象
                object = doGetObjectFromFactoryBean(factory, beanName);

                // 从缓存中获取
                Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                if (alreadyThere != null) {
                    object = alreadyThere;
                }
                else {
                    // 需要后续处理
                    if (shouldPostProcess) {
                        // 若该 bean 处于创建中，则返回非处理对象，而不是存储它
                        if (isSingletonCurrentlyInCreation(beanName)) {
                            return object;
                        }
                        // 前置处理
                        beforeSingletonCreation(beanName);
                        try {
                            // 对从 FactoryBean 获取的对象进行后处理
                            // 生成的对象将暴露给bean引用
                            object = postProcessObjectFromFactoryBean(object, beanName);
                        }
                        catch (Throwable ex) {
                            throw new BeanCreationException(beanName,
                                    "Post-processing of FactoryBean's singleton object failed", ex);
                        }
                        finally {
                            // 后置处理
                            afterSingletonCreation(beanName);
                        }
                    }
                    // 缓存
                    if (containsSingleton(beanName)) {
                        this.factoryBeanObjectCache.put(beanName, object);
                    }
                }
            }
            return object;
        }
    }
    else {
        // 非单例
        Object object = doGetObjectFromFactoryBean(factory, beanName);
        if (shouldPostProcess) {
            try {
                object = postProcessObjectFromFactoryBean(object, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
            }
        }
        return object;
    }
}
```

主要流程如下：
1. 若为单例且单例 bean 缓存中存在 beanName，则进行后续处理（跳转到下一步），否则则从 FactoryBean 中获取 bean 实例对象，如果接受后置处理，则调用 `postProcessObjectFromFactoryBean()` 进行后置处理；
2. 首先获取锁，然后从 factoryBeanObjectCache 缓存中获取实例对象 object，若 object 为空，则调用 `doGetObjectFromFactoryBean()` 方法从 FactoryBean 获取对象，其实内部就是调用 `FactoryBean.getObject()`；
3. 如果需要后续处理，则进行进一步处理，步骤如下：
   - 若该 bean 处于创建中（isSingletonCurrentlyInCreation），则返回非处理对象，而不是存储它
   - 调用 **`beforeSingletonCreation()`** 进行创建之前的处理，默认实现将该 bean 标志为当前创建的
   - 调用 **`postProcessObjectFromFactoryBean()`** 对从 FactoryBean 获取的 bean 实例对象进行后置处理，默认实现是按照原样直接返回，具体实现是在 AbstractAutowireCapableBeanFactory 中实现的，当然子类也可以重写它，比如应用后置处理
   - 调用 **`afterSingletonCreation()`** 进行创建 bean 之后的处理，默认实现是将该 bean 标记为不再在创建中
4. 最后加入到 FactoryBeans 缓存中。