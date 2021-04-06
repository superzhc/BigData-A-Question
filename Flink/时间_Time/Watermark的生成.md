# Watermark 的生成方式

## With Periodic Watermarks

周期性地触发 Watermark 的生成和发送，默认是 100ms。

每隔 N 秒自动向流里注入一个 Watermark，时间间隔由 `ExecutionConfig.setAutoWatermarkInterval` 决定。每次调用 getCurrentWatermark 方法，如果得到的 Watermark 不为空并且比之前的大，就注入流中。 

可以定义一个最大允许乱序的时间，这种比较常用。

## With Punctuated Watermarks

基于某些事件触发 Watermark 的生成和发送。

基于事件向流里注入一个 Watermark，每一个元素都有机会判断是否生成一个 Watermark，如果得到的 Watermark 不为空并且比之前的大，就注入流中。