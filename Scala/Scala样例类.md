样例类 `Case Class`

`Case Class` 一般被翻译成**样例类**，它是一种特殊的类，能够被优化以用于模式匹配。

当一个类被声名为`case class`的时候，scala会帮助我们做下面几件事情：
  * 1、构造器中的参数如果不被声明为var的话，它**默认的是val类型**的，但一般不推荐将构造器中的参数声明为var。
  * 2、**自动创建伴生对象**，同时在里面给我们**实现了apply方法**，使我们在使用的时候可以不直接使用new创建对象。
  * 3、伴生对象中同样会帮我们**实现unapply方法**，从而可以将case class应用于模式匹配。
  * 4、实现自己的toString、hashCode、copy、equals方法

除此之此，case class与其它普通的scala类没有区别

case class在实用应用中的用途：

某个类一旦被定义为case class，则编译器会自动生成该类的伴生对象，伴生对象中包括了apply方法及unapply方法，apply方法使得我们可以不需要new关键字就可以创建对象，而unapply方法，则使得可以方便地应用在模式匹配当中，另外编译器还自动地帮我们实现对应的toString、equals、copy等方法。

在实际中，case class除了在模式匹配时能发挥其强大的威力之外，在进行其它应用时，也显示出了其强大的功能。

