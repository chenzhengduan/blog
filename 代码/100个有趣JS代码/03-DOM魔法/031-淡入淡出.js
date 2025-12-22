// 🎨 元素淡入淡出动画
// 学习点：CSS过渡与JS结合

function fadeOut(element, duration = 300) {
  element.style.transition = `opacity ${duration}ms`;
  element.style.opacity = '0';
  
  setTimeout(() => {
    element.style.display = 'none';
  }, duration);
}

function fadeIn(element, duration = 300, display = 'block') {
  element.style.display = display;
  element.style.opacity = '0';
  element.style.transition = `opacity ${duration}ms`;
  
  setTimeout(() => {
    element.style.opacity = '1';
  }, 10);
}

// 使用示例
console.log('淡入淡出函数已定义');
console.log('使用方法:');
console.log('fadeOut(document.querySelector(".box"), 500)');
console.log('fadeIn(document.querySelector(".box"), 500)');

console.log('\n💡 可在浏览器控制台配合HTML元素使用');
