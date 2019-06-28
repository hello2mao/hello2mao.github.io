---
layout:     post
title:      "以太坊最新进展-20190628"
subtitle:   "what's new in ethereum"
date:       2019-06-28 10:51:11
author:     "hello2mao"
tags:
    - ethereum
---

## [What's New in Eth2 - 21 June 2019](https://notes.ethereum.org/c/Sk8Zs--CQ/https%3A%2F%2Fbenjaminion.xyz%2Fnewineth2%2F20190621.html)
本文是对最近几个月Ethereum 2.0进度的综述。

#### Phase 0：信标链
Phase 0 的Spec会在6月30日定稿，详见报道[Code For Ethereum’s Proof-of-Stake Blockchain to Be Finalized Next Month](https://www.coindesk.com/code-for-ethereums-proof-of-stake-blockchain-to-be-finalized-next-month)

- 互用性
    - 当前有八个团队分别实现了ETH2客户端
    - 这个几个客户端需严格按照Spec进行通讯
- 网络
    - ETH2使用[libp2p](https://github.com/libp2p)作为底层p2p通信协议
- 测试
    - 测试进度见：https://www.youtube.com/watch?v=4V-WQ2CnRfA
    - 当前的Spec是可以直接执行，方便进行跨客户端的测试
- 形式化验证
    - ETH2的存款合约的设计报告见：https://github.com/runtimeverification/verified-smart-contracts/blob/master/deposit/formal-incremental-merkle-tree-algorithm.pdf
- 信标链上线
    - 时间点：DevCon V；把ETH2的存款合约部署到ETH1上
    - 时间点：2020年1月；发布信标链上线计划
    
#### Phase 1：分片
发布了Spec：https://github.com/ethereum/eth2.0-specs/blob/dev/specs/core/1_shard-data-chains.md

#### Phase 2：虚拟机，即执行层
- 有了两个初步的提案：
    - [Phase One and Done: eth2 as a data availability engine](https://ethresear.ch/t/phase-one-and-done-eth2-as-a-data-availability-engine/5269?u=benjaminion) 
    - [Phase 2 Proposal 1](https://notes.ethereum.org/s/HylpjAWsE#)
- Phase 2初步方案：
    - 有很多类型的EVM，称之为EE（execution environment），每个EE能够运行eWASM合约
    - EE不存储状态，所有的数据都需要额外提供
    - 支付一定费用户，用户可以部署他们自己的EE到主链上；ETH2分片只关注底层架构：交易排序和数据
 
 
       