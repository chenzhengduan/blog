// 🔄 无限滚动加载
// 学习点：Intersection Observer应用

class InfiniteScroll {
  constructor(container, options = {}) {
    this.container = container;
    this.options = {
      threshold: 0.1,
      rootMargin: '200px',
      loadMore: () => {},
      hasMore: () => true,
      loading: false,
      ...options
    };
    
    this.sentinel = this.createSentinel();
    this.observer = this.createObserver();
  }
  
  createSentinel() {
    const sentinel = document.createElement('div');
    sentinel.className = 'infinite-scroll-sentinel';
    sentinel.style.height = '1px';
    this.container.appendChild(sentinel);
    return sentinel;
  }
  
  createObserver() {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !this.options.loading && this.options.hasMore()) {
          this.loadMore();
        }
      });
    }, {
      root: null,
      rootMargin: this.options.rootMargin,
      threshold: this.options.threshold
    });
    
    observer.observe(this.sentinel);
    return observer;
  }
  
  async loadMore() {
    this.options.loading = true;
    this.showLoading();
    
    try {
      await this.options.loadMore();
    } catch (error) {
      console.error('加载失败:', error);
    } finally {
      this.options.loading = false;
      this.hideLoading();
    }
  }
  
  showLoading() {
    const loader = document.createElement('div');
    loader.className = 'infinite-scroll-loader';
    loader.textContent = '加载中...';
    loader.style.textAlign = 'center';
    loader.style.padding = '20px';
    this.container.insertBefore(loader, this.sentinel);
  }
  
  hideLoading() {
    const loader = this.container.querySelector('.infinite-scroll-loader');
    if (loader) {
      loader.remove();
    }
  }
  
  destroy() {
    this.observer.disconnect();
    this.sentinel.remove();
  }
}

console.log('无限滚动已定义');
console.log('使用示例:');
console.log(`
const container = document.querySelector('.list');
let page = 1;
let hasMore = true;

const infiniteScroll = new InfiniteScroll(container, {
  loadMore: async () => {
    // 模拟API请求
    const response = await fetch(\`/api/items?page=\${page}\`);
    const data = await response.json();
    
    // 添加新项目
    data.items.forEach(item => {
      const div = document.createElement('div');
      div.textContent = item.title;
      container.appendChild(div);
    });
    
    page++;
    hasMore = data.hasMore;
  },
  hasMore: () => hasMore
});
`);

console.log('\n💡 自动检测滚动到底部并加载更多内容');
