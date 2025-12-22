// 🔢 质数判断与生成
// 学习点：数学算法

// 判断是否为质数
function isPrime(n) {
  if (n < 2) return false;
  if (n === 2) return true;
  if (n % 2 === 0) return false;
  
  for (let i = 3; i <= Math.sqrt(n); i += 2) {
    if (n % i === 0) return false;
  }
  return true;
}

// 埃拉托斯特尼筛法生成质数
function sieveOfEratosthenes(n) {
  const primes = new Array(n + 1).fill(true);
  primes[0] = primes[1] = false;
  
  for (let i = 2; i <= Math.sqrt(n); i++) {
    if (primes[i]) {
      for (let j = i * i; j <= n; j += i) {
        primes[j] = false;
      }
    }
  }
  
  return primes.map((isPrime, num) => isPrime ? num : null)
    .filter(num => num !== null);
}

console.log('100以内的质数:');
console.log(sieveOfEratosthenes(100));

console.log('\n判断质数:');
[2, 17, 20, 97, 100].forEach(n => {
  console.log(`${n} 是质数吗？`, isPrime(n));
});
