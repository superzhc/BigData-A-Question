<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-06-11 09:58:54
 * @LastEditTime : 2021-02-05 16:33:29
 * @Copyright 2021 SUPERZHC
-->
# Livy

## 简介

[Apache Livy](http://livy.incubator.apache.org/)是一个基于Spark的开源REST服务，它能够以REST的方式将代码片段或是jar包提交到Spark集群中去执行。它提供了以下基本功能：

- 提交Java、Scala、Python代码片段到远端的Spark集群上执行
- 提交Java、Scala、Python所编写的Spark作业到远端的Spark集群上执行
- 提交批处理应用到集群中运行

Livy简化了spark与应用程序服务的交互，这允许通过web/mobile与spark的使用交互。其他特点还包含：

- 长时间运行的SparkContext，允许多个spark job和多个client使用
- 在多个spark job和客户端之间共享RDD和Dataframe
- 多个sparkcontext可以简单的管理，并运行在集群中而不是Livy Server，以此获取更好的容错性和并行度
- 作业可以通过重新编译的jar、片段代码、或Java/Scala的客户端API提交

Livy结合了spark job server和Zeppelin的优点，并解决了spark job server和Zeppelin的缺点

- 支持jar和snippet code
- 支持SparkContext和Job的管理
- 支持不同SparkContext运行在不同进程，同一个进程只能运行一个SparkContext
- 支持Yarn cluster模式
- 提供restful接口，暴露SparkContext

API地址：

- Java API地址：<https://livy.incubator.apache.org/docs/latest/api/java/index.html>

## 架构

Livy是一个典型的REST服务架构，它一方面接受并解析用户的REST请求，转换成相应的操作；另一方面它管理着用户所启动的所有的Spark集群。具体的架构如下图：

![](https://gitee.com/superzchao/GraphBed/raw/master/publish/Livy架构图.png)

用户可以以REST请求的方式通过Livy启动一个新的Spark集群，Livy将每一个启动的Spark集群称之为一个会话（session），并且通过RPC协议在Spark集群和Livy服务端之间进行通信。根据处理交互方式的不同，Livy将会话分成了两种类型：

- 交互式会话（interactive session）：这与Spark中的交互式处理相同，交互式会话在其启动后可以接收用户所提交的代码片段，在远端的spark集群上编译并执行
- 批处理会话（batch session）：用户可以通过Livy以批处理的方式启动Spark应用，这样的一个方式在Livy中称之为批处理会话，这与Spark中的批处理式相同的

可以看到，Livy所提供的核心功能与原生Spark是相同的，它提供了两种不同的会话类型来代替Spark中两类不同的处理交互方式。

## 交互式会话

使用交互式会话与使用Spark自带的spark-shell，pyspark或sparkR类似，它们都是由用户提交代码片段给REPL，由REPL来编译成Spark作业并执行。主要的不同点是spark-shell会在当前节点上启动REPL来接收用户的输入，而Livy交互式会话则是在远端的Spark集群中启动REPL，所有的代码、数据都需要通过网络来传输。

### 创建交互式会话

**POST /sessions**

```sh
curl -X POST -d '{}' -H "Content-Type:application/json" <livy-host>:<port>/sessions
```

使用交互式会话的前提是需要先创建会话。当前的Livy可在同一会话中支持spark、pyspark或是sparkR三种不同的解释器类型以满足不同语言的需求

当创建完会话后，Livy会返回给用户一个JSON格式的数据结构表示当前会话的所有信息：

```json
{
  "appId": "application_1493903362142_0005",
  …
  "id": 1,
  "kind": "shared",
  "log": [ ],
  "owner": null,
  "proxyUser": null,
  "state": "idle"
}
```

其中用户需要关注的是 **会话id**，id代表了此会话，所有基于该会话的操作都需要指明其id

### 提交代码

**POST /sessions/{sessionId}/statements**

```sh
curl <livy-host>:<port>/sessions/{sessionId}/statements -X POST -H 'Content-Type: application/json' -d '{"code":"sc.parallelize(1 to 2).count()", "kind": "spark"}'

{
  "id": 0,
  "output": null,
  "progress": 0.0,
  "state": "waiting"
}
```

创建完交互式会话后用户就可以提交代码到该会话上去执行。与创建会话相同的是，提交代码同样会返回给用户一个id用来标识该次请求，同样可以用id来查询该段代码执行的结果。

### 查询执行结果

**GET /sessions/{sessionId}/statements/{statementId}**

```json
{
  "id": 0,
  "output": {
    "data": {
      "text/plain": "res0: Long = 2"
    },
    "execution_count": 0,
    "status": "ok"
  },
  "progress": 1.0,
  "state": "available"
}
```

Livy的REST API设置为非阻塞的方式，当提交代码请求后Livy会立即返回该请求id而并非阻塞在该次请求上直到执行完成，因此用户可以使用该id来反复轮询结果，当然只有当该段代码执行完毕后，用户的查询请求才能得到正确的结果

### 使用编程API

在交互式会话模式中，Livy不仅可以接收用户提交的代码，而且还可以接收序列化的Spark作业。为此Livy提供了一套编程式的API供用户使用，用户可以像使用原生Spark API那样使用Livy提供的API编写Spark作业，Livy会将用户编写的Spark作业序列化并发送到远端Spark集群中执行。

## 批处理会话

在Spark应用中有一大类应用是批处理应用，这些应用在运行期间无须与用户进行交互，最典型的就是Spark Streaming流式应用。用户会将业务逻辑编译打包成jar包，并通过spark-submit启动Spark集群来执行业务逻辑。

```sh
./bin/spark-submit \
  --class org.apache.spark.examples.streaming.DirectKafkaWordCount \
  --master yarn \
  --deploy-mode cluster \
  --executor-memory 20G \
  /path/to/examples.jar
```

Livy也为用户带来相同的功能，用户可以通过REST的方式来创建批处理应用：

```sh
curl -H "Content-Type: application/json" -X POST -d '{ "file":"<path to application jar>", "className":"org.apache.spark.examples.streaming.DirectKafkaWordCount" }' <livy-host>:<port>/batches
```

通过用户所指定的className和file，Livy会启动Spark集群来运行该应用，这样一种方式就称之为批处理会话

## 代码开发

Livy提供了Java/Scala和Python API进行代码开发，允许应用程序在Spark集群上运行代码而无需维护本地Spark上下文。

> 下面以Java API为例

添加livy客户端的依赖到pom文件中：

```xml
<dependency>
  <groupId>org.apache.livy</groupId>
  <artifactId>livy-client-http</artifactId>
  <version>0.5.0-incubating</version>
</dependency>
```

> 为了能够编译使用Spark API的代码，还需要添加相应的Spark依赖项

```java
import java.util.*;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

import org.apache.livy.*;

public class PiJob implements Job<Double>, Function<Integer, Integer>,
  Function2<Integer, Integer, Integer> {

  private final int samples;

  public PiJob(int samples) {
    this.samples = samples;
  }

  @Override
  public Double call(JobContext ctx) throws Exception {
    List<Integer> sampleList = new ArrayList<Integer>();
    for (int i = 0; i < samples; i++) {
      sampleList.add(i + 1);
    }

    return 4.0d * ctx.sc().parallelize(sampleList).map(this).reduce(this) / samples;
  }

  @Override
  public Integer call(Integer v1) {
    double x = Math.random();
    double y = Math.random();
    return (x*x + y*y < 1) ? 1 : 0;
  }

  @Override
  public Integer call(Integer v1, Integer v2) {
    return v1 + v2;
  }

}
```

要使用Livy提交上面的代码，需要创建一个LivyClient实例，然后将代码上传到spark上下文。

```java
LivyClient client = new LivyClientBuilder()
  .setURI(new URI(livyUrl))
  .build();

try {
  System.err.printf("Uploading %s to the Spark context...\n", piJar);
  client.uploadJar(new File(piJar)).get();

  System.err.printf("Running PiJob with %d samples...\n", samples);
  double pi = client.submit(new PiJob(samples)).get();

  System.out.println("Pi is roughly: " + pi);
} finally {
  client.stop(true);
}
```