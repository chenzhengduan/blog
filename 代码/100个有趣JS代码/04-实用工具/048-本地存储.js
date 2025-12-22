// 🎯 本地存储封装
// 学习点：localStorage/sessionStorage

class Storage {
  constructor(type = 'local') {
    this.storage = type === 'local' ? localStorage : sessionStorage;
  }
  
  set(key, value, expire = null) {
    const data = {
      value,
      expire: expire ? Date.now() + expire : null
    };
    this.storage.setItem(key, JSON.stringify(data));
  }
  
  get(key, defaultValue = null) {
    const item = this.storage.getItem(key);
    if (!item) return defaultValue;
    
    try {
      const data = JSON.parse(item);
      
      // 检查是否过期
      if (data.expire && Date.now() > data.expire) {
        this.remove(key);
        return defaultValue;
      }
      
      return data.value;
    } catch {
      return defaultValue;
    }
  }
  
  remove(key) {
    this.storage.removeItem(key);
  }
  
  clear() {
    this.storage.clear();
  }
  
  has(key) {
    return this.storage.getItem(key) !== null;
  }
}

const storage = new Storage();

console.log('本地存储封装类已定义');
console.log('使用方法:');
console.log('storage.set("user", {name: "John"}, 3600000)  // 1小时过期');
console.log('storage.get("user")');
console.log('storage.remove("user")');
