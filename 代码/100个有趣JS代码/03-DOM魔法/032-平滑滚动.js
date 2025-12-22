// 📜 平滑滚动到顶部
// 学习点：scrollTo API与动画

function smoothScrollToTop(duration = 500) {
  const start = window.scrollY;
  const startTime = performance.now();
  
  function scroll(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    
    // 缓动函数
    const easeOutCubic = progress => 1 - Math.pow(1 - progress, 3);
    
    window.scrollTo(0, start * (1 - easeOutCubic(progress)));
    
    if (progress < 1) {
      requestAnimationFrame(scroll);
    }
  }
  
  requestAnimationFrame(scroll);
}

// 现代浏览器简化版
function smoothScrollToTopModern() {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
}

console.log('平滑滚动函数已定义');
console.log('使用方法:');
console.log('smoothScrollToTop(500)  // 自定义动画');
console.log('smoothScrollToTopModern()  // 浏览器原生');
