// 📊 数组统计工具
// 学习点：数组高级操作

const arrayStats = {
  // 求和
  sum(arr) {
    return arr.reduce((sum, num) => sum + num, 0);
  },
  
  // 平均值
  average(arr) {
    return this.sum(arr) / arr.length;
  },
  
  // 中位数
  median(arr) {
    const sorted = [...arr].sort((a, b) => a - b);
    const mid = Math.floor(sorted.length / 2);
    return sorted.length % 2 === 0
      ? (sorted[mid - 1] + sorted[mid]) / 2
      : sorted[mid];
  },
  
  // 众数
  mode(arr) {
    const frequency = {};
    let maxFreq = 0;
    let modes = [];
    
    arr.forEach(num => {
      frequency[num] = (frequency[num] || 0) + 1;
      if (frequency[num] > maxFreq) {
        maxFreq = frequency[num];
        modes = [num];
      } else if (frequency[num] === maxFreq && !modes.includes(num)) {
        modes.push(num);
      }
    });
    
    return modes;
  },
  
  // 方差
  variance(arr) {
    const avg = this.average(arr);
    return arr.reduce((sum, num) => sum + Math.pow(num - avg, 2), 0) / arr.length;
  },
  
  // 标准差
  standardDeviation(arr) {
    return Math.sqrt(this.variance(arr));
  }
};

const numbers = [1, 2, 3, 4, 5, 5, 6, 7, 8, 9];

console.log('数组:', numbers);
console.log('求和:', arrayStats.sum(numbers));
console.log('平均值:', arrayStats.average(numbers).toFixed(2));
console.log('中位数:', arrayStats.median(numbers));
console.log('众数:', arrayStats.mode(numbers));
console.log('标准差:', arrayStats.standardDeviation(numbers).toFixed(2));
