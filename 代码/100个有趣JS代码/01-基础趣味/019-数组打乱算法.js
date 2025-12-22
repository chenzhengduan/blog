// 🎯 数组打乱（洗牌算法）
// 学习点：Fisher-Yates算法

function shuffle(array) {
  const arr = [...array]; // 不修改原数组
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

const cards = ['♠A', '♥K', '♣Q', '♦J', '♠10'];
console.log('原数组:', cards);
console.log('打乱后:', shuffle(cards));
console.log('再打乱:', shuffle(cards));

// 简化版（但不够随机）
const simpleShffle = arr => arr.sort(() => Math.random() - 0.5);
console.log('简化版:', simpleShffle([...cards]));

console.log('💡 应用：扑克洗牌、随机排序等');
