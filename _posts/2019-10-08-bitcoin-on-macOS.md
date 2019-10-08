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