// 🔒 单例模式
// 学习点：设计模式

class Singleton {
  constructor(name) {
    if (Singleton.instance) {
      return Singleton.instance;
    }
    this.name = name;
    Singleton.instance = this;
  }
  
  getName() {
    return this.name;
  }
}

const obj1 = new Singleton('第一次');
const obj2 = new Singleton('第二次');

console.log('obj1 === obj2:', obj1 === obj2); // true
console.log('obj1.getName():', obj1.getName()); // 第一次
console.log('obj2.getName():', obj2.getName()); // 第一次

console.log('💡 应用场景：全局状态管理、配置对象等');
