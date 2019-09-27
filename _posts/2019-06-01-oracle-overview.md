---
layout: post
title: "预言机（Oracle）调研"
subtitle: "oracle overview"
date: 2019-06-01 10:51:11
author: "hello2mao"
tags:
    - blockchain
---

<!-- TOC -->

-   [一、概述](#一概述)
    -   [1.1、为什么需要](#11为什么需要)
    -   [1.2、应用场景](#12应用场景)
    -   [1.3、现有的预言机产品](#13现有的预言机产品)
    -   [1.4、基本原理](#14基本原理)
-   [二、蚂蚁区块链 BaaS 平台–外部数据源服务](#二蚂蚁区块链-baas-平台外部数据源服务)
-   [三、主流预言机：Oraclize](#三主流预言机oraclize)
    -   [3.1 使用举例](#31-使用举例)
    -   [3.2 数据源](#32-数据源)
    -   [3.3 收费](#33-收费)
    -   [3.4 真实性：](#34-真实性)
    -   [3.5 解析助手](#35-解析助手)
    -   [3.6 加密查询](#36-加密查询)
    -   [3.7 原理](#37-原理)
-   [四、参考](#四参考)

<!-- /TOC -->

# 一、概述

受限于区块链的共识模型，智能合约只能调用内部合约，无法直接与外部系统进行交互。将智能合约与外部系统打通，有助于区块链技术更进一步扩展应用场景。

区块链预言机（Oracle）是区块链与外部世界交互的一种实现机制，它在区块链与外部世界间建立一种可信任的桥接机制，使得外部数据可以安全可靠地进入区块链。

## 1.1、为什么需要

区块链上的智能合约和去中心化应用（Dapp）对外界数据拥有交互需求
当智能合约的触发条件取决于区块链外信息时，这些信息需先写入区块链内记录。此时需要通过此机制来提供这些区块链外的信息

## 1.2、应用场景

适用于以下任意场景：

-   智能合约需要可信访问 Web 数据。
-   智能合约通过调用 Open API 使用互联网服务。
-   智能合约需要与外部系统交互。
-   智能合约依赖公共现实事件，如天气、赛事信息、航班信息等。

具体的：

**金融衍生品交易平台**  
衍生品交易平台提供金融类的智能合约，允许用户做空或者做多背后的资产，例如 Market Protocol, Decentralized Derivatives Association, DyDx Protocol 等都提供类似的服务。这类智能合约需要实时从链外获取资产价格，来确定参与各方的收益和损失，以及触发平仓交易等。

**稳定货币**  
稳定货币是一种和法币有稳定兑换率的加密货币，稳定货币可以作为价值的储藏和交易的中间媒介，因此又被誉为数字货币世界里的圣杯。 这里的稳定货币并不是指 tether 或者 digix 那种由一个中心化机构发行的货币，而应该是一种去中心化的被算法自动控制的加密货币，包括 bitUSD, Dai 等以加密资产抵押物为基础的稳定货币，和 Basecoin, kUSD 等以算法银行为基础的稳定货币。所有的稳定货币都需要 Oralce 的帮助来获取外部世界稳定货币本身和锚定资产的兑换率等数据

**借贷平台**  
SALT Lending, ETHlend 等去中心化 P2P 借贷平台允许匿名的用户用区块链上的加密资产抵押，来借贷出法币或者加密资产。 这类应用需要使用 Oracle 在贷款生成时提供价格数据， 并且能监控加密抵押物的保证金比率，在保证金不足的时候发出警告并触发清算程序。 借贷平台也能用 Oracle 来导入借款人的社交和信用和身份信息来确定不同的贷款利率

**保险应用**  
Etherisc 正在建立一个高效透明低消耗的去中心化的保险应用平台， 包括航空延误险， 农作物保险等等。用户以 ether 支付保费，购买保险，并根据保险协议得到自动赔付。Oracle 能为这类应用引入外部数据源和事件， 帮助去中心化的保险产品作出赔付的决定，并能安排未来的自动赔付

**赌场应用**  
由于区块链技术保证的透明， 即时的安全转账，以及相对传统线上赌场高达 15%的零庄家优势, 涌现了一大批如 Edgeless, DAO.Casino, FunFair 等去中心化赌场。任何在线赌场游戏的核心是产生不可预测的，可验证的随机数。 但是在链内纯确定性的环境下， 随机数的生成是很困难的。 Oracle 可以从链外注入一个安全可靠的无偏的可验证随机熵源给赌场合约使用。

**预测市场**  
去中心化的预测市场比如 Augur, Gnosis 等等，他们应用了群体的智慧来预测真实世界的结果, 比如总统选举和体育结果竞猜。在投票结果被用户质疑的时候，需要 Oracle 提供真实的最终结果。
无信任环境下如何验证身份
很多区块链应用需要通过 Oracle 从链外获取用户的身份数据，信用数据，或者社交媒体数据等。

**快递追踪和 IoT 应用**  
真实世界中的快递寄送或到达信息可以通过 Oracle 被传递到链上，触发链上智能合约的自动付款。对于区块链上的 IoT 应用, 也需要 Oracle 把链外的传感信息传到链上，让智能合约验证并触发下一步的行为。

## 1.3、现有的预言机产品

![image](https://user-images.githubusercontent.com/8265961/58678927-0b548e00-8394-11e9-90b8-5784ebc45f9e.png)

## 1.4、基本原理

外部数据源服务在区块链上部署了区块链预言机合约，提供异步查询互联网数据接口供用户合约使用。正常情况下，用户合约调用预言机合约发起查询请求后，预言机合约在 1~3 个区块内就能得到外部数据源服务取回的数据，然后回调用户合约传入数据。
![image](https://user-images.githubusercontent.com/8265961/58678936-14ddf600-8394-11e9-8c08-39366b60905a.png)

# 二、蚂蚁区块链 BaaS 平台–外部数据源服务

参见：https://tech.antfin.com/docs/2/108575

外部数据源服务会在智能合约平台部署一个外部数据源服务合约，用户合约通过调用该服务合约发送外部数据源请求，链下的 TEE 外部数据源服务对接该服务合约，监听用户的请求，然后去对应的外部数据源取数据，最后将结果返回给用户合约。

![image](https://user-images.githubusercontent.com/8265961/58678951-1f988b00-8394-11e9-96d8-9cbe6ac2bad6.png)

智能合约分为：用户合约和预言机合约

![image](https://user-images.githubusercontent.com/8265961/58678960-26bf9900-8394-11e9-9ded-7441f990fb7c.png)

OracleInterface.sol 中定义了用户合约与预言机合约的通信接口，其中用户通过 curlRequest 接口调用预言机合约。用户合约需要实现 oracleCallbackCurlResponse 接口，用于接收预言机合约的请求结果回调。

```
interface OracleInterface {
    /**
     * function： 发送 CURL 请求
     * parameters：
     *         _biz_id            ：用户自定义的业务请求 ID
     *         _curl_cmd          ：CURL 命令，参考 CURL 命令使用文档进行构造
     *         _if_callback       ：是否需要预言机将请求结果回调用户合约
     *         _callback_identity ：预言机请求结果回调的合约 ID，可以是发送请求的合约，也可以是其他合约
     *         _delay_time        ：该特性未激活，填 0 即可
     * return value          ：预言机请求 ID，是预言机合约为本次请求生成的唯一请求 ID
     */
    function curlRequestDefault(bytes32 _biz_id, string _curl_cmd, bool _if_callback, identity _callback_identity, uint256 _delay_time) external returns (bytes32);

    /**
     * function: oracleCallbackCurlResponse
     * parameters:
     *         _request_id        ：预言机合约请求 ID（在发送请求时预言机合约会返回此 ID）
     *         _biz_id            ： 用户合约的业务请求 ID
     *         _error_code        ：请求结果码，如果值是 0，则表示预言机请求处理成功；如果是其他值，则为请求处理失败，详见合约错误码表
     *        _resp_status       ：HTTP 响应的状态码，一般 200 表示 HTTP 请求处理成功，5xx 表示服务端处理错误，调用者可根据自己的使用场景做判断
     *        _resp_header       ：HTTP 响应的 header，如果 CURL 中指定了要返回 HTTP 响应的 header，则回调时会返回对应的值
     *        _resp_body         ：HTTP 响应的 body
     *        _call_identity     ：发起该请求的合约 ID
     * return value            ： 无
     */
    function oracleCallbackCurlResponse (bytes32 _request_id, bytes32 _biz_id, uint32 _error_code, uint32 _resp_status, bytes _resp_header, bytes _resp_body, identity _call_identity) external returns (bool);
}
```

# 三、主流预言机：Oraclize

## 3.1 使用举例

举例 1：查询获得的 Json 数据（http://api.k780.com/?app=finance.globalindex&inxno=000001&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json）,在oraclize query 中用 “.result.yesy_price” 可以取到 yesy_price 数据:

```
{
    "success": "1",
    "result": {
        "inxid": "1",
        "typeid": "hs",
        "inxno": "000001",
        "inxnm": "上证指数",
        "yesy_price": "3136.63",
        "open_price": "3147.05",
        "last_price": "3131.11",
        "change_price": "-5.52",
        "change_price_per": "-0.18%",
        "high_price": "3163.34",
        "low_price": "3128.87",
        "amplitude_price_per": "1.10%",
        "uptime": "2018-04-04 15:34:58"
    }
}
```

使用方式如下：

```
pragma solidity ^0.4.21;
import "github.com/oraclize/ethereum-api/oraclizeAPI.sol";

contract oraclizeJson is usingOraclize{
    event logString(string);
    event logUint(uint);
    string public str = strConcat("http://119.28.70.201:8792/getprice/?index=","DJI","&date=",uint2str(17623));

    function strConcat(){
        // oraclize解析json数据
        bytes32 queryId = oraclize_query("URL", "json(http://api.k780.com/?app=finance.globalindex&inxno=000001&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json).result.yesy_price");
    }

    function __callback(bytes32 myid, string result, bytes proof) public{
        emit logString(result);
        uint i = parseInt(result,3); //string to uint转换，
        emit logUint(i+1);
    }
}
```

举例 2：查看 Youtube 某个视频的观看人数

```
/*
   Youtube video views

   This contract keeps in storage a views counter
   for a given Youtube video.
*/


pragma solidity ^0.4.0;
import "github.com/oraclize/ethereum-api/oraclizeAPI.sol";

contract YoutubeViews is usingOraclize {

    string public viewsCount;

    event newOraclizeQuery(string description);
    event newYoutubeViewsCount(string views);

    function YoutubeViews() {
        update();
    }

    function __callback(bytes32 myid, string result) {
        if (msg.sender != oraclize_cbAddress()) throw;
        viewsCount = result;
        newYoutubeViewsCount(viewsCount);
        // do something with viewsCount. like tipping the author if viewsCount > X?
    }

    function update() payable {
        newOraclizeQuery("Oraclize query was sent, standing by for the answer..");
        oraclize_query('URL', 'html(https://www.youtube.com/watch?v=9bZkp7q19f0).xpath(//*[contains(@class, "watch-view-count")]/text())');
    }

}
```

## 3.2 数据源

Orcalize 的数据源有：

-   URL：互联网中的连接。
-   computation：它允许抓取应用的链下执行结果。在退出前，该应用必须在最后一行打印查询结果（在标准输出栏）
-   nested：nested 数据源是一个元数据源，它不提供访问其他服务的权限。它用来提供简单相加逻辑，允许单个查询在任何可用数据源的基础上进行子查询，并产生一个单独字符串作为结果。
-   IPFS：文件传输系统中的数据
-   blockchain：blockchain 数据源允许用户访问其他区块链的数据。可以提交给 blockchain 数据源的查询包括 bitcoin blockchain height、litecoinhashrate、bitcoin difficulty、1NPFRDJuEdyqEn2nmLNaWMfojNksFjbL4S balance 等
-   WolframAlpha：WolframAlpha 是开发计算数学应用软件的沃尔夫勒姆研究公司开发出的新一代的搜索引擎，能根据问题直接给出答案的网站，于 2009 年 5 月 15 日晚 7 点（美国中部当地时间，北京时间 5 月 16 日上午 8 点）提前上线，用户在搜索框键入需要查询的问题后，该搜索引擎将直接向用户返回答案，而不是返回一大堆链接。

## 3.3 收费

![image](https://user-images.githubusercontent.com/8265961/58679045-70a87f00-8394-11e9-96ce-ea90dffdaa59.png)

## 3.4 真实性：

Oraclize 提供了的 TLSNotary Proof 来证明返回的结果是没有经过任何人修改的。TLS 是一个网络传输协议，利用可信机构颁发的 CA 来保证传输正确。TLSNotary 是 TLS 的改进，能够证明 Oraclize 提供给合约的数据就是某个特点时间点的正确数据。

参见：https://github.com/Oraclize/proof-verification-tool

## 3.5 解析助手

HTTP 请求返回的结果可以是 HTML、JSON、XML 或二进制等格式。在 Solidity 中，解析结果是很困难的，且代价很高。Oraclize 提供了解析助手，在服务端上处理解析，最终得到的结果就是用户需要的那部分。

## 3.6 加密查询

为了满足一些场景：比如用户不想暴露自己的查询地址，参数等信息。Oraclize 提供了一个加密查询的方法。

参见：https://github.com/Oraclize/encrypted-queries

## 3.7 原理

Oraclize 在以太坊上部署了一个名为 usingOraclize 的智能合约，如果需要其数据访问服务，只需要在自己的智能合约中引用该智能合约，然后根据 API 文档中描述的方法进行相关的调用即可。

![image](https://user-images.githubusercontent.com/8265961/58679064-8027c800-8394-11e9-9328-099555a2a11c.png)

# 四、参考

-   Oraclize：https://provable.xyz/
-   去中心化的预言机：全面概览：https://medium.com/@liyunlong518/%E5%8E%BB%E4%B8%AD%E5%BF%83%E5%8C%96%E7%9A%84%E9%A2%84%E8%A8%80%E6%9C%BA-%E5%85%A8%E9%9D%A2%E6%A6%82%E8%A7%88-2487b5d97926
-   什么是区块链预言机（BlockChain Oracle）：https://juejin.im/post/5c236f456fb9a049c965b9e4
-   区块链落地的必需工具——预言机（Oracle)：https://blog.csdn.net/weixin_43761479/article/details/85067727
