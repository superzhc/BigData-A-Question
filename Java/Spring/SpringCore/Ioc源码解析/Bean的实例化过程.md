# Bean的实例化过程

Bean的创建的核心方法是`createBean()`，该方法定义在AbstractBeanFactory中。其含义是根据给定的 BeanDefinition 和 args 实例化一个 bean 对象，如果该 BeanDefinition 存在父类，则该 BeanDefinition 已经合并了父类的属性。所有 Bean 实例的创建都会委托给该方法实现。

该方法的定义如下：

```java
protected abstract Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
            throws BeanCreationException;
```

方法接受三个参数：

- `beanName`：bean 的名字
- `mbd`：已经合并了父类属性的（如果有的话）BeanDefinition
- `args`：用于构造函数或者工厂方法创建 bean 实例对象的参数

**该抽象方法的默认实现是在类AbstractAutowireCapableBeanFactory中实现**，如下：

```java
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException {

    if (logger.isDebugEnabled()) {
        logger.debug("Creating instance of bean '" + beanName + "'");
    }
    RootBeanDefinition mbdToUse = mbd;

    // 确保此时的 bean 已经被解析了
    // 如果获取的 class 属性不为null，则克隆该 BeanDefinition
    // 主要是因为该动态解析的 class 无法保存到到共享的 BeanDefinition
    Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
    if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
        mbdToUse = new RootBeanDefinition(mbd);
        mbdToUse.setBeanClass(resolvedClass);
    }

    try {
        // 验证和准备覆盖方法
        mbdToUse.prepareMethodOverrides();
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
                beanName, "Validation of method overrides failed", ex);
    }

    try {
        // 给 BeanPostProcessors 一个机会用来返回一个代理类而不是真正的类实例
        // AOP 的功能就是基于这个地方
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
        if (bean != null) {
            return bean;
        }
    }
    catch (Throwable ex) {
        throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                "BeanPostProcessor before instantiation of bean failed", ex);
    }

    try {
        // 执行真正创建 bean 的过程
        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        if (logger.isDebugEnabled()) {
            logger.debug("Finished creating instance of bean '" + beanName + "'");
        }
        return beanInstance;
    }
    catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
        throw ex;
    }
    catch (Throwable ex) {
        throw new BeanCreationException(
                mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
    }
}

/**
* 这个方法核心就在于 applyBeanPostProcessorsBeforeInstantiation() 和 applyBeanPostProcessorsAfterInitialization() 两个方法，before 为实例化前的后处理器应用，after 为实例化后的后处理器应用
*/
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
    Object bean = null;
    if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            Class<?> targetType = determineTargetType(beanName, mbd);
            if (targetType != null) {
                bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                if (bean != null) {
                    bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                }
            }
        }
        mbd.beforeInstantiationResolved = (bean != null);
    }
    return bean;
}

protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
        throws BeanCreationException {

    // BeanWrapper是对Bean的包装，其接口中所定义的功能很简单包括设置获取被包装的对象，获取被包装bean的属性描述器
    BeanWrapper instanceWrapper = null;
    // 单例模型，则从未完成的 FactoryBean 缓存中删除
    if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    }

    // 使用合适的实例化策略来创建新的实例：工厂方法、构造函数自动注入、简单初始化
    if (instanceWrapper == null) {
        instanceWrapper = createBeanInstance(beanName, mbd, args);
    }

    // 包装的实例对象
    final Object bean = instanceWrapper.getWrappedInstance();
    // 包装的实例对象的类型
    Class<?> beanType = instanceWrapper.getWrappedClass();
    if (beanType != NullBean.class) {
        mbd.resolvedTargetType = beanType;
    }

    // 检测是否有后置处理
    // 如果有后置处理，则允许后置处理修改 BeanDefinition
    synchronized (mbd.postProcessingLock) {
        if (!mbd.postProcessed) {
            try {
                // applyMergedBeanDefinitionPostProcessors
                // 后置处理修改 BeanDefinition
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Post-processing of merged bean definition failed", ex);
            }
            mbd.postProcessed = true;
        }
    }

    // 解决单例模式的循环依赖
    // 单例模式 & 运行循环依赖&当前单例 bean 是否正在被创建
    boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
            isSingletonCurrentlyInCreation(beanName));
    if (earlySingletonExposure) {
        if (logger.isDebugEnabled()) {
            logger.debug("Eagerly caching bean '" + beanName +
                    "' to allow for resolving potential circular references");
        }
        // 提前将创建的 bean 实例加入到ectFactory 中
        // 这里是为了后期避免循环依赖
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
    }

    /*
    * 开始初始化 bean 实例对象
    */
    Object exposedObject = bean;
    try {
        // 对 bean 进行填充，将各个属性值注入，其中，可能存在依赖于其他 bean 的属性
        // 则会递归初始依赖 bean
        populateBean(beanName, mbd, instanceWrapper);
        // 调用初始化方法
        exposedObject = initializeBean(beanName, exposedObject, mbd);
    }
    catch (Throwable ex) {
        if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
            throw (BeanCreationException) ex;
        }
        else {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
        }
    }

    /**
    * 循环依赖处理
    */
    if (earlySingletonExposure) {
        // 获取 earlySingletonReference
        Object earlySingletonReference = getSingleton(beanName, false);
        // 只有在存在循环依赖的情况下，earlySingletonReference 才不会为空
        if (earlySingletonReference != null) {
            // 如果 exposedObject 没有在初始化方法中被改变，也就是没有被增强
            if (exposedObject == bean) {
                exposedObject = earlySingletonReference;
            }
            // 处理依赖
            else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                String[] dependentBeans = getDependentBeans(beanName);
                Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                for (String dependentBean : dependentBeans) {
                    if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                        actualDependentBeans.add(dependentBean);
                    }
                }
                if (!actualDependentBeans.isEmpty()) {
                    throw new BeanCurrentlyInCreationException(beanName,
                            "Bean with name '" + beanName + "' has been injected into other beans [" +
                                    StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                                    "] in its raw version as part of a circular reference, but has eventually been " +
                                    "wrapped. This means that said other beans do not use the final version of the " +
                                    "bean. This is often the result of over-eager type matching - consider using " +
                                    "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
                }
            }
        }
    }
    try {
        // 注册 bean
        registerDisposableBeanIfNecessary(beanName, bean, mbd);
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanCreationException(
                mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
    }

    return exposedObject;
}
```

过程如下：

- **解析指定 BeanDefinition 的 class**：这个方法主要是解析 bean definition 的 class 类，并将已经解析的 Class 存储在 bean definition 中以供后面使用。如果解析的 class 不为空，则会将该 BeanDefinition 进行克隆至 mbdToUse，这样做的主要目的是以为动态解析的 class 是无法保存到共享的 BeanDefinition 中。
- **处理 override 属性**：`mbdToUse.prepareMethodOverrides()`，对methodOVerrides属性做一些校验
- **实例化的前置处理**：`resolveBeforeInstantiation()`，它的作用是给BeanPostProcessors后置处理器返回一个代理对象，其实在调用该方法之前Spring一直没有创建bean，这个bean在不为空，则直接返回代理对象，后续AOP的实现就是基于这个地方判断的
- **创建 bean**：`doCreateBean()`，完成bean的创建和初始化工作。整体思路如下：
    1. 如果是单例模式，则清除 factoryBeanInstanceCache 缓存，同时返回 BeanWrapper 实例对象，当然如果存在
    2. 如果缓存中没有 BeanWrapper 或者不是单例模式，则调用 `createBeanInstance()` 实例化 bean，主要是将 BeanDefinition 转换为 BeanWrapper
    3. MergedBeanDefinitionPostProcessor 的应用
    4. 调用 `populateBean()` 进行属性填充。将所有属性填充至 bean 的实例中
    5. 循环依赖处理
    6. 调用 `initializeBean()` 初始化 bean
    7. 依赖检查
    8. 注册 DisposableBean

## `createBeanInstance()`实例化bean

```java
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
    // 解析 bean，将 bean 类名解析为 class 引用
    Class<?> beanClass = resolveBeanClass(mbd, beanName);

    if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
    }

    // 如果存在 Supplier 回调，则使用给定的回调方法初始化策略
    Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
    if (instanceSupplier != null) {
        return obtainFromSupplier(instanceSupplier, beanName);
    }

    // 如果工厂方法不为空，则使用工厂方法初始化策略
    if (mbd.getFactoryMethodName() != null)  {
        return instantiateUsingFactoryMethod(beanName, mbd, args);
    }

    boolean resolved = false;
    boolean autowireNecessary = false;
    if (args == null) {
        // constructorArgumentLock 构造函数的常用锁
        synchronized (mbd.constructorArgumentLock) {
            // 如果已缓存的解析的构造函数或者工厂方法不为空，则可以利用构造函数解析
            // 因为需要根据参数确认到底使用哪个构造函数，该过程比较消耗性能，所有采用缓存机制
            if (mbd.resolvedConstructorOrFactoryMethod != null) {
                resolved = true;
                autowireNecessary = mbd.constructorArgumentsResolved;
            }
        }
    }
    // 已经解析好了，直接注入即可
    if (resolved) {
        // 自动注入，调用构造函数自动注入
        if (autowireNecessary) {
            return autowireConstructor(beanName, mbd, null, null);
        }
        else {
            // 使用默认构造函数构造
            return instantiateBean(beanName, mbd);
        }
    }

    // 确定解析的构造函数
    // 主要是检查已经注册的 SmartInstantiationAwareBeanPostProcessor
    Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
    if (ctors != null ||
            mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
            mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
        // 构造函数自动注入
        return autowireConstructor(beanName, mbd, ctors, args);
    }

    //使用默认构造函数注入
    return instantiateBean(beanName, mbd);
}
```

实例化bean的主要逻辑为：
- 如果存在Supplier回调，则调用 `obtainFromSupplier()` 进行初始化
- 如果存在工厂方法，则使用工厂方法进行初始化
- 首先判断缓存，如果缓存中存在，即已经解析过了，则直接使用已经解析了的，根据 constructorArgumentsResolved 参数来判断是使用构造函数自动注入还是默认构造函数
- 如果缓存中没有，则需要先确定到底使用哪个构造函数来完成解析工作，因为一个类有多个构造函数，每个构造函数都有不同的构造参数，所以需要根据参数来锁定构造函数并完成初始化，如果存在参数则使用相应的带有参数的构造函数，否则使用默认构造函数。