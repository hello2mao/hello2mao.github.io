---
layout: post
title: "IPFS节点对外入口"
date: 2020-04-22
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->

- [一、启动日志](#一启动日志)
- [二、端口分析](#二端口分析)

<!-- /TOC -->

# 一、启动日志

```
Initializing daemon...
go-ipfs version: 0.4.23-
Repo version: 7
System version: amd64/darwin
Golang version: go1.13.7
Swarm is limited to private network of peers with the swarm key
Swarm key fingerprint: dd62abb4462606033aab086cbfac6270
Swarm listening on /ip4/127.0.0.1/tcp/4001
Swarm listening on /ip4/172.18.27.54/tcp/4001
Swarm listening on /ip6/::1/tcp/4001
Swarm listening on /p2p-circuit
Swarm announcing /ip4/127.0.0.1/tcp/4001
Swarm announcing /ip4/172.18.27.54/tcp/4001
Swarm announcing /ip6/::1/tcp/4001
API server listening on /ip4/127.0.0.1/tcp/5001
WebUI: http://127.0.0.1:5001/webui
Gateway (readonly) server listening on /ip4/127.0.0.1/tcp/8080
Daemon is ready
```

# 二、端口分析
|  端口号 | 用途 |安全性|
| :--| :--|:--|
| 4001 |  （1）主要端口，进行p2p连接和同步数据<br>（2）swarm address<br>（3）tcp | （1）私有网络：需要共享秘钥，安全 <br>（2）公共网络：安全|
| 5001 | （1）ipfs daemon的api端口<br>（2）管理API的入口，也可以读写数据 |（1）目前没有鉴权逻辑，能控制ipfs daemon的配置，不安全|
| 8080 |（1）ipfs gateway端口<br>（2）可以用作读取ipfs节点上数据的入口  |（1）默认只读<br>（2）对于公有ipfs是安全的<br>（3）对于私有ipfs会暴露读数据接口|
