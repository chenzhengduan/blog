// 🎲 UUID生成器
// 学习点：唯一标识符生成

function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

// 短ID生成
function generateShortId(length = 8) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

// 纳秒级唯一ID
function generateNanoId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

console.log('UUID:', generateUUID());
console.log('短ID:', generateShortId());
console.log('纳秒ID:', generateNanoId());

console.log('\n生成10个短ID:');
for (let i = 0; i < 10; i++) {
  console.log(generateShortId(6));
}
