# break

## 基础

break不仅能够在switch...case...中使用外，还可以用于跳出循环

## 跳出多层循环

```java
for(int i=0;i<3;i++){
    for(int j=0;j<3;j++){
        if(i==1&&j==1){
            break;
        }
        System.out.println("i="+i+",j="+j);
    }
}

// 结果：
// i=0,j=0
// i=0,j=1
// i=0,j=2
// i=1,j=0
// i=2,j=0
// i=2,j=1
// i=2,j=2
```

由上可知，break循环只跳出了内层的for循环。

若需要跳出最外层的循环，可以在循环前边加标号来实现，如下：

```java
OUT:for(int i=0;i<3;i++){
    for(int j=0;j<3;j++){
        if(i==1&&j==1){
            break OUT;
        }
        System.out.println("i="+i+",j="+j);
    }
}

// 结果：
// i=0,j=0
// i=0,j=1
// i=0,j=2
// i=1,j=0
```

### 说明

1. 标号紧贴循环语句，并且处于循环语句的前边；
2. 通过标号能跳出任意层数的循环；
3. break和continue都可以使用标号来灵活控制循环语句；
4. 反编译.class文件时，能经常见到"break label;"这样的标号使用。

