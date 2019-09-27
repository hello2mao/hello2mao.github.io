---
layout: post
title: "ArcBlock Decentralized Identifier 解析"
subtitle: "ArcBlock Decentralized Identifier"
date: 2019-07-16 10:51:11
author: "hello2mao"
tags:
    - blockchain
---

<!-- TOC -->

- [一、背景](#%e4%b8%80%e8%83%8c%e6%99%af)
  - [1.1 ArcBlock](#11-arcblock)
  - [1.2 DID](#12-did)
  - [1.3 ArcBlock DID](#13-arcblock-did)
- [二、特性](#%e4%ba%8c%e7%89%b9%e6%80%a7)
- [三、产品试用](#%e4%b8%89%e4%ba%a7%e5%93%81%e8%af%95%e7%94%a8)
  - [3.1 本地搭建 DApps Workshop](#31-%e6%9c%ac%e5%9c%b0%e6%90%ad%e5%bb%ba-dapps-workshop)
  - [3.2 创建 DApp](#32-%e5%88%9b%e5%bb%ba-dapp)
  - [3.3 创建 DID 以及使用 DID](#33-%e5%88%9b%e5%bb%ba-did-%e4%bb%a5%e5%8f%8a%e4%bd%bf%e7%94%a8-did)
- [四、ArcBlock DID 认证协议](#%e5%9b%9barcblock-did-%e8%ae%a4%e8%af%81%e5%8d%8f%e8%ae%ae)
  - [4.1 DID Schema](#41-did-schema)
  - [4.2 DID Auth 流程](#42-did-auth-%e6%b5%81%e7%a8%8b)
    - [4.2.1 获取 Deep Link](#421-%e8%8e%b7%e5%8f%96-deep-link)
    - [4.2.2 Request DID Authentication](#422-request-did-authentication)
    - [4.2.3 Response DID Authentication](#423-response-did-authentication)
  - [4.3 Verifiable Claims](#43-verifiable-claims)
  - [4.4 使用场景](#44-%e4%bd%bf%e7%94%a8%e5%9c%ba%e6%99%af)
- [五、参考](#%e4%ba%94%e5%8f%82%e8%80%83)

<!-- /TOC -->

# 一、背景

## 1.1 ArcBlock

ArcBlock 是一个去中心化应用开发部署平台，能降低开发者门槛。ArcBlock 类似 PaaS 平台，搭建了一个去中心化的区块链应用的开发框架，开发者可借助上面的工具开发、分享自身开发的功能模块。开发者使用平台上的资源都要支付代币。
ArcBlock 平台的核心部件和体系包括:

-   去中心化访问协议 （云节点）
-   开放链访问协议
-   基石程序和构件（Blocklet Components）
-   代币经济服务体系。

架构如下图所示：
![image](https://user-images.githubusercontent.com/8265961/61207537-ee97ce00-a727-11e9-8d5f-9b39e03cca4f.png)

## 1.2 DID

去中心化身份(DID)可取代电子邮件或用户名等传统 ID，无需在第三方服务器维护用户的数字身份。植根于区块链，DID 充分利用分布式账本技术来保护隐私、确保交易安全。
![image](https://user-images.githubusercontent.com/8265961/61199905-40cdf480-a712-11e9-8bf9-2a25d15aa150.png)
万维网联盟（W3C）正在主持开发的去中心化标识符（Decentralized Identitfiers，DID）标准正在成为去中心化身份（DID）技术实现标准，目前有微软、ArcBlock、uPort、lifeID 等企业或项目提交了各自的 DID 协议方法。
![image](https://user-images.githubusercontent.com/8265961/61279983-b35dd380-a7e9-11e9-9ed5-a19e058fad4d.png)
DIDs 是身份主体相关、与该主体进行可信互动的 URL。DIDs 解析为 DID 文档 ——描述如何使用该 DID 的简单文档。每个 DID 文档可能至少包含三部分：证明目的、验证方法和服务端点。证明目的与验证方法相结合，以提供证明事物的机制。例如，DID 文档可以指定特定的验证方法，例如密码公钥或化名生物特征协议，可以用于验证为目的而创建的方法。服务端点支持与 DID 控制器的可信交互。
![image](https://user-images.githubusercontent.com/8265961/61279999-bb1d7800-a7e9-11e9-9a23-2270f25f8dab.png)
这一可验证、“自我主权”的数字身份新型标识能够让身份数据始终置于终端用户的控制之下，并且不把个人身份信息存储在区块链上（仅将签名的哈希值作为证据），让用户成为身份的唯一所有者，从而摆脱任何中心化注册服务、身份提供商或证书颁发机构的控制。为保护隐私，DID 通常使用零知识证明方法让声明信息的披露尽可能的少：比如国外超市酒吧禁止向未成年人卖酒，有了 DID，你只需要提供由相关部门签名认证的声明说你已经超过 18 岁，而不需要分享你的出生日期。
![image](https://user-images.githubusercontent.com/8265961/61280062-d4bebf80-a7e9-11e9-8721-6384f67e1a1f.png)

## 1.3 ArcBlock DID

ArcBlock 开发了一个符合 W3C 标准的去中心化身份协议([ABT:DID Protocol](https://github.com/ArcBlock/abt-did-spec))，这是一个基于 W3C DID 解决方案的开放协议。
作为参与者，ArcBlock 计划与其他贡献者合作，推广普及 ABT:DID 身份验证标准。ArcBlock DID 协议已在 W3C DID 方法注册表中注册(地址: https://w3c-ccg.github.io/did-method-registry/#the-registry )。
默认情况下，任何与 W3CDID 标准兼容 DID 的提供者均可互操作，这意味着 ArcBlock DID 标准对例如最近宣布 DID 方案的微软，或者该领域其他服务方开放可用。

# 二、特性

-   DApps Workshop，一个展示 DID 应用场景及简单实用的 Demo，可以构建一些简单的 POC 场景进行测试。
-   ABT 钱包，即 DID Wallet，用于创建用户 DID、保管用户私钥、使用 DID 登入 DApp。
-   DID 开发包，即 DID SDK，用于生成 DID 等操作。

# 三、产品试用

## 3.1 本地搭建 DApps Workshop

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

-   Authentication：创建 DID 及 DID DApp
-   Transactions：发送交易

## 3.2 创建 DApp

此 DApp 即需要 DID Auth 才能登入的应用。
**可选 DID Type：**
![image](https://user-images.githubusercontent.com/8265961/61216473-054a1f00-a740-11e9-8415-003d35cf29f6.png)
**可选 DSA Algorithm：**
![image](https://user-images.githubusercontent.com/8265961/61216530-227eed80-a740-11e9-9f92-36f28a1a3cc9.png)
**可选 Hash Function：**
![image](https://user-images.githubusercontent.com/8265961/61216562-36c2ea80-a740-11e9-98b9-30b473e06b9a.png)
**配置 DApp：**
![image](https://user-images.githubusercontent.com/8265961/61216590-493d2400-a740-11e9-9739-6d5fb13b748f.png)
**配置 Claim：**
![image](https://user-images.githubusercontent.com/8265961/61216611-58bc6d00-a740-11e9-91ea-506ab2220346.png)
**创建 DApp 成功：**
![image](https://user-images.githubusercontent.com/8265961/61216651-725db480-a740-11e9-8fe7-7a0da98f6fef.png)

**可以看到此 DApp 有如下几个关键信息：**

-   Application DID，即 appDid
-   Application Public Key，即 appPk
-   Application Secret Key，即 appSk

其中 appDid 和 appPk 会用来构建一个 Auth URL，用户通过这个 URL 来认证自己的 DID，经过认证后就可以登入 DApp。此 URL 在 ArcBlock DID 中称为 Deep Link，例如：

```
https://abtwallet.io/i?appPk=z8Ks49bptUsBUTq1bdbs23TVe2swE4BfbVGpHr5Nxasi4&appDid=did:abt:zNKedP579cjMW9gRG7UEBAt1PASmE9nwnSFh&action=requestAuth&url=http%3A%2F%2F169.254.10.127%3A8807%2Fapi%2Fauth%2F
```

## 3.3 创建 DID 以及使用 DID

**扫码注册 DID：**
![image](https://user-images.githubusercontent.com/8265961/61216746-b781e680-a740-11e9-802a-55d4bdf923bf.png)
**在 DApp 上看到新建的 DID，以及提供的 Claim：**
![image](https://user-images.githubusercontent.com/8265961/61217045-750cd980-a741-11e9-8bee-6adb28d04912.png)
**在 DID Wallet 查看 DID：**
![image](https://user-images.githubusercontent.com/8265961/61217159-af767680-a741-11e9-9662-f94db7c0553a.png)
![image](https://user-images.githubusercontent.com/8265961/61217167-b604ee00-a741-11e9-873c-b25a459ba458.png)
**使用 DID 登入 DApp：**
![image](https://user-images.githubusercontent.com/8265961/61217233-d0d76280-a741-11e9-84f1-c30653c3a323.png)

# 四、ArcBlock DID 认证协议

## 4.1 DID Schema

```
did:abt:z1muQ3xqHQK2uiACHyChikobsiY5kLqtShA
  DID            DID string
 schema
```

创建一个 ABT DID 需要：

-   DID Type：11 类
-   DSA Algorithm：由私钥生成公钥的算法，支持 ED25519 和 SECP256K1
-   Hash function：公钥 Hash 的算法，支持 6 种
-   私钥：随机生成

由上述 4 个数据，参考比特币，经过 11 步，生成`DID string`，然后加上`did:abt:`，作为最终的 ABT DID。
详细的生成过程见：https://arcblock.github.io/abt-did-spec/

## 4.2 DID Auth 流程

### 4.2.1 获取 Deep Link

Deep Link 即用户认证 DID 的 URL，当然也可以做成二维码，让用户扫码认证。关键是`appPk`和`appDid`这个两个参数，前者是 DApp 的公钥，用于加密用户 DID，后者是 DApp 的 DID，用于生成用户的 DID。

### 4.2.2 Request DID Authentication

使用 DID 认证登入的作用，主要是 DApp 为了获取用户的认证声明（verifiable claims ）。  
（1）使用 DApp 的`appDid`生成用户的 DID，即`userDid`  
（2）使用 DApp 的`appPk`加密用户的 DID  
（3）发送此 DID 到 DApp 的 Auth Endpoint  
（4）获取 Auth Response，包含`appPk`和`authInfo`，其中`authInfo`是签名过的 JWT 对象。  
（5）DID Wallet 根据 Auth Response 进行响应，详见 4.2.3

### 4.2.3 Response DID Authentication

在 Auth Response 中，DApp 会要求 DID Wallet 提供 Claim，此时 DID Wallet 只需按照格式 POST 内容给 DApp 即可完成 DID Auth，如下：

```
header：
{
   "alg": "Ed25519",
   "typ": "JWT"
 }
body：
 {
   "userPk": "",
   "userInfo": ""
 }

其中userInfo需要编码，原文为：
 {
   "iss": "userDid",
   "iat": "1548713422",
   "nbf": "1548713422",
   "exp": "1548813422",
   "requestedClaims": [
     {
       "type": "profile",
       "fullName": "Alice Bean",
       "mobilePhone": "123456789",
       "mailingAddress": {
         "addressLine1": "456 123th AVE",
         "addressLine2": "Apt 106",
         "city": "Redmond",
         "state": "WA",
         "postalCode": "98052",
         "country": "USA"
       }
     },
     {
       "type": "agreement",
       "uri": "https://document-1.io",
       "hash": {
         "method": "sha256",
         "digest": "The hash result of the document's content"
       },
       "agreed": true,
       "sig": "user's signature against the doc hash plus AGREED."
     },
     {
       "type": "agreement",
       "uri": "ipfs://document-2",
       "hash": {
         "method": "sha3",
         "digest": "The hash result of the document's content"
       },
       "agreed": false
     }
   ]
 }
```

**注意到，上述两类 claim（profile 和 agreement）都是直接把用户的隐私信息透露给 DApp 的**

## 4.3 Verifiable Claims

定义了 3 类 claim：

-   profile：例如个人资料
-   agreement：例如某某协议
-   proofOfHolding：第三方签发的、拥有某 token、资产的权威证明（TBD）

## 4.4 使用场景

-   用户注册
-   用户登入
-   签署文件
-   发布证书
-   申请 VISA
-   p2p 信息交换

# 五、参考

-   [ArcBlock DID](https://www.arcblock.io/zh/decentralized-identity)
-   [abt-did-spec](https://arcblock.github.io/abt-did-spec/)
