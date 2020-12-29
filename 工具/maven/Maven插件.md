# Maven 插件

## CXF 代理类生成插件

```xml
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <version>${cxf.version}</version>
    <executions>
        <execution>
            <!--生成第一个客户端代码 -->
            <id>generate-sources-client1</id>
            <phase>generate-sources</phase>
            <configuration>
                <!-- 生成代理类的目录 -->
                <sourceRoot>src/main/java</sourceRoot>
                <wsdlOptions>
                    <wsdlOption>
                        <!-- wsdl地址 -->
                        <wsdl>http://localhost:8090/project-name/MyFirstService?wsdl</wsdl>
                        <!-- 指定代理类包名 -->
                        <packagenames>
                            <packagename>com.airkisser.client.first_client</packagename>
                        </packagenames>
                        <!-- 是否生成soapheader -->
                        <extendedSoapHeaders>true</extendedSoapHeaders>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
            <goals>
                <goal>wsdl2java</goal>
            </goals>
        </execution>
        <execution>
            <!--生成第二个客户端代码 -->
            <id>generate-sources-client2</id>
            <phase>generate-sources</phase>
            <configuration>
                <!-- 生成代理类的目录 -->
                <sourceRoot>src/main/java</sourceRoot>
                <wsdlOptions>
                    <wsdlOption>
                        <!-- wsdl地址 -->
                        <wsdl>http://localhost:8090/project-name/MySecondService?wsdl</wsdl>
                        <!-- 指定代理类包名 -->
                        <packagenames>
                            <packagename>com.airkisser.client.second_client</packagename>
                        </packagenames>
                        <!-- 是否生成soapheader -->
                        <extendedSoapHeaders>true</extendedSoapHeaders>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
            <goals>
                <goal>wsdl2java</goal>
            </goals>
        </execution>
    </executions>
    </plugin>
```