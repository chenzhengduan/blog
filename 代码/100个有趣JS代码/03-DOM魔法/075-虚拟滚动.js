// 🎬 虚拟滚动实现
// 学习点：性能优化技巧

class VirtualScroll {
  constructor(container, options = {}) {
    this.container = container;
    this.itemHeight = options.itemHeight || 50;
    this.buffer = options.buffer || 3;
    this.data = [];
    
    this.viewport = document.createElement('div');
    this.viewport.style.overflowY = 'auto';
    this.viewport.style.height = options.height || '400px';
    
    this.content = document.createElement('div');
    this.content.style.position = 'relative';
    
    this.viewport.appendChild(this.content);
    this.container.appendChild(this.viewport);
    
    this.viewport.addEventListener('scroll', () => this.render());
  }
  
  setData(data) {
    this.data = data;
    this.content.style.height = `${data.length * this.itemHeight}px`;
    this.render();
  }
  
  render() {
    const scrollTop = this.viewport.scrollTop;
    const viewportHeight = this.viewport.clientHeight;
    
    const startIndex = Math.max(0, Math.floor(scrollTop / this.itemHeight) - this.buffer);
    const endIndex = Math.min(
      this.data.length,
      Math.ceil((scrollTop + viewportHeight) / this.itemHeight) + this.buffer
    );
    
    // 清空当前内容
    this.content.innerHTML = '';
    
    // 渲染可见项
    for (let i = startIndex; i < endIndex; i++) {
      const item = document.createElement('div');
      item.style.position = 'absolute';
      item.style.top = `${i * this.itemHeight}px`;
      item.style.height = `${this.itemHeight}px`;
      item.style.width = '100%';
      item.textContent = this.data[i];
      this.content.appendChild(item);
    }
    
    console.log(`渲染 ${startIndex}-${endIndex} (共${endIndex - startIndex}项)`);
  }
}

console.log('虚拟滚动已定义');
console.log('使用示例:');
console.log(`
const container = document.getElementById('container');
const vs = new VirtualScroll(container, {
  itemHeight: 50,
  height: '400px'
});

const data = Array.from({length: 10000}, (_, i) => '项目 ' + i);
vs.setData(data);
`);

console.log('\n💡 适用于大数据列表渲染');
