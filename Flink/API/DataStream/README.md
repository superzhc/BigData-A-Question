# DataStream API

DataStream API 主要分为3块：Source、Transformation、Sink。

- Source 是程序的数据源输入，可以通过 `StreamExecutionEnvironment.addSource(sourceFunction)` 为程序添加一个数据源。
- Transformation 是具体的操作，它对一个或多个输入数据源进行计算处理，比如 map、flatMap 和 filter 等操作。
- Sink 是程序的输出，它可以把 Transformation 处理之后的数据输出到指定的存储介质中。

## Transformation

即通过从一个或多个 DataStream 生成新的 DataStream 的过程被称为 Transformation 操作。在转换过程中，每种操作类型被定义为不同的 Operator，Flink 程序能够将多个 Transformation 组成一个 DataFlow 的拓扑。所有 DataStream 的转换操作可分为单 Single-DataStream、Multi-DaataStream、物理分区三类类型。

- Single-DataStream 操作定义了对单个 DataStream 数据集元素的处理逻辑
- Multi-DataStream 操作定义了对多个 DataStream 数据集元素的处理逻辑
- 物理分区定义了对数据集中的并行度和数据分区调整转换的处理逻辑。

### map

> 输入一个元素，然后返回一个元素，中间可以进行清洗转换等操作。

**Java 版本**

```java
SingleOutputStreamOperator<Tuple1<Long>> val= dss.map(new MapFunction<Long, Tuple1<Long>>()
{
    @Override public Tuple1<Long> map(Long value) throws Exception {
        return new Tuple1<>(value);
    }
});
```

**Scala 版本**

```scala
val v=dss.map(d=>(d))
```

### flatMap

> 输入一个元素，可以返回零个、一个或者多个元素。

```java
SingleOutputStreamOperator<Student> flatMap = dss.flatMap(new FlatMapFunction<Student, Student>() {
    @Override
    public void flatMap(Student value, Collector<Student> out) throws Exception {
        if (value.id % 2 == 0) {
            out.collect(value);
        }
    }
});
```

### filter

> 过滤函数，对传入的数据进行判断，符合条件的数据会被留下。

```java
SingleOutputStreamOperator<Student> filter = dss.filter(new FilterFunction<Student>() {
    @Override
    public boolean filter(Student value) throws Exception {
        if (value.id > 95) {
            return true;
        }
        return false;
    }
});
```

### keyBy

> 根据指定的 Key 进行分组，Key 相同的数据会进入同一个分区。

注：如果有多个分区，则设置并行度需大于 1，或者在算子上设置 `setParallelism(2)` 前行度，否则算子只有一个并行度，则计算结果始终只有一个分区

keyBy 的两种典型用法如下：

- `dss.keyBy("someKey")`：指定对象中的 someKey 属性作为分组 Key
- ~~`dss.keyBy(0)`：指定 Tuple 中的第一个元素作为分组 Key~~

```java
KeyedStream<Student, Integer> keyBy = student.keyBy(new KeySelector<Student, Integer>() {
    @Override
    public Integer getKey(Student value) throws Exception {
        return value.age;
    }
});
```

### fold

> 将数据流的每一次输出进行滚动叠加，合并输出结果

```java
import com.flink.examples.DataSource;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.List;

/**
 * @Description Fold算子：将数据流的每一次输出进行滚动叠加，合并输出结果
 * （与Reduce的区别是，Reduce是拿前一次聚合结果累加后一次的并输出数据流；Fold是直接将当前数据对象追加到前一次叠加结果上并输出数据流）
 */
public class Fold {
    /**
     * 遍历集合，分区打印每一次滚动叠加的结果（示例：按性别分区，按排序，未位追加输出）
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        List<Tuple3<String,String,Integer>> tuple3List = DataSource.getTuple3ToList();
        //注意：使用Integer进行分区时，会导致分区结果不对，转换成String类型输出key即可正确输出
        KeyedStream<Tuple3<String,String,Integer>, String> keyedStream = env.fromCollection(tuple3List).keyBy(new KeySelector<Tuple3<String,String,Integer>, String>() {
            @Override
            public String getKey(Tuple3<String, String, Integer> tuple3) throws Exception {
                //f1为性别字段,以相同f1值（性别）进行分区
                return String.valueOf(tuple3.f1);
            }
        });

        SingleOutputStreamOperator<String> result = keyedStream.fold("同学：", new FoldFunction<Tuple3<String, String, Integer>, String>() {
            @Override
            public String fold(String s, Tuple3<String, String, Integer> tuple3) throws Exception {
                if (s.startsWith("男") || s.startsWith("女")){
                    return s + tuple3.f0 + "、";
                } else {
                    return (tuple3.f1.equals("man") ? "男" : "女") + s + tuple3.f0 + "、";
                }
            }
        });
        result.print();
        env.execute("flink Fold job");
    }
}
```

**输出结果**

```
2> 男同学：张三、
2> 男同学：张三、王五、
2> 男同学：张三、王五、吴八、
1> 女同学：李四、
1> 女同学：李四、刘六、
1> 女同学：李四、刘六、伍七、
```

### reduce

> 对数据进行聚合操作，结合当前元素和上一次 reduce 返回的值进行聚合操作，然后返回一个新的值。

```java
SingleOutputStreamOperator<Student> reduce = student.keyBy(new KeySelector<Student, Integer>() {
    @Override
    public Integer getKey(Student value) throws Exception {
        return value.age;
    }
}).reduce(new ReduceFunction<Student>() {
    @Override
    public Student reduce(Student value1, Student value2) throws Exception {
        Student student1 = new Student();
        student1.name = value1.name + value2.name;
        student1.id = (value1.id + value2.id) / 2;
        student1.password = value1.password + value2.password;
        student1.age = (value1.age + value2.age) / 2;
        return student1;
    }
});
```

注意：reduce 操作只能是 KeyedStream

### Aggregations

> DataStream API 支持各种聚合，例如 min，max，sum 等。 这些函数可以应用于 KeyedStream 以获得 Aggregations 聚合。

```java
KeyedStream.sum(0) 
KeyedStream.sum("key") 
KeyedStream.min(0) 
KeyedStream.min("key") 
KeyedStream.max(0) 
KeyedStream.max("key") 
KeyedStream.minBy(0) 
KeyedStream.minBy("key") 
KeyedStream.maxBy(0) 
KeyedStream.maxBy("key")
```

max 和 maxBy 之间的区别在于 max 返回流中的最大值，但 maxBy 返回具有最大值的键， min 和 minBy 同理。

### join

> 两个数据流通过内部相同的key分区，将窗口内两个数据流相同key数据元素计算后，合并输出

Flink 支持了两种 Join：

- Window Join（窗口连接）
- Interval Join（时间间隔连接）

> 官方文档：<https://ci.apache.org/projects/flink/flink-docs-release-1.11/zh/dev/stream/operators/joining.html>

### CoGroup

> 将两个数据流按照 key 进行 group 分组，并将数据流按 key 进行分区的处理，最终合成一个数据流（与 join 有区别，不管 key 有没有关联上，最终都会合并成一个数据流）

### union

> 合并多个流，新的流会包含所有流中的数据，但是 union 有一个限制，就是所有合并的流类型必须是一致的。

```java
inputStream.union(inputStream1, inputStream2, ...);
```

### connect

> 和 union 类似，但是只能连接两个流，两个流的数据类型可以不同，会对两个流中的数据应用不同的处理方法。

```java
import com.flink.examples.DataSource;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

import java.util.Arrays;
import java.util.List;

/**
 * @Description Connect算子：功能与union类似，将两个流（union支持两个或以上）合并为一个流，但区别在于connect不要求数据类型一致
 */
public class Connect {

    /**
     * 将两个不区分数据类型的数据流合并成一个数据流，并打印
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        List<Tuple3<String, String, Integer>> tuple3List = DataSource.getTuple3ToList();
        //dataStream 1
        DataStream<Tuple3<String, String, Integer>> dataStream1 = env.fromCollection(tuple3List);
        //dataStream 2
        DataStream<Tuple3<String, String, Integer>> dataStream2 = env.fromCollection(Arrays.asList(
                new Tuple3<>("医生", "上海", 2),
                new Tuple3<>("老师", "北京", 4),
                new Tuple3<>("工人", "广州", 9)
        ));
        //合关两个数据流
        DataStream<Tuple4<String, String, Integer, String>> dataStream = dataStream1.connect(dataStream2)
                .map(new CoMapFunction<Tuple3<String, String, Integer>, Tuple3<String, String, Integer>, Tuple4<String, String, Integer, String>>() {
                    //表示dataStream1的流输入
                    @Override
                    public Tuple4<String, String, Integer, String> map1(Tuple3<String, String, Integer> value) throws Exception {
                        return Tuple4.of(value.f0, value.f1, value.f2, "用户");
                    }
                    //表示dataStream2的流输入
                    @Override
                    public Tuple4<String, String, Integer, String> map2(Tuple3<String, String, Integer> value) throws Exception {
                        return Tuple4.of(value.f0, value.f1, value.f2, "职业");
                    }
                });

        //打印
        dataStream.print();
        env.execute("flink Split job");
    }
}
```

**打印结果**

```
(张三,man,20,用户)
(李四,girl,24,用户)
(王五,man,29,用户)
(刘六,girl,32,用户)
(伍七,girl,18,用户)
(吴八,man,30,用户)
(医生,上海,2,职业)
(老师,北京,4,职业)
(工人,广州,9,职业)
```

### coMap 和 coFlatMap

> 在 ConnectedStream 中需要使用这种函数，类似于 map 和 flatMap。

### ~~split~~【过时】

> 将数据流切分成多个数据流（已过时，并且不能二次切分，不建议使用）

```java
SplitStream<Integer> split = inputStream.split(new OutputSelector<Integer>() {
    @Override
    public Iterable<String> select(Integer value) {
        List<String> output = new ArrayList<String>(); 
        if (value % 2 == 0) {
            output.add("even");
        }
        else {
            output.add("odd");
        }
        return output;
    }
});
```

### ~~select~~

> 和 split 配合使用，选择切分后的流。

```java
DataStream<Integer> even = split.select("even"); 
DataStream<Integer> odd = split.select("odd"); 
DataStream<Integer> all = split.select("even","odd");
```

### apply

> 对窗口内的数据流进行处理

```java
import com.flink.examples.DataSource;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;

import java.util.Iterator;
import java.util.List;

/**
 * @Description Apply方法：对窗口内的数据流进行处理
 */
public class Apply {

    /**
     * 遍历集合，分别打印不同性别的总人数与年龄之和
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        List<Tuple3<String, String, Integer>> tuple3List = DataSource.getTuple3ToList();
        DataStream<String> dataStream = env.fromCollection(tuple3List)
                .keyBy((KeySelector<Tuple3<String, String, Integer>, String>) k -> k.f1)
                //按数量窗口滚动，每3个输入窗口数据流，计算一次
                .countWindow(3)
                //只能基于Windowed窗口Stream进行调用
                .apply(
                        //WindowFunction<IN, OUT, KEY, W extends Window>
                        new WindowFunction<Tuple3<String, String, Integer>, String, String, GlobalWindow>() {
                            /**
                             * 处理窗口数据集合
                             * @param s         从keyBy里返回的key值
                             * @param window    窗口类型
                             * @param input     从窗口获取的所有分区数据流
                             * @param out       输出数据流对象
                             * @throws Exception
                             */
                            @Override
                            public void apply(String s, GlobalWindow window, Iterable<Tuple3<String, String, Integer>> input, Collector<String> out) throws Exception {
                                Iterator<Tuple3<String, String, Integer>> iterator = input.iterator();
                                int total = 0;
                                int i = 0;
                                while (iterator.hasNext()){
                                    Tuple3<String, String, Integer> tuple3 = iterator.next();
                                    total += tuple3.f2;
                                    i ++ ;
                                }
                                out.collect(s + "共:"+i+"人，累加总年龄：" + total);
                            }
                        });
        dataStream.print();
        env.execute("flink Filter job");
    }
}
```

### process

> 处理每个 keyBy（分区）输入到窗口的批量数据流（为 KeyedStream 类型数据流）

```java
import com.flink.examples.DataSource;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import java.util.Iterator;
import java.util.List;
/**
 * @Description process算子：处理每个keyBy（分区）输入到窗口的批量数据流（为KeyedStream类型数据流）
 */
public class Process {

    /**
     * 遍历集合，分别打印不同性别的总人数与年龄之和
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        List<Tuple3<String, String, Integer>> tuple3List = DataSource.getTuple3ToList();
        DataStream<String> dataStream = env.fromCollection(tuple3List)
                .keyBy((KeySelector<Tuple3<String, String, Integer>, String>) k -> k.f1)
                //按数量窗口滚动，每3个输入数据流，计算一次
                .countWindow(3)
                //处理每keyBy后的窗口数据流，process方法通常应用于KeyedStream类型的数据流处理
                .process(new ProcessWindowFunction<Tuple3<String, String, Integer>, String, String, GlobalWindow>() {
                    /**
                     * 处理窗口数据集合
                     * @param s         从keyBy里返回的key值
                     * @param context   窗口的上下文
                     * @param input     从窗口获取的所有分区数据流
                     * @param out       输出数据流对象
                     * @throws Exception
                     */
                    @Override
                    public void process(String s, Context context, Iterable<Tuple3<String, String, Integer>> input, Collector<String> out) throws Exception {
                        Iterator<Tuple3<String, String, Integer>> iterator = input.iterator();
                        int total = 0;
                        int i = 0;
                        while (iterator.hasNext()){
                            Tuple3<String, String, Integer> tuple3 = iterator.next();
                            total += tuple3.f2;
                            i ++ ;
                        }
                        out.collect(s + "共:"+i+"人，平均年龄：" + total/i);
                    }
                });
        dataStream.print();
        env.execute("flink Process job");
    }
}
```

### 数据分区算子

物理分区（Physical Partitioning）操作的作用是根据指定的分区策略将数据重新分配到不同节点的 Task 案例上执行。当使用 DataStream 提供的 API 对数据处理过程中，依赖于算子本身对数据的分区控制，如果用户希望自己控制数据分区，例如当数据发生了数据倾斜的时候，就需要通过定义物理分区策略的方式对数据集进行重新分布处理。Flink 中已经提供了常见的分区策略，例如随机分区（Random Partitioning）、平衡分区（Roundobin Partitioning）、按比例分区（Rescaling Partitioning）等。当然如果给定的分区策略无法满足需求，也可以根据 Flink 提供的分区控制接口创建分区器，实现自定义分区控制。

Flink 针对 DataStream 提供了一些数据分区规则，具体如下：

- Random Partitioning：随机分区
  
  通过随机的方式将数据分配在下游算子的每个分区中，分区相对均衡，但是较容易失去原有数据的分区结构。
  ```java
  dss.shuffle()
  ```

- Rebalancing Partitionin：对数据集进行再平衡、重分区和消除数据倾斜
  
  通过循环的方式对数据集中的数据进行重分区，能够尽可能保证每个分区的数据平衡，当数据集发生数据倾斜的时候使用这种策略就是比较有效的优化方法。
  ```java
  dss.rebalance()
  ```

- Rescaling Partitionin：重新调节
  
  ```java
  dss.rescale()
  ```
  如果上游操作有 2 个并发，而下游操作有 4 个并发，那么上游的 1 个并发结果分配给了下游的 2 个并发操作，另外的 1 个并发结果则分配给了下游的另外 2 个并发操作。另一方面，下游有 2 个并发操作而上游有 4 个并发操作，那么上游的其中 2 个操作的结果分配给了下游的一个并发操作，而另外 2 个并发操作的结果则分配给了另外 1 个并发操作。

  Rescaling 与 Rebalancing 的区别为 Rebalancing 会产生全量重分区，而 Rescaling 不会。
  
- Custom partitioning：自定义分区

  自定义分区实现 Partitioner 接口的方法如下:
  ```java
  dss.partitionCustom(partitioner,"someKey");
  // 或者
  dss.partitionCustom(partitioner,0);
  ```