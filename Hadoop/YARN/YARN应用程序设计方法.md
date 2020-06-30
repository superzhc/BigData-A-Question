应用程序（Application）是用户编写的处理数据的程序的统称，它从YARN中申请资源以完成自己的计算任务。YARN自身对应用程序类型没有任何限制，它可以是处理短类型任务的MapReduce作业，也可以是部署长时间运行的服务的应用程序。

~~由于YARN应用程序编写比较复杂，且需对YARN本身的架构有一定了解，因此通常由专业开发人员开发，通过回调的形式供其他普通用户使用。~~

## 概述

YARN是一个资源管理系统，负责集群资源的管理和调度。如果想要将一个新的应用程序运行在YARN之上，通常需要编写两个组件**Client（客户端）**和**ApplicationMaster**。***这两个组件编写非常复杂**，尤其ApplicationMaster，需要考虑RPC调用、任务容错等细节*。如果大量应用程序可抽象成一种通用框架，只需实现一个客户端和一个ApplicationMaster，然后让所有应用程序重用这两个组件即可。比如MapReduce是一种通用的计算框架，YARN已经为其实现了一个直接可以使用的客户端（JobClient）和ApplicationMaster（MRAppMaster）。

用户自定义编写的应用程序或新的框架运行于YARN之上，需要编写客户端和ApplicationMaster两个组件完成该功能，其中，**客户端负责向ResourceManager提交ApplicationMaster，并查询应用程序运行状态**；**ApplicationMaster负责向ResourceManager申请资源（以Container形式表示），并与NodeManager通信以启动各个Container**，此外，**ApplicationMaster还负责监控各个任务运行状态，并在失败时为其重新申请资源**。

通常而言，编写一个YARN Application会涉及3个RPC协议，如下图所示，分别为：

- ApplicationClientProtocol（用于Client与ResourceManager之间）。Client通过该协议可实现将应用程序提交到ResourceManager上、查询应用程序的运行状态或者杀死应用程序等功能
- ApplicationMasterProtocol（用于ApplicationMaster与ResourceManager之间）。ApplicationMaster使用该协议向ResourceManager注册、申请资源、获取各个任务运行情况等
- ContainerManagementProtocol（用于ApplicationMaster与NodeManager之间）。ApplicationMaster使用该协议要求NodeManager启动/撤销Container或者查询Container的运行状态

![](https://gitee.com/superzchao/GraphBed/raw/master/publish/2019/Yarn/应用程序设计相关的通信协议.png)

## 客户端设计

YARN Application客户端的主要作用是**提供一系列访问接口供用户与YARN交互**，包括*提交Application*、*查询Application运行状态*，*修改Application属性（如优先级）*等。其中，最重要的访问接口之一是提交Application的函数。

## ApplicationMaster设计

ApplicationMaster（AM）需要与ResourceManager（RM）和NodeManager（NM）两个服务交互，通过与ResourceManager交互，ApplicationMaster可获得任务计算所需的资源；通过与NodeManager交互，ApplicationMaster可启动计算任务（container），并监控它直到运行完成。