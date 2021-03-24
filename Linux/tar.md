# tar 命令

Linux tar（英文全拼：tape archive ）命令用于备份文件。

tar 是用来建立，还原备份文件的工具程序，它可以加入，解开备份文件内的文件。

## 语法

```bash
tar 
    [-ABcdgGhiklmMoOpPrRsStuUvwWxzZ]
    [-b <区块数目>]
    [-C <目的目录>]
    [-f <备份文件>]
    [-F <Script文件>]
    [-K <文件>]
    [-L <媒体容量>]
    [-N <日期时间>]
    [-T <范本文件>]
    [-V <卷册名称>]
    [-X <范本文件>]
    [-<设备编号><存储密度>]
    [--after-date=<日期时间>]
    [--atime-preserve]
    [--backuup=<备份方式>]
    [--checkpoint]
    [--concatenate]
    [--confirmation]
    [--delete]
    [--exclude=<范本样式>]
    [--force-local]
    [--group=<群组名称>]
    [--help]
    [--ignore-failed-read]
    [--new-volume-script=<Script文件>]
    [--newer-mtime]
    [--no-recursion]
    [--null]
    [--numeric-owner]
    [--owner=<用户名称>]
    [--posix]
    [--erve]
    [--preserve-order]
    [--preserve-permissions]
    [--record-size=<区块数目>]
    [--recursive-unlink]
    [--remove-files]
    [--rsh-command=<执行指令>]
    [--same-owner]
    [--suffix=<备份字尾字符串>]
    [--totals]
    [--use-compress-program=<执行指令>]
    [--version]
    [--volno-file=<编号文件>]
[文件或目录...]
```

**参数说明**：

- `-A` 或 `--catenate` 新增文件到已存在的备份文件。
-b<区块数目>或--blocking-factor=<区块数目> 设置每笔记录的区块数目，每个区块大小为12Bytes。
-B或--read-full-records 读取数据时重设区块大小。
-c或--create 建立新的备份文件。
- `-C <目的目录>` 或 `--directory=<目的目录>` 切换到指定的目录。
-d或--diff或--compare 对比备份文件内和文件系统上的文件的差异。
- `-f<备份文件>` 或 `--file=<备份文件>` 指定备份文件。
-F<Script文件>或--info-script=<Script文件> 每次更换磁带时，就执行指定的Script文件。
-g或--listed-incremental 处理GNU格式的大量备份。
-G或--incremental 处理旧的GNU格式的大量备份。
-h或--dereference 不建立符号连接，直接复制该连接所指向的原始文件。
-i或--ignore-zeros 忽略备份文件中的0 Byte区块，也就是EOF。
-k或--keep-old-files 解开备份文件时，不覆盖已有的文件。
-K<文件>或--starting-file=<文件> 从指定的文件开始还原。
-l或--one-file-system 复制的文件或目录存放的文件系统，必须与tar指令执行时所处的文件系统相同，否则不予复制。
-L<媒体容量>或-tape-length=<媒体容量> 设置存放每体的容量，单位以1024 Bytes计算。
-m或--modification-time 还原文件时，不变更文件的更改时间。
-M或--multi-volume 在建立，还原备份文件或列出其中的内容时，采用多卷册模式。
-N<日期格式>或--newer=<日期时间> 只将较指定日期更新的文件保存到备份文件里。
-o或--old-archive或--portability 将资料写入备份文件时使用V7格式。
-O或--stdout 把从备份文件里还原的文件输出到标准输出设备。
-p或--same-permissions 用原来的文件权限还原文件。
-P或--absolute-names 文件名使用绝对名称，不移除文件名称前的"/"号。
-r或--append 新增文件到已存在的备份文件的结尾部分。
-R或--block-number 列出每个信息在备份文件中的区块编号。
-s或--same-order 还原文件的顺序和备份文件内的存放顺序相同。
-S或--sparse 倘若一个文件内含大量的连续0字节，则将此文件存成稀疏文件。
-t或--list 列出备份文件的内容。
-T<范本文件>或--files-from=<范本文件> 指定范本文件，其内含有一个或多个范本样式，让tar解开或建立符合设置条件的文件。
-u或--update 仅置换较备份文件内的文件更新的文件。
-U或--unlink-first 解开压缩文件还原文件之前，先解除文件的连接。
- `-v` 或 `--verbose` 显示指令执行过程。
-V<卷册名称>或--label=<卷册名称> 建立使用指定的卷册名称的备份文件。
-w或--interactive 遭遇问题时先询问用户。
-W或--verify 写入备份文件后，确认文件正确无误。
- `-x` 或 `--extract` 或 `--get` 从备份文件中还原文件。
-X<范本文件>或--exclude-from=<范本文件> 指定范本文件，其内含有一个或多个范本样式，让ar排除符合设置条件的文件。
- `-z` 或 `--gzip` 或 `--ungzip` 通过gzip指令处理备份文件。
-Z或--compress或--uncompress 通过compress指令处理备份文件。
-<设备编号><存储密度> 设置备份用的外围设备编号及存放数据的密度。
--after-date=<日期时间> 此参数的效果和指定"-N"参数相同。
--atime-preserve 不变更文件的存取时间。
--backup=<备份方式>或--backup 移除文件前先进行备份。
--checkpoint 读取备份文件时列出目录名称。
--concatenate 此参数的效果和指定"-A"参数相同。
--confirmation 此参数的效果和指定"-w"参数相同。
--delete 从备份文件中删除指定的文件。
--exclude=<范本样式> 排除符合范本样式的文件。
--group=<群组名称> 把加入设备文件中的文件的所属群组设成指定的群组。
--help 在线帮助。
--ignore-failed-read 忽略数据读取错误，不中断程序的执行。
--new-volume-script=<Script文件> 此参数的效果和指定"-F"参数相同。
--newer-mtime 只保存更改过的文件。
--no-recursion 不做递归处理，也就是指定目录下的所有文件及子目录不予处理。
--null 从null设备读取文件名称。
--numeric-owner 以用户识别码及群组识别码取代用户名称和群组名称。
--owner=<用户名称> 把加入备份文件中的文件的拥有者设成指定的用户。
--posix 将数据写入备份文件时使用POSIX格式。
--preserve 此参数的效果和指定"-ps"参数相同。
--preserve-order 此参数的效果和指定"-A"参数相同。
--preserve-permissions 此参数的效果和指定"-p"参数相同。
--record-size=<区块数目> 此参数的效果和指定"-b"参数相同。
--recursive-unlink 解开压缩文件还原目录之前，先解除整个目录下所有文件的连接。
--remove-files 文件加入备份文件后，就将其删除。
--rsh-command=<执行指令> 设置要在远端主机上执行的指令，以取代rsh指令。
--same-owner 尝试以相同的文件拥有者还原文件。
--suffix=<备份字尾字符串> 移除文件前先行备份。
--totals 备份文件建立后，列出文件大小。
--use-compress-program=<执行指令> 通过指定的指令处理备份文件。
--version 显示版本信息。
--volno-file=<编号文件> 使用指定文件内的编号取代预设的卷册编号。

## 示例

**压缩文件**

```bash
touch a.c
#压缩 a.c文件为test.tar.gz
tar -czvf test.tar.gz a.c
```

**列出压缩文件内容**

```bash
tar -tzvf test.tar.gz
```

**解压文件**

```bash
tar -xzvf test.tar.gz
```