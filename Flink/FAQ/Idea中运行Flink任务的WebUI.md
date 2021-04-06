# Idea 中运行 Flink 任务的 WebUI

Flink 任务是可以在 Idea 中真正执行起来的，但是 WebUI 通常是无法打开的，会提示 `{errors:["Not found"]}`，这是因为缺少了相关的 jar 包，添加如下依赖即可使用 Web UI：

```xml
<dependency>
   <groupId>org.apache.flink</groupId>
   <artifactId>flink-runtime-web_${scala.binary.version}</artifactId>
   <version>${flink.version}</version>
</dependency>
```