// 📦 扁平化数组
// 学习点：递归和数组操作

const nested = [1, [2, 3], [4, [5, 6]], 7];

// 方法1：ES6 flat
console.log('flat(Infinity):', nested.flat(Infinity));

// 方法2：递归
function flatten(arr) {
  return arr.reduce((acc, val) => 
    Array.isArray(val) ? acc.concat(flatten(val)) : acc.concat(val), []);
}
console.log('递归方法:', flatten(nested));

// 方法3：toString（仅数字）
const flattened = nested.toString().split(',').map(Number);
console.log('toString:', flattened);

// 方法4：扩展运算符（仅一层）
const oneLevelFlat = [].concat(...nested);
console.log('一层扁平:', oneLevelFlat);
