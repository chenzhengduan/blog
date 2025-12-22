// 🎒 0-1背包问题
// 学习点：动态规划

function knapsack(weights, values, capacity) {
  const n = weights.length;
  const dp = Array(n + 1).fill(0).map(() => Array(capacity + 1).fill(0));
  
  for (let i = 1; i <= n; i++) {
    for (let w = 1; w <= capacity; w++) {
      if (weights[i - 1] <= w) {
        dp[i][w] = Math.max(
          dp[i - 1][w],
          dp[i - 1][w - weights[i - 1]] + values[i - 1]
        );
      } else {
        dp[i][w] = dp[i - 1][w];
      }
    }
  }
  
  return dp[n][capacity];
}

const weights = [2, 3, 4, 5];
const values = [3, 4, 5, 6];
const capacity = 8;

console.log('物品重量:', weights);
console.log('物品价值:', values);
console.log('背包容量:', capacity);
console.log('最大价值:', knapsack(weights, values, capacity));

console.log('\n💡 应用场景: 资源分配、投资组合优化等');
