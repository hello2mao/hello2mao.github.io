---
layout: post
title: 0xBenchmark中垃圾回收测试模块的分析及改进
category: 技术
tags: Android
keywords: android,GC
description:
---

***版权声明：本文为博主原创文章，转载请注明来自 https://hello2mao.github.io***

---

###1. 0xBenchmark介绍
0xBenchmark是google官方的测试程序，0xlab给0xBenchmark集成了17个benchmark，包括2个计算性能，1个JavaScript基准测试，7个2D图形渲染，4个3D图形渲染，1个垃圾回收性能测试，2个本地benchmark用来测试系统性能。测试结果 供详细精确的数据,可以为性能优化供指导和比对。它包含的测试项目如下。

**计算性能**

* Linpack 测试Java的浮点性能* Scimark2 测试常用数学运算性能如快速Fourier变换、矩阵运算等性能
**JavaScript**
* SunSpider JavaScript基准测试
**2D图形渲染**

* Canvas Redraw 测试Canvas绘图性能* Draw Circle 一个简单的2D动画程序，测试刷新率
* DrawRect 在画布上随机添加矩形
* DrawCircle2 在画布上随机渲染圆圈
* DrawArc 简单的2D动画
* DrawText 计算文本渲染速度
* DrawImage 计算图片渲染速度

**3D图形渲染**

* GL Cube 用OpenGL ES去渲染一个旋转的魔方
* GL Teapot 用OpenGL ES去渲染一个旋转的茶壶模型
* NeHe Lesson08 测试贴图着色混合性能
* NeHe Lesson16 测试雾化效果性能

**垃圾回收性能**

* Garbage Collection 测试垃圾回收性能**本地测试程序**
* LibMicro 测试系统调用和库调用的性能* BYTE UnixBench 为Unix类的系统提供了一些基本的性能指标
###2. 详解垃圾回收测试模块
垃圾回收机制，最早出现于世界上第二元老语言Lisp，Jean E. Sammet曾经说过，Lisp语言最长久的共享之一是一个非语言特征，即代表了系统自动处理内存的方法的术语及其技术----垃圾收集（GC，Garbage Collection）。

0xBenchmark中就有专门测试android虚拟机的垃圾回收性能的模块。此测试程序的代码移植于由John Ellis和Pete Kovac写的一个基准测试程序。测试的算法是递归自顶向下和递归自底向上创建完全二叉树,以及创建大的浮点数组,对于内存块的创建还分为长生命周期对象和临时对象,长生命周期对象的引用要在测试函数运行完毕时才会丢失,而临时对象在创建完毕后即被丢失,测试的基准为创建对象所需要的时间。

每个二叉树节点的数据结构如下：

	class Node {
    	Node left, right;
    	int i, j;
    	Node(Node l, Node r) { left = l; right = r; }
    	Node() { }
	}
递归自顶向下创建完全二叉树的代码如下：

    static void Populate(int iDepth, Node thisNode) {
        if (iDepth<=0) {//递归结束条件
            return;
        } else {
            iDepth--;//树深度减一
            thisNode.left  = new Node();
            thisNode.right = new Node();
            Populate (iDepth, thisNode.left);//递归创建左子树
            Populate (iDepth, thisNode.right);//递归创建右子树
        }
    }
递归自底向上创建完全二叉树的代码如下：

	static Node MakeTree(int iDepth) {
        if (iDepth<=0) {//递归结束条件
            return new Node();
        } else {
            return new Node(MakeTree(iDepth-1),//自底向上递归调用
                    MakeTree(iDepth-1));
        }
    }
在测试程序一开始运行的时候就创建长生命周期对象，并在测试结束前检查长生命周期的对象是否被回收了，如果回收了则测试失败。并且长生命周期对象分为两类，第一类是完全二叉树，第二类是浮点数组。

	longLivedTree = new Node();
    Populate(kLongLivedTreeDepth, longLivedTree);//完全二叉树

    double array[] = new double[kArraySize];//浮点数组
    for (int i = 0; i < kArraySize/2; ++i) {//把数组的一半初始化
        array[i] = 1.0/i;
    }
    ....
    if (longLivedTree == null || array[1000] != 1.0/1000)
            update("Failed");//测试结束，检查是否还存活。
测试程序最主要的测试部分是递归创建不同深度的完全二叉树，同一深度又有自顶向下和自底向上两种，同时需要记录创建完成所需的时间：

    static void TimeConstruction(int depth) {
        ....
        tStart = System.currentTimeMillis();//记录开始创建时间
        for (int i = 0; i < iNumIters; ++i) {
            tempTree = new Node();
            Populate(depth, tempTree);//自顶向下创建完全二叉树
            tempTree = null;
        }
        tFinish = System.currentTimeMillis();//记录完成时间
        ....
        tStart = System.currentTimeMillis();
        for (int i = 0; i < iNumIters; ++i) {
            tempTree = MakeTree(depth);//自底向上创建完全二叉树
            tempTree = null;
        }
        tFinish = System.currentTimeMillis();
        ....
    }
上面测试程序中，二叉树的创建深度以及数组的大小由以下常数决定：

    public static final int kStretchTreeDepth    = 16;    // about 8Mb
    public static final int kLongLivedTreeDepth  = 14;  // about 2Mb
    public static final int kArraySize  = 125000;  // about 2Mb
    public static final int kMinTreeDepth = 2;
    public static final int kMaxTreeDepth = 8;
可以看到无论是二叉树的深度还是浮点数组的大小，对象系统内存的占用都不是很大，这是因为以前android设备的硬件配置都比较低，内存也比较少，而现在android设备的硬件配置普遍比较高，无论是CPU主频还是内存容量都有了很大的提高。导致的结果就是现在android设备运行这个benchmark测出来的垃圾回收性能的差异很难观察到。这也是本文想对此benchmark进行改进的原因，改进后使其能测试当前主流android设备的垃圾回收性能。
###3. 改进及测试结果分析
根据上节分析，准备根据真实硬件的配置相应修改二叉树的创建深度以及数组的大小，并在android设备上测试。选用的开发板是Tiny4412，是Samsung ARM Cortex-A9四核Exynos4412 Quad-core处理器，运行主频是1.5GHz，内存是DDR3 RAM，大小为1G。运行的android版本是5.0.2，由于android5.0中得ART GC不会把所有GC log都打印出来，为了更加准确的获知GC的运行情况，需要修改android源码，屏蔽过滤GC log的语句，重新编译并生成libart.so。

未经修改的0xBenchmark的垃圾回收性能测试结果如下：

![1](/public/img/technology/gc_6.png)

程序测试过程中内存的变化情况如下所示：

![2](/public/img/technology/gc_1.png)

测试过程中共发生15次GC：

![3](/public/img/technology/gc_3.png)

由上面的测试结果可以看出，由于原始的测试程序设置的二叉树深度和数组的大小都相对较小，所以测试所需求的内存也比较少，基本都在8M左右，完成整个垃圾回收测试所需要的时间只有847ms，其中GC只触发了15次。

为了使0xBenchmark能更好的适配现在主流的android硬件设备，能更加好的使测试结果能表征android虚拟机的垃圾回收性能，调整二叉树的创建深度及数组大小如下：

	public static final int kStretchTreeDepth    = 19;
    public static final int kLongLivedTreeDepth  = 18;
    public static final int kArraySize  = 125000*8;
    public static final int kMinTreeDepth = 2;
    public static final int kMaxTreeDepth = 12;
经过改进后的垃圾回收测试结果如下：

![4](/public/img/technology/gc_5.png)

测试过程中内存的变化情况如下所示：

![5](/public/img/technology/gc_2.png)

测试过程中共发生86次GC，部分截图如下：

![6](/public/img/technology/gc_4.png)

可以看到，完成整个测试所需的时间变为了9162ms，在测试过程中也发成了较多的GC，共86次，更加能够表征GC对于对象分配的影响，同时，测试过程中java堆的大小在32M左右，由android启动参数dalvik.vm.heapgrowthlimit=64m可知，每个app的堆得增长上限是64M，测试程序更好的模拟了正常应用的内存消耗行为。
