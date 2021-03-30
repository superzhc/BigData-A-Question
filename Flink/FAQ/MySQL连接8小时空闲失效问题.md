# MySQL 连接 8 小时空闲失效问题

**报错信息**

```
2021-03-27 11:00:01,164 ERROR org.apache.flink.connector.jdbc.internal.JdbcBatchingOutputFormat [] - JDBC executeBatch error, retry times = 0
com.mysql.cj.jdbc.exceptions.CommunicationsException: The last packet successfully received from the server was 71,975,432 milliseconds ago.  The last packet sent successfully to the server was 71,975,432 milliseconds ago. is longer than the server configured value of 'wait_timeout'. You should consider either expiring and/or testing connection validity before use in your application, increasing the server configured values for client timeouts, or using the Connector/J connection property 'autoReconnect=true' to avoid this problem.
	at com.mysql.cj.jdbc.exceptions.SQLError.createCommunicationsException(SQLError.java:174)
    at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:64)
    at com.mysql.cj.jdbc.ConnectionImpl.isReadOnly(ConnectionImpl.java:1429)
    at com.mysql.cj.jdbc.ConnectionImpl.isReadOnly(ConnectionImpl.java:1408)
    at com.mysql.cj.jdbc.ClientPreparedStatement.executeBatchInternal(ClientPreparedStatement.java:424)
    at com.mysql.cj.jdbc.StatementImpl.executeBatch(StatementImpl.java:814)
    at org.apache.flink.connector.jdbc.internal.executor.SimpleBatchStatementExecutor.executeBatch(SimpleBatchStatementExecutor.java:73)
    at org.apache.flink.connector.jdbc.internal.JdbcBatchingOutputFormat.attemptFlush(JdbcBatchingOutputFormat.java:216)
    ...
```

## 解决方案

TODO