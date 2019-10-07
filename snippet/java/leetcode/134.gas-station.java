/*
 * @lc app=leetcode id=134 lang=java
 *
 * [134] Gas Station
 *
 * https://leetcode.com/problems/gas-station/description/
 *
 * algorithms
 * Medium (34.69%)
 * Likes:    856
 * Dislikes: 304
 * Total Accepted:    155.5K
 * Total Submissions: 447K
 * Testcase Example:  '[1,2,3,4,5]\n[3,4,5,1,2]'
 *
 * There are N gas stations along a circular route, where the amount of gas at
 * station i is gas[i].
 * 
 * You have a car with an unlimited gas tank and it costs cost[i] of gas to
 * travel from station i to its next station (i+1). You begin the journey with
 * an empty tank at one of the gas stations.
 * 
 * Return the starting gas station's index if you can travel around the circuit
 * once in the clockwise direction, otherwise return -1.
 * 
 * Note:
 * 
 * 
 * If there exists a solution, it is guaranteed to be unique.
 * Both input arrays are non-empty and have the same length.
 * Each element in the input arrays is a non-negative integer.
 * 
 * 
 * Example 1:
 * 
 * 
 * Input: 
 * gas  = [1,2,3,4,5]
 * cost = [3,4,5,1,2]
 * 
 * Output: 3
 * 
 * Explanation:
 * Start at station 3 (index 3) and fill up with 4 unit of gas. Your tank = 0 +
 * 4 = 4
 * Travel to station 4. Your tank = 4 - 1 + 5 = 8
 * Travel to station 0. Your tank = 8 - 2 + 1 = 7
 * Travel to station 1. Your tank = 7 - 3 + 2 = 6
 * Travel to station 2. Your tank = 6 - 4 + 3 = 5
 * Travel to station 3. The cost is 5. Your gas is just enough to travel back
 * to station 3.
 * Therefore, return 3 as the starting index.
 * 
 * 
 * Example 2:
 * 
 * 
 * Input: 
 * gas  = [2,3,4]
 * cost = [3,4,3]
 * 
 * Output: -1
 * 
 * Explanation:
 * You can't start at station 0 or 1, as there is not enough gas to travel to
 * the next station.
 * Let's start at station 2 and fill up with 4 unit of gas. Your tank = 0 + 4 =
 * 4
 * Travel to station 0. Your tank = 4 - 3 + 2 = 3
 * Travel to station 1. Your tank = 3 - 3 + 3 = 3
 * You cannot travel back to station 2, as it requires 4 unit of gas but you
 * only have 3.
 * Therefore, you can't travel around the circuit once no matter where you
 * start.
 * 
 * 
 */
class Solution {

    public int canCompleteCircuit(int[] gas, int[] cost) {
        return canCompleteCircuit_1(gas, cost);
    }

    // Time Complexity: O(n)
    // Space Complexity: O(1)
    // 我们首先要知道能走完整个环的前提是gas的总量要大于cost的总量，这样才会有起点的存在。
    // 假设开始设置起点start = 0, 并从这里出发，如果当前的gas值大于cost值，就可以继续前进，
    // 此时到下一个站点，剩余的gas加上当前的gas再减去cost，看是否大于0，
    // 若大于0，则继续前进。
    // 当到达某一站点时，若这个值小于0了，则说明从起点到这个点中间的任何一个点都不能作为起点，则把起点设为下一个点，继续遍历。当遍历完整个环时，当前保存的起点即为所求。
    public int canCompleteCircuit_1(int[] gas, int[] cost) {
        int tank = 0;
        for (int i = 0; i < gas.length; i++) {
            tank += gas[i] - cost[i];
        }
        if (tank < 0) {
            return -1;
        }

        int start = 0, accumulate = 0;
        for (int i = 0; i < gas.length; i++) {
            int curGain = gas[i] - cost[i];
            if (accumulate + curGain >= 0) {
                accumulate += curGain;
            } else {
                accumulate = 0;
                start = i + 1;
            }
        }
        return start;
    }

    // Time Complexity: O(n^2)
    // Space Complexity: O(1)
    public int canCompleteCircuit_2(int[] gas, int[] cost) {
        for (int i = 0; i < gas.length; i++) {
            int tank = gas[i];
            for (int j = 0; j < gas.length; j++) {
                tank -= cost[(i + j) % gas.length];
                if (tank < 0) {
                    break;
                }
                tank += gas[(i + j + 1) % gas.length];
            }
            if (tank >= 0) {
                return i;
            }
        }
        return -1;
    }
}
