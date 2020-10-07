---
layout: post
title: "Fabric创建通道流程解析"
date: 2020-05-08
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->

- [一、命令行步骤](#一命令行步骤)
- [二、peer channel create](#二peer-channel-create)
	- [2.1 入口](#21-入口)
	- [2.2 发送创建通道的交易](#22-发送创建通道的交易)
	- [2.3 获得该通道的创世块](#23-获得该通道的创世块)
	- [2.4 Orderer侧处理逻辑](#24-orderer侧处理逻辑)
	- [2.5 小结](#25-小结)
- [三、peer channel join](#三peer-channel-join)
- [四、peer channel update](#四peer-channel-update)

<!-- /TOC -->

> 注：当前Fabric版本 v2.x

# 一、命令行步骤

先看下命令行的步骤。
以两个组织Org1和Org2加入新创建的通道mychannel为例，给出命令行的步骤如下：

```bash
# 1.1 生成创建通道的交易：mychannel.tx
configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/mychannel.tx -channelID mychannel

# 1.2 为组织Org1生成锚节点更新的交易：Org1MSPanchors.tx 
configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org1MSPanchors.tx -channelID mychannel -asOrg Org1MSP

# 1.3 为组织Org2生成锚节点更新的交易：Org2MSPanchors.tx 
configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org2MSPanchors.tx -channelID mychannel -asOrg Org2MSP

# 2. 组织Org1：发送创建通道的交易到Orderer节点，并获取该通道的创世块：mychannel.block
peer channel create -o localhost:7050 -c mychannel --ordererTLSHostnameOverride orderer.example.com -f ./channel-artifacts/mychannel.tx --outputBlock ./channel-artifacts/mychannel.block --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA

# 3.1 组织Org1：peers加入目标通道
peer channel join -b ./channel-artifacts/mychannel.block

# 3.2 组织Org2：peers加入目标通道
peer channel join -b ./channel-artifacts/mychannel.block

# 4.1 组织Org1：更新锚节点
peer channel update -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c mychannel -f ./channel-artifacts/${CORE_PEER_LOCALMSPID}anchors.tx --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA

# 4.2 组织Org2：更新锚节点
peer channel update -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c mychannel -f ./channel-artifacts/${CORE_PEER_LOCALMSPID}anchors.tx --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA

```

可以看到，总共需要4大步：
1. 通过configtxgen工具生成创建通道所需的交易、锚节点更新的交易
2. 通过peer命令，即`peer channel create`，把创建通道的交易发送给Orderer并获得该通道的创世块
3. 各组织通过peer命令，即`peer channel join`，加入目标通道
4. 各组织通过peer命令，即`peer channel update`，更新锚节点

下面简单分析下这三个命令内部做了哪些事：

# 二、peer channel create

## 2.1 入口

`peer channel create`入口在internal/peer/channel/create.go
```go
func executeCreate(cf *ChannelCmdFactory) error {
  // 把创建通道的交易发送给Orderer
	err := sendCreateChainTransaction(cf)
	if err != nil {
		return err
	}

  // 获得该通道的创世块
	block, err := getGenesisBlock(cf)
	if err != nil {
		return err
	}

	b, err := proto.Marshal(block)
	if err != nil {
		return err
	}

	file := channelID + ".block"
	if outputBlock != common.UndefinedParamValue {
		file = outputBlock
  }
  // 保存到本地
	err = ioutil.WriteFile(file, b, 0644)
	if err != nil {
		return err
	}

	return nil
}
```

`executeCreate`主要做了三件事：
- `sendCreateChainTransaction`：把创建通道的交易发送给Orderer
- `getGenesisBlock`：获得该通道的创世块
- `ioutil.WriteFile`：把该通道的创世块保存到本地

## 2.2 发送创建通道的交易

```go
func sendCreateChainTransaction(cf *ChannelCmdFactory) error {
	var err error
	var chCrtEnv *cb.Envelope

	if channelTxFile != "" {
    // 根据指定的交易，即mychannel.tx ，把创建通道这个交易打包成信封Envelope
		if chCrtEnv, err = createChannelFromConfigTx(channelTxFile); err != nil {
			return err
		}
	} else {
		if chCrtEnv, err = createChannelFromDefaults(cf); err != nil {
			return err
		}
	}

  // 做一些检查，并对此信封签名
	if chCrtEnv, err = sanityCheckAndSignConfigTx(chCrtEnv, cf.Signer); err != nil {
		return err
	}

	var broadcastClient common.BroadcastClient
	broadcastClient, err = cf.BroadcastFactory()
	if err != nil {
		return errors.WithMessage(err, "error getting broadcast client")
	}

  defer broadcastClient.Close()
  // 把此信封发送给Orderer
	err = broadcastClient.Send(chCrtEnv)

	return err
}
```

`sendCreateChainTransaction`主要做了三件事：
- `createChannelFromConfigTx`：根据指定的交易，即mychannel.tx ，把创建通道这个交易打包成信封Envelope
- `sanityCheckAndSignConfigTx`：做一些检查，并对此信封签名
- `broadcastClient.Send(chCrtEnv)`：把此信封发送给Orderer

## 2.3 获得该通道的创世块

```go
func getGenesisBlock(cf *ChannelCmdFactory) (*cb.Block, error) {
	timer := time.NewTimer(timeout)
	defer timer.Stop()

	for {
		select {
		case <-timer.C:
			cf.DeliverClient.Close()
			return nil, errors.New("timeout waiting for channel creation")
    default:
      // 周期性的从Orderer获取新建通道的创世块
			if block, err := cf.DeliverClient.GetSpecifiedBlock(0); err != nil {
				cf.DeliverClient.Close()
				cf, err = InitCmdFactory(EndorserNotRequired, PeerDeliverNotRequired, OrdererRequired)
				if err != nil {
					return nil, errors.WithMessage(err, "failed connecting")
				}
				time.Sleep(200 * time.Millisecond)
			} else {
				cf.DeliverClient.Close()
				return block, nil
			}
		}
	}
}
```

`getGenesisBlock`就做了一件事：周期性的从Orderer获取新建通道的创世块，即索引为0的块

至此，`peer channel create`本地侧的逻辑分析完了，下面看下Orderer侧的逻辑，即从本地通过peer命令把创建通道的交易发给Orderer到收到Orderer给出的新建通道的创世块的这段过程中Orderer做了哪些工作。

## 2.4 Orderer侧处理逻辑

Orderer接收信封并处理的逻辑在orderer/common/broadcast/broadcast.go
```go
// Handle reads requests from a Broadcast stream, processes them, and returns the responses to the stream
func (bh *Handler) Handle(srv ab.AtomicBroadcast_BroadcastServer) error {
	addr := util.ExtractRemoteAddress(srv.Context())
	logger.Debugf("Starting new broadcast loop for %s", addr)
	for {
    // 接收信封消息
		msg, err := srv.Recv()
		if err == io.EOF {
			logger.Debugf("Received EOF from %s, hangup", addr)
			return nil
		}
		if err != nil {
			logger.Warningf("Error reading from %s: %s", addr, err)
			return err
		}
    // 处理消息
    resp := bh.ProcessMessage(msg, addr)
    // 返回处理结果
		err = srv.Send(resp)
		if resp.Status != cb.Status_SUCCESS {
			return err
		}

		if err != nil {
			logger.Warningf("Error sending to %s: %s", addr, err)
			return err
		}
	}

}
```

`Handle`就做了三件事：
- `Recv`：接收信封消息
- `ProcessMessage`：处理消息
- `Send`：返回处理结果

重点看下`ProcessMessage`中对消息的处理：

```go
// ProcessMessage validates and enqueues a single message
func (bh *Handler) ProcessMessage(msg *cb.Envelope, addr string) (resp *ab.BroadcastResponse) {
	...

  // 获取接收到的消息的Header、判断是否为配置信息、获取消息处理器
	chdr, isConfig, processor, err := bh.SupportRegistrar.BroadcastChannelSupport(msg)
	if chdr != nil {
		tracker.ChannelID = chdr.ChannelId
		tracker.TxType = cb.HeaderType(chdr.Type).String()
	}
	if err != nil {
		logger.Warningf("[channel: %s] Could not get message processor for serving %s: %s", tracker.ChannelID, addr, err)
		return &ab.BroadcastResponse{Status: cb.Status_BAD_REQUEST, Info: err.Error()}
	}

	if !isConfig {
		...
	} else { // isConfig
		logger.Debugf("[channel: %s] Broadcast is processing config update message from %s", chdr.ChannelId, addr)
    
    // 对于配置消息，使用处理器处理配置变更消息
		config, configSeq, err := processor.ProcessConfigUpdateMsg(msg)
		if err != nil {
			logger.Warningf("[channel: %s] Rejecting broadcast of config message from %s because of error: %s", chdr.ChannelId, addr, err)
			return &ab.BroadcastResponse{Status: ClassifyError(err), Info: err.Error()}
		}
		tracker.EndValidate()

		tracker.BeginEnqueue()
		if err = processor.WaitReady(); err != nil {
			logger.Warningf("[channel: %s] Rejecting broadcast of message from %s with SERVICE_UNAVAILABLE: rejected by Consenter: %s", chdr.ChannelId, addr, err)
			return &ab.BroadcastResponse{Status: cb.Status_SERVICE_UNAVAILABLE, Info: err.Error()}
		}

    // 把配置变更的交易进行全网共识
		err = processor.Configure(config, configSeq)
		if err != nil {
			logger.Warningf("[channel: %s] Rejecting broadcast of config message from %s with SERVICE_UNAVAILABLE: rejected by Configure: %s", chdr.ChannelId, addr, err)
			return &ab.BroadcastResponse{Status: cb.Status_SERVICE_UNAVAILABLE, Info: err.Error()}
		}
	}

	logger.Debugf("[channel: %s] Broadcast has successfully enqueued message of type %s from %s", chdr.ChannelId, cb.HeaderType_name[chdr.Type], addr)

  // 返回处理结果
	return &ab.BroadcastResponse{Status: cb.Status_SUCCESS}
}
```

`ProcessMessage`主要做了四件事：
- `BroadcastChannelSupport`：获取接收到的消息的Header、判断是否为配置信息、获取消息处理器
- `processor.ProcessConfigUpdateMsg`：对于配置消息，使用处理器处理配置变更消息
- `processor.Configure`：把配置变更的交易进行全网共识
- `BroadcastResponse`：返回处理结果

## 2.5 小结

`peer channel create`主要流程如下：
- 【本地】通过peer命令把创建通道的交易发给Orderer
- 【Orderer】对创建通道的交易，也就是配置变更的交易，进行验证和全网共识
- 【本地】从Orderer获取新建通道的创世块

# 三、peer channel join

TODO:

# 四、peer channel update

TODO: