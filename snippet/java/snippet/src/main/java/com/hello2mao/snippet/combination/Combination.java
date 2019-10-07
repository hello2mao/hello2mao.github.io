package com.hello2mao.snippet.combination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Combination {

    public static void C(int[] nums, int d, int k, int start, List<Integer> cur, List<List<Integer>> res) {
        if (d == k) {
            res.add(new ArrayList<>(cur));
            return;
        }

        for (int i = start; i < nums.length; i++) {
            cur.add(nums[i]);
            C(nums, d + 1, k, i + 1, cur, res);
            cur.remove(cur.size() - 1);
        }
    }

    public static void main(String[] args) {
        int[] testNums = new int[]{1,2,3};
        List<List<Integer>> res = new ArrayList<>();
        C(testNums, 0, 2, 0, new ArrayList<>(), res);

        for (List<Integer> item : res) {
            System.out.println(Arrays.toString(item.toArray()));
        }
    }
}
