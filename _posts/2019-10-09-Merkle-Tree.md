---
layout: post
title: "[WIP]Merkle树"
subtitle: "Merkle Tree"
date: 2019-10-09 10:24:00
author: "hello2mao"
hidden: true
tags:
  - algorithm
---

<!-- TOC -->

- [1. 概念](#1-%e6%a6%82%e5%bf%b5)
- [2. 检索](#2-%e6%a3%80%e7%b4%a2)
- [3. Merkle Tree 的应用](#3-merkle-tree-%e7%9a%84%e5%ba%94%e7%94%a8)
  - [3.1. P2P 网络](#31-p2p-%e7%bd%91%e7%bb%9c)
  - [3.2. Trusted Computing](#32-trusted-computing)
  - [3.3. IPFS](#33-ipfs)
  - [3.4. BitCoin 和 Ethereum](#34-bitcoin-%e5%92%8c-ethereum)
  - [3.5. MPT(Merkle Patricia Trees)](#35-mptmerkle-patricia-trees)
- [4. Ref](#4-ref)

<!-- /TOC -->

# 1. 概念

Merkle 树的叶子是数据块的 hash 值。非叶节点是其对应子节点串联字符串的 hash。

![](/img/posts/merkle-tree.png)

# 2. 检索

我们假设有 A 和 B 两台机器，A 需要与 B 相同目录下有 8 个文件，文件分别是 f1 f2 f3 ....f8。这个时候我们就可以通过 Merkle Tree 来进行快速比较。假设我们在文件创建的时候每个机器都构建了一个 Merkle Tree。具体如下图:

![](/img/posts/mt-search.jpeg)

从上图可得知，叶子节点 node7 的 value = hash(f1),是 f1 文件的 HASH;而其父亲节点 node3 的 value = hash(v7, v8)，也就是其子节点 node7 node8 的值得 HASH。就是这样表示一个层级运算关系。root 节点的 value 其实是所有叶子节点的 value 的唯一特征。

假如 A 上的文件 5 与 B 上的不一样。我们怎么通过两个机器的 merkle treee 信息找到不相同的文件? 这个比较检索过程如下:

1. 首先比较 v0 是否相同,如果不同，检索其孩子 node1 和 node2.
1. v1 相同，v2 不同。检索 node2 的孩子 node5 node6;
1. v5 不同，v6 相同，检索比较 node5 的孩子 node 11 和 node 12
1. v11 不同，v12 相同。node 11 为叶子节点，获取其目录信息。
1. 检索比较完毕。

以上过程的理论复杂度是 Log(N)。

# 3. Merkle Tree 的应用

## 3.1. P2P 网络

在 P2P 网络中，Merkle Tree 用来确保从其他节点接受的数据块没有损坏且没有被替换，甚至检查其他节点不会欺骗或者发布虚假的块。

在 p2p 网络下载网络之前，先从可信的源获得文件的 Merkle Tree 树根。一旦获得了树根，就可以从其他从不可信的源获取 Merkle tree。通过可信的树根来检查接受到的 Merkle Tree。如果 Merkle Tree 是损坏的或者虚假的，就从其他源获得另一个 Merkle Tree，直到获得一个与可信树根匹配的 Merkle Tree。

## 3.2. Trusted Computing

可信计算是可信计算组为分布式计算环境中参与节点的计算平台提供端点可信性而提出的。可信计算技术在计算平台的硬件层引入可信平台模块(Trusted Platform，TPM)，实际上为计算平台提供了基于硬件的可信根(Root of trust，RoT)。从可信根出发，使用信任链传递机制，可信计算技术可对本地平台的硬件及软件实施逐层的完整性度量，并将度量结果可靠地保存再 TPM 的平台配置寄存器(Platform configuration register，PCR)中，此后远程计算平台可通过远程验证机制(Remote Attestation)比对本地 PCR 中度量结果，从而验证本地计算平台的可信性。可信计算技术让分布式应用的参与节点摆脱了对中心服务器的依赖，而直接通过用户机器上的 TPM 芯片来建立信任，使得创建扩展性更好、可靠性更高、可用性更强的安全分布式应用成为可能。可信计算技术的核心机制是远程验证(remote attestation),分布式应用的参与结点正是通过远程验证机制来建立互信,从而保障应用的安全。

基于 Merkle Tree 的远程验证机制:
![](/img/posts/mt-tc.png)

## 3.3. IPFS

## 3.4. BitCoin 和 Ethereum

## 3.5. MPT(Merkle Patricia Trees)

# 4. Ref

- [Merkle Tree 学习](https://www.cnblogs.com/fengzhiwu/p/5524324.html)
