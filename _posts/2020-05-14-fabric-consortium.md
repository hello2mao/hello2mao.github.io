---
layout: post
title: "图解Fabric混合组网架构-Case1"
date: 2020-05-14
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->


<!-- /TOC -->

Fabric的优势就是联盟链，企业间（或者是云平台间）在构建联盟链的时候，必然需要对Fabric各个模块间的通讯链路有所了解，从而可以给内部机器间开好正确的访问白名单、给对外的防火墙配置正确的策略。

下图是组网的一种情形：

- Peer组织Org1在企业1内部
- Peer组织Org2在企业2内部
- **Orderer组织只在企业1内部部署**

Peer和Orderer内部的grpc服务已经标注在模块内部：

- Peer：Endorser、Deliver、Discovery、Gossip
- Orderer：AtomicBroadcast、Cluster

grpc服务详细说明可参考：

- [图解Fabric各模块间grpc通讯情况](https://hello2mao.blog.csdn.net/article/details/106081431)
- [Hyperledger Fabric 2.0 gRPC接口](https://blog.csdn.net/DAOSHUXINDAN/article/details/104668870)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200514185016539.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)