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

## Facebook Libra is Architecturally Unsound

原文链接：http://www.stephendiehl.com/posts/libra.html

本文从纯技术角度，分析了Libra架构设计的不合理之处。

- Libra 在许可网络上采用拜占庭容错设计的作法不合逻辑：共识算法带来的根本就是废热式的开销，只会限制系统的整体吞吐量。
- Libra 缺乏交易隐私
- Libra HotStuff BFT 无法支撑支付通道所需要的吞吐量
- Libra 的 Move 语言怕是靠不住
- Libra 的密码学工程尚不完善
- Libra 项目缺少消费者保护机制

## It’s Not That Difficult

原文链接：https://medium.com/@tjayrush/its-not-that-difficult-33a428c3c2c3

本文对以太坊难度炸弹的爆发和拆除进行了详细的分析。包括如下几个方面：

- 原始难度数据
- 每个区块的难度变化
- 难度的相对变化
- 拆除炸弹的更佳方式
- 如何更好地重置定时的难度炸弹

## leverage blockchain to secure the data in 5G telecommunication networks

原文链接：https://www.coindesk.com/cisco-patent-would-secure-5g-networks-with-a-blockchain

网络技术巨头思科（Cisco）获得了一项专利，详细说明了它将如何利用区块链技术来保护 5G 电信网络中的数据。

区块链平台旨在管理设备用户（如电话或笔记本电脑）与虚拟网络之间的数据会话。也就是说，该项新技术可以通过区块链网络接口管理网络和连接设备之间的部分数据交换。该公司描述了如何使用区块链平台来支持网络层，这是一种能够实现多个独立虚拟化网络更有效地在同一个物理基础设施中运行的架构。

根据专利文件显示，这种面向服务的体系结构支持网络层，使用了一组隔离的可编程资源集，通过相应网络层内的软件程序实现单独的网络功能和应用服务，而不干扰共存网络层上的其他功能和服务。


## Three Ways to Prepare for Mainnet

原文链接：https://developers.libra.org/blog/2019/10/29/a-guide-to-running-libra-validators

本文讲述了如何去运行一个Libra Validator节点，以及作为一个Validator节点，在主网到来前，需要做的准备及建议。

这些建议包括：

- 准备额外的数据目录：因为区块链的数据是与日俱增的，可以通过node-config的dir选项指定。
- 监控：推荐使用Prometheus进行性能监控
- 安全的保存私钥：Libra Validator节点会有三组私钥，分别是共识私钥、网络身份私钥、网络签名私钥。

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