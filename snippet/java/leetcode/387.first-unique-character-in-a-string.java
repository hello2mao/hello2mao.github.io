/*
 * @lc app=leetcode id=387 lang=java
 *
 * [387] First Unique Character in a String
 *
 * https://leetcode.com/problems/first-unique-character-in-a-string/description/
 *
 * algorithms
 * Easy (50.50%)
 * Likes:    1158
 * Dislikes: 84
 * Total Accepted:    308.4K
 * Total Submissions: 610.1K
 * Testcase Example:  '"leetcode"'
 *
 * 
 * Given a string, find the first non-repeating character in it and return it's
 * index. If it doesn't exist, return -1.
 * 
 * Examples:
 * 
 * s = "leetcode"
 * return 0.
 * 
 * s = "loveleetcode",
 * return 2.
 * 
 * 
 * 
 * 
 * Note: You may assume the string contain only lowercase letters.
 * 
 */
class Solution {
    public int firstUniqChar(String s) {
        int[] tmp = new int[256];
        for (int i = 0; i < s.length(); i++) {
            tmp[Character.getNumericValue(s.charAt(i))] += 1;
        }
        for (int i = 0; i < s.length(); i++) {
            if (tmp[Character.getNumericValue(s.charAt(i))] == 1) {
                return i;
            }
        }
        return -1;

    }
}
