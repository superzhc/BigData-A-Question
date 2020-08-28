# StatementHandler

## StatementHandler 接口

```java
/**
 * @author Clinton Begin
 */
public interface StatementHandler {

    Statement prepare(Connection connection)
        throws SQLException;

    void parameterize(Statement statement)
        throws SQLException;

    void batch(Statement statement)
        throws SQLException;

    int update(Statement statement)
        throws SQLException;

    <E> List<E> query(Statement statement, ResultHandler resultHandler)
        throws SQLException;

    BoundSql getBoundSql();

    ParameterHandler getParameterHandler();

}
```

## BaseStatementHandler

```java
/**
 * @author Clinton Begin
 */
public abstract class BaseStatementHandler implements StatementHandler {

    protected final Configuration configuration;
    protected final ObjectFactory objectFactory;
    protected final TypeHandlerRegistry typeHandlerRegistry;
    protected final ResultSetHandler resultSetHandler;
    protected final ParameterHandler parameterHandler;

    protected final Executor executor;
    protected final MappedStatement mappedStatement;
    protected final RowBounds rowBounds;

    protected BoundSql boundSql;

    protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();

        if (boundSql == null) { // issue #435, get the key before calculating the statement
            generateKeys(parameterObject);
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }

        this.boundSql = boundSql;

        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }

    public Statement prepare(Connection connection) throws SQLException {
        ErrorContext.instance().sql(boundSql.getSql());
        Statement statement = null;
        try {
            statement = instantiateStatement(connection);
            setStatementTimeout(statement);
            setFetchSize(statement);
            return statement;
        } catch (SQLException e) {
            closeStatement(statement);
            throw e;
        } catch (Exception e) {
            closeStatement(statement);
            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    protected void setStatementTimeout(Statement stmt) throws SQLException {
        Integer timeout = mappedStatement.getTimeout();
        Integer defaultTimeout = configuration.getDefaultStatementTimeout();
        if (timeout != null) {
            stmt.setQueryTimeout(timeout);
        } else if (defaultTimeout != null) {
            stmt.setQueryTimeout(defaultTimeout);
        }
    }

    protected void setFetchSize(Statement stmt) throws SQLException {
        Integer fetchSize = mappedStatement.getFetchSize();
        if (fetchSize != null) {
            stmt.setFetchSize(fetchSize);
        }
    }

    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }

    protected void generateKeys(Object parameter) {
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        ErrorContext.instance().store();
        keyGenerator.processBefore(executor, mappedStatement, null, parameter);
        ErrorContext.instance().recall();
    }

}
```

## CallableStatementHandler

```java
/**
 * @author Clinton Begin
 */
public class CallableStatementHandler extends BaseStatementHandler {

    public CallableStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    public int update(Statement statement) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.execute();
        int rows = cs.getUpdateCount();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, cs, parameterObject);
        resultSetHandler.handleOutputParameters(cs);
        return rows;
    }

    public void batch(Statement statement) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.addBatch();
    }

    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.execute();
        List<E> resultList = resultSetHandler.<E>handleResultSets(cs);
        resultSetHandler.handleOutputParameters(cs);
        return resultList;
    }

    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        if (mappedStatement.getResultSetType() != null) {
            return connection.prepareCall(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        } else {
            return connection.prepareCall(sql);
        }
    }

    public void parameterize(Statement statement) throws SQLException {
        registerOutputParameters((CallableStatement) statement);
        parameterHandler.setParameters((CallableStatement) statement);
    }

    private void registerOutputParameters(CallableStatement cs) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0, n = parameterMappings.size(); i < n; i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
                if (null == parameterMapping.getJdbcType()) {
                    throw new ExecutorException("The JDBC Type must be specified for output parameter.  Parameter: " + parameterMapping.getProperty());
                } else {
                        if (parameterMapping.getNumericScale() != null && (parameterMapping.getJdbcType() == JdbcType.NUMERIC || parameterMapping.getJdbcType() == JdbcType.DECIMAL)) {
                            cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getNumericScale());
                    } else {
                        if (parameterMapping.getJdbcTypeName() == null) {
                            cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE);
                        } else {
                            cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getJdbcTypeName());
                        }
                    }
                }
            }
        }
    }

}
```

## PreparedStatementHandler

```java
/**
 * @author Clinton Begin
 */
public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, ps, parameterObject);
        return rows;
    }

    public void batch(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.addBatch();
    }

    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.<E> handleResultSets(ps);
    }

    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
            String[] keyColumnNames = mappedStatement.getKeyColumns();
            if (keyColumnNames == null) {
                return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                return connection.prepareStatement(sql, keyColumnNames);
            }
        } else if (mappedStatement.getResultSetType() != null) {
            return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        } else {
            return connection.prepareStatement(sql);
        }
    }

    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

}
```

## RoutingStatementHandler

```java
/**
 * @author Clinton Begin
 */
public class RoutingStatementHandler implements StatementHandler {

    private final StatementHandler delegate;

    public RoutingStatementHandler(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

        switch (ms.getStatementType()) {
            case STATEMENT:
                delegate = new SimpleStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            case PREPARED:
                delegate = new PreparedStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            case CALLABLE:
                delegate = new CallableStatementHandler(executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            default:
                throw new ExecutorException("Unknown statement type: " + ms.getStatementType());
        }

    }

    public Statement prepare(Connection connection) throws SQLException {
        return delegate.prepare(connection);
    }

    public void parameterize(Statement statement) throws SQLException {
        delegate.parameterize(statement);
    }

    public void batch(Statement statement) throws SQLException {
        delegate.batch(statement);
    }

    public int update(Statement statement) throws SQLException {
        return delegate.update(statement);
    }

    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        return delegate.<E>query(statement, resultHandler);
    }

    public BoundSql getBoundSql() {
        return delegate.getBoundSql();
    }

    public ParameterHandler getParameterHandler() {
        return delegate.getParameterHandler();
    }
}
```

## SimpleStatementHandler

```java
/**
 * @author Clinton Begin
 */
public class SimpleStatementHandler extends BaseStatementHandler {

    public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        //获得sql语句？  
        String sql = boundSql.getSql();
        //获得参数？
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        int rows;
        //statement执行sql语句返回更新数目
        if (keyGenerator instanceof Jdbc3KeyGenerator) {
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
            rows = statement.getUpdateCount();
            keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
        } else if (keyGenerator instanceof SelectKeyGenerator) {
            statement.execute(sql);
            rows = statement.getUpdateCount();
            keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
        } else {
            statement.execute(sql);
            rows = statement.getUpdateCount();
        }
        return rows;
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        //批处理
        String sql = boundSql.getSql();
        statement.addBatch(sql);
    }

    //ResultHandler resultHandler这个参数好像没用啊
    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        String sql = boundSql.getSql();
        //statement执行sql语句
        statement.execute(sql);
        return resultSetHandler.<E>handleResultSets(statement);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        if (mappedStatement.getResultSetType() != null) {
            return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        } else {
            return connection.createStatement();
        }
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        // N/A
    }

}
```