---
layout:     post
title:      "解析Quorum -- 摩根大通的企业级区块链解决方案 （一）"
subtitle:   "Quorum overview"
date:       2019-02-12 18:28:25
author:     "hello2mao"
tags:
    - ethereum
---

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
- [概述](#%E6%A6%82%E8%BF%B0)
- [架构](#%E6%9E%B6%E6%9E%84)
- [隐私性](#%E9%9A%90%E7%A7%81%E6%80%A7)
  - [方案概述](#%E6%96%B9%E6%A1%88%E6%A6%82%E8%BF%B0)
  - [案例一](#%E6%A1%88%E4%BE%8B%E4%B8%80)
  - [案例二](#%E6%A1%88%E4%BE%8B%E4%BA%8C)
  - [实现细节](#%E5%AE%9E%E7%8E%B0%E7%BB%86%E8%8A%82)
    - [Quorum组件](#quorum%E7%BB%84%E4%BB%B6)
    - [Tessera组件](#tessera%E7%BB%84%E4%BB%B6)
- [共识算法](#%E5%85%B1%E8%AF%86%E7%AE%97%E6%B3%95)
  - [Raft](#raft)
    - [Lifecycle of a Transaction](#lifecycle-of-a-transaction)
    - [Block Race](#block-race)
    - [Speculative Minting](#speculative-minting)
  - [IBFT](#ibft)
- [节点的许可管理](#%E8%8A%82%E7%82%B9%E7%9A%84%E8%AE%B8%E5%8F%AF%E7%AE%A1%E7%90%86)
- [更高的性能](#%E6%9B%B4%E9%AB%98%E7%9A%84%E6%80%A7%E8%83%BD)
  - [TPS测试](#tps%E6%B5%8B%E8%AF%95)
- [参考](#%E5%8F%82%E8%80%83)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 概述
Quorum是基于以太坊的Golang实现[go-ethereum](https://github.com/ethereum/go-ethereum)开发而来。  详细的可参考如下链接：  
- [Quorum主页](https://www.jpmorgan.com/quorum)
- [Quorum Github](https://github.com/jpmorganchase/quorum)
- [Quorum白皮书](https://github.com/jpmorganchase/quorum-docs/blob/master/Quorum%20Whitepaper%20v0.2.pdf)

在go-ethereum基础上，Quorum主要做了如下扩展：  
- 隐私性（Privacy）
- 共识算法（Alternative Consensus Mechanisms）
- 节点的许可管理（Peer Permissioning）
- 更高的性能（Higher Performance）

# 架构
Quorum有两个组件：  
- [Quorum](https://github.com/jpmorganchase/quorum)节点
- [Tessera](https://github.com/jpmorganchase/tessera)(Java)或者[Constellation](https://github.com/jpmorganchase/constellation)(Haskell)节点，注：建议使用Tessera，Constellation感觉要被废弃

整体架构如下：  
![image](https://user-images.githubusercontent.com/8265961/52621270-d1917180-2ee1-11e9-9944-00f693fa8867.png)
Quorum的本质，是使用密码学技术来防止交易方以外的人看到敏感数据。对于私有交易，会进行加密处理，公链（Quorum chain）上只存储加密后的数据的hash值，而私有交易的数据加密后将存储在链下，通过定制的一个模块（Tessera或者Constellation）在节点间安全的共享。状态数据库被分成私有状态数据库和公开状态数据库两类。网络中所有节点的公开状态，均完美达成状态共识，而私有状态数据库的情况有所不同，将不再保存整个全局私有状态数据库的状态，如下图所示：  
![image](https://user-images.githubusercontent.com/8265961/52621869-5335cf00-2ee3-11e9-8c90-0bc43290e3cf.png)

# 隐私性
## 方案概述
（1）公开合约、交易  
与go-ethereum基本保持一致

（2）私有合约、交易  
通过privateFor参数标识合约为私有合约，参数的值为私有合约的其他参与者的公钥，如下所示：  
```
var simple = simpleContract.new(42, {from:web3.eth.accounts[0], data: bytecode, gas: 0x47b760, privateFor: ["ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="]}, function(e, contract) {
  if (e) {
    console.log("err creating contract", e);
  } else {
    if (!contract.address) {
      console.log("Contract transaction send: TransactionHash: " + contract.transactionHash + " waiting to be mined...");
    } else {
      console.log("Contract mined! Address: " + contract.address);
      console.log(contract);
    }
  }
});
```

通过privateFor参数标识交易为私有交易，参数的值为私有交易的其他参与者的公钥，如下所示：  
```
{
    "jsonrpc": "2.0",
    "method": "eth_sendTransaction",
    "params": [
        {
            "from": "$FROM_AC",
            "to": "$TO_AC",
            "data": "$CODEHASH",
            "privateFor": [
                "$PUBKEY1,PUBKEY2"
            ]
        }
    ],
    "id": "$ID"
}
```

## 案例一
七个节点的例子：[7nodes](https://github.com/jpmorganchase/quorum-examples/tree/master/examples/7nodes)  
（1）使用docker-compose部署好区块链：  
```
$git clone https://github.com/jpmorganchase/quorum-examples
$cd quorum-examples
$docker-compose up -d
$docker ps
CONTAINER ID        IMAGE                            COMMAND                  CREATED             STATUS                    PORTS                                                                           NAMES
19522838f213        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 30 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22006->8545/tcp   quorum-examples_node7_1
6c05e4441202        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 28 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22002->8545/tcp   quorum-examples_node3_1
e39674824a33        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 30 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22001->8545/tcp   quorum-examples_node2_1
6df304600bde        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 30 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22005->8545/tcp   quorum-examples_node6_1
3e05a8e19444        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 30 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22004->8545/tcp   quorum-examples_node5_1
d0c109e234a7        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 30 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22003->8545/tcp   quorum-examples_node4_1
d246eaea26f6        quorumengineering/quorum:2.2.1   "/bin/sh -c 'UDS_WAI…"   30 minutes ago      Up 28 minutes (healthy)   8546/tcp, 21000/tcp, 30303/tcp, 50400/tcp, 30303/udp, 0.0.0.0:22000->8545/tcp   quorum-examples_node1_1
764e0941d9b3        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9087->9080/tcp                                                quorum-examples_txmanager7_1
54bc0ed8a974        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9086->9080/tcp                                                quorum-examples_txmanager6_1
5495f660a2d2        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9084->9080/tcp                                                quorum-examples_txmanager4_1
ec124de173a8        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9082->9080/tcp                                                quorum-examples_txmanager2_1
faa801660985        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9085->9080/tcp                                                quorum-examples_txmanager5_1
a2cf351d787a        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9083->9080/tcp                                                quorum-examples_txmanager3_1
7931f1f1c14e        quorumengineering/tessera:0.8    "/bin/sh -c 'DDIR=/q…"   31 minutes ago      Up 30 minutes (healthy)   9000/tcp, 0.0.0.0:9081->9080/tcp                                                quorum-examples_txmanager1_1
```

（2）在节点1和节点7之间部署私有智能合约  
attach到节点1上部署智能合约，其中privateFor为节点7的公钥：  
```
a = eth.accounts[0]
web3.eth.defaultAccount = a;

// abi and bytecode generated from simplestorage.sol:
// > solcjs --bin --abi simplestorage.sol
var abi = [{"constant":true,"inputs":[],"name":"storedData","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"x","type":"uint256"}],"name":"set","outputs":[],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"get","outputs":[{"name":"retVal","type":"uint256"}],"payable":false,"type":"function"},{"inputs":[{"name":"initVal","type":"uint256"}],"payable":false,"type":"constructor"}];

var bytecode = "0x6060604052341561000f57600080fd5b604051602080610149833981016040528080519060200190919050505b806000819055505b505b610104806100456000396000f30060606040526000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632a1afcd914605157806360fe47b11460775780636d4ce63c146097575b600080fd5b3415605b57600080fd5b606160bd565b6040518082815260200191505060405180910390f35b3415608157600080fd5b6095600480803590602001909190505060c3565b005b341560a157600080fd5b60a760ce565b6040518082815260200191505060405180910390f35b60005481565b806000819055505b50565b6000805490505b905600a165627a7a72305820d5851baab720bba574474de3d09dbeaabc674a15f4dd93b974908476542c23f00029";

var simpleContract = web3.eth.contract(abi);
var simple = simpleContract.new(42, {from:web3.eth.accounts[0], data: bytecode, gas: 0x47b760, privateFor: ["ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="]}, function(e, contract) {
  if (e) {
    console.log("err creating contract", e);
  } else {
    if (!contract.address) {
      console.log("Contract transaction send: TransactionHash: " + contract.transactionHash + " waiting to be mined...");
    } else {
      console.log("Contract mined! Address: " + contract.address);
      console.log(contract);
    }
  }
});
```
部署的智能合约为一个简单的数据存取的合约：  
```
pragma solidity ^0.4.15;

contract simplestorage {
  uint public storedData;

  function simplestorage(uint initVal) {
    storedData = initVal;
  }

  function set(uint x) {
    storedData = x;
  }

  function get() constant returns (uint retVal) {
    return storedData;
  }
}
```

（3）对于公开交易，则直接在Quorum节点间完成，与go-ethereum基本一致，如下：  
![image](https://user-images.githubusercontent.com/8265961/52626092-69488d00-2eed-11e9-9b66-e37b7f962735.png)

（4）对于私有交易
例如节点1上set(4)，分别在节点4和节点7上get()则有不同现象：  
```
// Node 1
> private.set(4,{from:eth.accounts[0],privateFor:["ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="]});
"0x678f838f0e05187228ea3c890f1feeff2d0e948da5de699392b0dcec3c0eee59"
> private.get()
4

// Node 4
> private.get()
0

// Node 7
> private.get()
4
```
可以发现，私有交易的双方，即节点1和7都能正确获取，而非交易方节点4则无法正确获取，从而实现交易的隐私性。若在这三个节点上查询此交易0x678f838f0e05187228ea3c890f1feeff2d0e948da5de699392b0dcec3c0eee59，会发现都能查到，只不过交易的input参数的值不再是原始交易的payload，而是加密后的payload的hash值，且value是一个特殊的值，用来标识此交易为私有交易。如下图所示：  
![image](https://user-images.githubusercontent.com/8265961/52626812-f93b0680-2eee-11e9-9304-98ee27c458d5.png)
交易真实的payload会通过TransactionManager安全的在节点间共享，Quorum节点通过加密后的payload的hash值作为索引在与之关联的[Tessera](https://github.com/jpmorganchase/tessera)或者[Constellation](https://github.com/jpmorganchase/constellation)节点上获取解密后的payload，从而执行交易。如下图所示：  
![image](https://user-images.githubusercontent.com/8265961/52627001-69498c80-2eef-11e9-8172-25928d02b03d.png)



## 案例二
在这个案例中，A机构和B机构构成了私有交易AB的交易双方，而C机构不参与该交易。  
![Quorum Tessera Privacy Flow](https://raw.githubusercontent.com/jpmorganchase/quorum-docs/master/images/QuorumTransactionProcessing.JPG)

1.  A机构发出一笔私有交易AB到Quorum节点，指定交易的有效负载，并为A机构和B机构设定privateFor参数，形成其公钥；
2. A机构的Quorum节点发送交易到与之配对的事务管理器，为其请求保存交易的有效负载；
3. A机构的事务管理器向与之相关的Enclave发出请求，验证发送者并加密有效负载；
4. A机构的Enclave检验A机构的私钥，一旦验证通过，就会进行交易对话。这就需要：  
  i. 生成系统密钥和随机数；  
  ii. 对交易有效负载和来自i）项中的系统密钥进行加密；  
  iii. 计算ii）项中已加密有效负载的SHA3-512哈希值；  
  iv. 遍历交易参与方列表（本例中为A机构和B机构），加密i）项中的系统密钥和参与者的公钥（PGP加密）；  
  v. 向事务管理器返回ii）项中的已加密有效负载、iii）项中的哈希值，iv）项中每个参与者的加密密钥。
5. 然后，A机构的事务管理器使用哈希作为索引，保存已加密有效负载（使用系统密钥加密）和已加密系统密钥，再将哈希值、已加密有效负载和与B机构公钥加密而成的已加密系统密钥等安全地发送给事务管理器。B机构的事务管理器使用Ack/Nack进行响应。需要注意的是，如果A机构并未从B机构处收到响应或Nack，那么交易将不会传播到整个网络。它是参与者保存通讯有效负载的前提条件。
6. 一旦数据成功传到B机构的事务管理器，A机构的事务管理器向Quorum节点返回哈希值，该节点就使用哈希值替换交易的初始负载，修改交易的V值为37或38，这将对其他节点进行提示，该哈希值表示一个与已加密有效负载相关的私有交易；相反，则提示一个无意义字节码相关的公开交易；
7. 接下来，交易将基于标准的以太坊P2P协议，传播到剩余网络中；
8. 生成一个包含交易AB的区块，并且发散到网络中的每一个机构；
9. 处理区块时，所有机构都将处理交易。每一个Quorum节点将认识到一个37或38的V值，表明交易的有效负载需要加密、需要联系他们本地的事务管理器，从而决定他们是否需要同意这笔交易（使用哈希值作为索引进行查找）；
10. 由于C机构并未同意这笔交易，它将收到一条NotARecipient的消息，并将忽略该交易——它将不会升级其私有状态数据库。A机构和B机构将会在他们本地的事务管理器中查找哈希值，识别他们的确赞成该交易，然后每位参与者与其对应的Enclave进行通讯，发送已加密有效负载、已加密系统密钥和签名；
11. Enclave验证签名，然后使用Enclave中该机构的私钥来解密系统密钥，使用刚刚显示的系统密钥解密交易有效负载，并向事务管理器返回已加密有效负载；
12. 机构A和B的事务管理器，通过执行合约代码，向EVM发送已解密有效负载。这次执行将会升级仅在Quorum节点的私有状态数据库的状态。注意：代码一旦运行即会无效，所以没有上述流程它将无法阅读。

## 实现细节
注：以Tessera为例分析
![image](https://user-images.githubusercontent.com/8265961/52684786-60ee6180-2f82-11e9-9f8e-a4a606d8161e.png)

### Quorum组件
Quorum组件基于go-ethereum修改：
* 共识算法，增加Raft和IBFT共识
* P2P网络层，改成只有授权节点才能连入或连出网络
* 区块生成逻辑，由检查“全局状态根”改为检查“全局公开状态根”
* 区块验证逻辑，在区块头，将“全局状态根”替换成“全局公开状态根”
* 状态树，分成公开状态树和私有状态树
* 区块链验证逻辑，改成处理“私有事务”
* 创建事务，改成允许交易数据被加密哈希替代，以维护必需的隐私数据
* 删除以太坊中Gas的定价，尽管保留Gas本身

当发送私有交易时，即添加privateFor参数时，Quorum节点会检测到这个是一个私有交易，如下：

```
// SendTransaction will create a transaction from the given arguments and
// tries to sign it with the key associated with args.To. If the given passwd isn't
// able to decrypt the key it fails.
func (s *PrivateAccountAPI) SendTransaction(ctx context.Context, args SendTxArgs, passwd string) (common.Hash, error) {
  // Look up the wallet containing the requested signer
  account := accounts.Account{Address: args.From}

  wallet, err := s.am.Find(account)
  if err != nil {
    return common.Hash{}, err
  }

  if args.Nonce == nil {
    // Hold the addresse's mutex around signing to prevent concurrent assignment of
    // the same nonce to multiple accounts.
    s.nonceLock.LockAddr(args.From)
    defer s.nonceLock.UnlockAddr(args.From)
  }

  isPrivate := args.PrivateFor != nil

  if isPrivate { // Quorum节点会检测到这个是一个私有交易
    data := []byte(*args.Data)
    if len(data) > 0 {
      log.Info("sending private tx", "data", fmt.Sprintf("%x", data), "privatefrom", args.PrivateFrom, "privatefor", args.PrivateFor)
                         // 向Tessera组件发送交易数据，Tessera组件返回加密后的交易数据的hash值
      data, err := private.P.Send(data, args.PrivateFrom, args.PrivateFor)
      log.Info("sent private tx", "data", fmt.Sprintf("%x", data), "privatefrom", args.PrivateFrom, "privatefor", args.PrivateFor)
      if err != nil {
        return common.Hash{}, err
      }
    }
    // zekun: HACK
    d := hexutil.Bytes(data)
    args.Data = &d
  }

  // Set some sanity defaults and terminate on failure
  if err := args.setDefaults(ctx, s.b); err != nil {
    return common.Hash{}, err
  }
  // Assemble the transaction and sign with the wallet
  tx := args.toTransaction()

  var chainID *big.Int
  if config := s.b.ChainConfig(); config.IsEIP155(s.b.CurrentBlock().Number()) && !isPrivate {
    chainID = config.ChainID
  }
  signed, err := wallet.SignTxWithPassphrase(account, passwd, tx, chainID)
  if err != nil {
    return common.Hash{}, err
  }
  return submitTransaction(ctx, s.b, signed, isPrivate)
}
```
Quorum节点就会向Tessera组件发送交易数据，Tessera组件返回加密后的交易数据的hash值。

```
func (g *Constellation) Send(data []byte, from string, to []string) (out []byte, err error) {
  if g.isConstellationNotInUse {
    return nil, ErrConstellationIsntInit
  }
  out, err = g.node.SendPayload(data, from, to)
  if err != nil {
    return nil, err
  }
  g.c.Set(string(out), data, cache.DefaultExpiration)
  return out, nil
}
```
Quorum节点与Tessera的通讯是使用的[基于Unix Domain Socket的Private API](https://github.com/jpmorganchase/tessera/wiki/Interface-&-API#unix-domain-socket-private-api)

```
func (c *Client) SendPayload(pl []byte, b64From string, b64To []string) ([]byte, error) {
  buf := bytes.NewBuffer(pl)
  req, err := http.NewRequest("POST", "http+unix://c/sendraw", buf)
  if err != nil {
    return nil, err
  }
  if b64From != "" {
    req.Header.Set("c11n-from", b64From)
  }
  req.Header.Set("c11n-to", strings.Join(b64To, ","))
  req.Header.Set("Content-Type", "application/octet-stream")
  res, err := c.httpClient.Do(req)

  if res != nil {
    defer res.Body.Close()
  }
  if err != nil {
    return nil, err
  }
  if res.StatusCode != 200 {
    return nil, fmt.Errorf("Non-200 status code: %+v", res)
  }

  return ioutil.ReadAll(base64.NewDecoder(base64.StdEncoding, res.Body))
}
```
这样以后，在Quorum链上存储的就是加密后的私有交易的hash值，而实际的交易内容则被Tessera安全的存储在DB内。

当出块时，在**CommitTransactions**阶段会进行交易的执行，如下：

![miner-worker](https://user-images.githubusercontent.com/8265961/52096073-736eaf80-2600-11e9-92f9-aa0058062260.png)

其中tx的执行在**TransitionDb**中：

```
// TransitionDb will transition the state by applying the current message and
// returning the result including the the used gas. It returns an error if it
// failed. An error indicates a consensus issue.
func (st *StateTransition) TransitionDb() (ret []byte, usedGas uint64, failed bool, err error) {
  if err = st.preCheck(); err != nil {
    return
  }
  msg := st.msg
  sender := vm.AccountRef(msg.From())
  homestead := st.evm.ChainConfig().IsHomestead(st.evm.BlockNumber)
  contractCreation := msg.To() == nil
  isQuorum := st.evm.ChainConfig().IsQuorum

  var data []byte
  isPrivate := false
  publicState := st.state
        // 私有交易
  if msg, ok := msg.(PrivateMessage); ok && isQuorum && msg.IsPrivate() {
    isPrivate = true
                // 向Tessera发起请求获取解密后的交易数据
    data, err = private.P.Receive(st.data)
    // Increment the public account nonce if:
    // 1. Tx is private and *not* a participant of the group and either call or create
    // 2. Tx is private we are part of the group and is a call
    if err != nil || !contractCreation {
      publicState.SetNonce(sender.Address(), publicState.GetNonce(sender.Address())+1)
    }

    if err != nil {
      return nil, 0, false, nil
    }
  } else {
    data = st.data
  }

  // Pay intrinsic gas
  gas, err := IntrinsicGas(st.data, contractCreation, homestead)
  if err != nil {
    return nil, 0, false, err
  }
  if err = st.useGas(gas); err != nil {
    return nil, 0, false, err
  }

  var (
    evm = st.evm
    // vm errors do not effect consensus and are therefor
    // not assigned to err, except for insufficient balance
    // error.
    vmerr error
  )
  if contractCreation {
    ret, _, st.gas, vmerr = evm.Create(sender, data, st.gas, st.value)
  } else {
    // Increment the account nonce only if the transaction isn't private.
    // If the transaction is private it has already been incremented on
    // the public state.
    if !isPrivate {
      publicState.SetNonce(msg.From(), publicState.GetNonce(sender.Address())+1)
    }
    var to common.Address
    if isQuorum {
      to = *st.msg.To()
    } else {
      to = st.to()
    }
    //if input is empty for the smart contract call, return
    if len(data) == 0 && isPrivate {
      return nil, 0, false, nil
    }

    ret, st.gas, vmerr = evm.Call(sender, to, data, st.gas, st.value)
  }
  if vmerr != nil {
    log.Info("VM returned with error", "err", vmerr)
    // The only possible consensus-error would be if there wasn't
    // sufficient balance to make the transfer happen. The first
    // balance transfer may never fail.
    if vmerr == vm.ErrInsufficientBalance {
      return nil, 0, false, vmerr
    }
  }
  st.refundGas()
  st.state.AddBalance(st.evm.Coinbase, new(big.Int).Mul(new(big.Int).SetUint64(st.gasUsed()), st.gasPrice))

  if isPrivate {
    return ret, 0, vmerr != nil, err
  }
  return ret, st.gasUsed(), vmerr != nil, err
}
```
如果是私有交易，Quorum节点则会向Tessera发起请求，以Quorum链上存储的data为key，即之前加密后的交易数据的hash值，获取解密后的交易的实际数据，然后在evm中执行交易。

### Tessera组件
Tessera组件是Transaction Manager的java实现，详见：https://github.com/jpmorganchase/tessera/wiki

由两个部分组成：
- Transaction Manager：负责事务隐私，存储并允许访问加密的交易数据，与其他参与方的事务管理器交换加密的有效载荷，但没有访问任何敏感私钥的权限。它用Enclave来加密
- Enclave：分布式账本协议，通常利用密码技术来保证事务真实性、参与者身份验证和历史数据存储（即通过加密哈希数据链）。为了实现相关事务的隔离，同时通过特定加密的并行操作来提供性能优化，包括系统密钥生成和数据加解密的大量密码学工作，会委托给Enclave。

# 共识算法
## Raft
Quorum采用了基于Raft的共识机制（使用etcd的Raft实现），而不是以太坊默认的PoW方案。这对于不需要拜占庭容错并且需要更快出块时间（以毫秒而非秒为单位）和事务结束（不存在分支）的封闭式成员资格/联盟设置非常有效。

在以太坊中，任意节点都可以作为区块的打包者，只要其在一轮 pow 中胜出。我们知道 Quorum 的节点沿用了以太坊的设计和代码。所以为了连接以太坊节点和 Raft 共识，Quorum 采用了网络节点和 Raft 节点一对一的方式来实现 Raft-based 共识。当一笔 TX 诞生后，TX 会在以太坊的 P2P 网络中传输。同时，Raft 的 leader 竞选一直在同步进行。当前 leader 节点对应的以太坊节点收到 TX 时，以太坊节点就会将 TX 打包成区块并将区块通过 Raft 节点发送给 Raft 网络上的 follower。follower 节点收到区块后将区块交给对应的以太坊节点。然后以太坊节点将区块记录到链上。

与以太坊不同的是，当一个节点收到区块后并不会马上记录到链上。而是等 Raft 网络中的 leader 收到所有 follower 的确认回执后，广播一个执行的消息。然后所有收到执行消息的 follower 才会将区块记录在本地的链上。

### Lifecycle of a Transaction

- 客户端发起一笔 TX 并通过 RPC 来呼叫节点。
- 节点通过以太坊的 P2P 协议将节点广播给网络。
- 当前的 Raft leader 对应的以太坊节点收到了 TX 后将 TX 打包成区块。
- 区块被 RLP 编码后传递给对应的 Raft leader。
- leader 收到区块后通过 Raft 算法将区块传递给 follower。这包括如下步骤： 
  - leader 发送 AppendEntries 指令给 follower。
  - follower 收到这个包含区块信息的指令后，返回确认回执给 leader。
  - leader 收到不少于指定数量的确认回执后，发送确认 append 的指令给 follower。
  - follower 收到确认 append 的指令后将区块信息记录到本地的 Raft log 上。
- Raft 节点将区块传递给对应的 Quorum 节点。Quorum 节点校验区块的合法性，如果合法则记录到本地链上。

### Block Race
通常情况下，每一个被传至 Raft 的区块最终都会被添加到链上。但是也会有意外出现。比如因为一些网络的原因，某个 leader 无法与大部分的 follower 进行交互了。这时其他 follower 就会推选出新的 leader。在这期间，旧的 leader 还会产生新的区块。但是因为没有收到足量的 follower 回执，所以它产生的区块都不会最终写到链上。与之相对的，新的 leader 这边则会正常进行区块同步。一旦旧 leader 这边恢复通信，它会将自己产生的 AppendEntries 指令广播出去。由于其发出的指令已经过时了，所以大部分的 follower 不会给予这些指令正确的回执。

具体流程如下：

- Node1 作为 leader 产生一个新的区块：[0x002, parent: 0x001]。这个区块的父块是编号为 0x001 的区块。Node1 通过 Raft 将这个区块进行共识。
- 0X002 区块共识成功后网络出现了问题，Node1 无法与大部分的 follower 进行通信。
- 网络问题并没有影响 Node1 的产块。一个新的区块被产出：[0x003, parent: 0x002]。为了共识这个新的区块，Node1 向 Raft 网络发送 AppendEntries 指令（指令中包含新区块的信息），并等待 follower 的确认回执。因为网络问题，Node1 一直没有收到足够数量的 follower 回执。
- 于此同时，那些无法与 Node1 通信的 follower 因为长时间没收到 leader 的心跳，所以推选出了新的 leader：Node2。
- Node2 产生区块[0x004, parent: 0x002] 后将含此区块信息的 AppendEntries 指令发送给 follower。follower 确认这个指令后返回确认回执。最终这个指令被执行并记录在 Raft log中。
- 0x004 区块共识完成后网络状态得到恢复。此时第三步中的来自 Node1 的 AppendEntries 指令终于被传递给大部分的 follower。但是此时 follower 的链上的最终块已经是第五步中的 0x004，所以区块 [0x003, parent: 0x002] 无法被执行，因为其 parent 是 0x002 不满足当前链的状态。这条不执行的动作也会被记录到 Raft log 中去。
- Node2 继续生成区块 [0x005, parent: 0x004]。
- 最后整个流程下来，follower 的 Raft log 内容大致会长这样：
```
得到区块[0x002, parent: 0x001, sender: Node1] - 执行     
得到区块[0x004, parent: 0x002, sender: Node2] - 执行     
得到区块[0x003, parent: 0x002, sender: Node1] - 不执行     
得到区块[0x005, parent: 0x004, sender: Node2] - 执行   
```

需要注意的是，整个共识过程中，Raft 层面只负责记录自己节点的 Raft log。真正执行 log 内容的是 Quorum 节点。Quorum 节点根据其节点对应的 Raft log 来做具体的操作。

### Speculative Minting
一个区块从被创建，到经过 Raft 同步，到最后记录到链上多多少少会经历一段时间（尽管非常短）。如果等上一个区块写入到链上以后下一个区块才能生成，那么就会使得 TX 的确认时间增长。为了解决这个问题，同时为了能更有效率的处理区块生成，Quorum 推出了 speculative minting 机制。在这种机制下，新区块可以在其父区块没有完全上链的情况下被创建。如果这个场景重复出现，那么就会出现一串未被上链的区块，这些区块都会有指向其父区块的索引，我们将这类区块串称为 speculative chain。

在维护 speculative chain 的同时，系统还会维护一份被称作 proposedTxes 的数组。这份数组包含了所有 speculative chain 中的 TX。主要是为了记录已经被传输到 Raft 中但是还没被正式上链的交易。防止同一条交易被重复打包。

## IBFT
详见：https://github.com/ethereum/EIPs/issues/650

# 节点的许可管理
节点的授权，是用来控制哪些节点可以连接到指定节点、以及可以从哪些指定节点拨出的功能。目前，当启动节点时，通过--permissioned参数在节点级别处进行管理。

如果设置了--permissioned参数，节点将查找名为/permissioned-nodes.json的文件。此文件包含此节点可以连接并接受来自其连接的enodes白名单。因此，启用权限后，只有permissioned-nodes.json文件中列出的节点成为网络的一部分。 如果指定了--permissioned参数，但没有节点添加到permissioned-nodes.json文件，则该节点既不能连接到任何节点也不能接受任何接入的连接。

permissioned-nodes.json文件的格式如下所示
```
[ 
    "enode://remoteky1@ip1:port1",
    "enode://remoteky1@ip2:port2",
    "enode://remoteky1@ip3:port3"
]
```

在geth建立p2p连接的时候，如果启用了节点的许可管理，则会调用isNodePermissioned方法去检查目标节点是否被授权，如下所示：

```
func (srv *Server) setupConn(c *conn, flags connFlag, dialDest *discover.Node) error {
        ...

  //START - QUORUM Permissioning
  currentNode := srv.NodeInfo().ID
  cnodeName := srv.NodeInfo().Name
  clog.Trace("Quorum permissioning",
    "EnableNodePermission", srv.EnableNodePermission,
    "DataDir", srv.DataDir,
    "Current Node ID", currentNode,
    "Node Name", cnodeName,
    "Dialed Dest", dialDest,
    "Connection ID", c.id,
    "Connection String", c.id.String())

  if srv.EnableNodePermission {
    clog.Trace("Node Permissioning is Enabled.")
    node := c.id.String()
    direction := "INCOMING"
    if dialDest != nil {
      node = dialDest.ID.String()
      direction = "OUTGOING"
      log.Trace("Node Permissioning", "Connection Direction", direction)
    }

    if !isNodePermissioned(node, currentNode, srv.DataDir, direction) {
      return nil
    }
  } else {
    clog.Trace("Node Permissioning is Disabled.")
  }

  //END - QUORUM Permissioning
        ...
}
```
在isNodePermissioned中则会遍历目标节点是否在permissioned-nodes.json的节点列表内，如下：

```
// check if a given node is permissioned to connect to the change
func isNodePermissioned(nodename string, currentNode string, datadir string, direction string) bool {

  var permissionedList []string
  nodes := parsePermissionedNodes(datadir)
  for _, v := range nodes {
    permissionedList = append(permissionedList, v.ID.String())
  }

  log.Debug("isNodePermissioned", "permissionedList", permissionedList)
  for _, v := range permissionedList {
    if v == nodename {
      log.Debug("isNodePermissioned", "connection", direction, "nodename", nodename[:NODE_NAME_LENGTH], "ALLOWED-BY", currentNode[:NODE_NAME_LENGTH])
      return true
    }
  }
  log.Debug("isNodePermissioned", "connection", direction, "nodename", nodename[:NODE_NAME_LENGTH], "DENIED-BY", currentNode[:NODE_NAME_LENGTH])
  return false
}
```

# 更高的性能
## TPS测试
benchmark: https://github.com/drandreaskrueger/chainhammer
对比结果:

| hardware    | node type       | #nodes  | config  | peak TPS_av   | final TPS_av  |
|-----------  |-----------      |-------- |-------- |-------------  |-------------- |
| t2.micro      | parity aura     | 4       | (D)     | 45.5          |  44.3        |
| t2.large      | parity aura     | 4       | (D)     | 53.5          |  52.9        |
| t2.xlarge   | parity aura     | 4       | (J)     | 57.1          |  56.4        |
| t2.2xlarge  | parity aura     | 4       | (D)     | 57.6          |  57.6        |
|               |                   |           |         |               |              |
| t2.micro      | parity instantseal | 1        | (G)     | 42.3          |  42.3        |
| t2.xlarge     | parity instantseal | 1        | (J)     | 48.1          |  48.1        |
|               |                   |           |         |               |              |
| t2.2xlarge  | geth clique       | 3+1 +2    | (B)     | 421.6         | 400.0        |
| t2.xlarge   | geth clique       | 3+1 +2    | (B)     | 386.1         | 321.5        |
| t2.xlarge   | geth clique       | 3+1       | (K)     | 372.6         | 325.3        |
| t2.large      | geth clique       | 3+1 +2    | (B)     | 170.7         | 169.4        |
| t2.small      | geth clique       | 3+1 +2    | (B)     |  96.8         |  96.5        |
| t2.micro      | geth clique       | 3+1       | (H)     | 124.3         | 122.4        |
|               |                   |           |         |               |              |
| t2.micro SWAP | quorum crux IBFT  | 4         | (I) SWAP! |  98.1         |  98.1        |
|               |                   |           |         |               |              |
| t2.micro      | quorum crux IBFT  | 4         | (F)       | lack of RAM   |              |
| t2.large      | quorum crux IBFT  | 4         | (F)     | 207.7       | 199.9        |
| t2.xlarge   | quorum crux IBFT  | 4         | (F)     | 439.5       | 395.7        |
| t2.xlarge   | quorum crux IBFT  | 4         | (L)     | 389.1       | 338.9        |
| t2.2xlarge  | quorum crux IBFT  | 4         | (F)     | 435.4       | 423.1        |
| c5.4xlarge  | quorum crux IBFT  | 4         | (F)     | 536.4       | 524.3        |

（1）Raft
- 1000 transactions 
- multi-threaded with 23 workers
- average TPS around 160 TPS
- 20 raft blocks per second) 

![image](https://user-images.githubusercontent.com/8265961/52631182-0d840100-2ef9-11e9-94e4-80425211919e.png)

（2）IBFT
- 20 millions gasLimit
- 1 second istanbul.blockperiod
- 20000 transactions 
- multi-threaded with 23 workers
-  Initial average >400 TPS then drops to below 300 TPS

![image](https://user-images.githubusercontent.com/8265961/52631225-34423780-2ef9-11e9-8e06-021882c21122.png)


# 参考
- [初探摩根大通的企业级区块链解决方案—Quorum](http://rdc.hundsun.com/portal/article/892.html)
- [基于以太坊的联盟链？Quorum机制初探（下）](https://blog.csdn.net/about_blockchain/article/details/78814873)
- [Exploring How Private Transaction Works in Quorum](https://medium.com/@kctheservant/exploring-how-private-transaction-works-in-quorum-53612a9e7206)