# DelayQueue

无界的保存Delayed元素的集合。元素只有在延时已经过期的时候才能被取出。队列的第一个元素延期最小（包含负值——延时已经过期）。当你要实现一个延期任务的队列的时候使用（不要自己手动实现——使用ScheduledThreadPoolExecutor）。
