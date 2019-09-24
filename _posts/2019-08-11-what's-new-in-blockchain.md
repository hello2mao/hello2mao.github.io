---
layout: post
title: "区块链技术追踪"
subtitle: "what's new in blockchain"
date: 2019-08-11 10:51:11
author: "hello2mao"
header-img: "img/eye.png"
tags:
    - blockchain
---

## [Randomness in blockchain protocols](https://nearprotocol.com/blog/randomness-in-blockchain-protocols/)

本文将介绍分布式随机信标的基础知识，说明为何朴素的技术方案无法达成效果，然后介绍 RANDAO、DFinity以及 NEAR 协议所采用的随机信标方案，并逐一剖析其优越性与不足之处。

- RANDAO：网络中的所有人首先各自私下选定某个随机数，然后向 RANDAO 提交该随机数的承诺，接着所有人根据一定的共识算法从所有的承诺中选定一组；在参与者揭示这组承诺背后的随机数之后，大家对该组随机数达成共识；最后这组随机数进行异或操作得到的结果就是一轮 RANDO 协议产生的随机数。
- RANDAO + VDFs：替换掉最后的那个异或计算，将其改变为执行时间必定长于各方随机数揭露等待期的操作。
- 门限签名：系统中参与者首先对某则他们未来会进行签名的信息达成一致（可以是 RANDAO 的输出，也可以是最近一个区块的哈希，只要是每次都不一样的值就可以了），然后就此产生一个聚合签名作为随机数。
- RandShare：RandShare 是一个无偏见且不可预测的协议，支持 1/3 恶意节点容错。原理：略。
- NEAR 方案：NEAR 能保证 2/3 恶意节点的容错。原理：略。当前 NEAR 协议架构已经应用了类似的纠删码思想，系统中区块生产者会在特定时期内创建一些分块，其中包含了对于某一特定分片的所有交易，然后将分块进行纠删码编码后的版本附带默尔克证明发送给其它区块生产者，以保证数据可用性。

## [Getting the most out of CREATE2](https://blog.openzeppelin.com/getting-the-most-out-of-create2/)

本文深入探讨 CREATE2 操作码及其在反事实实例化（counterfactual instantiation）以及用户引导中的应用，介绍了如何将 CREATE2 与初始化程序、代理以及元交易等不同的技术相结合并投入应用。这些技术为创建用户身份开辟了新的方法，甚至能让我们在创建身份之前快速迭代并修复漏洞。

- 利用外部账户（EOA）或者使用原生 CREATE 操作的合约账户创建一个合约，很容易就能确定被创建合约的地址：合约地址 = hash (发送者地址, nonce )
- 反事实实例化 是在广义状态通道的背景下逐渐流行起来的概念。它指的是创建一个还未部署上链，但满足有可能部署上链这一事实条件的合约。
- CREATE2 是在君士坦丁堡硬分叉过程中引入的新操作码，用来替代原来的 CREATE 操作码。两种操作码的主要区别在于合约地址的计算方法。新的操作码不再依赖于账户的 nonce ，而是对以下参数进行哈希计算，得出新的地址：合约创建者的地址、作为参数的混淆值（salt）、合约创建代码。这些参数都不依赖于合约创建者的状态。这意味着你可以尽情创建合约而无需考虑 nonce，同时还可以在有需要的时候将这些合约部署到被保留的地址。
- 在该操作码中添加代理可以降低我们的部署成本，同时还能将对身份合约所用的逻辑合约的选择推迟到实际需要之时。这可以让我们更加灵活地对身份合约的实现进行快速迭代，并确保用户无论在何时保留的身份合约地址，都会被直接引导至最新版本。

## [A tweet storm explaining the history and state of Ethereum’s Casper research](https://medium.com/taipei-ethereum-meetup/history-and-state-of-ethereums-casper-research-85e8fba26002)

本文解释了以太坊 Casper 共识协定的研究历史/脉络/进展，包括 FFG vs CBC 的争论、混合模型到完全 PoS 的转换、随机性扮演的角色、共识机制的设计考量以及其他议题。

## [Versionless Ethereum Virtual Machine](https://that.world/~essay/nevm/)

本文为 Parity 开发者 Wei Tang 写作的，关于如何增强 EVM 后向兼容性的文章，改进 Gas 机制的方案堪称大胆。

-   无效操作码：，在执行合约创建的状态转变函数（或者说给状态添加合约代码）之前，为合约部署添加一个验证过程。
-   功能调查（Feature Probe）：定义一种新的操作码 HAS_FEATURE。
-   例外与捕捉（Exceptions and Trap）：所有调用框架的所有执行过程中、消耗任意 gas 的时候、甚至被当前的状态函数回滚变更的时候，都可以有 trap
-   Gas 消耗量：将 EVM 内所有关于 Gas 消耗量的公开信息都移除。

## [Security Budget in the Long Run](http://www.truthcoin.info/blog/security-budget/)

本文旨在探讨比特币抵御 51% 攻击的能力（即比特币的 “安全预算”）。矿工之间的激烈竞争使得系统无法从一个网络中征收足以维持安全预算的交易费用，因此我们应该从所有的支付市场中取得交易费。

本文作者认为：

-   抵御 51% 攻击需要比特币有高 “安全预算”。今时今日的交易费收入远远不够，我们必须确保未来交易费总额能提高到足以给付 “安全预算”。
-   虽然提高价格（例如提高 聪/字节 的交易费率）一定程度可以提高收入，但很不幸由于诸多链之间的竞争，盲目提高交易费率只会遭到市场的反扑，适得其反。
-   更好的方法是一口吞下整个支付市场，把所有的交易费收入收入囊中。利用合并侧链挖矿，可以在不损害去中心化的前提下达成这一目标。

## [Rethinking Sharding and Smart Contracts For Maximizing Blockchain Throughput](https://medium.com/swlh/rethinking-sharding-and-smart-contracts-for-maximizing-blockchain-throughput-acb7f5d32063#_ftn3)

本文作者认为，作为一种已经假定可用的分片共识算法，一个突出的问题在于如何使用这些技术。  
智能合约天然地会将交易串行化，除非使用复杂的 SIMD 类型的解决方案，就只能通过使用多个隔离的智能合约来提供扩展性。即使使用这一方案，每个智能合约的吞吐量仍会受到单个分片的吞吐量限制。  
通过重新让用户账户来包含状态信息，并使用 XBOM 模型，DataGrid 区块链提供了一种提升分片可扩展性的解决方案——根据账户的数量和账户之间的不相交交易来进行扩展。除了支持继承和实时代码重用以外，我们认为这是一个对于区块链扩展性问题的重要的解决方案。

## [Dynamic Mediation Fees in Raiden Explained](https://medium.com/raiden-network/dynamic-mediation-fees-in-raiden-explained-dbc29f032e4b)

本文为雷电团队介绍雷电支付网络通道的手续费制度的文章。

-   在这一制度中，资金发送方不仅要按转移资金的数量为路由沿线节点支付一般意义上的手续费，还必须为改变节点通道容量的大小支付另一类手续费。
-   调解费可以激励用户通过参与转账调解来创建并维护一个健康的网络。用户可以通过调解转账来收回参与网络的全部（或部分）成本，甚至于还有可能实现盈利。
-   在初步实现中，失衡费用可以用一个二次函数来描述，失衡费用的机制就是希望每一个通道都处在理想状态。我们认为，这样设计的失衡费用可以作为大多数用户的默认选择，而且在更多样的策略实现之前也是个不错的起点。
-   长远来看，失衡费用对每个调解节点来说都是一场零和博弈：纵观整个支付情况，调解费中的失衡费用部分并不会抬高总成本，因为它增加的费用和减少的费用相当。因此，从整体的转账情况来看，引入失衡费用既不会抬高也不会降低成本。然而，失衡费用确实会改变所有转账之间的费用分配情况，激励那些能够改进通道状态的转账，从而改进通道状态的平均水平。

## [It’s the settlement assurances, stupid](https://medium.com/@nic__carter/its-the-settlement-assurances-stupid-5dcd1c3f4e41)

本文讨论了影响区块链交易结算的关键因素。

笔者认为不能将一笔交易在工作量证明机制中的结算过程简单看作是交易确认数量的函数，而要将它看成是一个类似木材石化一样的缓慢过程。它会以一个给定的速率进行，并且无法被加速。这个速率由以下变量决定：主要包括记账成本，交易大小和获取其挖矿硬件的难度。一旦结算完成，木材将完全被矿物质给替换并且变得坚如磐石，而不是像原来一样柔软和可塑。关于这块木材的特征将被永远的保留。

## [Lightning Network Routing: Privacy and Efficiency in a Positive-Sum Game](https://medium.com/breez-technology/lightning-network-routing-privacy-and-efficiency-in-a-positive-sum-game-b8e443f50247)

本文将解释支付路由是如何在闪电网络上运行的，有哪些方面依然需要改进，以及路由技术未来的发展方向在哪。

-   闪电网络使用的是源路由和洋葱路由。通过源路由，源节点负责计算从源到目的地的整条路径。闪电网路还实行洋葱路由，极大地提高了隐私性。
-   交易都是有时限的，达成速度不够快的话就会失效，因此提高交易传播速度加快也会提高闪电网络的效率。
    基于概率的任务控制（PBMC）通过了解之前的成功支付案例来解决这一问题。
    每个节点一开始都有一个默认的成功率，并根据实际的转账完成率有所增减。网络路由的支付交易越多，任务控制组件就越了解这个网络的特性，将来就能更好地规划付款路径。

路由技术的未来:

-   蹦床支付:将路由外包给“蹦床节点”，减少移动节点在数据量和计算量上的负担。
-   蚂蚁路由:消除闪电网络中的等级划分，让所有节点执行所有功能。

## [4 Eras of Blockchain Computing: Degrees of Composability](https://a16z.com/2018/12/16/4-eras-of-blockchain-computing-degrees-of-composability/)

从 “可组合度” 看区块链计算的 4 个时代:

-   计算器时代——面向特定应用，可组合性有限
-   大型机时代——图灵完备，可组合性高
-   服务器时代——面向特定应用，不具备可组合性
-   云时代——图灵完备，具备可扩展的可组合性

比特币对应计算器时代，它是一个面向特定应用问题的全栈解决方案：健全货币或者说数字黄金。
除了具备追踪余额和转账等简单的功能之外，比特币还提供了一种脚本语言，可以用来构建更复杂的功能。

以太坊是大型机时代，是构建在由比特币开创的核心理念上的，并纳入了图灵完备的虚拟机来使区块链计算机变得更加通用。
这意味着开发者可以在去中心化的机器网络中部署和运行任何程序。

Polkadot 和 Cosmos 这类项目是服务器时代，愿景就是构建一条多重的异构链——其中每条链都会进行个性化调整来打造定制化应用程序。
Polkadot 的 Substrate 和 Cosmos SDK 都是模块化的区块链构建工具包，用于构建你自己的全栈 “应用程序链（app chain）”。

“服务器时代” 架构的支持者认为，实现了异构区块链之间跨链通信的标准化和抽象化之后，我们将迎来 “云时代”。

## [Solving Identity for the New Decentralized Economy](https://www.tokendaily.co/blog/solving-identity-for-the-new-decentralized-economy)

本文论述了去中心化经济时代如何解决身份问题。
传统的身份识别方法现在已经陷入瓶颈，“自主身份” 的概念逐渐开始流行。

谷歌、Uber、Facebook 和微软等互联网巨头能够在了解更多用户信息的同时成为大型数据聚合平台。
依托于这些互联网平台，数字身份开始占据主导地位。这种转变标志着异构身份的诞生，也就是说，一个身份可以分割成特定的用户行为。

异构 2.0 带来的机遇是基于区块链的身份识别：

-   去中心化的网络认证：去中心化的网络认证能使用户通过其它应用程序或服务的验证，而无需泄漏个人信息。
-   生态系统方法：Blockstack 的设想是，用户在浏览网页时进行过一次身份验证之后无需再次验证。
-   以协议为基础：微软正在采用引入身份覆盖网络（Identity Overlay Network，ION）的开放式标准。
    身份覆盖网络是一种建立在比特币区块链之上的开放式协议。身份覆盖网络是一个管理和锚定去中心化标识符（DID）的通信与批处理层。
    去中心化标识符 （DID）是不可更改的文档，每个文档都用独一无二的 ID 表示，可以存储用户的个人信息。

## [Web3.0 的宏观架构](https://mp.weixin.qq.com/s/dqnZN8GTuxvpcV2fPEXKOg)

现在大家的共识是下一代 Web 一定是去中心化的。
本文讲述了互联网去中心化的过程会面临哪些具体的困难，区块链如何解决这些困难，DApp 为何要实现数据和 App 的分离。

大致分为以下几个部分：

-   为何下一代互联网的未来是去中心化的
-   区块链作为激励层和事实层，如何解决中心化的问题
-   去中心化用户 ID 的工作流程
-   从 App 到 DApp—范式转换

结论：

-   Web3.0 将会是可信互联网，各项基础设施都会根植到区块链中，利用数据的不可篡改性，实现 trust-less 的系统。
-   DAPP 是基础设施，会成为互联网的一部分。
-   数据，其中主要的一个就是每个人的 ID。 未来不会割裂到各个 App 之中了，而是一个完整的符合公开标准的金矿。

## [Overview of Layer 2 approaches: Plasma, State Channels, Side Chains, Roll Ups](https://nearprotocol.com/blog/layer-2/)

本文是对 Layer 2 方案的概览，讲述了 Plasma、状态通道、侧链以及 Roll Up，深入探究了这些方案各自的技术细节和优缺点。

#### Plasma

Plasma 是一种可以实现 “无监管” 侧链的技术，换言之，即使侧链（通常被称为 “plasma 链”）上所有验证者串谋起来作恶，plasma 链上的资产也是安全的，而且可以退回主链。

Plasma 最大的优点是存储在 plasma 链上的代币安全性很高。缺点在于，在转移代币之时必须提供该代币的完整历史，另外就是退出机制非常复杂（以及相关的推论过程）。

#### 状态通道

支付通道是状态通道的一个具体实例，一旦参与者中有一方想要停止使用支付通道，可以执行 “退出” 操作。

这种 “退出” 模式存在一个问题，即主链无法验证支付通道是否提交了全部交易，也就是说，在提交了状态更新之后是否不再出现新的状态更新。

#### 侧链

侧链的核心思路是构建一条完全独立的区块链，有自己的验证者和运营者，可以与主链互相转移资产，而且会选择性地将区块头的快照发送至主链，从而防止分叉产生。

虽然侧链可以利用主链的安全性来防止分叉，但是验证者依然可以通过串谋来发动另一种叫做 无效状态转换 的攻击。
如果有超过 50% 或 66%（取决于侧链的架构）的验证者串谋的话，他们可以创建一个完全无效的区块，窃取其他参与者的资产，并将这个区块的快照发送至主链，发起并完成一个“退出”交易，就可以成功偷走这些资产。

#### Roll Up

Roll Up 实际上是一条侧链，因此它会生成区块，并且将这些区块的快照发送到主链上。不过，Roll Up 上的运营者是无需信任的。

此 L2 方案没有显著扩大调用数据中的存储空间，但是实际消耗的可写存储量是恒定的（而且非常小），链上验证的 gas 成本低至 1k gas/tx ，是主链交易的 1/21 。

## [Geth v1.9.0: Six months distilled](https://blog.ethereum.org/2019/07/10/geth-v1-9-0/)

geth v1.9.0 刚刚发布了。该版本包含了大量的更新。

-   性能得到全面提高。LevelDB 的 GO 语言实现，账户/存储树（trie）的访问模式，EVM 代码的分析和优化、数据库模式的分析和优化，这些是全方位的性能提升。
-   快速同步（Fast Sync）、完全同步（Full Sync）、Archive 节点同步很大优化。
-   Freezer（冷藏室）
    数据库现在分为两部分：
    1. 最近的区块和状态保存在 LevelDB 的快速键值存储中。适合 SSD。
    2. 3 个轮次（epoch）以前的区块和收据被移动到定制的冷藏室数据库中，不会被频繁访问。适合 HDD。
-   GraphQL
    引入了一种全新的基于 GraphQL 的节点查询接口。使用 GraphQL，用户可以只查询他们需要的数据，同时仍保持计算和数据传输的开销最小化。
-   额外的硬件钱包支持，支持 @Ledger Nano X，支持更新的 @Trezor One
-   Geth 现在支持 status keycard
-   Clef: 一个独立的交易签名器。
-   轻客户端的硬编码检查点如今被链上预言机所取代。轻客户端现在可以向轻服务器请求检查点，轻客户端会负责询问链上预言机获得最新检查点。
-   Geth 对其所有子系统和事件的监控都得到了重塑和升级。
-   在新的发现协议准备就绪并推出之前，geth v1.9.0 向旧的发现协议提供了两个 ENR 扩展： 1.可以同时支持两种版本的 IP 协议 2.节点现在可以公布其链的配置，以增加可发现性

## [Blockchain search engine — real-time data for decentralized applications (DApps)](https://blog.secondstate.io/post/20190703-search-engine-overview/)

本文设计了一个区块链搜索引擎，用来对以太坊上的智能合约进行搜索。
此搜索引擎是开源的，见[smart-contract-search-engine](https://github.com/second-state/smart-contract-search-engine)，能够根据关键词、address 等进行搜索。目前已经能够对 953556 个公链上的智能合约进行搜索。

试用地址：https://ethereum.search.secondstate.io/

## [What's New in Eth2](https://notes.ethereum.org/c/Sk8Zs--CQ/https%3A%2F%2Fbenjaminion.xyz%2Fnewineth2%2F20190621.html)

本文是对最近几个月 Ethereum 2.0 进度的综述。

#### Phase 0：信标链

Phase 0 的 Spec 会在 6 月 30 日定稿，详见报道：[Code For Ethereum’s Proof-of-Stake Blockchain to Be Finalized Next Month](https://www.coindesk.com/code-for-ethereums-proof-of-stake-blockchain-to-be-finalized-next-month)

-   互用性
    -   当前有八个团队分别实现了 ETH2 客户端
    -   这个几个客户端需严格按照 Spec 进行通讯
-   网络
    -   ETH2 使用[libp2p](https://github.com/libp2p)作为底层 p2p 通信协议
-   测试
    -   测试进度见：https://www.youtube.com/watch?v=4V-WQ2CnRfA
    -   当前的 Spec 是可以直接执行，方便进行跨客户端的测试
-   形式化验证
    -   ETH2 的存款合约的设计报告见：https://github.com/runtimeverification/verified-smart-contracts/blob/master/deposit/formal-incremental-merkle-tree-algorithm.pdf
-   信标链上线
    -   时间点：DevCon V；把 ETH2 的存款合约部署到 ETH1 上
    -   时间点：2020 年 1 月；发布信标链上线计划

#### Phase 1：分片

发布了 Spec：https://github.com/ethereum/eth2.0-specs/blob/dev/specs/core/1_shard-data-chains.md

#### Phase 2：虚拟机，即执行层

-   有了两个初步的提案：
    -   [Phase One and Done: eth2 as a data availability engine](https://ethresear.ch/t/phase-one-and-done-eth2-as-a-data-availability-engine/5269?u=benjaminion)
    -   [Phase 2 Proposal 1](https://notes.ethereum.org/s/HylpjAWsE#)
-   Phase 2 初步方案：
    -   有很多类型的 EVM，称之为 EE（execution environment），每个 EE 能够运行 eWASM 合约
    -   EE 不存储状态，所有的数据都需要额外提供
    -   支付一定费用，用户可以部署他们自己的 EE 到主链上；ETH2 分片只关注底层架构：交易排序和数据

## [V 神最新演讲：以太坊 2.0 之跨分片交易](https://mp.weixin.qq.com/s/luxI17CINlpJCFwmHJ4_Lg)

2019 年 6 月 29 日，第二届以太坊技术及应用大会在京举行，以太坊创始人 Vitalik Buterin 分享了题为《以太坊 2.0 之跨分片交易》的主题演讲。
讲述了 ETH2.0 设计：

-   1024 个分片
-   信标链管理共识和跨分片通讯
-   每 6 分钟一次跨分片通讯
-   更快的异步交易
-   猛拉（yank）机制
-   通过 plasma 的方式做同步交易

## [Anonymous Zether: Technical Report](https://www.coindesk.com/jpmorgan-adds-new-privacy-features-to-its-ethereum-based-quorum-blockchain)

总部位于纽约的摩根大通透露，他们已经构建了 Zether 协议扩展，这是一种完全去中心化的加密协议，可以用于隐私支付，
而且还能与以太坊和其他智能合约平台兼容，旨在为交易增加一个“匿名层”。
据悉，摩根大通已经在今年 5 月 28 日开放了该扩展协议，而且会与其私有链 Quorum 一起使用。
在基本的 Zether 协议中，账户余额和转账账户信息都是被隐藏的，但参与者的身份并不会被隐藏。

摩根大通针还发布了针对匿名 Zether 的技术报告：Anonymous Zether: Technical Report（https://github.com/jpmorganchase/anonymous-zether/blob/master/docs/AnonZether.pdf）
匿名 Zether 是一个私有的数值追踪系统，在这个系统中，账户的余额都被加密存储在以太坊上。
每一个 Zether 合约在部署时都必须附属到一个已经存在的 ERC20 合约上，在 Zether 合约部署完成后，用户就可以匿名的进行 ERC20 代币的转账和取款。目前匿名 Zether 因为 gas 消耗问题还不能在主网使用。

## [A nearly-trivial-on-zero-inputs 32-bytes-long collision-resistant hash function](https://ethresear.ch/t/a-nearly-trivial-on-zero-inputs-32-bytes-long-collision-resistant-hash-function/5511)

提出了一种 32 位的抗碰撞的 hash 函数：H(l,r)=x；

-   如果 l 和 r 都不等于 0，那个 x=2^240+sha256(l,r)mod2^240
-   如果 l 和 r 都等于 0，那么 x=0
-   如果 l>=2^255 或 r>=2^255 或者 l<2^240 或者 r<2^240，那么 x=2^240+sha256(l,r)mod2^240

此 hash 函数用在 M 树时，有 5 倍复杂度的降低和 4 倍空间利用率的提高。

## [StarkDEX: Bringing STARKs to Ethereum](https://blog.0xproject.com/starkdex-bringing-starks-to-ethereum-6a03fffc0eb7)

Starkware 和 0x 发布了一个使用 STARKs 技术的 POC [demo](https://www.starkdex.io/)，
此 demo 目前运行在 Ropsten 测试网络上，使用 STARKs 技术后，TPS 可以达到 550，且每笔交易只需大约 6000gas，节省了 200 倍的 gas 消耗。
STARKs（可扩容的透明知识论证）是创建一种证明的技术，这项证明中 f(x)=y，其中 f 可能要花很长的时间来进行计算，但是这个证明可以被很快验证。STARK 是“双重扩容”：对于一个需要 t 步骤的计算，这会花费大约 O(t \* log(t))步骤才能完成这个证明，这可能是最优的情况，而且这需要通过~O(log2(t))个步骤才能验证，对于中等大小的 T 值，它比原始计算快得多。

## [Work to natively integrate Eth1 into Eth2](https://ethresear.ch/t/work-to-natively-integrate-eth1-into-eth2/5573)

此文概括了把 ETH1 的共识以及世界状态集成进 ETH2 分片内的可能工作量。
一共列举了 23 个点，包括：BLS VS ECDSA；Keccak256 vs SHA256；SSZ vs RLP；轻客户端中使用 POS 的信标链代替 POW；移除难度炸弹；使用 libp2p 代替 devp2p；移除 ETH1 的 gasLimit 等。

## [Ethereum 2.0 Deposit Merkle Tree](https://medium.com/@josephdelong/ethereum-2-0-deposit-merkle-tree-13ec8404ca4f)

本文介绍了 ETH 2.0 中存款树使用的数据结构 Sparse Merkle Tree，简称 SMT，它是默克尔数的变种，详见：https://eprint.iacr.org/2016/683.pdf。
因为 SMT 在计算根 hash 的过程能够显著的减少内存占用，ETH 2.0 的存款树 Deposite Tree 选择使用 SMT。

## [Formal Verification of Ethereum 2.0 Deposit Contract](https://runtimeverification.com/blog/formal-verification-of-ethereum-2-0-deposit-contract-part-1/)

本文是对 ETH 2.0 存款合约的形式化验证。

在 ETH 2.0 的信标链上，验证者是动态加入和退出的，他们只需向存款合约发起一笔交易存入一定数量的 ETH 就可以成为验证者，然后在退出验证者角色时，存款合约会把抵押退还。因为验证者的列表存在默克尔树上，在动态更新验证者时，默克尔树都需要重构，当验证者数量巨大时，这将非常耗时以及消耗内存空间。
为了减少时间和空间的消耗，同时节省 gas，存款合约实现了一种增量默克尔树算法，详见：https://github.com/ethereum/research/blob/master/beacon_chain_impl/progressive_merkle_tree.py。
在重构一棵高度是 h 的默克尔树时，增量默克尔树算法具有 O(h)的时间和空间复杂度。
