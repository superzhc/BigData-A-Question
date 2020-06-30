# Request and Response

> HttpServletRequest和HTTPServletResponse的接口实现是由容器来实现的。

在创建 Servlet 时会覆盖 service() 方法，或者 doGet()/doPost()，这些方法都有两个参数，一个为代表请求的 request 和代表响应的 response。

service 方法中的 request 的类型是 ServleRequest，而 doGet()/doPost() 方法的 request 的类型是 HTTPServletRequest 是 ServletRequest 的子接口，功能和方法更加强大。

## Request

### HttpServletRequest 概述

因为 request 代表请求，所以可以通过该对象分别获得 Http 请求的请求行，请求头和请求体。

常用方法：

- 获取请求参数：`request.getParameter("param1")`
- 客户的平台和浏览器信息：`request.getHeader("User-Agent")`
- 与请求相关的cookie：`request.getCookies()`
- 与客户相关的会话（session）：`request.getSession()`
- 请求的HTTP方法：`request.getMethod()`
- 请求的输入流：`request.getInputStream()`

> 转发和重定向的区别

1. 重定向两次请求，转发一次请求
2. 重定向地址栏的地址变化，转发地址不变
3. 重定向可以访问外部网站，转发只能访问内部资源
4. 转发的性能要优于重定向