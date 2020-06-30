[TOC]

## 简介

Kettle 是一款纯 Java 开发的 ETL 工具，用于数据库间的数据迁移。它是跨平台的，所以它可以在 Window、Linux、Unix 上运行。有图形界面，也有命令脚本还可以二次开发。

kettle 的官网是 <https://community.hitachivantara.com/docs/DOC-1009855>，github 地址是 <https://github.com/pentaho/pentaho-kettle>。

## 安装

## 启动

双击 `Spoon.bat` 就能启动 Kettle。

## 任务（`.kjb`）与转换（`.ktr`）

一个转换就是一个 ETL 的过程，而作业则是多个转换、作业的集合，在作业中可以对转换或作业进行调度、定时任务等。

### 转换

转换包括一个或多个步骤，步骤之间通过 hop 来连接。定义了一个单向通道，允许数据从一个步骤流向另一个步骤。

**在 Kettle 中，数据的单位是行，数据流就是数据行从一个步骤到另一个步骤的移动**。

#### [Kettle转换示例](Kettle转换示例.md) 

#### [Kettle转换的核心对象](Kettle转换的核心对象.md)

#### 日志

日志里面记录了一些运行信息，其中有几个比较关键的输出信息：

- `I`:表示从表中读取了多少条数据
- `O`:表示向目标表中写入了多少条数据
- `R`:从之前的步骤中读取了多少条数据
- `W`:向下一个步骤写入了多少条数据

### 作业

#### [Kettle作业示例](Kettle作业示例.md)

## FAQ

[FAQ](Kettle的FAQ.md)

