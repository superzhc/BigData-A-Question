<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-11-27 16:15:54
 * @LastEditTime : 2020-11-27 16:21:35
 * @Copyright 2020 SUPERZHC
-->
# Apache Commons Math

Apache Commons Math 3.6.1的stat包的内容非常丰富，并且得到很好的优化。

使用这个包能够生成如下描述性统计：

- 算术与几何平均数；
- 方差和标准差；
- 和、积、对数求和、平方和；
- 最小值、最大值、中位数与百分位数；
- 偏度和峰度；
- 一阶、二阶、三阶、四阶矩量。

而且，根据官方网站的说法，这些方法都经过优化，执行时占用的内存更少。

除了百分位数与中位数之外，所有这些统计量在计算时都不需要在内存中维护输入数据值的完整列表。

## 引入依赖

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-math3</artifactId>
    <version>3.6.1</version>
</dependency>
```