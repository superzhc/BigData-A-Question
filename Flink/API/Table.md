# Table API 和 SQL

Flink 针对标准的流处理和批处理提供了两种关系型 API：Table API 和 SQL。Table API 允许用户以一种很直观的方式进行 select、filter 和 join 操作；Flink SQL 支持基于 Apache Calcite 实现的标准 SQL。针对批处理和流处理可以提供相同的处理语义和结果。

Flink Table API、SQL 接口和 Flink 的 DataStream API、DataSet API 是紧密联系在一起的。

Table API 和 SQL 是关系型 API，用户可以像操作 MySQL 数据库表一样来操作数据，而不需要通过编写 Java 代码来完成 Flink Function，更不需要手工为 Java 代码调优。

