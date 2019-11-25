---
layout: post
title: "以太坊世界状态"
subtitle: "Ethereum world state"
date: 2019-05-30 10:51:11
author: "hello2mao"
tags:
  - blockchain
---

<!-- TOC -->

- [世界状态](#%e4%b8%96%e7%95%8c%e7%8a%b6%e6%80%81)
- [数据](#%e6%95%b0%e6%8d%ae)

<!-- /TOC -->

# 世界状态
像账户余额这样的数据并不直接保存在以太坊区块链的区块中。区块中只保存交易树、状态树和收据树的根节点哈希值。
![image](https://user-images.githubusercontent.com/8265961/55318029-54778700-54a4-11e9-8a27-675cc8d09a75.png)

**状态前缀树-- 以太坊中有且只有一个全局状态前缀树**
![image](https://user-images.githubusercontent.com/8265961/56782400-8c5eb980-6819-11e9-9a62-a316fa4eb9fa.png)

**存储前缀树 -- 智能合约数据的存储**
存储前缀树是智能合约数据存储的位置。每一个以太坊账户都有自己的存储前缀树。在全局状态前缀树中保存着存储前缀树根节点的 256 位哈希 storageRoot 值。
![image](https://user-images.githubusercontent.com/8265961/55318307-129b1080-54a5-11e9-8376-5758d4628474.png)

# 数据
![image](https://user-images.githubusercontent.com/8265961/56782763-1fe4ba00-681b-11e9-9a98-e3d9c5f24d66.png)