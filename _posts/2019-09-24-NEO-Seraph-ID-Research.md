---
layout: post
title: "NEO（小蚁）Seraph ID调研"
subtitle: "NEO Seraph ID Research"
date: 2019-09-24 17:51:11
author: "hello2mao"
header-img: "img/posts/NEO-Seraph-ID.jpg"
tags:
    - blockchain
---

<!-- TOC -->

- [1. 概述](#1-%e6%a6%82%e8%bf%b0)
- [2. 解析](#2-%e8%a7%a3%e6%9e%90)
  - [2.1. 工作流程](#21-%e5%b7%a5%e4%bd%9c%e6%b5%81%e7%a8%8b)
  - [2.2. 密码学相关](#22-%e5%af%86%e7%a0%81%e5%ad%a6%e7%9b%b8%e5%85%b3)
- [3. 参考](#3-%e5%8f%82%e8%80%83)

<!-- /TOC -->

# 1. 概述

近日，据外媒报道，NEO 携手瑞士最大的电信运营商瑞士电信（Swisscom）的子公司 Swisscom Blockchain，合作推出 Seraph ID 新技术——一个基于 NEO 底层技术的自主身份（SSI）框架。

![](/img/posts/seraph-id-main.png)

报道详见：https://cryptoinfos.eu/decentralized-digital-identity-solution-neo-swisscom-blockchain/  
NEO DevCon 2019 视频介绍：https://www.youtube.com/watch?time_continue=17432&v=DjSSvE7OmOI

# 2. 解析

这个 Seraph ID 实际是 NEO 的 DID 实现方案，遵循的是 W3C 的 DID 标准：https://w3c-ccg.github.io/did-spec/

## 2.1. 工作流程

以租房为例举例此 DID 的工作流程如下：

![](/img/posts/seraphid-demo-workflow.png)

## 2.2. 密码学相关

使用了如下密码学相关：

1. Encryption algorithm  
   • ECC: secp256r1
2. Signing algorithm  
   • ECDSA
3. Hash algorithm  
   • RIPEMD160
   • SHA256
   • Murmur3
   • Scrypt

# 3. 参考

-   [Seraph ID: Introducing Swisscom Blockchain’s digital identity solution on NEO](https://neonewstoday.com/general/seraph-id-introducing-swisscom-blockchains-digital-identity-solution-on-neo/)
-   [seraphid.io](https://www.seraphid.io/)
-   [white_paper](https://www.seraphid.io/assets/files/white_paper.pdf)
