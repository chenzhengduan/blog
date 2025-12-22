// 📋 对象深拷贝
// 学习点：引用类型的拷贝

const obj = {
  name: '张三',
  age: 25,
  hobbies: ['读书', '游戏'],
  address: { city: '北京' }
};

// 方法1：JSON（最简单但有限制）
const copy1 = JSON.parse(JSON.stringify(obj));
console.log('JSON方法:', copy1);

// 方法2：递归（最完整）
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj;
  const clone = Array.isArray(obj) ? [] : {};
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      clone[key] = deepClone(obj[key]);
    }
  }
  return clone;
}
const copy2 = deepClone(obj);
console.log('递归方法:', copy2);

// 方法3：结构化克隆（新API）
// const copy3 = structuredClone(obj);
// console.log('structuredClone:', copy3);

// 验证深拷贝
copy2.hobbies.push('运动');
console.log('修改copy后原对象:', obj.hobbies); // 不受影响
