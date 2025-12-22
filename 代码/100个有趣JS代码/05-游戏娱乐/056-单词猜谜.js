// 🔡 单词猜谜游戏 (Wordle)
// 学习点：字符串比较

class WordleGame {
  constructor(word = 'HELLO') {
    this.word = word.toUpperCase();
    this.attempts = [];
    this.maxAttempts = 6;
  }
  
  guess(word) {
    word = word.toUpperCase();
    
    if (word.length !== this.word.length) {
      return { error: `单词必须是${this.word.length}个字母` };
    }
    
    if (this.attempts.length >= this.maxAttempts) {
      return { error: '已达到最大尝试次数' };
    }
    
    const feedback = [];
    const wordArray = this.word.split('');
    const guessArray = word.split('');
    
    // 标记正确位置
    guessArray.forEach((letter, i) => {
      if (letter === wordArray[i]) {
        feedback[i] = '🟩';  // 正确位置
      } else if (wordArray.includes(letter)) {
        feedback[i] = '🟨';  // 错误位置
      } else {
        feedback[i] = '⬜';  // 不存在
      }
    });
    
    this.attempts.push({ word, feedback });
    
    const won = word === this.word;
    const gameOver = won || this.attempts.length >= this.maxAttempts;
    
    return {
      success: true,
      feedback,
      display: feedback.join(''),
      won,
      gameOver,
      attempts: this.attempts.length,
      answer: gameOver ? this.word : null
    };
  }
  
  getBoard() {
    return this.attempts.map(a => `${a.word} ${a.feedback.join('')}`).join('\n');
  }
}

const wordle = new WordleGame('REACT');
console.log('Wordle 游戏！猜一个5字母单词');
console.log(wordle.guess('HELLO'));
console.log(wordle.guess('REACH'));
console.log(wordle.guess('REACT'));
console.log('\n游戏板:');
console.log(wordle.getBoard());
