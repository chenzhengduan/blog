// 🎭 函数记忆化
// 学习点：性能优化技巧

function memoize(fn, options = {}) {
  const {
    maxSize = 100,
    getKey = (...args) => JSON.stringify(args)
  } = options;
  
  const cache = new Map();
  let stats = { hits: 0, misses: 0 };
  
  const memoized = function(...args) {
    const key = getKey(...args);
    
    if (cache.has(key)) {
      stats.hits++;
      console.log('📦 缓存命中:', key);
      return cache.get(key);
    }
    
    stats.misses++;
    console.log('🔍 计算结果:', key);
    const result = fn.apply(this, args);
    
    // LRU: 如果超出大小限制，删除最早的
    if (cache.size >= maxSize) {
      const firstKey = cache.keys().next().value;
      cache.delete(firstKey);
    }
    
    cache.set(key, result);
    return result;
  };
  
  memoized.cache = cache;
  memoized.stats = () => stats;
  memoized.clear = () => {
    cache.clear();
    stats = { hits: 0, misses: 0 };
  };
  
  return memoized;
}

// 示例：斐波那契
const fibonacci = memoize(function(n) {
  if (n <= 1) return n;
  return fibonacci(n - 1) + fibonacci(n - 2);
});

console.log('记忆化斐波那契:\n');
console.log('fib(10) =', fibonacci(10));
console.log('fib(10) =', fibonacci(10));  // 缓存命中
console.log('\n统计:', fibonacci.stats());
