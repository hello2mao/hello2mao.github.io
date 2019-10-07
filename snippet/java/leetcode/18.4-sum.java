/*
 * @lc app=leetcode id=18 lang=java
 *
 * [18] 4Sum
 *
 * https://leetcode.com/problems/4sum/description/
 *
 * algorithms
 * Medium (31.27%)
 * Likes:    1216
 * Dislikes: 239
 * Total Accepted:    254.9K
 * Total Submissions: 815K
 * Testcase Example:  '[1,0,-1,0,-2,2]\n0'
 *
 * Given an array nums of n integers and an integer target, are there elements
 * a, b, c, and d in nums such that a + b + c + d = target? Find all unique
 * quadruplets in the array which gives the sum of target.
 * 
 * Note:
 * 
 * The solution set must not contain duplicate quadruplets.
 * 
 * Example:
 * 
 * 
 * Given array nums = [1, 0, -1, 0, -2, 2], and target = 0.
 * 
 * A solution set is:
 * [
 * ⁠ [-1,  0, 0, 1],
 * ⁠ [-2, -1, 1, 2],
 * ⁠ [-2,  0, 0, 2]
 * ]
 * 
 * 
 */
class Solution {

    // Time Complexity: O(n^3)
    // Space Complexity: 0(1)
    public List<List<Integer>> fourSum(int[] nums, int target) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();

        for (int i = 0; i < nums.length - 3; i++) {
            if (i != 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            for (int j = i + 1; j < nums.length - 2; j++) {
                if (j != i + 1 && nums[j] == nums[j - 1]) {
                    continue;
                }
                int low = j + 1;
                int high = nums.length - 1;
                int sum = target - nums[i] - nums[j];
                boolean firstAdd = true;
                while (low < high) {
                    if (nums[low] + nums[high] == sum) {
                        if (firstAdd || res.get(res.size() - 1).get(2) != nums[low]) {
                            res.add(Arrays.asList(nums[i], nums[j], nums[low], nums[high]));
                            firstAdd = false;
                        }
                        low++;
                        high--;
                    } else if (sum > nums[low] + nums[high]) {
                        low++;
                    } else if (sum < nums[low] + nums[high]) {
                        high--;
                    }
                }
            }
        }
        return res;

    }

    // Time Complexity: O(n^2)
    // Space Complexity:
    // Ref: https://www.1point3acres.com/bbs/thread-142138-1-1.html
    public List<List<Integer>> fourSum_2(int[] nums, int target) {
        Arrays.sort(nums);

        // cache all 2 sum in hashmap
        Map<Integer, List<Pair>> tmp = new HashMap<>();
        for (int i = 0; i < nums.length - 1; i++) { // O(n^2)
            for (int j = i + 1; j < nums.length; j++) {
                int key = nums[i] + nums[j];
                if (tmp.containsKey(key)) {
                    List<Pair> list = tmp.get(key);
                    list.add(new Pair(i, j));
                    tmp.put(key, list);
                } else {
                    List<Pair> list = new ArrayList<>();
                    list.add(new Pair(i, j));
                    tmp.put(key, list);
                }
            }
        }

        // get all result
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 3; i++) {
            if (i == 0 || nums[i] != nums[i - 1]) {
                for (int j = i + 1; j < nums.length - 2; j++) {
                    if (j == i + 1 || nums[j] != nums[j - 1]) {
                        int key = target - nums[i] - nums[j];
                        if (tmp.containsKey(key)) {
                            List<Pair> list = tmp.get(key);
                            boolean firstAdd = true;
                            for (Pair pair : list) {
                                if (j < pair.first
                                        && (firstAdd || res.get(res.size() - 1).get(2) != nums[pair.first])) {
                                    firstAdd = false;
                                    res.add(Arrays.asList(nums[i], nums[j], nums[pair.first], nums[pair.second]));
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public static class Pair {
        private int first;
        private int second;

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }
}
