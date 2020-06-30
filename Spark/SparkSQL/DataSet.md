Spark 从 1.6 版本起，引入了一个实验性的接口，名为 DataSet。提供该接口的目的是为了在 RDD 的特性上，再集成 Spark SQL 的一些优化操作引擎。DataSet 可以由 JVM 对象构造，并且可以通过 map、flatMap、filter 等函数对其进行计算。

## Select 函数

检索函数 `select()` 是Spark SQL 中最常用的函数。

Spark SQL API 中涉及到检索的函数主要有：

- `select(col:string,cols:string*)`:该函数基于已有的列名进行查询，返回一个 DataFrame 对象。使用方法如 `df.select($"colA", $"colB") `
- `select(cols:Column*)`:该函数可以基于表达式来进行查询，返回一个 DataFrame 对象。使用方法如 `df.select($"colA", $"colB" + 1)`
- `selectExpr(exprs:String*)`:该函数的参数可以是一句完整的SQL语句，仍然返回一个 DataFrame 对象。具体的 SQL 语法规则可以参考通用的 SQL 规则。该函数使用方法如 `df.selectExpr("colA", "colB as newName", "abs(colC)")` ，当然也可以进行表达式的组合，如 `df.select(expr("colA"), expr("colB as newName"), expr("abs(colC)"))`

### 产生随机数据

在 Spark SQL 中，`org.apache.spark.sql.functions` 包提供了一些实用的函数，其中就包含了产生随机数的函数。它们可以从一个特定的分布中提取出独立同分布值，例如产生均匀分布随机数的函数 `rand()` 和产生服从正态分布的随机数的函数 `randn()` 。

