---
layout: post
title: "以太坊难度调整算法"
subtitle: "Ethereum block difficulty adjustment algorithm"
date: 2019-02-07 10:51:11
author: "hello2mao"
tags:
  - blockchain
---
<!-- TOC -->

- [一、什么是难度](#%e4%b8%80%e4%bb%80%e4%b9%88%e6%98%af%e9%9a%be%e5%ba%a6)
- [二、以太坊中难度的计算](#%e4%ba%8c%e4%bb%a5%e5%a4%aa%e5%9d%8a%e4%b8%ad%e9%9a%be%e5%ba%a6%e7%9a%84%e8%ae%a1%e7%ae%97)

<!-- /TOC -->

# 一、什么是难度
难度(Difficulty)一词来源于区块链技术的先驱比特币，用来度量挖出一个区块平均需要的运算次数。

难度(Difficulty)通过控制合格的解在空间中的数量来控制平均求解所需要尝试的次数，也就可以间接的控制产生一个区块需要的时间，这样就可以使区块以一个合理而稳定的速度产生。

当挖矿的人很多，单位时间能够尝试更多次时，难度就会增大，当挖矿的人减少，单位时间能够尝试的次数变少时，难度就降低。这样产生一个区块需要的时间就可以做到稳定。

# 二、以太坊中难度的计算
关键方法CalcDifficulty在consensus/ethash/consensus.go中：
```golang
// CalcDifficulty is the difficulty adjustment algorithm. It returns
// the difficulty that a new block should have when created at time
// given the parent block's time and difficulty.
// TODO (karalabe): Move the chain maker into this package and make this private!
func CalcDifficulty(config *params.ChainConfig, time uint64, parent *types.Header) *big.Int {
	next := new(big.Int).Add(parent.Number, big1)
	switch {
	case config.IsMetropolis(next):
		return calcDifficultyMetropolis(time, parent)
	case config.IsHomestead(next):
	    // 正在使用的
		return calcDifficultyHomestead(time, parent)
	default:
		return calcDifficultyFrontier(time, parent)
	}
}

// calcDifficultyHomestead is the difficulty adjustment algorithm. It returns
// the difficulty that a new block should have when created at time given the
// parent block's time and difficulty. The calculation uses the Homestead rules.
func calcDifficultyHomestead(time uint64, parent *types.Header) *big.Int {
	// https://github.com/ethereum/EIPs/blob/master/EIPS/eip-2.mediawiki
	// algorithm:
	// diff = (parent_diff +
	//         (parent_diff / 2048 * max(1 - (block_timestamp - parent_timestamp) // 10, -99))
	//        ) + 2^(periodCount - 2)

	bigTime := new(big.Int).SetUint64(time)
	bigParentTime := new(big.Int).Set(parent.Time)

	// holds intermediate values to make the algo easier to read & audit
	x := new(big.Int)
	y := new(big.Int)

	// 1 - (block_timestamp - parent_timestamp) // 10
	x.Sub(bigTime, bigParentTime)
	x.Div(x, big10)
	x.Sub(big1, x)

	// max(1 - (block_timestamp - parent_timestamp) // 10, -99)
	if x.Cmp(bigMinus99) < 0 {
		x.Set(bigMinus99)
	}
	// (parent_diff + parent_diff // 2048 * max(1 - (block_timestamp - parent_timestamp) // 10, -99))
	y.Div(parent.Difficulty, params.DifficultyBoundDivisor)
	x.Mul(y, x)
	x.Add(parent.Difficulty, x)

	// minimum difficulty can ever be (before exponential factor)
	if x.Cmp(params.MinimumDifficulty) < 0 {
		x.Set(params.MinimumDifficulty)
	}
	// for the exponential factor
	periodCount := new(big.Int).Add(parent.Number, big1)
	periodCount.Div(periodCount, expDiffPeriod)

	// the exponential factor, commonly referred to as "the bomb"
	// diff = diff + 2^(periodCount - 2)
	if periodCount.Cmp(big1) > 0 {
		y.Sub(periodCount, big2)
		y.Exp(big2, y, nil)
		x.Add(x, y)
	}
	return x
}
```
计算一个区块的难度时，需要以下输入：

 - parent_timestamp：上一个区块产生的时间
 - parent_diff：上一个区块的难度
 - block_timestamp：当前区块产生的时间
 - block_number：当前区块的序号

难度block_diff的计算公式为：
```
block_diff = parent_diff + [难度调整] + [难度炸弹]
[难度调整] = parent_diff // 2048 * MAX(1 - (block_timestamp - parent_timestamp) // 10, -99))
[难度炸弹] = INT(2**((block_number // 100000) - 2))
```
另外，区块难度不能低于以太坊的创世区块的难度，这是以太坊难度的下限。