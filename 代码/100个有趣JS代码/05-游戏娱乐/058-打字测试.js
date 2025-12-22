// 🎯 打字速度测试
// 学习点：时间计算与准确率

class TypingTest {
  constructor(text) {
    this.text = text;
    this.startTime = null;
    this.endTime = null;
    this.typed = '';
  }
  
  start() {
    this.startTime = Date.now();
    console.log('开始打字！');
    console.log('目标文本:', this.text);
  }
  
  input(char) {
    if (!this.startTime) {
      this.start();
    }
    this.typed += char;
  }
  
  finish() {
    this.endTime = Date.now();
    return this.getResults();
  }
  
  getResults() {
    const duration = (this.endTime - this.startTime) / 1000 / 60; // 分钟
    const wordsTyped = this.typed.split(' ').length;
    const wpm = Math.round(wordsTyped / duration);
    
    // 计算准确率
    let correct = 0;
    const minLength = Math.min(this.text.length, this.typed.length);
    
    for (let i = 0; i < minLength; i++) {
      if (this.text[i] === this.typed[i]) correct++;
    }
    
    const accuracy = (correct / this.text.length * 100).toFixed(2);
    
    return {
      wpm,
      accuracy: accuracy + '%',
      duration: (this.endTime - this.startTime) / 1000,
      correct,
      total: this.text.length
    };
  }
}

// 模拟测试
const test = new TypingTest('The quick brown fox jumps over the lazy dog');
test.start();

// 模拟打字
setTimeout(() => {
  test.typed = 'The quick brown fox jumps over the lazy dog';
  test.endTime = Date.now();
  console.log('\n测试结果:', test.getResults());
}, 100);

console.log('\n💡 实际使用时配合键盘事件');
