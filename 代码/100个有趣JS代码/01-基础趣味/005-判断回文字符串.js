// 🔄 判断回文字符串
// 学习点：字符串算法

function isPalindrome(str) {
  const cleaned = str.toLowerCase().replace(/[^a-z0-9]/g, '');
  return cleaned === cleaned.split('').reverse().join('');
}

console.log('测试回文：');
console.log('level:', isPalindrome('level'));           // true
console.log('A man a plan a canal Panama:', 
  isPalindrome('A man a plan a canal Panama'));        // true
console.log('hello:', isPalindrome('hello'));           // false
console.log('12321:', isPalindrome('12321'));          // true
