// 🔍 深度搜索对象
// 学习点：递归与对象遍历

function deepSearch(obj, searchKey, searchValue) {
  const results = [];
  
  function search(current, path = '') {
    if (typeof current !== 'object' || current === null) return;
    
    for (const [key, value] of Object.entries(current)) {
      const currentPath = path ? `${path}.${key}` : key;
      
      // 匹配键名
      if (key === searchKey) {
        results.push({ path: currentPath, key, value });
      }
      
      // 匹配值
      if (value === searchValue) {
        results.push({ path: currentPath, key, value });
      }
      
      // 递归搜索
      if (typeof value === 'object' && value !== null) {
        search(value, currentPath);
      }
    }
  }
  
  search(obj);
  return results;
}

const data = {
  name: 'John',
  age: 30,
  address: {
    city: 'Beijing',
    zip: '100000'
  },
  hobbies: ['reading', 'coding'],
  friends: [
    { name: 'Alice', age: 28 },
    { name: 'Bob', age: 30 }
  ]
};

console.log('搜索 age:');
console.log(deepSearch(data, 'age'));

console.log('\n搜索值为 30:');
console.log(deepSearch(data, null, 30));
