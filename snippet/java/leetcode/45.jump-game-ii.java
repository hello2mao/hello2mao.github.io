/*
 * @lc app=leetcode id=45 lang=java
 *
 * [45] Jump Game II
 *
 * https://leetcode.com/problems/jump-game-ii/description/
 *
 * algorithms
 * Hard (28.60%)
 * Likes:    1377
 * Dislikes: 80
 * Total Accepted:    187.4K
 * Total Submissions: 653.9K
 * Testcase Example:  '[2,3,1,1,4]'
 *
 * Given an array of non-negative integers, you are initially positioned at the
 * first index of the array.
 * 
 * Each element in the array represents your maximum jump length at that
 * position.
 * 
 * Your goal is to reach the last index in the minimum number of jumps.
 * 
 * Example:
 * 
 * 
 * Input: [2,3,1,1,4]
 * Output: 2
 * Explanation: The minimum number of jumps to reach the last index is 2.
 * ⁠   Jump 1 step from index 0 to 1, then 3 steps to the last index.
 * 
 * Note:
 * 
 * You can assume that you can always reach the last index.
 * 
 */
class Solution {
    // 贪婪算法Greedy的思想
    // ref: https://www.cnblogs.com/lichen782/p/leetcode_Jump_Game_II.html
    public int jump(int[] nums) {
        int res = 0, lastMax = 0, curMax = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            curMax = Math.max(curMax, i + nums[i]);
            if (i == lastMax) {
                res++;
                lastMax = curMax;
                if (i == nums.length - 1)
                    break;
            }
        }
        return res;
    }
}
