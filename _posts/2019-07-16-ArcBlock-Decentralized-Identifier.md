---
layout:     post
title:      "ArcBlock Decentralized Identifier 解析"
subtitle:   "ArcBlock Decentralized Identifier"
date:       2019-07-16 10:51:11
author:     "hello2mao"
tags:
    - blockchain
---

# 一、背景
## 1.1 ArcBlock
ArcBlock是一个去中心化应用开发部署平台，能降低开发者门槛。。ArcBlock类似PaaS平台，搭建了一个去中心化的区块链应用的开发框架，开发者可借助上面的工具开发、分享自身开发的功能模块。开发者使用平台上的资源都要支付代币。
ArcBlock平台的核心部件和体系包括去中心化访问协议 （云节点）、开放链访问协议、基石程序和构件（Blocklet Components），还有代币经济服务体系。
![image](https://user-images.githubusercontent.com/8265961/61207537-ee97ce00-a727-11e9-8d5f-9b39e03cca4f.png)

## 1.2 DID
去中心化身份(DID)可取代电子邮件或用户名等传统 ID，无需在第三方服务器维护用户的数字身份。植根于区块链，DID 充分利用分布式账本技术来保护隐私、确保交易安全。
![image](https://user-images.githubusercontent.com/8265961/61199905-40cdf480-a712-11e9-8bf9-2a25d15aa150.png)

## 1.3 [ArcBlock DID](https://www.arcblock.io/zh/decentralized-identity)
ArcBlock 开发了一个符合 W3C 标准的去中心化身份协议([ABT:DID Protocol](https://github.com/ArcBlock/abt-did-spec))，这是一个基于 W3C DID 解决方案的开放协议。作为参与者，ArcBlock 计划与其他贡献者合作，推广普及 ABT:DID 身份验证标准。ArcBlock DID 协议已在 W3C DID 方法注册表中注册(地址: https://w3c-ccg.github.io/did-method-registry/#the-registry )。默认情况下，任何与W3CDID 标准兼容 DID 的提供者均可互操作，这意味着 ArcBlock DID 标准对例如最近宣布 DID 方案的微软，或者该领域其他服务方开放可用。

# 二、特性
- DApps Workshop，一个展示DID应用场景及简单实用的Demo，可以构建一些简单的POC场景进行测试。
- ABT 钱包，即DID Wallet。
- DID 开发包，即DID SDK

# 三、产品试用
## （1）本地搭建DApps Workshop
```
npm install -g @arcblock/forge-cli
forge init
forge start
forge workshop start

==> visit http://localhost:8807
```
登入后界面如下：
![image](https://user-images.githubusercontent.com/8265961/61216302-8b199a80-a73f-11e9-8205-8c5c411ceab5.png)
提供两个功能：
- Authentication：创建DID及DID DApp
- Transactions：发送交易

## （2）创建DApp
**可选DID Type：**
![image](https://user-images.githubusercontent.com/8265961/61216473-054a1f00-a740-11e9-8415-003d35cf29f6.png)
**可选DSA Algorithm：**
![image](https://user-images.githubusercontent.com/8265961/61216530-227eed80-a740-11e9-9f92-36f28a1a3cc9.png)
**可选Hash Function：**
![image](https://user-images.githubusercontent.com/8265961/61216562-36c2ea80-a740-11e9-98b9-30b473e06b9a.png)
**配置DApp：**
![image](https://user-images.githubusercontent.com/8265961/61216590-493d2400-a740-11e9-9739-6d5fb13b748f.png)
**配置Claim：**
![image](https://user-images.githubusercontent.com/8265961/61216611-58bc6d00-a740-11e9-91ea-506ab2220346.png)
**创建DApp成功：**
![image](https://user-images.githubusercontent.com/8265961/61216651-725db480-a740-11e9-8fe7-7a0da98f6fef.png)

## （3）创建DID以及使用DID
**扫码注册DID：**
![image](https://user-images.githubusercontent.com/8265961/61216746-b781e680-a740-11e9-802a-55d4bdf923bf.png)
**在DApp上看到新建的DID，以及提供的Claim：**
![image](https://user-images.githubusercontent.com/8265961/61217045-750cd980-a741-11e9-8bee-6adb28d04912.png)
**在DID Wallet查看DID：**
![image](https://user-images.githubusercontent.com/8265961/61217159-af767680-a741-11e9-9662-f94db7c0553a.png)
![image](https://user-images.githubusercontent.com/8265961/61217167-b604ee00-a741-11e9-873c-b25a459ba458.png)
**使用DID登入DApp：**
![image](https://user-images.githubusercontent.com/8265961/61217233-d0d76280-a741-11e9-84f1-c30653c3a323.png)

