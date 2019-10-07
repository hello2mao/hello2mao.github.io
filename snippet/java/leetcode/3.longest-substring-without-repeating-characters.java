/*
 * @lc app=leetcode id=3 lang=java
 *
 * [3] Longest Substring Without Repeating Characters
 *
 * https://leetcode.com/problems/longest-substring-without-repeating-characters/description/
 *
 * algorithms
 * Medium (28.72%)
 * Likes:    6077
 * Dislikes: 349
 * Total Accepted:    1M
 * Total Submissions: 3.6M
 * Testcase Example:  '"abcabcbb"'
 *
 * Given a string, find the length of the longest substring without repeating
 * characters.
 * 
 * 
 * Example 1:
 * 
 * 
 * Input: "abcabcbb"
 * Output: 3 
 * Explanation: The answer is "abc", with the length of 3. 
 * 
 * 
 * 
 * Example 2:
 * 
 * 
 * Input: "bbbbb"
 * Output: 1
 * Explanation: The answer is "b", with the length of 1.
 * 
 * 
 * 
 * Example 3:
 * 
 * 
 * Input: "pwwkew"
 * Output: 3
 * Explanation: The answer is "wke", with the length of 3. 
 * ⁠            Note that the answer must be a substring, "pwke" is a
 * subsequence and not a substring.
 * 
 * 
 * 
 * 
 * 
 */
class Solution {

    // 滑动窗口
    // 参考：https://www.jianshu.com/p/8739bed84efa
    public int lengthOfLongestSubstring(String s) {
        int[] pos = new int[256];
        for (int i = 0; i < pos.length; i ++) {
            pos[i] = -1;
        }
        int res = 0, left = 0;
        for (int i = 0; i < s.length(); i ++) {
            left = Math.max(left, pos[s.charAt(i)] + 1);
            res = Math.max(res, i - left + 1);
            pos[s.charAt(i)] = i;
        }
        return res;
    }
}

