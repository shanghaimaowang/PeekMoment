# PeekMoment 动态统计小工具

软件工程 第五次实验（配置管理）使用的 C 语言示例项目，用于演示 Git 本地库管理、
分支合并、多人协作与冲突解决。

## 目录结构

```
.
├── include/moment.h      # 数据结构与接口声明
├── src/main.c            # 程序入口
├── src/moment.c          # 统计逻辑实现
├── data/moments.txt      # 示例数据：主题 与 点赞数
├── Makefile
└── README.md
```

## 编译与运行

```sh
make
./moment_stats data/moments.txt
```

输出示例：

```
moments      : 5
total likes  : 93
peak likes   : 35
```

## 说明

- 语言标准：C99
- 编译参数：`-Wall -Wextra`，要求无警告编译通过
- 作者：凌彦斌 2024270115
