# ParameterTool

## 读取运行参数

Flink Job 支持传入参数，格式如下：

```
--brokers 127.0.0.1:9200
--username root
--password 123456
```

对于这种参数，在程序中可以使用 `ParameterTool.fromArgs(args)` 获取到所有的参数，若需要获取某个参数对应的值可以通过 `parameterTool.get("username")` 来获取。

## 读取系统属性

通过 `ParameterTool.fromSystemProperties()` 方法读取系统属性。

## 读取配置文件

```java
ParameterTool.fromPropertiesFile(...)
```

## 合并多个配置项

```java
paramterTool.mergeWith(paramterTool2)
```