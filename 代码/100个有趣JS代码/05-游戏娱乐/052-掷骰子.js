// 🎲 掷骰子模拟器
// 学习点：随机数与概率

class DiceSimulator {
  constructor(sides = 6) {
    this.sides = sides;
    this.history = [];
  }
  
  roll(times = 1) {
    const results = [];
    for (let i = 0; i < times; i++) {
      const result = Math.floor(Math.random() * this.sides) + 1;
      results.push(result);
      this.history.push(result);
    }
    return results;
  }
  
  getStatistics() {
    const freq = {};
    this.history.forEach(num => {
      freq[num] = (freq[num] || 0) + 1;
    });
    
    return {
      total: this.history.length,
      frequency: freq,
      average: this.history.reduce((a, b) => a + b, 0) / this.history.length
    };
  }
}

const dice = new DiceSimulator(6);

console.log('掷骰子10次:', dice.roll(10));
console.log('\n掷骰子100次统计:');
dice.roll(90);
const stats = dice.getStatistics();
console.log('总次数:', stats.total);
console.log('各面频率:', stats.frequency);
console.log('平均值:', stats.average.toFixed(2));

console.log('\n💡 理论平均值应该接近 3.5');
