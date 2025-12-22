// 🎰 老虎机模拟器
// 学习点：概率计算

class SlotMachine {
  constructor() {
    this.symbols = ['🍒', '🍋', '🍊', '🍉', '⭐', '💎'];
    this.reels = 3;
  }
  
  spin() {
    const result = [];
    for (let i = 0; i < this.reels; i++) {
      const symbol = this.symbols[Math.floor(Math.random() * this.symbols.length)];
      result.push(symbol);
    }
    
    const prize = this.calculatePrize(result);
    
    return {
      result,
      display: result.join(' | '),
      ...prize
    };
  }
  
  calculatePrize(result) {
    const [a, b, c] = result;
    
    // 三个相同
    if (a === b && b === c) {
      const multipliers = {
        '💎': 100,
        '⭐': 50,
        '🍉': 20,
        '🍊': 15,
        '🍋': 10,
        '🍒': 5
      };
      return {
        win: true,
        multiplier: multipliers[a] || 5,
        message: `🎉 大奖！三个 ${a}`
      };
    }
    
    // 两个相同
    if (a === b || b === c || a === c) {
      return {
        win: true,
        multiplier: 2,
        message: '🎊 小奖！两个相同'
      };
    }
    
    return {
      win: false,
      multiplier: 0,
      message: '再试一次'
    };
  }
}

const slot = new SlotMachine();

console.log('老虎机模拟器');
console.log('玩10次:\n');

let totalWin = 0;
for (let i = 1; i <= 10; i++) {
  const result = slot.spin();
  console.log(`第${i}次: ${result.display} - ${result.message}`);
  if (result.win) totalWin++;
}

console.log(`\n胜率: ${(totalWin / 10 * 100).toFixed(0)}%`);
