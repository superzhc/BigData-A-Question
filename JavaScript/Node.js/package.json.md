<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-28 16:38:47
 * @LastEditTime : 2021-01-28 16:49:02
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

