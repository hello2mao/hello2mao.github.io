/*
 * @lc app=leetcode id=77 lang=java
 *
 * [77] Combinations
 *
 * https://leetcode.com/problems/combinations/description/
 *
 * algorithms
 * Medium (49.30%)
 * Likes:    953
 * Dislikes: 56
 * Total Accepted:    230.7K
 * Total Submissions: 460.8K
 * Testcase Example:  '4\n2'
 *
 * Given two integers n and k, return all possible combinations of k numbers
 * out of 1 ... n.
 * 
 * Example:
 * 
 * 
 * Input: n = 4, k = 2
 * Output:
 * [
 * ⁠ [2,4],
 * ⁠ [3,4],
 * ⁠ [2,3],
 * ⁠ [1,2],
 * ⁠ [1,3],
 * ⁠ [1,4],
 * ]
 * 
 * 
 */

// @lc code=start
class Solution {
    // DFS
    // (n-1, k-1) => (n, k) 
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> res = new ArrayList<>();
        helper(res, new ArrayList<Integer>(), 1, n, k);
        return res;
    }

    private helper(List<List<Integer>> combs, List<Integer> comb, int start, int n, int k) {
        // TODO
    }

    // DFS
    // (k-1) => (k)
    public List<List<Integer>> combine_2(int n, int k) {
        List<List<Integer>> res = new ArrayList<>();
        if (k == 1) {
            for (int i = 1; i < n + 1; i++) {
                List<Integer> tmp = new ArrayList<>();
                tmp.add(i);
                res.add(tmp);
            }
            return res;
        }

        List<List<Integer>> subRes = combine(n, k - 1);
        for (List<Integer> item : subRes) {
            if (item.get(k-2) == n) {
                continue;
            }
            for (int i = item.get(k-2) + 1; i < n + 1; i++) {
                List<Integer> tmp = new ArrayList<>(item);
                tmp.add(i);
                res.add(tmp);
            }
        }
        return res;
    }
}
// @lc code=end

