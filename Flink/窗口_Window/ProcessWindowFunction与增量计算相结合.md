# ProcessWindowFunction 与增量计算相结合

当我们既想访问窗口里的元数据，又不想缓存窗口里的所有数据时，可以将 ProcessWindowFunction 与增量计算函数 `reduce()` 和 `aggregate()` 相结合。对于一个窗口来说，Flink 先增量计算，窗口关闭前，将增量计算结果发送给ProcessWindowFunction 作为输入再进行处理。

下面的代码中，计算的结果保存在四元组 `(股票代号，最大值、最小值，时间戳)` 中，`reduce()` 部分是增量计算，其结果传递给 `WindowEndProcessFunction`，`WindowEndProcessFunction` 只需要将窗口结束的时间戳添加到四元组的最后一个字段上即可。

```java
// 读入股票数据流
DataStream<StockPrice> stockStream = env
  	.addSource(new StockSource("stock/stock-tick-20200108.csv"));

// reduce的返回类型必须和输入类型相同
// 为此我们将StockPrice拆成一个四元组 (股票代号，最大值、最小值，时间戳)
DataStream<Tuple4<String, Double, Double, Long>> maxMin = stockStream
    .map(s -> Tuple4.of(s.symbol, s.price, s.price, 0L))
    .returns(Types.TUPLE(Types.STRING, Types.DOUBLE, Types.DOUBLE, Types.LONG))
    .keyBy(s -> s.f0)
    .timeWindow(Time.seconds(10))
    .reduce(new MaxMinReduce(), new WindowEndProcessFunction());

// 增量计算最大值和最小值
public static class MaxMinReduce implements ReduceFunction<Tuple4<String, Double, Double, Long>> {
        @Override
        public Tuple4<String, Double, Double, Long> reduce(Tuple4<String, Double, Double, Long> a, Tuple4<String, Double, Double, Long> b) {
            return Tuple4.of(a.f0, Math.max(a.f1, b.f1), Math.min(a.f2, b.f2), 0L);
        }
    }

// 利用ProcessFunction可以获取Context的特点，获取窗口结束时间
public static class WindowEndProcessFunction extends ProcessWindowFunction<Tuple4<String, Double, Double, Long>, Tuple4<String, Double, Double, Long>, String, TimeWindow> {
    @Override
    public void process(String key,
                        Context context,
                        Iterable<Tuple4<String, Double, Double, Long>> elements,
                        Collector<Tuple4<String, Double, Double, Long>> out) {
        long windowEndTs = context.window().getEnd();
        if (elements.iterator().hasNext()) {
            Tuple4<String, Double, Double, Long> firstElement = elements.iterator().next();
            out.collect(Tuple4.of(key, firstElement.f1, firstElement.f2, windowEndTs));
        }
    }
}
```