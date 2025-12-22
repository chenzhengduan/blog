// 🎮 猜数字游戏
// 学习点：交互式游戏逻辑

class GuessNumberGame {
  constructor(min = 1, max = 100) {
    this.min = min;
    this.max = max;
    this.target = Math.floor(Math.random() * (max - min + 1)) + min;
    this.attempts = 0;
    this.history = [];
  }
  
  guess(number) {
    this.attempts++;
    this.history.push(number);
    
    if (number === this.target) {
      return {
        success: true,
        message: `🎉 恭喜！猜对了！用了${this.attempts}次`,
        attempts: this.attempts,
        history: this.history
      };
    } else if (number < this.target) {
      return {
        success: false,
        message: '📈 太小了，再大一点',
        hint: 'higher'
      };
    } else {
      return {
        success: false,
        message: '📉 太大了，再小一点',
        hint: 'lower'
      };
    }
  }
  
  reset() {
    this.target = Math.floor(Math.random() * (this.max - this.min + 1)) + this.min;
    this.attempts = 0;
    this.history = [];
  }
}

// 演示
const game = new GuessNumberGame(1, 100);
console.log('猜数字游戏开始！范围: 1-100');
console.log(game.guess(50));
console.log(game.guess(75));
console.log(game.guess(88));

console.log('\n💡 在浏览器中可以做成交互式游戏');
