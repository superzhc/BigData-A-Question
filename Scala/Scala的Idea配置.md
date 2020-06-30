### IDEA 开发 “Hello World” 工程

1. 创建 maven 项目
2. 添加 `src/main/scala` 设置为 `Source Dir`
3. 添加 scala 框架，在 SDK 添加 scala 的版本
4. 在 scala 文件右击创建 scala class

### 构建 Java/Scala 混合项目

#### 方案一：

`File` --> `New Project` --> `勾选Maven` ---> `再勾选Create from archetype` --> `选择 "org.scala-tools.archetypes:scala-archetype-simple"`

这种方式构建混合工程较为简单，但是由于插件更新不及时，安装时的scala版本及jdk版本可能较小，需要与目标项目的版本做相应的适配。

#### 方案二：

①、**本机安装scala sdk**

去scala官网 http://www.scala-lang.org/download 下载与操作系统对应的scala版本，安装。

②、**下载Scala插件**

Idea上Plugins搜索下载Scala插件，也可以下载离线安装包（点击打开链接），本地以disk方式安装。

务必注意在下载时，留意scala插件库中版本号与本机IEDA的版本对应关系。

③、**与maven整合**

编辑pom.xml，设置为如下内容：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.balabala.mixed</groupId>
  <artifactId>mixed-scala-java-test</artifactId>
  <version>1.0-SNAPSHOT</version>
  <properties>
    <scala.version>2.10.0</scala.version>
  </properties>

  <repositories>
    <repository>
      <id>scala-tools.org</id>
      <name>Scala-Tools Maven2 Repository</name>
      <url>http://scala-tools.org/repo-releases</url>
    </repository>
  </repositories>

  <pluginRepositories>
    <pluginRepository>
      <id>scala-tools.org</id>
      <name>Scala-Tools Maven2 Repository</name>
      <url>http://scala-tools.org/repo-releases</url>
    </pluginRepository>
  </pluginRepositories>

  <dependencies>
    <dependency>
      <groupId>org.scala-lang</groupId>
      <artifactId>scala-library</artifactId>
      <version>${scala.version}</version>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.4</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.specs</groupId>
      <artifactId>specs</artifactId>
      <version>1.2.5</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <sourceDirectory>src/main/scala</sourceDirectory>
    <testSourceDirectory>src/test/scala</testSourceDirectory>
    <plugins>
      <plugin>
        <groupId>org.scala-tools</groupId>
        <artifactId>maven-scala-plugin</artifactId>
        <version>2.15.2</version>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
              <goal>testCompile</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <scalaVersion>${scala.version}</scalaVersion>
          <args>
            <arg>-target:jvm-1.8</arg>
          </args>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-eclipse-plugin</artifactId>
        <configuration>
          <downloadSources>true</downloadSources>
          <buildcommands>
            <buildcommand>ch.epfl.lamp.sdt.core.scalabuilder</buildcommand>
          </buildcommands>
          <additionalProjectnatures>
            <projectnature>ch.epfl.lamp.sdt.core.scalanature</projectnature>
          </additionalProjectnatures>
          <classpathContainers>
            <classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>
            <classpathContainer>ch.epfl.lamp.sdt.launching.SCALA_CONTAINER</classpathContainer>
          </classpathContainers>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <reporting>
    <plugins>
      <plugin>
        <groupId>org.scala-tools</groupId>
        <artifactId>maven-scala-plugin</artifactId>
        <configuration>
          <scalaVersion>${scala.version}</scalaVersion>
        </configuration>
      </plugin>
    </plugins>
  </reporting>
</project>
```

④、**maven编译打包**

直接使用 `mvn clean package` 会报错，因为默认只会打包java文件，需要使用如下命令替换之：

`mvn clean scala:compile compile package`

命令解释：

如③中的 `maven-scala-plugin` 配置，是其提供的对scala编译打包的插件命令，将先编译scala源码，再编译java源码，最后做package操作。

