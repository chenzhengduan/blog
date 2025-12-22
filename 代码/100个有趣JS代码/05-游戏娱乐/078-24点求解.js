// 🎯 24点游戏求解器
// 学习点：回溯算法

class TwentyFourSolver {
  constructor() {
    this.epsilon = 1e-6;
  }
  
  solve(numbers) {
    const solutions = [];
    this.search(numbers.map(n => ({ value: n, expr: n.toString() })), 24, solutions);
    return solutions;
  }
  
  search(numbers, target, solutions) {
    if (numbers.length === 1) {
      if (Math.abs(numbers[0].value - target) < this.epsilon) {
        solutions.push(numbers[0].expr);
      }
      return;
    }
    
    for (let i = 0; i < numbers.length; i++) {
      for (let j = i + 1; j < numbers.length; j++) {
        const a = numbers[i];
        const b = numbers[j];
        
        // 剩余数字
        const remaining = numbers.filter((_, idx) => idx !== i && idx !== j);
        
        // 尝试所有运算
        const operations = [
          { value: a.value + b.value, expr: `(${a.expr}+${b.expr})` },
          { value: a.value - b.value, expr: `(${a.expr}-${b.expr})` },
          { value: b.value - a.value, expr: `(${b.expr}-${a.expr})` },
          { value: a.value * b.value, expr: `(${a.expr}*${b.expr})` }
        ];
        
        if (Math.abs(b.value) > this.epsilon) {
          operations.push({ value: a.value / b.value, expr: `(${a.expr}/${b.expr})` });
        }
        if (Math.abs(a.value) > this.epsilon) {
          operations.push({ value: b.value / a.value, expr: `(${b.expr}/${a.expr})` });
        }
        
        // 递归搜索
        operations.forEach(op => {
          this.search([...remaining, op], target, solutions);
        });
      }
    }
  }
}

const solver = new TwentyFourSolver();

console.log('24点求解器\n');

const testCases = [
  [3, 3, 8, 8],
  [1, 5, 5, 5],
  [4, 4, 10, 10]
];

testCases.forEach(numbers => {
  console.log(`数字: ${numbers.join(', ')}`);
  const solutions = solver.solve(numbers);
  
  if (solutions.length > 0) {
    console.log('解法:', solutions[0]);
  } else {
    console.log('无解');
  }
  console.log();
});
