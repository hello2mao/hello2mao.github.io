---
layout: post
title: Android ART GC之GrowForUtilization的分析
category: 技术
tags: Android
keywords: android,GC
description:
---
***版权声明：本文为博主原创文章，转载请注明来自https://hello2mao.github.io***


Android运行过程中有多种情况会触发垃圾回收（GC，garbage collection），以android 5.0为例，可以发现，在应用运行过程中最常见的触发GC的方式如下图所示：

![gc_trigger.png](/public/img/technology/gc_trigger.png)

此图是通过android studio截取的android应用运行过程中某应用内存占用情况的动态变化图，蓝色部分是应用占用的内存，灰色部分是当前空闲的内存。可以看到，在白色圈内的那点，当应用空闲的内存到达某阈值时，android系统认为当前内存不太够，所以系统唤醒GC线程来进行垃圾回收。通过logcat可以看到打印出如下log表示垃圾回收的效果。

![gc_logcat_log.png](/public/img/technology/gc_logcat_log.png)

在现在android 5.0 ART GC中，每次GC清扫玩垃圾之后，系统都会重新调整堆大小以控制堆的剩余内存使其满足预先设置好的堆利用率等限制条件（实际上，java堆在应用启动时就已经初始化并固定到内存地址空间中，这里调整堆大小的意思只是调整堆可用内存这个统计量，android这样做的目的是通过动态调整堆可用内存这个统计量，使堆中的对象分布更加紧凑，可以稍微消除由于采取标记清楚垃圾回收算法导致的堆内存碎片化）。如下图所示：

![gc_trigger_al.png](/public/img/technology/gc_trigger_al.png)

GC触发后，回收了应用不再使用的垃圾对象，这样可用内存就变大了，如上图右边collect garbage过程所示，但是android系统不会把这么大一块可用内存都给应用，它会根据系统预先设定的堆利用率等参数调整可用内存的大小，暂且把这块调节大小后的可用内存称为预留空闲内存，这一过程在代码里通过调用GrowForUtilization实现。当这块预留空闲内存被应用使用差不多的时候就会触发下次GC。通过后面的分析可以看出这个预留空闲内存的大小从某种意义上来说几乎就是一个定值。

下面我就详细分析下GrowForUtilization的实现。

GrowForUtilization在art/runtime/gc/heap.cc中实现。代码如下：

	void Heap::GrowForUtilization(collector::GarbageCollector* collector_ran) {
	  // We know what our utilization is at this moment.
	  // This doesn't actually resize any memory. It just lets the heap grow more when necessary.
	  const uint64_t bytes_allocated = GetBytesAllocated();
	  last_gc_size_ = bytes_allocated;
	  last_gc_time_ns_ = NanoTime();
	  uint64_t target_size;
	  collector::GcType gc_type = collector_ran->GetGcType();
      if (gc_type != collector::kGcTypeSticky) {
        // Grow the heap for non sticky GC.
        const float multiplier = HeapGrowthMultiplier();  // Use the multiplier to grow more for
        // foreground.
        intptr_t delta = bytes_allocated / GetTargetHeapUtilization() - bytes_allocated;
        CHECK_GE(delta, 0);
        target_size = bytes_allocated + delta * multiplier;
        target_size = std::min(target_size,
                             bytes_allocated + static_cast<uint64_t>(max_free_ * multiplier));
        target_size = std::max(target_size,
                             bytes_allocated + static_cast<uint64_t>(min_free_ * multiplier));
        native_need_to_run_finalization_ = true;
        next_gc_type_ = collector::kGcTypeSticky;
      } else {
        collector::GcType non_sticky_gc_type =
            have_zygote_space_ ? collector::kGcTypePartial : collector::kGcTypeFull;
        // Find what the next non sticky collector will be.
        collector::GarbageCollector* non_sticky_collector = FindCollectorByGcType(non_sticky_gc_type);
        // If the throughput of the current sticky GC >= throughput of the non sticky collector, then
        // do another sticky collection next.
        // We also check that the bytes allocated aren't over the footprint limit in order to prevent a
        // pathological case where dead objects which aren't reclaimed by sticky could get accumulated
        // if the sticky GC throughput always remained >= the full/partial throughput.
        if (current_gc_iteration_.GetEstimatedThroughput() * kStickyGcThroughputAdjustment >=
            non_sticky_collector->GetEstimatedMeanThroughput() &&
            non_sticky_collector->NumberOfIterations() > 0 &&
            bytes_allocated <= max_allowed_footprint_) {
          next_gc_type_ = collector::kGcTypeSticky;
        } else {
          next_gc_type_ = non_sticky_gc_type;
        }
        // If we have freed enough memory, shrink the heap back down.
        if (bytes_allocated + max_free_ < max_allowed_footprint_) {
          target_size = bytes_allocated + max_free_;
        } else {
          target_size = std::max(bytes_allocated, static_cast<uint64_t>(max_allowed_footprint_));
        }
      }
      if (!ignore_max_footprint_) {
        SetIdealFootprint(target_size);
        if (IsGcConcurrent()) {
          // Calculate when to perform the next ConcurrentGC.
          // Calculate the estimated GC duration.
          const double gc_duration_seconds = NsToMs(current_gc_iteration_.GetDurationNs()) / 1000.0;
          // Estimate how many remaining bytes we will have when we need to start the next GC.
          size_t remaining_bytes = allocation_rate_ * gc_duration_seconds;
          remaining_bytes = std::min(remaining_bytes, kMaxConcurrentRemainingBytes);
          remaining_bytes = std::max(remaining_bytes, kMinConcurrentRemainingBytes);
          if (UNLIKELY(remaining_bytes > max_allowed_footprint_)) {
            // A never going to happen situation that from the estimated allocation rate we will exceed
            // the applications entire footprint with the given estimated allocation rate. Schedule
            // another GC nearly straight away.
            remaining_bytes = kMinConcurrentRemainingBytes;
          }
          DCHECK_LE(remaining_bytes, max_allowed_footprint_);
          DCHECK_LE(max_allowed_footprint_, GetMaxMemory());
          // Start a concurrent GC when we get close to the estimated remaining bytes. When the
          // allocation rate is very high, remaining_bytes could tell us that we should start a GC
          // right away.
          concurrent_start_bytes_ = std::max(max_allowed_footprint_ - remaining_bytes,
                                           static_cast<size_t>(bytes_allocated));
        }
      }
    }

代码根据GC的种类来设置target_size，而target-size - bytes-allocated就是上面所说的预留空闲内存。设max-free- = a，min-free- = b，utilization = c，multiplier = d，bytes-allocated = x，target-size = T。

当GC是不是sticky gc时，  
delta = x/c - x  
t1 = x + d * delta = x + d(1-c)x/c  
t2 = min(t1,x + da)  
T = max(t2,x + db)  
预留空闲内存 = T - x  
而当GC是sticky gc时，  
预留空闲内存 = a  
假如开发版的配置（build.prop中配置）：max-free- = 8M，min-free- = 4M，utilization = 0.75，multiplier = 2 。  

	dalvik.vm.heaptargetutilization=0.75
	dalvik.vm.heapminfree=4m
	dalvik.vm.heapmaxfree=8m

可以得到预留空闲内存与已使用内存的关系如下图所示：

![gc_graph.png](/public/img/technology/gc_graph.png)

可以看出，当GC为sticky gc的时候，预留空闲内存就是一个定值为max-free-。而当GC为非sticky gc
的时候，预留空闲内存的大小与应用已使用的内存有关，以本开发版为例，当应用的占用内存超过24M后，预留空闲内存也成为了一个定值16M，而对于用户经常使用的应用，很容易超过24M。超过24M之后，每次GC后，预留空闲内存就为16M，当16M用光，就会触发下一个GC。

所以当应用在短时间内分配了很多对象的话，8/16M内存会很快用光，这样的话GC的数量会非常的多，如下图所示：

![gc_sohu.png](/public/img/technology/gc_sohu.png)

从代码里也可以看出，如果本次GC不是sticky gc，那么下次gc就一定是sticky gc。而如果本次gc是sticky gc，那么会根据gc的吞吐量来决定下次是sticky gc还是partial gc或者full gc。
