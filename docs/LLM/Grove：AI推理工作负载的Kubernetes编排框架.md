---
title: Grove：AI推理工作负载的Kubernetes编排框架
parent: LLM
---

# Grove：AI推理工作负载的Kubernetes编排框架

> 项目地址：[https://github.com/ai-dynamo/grove](https://github.com/ai-dynamo/grove)

## 一、项目概述

**Grove** 是由 NVIDIA Dynamo 团队开源的 Kubernetes API 框架，口号是 **"One API. Any inference architecture."**（一套 API，任意推理架构）。

它提供了一个统一的声明式接口，用于编排各类 AI 推理工作负载——从简单的单 Pod 部署，到跨数万张 GPU 的复杂多节点分离式（Disaggregated）推理系统。

**解决的核心问题**：原生 Kubernetes 缺乏对现代 AI 推理工作负载的直接支持：

| 问题 | 说明 |
|------|------|
| 多节点/多 Pod 扩缩单元 | 大模型跨多节点分片时，调度单元不再是单个 Pod，而是整组 Pod |
| 层次化 Gang Scheduling | 分离推理的 Prefill/Decode 必须同时就绪才能形成可用流水线 |
| 启动顺序控制 | MPI 等工作负载要求 Worker Pod 全部就绪后才能启动 Leader |
| 拓扑感知调度 | 推理组件间通信密集，需要 NVLink 域内优化布局 |

**许可证**：Apache 2.0 | **所属**：NVIDIA Dynamo 生态

---

## 二、核心概念

Grove 引入了四个核心抽象：

### 1. PodClique（Pod 小组）

**基础构建单元**。代表一组具有相同配置的 Pod，对应推理系统中的某个角色（如 Leader、Worker、Frontend）。类似 ReplicaSet，但增加了 **Gang 终止语义**——整组 Pod 要么全部运行，要么全部终止。

- 支持独立的扩缩策略
- 可单独使用（单节点组件），也可组合到 PodCliqueScalingGroup 中（多节点组件的角色）

### 2. PodCliqueScalingGroup（Pod 小组扩缩组）

**多节点协调单元**。将多个 PodClique 捆绑在一起，作为一个整体进行 Gang 调度和同步扩缩。适用于紧耦合的角色组合，如：

- 多节点推理中的 Leader + Worker
- 分离推理中的 Prefill + Decode（要求两者同时就绪）

扩缩时保持各角色之间预设的副本数比例。

### 3. PodCliqueSet（Pod 小组集合）

**顶层资源对象**。定义完整推理服务的所有组件，管理一组 PodClique 和 PodCliqueScalingGroup 的协同部署与共同调度。PodCliqueSet 的副本（replicas）用于系统级扩缩：

- A/B 测试 / 灰度发布
- 跨可用区高可用部署
- 完整服务实例的水平扩展

### 4. PodGang（Pod 帮）

**调度器 API**。定义 Gang 调度的最小单元。PodGang 是多组同类 Pod 的集合，每组定义参与 Gang 调度所需的最小副本数。由 Grove 自动创建和管理，用户通常无需直接操作。

---

## 三、三层扩缩体系

Grove 提供三个独立的扩缩层次，满足不同粒度的运维需求：

```
PodCliqueSet (replicas)          ← 系统级：复制整套推理服务
    └── PodCliqueScalingGroup (replicas)   ← 组件级：扩缩多节点组件实例
            └── PodClique (replicas)       ← 角色级：调整某一角色的 Pod 数量
```

| 扩缩层次 | 命令示例 | 适用场景 |
|---------|---------|---------|
| PodCliqueSet | `kubectl scale pcs my-service --replicas=2` | 灰度发布、跨区高可用 |
| PodCliqueScalingGroup | `kubectl scale pcsg my-service-0-sga --replicas=2` | 增加多节点组件实例容量 |
| PodClique | `kubectl scale pclq my-service-0-worker --replicas=4` | 微调单一角色 Pod 数量 |

---

## 四、典型使用场景

### 场景一：单节点聚合推理（传统部署）

每个 Pod 是一个完整的模型实例，包含完整的推理流水线。

```yaml
apiVersion: grove.io/v1alpha1
kind: PodCliqueSet
metadata:
  name: single-node-aggregated
spec:
  replicas: 1
  template:
    cliques:
    - name: model-worker
      spec:
        replicas: 2
        podSpec:
          containers:
          - name: model-worker
            image: my-llm-server:latest
```

### 场景二：单节点分离推理（Prefill/Decode 分离）

将 Prefill 和 Decode 拆分到不同的 Worker，独立扩缩：

```yaml
apiVersion: grove.io/v1alpha1
kind: PodCliqueSet
metadata:
  name: single-node-disaggregated
spec:
  replicas: 1
  template:
    cliques:
    - name: prefill
      spec:
        replicas: 3
        podSpec:
          containers:
          - name: prefill
            image: my-prefill-server:latest
    - name: decode
      spec:
        replicas: 2
        podSpec:
          containers:
          - name: decode
            image: my-decode-server:latest
```

### 场景三：多节点分离推理（大模型，如 DeepSeek-R1、Llama-4-Maverick）

通过 PodCliqueScalingGroup 将 Leader 和 Worker 捆绑为 Gang 调度单元：

```
PodCliqueSet
└── PodCliqueScalingGroup (prefill-group)
│   ├── PodClique: prefill-leader (1 Pod)
│   └── PodClique: prefill-worker (N Pods)
└── PodCliqueScalingGroup (decode-group)
    ├── PodClique: decode-leader (1 Pod)
    └── PodClique: decode-worker (M Pods)
```

### 场景四：Agentic Pipeline

多模型串联的 Agent 流水线，每个模型作为独立的 PodClique，通过 PodCliqueSet 统一管理生命周期。

---

## 五、架构与实现

### 项目结构

```
grove/
├── operator/           # Kubernetes Operator（Go）
│   ├── api/           # CRD 定义（PodClique、PodCliqueSet 等）
│   ├── internal/      # 控制器逻辑
│   └── samples/       # 示例 YAML
├── scheduler/          # 自定义调度器插件
│   └── api/           # PodGang API 定义
└── docs/              # 文档
    ├── quickstart.md
    ├── installation.md
    └── user-guide/
```

### 关键组件

**Grove Operator**：
- 监听 PodCliqueSet 等 CR 的变化
- 自动创建和管理 PodClique、PodCliqueScalingGroup、PodGang 等子资源
- 负责 Rolling Updates（滚动升级）和生命周期管理
- 集成 HPA（Horizontal Pod Autoscaler）支持多级自动扩缩

**Grove Scheduler Plugin**：
- 实现 Gang Scheduling 逻辑
- 确保 PodGang 内所有 Pod 同时被调度（All-or-Nothing）
- 支持拓扑感知放置（NVLink 域、NUMA 节点等）

### 2025 已完成特性

- ✅ 层次化 Gang Scheduling
- ✅ 多级水平自动扩缩
- ✅ 启动顺序控制
- ✅ 滚动更新
- ✅ 拓扑感知调度

### 2026 规划特性

- 资源优化滚动更新
- 拓扑分散约束
- 自动拓扑检测

---

## 六、快速开始

```bash
# 1. 创建本地 kind 集群
cd operator && make kind-up

# 2. 部署 Grove Operator
make deploy

# 3. 部署示例工作负载
kubectl apply -f samples/simple/simple1.yaml

# 4. 查看创建的资源
kubectl get pcs,pclq,pcsg,pg,pod -o wide
```

**资源缩写**：
- `pcs` = PodCliqueSet
- `pclq` = PodClique
- `pcsg` = PodCliqueScalingGroup
- `pg` = PodGang

---

## 七、与生态的关系

Grove 是 **NVIDIA Dynamo** AI 推理生态的基础设施层之一：

```
应用层：推理请求
    ↓
路由层：Dynamo Router / 负载均衡
    ↓
推理引擎：vLLM + LMCache（KV Cache 管理）
    ↓
编排层：Grove（Kubernetes 编排）
    ↓
基础设施：GPU / NVLink / RDMA
```

Grove 处于最底层，负责将推理引擎的所有组件在 Kubernetes 上正确调度、部署和扩缩，它与 **LMCache**（KV Cache 管理层）是互补关系：Grove 解决"如何部署"，LMCache 解决"如何高效利用缓存"。

Grove 也支持与 [Dynamo Planner](https://docs.nvidia.com/dynamo/latest/planner/planner_intro.html) 集成，由 Planner 负责自动扩缩决策，Grove 负责执行。

---

## 八、总结

Grove 的核心价值在于将分离推理（Disaggregated Inference）、多节点大模型部署这类原本需要大量自定义脚本和 Operator 才能实现的复杂场景，抽象为简洁的 Kubernetes 原语。

**三个关键设计决策**：
1. **声明式 API**：用户只需描述"我要什么"，不用关心"怎么做"
2. **层次化 Gang Scheduling**：同时满足多节点模型和 PD 分离两个层次的调度约束
3. **三层扩缩模型**：角色级、组件级、系统级三个独立维度，粒度匹配不同运维需求

对于在 Kubernetes 上部署 LLM 推理服务——尤其是涉及多节点张量并行或 Prefill/Decode 分离架构——Grove 提供了一套完整且生产级的编排解决方案，值得重点关注。

---

**延伸阅读**：
- [Grove 核心概念文档](https://github.com/ai-dynamo/grove/blob/main/docs/user-guide/01_core-concepts/01_overview.md)
- [Grove API 参考](https://github.com/ai-dynamo/grove/blob/main/docs/api-reference/operator-api.md)
- [NVIDIA Dynamo 文档](https://docs.nvidia.com/dynamo/latest/)
