---
layout: post
title: "simple promise retry"
subtitle: "simple promise retry"
date: 2019-10-01 18:15:00
author: "hello2mao"
hidden: true
tags:
  - javascript
---

```javascript
function retry(fn, retriesLeft = 5, interval = 1000) {
  return new Promise((resolve, reject) => {
    fn()
      .then(resolve)
      .catch(error => {
        if (retriesLeft === 1) {
          // reject('maximum retries exceeded');
          reject(error);
          return;
        }
        setTimeout(() => {
          // Passing on "reject" is the important part
          retry(fn, retriesLeft - 1, interval).then(resolve, reject);
        }, interval);
      });
  });
}
```
