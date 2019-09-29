---
layout: post
title: "docker信号机制以及dumb-init的使用"
subtitle: "docker sig and the usage of dumb-init"
date: 2019-09-29 13:19:00
author: "hello2mao"
hidden: true
tags:
  - cloud native
---

1. 容器中部署的时候往往都是直接运行二进制文件或命令，这样对于容器的作用更加直观，但是也会出现新的问题，比如子进程的资源回收、释放、托管等，处理不好，便会成为可怕的僵尸进程.
2. docker 引擎会向容器中 1 号进程发送信号，如果你的 1 号进程具备处理子进程各种状态的能力，那完全可以直接启动（比如 nginx 会处理它的 worker 进程）；否则就需要使用像 dumb-init 之类的来充当 1 号进程
3. e.g.

```
FROM xxx

...

RUN wget https://github.com/Yelp/dumb-init/releases/download/v1.2.2/dumb-init_1.2.2_amd64.deb
RUN dpkg -i dumb-init_*.deb

...

ENTRYPOINT ["/usr/bin/dumb-init", "--", "./entrypoint.sh"]
```
