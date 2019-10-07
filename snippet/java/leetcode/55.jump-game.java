/*
 * @lc app=leetcode id=55 lang=java
 *
 * [55] Jump Game
 *
 * https://leetcode.com/problems/jump-game/description/
 *
 * algorithms
 * Medium (32.44%)
 * Likes:    2282
 * Dislikes: 222
 * Total Accepted:    296.6K
 * Total Submissions: 913.2K
 * Testcase Example:  '[2,3,1,1,4]'
 *
 * Given an array of non-negative integers, you are initially positioned at the
 * first index of the array.
 * 
 * Each element in the array represents your maximum jump length at that
 * position.
 * 
 * Determine if you are able to reach the last index.
 * 
 * Example 1:
 * 
 * 
 * Input: [2,3,1,1,4]
 * Output: true
 * Explanation: Jump 1 step from index 0 to 1, then 3 steps to the last
 * index.
 * 
 * 
 * Example 2:
 * 
 * 
 * Input: [3,2,1,0,4]
 * Output: false
 * Explanation: You will always arrive at index 3 no matter what. Its
 * maximum
 * jump length is 0, which makes it impossible to reach the last index.
 * 
 * 
 */
class Solution {

    // Time Complexity: O(n)
    // Space Complexity: O(1)
    public boolean canJump(int[] nums) {
        int maxReachIndex = 0;
        for (int i = 0; i < nums.length; i++) {
            if (maxReachIndex < i) {
                return false;
            }
            maxReachIndex = Math.max(i + nums[i], maxReachIndex);
        }
        return true;

    }
}
