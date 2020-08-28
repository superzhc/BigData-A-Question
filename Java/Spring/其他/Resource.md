---
title: 资源
date: 2017-11-15
tags: [spring,java]
---

## 基础知识

### 概述

在日常程序开发中，处理外部资源是很繁琐的事情，我们可能需要处理URL资源、File资源资源、ClassPath相关资源、服务器相关资源（JBoss AS 5.x上的VFS资源）等等很多资源。因此处理这些资源需要使用不同的接口，这就增加了我们系统的复杂性；而且处理这些资源步骤都是类似的（打开资源、读取资源、关闭资源），因此如果能抽象出一个统一的接口来对这些底层资源进行统一访问，是不是很方便，而且使我们系统更加简洁，都是对不同的底层资源使用同一个接口进行访问。

Spring 提供一个Resource接口来统一这些底层资源一致的访问，而且提供了一些便利的接口，从而能提供我们的生产力。

### Resource接口

Spring的Resource接口代表底层外部资源，提供了对底层外部资源的一致性访问接口。

```java
public interface InputStreamSource {
    InputStream getInputStream() throws IOException;
}
```

getInputStream：每次调用都将返回一个新的资源对应的java.io. InputStream字节流，调用者在使用完毕后必须关闭该资源。

```java
public interface Resource extends InputStreamSource {
    boolean exists();
    boolean isReadable();
    boolean isOpen();
    URL getURL() throws IOException;
    URI getURI() throws IOException;
    File getFile() throws IOException;
    long contentLength() throws IOException;
    long lastModified() throws IOException;
    Resource createRelative(String relativePath) throws IOException;
    String getFilename();
    String getDescription();
}
```

- **exists** ：返回当前Resource代表的底层资源是否存在，true表示存在。
- **isReadable** ：返回当前Resource代表的底层资源是否可读，true表示可读。
- **isOpen** ：返回当前Resource代表的底层资源是否已经打开，如果返回true，则只能被读取一次然后关闭以避免资源泄露；常见的Resource实现一般返回false。
- **getURL** ：如果当前Resource代表的底层资源能由java.util.URL代表，则返回该URL，否则抛出IOException。
- **getURI** ：如果当前Resource代表的底层资源能由java.util.URI代表，则返回该URI，否则抛出IOException。
- **getFile** ：如果当前Resource代表的底层资源能由java.io.File代表，则返回该File，否则抛出IOException。
- **contentLength** ：返回当前Resource代表的底层文件资源的长度，一般是值代表的文件资源的长度。
- **lastModified** ：返回当前Resource代表的底层资源的最后修改时间。
- **createRelative** ：用于创建相对于当前Resource代表的底层资源的资源，比如当前Resource代表文件资源“d:/test/”则createRelative（“test.txt”）将返回表文件资源“d:/test/test.txt”Resource资源。
- **getFilename** ：返回当前Resource代表的底层文件资源的文件路径，比如File资源“file://d:/test.txt”将返回“d:/test.txt”，而URL资源http://www.javass.cn将返回“”，因为只返回文件路径。
- **getDescription** ：返回当前Resource代表的底层资源的描述符，通常就是资源的全路径（实际文件名或实际URL地址）。

Resource接口提供了足够的抽象，足够满足我们日常使用。而且提供了很多内置Resource实现：ByteArrayResource、InputStreamResource 、FileSystemResource 、UrlResource 、ClassPathResource、ServletContextResource、VfsResource等。

## 内置Resource实现

### ByteArrayResource

ByteArrayResource代表byte[]数组资源，对于“getInputStream”操作将返回一个ByteArrayInputStream。

ByteArrayResource可多次读取数组资源，即isOpen ()永远返回false。

### InputStreamResource

InputStreamResource代表java.io.InputStream字节流，对于“getInputStream ”操作将直接返回该字节流，因此只能读取一次该字节流，即“isOpen”永远返回true。

### FileSystemResource

FileSystemResource代表java.io.File资源，对于“getInputStream ”操作将返回底层文件的字节流，“isOpen”将永远返回false，从而表示可多次读取底层文件的字节流。

### ClassPathResource

ClassPathResource代表classpath路径的资源，将使用ClassLoader进行加载资源。classpath 资源存在于类路径中的文件系统中或jar包里，且“isOpen”永远返回false，表示可多次读取资源。

ClassPathResource加载资源替代了Class类和ClassLoader类的“getResource(String name)”和“getResourceAsStream(String name)”两个加载类路径资源方法，提供一致的访问方式。

ClassPathResource提供了三个构造器：

- public ClassPathResource(String path)：使用默认的ClassLoader加载“path”类路径资源；
- public ClassPathResource(String path, ClassLoader classLoader)：使用指定的ClassLoader加载“path”类路径资源；
- public ClassPathResource(String path, Class<?> clazz)：使用指定的类加载“path”类路径资源，将加载相对于当前类的路径的资源；

**注：** 加载jar包里的资源，首先在当前类路径下找不到，最后才到Jar包里找，而且在第一个Jar包里找到的将被返回；

如果当前类路径包含“overview.html”，在项目的“resources”目录下，将加载该资源，否则将加载Jar包里的“overview.html”，而且不能使用“resource.getFile()”，应该使用“resource.getURL()”，因为资源不存在于文件系统而是存在于jar包里，URL类似于“file:/C:/.../***.jar!/overview.html”。

类路径一般都是相对路径，即相对于类路径或相对于当前类的路径，因此如果使用“/test1.properties”带前缀“/”的路径，将自动删除“/”得到“test1.properties”。

### UrlResource

UrlResource代表URL资源，用于简化URL资源访问。“isOpen”永远返回false，表示可多次读取资源。

UrlResource一般支持如下资源访问：

- http：通过标准的http协议访问web资源，如new UrlResource(“http://地址”)；
- ftp：通过ftp协议访问资源，如new UrlResource(“ftp://地址”)；
- file：通过file协议访问本地文件系统资源，如new UrlResource(“file:d:/test.txt”)；

### ServletContextResource

ServletContextResource代表web应用资源，用于简化servlet容器的ServletContext接口的getResource操作和getResourceAsStream操作；

### VfsResource

VfsResource代表Jboss 虚拟文件系统资源。

Jboss VFS(Virtual File System)框架是一个文件系统资源访问的抽象层，它能一致的访问物理文件系统、jar资源、zip资源、war资源等，VFS能把这些资源一致的映射到一个目录上，访问它们就像访问物理文件资源一样，而其实这些资源不存在于物理文件系统。

## 访问Resource

### ResourceLoader接口

ResourceLoader接口用于返回Resource对象；其实现可以看作是一个生产Resource的工厂类。

Spring提供了一个适用于所有环境的DefaultResourceLoader实现，可以返回ClassPathResource、UrlResource；还提供一个用于web环境的ServletContextResourceLoader，它继承了DefaultResourceLoader的所有功能，又额外提供了获取ServletContextResource的支持。

ResourceLoader在进行加载资源时需要使用前缀来指定需要加载：“classpath:path”表示返回ClasspathResource，“http://path”和“file:path”表示返回UrlResource资源，如果不加前缀则需要根据当前上下文来决定，DefaultResourceLoader默认实现可以加载classpath资源。

对于目前所有ApplicationContext都实现了ResourceLoader，因此可以使用其来加载资源。

- ClassPathXmlApplicationContext：不指定前缀将返回默认的ClassPathResource资源，否则将根据前缀来加载资源；
- FileSystemXmlApplicationContext：不指定前缀将返回FileSystemResource，否则将根据前缀来加载资源；
- WebApplicationContext：不指定前缀将返回ServletContextResource，否则将根据前缀来加载资源；
- 其他：不指定前缀根据当前上下文返回Resource实现，否则将根据前缀来加载资源。

### ResourceLoaderAware接口

ResourceLoaderAware是一个标记接口，用于通过ApplicationContext上下文注入ResourceLoader。