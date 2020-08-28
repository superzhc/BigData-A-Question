---
title: Spring表达式语言
date: 2017-11-15
tags: spring
---
## 基础

Spring表达式语言全称为“Spring Expression Language”，缩写为“SpEL”，能在运行时构建复杂表达式、存取对象图属性、对象方法调用等等，并且能与Spring功能完美整合，如能用来配置Bean定义。

表达式语言给静态Java语言增加了动态功能。

SpEL是单独模块，只依赖于core模块，不依赖于其他模块，可以单独使用。

SpEL支持如下表达式：

1. **基本表达式：** 字面量表达式、关系，逻辑与算数运算表达式、字符串连接及截取表达式、三目运算及Elivis表达式、正则表达式、括号优先级表达式；
2. **类相关表达式：** 类类型表达式、类实例化、instanceof表达式、变量定义及引用、赋值表达式、自定义函数、对象属性存取及安全导航表达式、对象方法调用、Bean引用；
3. **集合相关表达式：** 内联List、内联数组、集合，字典访问、列表，字典，数组修改、集合投影、集合选择；不支持多维内联数组初始化；不支持内联字典定义；
4. **其他表达式：** 模板表达式。

**注：** SpEL表达式中的关键字是不区分大小写的。

支持SpEL的Jar包：“org.springframework.expression-3.0.5.RELEASE.jar”

代码片段：

```java
ExpressionParser parser = new SpelExpressionParser();  
Expression expression = parser.parseExpression("('Hello' + ' World').concat(#end)");  
EvaluationContext context = new StandardEvaluationContext();  
context.setVariable("end", "!");  
Assert.assertEquals("Hello World!", expression.getValue(context)); 
```

1. 创建解析器：SpEL使用ExpressionParser接口表示解析器，提供SpelExpressionParser默认实现；
2. 解析表达式：使用ExpressionParser的parseExpression来解析相应的表达式为Expression对象。
3. 构造上下文：准备比如变量定义等等表达式需要的上下文数据。
4. 求值：通过Expression接口的getValue方法根据上下文获得表达式值。

### SpEL原理及接口

1. 表达式：表达式是表达式语言的核心，所以表达式语言都是围绕表达式进行的
2. 解析器：用于将字符串表达式解析为表达式对象
3. 上下文：表达式对象执行的环境，该环境可能定义变量、定义自定义函数、提供类型转换等等
4. 根对象及活动上下文对象：根对象是默认的活动上下文对象，活动上下文对象表示了当前表达式操作的对象

工作原理图：

![](http://ozchbp0v3.bkt.clouddn.com/Spring_SpEL.jpg)

工作原理：
1. 首先定义表达式：“1+2”；
2. 定义解析器ExpressionParser实现，SpEL提供默认实现SpelExpressionParser；
    1. SpelExpressionParser解析器内部使用Tokenizer类进行词法分析，即把字符串流分析为记号流，记号在SpEL使用Token类来表示；
    2. 有了记号流后，解析器便可根据记号流生成内部抽象语法树；在SpEL中语法树节点由SpelNode接口实现代表：如OpPlus表示加操作节点、IntLiteral表示int型字面量节点；使用SpelNodel实现组成了抽象语法树；
    3. 对外提供Expression接口来简化表示抽象语法树，从而隐藏内部实现细节，并提供getValue简单方法用于获取表达式值；SpEL提供默认实现为SpelExpression；
3. 定义表达式上下文对象（可选），SpEL使用EvaluationContext接口表示上下文对象，用于设置根对象、自定义变量、自定义函数、类型转换器等，SpEL提供默认实现StandardEvaluationContext；
4. 使用表达式对象根据上下文对象（可选）求值（调用表达式对象的getValue方法）获得结果。

## SpEL语法

### 基本表达式

#### 字面量表达式

|类型|示例|
|:--:|:--|
|字符串|`String str1 = parser.parseExpression("'Hello World!'").getValue(String.class);`|
|数字类型|`int int1 = parser.parseExpression("1").getValue(Integer.class);`|
|布尔类型|`boolean true1 = parser.parseExpression("true").getValue(boolean.class);`|
|null类型|`Object null1 = parser.parseExpression("null").getValue(Object.class);`|

#### 算数运算表达式

|类型|示例|
|:--:|:--|
|加减乘除|`int result1 = parser.parseExpression("1+2-3*4/2").getValue(Integer.class);//-3`|
|求余|`int result2 = parser.parseExpression("4%3").getValue(Integer.class);//1`|
|幂运算|`int result3 = parser.parseExpression("2^3").getValue(Integer.class);//8`|

#### 关系表达式

等于（==）、不等于(!=)、大于(>)、大于等于(>=)、小于(<)、小于等于(<=)，区间（between）运算，如`parser.parseExpression("1>2").getValue(boolean.class);`将返回false；而`parser.parseExpression("1 between {1, 2}").getValue(boolean.class);`将返回true。

between运算符右边操作数必须是列表类型，且只能包含2个元素。第一个元素为开始，第二个元素为结束，区间运算是包含边界值的，即 xxx>=list.get(0) && xxx<=list.get(1)。

SpEL同样提供了等价的“EQ” 、“NE”、 “GT”、“GE”、 “LT” 、“LE”来表示等于、不等于、大于、大于等于、小于、小于等于，不区分大小写。

#### 逻辑表达式

且（and）、或(or)、非(!或NOT)

```java
String expression1 = "2>1 and (!true or !false)";  
boolean result1 = parser.parseExpression(expression1).getValue(boolean.class);  
Assert.assertEquals(true, result1);  
   
String expression2 = "2>1 and (NOT true or NOT false)";  
boolean result2 = parser.parseExpression(expression2).getValue(boolean.class);  
Assert.assertEquals(true, result2); 
```

**注：** 逻辑运算符不支持 Java中的 && 和 ||。

#### 字符串连接及截取表达式

使用“+”进行字符串连接，使用“'String'[0] [index]”来截取一个字符，目前只支持截取一个，如“'Hello ' + 'World!'”得到“Hello World!”；而“'Hello World!'[0]”将返回“H”。

#### 三目运算及Elivis运算表达式

三目运算符 “表达式1?表达式2:表达式3”用于构造三目运算表达式，如“2>1?true:false”将返回true；

Elivis运算符“表达式1?:表达式2”从Groovy语言引入用于简化三目运算符的，当表达式1为非null时则返回表达式1，当表达式1为null时则返回表达式2，简化了三目运算符方式“表达式1? 表达式1:表达式2”，如“null?:false”将返回false，而“true?:false”将返回true；

#### 正则表达式

使用“str matches regex，如“'123' matches '\\d{3}'”将返回true

#### 括号优先级表达式

使用“(表达式)”构造，括号里的具有高优先级

### 类相关表达式

#### 类类型表达式

使用“T(Type)”来表示java.lang.Class实例，“Type”必须是类全限定名，“java.lang”包除外，即该包下的类可以不指定包名；使用类类型表达式还可以进行访问类静态方法及类静态字段

```java
@Test  
public void testClassTypeExpression() {  
    ExpressionParser parser = new SpelExpressionParser();  
    //java.lang包类访问  
    Class<String> result1 = parser.parseExpression("T(String)").getValue(Class.class);  
    Assert.assertEquals(String.class, result1);  
    //其他包类访问,待验证
    String expression2 = "T(cn.javass.spring.chapter5.SpELTest)";  
    Class<String> result2 = parser.parseExpression(expression2).getValue(Class.class);
    Assert.assertEquals(SpELTest.class, result2);  
    //类静态字段访问  
    int result3=parser.parseExpression("T(Integer).MAX_VALUE").getValue(int.class);  
    Assert.assertEquals(Integer.MAX_VALUE, result3);  
    //类静态方法调用  
    int result4 = parser.parseExpression("T(Integer).parseInt('1')").getValue(int.class);  
    Assert.assertEquals(1, result4);  
}
```

#### 类实例化

类实例化同样使用java关键字“new”，类名必须是全限定名，但java.lang包内的类型除外

```java
@Test  
public void testConstructorExpression() {  
    ExpressionParser parser = new SpelExpressionParser();  
    String result1 = parser.parseExpression("new String('haha')").getValue(String.class);  
    Assert.assertEquals("haha", result1);  
    Date result2 = parser.parseExpression("new java.util.Date()").getValue(Date.class);  
    Assert.assertNotNull(result2);  
}
```

#### instanceof表达式

SpEL支持instanceof运算符，跟Java内使用同义；如“'haha' instanceof T(String)”将返回true

#### 变量定义及引用

变量定义通过EvaluationContext接口的setVariable(variableName, value)方法定义；在表达式中使用“#variableName”引用；除了引用自定义变量，SpE还允许引用根对象及当前上下文对象，使用“#root”引用根对象，使用“#this”引用当前上下文对象；

```java
@Test  
public void testVariableExpression() {  
    ExpressionParser parser = new SpelExpressionParser();  
    EvaluationContext context = new StandardEvaluationContext();  
    context.setVariable("variable", "haha");
    String result1 = parser.parseExpression("#variable").getValue(context, String.class);  
    Assert.assertEquals("haha", result1);  
   
    context = new StandardEvaluationContext("haha");  
    String result2 = parser.parseExpression("#root").getValue(context, String.class);  
    Assert.assertEquals("haha", result2);  
    String result3 = parser.parseExpression("#this").getValue(context, String.class);  
    Assert.assertEquals("haha", result3);  
} 
```

#### 自定义函数

目前只支持类静态方法注册为自定义函数；SpEL使用StandardEvaluationContext的registerFunction方法进行注册自定义函数，其实完全可以使用setVariable代替，两者其实本质是一样的

#### 赋值表达式

SpEL即允许给自定义变量赋值，也允许给跟对象赋值，直接使用“#variableName=value”即可赋值

```java
@Test  
public void testAssignExpression() {  
    ExpressionParser parser = new SpelExpressionParser();  
    //1.给root对象赋值  
    EvaluationContext context = new StandardEvaluationContext("aaaa");  
    String result1 = parser.parseExpression("#root='aaaaa'").getValue(context, String.class);  
    Assert.assertEquals("aaaaa", result1);  
    String result2 = parser.parseExpression("#this='aaaa'").getValue(context, String.class);  
    Assert.assertEquals("aaaa", result2);  
   
    //2.给自定义变量赋值  
    context.setVariable("#variable", "variable");  
    String result3 = parser.parseExpression("#variable=#root").getValue(context, String.class);  
    Assert.assertEquals("aaaa", result3);  
}  
```

#### 对象属性存取及安全导航表达式

对象属性获取非常简单，即使用如“a.property.property”这种点缀式获取，SpEL对于属性名首字母是不区分大小写的；SpEL还引入了Groovy语言中的安全导航运算符“(对象|属性)?.属性”，用来避免但“?.”前边的表达式为null时抛出空指针异常，而是返回null；修改对象属性值则可以通过赋值表达式或Expression接口的setValue方法修改。

当前上下文对象属性及方法访问，可以直接使用属性或方法名访问。

#### 对象方法调用

对象方法调用更简单，跟Java语法一样；如“'haha'.substring(2,4)”将返回“ha”；而对于根对象可以直接调用方法

```java
Date date = new Date();  
StandardEvaluationContext context = new StandardEvaluationContext(date);  
int result2 = parser.parseExpression("getYear()").getValue(context, int.class);  
Assert.assertEquals(date.getYear(), result2);
```

#### Bean引用

SpEL支持使用“@”符号来引用Bean，在引用Bean时需要使用BeanResolver接口实现来查找Bean，Spring提供BeanFactoryResolver实现

```java
@Test  
public void testBeanExpression() {  
    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();  
    ctx.refresh();  
    ExpressionParser parser = new SpelExpressionParser();  
    StandardEvaluationContext context = new StandardEvaluationContext();  
    context.setBeanResolver(new BeanFactoryResolver(ctx));  
    Properties result1 = parser.parseExpression("@systemProperties").getValue(context, Properties.class);  
    Assert.assertEquals(System.getProperties(), result1);  
}
```

### 集合相关表达式

#### 内联List

从Spring3.0.4开始支持内联List，使用{表达式，……}定义内联List，如“{1,2,3}”将返回一个整型的ArrayList，而“{}”将返回空的List，对于字面量表达式列表，SpEL会使用java.util.Collections.unmodifiableList方法将列表设置为不可修改。

#### 内联数组

和Java 数组定义类似，只是在定义时进行多维数组初始化。

#### 集合，字典元素访问

SpEL目前支持所有集合类型和字典类型的元素访问，使用“集合[索引]”访问集合元素，使用“map[key]”访问字典元素；

#### 列表，字典，数组元素修改

可以使用赋值表达式或Expression接口的setValue方法修改

#### 集合投影

在SQL中投影指从表中选择出列，而在SpEL指根据集合中的元素中通过选择来构造另一个集合，该集合和原集合具有相同数量的元素；SpEL使用“（list|map）.![投影表达式]”来进行投影运算

#### 集合选择

在SQL中指使用select进行选择行数据，而在SpEL指根据原集合通过条件表达式选择出满足条件的元素并构造为新的集合，SpEL使用“(list|map).?[选择表达式]”，其中选择表达式结果必须是boolean类型，如果true则选择的元素将添加到新集合中，false将不添加到新集合中。

### 表达式模板

模板表达式就是由字面量与一个或多个表达式块组成。每个表达式块由“前缀+表达式+后缀”形式组成，如“${1+2}”即表达式块。

## 在Bean定义中使用EL

### xml风格的配置

SpEL支持在Bean定义时注入，默认使用“#{SpEL表达式}”表示，其中“#root”根对象默认可以认为是ApplicationContext，只有ApplicationContext实现默认支持SpEL，获取根对象属性其实是获取容器中的Bean。

模板默认以前缀“#{”开头，以后缀“}”结尾，且不允许嵌套，如“#{'Hello'#{world}}”错误，如“#{'Hello' + world}”中“world”默认解析为Bean。当然可以使用“@bean”引用了。

### 注解风格的配置

基于注解风格的SpEL配置也非常简单，使用@Value注解来指定SpEL表达式，该注解可以放到字段、方法及方法参数上。

**注：** 配置时必须使用“<context:annotation-config/>”来开启对注解的支持。