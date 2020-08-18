## Filter

### 简介

Filter也称之为过滤器，它是Servlet技术中最激动人心的技术之一，WEB开发人员通过Filter技术，对web服务器管理的所有web资源：例如Jsp,Servlet, 静态图片文件或静态html文件等进行拦截，从而实现一些特殊的功能。例如实现URL级别的权限访问控制、过滤敏感词汇、压缩响应信息等
一些高级功能。

Servlet API中提供了一个Filter接口，开发web应用时，如果编写的Java类实现了这个接口，则把这个java类称之为过滤器Filter。通过Filter技术，开发人员可以实现用户在访问某个目标资源之前，对访问的请求和响应进行拦截，Filter接口源代码：

```java
public abstract interface Filter{
    public abstract void init(FilterConfig paramFilterConfig) throws ServletException;
    public abstract void doFilter(ServletRequest paramServletRequest, ServletResponse paramServletResponse, FilterChain paramFilterChain) throws IOException, ServletException;
    public abstract void destroy();
}
```

### 工作原理

Filter接口中有一个doFilter方法，当我们编写好Filter，并配置对哪个web资源进行拦截后，WEB服务器每次在调用web资源的service方法之前，都会先调用一下filter的doFilter方法，因此，在该方法内编写代码可达到如下目的：

- 调用目标资源之前，让一段代码执行。
- 是否调用目标资源（即是否让用户访问web资源）。
- 调用目标资源之后，让一段代码执行。

web服务器在调用doFilter方法时，会传递一个filterChain对象进来，filterChain对象是filter接口中最重要的一个对象，它也提供了一个doFilter方法，开发人员可以根据需求决定是否调用此方法，调用该方法，则web服务器就会调用web资源的service方法，即web资源就会被访问，否则web资源不会被访问。

### Filter开发流程

#### 开发步骤

1. 编写java类实现Filter接口，并实现其doFilter方法。
2. 在web.xml 文件中使用\<filter\>和\<filter-mapping\>元素对编写的filter类进行注册，并设置它所能拦截的资源。

#### Filter链

在一个web应用中，可以开发编写多个Filter，这些Filter组合起来称之为一个Filter链。web服务器根据Filter在web.xml文件中的注册顺序，决定先调用哪个Filter，当第一个Filter的doFilter方法被调用时，web服务器会创建一个代表Filter链的FilterChain对象传递给该方法。在doFilter方法中，开发人员如果调用了FilterChain对象的doFilter方法，则web服务器会检查FilterChain对象中是否还有filter，如果有，则调用第2个filter，如果没有，则调用目标资源。

### Filter的生命周期

#### Filter的创建

Filter的创建和销毁由web服务器负责。 web应用程序启动时，web服务器将创建Filter的实例对象，并调用其init方法，完成对象的初始化功能，从而为后续的用户请求作好拦截的准备工作，filter对象只会创建一次，init方法也只会执行一次。通过init方法的参数，可获得代表当前filter配置信息的FilterConfig对象。

#### Filter的销毁

web容器调用destroy方法销毁Filter。destroy方法在Filter的生命周期中仅执行一次。在destroy方法中，可以释放过滤器使用的资源。

#### FilterConfig接口

用户在配置filter时，可以使用\<init-param\>为filter配置一些初始化参数，当web容器实例化Filter对象，调用其init方法时，会把封装了filter初始化参数的filterConfig对象传递进来。因此开发人员在编写filter时，通过filterConfig对象的方法，就可获得：

- String getFilterName()：得到filter的名称。
- String getInitParameter(String name)： 返回在部署描述中指定名称的初始化参数的值。如果不存在返回null.
- Enumeration getInitParameterNames()：返回过滤器的所有初始化参数的名字的枚举集合。
- public ServletContext getServletContext()：返回Servlet上下文对象的引用。

### Filter部署

1. 注册Filter
2. 映射Filter

#### 注册Filter

开发好Filter之后，需要在web.xml文件中进行注册，这样才能够被web服务器调用。

注册filter中参数：

- \<description\>用于添加描述信息，该元素的内容可为空，\<description\>可以不配置。
- \<filter-name\>用于为过滤器指定一个名字，该元素的内容不能为空。
- \<filter-class\>元素用于指定过滤器的完整的限定类名。
- \<init-param\>元素用于为过滤器指定初始化参数，它的子元素\<param-name\>指定参数的名字，\<param-value\>指定参数的值。在过滤器中，可以使用FilterConfig接口对象来访问初始化参数。如果过滤器不需要指定初始化参数，那么\<init-param\>元素可以不配置。

#### 映射Filter

在web.xml文件中注册了Filter之后，还要在web.xml文件中映射Filter。

映射filter的参数：

- \<filter-mapping\>元素用于设置一个 Filter 所负责拦截的资源。一个Filter拦截的资源可通过两种方式来指定：Servlet 名称和资源访问的请求路径
- \<filter-name\>子元素用于设置filter的注册名称。该值必须是在\<filter\>元素中声明过的过滤器的名字
- \<url-pattern\>设置 filter 所拦截的请求路径(过滤器关联的URL样式)
- \<servlet-name\>指定过滤器所拦截的Servlet名称
- \<dispatcher\>指定过滤器所拦截的资源被 Servlet 容器调用的方式，可以是REQUEST,INCLUDE,FORWARD和ERROR之一，默认REQUEST。用户可以设置多个\<dispatcher\> 子元素用来指定 Filter 对资源的多种调用方式进行拦截。

## Listener

Servlet的监听器Listener，它是实现了javax.servlet.ServletContextListener 接口的服务器端程序，它也是随web应用的启动而启动，只初始化一次，随web应用的停止而销毁。

监听器用于监听web应用中某些对象、信息的创建、销毁、增加，修改，删除等动作的发生，然后作出相应的响应处理。当范围对象的状态发生变化的时候，服务器自动调用监听器对象中的方法。常用于统计在线人数和在线用户，系统加载时进行信息初始化，统计网站的访问量等等。

分类：

- 按监听的对象划分，可以分为：
    - ServletContext对象监听器
    - HttpSession对象监听器
    - ServletRequest对象监听器
- 按监听的事件划分：
    - 对象自身的创建和销毁的监听器
    - 对象中属性的创建和消除的监听器
    - session中的某个对象的状态变化的监听器

```java
public abstract interface ServletContextListener extends EventListener{
    public abstract void contextInitialized(ServletContextEvent paramServletContextEvent);
    public abstract void contextDestroyed(ServletContextEvent paramServletContextEvent);
}
```

**注：** 在web.xml中配置监听器，监听器>过滤器>serlvet，配置的时候要注意先后顺序

## Interceptor

### 拦截器的概念

Java里的拦截器是动态拦截Action调用的对象，它提供了一种机制可以使开发者在一个Action执行的前后执行一段代码，也可以在一个Action执行前阻止其执行，同时也提供了一种可以提取Action中可重用部分代码的方式。在AOP中，拦截器用于在某个方法或者字段被访问之前，进行拦截然后在之前或者之后加入某些操作。

### 拦截器的原理

大部分时候，拦截器方法都是通过代理的方式来调用的。Struts2的拦截器实现相对简单。当请求到达Struts2的ServletDispatcher时，Struts2会查找配置文件，并根据配置实例化相对的拦截器对象，然后串成一个列表（List），最后一个一个的调用列表中的拦截器。Struts2的拦截器是可插拔的，拦截器是AOP的一个实现。Struts2拦截器栈就是将拦截器按一定的顺序连接成一条链。在访问被拦截的方法或者字段时，Struts2拦截器链中的拦截器就会按照之前定义的顺序进行调用。

### 自定义拦截器的步骤

1. 自定义一个实现了Interceptor接口的类，或者继承抽象类AbstractInterceptor。
2. 在配置文件中注册定义的拦截器。
3. 在需要使用Action中引用上述定义的拦截器，为了方便也可以将拦截器定义为默认的拦截器，这样在不加特殊说明的情况下，所有的Action都被这个拦截器拦截。

### 过滤器与拦截器的区别

过滤器可以简单的理解为“取你所想取”，过滤器关注的是web请求；拦截器可以简单的理解为“拒你所想拒”，拦截器关注的是方法调用，比如拦截敏感词汇。

- 拦截器是基于java反射机制来实现的，而过滤器是基于函数回调来实现的。（有人说，拦截器是基于动态代理来实现的）
- 拦截器不依赖servlet容器，过滤器依赖于servlet容器。
- 拦截器只对Action起作用，过滤器可以对所有请求起作用。
- 拦截器可以访问Action上下文和值栈中的对象，过滤器不能。
- 在Action的生命周期中，拦截器可以多次调用，而过滤器只能在容器初始化时调用一次。

