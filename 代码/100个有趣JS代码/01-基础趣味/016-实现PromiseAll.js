// 🔄 Promise.all的实现
// 学习点：理解Promise原理

function promiseAll(promises) {
  return new Promise((resolve, reject) => {
    if (!Array.isArray(promises)) {
      return reject(new TypeError('参数必须是数组'));
    }
    
    const results = [];
    let completed = 0;
    
    promises.forEach((promise, index) => {
      Promise.resolve(promise).then(value => {
        results[index] = value;
        completed++;
        if (completed === promises.length) {
          resolve(results);
        }
      }).catch(reject);
    });
  });
}

// 测试
const p1 = Promise.resolve(1);
const p2 = new Promise(resolve => setTimeout(() => resolve(2), 1000));
const p3 = Promise.resolve(3);

promiseAll([p1, p2, p3]).then(results => {
  console.log('所有Promise完成:', results);
});
