// 📈 找出数组最大值的5种方法
// 学习点：数组操作技巧

const numbers = [1, 5, 3, 9, 2, 8, 4];

// 方法1：Math.max + 扩展运算符
console.log('Math.max(...):', Math.max(...numbers));

// 方法2：Math.max + apply
console.log('apply:', Math.max.apply(null, numbers));

// 方法3：reduce
const max3 = numbers.reduce((max, num) => num > max ? num : max);
console.log('reduce:', max3);

// 方法4：sort
const max4 = [...numbers].sort((a, b) => b - a)[0];
console.log('sort:', max4);

// 方法5：循环
let max5 = numbers[0];
for (let num of numbers) {
  if (num > max5) max5 = num;
}
console.log('循环:', max5);
