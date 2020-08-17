# IO

> Java IO 是一套Java用来读写数据（输入和输出）的API。大部分程序都要处理一些输入，并由输入产生一些输出。Java为此提供了java.io包。

## 流的概念和作用

Java 的 IO 包主要关注的是从原始数据源的读取以及输出原始数据到目标媒介。以下是最典型的数据源和目标媒介：

- 文件
- 管道
- 网络连接
- 内存缓存
- System.in, System.out, System.error(注：Java标准输入、输出、错误输出)

下面这张图描绘了一个程序从数据源读取数据，然后将数据输出到其他媒介的原理：

![数据源与目标媒介](images/IO1.png)

### 流

在 Java IO 中，流是一个核心的概念。 **流从概念上来说是一个连续的数据流** 。你既可以从流中读取数据，也可以往流中写数据。流与数据源或者数据流向的媒介相关联。在 Java IO 中流既可以是 **字节流(以字节为单位进行读写)**，**也可以是字符流(以字符为单位进行读写)**。

流和数组不一样，不能通过索引读写数据。在流中，也不能像数组那样前后移动读取数据，除非使用 `RandomAccessFile` 处理文件。流仅仅只是一个连续的数据流。

某些类似 `PushbackInputStream` 流的实现允许将数据重新推回到流中，以便重新读取。然而也只能把有限的数据推回流中，并且不能像操作数组那样随意读取数据。**流中的数据只能够顺序访问。**

Java IO 流通常是基于字节或者基于字符的:

- 字节流通常以“**Stream**”命名，比如 `InputStream` 和 `OutputStream`。除了 `DataInputStream` 和 `DataOutputStream` 还能够读写int, long, float和double类型的值以外，其他流在一个操作时间内只能读取或者写入一个原始字节。
- 字符流通常以“**Reader**”或者“**Writer**”命名。字符流能够读写字符(比如Latin1或者Unicode字符)。

### Java IO 的用途和特征

Java IO 中包含了许多 InputStream、OutputStream、Reader、Writer 的子类。这样设计的原因是让每个类都负责不同的功能。这也就是为什么 IO 包中有这么多不同类的缘故。各类用途汇总如下：

- 文件访问
- 网络访问
- 内存缓存访问
- 线程内部通信（管道）
- 缓冲
- 过滤
- 解析
- 读写文本（Reader、Writer）
- 读写基本类型数据（long、int...）
- 读写对象

### Java IO类概述表

![Java IO类概述表](images/IO3.png)

## 流的分类

- 根据处理数据类型的不同分为：字符流和字节流
- 根据数据流向不同分为：输入流和输出流

### 字节流和字符流

字符流的由来：因为数据编码的不同，而有了对字符进行高效操作的流对象。本质其实就是基于字节流读取时，去查了指定的码表。

字节流和字符流的区别：

- 读写单位不同：字节流以字节（8bit为单位），字符流以字符为单位，根据码表映射字符，一次可能读多个字节
- 处理对象不同：字节流能处理所有类型的数据（如图片、avi等），而字符流只能处理字符类型的数据

结论：只要是处理纯文本数据，就优先考虑使用字符流。除此之外都使用字节流。

### 输入和输出流

对输入流只能进行读操作，对输出流只能进行写操作，程序中需要根据待传输数据的不同特性而使用不同的流。

## 文件

在 Java 应用程序中，文件是一种常用的数据源或者存储数据的媒介。

### 读文件

可以根据文件是二进制文件还是文本文件来选择使用 FileInputStream 或者 FileReader。这两个类允许你从文件开始到文件末尾一次读取一个字节或一个字符，或者将读取到的字节写入到字节数组或者字符数组。不必一次性读取整个文件，相反可以按顺序地读取文件中的字节和字符。

如果需要跳跃式的读取文件其中的某些部分，可以使用 RandomAccessFile。

### 写文件

可以根据要写入的数据是二进制型数据还是字符型数据选用 FileOutputStream 或者 FileWriter。可以一次写入一个字节或者字符到文件中，也可以直接写入一个字节数组或者字符数据。数据按照写入的顺序存储在文件当中。

### 随机存取文件

通过RandomAccessFile对文件进行随机存取。

随机存取并不意味着可以在真正随机的位置进行读写操作，它只是意味着可以跳过文件中某些部分进行操作，并且支持同时读写，不要求特定的存取顺序。这使得 RandomAccessFile 可以覆盖一个文件的某些部分、或者追加内容到它的末尾、或者删除它的某些内容，当然它也可以从文件的任何位置开始读取文件。

## 管道

Java IO 中的管道为运行在同一个 JVM 中的两个线程提供了通信的能力，所以管道也可以作为数据源以及目标媒介。

注：不能利用管道与不同的 JVM 中的线程通信（不同的进程）。在概念上，Java 的管道不同于 Unit/Linux 系统中的管道。在 Unit/Linux 中，运行在不同地址空间的两个进程可以通过管道通信，在 Java 中，通信的双方应该是运行在同一进程中不同线程。

### 创建管道

可以通过 Java IO 中的 PipedOutputStream 和 PipedInputStream 创建管道。一个 PipedInputStream 流应该和一个 PipedOutputStream 流相关联。一个线程通过 PipedOutputStream 写入的数据可以被另一个线程通过相关联的 PipedInputStream 读取出来。

### 管道和线程

当使用两个相关联的管道流时，务必将它们分配给不同的线程。`read()` 方法和 `write()` 方法调用是会导致流阻塞，这意味着如果尝试在一个线程中同时进行读和写，可能会导致线程死锁。

### 管道的替代

除了管道之外，一个 JVM 中不同线程之间还有许多通信的方式。实际上，线程在大多数的情况下会传递完整的对象信息而非原始的字节数据。但是，如果需要在线程之间传递字节数据，Java IO 的管道是一个不错的选择。

## 网络

网络是一个常见的数据来源以及数据流目的地。

当两个进程之间建立了网络连接之后，它们通信的方式如同操作文件一样：利用InputStream读取数据，利用OutputStream写入数据。换句话来说，Java网络API用来在不同进程之间建立网络连接，而Java IO则用来在建立了连接之后的进程之间交换数据。

基本上意味着如果你有一份能够对文件进行写入某些数据的代码，那么这些数据也可以很容易地写入到网络连接中去。

## 字节和字符数组

内容列表：

- 从 InputStream 或者 Reader 中读入数组
- 从 OutputStream 或者 Writer 中写数组

在java中常用字节和字符数组在应用中临时存储数据。而这些数组又是通常的数据读取来源或者写入目的地。

## System.in,System.out,System.err

`System.in`, `System.out`, `System.err` 这3个流同样是常见的数据来源和数据流目的地。使用最多的可能是在控制台程序里利用 `System.out` 将输出打印到控制台上。

JVM 启动的时候通过 Java 运行时初始化这3个流，所以用户不需要初始化它们(尽管用户可以在运行时替换掉它们)。

### System.in

`System.in` 是一个典型的连接控制台程序和键盘输入的 InputStream 流。通常当数据通过命令行参数或者配置文件传递给命令行 Java 程序的时候，System.in 并不是很常用。图形界面程序通过界面传递参数给程序，这是一块单独的 Java IO 输入机制。

### System.out

`System.out` 是一个 PrintStream 流。`System.out` 一般会把你写到其中的数据输出到控制台上。`System.out` 通常仅用在类似命令行工具的控制台程序上。`System.out` 也经常用于打印程序的调试信息(尽管它可能并不是获取程序调试信息的最佳方式)。

### System.err

`System.err` 是一个 PrintStream 流。`System.err` 与 `System.out` 的运行方式类似，但它更多的是用于打印错误文本。一些类似Eclipse的程序，为了让错误信息更加显眼，会将错误信息以红色文本的形式通过 `System.err` 输出到控制台上。

### 替换系统流

尽管 `System.in`, `System.out`, `System.err` 这3个流是 `java.lang.System` 类中的静态成员(这3个变量均为final static常量)，并且已经预先在JVM启动的时候初始化完成，但用户依然可以更改它们。只需要把一个新的 InputStream 设置给 `System.in` 或者一个新的 OutputStream 设置给 `System.out` 或者 `System.err`，之后的数据都将会在新的流中进行读取、写入。

可以使用 `System.setIn()`, `System.setOut()`, `System.setErr()` 方法设置新的系统流(这三个方法均为静态方法，内部调用了本地native方法重新设置系统流)。例子如下：

```java
OutputStream output = new FileOutputStream("c:\\data\\system.out.txt");
PrintStream printOut = new PrintStream(output);
System.setOut(printOut);
```

现在所有的System.out都将重定向到”c:\\data\\system.out.txt”文件中。请记住，务必在JVM关闭之前冲刷System.out(调用flush())，确保System.out把数据输出到了文件中。

## Java IO 流对象

Java 流操作有关的类和接口

类              | 说明
:--------------|:------------------
File           | 文件类
RandomAccessFile | 随机存取文件类
InputStream    | 字节输入流
OutputStream   | 字节输出流
Reader         | 字符输入流
Writer         | 字符输出流

一个程序需要 InputStream 或者 Reader 从数据源读取数据，需要 OutputStream 或者 Writer 将数据写入到目标媒介中。以下的图说明了这一点：

![InputStream、Reader、OutputStream、Writer](images/IO2.png)

InputStream 和 Reader 与数据源关联，OutputStream 和 Writer 与目标媒介相关联。

### InputStream 输入字节流

InputStream 是所有的输入字节流的父类，它是一个抽象类。它的子类分类如下：

- ByteArrayInputStream、StringBufferInputStream、FileInputStream 是三种基本的介质流，它们分别从Byte 数组、StringBuffer、和本地文件中读取数据。PipedInputStream 是从与其它线程共用的管道中读取数据。
- ObjectInputStream 和所有FilterInputStream 的子类都是装饰流（装饰器模式的主角）。

通常使用输入流中的read()方法读取数据。read()方法返回一个整数，代表了读取到的字节的内容(0 ~ 255)。当达到流末尾没有更多数据可以读取的时候，read()方法返回-1。

示例：

```java
InputStream input = new FileInputStream("c:\\data\\input-file.txt");
int data = input.read();
while(data != -1){
    data = input.read();
}
```

注：从Java7开始，可以使用“try-with-resource”结构确保InputStream在结束使用之后关闭，当执行线程退出try语句块的时候，InputStream变量会被关闭。代码如下：

```java
try( InputStream inputstream = new FileInputStream("file.txt") ) {
    int data = inputstream.read();
    while(data != -1){
        System.out.print((char) data);
        data = inputstream.read();
    }
}
```

#### read()

read()方法返回从InputStream流内读取到的一个字节内容(0~255)，例子如下：

`int data = inputstream.read();`

可以把返回的int类型转化成char类型：

`char aChar = (char) data;`

InputStream 的子类可能会包含 `read()` 方法的替代方法。比如，DataInputStream允许使用 `readBoolean()`，`readDouble()` 等方法读取Java基本类型变量int，long，float，double和boolean。

#### 流末尾

如果read()方法返回-1，意味着程序已经读到了流的末尾，此时流内已经没有多余的数据可供读取了。-1是一个int类型，不是byte或者char类型，这是不一样的。

注：当达到流末尾时，就可以关闭流了。

#### read(byte[])

InputStream 包含了2个从 InputStream 中读取数据并将数据存储到缓冲数组中的read()方法，它们分别是：

- int read(byte[])
- int read(byte, int offset, int length)

一次性读取一个字节数组的方式，比一次性读取一个字节的方式快的多，所以，尽可能使用这两个方法代替read()方法。

- read(byte[])方法会尝试读取与给定字节数组容量一样大的字节数，返回值说明了已经读取过的字节数。如果InputStream内可读的数据不足以填满字节数组，那么数组剩余的部分将包含本次读取之前的数据。记得检查有多少数据实际被写入到了字节数组中。
- read(byte, int offset, int length)方法同样将数据读取到字节数组中，不同的是，该方法从数组的offset位置开始，并且最多将length个字节写入到数组中。同样地，read(byte, int offset, int length)方法返回一个int变量，告诉你已经有多少字节已经被写入到字节数组中，所以请记得在读取数据前检查上一次调用read(byte, int offset, int length)的返回值。

这两个方法都会在读取到达到流末尾时返回-1。

```java
InputStream inputstream = new FileInputStream("c:\\data\\input-text.txt");
byte[] data = new byte[1024];
int bytesRead = inputstream.read(data);
while(bytesRead != -1) {
    doSomethingWithData(data, bytesRead);
    bytesRead = inputstream.read(data);
}

inputstream.close();
```

在代码中，首先创建了一个字节数组。然后声明一个叫做bytesRead的存储每次调用read(byte[])返回值的int变量，并且将第一次调用read(byte[])得到的返回值赋值给它。在while循环内部，把字节数组和已读取字节数作为参数传递给doSomethingWithData方法然后执行调用。在循环的末尾，再次将数据写入到字节数组中。

### OutputStream 输出字节流

OutputStream 是所有的输出字节流的父类，它是一个抽象类。它的子类分类如下：

- ByteArrayOutputStream、FileOutputStream 是两种基本的介质流，它们分别向Byte 数组、和本地文件中写入数据。PipedOutputStream 是向与其它线程共用的管道中写入数据，
- ObjectOutputStream 和所有FilterOutputStream 的子类都是装饰流。

示例：

```java
OutputStream output = new FileOutputStream("c:\\data\\output-file.txt");
output.write("Hello World".getBytes());
output.close();
```

### 字节流子类

#### FileInputStream

FileInputStream 可以以字节流的形式读取文件内容。FileInputStream 是 InputStream 的子类，这意味着可以把 FileInputStream 当做 InputStream 使用(FileInputStream 与 InputStream 的行为类似)。

```java
InputStream input = new FileInputStream("c:\\data\\input-text.txt");
int data = input.read();
while(data != -1) {
    //do something with data...
    doSomethingWithData(data);
    data = input.read();
}
input.close();
```

FileInputStream 的 `read()` 方法返回读取到的包含一个字节内容的int变量(注：0~255)。如果 `read()` 方法返回-1，意味着程序已经读到了流的末尾，此时流内已经没有多余的数据可供读取了，你可以关闭流。-1是一个int类型，不是byte类型，这是不一样的。

FileInputStream也有其他的构造函数，允许你通过不同的方式读取文件。

#### FileOutputStream

FileOutputStream可以往文件里写入字节流，它是OutputStream的子类。

```java
OutputStream output = new FileOutputStream("c:\\data\\output-text.txt");
while(moreData) {
    int data = getMoreData();
    output.write(data);
}
output.close();
```

FileOutputStream的write()方法取一个包含了待写入字节(译者注：低8位数据)的int变量作为参数进行写入。

FileOutputStream也有其他的构造函数，允许你通过不同的方式写入文件。

##### flush()

当往 FileOutputStream 里写数据的时候，这些数据有可能会缓存在内存中。在之后的某个时间，比如，每次都只有X份数据可写，或者 FileOutputStream 关闭的时候，才会真正地写入磁盘。当 FileOutputStream 没被关闭，而又想确保写入到 FileOutputStream 中的数据写入到磁盘中，可以调用 `flush()` 方法，该方法可以保证所有写入到 FileOutputStream 的数据全部写入到磁盘中。

#### RandomAccessFile

RandomAccessFile 允许来回读写文件，也可以替换文件中的某些部分。FileInputStream 和 FileOutputStream 没有这样的功能。

##### 创建一个 RandomAccessFile

在使用RandomAccessFile之前，必须初始化它。这是例子：

`RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");`

请注意构造函数的第二个参数：“rw”，表明以读写方式打开文件。

##### 在 RandomAccessFile 中来回读写

在 RandomAccessFile 的某个位置读写之前，必须把文件指针指向该位置。通过 `seek()` 方法可以达到这一目标。可以通过调用 `getFilePointer()` 获得当前文件指针的位置。例子如下：

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");
file.seek(200);
long pointer = file.getFilePointer();
file.close();
```

##### 读取 RandomAccessFile

RandomAccessFile 中的任何一个 `read()` 方法都可以读取 RandomAccessFile 的数据。例子如下：

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");
int aByte = file.read();
file.close();
```

`read()` 方法返回当前 RandomAccessFile 实例的文件指针指向的位置中包含的字节内容。Java文档中遗漏了一点：`read()` 方法在读取完一个字节之后，会自动把指针移动到下一个可读字节。这意味着使用者在调用完 `read()` 方法之后不需要手动移动文件指针。

##### 写入 RandomAccessFile

RandomAccessFile 中的任何一个 `write()` 方法都可以往 RandomAccessFile 中写入数据。例子如下：

```java
RandomAccessFile file = new RandomAccessFile("c:\\data\\file.txt", "rw");
file.write("Hello World".getBytes());
file.close();
```

与 `read()` 方法类似，`write()` 方法在调用结束之后自动移动文件指针，所以不需要频繁地把指针移动到下一个将要写入数据的位置。

##### RandomAccessFile 异常处理

RandomAccessFile 与其他流一样，在使用完毕之后必须关闭。

#### PipedInputStream

PipedInputStream 可以从管道中读取字节流数据，代码如下：

```java
InputStream input = new PipedInputStream(pipedOutputStream);
int data = input.read();
while(data != -1) {
    //do something with data...
    doSomethingWithData(data);
    data = input.read();
}
input.close();
```

PipedInputStream 的 `read()` 方法返回读取到的包含一个字节内容的int变量(注：0~255)。如果 `read()` 方法返回-1，意味着程序已经读到了流的末尾，此时流内已经没有多余的数据可供读取了，可以关闭流。-1是一个int类型，不是byte类型，这是不一样的。

#### PipedOutputStream

PipedOutputStream 可以往管道里写入读取字节流数据，代码如下：

```java
OutputStream output = new PipedOutputStream(pipedInputStream);
while(moreData) {
    int data = getMoreData();
    output.write(data);
}
output.close();
```

PipedOutputStream 的 `write()` 方法取一个包含了待写入字节的int类型变量作为参数进行写入。

#### 字节流的ByteArray和Filter

本小节简要的概括Java IO中字节数组与过滤器的输入输出流，主要涉及以下4个类型的流：ByteArrayInputStream，ByteArrayOutputStream，FilterInputStream，FilterOutputStream。

##### ByteArrayInputStream

ByteArrayInputStream 允许从字节数组中读取字节流数据，代码如下：

```java
byte[] bytes = ... //get byte array from somewhere.
InputStream input = new ByteArrayInputStream(bytes);
int data = input.read();
while(data != -1) {
    //do something with data
    data = input.read();
}
input.close();
```

如果数据存储在数组中，ByteArrayInputStream 可以很方便地读取数据。

##### ByteArrayOutputStream

ByteArrayOutputStream允许以数组的形式获取写入到该输出流中的数据，代码如下：

```java
ByteArrayOutputStream output = new ByteArrayOutputStream();

//write data to output stream

byte[] bytes = output.toByteArray();
```

##### FilterInputStream

FilterInputStream 是实现自定义过滤输入流的基类，基本上它仅仅只是覆盖了InputStream中的所有方法。

##### FilterOutputStream

#### 字节流的Buffered和Data

##### BufferedInputStream

BufferedInputStream 能为输入流提供缓冲区，能提高很多IO的速度。可以一次读取一大块的数据，而不需要每次从网络或者磁盘中一次读取一个字节。特别是在访问大量磁盘数据时，缓冲通常会让IO快上许多。

为了给输入流加上缓冲，需要把输入流包装到 BufferedInputStream 中，代码如下：

```java
InputStream input = new BufferedInputStream(new FileInputStream("c:\\data\\input-file.txt"));
```

可以给 BufferedInputStream 的构造函数传递一个值，设置内部使用的缓冲区设置大小(注：默认缓冲区大小8 * 1024B)，就像这样：

`InputStream input = new BufferedInputStream(new FileInputStream("c:\\data\\input-file.txt"), 8 * 1024);`

这个例子设置了8KB的缓冲区。最好把缓冲区大小设置成1024字节的整数倍，这样能更高效地利用内置缓冲区的磁盘。

除了能够为输入流提供缓冲区以外，其余方面 BufferedInputStream 基本与 InputStream 类似。

##### BufferedOutputStream

与BufferedInputStream类似，BufferedOutputStream可以为输出流提供缓冲区。可以构造一个使用默认大小缓冲区的BufferedOutputStream(注：默认缓冲区大小8 * 1024B)，代码如下：

`OutputStream output = new BufferedOutputStream(new FileOutputStream("c:\\data\\output-file.txt"));`

也可以手动设置缓冲区大小，代码如下：

`OutputStream output = new BufferedOutputStream(new FileOutputStream("c:\\data\\output-file.txt"), 8 * 1024);`

为了更好地使用内置缓冲区的磁盘，同样建议把缓冲区大小设置成1024的整数倍。

除了能够为输出流提供缓冲区以外，其余方面BufferedOutputStream基本与OutputStream类似。唯一不同的时，需要手动flush()方法确保写入到此输出流的数据真正写入到磁盘或者网络中。

##### DataInputStream

DataInputStream可以从输入流中读取Java基本类型数据，而不必每次读取字节数据。可以把InputStream包装到DataInputStream中，然后就可以从此输入流中读取基本类型数据了，代码如下：

```java
DataInputStream input = new DataInputStream(new FileInputStream("binary.data"));

int aByte = input.read();

int anInt = input.readInt();

float aFloat = input.readFloat();

double aDouble = input.readDouble();//etc.

input.close();
```

当要读取的数据中包含了int，long，float，double这样的基本类型变量时，DataInputStream可以很方便地处理这些数据。

##### DataOutputStream

DataOutputStream可以往输出流中写入Java基本类型数据，例子如下：

```java
DataOutputStream output = new DataOutputStream(new FileOutputStream("binary.data"));

output.write(45);

//byte data output.writeInt(4545);

//int data output.writeDouble(109.123);

//double data  output.close();
```

其他方面与DataInputStream类似，不再赘述。

#### 序列化与ObjectInputStream、ObjectOutputStream

##### Serializable

如果希望类能够序列化和反序列化，必须实现Serializable接口。

##### ObjectInputStream

ObjectInputStream 能够从输入流中读取Java对象，而不需要每次读取一个字节。可以把InputStream包装到ObjectInputStream中，然后就可以从中读取对象了。代码如下：

```java
ObjectInputStream input = new ObjectInputStream(new FileInputStream("object.data"));
MyClass object = (MyClass) input.readObject(); //etc.
input.close();
```

在这个例子中，读取的对象必须是MyClass的一个实例，并且必须事先通过ObjectOutputStream序列化到“object.data”文件中。(注：ObjectInputStream和ObjectOutputStream还有许多read和write方法，比如readInt、writeLong等等)。

注：在序列化和反序列化一个对象之前，该对象的类必须实现了java.io.Serializable接口。

##### ObjectOutputStream

ObjectOutputStream能够把对象写入到输出流中，而不需要每次写入一个字节。可以把OutputStream包装到ObjectOutputStream中，然后就可以把对象写入到该输出流中了。代码如下：

```java
ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("object.data"));
MyClass object = new MyClass();  
output.writeObject(object); //etc.
output.close();
```

例子中序列化的对象object现在可以从ObjectInputStream中读取了。

同样，在序列化和反序列化一个对象之前，该对象的类必须实现了java.io.Serializable接口。

#### 其他字节流

### Reader 字符输入流

Reader类是Java IO中所有Reader的基类。子类包括BufferedReader，PushbackReader，InputStreamReader，StringReader和其他Reader。

Reader 基于字符而非基于字节，换句话说，Reader用于读取文本，而InputStream用于读取原始字节。

注：Java内部使用UTF8编码表示字符串。输入流中一个字节可能并不等同于一个UTF8字符。如果从输入流中以字节为单位读取UTF8编码的文本，并且尝试将读取到的字节转换成字符，可能会得不到预期的结果。

```java
Reader reader = new FileReader("c:\\data\\myfile.txt");
int data = reader.read();
while(data != -1){
    char dataChar = (char) data;
    data = reader.read();
}
```

注意：InputStream的read()方法返回一个字节，意味着这个返回值的范围在0到255之间(当达到流末尾时，返回-1)，Reader的read()方法返回一个字符，意味着这个返回值的范围在0到65535之间(当达到流末尾时，同样返回-1)。这并不意味着Reade只会从数据源中一次读取2个字节，Reader会根据文本的编码，一次读取一个或者多个字节。

### Writer 字符输出流

Writer类是Java IO中所有Writer的基类。子类包括BufferedWriter和PrintWriter等等。

```java
Writer writer = new FileWriter("c:\\data\\file-output.txt");
writer.write("Hello World Writer");
writer.close();
```

### 字符流子类

#### InputStreamReader和OutputStreamWriter

##### InputStreamReader

InputStreamReader会包含一个InputStream，从而可以将该输入字节流转换成字符流，代码例子：

```java
InputStream inputStream = new FileInputStream("c:\\data\\input.txt");
Reader reader = new InputStreamReader(inputStream);
int data = reader.read();
while(data != -1){
    char theChar = (char) data;
    data = reader.read();
}
reader.close();
```

read()方法返回一个包含了读取到的字符内容的int类型变量(译者注：0~65535)。代码如下：

`int data = reader.read();`

可以把返回的int值转换成char变量，就像这样：

`char aChar = (char) data; //注：这里不会造成数据丢失，因为返回的int类型变量data只有低16位有数据，高16位没有数据`

如果方法返回-1，表明Reader中已经没有剩余可读取字符，此时可以关闭Reader。-1是一个int类型，不是byte或者char类型，这是不一样的。

InputStreamReader同样拥有其他可选的构造函数，能够让你指定将底层字节流解释成何种编码的字符流。例子如下：

```java
InputStream inputStream = new FileInputStream("c:\\data\\input.txt");

Reader reader = new InputStreamReader(inputStream, "UTF-8");
```

##### OutputStreamWriter

OutputStreamWriter会包含一个OutputStream，从而可以将该输出字节流转换成字符流，代码如下：

```java
OutputStream outputStream = new FileOutputStream("c:\\data\\output.txt");
Writer writer = new OutputStreamWriter(outputStream);
writer.write("Hello World");
writer.close();
```

OutputStreamWriter同样拥有将输出字节流转换成指定编码的字符流的构造函数。

#### FileReader和FileWriter

#### 字符流的Buffered和Filter

#### 字符流的Piped和CharArray

#### 其他字符流

### File

Java IO API 中的 File 类可以让访问底层文件系统，通过 File 类，可以做到以下几点：

- 检测文件是否存在
- 读取文件长度
- 重命名或移动文件
- 删除文件
- 检测某个路径是文件还是目录
- 读取目录中的文件列表

注意：File 只能访问文件以及文件系统的元数据。如果想读写文件内容，需要使用 FileInputStream、FileOutputStream 或者 RandomAccessFile。如果正在使用Java NIO，并且想使用完整的NIO解决方案，会使用到 `java.nio.FileChannel`(否则也可以使用File)。

#### 实例化一个 java.io.File 对象

在使用File之前，必须拥有一个File对象，这是实例化的代码例子：

`File file = new File("c:\\data\\input-file.txt");`

#### 检测文件是否存在

通过调用exists()方法，可以检测文件是否存在，代码如下：

```java
File file = new File("c:\\data\\input-file.txt");
boolean fileExists = file.exists();
```

#### 文件长度

通过调用length()可以获得文件的字节长度，代码如下：

```java
File file = new File("c:\\data\\input-file.txt");
long length = file.length();
```

#### 重命名或移动文件

通过调用File类中的renameTo()方法可以重命名(或者移动)文件，代码如下：

```java
File file = new File("c:\\data\\input-file.txt");
boolean success = file.renameTo(new File("c:\\data\\new-file.txt"));
```

#### 删除文件

通过调用delete()方法可以删除文件，代码如下：

```java
File file = new File("c:\\data\\input-file.txt");
boolean success = file.delete();
```

delete()方法与rename()方法一样，返回布尔值表明是否成功删除文件，同样也会有相同的操作失败原因。

#### 检测某个路径是文件还是目录

File 对象既可以指向一个文件，也可以指向一个目录。可以通过调用 `isDirectory()` 方法，可以判断当前 File 对象指向的是文件还是目录。当方法返回值是true时，File指向的是目录，否则指向的是文件，代码如下：

```java
File file = new File("c:\\data");
boolean isDirectory = file.isDirectory();
```

#### 读取目录中的文件列表

可以通过调用 `list()` 或者 `listFiles()` 方法获取一个目录中的所有文件列表。`list()` 方法返回当前File对象指向的目录中所有文件与子目录的字符串名称(注：不会返回子目录下的文件及其子目录名称)。`listFiles()` 方法返回当前File对象指向的目录中所有文件与子目录相关联的File对象(注：与 `list()` 方法类似，不会返回子目录下的文件及其子目录)。代码如下：

```java
File file = new File("c:\\data");
String[] fileNames = file.list();
File[] files = file.listFiles();
```