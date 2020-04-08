#!/bin/bash

# 获取进程下的线程
# 1）ps -Lfp <pid>
# 2）ps -mp <pid> -o THREAD, tid, time
# 3）top -Hp <pid>

# sort 参数是根据线程占用的 CPU 比例进行排序的
# ps -mp <pid> -o THREAD,tid,time | sort -k2r
ps -mp $1 -o THREAD,tid,time | sort -k2r