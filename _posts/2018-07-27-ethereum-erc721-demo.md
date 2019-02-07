---
layout:     post
title:      "ERC721智能合约和Dapp实践 -- 以太猫CryptoKitties的简单实现"
subtitle:   "Ethereum ERC721 Demo -- Simple CryptoKitties"
date:       2018-07-27 17:21:52
author:     "hello2mao"
tags:
    - ethereum
---

- [一、概述](#%E4%B8%80%E6%A6%82%E8%BF%B0)
- [二、UI](#%E4%BA%8Cui)
- [三、设计目标](#%E4%B8%89%E8%AE%BE%E8%AE%A1%E7%9B%AE%E6%A0%87)
- [四、游戏系统设计](#%E5%9B%9B%E6%B8%B8%E6%88%8F%E7%B3%BB%E7%BB%9F%E8%AE%BE%E8%AE%A1)
  - [4.1 交易系统](#41-%E4%BA%A4%E6%98%93%E7%B3%BB%E7%BB%9F)
      - [4.1.1 帐号](#411-%E5%B8%90%E5%8F%B7)
      - [4.1.2 产品](#412-%E4%BA%A7%E5%93%81)
      - [4.1.3 买卖交易](#413-%E4%B9%B0%E5%8D%96%E4%BA%A4%E6%98%93)
  - [4.2 繁育系统](#42-%E7%B9%81%E8%82%B2%E7%B3%BB%E7%BB%9F)
  - [4.3 对战系统](#43-%E5%AF%B9%E6%88%98%E7%B3%BB%E7%BB%9F)
  - [4.4 喂养系统](#44-%E5%96%82%E5%85%BB%E7%B3%BB%E7%BB%9F)
  - [4.5 升级系统](#45-%E5%8D%87%E7%BA%A7%E7%B3%BB%E7%BB%9F)
- [五、合约设计](#%E4%BA%94%E5%90%88%E7%BA%A6%E8%AE%BE%E8%AE%A1)
  - [5.1 产品数据结构](#51-%E4%BA%A7%E5%93%81%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84)
  - [5.2 DNA属性](#52-dna%E5%B1%9E%E6%80%A7)
  - [5.3 繁育&喂养](#53-%E7%B9%81%E8%82%B2%E5%96%82%E5%85%BB)
  - [5.4 对战](#54-%E5%AF%B9%E6%88%98)
  - [5.5 升级](#55-%E5%8D%87%E7%BA%A7)

> **More:** [https://github.com/hello2mao/Xnotes/issues/24](https://github.com/hello2mao/Xnotes/issues/24)

# 一、概述
设计一个基于以太坊ERC721合约的DAPP游戏。
源码：https://github.com/hello2mao/CryptoKitties
# 二、UI
![image](https://user-images.githubusercontent.com/8265961/52388292-b3360b00-2ac8-11e9-9410-ee1f02babb51.png)

# 三、设计目标
（1）交易系统：用户可使用帐号在商店里对产品进行买卖交易。  
（2）繁育系统：用户可以使用已有的产品与繁殖中心的产品进行繁殖，产生新的产品。  
（3）对战系统：用户可以使用已有的产品与对战心中的产品进行对战，赢了将升级并产生一个新产品，输了失败次数将加一。  
（4）喂养系统：用户可以对已有的产品喂养以太坊公链上的以太猫，从而产生新的带以太猫基因的杂交品种。  
（5）升级系统：用户可以对已有的产品花ETH进行升级，2级以后可以改名，20级后可以定制DNA，从而用户激励升级。  
# 四、游戏系统设计
## 4.1 交易系统
#### 4.1.1 帐号
**注：所有帐号都默认设置好，不提供注册新账号功能。**  
（1）1个交易中心管理员帐号：对用户不可见，用于初始化交易中心内的默认摆放产品  
（2）1个繁育中心管理员帐号：对用户不可见，用于初始化繁殖中心内的默认摆放产品  
（3）1个对战中心管理员帐号：对用户不可见，用于初始化对战中心内的默认摆放产品  
（4）几个测试用户帐号  
#### 4.1.2 产品
产品有名字、ID、价格、等级、代数、技能冷却、战斗记录和基因属性。所有数据都入链。  
（1）名字：随机字符串  
（2）ID：唯一ID  
（3）价格：默认价格由出生时的基因决定  
（4）等级：默认1级，在升级系统进行升级，需花费ETH  
（5）代数：繁育代数，新生的为0代  
（6）技能冷却：技能冷却剩余时间，所有技能默认冷却时间是1天  
（7）战斗记录：战斗中心进行战斗后的记录  
（8）属性：分为头部、眼部、皮肤、上身、下身五个部分，最终决定产品的外形  
#### 4.1.3 买卖交易
（1）用户默认拥有大约10000ETH  
（2）用户花费ETH对产品进行买卖。  
（3）买卖需以太坊确认后才入链生效。  
## 4.2 繁育系统
（1）用户可以使用已有的产品与繁育中心的产品进行繁殖，产生新的产品。  
（2）默认为第0代，繁育后代数加一。  
（3）技能冷却时间为1天。  
（4）繁育立马产生新产品。  
## 4.3 对战系统
用户可以使用已有的产品与对战心中的产品进行对战，赢了将升级并产生一个新产品，输了失败次数将加一。
对战流程如下：  
（1）选择一个自己的产品，然后选择一个对战中心的产品去攻击。  
（2）如果你是攻击方，你将有70%的几率获胜，防守方将有30%的几率获胜。  
（3）所有的产品（攻守双方）都将有一个 winCount 和一个 lossCount，这两个值都将根据战斗结果增长。  
（4）若攻击方获胜，这个产品将升级并产生一个新产品。  
（5）如果攻击方失败，除了失败次数将加一外，什么都不会发生。  
（6）无论输赢，当前产品的冷却时间都将被激活。  
## 4.4 喂养系统
（1）用户可以对已有的产品喂养以太坊公链上的以太猫，从而产生新的带以太猫基因的杂交品种。  
（2）用户只需输入公链上以太猫的ID  
（3）产生的杂交品种有特殊的标记，且基因也很特殊。  
## 4.5 升级系统
（1）用户可以对已有的产品花ETH进行升级  
（2）2级以后可以改名，20级后可以定制DNA，从而用户激励升级。  
# 五、合约设计
## 5.1 产品数据结构
```solidity
struct Thing {
    string name;       // 名字
    uint price;        // 价格
    uint dna;          // DNA
    uint32 level;      // 等级
    uint32 readyTime;  // 技能冷却
    uint32 generation; // 代数
    uint16 winCount;   // 战斗胜利次数
    uint16 lossCount;  // 战斗失败次数
}
```
## 5.2 DNA属性
DNA一共16位，会映射到UI上。  
0到1位：头部基因  
2到3位：眼部基因  
4到5位：皮肤基因  
6到7位：上身基因  
8到9位：下身基因  
10到13：空  
14到15位：喂养后的杂交基因  
## 5.3 繁育&喂养
繁育：两产品的DNA求和作为最终DNA  
喂养：使用公链上的以太猫的id作为合成DNA来源，与目标产品的DNA求平均作为最终的DNA，并且设置最后两位为99来标记。  
如下：  
```
function feedAndMultiply(uint _thingId, uint _targetDna, string _species) internal onlyOwnerOf(_thingId) {
    Thing storage myThing = things[_thingId];
    require(_isReady(myThing));
    _targetDna = _targetDna % dnaModulus;
    uint newDna = (myThing.dna + _targetDna) / 2;
    // 吃了Kitty后，dna最后两个数字设定为99
    // 例如：7290459416715799
    if (keccak256(_species) == keccak256("kitty")) {
        newDna = newDna - newDna % 100 + 99;
    }
    _createThing(strConcat("n",myThing.name, "", "", ""), newDna, myThing.generation + 1);
    _triggerCooldown(myThing);
}

// 喂养
function feedOnKitty(uint _thingId, uint _kittyId) public {
    // 使用_kittyId作为kittyDna
    uint kittyDna = _kittyId;
    // (,,,,,,,,,kittyDna) = kittyContract.getKitty(_kittyId);
    feedAndMultiply(_thingId, kittyDna, "kitty");
}

// 繁育
function breed(uint _thingId, uint _targetThingId) public {
    uint _targetDna = things[_targetThingId].dna;
    feedAndMultiply(_thingId, _targetDna, "thing");
}
```
## 5.4 对战
对战流程详见：4.3 对战系统  
```
function attack(uint _thingId, uint _targetId) external onlyOwnerOf(_thingId) {
    Thing storage myThing = things[_thingId];
    Thing storage enemyThing = things[_targetId];
    uint rand = randMod(100);
    if (rand <= attackVictoryProbability) {
        myThing.winCount++;
        myThing.level++;
        enemyThing.lossCount++;
        feedAndMultiply(_thingId, enemyThing.dna, "thing");
    } else {
        myThing.lossCount++;
        enemyThing.winCount++;
        _triggerCooldown(myThing);
    }
}
```
## 5.5 升级
消耗升级费用后升级产品  
```
function levelUp(uint _thingId) external payable {
    require(msg.value == levelUpFee);
    things[_thingId].level++;
}
```