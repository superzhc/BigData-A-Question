<!--
 * @Github       : https://github.com/superzhc/BigData-A-Question
 * @Author       : SUPERZHC
 * @CreateDate   : 2018-11-30 14:53:21
 * @LastEditTime : 2021-01-25 14:31:54
 * @Copyright 2020 SUPERZHC
-->
- [Git](#git)
  - [简介](#简介)
  - [安装](#安装)
  - [工作区](#工作区)
  - [版本库](#版本库)
  - [暂存区](#暂存区)
  - [远程仓库](#远程仓库)
    - [GitHub](#github)
  - [版本控制命令](#版本控制命令)
    - [`git init`](#git-init)
    - [`git add`](#git-add)
    - [`git commit`](#git-commit)
    - [`git status` 查看仓库状态](#git-status-查看仓库状态)
    - [`git diff`](#git-diff)
    - [`git log` 查看历史纪录](#git-log-查看历史纪录)
    - [`git reset`](#git-reset)
    - [`git reflog` 记录每次提交命令](#git-reflog-记录每次提交命令)
    - [`git checkout`](#git-checkout)
    - [`git rm` 删除文件](#git-rm-删除文件)
    - [`git clone`](#git-clone)
    - [`git remote`](#git-remote)
    - [`git pull`](#git-pull)
    - [`git push`](#git-push)
    - [`git branch`](#git-branch)
    - [`git merge`](#git-merge)
    - [`git stash`](#git-stash)
  - [分支管理](#分支管理)
    - [创建和合并分支](#创建和合并分支)
    - [解决冲突](#解决冲突)
    - [分支管理策略](#分支管理策略)
    - [多人协作](#多人协作)

# Git

## 简介

Git 是目前世界上最先进的分布式版本控制系统。

- **集中式和分布式版本控制系统的区别**

集中式版本控制系统，版本库是存储在中央服务器的，使用的时候要从中央服务器取得最新的版本，进行了操作要将这些操作推送给中央服务器。

![集中式版本控制系统](../images/Git_集中式.jpg)

集中式版本控制系统最大的问题就是必须联网才能工作，如果在局域网内还好，带宽够大，速度够快，可如果在互联网上，遇到网速慢的话，可能提交一个 10M 的文件就需要 5 分钟。

分布式版本控制系统，它根本没有"中央服务器"，每个人的电脑上都是一个**完整的版本库**，*它自身就可以进行版本上的维护*，这样，工作的时候就不需要联网了，因为版本库就在自己的电脑上。因为版本库在每个人的电脑上，每个版本库都可以拉取和推送数据给其他版本库来完成协作工作。比方说你在自己的电脑上修改了文件 A，你的同事也在他的电脑上修改了文件 A，这时，你们俩之间只需把各自的修改推送给对方，就可以互相看到对方的修改了。

![分布式版本控制系统](../images/Git_分布式.jpg)

和集中式版本控制系统相比，分布式版本控制系统的安全性要高很多，因为每个人电脑里都有完整的版本库，某一个人的电脑坏掉了不要紧，随便从其他人那里复制一个就可以了。而集中式版本控制系统的中央服务器要是出了问题，所有人都没法使用了。

在实际使用分布式版本控制系统的时候，其实很少在两人之间的电脑上推送版本库的修改，因为可能你们俩不在一个局域网内，两台电脑互相访问不了，也可能今天你的同事病了，他的电脑压根没有开机。因此，分布式版本控制系统通常也有一台充当“中央服务器”的电脑，但这个服务器的作用仅仅是用来方便“交换”大家的修改，没有它大家也一样干活，只是交换修改不方便而已。

- **其他版本控制系统**

CVS 作为最早的开源而且免费的集中式版本控制系统，直到现在还有不少人在用。由于 CVS 自身设计的问题，会造成提交文件不完整，版本库莫名其妙损坏的情况。同样是开源而且免费的 SVN 修正了 CVS 的一些稳定性问题，是目前用得最多的集中式版本库控制系统。

除了免费的外，还有收费的集中式版本控制系统，比如 IBM 的 ClearCase（以前是 Rational 公司的，被 IBM 收购了），特点是安装比 Windows 还大，运行比蜗牛还慢，能用 ClearCase 的一般是世界 500 强，他们有个共同的特点是财大气粗。

微软自己也有一个集中式版本控制系统叫 VSS，集成在 Visual Studio 中。由于其反人类的设计，连微软自己都不好意思用了。

分布式版本控制系统除了 Git 以及促使 Git 诞生的 BitKeeper 外，还有类似 Git 的 Mercurial 和 Bazaar 等。这些分布式版本控制系统各有特点，但最快、最简单也最流行的依然是 Git。

## 安装

下载地址：<http://git-scm.com/downloads>

- **Linux 上安装**

1. 查看系统是否安装 Git，直接输入命令 `git`
2. 如果没有，进行 git 安装
   - 直接安装 `apt-get install git` 或 `yum install -y git`
   - 通过源码安装，官网下载源码，然后解压，依次输入：
     1. `./config`
     2. `make`
     3. `sudo make install`

- **Windows 上安装**

在 Windows 上使用 Git，可以从 Git 官网直接下载安装程序，然后按默认选项安装即可。安装完成后，在菜单中找到 "Git" -> "Git Bash"，打开窗口就说明 Git 安装成功了。

- **配置**

因为 Git 是分布式系统，所以每个机器都必须提供用户名和 Email 地址。

- 命令行配置
    ```sh
    git config --global user.name "[用户名称]"
    git config --global user.email "[用户邮箱]"
    # 注意：`git config`命令的`--global`参数，用了这个参数，表示你这台机器上所有的Git仓库都会使用这个配置，当然也可以对某个仓库指定不同的用户名和Email地址。
    ```
- 文件配置（Windows 下一般在 C 盘用户目录下`.gitconfig`文件），文件如下：
    ```properties
    [user]
        name = [用户名称]
        email = [用户邮箱]
    [credential]
        #helper = Advanced
        helper = store
    [http]
        proxy = http://127.0.0.1:1080
    ```

## 工作区

工作区：Working Directory

电脑可见的目录都是工作区。

## 版本库

版本库，又被称为仓库（repository），可以简单的理解为一个目录，这个目录里面的所有文件都可以被 Git 管理起来，每个文件的修改、删除，Git 都能跟踪，以便任何时刻都可以追踪历史，或者在将来某个时刻可以“还原”。

在工作区中有一个隐藏目录 `.git`，这个不算工作区，而是 Git 的版本库。

- **创建版本库**

1. 在一个目录下，创建一个空目录【注：Windows 系统下，为了避免遇到各种莫名其妙的问题，确保目录名（包括父目录）中不包含中文】
2. 通过 `git init` 命令把这个目录变成 Git 可以管理的仓库

注：创建好的版本仓库下会有一个 `.git` 目录，这个目录是 Git 用来跟踪管理版本库的。

- **把文件添加到版本库**

首先明确一下，所有的版本控制系统，其实只能跟踪文本文件的改动，比如 TXT 文件，网页，所有的程序代码等等，Git 也不例外。版本控制系统可以记录每次的改动，比如在第 5 行加了一个单词 “Linux”，在第 8 行删了一个单词 “Windows”。而图片、视频这些二进制文件，虽然也能由版本控制系统管理，但没法跟踪文件的变化，只能把二进制文件每次改动串起来，也就是只知道图片从 100KB 改成了 120KB，但到底改了啥，版本控制系统不知道，也没法知道。

注：Microsoft 的 Word 格式也是二进制格式，因此，版本控制系统是没法跟踪 Word 文件的改动的。

添加文件到 Git 仓库，分两步：

1. 使用命令 `git add <file>`，注意，可反复多次使用，添加多个文件；
2. 使用命令 `git commit -m <message>`，完成。

## 暂存区

Git 的版本库里存了很多东西，其中最重要的就是称为 `stage`（或者叫 `index`）的**暂存区**，还有 Git 为我们自动创建的第一个分支 `master`，以及指向 `master` 的一个指针叫 `HEAD`。

![暂存区](../images/Git_stage.jpg)

## 远程仓库

Git 是分布式版本控制系统，同一个 Git 仓库，可以分布到不同的机器上。因为最早肯定只有一台机器有一个原始版本库，此后，别的机器可以“克隆”这个原始版本库，而且每台机器的版本库其实都是一样的，并没有主次之分。

### GitHub

GitHub 是提供 Git 仓库托管服务的，所以只要注册一个 GitHub 账号，就可以免费获得 Git 远程仓库。

因*本地 Git 仓库和 GitHub 仓库之间的传输是通过 SSH 加密*的，所以要进行如下设置：
1. **创建SSH Key**。在用户主目录下，看看有没有`.ssh`目录，如果有，再看看这个目录下有没有 `id_rsa` 和 `id_rsa.pub` 这两个文件，如果已经有了，可直接跳到下一步。如果没有，打开 Shell（Windows 下打开 Git Bash），创建 SSH Key：`$ ssh-keygen -t rsa -C "youremail@example.com"`，把邮件地址换成你自己的邮件地址，然后一路回车，使用默认值即可，由于这个 Key 也不是用于军事目的，所以也无需设置密码。成功后可以在用户主目录里找到 `.ssh` 目录，里面有 `id_rsa` 和 `id_rsa.pub` 两个文件，这两个就是 SSH Key 的秘钥对，`id_rsa` 是私钥，不能泄露出去，`id_rsa.pub` 是公钥，可以放心地告诉任何人。
2. 登陆 GitHub=>"Account settings"=>"SSH Keys" 页面=>点击 "Add SSH Key"=>填上任意 Title，在 Key 文本框里粘贴 `id_rsa.pub` 文件的内容，点击 "Add Key"

- **为什么GitHub需要SSH Key**

因为 GitHub 需要识别出推送的提交是你自己推送的，而不是别人冒充的，而 Git 支持 SSH 协议，所以 GitHub 只要知道了你的公钥，就可以确认只有你自己才能推送。

注：GitHub 允许你添加多个 Key。

## 版本控制命令

### `git init`

初始化一个Git仓库

### `git add`

将修改文件添加到暂存区

注：支持 Ant 风格添加修改

### `git commit`

格式：`git commit -m <comment>`

将暂存区的所有内容提交到当前分支，后面添加上有意义的注释信息。

### `git status` 查看仓库状态

`git status` 命令可以查看仓库当前的状态。通过 `git status` 命令会输出用户对文件的操作情况。

### `git diff` 

格式：`git diff file`

在 file 被修改了还未提交的时候查看修改的部分，显示的格式是 Unix 通用的 diff 格式。

注：diff 比对的是工作区和版本库最新版本的区别

### `git log` 查看历史纪录

查看 git 的 commit 信息，每次提交的信息包括注释在内，从最新提交到最久提交。

参数：
- `--pretty=oneline`：将 commit 信息简化成一行显示
- `--graph`:查看图形化日志

### `git reset`

`git reset` 命令既可以回退版本，也可以把暂存区的修改回退到工作区。

- **回退版本**

要进行版本回退，首先 Git 必须知道当前版本是哪个版本，*在 Git 中，用 HEAD 表示当前版本，也就是最新的提交 `commit ID`，上一个版本就是 `HEAD^`，上上一个版本就是`HEAD^^`，当然往上 100 个版本写 100 个 `^` 比较容易数不过来，所以写成 `HEAD~100`*。

用法：

```bash
git reset --hard HEAD^   #退回到相对于当前版本的上一个版本 HEAD 表示当前版本
git reset --hard HEAD^^  #退回到相对于当前版本的上上一个版本  HEAD 代表当前版本
git reset --hard HEAD~100 #退回到相对于当前版本的上 100 个版本去  HEAD 表示当前版本
git reset --hard 3628164 #退回到指定的版本   这里不需要全部写commit id  Git 会去自动适配,Git的版本回退速度非常快，因为Git在内部有个指向当前版本的HEAD指针，当你回退版本的时候，Git仅仅是把HEAD从指向commit ID
```

回退版本后，若需要还原回来，通过找到还原版本的 commit ID，就可以还原回来

Git 回退版本的过程：

Git 在内部有个指向当前版本的 HEAD 指针，当你回退版本的时候，Git 仅仅是把 HEAD 从指向 `append GPL` 改为指向 `add distributed`:

![git回退](../images/Git_reset1.jpg)

变更指向：

![git回退](../images/Git_reset2.jpg)

然后把工作区的文件更新下。所以 HEAD 指向哪个版本号，当前工作空间就处于那个版本。

也可以对单个文件进行版本回退的操作，如下所示：

```bash
git reset --hard HEAD^ test.txt
```

- **暂存区的修改回退到工作区**

格式：

```bash
git reset HEAD <file>
```

把暂存区的修改撤销掉（unstage），重新放回工作区。即，撤销先前 `git add` 的操作

### `git reflog` 记录每次提交命令

当需要从回退版本回到最新版本，可以使用 `git reflog` 来找到旧版本之前的提交日志的 commit Id。

### `git checkout`

- **撤销修改**

格式：

```bash
# 单个文件的修改
git checkout -- <file>
# 整个文件夹的修改
git checkout .
git checkout src/main/resources
```

把 file 在工作区的所有修改都撤销，这里有两种情况：
- 一种是 file 自修改后还没有被放到暂存区，现在，撤销修改就回到和版本库一模一样的状态；
- 一种是 file 已经添加到暂存区后，又做了修改，现在，撤销修改就回到添加到暂存区后的状态。

注：`git checkout -- <file> `命令中的 `--` 很重要，没有 `--`，就变成了“切换到另一个分支”的命令

- **切换分支**

格式：`git checkout <name>`

参数：
- `-b` :表示创建并切换，相当于以下两个命令
    ```bash
    git checkout -b dev
    # ||
    #\||/
    # \/
    git branch dev
    git checkout master
    ```

### `git rm` 删除文件

格式：`git rm <file>`

从 Git 版本库中删除文件(同时从文件系统中删除文件)

### `git clone`

将一个远程的仓库克隆到本地

### `git remote`

- **查看远程库信息**

格式：`git remote`

参数:
- `-v`:显示更详细的信息，会显示抓取和推送的 `origin` 的地址，如果没有推送权限是看不到push地址的。

- **添加远程仓库信息**

格式：`git remote add origin git@github.com:zhangsan/ylez.git`

添加一个远程仓库

参数解析：
- `add origin`:添加一个远程仓库
- `git@github.com:zhangsan/ylez.git`:远程仓库的地址
- `git@github.com`:主机地址
- `zhangsan`:用户名称
- `/ylez.git`:仓库名

- **解除远程仓库的关联**

```bash
git remote remove origin
```

### `git pull`

`git pull` 命令用于从另一个存储库或本地分支获取并集成(整合)。`git pull` 命令的作用是：取回远程主机某个分支的更新，再与本地的指定分支合并。

`git pull` 其实就是 `git fetch` 和 `git merge FETCH_HEAD` 的简写。

```bash
git pull [options] [<repository> [<refspec>…]]
# ==
git pull <远程主机名> <远程分支名>:<本地分支名>
```

**示例**

```bash
# 更新操作
git pull
git pull origin

# 将远程主机 origin 的 master 分支拉取过来，与本地的 brantest 分支合并
git pull origin master:brantest

# 如果远程分支是与当前分支合并，则冒号后面的部分可以省略
git pull origin master
```

### `git push`

把本地库的内容推送到远程

```bash
git push -u origin master #将本地的master分支推送到远程的master分支中
git push -u origin dev #本地切换到dev分支然后将本地的dev分支推送到远程
```

### `git branch`

- **查看分支**

格式：`git branch`

`git branch` 命令会列出所有分支，当前分支前面会标一个 `*` 号。

- **创建分支**

格式：`git branch <name>`

- **删除分支**

格式：`git branch -d <name>`

- **丢弃一个没有被合并过的分支**

格式：`git branch -D <name>`

强制删除一个没有被合并过的分支

### `git merge`

格式：`git merge <name>`

合并某分支到当前分支

### `git stash`

## 分支管理

### 创建和合并分支

在 Git 里，`master` 分支叫**主分支**。`HEAD` 严格来说不是指向提交，而是指向 `master`，`master` 才是指向提交的，所以 `HEAD` 指向的就是当前分支。

- **Git创建和合并分支过程**

1. 最开始 `master` 分支是一条线，Git 用 `master` 指向最新的提交，再用 `HEAD` 指向 `master`，就能确定当前分支，以及当前分支的提交点：
   
    ![Git分支使用](../images/Git_分支1.png)
    
    每次提交，`master` 分支都会向前移动一步，这样，随着不断提交，`master` 分支的线也越来越长。

2. 当创建新的分支，例如 `dev` 时，Git 新建了一个指针叫 `dev`，指向 `master` 相同的提交，再把 `HEAD` 指向 `dev`，就表示当前分支在 `dev` 上：
    
    ![Git分支使用](../images/Git_分支2.png)

3. 从现在开始，对工作区的修改和提交就是针对 `dev` 分支了，比如新提交一次后，`dev` 指针往前移动一步，而 `master` 指针不变：
    
    ![Git分支使用](../images/Git_分支3.png)

4. 假如在 `dev` 上的工作完成了，就可以把 `dev` 合并到 `master` 上。实现方式就是直接把 `master` 指向 `dev` 的当前提交，就完成了合并：
    
    ![Git分支使用](../images/Git_分支4.png)

5. 合并完分支后，甚至可以删除 `dev` 分支。删除 `dev` 分支就是把 `dev` 指针给删掉，删掉后就剩下了一条 `master` 分支：
    
    ![Git分支使用](../images/Git_分支5.png)

- **创建和合并分支实战**

- 创建 `dev` 分支，然后切换到 `dev` 分支:`git checkout -b dev`
- 合并到主分支上，首先切换到主分支:`git checkout master`，再进行合并:`git merge dev`
- 删除 `dev` 分支:`git branch -d dev`

### 解决冲突

当 Git 无法执行“快速合并”，只能试图把各自的修改合并起来，但这种合并就可能会有冲突；对于冲突的文件必须手动进行修改，直接查看冲突文件会发现 Git 用 `<<<<<<<`，`=======`，`>>>>>>>` 标记出不同分支的内容，对文件进行修改后再次提交，即可合并成功。

- **总结**

当 Git 无法自动合并分支时，就必须首先解决冲突。解决冲突后，再提交，合并完成。

解决冲突就是把 Git 合并失败的文件手动编辑为我们希望的内容，再提交。

### 分支管理策略

- `master` 分支是稳定版本，不用于开发，仅用来发布新版本
- `dev` 分支，用于开发，不是稳定版本；到某个时候，比如 1.0 版本发布时，再把 dev 分支合并到 master 上，在 master 分支发布 1.0 版本；

团队合作的分支一般如下：

![Git分支策略](../images/Git_分支策略.png)

### 多人协作

当从远程仓库克隆时，实际上 Git 自动把本地的 `master` 分支和远程的 `master` 分支对应起来了，并且远程仓库的默认名称是 `origin`。

- **推送分支**

推送分支，就是把该分支上的所有本地提交推送到远程库。推送时要指定本地分支，这样 svn 就会把该分支推送到远程库对应的远程分支上

```bash
git push origin master
```

如果要推送其他分支，如 `dev`，就改成：

```bash
git push origin dev
```

推送分支的规则：
- `master` 分支是主分支，因此要时刻与远程同步；
- `dev` 分支是开发分支，团队所有的成员都需要在上面工作，所以也需要与远程同步；
- `bug` 分支之用于在本地修复bug，不需要推送到远程
- `feature` 分支是否需要推送到远程，取决于是否需要协作开发

- **多人协作的工作模式**

1. 首先试图用 `git push origin <branch-name>` 推送修改；
2. 如果推送失败，则因为远程分支的版本比本地更新，需要先用 `git pull` 试图合并；
3. 如果合并有冲突，则解决冲突，并本地提交；
4. 没有冲突或者解决掉冲突后，再用 `git push origin <branch-name>` 推送就能成功

注：如果 `git pull` 提示 `no tracking information`，则说明本地分支和远程分支的链接关系没有创建，用命令 `git branch --set-upstream-to <branch-name> origin/<branch-name>`。