<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-25 15:39:18
 * @LastEditTime : 2021-01-25 18:04:45
 * @Copyright 2021 SUPERZHC
-->
# Spring Data JPA

> **JPA**，即 Java Persistence API ，中文意为 Java 持久层 API，是 Sun 公司提出的一套标准，用于将运行期对象持久化存储到数据库，具体实现的产品有： Hiberate、Eclipselink、Toplink、Spring Data JPA 等。

## 使用

**引入依赖**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

**配置文件**

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/testjpa?useUnicode=true&characterEncoding=utf8&useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
# 下面这个配置项表示是否需要自动创建数据库表，可选值有：none 不自动创建; create 每次项目启动时自动创建; create-drop 项目启动时创建表，项目关闭时删除; update 有需要时更新数据库; validate 验证数据库，但不更新; 
spring.jpa.hibernate.ddl-auto=create
# 打印出sql语句，可用于开发调试
spring.jpa.show-sql=true
```

## JPQL 查询

> JPQL 全称 Java Persistence Query Language，即 Java 持久化查询语言，与原生 SQL 语句类似，完全面向对象，通过实体名与属性名访问，不是表名和表的字段名，不支持 INSERT 操作。
> 
> **注意：这里用的是实体名，默认为类名，可以在 @Entity 注解中修改实体名，如 `@Entity(name="tb_student")` 查询时要使用 tb_student**

### JPQL 的 SELECT

**语法**

```sql
SELECT ... FROM ... [WHERE ...] [GROUP BY ... [HAVING ...]] [ORDER BY ...]
```

在 Spring Data JPA 中使用时，需要在 Repository 接口中的方法上加上 `@Query` 注解，在注解中申明查询语句。

参数绑定：

- 按参数位置传参，使用 `?X` ，X 为方法中参数位置，从 1 开始计算，参数的个数与顺序要与方法参数保持一致；
- 使用参数名传参，使用 `:paramName` 的方式，这种方式不用管参数顺序，paramName 为参数名，参数名需要使用 `@Param("paramName")` 注解指定的名称，而不是方法的参数名称；
- 使用 SPEL 的取值表达式进行参数绑定。

示例：

```java
// 按参数位置绑定
@Query("select student from Student as student where student.birthday between ?1 and ?2")
List<Student> findStudentByBirthdayBetween(String startTime,String endTime);

// 按参数名绑定
@Query("select student from Student student where student.age between :startAge and :endAge")
List<Student> findStudentByAgeBetween(@Param("startAge") int startAge, @Param("endAge") int endAge);

// JPQL查询语句中可以不写 select ，从 from 开始写就可以
@Query("from Student s where s.name = ?1 and s.age = ?2")
Student findStudentByNameAndAge(String name, int age);	

// 模糊查询时要在参数前后添加 “%”
@Query("select s from Student s where s.name like %?1%")
List<Student> findStudentLikeName(String likeName);

// 参数为集合
@Query("select s from Student s where s.id in ?1")
List<Student> findByStudentIds(List<Long> idList);

// 传入Bean进行查询（SPEL表达式查询）
@Query(value = "from Student s where s.name=:#{#std.name} and s.age=:#{#std.age}")
Student findByNameAndAge(@Param("std")Student student);

// 分页
@Query("from Student s")
Page<Student> findAllStudent(Pageable pageable);

// 带查询条件的分页
@Query("select s from Student s where s.age >?1")
Page<Student> findStudentAgeGreaterThan(int age, Pageable pageable);
```

注意：语句中使用的都是实体名和属性名，不是表名和表中字段名。

查询结果分页，可以在查询方法中加入 `Pageable pageable` 参数，即可对查询结果进行分页。

### JPQL 的 UPDATE

**语法**

```sql
UPDATE ... SET ... [WHERE ...]
```

在使用时需要添加 `@Modified` 和 `@Query` 注解，在调用的 Service 方法上要添加 `@Transaction` 注解，否则会报缺少事务控制的错。

**示例**

```java
@Modified
@Query("update Student s set s.age = ?1 where name = ?2")
long modifyStudentAgeByName(int age, String name);
```

### JPQL 的 DELETE

**语法**

```sql
DELETE FROM ... [WHERE ...]
```

与 UPDATE 的使用方式相同，也需要添加 `@Modified` 和 `@Query` 注解，在调用的 Service 方法上要添加 `@Transaction` 注解，否则会报错。

**示例**

```java
@Modified
@Query("delete from Student s where s.name = ?1")
void deleteStudentByName(String name);
```

## JPA 注解

### `@Entity`、`@Table`

在自动建表时， 指定表名：

- 给 `@Entity` 注解加上 name 参数，改为 `@Entity(name = "tb_student")`
- 或添加 `@Table` 注解，并添加 name 参数：`@Table(name = "tb_student")`

`@Entity` 注解表明这个类是需要 ORM 映射的，只有 name 一个参数，而 `@Table` 注解中可以修改一些映射的规则。

`@Table` 注解参数：

| 名称              | 说明                                                | 默认值             |
| ----------------- | --------------------------------------------------- | ------------------ |
| name              | (可选) 表名                                         | 当前类名           |
| catalog           | (可选) 数据库名                                     | 配置中指定的数据库 |
| schema            | (可选) 查询数据时使用的用户名                       | 配置中指定的用户名 |
| uniqueConstraints | (可选) 创建单个或联合唯一约束，可以在建表时添加索引 | `{}`               |

**示例**

```java
@Table(name = "tb_student",catalog = "test",schema = "testjpa",uniqueConstraints = {@UniqueConstraints(columnNames = {"name","age"})})
// 将在 test 数据库中创建名为 tb_student 的表，查询时使用 testjpa 的用户查询，并给这张表添加 name 与 age 的唯一约束
```

### `@Id`

主键字段添加上该标识

### `@GeneratedValue`

设置主键的生成策略，主键的生成策略的可选项如下所示：

| 名称                    | 说明                                                                           |
| ----------------------- | ------------------------------------------------------------------------------ |
| GenerationType.AUTO     | (默认值)，主键由 jpa 自动生成                                                  |
| GenerationType.IDENTITY | 使用数据库自增值作为主键值，适用于HSQL、SQL Server、MySQL、DB2、Derby 等数据库 |
| GenerationType.SEQUENCE | 使用数据库序列号作为主键值，适用于Oracle、PostgreSQL 等数据库                  |
| GenerationType.TABLE    | 使用数据中一张表中某个字段作为主键                                             |

### `@Column`

`@Column` 注解参数说明：

| 名称             | 说明                                                                                                                               | 默认值                 |
| ---------------- | ---------------------------------------------------------------------------------------------------------------------------------- | ---------------------- |
| name             | (可选) 建表时使用的字段名                                                                                                          | 默认为实体类中的字段名 |
| unique           | (可选) 该字段是否唯一                                                                                                              | false                  |
| nullable         | (可选) 是否可为空                                                                                                                  | true                   |
| insertable       | (可选) 使用的 insert 语句中包含该字段时，该字段是否要插入值                                                                        | true                   |
| updatable        | (可选) 使用的 update 语句中包含该字段时，该字段是否要更新，用于表中的只读字段                                                      | true                   |
| columnDefinition | (可选) 字段描述，在用来定义建表时数据库字段属性，如指定字段类型与注释: `@Column(columnDefinition = "smallint COMMENT '学生年龄'")` | `""`                   |
| table            | (可选) 包含当前字段的表名。默认值为主表的表名。                                                                                    | `""`                   |
| length           | (可选) 最大长度，只有字段类型是字符串类型时生效                                                                                    | 255                    |
| precision        | (可选) 小数的精度，小数数据的长度，仅小数数据类型字段可用                                                                          | 0                      |
| scale            | (可选) 小数数据的小数位数，仅小数数据类型字段可用                                                                                  | 0                      |