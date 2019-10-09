---
layout: post
title: "[WIP] 去中心化数字身份协议Sidetree解析"
subtitle: "Sidetree-Protocol-Overview"
date: 2019-09-29 10:51:11
author: "hello2mao"
tags:
  - blockchain
---

<!-- TOC -->

- [1. 概述](#1-%e6%a6%82%e8%bf%b0)
- [2. 背景知识](#2-%e8%83%8c%e6%99%af%e7%9f%a5%e8%af%86)
  - [2.1. DID](#21-did)
  - [2.2. Bitcoin](#22-bitcoin)
  - [IPFS](#ipfs)
  - [JSON Web Signature (JWS)](#json-web-signature-jws)
- [3. Sidetree 协议](#3-sidetree-%e5%8d%8f%e8%ae%ae)
  - [3.1. 协议概述](#31-%e5%8d%8f%e8%ae%ae%e6%a6%82%e8%bf%b0)
  - [3.2. 工作原理](#32-%e5%b7%a5%e4%bd%9c%e5%8e%9f%e7%90%86)
  - [3.3. Sidetree 协议的 DID 操作](#33-sidetree-%e5%8d%8f%e8%ae%ae%e7%9a%84-did-%e6%93%8d%e4%bd%9c)
    - [3.3.1. DID OP](#331-did-op)
    - [3.3.2. Batch File 和 Anchor File](#332-batch-file-%e5%92%8c-anchor-file)
  - [3.4. Sidetree REST API](#34-sidetree-rest-api)
- [4. Sidetree 协议 的 Node.js 实现](#4-sidetree-%e5%8d%8f%e8%ae%ae-%e7%9a%84-nodejs-%e5%ae%9e%e7%8e%b0)
  - [4.1. 整体架构](#41-%e6%95%b4%e4%bd%93%e6%9e%b6%e6%9e%84)
  - [4.2. 分层设计](#42-%e5%88%86%e5%b1%82%e8%ae%be%e8%ae%a1)
    - [4.2.1. Orchestration Layer (编排层)](#421-orchestration-layer-%e7%bc%96%e6%8e%92%e5%b1%82)
      - [4.2.1.1. Version Manager](#4211-version-manager)
      - [4.2.1.2. Batch Scheduler](#4212-batch-scheduler)
      - [4.2.1.3. Observer](#4213-observer)
      - [4.2.1.4. Resolver](#4214-resolver)
    - [4.2.2. Protocol Version Specific Layer (版本适配层)](#422-protocol-version-specific-layer-%e7%89%88%e6%9c%ac%e9%80%82%e9%85%8d%e5%b1%82)
  - [4.3. REST API](#43-rest-api)
    - [4.3.1. Blockchain REST API](#431-blockchain-rest-api)
    - [4.3.2. CAS REST API](#432-cas-rest-api)
- [5. ION: 基于比特币的、使用 Sidetree 协议的 DID 实现](#5-ion-%e5%9f%ba%e4%ba%8e%e6%af%94%e7%89%b9%e5%b8%81%e7%9a%84%e4%bd%bf%e7%94%a8-sidetree-%e5%8d%8f%e8%ae%ae%e7%9a%84-did-%e5%ae%9e%e7%8e%b0)
  - [5.1. 使用 ION（macOS）](#51-%e4%bd%bf%e7%94%a8-ionmacos)
- [6. Element: 基于以太坊的、使用 Sidetree 协议的 DID 实现](#6-element-%e5%9f%ba%e4%ba%8e%e4%bb%a5%e5%a4%aa%e5%9d%8a%e7%9a%84%e4%bd%bf%e7%94%a8-sidetree-%e5%8d%8f%e8%ae%ae%e7%9a%84-did-%e5%ae%9e%e7%8e%b0)
- [7. 参考](#7-%e5%8f%82%e8%80%83)

<!-- /TOC -->

# 1. 概述

Sidetree 是一个基于现有区块链的第二层（L2）协议，专门用于去中心化身份（DID）的管理。

目前 [DIF](https://identity.foundation/) 中有两个基于区块链的、使用 Sidetree 协议的 DID 实现：

- [ION](https://github.com/decentralized-identity/ion)：微软开源 的 ION 项目，基于比特币的、使用 Sidetree 协议的 DID 实现。
- [Element](https://github.com/decentralized-identity/element)：Transmute Industries 与 ConsenSys 合作的项目，基于以太坊的、使用 Sidetree 协议的 DID 实现。

# 2. 背景知识

## 2.1. DID

去中心化身份（Decentralized ID, DID）用来解决目前中心化身份系统的一系列问题，W3C 制定了一套 DID 的标准：[W3C DID Spec](https://w3c-ccg.github.io/did-spec/)，而[DIF](https://identity.foundation/)则基于此标准给出了 DID 的实现方案。

简单来说，did 是类似如下的一个字符串，用户使用此字符串来标识自己的身份。
![](/img/posts/did-example.png)

DID 解决方案使用 DID 文档、可验证声明、PKI 体系等来解决去中心化身份管理的五大核心挑战：

- 表示：用来描述主体身份的可迁移表示
- 持久化：用来存储、提取主体身份的机制，同时还需要保持其隐私
- 隐私：在去中心化账本中保护主体身份的模型
- 断言： 确定主体身份的特定语句
- 解析：解析、验证特定主体身份的机制

## 2.2. Bitcoin

## IPFS

## JSON Web Signature (JWS)

# 3. Sidetree 协议

## 3.1. 协议概述

区块链的 TPS 一般都比较低，例如比特币的 TPS 大概为 7~8，所以如果把 DID 相关数据上链，那么会遇到严重的性能问题。所以，出现了 [Sidetree 协议](https://github.com/decentralized-identity/sidetree/blob/master/docs/protocol.md)，它是一个区块链 L2 层的协议，核心思想是把 DID 的批量操作打包进一个区块链交易中，从而显著的提高 DID Operation 的数目。

## 3.2. 工作原理

![](/img/posts/sidetree-workflow.png)

1. Sidetree 节点互相连接构成一个 L2 层的 P2P 网络，每个 Sidetree 节点都对外暴露 Restful API 来处理 DID 的 CURD 操作。
2. Sidetree 节点尽可能多的收集 DID 操作，然后把这批操作打包，并创建一个 L1 链上交易并在交易中嵌入该操作批次的哈希。
3. 批操作的源数据会推送到内容寻址存储（IPFS）上。当其他节点获知嵌入 Sidetree 操作的底层链上交易后，这些节点将向原始节点或其他 IPFS 节点请求该批次数据。
4. 当一个节点收到某个批次后，它会将元数据固定到本地，然后 Sidetree 核心逻辑模块解压批次数据来 解析并验证其中的每个操作。

**需要注意的是**：目标链的区块/交易体系是 Sidetree 协议唯一需要的共识机制，不需要额外的 区块链、侧链或咨询权威单元来让网络中的 DID 达成正确的 PKI 状态。

## 3.3. Sidetree 协议的 DID 操作

### 3.3.1. DID OP

DID 操作不外乎 CURD。

### 3.3.2. Batch File 和 Anchor File

如下所示，把批操作的源数据推送到内容寻址存储（IPFS）上时，会存在两种文件：`Batch File` 和 `Anchor File`：

![](/img/posts/did-op-chain.png)

Batch File:

```
{
  "operations": [
    "Encoded operation",
    "Encoded operation",
    ...
  ]
}
```

Anchor File:

```
{
  "batchFileHash": "Encoded multihash of the batch file.",
  "didUniqueSuffixes": ["Unique suffix of DID of 1st operation", "Unique suffix of DID of 2nd operation", "..."],
  "merkleRoot": "Encoded multihash of the root of the Merkle tree constructed from the operations included in the batch file."
}
```

可以看到，`Batch File` 是原始数据，`Anchor File` 是 `Batch File` 的元数据，而 L1 链上存储的是 `Anchor File` 的 hash。

**为什么要这样设计呢？**  
这其实是为了后续实现 Sidetree 轻节点预留的，因为进行 DID 解析（例如 DID URL Dereferrence）的时候，如果只是需要一些元数据，那么只需下载`Anchor File`即可，而不需要把较大的`Batch File`下载下来。

## 3.4. Sidetree REST API

实现 Sidetree 协议的节点需要提供 REST API，且所有的请求都需要使用 JWS 签名。

提供的接口列表如下，详细的可参考：[sidetree-rest-api](https://github.com/decentralized-identity/sidetree/blob/master/docs/protocol.md#sidetree-rest-api)

- DID and DID Document Creation
- DID Document resolution
- Updating a DID Document
- DID Deletion
- DID Recovery
- Fetch the current service versions (optional)

# 4. Sidetree 协议 的 Node.js 实现

## 4.1. 整体架构

Sidetree Node.js 实现的整体架构如下图所示，可以认为有三部分组成：

- Sidetree 节点：实现 Sidetree 协议的节点
- 区块链及其适配器
- CAS（Content Address Storage）存储，目前是 IPFS

![](/img/posts/sidetree-nodejs-arch.png)

## 4.2. 分层设计

为了解决 Sidetree 协议的后向兼容性，采取分层实现。

- Orchestration Layer (编排层)：与协议版本无关的通用模块放在此层中实现。
- Protocol Version Specific Layer (版本适配层)：与协议版本相关的模块放在此层中实现。

### 4.2.1. Orchestration Layer (编排层)

#### 4.2.1.1. Version Manager

#### 4.2.1.2. Batch Scheduler

#### 4.2.1.3. Observer

#### 4.2.1.4. Resolver

### 4.2.2. Protocol Version Specific Layer (版本适配层)

## 4.3. REST API

### 4.3.1. Blockchain REST API

- Get latest blockchain time
- Get blockchain time by hash
- Fetch Sidetree transactions
- Get first valid Sidetree transaction
- Write a Sidetree transaction
- Fetch normalized transaction fee for proof-of-fee calculation
- Fetch the current service version

### 4.3.2. CAS REST API

- Read content
- Write content
- Fetch the current service version

# 5. ION: 基于比特币的、使用 Sidetree 协议的 DID 实现

## 5.1. 使用 ION（macOS）

> 参考官方教程：[ION Installation Guide](https://github.com/decentralized-identity/ion/blob/master/install-guide.md)

（1） 启动比特币客户端，并同步测试网

```shell
$ cat bitcoin.conf
testnet=1
server=1
rpcuser=hello2mao
rpcpassword=123

$ bitcoind -datadir=/xxx/btc
```

（2） 启动 MongoDB

```shell
$ cat /usr/local/etc/mongod.conf
systemLog:
  destination: file
  path: /usr/local/var/log/mongodb/mongo.log
  logAppend: true
storage:
  dbPath: /usr/local/var/mongodb
net:
  bindIp: 127.0.0.1

$ mongod --config /usr/local/etc/mongod.conf
```

（3） 下载 ION 并 build

```
git clone https://github.com/decentralized-identity/ion
cd ion
npm run build
```

（4） 启动 Sidetree 的区块链适配层微服务，ION 是比特币的实现

```shell
npm run bitcoin
```

（5） 启动 Sidetree 的 CAS，ION 是 IFPS 网络的适配层微服务

```
npm run ipfs
```

（6）启动 Sidetree 核心服务

```
npm run core
```

（7）创建 ION DID
使用 ION 的 js sdk 创建 ION DID，如下：

```javascript
var didAuth = require("@decentralized-identity/did-auth-jose");
var http = require("http");

async function createIONDid() {
  // gen key
  const kid = "#key-1";
  const jwkPriv = await didAuth.EcPrivateKey.generatePrivateKey(kid);
  const jwkPub = jwkPriv.getPublicKey();
  jwkPub.defaultSignAlgorithm = "ES256K";

  // load JWK into an EcPrivateKey object
  const privateKey = didAuth.EcPrivateKey.wrapJwk(jwkPriv.kid, jwkPriv);
  // construct the JWS payload
  const body = {
    "@context": "https://w3id.org/did/v1",
    publicKey: [
      {
        id: jwkPub.kid,
        type: "Secp256k1VerificationKey2018",
        publicKeyJwk: jwkPub
      }
    ],
    service: [
      {
        id: "IdentityHub",
        type: "IdentityHub",
        serviceEndpoint: {
          "@context": "schema.identity.foundation/hub",
          "@type": "UserServiceEndpoint",
          instance: ["did:test:hub.id"]
        }
      }
    ]
  };

  // Construct the JWS header
  const header = {
    alg: jwkPub.defaultSignAlgorithm,
    kid: jwkPub.kid,
    operation: "create",
    proofOfWork: "{}"
  };

  // Sign the JWS
  const cryptoFactory = new didAuth.CryptoFactory([
    new didAuth.Secp256k1CryptoSuite()
  ]);
  const jwsToken = new didAuth.JwsToken(body, cryptoFactory);
  const signedBody = await jwsToken.signAsFlattenedJson(privateKey, { header });

  // Print out the resulting JWS to the console in JSON format
  console.log("Request: \n" + JSON.stringify(signedBody));
  console.log("\n");

  const data = JSON.stringify(signedBody);
  var options = {
    host: "127.0.0.1",
    port: 3000,
    path: "/",
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Content-Length": data.length
    }
  };
  var req = http.request(options, function(res) {
    // console.log("STATUS: " + res.statusCode);
    // console.log("HEADERS: " + JSON.stringify(res.headers));
    res.setEncoding("utf8");
    res.on("data", function(chunk) {
      console.log("Response: \n" + chunk);
    });
  });
  req.on("error", function(e) {
    console.log("problem with request: " + e.message);
  });
  // write data to request body
  req.write(data);
  req.end;
}

createIONDid();
```

Output:

```shell
Request:
{
    "header": {
        "alg": "ES256K",
        "kid": "#key-1",
        "operation": "create",
        "proofOfWork": "{}"
    },
    "payload": "eyJAY29udGV4dCI6Imh0dHBzOi8vdzNpZC5vcmcvZGlkL3YxIiwicHVibGljS2V5IjpbeyJpZCI6IiNrZXktMSIsInR5cGUiOiJTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE4IiwicHVibGljS2V5SndrIjp7Imt0eSI6IkVDIiwia2lkIjoiI2tleS0xIiwiY3J2IjoiUC0yNTZLIiwieCI6Ikp6UTNiQWZmUzc2Y3R3dEJ4S0NBbnhMcXcyckRlaEd3eU9POGwta1dNclkiLCJ5IjoiSGlLb0xwbWdEVXhHSkhQdHJseHkzd2JPREZhWHA5OHhXUndleGRnTWlFVSIsImRlZmF1bHRFbmNyeXB0aW9uQWxnb3JpdGhtIjoibm9uZSIsImRlZmF1bHRTaWduQWxnb3JpdGhtIjoiRVMyNTZLIn19XSwic2VydmljZSI6W3siaWQiOiJJZGVudGl0eUh1YiIsInR5cGUiOiJJZGVudGl0eUh1YiIsInNlcnZpY2VFbmRwb2ludCI6eyJAY29udGV4dCI6InNjaGVtYS5pZGVudGl0eS5mb3VuZGF0aW9uL2h1YiIsIkB0eXBlIjoiVXNlclNlcnZpY2VFbmRwb2ludCIsImluc3RhbmNlIjpbImRpZDp0ZXN0Omh1Yi5pZCJdfX1dfQ",
    "signature": "MEYCIQDIrTPcCV35zQRojk8KtlMAsbJKsbnMt8uEOD0XUspOUwIhAIbeS1r9dPU6cGvyNnWbChGR36HRG3VILr78M39xeG1H"
}

Response:
{
  "@context": "https://w3id.org/did/v1",
  "publicKey": [
    {
      "id": "#key-1",
      "type": "Secp256k1VerificationKey2018",
      "publicKeyJwk": {
        "kty": "EC",
        "kid": "#key-1",
        "crv": "P-256K",
        "x": "JzQ3bAffS76ctwtBxKCAnxLqw2rDehGwyOO8l-kWMrY",
        "y": "HiKoLpmgDUxGJHPtrlxy3wbODFaXp98xWRwexdgMiEU",
        "defaultEncryptionAlgorithm": "none",
        "defaultSignAlgorithm": "ES256K"
      }
    }
  ],
  "service": [
    {
      "id": "IdentityHub",
      "type": "IdentityHub",
      "serviceEndpoint": {
        "@context": "schema.identity.foundation/hub",
        "@type": "UserServiceEndpoint",
        "instance": [
          "did:test:hub.id"
        ]
      }
    }
  ],
  "id": "did:ion:test:EiBNbUbOyzSmE66Akhc-6fYoo_A6QPF15VHSRNFLIJgUsw"
}
```

则新建的 DID 为：`did:ion:test:EiBNbUbOyzSmE66Akhc-6fYoo_A6QPF15VHSRNFLIJgUsw`

（8）查询 ION DID
使用 ION 的 js sdk 查询 ION DID，如下：

```javascript
const http = require("http");

const options = {
  hostname: "127.0.0.1",
  port: 3000,
  path: "/did:ion:test:EiBNbUbOyzSmE66Akhc-6fYoo_A6QPF15VHSRNFLIJgUsw",
  method: "GET"
};

const req = http.request(options, res => {
  // console.log("statusCode:", res.statusCode);
  // console.log("headers:", res.headers);

  res.on("data", d => {
    console.log("Response: \n" + d);
  });
});

req.on("error", e => {
  console.error(e);
});
req.end();
```

Output:

```shell
Response:
{
  "document": {
    "@context": "https://w3id.org/did/v1",
    "publicKey": [
      {
        "id": "#key-1",
        "type": "Secp256k1VerificationKey2018",
        "publicKeyJwk": {
          "kty": "EC",
          "kid": "#key-1",
          "crv": "P-256K",
          "x": "JzQ3bAffS76ctwtBxKCAnxLqw2rDehGwyOO8l-kWMrY",
          "y": "HiKoLpmgDUxGJHPtrlxy3wbODFaXp98xWRwexdgMiEU",
          "defaultEncryptionAlgorithm": "none",
          "defaultSignAlgorithm": "ES256K"
        }
      }
    ],
    "service": [
      {
        "id": "IdentityHub",
        "type": "IdentityHub",
        "serviceEndpoint": {
          "@context": "schema.identity.foundation/hub",
          "@type": "UserServiceEndpoint",
          "instance": [
            "did:test:hub.id"
          ]
        }
      }
    ],
    "id": "did:ion:test:EiBNbUbOyzSmE66Akhc-6fYoo_A6QPF15VHSRNFLIJgUsw"
  },
  "resolverMetadata": {
    "driverId": "did:ion:test",
    "driver": "HttpDriver",
    "retrieved": "2019-10-08T07:54:21.793Z",
    "duration": "49.3152ms"
  }
}
```

# 6. Element: 基于以太坊的、使用 Sidetree 协议的 DID 实现

TBD

# 7. 参考

- [Sidetree Protocol Specification](https://github.com/decentralized-identity/sidetree/blob/master/docs/protocol.md)
- [ION](https://github.com/decentralized-identity/ion)
- [Element](https://github.com/decentralized-identity/element)
- [DIF](https://identity.foundation/)
- [The Sidetree Protocol: Scalable DPKI for Decentralized Identity](https://medium.com/decentralized-identity/the-sidetree-scalable-dpki-for-decentralized-identity-1a9105dfbb58)
- [Sidetree - 去中心化身份管理协议](http://blog.hubwiz.com/2019/05/18/sidetree-protocol/)
- [Azure DID Project](https://didproject.azurewebsites.net/)
- [去中心化身份（Decentralized ID, DID）介绍](https://blog.csdn.net/treaser/article/details/99004355)
- [A Primer for Decentralized Identifiers](https://w3c-ccg.github.io/did-primer/)
