<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2021-01-19 10:19:21
 * @LastEditTime : 2021-01-19 10:35:02
 * @Copyright 2021 SUPERZHC
-->
# GitBook 制作电子书

## 安装 `gitbook-cli`

```bash
npm install -g gitbook-cli
```

安装成功之后，再执行 `gitbook -V` 命令确认是否安装成功：

```bash
gitbook -V
CLI version: 2.3.2
GitBook version: 3.2.3
```

## 使用

### 项目初始化

在当前目录下，执行如下命令：

```bash
gitbook init
```

此时，项目下会自动生成如下两个文件：

- `README.md`：书籍的简介放在这个文件里；
- `SUMMARY.md`：书籍的目录结构在这里配置。

### 本地预览

```bash
gitbook serve
```

执行上方命令后，工具会对项目里的 Markdown 格式的文件进行转换，默认转换为 html 格式，最后提示 `Serving book on http://localhost:4000`，打开该地址进行预览。

### 导出电子书

**生成 PDF 格式的电子书**

```bash
gitbook pdf . ./mybook.pdf
```

**生成 epub 格式的电子书**

```bash
gitbook epub . ./mybook.pdf
```

**生成 mobi 格式的电子书**

```bash
gitbook mobi . ./mybook.pdf
```