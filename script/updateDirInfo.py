# -*- coding: UTF-8 -*-

import os

ignores_file = ["_sidebar.md", "_navbar.md", "BlogDir.md", "README.md",
                "sw.js", ".nojekyll", ".gitignore", "index.html"]

ignores_dir = [".git", "image", "images", "Epoint"]

ignores_full_path = []


def traverse_dir(path, tab=0):
    # print(path)
    str_tab = "   "*tab
    s = ""
    files = os.listdir(path)

    if tab > 0:  # root目录不作任何判断
        # 判断文件中是否存在README.md，直接放到顶层中
        superior = path.split("/")[-2]  # 上层目录的名称
        superior_tab = "   "*(tab-1)
        if "README.md" in files:
            s = "%s- [%s](%sREADME.md)\n" % (superior_tab, superior, path[2:])
        else:
            s = "%s- %s\n" % (superior_tab, superior)

    for file in files:
        full_path = path+file

        # 判断忽略的路径
        if full_path in ignores_full_path:
            continue

        if not os.path.isdir(full_path):  # 判断是否是文件夹
            if file in ignores_file:
                continue

            # s = s+"   "*tab+file+"\n"
            infos = os.path.splitext(file)
            if infos[-1] == ".md":
                # fix bug：如果markdown文件中有空格要做一步处理，将空格转换成 %20
                deal_file_path = file.replace(" ", "%20")
                record = "%s- [%s](%s)\n" % (str_tab,
                                             infos[0], path[2:]+deal_file_path)
                s = s+record
        else:
            if file in ignores_dir:
                continue

            # record = "%s- %s\n" % (str_tab, file)
            # s = s+record
            s = s+traverse_dir(path+file+"/", tab+1)
    return s


if __name__ == "__main__":
    root = "./"
    ret = traverse_dir(root, 0)
    # print(ret)
    with open(root+"BlogDir.md", "w", encoding="utf-8") as f:
        f.write(ret)
