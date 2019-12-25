
<!-- TOC -->

- [1. Mempool](#1-mempool)
- [2. MP.1](#2-mp1)

<!-- /TOC -->

# 1. Mempool

`Mempool`用于缓存未打包的合法交易，类似于以太坊的`TxPool`。

如下图所示：

![](/img/posts/libra-mempool.png)

# 2. MP.1

当用户提交的交易经过`Admission Control`简单校验后，就会提交给`Mempool`（对应流程MP.1）缓存起来供后续的打包上链。

在流程MP.1中，AC一共会调用Mempool两个gRPC接口，