// 🔄 字符串反转的多种方法
// 学习点：字符串操作技巧

const str = 'Hello World';

// 方法1：使用数组reverse
const reversed1 = str.split('').reverse().join('');
console.log('方法1:', reversed1);

// 方法2：for循环
let reversed2 = '';
for (let i = str.length - 1; i >= 0; i--) {
  reversed2 += str[i];
}
console.log('方法2:', reversed2);

// 方法3：reduce
const reversed3 = str.split('').reduce((acc, char) => char + acc, '');
console.log('方法3:', reversed3);

// 方法4：递归
function reverseStr(s) {
  return s ? reverseStr(s.slice(1)) + s[0] : '';
}
console.log('方法4:', reverseStr(str));

// 方法5：扩展运算符
const reversed5 = [...str].reverse().join('');
console.log('方法5:', reversed5);
