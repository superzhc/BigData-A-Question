# SqlSessionFactoryBuilder

SqlSessionFactoryBuilder类的主要作用就是创建一个SqlSessionFactory，通过输入mybatis配置文件的字节流或者字符流，生成XMLConfigBuilder，XMLConfigBuilder创建一个Configuration，Configuration这个类中包含了mybatis的配置的一切信息，mybatis进行的所有操作都需要根据Configuration中的信息来进行。

SqlSessionFactoryBuilder源码及注释：

```java
/*
* Builds {@link SqlSession} instances.
*
* @author Clinton Begin
*/
//通过读入配置文件，最终获得一个sqlSessionFactory
public class SqlSessionFactoryBuilder {
    //以字符输入流引入配置文件
    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    //environment是数据源环境
    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            //解析myBatis配置文件，以字符流的方式
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            return build(parser.parse());//获得SqlSessionFactory
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    //以字节输入流传入配置信息
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            //解析myBatis配置文件，以字节流的方式
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            return build(parser.parse());//获得SqlSessionFactory
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(Configuration config) {
        //获得sqlsessionFactory工厂，再从工厂中获得sqlsession
        return new DefaultSqlSessionFactory(config);
    }
}
```
