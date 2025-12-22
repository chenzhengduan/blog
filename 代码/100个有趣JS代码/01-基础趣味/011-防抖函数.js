// ⏱️ 防抖函数
// 学习点：性能优化技巧

function debounce(fn, delay = 300) {
  let timer = null;
  return function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}

// 使用示例
const log = debounce((msg) => console.log('执行:', msg), 1000);

console.log('连续调用3次，只执行最后一次：');
log('第1次');
log('第2次');
log('第3次'); // 只有这次会执行

setTimeout(() => {
  console.log('💡 应用场景：搜索框输入、窗口resize等');
}, 1500);
