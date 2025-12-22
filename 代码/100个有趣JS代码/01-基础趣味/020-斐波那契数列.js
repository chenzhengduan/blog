// 🔢 生成斐波那契数列
// 学习点：递归、迭代、记忆化

// 方法1：递归（慢）
function fib1(n) {
  if (n <= 1) return n;
  return fib1(n - 1) + fib1(n - 2);
}

// 方法2：迭代（快）
function fib2(n) {
  if (n <= 1) return n;
  let a = 0, b = 1;
  for (let i = 2; i <= n; i++) {
    [a, b] = [b, a + b];
  }
  return b;
}

// 方法3：记忆化递归（优化）
function fib3(n, memo = {}) {
  if (n <= 1) return n;
  if (memo[n]) return memo[n];
  memo[n] = fib3(n - 1, memo) + fib3(n - 2, memo);
  return memo[n];
}

console.log('前10项斐波那契数列:');
for (let i = 0; i < 10; i++) {
  console.log(`第${i}项: ${fib2(i)}`);
}

console.time('递归');
fib1(35);
console.timeEnd('递归');

console.time('迭代');
fib2(35);
console.timeEnd('迭代');
