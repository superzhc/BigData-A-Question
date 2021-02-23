<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-02-05 16:55:39
 * @LastEditTime : 2021-02-05 16:56:47
 * @Copyright 2021 SUPERZHC
-->
# Kafka 安全机制

## 简介

自0.9.0.0.版本引入Security之后，Kafka一直在完善security的功能。当前Kafka security主要包含3大功能：**认证(authentication)**、**信道加密(encryption)** 和 **授权(authorization)**

信道加密就是为client到broker、broker到broker以及客户端与broker之间的数据传输配置SSL；认证机制主要是指配置SASL，而授权是通过ACL接口命令来完成的。

## Kafka 的认证机制

2.3.0版本的Kafka支持多种认证机制，如：

- SSL
- SASL-Kerberos
- SASL-PLAIN
- SASL-SCRAM

## Kafka 的认证范围

Kafka的认证范围包含如下：

- Client与broker之间
- broker与broker之间
- broker与Zookeeper之间