Scala 构造对象也需要调用构造方法，并且可以有任意多个构造方法（即 scala 中构造器也支持重载）

Scala 类的构造器包括：**主构造器**和**辅助构造器**

**Scala 构造器的基本语法**

```scala
class 类名(形参列表){// 主构造器，只能有一个
    def this(形参列表){// 辅助构造器
        
    }
    def this(形参列表){//辅助构造器可以有多个
        
    }
}
```

辅助构造器函数的名称是 this，可以有多个，编译器通过不同参数来区分

**注意事项和细节**

1. Scala 的构造器作用是完成对新对象的初始化，构造器没有返回值

2. 主构造器的声明直接放置于类名之后

3. 主构造器会执行类定义中的所有语句

4. 如果主构造器无参数，小括号可以省略，构造对象时调用的构造方法的小括号也可以省略

5. 辅助构造器名称为this，多个辅助构造器通过不同参数列表进行区分，在底层就是 java 的构造器重载

6. 辅助构造器的函数体必须在第一行显式的调用主构造器或其他辅助构造器

7. 如果想让主构造器变成私有的，可以在 () 之前加上private，这样用户只能通过辅助构造器来构建对象

   ```scala
   class Person private(){
       //....
   }
   ```

8. 辅助构造器的声明不能和主构造器的声明一致，会发生错误