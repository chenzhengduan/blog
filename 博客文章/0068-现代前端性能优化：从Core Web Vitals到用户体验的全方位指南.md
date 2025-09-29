# 现代前端性能优化：从Core Web Vitals到用户体验的全方位指南

> 性能优化不是一次性的工作，而是持续的过程。本文将深入探讨现代前端性能优化的核心指标、技术手段和最佳实践。

## 引言

在当今的互联网环境中，页面性能直接影响用户体验和业务转化率。Google提出的Core Web Vitals已经成为衡量网站性能的重要标准。本文将从理论基础到实践应用，全面解析现代前端性能优化的各个方面。

## 一、Core Web Vitals深度解析

### 1.1 LCP (Largest Contentful Paint)

**定义：** 页面主要内容加载完成的时间

**目标：** ≤ 2.5秒

**优化策略：**
```javascript
// 监控LCP
const observer = new PerformanceObserver((list) => {
  const entries = list.getEntries();
  const lastEntry = entries[entries.length - 1];
  console.log('LCP:', lastEntry.startTime);
});

observer.observe({ entryTypes: ['largest-contentful-paint'] });

// 优化图片加载
function optimizeImageLoading() {
  // 1. 使用适当的图片格式
  const picture = document.createElement('picture');

  // WebP格式优先
  const sourceWebP = document.createElement('source');
  sourceWebP.srcset = 'image.webp';
  sourceWebP.type = 'image/webp';

  // JPEG备用
  const sourceJPEG = document.createElement('source');
  sourceJPEG.srcset = 'image.jpg';
  sourceJPEG.type = 'image/jpeg';

  // 默认图片
  const img = document.createElement('img');
  img.src = 'image.jpg';
  img.alt = 'Optimized image';
  img.loading = 'lazy'; // 懒加载

  picture.appendChild(sourceWebP);
  picture.appendChild(sourceJPEG);
  picture.appendChild(img);

  return picture;
}

// 预加载关键资源
function preloadCriticalResources() {
  const resources = [
    { href: '/styles/main.css', as: 'style' },
    { href: '/scripts/main.js', as: 'script' },
    { href: '/fonts/main.woff2', as: 'font', crossOrigin: 'anonymous' }
  ];

  resources.forEach(resource => {
    const link = document.createElement('link');
    link.rel = 'preload';
    link.href = resource.href;
    link.as = resource.as;
    if (resource.crossOrigin) {
      link.crossOrigin = resource.crossOrigin;
    }
    document.head.appendChild(link);
  });
}
```

### 1.2 FID (First Input Delay)

**定义：** 用户首次交互到浏览器响应的时间

**目标：** ≤ 100ms

**优化策略：**
```javascript
// 分割长任务
function splitLongTasks() {
  // 使用requestIdleCallback处理非关键任务
  function scheduleWork(work) {
    if ('requestIdleCallback' in window) {
      requestIdleCallback(work, { timeout: 2000 });
    } else {
      setTimeout(work, 0);
    }
  }

  // 任务分解示例
  function processLargeDataset(data) {
    const chunkSize = 1000;
    let index = 0;

    function processChunk() {
      const chunk = data.slice(index, index + chunkSize);

      // 处理数据块
      chunk.forEach(item => {
        // 处理逻辑
      });

      index += chunkSize;

      if (index < data.length) {
        scheduleWork(processChunk);
      }
    }

    scheduleWork(processChunk);
  }
}

// 优化事件监听器
function optimizeEventListeners() {
  // 使用事件委托
  document.addEventListener('click', (event) => {
    const target = event.target;

    if (target.matches('.button')) {
      handleButtonClick(target);
    } else if (target.matches('.link')) {
      handleLinkClick(target);
    }
  });

  // 防抖处理
  function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

  // 节流处理
  function throttle(func, limit) {
    let inThrottle;
    return function() {
      const args = arguments;
      const context = this;
      if (!inThrottle) {
        func.apply(context, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  }
}
```

### 1.3 CLS (Cumulative Layout Shift)

**定义：** 页面元素在加载过程中发生的意外位移

**目标：** ≤ 0.1

**优化策略：**
```javascript
// 为图片和广告预留空间
function reserveSpaceForDynamicContent() {
  // 图片占位符
  const imagePlaceholder = {
    width: '300px',
    height: '200px',
    backgroundColor: '#f0f0f0',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#666'
  };

  // 应用样式
  const style = document.createElement('style');
  style.textContent = `
    .image-placeholder {
      width: ${imagePlaceholder.width};
      height: ${imagePlaceholder.height};
      background-color: ${imagePlaceholder.backgroundColor};
      display: ${imagePlaceholder.display};
      align-items: ${imagePlaceholder.alignItems};
      justify-content: ${imagePlaceholder.justifyContent};
      color: ${imagePlaceholder.color};
    }
  `;
  document.head.appendChild(style);
}

// 优化字体加载
function optimizeFontLoading() {
  // 字体预加载
  const fontLink = document.createElement('link');
  fontLink.rel = 'preload';
  fontLink.href = '/fonts/main.woff2';
  fontLink.as = 'font';
  fontLink.crossOrigin = 'anonymous';
  document.head.appendChild(fontLink);

  // 字体加载策略
  const fontFace = new FontFace(
    'MainFont',
    'url(/fonts/main.woff2)',
    { display: 'swap' }
  );

  fontFace.load().then((loadedFont) => {
    document.fonts.add(loadedFont);
  }).catch((error) => {
    console.error('Font loading failed:', error);
  });
}

// 监控CLS
const clsObserver = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    if (entry.hadRecentInput) {
      continue;
    }
    console.log('CLS entry:', entry);
  }
});

clsObserver.observe({ type: 'layout-shift', buffered: true });
```

## 二、加载性能优化

### 2.1 资源加载优化

```javascript
// 智能资源加载
class SmartResourceLoader {
  constructor() {
    this.loadedResources = new Set();
    this.loadingResources = new Map();
  }

  async loadResource(url, options = {}) {
    if (this.loadedResources.has(url)) {
      return Promise.resolve();
    }

    if (this.loadingResources.has(url)) {
      return this.loadingResources.get(url);
    }

    const loadPromise = this._loadResource(url, options);
    this.loadingResources.set(url, loadPromise);

    try {
      await loadPromise;
      this.loadedResources.add(url);
      return loadPromise;
    } finally {
      this.loadingResources.delete(url);
    }
  }

  async _loadResource(url, options) {
    const { priority = 'auto', fetchPriority = 'auto' } = options;

    if (url.endsWith('.js')) {
      return this._loadScript(url, { fetchPriority });
    } else if (url.endsWith('.css')) {
      return this._loadStyle(url, { fetchPriority });
    } else if (url.match(/\.(png|jpg|jpeg|webp|gif)$/)) {
      return this._loadImage(url, { priority });
    } else if (url.match(/\.(woff|woff2|ttf|otf)$/)) {
      return this._loadFont(url);
    }
  }

  _loadScript(url, options) {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      if (options.fetchPriority) {
        script.fetchPriority = options.fetchPriority;
      }
      script.onload = resolve;
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }

  _loadStyle(url, options) {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = url;
      if (options.fetchPriority) {
        link.fetchPriority = options.fetchPriority;
      }
      link.onload = resolve;
      link.onerror = reject;
      document.head.appendChild(link);
    });
  }

  _loadImage(url, options) {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = url;
      if (options.priority === 'high') {
        img.fetchPriority = 'high';
      }
      img.onload = resolve;
      img.onerror = reject;
    });
  }

  _loadFont(url) {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.href = url;
      link.as = 'font';
      link.crossOrigin = 'anonymous';
      link.onload = resolve;
      link.onerror = reject;
      document.head.appendChild(link);
    });
  }
}

// 使用示例
const resourceLoader = new SmartResourceLoader();

// 关键资源高优先级加载
resourceLoader.loadResource('/styles/main.css', {
  fetchPriority: 'high'
});

// 非关键资源低优先级加载
resourceLoader.loadResource('/scripts/analytics.js', {
  fetchPriority: 'low'
});
```

### 2.2 代码分割与懒加载

```javascript
// 路由级别的代码分割
const routes = [
  {
    path: '/',
    component: () => import('./views/Home.vue')
  },
  {
    path: '/about',
    component: () => import('./views/About.vue')
  },
  {
    path: '/contact',
    component: () => import('./views/Contact.vue')
  }
];

// 组件级别的懒加载
const LazyComponent = React.lazy(() => import('./LazyComponent'));

// 条件加载
class ConditionalLoader {
  constructor() {
    this.loadedModules = new Map();
  }

  async loadModule(name, loadFunction) {
    if (this.loadedModules.has(name)) {
      return this.loadedModules.get(name);
    }

    const module = await loadFunction();
    this.loadedModules.set(name, module);
    return module;
  }
}

// 使用Intersection Observer实现懒加载
class LazyLoader {
  constructor() {
    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            this.loadElement(entry.target);
            this.observer.unobserve(entry.target);
          }
        });
      },
      {
        rootMargin: '50px 0px',
        threshold: 0.1
      }
    );
  }

  observe(element) {
    this.observer.observe(element);
  }

  loadElement(element) {
    const src = element.dataset.src;
    if (src) {
      if (element.tagName === 'IMG') {
        element.src = src;
      } else if (element.tagName === 'IFRAME') {
        element.src = src;
      }
    }
  }
}

// 使用示例
const lazyLoader = new LazyLoader();
document.querySelectorAll('[data-src]').forEach(element => {
  lazyLoader.observe(element);
});
```

### 2.3 缓存策略优化

```javascript
// 多级缓存系统
class AdvancedCacheSystem {
  constructor() {
    this.memoryCache = new Map();
    this.sessionStorage = new SessionStorageCache();
    this.localStorage = new LocalStorageCache();
    this.indexedDB = new IndexedDBCache();
  }

  async get(key, options = {}) {
    const { useMemory = true, useSession = true, useLocal = true, useIndexedDB = true } = options;

    // 1. 检查内存缓存
    if (useMemory && this.memoryCache.has(key)) {
      const cached = this.memoryCache.get(key);
      if (!this.isExpired(cached)) {
        return cached.value;
      }
    }

    // 2. 检查sessionStorage
    if (useSession) {
      const sessionData = await this.sessionStorage.get(key);
      if (sessionData && !this.isExpired(sessionData)) {
        if (useMemory) {
          this.memoryCache.set(key, sessionData);
        }
        return sessionData.value;
      }
    }

    // 3. 检查localStorage
    if (useLocal) {
      const localData = await this.localStorage.get(key);
      if (localData && !this.isExpired(localData)) {
        if (useMemory) {
          this.memoryCache.set(key, localData);
        }
        return localData.value;
      }
    }

    // 4. 检查IndexedDB
    if (useIndexedDB) {
      const indexedData = await this.indexedDB.get(key);
      if (indexedData && !this.isExpired(indexedData)) {
        if (useMemory) {
          this.memoryCache.set(key, indexedData);
        }
        return indexedData.value;
      }
    }

    return null;
  }

  async set(key, value, options = {}) {
    const { ttl = 3600000, useMemory = true, useSession = true, useLocal = true, useIndexedDB = true } = options;

    const cacheEntry = {
      value,
      timestamp: Date.now(),
      ttl
    };

    // 存储到内存缓存
    if (useMemory) {
      this.memoryCache.set(key, cacheEntry);
    }

    // 存储到sessionStorage
    if (useSession) {
      await this.sessionStorage.set(key, cacheEntry);
    }

    // 存储到localStorage
    if (useLocal) {
      await this.localStorage.set(key, cacheEntry);
    }

    // 存储到IndexedDB
    if (useIndexedDB) {
      await this.indexedDB.set(key, cacheEntry);
    }
  }

  isExpired(cacheEntry) {
    return Date.now() - cacheEntry.timestamp > cacheEntry.ttl;
  }
}

// 缓存装饰器
function withCache(cacheKey, options = {}) {
  return function(target, propertyKey, descriptor) {
    const originalMethod = descriptor.value;
    const cacheSystem = new AdvancedCacheSystem();

    descriptor.value = async function(...args) {
      const key = `${cacheKey}-${JSON.stringify(args)}`;

      let result = await cacheSystem.get(key, options);

      if (result === null) {
        result = await originalMethod.apply(this, args);
        await cacheSystem.set(key, result, options);
      }

      return result;
    };

    return descriptor;
  };
}

// 使用示例
class DataService {
  @withCache('user-data', { ttl: 300000 })
  async fetchUserData(userId) {
    const response = await fetch(`/api/users/${userId}`);
    return response.json();
  }

  @withCache('products', { ttl: 600000 })
  async fetchProducts() {
    const response = await fetch('/api/products');
    return response.json();
  }
}
```

## 三、运行时性能优化

### 3.1 内存管理优化

```javascript
// 内存泄漏检测和预防
class MemoryManager {
  constructor() {
    this.observers = new Map();
    this.weakReferences = new Map();
    this.cleanupCallbacks = new Set();
  }

  // 创建弱引用
  createWeakReference(key, object) {
    const weakRef = new WeakRef(object);
    this.weakReferences.set(key, weakRef);
    return weakRef;
  }

  // 获取弱引用
  getWeakReference(key) {
    const weakRef = this.weakReferences.get(key);
    return weakRef ? weakRef.deref() : null;
  }

  // 监控对象生命周期
  observeObject(object, callback) {
    const observer = new FinalizationRegistry((heldValue) => {
      callback(heldValue);
      this.observers.delete(heldValue);
    });

    const id = Symbol('observer');
    observer.register(object, id);
    this.observers.set(id, { observer, object });

    return id;
  }

  // 清理资源
  cleanup() {
    this.cleanupCallbacks.forEach(callback => callback());
    this.cleanupCallbacks.clear();

    this.observers.forEach(({ observer }, id) => {
      observer.unregister(id);
    });
    this.observers.clear();
  }

  // 添加清理回调
  addCleanupCallback(callback) {
    this.cleanupCallbacks.add(callback);
    return () => this.cleanupCallbacks.delete(callback);
  }
}

// 使用示例
const memoryManager = new MemoryManager();

// 监控DOM元素生命周期
function monitorElement(element, cleanupCallback) {
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.type === 'childList') {
        mutation.removedNodes.forEach((node) => {
          if (node === element) {
            cleanupCallback();
            observer.disconnect();
          }
        });
      }
    });
  });

  observer.observe(document.body, { childList: true, subtree: true });

  return observer;
}
```

### 3.2 事件循环优化

```javascript
// 事件循环优化工具
class EventLoopOptimizer {
  constructor() {
    this.taskQueue = [];
    this.isProcessing = false;
    this.frameBudget = 16; // 60fps
  }

  // 使用requestIdleCallback处理非关键任务
  scheduleIdleTask(task, timeout = 2000) {
    if ('requestIdleCallback' in window) {
      return requestIdleCallback(task, { timeout });
    } else {
      return setTimeout(task, 0);
    }
  }

  // 使用requestAnimationFrame处理视觉任务
  scheduleVisualTask(task) {
    return requestAnimationFrame(task);
  }

  // 任务分片处理
  async processLargeTask(data, chunkSize = 1000) {
    return new Promise((resolve) => {
      let index = 0;

      const processChunk = () => {
        const startTime = performance.now();

        while (index < data.length && performance.now() - startTime < this.frameBudget) {
          const chunk = data.slice(index, index + chunkSize);
          this.processDataChunk(chunk);
          index += chunkSize;
        }

        if (index < data.length) {
          this.scheduleVisualTask(processChunk);
        } else {
          resolve();
        }
      };

      this.scheduleVisualTask(processChunk);
    });
  }

  processDataChunk(chunk) {
    // 处理数据块的具体逻辑
    chunk.forEach(item => {
      // 处理单个数据项
    });
  }

  // 优先级任务调度
  scheduleTask(task, priority = 'normal') {
    const taskWrapper = {
      task,
      priority,
      timestamp: Date.now()
    };

    this.taskQueue.push(taskWrapper);
    this.taskQueue.sort((a, b) => {
      const priorityOrder = { 'high': 0, 'normal': 1, 'low': 2 };
      return priorityOrder[a.priority] - priorityOrder[b.priority];
    });

    if (!this.isProcessing) {
      this.processTasks();
    }
  }

  async processTasks() {
    this.isProcessing = true;

    while (this.taskQueue.length > 0) {
      const taskWrapper = this.taskQueue.shift();

      try {
        await taskWrapper.task();
      } catch (error) {
        console.error('Task execution failed:', error);
      }
    }

    this.isProcessing = false;
  }
}

// 使用示例
const eventLoopOptimizer = new EventLoopOptimizer();

// 处理大量数据
async function processLargeDataset(data) {
  await eventLoopOptimizer.processLargeTask(data, 500);
}

// 调度不同优先级的任务
eventLoopOptimizer.scheduleTask(
  () => console.log('High priority task'),
  'high'
);

eventLoopOptimizer.scheduleTask(
  () => console.log('Normal priority task'),
  'normal'
);

eventLoopOptimizer.scheduleTask(
  () => console.log('Low priority task'),
  'low'
);
```

### 3.3 DOM操作优化

```javascript
// DOM操作优化工具
class DOMOptimizer {
  constructor() {
    this.fragmentCache = new Map();
    this.templateCache = new Map();
  }

  // 批量DOM操作
  batchUpdate(element, updates) {
    const fragment = document.createDocumentFragment();

    updates.forEach(update => {
      const { type, data } = update;

      switch (type) {
        case 'createElement':
          const newElement = document.createElement(data.tagName);
          if (data.attributes) {
            Object.entries(data.attributes).forEach(([key, value]) => {
              newElement.setAttribute(key, value);
            });
          }
          if (data.textContent) {
            newElement.textContent = data.textContent;
          }
          fragment.appendChild(newElement);
          break;

        case 'updateText':
          const textNode = document.createTextNode(data);
          fragment.appendChild(textNode);
          break;
      }
    });

    element.appendChild(fragment);
  }

  // 虚拟滚动实现
  createVirtualScroll(container, items, itemHeight = 50) {
    const viewportHeight = container.clientHeight;
    const totalHeight = items.length * itemHeight;
    const visibleCount = Math.ceil(viewportHeight / itemHeight);
    const startIndex = Math.max(0, Math.floor(container.scrollTop / itemHeight));
    const endIndex = Math.min(items.length, startIndex + visibleCount + 2);

    // 设置容器高度
    container.style.height = `${viewportHeight}px`;
    container.style.overflow = 'auto';

    // 创建内容容器
    const contentContainer = document.createElement('div');
    contentContainer.style.height = `${totalHeight}px`;
    contentContainer.style.position = 'relative';

    // 渲染可见项目
    for (let i = startIndex; i < endIndex; i++) {
      const item = items[i];
      const itemElement = document.createElement('div');
      itemElement.style.position = 'absolute';
      itemElement.style.top = `${i * itemHeight}px`;
      itemElement.style.height = `${itemHeight}px`;
      itemElement.textContent = item.text;
      contentContainer.appendChild(itemElement);
    }

    container.appendChild(contentContainer);

    // 滚动事件处理
    container.addEventListener('scroll', () => {
      const newStartIndex = Math.max(0, Math.floor(container.scrollTop / itemHeight));
      const newEndIndex = Math.min(items.length, newStartIndex + visibleCount + 2);

      if (newStartIndex !== startIndex || newEndIndex !== endIndex) {
        this.updateVirtualScroll(container, items, newStartIndex, newEndIndex, itemHeight);
      }
    });
  }

  updateVirtualScroll(container, items, startIndex, endIndex, itemHeight) {
    const contentContainer = container.firstChild;
    contentContainer.innerHTML = '';

    for (let i = startIndex; i < endIndex; i++) {
      const item = items[i];
      const itemElement = document.createElement('div');
      itemElement.style.position = 'absolute';
      itemElement.style.top = `${i * itemHeight}px`;
      itemElement.style.height = `${itemHeight}px`;
      itemElement.textContent = item.text;
      contentContainer.appendChild(itemElement);
    }
  }

  // 模板缓存
  getTemplate(templateId) {
    if (this.templateCache.has(templateId)) {
      return this.templateCache.get(templateId);
    }

    const template = document.getElementById(templateId);
    if (template) {
      const clone = template.content.cloneNode(true);
      this.templateCache.set(templateId, clone);
      return clone;
    }

    return null;
  }

  // 事件委托优化
  setupEventDelegate(container, selector, eventType, handler) {
    container.addEventListener(eventType, (event) => {
      const target = event.target;
      const matchingElement = target.closest(selector);

      if (matchingElement && container.contains(matchingElement)) {
        handler.call(matchingElement, event);
      }
    });
  }
}

// 使用示例
const domOptimizer = new DOMOptimizer();

// 批量DOM更新
const updates = [
  {
    type: 'createElement',
    data: {
      tagName: 'div',
      attributes: { class: 'item' },
      textContent: 'Item 1'
    }
  },
  {
    type: 'createElement',
    data: {
      tagName: 'div',
      attributes: { class: 'item' },
      textContent: 'Item 2'
    }
  }
];

domOptimizer.batchUpdate(container, updates);

// 虚拟滚动
const items = Array.from({ length: 10000 }, (_, i) => ({
  text: `Item ${i + 1}`
}));

domOptimizer.createVirtualScroll(container, items);

// 事件委托
domOptimizer.setupEventDelegate(
  document.body,
  '.button',
  'click',
  function(event) {
    console.log('Button clicked:', this.textContent);
  }
);
```

## 四、网络性能优化

### 4.1 网络请求优化

```javascript
// 网络请求优化工具
class NetworkOptimizer {
  constructor() {
    this.requestCache = new Map();
    this.requestQueue = new Map();
    this.connectionManager = new ConnectionManager();
  }

  // 智能重试机制
  async fetchWithRetry(url, options = {}) {
    const {
      maxRetries = 3,
      retryDelay = 1000,
      retryBackoff = 2,
      timeout = 10000
    } = options;

    let lastError;

    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), timeout);

        const response = await fetch(url, {
          ...options,
          signal: controller.signal
        });

        clearTimeout(timeoutId);

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
      } catch (error) {
        lastError = error;

        if (attempt < maxRetries) {
          const delay = retryDelay * Math.pow(retryBackoff, attempt);
          await this.delay(delay);
        }
      }
    }

    throw lastError;
  }

  // 请求去重
  async fetchDedupe(url, options = {}) {
    const cacheKey = `${url}-${JSON.stringify(options)}`;

    if (this.requestQueue.has(cacheKey)) {
      return this.requestQueue.get(cacheKey);
    }

    if (this.requestCache.has(cacheKey)) {
      return this.requestCache.get(cacheKey);
    }

    const requestPromise = this.fetchWithRetry(url, options);
    this.requestQueue.set(cacheKey, requestPromise);

    try {
      const result = await requestPromise;
      this.requestCache.set(cacheKey, result);
      return result;
    } finally {
      this.requestQueue.delete(cacheKey);
    }
  }

  // 批量请求
  async fetchBatch(requests, concurrency = 3) {
    const results = [];
    const chunks = [];

    // 分块处理
    for (let i = 0; i < requests.length; i += concurrency) {
      chunks.push(requests.slice(i, i + concurrency));
    }

    for (const chunk of chunks) {
      const chunkResults = await Promise.all(
        chunk.map(request => this.fetchDedupe(request.url, request.options))
      );
      results.push(...chunkResults);
    }

    return results;
  }

  // 预测性加载
  async prefetchResources(resources) {
    const connection = this.connectionManager.getConnection();

    if (connection.effectiveType === 'slow-2g' || connection.effectiveType === '2g') {
      console.log('Skipping prefetch on slow connection');
      return;
    }

    const prefetchPromises = resources.map(resource => {
      const link = document.createElement('link');
      link.rel = 'prefetch';
      link.href = resource.url;
      if (resource.as) {
        link.as = resource.as;
      }
      document.head.appendChild(link);

      return new Promise((resolve, reject) => {
        link.onload = resolve;
        link.onerror = reject;
      });
    });

    try {
      await Promise.all(prefetchPromises);
    } catch (error) {
      console.error('Prefetch failed:', error);
    }
  }

  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

// 连接管理器
class ConnectionManager {
  constructor() {
    this.connection = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
    this.listeners = new Set();
  }

  getConnection() {
    return this.connection;
  }

  addListener(callback) {
    if (this.connection) {
      this.connection.addEventListener('change', callback);
      this.listeners.add(callback);
    }
  }

  removeListener(callback) {
    if (this.connection) {
      this.connection.removeEventListener('change', callback);
      this.listeners.delete(callback);
    }
  }

  isSlowConnection() {
    if (!this.connection) return false;

    return this.connection.effectiveType === 'slow-2g' ||
           this.connection.effectiveType === '2g' ||
           (this.connection.saveData && this.connection.saveData === true);
  }
}

// 使用示例
const networkOptimizer = new NetworkOptimizer();

// 智能请求
async function fetchUserData(userId) {
  return await networkOptimizer.fetchDedupe(`/api/users/${userId}`);
}

// 批量请求
async function fetchMultipleData() {
  const requests = [
    { url: '/api/users' },
    { url: '/api/products' },
    { url: '/api/orders' }
  ];

  return await networkOptimizer.fetchBatch(requests, 2);
}

// 预测性加载
async function prefetchNextPageResources() {
  const resources = [
    { url: '/api/next-page-data', as: 'fetch' },
    { url: '/images/next-page-hero.jpg', as: 'image' },
    { url: '/scripts/next-page.js', as: 'script' }
  ];

  await networkOptimizer.prefetchResources(resources);
}
```

### 4.2 Service Worker缓存策略

```javascript
// Service Worker缓存策略
class ServiceWorkerCache {
  constructor() {
    this.cacheName = 'app-cache-v1';
    this.staticCache = 'static-cache-v1';
    this.dynamicCache = 'dynamic-cache-v1';
  }

  // 安装事件
  async install(event) {
    const cache = await caches.open(this.staticCache);
    const urlsToCache = [
      '/',
      '/index.html',
      '/styles/main.css',
      '/scripts/main.js',
      '/images/logo.png'
    ];

    await cache.addAll(urlsToCache);
  }

  // 激活事件
  async activate(event) {
    const cacheKeys = await caches.keys();
    const oldCaches = cacheKeys.filter(key =>
      key !== this.staticCache && key !== this.dynamicCache
    );

    await Promise.all(oldCaches.map(key => caches.delete(key)));
  }

  // 拦截请求
  async fetch(event) {
    const { request } = event;

    // 尝试从缓存获取
    const cachedResponse = await this.matchFromCache(request);
    if (cachedResponse) {
      return cachedResponse;
    }

    // 从网络获取
    try {
      const networkResponse = await fetch(request);

      // 缓存成功的响应
      if (networkResponse.ok) {
        const cache = await caches.open(this.dynamicCache);
        cache.put(request, networkResponse.clone());
      }

      return networkResponse;
    } catch (error) {
      // 网络失败，返回离线页面
      return this.getOfflinePage();
    }
  }

  async matchFromCache(request) {
    // 静态资源缓存策略
    if (this.isStaticResource(request.url)) {
      return caches.match(request);
    }

    // API请求缓存策略
    if (this.isAPIRequest(request.url)) {
      return this.matchAPIFromCache(request);
    }

    // 动态资源缓存策略
    return caches.match(request);
  }

  isStaticResource(url) {
    return url.match(/\.(css|js|png|jpg|jpeg|gif|svg|woff|woff2|ttf|otf)$/);
  }

  isAPIRequest(url) {
    return url.includes('/api/');
  }

  async matchAPIFromCache(request) {
    const cache = await caches.open(this.dynamicCache);
    const cachedResponse = await cache.match(request);

    if (cachedResponse) {
      // 检查缓存是否过期
      const dateHeader = cachedResponse.headers.get('date');
      if (dateHeader) {
        const cacheDate = new Date(dateHeader);
        const now = new Date();
        const cacheAge = now - cacheDate;
        const maxAge = 5 * 60 * 1000; // 5分钟

        if (cacheAge < maxAge) {
          return cachedResponse;
        }
      }
    }

    return null;
  }

  async getOfflinePage() {
    const cache = await caches.open(this.staticCache);
    return cache.match('/offline.html');
  }
}

// Service Worker注册
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(registration => {
        console.log('ServiceWorker registration successful');
      })
      .catch(error => {
        console.log('ServiceWorker registration failed:', error);
      });
  });
}
```

## 五、性能监控与分析

### 5.1 性能数据收集

```javascript
// 性能监控工具
class PerformanceMonitor {
  constructor() {
    this.metrics = new Map();
    this.observers = new Set();
    this.isRecording = false;
    this.startTime = null;
  }

  // 开始性能监控
  startMonitoring() {
    this.isRecording = true;
    this.startTime = performance.now();

    // 监控关键指标
    this.observeWebVitals();
    this.observeResourceTiming();
    this.observeUserTimings();
    this.observeLongTasks();
  }

  // 停止性能监控
  stopMonitoring() {
    this.isRecording = false;
    this.generateReport();
  }

  // 监控Web Vitals
  observeWebVitals() {
    // LCP监控
    const lcpObserver = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      this.recordMetric('lcp', lastEntry.startTime);
    });

    lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });

    // FID监控
    const fidObserver = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        this.recordMetric('fid', entry.processingStart - entry.startTime);
      });
    });

    fidObserver.observe({ entryTypes: ['first-input'] });

    // CLS监控
    const clsObserver = new PerformanceObserver((list) => {
      let clsValue = 0;
      list.getEntries().forEach(entry => {
        if (!entry.hadRecentInput) {
          clsValue += entry.value;
        }
      });
      this.recordMetric('cls', clsValue);
    });

    clsObserver.observe({ entryTypes: ['layout-shift'] });
  }

  // 监控资源加载时间
  observeResourceTiming() {
    const resourceObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        if (entry.initiatorType === 'script') {
          this.recordMetric('script_load_time', entry.duration);
        } else if (entry.initiatorType === 'link' && entry.name.includes('.css')) {
          this.recordMetric('css_load_time', entry.duration);
        } else if (entry.initiatorType === 'img') {
          this.recordMetric('image_load_time', entry.duration);
        }
      });
    });

    resourceObserver.observe({ entryTypes: ['resource'] });
  }

  // 监控用户计时
  observeUserTimings() {
    const userTimingObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        this.recordMetric(`user_timing_${entry.name}`, entry.duration);
      });
    });

    userTimingObserver.observe({ entryTypes: ['measure'] });
  }

  // 监控长任务
  observeLongTasks() {
    const longTaskObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        this.recordMetric('long_task_duration', entry.duration);
        this.recordMetric('long_task_count', 1, 'increment');
      });
    });

    if ('PerformanceLongTaskTiming' in window) {
      longTaskObserver.observe({ entryTypes: ['longtask'] });
    }
  }

  // 记录性能指标
  recordMetric(name, value, type = 'value') {
    if (!this.metrics.has(name)) {
      this.metrics.set(name, {
        values: [],
        count: 0,
        sum: 0,
        min: Infinity,
        max: -Infinity
      });
    }

    const metric = this.metrics.get(name);

    if (type === 'increment') {
      metric.count += value;
    } else {
      metric.values.push(value);
      metric.count++;
      metric.sum += value;
      metric.min = Math.min(metric.min, value);
      metric.max = Math.max(metric.max, value);
    }

    // 通知观察者
    this.notifyObservers(name, value);
  }

  // 添加观察者
  addObserver(callback) {
    this.observers.add(callback);
  }

  // 移除观察者
  removeObserver(callback) {
    this.observers.delete(callback);
  }

  // 通知观察者
  notifyObservers(name, value) {
    this.observers.forEach(callback => {
      callback(name, value);
    });
  }

  // 生成性能报告
  generateReport() {
    const report = {
      timestamp: new Date().toISOString(),
      duration: this.isRecording ? performance.now() - this.startTime : null,
      metrics: {}
    };

    this.metrics.forEach((metric, name) => {
      report.metrics[name] = {
        count: metric.count,
        sum: metric.sum,
        average: metric.count > 0 ? metric.sum / metric.count : 0,
        min: metric.min,
        max: metric.max,
        values: metric.values
      };
    });

    console.log('Performance Report:', report);
    return report;
  }

  // 导出性能数据
  exportData(format = 'json') {
    const report = this.generateReport();

    if (format === 'json') {
      return JSON.stringify(report, null, 2);
    } else if (format === 'csv') {
      return this.exportToCSV(report);
    }
  }

  exportToCSV(report) {
    const lines = ['Metric,Count,Sum,Average,Min,Max'];

    Object.entries(report.metrics).forEach(([name, metric]) => {
      lines.push(`${name},${metric.count},${metric.sum},${metric.average},${metric.min},${metric.max}`);
    });

    return lines.join('\n');
  }
}

// 使用示例
const performanceMonitor = new PerformanceMonitor();

// 开始监控
performanceMonitor.startMonitoring();

// 添加观察者
performanceMonitor.addObserver((name, value) => {
  console.log(`Metric ${name}: ${value}`);
});

// 自定义性能标记
function markUserAction(actionName) {
  performance.mark(`${actionName}_start`);
}

function measureUserAction(actionName) {
  performance.mark(`${actionName}_end`);
  performance.measure(actionName, `${actionName}_start`, `${actionName}_end`);
}

// 使用示例
markUserAction('button_click');
// ... 执行一些操作
measureUserAction('button_click');
```

### 5.2 性能分析工具

```javascript
// 性能分析工具
class PerformanceAnalyzer {
  constructor() {
    this.frameData = [];
    this.memoryData = [];
    this.networkData = [];
  }

  // 帧率分析
  analyzeFrameRate() {
    let lastTime = performance.now();
    let frames = 0;

    const measureFrameRate = () => {
      const currentTime = performance.now();
      frames++;

      if (currentTime - lastTime >= 1000) {
        const fps = Math.round((frames * 1000) / (currentTime - lastTime));
        this.frameData.push({
          timestamp: currentTime,
          fps: fps
        });

        frames = 0;
        lastTime = currentTime;
      }

      if (this.isAnalyzing) {
        requestAnimationFrame(measureFrameRate);
      }
    };

    this.isAnalyzing = true;
    requestAnimationFrame(measureFrameRate);
  }

  // 内存使用分析
  analyzeMemoryUsage() {
    const measureMemory = () => {
      if (performance.memory) {
        this.memoryData.push({
          timestamp: performance.now(),
          usedJSHeapSize: performance.memory.usedJSHeapSize,
          totalJSHeapSize: performance.memory.totalJSHeapSize,
          jsHeapSizeLimit: performance.memory.jsHeapSizeLimit
        });
      }

      if (this.isAnalyzing) {
        setTimeout(measureMemory, 1000);
      }
    };

    this.isAnalyzing = true;
    measureMemory();
  }

  // 网络请求分析
  analyzeNetworkRequests() {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        if (entry.initiatorType === 'fetch' || entry.initiatorType === 'xmlhttprequest') {
          this.networkData.push({
            timestamp: entry.startTime,
            url: entry.name,
            duration: entry.duration,
            size: entry.transferSize,
            status: entry.responseStatus
          });
        }
      });
    });

    observer.observe({ entryTypes: ['resource'] });
  }

  // 停止分析
  stopAnalysis() {
    this.isAnalyzing = false;
    return this.generateAnalysisReport();
  }

  // 生成分析报告
  generateAnalysisReport() {
    const report = {
      frameRate: this.analyzeFrameRateData(),
      memory: this.analyzeMemoryData(),
      network: this.analyzeNetworkData()
    };

    console.log('Performance Analysis Report:', report);
    return report;
  }

  analyzeFrameRateData() {
    if (this.frameData.length === 0) return null;

    const fpsValues = this.frameData.map(d => d.fps);
    return {
      average: fpsValues.reduce((a, b) => a + b, 0) / fpsValues.length,
      min: Math.min(...fpsValues),
      max: Math.max(...fpsValues),
      framesBelow60: fpsValues.filter(fps => fps < 60).length,
      framesBelow30: fpsValues.filter(fps => fps < 30).length
    };
  }

  analyzeMemoryData() {
    if (this.memoryData.length === 0) return null;

    const memoryUsage = this.memoryData.map(d => d.usedJSHeapSize);
    return {
      average: memoryUsage.reduce((a, b) => a + b, 0) / memoryUsage.length,
      min: Math.min(...memoryUsage),
      max: Math.max(...memoryUsage),
      growth: memoryUsage[memoryUsage.length - 1] - memoryUsage[0]
    };
  }

  analyzeNetworkData() {
    if (this.networkData.length === 0) return null;

    return {
      totalRequests: this.networkData.length,
      averageDuration: this.networkData.reduce((a, b) => a + b.duration, 0) / this.networkData.length,
      totalSize: this.networkData.reduce((a, b) => a + b.size, 0),
      slowestRequest: this.networkData.reduce((max, current) => current.duration > max.duration ? current : max),
      largestRequest: this.networkData.reduce((max, current) => current.size > max.size ? current : max)
    };
  }
}

// 使用示例
const performanceAnalyzer = new PerformanceAnalyzer();

// 开始分析
performanceAnalyzer.analyzeFrameRate();
performanceAnalyzer.analyzeMemoryUsage();
performanceAnalyzer.analyzeNetworkRequests();

// 停止分析并获取报告
setTimeout(() => {
  const report = performanceAnalyzer.stopAnalysis();
  console.log('Analysis Report:', report);
}, 10000);
```

## 六、总结

现代前端性能优化是一个系统性的工程，需要从多个维度进行考虑：

1. **Core Web Vitals优化**：重点关注LCP、FID、CLS三个核心指标
2. **资源加载优化**：使用智能加载、代码分割、缓存策略等技术
3. **运行时性能优化**：优化内存管理、事件循环、DOM操作
4. **网络性能优化**：使用智能重试、请求去重、Service Worker缓存
5. **性能监控分析**：持续监控性能指标，及时发现和解决问题

性能优化不是一次性的工作，而是需要持续改进的过程。通过建立完善的性能监控体系，结合具体业务场景选择合适的优化策略，才能实现真正的高性能前端应用。

记住，**性能优化的最终目标是提升用户体验**，而不仅仅是追求技术指标。在优化过程中，需要平衡性能、功能、开发成本等多个因素，找到最适合自己项目的优化方案。