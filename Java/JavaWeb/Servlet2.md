---
title: JavaWeb
date: 2017-12-20
tags: java
---
# Java Web

## Servlet

Servlet 是一些遵从Java Servlet API的Java类，这些Java类可以响应请求。尽管Servlet可以响应任意类型的请求，但是它们使用最广泛的是响应web方面的请求。 Servlet必须部署在Java servlet容器才能使用。虽然很多开发者都使用Java Server Pages（JSP）和Java Server Faces（JSF）等Servlet框架，但是这些技术都要在幕后通过Servlet容器把页面编译为Java Servlet。

### Servlet生命周期事件（或方法）

Servlet生命周期的三个核心方法分别是 init() , service() 和 destroy()。每个Servlet都会实现这些方法，并且在特定的运行时间调用它们。

1. 在Servlet生命周期的初始化阶段，web容器通过调用init()方法来初始化Servlet实例，并且可以传递一个实现 javax.servlet.ServletConfig 接口的对象给它。这个配置对象（configuration object）使Servlet能够读取在web应用的web.xml文件里定义的名值（name-value）初始参数。这个方法在Servlet实例的生命周期里只调用一次。
2.  初始化后，Servlet实例就可以处理客户端请求了。web容器调用Servlet的service()方法来处理每一个请求。service() 方法定义了能够处理的请求类型并且调用适当方法来处理这些请求。编写Servlet的开发者必须为这些方法提供实现。如果发出一个Servlet没实现的请求，那么父类的方法就会被调用并且通常会给请求方（requester）返回一个错误信息。
3. 最后，web容器调用destroy()方法来终结Servlet。destroy() 方法和init()方法一样，在Servlet的生命周期里只能调用一次。

### 使用@WebServlet注解来开发Servlet

使用 @WebServlet 注解并且不需要在web.xml里为Servlet注册任何信息。容器会自动注册你的Servlet到运行环境，并且像往常一样处理它。

```java
package com.howtodoinjava.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "MyFirstServlet", urlPatterns = {"/MyFirstServlet"})
public class MyFirstServlet extends HttpServlet {

    private static final long serialVersionUID = -1915463532411657451L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException
    {
        //Do some work
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        //Do some other work
    }
}
```

### 处理Servlet请求和响应

Servlet可以轻松创建一个基于请求和响应生命周期的web应用。它们能够提供HTTP响应并且可以使用同一段代码来处理业务逻辑。处理业务逻辑的能力使Servlet比标准的HTML代码更强大。

使用 HttpServletRequest 对象的 getParameter() 方法，获取请求参数。使用 HttpServletResponse 对象给客户端发送响应。

### 监听Servlet容器事件

为了创建一个基于容器事件执行动作的监听器，必须创建一个实现 ServletContextListener 接口的类。这个类必须实现的方法有 contextInitialized() 和 contextDestroyed()。这两个方法都需要 ServletContextEvent 作为参数，并且在每次初始化或者关闭Servlet容器时都会被自动调用。

为了在容器注册监听器，可以使用下面其中一个方法：

- 利用 @WebListener 注解。
- 在web.xml应用部署文件里注册监听器。
- 使用 ServletContext 里定义的 addListener() 方法。

### 传递Servlet初始化参数

Servlet可以接受初始化参数，并在处理第一个请求前来使用它们来构建配置参数。

```xml
<web-app>
    <servlet>
        <servlet-name>SimpleServlet</servlet-name>
        <servlet-class>com.howtodoinjava.servlets.SimpleServlet</servlet-class>

        <!-- Servlet init param -->
        <init-param>
            <param-name>name</param-name>
            <param-value>value</param-value>
        </init-param>

    </servlet>
</web-app>
```

**注：** 在Servlet配置的参数发生改动时需要再次重新编译整个应用。

在代码中调用使用getServletConfig.getInitializationParameter() 并传递参数名给该方法来使用参数。`String value = getServletConfig().getInitParameter("name");`

### 为特定的URL请求添加Servlet过滤器

过滤器必须要实现 javax.servlet.Filter 接口。这个接口包含了init()，descriptor()和doFilter()这些方法。init()和destroy()方法会被容器调用。 doFilter()方法用来在过滤器类里实现逻辑任务。如果想把过滤器组成过滤链（chain filter）或者存在多匹配给定URL模式的个过滤器，它们就会根据web.xml里的配置顺序被调用。

为了在web.xml里配置过滤器，需要使用\<filter\>和\<filter-mapping\> XML元素以及相关的子元素标签。

```xml
<filter>
    <filter-name>LoggingFilter</filter-name>
    <filter-class>LoggingFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>LogingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

**注：** 也可以@WebFilter注解为特定的servlet配置过滤器。

### 使用RequestDispatcher.forward()转发请求到另一个Servlet

有时候，应用需要把一个Servlet要处理的请求转让给另外的Servlet来处理并完成任务。而且，转让请求时不能重定向客户端的URL。即浏览器地址栏上的URL不会改变。

在 ServletContext 里已经内置了实现上面需求的方法。所以，当获取了 ServletContext 的引用，就可以简单地调用getRequestDispatcher() 方法去获取用来转发请求的 RequestDispatcher 对象。当调用 getRequestDispatcher() 方法时，需要传递包含servlet名的字符串，这个Servlet就是用来处理转让请求的Servlet。获取 RequestDispatcher 对象后，通过传递 HttpServletRequest 和HttpServletResponse 对象给它来调用转发方法。转发方法负责对请求进行转发。

```java
RequestDispatcher rd = servletContext.getRequestDispatcher("/NextServlet");
rd.forward(request, response);
```

### 使用HttpServletResponse.sendRedirect()重定向请求到另一个Servlet

在某些情况下，我们确实想要通知用户。当应用内的特定URL被访问时，你想把浏览器的URL重定向到另外一个。要实现这种功能，你需要调用 HttpServletResponse 对象的sendRedirect()方法。`httpServletResponse.sendRedirect("/anotherURL");`