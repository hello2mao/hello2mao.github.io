---
layout: post
title: "java基础代码实现"
subtitle: "java basic for coding"
date: 2019-10-01 15:00:00
author: "hello2mao"
hidden: true
tags:
  - algorithm
---

<!-- TOC -->

- [1. java.lang.Integer](#1-javalanginteger)
- [2. java.lang.Long](#2-javalanglong)
- [3. java.lang.StringBuffer](#3-javalangstringbuffer)
- [4. java.lang.Math](#4-javalangmath)
- [5. java.util.Collections](#5-javautilcollections)
- [6. java.util.Arrays](#6-javautilarrays)
- [7. java.util.ArrayList](#7-javautilarraylist)
- [8. java.util.LinkedList](#8-javautillinkedlist)
- [9. java.util.Queue](#9-javautilqueue)
- [10. java.util.Stack](#10-javautilstack)
- [11. java.util.HashMap](#11-javautilhashmap)
- [12. ASCII](#12-ascii)

<!-- /TOC -->

# 1. java.lang.Integer

```java
// max int
Integer.MAX_VALUE

// min int
Integer.MIN_VALUE

// String 转 int
int s = Integer.parseInt(sInt);
```

# 2. java.lang.Long

```java
 // String 转 long
 long ls = Long.parseLong(sInt);
```

# 3. java.lang.StringBuffer

```java
// String 转 StringBuffer
StringBuffer sb = new StringBuffer("abc");

// append
sb.append("efg");

// toString
System.out.println(s.toString());

// 删除指定位置的字符
sb.deleteCharAt(index)

// 删除指定区间以内的所有字符，包含 start，不包含 end 索引值的区间
sb.delete(start, end)

// 在 StringBuffer 对象中插入内容，然后形成新的字符串
sb.insert(4, "hello");

// 将 StringBuffer 对象中的内容反转，然后形成新的字符串
sb.reverse();

// 修改对象中索引值为 index 位置的字符为新的字符 ch
sb.setCharAt(1,'D');
```

# 4. java.lang.Math

```java
// min
Math.min(num1, num2)

// max
Math.max(num1, num2)
```

# 5. java.util.Collections

```java
// reverse ArrayList
Collections.reverse(aList);
```

# 6. java.util.Arrays

```java
// sort array with quick sort: O(nlogn)，快排
Arrays.sort(nums)

// 字符串数组类型的 aStr 转换成 List 类型的 aList
List<String> aList = Arrays.asList(aStr);
```

# 7. java.util.ArrayList

```java
// new ArrayList
List<Integer> res = new ArrayList<>();

// size
int size = res.size();

// clone
List<Integer> newItem = new ArrayList<>(oldItem);
```

# 8. java.util.LinkedList

```java
// 声明生成的是一个链队列。list.get(0)获得最后 add 进去的元素。和 getLast()同样效果。
Queue<String> queue = new LinkedList<String>();
```

# 9. java.util.Queue

```java
// queue
Queue<String> queue = new LinkedList<String>();

 // 添加元素
queue.offer("a");

// 返回第一个元素，并在队列中删除
e = queue.poll());

// 返回第一个元素(查询)
e = queue.peek());
```

# 10. java.util.Stack

```java
// 堆栈。
Stack<Integer> stack = new Stack();
```

# 11. java.util.HashMap

```java
public class GFG { 
    public static void main(String[] args) 
    { 
  
        HashMap<String, Integer> map 
            = new HashMap<>(); 
  
        print(map); 
        map.put("vishal", 10); 
        map.put("sachin", 30); 
        map.put("vaibhav", 20); 
  
        System.out.println("Size of map is:- "
                           + map.size()); 
  
        print(map); 
        if (map.containsKey("vishal")) { 
            Integer a = map.get("vishal"); 
            System.out.println("value for key"
                               + " \"vishal\" is:- "
                               + a); 
        } 
  
        map.clear(); 
        print(map); 
    } 
  
    public static void print(Map<String, Integer> map) 
    { 
        if (map.isEmpty()) { 
            System.out.println("map is empty"); 
        } 
  
        else { 
            System.out.println(map); 
        } 
    } 
} 
```

# 12. ASCII

- 需要记住的 ASCII 码表：0（48），9（57），A（65），Z（90），a（97），z（122）
