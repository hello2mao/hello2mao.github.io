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

**许可证**：Apache 2.0 | **语言**：Go | **所属**：NVIDIA Dynamo 生态

---

## 二、核心概念

Grove 引入了四个核心抽象，构成完整的层级体系：

```
PodCliqueSet (PCS)                  ← 顶层资源，定义完整推理服务
├── PodGang                         ← 调度器层：定义 Gang 调度单元（自动创建）
├── PodClique（独立）                ← 单节点组件（路由层、Cache 等）
└── PodCliqueScalingGroup (PCSG)    ← 多节点组件分组（Prefill/Decode）
    └── PodClique（多个角色）        ← leader/worker 等
```

### 1. PodClique（`pclq`）

**基础构建单元**。代表一组具有相同配置的 Pod，对应推理系统中的某个角色（如 Leader、Worker、Frontend）。类似 ReplicaSet，但增加了 **Gang 终止语义**。

关键字段：
- `roleName`：角色名称（prefill/decode/leader/worker 等）
- `replicas` / `minAvailable`：副本数与最小可用数（Gang 调度和终止阈值）
- `startsAfter`：显式启动依赖（DAG 定义，用于控制启动顺序）
- `scaleConfig`：HPA 自动扩缩配置

### 2. PodCliqueScalingGroup（`pcsg`）

**多节点协调单元**。将多个 PodClique 捆绑在一起，作为一个整体进行 Gang 调度和同步扩缩。适用于紧耦合的角色组合，如：

- 多节点推理中的 Leader + Worker
- 分离推理中的 Prefill + Decode（要求两者同时就绪）

扩缩时保持各角色之间预设的副本数比例。

### 3. PodCliqueSet（`pcs`）

**顶层资源对象**。定义完整推理服务的所有组件，支持系统级扩缩（多副本）用于 A/B 测试、灰度发布、跨可用区高可用。

### 4. PodGang（`pg`）

**调度器 API**。定义 Gang 调度的最小单元——一组 Pod 集合，每组定义参与 Gang 调度的最小副本数（`MinReplicas`）。由 Grove 自动创建，用户通常无需直接操作。

PodGang 状态：
- `Phase`：Pending → Starting → Running
- `PlacementScore`：网络拓扑最优性评分（1.0 为最优，可用于监控调优）

---

## 三、三层扩缩体系

```
PodCliqueSet (replicas)                      ← 系统级：复制整套推理服务
    └── PodCliqueScalingGroup (replicas)     ← 组件级：扩缩多节点组件实例
            └── PodClique (replicas)         ← 角色级：调整某一角色的 Pod 数量
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
spec:
  template:
    cliques:
    - name: prefill
      spec:
        replicas: 3
    - name: decode
      spec:
        replicas: 2
```

### 场景三：多节点分离推理（大模型，如 DeepSeek-R1、Llama-4-Maverick）

通过 PodCliqueScalingGroup 将 Leader 和 Worker 捆绑为 Gang 调度单元：

```yaml
spec:
  template:
    podCliqueScalingGroups:
    - name: prefill-group
      cliqueNames: [prefill-leader, prefill-worker]
      replicas: 2    # 2 个 Prefill 实例
    - name: decode-group
      cliqueNames: [decode-leader, decode-worker]
      replicas: 1
```

### 场景四：Agentic Pipeline

多模型串联的 Agent 流水线，每个模型作为独立的 PodClique，通过 PodCliqueSet 统一管理生命周期。

---

## 五、核心机制深度解析

### 5.1 调度门控机制（Scheduling Gates）

Grove 使用 Kubernetes 1.27+ 的 Scheduling Gates 特性实现 Gang 调度：

1. Operator 创建所有 Pod 时设置 `schedulingGates`（如 `grove.io/podgang`）
2. Pod 被创建但不会被调度（处于 `ScheduleGated` 状态）
3. 当所有 Pod 创建完毕（PodGang `Initialized` 条件为 True）
4. Operator **仅移除自己的** 调度门控，保留其他 Controller（如 Kueue）的门控
5. 调度器开始对全部 Pod 进行 Gang 调度（All-or-Nothing）

> ⚠️ 设计要点：第 4 步"仅移除 Grove 自己的门控"是多租户生产环境下的关键修复，避免与 Kueue 等其他调度 Controller 竞争。

### 5.2 Gang 终止（Gang Termination）

当可运行 Pod 数低于 `MinAvailable` 阈值超过 `TerminationDelay`（默认 4 小时）时，触发 Gang 终止。

**正确的 PCSG 终止公式**：
```
existingReplicas - breachedReplicas < MinAvailable  →  触发终止
```

**双重保护机制**，防止边界情况下的循环操作：
1. **WasPCSGEverHealthy 门控**：仅对曾经健康过的工作负载触发终止，避免对永远无法调度的工作负载持续循环删除
2. **GangTerminationInProgress 标志**：DeleteAllOf 成功后立即设置，防止在终止完成前再次触发

### 5.3 拓扑感知调度

通过 `ClusterTopologyBinding`（集群级别资源）将可移植的 Grove 拓扑域名映射到实际节点标签：

```yaml
apiVersion: grove.io/v1alpha1
kind: ClusterTopologyBinding
metadata:
  name: gpu-fabric
spec:
  levels:                    # 从宽到窄排列
    - domain: zone
      key: topology.kubernetes.io/zone
    - domain: rack
      key: topology.kubernetes.io/rack
    - domain: host
      key: kubernetes.io/hostname
```

拓扑约束层级继承：**PCS → PCSG → PCLQ**，子层约束必须等于或更严格于父层。

两种约束类型：
- `pack.required`：硬要求，无法满足则不调度
- `pack.preferred`：软偏好，无法满足则回退到更宽泛的域

### 5.4 滚动更新

- **Generation Hash 追踪**：每次 PCS Spec 变更生成 Hash，所有子资源的 label 记录当前 Hash，实现精确的更新进度追踪
- **更新策略**：`RollingRecreate`（默认，维护可用性）或 `OnDelete`（手动触发）
- **ReuseReservationRef 优化**：Rolling Update 时建议复用上次 PodGang 的拓扑预留，节省重新调度的开销

### 5.5 自动 Multi-Node NVLink（Auto MNNVL）

通过注解驱动，自动管理 NVIDIA Multi-Node NVLink：

```yaml
metadata:
  annotations:
    grove.io/mnnvl-group: "my-group"    # 加入 MNNVL 组
    # grove.io/mnnvl-group: "none"      # 显式退出（覆盖父层继承）
```

注解层级继承（PCS → PCSG → PCLQ），每个 MNNVL 组每个 PCS 副本创建一个 `ComputeDomain` 资源，与 NVIDIA DRA Driver 深度集成。

### 5.6 启动顺序（DAG 依赖）

通过 `startsAfter` 字段在 PodClique 间定义 DAG 依赖关系，Webhook 自动检测循环并拒绝非法配置：

```yaml
cliques:
- name: worker
  spec: ...
- name: leader
  spec:
    startsAfter: [worker]    # leader 等 worker 全部就绪后才启动
```

### 5.7 Pod 命名规范

Grove 使用层级化、可读性强的命名，通过 Pod 名称直接反映拓扑层级：

| 场景 | 格式 |
|------|------|
| 独立 PodClique 的 Pod | `<pcs-name>-<pcs-idx>-<pclq-name>-<random>` |
| PCSG 所属 PodClique 的 Pod | `<pcs-name>-<pcs-idx>-<pcsg-name>-<pcsg-idx>-<pclq-name>-<random>` |

---

## 六、架构与实现

### 项目结构

```
grove/
├── operator/                   # Kubernetes Operator（Go）
│   ├── api/core/v1alpha1/     # CRD 定义（PodClique、PodCliqueSet 等）
│   ├── internal/
│   │   ├── controller/        # 各资源的调和器（Reconciler）
│   │   ├── mnnvl/             # Multi-Node NVLink 管理
│   │   ├── scheduler/         # 调度器后端适配（KAI / Volcano）
│   │   └── webhook/           # 准入 Webhook
│   ├── charts/                # Helm Chart
│   └── samples/               # 示例 YAML
├── scheduler/                  # 自定义调度器插件
│   └── api/core/v1alpha1/
│       └── podgang.go         # PodGang 调度 API
└── docs/                      # 文档
```

**调度器后端**：支持 KAI-Scheduler（NVIDIA）和 Volcano，通过 Operator 内部 `scheduler` 包适配，可插拔切换。

### 关键设计创新

1. **Scheduling Gates 的正确性处理**：仅移除 Grove 自己的 Gate，与 Kueue 等其他 Controller 安全共存
2. **层级化命名规范**：通过 Pod 名称直接反映拓扑层级，极大提升可观测性
3. **分层拓扑约束继承**：PCS → PCSG → PCLQ 的约束继承与覆盖，通过 ClusterTopologyBinding 解耦硬件细节
4. **注解驱动的 MNNVL 管理**：不污染核心 API，通过注解 opt-in，与 DRA 深度集成
5. **Generation Hash 驱动的滚动更新**：精确追踪每个子资源的更新进度
6. **双重 Gang 终止保护**：WasEverHealthy + GangTerminationInProgress 防止各类边界情况下的循环操作
7. **PlacementScore**：调度器返回网络最优性评分（1.0 最优），可用于监控和调优

### 路线图

**2025 年已完成**：
- ✅ 层次化 Gang Scheduling
- ✅ 多级水平自动扩缩
- ✅ 启动顺序控制
- ✅ 滚动更新
- ✅ 拓扑感知调度

**2026 年规划**：
- 资源优化滚动更新（Resource-Optimized Rolling Updates）
- 拓扑分散约束（Topology Spread Constraints）
- 自动拓扑检测（Automatic Topology Detection）

---

## 七、快速开始

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

示例工作负载会自动创建：1 个 PodCliqueSet、4 个 PodClique、1 个 PodCliqueScalingGroup、1 个 PodGang 和 9 个 Pod，可以直接观察 Gang Scheduling 效果。

---

## 八、与生态的关系

Grove 是 **NVIDIA Dynamo** AI 推理生态的基础设施编排层：

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

Grove 与 **LMCache**（KV Cache 管理层）互补：Grove 解决"如何部署"，LMCache 解决"如何高效利用缓存"。

Grove 也支持与 [Dynamo Planner](https://docs.nvidia.com/dynamo/latest/planner/planner_intro.html) 集成，由 Planner 负责自动扩缩决策，Grove 负责执行。

---

## 九、总结

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
- [拓扑感知调度文档](https://github.com/ai-dynamo/grove/blob/main/docs/user-guide/topology-aware-scheduling.md)
- [NVIDIA Dynamo 文档](https://docs.nvidia.com/dynamo/latest/)
