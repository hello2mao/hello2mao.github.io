---
layout: post
title: "区块链技术追踪 （二）"
subtitle: "what's new in blockchain （2）"
date: 2019-11-22 10:51:11
author: "hello2mao"
header-img: "img/posts/eye.png"
tags:
  - blockchain
---

## eth-2.0-roadmap

原文链接：https://medium.com/taipei-ethereum-meetup/eth2-0-roadmap-70e1c23f139f

本文是Crosslink 2019 Taiwan中以太坊基金会（Etherium Foundation, EF）的核心研究员 Danny Ryan的演讲，会中分享了以太坊 2.0 （Ethereum 2.0）目前的研究方向以及遇到的挑战，演讲的内容主要包含了以太坊 2.0 的架构，新的分片提案，执行环境（Execution Environments, EE）以及双向桥接（Two-Way Bridge）等议题。

（1）以太坊 2.0 的架构

- phase 0：beacon chain
- phase 1：shared chain
- phase 2：VM

（2）新的分片提案

以太坊2.0原提案所运作的机制，是以每个时期(Epoch)为单位，来进行交联的动作，每个链上有1024个片(Shards)，当需要跨分链交易(Tx)时，由于是每个时期进行交联，会有较大的延迟时间；新提案更新为每个时段都进行交联的动作，并减少片(Shards)的数量为32个，来降低跨分片( Cross-Shard)交易时的延迟时间，每个时段都进行跨分片交易。

（3）执行环境

支持多个执行环境。也可以有多个状态根，不同的帐户模型等。

（4）双向桥接

双向桥接目前可能的路线有两条，一种是在以太坊1.0 上面，建立以太坊2.0 的轻节点；另一种是在以太坊1.0 上运作以太坊2.0 的全节点。

## 以太坊伊斯坦布尔升级公告

原文链接：https://blog.ethereum.org/2019/11/20/ethereum-istanbul-upgrade-announcement/

以太坊网络将于区块号 906 9000 处激活一个计划好的升级；该块预计将于 2019 年 12 月 7 日，周六挖出。

（1）我是节点运营者、我是矿工，我需要做什么？

升级以太坊客户端即可

（2）要是我不升级自己的节点、不参与本次升级呢，会出现什么情况？

如果您的以太坊客户端没有升级到最新版本，您的客户端将只会安排分叉前的共识规则来同步区块链。

（3）伊斯坦布尔升级吸收了哪些改动？

在伊斯坦布尔升级中实现的规则改动都使用 EIP（以太坊升级提案）来定义的，其中包括6个不同的代码更改（EIP）。以太坊升级提案用于描述以太坊平台所用的标准，包括核心协议的技术详述、客户端 API，还有合约标准。