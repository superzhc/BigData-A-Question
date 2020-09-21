# Livy

## 简介

我们平时提交的spark任务，通常是使用Apache Spark本身提供的spark-submit、spark-shell和Thrift Server外，Apache Livy提供了另外一种与Spark集群交互的方式，通过REST接口。

Apache Livy 支持同时维护多个会话,可以通过REST接口、Java/Scala 库和 Apache Zeppelin 访问 Apache Livy。

## 架构

![img](images/5934d153b0cf4.jpg)

Livy是一个典型的REST服务架构，一方面接收并解析用户的REST请求，转换成相应的操作；另一方面管理着用户所启动的所有Spark集群。

用户可以以REST请求方式通过Livy启动一个会话(session),一个会话是由一个spark集群所构成的，并且通过RPC协议在Spark集群和Livy服务端之间进行通信。根据处理交互方式不同，Livy将会话分为两种类型:

1. **交互式会话(interactive session)**:跟spark的交互处理相同，在启动会话后可以接收用户所提交的代码片段,提交至远程的spark集群编译并执行
2. **批处理会话(batch session)**:用户可以通过livy以批处理的方式启动spark应用,这样的一个方式在Livy中称之为批处理会话，与spark中的批处理是相同的