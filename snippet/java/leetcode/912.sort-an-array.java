/*
 * @lc app=leetcode id=912 lang=java
 *
 * [912] Sort an Array
 *
 * https://leetcode.com/problems/sort-an-array/description/
 *
 * algorithms
 * Medium (63.16%)
 * Likes:    108
 * Dislikes: 115
 * Total Accepted:    19.3K
 * Total Submissions: 30.6K
 * Testcase Example:  '[5,2,3,1]'
 *
 * Given an array of integers nums, sort the array in ascending order.
 * 
 * 
 * 
 * 
 * 
 * 
 * Example 1:
 * 
 * 
 * Input: [5,2,3,1]
 * Output: [1,2,3,5]
 * 
 * 
 * Example 2:
 * 
 * 
 * Input: [5,1,1,2,0,0]
 * Output: [0,0,1,1,2,5]
 * 
 * 
 * 
 * 
 * Note:
 * 
 * 
 * 1 <= A.length <= 10000
 * -50000 <= A[i] <= 50000
 * 
 * 
 */
class Solution {

    public int[] sortArray(int[] nums) {
        return nums;
    }

    // 快排 => Arrays.sort()
    // Time complexity : O(nlogn)
    // Space complexity : O(nlogn)
    // 思想：分治策略。
    // 快速排序的原理：通过一趟排序将要排序的数据分割成独立的两部分，其中一部分的所有数据都比另外一部分的所有数据都要小，
    // 然后再按此方法对这两部分数据分别进行快速排序。
    // 保证列表的前半部分都小于后半部分"就使得前半部分的任何一个数从此以后都不再跟后半部分的数进行比较了，大大减少了数字间的比较次数。
    // 填坑法，参考：https://blog.csdn.net/nrsc272420199/article/details/82587933
    public int[] sortArray_quickSort(int[] nums) {
        return quickSort(nums, 0, nums.length - 1);
    }

    private int[] quickSort(int[] nums, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(nums, low, high);
            nums = quickSort(nums, low, pivotIndex - 1);
            nums = quickSort(nums, pivotIndex + 1, high);
        }
        return nums;
    }

    private int partition(int[] nums, int low, int high) {
        int pivot = nums[low]; // 挖坑
        while (low < high) {
            while (nums[high] >= pivot && low < high) {
                high--;
            }
            nums[low] = nums[high]; // 坑位=nums[high]
            while (nums[low] <= pivot && low < high) {
                low++;
            }
            nums[high] = nums[low]; // 坑位=nums[low]
        }
        nums[low] = pivot; // 填坑
        return low;
    }

    // 归并排序
    // Time complexity : O(nlogn)
    // Space complexity : O(n)
    public int[] sortArray_mergeSort(int[] nums) {
        return nums;
    }

    // 堆排序
    // Time complexity : O(nlogn)
    // Space complexity : O(1)
    public int[] sortArray_heapSort(int[] nums) {
        return nums;
    }

    // 计数排序
    // Time complexity : O(n+k)
    // Space complexity : O(n+k)
    public int[] sortArray_countingSort(int[] nums) {
        return nums;
    }

    // 桶排序
    // Time complexity : O(n+k)
    // Space complexity : O(n+k)
    public int[] sortArray_bucketSort(int[] nums) {
        return nums;
    }

    // 基数排序
    // Time complexity : O(n+k)
    // Space complexity : O(n+k)
    public int[] sortArray_radixSort(int[] nums) {
        return nums;
    }

}
