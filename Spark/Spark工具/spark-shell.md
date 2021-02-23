<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2019-10-08 11:41:38
 * @LastEditTime : 2021-02-05 16:46:04
 * @Copyright 2021 SUPERZHC
-->
# `spark-shell`

`spark-shell` 作为一个强大的交互式数据分析工具，提供了一个简单的方式来学习 API。

在 Spark 目录里使用下面的方式开始运行：

```sh
./bin/spark-shell
```

`spark-shell` 启动的时候会自动创建 SparkContext 以及 SparkSession 的实例，变量名分别为 **`sc`** 和 **`spark`**，可以在 `spark-shell` 中直接使用。

输入`:help` 可以查看 `spark-shell` 的命令行帮助

输入 `:quit` 可以退出 `spark-shell`

**用户也可以使用 `bin/spark-shell` 连接到一个 cluster 来进行交互操作**

