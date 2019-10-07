package com.hello2mao.snippet.permutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permutation {

    // n = nums.length
    // p(n, k)
    public static void P(int[] nums, int d, int k, boolean[] used, List<Integer> cur, List<List<Integer>> res) {
        if (d == k) {
            res.add(new ArrayList<>(cur));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (used[i]) {
                continue;
            }
            cur.add(nums[i]);
            used[i] = true;
            P(nums, d + 1, k, used, cur, res);
            cur.remove(cur.size() - 1);
            used[i] = false;
        }
    }

    public static void main(String[] args) {
        int[] testNums = new int[]{1,2,3};
        boolean[] used = new boolean[testNums.length];
        List<List<Integer>> res = new ArrayList<>();
        P(testNums, 0, 2, used, new ArrayList<>(), res);

        for (List<Integer> item : res) {
            System.out.println(Arrays.toString(item.toArray()));
        }
    }
}
