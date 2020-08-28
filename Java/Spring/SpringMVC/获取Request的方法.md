# SpringMVC 中获取 Request 的几种方法

<https://mp.weixin.qq.com/s/guxNp4HOL5gT64F6hzxUOQ>

## 概述

在使用Spring MVC开发Web系统时，经常需要在处理请求时使用request对象，比如获取客户端ip地址、请求的url、header中的属性（如cookie、授权信息）、body中的数据等。由于在Spring MVC中，处理请求的Controller、Service等对象都是单例的，因此获取request对象时最需要注意的问题，便是request对象是否是线程安全的：当有大量并发请求时，能否保证不同请求/线程中使用不同的request对象。

获取request对象的方法有微小的不同，大体可以分为两类：

- 在Spring的Bean中使用request对象：既包括Controller、Service、Repository等MVC的Bean，也包括了Component等普通的Spring Bean。
- 在非Bean中使用request对象：如普通的Java对象的方法中使用，或在类的静态方法中使用。

注：本文讨论是围绕代表请求的request对象展开的，但所用方法同样适用于response对象、InputStream/Reader、OutputStream/ Writer等；其中InputStream/Reader可以读取请求中的数据，OutputStream/ Writer可以向响应写入数据。