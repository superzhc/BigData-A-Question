# 引入 `jol-core` 包查看 Hotspot 信息

## 引入 `jol-core`

```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.9</version>
</dependency>
```

## 查看当前 VM 的信息

```java
// VM.current().details()

System.out.println(VM.current().details());

// 返回信息

# Running 64-bit HotSpot VM.
# Using compressed oop with 0-bit shift.
# Using compressed klass with 3-bit shift.
# Objects are 8 bytes aligned.
# Field sizes by type: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]
# Array element sizes: 4, 1, 1, 2, 2, 4, 4, 8, 8 [bytes]
```

## 查看对象的内存结构

```java
public class ObjectLayout
{
    public static void main(String[] args) {
        ObjectDemo obj = new ObjectDemo();
        System.out.println(ClassLayout.parseClass(ObjectDemo.class).toPrintable());
    }

    private static class ObjectDemo
    {
        byte a;

        boolean b;

        char c;

        short s;

        int i;

        float f;

        double d;

        long l;

        Object o;
    }
}
```

**返回结果**

注：`//` 后都是标注的内容，非返回内容

```
com.epoint.superz.thread.ObjectLayout$ObjectDemo object internals:
 OFFSET  SIZE               TYPE DESCRIPTION                               VALUE
      0    12                    (object header)                           N/A
     12     4                int ObjectDemo.i                              N/A //因对象头占用的空间是12 bytes，需要填充到 16 bytes，添加 padding 会浪费资源，直接使用 4 bytes 类型来优先添加
     16     8             double ObjectDemo.d                              N/A // 默认尽可能优先分配大的类型，这里优先分配了 int 看上面的原因
     24     8               long ObjectDemo.l                              N/A
     32     4              float ObjectDemo.f                              N/A
     36     2               char ObjectDemo.c                              N/A
     38     2              short ObjectDemo.s                              N/A
     40     1               byte ObjectDemo.a                              N/A
     41     1            boolean ObjectDemo.b                              N/A
     42     2                    (alignment/padding gap)                  
     44     4   java.lang.Object ObjectDemo.o                              N/A
Instance size: 48 bytes
Space losses: 2 bytes internal + 0 bytes external = 2 bytes total
```

> 由上面的结果就可以看出存在重排序的现象，因为跟我定义的顺序存在不同