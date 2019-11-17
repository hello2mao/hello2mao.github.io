---
layout: post
title: "海量数据处理问题"
subtitle: "big data problem"
date: 2019-10-15 18:46:00
author: "hello2mao"
hidden: true
tags:
  - algorithm
---

# 方案概述

- 分而治之/hash 映射 + hash 统计 + 堆/快速/归并排序
- 双层桶划分
- Bloom filter/Bitmap
- Trie 树/数据库/倒排索引
- 外排序
- 分布式处理之 Hadoop/Mapreduce

# 分而治之/hash 映射 + hash 统计 + 堆/快速/归并排序

**1. 海量日志数据，提取出某日访问百度次数最多的那个 IP**

- 分而治之/hash 映射：针对数据太大，内存受限，只能是：把大文件化成(取模映射)小文件，即 16 字方针：大而化小，各个击破，缩小规模，逐个解决
- hash_map 统计：当大文件转化了小文件，那么我们便可以采用常规的 hash_map(ip，value)来进行频率统计。
- 堆/快速排序：统计完了之后，便进行排序(可采取堆排序)，得到次数最多的 IP。

即：
首先是这一天，并且是访问百度的日志中的 IP 取出来，逐个写入到一个大文件中。注意到 IP 是 32 位的，最多有个 2^32 个 IP。同样可以采用映射的方法，比如%1000，把整个大文件映射为 1000 个小文件，再找出每个小文中出现频率最大的 IP（可以采用 hash_map 对那 1000 个文件中的所有 IP 进行频率统计，然后依次找出各个文件中频率最大的那个 IP）及相应的频率。然后再在这 1000 个最大的 IP 中，找出那个频率最大的 IP，即为所求。

**2、寻找热门查询，300 万个查询字符串中统计最热门的 10 个查询**

原题：搜索引擎会通过日志文件把用户每次检索使用的所有检索串都记录下来，每个查询串的长度为 1-255 字节。假设目前有一千万个记录（这些查询串的重复度比较高，虽然总数是 1 千万，但如果除去重复后，不超过 3 百万个。一个查询串的重复度越高，说明查询它的用户越多，也就是越热门），请你统计最热门的 10 个查询串，要求使用的内存不能超过 1G。

TBD

# Ref

- https://blog.csdn.net/v_july_v/article/details/7382693