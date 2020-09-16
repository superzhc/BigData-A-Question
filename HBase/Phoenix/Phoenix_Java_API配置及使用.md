**Phoenix**定位为OLTP和操作型分析（operational analytics），大多用于在线业务，稳定性要求第一位。Phoenix的功能很强大，也很灵活，Phoenix SQL基于SQL-92标准，但是还是有很多方言，使用时需要特别注意。

## ZK方式

0. 需要把 `hbase-site.xml` 放到 resource 下面

1. `pom.xml` 依赖 `phoenix-core`

    ```xml
    <dependency>
        <groupId>org.apache.phoenix</groupId>
        <artifactId>phoenix-core</artifactId>
        <version>4.14.3-HBase-1.4</version>
    </dependency>
    ```

2. 可以配置到 Spring 项目中，比如下面数据源配置

    ```xml
    <bean id="phoenixDataSource" class="org.apache.commons.dbcp.BasicDataSource"  destroy-method="close">
        <property name="driverClassName" value="org.apache.phoenix.jdbc.PhoenixDriver"/>
        <property name="url" value="jdbc:phoenix:192.168.13.219,192.168.13.220,192.168.13.221:12181"/>
        <property name="username" value="" />
        <property name="password" value="" />
    </bean>
    ```

3. 也可以简单 main 方法进行 jdbc 测试

    ```java
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    
    public class TestPhoenix {
        public static void main(String[] args) throws Exception {
            Connection conn = DriverManager.getConnection("jdbc:phoenix:192.168.13.219,192.168.13.220,192.168.13.221:12181");
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("select * from zlxx.wt_trade_refund limit 5");
            while (rs.next()) {
                System.out.println("id=" + rs.getString("id") + ";amount=" + rs.getString("amount") + ";status=" + rs.getString("status"));
            }
            stat.close();
            conn.close();
        }
    }
    ```

## Query Server方式

**不需要 `hbase-site.xml`，也不需要 `phoenix-core`**

0. 不需要 `hbase-site.xml`

1. maven依赖`phoenix-queryserver-client`

    ```xml
    <dependency>
        <groupId>org.apache.phoenix</groupId>
        <artifactId>phoenix-queryserver-client</artifactId>
        <version>4.14.3-HBase-1.4</version>
    </dependency>
    ```

2. 配置到Spring项目中，如下数据源

    ```xml
    <bean id="phoenixDataSource" class="org.apache.commons.dbcp.BasicDataSource"  destroy-method="close">
        <property name="driverClassName" value="org.apache.phoenix.queryserver.client.Driver"/>
        <property name="url" value="jdbc:phoenix:thin:url=http://172.168.13.72:8765;serialization=PROTOBUF"/>
        <property name="username" value="" />
        <property name="password" value="" />
    </bean>
    ```

3. 简单的 main 方法测试

    ```java
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.ResultSet;
    import java.sql.Statement;
    
    public class TestPhoenix2 {
        public static void main(String[] args) throws Exception {
            Connection conn = DriverManager.getConnection("jdbc:phoenix:thin:url=http://192.168.13.72:8765;serialization=PROTOBUF");
            Statement stat = conn.createStatement();
            ResultSet rs = stat.executeQuery("select * from zlxx.wt_trade_refund limit 5");
            while (rs.next()) {
                System.out.println("id=" + rs.getString("id") + ";amount=" + rs.getString("amount") + ";status=" + rs.getString("status"));
            }
            stat.close();
            conn.close();
        }
    }
    ```