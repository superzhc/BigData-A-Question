Scala 提供 `try...catch` 块来处理异常，语法处理上和 Java 类似，但是又不尽相同。

1. 在 scala 中只有一个catch
2. 在 catch 中有多个 case，每个 case 可以匹配一种异常 `case ex:ArithmeticException`
3. `=>` 关键符号，表示后面是该异常的处理块