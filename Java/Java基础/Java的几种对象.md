# Java的几种对象

- [Java的几种对象](#java的几种对象)
  - [PO](#po)
  - [VO](#vo)
  - [DAO](#dao)
  - [BO](#bo)
  - [POJO](#pojo)
  - [DTO](#dto)

## PO

PO：`persistan object` 持久对象，可以看成是与数据库中的表相映射的java对象。

最简单的PO就是对应数据库中某个表中的一条记录，多个记录可以用PO的集合。

PO中应该不包含任何对数据库的操作。

## VO

VO：`value object` 值对象。通常用于业务层之间的数据传递，和PO一样也是仅仅包含数据而已，但应是抽象出的业务对象，可以和表对应，也可以不和表对应，这根据业务的需要。

## DAO

DAO：`data access object` 数据访问对象。是Sun的一个标准J2EE设计模式，此对象用于访问数据库。通常和PO结合使用，DAO中包含了各种数据库的操作方法。

通过它的方法，结合PO对数据库进行相关的操作。

提供数据库的CRUD操作。

## BO

BO：`business object` 业务对象。

## POJO

POJO：`plain ordinary java object` 简单无规则java对象。

## DTO

DTO：`Data Transfer Object` 数据传输对象。DTO是一组需要跨进程或网络边界传输的聚合数据的简单容器。它不应该包含业务逻辑，并将其行为限制为诸如内部一致性检查和基本验证之类的活动。

注：不要因实现这些方法而导致DTO依赖于任何新类。

在设计数据传输对象时，有两种主要选择：
- 使用一般集合
- 使用显式的getter和setter方法创建自定义对象