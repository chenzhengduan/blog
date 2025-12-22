// 🎨 ASCII艺术生成器
// 学习点：字符串处理

const asciiArt = {
  // 文字转ASCII艺术
  text(str) {
    const chars = {
      'A': ['  A  ', ' A A ', 'AAAAA', 'A   A', 'A   A'],
      'B': ['BBBB ', 'B   B', 'BBBB ', 'B   B', 'BBBB '],
      'C': [' CCC ', 'C   C', 'C    ', 'C   C', ' CCC '],
      // 可以添加更多字母...
    };
    
    const lines = ['', '', '', '', ''];
    
    str.toUpperCase().split('').forEach(char => {
      if (chars[char]) {
        chars[char].forEach((line, i) => {
          lines[i] += line + '  ';
        });
      }
    });
    
    return lines.join('\n');
  },
  
  // 简单图案
  heart() {
    return `
  ♥♥   ♥♥
 ♥  ♥ ♥  ♥
♥    ♥    ♥
 ♥       ♥
  ♥     ♥
   ♥   ♥
    ♥ ♥
     ♥
    `.trim();
  },
  
  christmas() {
    return `
     *
    ***
   *****
  *******
 *********
***********
    |||
    `.trim();
  },
  
  // 进度条
  progressBar(progress, width = 20) {
    const filled = Math.round(width * progress);
    const bar = '█'.repeat(filled) + '░'.repeat(width - filled);
    return `[${bar}] ${(progress * 100).toFixed(0)}%`;
  }
};

console.log('ASCII艺术生成器\n');
console.log('字母A:');
console.log(asciiArt.text('A'));

console.log('\n❤️ 爱心:');
console.log(asciiArt.heart());

console.log('\n🎄 圣诞树:');
console.log(asciiArt.christmas());

console.log('\n进度条:');
console.log(asciiArt.progressBar(0.3));
console.log(asciiArt.progressBar(0.7));
console.log(asciiArt.progressBar(1.0));
