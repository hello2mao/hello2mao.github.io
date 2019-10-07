/*
 * @lc app=leetcode id=58 lang=java
 *
 * [58] Length of Last Word
 *
 * https://leetcode.com/problems/length-of-last-word/description/
 *
 * algorithms
 * Easy (32.28%)
 * Likes:    423
 * Dislikes: 1765
 * Total Accepted:    291.8K
 * Total Submissions: 903.8K
 * Testcase Example:  '"Hello World"'
 *
 * Given a string s consists of upper/lower-case alphabets and empty space
 * characters ' ', return the length of last word in the string.
 * 
 * If the last word does not exist, return 0.
 * 
 * Note: A word is defined as a character sequence consists of non-space
 * characters only.
 * 
 * Example:
 * 
 * 
 * Input: "Hello World"
 * Output: 5
 * 
 * 
 * 
 * 
 */
class Solution {

    public int lengthOfLastWord(String s) {
        return lengthOfLastWord_1(s);
    }

    public int lengthOfLastWord_1(String s) {
        return s.trim().length() - s.trim().lastIndexOf(" ") - 1;
    }

    public int lengthOfLastWord_2(String s) {
        int res = 0, tmp = 0;
        if (s == null || s.length() == 0) {
            return 0;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                tmp++;
            } else if (tmp != 0) {
                res = tmp;
                tmp = 0;
            }
            if (i == s.length() - 1 && s.charAt(i) != ' ') {
                return tmp;
            }
            if (i == s.length() - 1 && s.charAt(i) == ' ') {
                return res;
            }
        }
        return res;
    }
}
