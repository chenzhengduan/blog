// ⏱️ 性能监控工具
// 学习点：Performance API

class PerformanceMonitor {
  constructor() {
    this.marks = new Map();
    this.measures = [];
  }
  
  mark(name) {
    const time = performance.now();
    this.marks.set(name, time);
    console.log(`📍 标记: ${name} at ${time.toFixed(2)}ms`);
  }
  
  measure(name, startMark, endMark) {
    if (!this.marks.has(startMark) || !this.marks.has(endMark)) {
      console.error('标记不存在');
      return;
    }
    
    const duration = this.marks.get(endMark) - this.marks.get(startMark);
    const measure = {
      name,
      startMark,
      endMark,
      duration
    };
    
    this.measures.push(measure);
    console.log(`⏱️ 测量: ${name} = ${duration.toFixed(2)}ms`);
    
    return duration;
  }
  
  // 自动测量函数执行时间
  async measureAsync(name, fn) {
    const startMark = `${name}-start`;
    const endMark = `${name}-end`;
    
    this.mark(startMark);
    const result = await fn();
    this.mark(endMark);
    this.measure(name, startMark, endMark);
    
    return result;
  }
  
  getReport() {
    console.log('\n📊 性能报告:');
    console.log('=' .repeat(50));
    
    this.measures.forEach(m => {
      console.log(`${m.name}: ${m.duration.toFixed(2)}ms`);
    });
    
    if (this.measures.length > 0) {
      const total = this.measures.reduce((sum, m) => sum + m.duration, 0);
      console.log('=' .repeat(50));
      console.log(`总计: ${total.toFixed(2)}ms`);
    }
  }
  
  clear() {
    this.marks.clear();
    this.measures = [];
  }
}

// 演示
const monitor = new PerformanceMonitor();

console.log('性能监控演示:\n');

monitor.mark('start');

// 模拟一些操作
setTimeout(() => {
  monitor.mark('operation1');
  monitor.measure('初始化', 'start', 'operation1');
  
  setTimeout(() => {
    monitor.mark('operation2');
    monitor.measure('数据加载', 'operation1', 'operation2');
    monitor.measure('总耗时', 'start', 'operation2');
    
    monitor.getReport();
  }, 150);
}, 100);
