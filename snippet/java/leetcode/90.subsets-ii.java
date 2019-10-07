/*
 * @lc app=leetcode id=90 lang=java
 *
 * [90] Subsets II
 *
 * https://leetcode.com/problems/subsets-ii/description/
 *
 * algorithms
 * Medium (43.69%)
 * Likes:    1076
 * Dislikes: 53
 * Total Accepted:    224.8K
 * Total Submissions: 513.5K
 * Testcase Example:  '[1,2,2]'
 *
 * Given a collection of integers that might contain duplicates, nums, return
 * all possible subsets (the power set).
 * 
 * Note: The solution set must not contain duplicate subsets.
 * 
 * Example:
 * 
 * 
 * Input: [1,2,2]
 * Output:
 * [
 * ⁠ [2],
 * ⁠ [1],
 * ⁠ [1,2,2],
 * ⁠ [2,2],
 * ⁠ [1,2],
 * ⁠ []
 * ]
 * 
 * 
 */
class Solution {
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        res.add(new ArrayList<>());
        if (nums.length == 0) {
            return res;
        }

        Arrays.sort(nums); // O(nlogn)
        int last = nums[0];
        int lastStartIndex = 0;
        for (int num : nums) {
            int startIndex = 0;
            if (num == last) {
                startIndex = lastStartIndex;
            }
            last = num;
            lastStartIndex = res.size();

            List<List<Integer>> tmp = new ArrayList<>();
            for (int i = startIndex; i < res.size(); i++) {
                List<Integer> newItem = new ArrayList<>(res.get(i));
                newItem.add(num);
                tmp.add(newItem);
            }
            res.addAll(tmp);
        }
        return res;
    }
}
