// 🎯 汉诺塔问题
// 学习点：递归思想

function hanoi(n, from = 'A', to = 'C', aux = 'B') {
  const moves = [];
  
  function solve(n, from, to, aux) {
    if (n === 1) {
      moves.push({ from, to, disk: 1 });
      return;
    }
    
    // 将n-1个盘子从from移到aux
    solve(n - 1, from, aux, to);
    
    // 将最大的盘子从from移到to
    moves.push({ from, to, disk: n });
    
    // 将n-1个盘子从aux移到to
    solve(n - 1, aux, to, from);
  }
  
  solve(n, from, to, aux);
  return moves;
}

function visualizeHanoi(n) {
  console.log(`汉诺塔问题 (${n}个盘子)\n`);
  
  const moves = hanoi(n);
  
  moves.forEach((move, index) => {
    console.log(`步骤${index + 1}: 将盘子${move.disk}从 ${move.from} 移到 ${move.to}`);
  });
  
  console.log(`\n总步数: ${moves.length}`);
  console.log(`公式: 2^n - 1 = ${Math.pow(2, n) - 1}`);
}

visualizeHanoi(3);

console.log('\n💡 时间复杂度: O(2^n)');
console.log('💡 经典递归问题，体现分治思想');
