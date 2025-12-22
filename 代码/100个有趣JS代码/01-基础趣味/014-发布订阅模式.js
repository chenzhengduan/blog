// 📢 发布订阅模式
// 学习点：设计模式

class EventEmitter {
  constructor() {
    this.events = {};
  }
  
  on(event, callback) {
    if (!this.events[event]) {
      this.events[event] = [];
    }
    this.events[event].push(callback);
  }
  
  emit(event, ...args) {
    if (this.events[event]) {
      this.events[event].forEach(callback => callback(...args));
    }
  }
  
  off(event, callback) {
    if (this.events[event]) {
      this.events[event] = this.events[event].filter(cb => cb !== callback);
    }
  }
}

// 使用示例
const emitter = new EventEmitter();

emitter.on('greet', (name) => console.log(`你好, ${name}!`));
emitter.on('greet', (name) => console.log(`Hello, ${name}!`));

emitter.emit('greet', '张三');

console.log('💡 应用场景：组件通信、事件系统等');
