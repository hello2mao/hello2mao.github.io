---
layout: post
title: "Kubernetes：从Cloud Provider到Cloud Controller Mananger全解析"
subtitle: "Concepts Underlying the Cloud Controller Manager "
date: 2018-08-08 21:54:25
author: "hello2mao"
tags:
    - cloud native
---

<!-- TOC -->

- [一、背景](#%e4%b8%80%e8%83%8c%e6%99%af)
  - [1.1 基于 Kubernetes 的容器云](#11-%e5%9f%ba%e4%ba%8e-kubernetes-%e7%9a%84%e5%ae%b9%e5%99%a8%e4%ba%91)
  - [1.2 Cloud Provider 与云厂商](#12-cloud-provider-%e4%b8%8e%e4%ba%91%e5%8e%82%e5%95%86)
  - [1.3 Cloud Provider 的重构之路](#13-cloud-provider-%e7%9a%84%e9%87%8d%e6%9e%84%e4%b9%8b%e8%b7%af)
- [二、Cloud Provider 解析](#%e4%ba%8ccloud-provider-%e8%a7%a3%e6%9e%90)
  - [2.1 Cloud Provider 的作用](#21-cloud-provider-%e7%9a%84%e4%bd%9c%e7%94%a8)
    - [2.1.1 kube-controller-manager 依赖 Cloud Provider 相关部分](#211-kube-controller-manager-%e4%be%9d%e8%b5%96-cloud-provider-%e7%9b%b8%e5%85%b3%e9%83%a8%e5%88%86)
      - [2.1.1.1 Node Controller](#2111-node-controller)
      - [2.1.1.2 Route Controller](#2112-route-controller)
      - [2.1.1.3 Service Controller](#2113-service-controller)
      - [2.1.1.4 PersistentVolumeLabel Controller](#2114-persistentvolumelabel-controller)
    - [2.1.2 kubelet 依赖 Cloud Provider 相关部分](#212-kubelet-%e4%be%9d%e8%b5%96-cloud-provider-%e7%9b%b8%e5%85%b3%e9%83%a8%e5%88%86)
    - [2.1.3 kube-apiserver 依赖 Cloud Provider 相关部分](#213-kube-apiserver-%e4%be%9d%e8%b5%96-cloud-provider-%e7%9b%b8%e5%85%b3%e9%83%a8%e5%88%86)
  - [2.2 Cloud Provider 的设计](#22-cloud-provider-%e7%9a%84%e8%ae%be%e8%ae%a1)
    - [2.2.1 LoadBalancer()的接口设计](#221-loadbalancer%e7%9a%84%e6%8e%a5%e5%8f%a3%e8%ae%be%e8%ae%a1)
    - [2.2.2 Routes()的接口设计](#222-routes%e7%9a%84%e6%8e%a5%e5%8f%a3%e8%ae%be%e8%ae%a1)
- [三、从 Cloud Provider 到 Cloud Controller Manager](#%e4%b8%89%e4%bb%8e-cloud-provider-%e5%88%b0-cloud-controller-manager)
  - [3.1 kube-controller-manager 的重构策略](#31-kube-controller-manager-%e7%9a%84%e9%87%8d%e6%9e%84%e7%ad%96%e7%95%a5)
  - [3.2 kube-apiserver 的重构策略](#32-kube-apiserver-%e7%9a%84%e9%87%8d%e6%9e%84%e7%ad%96%e7%95%a5)
  - [3.3 kubelet 的重构策略](#33-kubelet-%e7%9a%84%e9%87%8d%e6%9e%84%e7%ad%96%e7%95%a5)
- [四、Cloud Controller Manager 解析](#%e5%9b%9bcloud-controller-manager-%e8%a7%a3%e6%9e%90)
  - [4.1 Cloud Controller Manager 架构](#41-cloud-controller-manager-%e6%9e%b6%e6%9e%84)
  - [4.2 Cloud Controller Manager 实现举例](#42-cloud-controller-manager-%e5%ae%9e%e7%8e%b0%e4%b8%be%e4%be%8b)
- [五、部署使用 Cloud Controller Manager 实践](#%e4%ba%94%e9%83%a8%e7%bd%b2%e4%bd%bf%e7%94%a8-cloud-controller-manager-%e5%ae%9e%e8%b7%b5)
  - [5.1 总体要求](#51-%e6%80%bb%e4%bd%93%e8%a6%81%e6%b1%82)
  - [5.2 k8s 相关组件的启动配置变化](#52-k8s-%e7%9b%b8%e5%85%b3%e7%bb%84%e4%bb%b6%e7%9a%84%e5%90%af%e5%8a%a8%e9%85%8d%e7%bd%ae%e5%8f%98%e5%8c%96)
    - [5.2.1 kube-controller-manager 启动配置变化](#521-kube-controller-manager-%e5%90%af%e5%8a%a8%e9%85%8d%e7%bd%ae%e5%8f%98%e5%8c%96)
    - [5.2.2 kube-apiserver 启动配置变化](#522-kube-apiserver-%e5%90%af%e5%8a%a8%e9%85%8d%e7%bd%ae%e5%8f%98%e5%8c%96)
    - [5.2.3 kubelet 启动配置变化](#523-kubelet-%e5%90%af%e5%8a%a8%e9%85%8d%e7%bd%ae%e5%8f%98%e5%8c%96)
  - [5.3 启动 CCM 举例](#53-%e5%90%af%e5%8a%a8-ccm-%e4%b8%be%e4%be%8b)
    - [5.3.1 启用 initializers 并添加 InitializerConifguration](#531-%e5%90%af%e7%94%a8-initializers-%e5%b9%b6%e6%b7%bb%e5%8a%a0-initializerconifguration)
    - [5.3.2 创建 CCM 的 RBAC](#532-%e5%88%9b%e5%bb%ba-ccm-%e7%9a%84-rbac)
    - [5.3.3 启动 CCM](#533-%e5%90%af%e5%8a%a8-ccm)
- [六、参考](#%e5%85%ad%e5%8f%82%e8%80%83)

<!-- /TOC -->

> **Published:** [https://mp.weixin.qq.com/s/a_540yJ1EGVroJ9TpvYtPw](https://mp.weixin.qq.com/s/a_540yJ1EGVroJ9TpvYtPw)

# 一、背景

## 1.1 基于 Kubernetes 的容器云

容器云最主要的功能帮助用户把应用以容器的形式在集群中跑起来。目前很多的容器云平台通过 Docker 及 Kubernetes 等技术提供应用运行平台，从而实现运维自动化、快速部署应用、弹性伸缩和动态调整应用环境资源，提高研发运营效率。

## 1.2 Cloud Provider 与云厂商

为了更好的让 Kubernetes 在公有云平台上运行，提供容器云服务，云厂商需要实现自己的 Cloud Provider，即实现[cloudprovider.Interface](https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/cloud-provider/cloud.go)。
它是 Kubernetes 中开放给云厂商的通用接口，便于 Kubernetes 自动管理和利用云服务商提供的资源，这些资源包括虚拟机资源、负载均衡服务、弹性公网 IP、存储服务等。
如下图所示，Kubernetes 核心库内置了很多主流云厂商的实现，包括 aws、gce、azure：
![image](https://user-images.githubusercontent.com/8265961/52250858-c44e1300-2934-11e9-9448-51e60cdbffc7.png)

## 1.3 Cloud Provider 的重构之路

但是，问题随之而来。
随着 Kubernetes 成为在私有云、公有云和混合云环境中大规模部署容器化应用的事实标准，越来越多的云厂商加入了进来，Cloud Provider 的实现也越来越多，作为在 Kubernetes 核心库中的代码，这必将影响其快速的更新和迭代。
所以产生了把 Cloud Provider 移出 Kubernetes 核心库并进行重构的提案（[Refactor Cloud Provider out of Kubernetes Core](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/cloud-provider/cloud-provider-refactoring.md)）。
在 k8s v1.6，引入了 Cloud Controller Manager（CCM），目的就是最终替代 Cloud Provider。截止到最新的 k8s v1.11，还是处于 beta 阶段。

# 二、Cloud Provider 解析

## 2.1 Cloud Provider 的作用

在 k8s 中有三个组件对 Cloud Provider 有依赖，分别是：

-   kube-controller-manager
-   kubelet
-   kube-apiserver

这三个组件对 Cloud Provider 的依赖部分会最终编译进相应的二进制中，进一步的依赖关系如下图所示：
![image](https://user-images.githubusercontent.com/8265961/52255257-c8862a80-294c-11e9-9ce3-422969fe3e57.png)

### 2.1.1 kube-controller-manager 依赖 Cloud Provider 相关部分

kube-controller-manager 对 Cloud Provider 的依赖分布在四个 controller 中。

#### 2.1.1.1 Node Controller

Node Controller 使用 Cloud Provider 来检查 node 是否已经在云上被删除了，如果 Cloud Provider 返回有 node 被删除了，那么 Node Controller 立马就会把此 node 从 k8s 中删除。

#### 2.1.1.2 Route Controller

用来配置 node 的路由。
对于 Kubernetes 的容器网络，基本的原则是：每个 pod 都拥有一个独立的 IP 地址（IP per Pod），而且假定所有的 pod 都在一个可以直接连通的、扁平的网络空间中。而在云上，node 的基础设施是由云厂商提供的，所以 Route Controller 需要调用 Cloud Provider 来配置云上 node 的底层路由从而实现 Kubernetes 的容器网络。

#### 2.1.1.3 Service Controller

Service Controller 维护了当前可用 node 的列表，同时负责创建、删除、更新类型是 LoadBalancer 的 Service，从而使用云厂商额外提供的负载均衡服务、弹性公网 IP 等服务。

#### 2.1.1.4 PersistentVolumeLabel Controller

PersistentVolumeLabel Controller 使用 Cloud Provider 来创建、删除、挂载、卸载 node 上的卷，因为卷也是云厂商额外提供的云存储服务。

### 2.1.2 kubelet 依赖 Cloud Provider 相关部分

kubelet 中的 Node Status 使用 Cloud Provider 来获得 node 的信息。包括：

-   nodename：运行 kubelet 的节点名字
-   InstanceID, ProviderID, ExternalID, Zone Info：初始化 kubelet 的时候需要
-   周期性的同步 node 的 IP

### 2.1.3 kube-apiserver 依赖 Cloud Provider 相关部分

kube-apiserver 使用 Cloud Provider 来给所有 node 派发 SSH Keys。

## 2.2 Cloud Provider 的设计

云厂商在实现自己的 Cloud Provider 时只需要实现 cloudprovider.Interface 即可，如下：

```
type Interface interface {
	// 初始化一个k8s client，用于和kube-apiserver通讯
	Initialize(clientBuilder controller.ControllerClientBuilder)
	// 与负载均衡相关的接口
	LoadBalancer() (LoadBalancer, bool)
	// 与节点信息相关的接口
	Instances() (Instances, bool)
	// 与节点可用区相关的接口
	Zones() (Zones, bool)
	// 与集群相关的接口
	Clusters() (Clusters, bool)
	// 与路由相关的接口
	Routes() (Routes, bool)
	// cloud provider ID.
	ProviderName() string
	// ClusterID
	HasClusterID() bool
}
```

重点讲下两个比较重要的接口 LoadBalancer()与 Routes()。

### 2.2.1 LoadBalancer()的接口设计

LoadBalancer()接口用来为 kube-controller-manager 的 Service Controller 服务，接口说明如下：

```
type LoadBalancer interface {
	// 根据clusterName和service返回是否存LoadBalancer，若存在则返回此LoadBalancer的状态信息，状态信息里包含此LoadBalancer的对外IP和一个可选的HostName
	GetLoadBalancer(ctx context.Context, clusterName string, service *v1.Service) (status *v1.LoadBalancerStatus, exists bool, err error)
	// 更新或者创建一个LoadBalancer，在用户创建一个type是LoadBalancer的Service时触发。
	EnsureLoadBalancer(ctx context.Context, clusterName string, service *v1.Service, nodes []*v1.Node) (*v1.LoadBalancerStatus, error)
	// 更新一个LoadBalancer，在Service信息发生改变或者集群信息发生改变时，例如集群新加入一个节点，那么需要更新下LoadBalancer，目的是为LoadBalancer更新下后端需要监听的新节点。
	UpdateLoadBalancer(ctx context.Context, clusterName string, service *v1.Service, nodes []*v1.Node) error
	// 删除一个LoadBalancer
	EnsureLoadBalancerDeleted(ctx context.Context, clusterName string, service *v1.Service) error
}
```

### 2.2.2 Routes()的接口设计

Routes()接口用来为 kube-controller-manager 的 Route Controller 服务，接口说明如下：

```
type Routes interface {
	// 列举集群的路由规则
	ListRoutes(ctx context.Context, clusterName string) ([]*Route, error)
	// 为当前集群新建路由规则
	CreateRoute(ctx context.Context, clusterName string, nameHint string, route *Route) error
	// 删除路由规则
	DeleteRoute(ctx context.Context, clusterName string, route *Route) error
}
```

# 三、从 Cloud Provider 到 Cloud Controller Manager

从 k8s v1.6 开始，k8s 的编译产物中多了一个二进制：cloud-controller manager，它就是用来替代 Cloud Provider 的。
因为原先的 Cloud Provider 与 mater 中的组件 kube-controller-manager、kube-apiserver 以及 node 中的组件 kubelet 耦合很紧密，所以这三个组件也需要相应的进行重构。

## 3.1 kube-controller-manager 的重构策略

kube-controller-manager 中有四个 controller 与 Cloud Provider 相关，相应的重构策略如下：

-   Route Controller - 移入 CCM，并在相应的 controller loop 中运行。
-   Service Controller - 移入 CCM，并在相应的 controller loop 中运行。
-   PersistentVolumeLabel Controller - 移入 CCM，并在相应的 controller loop 中运行。
-   Node Controller - 在 CCM 中增加新 controller：Cloud Node Controller。 - Cloud Node Controller 除了实现原来 Node Controller 的功能外，增加新功能： - CIDR 的管理 - 监控节点的状态 - 节点 Pod 的驱逐策略

## 3.2 kube-apiserver 的重构策略

对于 kube-apiserver 使用 Cloud Provider 的两个功能：

-   分发 SSH Keys - 移入 CCM
-   对于 PV 的 Admission Controller - 在 kubelet 中实现

## 3.3 kubelet 的重构策略

kubelet 需要增加一个新功能：在 CCM 还未初始化 kubelet 所在节点时，需标记此节点类似“NotReady”的状态，防止 scheduler 调度 pod 到此节点时产生一系列错误。此功能通过给节点加上如下 Taints 并在 CCM 初始化后删去此 Taints 实现：

```
node.cloudprovider.kubernetes.io/uninitialized=true:NoSchedule
```

# 四、Cloud Controller Manager 解析

## 4.1 Cloud Controller Manager 架构

按照第三节所述进行重构后，新的模块 Cloud Controller Manager 将作为一个新的组件直接部署在集群内，如下图所示：
![image](https://user-images.githubusercontent.com/8265961/52255325-0e42f300-294d-11e9-8465-200594797af2.png)

CCM 组件内各小模块的功能与原先 Cloud Provider 的差不多，见第二节对 Cloud Provider 的解析。
对于云厂商来说，需要：  
（1）实现 cloudprovider.Interface 接口的功能，这部分在 Cloud Provider 中已经都实现，直接迁移就行。  
（2）实现自己的 Cloud Controller Manager，并在部署 k8s 时，把 CCM 按要求部署在集群内，部署时的注意事项及部署参考实践见第五节。

## 4.2 Cloud Controller Manager 实现举例

实现自己的 CCM 也比较简单，举例如下：

```
package main

import (
	"k8s.io/kubernetes/cmd/cloud-controller-manager/app"
	"k8s.io/kubernetes/cmd/cloud-controller-manager/app/options"

	// （1）初始化原来Cloud Provider的相关逻辑，读取cloud配置、初始化云厂商的Clod SDK。
	_ "k8s.io/cloud-provider-baiducloud/pkg/cloud-provider"
)

func main() {
	goflag.CommandLine.Parse([]string{})
	// (2) 初始化一个默认的CCM配置
	s, _ := options.NewCloudControllerManagerOptions()
	if err != nil {
		glog.Fatalf("unable to initialize command options: %v", err)
	}
	// (3) CCM启动命令
	cmd := &cobra.Command{
		Use: "cloud-controller-manager",
		Long: `The Cloud controller manager is a daemon that embeds the cloud specific control loops shipped with Kubernetes.`,
		Run: func(cmd *cobra.Command, args []string) {
			c, err := s.Config()
			if err != nil {
				fmt.Fprintf(os.Stderr, "%v\n", err)
				os.Exit(1)
			}
			// (4) Run里会运行相关controller loops：
			// CloudNode Controller
			// PersistentVolumeLabel Controller
			// Service Controller
			// Route Controller
			if err := app.Run(c.Complete()); err != nil {
				fmt.Fprintf(os.Stderr, "%v\n", err)
				os.Exit(1)
			}
		},
	}
	s.AddFlags(cmd.Flags())
	pflag.CommandLine.SetNormalizeFunc(flag.WordSepNormalizeFunc)
	pflag.CommandLine.AddGoFlagSet(goflag.CommandLine)
	logs.InitLogs()
	defer logs.FlushLogs()
	if err := c.Execute(); err != nil {
		fmt.Fprintf(os.Stderr, "%v\n", err)
		os.Exit(1)
	}
}
```

# 五、部署使用 Cloud Controller Manager 实践

## 5.1 总体要求

-   云厂商提供给 CCM 的 API 需要有认证鉴权机制，防止恶意行为。
-   因为 CCM 运行在集群内，所以需要 RBAC 规则去跟 kube-apiserver 通讯
-   CCM 为了高可用，可开启选主功能

## 5.2 k8s 相关组件的启动配置变化

将 Cloud Provider 改为 CCM 后，相关组件启动的配置需要修改。

### 5.2.1 kube-controller-manager 启动配置变化

不指定 cloud-provider。

### 5.2.2 kube-apiserver 启动配置变化

（1）不指定 cloud-provider  
（2）admission-control 中删去 PersistentVolumeLabel，因为 CCM 将接手 PersistentVolumeLabel  
（3）admission-control 中增加 Initializers  
（4）runtime-config 中增加 admissionregistration.k8s.io/v1alpha1

### 5.2.3 kubelet 启动配置变化

指定 cloud-provider=external，告诉 kubelet，在它开始调度工作前，需要被 CCM 初始化。（node 会被打上 Taints:node.cloudprovider.kubernetes.io/uninitialized=true:NoSchedule）

## 5.3 启动 CCM 举例

### 5.3.1 启用 initializers 并添加 InitializerConifguration

CCM 为了给 PV 打标签需要：  
（1）启用 initializers（https://kubernetes.io/docs/reference/access-authn-authz/extensible-admission-controllers/#enable-initializers-alpha-feature）  
（2）添加 InitializerConifguration：persistent-volume-label-initializer-config.yaml 如下：

```yaml
admin/cloud/pvl-initializer-config.yaml
kind: InitializerConfiguration
apiVersion: admissionregistration.k8s.io/v1alpha1
metadata:
  name: pvlabel.kubernetes.io
initializers:
  - name: pvlabel.kubernetes.io
    rules:
    - apiGroups:
      - ""
      apiVersions:
      - "*"
      resources:
      - persistentvolumes
```

### 5.3.2 创建 CCM 的 RBAC

```yaml
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
    name: system:cloud-controller-manager
    labels:
        kubernetes.io/cluster-service: "true"
rules:
    - apiGroups:
          - ""
      resources:
          - nodes
      verbs:
          - "*"

    - apiGroups:
          - ""
      resources:
          - nodes/status
      verbs:
          - patch

    - apiGroups:
          - ""
      resources:
          - services
      verbs:
          - list
          - watch
          - patch

    - apiGroups:
          - ""
      resources:
          - services/status
      verbs:
          - update

    - apiGroups:
          - ""
      resources:
          - events
      verbs:
          - create
          - patch
          - update

    # For leader election
    - apiGroups:
          - ""
      resources:
          - endpoints
      verbs:
          - create

    - apiGroups:
          - ""
      resources:
          - endpoints
      resourceNames:
          - "cloud-controller-manager"
      verbs:
          - get
          - list
          - watch
          - update

    - apiGroups:
          - ""
      resources:
          - configmaps
      verbs:
          - create

    - apiGroups:
          - ""
      resources:
          - configmaps
      resourceNames:
          - "cloud-controller-manager"
      verbs:
          - get
          - update

    - apiGroups:
          - ""
      resources:
          - serviceaccounts
      verbs:
          - create
    - apiGroups:
          - ""
      resources:
          - secrets
      verbs:
          - get
          - list

    # For the PVL
    - apiGroups:
          - ""
      resources:
          - persistentvolumes
      verbs:
          - list
          - watch
          - patch
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
    name: xxx-cloud-controller-manager
roleRef:
    apiGroup: rbac.authorization.k8s.io
    kind: ClusterRole
    name: system:cloud-controller-manager
subjects:
    - kind: ServiceAccount
      name: cloud-controller-manager
      namespace: kube-system
```

### 5.3.3 启动 CCM

可以通过 DaemonSet 或者 Deployment 的方式启动 CCM：

```yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
    name: cloud-controller-manager
    namespace: kube-system
---
apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
    name: xxx-cloud-controller-manager
    namespace: kube-system
    labels:
        k8s-app: xxx-cloud-controller-manager
spec:
    selector:
        matchLabels:
            k8s-app: xxx-cloud-controller-manager
    updateStrategy:
        type: RollingUpdate
    template:
        metadata:
            labels:
                k8s-app: xxx-cloud-controller-manager
        spec:
            hostNetwork: true
            nodeSelector:
                node-role.kubernetes.io/master: ""
            tolerations:
                - key: node.cloudprovider.kubernetes.io/uninitialized
                  value: "true"
                  effect: NoSchedule
                - key: node-role.kubernetes.io/master
                  operator: Exists
                  effect: NoSchedule
            serviceAccountName: cloud-controller-manager
            containers:
                - name: cloud-controller-manager
                  image: xxx-cloud-controller-manager:v1.0.0
                  args:
                      - --cloud-config=/path/to/cloud-config.yaml
                      - --cloud-provider=xxx
                      - -v=4
                  volumeMounts:
                      - name: cfg
                        mountPath: /etc/xxx
                        readOnly: true
                      - name: kubernetes
                        mountPath: /etc/kubernetes
                        readOnly: true
            volumes:
                - name: cfg
                  secret:
                      secretName: xxx-cloud-controller-manager
                - name: kubernetes
                  hostPath:
                      path: /etc/kubernetes
```

# 六、参考

-   [Kubernetes Cloud Controller Manager](https://kubernetes.io/docs/tasks/administer-cluster/running-cloud-controller/)
-   [Refactor Cloud Provider out of Kubernetes Core](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/cloud-provider/cloud-provider-refactoring.md)
-   [Cloud Providers](https://kubernetes.io/docs/concepts/cluster-administration/cloud-providers/)
