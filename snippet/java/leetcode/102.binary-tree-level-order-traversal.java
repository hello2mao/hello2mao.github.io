/*
 * @lc app=leetcode id=102 lang=java
 *
 * [102] Binary Tree Level Order Traversal
 *
 * https://leetcode.com/problems/binary-tree-level-order-traversal/description/
 *
 * algorithms
 * Medium (49.68%)
 * Likes:    1725
 * Dislikes: 44
 * Total Accepted:    422.3K
 * Total Submissions: 847.1K
 * Testcase Example:  '[3,9,20,null,null,15,7]'
 *
 * Given a binary tree, return the level order traversal of its nodes' values.
 * (ie, from left to right, level by level).
 * 
 * 
 * For example:
 * Given binary tree [3,9,20,null,null,15,7],
 * 
 * ⁠   3
 * ⁠  / \
 * ⁠ 9  20
 * ⁠   /  \
 * ⁠  15   7
 * 
 * 
 * 
 * return its level order traversal as:
 * 
 * [
 * ⁠ [3],
 * ⁠ [9,20],
 * ⁠ [15,7]
 * ]
 * 
 * 
 */
/**
 * Definition for a binary tree node. public class TreeNode { int val; TreeNode
 * left; TreeNode right; TreeNode(int x) { val = x; } }
 */
class Solution {

    public List<List<Integer>> levelOrder(TreeNode root) {
        return levelOrder_DFS(root);
    }

    // DFS(递归Recursion)
    // 先序遍历preorder的递归做法加上一个层
    // Time Complexity: O(n)
    // Space Complexity: O(n)
    public List<List<Integer>> levelOrder_DFS(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null)
            return res;
        preorderTraversal(root, 1, res);
        return res;
    }

    private void preorderTraversal(TreeNode root, int level, List<List<Integer>> res) {
        if (root == null)
            return;

        if (level > res.size())
            res.add(new ArrayList<>());

        // preorder
        res.get(level - 1).add(root.val); // ref object
        preorderTraversal(root.left, level + 1, res);
        preorderTraversal(root.right, level + 1, res);
    }

    // BFS(迭代Iteration)
    // Time Complexity: O(n)
    // Space Complexity: O(1)
    public List<List<Integer>> levelOrder_BFS(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null)
            return res;

        Queue<TreeNode> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int levelNum = q.size();
            List<Integer> subList = new ArrayList<>();
            for (int i = 0; i < levelNum; i++) {
                if (q.peek().left != null)
                    q.offer(q.peek().left);
                if (q.peek().right != null)
                    q.offer(q.peek().right);
                subList.add(q.poll().val);
            }
            res.add(subList);
        }
        return res;
    }
}
