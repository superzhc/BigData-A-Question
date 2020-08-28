# 各Scope的Bean创建

在Spring中存在这不同的scope，默认是singleton，还有prototype、request等等其他的scope。

## singleton

Spring的scope默认为singleton，其初始化的代码如下：

```java
if (mbd.isSingleton()) {
    sharedInstance = getSingleton(beanName, () -> {
        try {
            return createBean(beanName, mbd, args);
        }
        catch (BeansException ex) {
            destroySingleton(beanName);
            throw ex;
        }
    });
    bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
}

public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
    Assert.notNull(beanName, "Bean name must not be null");

    // 全局加锁
    synchronized(this.singletonObjects) {
        // 从缓存中检查一遍
        // 因为 singleton 模式其实就是复用已经创建的 bean 所以这步骤必须检查
        Object singletonObject = this.singletonObjects.get(beanName);
        //  为空，开始加载过程
        if (singletonObject == null) {
            // 省略 部分代码

            // 加载前置处理
            beforeSingletonCreation(beanName);
            boolean newSingleton = false;
            // 省略代码
            try {
                // 初始化 bean
                // 这个过程其实是调用 createBean() 方法
                singletonObject = singletonFactory.getObject();
                newSingleton = true;
            }
            // 省略 catch 部分
            }
            finally {
                // 后置处理
                afterSingletonCreation(beanName);
            }
            // 加入缓存中
            if (newSingleton) {
                addSingleton(beanName, singletonObject);
            }
        }
        // 直接返回
        return singletonObject;
    }
}

protected void addSingleton(String beanName, Object singletonObject) {
    synchronized (this.singletonObjects) {
        this.singletonObjects.put(beanName, singletonObject);
        this.singletonFactories.remove(beanName);
        this.earlySingletonObjects.remove(beanName);
        this.registeredSingletons.add(beanName);
    }
}
```

上述过程并没有真正创建bean，仅仅只是做了一部分准备和预处理步骤，真正获取单例bean的方法是由`singletonFactory.getObject()`这部分实现，而singletonFactory由回调方法产生。

## 原型模式

```java
else if (mbd.isPrototype()) {
    Object prototypeInstance = null;
    try {
        beforePrototypeCreation(beanName);
        prototypeInstance = createBean(beanName, mbd, args);
    }
    finally {
        afterPrototypeCreation(beanName);
    }
    bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
}
```

原型模式的初始化过程很简单：直接创建一个新的实例就可以了。过程如下：

1. 调用 `beforeSingletonCreation()` 记录加载原型模式 bean 之前的加载状态，即前置处理；
2. 调用 `createBean()` 创建一个 bean 实例对象；
3. 调用 `afterSingletonCreation()` 进行加载原型模式 bean 后的后置处理；
4. 调用 `getObjectForBeanInstance()` 从 bean 实例中获取对象。

## 其他作用域

```java
String scopeName = mbd.getScope();
final Scope scope = this.scopes.get(scopeName);
if (scope == null) {
    throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
}
try {
    Object scopedInstance = scope.get(beanName, () -> {
        beforePrototypeCreation(beanName);
        try {
            return createBean(beanName, mbd, args);
        }
        finally {
            afterPrototypeCreation(beanName);
        }
    });
    bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
}
catch (IllegalStateException ex) {
    throw new BeanCreationException(beanName,
            "Scope '" + scopeName + "' is not active for the current thread; consider " +
            "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
            ex);
}
```

核心流程和原型模式一样，只不过获取 bean 实例是由 `scope.get()` 实现，如下：

```java
public Object get(String name, ObjectFactory<?> objectFactory) {
    // 获取 scope 缓存
    Map<String, Object> scope = this.threadScope.get();
    Object scopedObject = scope.get(name);
    if (scopedObject == null) {
        scopedObject = objectFactory.getObject();
        // 加入缓存
        scope.put(name, scopedObject);
    }
    return scopedObject;
}
```