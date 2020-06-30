Sqoop1 和 Sqoop2 是完全不兼容的，其具体的版本号区别为 `1.4.x` 为 Sqoop1，`1.9.x` 为 Sqoop2。

Sqoop1 和 Sqoop2 在架构和用法上已经完全不同。在架构上，Sqoop1 仅仅使用一个 Sqoop 客户端；Sqoop2 引入了 sqoopserver，对 connector 实现了集中的管理，其访问方式也变得多样化了，可以通过 RESTAPI、JavaAPI、WebUI 以及 CLI 控制台方式进行访问。另一方面在安全性能方面也有一定的改善，在 Sqoop1 中使用脚本基本都是通过显式指定数据库的用户名和密码的，安全性做的不是太完善；在 Sqoop2 中，如果是通过 CLI 方式访问的话，会有一个交互式过程界面，这样密码会更安全，同时 Sqoop2 引入了基于角色的安全机制。

**Sqoop1 架构图**：

![img](../images/7240015-8eb2bedb021b8c68.jpg)

**Sqoop2 架构图**：

![img](../images/7240015-07634c957e34c5a1.jpg)

**Sqoop1优缺点**：

优点：架构部署简单

缺点：命令行方式容易出错，格式紧耦合，无法支持所有数据类型，安全机制不够完善，例如密码暴露，安装需要 root 权限，connector 必须符合 JDBC 模型

**Sqoop2优缺点**：

优点：多种交互方式，connector 集中化管理，完善的权限管理机制

缺点：架构稍复杂，配置部署更繁琐

