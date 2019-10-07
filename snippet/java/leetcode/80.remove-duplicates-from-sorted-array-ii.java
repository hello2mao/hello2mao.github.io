/*
 * @lc app=leetcode id=80 lang=java
 *
 * [80] Remove Duplicates from Sorted Array II
 *
 * https://leetcode.com/problems/remove-duplicates-from-sorted-array-ii/description/
 *
 * algorithms
 * Medium (41.09%)
 * Likes:    737
 * Dislikes: 570
 * Total Accepted:    214.9K
 * Total Submissions: 523.1K
 * Testcase Example:  '[1,1,1,2,2,3]'
 *
 * Given a sorted array nums, remove the duplicates in-place such that
 * duplicates appeared at most twice and return the new length.
 * 
 * Do not allocate extra space for another array, you must do this by modifying
 * the input array in-place with O(1) extra memory.
 * 
 * Example 1:
 * 
 * 
 * Given nums = [1,1,1,2,2,3],
 * 
 * Your function should return length = 5, with the first five elements of nums
 * being 1, 1, 2, 2 and 3 respectively.
 * 
 * It doesn't matter what you leave beyond the returned length.
 * 
 * Example 2:
 * 
 * 
 * Given nums = [0,0,1,1,1,1,2,3,3],
 * 
 * Your function should return length = 7, with the first seven elements of
 * nums being modified to 0, 0, 1, 1, 2, 3 and 3 respectively.
 * 
 * It doesn't matter what values are set beyond the returned length.
 * 
 * 
 * Clarification:
 * 
 * Confused why the returned value is an integer but your answer is an array?
 * 
 * Note that the input array is passed in by reference, which means
 * modification to the input array will be known to the caller as well.
 * 
 * Internally you can think of this:
 * 
 * 
 * // nums is passed in by reference. (i.e., without making a copy)
 * int len = removeDuplicates(nums);
 * 
 * // any modification to nums in your function would be known by the caller.
 * // using the length returned by your function, it prints the first len
 * elements.
 * for (int i = 0; i < len; i++) {
 * print(nums[i]);
 * }
 * 
 * 
 */
class Solution {
    // Time Complexity: O(n)
    // Space Complexity: O(1)
    public int removeDuplicates(int[] nums) {
        int l = 0;
        for (int num : nums) {
            if (l < 2 || num > nums[l - 2]) {
                nums[l++] = num;
            }
        }
        return l;
    }

    // Time Complexity: O(n)
    // Space Complexity: O(n)
    // HashMap
    // ✔ Accepted
    // ✔ 166/166 cases passed (3 ms)
    // ✔ Your runtime beats 5.84 % of java submissions
    // ✔ Your memory usage beats 89.47 % of java submissions (38.1 MB)
    public int removeDuplicates_2(int[] nums) {
        Map<Integer, Integer> hashMap = new HashMap<>();

        int l = 0;
        for (int i = 0; i < nums.length; i++) {
            if (hashMap.containsKey(nums[i])) {
                int count = hashMap.get(nums[i]);
                if (count < 2) {
                    hashMap.put(nums[i], hashMap.get(nums[i]) + 1);
                    nums[l++] = nums[i];
                }
            } else {
                hashMap.put(nums[i], 1);
                nums[l++] = nums[i];
            }
        }
        return l;
    }
}
