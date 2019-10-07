/*
 * @lc app=leetcode id=124 lang=java
 *
 * [124] Binary Tree Maximum Path Sum
 *
 * https://leetcode.com/problems/binary-tree-maximum-path-sum/description/
 *
 * algorithms
 * Hard (30.66%)
 * Likes:    1951
 * Dislikes: 145
 * Total Accepted:    219.4K
 * Total Submissions: 711.3K
 * Testcase Example:  '[1,2,3]'
 *
 * Given a non-empty binary tree, find the maximum path sum.
 * 
 * For this problem, a path is defined as any sequence of nodes from some
 * starting node to any node in the tree along the parent-child connections.
 * The path must contain at least one node and does not need to go through the
 * root.
 * 
 * Example 1:
 * 
 * 
 * Input: [1,2,3]
 * 
 * ⁠      1
 * ⁠     / \
 * ⁠    2   3
 * 
 * Output: 6
 * 
 * 
 * Example 2:
 * 
 * 
 * Input: [-10,9,20,null,null,15,7]
 * 
 * -10
 * / \
 * 9  20
 * /  \
 * 15   7
 * 
 * Output: 42
 * 
 * 
 */
/**
 * Definition for a binary tree node. public class TreeNode { int val; TreeNode
 * left; TreeNode right; TreeNode(int x) { val = x; } }
 */
class Solution {
    public int maxPathSum(TreeNode root) {
        return maxPathSum_1(root);
    }

    int max = Integer.MIN_VALUE;

    public int maxPathSum_1(TreeNode root) {
        helper(root);
        return max;
    }

    // helper returns the max branch
    // plus current node's value
    private int helper(TreeNode root) {
        if (root == null)
            return 0;
        int leftMax = Math.max(0, helper(root.left));
        int rightMax = Math.max(0, helper(root.right));
        max = Math.max(max, root.val + leftMax + rightMax);
        return root.val + Math.max(leftMax, rightMax);
    }

    public int maxPathSum_2(TreeNode root) {
        Integer res = 0;
        return traversal(root);
    }

    private int traversal(TreeNode root) {
        if (root == null)
            return Integer.MIN_VALUE;
        int res = Math.max(maxSinglePathSumToRoot(root.left), 0) + Math.max(maxSinglePathSumToRoot(root.right), 0)
                + root.val;
        int leftMax = traversal(root.left);
        int rightMax = traversal(root.right);
        return Math.max(res, Math.max(leftMax, rightMax));
    }

    private int maxSinglePathSumToRoot(TreeNode root) {
        if (root == null)
            return Integer.MIN_VALUE;
        if (root.left == null && root.right == null)
            return root.val;
        return root.val + Math.max(0, Math.max(maxSinglePathSumToRoot(root.left), maxSinglePathSumToRoot(root.right)));
    }
}
