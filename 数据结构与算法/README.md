# 数据结构与算法

## 数据结构

数据结构是一门研究非数值计算的程序设计问题中计算机的操作对象以及它们之间的关系及操作的学科。

数据结构是一门介于数学、计算机硬件和计算机软件三者之间的核心课程。数据结构不仅是一般程序设计的基础，而且是设计和实现编译程序、操作系统、数据库系统及其他系统程序和大型应用程序的重要基础。

### 概念和术语

#### 数据

数据即信息的载体，是对客观事物的符号表示，凡能输入到计算机中并被计算机程序处理的符号都可称之为数据，如整数、实数、字符、文字、声音、图像等都是数据。

#### 数据元素

数据元素是数据的基本单位，它在计算机处理和程序设计中通常作为独立个体。数据元素一般由一个或多个数据项组成，一个数据元素包含多个数据项时，常称为记录、结点等。数据项也称为域、字段、属性、表目、顶点。

#### 数据对象

数据对象是具有相同特征的数据元素的集合，是数据的一个子集，如一个整型数组、一个字符串数组都是一个数组对象。

#### 数据结构

数据结构简称 DS（Data Structure），是数据及数据元素的组织形式。任何数据都不是彼此孤立的，通常把相关联的数据按照一定的逻辑关系组织起来，这样就形成了一个数据结构。

数据结构包含两个方面的内容，一是数据对象，二是数据对象中数据元素之间内在的关系。数据结构通常有四类基本形式：*集合结构*，*线性结构*，*树型结构*，*图形结构*或*网状结构*。

**集合结构**

集合结构中的数据元素除了同属于一个集合外，它们之间没有其他关系。各个数据元素是“平等”的，它们的共同属性是“同属于一个集合”。数据结构中的集合关系就类似于数学中的集合。

**线性结构**

线性结构中的数据元素之间是一对一的关系。

**树型结构**

树形结构中的数据元素之间存在一种一对多的层次关系。

**图状结构**

图状结构中的数据元素是多对多的关系。

#### 数据类型

数据类型是一组具有相同性质的操作对象以及该组操作对象上的运算方法的集合，如整数类型、字符类型等。每一种数据类型都有自身特点的一组操作方法，即运算规则。JAVA 中提供的基本的数据类型有 int，double，long，float，short，byte，character，boolean。由于集合中的元素的关系极为松散，可用其他数据结构来表示。从数据类型和数据结构的概念可知，二者的关系非常密切。数据类型可以看作是简单的数据结构。数据的取值范围可以看作是数据元素的有限集合，而对数据进行操作的集合可以看作是数据元素之间关系的集合。

### 数据的逻辑结构

**数据的逻辑结构**（Logic Structure）是从具体问题抽象出来的数学模型，与数据在计算机中的具体存储没有关系。数据的逻辑结构独立于计算机，是数据本身所固有的特性。从逻辑上可以把数据结构分为线性结构和非线性结构，上述数据结构的定义就是数据的逻辑结构（Logic Structure），主要包括：集合、线性、树和图形结构，其中集合、树和图形结构都属于非线性结构。

数据的逻辑结构有两个要素：一是数据元素的集合，通常记为 D；二是 D 上的关系集，它反映了 D 中各数据元素之间的前驱后继关系，通常记为 R，即一个数据结构可以表示成二元组 `B=(D,R)`。

### 数据的物理结构

**数据的物理结构**（Physical Structure）又称存储结构，它的实现依赖于具体的计算机语言。数据存储结构有顺序和链式两种不同的方式，顺序存储的特点是数据元素在存储器的相对位置来体现数据元素相互间的逻辑关系，顺序存储结构通常用高级编程语言中的一维数组来描述或实现。链式存储结构是通过一组任意的存储单元来存储数据元素的，而这些存储单元可以是连续的，也可以是不连续的。

## 算法

### 算法的定义

> 算法（algorithm）是解决特定问题求解步骤的描述，在计算机中表现得为指令的有限序列，并且每条指令表示一个或多个操作。

### 算法的特性

算法具有五个基本特性：**输入**、**输出**、**有穷性**、**确定性**和**可行性**

- **输入输出**

> **算法具有零个或多个输入**
>
> **算法至少有一个或多个输出**

- **有穷性**

> 有穷性：指算法在执行有限的步骤之后，自动结束而不会出现无限循环，并且每一个步骤在可接受的时间内完成

- **确定性**

> 确定性：算法的每一个步骤都具有确定的定义，不会出现二义性。

- **可行性**

> 可行性：算法的每一步都必须是可行的，也就是说，每一步都能够通过执行有限次数完成。

### 算法设计的要求

- **正确性**

> 正确性：算法的正确性是指算法至少应该具有输入、输出和加工处理无歧义性、能正确反映问题的需求、能够得到问题的正确答案。

- **可读性**

> 可读性：算法设计的另一个目的是为了便于阅读、理解和交流。

- **健壮性**

> 健壮性：当输入数据不合法时，算法也能够做出相应处理，而不是产生异常或莫名其妙的结果。

- **时间效率高和存储量低**

> 设计算法应该尽量满足时间效率高和存储量低的需求。

