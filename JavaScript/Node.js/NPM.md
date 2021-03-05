<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-28 16:02:29
 * @LastEditTime : 2021-01-28 16:02:29
 * @Copyright 2021 SUPERZHC
-->
# NPM

NPM 是 Node.js 的包管理工具。当 Node.js 安装完成后，也默认将 NPM 安装完成。

## 使用

**验证是否安装**

```bash
npm
```

**查看安装版本**

```bash
npm -v 或 npm version
```

**查看帮助**

```bash
npm help 或 npm h
```

**安装模块**

```bash
npm install <Module Name>
```

在安装包的时候同样可以在命令后添加 `--save` 或者 `-S` 参数，这样安装包的信息将会记录在 `package.json` 文件的 dependencies 字段中，这样将可以很方便地管理包的依赖关系。

如果这个包只是开发阶段需要的，可以继续添加 `-dev` 参数。这样安装包的信息将会记录在 `package.json` 文件的 devDependencies 字段中。

建议将所有项目安装的包都记录在 `package.json` 文件中。当 `package.json` 文件中有了依赖包的记录时，只需要运行 `npm install` 命令，系统就会自动安装所有项目需要的依赖包。

**在全局模块中安装模块**

```bash
# -g：启用global模式
npm install -g <Module Name>
```

**卸载模块**

```bash
npm uninstall <Module Name>
```

**显示当前目录下安装的模块**

```bash
npm list
```

## CNPM

> CNPM 是 NPM 在国内的镜像，可以提高下载速度。

安装 CNPM 的命令如下：

```bash
npm install -g cnpm --registry=https://registry.npm.taobao.org
```
