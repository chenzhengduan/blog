// 🔗 最长公共子序列 (LCS)
// 学习点：动态规划

function lcs(str1, str2) {
  const m = str1.length;
  const n = str2.length;
  const dp = Array(m + 1).fill(0).map(() => Array(n + 1).fill(0));
  
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (str1[i - 1] === str2[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1] + 1;
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
      }
    }
  }
  
  // 回溯找出LCS
  let i = m, j = n;
  let result = '';
  while (i > 0 && j > 0) {
    if (str1[i - 1] === str2[j - 1]) {
      result = str1[i - 1] + result;
      i--;
      j--;
    } else if (dp[i - 1][j] > dp[i][j - 1]) {
      i--;
    } else {
      j--;
    }
  }
  
  return { length: dp[m][n], sequence: result };
}

const str1 = 'ABCDGH';
const str2 = 'AEDFHR';

console.log('字符串1:', str1);
console.log('字符串2:', str2);
const result = lcs(str1, str2);
console.log('LCS长度:', result.length);
console.log('LCS序列:', result.sequence);

console.log('\n💡 应用: 版本控制、DNA序列比对等');
