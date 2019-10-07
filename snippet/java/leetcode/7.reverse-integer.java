/*
 * @lc app=leetcode id=7 lang=java
 *
 * [7] Reverse Integer
 *
 * https://leetcode.com/problems/reverse-integer/description/
 *
 * algorithms
 * Easy (25.44%)
 * Likes:    2364
 * Dislikes: 3626
 * Total Accepted:    769.1K
 * Total Submissions: 3M
 * Testcase Example:  '123'
 *
 * Given a 32-bit signed integer, reverse digits of an integer.
 * 
 * Example 1:
 * 
 * 
 * Input: 123
 * Output: 321
 * 
 * 
 * Example 2:
 * 
 * 
 * Input: -123
 * Output: -321
 * 
 * 
 * Example 3:
 * 
 * 
 * Input: 120
 * Output: 21
 * 
 * 
 * Note:
 * Assume we are dealing with an environment which could only store integers
 * within the 32-bit signed integer range: [−2^31,  2^31 − 1]. For the purpose
 * of this problem, assume that your function returns 0 when the reversed
 * integer overflows.
 * 
 */
class Solution {
    // 时间复杂度为O(log10(x))
    // 空间复杂度O(1)
    public int reverse(int x) {
        long r = 0;
        while (x != 0) {
            r = r * 10 + x %10;
            x = x / 10;
        }
        if (r >= Integer.MIN_VALUE && r <= Integer.MAX_VALUE) {
            return (int) r;
        } else {
            return 0;
        }
    }
}

