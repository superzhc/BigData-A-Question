# Maven

## 简介

Apache Maven 定义：

> Maven是一个项目管理工具，它包含了一个项目对象模型 (Project Object Model)，一组标准集合，一个项目生命周期(Project Lifecycle)，一个依赖管理系统(Dependency Management System)，和用来运行定义在生命周期阶段(phase)中插件(plugin)目标(goal)的逻辑。 当你使用Maven的时候，你用一个明确定义的项目对象模型来描述你的项目，然后 Maven 可以应用横切的逻辑，这些逻辑来自一组共享的（或者自定义的）插件。

Maven 的用途之一是服务于构建，它是一个异常强大的构建工具，能够自动化构建过程，从清理、编译到生成报告。再到打包和部署。

Maven 最大化地消除了构建的重复，抽象了构建生命周期，并且为绝大部分的构建任务提供了已实现的插件，不需要定义过程，甚至也不需要去实现这个过程中的一些任务。

> Maven 作为一个构建工具，不仅能自动化构建，还能够抽象构建过程，提供构建任务实现；它跨平台，对外提供了一致的操作接口，这一切足以使它成为优秀的、流行的构建工具。

Maven 还是一个依赖管理工具和项目信息管理工具。它提供了中央仓库，能自动下载构件。

Maven 对于项目目录结构、测试用例命名方式等内容都有既定的规则，可以说是约定大于配置。

## 安装和配置

## 基础

Maven 项目的核心是 `pom.xml`。POM(Project Object Model，项目对象模型)定义了项目的基本信息，用于描述项目如何创建，声明项目依赖。。。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.epoint.ztb_ah</groupId>
    <artifactId>ZtbCustomMis_AH</artifactId>
    <packaging>jar</packaging>
    <version>7.1.30.2</version>

</project>
```

- modelVersion:指定了当前POM模型的版本，对于Maven2及Maven3只能是4.0.0
- groupId、artifactId和version这三个元素定义了一个项目基本的坐标

## 坐标和依赖

### 坐标

Maven 定义的规则：

> 世界上任何一个构建都可以使用Maven坐标唯一标识，Maven坐标的元素包括`groupId`、`artifactId`、`version`、`packaging`、`classifier`。

- groupId:定义当前Maven项目隶属的实际项目
- artifactId：该元素定义实际项目中的一个Maven项目（模块），推荐的做法是使用实际项目名称作为artifactId的前缀
- version：该元素定义Maven项目当前所处的版本
- packaging：该元素定义Maven项目的打包方式，注：当不定义packaging的时候，Maven会使用默认值jar【可选】
- classifier：该元素用来帮助定义构建输出的一些附属构建。附属构建与主构建对应，如javadoc和source这两个附属构件，其包含了Java文档和源代码。注：不能直接定义项目的classifier，因为附属构件不是项目默认直接生成的，而是由附加的插件帮助生成。

### 依赖

#### 依赖配置

![依赖配置模版](../images/Maven_dependency.jpg)

根元素project下的dependencies可以包含一个或者多个dependency元素，以声明一个或者多个项目依赖。每个依赖可以包含的元素有：

- groupId、artificial和version：依赖的基本坐标，对于任何一个依赖来说，基本坐标是最重要的，Maven根据坐标才能找到需要的依赖
- type：依赖的类型，对应于项目坐标定义的packaging。大部分情况下，该元素不必声明，其默认值为jar
- scope：依赖的范围。因Maven 在编译项目主代码的时候需要使用一套classpath，在编译和执行测试的时候使用另一套classpath，实际运行Maven项目的时候，又会使用一套classpath，依赖范围就是用来控制依赖于这三种classpath（编译classpath、测试classpath、运行classpath）的关系，Maven有以下几种依赖范围：
    - compile：编译依赖范围。如果没有指定，就会默认使用该依赖范围。使用此依赖范围的Maven依赖，对于编译、测试、运行三种classpath都有效
    - test：测试依赖范围，使用此依赖范围的Maven依赖只对于测试classpath有效，在编译主代码或者运行项目的时候时将无法使用此类依赖
    - provided：已提供依赖范围，使用此依赖的Maven依赖，对于编译和测试classpath有效，但在运行时无效。如servlet-api，编译和测试项目的时候需要该依赖，但在运行项目的时候，由于容器已经提供，就不需要Maven重复地引入一遍
    - runtime：运行时依赖范围。使用此依赖范围的Maven依赖，对于测试和运行classpath有效，但在编译主代码时无效。典型的例子是JDBC驱动实现，项目主代码的编译只需要JDK提供的JDBC接口，只有在执行测试或者运行项目的时候才需要实现上述接口的具体JDBC驱动
    - system：系统以来范围。该依赖与三种classpath的关系，和provided依赖范围完全一致。但是，使用system范围的依赖时必须通过systemPath元素显式地指定依赖文件的路径。由于此依赖不是通过Maven仓库解析的，而且往往与本机系统绑定，可能造成构建的不可移植，因此应该谨慎使用。systemPath元素可以引用环境变量。
    - import：导入依赖范围。该依赖范围不会对三种classpath产生实际影响
- optional：标记依赖是否可选
- exclusions：用来排除传递性依赖

#### 传递性依赖

Maven 会直接解析各个直接依赖的POM，将那些必要的间接依赖，以传递性依赖的形式引入到当前项目中。

依赖范围：假设A依赖于B，B依赖于C，也就是说A对于B是第一直接依赖，B对于C是第二直接依赖，A对于C是传递性依赖。第一直接依赖的范围和第二直接依赖的范围决定了传递性依赖的范围，规律为：当第二直接依赖范围是compile的时候，传递性依赖的范围与第一直接依赖范围一致；当第二直接依赖的范围是test的时候，依赖不会得以传递；当第二直接依赖的范围是provided的依赖，只传递第一直接依赖范围也为provided的依赖，且传递性依赖的范围同样为provided；当第二直接依赖的范围是runtime的时候，传递性依赖的范围与第一直接依赖范围一致，但compile例外，此时传递性依赖的范围为runtime。

依赖调解： 项目A有这样的依赖：A->B->C->X(1.0) 、A->D->X(2.0)，X是A的传递性依赖，但存在两个版本的X，依据Maven依赖调解的第一原则是**路径最近者优先**，X(1.0)的路径长度为3，X(2.0)的路径长度为2，因此X(2.0)会被解析使用。当路径长度一样的时候，Maven定义了依赖调解的第二原则 **第一声明者优先**，即在依赖路径长度相等的前提下，在POM中依赖声明的顺序决定了谁会被解析使用，顺序最靠前的那个依赖被使用。

## 仓库

> 在Maven中任何一个依赖、插件或者项目构建的输出都可以称为构件。得益于坐标机制，任何Maven项目使用任何一个构件的方式都是相同的。在此基础上，Maven可以在某个位置统一存储所有Maven项目共享的构件，这个统一的位置就是仓库。

仓库分类：本地仓库和远程仓库。

当Maven根据坐标寻找构件的时候，它首先会查看本地仓库，如果本地仓库存在此构件，则直接使用；如果本地仓库不存在此构件，或者需要查看是否有更新的构件版本，Maven会去远程仓库查找，发现需要的构件之后，下载到本地仓库再使用。如果本地仓库和远程仓库都没有需要的构件，Maven就会报错。

本地仓库：

默认情况下，本地都会有一个路径名为`.m2/repository/`的仓库目录。

一个构件只有在本地仓库中，才能由其他Maven项目使用。

远程仓库的分类：

- 中央仓库：Maven核心自带的远程仓库，它包含了绝大部分开源的构件。在默认配置下，当本地仓库没有Maven需要的构件的时候，它就会尝试从中央仓库下载。中央仓库的地址(<http://repo1.maven.org/maven2>)
- 私服：一种特殊的远程仓库，它是架设在局域网内的仓库服务，私服代理广域网上的远程仓库，供局域网内的Maven用户使用。当Maven需要下载构件的时候，它从私服请求，如果私服上不存在该构件，则从外部的远程仓库下载，缓存在私服上之后，再为Maven的下载请求提供服务。此外一些无法从外部仓库下载到的构件也能从本地上传到私服上供其他用户使用。
    - 节约自己的外网带宽
    - 加速Maven的构建
    - 部署第三方构件
    - 提高稳定性，增强控制机制
    - 降低中央仓库的负荷
- 其他公共库

快照版本：`-SNAPSHOT`后缀，快照版本是不稳定的。

从仓库解析依赖的机制：

1. 当依赖的范围是system的时候，Maven直接从本地文件系统解析构件
2. 根据依赖坐标计算仓库路径后，尝试直接从本地仓库寻找构件，如果发现相应构件，则解析成功
3. 在本地仓库不存在相应构件的情况下，如果依赖的版本是显式的发布版本构件，如1.2、2.1-beta-1等，则遍历所有的远程仓库，发现后，下载并解析使用
4. 如果依赖的版本是RELEASE或者LATEST，则基于更新策略读取所有远程仓库的元数据groupId/artifactId/maven-metadata.xml，将其与本地仓库的对应元数据合并后，计算出RELEASE或者LASTET真实的值，然后基于这个真实的值检查本地和远程仓库，如步骤2)和3)
5. 如果依赖的版本是SNAPSHOT，则基于更新策略读取所有远程仓库的元数据groupId/artifactId/version/maven-metadata.xml，将其与本地仓库的对应元数据合并后，得到最新快照版本的值，然后基于该值检查本地仓库，或者从远程仓库下载
6. 如果最后解析得到的构件版本是时间戳格式的快照，如1.4.1-20091104.121450-121，则复制其时间戳格式的文件至非时间戳，如SNAPSHOT，并使用该非时间戳格式的构件

当依赖的版本不明晰的时候，如RELEASE、LATEST和SNAPSHOT，Maven就需要基于更新远程仓库的更新策略来检查更新。当Maven检查完更新策略，并决定检查依赖更新的时候，就需要检查仓库元数据maven-metadata.xml。

### 镜像

如果仓库X可以提供仓库Y存储的所有内容，那么就可以认为X是Y的一个镜像，也就是说，任何一个可以从仓库Y获得的构件，都能够从它的镜像获取。

镜像常见的用法是结合私服，由于私服可以代理任何外部的公共仓库（包括中央仓库），因此对于组织内部的Maven用户来说，使用一个私服地址就等于使用了所有的需要的外部仓库，这可以将配置集中到私服，从而简化Maven本身的配置。在这种情况下，任何需要的构件都可以从私服获得，私服就是所有仓库的镜像。

### 仓库搜索服务

- Sonatype Nexus
- Jarvana
- MVNbrowser
- MVNrepository

## 生命周期和插件

Maven的生命周期就是为了对所有的构件过程进行抽象和统一。这个生命周期包含了项目的清理、初始化、编译、测试、打包、继承测试、验证、部署和站点生成等几乎所有的构建步骤。

Maven的生命周期是抽象的，这意味着生命周期本身不做任何实际工作，在Maven的设计中，实际的任务都交由插件来完成。

Maven设计了插件机制，每个构建步骤都可以绑定一个或者多个插件行为，而且Maven为大多数构建步骤编写并绑定了默认插件。

Maven定义的生命周期和插件机制一方面保证了所有Maven项目有一致的构建标准，另一方面又通过默认插件简化和稳定了实际项目的构建。此外，该机制还提供了足够的扩展空间，用户可以通过配置现有插件或者自行编写插件来自定义构建行为。

### 三套生命周期

Maven拥有三套相互独立的生命周期，它们分别为`clean`、`default`和`site`，`clean`生命周期的目的是清理项目，`default`生命周期的目的是构建项目，而`site`生命周期的目的是建立项目站点。

每个生命周期包含一些阶段（phase），这些阶段是有顺序的，并且后面的阶段依赖于前面的阶段，用户和Maven最直接的交互方式就是调用这些生命周期阶段。

较之于生命周期阶段的前后依赖关系，三套生命周期本身是相互独立的，即调用clean不会对其他生命周期default、site产生影响。

#### clean生命周期

clean生命周期的目的是清理项目，它包含三个阶段：

1. pre-clean：执行一些清理前需要完成的工作
2. clean：清理上一次构建生成的文件
3. post-clean：执行一些清理后需要完成的工作

#### default生命周期

default生命周期定义了真正构建时所需要执行的所有步骤，它是所有生命周期中最核心的部分，其包含的阶段如下：

1. validate
2. initialize
3. generate-sources
4. process-sources：处理项目主资源文件。一般来说，是对`src/main/resource`目录的内容进行变量替换等工作后，复制到项目输出的主classpath目录中
5. generate-resources
6. process-resources
7. compile：编译项目的主源代码。一般来说，是编译`src/main/java`目录下的Java文件至项目输出的主classpath目录中
8. process-classes
9. generate-test-sources
10. process-test-soures：处理项目测试资源文件。一般来说，是对`src/test/resources`目录的内容进行变量替换等工作后，复制到项目输出的测试classpath目录中
11. generate-test-resources
12. process-test-resources
13. test-compile：编译项目的测试代码。一般来说，是编译`src/test/java`目录下的Java文件至项目输出的测试classpath目录中
14. process-test-classes
15. test：使用单元测试框架运行测试，测试代码不会被打包或部署
16. prepare-package
17. package：接受编译好的代码，打包成可发布的格式，如JAR
18. pre-integration-test
19. integration-test
20. post-integration-test
21. verify
22. install：将包安装到Maven本地仓库，供本地其他Maven项目时候用
23. deploy：将最终的包复制到远程仓库，供其他开发人员和Maven项目使用

#### site生命周期

site生命周期的目的是建立和发布项目站点，Maven能够基于POM所包含的信息，自动生成一个友好的站点，方便团队交流和发布项目信息。该生命周期包含如下阶段：

1. pre-site：执行一些在生成项目站点之前需要完成的工作
2. site：生成项目站点文档
3. post-site：执行一些在生成项目站点之后需要完成的工作
4. site-deploy：将生成的项目站点发布到服务器上

### 插件

一个Maven插件是一个单个或者多个目标(goal)的集合。一个目标是明确的任务，它可以作为单独的目标运行，或者作为一个大的构建的

Maven的核心仅仅定义了抽象的生命周期，具体的任务是交由插件完成的，插件以独立的构件形式存在。

#### 插件绑定

Maven的生命周期与插件相互绑定，用以完成实际的构建任务。具体而言，是生命周期的阶段与插件的目标相互绑定，以完成某个具体的构建任务。

#### 插件解析机制

与依赖构件一样，插件构件同样基于坐标存储在Maven仓库中。在需要的时候，Maven会从本地仓库寻找插件，如果不存在，则从远程仓库查找。找到插件之后，再下载到本地仓库使用。

## 聚合与继承

### 聚合

### 继承

#### 可继承的POM元素

- groupId
- version
- description：项目的描述信息
- organization：项目的组织信息
- distributionManagementt：项目的部署配置
- scm：项目的版本控制系统信息
- properties：自定义的Maven属性
- dependencies：项目的依赖配置
- dependencyManagement：项目的依赖管理配置
- repositories：项目的仓库配置
- build：包含项目的源码配置、输出目录配置、插件配置、插件管理配置等

注：只罗列出部分元素，后期需要则添加【TODO】

#### 依赖管理

Maven提供的dependencyManagement元素既能让子模块继承到父模块的依赖配置，又能保证子模块依赖使用的灵活性。在dependencyManagement元素下的依赖声明不会引入实际的依赖，不过它能约束dependencies下的依赖使用。

优势：在父POM中使用dependencyManagement声明依赖能够统一项目范围中依赖的版本，当依赖版本在父POM声明之后，子模块在使用依赖的时候就无需声明版本，也就不会发生多个子模块使用依赖版本不一致的情况，这可以帮助降低依赖冲突的几率。

如果子模块不声明依赖的使用，即使该依赖已经在父POM的dependencyManagement中声明了，也不会产生任何实际的效果

在依赖范围中有个`import`依赖范围，这个依赖范围只在dependencyManagement元素下有效果，使用该范围的依赖通常指向一个POM，作用是将目标POM中的dependencyManagement配置导入并合并到当前POM的dependencyManagement配置中。

### 聚合和继承的关系

多模块Maven项目中的聚合与继承其实是两个概念，其目的完全是不同的。前者主要是为了方便快速的构建项目，后者主要是为了消除重复配置。

对于聚合模块来说，它知道有哪些被聚合的模块，但那些被聚合的模块不知道这个聚合模块的存在

对于继承关系的父POM来说，它不知道有哪些子模块继承于它，但那些子模块都必须知道自己的父POM是什么。

在现有的实际项目中，很多项目会有一个POM既是聚合POM，又是父POM，这样做主要是为了方便。一般来说，融合使用聚合与继承也没有什么问题。

## 其他

### Maven属性

Maven的属性分为6类，分别为：

- 内置属性：主要有两个常用内置属性--`${basedir}`表示项目根目录，即包含`pom.xml`文件的目录；`${version}`表示项目版本
- POM属性：用户可以使用该类属性引用POM文件中对应元素的值。如`${project.artifactId}`就对应了`<project>`、`<artifactId>`元素的值，常用的POM属性包括：
    - `${project.build.sourceDirectory}`:项目的主源码目录，默认为`src/main/java`
    - `${project.build.testSourceDirectory}`:项目的测试源码目录，默认为`src/test/java`
    - `${project.build.directory}`:项目构建输出目录，默认为`target/`
    - `${project.outputDirectory}`:项目主代码变意思输出目录，默认为`target/classes`
    - `${project.testOutputDirectory}`:项目测试代码编译输出目录，默认为`target/test-classes/`
    - `${project.groupId}`:项目的groupId
    - `${project.artifactId}`:项目的artifactId
    - `${project.version}`:项目的version，与`${version}`等价
    - `${project.build.finalName}`:项目打包输出文件的名称，默认为`${project.artifactId}-${project.version}`
    这些属性都对应了一个POM元素，它们中一些属性的默认值都是在超级POM中定义的
- 自定义属性：用户可以自己在POM的`<properties>`元素自定义Maven属性，然后在POM的其他地方使用`${属性名称}`的方式引用该属性，这种做法的最大意义在于消除重复。
- Settings属性：与POM属性同理，用户使用已`settings`开头的属性引用`settings.xml`文件中XML元素的值，如常用的`${settings.localRepository}`指向用户本地仓库的地址
- Java系统属性：所有Java系统属性都可以使用Maven属性引用，如`${user.home}`指向了用户目录
- 环境变量属性：所有环境变量都可以使用以`env.`开头的Maven属性引用，如`${env.JAVA_HOME}`指代了JAVA_HOME环境变量的值

### Maven profile【代码组-构建组织项目，TODO】

为了能让构建在各个环境下方便地配置，Maven引入了profile的概念，profile能够在构建的时候修改POM的一个子集，或者添加额外的配置元素。用户可以使用很多方式激活profile，以实现构建在不同环境下的移植。

#### profile 简介

profile可以定义一系列的配置信息，然后指定其激活条件。这样就可以定义多个profile，然后每个profile对应不同的激活条件和配置信息，从而达到不同环境使用不同配置信息的效果。比如说，可以通过profile定义在jdk1.5以上使用一套配置信息，在jdk1.5以下使用另外一套配置信息；或者有时候可以通过操作系统的不同来使用不同的配置信息，比如windows下是一套信息，linux下又是另外一套信息，等等。

#### profile 的定义位置

对于使用Maven3，有多个地方定义profile。定义的地方不同，它的作用范围也不同：

- 针对于特定项目的profile配置，可以定义在该项目的pom.xml中。
- 针对于特定用户的profile配置，可以在用户的settings.xml文件中定义profile。该文件在用户仓库目录下的“.m2”目录下。
- 全局的profile配置。全局的profile是定义在Maven安装目录下的“conf/settings.xml”文件中的。