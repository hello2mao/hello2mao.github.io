---
layout: post
title: "javascript async await"
subtitle: "javascript async await"
date: 2019-09-29 18:15:00
author: "hello2mao"
hidden: true
tags:
  - javascript
---

# 简介

async 是“异步”的简写，而 await 可以认为是 async wait 的简写。async 用于申明一个 function 是异步的，而 await 用于等待一个异步方法执行完成。另外还有一个很有意思的语法规定，await 只能出现在 async 函数中。

# async

async 函数（包含函数语句、函数表达式、Lambda 表达式）会返回一个 Promise 对象，如果在函数中 return 一个直接量，async 会把这个直接量通过 Promise.resolve() 封装成 Promise 对象。

# await

一般来说，都认为 await 是在等待一个 async 函数完成。  
await 不仅仅用于等 Promise 对象，它可以等任意表达式的结果，所以，await 后面实际是可以接普通函数调用或者直接量的：

- 如果它等到的不是一个 Promise 对象，那 await 表达式的运算结果就是它等到的东西。
- 如果它等到的是一个 Promise 对象，await 就忙起来了，它会阻塞后面的代码，等着 Promise 对象 resolve，然后得到 resolve 的值，作为 await 表达式的运算结果。

# async/await 的优势在于处理 then 链

举例如下：

```javascript
/**
/**
 * 传入参数 n，表示这个函数执行的时间（毫秒）
 * 执行的结果是 n + 200，这个值将用于下一步骤
 */
function takeLongTime(n) {
  return new Promise(resolve => {
    setTimeout(() => resolve(n + 200), n);
  });
}

function step1(n) {
  console.log(`step1 with ${n}`);
  return takeLongTime(n);
}

function step2(n) {
  console.log(`step2 with ${n}`);
  return takeLongTime(n);
}

function step3(n) {
  console.log(`step3 with ${n}`);
  return takeLongTime(n);
}

// Promise
function doIt() {
  console.time("doIt");
  const time1 = 300;
  step1(time1)
    .then(time2 => step2(time2))
    .then(time3 => step3(time3))
    .then(result => {
      console.log(`result is ${result}`);
      console.timeEnd("doIt");
    });
}
doIt();

// async await - 1
async function doIt() {
  console.time("doIt");
  const time1 = 300;
  const time2 = await step1(time1);
  const time3 = await step2(time2);
  const result = await step3(time3);
  console.log(`result is ${result}`);
  console.timeEnd("doIt");
}
doIt();

// async await - 2
async function doIt() {
  console.time("doIt");
  const time1 = 300;
  const time2 = await step1(time1);
  const time3 = await step2(time1, time2);
  const result = await step3(time1, time2, time3);
  console.log(`result is ${result}`);
  console.timeEnd("doIt");
}
doIt();
```
