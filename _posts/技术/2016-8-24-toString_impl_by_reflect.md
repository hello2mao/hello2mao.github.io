---
layout: post
title: Android ART GC -- AtomicStack 无锁原子栈的实现分析
category: 技术
tags: Android
keywords: android,GC
description:
---

***版权声明：本文为博主原创文章，转载请注明来自 https://hello2mao.github.io***

---

在android ART GC 中类AtomicStack用来实现对象的栈，如下：
```
typedef AtomicStack<mirror::Object*> ObjectStack;
```
ObjectStack用来实现android ART GC中三个最重要的栈，即mark_stack_,allocation_stack_,live_stack，对于这三个栈的详细分析见 ：
```
Android ART GC --mark_stack_,allocation_stack_,live_stack_
```
在模板类AtomicStack中实现了栈的一些基本操作，包括入栈、出栈等。同时，也利用C++11的原子操作实现了无锁栈（lock-free stack），提高了栈的性能。

模板类AtomicStack的定义如下（art/runtime/gc/accounting/atomic_stack.h）：
```
template <typename T>
class AtomicStack {
public:
 static AtomicStack* Create(const std::string& name, size_t growth_limit, size_t capacity);
 ~AtomicStack() {}
 void Reset();
 bool AtomicPushBackIgnoreGrowthLimit(const T& value);
 bool AtomicPushBack(const T& value);
 bool AtomicBumpBack(size_t num_slots, T** start_address, T** end_address);
 void AssertAllZero();
 void PushBack(const T& value);
 T PopBack();
 T PopFront();
 void PopBackCount(int32_t n);
 bool IsEmpty();
 size_t Size();
 T* Begin();
 T* End();
 size_t Capacity();
 void Resize(size_t new_capacity);
 void Sort();
 bool ContainsSorted(const T& value);
 bool Contains(const T& value);
private:
 AtomicStack(const std::string& name, size_t growth_limit, size_t capacity)
		 : name_(name),
			 back_index_(0),
			 front_index_(0),
			 begin_(nullptr),
			 growth_limit_(growth_limit),
			 capacity_(capacity),
			 debug_is_sorted_(true) {
 }
 bool AtomicPushBackInternal(const T& value, size_t limit);
 void Init();
 std::string name_;
 std::unique_ptr<MemMap> mem_map_;
 AtomicInteger back_index_;
 AtomicInteger front_index_;
 T* begin_;
 size_t growth_limit_;
 size_t capacity_;
 bool debug_is_sorted_;
};
```
			 可以看到模板类AtomicStack实现了栈的一些操作，并且是基于std::atomic的原子操作，这是C++11的新标准。所谓的原子操作，就是确保了在同一时刻只有唯一的线程对这个资源进行访问。这有点类似互斥对象对共享资源的访问的保护，但是原子操作更加接近底层，因而效率更高。

============================================================================

			 name_是栈的名字，mem_map_是栈的内存映射的一个unique_ptr智能指针，back_index_和front_index_是原子操作的栈顶（最后入栈对象的下一个空间）和栈底的索引，类型为AtomicInteger，begin_是这个原子栈的基址，growth_limit_是当前栈的最大可允许增加对象数，capacity_是栈的最大可容纳对象数，debug_is_sorted_用来在调试时对栈是否排序进行设定。

其中AtomicInteger如下：
```
typedef Atomic<int32_t> AtomicInteger;
```

下面来看下模板类AtomicStack的公有成员函数的实现，其中包括如何实现栈的一些基本操作。

1.

```
 static AtomicStack* Create(const std::string& name, size_t growth_limit, size_t capacity) {
	 std::unique_ptr<AtomicStack> mark_stack(new AtomicStack(name, growth_limit, capacity));
	 mark_stack->Init();
	 return mark_stack.release();
 }
 ```
Create实现了创建一个标记栈的方法，标记栈在gc的mark阶段用来加速递归标记。
2.

```
 void Reset() {
	 DCHECK(mem_map_.get() != nullptr);
	 DCHECK(begin_ != NULL);
	 front_index_.StoreRelaxed(0);
	 back_index_.StoreRelaxed(0);
	 debug_is_sorted_ = true;
	 mem_map_->MadviseDontNeedAndZero();
 }
 ```
Reset把栈复位，包括栈顶、栈底索引的置0，栈所映射的内存的置0。
其中
```
front_index_.StoreRelaxed(0);
```
用到了模板类Atomic中的方法。
模板类Atomic在art/runtime/atomic.h中定义：

```
template<typename T>
class PACKED(sizeof(T)) Atomic : public std::atomic<T> {..}
```
所以模板类是继承了atomic的。

而StoreRelaxed()如下：
```
void StoreRelaxed(T desired) {
 this->store(desired, std::memory_order_relaxed);
}
```
调用std::atomic::store()进行存储值的替换.

3.

原子操作的入栈
```
 bool AtomicPushBackIgnoreGrowthLimit(const T& value) {
	 return AtomicPushBackInternal(value, capacity_);
 }
 bool AtomicPushBack(const T& value) {
	 return AtomicPushBackInternal(value, growth_limit_);
 }
```
调用类的私有成员函数AtomicPushBackInternal()进行入栈操作。
```
 bool AtomicPushBackInternal(const T& value, size_t limit) ALWAYS_INLINE {
	 if (kIsDebugBuild) {
		 debug_is_sorted_ = false;
	 }
	 int32_t index;
	 do {
		 index = back_index_.LoadRelaxed();
		 if (UNLIKELY(static_cast<size_t>(index) >= limit)) {
			 // Stack overflow.
			 return false;
		 }
	 } while (!back_index_.CompareExchangeWeakRelaxed(index, index + 1));
	 begin_[index] = value;
	 return true;
 }
```
在runtime/base/macros.h中定义了ALWAYS_INLINE，表示这是一个内联函数：
```
#define ALWAYS_INLINE  __attribute__ ((always_inline))
```
此私有成员函数涉及两个关于模板类Atomic的操作：
```
 T LoadRelaxed() const {
	 return this->load(std::memory_order_relaxed);
 }
std::atomic::load用来装载存储值。
 bool CompareExchangeWeakRelaxed(T expected_value, T desired_value) {
	 return this->compare_exchange_weak(expected_value, desired_value, std::memory_order_relaxed);
 }
std::atomic::compare_exchange_weak,
可以参考http://blog.csdn.net/anzhsoft/article/details/19125619对CAS原子操作的讲解。
```
关键在于理解
```
back_index_.CompareExchangeWeakRelaxed(index, index + 1)
```
（1）如果back_index_存储的值等于index，那么back_index_存储的值就等于index+1，并且返回true，也就跳出while循环，然后用begin_[index]=value完成入栈操作，此时back_index_也已经更新为index+1了。

（2）否则，index就等于back_index_存储的值（在此处似乎没什么用？因为do while中又对index进行了更新），返回false，说明有其他线程对栈进行了修改，然后更新index，继续循环，直到原子入栈操作完成。

4.
```
 // Atomically bump the back index by the given number of
 // slots. Returns false if we overflowed the stack.
 bool AtomicBumpBack(size_t num_slots, T** start_address, T** end_address) {
	 if (kIsDebugBuild) {
		 debug_is_sorted_ = false;
	 }
	 int32_t index;
	 int32_t new_index;
	 do {
		 index = back_index_.LoadRelaxed();
		 new_index = index + num_slots;
		 if (UNLIKELY(static_cast<size_t>(new_index) >= growth_limit_)) {
			 // Stack overflow.
			 return false;
		 }
	 } while (!back_index_.CompareExchangeWeakRelaxed(index, new_index));
	 *start_address = &begin_[index];
	 *end_address = &begin_[new_index];
	 if (kIsDebugBuild) {
		 // Sanity check that the memory is zero.
		 for (int32_t i = index; i < new_index; ++i) {
			 DCHECK_EQ(begin_[i], static_cast<T>(0))
					 << "i=" << i << " index=" << index << " new_index=" << new_index;
		 }
	 }
	 return true;
 }
```
与AtomicPushBackInternal()相似，只是返回栈中指定入栈对象个数时的起始地址和结束地址。
5.

非原子操作的入栈、出栈，很好理解
```
 void PushBack(const T& value) {
	 if (kIsDebugBuild) {
		 debug_is_sorted_ = false;
	 }
	 int32_t index = back_index_.LoadRelaxed();
	 DCHECK_LT(static_cast<size_t>(index), growth_limit_);
	 back_index_.StoreRelaxed(index + 1);
	 begin_[index] = value;
 }

 T PopBack() {
	 DCHECK_GT(back_index_.LoadRelaxed(), front_index_.LoadRelaxed());
	 // Decrement the back index non atomically.
	 back_index_.StoreRelaxed(back_index_.LoadRelaxed() - 1);
	 return begin_[back_index_.LoadRelaxed()];
 }

 // Take an item from the front of the stack.
 T PopFront() {
	 int32_t index = front_index_.LoadRelaxed();
	 DCHECK_LT(index, back_index_.LoadRelaxed());
	 front_index_.StoreRelaxed(index + 1);
	 return begin_[index];
 }
```
6.
弹出指定个数的对象，只更新栈顶
```
 // Pop a number of elements.
 void PopBackCount(int32_t n) {
	 DCHECK_GE(Size(), static_cast<size_t>(n));
	 back_index_.FetchAndSubSequentiallyConsistent(n);
 }


 T FetchAndSubSequentiallyConsistent(const T value) {
	 return this->fetch_sub(value, std::memory_order_seq_cst);  // Return old value.
 }
```
调用std::atomic::fetch_sub使back_index减n。
7.
一些栈的基本信息的输出
```
 bool IsEmpty() const {
	 return Size() == 0;
 }

 size_t Size() const {
	 DCHECK_LE(front_index_.LoadRelaxed(), back_index_.LoadRelaxed());
	 return back_index_.LoadRelaxed() - front_index_.LoadRelaxed();
 }

 T* Begin() const {
	 return const_cast<T*>(begin_ + front_index_.LoadRelaxed());
 }

 T* End() const {
	 return const_cast<T*>(begin_ + back_index_.LoadRelaxed());
 }

 size_t Capacity() const {
	 return capacity_;
 }

 // Will clear the stack.
 void Resize(size_t new_capacity) {
	 capacity_ = new_capacity;
	 growth_limit_ = new_capacity;
	 Init();
 }
```
8.
调用std::sort进行快速排序，性能可能比较差。
```
 void Sort() {
	 int32_t start_back_index = back_index_.LoadRelaxed();
	 int32_t start_front_index = front_index_.LoadRelaxed();
	 std::sort(Begin(), End());
	 CHECK_EQ(start_back_index, back_index_.LoadRelaxed());
	 CHECK_EQ(start_front_index, front_index_.LoadRelaxed());
	 if (kIsDebugBuild) {
		 debug_is_sorted_ = true;
	 }
 }
```
9.
搜索
```
 bool ContainsSorted(const T& value) const {
	 DCHECK(debug_is_sorted_);
	 return std::binary_search(Begin(), End(), value);
 }
```
10.
查找
```
 bool Contains(const T& value) const {
	 return std::find(Begin(), End(), value) != End();
 }
```
11.
初始化
```
 void Init() {
	 std::string error_msg;
	 mem_map_.reset(MemMap::MapAnonymous(name_.c_str(), NULL, capacity_ * sizeof(T),
																			 PROT_READ | PROT_WRITE, false, &error_msg));
	 CHECK(mem_map_.get() != NULL) << "couldn't allocate mark stack.\n" << error_msg;
	 uint8_t* addr = mem_map_->Begin();
	 CHECK(addr != NULL);
	 debug_is_sorted_ = true;
	 begin_ = reinterpret_cast<T*>(addr);
	 Reset();
 }
```


参考：

【1】http://blog.csdn.net/anzhsoft/article/details/19125619 并发编程（三）： 使用C++11实现无锁stack（lock-free stack)
