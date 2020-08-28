# MapperMethod

```java
/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 */
//这个类是整个代理机制的核心类，对Sqlsession当中的操作进行了封装
public class MapperMethod {
    //一个内部类 封装了SQL标签的类型 insert update delete select
    private final SqlCommand command;
    //一个内部类 封装了方法的参数信息 返回类型信息等
    private final MethodSignature method;

    //构造参数
    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(config, method);
    }

    //这个方法是对SqlSession的包装调用
    public Object execute(SqlSession sqlSession, Object[] args) {
        //定义返回结果
        Object result;
        //如果是INSERT操作
        if (SqlCommandType.INSERT == command.getType()) {
            //处理参数
            Object param = method.convertArgsToSqlCommandParam(args);
            //调用sqlSession的insert方法
            result = rowCountResult(sqlSession.insert(command.getName(), param));
        //如果是UPDATE操作 同上
        } else if (SqlCommandType.UPDATE == command.getType()) {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.update(command.getName(), param));
        //如果是DELETE操作 同上
        } else if (SqlCommandType.DELETE == command.getType()) {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.delete(command.getName(), param));
        //如果是SELECT操作 那么情况会多一些 但是也都和sqlSession的查询方法一一对应
        } else if (SqlCommandType.SELECT == command.getType()) {
            //如果返回void 并且参数有resultHandler
            //则调用 void select(String statement, Object parameter, ResultHandler handler);方法
            if (method.returnsVoid() && method.hasResultHandler()) {
                executeWithResultHandler(sqlSession, args);
                result = null;
            //如果返回多行结果这调用 <E> List<E> selectList(String statement, Object parameter);
            //executeForMany这个方法调用的
            } else if (method.returnsMany()) {
                result = executeForMany(sqlSession, args);
            //如果返回类型是MAP 则调用executeForMap方法
            } else if (method.returnsMap()) {
                result = executeForMap(sqlSession, args);
            } else {
                //否则就是查询单个对象
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
            }
        } else if (SqlCommandType.FLUSH == command.getType()) {
            result = sqlSession.flushStatements();
        } else {
            //如果全都不匹配 说明mapper中定义的方法不对
            throw new BindingException("Unknown execution method for: " + command.getName());
        }
        //如果返回值为空 并且方法返回值类型是基础类型 并且不是VOID 则抛出异常
        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = Integer.valueOf(rowCount);
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = Long.valueOf(rowCount);
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = Boolean.valueOf(rowCount > 0);
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        if (void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException("method " + command.getName()
                + " needs either a @ResultMap annotation, a @ResultType annotation,"
                + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
        } else {
            sqlSession.select(command.getName(), param, method.extractResultHandler(args));
        }
    }

    //返回多行结果 调用sqlSession.selectList方法
    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        //如果参数含有rowBounds则调用分页的查询
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<E>selectList(command.getName(), param, rowBounds);
        } else {
            //没有分页则调用普通查询
            result = sqlSession.<E>selectList(command.getName(), param);
        }
        // issue #510 Collections & arrays support
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
            return convertToArray(result);
            } else {
            return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }
        return result;
    }

    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        Object collection = config.getObjectFactory().create(method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    @SuppressWarnings("unchecked")
    private <E> E[] convertToArray(List<E> list) {
        E[] array = (E[]) Array.newInstance(method.getReturnType().getComponentType(), list.size());
        array = list.toArray(array);
        return array;
    }

    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Map<K, V> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey());
        }
        return result;
    }

    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }
    //一个内部类 封装了具体执行的动作
    public static class SqlCommand {

        //xml标签的id
        private final String name;
        //insert update delete select的具体类型
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            //拿到全名 比如 org.mybatis.example.BlogMapper.selectBlog
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = null;
            //获取MappedStatement对象 这个对象封装了XML当中一个标签的所有信息 比如下面
            //<select id="selectBlog" resultType="Blog">
            //select * from Blog where id = #{id}
            //</select>
            if (configuration.hasStatement(statementName)) {
                ms = configuration.getMappedStatement(statementName);
            } else if (!mapperInterface.equals(method.getDeclaringClass())) { // issue #35
                String parentStatementName = method.getDeclaringClass().getName() + "." + method.getName();
                if (configuration.hasStatement(parentStatementName)) {
                    ms = configuration.getMappedStatement(parentStatementName);
                }
            }
            //为空抛出异常
            if (ms == null) {
                if(method.getAnnotation(Flush.class) != null){
                    name = null;
                    type = SqlCommandType.FLUSH;
                } else {
                    throw new BindingException("Invalid bound statement (not found): " + statementName);
                }
            } else {
                name = ms.getId();
                type = ms.getSqlCommandType();
                //判断SQL标签类型 未知就抛异常
                if (type == SqlCommandType.UNKNOWN) {
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }

    //内部类 封装了接口当中方法的 参数类型 返回值类型 等信息
    public static class MethodSignature {
        //是否返回多调结果
        private final boolean returnsMany;
        //返回值是否是MAP
        private final boolean returnsMap;
        //返回值是否是VOID
        private final boolean returnsVoid;
        //返回值类型
        private final Class<?> returnType;
        //mapKey
        private final String mapKey;
        //resultHandler类型参数的位置
        private final Integer resultHandlerIndex;
        //rowBound类型参数的位置
        private final Integer rowBoundsIndex;
        //用来存放参数信息
        private final SortedMap<Integer, String> params;
        //是否存在命名参数
        private final boolean hasNamedParameters;

        public MethodSignature(Configuration configuration, Method method) {
            this.returnType = method.getReturnType();
            this.returnsVoid = void.class.equals(this.returnType);
            this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
            this.mapKey = getMapKey(method);
            this.returnsMap = (this.mapKey != null);
            this.hasNamedParameters = hasNamedParams(method);
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            this.params = Collections.unmodifiableSortedMap(getParams(method, this.hasNamedParameters));
        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                return null;
            } else if (!hasNamedParameters && paramCount == 1) {
                return args[params.keySet().iterator().next().intValue()];
            } else {
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                for (Map.Entry<Integer, String> entry : params.entrySet()) {
                    param.put(entry.getValue(), args[entry.getKey().intValue()]);
                    // issue #71, add param names as param1, param2...but ensure backward compatibility
                    final String genericParamName = "param" + String.valueOf(i + 1);
                    if (!param.containsKey(genericParamName)) {
                        param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        public ResultHandler extractResultHandler(Object[] args) {
            return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
        }

        public String getMapKey() {
            return mapKey;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        public boolean returnsMany() {
            return returnsMany;
        }

        public boolean returnsMap() {
            return returnsMap;
        }

        public boolean returnsVoid() {
            return returnsVoid;
        }

        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }

        private String getMapKey(Method method) {
            String mapKey = null;
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
                if (mapKeyAnnotation != null) {
                    mapKey = mapKeyAnnotation.value();
                }
            }
            return mapKey;
        }

        private SortedMap<Integer, String> getParams(Method method, boolean hasNamedParameters) {
            final SortedMap<Integer, String> params = new TreeMap<Integer, String>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (!RowBounds.class.isAssignableFrom(argTypes[i]) && !ResultHandler.class.isAssignableFrom(argTypes[i])) {
                    String paramName = String.valueOf(params.size());
                    if (hasNamedParameters) {
                        paramName = getParamNameFromAnnotation(method, i, paramName);
                    }
                    params.put(i, paramName);
                }
            }
            return params;
        }

        private String getParamNameFromAnnotation(Method method, int i, String paramName) {
            final Object[] paramAnnos = method.getParameterAnnotations()[i];
            for (Object paramAnno : paramAnnos) {
                if (paramAnno instanceof Param) {
                    paramName = ((Param) paramAnno).value();
                    break;
                }
            }
            return paramName;
        }

        private boolean hasNamedParams(Method method) {
            final Object[][] paramAnnos = method.getParameterAnnotations();
            for (Object[] paramAnno : paramAnnos) {
                for (Object aParamAnno : paramAnno) {
                    if (aParamAnno instanceof Param) {
                        return true;
                    }
                }
            }
            return false;
        }

    }
}
```