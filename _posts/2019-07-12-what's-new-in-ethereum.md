---
layout:     post
title:      "以太坊最新进展-20190712"
subtitle:   "what's new in ethereum"
date:       2019-07-12 10:51:11
author:     "hello2mao"
tags:
    - ethereum
---

## [Overview of Layer 2 approaches: Plasma, State Channels, Side Chains, Roll Ups](https://nearprotocol.com/blog/layer-2/)
本文是对Layer 2方案的概览，讲述了Plasma、状态通道、侧链以及Roll Up，深入探究了这些方案各自的技术细节和优缺点。

#### Plasma
Plasma 是一种可以实现 “无监管” 侧链的技术，换言之，即使侧链（通常被称为 “plasma 链”）上所有验证者串谋起来作恶，plasma 链上的资产也是安全的，而且可以退回主链。

Plasma 最大的优点是存储在 plasma 链上的代币安全性很高。缺点在于，在转移代币之时必须提供该代币的完整历史，另外就是退出机制非常复杂（以及相关的推论过程）。

#### 状态通道
支付通道是状态通道的一个具体实例，一旦参与者中有一方想要停止使用支付通道，可以执行 “退出” 操作。

这种 “退出” 模式存在一个问题，即主链无法验证支付通道是否提交了全部交易，也就是说，在提交了状态更新之后是否不再出现新的状态更新。

#### 侧链
侧链的核心思路是构建一条完全独立的区块链，有自己的验证者和运营者，可以与主链互相转移资产，而且会选择性地将区块头的快照发送至主链，从而防止分叉产生。

虽然侧链可以利用主链的安全性来防止分叉，但是验证者依然可以通过串谋来发动另一种叫做 无效状态转换 的攻击。
如果有超过 50% 或 66%（取决于侧链的架构）的验证者串谋的话，他们可以创建一个完全无效的区块，窃取其他参与者的资产，并将这个区块的快照发送至主链，发起并完成一个“退出”交易，就可以成功偷走这些资产。

#### Roll Up
Roll Up 实际上是一条侧链，因此它会生成区块，并且将这些区块的快照发送到主链上。不过，Roll Up 上的运营者是无需信任的。

此L2方案没有显著扩大调用数据中的存储空间，但是实际消耗的可写存储量是恒定的（而且非常小），链上验证的 gas 成本低至 1k gas/tx ，是主链交易的 1/21 。

## [Geth v1.9.0: Six months distilled](https://blog.ethereum.org/2019/07/10/geth-v1-9-0/) 
geth v1.9.0 刚刚发布了。该版本包含了大量的更新。
- 性能得到全面提高。LevelDB 的 GO 语言实现，账户/存储树（trie）的访问模式，EVM 代码的分析和优化、数据库模式的分析和优化，这些是全方位的性能提升。
- 快速同步（Fast Sync）、完全同步（Full Sync）、Archive 节点同步很大优化。
- Freezer（冷藏室）
  数据库现在分为两部分：
  1. 最近的区块和状态保存在 LevelDB 的快速键值存储中。适合 SSD。
  2. 3 个轮次（epoch）以前的区块和收据被移动到定制的冷藏室数据库中，不会被频繁访问。适合 HDD。
- GraphQL
  引入了一种全新的基于 GraphQL 的节点查询接口。使用 GraphQL，用户可以只查询他们需要的数据，同时仍保持计算和数据传输的开销最小化。
- 额外的硬件钱包支持，支持 @Ledger Nano X，支持更新的 @Trezor One
- Geth 现在支持 status keycard
- Clef: 一个独立的交易签名器。
- 轻客户端的硬编码检查点如今被链上预言机所取代。轻客户端现在可以向轻服务器请求检查点，轻客户端会负责询问链上预言机获得最新检查点。
- Geth 对其所有子系统和事件的监控都得到了重塑和升级。
- 在新的发现协议准备就绪并推出之前，geth v1.9.0 向旧的发现协议提供了两个 ENR 扩展：
  1.可以同时支持两种版本的 IP 协议
  2.节点现在可以公布其链的配置，以增加可发现性

## [Blockchain search engine — real-time data for decentralized applications (DApps)](https://blog.secondstate.io/post/20190703-search-engine-overview/)
本文设计了一个区块链搜索引擎，用来对以太坊上的智能合约进行搜索。
此搜索引擎是开源的，见[smart-contract-search-engine](https://github.com/second-state/smart-contract-search-engine)，能够根据关键词、address等进行搜索。目前已经能够对953556个公链上的智能合约进行搜索。

试用地址：https://ethereum.search.secondstate.io/

