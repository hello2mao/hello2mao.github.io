---
layout: post
title: "在macOS上运行bitcoin客户端"
subtitle: "bitcoin on macOS"
date: 2019-10-08 10:27:00
author: "hello2mao"
hidden: true
tags:
  - blockchain
---

<!-- TOC -->

- [1. 安装比特币客户端](#1-%e5%ae%89%e8%a3%85%e6%af%94%e7%89%b9%e5%b8%81%e5%ae%a2%e6%88%b7%e7%ab%af)
- [2. 启动客户端，并同步testnet](#2-%e5%90%af%e5%8a%a8%e5%ae%a2%e6%88%b7%e7%ab%af%e5%b9%b6%e5%90%8c%e6%ad%a5testnet)
  - [2.1. 新建bitcoin.conf](#21-%e6%96%b0%e5%bb%babitcoinconf)
  - [2.2. 启动客户端](#22-%e5%90%af%e5%8a%a8%e5%ae%a2%e6%88%b7%e7%ab%af)
- [3. 常用命令](#3-%e5%b8%b8%e7%94%a8%e5%91%bd%e4%bb%a4)
  - [3.1. getnetworkinfo](#31-getnetworkinfo)
  - [3.2. getpeerinfo](#32-getpeerinfo)
  - [3.3. getblockchaininfo](#33-getblockchaininfo)
  - [3.4. listaddressgroupings](#34-listaddressgroupings)
  - [3.5. listunspent](#35-listunspent)
  - [3.6. getwalletinfo](#36-getwalletinfo)

<!-- /TOC -->
# 1. 安装比特币客户端

```shell
brew install bitcoin
```

# 2. 启动客户端，并同步testnet

## 2.1. 新建bitcoin.conf

选定一个存储数据的目录，例如/xxx/btc，在此目录下新建bitcoin.conf：
```shell
$ cat bitcoin.conf
testnet=1
server=1
rpcuser=hello2mao
rpcpassword=123
```

## 2.2. 启动客户端
```shell
bitcoind -datadir=/xxx/btc
```

# 3. 常用命令
## 3.1. getnetworkinfo

```shell
bitcoin-cli -rpcport=18332 -rpcuser=hello2mao -rpcpassword=123 getnetworkinfo
```
Output:
```json
{
  "version": 180100,
  "subversion": "/Satoshi:0.18.1/",
  "protocolversion": 70015,
  "localservices": "000000000000040d",
  "localrelay": true,
  "timeoffset": 0,
  "networkactive": true,
  "connections": 8,
  "networks": [
    {
      "name": "ipv4",
      "limited": false,
      "reachable": true,
      "proxy": "",
      "proxy_randomize_credentials": false
    },
    {
      "name": "ipv6",
      "limited": false,
      "reachable": true,
      "proxy": "",
      "proxy_randomize_credentials": false
    },
    {
      "name": "onion",
      "limited": true,
      "reachable": false,
      "proxy": "",
      "proxy_randomize_credentials": false
    }
  ],
  "relayfee": 0.00001000,
  "incrementalfee": 0.00001000,
  "localaddresses": [
  ],
  "warnings": "Warning: unknown new rules activated (versionbit 28)"
}
```

## 3.2. getpeerinfo
```shell
bitcoin-cli -rpcport=18332 -rpcuser=hello2mao -rpcpassword=123 getpeerinfo
```
Output:
```json
[
  {
    "id": 1,
    "addr": "141.223.85.149:18333",
    "addrlocal": "116.247.112.152:33089",
    "addrbind": "172.18.27.31:52660",
    "services": "000000000000040d",
    "relaytxes": true,
    "lastsend": 1570503053,
    "lastrecv": 1570503027,
    "bytessent": 36099,
    "bytesrecv": 70612790,
    "conntime": 1570501375,
    "timeoffset": 0,
    "pingtime": 0.06261799999999999,
    "minping": 0.056753,
    "version": 70015,
    "subver": "/Satoshi:0.18.1/",
    "inbound": false,
    "addnode": false,
    "startingheight": 1580715,
    "banscore": 0,
    "synced_headers": 1580717,
    "synced_blocks": 1580717,
    "inflight": [
    ],
    "whitelisted": false,
    "minfeefilter": 0.00001000,
    "bytessent_per_msg": {
      "addr": 55,
      "feefilter": 32,
      "getaddr": 24,
      "getdata": 14128,
      "getheaders": 1085,
      "headers": 106,
      "inv": 14252,
      "ping": 320,
      "pong": 320,
      "reject": 70,
      "sendcmpct": 66,
      "sendheaders": 24,
      "tx": 5467,
      "verack": 24,
      "version": 126
    },
    "bytesrecv_per_msg": {
      "addr": 30082,
      "block": 70492215,
      "feefilter": 32,
      "getdata": 560,
      "getheaders": 1085,
      "headers": 51431,
      "inv": 18776,
      "ping": 320,
      "pong": 320,
      "sendcmpct": 66,
      "sendheaders": 24,
      "tx": 17729,
      "verack": 24,
      "version": 126
    }
  }
]
```

## 3.3. getblockchaininfo
```shell
bitcoin-cli -rpcport=18332 -rpcuser=hello2mao -rpcpassword=123 getblockchaininfo
```
Output:
```json
{
  "chain": "test",
  "blocks": 1580718,
  "headers": 1580718,
  "bestblockhash": "00000000000001b4c7f414fceb0105a016b679040d83af501c62717e2b95b1ed",
  "difficulty": 4453199.07071689,
  "mediantime": 1570498098,
  "verificationprogress": 0.9999985435389783,
  "initialblockdownload": false,
  "chainwork": "00000000000000000000000000000000000000000000012b544262d1fcad7ff9",
  "size_on_disk": 25483259703,
  "pruned": false,
  "softforks": [
    {
      "id": "bip34",
      "version": 2,
      "reject": {
        "status": true
      }
    },
    {
      "id": "bip66",
      "version": 3,
      "reject": {
        "status": true
      }
    },
    {
      "id": "bip65",
      "version": 4,
      "reject": {
        "status": true
      }
    }
  ],
  "bip9_softforks": {
    "csv": {
      "status": "active",
      "startTime": 1456790400,
      "timeout": 1493596800,
      "since": 770112
    },
    "segwit": {
      "status": "active",
      "startTime": 1462060800,
      "timeout": 1493596800,
      "since": 834624
    }
  },
  "warnings": "Warning: unknown new rules activated (versionbit 28)"
}
```

## 3.4. listaddressgroupings
```shell
bitcoin-cli -rpcport=18332 -rpcuser=hello2mao -rpcpassword=123 listaddressgroupings
```
Output:
```json
[
  [
    [
      "myKuwFKgkZjX8tKsr1CojHRHKw2y1jcuvr",
      0.04862123,
      "testing"
    ]
  ]
]
```

## 3.5. listunspent
```shell
bitcoin-cli -rpcport=18332 -rpcuser=hello2mao -rpcpassword=123 listunspent 0 'null'  "[\"myKuwFKgkZjX8tKsr1CojHRHKw2y1jcuvr\"]"
```
Output:
```json
[
  {
    "txid": "3427d4f5cf585ece6de1065bafcf33734527d2e9612cd1109aa6ddb288342708",
    "vout": 1,
    "address": "myKuwFKgkZjX8tKsr1CojHRHKw2y1jcuvr",
    "label": "testing",
    "scriptPubKey": "76a914c35bb83520bd7bfd880287a58124846cf6c95f0a88ac",
    "amount": 0.04862123,
    "confirmations": 635,
    "spendable": true,
    "solvable": true,
    "desc": "pkh([c35bb835]02d778fc659457473a52ba6d28f3bbab591693a4243504de7a131efc95082f4ad8)#6qqntdr4",
    "safe": true
  }
]
```

## 3.6. getwalletinfo
```shell
bitcoin-cli -rpcport=18332 -rpcuser=hello2mao -rpcpassword=123 getwalletinfo
```
Output:
```json
{
  "walletname": "",
  "walletversion": 169900,
  "balance": 0.04862123,
  "unconfirmed_balance": 0.00000000,
  "immature_balance": 0.00000000,
  "txcount": 10,
  "keypoololdest": 1569749992,
  "keypoolsize": 1000,
  "keypoolsize_hd_internal": 1000,
  "paytxfee": 0.00000000,
  "hdseedid": "496e12e58366b15bb25e3a8b7ef64a161f0ee735",
  "private_keys_enabled": true
}
```