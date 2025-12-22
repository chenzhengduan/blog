// 🎯 节流函数
// 学习点：性能优化技巧

function throttle(fn, delay = 300) {
  let last = 0;
  return function(...args) {
    const now = Date.now();
    if (now - last > delay) {
      fn.apply(this, args);
      last = now;
    }
  };
}

// 使用示例
const log = throttle((msg) => console.log('执行:', msg), 1000);

console.log('连续调用，每秒最多执行一次：');
let count = 0;
const timer = setInterval(() => {
  log(`第${++count}次调用`);
  if (count >= 5) clearInterval(timer);
}, 200);

setTimeout(() => {
  console.log('💡 应用场景：滚动事件、按钮防重复点击等');
}, 2000);
