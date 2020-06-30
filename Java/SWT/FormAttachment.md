像其它的 Layout 类一样，`FormLayout` 用的 data 类是:`FormData`。`FormData` 用另外一 个类来控制窗口小部件的大小和位置: `FormAttachment`。一个FormData最多用4个FormAttachment,它们分别对应这个小部件的4个面:顶部，底部，左边和右 边。

FormAttachment定义了小部件在 parent composite 或是这个 composite 里其它部件的位置。

FormAttachment 计算位置和大小的方法:

```
y=ax+b
```

在这个等式中，在数学上`y`代表的是纵坐标；`x`是横坐标值；`a`是斜率；`b`是偏移量。

按照FormAttachment的规则，`y`是高；`x`是宽度；`a`是一个相对其它部件的百分率；`b`是偏移量。FormAttachment实例中的每个数据成员分别代表这些值。

以下(表一)是FormAttachment的数据成员表和相应的意思:

表 一:FormAttachment数据成员

|Attribute|Description|
|----------|---------------|
|int alignment|指定是以另外一个部件的哪一边为基准的.可能的值是:SWT.TOP , SWT.CENTER , 和 SWT.BOTTOM.默认是以最近的一边为基准.|
|Control control|指定 FormAttachment是以哪个部件为参照物.|
|int denominator|指定分母.默认值为100|
|int numerator|指定分子|
|int offset|指定离 composite边界或部件的边界的偏移量.单位是象素.|

FormAttachment 拥有5个构造函数,没有一个是空的,它们如下表(表二)所示:

表二:FormAttachment 的 构造函数:

|Constructor|Description|
|---------------|-------------------------|
|FormAttachment(Control control)|以另外的某个部件为参照物.|
|FormAttachment(Control control, int offset)|以另外的某个部件为参照物,加上偏移量.|
|FormAttachment(Control control, int offset, int alignment)|以另外的某个部件为参照物,加上偏移量和alignment|
|FormAttachment(int numerator)|设定分子.分母为100,没有偏移量|
|FormAttachment(int numerator, int offset)|指定分子和偏移量和 100的分母|
|FormAttachment(int numerator, int denominator, int offset)|特定的分子,分母,偏移 量|

FormData最多包含4个FormAttachment 实例,每个对应了与它联系的部件的一边.另外,FormData也可以指定宽和高.表四列出了FormData的数据成员:

|Attribute|Description|
|---|----|
|FormAttachment bottom|这个FormAttachment 用 来指定部件的底部位置|
|int height|这个部件的高度.单位为象素.|
|FormAttachment left|这个FormAttachment 用来指定部件的左部位置|
|FormAttachment right|这个FormAttachment 用来指定部件的右部位置|
|FormAttachment top|这个FormAttachment 用来指定部件的顶部位置|
|int width|这个部件的宽度.单位为 象素.|

当生成一个FormData对象,可以自己传给它宽和高的值.如果没有指定FormAttachment 对 象,部件会自动以parent composite的上边界和左边为起始边界.如果这样定义了多个部件,它们会都在composite的左上角.

FormLayout 有 两个数据成员 `marginHeight` 和 `marginWidth`,用来以象素为单位来指定大小.它们用来指定包围composite里所有内容的空白. marginHeight对应的是顶部和底部的空白大小,marginWidth对应的是左边和右边的空白大小.空白的默认值是0.

