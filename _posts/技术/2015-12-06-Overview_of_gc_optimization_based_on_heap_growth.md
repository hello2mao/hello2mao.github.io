---
layout: post
title: 基于动态堆增长技术的GC调优综述
category: 技术
tags: Android
keywords: android,GC
description:
---

***版权声明：本文为博主原创文章，转载请注明来自 https://hello2mao.github.io***

---

###一、论文概要

***The Economics of Garbage Collection***  
***作  者：***  J Singer，RE Jones，G Brown，M Luján  
***摘  要：***  This paper argues that economic theory can improve our understanding of memory management. We introduce the allocation curve, as an analogue of the demand curve from microeconomics. An allocation curve for a program characterises how the amount of garbage collection activity required during its execution varies in relation to the heap size associated with that program. The standard treatment of microeconomic demand curves (shifts and elasticity) can be applied directly and intuitively to our new allocation curves. As an application of this new theory, we show how allocation elasticity can be used to control the heap growth rate for variable sized heaps in Jikes RVM.  
***出版源：***  《Acm Sigplan Notices》, 2010, 45(8):103-112  
***关键词：***  allocation curve / elasticity / garbage collection / java / memory management / microeconomics  
***被引量：***  10

---
***Controlling garbage collection and heap growth to reduce the execution time of Java applications***  
***作  者：***  T Brecht，E Arjomandi，L Chang，P Hang  
***摘  要：***  In systems that support garbage collection, a tension exists between collecting garbage too frequently and not collecting garbage frequently enough. Garbage collection that occurs too frequently may introduce unnecessary overheads at the rist of not collecting much garbage during each cycle. On the other hand, collecting garbage too infrequently can result in applications that execute with a large amount of virtual memory (i.e., with a large footprint) and suffer from increased execution times die to paging. In this paper, we use a large colleciton of Java applications and the highly tuned and widely used Boehm-Demers-Weiser (BDW) conservative mark-and-sweep garbage collector to experimentally examine the extent to which the frequency of garbage collectio impacts an application's execution time, footprint, and pause times. We use these results to devise some guidelines for controlling garbage and heap growth in a conservative garbage collection in order to minimize application execution times. Then we describe new strategies for controlling in order to minimize application execution times.  
***出版源：***  《Acm Transactions on Programming Languages & Sy..., 2006, 28(11):353--366  
***关键词：***  garbage collection / heap growth / implementation / Java / memory / management / performance measurement / programming languages  
***被引量：***  73



###二、The Economics of Garbage Collection
全文在这里：[Citeseer](http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=CF223C43EEB7D2C4D85275FD75F03E7F?doi=10.1.1.164.2789&rep=rep1&type=pdf)  

这篇论文提出了分配弹性的概念，并利用系统的实时弹性来调节堆的增长速率。

####2.1 GC的分配曲线及分配弹性
这篇论文针对GC提出了分配曲线的概念，如下图所示：

![allocation_curve.png](/public/img/technology/allocation_curve.png)  

此概念来源于微观经济学中的供求关系，本文模仿微观经济学中的弹性理论，提出了分配弹性的概念，用来描述java堆大小的变化对GC数量的影响(其中g是GC的数量，h是java堆的大小)：

![Eq_2.png](/public/img/technology/Eq_2.png)

作者把此公式化简为：

![Eq_3](/public/img/technology/Eq_3.png)

那么当前的分配弹性就可以用下面的公式来计算：

![Eq_4.png](/public/img/technology/Eq_4.png)

用DaCapo Benchmark测试此分配曲线，得到如下图所示：

![benchmark-test.png](/public/img/technology/benchmark-test.png)

假设分配曲线的分配弹性为E，如果|E|<1,那么此时java堆过大，存在浪费空间的现象；如果|E|>1,那么此时大多数GC都不能回收足够的内存，也就是java堆剩余空间过小。

####2.2 利用分配弹性来控制堆增长
我们知道java堆大小或者说java堆是否需要扩大、扩大多少取决于两个因素：（1）当前GC负载，也就是当前GC执行的时间。（2）当前对象存活率。当这两个值都比较大的时候，java堆就需要增大，并且值越大java堆增大越多。

论文的作者提出了利用系统的实时分配弹性来控制堆增长的方法。首先系统启动的时候设定一个作为参考标准的分配弹性E，当系统的实时弹性|currE| > |E|时，java堆的大小就需要增加，此时，较大的|currE|会减慢堆增长的频率，较小的|currE|意味着堆会迅速的扩大。同样用benchmark测试得到如下图所示：

![benchmark-test-2.png](/public/img/technology/benchmark-test-2.png)

可以看出基于弹性的堆增长策略更加的灵活，能更加简单的去改变堆增长的速率，当然，减小应用执行时间的开销就是堆变得更大，也就消耗了更多的内存空间。

###三、 Controlling garbage collection and heap growth to reduce the execution time of Java applications
全文在这里：[Citeseer](http://citeseerx.ist.psu.edu/viewdoc/download;jsessionid=227A37ABF73E6C66E33D41B0EFE7CF96?doi=10.1.1.23.6538&rep=rep1&type=pdf)

这篇论文主要关注以下两点：  
（1）何时触发GC。  
（2）何时java堆应该扩张，应该扩张多大。

####3.1 BDW标记清楚垃圾回收器
针对问题一，作者使用的是Boehm-Demers-Weiser(BDW)标记清除垃圾回收器，何时触发GC取决于一个称为free-space-divisor(FSD)的常数，如下面伪代码所示：

![BDW.png](/public/img/technology/BDW.png)

当内存分配器在分配一个对象遇到java堆空间不够时就会调用此代码，此段代码会检查已使用的内存是否大于heap/FSD，如果是就触发GC，否则就根据要求扩展堆。

####3.2 基于阈值的堆扩展
上节介绍的BDW垃圾回收器扩展堆时没有考虑系统的可用内存，如果系统可用内存比较少了，对于扩展堆就应该谨慎考虑。

考虑到这个，作者设计并实现了一个基于可用内存阈值的的新算法来控制GC和堆增长。同时也针对最开始提出的问题二。如下图所示：

![thresholds-1.png](/public/img/technology/thresholds-1.png)

T1~Ti为设定的阈值，当第一次到达阈值时，GC会触发。到达阈值Ti而触发GC所回收的内存为Ri，Ri用来决定下次到达Ti时是否GC。假设：

![thresholds-2.png](/public/img/technology/thresholds-2.png)

当第一次到达T1时触发GC，也就是C点。回收内存R1，降到D。第二次到达T1时也就是在E点，此时由于前一次GC的R1小于S1，也就是前一次回收的内存太少，所以此时不触发GC，而是扩展堆到T2。当到达T2，也就是F点时触发GC回到G，回收内存为R2。再次到达T1，由于R2大于S1，也就是前一次GC回收了足够多的内存，此时触发GC。后面以此类推。

关于Ti阈值的选择，作者是通过实验决定的，给出了两个参考，对于可用内存为64MB的应用：  0.40, 0.55, 0.70, 0.85, 0.92, 1.00, 1.15, 和 30.00。  对于可用内存为128MB的应用：  0.80, 0.85, 0.90, 0.95, 1.00, 1.05, 和 10.00。
###四、总结
这两篇论文都尝试用数学的方法来描述所遇到的GC问题。第一篇利用分配弹性的计算来改变堆的增长率，第二篇利用FSD来限定是触发GC还是增长堆，并且设计基于阈值的新算法来进行堆扩展。
