// 🎲 概率抽奖系统
// 学习点：加权随机算法

class LotterySystem {
  constructor(prizes) {
    this.prizes = prizes;
    this.calculateWeights();
  }
  
  calculateWeights() {
    let totalWeight = 0;
    this.prizes.forEach(prize => {
      totalWeight += prize.weight;
      prize.accumulated = totalWeight;
    });
    this.totalWeight = totalWeight;
  }
  
  draw() {
    const random = Math.random() * this.totalWeight;
    
    for (const prize of this.prizes) {
      if (random <= prize.accumulated) {
        return { ...prize };
      }
    }
  }
  
  // 模拟多次抽奖统计
  simulate(times = 10000) {
    const stats = {};
    
    this.prizes.forEach(prize => {
      stats[prize.name] = 0;
    });
    
    for (let i = 0; i < times; i++) {
      const result = this.draw();
      stats[result.name]++;
    }
    
    // 计算概率
    const report = {};
    Object.entries(stats).forEach(([name, count]) => {
      report[name] = {
        count,
        probability: (count / times * 100).toFixed(2) + '%',
        expected: (this.prizes.find(p => p.name === name).weight / this.totalWeight * 100).toFixed(2) + '%'
      };
    });
    
    return report;
  }
}

// 奖品配置
const prizes = [
  { name: '一等奖', value: 'iPhone', weight: 1 },
  { name: '二等奖', value: 'iPad', weight: 5 },
  { name: '三等奖', value: '耳机', weight: 20 },
  { name: '参与奖', value: '红包', weight: 74 }
];

const lottery = new LotterySystem(prizes);

console.log('概率抽奖系统\n');
console.log('单次抽奖结果:');
for (let i = 0; i < 5; i++) {
  console.log(lottery.draw());
}

console.log('\n模拟10000次抽奖:');
const stats = lottery.simulate(10000);
console.table(stats);
