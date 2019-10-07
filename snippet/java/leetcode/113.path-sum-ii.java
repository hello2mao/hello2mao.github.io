/*
 * @lc app=leetcode id=113 lang=java
 *
 * [113] Path Sum II
 *
 * https://leetcode.com/problems/path-sum-ii/description/
 *
 * algorithms
 * Medium (41.77%)
 * Likes:    1050
 * Dislikes: 37
 * Total Accepted:    253.6K
 * Total Submissions: 603.3K
 * Testcase Example:  '[5,4,8,11,null,13,4,7,2,null,null,5,1]\n22'
 *
 * Given a binary tree and a sum, find all root-to-leaf paths where each path's
 * sum equals the given sum.
 * 
 * Note: A leaf is a node with no children.
 * 
 * Example:
 * 
 * Given the below binary tree and sum = 22,
 * 
 * 
 * ⁠     5
 * ⁠    / \
 * ⁠   4   8
 * ⁠  /   / \
 * ⁠ 11  13  4
 * ⁠/  \    / \
 * 7    2  5   1
 * 
 * 
 * Return:
 * 
 * 
 * [
 * ⁠  [5,4,11,2],
 * ⁠  [5,8,4,5]
 * ]
 * 
 * 
 */
/**
 * Definition for a binary tree node. public class TreeNode { int val; TreeNode
 * left; TreeNode right; TreeNode(int x) { val = x; } }
 */
class Solution {
    public List<List<Integer>> pathSum(TreeNode root, int sum) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        if (root.left == null && root.right == null) {
            if (root.val == sum) {
                res.add(Arrays.asList(root.val));
            }
            return res;
        }
        for (List<Integer> item : pathSum(root.left, sum - root.val)) {
            ArrayList<Integer> newItem = new ArrayList<Integer>(item);
            newItem.add(0, root.val);
            res.add(newItem);
        }
        for (List<Integer> item : pathSum(root.right, sum - root.val)) {
            ArrayList<Integer> newItem = new ArrayList<Integer>(item);
            newItem.add(0, root.val);
            res.add(newItem);
        }
        return res;

    }
}
