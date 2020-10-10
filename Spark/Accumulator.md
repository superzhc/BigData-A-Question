# Accumulator-累加器

Accumulator 是 spark 提供的累加器，顾名思义，该变量只能够增加。只有 Driver 能获取到 Accumulator 的值（使用value方法），Task 只能对其做增加操作（使用 `+=`）。

**使用示例**

```scala
//在driver中定义
val accum = sc.accumulator(0, "Example Accumulator")
//在task中进行累加
sc.parallelize(1 to 10).foreach(x=> accum += 1)

//在driver中输出
accum.value
//结果将返回10
res: 10
```