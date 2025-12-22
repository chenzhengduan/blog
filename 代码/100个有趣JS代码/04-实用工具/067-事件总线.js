// 🎪 事件总线 (EventBus)
// 学习点：发布订阅模式高级用法

class EventBus {
  constructor() {
    this.events = new Map();
  }
  
  on(event, handler, options = {}) {
    const { once = false, priority = 0 } = options;
    
    if (!this.events.has(event)) {
      this.events.set(event, []);
    }
    
    const wrapper = {
      handler,
      once,
      priority
    };
    
    const handlers = this.events.get(event);
    handlers.push(wrapper);
    
    // 按优先级排序
    handlers.sort((a, b) => b.priority - a.priority);
    
    // 返回取消订阅函数
    return () => this.off(event, handler);
  }
  
  once(event, handler, priority = 0) {
    return this.on(event, handler, { once: true, priority });
  }
  
  off(event, handler) {
    if (!this.events.has(event)) return;
    
    const handlers = this.events.get(event);
    const index = handlers.findIndex(h => h.handler === handler);
    
    if (index > -1) {
      handlers.splice(index, 1);
    }
  }
  
  emit(event, ...args) {
    if (!this.events.has(event)) return;
    
    const handlers = [...this.events.get(event)];
    
    handlers.forEach(({ handler, once }) => {
      handler(...args);
      if (once) {
        this.off(event, handler);
      }
    });
  }
  
  clear(event) {
    if (event) {
      this.events.delete(event);
    } else {
      this.events.clear();
    }
  }
}

const bus = new EventBus();

console.log('EventBus演示:\n');

bus.on('user:login', (user) => {
  console.log('📝 记录日志:', user);
}, { priority: 10 });

bus.on('user:login', (user) => {
  console.log('👋 欢迎:', user);
}, { priority: 5 });

bus.once('user:login', (user) => {
  console.log('🎉 首次登录奖励:', user);
});

bus.emit('user:login', 'Alice');
console.log('\n再次触发:');
bus.emit('user:login', 'Bob');
