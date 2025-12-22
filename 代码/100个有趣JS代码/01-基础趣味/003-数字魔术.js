// 🎩 数字魔术
// 学习点：JavaScript中的数字技巧

console.log('=== 快速取整 ===');
console.log('~~4.9 =', ~~4.9);              // 4
console.log('4.9 | 0 =', 4.9 | 0);          // 4
console.log('4.9 >> 0 =', 4.9 >> 0);        // 4
console.log('Math.floor(4.9) =', Math.floor(4.9)); // 4

console.log('\n=== 数字反转 ===');
const num = 12345;
const reversed = +String(num).split('').reverse().join('');
console.log(`${num} 反转后 =`, reversed);

console.log('\n=== 生成随机数 ===');
console.log('0-10随机整数:', Math.floor(Math.random() * 11));
console.log('1-6骰子:', Math.floor(Math.random() * 6) + 1);

console.log('\n=== 判断奇偶 ===');
const testNum = 7;
console.log(`${testNum} 是${testNum & 1 ? '奇数' : '偶数'}`);

console.log('\n=== 数字格式化 ===');
const bigNum = 1234567890;
console.log('千分位:', bigNum.toLocaleString());
console.log('2位小数:', (123.456).toFixed(2));
