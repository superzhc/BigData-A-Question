<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2020-12-16 14:56:48
 * @LastEditTime : 2020-12-18 09:53:24
 * @Copyright 2020 SUPERZHC
-->
# 存储过程和函数

存储过程是数据库服务器上一组预先编译好的 SQL 语句的集合，作为一个对象存储在数据库中，可以被应用程序作为一个整体来调用。在调用过程中，存储过程可以从调用者哪里接收输入参数，执行后再通过输出参数向调用者返回处理结果。

在 MySQL 操作过程中，数据库开发人员可以根据实际需要，把数据库操作过程中频繁使用的一些 MySQL 代码封装在一个存储过程中，需要执行这些 MySQL 代码时则对存储过程进行调用，从而提高程序代码的复用性。

在进行数据库开发的过程中，数据库开发人员经常把一些需要反复执行的代码放在一个独立的语句块中。这些能实现一定具体功能、独立放置的语句块，称之为 **过程**（Procedure）。MySQL 的存储过程（Stored Procedure），就是为了完成某一特定功能，把一组 SQL 语句集合经过编译后作为一个整体存储在数据库中。用户需要的时候，可以通过存储过程名来调用存储过程。

存储过程增强了 SQL 语句编程的灵活性，提高了数据库应用程序的运行效率，增强了代码的复用性和安全性，同时也使程序代码维护起来更加容易，从而大大减少数据库开发人员的工作量，缩短整个数据库程序的开发时间。

- 存储函数（stored function）：根据调用者提供的参数进行处理，最终返回调用者一个值作为函数处理结果。
- 存储过程（stored procedure）：一般用来完成运算，并不返回结果。需要的时候可以把处理结果以输出参数的形式传递给调用者。

## 创建存储过程和函数

创建存储过程和函数是指将经常使用的一组 SQL 语句组合在一起，并将这些 SQL 语句当作一个整体存储在 MySQL 服务器中。存储程序可以分为存储过程和函数，MySQL 中创建存储过程和函数使用的语句分别是：`CREATE PROCEDURE` 和 `CREATE FUNCTION`。使用 CALL 语句来调用存储过程，只能用输出变量返回值。函数可以从语句外调用（通过引用函数名），也能返回标量值。存储过程也可以调用其他存储过程。

### 创建存储过程

在 MySQL 中创建存储过程通过 SQL 语句 `CREATE PROCEDURE` 来实现，其语法形式如下：

```sql
CREATE PROCEDURE procedure_name([proc_param[,...]])[characteristic...] routine_body
```

在上述语句中，参数 procedure_name 表示所要创建的存储过程的名字，参数 proc_param 表示存储过程的参数，参数 characteristic 表示存储过程的特性，参数 routine_body 表示存储过程的 SQL 语句代码，可以用 `BEGIN…END` 来标志 SQL 语句的开始和结束。

> 在具体创建存储过程时，存储过程名不能与已经存在的存储过程名重名。

**proc_param**

proc_param 中每个参数的语法形式如下：

```sql
[IN|OUT|INOUT] param_name type
```

在上述语句中，每个参数由 3 部分组成，分别为输入/输出类型、参数名和参数类型。

- 输入/输出类型有3种：
  - IN 表示输入类型；
  - OUT 表示输出类型；
  - INOUT 表示输入/输出类型。
- param_name 表示参数名。
- type 表示参数类型，可以是 MySQL 所支持的任意一个数据类型。

**charateristic**

参数 charateristic 指定存储过程的特性，有以下取值：

- LANGUAGE SQL：说明 routine_body 部分是由 SQL 语句组成的，当前系统支持的语言为 SQL，SQL 是 LANGUAGE 特性的唯一值。
- `[NOT] DETERMINISTIC`：指明存储过程执行的结果是否正确。如果没有指定任意一个值，就默认为 `NOT DETERMINISTIC`。
  - DETERMINISTIC 表示结果是确定的。每次执行存储过程时，相同的输入会得到相同的输出。
  - NOT DETERMINISTIC表示结果是不确定的，相同的输入可能得到不同的输出。
- `{CONTAINS SQL | NOSQL | READS SQL DATA | MODIFIES SQL DATA}`：指明子程序使用 SQL 语句的限制。默认情况下，系统会指定为 `CONTAINS SQL`。
  - `CONTAINS SQL` 表名子程序包含 SQL 语句，但是不包含读写数据的语句；
  - `NO SQL` 表明子程序不包含 SQL 语句；
  - `READS SQL DATA` 说明子程序包含读数据的语句；
  - `MODIFIES SQL DATA` 表明子程序包含写数据的语句。
- `SQL SECURITY{DEFINER | INVOKER}`：指明谁有权限来执行。默认情况下，系统指定为 `DEFINER`。
  - DEFINER 表示只有定义者才能执行。
  - INVOKER 表示拥有权限的调用者可以执行。
- `COMMENT 'string'`：注释信息，可以用来描述存储过程或函数。

> MySQL 中默认的语句结束符为分号（`;`）。存储过程中的 SQL 语句需要分号来结束。为了避免冲突，首先用 `DELIMITER $$` 将 MySQL 的结束符设置为 `$$`，然后用 `DELIMITER ;` 来将结束符恢复成分号。

### 创建存储函数

在MySQL中，创建函数通过 SQL 语句 `CREATE FUNCTION` 来实现，其语法形式如下：

```sql
CREATE FUNCTION func_name([func_param[,...]]) [characteristic...] routine_body
```

在上述语句中，参数 func_name 表示所要创建的函数的名字；参数 func_param 表示函数的参数；参数 characteristic 表示函数的特性，该参数的取值与存储过程中的取值相同；参数 routine_body 表示函数的 SQL 语句代码，可以用 `BEGIN…END` 来表示 SQL 语句的开始和结束。

> 在具体创建函数时，函数名不能与已经存在的函数名重名

**func_param**

func_param 中的每个参数的语法形式如下：

```sql
param_name type
```

在上述语句中，每个参数由两部分组成，分别为参数名和参数类型。

- param_name 表示参数名；
- type 表示参数类型，可以是 MySQL 所支持的任意一种数据类型。

### 变量的使用

[变量的使用](变量.md ':include')

### 定义条件和处理程序

定义条件和处理程序是事先定义程序执行过程中可能遇到的问题，并且可以在处理程序中定义解决这些问题的办法。这种方式可以提前预测可能出现的问题，并提出解决办法。这样可以增强程序处理问题的能力，避免程序异常停止。MySQL 中都是通过关键字 DECLARE 来定义条件和处理程序的。

#### 定义条件

MySQL 中可以使用 DECLARE 关键字来定义条件，其基本语法如下：

```sql
DECLARE condition_name CONDITION FOR condition_value
-- condition_value:
-- SQLSTATE[VALUE] sqlstate_value|mysql_error_code
```

其中，参数 condition_name 表示条件的名称；参数 condition_value 表示条件的类型；参数 sqlstate_value 和参数 mysql_error_code 都可以表示 MySQL 的错误。

**示例**

```sql
-- 方法一：使用 sqlstate_value
DECLARE can_not_find CONDITION FOR SQLSTATE '42S02';
-- 方法二：使用 mysql_error_code
DECLARE can_not_find CONDITION FOR 1146;
```

#### 定义处理程序

MySQL 中可以使用 DECLARE 关键字来定义处理程序，其基本语法如下：

```sql
DECLARE handler_type HANDLER FOR condition_value[,...] proc_statement
-- handler_type:
-- CONTINUE|EXIT|UNDO 
-- condition_value:
-- SQLSTATE[VALUE] sqlstate_value|condition_name|SQLWARNING|NOT FOUND|SQLEXCEPTION|mysql_error_code
```

- handler_type 指明错误的处理方式，该参数有3个取值，分别是 CONTINUE、EXIT 和 UNDO。
  - CONTINUE 表示遇到错误不进行处理，继续向下执行；
  - EXIT 表示遇到错误后马上退出；
  - UNDO 表示遇到错误后撤回之前的操作，MySQL 中暂时还不支持这种处理方式。
- condition_value 表示错误类型，可以有以下取值。
  - `SQLSTATE[VALUE] sqlstate_value`：包含5个字符的字符串错误值；
  - continue_name：表示 DECLARE CONDITION 定义的错误条件名称。
  - SQLWARNING：匹配所有以 01 开头的 SQLSTATE 错误代码。
  - NOT FOUND：匹配所有以 02 开头的 SQLSTATE 错误代码。
  - SQLEXCEPTION：匹配所有没有被 SQLWARNING 或 NOT FOUND 捕获的 SQLSTATE 错误代码。
  - mysql_error_code：匹配数值类型错误代码。
- proc_statement 为程序语句段，表示在遇到定义的错误时需要执行的存储过程或函数。

**示例**

```sql
-- 捕获 sqlstate_value
DECLARE CONTINUE HANDLER FOR SQLSTATE '42S02' SET @info='NOT FOUND';
-- 使用 mysql_error_code
DECLARE CONTINUE HANDLER FOR 1146 SET @info='NOT FOUND';
-- 先定义条件，然后调用
DECLARE not_found CONDITION FOR 1146;
DECLARE CONTINUE HANDLER FOR not_found SET @info='NOT FOUND';
-- 使用 SQLWARNING
DECLARE EXIT HANDLER FOR SQLWARNING SET @info='ERROR';
-- 使用 NOT FOUND
DECLARE EXIT HANDLER FOR NOT FOUND SET @info='NOT FOUND';
-- 使用 SQLEXCEPTION
DECLARE EXIT HANDLER FOR SQLEXCEPTION SET @info='ERROR';
```

### 游标的使用

[游标的使用](游标.md ':include')

### 流程控制的使用

存储过程和函数中可以使用流程控制来控制语句的执行。MySQL 中可以使用 IF 语句、CASE 语句、LOOP 语句、LEAVE 语句、ITERATE 语句、REPEAT 语句和 WHILE 语句来进行流程控制。

#### IF 语句

[IF 语句](IF语句.md ':include')

#### CASE 语句

[CASE 语句](CASE语句.md ':include')

#### LOOP 语句

[LOOP 语句](LOOP语句.md ':include')

#### LEAVE 语句

[LEAVE 语句](LEAVE语句.md ':include')

#### ITERATE 语句

[ITERATE 语句](ITERATE语句.md ':include')

#### REPEAT 语句

[REPEAT 语句](REPEAT语句.md ':include')

#### WHILE 语句

[WHILE 语句](WHILE语句.md ':include')

## 其他

- [动态 SQL](数据库/SQL编程/存储过程和函数/动态SQL.md)

## 调用存储过程和函数

存储过程和函数都是存储在服务器端的 SQL 语句的集合。要使用这些已经定义好的存储过程和存储函数，就必须通过调用的方式来实现。存储过程是通过 CALL 语句来调用的。而存储函数的使用方法与 MySQL 内部函数的使用方法是一样的。执行存储过程和存储函数需要拥有 EXECUTE 权限。EXECUTE 权限的信息存储在 `information_schema` 数据库下的 USER_PRIVILEGES 表中。

### 调用存储过程

MySQL 中使用 CALL 语句来调用存储过程。调用存储过程后，数据库系统将执行存储过程中的语句。然后，将结果返回给输出值。CALL 语句的基本语句形式如下：

```sql
CALL proc_name([parameter[,...]])
```

其中，proc_name 是存储过程的名称；parameter 是指存储过程的参数。

### 调用存储函数

在 MySQL 中，存储函数的使用方法与 MySQL 内部函数的使用方法是一样的。换言之，用户自己定义的存储函数与 MySQL 内部函数是一个性质的。区别在于，存储函数是用户自己定义的，而内部函数是 MySQL 的开发者定义的。

## 查看存储过程和函数

存储过程和函数创建以后，用户可以通过 `SHOW STATUS` 语句来查看存储过程和函数的状态，也可以通过 `SHOW CREATE` 语句来查看存储过程和函数的定义。用户也可以通过查询 information_schema 数据库下的 Routines 表来查看存储过程和函数的信息。

### 使用 `SHOW STATUS` 语句查看存储过程和函数的状态

MySQL 中可以通过 `SHOW STATUS` 语句查看存储过程和函数的状态。其基本语法形式如下：

```sql
SHOW {PROCEDURE|FUNCTION} STATUS {LIKE 'pattern'}
```

其中，参数 PROCEDURE 表示查询存储过程；参数 FUNCTION 表示查询存储函数；参数 `LIKE 'pattern'` 用来匹配存储过程或函数的名称。

### 使用 `SHOW CREATE` 语句查看存储过程和函数的定义

MySQL 中可以通过 `SHOW CREATE` 语句查看存储过程和函数的状态，语法形式如下：

```sql
SHOW CREATE {PROCEDURE|FUNCTION} proc_name
```

其中，参数 PROCEDURE 表示查询存储过程；参数 FUNCTION 表示查询存储函数；参数 proc_name 表示存储过程或函数的名称。

### 从 `information_schema.Routine` 表中查看存储过程和函数的信息

存储过程和函数的信息存储在 information_schema 数据库下的 Routines 表中，可以通过查询该表的记录来查询存储过程和函数的信息。其基本语法形式如下：

```sql
SELECT * FROM information_schema.ROUTINES WHERE ROUTINE_NAME ='proc_name';
```

其中，字段 ROUTINE_NAME 中存储的是存储过程和函数的名称；参数 proc_name 表示存储过程或函数的名称。

## 修改存储过程和函数

修改存储过程和函数是指修改已经定义好的存储过程和函数。MySQL 中通过 `ALTER PROCEDURE` 语句来修改存储过程。通过 `ALTER FUNCTION` 语句来修改存储函数。

MySQL 中修改存储过程和函数的语句的语法形式如下：

```sql
ALTER {PROCEDURE|FUNCTION} proc_name [characteristic...];
characteristic:
	{CONTAINS SQL|NO SQL|READS SQL DATA|MODIFIES SQL DATA}
|SQL SECURITY {DEFINDER|INVOKER}
|COMMENT 'string'
```

- proc_name 表示存储过程或函数的名称；
- characteristic 指定存储函数的特性。
  - `CONTAINS SQL`表示子程序包含 SQL 语句，但不包含读或写数据的语句；
  - `NO SQL` 表示子程序中不包含 SQL 语句；
  - `READS SQL DATA` 表示子程序中包含读数据的语句；
  - `MODIFIES SQL DATA` 表示子程序中包含写数据的语句。
- `SQLSECURITY{DEFINER|INVOKER}` 指明谁有权限来执行。
  - DEFINDER 表示只有定义者自己才能够执行；
  - INVOKER 表示调用者可以执行。
- `COMMENT 'string'` 是注释信息。

## 删除存储过程和函数

在 MySQL 中可以通过两种方式来删除存储过程和函数，分别为通过 DROP 语句和通过工具。

**删除存储过程**

在 MySQL 中删除存储过程通过 SQL 语句 DROP 来实现：

```sql
DROP PROCEDURE proc_name;
```

在上述语句中，关键字 `DROP PROCEDURE` 用来表示实现删除存储过程，参数 proc_name 表示所要删除的存储过程的名称。

**删除存储函数**

在 MySQL 中删除函数通过 SQL 语句 `DROP FUNCTION` 来实现，其语法形式如下：

```sql
DROP FUNCTION func_name;
```

关键字 `DROP FUNCTION` 用来实现删除函数，参数 func_name 表示要删除的函数名。