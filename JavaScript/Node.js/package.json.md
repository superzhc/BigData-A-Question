<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-28 16:38:47
 * @LastEditTime : 2021-01-29 16:54:59
 * @Copyright 2021 SUPERZHC
-->
# package.json

在 Node.js 中，一个包是一个文件夹，文件夹中的 `package.json` 文件以 json 格式存储该包的相关描述。

通过 `npm init` 命令可以生成一个 `package.json` 文件。这个文件是整个项目的描述文件。通过这个文件可以清楚地知道项目的包依赖关系、版本、作者等信息。

> 注：使用 `npm init` 生成 `package.json` 文件需要填写一些信息，如果不想填写这些内容，也可以在这条命令后参数 `-y` 或者 `--yes`，这样系统将会使用默认值生成package.json文件，例如：
> ```bash
> npm init -y
> // or
> npm init --yes
> ```

| 字段            | 描述                                                                                                                                                                                                 |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| name            | 包名。规范定义它需要由小写的字母和数字组成，可以包含 `.`、`_` 和 `-`，但不允许出现空格。包名必须是唯一的，以免对外公布时产生重名冲突的误解。                                                         |
| description     | 包简介                                                                                                                                                                                               |
| version         | 版本号                                                                                                                                                                                               |
| author          | 包作者                                                                                                                                                                                               |
| main            | 模块引入方法 `require()` 在引入包时，会优先检查这个字段，并将其作为包中其余模块的入口。如果不存在这个字段，`require()` 方法会查找包目录下的 `index.js`、`index.node`、`index.json`文件作为默认入口。 |
| dependencies    | 使用当前包所需要依赖的包列表。这个属性十分重要，NPM会通过这个属性帮助自动加载依赖的包。                                                                                                              |
| devDependencies | 一些模块只在开发时需要依赖。配置这个属性，可以提示包的后续开发者安装依赖包。                                                                                                                         |
| bin             | 包可以作为命令行工具使用。配置好bin字段后，通过 `npm install <package_name -g>`命令可以将脚本添加到执行路径中，之后可以在命令行中直接执行。                                                          |
| keywords        | 关键词数组，NPM中主要用来做分类搜索                                                                                                                                                                  |
| homepage        | 当前包的网站地址                                                                                                                                                                                     |
| maintainers     | 包维护者列表。每个维护者由name、email和web这3个属性组成                                                                                                                                              |
| contributors    | 贡献者列表                                                                                                                                                                                           |
| bugs            | 一个可以反馈bug的网页地址或邮件地址                                                                                                                                                                  |
| licenses        | 当前包所使用的许可证列表，表示这个包可以在哪些许可证下使用                                                                                                                                           |
| repositories    | 托管源代码的位置列表，表明可以通过哪些方式和地址访问包的源代码                                                                                                                                       |
| os              | 操作系统支持列表。这些操作系统的取值包括aix、freebsd、linux、macos、solaris、vxworks、windows。如果设置了列表为空，则不对操作系统做任何假设。                                                        |
| cpu             | CPU架构的支持列表，有效的架构名称有arm、mips、ppc、sparc、x86和x86_64。同os一样，如果列表为空，则不对CPU架构做任何假设。                                                                             |
| engine          | 支持的JavaScript引擎列表，有效的引擎取值包括ejs、flusspferd、gpsee、jsc、spidermonkey、narwhal、node和v8。                                                                                           |
| builtin         | 标志当前包是否是内建在底层系统的标准组件                                                                                                                                                             |
| directories     | 包目录说明                                                                                                                                                                                           |
| implements      | 实现规范的列表。标志当前包实现了CommonJS的哪些规范。                                                                                                                                                 |
| scripts         | 脚本说明对象。它主要被包管理器用来安装、编译、测试和卸载包                                                                                                                                           |

