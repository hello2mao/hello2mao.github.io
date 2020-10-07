---
layout: post
title: "图解Fabric--账本结构"
date: 2020-04-23
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->

- [一、账本](#一账本)
- [二、世界状态](#二世界状态)
- [三、区块链](#三区块链)
  - [3.1 区块](#31-区块)
  - [3.2 交易](#32-交易)

<!-- /TOC -->

# 一、账本
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200423132255855.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
Fabric账本有两部分组成：

 1. 世界状态：存储在DB中，可以认为是当前区块链的一个快照，方便查找。
 2. 区块链：由区块组成的链，区块内是交易。

# 二、世界状态
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200423132617663.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
# 三、区块链
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200423132626985.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
## 3.1 区块
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200423132809230.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
## 3.2 交易
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200423132819266.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
