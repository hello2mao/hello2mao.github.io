---
layout: post
title: "数字货币确定性钱包"
subtitle: "Deterministic Wallet"
date: 2019-11-18 13:05:00
author: "hello2mao"
tags:
  - blockchain
---

<!-- TOC -->

- [1. 确定性钱包](#1-%e7%a1%ae%e5%ae%9a%e6%80%a7%e9%92%b1%e5%8c%85)
- [2. 助记词 (BIP-39)](#2-%e5%8a%a9%e8%ae%b0%e8%af%8d-bip-39)
- [3. 拓展公钥和私钥](#3-%e6%8b%93%e5%b1%95%e5%85%ac%e9%92%a5%e5%92%8c%e7%a7%81%e9%92%a5)
- [4. 硬化密钥派生](#4-%e7%a1%ac%e5%8c%96%e5%af%86%e9%92%a5%e6%b4%be%e7%94%9f)
- [5. HD 钱包密钥标识符（路径）](#5-hd-%e9%92%b1%e5%8c%85%e5%af%86%e9%92%a5%e6%a0%87%e8%af%86%e7%ac%a6%e8%b7%af%e5%be%84)
- [6. 从种子开始生成 HD 钱包](#6-%e4%bb%8e%e7%a7%8d%e5%ad%90%e5%bc%80%e5%a7%8b%e7%94%9f%e6%88%90-hd-%e9%92%b1%e5%8c%85)
- [7. 子密钥衍生函数(Child Key Derivation, CKD)](#7-%e5%ad%90%e5%af%86%e9%92%a5%e8%a1%8d%e7%94%9f%e5%87%bd%e6%95%b0child-key-derivation-ckd)
- [8. 扩展密钥(extended key)](#8-%e6%89%a9%e5%b1%95%e5%af%86%e9%92%a5extended-key)
- [9. HD Wallet 的分层密钥生成结构图](#9-hd-wallet-%e7%9a%84%e5%88%86%e5%b1%82%e5%af%86%e9%92%a5%e7%94%9f%e6%88%90%e7%bb%93%e6%9e%84%e5%9b%be)
- [10. 参考：](#10-%e5%8f%82%e8%80%83)

<!-- /TOC -->

# 1. 确定性钱包

`确定性钱包`是指那些所有的私钥都是通过一个普通的种子使用一个单向哈希函数延伸而来的钱包。

在一个确定性钱包中:

- 一个种子就足够恢复出所有派生的密钥，因此只需要在创建的时候做一次备份就可以了。
- 这个种子对于钱包来说也是可以导入导出的，可以让所有用户的密钥在各种不同的钱包之间进行简单的迁移。

`确定性钱包`最高级的实现就是由比特币的 BIP-32 标准定义的 `HD钱包`

HD 钱包包含的密钥来源于一个树形的结构，例如一个父密钥可以派生出一系列子密钥，每一个子密钥又可以派生出一系列孙子密钥，不停的循环往复，没有尽头。

![](/img/post/../posts/hd-wallet.png)

# 2. 助记词 (BIP-39)

BIP-39 标准定义了助记词编码和种子的生成过程。

整个过程分成了两个部分：第一步到第六步在生成助记词，第七步到第九步在助记词到种子。

详见：[助记词 (BIP-39)](https://github.com/xitu/gold-miner/blob/master/TODO1/ethereumbook-wallets.md#%E5%8A%A9%E8%AE%B0%E8%AF%8D-bip-39)

BIP-39 标准允许用户在生成种子的时候使用可选密码。

# 3. 拓展公钥和私钥

在 BIP-32 的术语中，一个父密钥可以拓展的生成「儿子」，这个儿子就是拓展密钥。
如果它是一个私钥，那么它就是一个拓展私钥，并通过前缀 xprv 来区分；一个拓展公钥通过前缀 xpub 来区分。

无论什么情况只要部署的服务和应用有一份拓展公钥并且没有私钥，那么这个快捷方式就可以创建非常安全的公钥。这种部署可以生成无穷个公钥和以太坊地址，但是却不能花费任何发送到这些地址的资金。同时，在另外一个更安全的服务器上，拓展私钥可以派生出所有相关的私钥用来给交易签名，并花费资金。

利用这种解决方案的一个常见的应用就是在一个 web 服务器上安装一个拓展公钥，来为电子商务应用服务。这个网页服务器可以使用公钥派生函数为每一笔交易（例如客户的购物车）创造出一个全新的以太坊地址。这个网页服务器没有任何私钥所以盗贼也无法窃取。不使用 HD 钱包的情况下，想做到这个程度唯一的方法就是在一个分割的安全服务器上生成上千个以太坊地址然后在电子商务服务器上预加载他们。这个方法低效笨重，并且需要经常的维护以确保电子商务服务器不会泄露密钥。

还有一个常见的应用就是冷存储和硬件钱包。在这种场景下，拓展私钥可以存储在硬件钱包中，但是拓展公钥可以放在线上。用户可以按照他们的意愿创建接收的地址，私钥则会离线安全的保存。想花掉里面的资金的话，用户可以在离线签名的以太坊客户端或者支持交易签名的硬件钱包上使用拓展私钥。

# 4. 硬化密钥派生

如果不想承受泄露你自己链码风险，并且还想要方便的使用 xpub 来派生出公钥分支，那么你应该通过硬化父辈来派生它，而不是一个正常的父辈。这其中的最佳实践就是，为了防止威胁到主要的密钥，主要密钥的 level-1 子辈总是通过硬化派生来派生。

# 5. HD 钱包密钥标识符（路径）

<table>
<caption>HD 钱包路径示例</caption>
<colgroup>
<col width="50%" />
<col width="50%" />
</colgroup>
<thead>
<tr class="header">
<th align="left">HD path</th>
<th align="left">Key described</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left"><p>m/0</p></td>
<td align="left"><p>主私钥 m 的第一个（0）儿子私钥</p></td>
</tr>
<tr class="even">
<td align="left"><p>m/0/0</p></td>
<td align="left"><p>第一个子辈（m/0）的第一个孙子私钥</p></td>
</tr>
<tr class="odd">
<td align="left"><p>m/0'/0</p></td>
<td align="left"><p>第一个<em>硬化</em>子辈 (m/0') 的第一个标准孙子</p></td>
</tr>
<tr class="even">
<td align="left"><p>m/1/0</p></td>
<td align="left"><p>第二个子辈（m/1）的第一个孙子私钥</p></td>
</tr>
<tr class="odd">
<td align="left"><p>M/23/17/0/0</p></td>
<td align="left"><p>第 24 个子辈的第 18 个孙子辈的第一个曾孙辈的第一个玄孙的公钥</p></td>
</tr>
</tbody>
</table>

# 6. 从种子开始生成 HD 钱包

![](https://stevenocean.github.io/2018/09/23/generate-hd-wallet-by-bip39/generate-hd-master-key.jpg)

# 7. 子密钥衍生函数(Child Key Derivation, CKD)

![](https://stevenocean.github.io/2018/09/23/generate-hd-wallet-by-bip39/hd-key-derivation-bip32.jpg)

# 8. 扩展密钥(extended key)

将 密钥 Key 和 Chain Code 结合起来称为 扩展密钥（extended key），可以通过 扩展密钥 来生成自其而下的所有分支。

扩展密钥 中提供的密钥可以为 私钥 或者 公钥，和 链码 结合起来分别称为 扩展私钥（extended private key） 和 扩展公钥（extended public key），并且分别记为 (k, c) 和 (K, c)，其中公钥 K = point(k)。

# 9. HD Wallet 的分层密钥生成结构图

![](https://stevenocean.github.io/2018/09/23/generate-hd-wallet-by-bip39/generate-hd-wallet.jpg)

# 10. 参考：

- [bip-32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki)
- [bip-39](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki)
- [以太坊钱包详解](https://github.com/xitu/gold-miner/blob/master/TODO1/ethereumbook-wallets.md)
- [基于 BIP-32 和 BIP-39 规范生成 HD 钱包（分层确定性钱包）](https://stevenocean.github.io/2018/09/23/generate-hd-wallet-by-bip39.html)