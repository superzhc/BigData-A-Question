# 监控 executor 的 jvm 运行情况

如果想查看每个 executor 的 jvm 运行情况，可以开启 jmx。在 `/etc/spark/conf/spark-defaults.conf` 添加下面一行代码：

```sh
spark.executor.extraJavaOptions 
	-Dcom.sun.management.jmxremote.port=1099 
	-Dcom.sun.management.jmxremote.ssl=false 
	-Dcom.sun.management.jmxremote.authenticate=false
```

然后，通过 jconsole 监控 jvm 堆内存运行情况，这样方便调试内存大小