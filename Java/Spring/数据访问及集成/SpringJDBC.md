# Spring JDBC

## JDBC 框架

在使用原生的 JDBC 来操作数据库时，需要写很多重复的代码，如打开、关闭数据库连接，处理异常等。但 Spring JDBC 框架负责所有的底层细节，从开始打开链接，准备和执行 SQL 语句，处理异常，处理事务，到最后关闭连接。所以当从数据库中获取数据时，用户需要做的事定义连接参数，指定要执行的 SQL 语句，每次迭代完成所需的工作。

### `JdbcTemplate`

`JdbcTemplate` 类执行 SQL 查询、更新语句和存储过程调用，执行迭代结果集和提取返回参数值。它也捕获 JDBC 异常并转换它们到 `org.springframework.dao`包中定义的通用类。

`JdbcTemplate` 类的实例是线程安全的。所以可以配置 JdbcTemplate 为单例，然后将这个共享的引用安全地注入到多个 DAO 中。

使用 JdbcTemplate 类最常见的做法是在 Spring 配置文件中配置数据源，然后共享数据源 bean 依赖注入到 DAO 类中。

### `SimpleJdbcCall`

`SimpleJdbcCall` 类可以被用于调用一个包含 IN 和 OUT 参数的存储过程。可以在处理任何一个 RDBMS 时使用这个类。

## 事务管理

一个数据库事务是一个被视为单一的工作单元的操作序列。这些操作要么完整地执行，要么完全不执行。事务管理是一个重要组成部分，RDBMS 面向企业应用程序，以确保数据完整性和一致性。事务的概念可以描述为具有以下四个关键属性（ACID）：
- **原子性**：事务应该当作一个单独单元的操作，这意味着整个序列操作要么是成功的，要么是失败的
- **一致性**：这表示数据库的引用完整性的一致性，表中唯一的主键等【解释感觉有问题】
- **隔离性**：可能同时处理很多相同的数据集的事务，每个事务应该与其他事务隔离，以防止数据损坏
- **持久性**：一个事务一旦完成全部操作后，这个事务的结果必须是永久性的，不能因系统故障而从数据库中删除

一个真正的 RDBMS 数据库系统将为每个事务保证所有的四个属性。使用 SQL 发布到数据库中的事务的简单视图如下：
- 使用 `begin transaction` 命令开始事务
- 使用 SQL 语句执行各种删除、更新或插入操作
- 如果所有的操作都成功，则执行提交操作，否则回滚所有操作

Spring 框架在不同的底层事务管理 APIs 的顶部提供了一个抽象层。Spring 的事务支持旨在通过添加事务能力到 POJOs 来提供给 EJB 事务一个选择方案。

Spring 支持**编程式**和**声明式**事务管理。

EJB 需要一个应用程序服务器，但 Spring 事务管理可以在不需要应用程序服务器的情况下实现。

- **局部事务** vs **全局事务**

局部事务时特定于一个单一的事务资源，如一个 JDBC 连接，而全局事务可以跨多个事务资源食物，如在一个分布式系统中的事务

局部事务管理在一个集中的计算环境中是有用的，而计算环境中应用程序组件和资源位于一个单位点，而事务管理只涉及到一个运行在一个单一机器中的本地数据管理器。局部事务更容易实现。

全局事务管理需要在分布式计算环境中，所有的资源都分布在多个系统中。在这种情况下事务管理需要同时在局部和全局范围内进行。分布式或全局事务跨多个系统执行，它的执行需要全局事务管理系统和所有相关系统的局部数据管理人员之间的协调。

- **编程式** vs **声明式**

Spring 支持两种类型的事务管理：
- 编程式事务管理：编码过程下管理事务，灵活性强，但却难维护
- 声明式事务管理：从业务代码中分离事务管理，通过使用 **注解** 或 **XML** 配置来管理事务

声明式事务管理比编程式事务管理更可取，尽管它不如编程式事务管理灵活，但它允许你通过代码控制事务。作为一种横切关注点，声明式事务管理可以使用 AOP 方法进行模块化。Spring 支持使用 Spring AOP 框架的声明式事务管理

- **Spring 事务抽象**

Spring 事务抽象的关键是由 `org.springframework.transaction.PlatformTransactionManager` 接口定义，如下所示：

```java
public interface PlatformTransactionManager {
   TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;
   void commit(TransactionStatus status) throws TransactionException;
   void rollback(TransactionStatus status) throws TransactionException;
}
```

| 方法                                                                 | 描述                                                        |
|----------------------------------------------------------------------|-----------------------------------------------------------|
| `TransactionStatus getTransaction(TransactionDefinition definition)` | 根据指定的传播行为，该方法返回当前活动事务或创建一个新的事务 |
| `void commit(TransactionStatus status)`                              | 该方法提交给定的事务和关于它的状态                          |
| `void rollback(TransactionStatus status)`                            | 该方法执行一个给定事务的回滚                                |

`TransactionDefinition` 是在 Spring 中事务支持的核心接口，它的定义如下：

```java
public interface TransactionDefinition {
   int getPropagationBehavior();
   int getIsolationLevel();
   String getName();
   int getTimeout();
   boolean isReadOnly();
}
```