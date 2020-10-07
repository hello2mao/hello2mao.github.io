---
layout: post
title: "Fabric中的Second-Chance缓存淘汰算法的实现分析"
date: 2020-05-19
author: "hello2mao"
tags:
  - blockchain
---


<!-- TOC -->

- [一、概述](#一概述)
  - [1.1 Fabric MSP](#11-fabric-msp)
  - [1.2 Second-Chance Algorithm](#12-second-chance-algorithm)
- [二、Fabric实现分析](#二fabric实现分析)
  - [2.1 创建缓存实例](#21-创建缓存实例)
  - [2.2 查询对象](#22-查询对象)
  - [2.3 插入对象](#23-插入对象)

<!-- /TOC -->


# 一、概述
## 1.1 Fabric MSP
Fabric引入MSP（Membership Service Provider），即成员关系服务提供者，这一抽象化的模块组件来管理Fabric中的众多参与者（peer、orderer等）。
MSP将颁发证书和校验证书，以及用户认证背后的所有密码学机制与协议都抽象了出来。对Fabric网络中的成员进行身份的管理（身份验证）与认证（签名与验签）。

成员进行身份验证时，提供的是X.509的证书，所以MSP会频繁的**反序列化**、**验证**这些X.509证书，此时MSP模块就利用Second-Chance算法来缓存一些解析验证的结果，从而提高性能。

## 1.2 Second-Chance Algorithm
Second-Chance算法利用FIFO，在在淘汰对象时，会检查待淘汰对象的引用标志位，如果对象被引用过，该对象引用位清零，重新插入队列尾部，像新的对象一样；如果该对象未被引用过，则将被淘汰。

原理如下：

在FIFO算法的基础上，
- 为缓存中的所有对象增加一个“引用标志位”-
- 每次对象被使用时，设置标志位为1
- 新对象加入缓存时，设置其标志位为0
- 在淘汰对象时，查看它的标志位。如果为0，则淘汰该对象；如果为1，则设置其标志位为0，重新加入队列末尾。

# 二、Fabric实现分析
Fabric MSP模块实现了Second-Chance算法，在目录：`msp/cache`

## 2.1 创建缓存实例
创建缓存实例时仅需设置缓存大小：

```go
func newSecondChanceCache(cacheSize int) *secondChanceCache {
	var cache secondChanceCache
	cache.position = 0
	cache.items = make([]*cacheItem, cacheSize)
	cache.table = make(map[string]*cacheItem)

	return &cache
}
```

缓存实例定义为：

```go
type secondChanceCache struct {
	// manages mapping between keys and items
	table map[string]*cacheItem

	// holds a list of cached items.
	items []*cacheItem

	// indicates the next candidate of a victim in the items list
	position int

	// read lock for get, and write lock for add
	rwlock sync.RWMutex
}
```

其中：

- `table`：实际缓存对象的key和对象的映射
- `items`：缓存，大小在初始化的时候确定
- `position` ：下一个淘汰检查的对象在items中的索引；初始化时指向items[0]。

## 2.2 查询对象
```go
func (cache *secondChanceCache) get(key string) (interface{}, bool) {
	cache.rwlock.RLock()
	defer cache.rwlock.RUnlock()

	// 查询缓存
	item, ok := cache.table[key]
	if !ok {
		return nil, false
	}

	// 每次对象被使用时，设置标志位为1
	// referenced bit is set to true to indicate that this item is recently accessed.
	atomic.StoreInt32(&item.referenced, 1)

	// 返回缓存中的对象
	return item.value, true
}
```

查询的逻辑比较简单，每次对象被使用时，设置标志位为1。

## 2.3 插入对象
```go
func (cache *secondChanceCache) add(key string, value interface{}) {
	cache.rwlock.Lock()
	defer cache.rwlock.Unlock()

	// 插入key值已存在的对象，则直接修改缓存中对应key的对象为新对象，并把引用次数设为1
	if old, ok := cache.table[key]; ok {
		old.value = value
		atomic.StoreInt32(&old.referenced, 1)
		return
	}

	var item cacheItem
	item.key = key
	item.value = value
	atomic.StoreInt32(&item.referenced, 0)

	size := len(cache.items)
	num := len(cache.table)
	// 缓存还未填满，则新对象直接插到items数组里
	if num < size {
		// cache is not full, so just store the new item at the end of the list
		cache.table[key] = &item
		cache.items[num] = &item
		return
	}

	// starts victim scan since cache is full
	// 缓存已经用光，则需要扫描整个items数据，进行对象的淘汰
	// 在淘汰对象时，查看它的标志位。如果为0，则淘汰该对象；如果为1，则设置其标志位为0，重新加入队列末尾。
	for {
		// checks whether this item is recently accessed or not
		victim := cache.items[cache.position]
		// 标志为0，直接淘汰此对象
		if atomic.LoadInt32(&victim.referenced) == 0 {
			// a victim is found. delete it, and store the new item here.
			delete(cache.table, victim.key)
			cache.table[key] = &item
			cache.items[cache.position] = &item
			cache.position = (cache.position + 1) % size
			return
		}

		// 标志为1，则设置其标志位为0，重新加入队列末尾（此处是修改position）。
		// referenced bit is set to false so that this item will be get purged
		// unless it is accessed until a next victim scan
		atomic.StoreInt32(&victim.referenced, 0)
		cache.position = (cache.position + 1) % size
	}
}
```