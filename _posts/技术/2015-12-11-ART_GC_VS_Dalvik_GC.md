---
layout: post
title: Android 5.0 ART GC 对比 Android 4.x Dalvik GC
category: 技术
tags: Android
keywords: android,GC
description: 
---
***版权声明：本文为博主原创文章，未经博主允许不得转载。***


为了研究Android虚拟机中的内存管理机制，前期进行了初步调研，下面列出Android 5.0 ART 中GC的更新概要以供参考，资料来源于网络以及对源码的初步阅读。  
        
谷歌在2014年6月26日的I/O 2014开发者大会上正式推出了Android L,有以下几个方面值得重点关注：

1. 全新的UI/UE设计风格和框架Material Design以及和通知（Notification）栏有关的UI/UE变化
2. 能大幅改善系统运行速度的运行时库Android Runtime（简称ART）。
3. 致力于改善功耗的Project Volta。

其中第二点中得ART是Android Runtime的缩写，它是Google用于替代饱受诟病的Dalvik虚拟机的替代品。其实，ART早在Android KitKat（版本号为4.4）就已经推出，不过当时它还很不完善，所以被放到设置程序中的“开发者选项”里供一些供感兴趣的开发者使用。
        
ART究竟有什么神奇之处呢？根据相关资料，总结如下：  
（1）采用AOT（Ahead-Of-Time，预编译）编译技术，它能将Java字节码直接转换成目标机器的机器码。  
（2）更为高效和细粒度的垃圾回收机制（GC）。  

下面简短的介绍下android 5.0 ART GC 的改动：

（一）内存分配器  

1. Dalvik   
在Android 系统C 语言库bionic 中直接使用了dlmalloc这一个十分流行的开源内存分配器。

2. ART  
参考<http://blog.tek-life.com/understanding-ros-memory-allocator-in-art-virtual-machine/>
创建了一种名叫Runs-of-Slots-Allocator（RosAlloc）的分配器。这种分配器的特点是分配内存时，会采用更细粒度的锁控制。例如有不同的锁来保护不同的对象分配，或者当线程分配一些小尺寸对象时使用线程自己的堆栈，从而可完全不使用锁保护。同时这种分配器也更适合多线程的实现。  
据Google自己的数据，RosAlloc能达到最多10倍的速度提升.

（二）垃圾回收算法

1. Dalvik   
Dalvik的垃圾回收分为两个阶段。  
第一个阶段，Dalvik暂停所有的线程来分析堆的使用情况。  
第二个阶段，Dalvik暂停所有线程来清理堆。这就会导致应用在性能上的“卡顿”。  

2. ART  
ART改进后的垃圾回收算法只暂停线程一次。ART 能够做到这一点，是因为应用本身做了垃圾回收的一些工作。垃圾回收启动后，不再是两次暂停，而是一次暂停。在遍历阶段，应用不需要暂停，同时垃圾回收停时间也大大缩短，因为 Google使用了一种新技术（packard pre-cleaning），在暂停前就做了许多事情，减轻了暂停时的工作量。

（三）超大对象存储空间的支持  
ART还引入了一个特殊的超大对象存储空间(large object space，LOS)，这个空间与堆空间是分开的，不过仍然驻留在应用程序内存空间中。这一特殊的设计是为了让ART可以更好的管理较大的对象，比如位图对象(bitmaps)。  
堆空间碎片化严重时，较大的对象会带来一些问题。比如，在分配一个此类对象时，相比其他普通对象，会导致垃圾收集器启动的次数增加很多。有了这个超大对象存储空间的支持，垃圾收集器因堆空间分段而引发调用次数将会大大降低，这样垃圾收集器就能做更加合理的内存分配，从而降低运行时开销。

（四）Moving GC策略  
ART为了解决堆空间内存碎片化的问题，近期提出了“Moving GC”的方法。其目的是清理堆栈以减少内存碎片。由于这个工作会导致应用程序长时间中断，所以它必须等程序退到后台时才能开展。核心思想是，当应用程序运行在后台时，将程序的堆空间做段合并操作。

（五）GC调度策略的多样性  
经过比较Dalvik和ART的源码后，发现ART中GC调度策略发生了很大的变动。  
具体来说分为以下几个方面  
    
    （a）GC触发方式  
    （b）GC的种类  
    （c）垃圾回收算法的多样性  
  
（a）GC触发方式  
（1）Dalvik  
  GC触发方式主要有GC_FOR_MALLOC;GC_CONCURRENT;GC_EXPLICIT;GC_BEFORE_OOM这四种。  
（2）ART

	enum GcCause {  
	  // GC triggered by a failed allocation. Thread doing allocation is blocked waiting for GC before  
	  // retrying allocation.  
	  kGcCauseForAlloc,  
	  // A background GC trying to ensure there is free memory ahead of allocations.  
	  kGcCauseBackground,  
	  // An explicit System.gc() call.  
	  kGcCauseExplicit,  
	  // GC triggered for a native allocation.  
	  kGcCauseForNativeAlloc,  
	  // GC triggered for a collector transition.  
	  kGcCauseCollectorTransition,  
	  // Not a real GC cause, used when we disable moving GC (currently for 	GetPrimitiveArrayCritical).  
	  kGcCauseDisableMovingGc,  
	  // Not a real GC cause, used when we trim the heap.  
	  kGcCauseTrim,  
	  // GC triggered for background transition when both foreground and background 	collector are CMS.  
	  kGcCauseHomogeneousSpaceCompact, 
	};  
	
（b）GC的种类  
（1）Dalvik  
就一种GC(并发、非并发)  
（2）ART  
三种GC(并发、非并发)：快速GC策略Sticky GC；局部GC策略Partial GC；全局GC策略Full GC。


	enum GcType {  
	  // Placeholder for when no GC has been performed.  
	  kGcTypeNone,  
	  // Sticky mark bits GC that attempts to only free objects allocated since the last GC.  
	  kGcTypeSticky,  
	  // Partial GC that marks the application heap but not the Zygote.
	  kGcTypePartial,  
	  // Full GC that marks and frees in both the application and Zygote heap. 
	  kGcTypeFull,  
	  // Number of different GC types.  
	  kGcTypeMax, 
	};  
	
（c）垃圾回收算法的多样性  
（1）Dalvik  
两种：串行Mark-Sweep算法、并行Mark-Sweep算法  
（2）ART  

	enum CollectorType {  
	// No collector selected.  
	kCollectorTypeNone,  
	// Non concurrent mark-sweep.  
	kCollectorTypeMS,  
	// Concurrent mark-sweep.  
	kCollectorTypeCMS,  
	// Semi-space / mark-sweep hybrid, enables compaction.  
	kCollectorTypeSS,  
	// A generational variant of kCollectorTypeSS.  
	kCollectorTypeGSS,  
	// Mark compact colector.  
	kCollectorTypeMC,  
	// Heap trimming collector, doesn't do any actual collecting.  
	kCollectorTypeHeapTrim,  
	// A (mostly) concurrent copying collector.  
	kCollectorTypeCC,  
	// A homogeneous space compaction collector used in background transition  
	// when both foreground and background collector are CMS.  
	kCollectorTypeHomogeneousSpaceCompact,  
	};  
可以看到，除了标记清楚算法，基于半空间（semi-space）的拷贝算法也实现了，其中GSS（分代半空间拷贝算法）的实现具有很大的研究性。

上面只是初步的认识，很多地方得详细研究后才能确定。

参考：  
【1】<http://www.cnblogs.com/jinkeep/p/3818180.html>  【原创】【Android】揭秘 ART 细节 ---- Garbage collection  
【2】<https://www.infinum.co/the-capsized-eight/articles/art-vs-dalvik-introducing-the-new-android-runtime-in-kit-kat>  
【3】<http://www.lingcc.com/2014/07/16/12599/>  近距离端详Android ART运行时库  
【4】<http://blog.tek-life.com/understanding-garbage-collector-in-art-of-android/>   