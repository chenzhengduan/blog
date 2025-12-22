// 🔄 约瑟夫环问题
// 学习点：经典算法问题

function josephus(n, k) {
  const people = Array.from({ length: n }, (_, i) => i + 1);
  const eliminated = [];
  let index = 0;
  
  while (people.length > 0) {
    index = (index + k - 1) % people.length;
    eliminated.push(people.splice(index, 1)[0]);
  }
  
  return eliminated;
}

console.log('约瑟夫环问题:');
console.log('10个人，数到3的人出列:');
const result = josephus(10, 3);
console.log('出列顺序:', result);
console.log('最后幸存者:', result[result.length - 1]);

console.log('\n💡 问题描述: n个人围成圈，从1开始数到k，');
console.log('   第k个人出列，然后继续从1数到k...');
