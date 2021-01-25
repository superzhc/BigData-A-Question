<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-25 13:59:05
 * @LastEditTime : 2021-01-25 16:01:41
 * @Copyright 2021 SUPERZHC
-->
# FAQ

## Spring Data JPA 自动创建 MySQL 表使用的引擎是 `MyISAM`，若需要使用 `InnoDB` 引擎，可进行如下设置

```properties
# 不加这句则默认为 MyISAM 引擎
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
```

## Spring Data JPA 实体类字段与数据库关键字冲突

一般情况下实体类字段不建议取会与数据库关键字相同的名字，但总会有些特殊情况
 
比如下面这个情况，在使用 MySQL 的时候会出现错误（但是使用 h2 的 MySQL 模式不会有问题）

```java
@Entity
public class Category {

    @GeneratedValue
    @Id
    private int id;

    @Column(unique = true, nullable = false, length = 64)
    private String name;

    @Column
    private String desc;

    @Column(nullable = false)
    private Date created;

    @Column(nullable = false)
    privte Date modified;
}
```

报错信息如下：

```
Caused by: org.hibernate.tool.schema.spi.SchemaManagementException: Unable to execute schema management to JDBC target [create table category (id integer not null auto_increment, created datetime not null, desc varchar(255), modified datetime not null, name varchar(64) not null, primary key (id))]
```

**解决办法**：

> 把对应的 `@Column` 改为 `@Column(name="[desc]")` 或者自定义一个其他名字。