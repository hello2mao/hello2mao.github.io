---
layout: post
title: "Hyperledger Fabric v2.x 最新资料汇总"
date: 2020-04-22
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->

- [一、官方](#一官方)
- [二、技术解读](#二技术解读)
  - [2.1 视频讲解](#21-视频讲解)
  - [2.2 实操](#22-实操)
  - [2.3 Peer](#23-peer)
  - [2.4 Orderer](#24-orderer)
  - [2.5 CA](#25-ca)
  - [2.6 Chaincode](#26-chaincode)
  - [2.7 Gossip](#27-gossip)
  - [2.x 其他](#2x-其他)
- [三、专栏](#三专栏)
- [四、技术挑战](#四技术挑战)
- [五、企业落地实战分享](#五企业落地实战分享)
- [六、社区提案](#六社区提案)

<!-- /TOC -->

> 记录一些高质量的资料，不断更新中...

# 一、官方

 1. Github：[https://github.com/hyperledger/fabric](https://github.com/hyperledger/fabric)
 2. 文档：[https://hyperledger-fabric.readthedocs.io/en/latest/whatis.html](https://hyperledger-fabric.readthedocs.io/en/latest/whatis.html)
 3. Jira：[https://jira.hyperledger.org/projects/FAB/summary](https://jira.hyperledger.org/projects/FAB/summary)

# 二、技术解读

## 2.1 视频讲解
 - [IBM 超级账本Fabric v2.0系列课程](https://space.bilibili.com/102734951/channel/detail?cid=112557)
 - [IBM 超级账本Fabric v1.4系列课程](https://space.bilibili.com/102734951/channel/detail?cid=69148)

## 2.2 实操

 - [Hyperledger Fabric1.4环境搭建](https://ifican.top/2019/11/23/blog/fabric/Fabric%E7%8E%AF%E5%A2%83%E6%90%AD%E5%BB%BA/)
 - [深入解析Hyperledger Fabric搭建的全过程](https://ifican.top/2019/11/23/blog/fabric/%E6%B7%B1%E5%85%A5%E8%A7%A3%E6%9E%90Fabric%E6%90%AD%E5%BB%BA%E7%9A%84%E5%85%A8%E8%BF%87%E7%A8%8B/)
 - [Hyperledger Fabric动态添加组织到网络中](https://ifican.top/2019/12/08/blog/fabric/Hyperledger_Fabric%E5%8A%A8%E6%80%81%E6%B7%BB%E5%8A%A0%E7%BB%84%E7%BB%87%E5%88%B0%E7%BD%91%E7%BB%9C%E4%B8%AD/)
 - [Hyperledger Fabric多机部署](https://ifican.top/2019/11/23/blog/fabric/Fabric1.4%E5%A4%9A%E6%9C%BA%E9%83%A8%E7%BD%B2/)
 - [Hyperledger Fabric动态配置Raft节点](https://ifican.top/2019/12/31/blog/fabric/%E5%8A%A8%E6%80%81%E9%85%8D%E7%BD%AERaft%E8%8A%82%E7%82%B9/)
 - [Hyperledger Fabric 开启TLS调用Java SDK](https://ifican.top/2019/12/28/blog/fabric/TLS_SDK%E8%B0%83%E7%94%A8/)
 - [Hyperledger Fabric 最简单的方式测试你的链码](https://ifican.top/2019/11/27/blog/fabric/%E9%93%BE%E7%A0%81%E6%B5%8B%E8%AF%95/)
 - [Hyperledger Fabric私有数据](https://ifican.top/2019/12/04/blog/fabric/%E7%A7%81%E6%9C%89%E6%95%B0%E6%8D%AE/)
 - [Hyperledger Fabric使用硬件安全模块(HSM)](https://ifican.top/2019/12/24/blog/fabric/%E4%BD%BF%E7%94%A8%E7%A1%AC%E4%BB%B6%E5%AE%89%E5%85%A8%E6%A8%A1%E5%9D%97/)
 - [Hyperledger Fabric链码作为外部服务](https://ifican.top/2019/12/27/blog/fabric/%E9%93%BE%E7%A0%81%E4%BD%9C%E4%B8%BA%E5%A4%96%E9%83%A8%E6%9C%8D%E5%8A%A1/)
 - [Hyperledger Fabric外部链码构建与运行](https://ifican.top/2019/12/24/blog/fabric/%E5%A4%96%E9%83%A8%E9%93%BE%E7%A0%81%E6%9E%84%E5%BB%BA%E5%92%8C%E8%BF%90%E8%A1%8C/)
 - [Hyperledger Fabric-CA](https://ifican.top/2019/12/08/blog/fabric/Hyperledger_Fabric_CA/)
 - [Hyperledger Fabric手动生成CA证书搭建Fabric网络](https://ifican.top/2019/12/08/blog/fabric/Hyperledger_Fabric%E6%89%8B%E5%8A%A8%E7%94%9F%E6%88%90CA%E8%AF%81%E4%B9%A6%E6%90%AD%E5%BB%BAFabric%E7%BD%91%E7%BB%9C/)

## 2.3 Peer

 - [Fabric1.4源码解析：客户端创建通道过程](https://www.cnblogs.com/cbkj-xd/p/11113195.html)
 - [fabric源码分析之peer channel join流程](https://www.cnblogs.com/DavidHan/articles/6714161.html)
 - [Fabric源码分析之三启动流程代码Peer分析](https://blog.csdn.net/fpcc/article/details/104150418)

## 2.4 Orderer

- [Fabric源码分析-Orderer启动](https://www.jianshu.com/p/b828d56af119)
- [Fabric源码分析-生成创世块](https://www.jianshu.com/p/65230bbffb58)
- [Fabric源码分析之三启动流程代Orderer分析](https://blog.csdn.net/fpcc/article/details/104419269)

## 2.5 CA
## 2.6 Chaincode
## 2.7 Gossip

 - [Hyperledger Fabric 2.0 Gossip](https://blog.csdn.net/DAOSHUXINDAN/article/details/104944649)

## 2.x 其他

 - [Fabric 2.0新特性](https://haojunsheng.github.io/2020/02/fabric-relase-2/)
 - [Fabric v2.0 源码解析——典型的业务流程](https://uzshare.com/view/822851)
 - [Hyperledger Fabric 2.0 gRPC接口](https://blog.csdn.net/DAOSHUXINDAN/article/details/104668870)
 - [Fabric源码分析之二整体架构和流程](https://blog.csdn.net/fpcc/article/details/104125665)
 - [Hyperledger Fabric访问控制清单的配置与更新【ACL】](http://blog.hubwiz.com/2019/12/28/hyperledger-fabric-acl-config/)

# 三、专栏

 - [大彬：Fabric 1.4源码解读](https://lessisbetter.site/tags/Fabric/)
 - [从0到1：Hyperledger Fabric](https://www.chaindesk.cn/witbook/11/97)
 - [heyuanxun: Hyperledger Fabric 2.0](https://blog.csdn.net/daoshuxindan/category_9775667.html)
 - [Hyperledger Fabric源码分析](https://www.jianshu.com/c/870207138ae6)

# 四、技术挑战
- [fabric联盟链高并发场景下如何提高TPS](https://berryjam.github.io/2019/05/fabric%E8%81%94%E7%9B%9F%E9%93%BE%E9%AB%98%E5%B9%B6%E5%8F%91%E5%9C%BA%E6%99%AF%E4%B8%8B%E5%A6%82%E4%BD%95%E6%8F%90%E9%AB%98TPS/)
- [基于 tendermint 实现 Hyperledger Fabric 的拜占庭容错排序](https://www.infoq.cn/article/2xwfuclcKRAC5ZvqjDgW)
 - [Fabric自定义插件的开发-Decorator插件开发](https://blog.csdn.net/love_feng_forever/article/details/102666696)
 - [Fabric自定义插件的开发-Validators插件开发](https://blog.csdn.net/love_feng_forever/article/details/102687194)
 - [Fabric自定义插件的开发-Auth插件开发](https://blog.csdn.net/love_feng_forever/article/details/102842448)

# 五、企业落地实战分享
 - [HyperLedger Fabric在携程区块链平台中的应用实战](https://techsummit.ctrip.com/2018/pdf/hexinming.pdf)
 - [区块链 Hyperledger Fabric 的落地挑战与阿里云探索经验分享](https://www.infoq.cn/article/hyperledger-fabric-at-aliyun)
 - [华为云区块链服务安全隐私保护的设计与实现](https://www.infoq.cn/article/PQWJcgOf3JzmArxK1w69)
# 六、社区提案
 - [Channel participation API - Join, Leave & List channels w/o a system channel](https://jira.hyperledger.org/projects/FAB/issues/FAB-17712?filter=allopenissues&orderby=created+DESC%2C+priority+DESC%2C+updated+DESC)
 - [Implement Fabric programming model in the Go SDK](https://github.com/hyperledger/fabric-rfcs/blob/master/text/0002-go-sdk-programming-model.md)
 - [Interoperability: Atomic commit of transactions between channels and networks](https://jira.hyperledger.org/browse/FAB-13437)
 - [config-transaction-library](https://github.com/hyperledger/fabric-rfcs/blob/master/text/0000-config-transaction-library.md)