示例：

```scala
class A

a.getClass // Class[_<:A]=class A

classOf[A] // Class[A]=class A
```

示例中显示两者的不同，`getClass`方法得到的是`Class[A]`的某个子类，而`classOf[A]`得到是正确的`Class[A]`，但对这两个类型进行`==`比较是true。但有个细微的差别，体现在类型赋值时，因为Java里的`Class[T]`是不支持协变的，所以无法把一个`Class[_<:A]`赋值给一个`Class[A]`。