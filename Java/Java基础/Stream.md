---
title: Stream知识点
date: 2017-11-19
type: tags
tags: [java,java8]
---
## Stream流

Java 8 中的 Stream 是对集合（Collection）对象功能的增强，它专注于对集合对象进行各种非常便利、高效的聚合操作（aggregate operation），或者大批量数据操作 (bulk data operation)。

Stream 不是集合元素，它不是数据结构并不保存数据，它是有关算法和计算的，它更像一个高级版本的 Iterator。

Stream 就如同一个迭代器（Iterator），**单向**，**不可往复**，数据只能遍历一次，遍历过一次后即用尽了。

每次转换原有的Stream对象不改变，返回一个新的Stream对象（可以多次转换），这就允许对其操作可以像链条一样排列，变成一个管道。

## 使用详解

### 构造流的常见方法

```java
// 1. Individual values
Stream stream = Stream.of("a", "b", "c");
// 2. Arrays
String [] strArray = new String[] {"a", "b", "c"};
stream = Stream.of(strArray);
stream = Arrays.stream(strArray);
// 3. Collections
List<String> list = Arrays.asList(strArray);
stream = list.stream();
```
**注** 对于基本数值型，有三种对应的包装类型 Stream：IntStream、LongStream、DoubleStream。

###  流转换为其它数据结构

```java
// 1. Array
String[] strArray1 = stream.toArray(String[]::new);
// 2. Collection
List<String> list1 = stream.collect(Collectors.toList());
List<String> list2 = stream.collect(Collectors.toCollection(ArrayList::new));
Set set1 = stream.collect(Collectors.toSet());
Stack stack1 = stream.collect(Collectors.toCollection(Stack::new));
// 3. String
String str = stream.collect(Collectors.joining()).toString();
```

**注：** 一个 Stream 只可以使用一次

### 流的操作

流的常见操作：

在对于一个 Stream 进行多次转换操作 (Intermediate 操作)，每次都对 Stream 的每个元素进行转换，而且是执行多次，这样时间复杂度就是 N（转换次数）个 for 循环里把所有操作都做掉的总和吗？其实不是这样的，转换操作都是 lazy 的，多个转换操作只会在 Terminal 操作的时候融合起来，一次循环完成。我们可以这样简单的理解，Stream 里有个操作函数的集合，每次转换操作就是把转换函数放入这个集合中，在 Terminal 操作的时候循环 Stream 对应的集合，然后对每个元素执行所有的函数。

- Intermediate：一个流可以后面跟随零个或多个 intermediate 操作。其目的主要是打开流，做出某种程度的数据映射/过滤，然后返回一个新的流，交给下一个操作使用。这类操作都是惰性化的（lazy），就是说，仅仅调用到这类方法，并没有真正开始流的遍历。
> map (mapToInt, flatMap 等)、 filter、 distinct、 sorted、 peek、 limit、 skip、 parallel、 sequential、 unordered
- Terminal：一个流只能有一个 terminal 操作，当这个操作执行后，流就被使用“光”了，无法再被操作。所以这必定是流的最后一个操作。Terminal 操作的执行，才会真正开始流的遍历，并且会生成一个结果，或者一个 side effect。
> forEach、 forEachOrdered、 toArray、 reduce、 collect、 min、 max、 count、 anyMatch、 allMatch、 noneMatch、 findFirst、findAny、 iterator
- Short-circuiting：
    1. 对于一个 intermediate 操作，如果它接受的是一个无限大（infinite/unbounded）的 Stream，但返回一个有限的新 Stream。
    2. 对于一个 terminal 操作，如果它接受的是一个无限大的 Stream，但能在有限的时间计算出结果。
> anyMatch、 allMatch、 noneMatch、 findFirst、 findAny、 limit

#### map/flatMap

map 1对1
    把 input Stream 的每一个元素，映射成 output Stream 的另外一个元素

flatMap 1对多

#### filter

对元素进行过滤，满足条件的元素将被留下来生成一个新的Stream

#### forEach

forEach 方法接收一个 Lambda 表达式，然后在 Stream 的每一个元素上执行该表达式

#### findFirst

这是一个 termimal 兼 short-circuiting 操作，它总是返回 Stream 的第一个元素，或者空;返回值类型：Optional

#### reduce

把 Stream 元素组合起来。它提供一个起始值（种子），然后依照运算规则（BinaryOperator），和前面 Stream 的第一个、第二个、第 n 个元素组合。从这个意义上说，字符串拼接、数值的 sum、min、max、average 都是特殊的 reduce

#### limit/skip

limit 返回 Stream 的前面 n 个元素；skip 则是扔掉前 n 个元素

#### sorted

对 Stream 的排序通过 sorted 进行，它比数组的排序更强之处在于你可以首先对 Stream 进行各类 map、filter、limit、skip 甚至 distinct 来减少元素数量后，再排序，这能帮助程序明显缩短执行时间

#### min/max/distinct

min 和 max 的功能也可以通过对 Stream 元素先排序，再 findFirst 来实现，但前者的性能会更好，为 O(n)，而 sorted 的成本是 O(n log n)，distinct去除重复

#### groupingBy/partitioningBy

```db
示例数据Employee 对象流，每个对象对应一个名字、城市和销售数量，如下表所示：
+----------+------------+-----------------+
| Name     | City       | Number of Sales |
+----------+------------+-----------------+
| Alice    | London     | 200             |
| Bob      | London     | 150             |
| Charles  | New York   | 160             |
| Dorothy  | Hong Kong  | 190             |
+----------+------------+-----------------+
```

#### groupingBy-分组

首先，利用（lambda表达式出现之前的）命令式风格 Java 程序对流中的雇员按城市进行分组：

```java
Map<String, List<Employee>> result = new HashMap<>();
for (Employee e : employees) {
  String city = e.getCity();
  List<Employee> empsInCity = result.get(city);
  if (empsInCity == null) {
    empsInCity = new ArrayList<>();
    result.put(city, empsInCity);
  }
  empsInCity.add(e);
}
```

而在 Java 8 中，可以使用 groupingBy 收集器，一条语句就能完成相同的功能，如下：

```java
Map<String, List<Employee>> employeesByCity = 
    employees.stream().collect(groupingBy(Employee::getCity));
```

结果如下面的 map 所示：

> `{New York=[Charles], Hong Kong=[Dorothy], London=[Alice, Bob]}`

还可以计算每个城市中雇员的数量，只需传递一个计数收集器给 groupingBy 收集器。第二个收集器的作用是在流分类的同一个组中对每个元素进行递归操作。

```java
Map<String, Long> numEmployeesByCity =
    employees.stream().collect(groupingBy(Employee::getCity, counting()));
```
结果如下面的 map 所示：

> `{New York=1, Hong Kong=1, London=2}`

该功能与下面的 SQL 语句是等同的：

```sql
select city, count(*) from Employee group by city
```

另一个例子是计算每个城市的平均年龄，这可以联合使用 averagingInt 和 groupingBy 收集器：

```java
Map<String, Double> avgSalesByCity =
    employees.stream().collect(groupingBy(Employee::getCity,averagingInt(Employee::getNumSales)));
```
结果如下 map 所示：

> `{New York=160.0, Hong Kong=190.0, London=175.0}`

#### partitioningBy-分区

分区是一种特殊的分组，结果 map 至少包含两个不同的分组——一个true，一个false。例如，如果想找出最优秀的员工，可以将所有雇员分为两组，一组销售量大于 N，另一组小于 N，使用 partitioningBy 收集器：

```java
Map<Boolean, List<Employee>> partitioned =
    employees.stream().collect(partitioningBy(e -> e.getNumSales() > 150));
```
输出如下结果：

> `{false=[Bob], true=[Alice, Charles, Dorothy]}`

也可以将 groupingBy 收集器传递给 partitioningBy 收集器来将联合使用分区和分组。例如，可以统计每个分区中的每个城市的雇员人数：

```java
Map<Boolean, Map<String, Long>> result =
    employees.stream().collect(partitioningBy(e -> e.getNumSales() > 150,
                               groupingBy(Employee::getCity, counting())));
```

这样会生成一个二级 Map:

> `{false={London=1}, true={New York=1, Hong Kong=1, London=1}}`

#### Match

Stream 有三个 match 方法，从语义上说：

- allMatch：Stream 中全部元素符合传入的 predicate，返回 true
- anyMatch：Stream 中只要有一个元素符合传入的 predicate，返回 true
- noneMatch：Stream 中没有一个元素符合传入的 predicate，返回 true

它们都不是要遍历全部元素才能返回结果。例如 allMatch 只要一个元素不满足条件，就 skip 剩下的所有元素，返回 false

## 总结

Stream 的特性可以归纳为：

- 不是数据结构
- 它没有内部存储，它只是用操作管道从 source（数据结构、数组、generator function、IO channel）抓取数据。
- 它也绝不修改自己所封装的底层数据结构的数据。例如 Stream 的 filter 操作会产生一个不包含被过滤元素的新 Stream，而不是从 source 删除那些元素。
- 所有 Stream 的操作必须以 lambda 表达式为参数
- 不支持索引访问
- 你可以请求第一个元素，但无法请求第二个，第三个，或最后一个
- 很容易生成数组或者 List
- 惰性化
- 很多 Stream 操作是向后延迟的，一直到它弄清楚了最后需要多少数据才会开始。
- Intermediate 操作永远是惰性化的。
- 并行能力
- 当一个 Stream 是并行化的，就不需要再写多线程代码，所有对它的操作会自动并行进行的。
- 可以是无限的
- 集合有固定大小，Stream 则不必。limit(n) 和 findFirst() 这类的 short-circuiting 操作可以对无限的 Stream 进行运算并很快完成。

