---
layout: post
title: "Quorum新的权限模型"
subtitle: "Quorum Permission Model"
date: 2019-12-02 14:29:11
author: "hello2mao"
tags:
  - blockchain
---

<!-- TOC -->

- [1. 概述](#1-%e6%a6%82%e8%bf%b0)
- [2. 新的权限模型概览](#2-%e6%96%b0%e7%9a%84%e6%9d%83%e9%99%90%e6%a8%a1%e5%9e%8b%e6%a6%82%e8%a7%88)
- [3. 实现原理](#3-%e5%ae%9e%e7%8e%b0%e5%8e%9f%e7%90%86)
- [4. 启动](#4-%e5%90%af%e5%8a%a8)
- [5. API](#5-api)

<!-- /TOC -->

# 1. 概述

Quorum在最新发布的v2.3.0中增强了区块链的权限模型，详见[v.2.3.0](https://github.com/jpmorganchase/quorum/releases/tag/v2.3.0)

在以前的版本中，节点的许可管理是通过静态文件管理的。当节点启动时，如果设置了`–permissioned`参数，节点就会查找文件名为`permissioned-nodes.json`的文件，此文件包含此节点可以连接并接受来自其连接的节点白名单。因此，启用权限后，只有`permissioned-nodes.json`文件中列出的节点才能成为网络的一部分。如果指定了`–permissioned`参数，但没有节点添加到`permissioned-nodes.json`文件，则该节点既不能连接到任何节点也不能接受任何接入的连接。

这一个基于静态文件的权限模型显然存在很多问题，所以在最新的v2.3.0中，quorum修改为基于智能合约的`RBAC`权限模型，从而提供了更多的灵活性。

# 2. 新的权限模型概览

基于智能合约的`RBAC`权限模型能够把整个quorum网络按如下图所示的维度进行划分：
![](https://docs.goquorum.com/en/latest/Permissioning/images/PermissionsModel.png)

其中：

- `Network`：整个quorum网络
- `Organization`：组织
- `Sub Organization`：子组织
- `Account`：账户
- `Voter`：能够进行投票的账户
- `Role`：角色
- `Node`：一个quorum节点

有以下几点值得注意：

1. 联盟链划分为多个`组织`，组织内还可以划分`子组织`。
2. 联盟管理员通过`投票`和`表决`来批准新的组织加入quorum网路。
3. 组织管理员能够进行`RBAC`的管理。
4. 子组织也能进行自己的`RBAC`管理。

这样以后，整个quorum网络的组织管理的拓扑如下：

![](https://docs.goquorum.com/en/latest/Permissioning/images/sampleNetwork.png)

# 3. 实现原理

整个基于`RBAC`的权限模型的实现完全是基于智能合约实现的。

在智能合约的设计上，采取了逻辑和数据的分离，通过proxy来访问，从而可以做到智能合约逻辑部分的升级。如下所示：

![](https://docs.goquorum.com/en/latest/Permissioning/images/ContractDesign.png)

- `PermissionsUpgradable.sol`：存储逻辑合约地址的合约
- `PermissionsInterface.sol`：对外提供接口，对内proxy到逻辑合约
- `PermissionsImplementation.sol`：逻辑合约，合约实际逻辑都在这个合约内
- `OrgManager.sol`：数据合约，存储组织相关数据
- `AccountManager.sol`：数据合约，存储账户相关数据
- `NodeManager.sol`：数据合约，存储节点先关数据
- `RoleManager.sol`：数据合约，存储角色先关数据
- `VoterManager.sol`：数据合约，存储投票人先关数据

# 4. 启动

1. 部署所有智能合约
2. 创建配置文件：`permission-config.json`
3. 初始化`PermissionsUpgradable.sol`的`init`方法

# 5. API

新的权限管理提供了很对新的API，详见：https://docs.goquorum.com/en/latest/Permissioning/Permissioning%20apis/