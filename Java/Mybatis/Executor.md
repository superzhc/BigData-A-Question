# Executor

## Executor 接口

Executor接口主要提供了update、query方法及事物相关的方法接口。

```java
/**
 * @author Clinton Begin
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    /* 更新操作**/
    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    List<BatchResult> flushStatements() throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    boolean isCached(MappedStatement ms, CacheKey key);

    void clearLocalCache();

    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    Transaction getTransaction();

    void close(boolean forceRollback);

    boolean isClosed();

    void setExecutorWrapper(Executor executor);

}
```

## BaseExecutor 抽象类

抽象类BaseExecutor采用模板方法设计模式，具体的实现在其子类：BatchExecutor、ReuseExecutor和SimpleExecutor。

```java
/**
 * @author Clinton Begin
 */
public abstract class BaseExecutor implements Executor {

    private static final Log log = LogFactory.getLog(BaseExecutor.class);
    //事务
    protected Transaction transaction;
    //executor
    protected Executor wrapper;
    //线程安全队列
    protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;
    //缓存 永久缓存   本地缓存
    protected PerpetualCache localCache;
    //
    protected PerpetualCache localOutputParameterCache;
    //mybatis的配置信息
    protected Configuration configuration;

    //查询堆栈
    protected int queryStack = 0;
    private boolean closed;

    //构造函数
    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.transaction = transaction;
        this.deferredLoads = new ConcurrentLinkedQueue<DeferredLoad>();
        this.localCache = new PerpetualCache("LocalCache");
        this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");
        this.closed = false;
        this.configuration = configuration;
        this.wrapper = this;
    }

    public Transaction getTransaction() {
        if (closed) throw new ExecutorException("Executor was closed.");
        return transaction;
    }

    public void close(boolean forceRollback) {
        try {
            try {
                rollback(forceRollback);
            } finally {
                if (transaction != null) transaction.close();
            }
        } catch (SQLException e) {
            // Ignore.  There's nothing that can be done at this point.
            log.warn("Unexpected exception on closing transaction.  Cause: " + e);
        } finally {
            transaction = null;
            deferredLoads = null;
            localCache = null;
            localOutputParameterCache = null;
            closed = true;
        }
    }

    public boolean isClosed() {
        return closed;
    }

    //SqlSession的update/insert/delete会调用此方法
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
        if (closed) throw new ExecutorException("Executor was closed.");
        //先清局部缓存，再更新，如何更新由子类实现，模板方法模式
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    public List<BatchResult> flushStatements() throws SQLException {
        return flushStatements(false);
    }

    public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
        if (closed) throw new ExecutorException("Executor was closed.");
        return doFlushStatements(isRollBack);
    }

    //SqlSession.selectList会调用此方法
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        //得到绑定sql
        BoundSql boundSql = ms.getBoundSql(parameter);
        //创建缓存key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        //查询
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
        //如果已经关闭，报错
        if (closed) throw new ExecutorException("Executor was closed.");
        //先清局部缓存，再查询，但仅仅查询堆栈为0才清，为了处理递归调用
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            clearLocalCache();
        }
        List<E> list;
        try {
            //加一，这样递归调用到上面的时候就不会再清局部缓存了
            queryStack++;
            //根据cachekey从localCache去查
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list != null) {
                //如果查到localCache缓存，处理localOutputParameterCache
                handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {
                //从数据库查
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            //清空堆栈
            queryStack--;
        }
        if (queryStack == 0) {
            //延迟加载队列中所有元素
            for (DeferredLoad deferredLoad : deferredLoads) {
                deferredLoad.load();
            }
            //清空延迟加载队列
            deferredLoads.clear(); // issue #601
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                //如果是statement，清本地缓存
                clearLocalCache(); // issue #482
            }
        }
        return list;
    }

    //延迟加载
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        if (closed) throw new ExecutorException("Executor was closed.");
        DeferredLoad deferredLoad = new DeferredLoad(resultObject, property, key, localCache, configuration, targetType);
        //如果能加载则立即加载，否则加入到延迟加载队列中
        if (deferredLoad.canLoad()) {
            deferredLoad.load();
        } else {
            //这里怎么又new了一个新的，性能有点问题
            deferredLoads.add(new DeferredLoad(resultObject, property, key, localCache, configuration, targetType));
        }
    }

    //创建缓存key
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        if (closed) throw new ExecutorException("Executor was closed.");
        CacheKey cacheKey = new CacheKey();
        //MyBatis 对于其 Key 的生成采取规则为：[mappedStementId + offset + limit + SQL + queryParams + environment]生成一个哈希码
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        for (int i = 0; i < parameterMappings.size(); i++) { // mimic DefaultParameterHandler logic
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                cacheKey.update(value);
            }
        }
        return cacheKey;
    }

    public boolean isCached(MappedStatement ms, CacheKey key) {
        return localCache.getObject(key) != null;
    }

    public void commit(boolean required) throws SQLException {
        if (closed) throw new ExecutorException("Cannot commit, transaction is already closed");
        clearLocalCache();
        flushStatements();
        if (required) {
            transaction.commit();
        }
    }

    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
                flushStatements(true);
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    //清空本地缓存，一个map结构
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
            localOutputParameterCache.clear();
        }
    }

    protected abstract int doUpdate(MappedStatement ms, Object parameter)
        throws SQLException;

    protected abstract List<BatchResult> doFlushStatements(boolean isRollback)
        throws SQLException;

    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
        throws SQLException;

    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            // ignore
            }
        }
    }

    //处理存储过程的out参数
    private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            final Object cachedParameter = localOutputParameterCache.getObject(key);
            if (cachedParameter != null && parameter != null) {
                final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
                final MetaObject metaParameter = configuration.newMetaObject(parameter);
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    if (parameterMapping.getMode() != ParameterMode.IN) {
                    final String parameterName = parameterMapping.getProperty();
                    final Object cachedValue = metaCachedParameter.getValue(parameterName);
                    metaParameter.setValue(parameterName, cachedValue);
                    }
                }
            }
        }
    }
    //从数据库中查
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;
        //向缓存中放入占位符
        localCache.putObject(key, EXECUTION_PLACEHOLDER);
        try {
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            //清除占位符
            localCache.removeObject(key);
        }
        //加入缓存
        localCache.putObject(key, list);
        //如果是存储过程，OUT参数也加入缓存
        if (ms.getStatementType() == StatementType.CALLABLE) {
            localOutputParameterCache.putObject(key, parameter);
        }
        return list;
    }

    protected Connection getConnection(Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        if (statementLog.isDebugEnabled()) {
            return ConnectionLogger.newInstance(connection, statementLog, queryStack);
        } else {
            return connection;
        }
    }

    public void setExecutorWrapper(Executor wrapper) {
        this.wrapper = wrapper;
    }

    //延迟加载
    private static class DeferredLoad {

        private final MetaObject resultObject;
        private final String property;
        private final Class<?> targetType;
        private final CacheKey key;
        private final PerpetualCache localCache;
        private final ObjectFactory objectFactory;
        private final ResultExtractor resultExtractor;

        public DeferredLoad(MetaObject resultObject,
                            String property,
                            CacheKey key,
                            PerpetualCache localCache,
                            Configuration configuration,
                            Class<?> targetType) { // issue #781
            this.resultObject = resultObject;
            this.property = property;
            this.key = key;
            this.localCache = localCache;
            this.objectFactory = configuration.getObjectFactory();
            this.resultExtractor = new ResultExtractor(configuration, objectFactory);
            this.targetType = targetType;
        }

        //缓存中找到，且不为占位符，代表可以加载
        public boolean canLoad() {
            return localCache.getObject(key) != null && localCache.getObject(key) != EXECUTION_PLACEHOLDER;
        }

        //加载
        public void load() {
            @SuppressWarnings( "unchecked" ) // we suppose we get back a List
            List<Object> list = (List<Object>) localCache.getObject(key);
            Object value = resultExtractor.extractObjectFromList(list, targetType);
            resultObject.setValue(property, value);
        }

    }
}
```

## CachingExecutor 抽象类

CachingExecutor 从内存中获取数据，在查找数据库前先查找缓存，若没有找到的话调用delegate从数据库查询，并将查询结果存入缓存中。

```java
/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class CachingExecutor implements Executor {

    private Executor delegate;
    private TransactionalCacheManager tcm = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    public void close(boolean forceRollback) {
        try {
            //issues #499, #524 and #573
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    //是否关闭了executor
    public boolean isClosed() {
        return delegate.isClosed();
    }

    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        //是否需要更缓存
        flushCacheIfRequired(ms);
        //更新数据
        return delegate.update(ms, parameterObject);
    }

    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        //创建缓存值
        CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
        //获取记录
        return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
        throws SQLException {
        //获取缓存
        Cache cache = ms.getCache();
        if (cache != null) {
            flushCacheIfRequired(ms);
            if (ms.isUseCache() && resultHandler == null) {
                ensureNoOutParams(ms, parameterObject, boundSql);
                @SuppressWarnings("unchecked")
                //从缓存中获取数据
                List<E> list = (List<E>) tcm.getObject(cache, key);
                //为空执行一次，将结果保存到缓存中
                if (list == null) {
                    list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                    tcm.putObject(cache, key, list); // issue #578. Query must be not synchronized to prevent deadlocks
                }
                return list;
            }
        }
        return delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        tcm.commit();
    }

    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    private void ensureNoOutParams(MappedStatement ms, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
                }
            }
        }
    }

    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    public boolean isCached(MappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);
    }

    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }
}
```

## SimpleExecutor

```java
/**
 * @author Clinton Begin
 */
public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            //获得配置文件对象
            Configuration configuration = ms.getConfiguration();
            //获得statementHandler里面有statement，来处理
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
            stmt = prepareStatement(handler, ms.getStatementLog());
            //最终是一个statement进行处理
            return handler.update(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Statement stmt = null;
        try {
            //获得配置文件对象
            Configuration configuration = ms.getConfiguration();
            //获得statementHandler里面有statement，来处理
            StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
            //获得statement
            stmt = prepareStatement(handler, ms.getStatementLog());
            //最终是一个statement进行处理
            return handler.<E>query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        return Collections.emptyList();
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        Connection connection = getConnection(statementLog);
        stmt = handler.prepare(connection);
        handler.parameterize(stmt);
        return stmt;
    }

}
```

## ReuseExecutor

```java
/**
 * @author Clinton Begin
 */
public class ReuseExecutor extends BaseExecutor {

    private final Map<String, Statement> statementMap = new HashMap<String, Statement>();

    public ReuseExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.update(stmt);
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        Statement stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>query(stmt, resultHandler);
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        for (Statement stmt : statementMap.values()) {
            closeStatement(stmt);
        }
        statementMap.clear();
        return Collections.emptyList();
    }

    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
        Statement stmt;
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();
        if (hasStatementFor(sql)) {
            stmt = getStatement(sql);
        } else {
            Connection connection = getConnection(statementLog);
            stmt = handler.prepare(connection);
            putStatement(sql, stmt);
        }
        handler.parameterize(stmt);
        return stmt;
    }

    private boolean hasStatementFor(String sql) {
        try {
            return statementMap.keySet().contains(sql) && !statementMap.get(sql).getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private Statement getStatement(String s) {
        return statementMap.get(s);
    }

    private void putStatement(String sql, Statement stmt) {
        statementMap.put(sql, stmt);
    }

}
```