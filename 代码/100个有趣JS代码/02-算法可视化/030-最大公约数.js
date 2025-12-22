// 🎯 最大公约数和最小公倍数
// 学习点：辗转相除法（欧几里得算法）

// 最大公约数 (GCD)
function gcd(a, b) {
  while (b !== 0) {
    [a, b] = [b, a % b];
  }
  return a;
}

// 递归版本
function gcdRecursive(a, b) {
  return b === 0 ? a : gcdRecursive(b, a % b);
}

// 最小公倍数 (LCM)
function lcm(a, b) {
  return (a * b) / gcd(a, b);
}

console.log('最大公约数:');
console.log('gcd(48, 18) =', gcd(48, 18));
console.log('gcd(100, 50) =', gcd(100, 50));

console.log('\n最小公倍数:');
console.log('lcm(4, 6) =', lcm(4, 6));
console.log('lcm(15, 20) =', lcm(15, 20));

console.log('\n💡 应用: 分数化简、周期问题等');
