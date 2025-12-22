// 🌟 字符串模式匹配 (KMP算法)
// 学习点：高效字符串查找

class KMP {
  // 构建部分匹配表(前缀函数)
  buildPrefixTable(pattern) {
    const table = [0];
    let len = 0;
    let i = 1;
    
    while (i < pattern.length) {
      if (pattern[i] === pattern[len]) {
        len++;
        table[i] = len;
        i++;
      } else {
        if (len !== 0) {
          len = table[len - 1];
        } else {
          table[i] = 0;
          i++;
        }
      }
    }
    
    return table;
  }
  
  search(text, pattern) {
    if (pattern.length === 0) return [];
    
    const table = this.buildPrefixTable(pattern);
    const matches = [];
    
    let i = 0; // text的索引
    let j = 0; // pattern的索引
    
    while (i < text.length) {
      if (text[i] === pattern[j]) {
        i++;
        j++;
      }
      
      if (j === pattern.length) {
        matches.push(i - j);
        j = table[j - 1];
      } else if (i < text.length && text[i] !== pattern[j]) {
        if (j !== 0) {
          j = table[j - 1];
        } else {
          i++;
        }
      }
    }
    
    return matches;
  }
}

const kmp = new KMP();

console.log('KMP字符串匹配算法\n');

const text = 'ABABDABACDABABCABAB';
const pattern = 'ABABCABAB';

console.log('文本:', text);
console.log('模式:', pattern);

const matches = kmp.search(text, pattern);
console.log('找到位置:', matches);

console.log('\n💡 时间复杂度: O(n+m)');
console.log('💡 比暴力匹配O(n*m)更高效');
