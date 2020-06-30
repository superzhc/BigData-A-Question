常用选项：

#### `--class`

指向程序中的主类

```sh
--class "helloworld"
```

#### `--master`

设置集群的 master URL

**本地运行**

```sh
--master local # 这是在本地运行
--master local [K] # 这是在本地运行并且启动K（整数类型，如1，2，3）个 worker 线程，如果 K 设置为 * （这样：--master local [*]）则表示使用机器上的所有逻辑核心数作为参数K的值
--master local [K,F] # 这是在本地运行并且启动K个worker线程（这里的K也可以设置 *），并且设置任务的最大出错数为F次
```

使用这种方式由于是直接在本地执行的，并没有提交集群上，所以Web UI中看不到提交的任务。

**standalone模式**

```sh
--master spark://host:port # host是master的ip或者hostname，这里的端口默认是7077，这个端口可以在 Web UI 界面中看到的
--master spark://host1:port1,host2:port2 # 如果有备用的 master 则可以这样写，在不同的 master 之间用逗号隔开
```

**mesos作为资源管理器**

```sh
--master mesos://host:port # 跟standalone的写法类似
```

**Yarn作为资源管理器**

```sh
--master yarn # 如果使用这种方式则需要设置好 Hadoop 的 HADOOP_CONF_DIR 和 YARN_CONF_DIR 环境变量
```

#### `--deploy-mode`

设置以什么模式运行，有 cluster 和 client 两种，默认是 client 模式

#### `--conf`

配置属性的键和值

使用模板：

```sh
./bin/spark-submit \
--class <main-class> \
--master <master-url> \
--deploy-mode <deploy-mode> \
--conf <key>=<value> \
... # 其他参数，详细信息参考 spark-submit --help
<jar的路径>
```



