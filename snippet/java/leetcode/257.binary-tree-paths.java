/*
 * @lc app=leetcode id=257 lang=java
 *
 * [257] Binary Tree Paths
 *
 * https://leetcode.com/problems/binary-tree-paths/description/
 *
 * algorithms
 * Easy (46.89%)
 * Likes:    1009
 * Dislikes: 77
 * Total Accepted:    244.5K
 * Total Submissions: 519.1K
 * Testcase Example:  '[1,2,3,null,5]'
 *
 * Given a binary tree, return all root-to-leaf paths.
 * 
 * Note: A leaf is a node with no children.
 * 
 * Example:
 * 
 * 
 * Input:
 * 
 * ⁠  1
 * ⁠/   \
 * 2     3
 * ⁠\
 * ⁠ 5
 * 
 * Output: ["1->2->5", "1->3"]
 * 
 * Explanation: All root-to-leaf paths are: 1->2->5, 1->3
 * 
 */
/**
 * Definition for a binary tree node. public class TreeNode { int val; TreeNode
 * left; TreeNode right; TreeNode(int x) { val = x; } }
 */
class Solution {
    public List<String> binaryTreePaths(TreeNode root) {
        List<String> res = new ArrayList<>();
        if (root == null)
            return res;
        if ((root.left == null) && (root.right == null)) {
            res.add(Integer.toString(root.val));
            return res;
        }
        for (String iterm : binaryTreePaths(root.left)) {
            res.add(Integer.toString(root.val) + "->" + iterm);
        }
        for (String iterm : binaryTreePaths(root.right)) {
            res.add(Integer.toString(root.val) + "->" + iterm);
        }
        return res;
    }
}
