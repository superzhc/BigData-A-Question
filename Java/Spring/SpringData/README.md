<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-22 14:21:26
 * @LastEditTime : 2021-01-22 16:25:01
 * @Copyright 2021 SUPERZHC
-->
# Spring Data

## 查询方法

### 关键字列表

| 关键字             | 示例                                                      | JPQL表达式                                     |
| ------------------ | --------------------------------------------------------- | ---------------------------------------------- |
| And                | findByFirstnameAndLastname                                | `... where firstname=?1 and lastname=?2`       |
| Or                 | findByFirstnameOrLastname                                 | `... where firstname=?1 or lastname=?2`        |
| Is、Equals         | findByFirstname、findByFirstnameIs、findByFirstnameEquals | `... where firstname=?1`                       |
| Between            | findByStartDateBetween                                    | `... where startdate between ?1 and ?2 `       |
| LessThan           | findByAgeLessThan                                         | `... where age < ?1`                           |
| LessThanEqual      | findByAgeLessThanEqual                                    | `... where age <= ?1`                          |
| GreaterThan        | findByAgeGreaterThan                                      | `... where age > ?1`                           |
| GreaterThanEqual   | findByAgeGreaterThanEqual                                 | `... where age >= ?1`                          |
| After              | findByStartDateAfter                                      | `... where startdate > ?1`                     |
| Before             | findByStartDateBefore                                     | `... where startdate < ?1`                     |
| IsNull             | findByAgeIsNull                                           | `... where age is null`                        |
| IsNotNull、NotNull | findByAge(Is)NotNull                                      | `... where age is not null`                    |
| Like               | findByLastnameLike                                        | `... where lastname like ?1`                   |
| NotLike            | findByLastnameNotLike                                     | `... where lastname not like ?1`               |
| StartingWith       | findByLastnameStartingWith                                | `... where lastname like ?1`(参数增加前缀 `%`) |
| EndingWith         | findByLastnameEndingWith                                  | `... where lastname like ?1`(参数增加后缀 `%`) |
| Containing         | findByLastnameContaining                                  | `... where lastname like ?1`(参数被 `%` 包裹)  |
| OrderBy            | `findByLastnameOrderByAgeDesc`                            | `... where lastname=?1 order by age desc`      |
| Not                | findByLastnameNot                                         | `... where lastname != ?1`                     |
| In                 | `findByAgeIn(Collection<Integer> ages)`                   | `... where age in ?1`                          |
| NotIn              | `findByAgeNotIn(Collection<Integer> ages)`                | `... where age not in ?1`                      |
| True               | findByActiveTrue                                          | `... where active=true`                        |
| False              | findByActiveFalse                                         | `... where active=false`                       |
| IgnoreCase         | findByFirstnameIgnoreCase                                 | `... where UPPER(firstname)=UPPER(?1)`         |

注意，除了 find 的前缀之外，查看 PartTree 的源码，还有如下几种前缀：

```java
private static final String QUERY_PATTERN = "find|read|get|query|search|stream";
private static final String COUNT_PATTERN = "count";
private static final String EXISTS_PATTERN = "exists";
private static final String DELETE_PATTERN = "delete|remove";
```

使用的时候要搭配不同的返回结果进行使用。