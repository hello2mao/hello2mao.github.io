---
layout:     post
title:      "解析Quorum -- 摩根大通的企业级区块链解决方案"
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
  - [实现细节](#%E5%AE%9E%E7%8E%B0%E7%BB%86%E8%8A%82)
  - [宏观案例](#%E5%AE%8F%E8%A7%82%E6%A1%88%E4%BE%8B)
  - [微观案例](#%E5%BE%AE%E8%A7%82%E6%A1%88%E4%BE%8B)
- [共识算法](#%E5%85%B1%E8%AF%86%E7%AE%97%E6%B3%95)
  - [Raft](#raft)
  - [IBFT](#ibft)
- [节点的许可管理](#%E8%8A%82%E7%82%B9%E7%9A%84%E8%AE%B8%E5%8F%AF%E7%AE%A1%E7%90%86)
- [更高的性能](#%E6%9B%B4%E9%AB%98%E7%9A%84%E6%80%A7%E8%83%BD)
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
- [Tessera](https://github.com/jpmorganchase/tessera)或者[Constellation](https://github.com/jpmorganchase/constellation)节点，注：建议使用Tessera，Constellation感觉要被废弃

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

## 实现细节
TODO

## 宏观案例
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



## 微观案例
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

# 共识算法
## Raft
TODO
## IBFT
TODO

# 节点的许可管理
TODO

# 更高的性能
TODO

# 参考
- [初探摩根大通的企业级区块链解决方案—Quorum](http://rdc.hundsun.com/portal/article/892.html)
- [基于以太坊的联盟链？Quorum机制初探（下）](https://blog.csdn.net/about_blockchain/article/details/78814873)
- [Exploring How Private Transaction Works in Quorum](https://medium.com/@kctheservant/exploring-how-private-transaction-works-in-quorum-53612a9e7206)

