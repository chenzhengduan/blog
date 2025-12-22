// 🎲 生成随机颜色
// 学习点：颜色处理

// 方法1：16进制
function randomHex() {
  return '#' + Math.floor(Math.random() * 0xffffff).toString(16).padStart(6, '0');
}

// 方法2：RGB
function randomRGB() {
  const r = Math.floor(Math.random() * 256);
  const g = Math.floor(Math.random() * 256);
  const b = Math.floor(Math.random() * 256);
  return `rgb(${r}, ${g}, ${b})`;
}

// 方法3：HSL（更和谐的颜色）
function randomHSL() {
  const h = Math.floor(Math.random() * 360);
  const s = Math.floor(Math.random() * 100);
  const l = Math.floor(Math.random() * 100);
  return `hsl(${h}, ${s}%, ${l}%)`;
}

console.log('随机16进制:', randomHex());
console.log('随机RGB:', randomRGB());
console.log('随机HSL:', randomHSL());

// 生成5个随机颜色
console.log('\n5个随机颜色：');
for (let i = 0; i < 5; i++) {
  console.log(randomHex());
}
