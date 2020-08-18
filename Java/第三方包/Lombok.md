# Lombok

## 简介

Lombok是一个可以通过简单的注解形式来帮助我们简化消除一些必须有但显得很臃肿的Java代码的工具，通过使用对应的注解，可以在编译源码的时候生成对应的方法。官方地址：https://projectlombok.org/，github地址：https://github.com/rzwitserloot/lombok。

## 环境

maven添加jar包

```xml
<dependencies> 
    <dependency> 
        <groupId>org.projectlombok</groupId> 
        <artifactId>lombok</artifactId> 
        <version>1.16.16</version> 
        <scope>provided</scope> 
    </dependency> 
</dependencies>
```

## 注解介绍

### @Getter / @Setter

可以作用在类上和属性上，放在类上，会对所有的非静态(non-static)属性生成Getter/Setter方法，放在属性上，会对该属性生成Getter/Setter方法。并可以指定Getter/Setter方法的访问级别。

### @EqualsAndHashCode

默认情况下，会使用所有非瞬态(non-transient)和非静态(non-static)字段来生成equals和hascode方法，也可以指定具体使用哪些属性。

### ToString

生成toString方法，默认情况下，会输出类名、所有属性，属性会按照顺序输出，以逗号分割。

### @NoArgsConstructor, @RequiredArgsConstructor and @AllArgsConstructor

无参构造器、部分参数构造器、全参构造器，当我们需要重载多个构造器的时候，Lombok就无能为力了。

### @Data

@ToString, @EqualsAndHashCode, 所有属性的@Getter, 所有non-final属性的@Setter和@RequiredArgsConstructor的组合，通常情况下，我们使用这个注解就足够了。

### @NonNull

可以帮助我们避免空指针。

### @Cleanup

自动帮我们调用close()方法。