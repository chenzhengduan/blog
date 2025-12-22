// 🎯 数组去重的6种方法
// 学习点：不同的去重技巧

const arr = [1, 2, 2, 3, 4, 4, 5, 1, 2];

// 方法1：Set（最简单）
const unique1 = [...new Set(arr)];
console.log('方法1 (Set):', unique1);

// 方法2：filter + indexOf
const unique2 = arr.filter((item, index) => arr.indexOf(item) === index);
console.log('方法2 (filter):', unique2);

// 方法3：reduce
const unique3 = arr.reduce((acc, cur) => 
  acc.includes(cur) ? acc : [...acc, cur], []);
console.log('方法3 (reduce):', unique3);

// 方法4：Map
const unique4 = Array.from(new Map(arr.map(item => [item, item])).values());
console.log('方法4 (Map):', unique4);

// 方法5：双层循环
const unique5 = [];
for (let i = 0; i < arr.length; i++) {
  if (!unique5.includes(arr[i])) unique5.push(arr[i]);
}
console.log('方法5 (循环):', unique5);

// 方法6：对象键值
const unique6 = Object.keys(arr.reduce((obj, item) => 
  ({ ...obj, [item]: true }), {})).map(Number);
console.log('方法6 (对象):', unique6);
