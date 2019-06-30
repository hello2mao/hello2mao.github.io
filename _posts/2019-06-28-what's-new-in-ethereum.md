---
layout:     post
title:      "以太坊最新进展-20190628"
subtitle:   "what's new in ethereum"
date:       2019-06-28 10:51:11
author:     "hello2mao"
tags:
    - ethereum
---

## [What's New in Eth2](https://notes.ethereum.org/c/Sk8Zs--CQ/https%3A%2F%2Fbenjaminion.xyz%2Fnewineth2%2F20190621.html)
本文是对最近几个月Ethereum 2.0进度的综述。

#### Phase 0：信标链
Phase 0 的Spec会在6月30日定稿，详见报道：[Code For Ethereum’s Proof-of-Stake Blockchain to Be Finalized Next Month](https://www.coindesk.com/code-for-ethereums-proof-of-stake-blockchain-to-be-finalized-next-month)

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
    - 支付一定费用，用户可以部署他们自己的EE到主链上；ETH2分片只关注底层架构：交易排序和数据
 
 ## [V神最新演讲：以太坊2.0之跨分片交易](https://mp.weixin.qq.com/s/luxI17CINlpJCFwmHJ4_Lg)  
 
 2019年6月29日，第二届以太坊技术及应用大会在京举行，以太坊创始人 Vitalik Buterin 分享了题为《以太坊2.0之跨分片交易》的主题演讲。
 讲述了ETH2.0设计：
 
 - 1024个分片
 - 信标链管理共识和跨分片通讯
 - 每6分钟一次跨分片通讯  
 - 更快的异步交易
 - 猛拉（yank）机制
 - 通过plasma的方式做同步交易
 