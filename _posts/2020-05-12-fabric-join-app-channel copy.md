---
layout: post
title: "Fabric组织加入已经存在的应用通道流程"
date: 2020-05-12
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->


<!-- /TOC -->

以组织Org3加入已经存在的通道mychannel为例：

```bash

# 1.1 获取通道最新的配置块
peer channel fetch config config_block.pb -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c mychannel --tls --cafile $ORDERER_CA

# 1.2 解码成json格式，提取出配置部分
configtxlator proto_decode --input config_block.pb --type common.Block | jq .data.data[0].payload.data.config > config.json

# 1.3 修改配置，添加上新的组织，即Org3
jq -s '.[0] * {"channel_group":{"groups":{"Application":{"groups": {"Org3MSP":.[1]}}}}}' config.json ./organizations/peerOrganizations/org3.example.com/org3.json > modified_config.json

# 1.4 使用configtxlator工具计算出配置更新部分，并把此部分编码成信封交易`org3_update_in_envelope.pb`
configtxlator proto_encode --input config.json --type common.Config >original_config.pb
configtxlator proto_encode --input modified_config.json --type common.Config >modified_config.pb
configtxlator compute_update --channel_id mychannel --original original_config.pb --updated modified_config.pb >config_update.pb
configtxlator proto_decode --input config_update.pb --type common.ConfigUpdate >config_update.json
echo '{"payload":{"header":{"channel_header":{"channel_id":"'mychannel'", "type":2}},"data":{"config_update":'$(cat config_update.json)'}}}' | jq . >config_update_in_envelope.json
configtxlator proto_encode --input config_update_in_envelope.json --type common.Envelope >"org3_update_in_envelope.pb"

# 2.1 组织Org1：对此信封交易签名
peer channel signconfigtx -f org3_update_in_envelope.pb

# 2.2 组织Org2：对此信封交易签名
peer channel signconfigtx -f org3_update_in_envelope.pb

# 2.3 组织Org1：向orderer提交此通道配置更新的交易
peer channel update -f org3_update_in_envelope.pb -c mychannel -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile ${ORDERER_CA}

# 3.1 组织Org3：从orderer拉取目标通道的配置块（创世块）
peer channel fetch 0 mychannel.block -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com -c mychannel --tls --cafile $ORDERER_CA

# 3.2 组织Org3：加入目标通道
peer channel join -b mychannel.block
```

可以看到，总共需要3大步：
1. 获取当前通道的配置，并进行修改，增加新组织Org3的相关信息，然后把增量变更部分生成信封交易
2. 根据通道配置修改的策略（此处是大多数成员，MAJORITY Admins），由组织Org1和组织Org2分别签名，然后发给orderer对此通道进配置变更
3. 准备加入通道的组织Org3从orderer拉取目标通道的创世块，然后就可以加入此通道