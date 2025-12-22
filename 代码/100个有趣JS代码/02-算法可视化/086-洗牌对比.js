// 🎲 洗牌算法比较
// 学习点：随机算法的正确性

const shuffleAlgorithms = {
  // Fisher-Yates (正确)
  fisherYates(arr) {
    const result = [...arr];
    for (let i = result.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [result[i], result[j]] = [result[j], result[i]];
    }
    return result;
  },
  
  // 错误的方法：sort + random (分布不均)
  sortRandom(arr) {
    return [...arr].sort(() => Math.random() - 0.5);
  },
  
  // Knuth洗牌（Fisher-Yates的另一种写法）
  knuth(arr) {
    const result = [...arr];
    for (let i = 0; i < result.length - 1; i++) {
      const j = i + Math.floor(Math.random() * (result.length - i));
      [result[i], result[j]] = [result[j], result[i]];
    }
    return result;
  }
};

// 测试分布均匀性
function testShuffle(algorithm, arr, times = 10000) {
  const stats = {};
  
  for (let i = 0; i < times; i++) {
    const shuffled = algorithm(arr);
    const key = shuffled.join(',');
    stats[key] = (stats[key] || 0) + 1;
  }
  
  const permutations = Object.keys(stats).length;
  const frequencies = Object.values(stats);
  const avgFreq = times / permutations;
  const variance = frequencies.reduce((sum, f) => sum + Math.pow(f - avgFreq, 2), 0) / permutations;
  
  return {
    permutations,
    avgFrequency: avgFreq.toFixed(2),
    variance: variance.toFixed(2)
  };
}

console.log('洗牌算法比较\n');

const arr = [1, 2, 3];
console.log('原数组:', arr);
console.log('可能的排列数:', factorial(arr.length), '\n');

console.log('Fisher-Yates:');
console.log(testShuffle(shuffleAlgorithms.fisherYates, arr));

console.log('\nsort+random (不推荐):');
console.log(testShuffle(shuffleAlgorithms.sortRandom, arr));

console.log('\n💡 Fisher-Yates是最标准的洗牌算法');
console.log('💡 sort+random分布不均，不应使用');

function factorial(n) {
  let result = 1;
  for (let i = 2; i <= n; i++) result *= i;
  return result;
}
