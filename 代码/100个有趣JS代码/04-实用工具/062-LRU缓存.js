// 🎯 LRU缓存实现
// 学习点：Map数据结构与缓存策略

class LRUCache {
  constructor(capacity) {
    this.capacity = capacity;
    this.cache = new Map();
  }
  
  get(key) {
    if (!this.cache.has(key)) {
      return -1;
    }
    
    // 移到最新位置
    const value = this.cache.get(key);
    this.cache.delete(key);
    this.cache.set(key, value);
    
    return value;
  }
  
  put(key, value) {
    // 如果已存在，先删除
    if (this.cache.has(key)) {
      this.cache.delete(key);
    }
    
    // 如果已满，删除最旧的
    if (this.cache.size >= this.capacity) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
      console.log(`删除最久未使用: ${firstKey}`);
    }
    
    this.cache.set(key, value);
  }
  
  display() {
    console.log('缓存状态:', Array.from(this.cache.keys()));
  }
}

const cache = new LRUCache(3);

console.log('LRU缓存 (容量: 3)');
cache.put(1, 'a');
cache.put(2, 'b');
cache.put(3, 'c');
cache.display();

cache.get(1);  // 访问1
cache.put(4, 'd');  // 添加4，会移除2
cache.display();

console.log('\n💡 应用: 页面缓存、数据库查询缓存等');
