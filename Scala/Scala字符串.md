在 Scala 中，字符串的类型实际上是 `java.lang.String`，它本身没有 String 类。

在 Scala 中，String 是一个不可变的对象，所以该对象不可被修改。这意味着如果修改字符串就会产生一个新的字符串对象。

### 创建字符串

```scala
var test="Hello World"
// or
var test:String="Hello World"
```

如果需要创建一个可以修改的字符串，可以使用 StringBuilder 类，如下实例：

```scala
object Test {
   def main(args: Array[String]) {
      val buf = new StringBuilder;
      buf += 'a'
      buf ++= "bcdef"
      println( "buf is : " + buf.toString );
   }
}
```

### 字符串插值

自 2.10.0 版本开始，Scala 提供了一种新的机制来根据数据生成字符串：**字符串插值**。字符串插值允许使用者将变量引用直接插入处理过的字面字符中。如下例：

```scala
val name="James"
println(s"Hello,$name")//Hello,James
```

在上例中， `s"Hello,$name"` 是待处理字符串字面，编译器会对它做额外的工作。待处理字符串字面通过**分号(`"`)**前的字符来标示（例如：上例中是s）。

Scala 提供了三种创新的字符串插值方法：`s`,`f` 和 `raw`

**`s`字符串插值器**

在任何字符串前加上s，就可以直接在串中使用变量了。

字符串插值器也可以处理任意的表示式。例如：

```scala
println(s"1+1=${1+1}") 将会输出字符串1+1=2。任何表达式都可以嵌入到${}中
```

**`f`插值器**

在任何字符串字面前加上 f，就可以生成简单的格式化串，功能相似于其他语言中的 printf 函数。当使用 f 插值器的时候，所有的变量引用都应当后跟一个printf-style格式的字符串，如`%d`。

示例如下：

```scala
val height=1.9d
val name="James"
println(f"$name%s is $height%2.2f meters tall")//James is 1.90 meters tall f 插值器是类型安全的。如果试图向只支持 int 的格式化串传入一个double 值，编译器则会报错。例如：

val height:Double=1.9d

scala>f"$height%4d"
<console>:9: error: type mismatch;
 found : Double
 required: Int
           f"$height%4d"
              ^ f 插值器利用了java中的字符串数据格式。这种以%开头的格式在 [Formatter javadoc] 中有相关概述。如果在具体变量后没有%，则格式化程序默认使用 %s（串型）格式
```

**`raw`插值器**

除了对字面值中的字符不做编码外，raw 插值器与 s 插值器在功能上是相同的。如下是个被处理过的字符串：

```scala
scala>s"a\nb"
res0:String=
a
b 这里，s 插值器用回车代替了\n。而raw插值器却不会如此处理。

scala>raw"a\nb"
res1:String=a\nb 当不想输入\n被转换为回车的时候，raw 插值器是非常实用的。
```

### String 方法

| 序号 | 方法及|描述                                                 |
| :--- | :---------------------|:-------------------------------------- |
| 1    | **char charAt(int index)**|返回指定位置的字符                 |
| 2    | **int compareTo(Object o)**|比较字符串与对象                  |
| 3    | **int compareTo(String anotherString)**|按字典顺序比较两个字符串 |
| 4    | **int compareToIgnoreCase(String str)**|按字典顺序比较两个字符串，不考虑大小写 |
| 5    | **String concat(String str)**|将指定字符串连接到此字符串的结尾 |
| 6    | **boolean contentEquals(StringBuffer sb)**|将此字符串与指定的 StringBuffer 比较。 |
| 7    | **static String copyValueOf(char[] data)**|返回指定数组中表示该字符序列的 String |
| 8    | **static String copyValueOf(char[] data, int offset, int count)**|返回指定数组中表示该字符序列的 String |
| 9    | **boolean endsWith(String suffix)**|测试此字符串是否以指定的后缀结束 |
| 10   | **boolean equals(Object anObject)**|将此字符串与指定的对象比较 |
| 11   | **boolean equalsIgnoreCase(String anotherString)**|将此 String 与另一个 String 比较，不考虑大小写 |
| 12   | **byte getBytes()**|使用平台的默认字符集将此 String 编码为 byte 序列，并将结果存储到一个新的 byte 数组中 |
| 13   | **byte[] getBytes(String charsetName)** |使用指定的字符集将此 String 编码为 byte 序列，并将结果存储到一个新的 byte 数组中 |
| 14   | **void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin)**|将字符从此字符串复制到目标字符数组 |
| 15   | **int hashCode()**|返回此字符串的哈希码                       |
| 16   | **int indexOf(int ch)**|返回指定字符在此字符串中第一次出现处的索引 |
| 17   | **int indexOf(int ch, int fromIndex)**|返回在此字符串中第一次出现指定字符处的索引，从指定的索引开始搜索 |
| 18   | **int indexOf(String str)**|返回指定子字符串在此字符串中第一次出现处的索引 |
| 19   | **int indexOf(String str, int fromIndex)**|返回指定子字符串在此字符串中第一次出现处的索引，从指定的索引开始 |
| 20   | **String intern()**|返回字符串对象的规范化表示形式            |
| 21   | **int lastIndexOf(int ch)**|返回指定字符在此字符串中最后一次出现处的索引 |
| 22   | **int lastIndexOf(int ch, int fromIndex)**|返回指定字符在此字符串中最后一次出现处的索引，从指定的索引处开始进行反向搜索 |
| 23   | **int lastIndexOf(String str)**|返回指定子字符串在此字符串中最右边出现处的索引 |
| 24   | **int lastIndexOf(String str, int fromIndex)**|返回指定子字符串在此字符串中最后一次出现处的索引，从指定的索引开始反向搜索 |
| 25   | **int length()**|返回此字符串的长度                           |
| 26   | **boolean matches(String regex)**|告知此字符串是否匹配给定的正则表达式 |
| 27   | **boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len)**|测试两个字符串区域是否相等 |
| 28   | **boolean regionMatches(int toffset, String other, int ooffset, int len)**|测试两个字符串区域是否相等 |
| 29   | **String replace(char oldChar, char newChar)**|返回一个新的字符串，它是通过用 newChar 替换此字符串中出现的所有 oldChar 得到的 |
| 30   | **String replaceAll(String regex, String replacement)** |使用给定的 replacement 替换此字符串所有匹配给定的正则表达式的子字符串 |
| 31   | **String replaceFirst(String regex, String replacement)**|使用给定的 replacement 替换此字符串匹配给定的正则表达式的第一个子字符串 |
| 32   | **String[] split(String regex)**|根据给定正则表达式的匹配拆分此字符串 |
| 33   | **String[] split(String regex, int limit)**|根据匹配给定的正则表达式来拆分此字符串 |
| 34   | **boolean startsWith(String prefix)**|测试此字符串是否以指定的前缀开始 |
| 35   | **boolean startsWith(String prefix, int toffset)**|测试此字符串从指定索引开始的子字符串是否以指定前缀开始。 |
| 36   | **CharSequence subSequence(int beginIndex, int endIndex)**|返回一个新的字符序列，它是此序列的一个子序列 |
| 37   | **String substring(int beginIndex)**|返回一个新的字符串，它是此字符串的一个子字符串 |
| 38   | **String substring(int beginIndex, int endIndex)**|返回一个新字符串，它是此字符串的一个子字符串 |
| 39   | **char[] toCharArray()**|将此字符串转换为一个新的字符数组     |
| 40   | **String toLowerCase()**|使用默认语言环境的规则将此 String 中的所有字符都转换为小写 |
| 41   | **String toLowerCase(Locale locale)**|使用给定 Locale 的规则将此 String 中的所有字符都转换为小写 |
| 42   | **String toString()**|返回此对象本身（它已经是一个字符串！）  |
| 43   | **String toUpperCase()**|使用默认语言环境的规则将此 String 中的所有字符都转换为大写 |
| 44   | **String toUpperCase(Locale locale)**|使用给定 Locale 的规则将此 String 中的所有字符都转换为大写 |
| 45   | **String trim()**|删除指定字符串的首尾空白符                  |
| 46   | **static String valueOf(primitive data type x)**|返回指定类型参数的字符串表示形式 |

