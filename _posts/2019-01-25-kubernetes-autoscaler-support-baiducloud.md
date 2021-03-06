---
layout: post
title: "Kubernetes autoscaler增加对百度云容器引擎CCE的支持"
subtitle: "kubernetes autoscaler support BaiduCloud"
date: 2019-01-25 10:00:00
author: "hello2mao"
tags:
    - cloud native
---

<!-- TOC -->

-   [一、摘要](#一摘要)
-   [二、如何使用 CCE 的自动扩缩容功能](#二如何使用-cce-的自动扩缩容功能)
-   [三、技术细节](#三技术细节)

<!-- /TOC -->

# 一、摘要

CA（ [cluster-autoscaler](https://github.com/kubernetes/autoscaler)）是用来弹性伸缩 kubernetes 集群的。我们在使用 kubernetes 集群经常问到的一个问题是，我应该保持多大的节点规模来满足应用需求呢？ cluster-autoscaler 的出现解决了这个问题，它可以自动的根据部署的应用所请求的资源量来动态的伸缩集群。

CA 组件虽然是 Kubernetes 社区的开源组件，但并不是部署在集群内就可以正常工作的，因为涉及到节点的增删、节点信息的查询等，这些每个云厂商的实现都不一样，在此之前，官方只支持谷歌、微软、亚马逊和阿里的云平台，本次加入对百度云容器引擎 CCE 的支持，使百度云成为国内唯二的得到 Kubernetes autoscaler 官方支持的云厂商。

![image](https://user-images.githubusercontent.com/8265961/52893853-d30db300-31db-11e9-8159-339f3b142325.png)

# 二、如何使用 CCE 的自动扩缩容功能

（1）创建集群时设置自动扩缩容

创建集群时，用户可以在创建页面最下方的“购买信息”中选择开启自动扩缩容，默认为关闭状态。开启自动扩缩容后，可以配置自动扩缩容的最小节点数和最大节点数：

![image](https://user-images.githubusercontent.com/8265961/52893887-326bc300-31dc-11e9-89a5-e3cb9cdd6647.png)

（2）编辑集群的自动扩缩容

在集群列表中，开启自动扩缩容的集群将会在集群 ID 右侧有特殊标识，用户可以通过集群列表中的调整自动扩缩容按钮或者集群详情中的高级配置编辑按钮，对自动扩缩容配置进行修改，启停自动扩缩容或者调整最大最小节点数设置：

![image](https://user-images.githubusercontent.com/8265961/52893891-3f88b200-31dc-11e9-80f3-7873ef344465.png)

详见：https://cloud.baidu.com/doc/CCE/GettingStarted/2D.5C.E8.87.AA.E5.8A.A8.E6.89.A9.E7.BC.A9.E5.AE.B9.html#.A9.DF.B1.30.6B.EF.06.1D.4E.5D.F1.8B.EF.50.4F.8F

# 三、技术细节

CA 由以下几个模块组成：

-   CA autoscaler：核心模块，负责整体扩缩容功能
-   Estimator：负责评估计算扩容节点
-   Simulator：负责模拟调度，计算缩容节点
-   CA Cloud-Provider：与云交互进行节点的增删操作。

其中 CA Cloud-Provider 部分每个云厂商的实现都不一样，基于百度云的实现在这笔 PR（ https://github.com/kubernetes/autoscaler/pull/1536 ） 中被合入了 Kubernetes autoscaler 开源库。

![image](https://user-images.githubusercontent.com/8265961/52893897-4d3e3780-31dc-11e9-9f78-c343d8e8284b.png)

整体架构如下：

![image](https://user-images.githubusercontent.com/8265961/52893901-59c29000-31dc-11e9-9495-75947da9661f.png)

在百度云 CCE 的实现中，

-   CCEManager：CCE cluster-autoscaler 的中控模块，初始化各项配置，开启定时器缓存扩缩容相关信息。
-   ASG-Cache：缓存扩缩容各项配置
-   CloudProvider：能够获得集群内所有节点的相关信息，用户配置的扩缩容相关信息。
-   NodeGroup：能够根据扩缩容的估算结果，安全的扩容集群或者缩容集群。
-   Cloud-SDK：对 BCE 中 BCC、CCE 的 OpenAPI 的封装，用于下单扩容集群、删除空闲节点、查询集群节点信息等。

当 pod 由于资源不足，调度失败，即有 pod 一直处于 Pending 状态时，CA 会采取扩容操作；当 node 的资源利用率较低时，且此 node 上存在的 pod 都能被重新调度到其他 node 上运行时，CA 会采取缩容操作。如果你的集群同时也启用了 HPA（Horizontal Pod Autoscaling，是 k8s 中 pod 的水平自动扩展），那么 CA 也能与 HPA 协同工作，例如：当 CPU 负载增加，HPA 扩容 pod，如果此 pod 因为资源不足无法被调度，则此时 CA 会扩容节点。 当 CPU 负载减小，HPA 减少 pod，CA 发现有节点资源利用率低甚至已经是空时，CA 就会删除此节点。
