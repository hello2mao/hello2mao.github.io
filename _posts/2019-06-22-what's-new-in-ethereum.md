---
layout:     post
title:      "以太坊最新进展-20190622"
subtitle:   "what's new in ethereum"
date:       2019-06-22 10:51:11
author:     "hello2mao"
tags:
    - ethereum
---

## [Anonymous Zether: Technical Report](https://www.coindesk.com/jpmorgan-adds-new-privacy-features-to-its-ethereum-based-quorum-blockchain)

总部位于纽约的摩根大通透露，他们已经构建了Zether协议扩展，这是一种完全去中心化的加密协议，可以用于隐私支付，
而且还能与以太坊和其他智能合约平台兼容，旨在为交易增加一个“匿名层”。
据悉，摩根大通已经在今年5月28日开放了该扩展协议，而且会与其私有链Quorum一起使用。
在基本的Zether协议中，账户余额和转账账户信息都是被隐藏的，但参与者的身份并不会被隐藏。

摩根大通针还发布了针对匿名Zether的技术报告：Anonymous Zether: Technical Report（https://github.com/jpmorganchase/anonymous-zether/blob/master/docs/AnonZether.pdf）
匿名Zether是一个私有的数值追踪系统，在这个系统中，账户的余额都被加密存储在以太坊上。
每一个Zether合约在部署时都必须附属到一个已经存在的ERC20合约上，在Zether合约部署完成后，用户就可以匿名的进行ERC20代币的转账和取款。目前匿名Zether因为gas消耗问题还不能在主网使用。

## [A nearly-trivial-on-zero-inputs 32-bytes-long collision-resistant hash function](https://ethresear.ch/t/a-nearly-trivial-on-zero-inputs-32-bytes-long-collision-resistant-hash-function/5511)

提出了一种32位的抗碰撞的hash函数：H(l,r)=x；
- 如果l和r都不等于0，那个x=2^240+sha256(l,r)mod2^240
- 如果l和r都等于0，那么x=0
- 如果l>=2^255或r>=2^255或者l<2^240或者r<2^240，那么x=2^240+sha256(l,r)mod2^240

此hash函数用在M树时，有5倍复杂度的降低和4倍空间利用率的提高。

## [StarkDEX: Bringing STARKs to Ethereum](https://blog.0xproject.com/starkdex-bringing-starks-to-ethereum-6a03fffc0eb7)
Starkware和0x发布了一个使用STARKs技术的POC [demo](https://www.starkdex.io/)，
此demo目前运行在Ropsten测试网络上，使用STARKs技术后，TPS可以达到550，且每笔交易只需大约6000gas，节省了200倍的gas消耗。
STARKs（可扩容的透明知识论证）是创建一种证明的技术，这项证明中f(x)=y，其中f可能要花很长的时间来进行计算，但是这个证明可以被很快验证。STARK是“双重扩容”：对于一个需要t步骤的计算，这会花费大约O(t * log(t))步骤才能完成这个证明，这可能是最优的情况，而且这需要通过~O(log2(t))个步骤才能验证，对于中等大小的T值，它比原始计算快得多。

## [Work to natively integrate Eth1 into Eth2](https://ethresear.ch/t/work-to-natively-integrate-eth1-into-eth2/5573)
此文概括了把ETH1的共识以及世界状态集成进ETH2分片内的可能工作量。
一共列举了23个点，包括：BLS VS ECDSA；Keccak256 vs SHA256；SSZ vs RLP；轻客户端中使用POS的信标链代替POW；移除难度炸弹；使用libp2p代替devp2p；移除ETH1的gasLimit等。

## [Ethereum 2.0 Deposit Merkle Tree](https://medium.com/@josephdelong/ethereum-2-0-deposit-merkle-tree-13ec8404ca4f)
本文介绍了ETH 2.0中存款树使用的数据结构Sparse Merkle Tree，简称SMT，它是默克尔数的变种，详见：https://eprint.iacr.org/2016/683.pdf。
因为SMT在计算根hash的过程能够显著的减少内存占用，ETH 2.0的存款树Deposite Tree选择使用SMT。

## [Formal Verification of Ethereum 2.0 Deposit Contract](https://runtimeverification.com/blog/formal-verification-of-ethereum-2-0-deposit-contract-part-1/)
本文是对ETH 2.0存款合约的形式化验证。

在ETH 2.0的信标链上，验证者是动态加入和退出的，他们只需向存款合约发起一笔交易存入一定数量的ETH就可以成为验证者，然后在退出验证者角色时，存款合约会把抵押退还。因为验证者的列表存在默克尔树上，在动态更新验证者时，默克尔树都需要重构，当验证者数量巨大时，这将非常耗时以及消耗内存空间。
为了减少时间和空间的消耗，同时节省gas，存款合约实现了一种增量默克尔树算法，详见：https://github.com/ethereum/research/blob/master/beacon_chain_impl/progressive_merkle_tree.py。
在重构一棵高度是h的默克尔树时，增量默克尔树算法具有O(h)的时间和空间复杂度。