# Spring 的应用上下文

Spring  自带了多种类型的应用上下文。下面罗列的几个是最常见的上下文：

- AnnotationConfigApplicationContext ：从一个或多个基于 Java 的配置类中加载 Spring 应用上下文。
- AnnotationConfigWebApplicationContext ：从一个或多个基于 Java 的配置类中加载 Spring Web 应用上下文。
- ClassPathXmlApplicationContext ：从类路径下的一个或多个 XML 配置文件中加载上下文定义，把应用上下文的定义文件作为类资源。
- FileSystemXmlapplicationcontext ：从文件系统下的一个或多个 XML 配置文件中加载上下文定义。
- XmlWebApplicationContext ：从 Web 应用下的一个或多个 XML 配置文件中加载上下文定义。