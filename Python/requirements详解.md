python项目中必须包含一个`requirements.txt`文件，用于记录所有依赖包及其精确的版本号。以便新环境部署。

## 生成文件内容

```sh
pip freeze > requirements.txt
```

安装或升级更新后，需要及时更新这个文件

## 使用文件安装依赖

```sh
pip install -r requirements.txt