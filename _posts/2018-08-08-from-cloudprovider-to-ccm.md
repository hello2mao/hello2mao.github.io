---
layout:     post
title:      "Kubernetes：从Cloud Provider到Cloud Controller Mananger全解析"
subtitle:   "Concepts Underlying the Cloud Controller Manager "
date:       2018-08-08 21:54:25
author:     "hello2mao"
tags:
    - kubernetes
---

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
- [一、背景](#%E4%B8%80%E8%83%8C%E6%99%AF)
  - [1.1 基于Kubernetes的容器云](#11-%E5%9F%BA%E4%BA%8Ekubernetes%E7%9A%84%E5%AE%B9%E5%99%A8%E4%BA%91)
  - [1.2 Cloud Provider与云厂商](#12-cloud-provider%E4%B8%8E%E4%BA%91%E5%8E%82%E5%95%86)
  - [1.3 Cloud Provider的重构之路](#13-cloud-provider%E7%9A%84%E9%87%8D%E6%9E%84%E4%B9%8B%E8%B7%AF)
- [二、Cloud Provider解析](#%E4%BA%8Ccloud-provider%E8%A7%A3%E6%9E%90)
  - [2.1 Cloud Provider的作用](#21-cloud-provider%E7%9A%84%E4%BD%9C%E7%94%A8)
    - [2.1.1 kube-controller-manager依赖Cloud Provider相关部分](#211-kube-controller-manager%E4%BE%9D%E8%B5%96cloud-provider%E7%9B%B8%E5%85%B3%E9%83%A8%E5%88%86)
      - [2.1.1.1 Node Controller](#2111-node-controller)
      - [2.1.1.2 Route Controller](#2112-route-controller)
      - [2.1.1.3 Service Controller](#2113-service-controller)
      - [2.1.1.4 PersistentVolumeLabel Controller](#2114-persistentvolumelabel-controller)
    - [2.1.2 kubelet依赖Cloud Provider相关部分](#212-kubelet%E4%BE%9D%E8%B5%96cloud-provider%E7%9B%B8%E5%85%B3%E9%83%A8%E5%88%86)
    - [2.1.3 kube-apiserver依赖Cloud Provider相关部分](#213-kube-apiserver%E4%BE%9D%E8%B5%96cloud-provider%E7%9B%B8%E5%85%B3%E9%83%A8%E5%88%86)
  - [2.2 Cloud Provider的设计](#22-cloud-provider%E7%9A%84%E8%AE%BE%E8%AE%A1)
    - [2.2.1 LoadBalancer()的接口设计](#221-loadbalancer%E7%9A%84%E6%8E%A5%E5%8F%A3%E8%AE%BE%E8%AE%A1)
    - [2.2.2 Routes()的接口设计](#222-routes%E7%9A%84%E6%8E%A5%E5%8F%A3%E8%AE%BE%E8%AE%A1)
- [三、从Cloud Provider到Cloud Controller Manager](#%E4%B8%89%E4%BB%8Ecloud-provider%E5%88%B0cloud-controller-manager)
  - [3.1 kube-controller-manager的重构策略](#31-kube-controller-manager%E7%9A%84%E9%87%8D%E6%9E%84%E7%AD%96%E7%95%A5)
  - [3.2 kube-apiserver的重构策略](#32-kube-apiserver%E7%9A%84%E9%87%8D%E6%9E%84%E7%AD%96%E7%95%A5)
  - [3.3 kubelet的重构策略](#33-kubelet%E7%9A%84%E9%87%8D%E6%9E%84%E7%AD%96%E7%95%A5)
- [四、Cloud Controller Manager解析](#%E5%9B%9Bcloud-controller-manager%E8%A7%A3%E6%9E%90)
  - [4.1 Cloud Controller Manager架构](#41-cloud-controller-manager%E6%9E%B6%E6%9E%84)
  - [4.2 Cloud Controller Manager实现举例](#42-cloud-controller-manager%E5%AE%9E%E7%8E%B0%E4%B8%BE%E4%BE%8B)
- [五、部署使用Cloud Controller Manager实践](#%E4%BA%94%E9%83%A8%E7%BD%B2%E4%BD%BF%E7%94%A8cloud-controller-manager%E5%AE%9E%E8%B7%B5)
  - [5.1 总体要求](#51-%E6%80%BB%E4%BD%93%E8%A6%81%E6%B1%82)
  - [5.2 k8s相关组件的启动配置变化](#52-k8s%E7%9B%B8%E5%85%B3%E7%BB%84%E4%BB%B6%E7%9A%84%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E5%8F%98%E5%8C%96)
    - [5.2.1 kube-controller-manager启动配置变化](#521-kube-controller-manager%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E5%8F%98%E5%8C%96)
    - [5.2.2 kube-apiserver启动配置变化](#522-kube-apiserver%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E5%8F%98%E5%8C%96)
    - [5.2.3 kubelet启动配置变化](#523-kubelet%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E5%8F%98%E5%8C%96)
  - [5.3 启动CCM举例](#53-%E5%90%AF%E5%8A%A8ccm%E4%B8%BE%E4%BE%8B)
    - [5.3.1 启用initializers并添加InitializerConifguration](#531-%E5%90%AF%E7%94%A8initializers%E5%B9%B6%E6%B7%BB%E5%8A%A0initializerconifguration)
    - [5.3.2 创建CCM的RBAC](#532-%E5%88%9B%E5%BB%BAccm%E7%9A%84rbac)
    - [5.3.3 启动CCM](#533-%E5%90%AF%E5%8A%A8ccm)
- [六、参考](#%E5%85%AD%E5%8F%82%E8%80%83)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

> Finished: 2018-08-08
>Published: https://mp.weixin.qq.com/s/a_540yJ1EGVroJ9TpvYtPw 

# 一、背景
## 1.1 基于Kubernetes的容器云
容器云最主要的功能帮助用户把应用以容器的形式在集群中跑起来。目前很多的容器云平台通过Docker及Kubernetes等技术提供应用运行平台，从而实现运维自动化、快速部署应用、弹性伸缩和动态调整应用环境资源，提高研发运营效率。
## 1.2 Cloud Provider与云厂商
为了更好的让Kubernetes在公有云平台上运行，提供容器云服务，云厂商需要实现自己的Cloud Provider，即实现cloudprovider.Interface（https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/cloud-provider/cloud.go）。
它是Kubernetes中开放给云厂商的通用接口，便于Kubernetes自动管理和利用云服务商提供的资源，这些资源包括虚拟机资源、负载均衡服务、弹性公网IP、存储服务等。
如下图所示，Kubernetes核心库内置了很多主流云厂商的实现，包括aws、gce、azure：
![image](https://user-images.githubusercontent.com/8265961/52250858-c44e1300-2934-11e9-9448-51e60cdbffc7.png)

## 1.3 Cloud Provider的重构之路
但是，问题随之而来。
随着Kubernetes成为在私有云、公有云和混合云环境中大规模部署容器化应用的事实标准，越来越多的云厂商加入了进来，Cloud Provider的实现也越来越多，作为在Kubernetes核心库中的代码，这必将影响其快速的更新和迭代。
所以产生了把Cloud Provider移出Kubernetes核心库并进行重构的提案（[Refactor Cloud Provider out of Kubernetes Core](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/cloud-provider/cloud-provider-refactoring.md)）。
在k8s v1.6，引入了Cloud Controller Manager（CCM），目的就是最终替代Cloud Provider。截止到最新的k8s v1.11，还是处于beta阶段。
# 二、Cloud Provider解析
## 2.1 Cloud Provider的作用
在k8s中有三个组件对Cloud Provider有依赖，分别是：

 - kube-controller-manager
 - kubelet
 - kube-apiserver

这三个组件对Cloud Provider的依赖部分会最终编译进相应的二进制中，进一步的依赖关系如下图所示：
![image](https://user-images.githubusercontent.com/8265961/52255257-c8862a80-294c-11e9-9ce3-422969fe3e57.png)

### 2.1.1 kube-controller-manager依赖Cloud Provider相关部分
kube-controller-manager对Cloud Provider的依赖分布在四个controller中。
#### 2.1.1.1 Node Controller
Node Controller使用Cloud Provider来检查node是否已经在云上被删除了，如果Cloud Provider返回有node被删除了，那么Node Controller立马就会把此node从k8s中删除。
#### 2.1.1.2 Route Controller
用来配置node的路由。
对于Kubernetes的容器网络，基本的原则是：每个pod都拥有一个独立的IP地址（IP per Pod），而且假定所有的pod都在一个可以直接连通的、扁平的网络空间中。而在云上，node的基础设施是由云厂商提供的，所以Route Controller需要调用Cloud Provider来配置云上node的底层路由从而实现Kubernetes的容器网络。
#### 2.1.1.3 Service Controller
Service Controller维护了当前可用node的列表，同时负责创建、删除、更新类型是LoadBalancer的Service，从而使用云厂商额外提供的负载均衡服务、弹性公网IP等服务。
#### 2.1.1.4 PersistentVolumeLabel Controller
PersistentVolumeLabel Controller使用Cloud Provider来创建、删除、挂载、卸载node上的卷，因为卷也是云厂商额外提供的云存储服务。
### 2.1.2 kubelet依赖Cloud Provider相关部分
kubelet中的Node Status使用Cloud Provider来获得node的信息。包括：

 - nodename：运行kubelet的节点名字
 - InstanceID, ProviderID, ExternalID, Zone Info：初始化kubelet的时候需要
 - 周期性的同步node的IP
### 2.1.3 kube-apiserver依赖Cloud Provider相关部分
kube-apiserver使用Cloud Provider来给所有node派发SSH Keys。
## 2.2 Cloud Provider的设计
云厂商在实现自己的Cloud Provider时只需要实现cloudprovider.Interface即可，如下：
```golang
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
重点讲下两个比较重要的接口LoadBalancer()与Routes()。
### 2.2.1 LoadBalancer()的接口设计
LoadBalancer()接口用来为kube-controller-manager的Service Controller服务，接口说明如下：
```golang
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
Routes()接口用来为kube-controller-manager的Route Controller服务，接口说明如下：
```golang
type Routes interface {
	// 列举集群的路由规则
	ListRoutes(ctx context.Context, clusterName string) ([]*Route, error)
	// 为当前集群新建路由规则
	CreateRoute(ctx context.Context, clusterName string, nameHint string, route *Route) error
	// 删除路由规则
	DeleteRoute(ctx context.Context, clusterName string, route *Route) error
}
```
# 三、从Cloud Provider到Cloud Controller Manager
从k8s v1.6开始，k8s的编译产物中多了一个二进制：cloud-controller manager，它就是用来替代Cloud Provider的。
因为原先的Cloud Provider与mater中的组件kube-controller-manager、kube-apiserver以及node中的组件kubelet耦合很紧密，所以这三个组件也需要相应的进行重构。
## 3.1 kube-controller-manager的重构策略
kube-controller-manager中有四个controller与Cloud Provider相关，相应的重构策略如下：

 - Route Controller
	 - 移入CCM，并在相应的controller loop中运行。
 - Service Controller
	 - 移入CCM，并在相应的controller loop中运行。
 - PersistentVolumeLabel Controller
	 - 移入CCM，并在相应的controller loop中运行。
 - Node Controller
	 - 在CCM中增加新controller：Cloud Node Controller。
	 - Cloud Node Controller除了实现原来Node Controller的功能外，增加新功能：
		 - CIDR的管理
		 - 监控节点的状态
		 - 节点Pod的驱逐策略
## 3.2 kube-apiserver的重构策略
对于kube-apiserver使用Cloud Provider的两个功能：

 - 分发SSH Keys
	 - 移入CCM
 - 对于PV的Admission Controller
	 - 在kubelet中实现
## 3.3 kubelet的重构策略
kubelet需要增加一个新功能：在CCM还未初始化kubelet所在节点时，需标记此节点类似“NotReady”的状态，防止scheduler调度pod到此节点时产生一系列错误。此功能通过给节点加上如下Taints并在CCM初始化后删去此Taints实现：
```
node.cloudprovider.kubernetes.io/uninitialized=true:NoSchedule
```
# 四、Cloud Controller Manager解析
## 4.1 Cloud Controller Manager架构
按照第三节所述进行重构后，新的模块Cloud Controller Manager将作为一个新的组件直接部署在集群内，如下图所示：
![image](https://user-images.githubusercontent.com/8265961/52255325-0e42f300-294d-11e9-8465-200594797af2.png)

CCM组件内各小模块的功能与原先Cloud Provider的差不多，见第二节对Cloud Provider的解析。
对于云厂商来说，需要：
（1）实现cloudprovider.Interface接口的功能，这部分在Cloud Provider中已经都实现，直接迁移就行。
（2）实现自己的Cloud Controller Manager，并在部署k8s时，把CCM按要求部署在集群内，部署时的注意事项及部署参考实践见第五节。
## 4.2 Cloud Controller Manager实现举例
实现自己的CCM也比较简单，举例如下：
```golang
package main

import (
	...
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
			// (4) Run里会运行相关controller loops：CloudNode Controller、PersistentVolumeLabel Controller、Service Controller、Route Controller
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
# 五、部署使用Cloud Controller Manager实践
## 5.1 总体要求

 - 云厂商提供给CCM的API需要有认证鉴权机制，防止恶意行为。
 - 因为CCM运行在集群内，所以需要RBAC规则去跟kube-apiserver通讯
 - CCM为了高可用，可开启选主功能

## 5.2 k8s相关组件的启动配置变化
将Cloud Provider改为CCM后，相关组件启动的配置需要修改。
### 5.2.1 kube-controller-manager启动配置变化
不指定cloud-provider。
### 5.2.2 kube-apiserver启动配置变化
（1）不指定cloud-provider
（2）admission-control中删去PersistentVolumeLabel，因为CCM将接手PersistentVolumeLabel
（3）admission-control中增加Initializers
（4）runtime-config中增加admissionregistration.k8s.io/v1alpha1
### 5.2.3 kubelet启动配置变化
指定cloud-provider=external，告诉kubelet，在它开始调度工作前，需要被CCM初始化。（node会被打上   Taints:node.cloudprovider.kubernetes.io/uninitialized=true:NoSchedule）
## 5.3 启动CCM举例
### 5.3.1 启用initializers并添加InitializerConifguration
CCM为了给PV打标签需要：
（1）启用initializers（https://kubernetes.io/docs/reference/access-authn-authz/extensible-admission-controllers/#enable-initializers-alpha-feature）
（2）添加InitializerConifguration：persistent-volume-label-initializer-config.yaml如下：
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
### 5.3.2 创建CCM的RBAC
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
  - '*'

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
### 5.3.3 启动CCM
可以通过DaemonSet或者Deployment的方式启动CCM：
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
 - [Kubernetes Cloud Controller Manager](https://kubernetes.io/docs/tasks/administer-cluster/running-cloud-controller/)
 - [Refactor Cloud Provider out of Kubernetes Core](https://github.com/kubernetes/community/blob/master/contributors/design-proposals/cloud-provider/cloud-provider-refactoring.md)
 - [Cloud Providers](https://kubernetes.io/docs/concepts/cluster-administration/cloud-providers/)