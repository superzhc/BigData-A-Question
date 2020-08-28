# PropertyPlaceholderConfigurer

## 源码分析

> PropertyPlaceholderConfigurer 允许我们用 Properties 文件中的属性来定义应用上下文（配置文件或者注解）

上述描述的就是在XML配置文件（或者其他方式，如注解方式）中使用占位符的方式来定义一些资源，并将这些占位符所代表的资源配置到Properties中，这样只需对Properties文件进行修改即可

![](./images/PropertyPlaceholderConfigurer.png)

从PropertyPlaceholderConfigurer的结构图可以看出，它间接实现了Aware和BeanFactoryPostProcessor两大扩展接口。

PropertyResourceConfigurer实现了BeanFactoryPostProcessor 提供的 `postProcessBeanFactory()`方法，该类为属性资源的配置类，如下：

```java
public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    try {
        Properties mergedProps = mergeProperties();

        // 转换合并属性
        convertProperties(mergedProps);

        // 子类处理
        processProperties(beanFactory, mergedProps);
    }
    catch (IOException ex) {
        throw new BeanInitializationException("Could not load properties", ex);
    }
}
```

- `mergeProperties()`：返回合并的 Properties 实例，Properties 实例维护这一组 key-value ，其实就是 Properties 配置文件中的内容。
- `convertProperties()`：转换合并的值，其实就是将原始值替换为真正的值
- `processProperties()`：前面两个步骤已经将配置文件中的值进行了处理，那么该方法就是真正的替换过程，该方法由子类实现。

在 PropertyPlaceholderConfigurer 重写 `processProperties()`:

```java
protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
throws BeansException {

    StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);
    doProcessProperties(beanFactoryToProcess, valueResolver);
}
```

首先构造一个 PlaceholderResolvingStringValueResolver 类型的 StringValueResolver 实例。StringValueResolver 为一个解析 String 类型值的策略接口，该接口提供了 `resolveStringValue()` 方法用于解析 String 值。PlaceholderResolvingStringValueResolver 为其一个解析策略，构造方法如下：

```java
public PlaceholderResolvingStringValueResolver(Properties props) {
    this.helper = new PropertyPlaceholderHelper(
    placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
    this.resolver = new PropertyPlaceholderConfigurerResolver(props);
}
```

在构造 String 值解析器 StringValueResolver 时，将已经解析的 Properties 实例对象封装在 PlaceholderResolver 实例 resolver 中。PlaceholderResolver 是一个用于解析字符串中包含占位符的替换值的策略接口，该接口有一个 `resolvePlaceholder()` 方法，用于返回占位符的替换值。

得到 String 解析器的实例 valueResolver 后，则会调用 `doProcessProperties()` 方法来进行诊治的替换操作，该方法在父类 PlaceholderConfigurerSupport 中实现，如下：

```java
protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess, StringValueResolver valueResolver) {

    BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

    String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
    for (String curName : beanNames) {
        // 校验
        // 1. 当前实例 PlaceholderConfigurerSupport 不在解析范围内
        // 2. 同一个 Spring 容器
        if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
            BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
            try {
                visitor.visitBeanDefinition(bd);
            }
            catch (Exception ex) {
                throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
            }
        }
    }

    // 别名的占位符
    beanFactoryToProcess.resolveAliases(valueResolver);

    // 解析嵌入值的占位符，例如注释属性
    beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
}
```

流程如下：
1. 根据 String 值解析策略 valueResolver 得到 BeanDefinitionVisitor 实例。BeanDefinitionVisitor 是 BeanDefinition 的访问者，我们通过它可以实现对 BeanDefinition 内容的进行访问，内容很多，例如Scope、PropertyValues、FactoryMethodName 等等。
2. 得到该容器的所有 BeanName，然后对其进行访问（`visitBeanDefinition()`）。
3. 解析别名的占位符
4. 解析嵌入值的占位符，例如注释属性

这个方法核心在于 `visitBeanDefinition()` 的调用，如下：

```java
public void visitBeanDefinition(BeanDefinition beanDefinition) {
    visitParentName(beanDefinition);
    visitBeanClassName(beanDefinition);
    visitFactoryBeanName(beanDefinition);
    visitFactoryMethodName(beanDefinition);
    visitScope(beanDefinition);
    if (beanDefinition.hasPropertyValues()) {
        visitPropertyValues(beanDefinition.getPropertyValues());
    }
    if (beanDefinition.hasConstructorArgumentValues()) {
        ConstructorArgumentValues cas = beanDefinition.getConstructorArgumentValues();
        visitIndexedArgumentValues(cas.getIndexedArgumentValues());
        visitGenericArgumentValues(cas.getGenericArgumentValues());
    }
}

/**
* 对属性的处理过程就是对属性数组进行遍历，调用 resolveValue() 对属性进行解析获取最新值，如果新值和旧值不等，则用新值替换旧值。
*/
protected void visitPropertyValues(MutablePropertyValues pvs) {
    PropertyValue[] pvArray = pvs.getPropertyValues();
    for (PropertyValue pv : pvArray) {
        Object newVal = resolveValue(pv.getValue());
        if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
            pvs.add(pv.getName(), newVal);
        }
    }
}

protected Object resolveValue(@Nullable Object value) {
    // 由于 Properties 中的是 String，所以把前面一堆 if 去掉
    else if (value instanceof String) {
        return resolveStringValue((String) value);
    }
    return value;
}
```

## 应用

因PropertyPlaceholderConfigurer允许在XML配置文件中使用占位符并将这些占位符所代表的资源单独配置到简单的propreties文件中来加载，所以对Bean实例属性的配置可以很容易的控制，主要的使用场景有：
1. 动态加载配置文件，多环境切换
2. 属性加解密