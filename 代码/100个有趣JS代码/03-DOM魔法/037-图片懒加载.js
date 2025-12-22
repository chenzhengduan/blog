// 🖼️ 图片懒加载
// 学习点：Intersection Observer实际应用

function lazyLoadImages(selector = 'img[data-src]') {
  const images = document.querySelectorAll(selector);
  
  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src;
        
        img.onload = () => {
          img.removeAttribute('data-src');
          console.log('✅ 图片加载完成:', img.src);
        };
        
        observer.unobserve(img);
      }
    });
  }, {
    rootMargin: '50px'  // 提前50px开始加载
  });
  
  images.forEach(img => imageObserver.observe(img));
  
  return imageObserver;
}

console.log('图片懒加载函数已定义');
console.log('HTML使用方法:');
console.log('<img data-src="real-image.jpg" src="placeholder.jpg" />');
console.log('\nJS调用:');
console.log('lazyLoadImages()');

console.log('\n💡 优点: 提升页面加载速度，节省带宽');
