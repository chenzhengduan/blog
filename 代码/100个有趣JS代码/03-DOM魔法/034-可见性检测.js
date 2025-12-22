// 👁️ 元素可见性检测
// 学习点：Intersection Observer API

function observeVisibility(element, callback, options = {}) {
  const defaultOptions = {
    root: null,
    rootMargin: '0px',
    threshold: 0.5
  };
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      callback(entry.isIntersecting, entry);
    });
  }, { ...defaultOptions, ...options });
  
  observer.observe(element);
  return observer;
}

// 使用示例
console.log('可见性检测函数已定义');
console.log('使用方法:');
console.log(`
const element = document.querySelector('.my-element');
observeVisibility(element, (isVisible) => {
  console.log('元素是否可见:', isVisible);
});
`);

console.log('\n💡 应用: 懒加载、埋点统计、无限滚动等');
