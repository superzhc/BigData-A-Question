## 创建环境

```sh
# 下面是创建python=3.6版本的环境，取名叫py36
conda create -n py36 python=3.6 
```

## 删除环境

```sh
conda remove -n py36 --all
```

## 激活环境

```sh
source activate py36

# conda4
conda activate py36
```

## 退出环境

```sh
source deactivate

# conda4
conda deactivate
```