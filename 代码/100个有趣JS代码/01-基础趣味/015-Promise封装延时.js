// 🎁 Promise封装setTimeout
// 学习点：Promise的使用

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

console.log('开始:', new Date().toLocaleTimeString());

sleep(2000).then(() => {
  console.log('2秒后:', new Date().toLocaleTimeString());
});

// 使用async/await
async function demo() {
  console.log('异步开始');
  await sleep(1000);
  console.log('1秒后');
  await sleep(1000);
  console.log('又过了1秒');
}

demo();

console.log('💡 应用场景：延迟执行、动画间隔等');
