---
title: LMCache：LLM推理的KV Cache管理层
parent: LLM
---

# LMCache：LLM推理的KV Cache管理层

> 项目地址：[https://github.com/LMCache/LMCache](https://github.com/LMCache/LMCache)

## 一、项目概述

LMCache 是专为 LLM 推理设计的 **KV Cache 管理层**。它的核心思想是将 KV Cache 从"临时状态"升级为可复用的"AI 原生知识"——支持持久化存储、跨引擎共享、生产级可观测，以及灵活的 KV 变换能力。

**解决的核心问题**：LLM 推理中 KV Cache 生命周期短、利用率低。在多轮对话、RAG、Agentic 等重复上下文场景下，每次请求都需要重新 Prefill，浪费大量计算资源。

**主要收益**：
- 降低 TTFT（Time-to-First-Token，首 Token 延迟）
- 提升推理吞吐量
- 减少重复 Prefill 计算开销

**许可证**：Apache 2.0 | **所属**：独立开源项目，已加入 PyTorch Foundation，由 [Tensormesh](https://www.tensormesh.ai/) 支持

---

## 二、核心特性

### 1. 引擎无关的独立部署

LMCache 以**独立 Daemon 进程**运行，与推理引擎进程完全解耦。推理引擎崩溃后 KV Cache 不会丢失（无命运共担），同时多个推理引擎实例可共享同一个 LMCache 层。

### 2. 持久化分层 KV Cache 卸载与复用

KV Cache 可按需迁移到多级存储层次，实现跨请求、跨 Session、跨引擎实例的复用：

```
GPU 显存
   ↓ offload
CPU 内存
   ↓ offload
本地 SSD
   ↓ offload
远程存储（Redis / S3 / Mooncake / ...）
```

**支持的存储后端**：CPU RAM、本地 SSD、Redis/Valkey、Mooncake、InfiniStore、S3 兼容对象存储、NIXL、GDS

### 3. 生产级可观测性

- **Kubernetes 指标**：健康监控、性能诊断
- **KV Cache 专属指标**：前缀命中率（request/token 粒度）、Cache 生命周期、传输性能
- **管理指标**：用户级用量统计

### 4. 非前缀 KV 复用（CacheBlend）

传统前缀缓存只能复用 prompt 前缀匹配的 KV 块。LMCache 基于 **CacheBlend** 技术，允许在 prompt 任意位置复用 KV 块，通过选择性重计算少量 token 来恢复生成质量，显著提升 RAG 场景下的缓存命中率。

### 5. PD 分离与 KV 传输

支持 Prefill Worker 与 Decode Worker 独立扩展，两者之间的 KV Cache 通过以下传输层迁移：

- **NVLink**（同机多卡）
- **RDMA**（跨机高速网络）
- **TCP**（通用网络，通过 NIXL）

### 6. 可插拔 KV 变换

提供灵活的 **SERDE 接口**，研究者和开发者可自定义：
- KV 压缩（量化、token dropping）
- 自定义序列化格式
- KV 流式传输（CacheGen）

---

## 三、架构设计

### 部署模式

| 模式 | 说明 |
|------|------|
| 单引擎 + LMCache Daemon | 最简部署，KV Cache 持久化到本地 |
| 多引擎共享 LMCache | 多实例共享 KV，适合负载均衡场景 |
| PD 分离架构 | Prefill/Decode 独立扩展，通过 KV 传输连接 |
| 多节点 P2P | 跨机 CPU 内存共享 KV Cache |

### 代码结构

```
lmcache/
├── v1/                   # 新版核心逻辑（含多进程 MP 架构）
├── integration/          # 推理引擎集成（vLLM 等）
├── storage_backend/      # 存储后端实现
├── lmcache_frontend/     # 前端接口层
├── sdk/                  # Python SDK
├── cli/                  # 命令行工具
├── observability.py      # 可观测性指标核心
└── connections.py        # 连接管理

rust/                     # Rust 高性能存储操作
csrc/                     # C++/CUDA 扩展
operator/                 # Kubernetes Operator
benchmarks/               # 性能基准测试
```

---

## 四、生态集成

### 已集成推理引擎
- **vLLM**（含 V1，支持多模态模型）
- **NVIDIA Dynamo**

### 合作伙伴与采用方

| 类别 | 厂商/项目 |
|------|----------|
| 硬件 | NVIDIA、AMD（MI300X）、Arm、Ascend |
| 云/基础设施 | CoreWeave |
| 应用层 | Cohere（通过 CoreWeave） |
| 存储 | Redis、Mooncake、InfiniStore、S3 |

---

## 五、快速开始

```bash
pip install lmcache
```

以 vLLM 为例，启动带 LMCache 的推理服务：

```bash
# 启动 LMCache Daemon
lmcache_server start

# 启动 vLLM（通过环境变量或配置文件指定 LMCache）
LMCACHE_CONFIG_FILE=lmcache_config.yaml vllm serve meta-llama/Llama-3-8B
```

- 文档：[https://docs.lmcache.ai](https://docs.lmcache.ai)
- 博客：[https://blog.lmcache.ai](https://blog.lmcache.ai)

---

## 六、关键论文

| 论文 | 发表于 | 关键贡献 |
|------|--------|----------|
| [LMCache: An Efficient KV Cache Layer for Enterprise-Scale LLM Inference](https://arxiv.org/abs/2510.09665) | arXiv 2025 | 整体框架设计 |
| [CacheGen: KV Cache Compression and Streaming](https://dl.acm.org/doi/10.1145/3651890.3672274) | SIGCOMM 2024 | KV 压缩与流式传输 |
| [CacheBlend: Fast LLM Serving for RAG with Cached Knowledge Fusion](https://dl.acm.org/doi/10.1145/3689031.3696083) | EuroSys 2025 | 非前缀 KV 复用 |

---

## 七、近期重要进展

| 时间 | 事件 |
|------|------|
| 2026/05 | AMD MI300X Agentic 工作负载 benchmark 发布 |
| 2026/04 | 新多进程（MP）架构发布，MoE 推理性能提升约 10x |
| 2026/01 | 多节点 P2P CPU 内存共享功能进入生产 |
| 2025/10 | 加入 PyTorch Foundation，Tensormesh 对外发布 |
| 2025/09 | NVIDIA Dynamo 正式集成 LMCache |
| 2025/08 | GitHub Stars 突破 5,000 |
| 2025/07 | 与 Redis 合作，支持 Redis 作为 KV Cache 后端 |

---

## 八、总结

LMCache 将 KV Cache 从推理引擎的内部临时状态提升为独立管理的持久化资源，形成了一个"KV Cache 即服务"的中间层。其独立进程架构保证了与推理引擎的松耦合，可插拔存储后端提供了充分的灵活性，而 CacheBlend 等技术则将缓存复用能力从简单的前缀匹配扩展到了更通用的场景。对于需要处理大量重复上下文的生产 LLM 应用，LMCache 是值得关注的基础设施组件。
