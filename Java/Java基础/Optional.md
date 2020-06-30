---
title: Optional
date: 2017-11-14
tags: [java,java8]
---

Optional类的Javadoc描述如下：

*这是一个可以为null的容器对象。如果值存在则isPresent()方法会返回true，调用get()方法会返回该对象。*

#### of

*为非null的值创建一个Optional。*

of方法通过工厂方法创建Optional类。需要注意的是，创建对象时传入的参数不能为null。如果传入参数为null，则抛出NullPointerException 。

```java
//调用工厂方法创建Optional实例
Optional<String> name = Optional.of("Sanaulla");
//传入参数为null，抛出NullPointerException.
Optional<String> someNull = Optional.of(null);
```

#### ofNullable

*为指定的值创建一个Optional，如果指定的值为null，则返回一个空的Optional。*

ofNullable与of方法相似，唯一的区别是可以接受参数为null的情况。

```java
//下面创建了一个不包含任何值的Optional实例
Optional empty = Optional.ofNullable(null);
```

#### isPresent

*如果值存在返回true，否则返回false。*

```java
//isPresent方法用来检查Optional实例中是否包含值
if (name.isPresent()) {
  //在Optional实例内调用get()返回已存在的值
  System.out.println(name.get());//输出Sanaulla
}
```

#### get

*如果Optional有值则将其返回，否则抛出NoSuchElementException。*

#### ifPresent

*如果Optional实例有值则为其调用Consumer，否则不做处理*

**注：** Consumer类包含一个抽象方法。该抽象方法对传入的值进行处理，但没有返回值。

如果Optional实例有值，调用ifPresent()可以接受接口段或lambda表达式。

```java
//ifPresent方法接受lambda表达式作为参数。lambda表达式对Optional的值调用consumer进行处理。
name.ifPresent((value) -> {
  System.out.println("The length of the value is: " + value.length());
});
```

#### orElse

*如果有值则将其返回，否则返回指定的其它值。*

如果Optional实例有值则将其返回，否则返回orElse方法传入的参数。

```java
//如果值不为null，orElse方法返回Optional实例的值。
//如果为null，返回传入的消息。
//输出：There is no value present!
System.out.println(empty.orElse("There is no value present!"));
//输出：Sanaulla
System.out.println(name.orElse("There is some value!"));
```

#### orElseGet

orElseGet与orElse方法类似，区别在于得到的默认值。orElse方法将传入的字符串作为默认值，orElseGet方法可以接受Supplier接口的实现用来生成默认值。

```java
//orElseGet与orElse方法类似，区别在于orElse传入的是默认值，
//orElseGet可以接受一个lambda表达式生成默认值。
//输出：Default Value
System.out.println(empty.orElseGet(() -> "Default Value"));
//输出：Sanaulla
System.out.println(name.orElseGet(() -> "Default Value"));
```

#### orElseThrow

*如果有值则将其返回，否则抛出supplier接口创建的异常。*

在orElseThrow中我们可以传入一个lambda表达式或方法，如果值不存在来抛出异常。

```java
try {
  //orElseThrow与orElse方法类似。与返回默认值不同，
  //orElseThrow会抛出lambda表达式或方法生成的异常 
  empty.orElseThrow(new Throwable(){
    @Override
    public String getMessage() {
        return "No value present in the Optional instance";
    }
  });
} catch (Throwable ex) {
  //输出: No value present in the Optional instance
  System.out.println(ex.getMessage());
}
```

#### map

*如果有值，则对其执行调用mapping函数得到返回值。如果返回值不为null，则创建包含mapping返回值的Optional作为map方法返回值，否则返回空Optional。*

```java
//map方法执行传入的lambda表达式参数对Optional实例的值进行修改。
//为lambda表达式的返回值创建新的Optional实例作为map方法的返回值。
Optional<String> upperName = name.map((value) -> value.toUpperCase());
System.out.println(upperName.orElse("No value found"));
```

#### flatMap

*如果有值，为其执行mapping函数返回Optional类型返回值，否则返回空Optional。flatMap与map（Funtion）方法类似，区别在于flatMap中的mapper返回值必须是Optional。调用结束时，flatMap不会对结果用Optional封装。*

flatMap方法与map方法类似，区别在于mapping函数的返回值不同。map方法的mapping函数返回值可以是任何类型T，而flatMap方法的mapping函数必须是Optional。

```java
//flatMap与map（Function）非常类似，区别在于传入方法的lambda表达式的返回类型。
//map方法中的lambda表达式返回值可以是任意类型，在map函数返回之前会包装为Optional。 
//但flatMap方法中的lambda表达式返回值必须是Optionl实例。 
upperName = name.flatMap((value) -> Optional.of(value.toUpperCase()));
System.out.println(upperName.orElse("No value found"));//输出SANAULLA
```

#### filter

*如果有值并且满足断言条件返回包含该值的Optional，否则返回空Optional。*

filter个方法通过传入限定条件对Optional实例的值进行过滤。

```java
//filter方法检查给定的Option值是否满足某些条件。
//如果满足则返回同一个Option实例，否则返回空Optional。
Optional<String> longName = name.filter((value) -> value.length() > 6);
System.out.println(longName.orElse("The name is less than 6 characters"));//输出Sanaulla
 
//另一个例子是Optional值不满足filter指定的条件。
Optional<String> anotherName = Optional.of("Sana");
Optional<String> shortName = anotherName.filter((value) -> value.length() > 6);
//输出：name长度不足6字符
System.out.println(shortName.orElse("The name is less than 6 characters"));
```