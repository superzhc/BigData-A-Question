# Fiddler

## Fiddler抓包原理

Fiddler抓包工具的原理是基于代理的机制，启动Fiddler就会自动改写浏览器代理，浏览器访问服务器的请求会先发送到Fiddler，再由Fiddler转发给服务器，同样，服务器http响应也会先返回至Fiddler，Fiddler再转发给客户端浏览器。

![Fiddler 抓包原理](./images/Fiddler原理图.jpg)