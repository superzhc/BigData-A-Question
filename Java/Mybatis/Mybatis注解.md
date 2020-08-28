---
title: Mybatis注解
date: 2017-11-27
tags: mybatis
---
# 注解

## 映射语句

### @Insert

使用@Insert注解来定义一个INSERT映射语句：:

```java
@Insert("INSERT INTO STUDENTS(STUD_ID,NAME,EMAIL,ADDR_ID, PHONE) VALUES(#{studId},#{name},#{email},#{address.addrId},#{phone})")
int insertStudent(Student student);
```

使用了@Insert注解的insertMethod()方法将返回insert语句执行后影响的行数。

#### 自动生成主键

使用@Options注解的userGeneratedKeys 和keyProperty属性让数据库产生auto_increment（自增长）列的值，然后将生成的值设置到输入参数对象的属性中。

```java
@Insert("INSERT INTO STUDENTS(NAME,EMAIL,ADDR_ID, PHONE) VALUES(#{name},#{email},#{address.addrId},#{phone})")
@Options(useGeneratedKeys = true, keyProperty = "studId")
int insertStudent(Student student);
```

有一些数据库如Oracle，并不支持AUTO_INCREMENT列属性，它使用序列（SEQUENCE）来产生主键的值。使用@SelectKey注解来为任意SQL语句来指定主键值，作为主键列的值。

```java
@Insert("INSERT INTO STUDENTS(STUD_ID,NAME,EMAIL,ADDR_ID, PHONE) VALUES(#{studId},#{name},#{email},#{address.addrId},#{phone})")
@SelectKey(statement="SELECT STUD_ID_SEQ.NEXTVAL FROM DUAL",keyProperty="studId", resultType=int.class, before=true)
//这里使用了@SelectKey来生成主键值，并且存储到了student对象的studId属性上。由于设置了before=true,该语句将会在执行INSERT语句之前执行。
int insertStudent(Student student);
```

如果使用序列作为触发器来设置主键值，可以在INSERT语句执行后，从sequence_name.currval获取数据库产生的主键值。

```java
@Insert("INSERT INTO STUDENTS(NAME,EMAIL,ADDR_ID, PHONE) VALUES(#{name},#{email},#{address.addrId},#{phone})")
@SelectKey(statement="SELECT STUD_ID_SEQ.CURRVAL FROM DUAL",keyProperty="studId", resultType=int.class, before=false)
int insertStudent(Student student);
```

### @Update

使用@Update注解来定义一个UPDATE映射语句:

```java
@Update("UPDATE STUDENTS SET NAME=#{name}, EMAIL=#{email},PHONE=#{phone} WHERE STUD_ID=#{studId}")
int updateStudent(Student student);
```

使用了@Update的updateStudent()方法将会返回执行了update语句后影响的行数。

### @Delete

使用@Delete  注解来定义一个DELETE映射语句:

```java
@Delete("DELETE FROM STUDENTS WHERE STUD_ID=#{studId}")
int deleteStudent(int studId);
```

使用了@Delete的deleteStudent()方法将会返回执行了update语句后影响的行数。

### @Select

使用@ Select注解来定义一个SELECT映射语句:

```java
@Select("SELECT STUD_ID AS STUDID, NAME, EMAIL, PHONE FROM STUDENTS WHERE STUD_ID=#{studId}")
Student findStudentById(Integer studId);
```

可返回多行结果，需使用List<>。

## 结果映射

将查询结果通过别名或者是@Results注解与JavaBean属性映射起来:

```java
@Select("SELECT * FROM STUDENTS")
@Results(
{
    @Result(id = true, column = "stud_id", property = "studId"),
    @Result(column = "name", property = "name"),
    @Result(column = "email", property = "email"),
    @Result(column = "addr_id", property = "address.addrId")
})
List<Student> findAllStudents();
```

@Result注解和映射器XML配置文件元素\<resultMap\>相对应。可以在mapper.xml文件中定义好resultMap,直接在java程序中引用这个resultMap，例：`com.epoint.superz.studentResult`