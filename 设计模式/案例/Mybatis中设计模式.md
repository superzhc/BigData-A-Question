# Mybatis中设计模式

Mybatis中设计模式的使用如下：（部分）

- 建造者模式，例如[SqlSessionFactoryBuilder](../../Mybatis/SqlSessionFactoryBuilder.md)、[XMLConfigBuilder](../../Mybatis/XMLConfigBuilder.md)、`XMLMapperBuilder`、`XMLStatementBuilder`、`CacheBuilder`；
- 工厂模式，例如[SqlSessionFactory](../../Mybatis/SqlSessionFactory.md)、`ObjectFactory`、`MapperProxyFactory`；
- 单例模式，例如`ErrorContext`和`LogFactory`；
- 代理模式，Mybatis实现的核心，比如`MapperProxy`、`ConnectionLogger`，用的jdk的动态代理；还有executor.loader包使用了cglib或者javassist达到延迟加载的效果；
- 组合模式，例如`SqlNode`和各个子类`ChooseSqlNode`等；
- 模板方法模式，例如`BaseExecutor`和`SimpleExecutor`，还有`BaseTypeHandler`和所有的子类例如`IntegerTypeHandler`；
- 适配器模式，例如Log的Mybatis接口和它对jdbc、log4j等各种日志框架的适配实现；
- 装饰者模式，例如Cache包中的cache.decorators子包中等各个装饰者的实现；
- 迭代器模式，例如迭代器模式`PropertyTokenizer`；