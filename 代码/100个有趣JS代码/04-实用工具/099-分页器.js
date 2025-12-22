// 🎯 分页器组件
// 学习点：组件设计

class Paginator {
  constructor(options = {}) {
    this.currentPage = options.currentPage || 1;
    this.totalPages = options.totalPages || 1;
    this.pageSize = options.pageSize || 10;
    this.totalItems = options.totalItems || 0;
    this.visiblePages = options.visiblePages || 5;
    this.onChange = options.onChange || (() => {});
  }
  
  // 跳转到指定页
  goToPage(page) {
    if (page < 1 || page > this.totalPages || page === this.currentPage) {
      return;
    }
    
    this.currentPage = page;
    this.onChange(page);
  }
  
  // 上一页
  prev() {
    this.goToPage(this.currentPage - 1);
  }
  
  // 下一页
  next() {
    this.goToPage(this.currentPage + 1);
  }
  
  // 第一页
  first() {
    this.goToPage(1);
  }
  
  // 最后一页
  last() {
    this.goToPage(this.totalPages);
  }
  
  // 获取可见页码
  getVisiblePages() {
    const pages = [];
    const half = Math.floor(this.visiblePages / 2);
    
    let start = Math.max(1, this.currentPage - half);
    let end = Math.min(this.totalPages, start + this.visiblePages - 1);
    
    if (end - start + 1 < this.visiblePages) {
      start = Math.max(1, end - this.visiblePages + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }
  
  // 获取状态
  getState() {
    return {
      currentPage: this.currentPage,
      totalPages: this.totalPages,
      pageSize: this.pageSize,
      totalItems: this.totalItems,
      hasNext: this.currentPage < this.totalPages,
      hasPrev: this.currentPage > 1,
      visiblePages: this.getVisiblePages(),
      startItem: (this.currentPage - 1) * this.pageSize + 1,
      endItem: Math.min(this.currentPage * this.pageSize, this.totalItems)
    };
  }
  
  // 渲染为HTML
  render(container) {
    const state = this.getState();
    
    let html = '<div class="paginator">';
    
    // 上一页
    html += `<button ${!state.hasPrev ? 'disabled' : ''} 
             onclick="paginator.prev()">上一页</button>`;
    
    // 第一页
    if (state.visiblePages[0] > 1) {
      html += `<button onclick="paginator.goToPage(1)">1</button>`;
      if (state.visiblePages[0] > 2) {
        html += '<span>...</span>';
      }
    }
    
    // 页码
    state.visiblePages.forEach(page => {
      html += `<button ${page === this.currentPage ? 'class="active"' : ''}
               onclick="paginator.goToPage(${page})">${page}</button>`;
    });
    
    // 最后一页
    if (state.visiblePages[state.visiblePages.length - 1] < this.totalPages) {
      if (state.visiblePages[state.visiblePages.length - 1] < this.totalPages - 1) {
        html += '<span>...</span>';
      }
      html += `<button onclick="paginator.goToPage(${this.totalPages})">${this.totalPages}</button>`;
    }
    
    // 下一页
    html += `<button ${!state.hasNext ? 'disabled' : ''}
             onclick="paginator.next()">下一页</button>`;
    
    // 信息
    html += `<span class="info">
      第 ${state.startItem}-${state.endItem} 项，共 ${this.totalItems} 项
    </span>`;
    
    html += '</div>';
    
    if (container) {
      container.innerHTML = html;
    }
    
    return html;
  }
}

console.log('分页器组件已定义');
console.log('使用示例:\n');

const paginator = new Paginator({
  currentPage: 1,
  totalPages: 20,
  totalItems: 195,
  pageSize: 10,
  visiblePages: 5,
  onChange: (page) => {
    console.log(`跳转到第 ${page} 页`);
  }
});

console.log('当前状态:');
console.log(paginator.getState());

console.log('\n可见页码:', paginator.getVisiblePages());

console.log('\n操作:');
paginator.next();
console.log('下一页:', paginator.currentPage);

paginator.goToPage(10);
console.log('跳转到10:', paginator.currentPage);
