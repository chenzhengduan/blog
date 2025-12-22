// 🌈 随机渐变背景生成器
// 学习点：CSS渐变与随机颜色

function randomGradient() {
  const randomColor = () => {
    const r = Math.floor(Math.random() * 256);
    const g = Math.floor(Math.random() * 256);
    const b = Math.floor(Math.random() * 256);
    return `rgb(${r}, ${g}, ${b})`;
  };
  
  const color1 = randomColor();
  const color2 = randomColor();
  const angle = Math.floor(Math.random() * 360);
  
  const gradient = `linear-gradient(${angle}deg, ${color1}, ${color2})`;
  
  console.log('生成渐变:', gradient);
  return gradient;
}

function applyRandomGradient(element = document.body) {
  const gradient = randomGradient();
  element.style.background = gradient;
}

// 生成几个示例
console.log('随机渐变生成器已定义');
console.log('\n生成5个渐变示例:');
for (let i = 0; i < 5; i++) {
  randomGradient();
}

console.log('\n使用方法:');
console.log('applyRandomGradient()  // 应用到body');
console.log('applyRandomGradient(document.querySelector(".box"))');
