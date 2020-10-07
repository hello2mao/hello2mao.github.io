---
layout: post
title: "Fabric RFC: Channel Participation API"
date: 2020-04-29
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->

- [一、概述](#一概述)
- [二、现存问题解析](#二现存问题解析)
  - [2.1 隐私问题](#21-隐私问题)
  - [2.2 扩展性问题](#22-扩展性问题)
  - [2.3 可操作性](#23-可操作性)
- [三、Channel participation API 方案](#三channel-participation-api-方案)
- [四、链接](#四链接)

<!-- /TOC -->

# 一、概述
目前fabric中创建并加入通道的流程在**隐私性**、**扩展性**以及**可操作性**上都存在很多问题，作者提出了**Channel participation API**的设计来优化这些问题，原文：[RFC-0000](https://github.com/hyperledger/fabric-rfcs/blob/master/text/0000-channel-participation-api-without-system-channel.md)。

Channel participation API的核心是去除了目前通道管理对系统链码的依赖，在启动一个新的orderer时不会默认创建任何通道，同时，增加了**本地orderer通道参与管理员**（local orderer channel participation administrator） 这一角色，拥有此角色的用户能够调用orderer的Channel participation API来操作此orderer加入一个现存的通道、退出一个通道或者列出已经加入的通道。

# 二、现存问题解析

作者认为目前通过给系统通道发送交易来创建通道的方式存在以下几个问题：

## 2.1 隐私问题
创建通道时，会先根据配置生成一个创建通道的交易，然后发给orderer，在这个配置内会指明此应用通道的成员信息，如下：

创建交易流程：
```shell
// 根据配置生成创建通道的交易
configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/${orgmsp}anchors.tx -channelID $CHANNEL_NAME -asOrg ${orgmsp}
// 把此交易广播给orderer
peer channel create -o localhost:7050 -c $CHANNEL_NAME --ordererTLSHostnameOverride orderer.example.com -f ./channel-artifacts/${CHANNEL_NAME}.tx --outputBlock ./channel-artifacts/${CHANNEL_NAME}.block --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA >&log.txt
```
配置细节（configtx.yaml）：
```yaml
Profiles:
    TwoOrgsChannel:
        Consortium: SampleConsortium
        <<: *ChannelDefaults
        Application:
            <<: *ApplicationDefaults
            Organizations:
                - *OrgX
                - *OrgY
            Capabilities:
                <<: *ApplicationCapabilities
```

从上面可以看出，orderer在通道创建时能够知道所有联盟成员信息，这会导致在一个联盟内进行的部分组织间的隐私交易的相关信息暴露。

实际场景举例来说，

- 假设有三个组织X、Y、X，他们在一个联盟内，且各有一个orderer
- 组织X和Y在通道A上进行隐私交易
- 组织X和Z在通道B上进行隐私交易

那么，虽然X和Y在通道A上的交易账本是不会同步到Z上的，**但实际上Z可以从自己的orderer上了解到：商业伙伴X除了和我在通道B上进行交易外，居然还和Y在通道A上交易。**

## 2.2 扩展性问题
目前，所有的orderer都是系统通道的成员，这在大规模的fabric网络中会成为性能瓶颈。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200429102818561.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
在大规模fabric网络中，通道数会不断增加，在raft共识下，每个应用通道都会有自己的raft实例，所以，对与应用通道来说这部分的水平扩展是线性的，但是对与系统通道来说，因为所有orderer都是系统通道的成员，加入和退出应用通道都需要与系统通道交互，必然会导致应用通道的性能下降。所以作者认为，去除加入和退出通道对系统通道的依赖可以解决在fabric网络不断扩展时的导致的系统通道性能下降问题。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200429103006965.png)
另外，在扩容一个新的orderer并使此orderer加入一个已经存在的通道时，由于此orderer不得不通过遍历系统通道来发现目标应用通道相关信息，必然也会导致系统通道的负载变高，加入新orderer变慢。

## 2.3 可操作性
在大规模部署fabric时也存在很多操作上的不便捷性，特别是在云原生环境下利用自动化工作进行部署时尤其的麻烦。

（1）组织信息的更新
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200429104355648.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
通道内的组织需要更新自己的组织信息时，需要orderer的写权限，所以，有的时候，不得不与有orderer写权限的第三方组织进行协作才能完成。

（2）云原生环境下的问题
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200429112422620.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlbGxvMm1hbw==,size_16,color_FFFFFF,t_70)
在云上，可以将已经运行（且已经加入某些通道）的orderer节点在运行时加入到通道中。要做到这点，只需添加该节点的证书到通道的通道配置中。节点会自动检测其加入到新的通道（默认值是 5 分钟，但如果你想让节点更快检测新通道，可以重启节点），然后从通道中的 orderer 拉取通道区块，最后为该链启动 Raft 实例。

# 三、Channel participation API 方案

在启动一个新的orderer时不会默认创建任何通道，同时，增加了**本地orderer通道参与管理员**（local orderer channel participation administrator） 这一角色，拥有此角色的用户能够调用orderer的Channel participation API来操作此orderer加入一个现存的通道、退出一个通道或者列出已经加入的通道。

这部分精力问题不展开了，主要是对现存问题比较感兴趣。

详细见：[https://github.com/hyperledger/fabric-rfcs/blob/master/text/0000-channel-participation-api-without-system-channel.md](https://github.com/hyperledger/fabric-rfcs/blob/master/text/0000-channel-participation-api-without-system-channel.md)

# 四、链接
 - [RFC](https://github.com/hyperledger/fabric-rfcs/blob/master/text/0000-channel-participation-api-without-system-channel.md)
 - [Issue](https://jira.hyperledger.org/projects/FAB/issues/FAB-17712?filter=allopenissues&orderby=created+DESC%2C+priority+DESC%2C+updated+DESC)
