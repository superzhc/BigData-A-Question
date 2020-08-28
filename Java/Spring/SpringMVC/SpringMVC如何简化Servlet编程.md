# SpringMVC 如何简化 Servlet 编程

## 在 Servlet 中

在应用程序目录下是 WEB-INF 目录，它包含了 classes 子目录，Servlet 类以及其他的 Java 类必须放在这个下面，在最基本的 Servlet 类中，需要实现 Servlet 接口定义的 `init()`,`service()`,`destory()`,`getServletConfig()` 和 `getServletInfo()` 方法，其中最基本的逻辑方法 `service()` 方法中进行编写，在 `service()` 方法中最常用的是会通过 PrintWriter 进行内容的输出。

> 演进1：实现 Servlet 接口的时候必须将所有的方法进行实现，即便有些根本没有包含任何代码。但是 GenericServlet 抽象类实现了 Servlet 和 ServletConfig 接口简化了任务。

在 GenericServlet 中，只需要实现 service 方法来完成功能即可。

> 演进2：GenericServlet 并不常用，因为 HttpServlet 才是主角，并且不需要覆盖 `service()` 方法而是 `doGet()` ，`doPost()` 来编写逻辑。

HttpServlet 覆盖了 GenericServlet 类，它将 ServletRequest 和 ServletRespond 对象分别转换成了 HttpServletRequest 和 HttpServletRespond 对象，并调用最常用的 `doGet()`（从服务器端向客户端呈现），`doPost()`（从客户端获得到服务器端处理）等七种方法而不需要重写 service 方法。

利用部署描述符是一种配置 Servlet 应用程序的方法，部署描述符命名为 `web.xml` 并放在 WEB-INF 目录下。

Servlet 还提供了四种状态保持技术：

- URL重写
- 隐藏域
- cookies
- HTTPSession[其中HTTPSession是最常用的]

## JSP

> 演进3：Servlet 有两个缺点：1）写在 Servlet 中的所有HTML标签必须包含java字符串似的处理HTTP响应报文工作复杂；2）所有的文本都是硬编码，即是出现了一点点的变化也需要重现编译。JSP解决了上述的问题并与Servlet同时使用。

JSP 本质上是一个 Servlet，然而其不需要编译，JSP页面是一个以.jsp扩展名的文本文件。简单的JSP页面在第一次请求后被翻译为（JSP名）_jsp的Servlet，翻译之后的Servelt可以看到：`_jspInit()`，`_jspDestory()`，`_jspService()`这样的方法其实都是和Servlet相对应的。

放在WEB-INF文件夹下的内容是无法直接通过浏览器输入地址访问的，而WEB-INF文件夹外的则是可以的，并且添加了新的JSP页面后无需重启JSP/Servlet容器（如tomcat）。

**解耦1：** 使用标准JSP访问，操作JavaBean，是实现展现（HTML）与业务实现（Java 代码）分离的第一步。

> 演进4：JSP中的EL可以轻松访问应用程序数据，使得JSP页面不需要任何的声明，表达式和脚本。

EL表达式${expression}以及取值[]和.运算符。

> 演进5：JSP标准标签库（JSTL）在EL的基础上进一步解决了遍历Map，集合，条件测试，XML处理，数据库操作访问等操作的问题。

使用JSTL需要taglib指令：

`<%@ taglib uri="uri" prefix="prefix" %>`

JSTL标签类型：声明赋值，条件判断，循环遍历，格式化，函数（主要是字符串函数）

> 演进6：JSP标准标签库（JSTL）提供了一些标签能解决常用的问题，但是对于一些非常见恶问题，需要扩展 `javax.servlet.jsp.tagetx` 包中的成员实现自定义标签。

自定义标签的实现，叫作标签处理器，而简单标签处理器是指继承 SimpleTag 实现的经典自定义标签。经典标签处理器需要实现 Tag， IterationTag 及 BodyTag 接口或者扩展 TagSupport，BodyTagSupport 两个类；简单标签处理器需要实现

在构建标签处理器是，需要在构建目录中有Servlet API及JSP API（servlet-api.jar和jsp-api.jar）这两个文件。自定义标签由组件处理器（WEB-INF/classes）及标签描述器（WEB-INF中的.tld）文件组成。同样也需要taglib指令使用自定义标签。

可以把自定义的标签处理器以及标签描述器打包到jar包中，并指定绝对的URI，这样就可以把它像JSTL一样发布出来。

> 演进7：编写自定义标签是一件冗长琐碎的事，需要编写并变异一个标签处理类还要在标签库中进行描述。通过tag file的方式，无须辨析标签处理类和标签库描述文件也能够自定义标签。tag file使用前不需要编译，也不需要描述文件。

tag file无需提前编译且只需要JSP语法就可以。一个tag file拥有指令，脚本，EL，动作元素以及自定义标签，一个tag file以tag和tagx为后缀，它们可以包含其他资源，一个被其他文件包含的tag file应该以tagf为后缀。

tag文件必须放在路径的WEB-INF/tags目录下才能生效，和标签处理器一样，tag文件也可以打包成jar文件。

**解耦2：** Servlet提供了一系列的事件和事件监听接口，上层的servlet/JSP应用能够通过调用这些API进行事件驱动开发。

监听器都继承自java.util.Event对象，监听器接口可以分为ServletContext，HttpSession和ServletRequest。监听器即一组动作的接口。编写一个监听器，只需要写一个java类来实现对应的监听器接口就可以了，然后通过 `@WebListener` 注解或者部署描述文档中增加listener元素进行注册。

> 演进8：使用 Filter 来拦截Request的请求，在用户的请求访问资源前处理ServletRequest以及ServletResponse可以实现日志记录，加解密，session检查和图像文件保护。

Filter实现需要实现javax.servlet.Filter接口，需要实现 `init()`，`doFilter()`，`destroy()` 方法。Filter的配置可以通过 `@WebFilter` 或部署描述中的filter元素进行配置。Filter的使用需要考虑到Filter Chain的实现顺序和规则，在部署描述符中，先配置的先执行。

> 演进9： 修饰Request和Response实现Decorator模式

> 演进10： Servlet或者Filter占用请求处理线程，如果任务需要很长时间才能完成，当用户的并发请求超过线程树，容器会没有可用的线程。Servlet使用超时时间处理异步请求，释放正在等待完成的线程。

> 演进11： 尽管可以通过注解进行配置，但是在需要更加精细配置的情况下，部署描述符依然是需要的。部署描述符必须被命名为 `web.xml` 并且位于WEB-INF目录下，Java类必须放在WEB-INF/classes目录下，而Java的类库必须位于WEB-INF/lib目录下。所有的应用资源必须打包成.war为后缀的JAR文件。

> 演进12： web fragment可以实现在已有的web应用中部署插件和框架。

## SpringMVC 的实现

> 演进13： Servlet的动态加载可以实现在不重启web应用的前提下，添加新的web对象，Servlet容器加载器可以以插件形式发布应用而不需要修改部署描述，对框架的使用特别有用。

ServletContext接口中提供的（创建，注册，使用）（Filter，Listener，Servlet）的方法。

initializer库是一个插件化的框架，有两个资源MyServletContainerInitializer类以及javax.servlet.ServletContainerInitializer的元文件，这个元文件必须放在WEB-INF/services目录下，这个元文件只有一行：initializer.MyServletContainerInitializer的实现类名。

> 演进13： Spring作为开源的轻量级企业级应用开发框架，提供了依赖注入方法的实现。依赖注入是一种代码可测试性的解决方案。

简单来说，有两个组件A和B，A依赖于B，假定A是一个类且又一个方法使用到了B，那么A必须先获得组件B的实例引用。Spring的依赖注入会先创建B的实例，再创建A的实例，然后把B注入到A的实例中。

Spring XML的配置写在 `spring-config.xml` 文件中，配置文件可以是一份，也可以分解为多份以支持模块化的配置，既可以通过主配置文件读取多份配置文件，也可以在其他配置文件中读取主配置文件。

Spring创建控制反转容器可以通过构造器的方式，也可以是setter方法。

Spring MVC使用Servlet充当控制器，Structs2使用Filter充当控制器。大部分都采用JSP页面作为视图。模型采用POJO（Plain Old Java Object），在实践中会采用一个JavaBean来持有模型的状态，并将业务逻辑放到一个Action类中，一个JavaBean必须拥有一个无参的构造器，通过getter/setter访问参数，同时支持持久化。

> 演进14： 在Web应用执行action时，需要进行输入的校验，编程式的校验通过编码进行用户输入校验，声明式提供包含校验规则的XML文档或者属性文件。

> 演进15：在应用MVC时，可以在Controller类中调用后端业务逻辑。通常后段封装了复杂的逻辑service类，在service类中，可以实例化一个DAO类来访问数据库。在Spring环境中，Service对象可以自动被注入到Controller实例中，而DAO对象可以自动被注入到Service对象中。

## Spring MVC的优势

采用Spring MVC的优势：

1. 不需要编写DispatcherServlet；
2. 基于XML的文件配置不需要重新编译；
3. 可以实例化控制器，并根据用户的输入来构造bean；
4. 可以自动绑定用户输入，并正确进行数据类型的转换；
5. 可以进行用户输入的校验，可以重定向回输入表单，支持编程式校验和声明式校验；
6. 作为Spring框架的一部分，可以实现其他Spring提供的功能；
7. 支持国际化和本地化；
8. 支持多视图技术（JSP，FreeMarker，Velocity）。

> 演进16：Spring MVC自带一个开箱即用的Dispatcher Servlet。并提供了Controller接口并公开了handleRequest方法。

要使用这个Servlet，需要在部署描述符中进行配置，并且会寻找一个应用程序的WEB-INF目录下的配置文件servletName-servlet.xml。controller需要实现org.springframework.web.servlet.mvc.Controller，Controller接口的实现类职能处理一个单一动作。

> 演进17：Spring MVC使用视图解析器负责解析视图，可以通过在配置文件中定义一个ViewResolver来配置试图解析器。

springmvc-config.xml实现了Dispatcher Servlet和ViewResolver的配置。同时也需要在部署描述符中进行配置。

> 演进18：使用基于注解的控制器配置方法可以使得一个控制器类处理多个Action。

- @Controller注解类型用于指示Spring类的实例是一个控制器；
- @RequestMapping注解可以为控制器内部的每一个动作开发相应的处理方法；
- @Autowired和@Service注解可以将依赖注入到控制器内；
- @ModelAttribute注解可以实现使用Spring MVC每次调用请求处理方法产生的Model类型实例。

> 演进19：数据绑定是将用户的输入绑定到领域模型的一种特性，也不再需要form bean这样的表单bean。表单的标签库会辅助这样的工作。

使用表单标签库需要声明taglib指令。

> 演进20：由于Spring自身的数据绑定是杂乱无章的，需要通过Converter和Formatter来完成数据的绑定。

Converter是通用元件，可以在应用程序的任意层中使用，而Formatter则是专门为Web层设计的。

需要实现的Converter接口和Formatter接口。同时也需要在springmvc-config.xml中进行注册。

> 演进21：Converter和Formatter只是作用于field级，Validator可以作用于object级。在调用Controller期间，将会有一个或者多个Formatter视图进行field值的变换，一旦格式化成功，Validator将会介入。

Spring MVC中有两种用户输入验证方式：Spring自带的验证框架和JSR 303的实现。前者需要实现Validator接口，并且需要调用reject方法来添加错误；后者则是通过注解给对象属性添加约束。这样的验证器不需要显式注册，但如果想要从某个属性文件中获取错误信息可以在springmvc-config.xml中进行添加。

> 演进22：Spring MVC提供了国际化和本地方的支持，需要讲文本元文件隔离成属性文件。

将每一个语言区域的文本元素都单独保存在一个独立的属性文件中，每个文件都包含key/value对，并且每个key都卫衣表示一个特定语言区域对象。并在springmvc-config.xml中进行配置。