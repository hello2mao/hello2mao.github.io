---
layout: post
title: "排列与组合"
subtitle: "permutation and combination"
date: 2019-10-06 17:51:00
author: "hello2mao"
hidden: true
tags:
  - algorithm
---
<!-- TOC -->

- [1. permutation](#1-permutation)
- [2. combination](#2-combination)

<!-- /TOC -->


# 1. permutation

![](/img/posts/permutation.png)

Code implement:
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permutation {

    // n = nums.length
    // P(n, k)
    public static void P(int[] nums, int d, int k, boolean[] used, List<Integer> cur, List<List<Integer>> res) {
        if (d == k) {
            res.add(new ArrayList<>(cur));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (used[i]) {
                continue;
            }
            cur.add(nums[i]);
            used[i] = true;
            P(nums, d + 1, k, used, cur, res);
            cur.remove(cur.size() - 1);
            used[i] = false;
        }
    }

    public static void main(String[] args) {
        int[] testNums = new int[]{1,2,3};
        boolean[] used = new boolean[testNums.length];
        List<List<Integer>> res = new ArrayList<>();
        P(testNums, 0, 2, used, new ArrayList<>(), res);

        for (List<Integer> item : res) {
            System.out.println(Arrays.toString(item.toArray()));
        }
    }
}
```

Output:
```
[1, 2]
[1, 3]
[2, 1]
[2, 3]
[3, 1]
[3, 2]
```

# 2. combination

![](/img/posts/combination.png)

Code implement:
```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Combination {

    // n = nums.length
    // C(n, k)
    public static void C(int[] nums, int d, int k, int start, List<Integer> cur, List<List<Integer>> res) {
        if (d == k) {
            res.add(new ArrayList<>(cur));
            return;
        }

        for (int i = start; i < nums.length; i++) {
            cur.add(nums[i]);
            C(nums, d + 1, k, i + 1, cur, res);
            cur.remove(cur.size() - 1);
        }
    }

    public static void main(String[] args) {
        int[] testNums = new int[]{1,2,3};
        List<List<Integer>> res = new ArrayList<>();
        C(testNums, 0, 2, 0, new ArrayList<>(), res);

        for (List<Integer> item : res) {
            System.out.println(Arrays.toString(item.toArray()));
        }
    }
}
```

Output:
```
[1, 2]
[1, 3]
[2, 3]
```
