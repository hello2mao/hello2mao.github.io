---
layout: post
title: "比特币的UTXO"
subtitle: "Bitcoin UTXO"
date: 2019-09-30 16:33:00
author: "hello2mao"
hidden: true
tags:
  - blockchain
---


UTXO（Unspent Transaction Output）在比特币中类似于以太坊中的账户模型。

- 在比特币中，一笔交易的每一条输入和输出实际上都是 UTXO，输入 UTXO 就是以前交易剩下的， 更准确的说是以前交易的输出 UTXO。
- 除了 coinbase 交易（挖矿奖励）没有输入 UTXO 之外，其它交易都有输入和输出，都可以为多个。
- 比特币永远不会合并一个地址里的 UTXO，每一个 UTXO 都是可以追踪的
- 如果要花费某个地址中的余额，实际上可能会花费多个 UTXO

举例：

![](/img/posts/utxo-eg.png)

上图中有 3 笔交易：

- 第一笔交易来自挖矿，没有输入，输出为 Alice 地址，可以简单理解为一个 UTXO
- 第二笔交易是 Alice 转账给 Bob，输入是 Alice 的地址，输出为 Bob 地址和 Alice 地址，因为有余额
- 第三笔交易是 Bob 转账给 Lily，和上一笔交易类似
