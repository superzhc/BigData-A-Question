# Servlet

## 容器

Web服务器应用（如Apache）得到一个指向某servlet的请求（而不是其他请求，如请求一个普通的静态HTML页面），此时服务器不是把这个请求直接交给servlet本身，而是交给部署该servlet的容器，要由容器向servlet提供HTTP请求和响应，而且要由容器调用servlet的方法，如doPost()或doGet()。

容器用来管理和运行servlet。

容器提供：

- 通信支持：利用容器提供的方法，能让servlet与Web服务器对话，无需自行建立ServerSocket、监听端口、创建流等。
- 生命周期管理：容器控制着servlet的生与死。它会负责加载类、实例化和初始化servlet、调用servlet的方法，并使servlet实例能够被垃圾回收。
- 多线程支持：容器会自动地为它接收的每个servlet请求创建一个新的Java线程
- 声明方式实现安全：利用容器，可以使用xml部署描述文件来配置（和修改）安全性，而不必将其硬编码写到servlet（或其他）类代码中。
- JSP支持

容器处理请求的过程：

1. 浏览器发起请求，请求指向一个servlet而不是静态页面
2. 容器对servlet请求创建两个对象：
    - HttpServletRequest
    - HttpServletResponse
3. 容器根据请求中的Url找到正确的servlet，为这个请求创建或分配一个线程，并把请求和响应对象传递给这个servlet线程
4. 容器调用servlet的service()方法。根据请求的不同类型，`service()`方法会调用`doGet()`或`doPost()`方法
5. `do..()`方法生成动态页面，并把这个页面填入响应对象。注：容器还有一个响应对象的一个引用。
6. 线程结束，容器把响应对象转换为一个HTTP响应，把它发回给客户，然后删除请求和响应对象。

将servlet部署到Web容器时，会创建一个简单的XML文档，这称为部署描述文件(DD)，容器通过部署描述文件来运行servlet和JSP。除了把URL映射到实际servlet，还可以使用部署描述文件对Web应用的其他方面进行定制，包括安全角色、错误页面、标记库、初始化配置信息等。

部署描述文件(DD)提供了一种"声明"机制来定制Web应用，而无需修改源代码。

用于URL映射的两个部署描述文件的元素如下：

- `<servlet>`:内部名映射到完全限定类名
- `<servlet-mapping>`：内部名映射到公共URL名

Servlet的生命周期：

- 容器加载servlet类文件
- `init()`：servlet实例创建后，并在servlet能为客户请求提供服务之前，容器要对servlet调用`init()`

注：任何servlet类都不会有多个实例，只有一个特殊情况例外(称为SingleThreadModel)

> 容器运行多个线程来处理对一个servlet的多个请求

对应每个请求，会生成一对新的请求和响应对象。

## ServletConfig

- 每个servlet都有一个ServletConfig对象
- 用于向servlet传递部署时信息
- 用于访问ServletContext
- 参数在部署描述文件中配置

## ServletContext

- 每个Web应用有一个ServletContext
- 用于访问Web应用参数（也在部署描述文件中配置）
- 相当于一种应用公告栏，可以在这里放置消息（称为属性），应用的其他部分可以访问这些消息
- 用于得到服务器信息，包括容器名和容器版本，以及所支持API的版本等