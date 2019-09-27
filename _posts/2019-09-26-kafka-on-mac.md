---
layout: post
title: "kafka on mac"
subtitle: "kafka on mac"
date: 2019-09-26 10:31:11
author: "hello2mao"
hidden: true
tags:
    - message queue
---

1. 安装 kafka
   执行命令：brew install kafka

2. 修改 server.properties
   执行命令：vi /usr/local/etc/kafka/server.properties
   增加一行配置如下：
   listeners=PLAINTEXT://localhost:9092

3. 启动 zookeeper
   执行命令： zkServer start

4. 以 server.properties 的配置，启动 kafka
   执行命令：kafka-server-start /usr/local/etc/kafka/server.properties

5. 新建 session，查看 kafka 的 topic
   执行命令：kafka-topics --list --zookeeper localhost:2181

6. 启动 kafka 生产者
   执行命令：kafka-console-producer --topic [topic-name] --broker-list localhost:9092(第 2 步修改的 listeners)

7. 启动 kafka 消费者
   执行命令：kafka-console-consumer --bootstrap-server localhost:9092 —topic [topic-name]
