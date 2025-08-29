# 前端Sentry性能优化与调优：构建高效监控系统

在现代Web应用开发中，监控系统的性能直接影响用户体验和系统稳定性。本文将深入探讨前端Sentry的性能优化策略，从数据采集优化到传输压缩，从缓存策略到资源管理，构建一个高效、可靠的前端监控系统。

## 1. 性能监控基础架构

### 1.1 性能指标收集器

```javascript
// 高性能指标收集器
class PerformanceMetricsCollector {
  constructor(options = {}) {
    this.options = {
      sampleRate: 0.1, // 10%采样率
      bufferSize: 100, // 缓冲区大小
      flushInterval: 5000, // 5秒刷新间隔
      enableWebVitals: true, // 启用Web Vitals
      enableResourceTiming: true, // 启用资源时序
      enableNavigationTiming: true, // 启用导航时序
      enableUserTiming: true, // 启用用户时序
      ...options
    };
    
    this.metricsBuffer = [];
    this.observers = new Map();
    this.startTime = performance.now();
    
    this.init();
  }
  
  init() {
    this.setupPerformanceObservers();
    this.setupWebVitalsCollection();
    this.startBufferFlush();
    this.bindEventListeners();
  }
  
  setupPerformanceObservers() {
    // 导航时序观察器
    if (this.options.enableNavigationTiming && 'PerformanceObserver' in window) {
      try {
        const navObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.collectNavigationMetrics(entry);
          }
        });
        navObserver.observe({ entryTypes: ['navigation'] });
        this.observers.set('navigation', navObserver);
      } catch (error) {
        console.warn('Navigation timing observer not supported:', error);
      }
    }
    
    // 资源时序观察器
    if (this.options.enableResourceTiming && 'PerformanceObserver' in window) {
      try {
        const resourceObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.collectResourceMetrics(entry);
          }
        });
        resourceObserver.observe({ entryTypes: ['resource'] });
        this.observers.set('resource', resourceObserver);
      } catch (error) {
        console.warn('Resource timing observer not supported:', error);
      }
    }
    
    // 用户时序观察器
    if (this.options.enableUserTiming && 'PerformanceObserver' in window) {
      try {
        const userObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.collectUserTimingMetrics(entry);
          }
        });
        userObserver.observe({ entryTypes: ['measure', 'mark'] });
        this.observers.set('user', userObserver);
      } catch (error) {
        console.warn('User timing observer not supported:', error);
      }
    }
    
    // 长任务观察器
    if ('PerformanceObserver' in window) {
      try {
        const longTaskObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.collectLongTaskMetrics(entry);
          }
        });
        longTaskObserver.observe({ entryTypes: ['longtask'] });
        this.observers.set('longtask', longTaskObserver);
      } catch (error) {
        console.warn('Long task observer not supported:', error);
      }
    }
  }
  
  setupWebVitalsCollection() {
    if (!this.options.enableWebVitals) return;
    
    // 收集 FCP (First Contentful Paint)
    this.collectFCP();
    
    // 收集 LCP (Largest Contentful Paint)
    this.collectLCP();
    
    // 收集 FID (First Input Delay)
    this.collectFID();
    
    // 收集 CLS (Cumulative Layout Shift)
    this.collectCLS();
    
    // 收集 TTFB (Time to First Byte)
    this.collectTTFB();
  }
  
  collectFCP() {
    if ('PerformanceObserver' in window) {
      try {
        const fcpObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            if (entry.name === 'first-contentful-paint') {
              this.addMetric({
                name: 'fcp',
                value: entry.startTime,
                timestamp: Date.now(),
                type: 'web-vital'
              });
            }
          }
        });
        fcpObserver.observe({ entryTypes: ['paint'] });
        this.observers.set('fcp', fcpObserver);
      } catch (error) {
        console.warn('FCP observer not supported:', error);
      }
    }
  }
  
  collectLCP() {
    if ('PerformanceObserver' in window) {
      try {
        const lcpObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries();
          const lastEntry = entries[entries.length - 1];
          
          this.addMetric({
            name: 'lcp',
            value: lastEntry.startTime,
            timestamp: Date.now(),
            type: 'web-vital',
            element: lastEntry.element ? this.getElementSelector(lastEntry.element) : null
          });
        });
        lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });
        this.observers.set('lcp', lcpObserver);
      } catch (error) {
        console.warn('LCP observer not supported:', error);
      }
    }
  }
  
  collectFID() {
    if ('PerformanceObserver' in window) {
      try {
        const fidObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.addMetric({
              name: 'fid',
              value: entry.processingStart - entry.startTime,
              timestamp: Date.now(),
              type: 'web-vital',
              eventType: entry.name
            });
          }
        });
        fidObserver.observe({ entryTypes: ['first-input'] });
        this.observers.set('fid', fidObserver);
      } catch (error) {
        console.warn('FID observer not supported:', error);
      }
    }
  }
  
  collectCLS() {
    if ('PerformanceObserver' in window) {
      try {
        let clsValue = 0;
        let clsEntries = [];
        
        const clsObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            if (!entry.hadRecentInput) {
              clsValue += entry.value;
              clsEntries.push(entry);
              
              this.addMetric({
                name: 'cls',
                value: clsValue,
                timestamp: Date.now(),
                type: 'web-vital',
                entries: clsEntries.length
              });
            }
          }
        });
        clsObserver.observe({ entryTypes: ['layout-shift'] });
        this.observers.set('cls', clsObserver);
      } catch (error) {
        console.warn('CLS observer not supported:', error);
      }
    }
  }
  
  collectTTFB() {
    if ('PerformanceObserver' in window) {
      try {
        const ttfbObserver = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.addMetric({
              name: 'ttfb',
              value: entry.responseStart - entry.requestStart,
              timestamp: Date.now(),
              type: 'web-vital'
            });
          }
        });
        ttfbObserver.observe({ entryTypes: ['navigation'] });
        this.observers.set('ttfb', ttfbObserver);
      } catch (error) {
        console.warn('TTFB observer not supported:', error);
      }
    }
  }
  
  collectNavigationMetrics(entry) {
    const metrics = {
      name: 'navigation',
      timestamp: Date.now(),
      type: 'navigation',
      data: {
        // DNS查询时间
        dnsLookup: entry.domainLookupEnd - entry.domainLookupStart,
        // TCP连接时间
        tcpConnect: entry.connectEnd - entry.connectStart,
        // SSL握手时间
        sslHandshake: entry.secureConnectionStart > 0 ? 
          entry.connectEnd - entry.secureConnectionStart : 0,
        // 请求时间
        request: entry.responseStart - entry.requestStart,
        // 响应时间
        response: entry.responseEnd - entry.responseStart,
        // DOM解析时间
        domParse: entry.domContentLoadedEventEnd - entry.domContentLoadedEventStart,
        // 资源加载时间
        resourceLoad: entry.loadEventEnd - entry.loadEventStart,
        // 总页面加载时间
        totalLoad: entry.loadEventEnd - entry.navigationStart,
        // 首字节时间
        ttfb: entry.responseStart - entry.requestStart,
        // DOM就绪时间
        domReady: entry.domContentLoadedEventEnd - entry.navigationStart,
        // 页面完全加载时间
        pageLoad: entry.loadEventEnd - entry.navigationStart
      }
    };
    
    this.addMetric(metrics);
  }
  
  collectResourceMetrics(entry) {
    // 只收集关键资源的指标
    if (this.shouldCollectResource(entry)) {
      const metrics = {
        name: 'resource',
        timestamp: Date.now(),
        type: 'resource',
        data: {
          name: entry.name,
          type: this.getResourceType(entry),
          size: entry.transferSize || 0,
          duration: entry.duration,
          // DNS查询时间
          dnsLookup: entry.domainLookupEnd - entry.domainLookupStart,
          // TCP连接时间
          tcpConnect: entry.connectEnd - entry.connectStart,
          // 请求时间
          request: entry.responseStart - entry.requestStart,
          // 响应时间
          response: entry.responseEnd - entry.responseStart,
          // 是否来自缓存
          fromCache: entry.transferSize === 0 && entry.decodedBodySize > 0
        }
      };
      
      this.addMetric(metrics);
    }
  }
  
  collectUserTimingMetrics(entry) {
    const metrics = {
      name: 'user-timing',
      timestamp: Date.now(),
      type: 'user-timing',
      data: {
        name: entry.name,
        entryType: entry.entryType,
        startTime: entry.startTime,
        duration: entry.duration || 0
      }
    };
    
    this.addMetric(metrics);
  }
  
  collectLongTaskMetrics(entry) {
    const metrics = {
      name: 'long-task',
      timestamp: Date.now(),
      type: 'performance',
      data: {
        duration: entry.duration,
        startTime: entry.startTime,
        attribution: entry.attribution ? entry.attribution.map(attr => ({
          name: attr.name,
          type: attr.entryType,
          startTime: attr.startTime,
          duration: attr.duration
        })) : []
      }
    };
    
    this.addMetric(metrics);
  }
  
  shouldCollectResource(entry) {
    // 采样控制
    if (Math.random() > this.options.sampleRate) {
      return false;
    }
    
    // 过滤掉监控系统自身的请求
    if (entry.name.includes('sentry.io') || entry.name.includes('monitoring')) {
      return false;
    }
    
    // 只收集重要资源类型
    const resourceType = this.getResourceType(entry);
    const importantTypes = ['script', 'stylesheet', 'image', 'fetch', 'xmlhttprequest'];
    
    return importantTypes.includes(resourceType);
  }
  
  getResourceType(entry) {
    if (entry.initiatorType) {
      return entry.initiatorType;
    }
    
    // 根据URL推断资源类型
    const url = entry.name.toLowerCase();
    if (url.includes('.js')) return 'script';
    if (url.includes('.css')) return 'stylesheet';
    if (url.match(/\.(png|jpg|jpeg|gif|svg|webp)$/)) return 'image';
    if (url.includes('/api/') || url.includes('fetch')) return 'fetch';
    
    return 'other';
  }
  
  getElementSelector(element) {
    if (!element) return null;
    
    // 生成元素选择器
    let selector = element.tagName.toLowerCase();
    
    if (element.id) {
      selector += `#${element.id}`;
    } else if (element.className) {
      const classes = element.className.split(' ').filter(c => c.trim());
      if (classes.length > 0) {
        selector += `.${classes.join('.')}`;
      }
    }
    
    return selector;
  }
  
  addMetric(metric) {
    // 添加通用字段
    metric.id = this.generateMetricId();
    metric.sessionId = this.getSessionId();
    metric.url = window.location.href;
    metric.userAgent = navigator.userAgent;
    
    // 添加到缓冲区
    this.metricsBuffer.push(metric);
    
    // 检查缓冲区大小
    if (this.metricsBuffer.length >= this.options.bufferSize) {
      this.flushMetrics();
    }
  }
  
  generateMetricId() {
    return `metric_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  getSessionId() {
    // 获取或生成会话ID
    let sessionId = sessionStorage.getItem('sentry_session_id');
    if (!sessionId) {
      sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      sessionStorage.setItem('sentry_session_id', sessionId);
    }
    return sessionId;
  }
  
  startBufferFlush() {
    setInterval(() => {
      if (this.metricsBuffer.length > 0) {
        this.flushMetrics();
      }
    }, this.options.flushInterval);
  }
  
  flushMetrics() {
    if (this.metricsBuffer.length === 0) return;
    
    const metrics = [...this.metricsBuffer];
    this.metricsBuffer = [];
    
    // 发送指标数据
    this.sendMetrics(metrics);
  }
  
  async sendMetrics(metrics) {
    try {
      // 压缩数据
      const compressedData = await this.compressMetrics(metrics);
      
      // 发送到监控服务
      await this.transmitMetrics(compressedData);
      
    } catch (error) {
      console.error('Failed to send metrics:', error);
      
      // 错误恢复：将指标重新加入缓冲区
      this.metricsBuffer.unshift(...metrics);
    }
  }
  
  async compressMetrics(metrics) {
    // 使用简单的JSON压缩
    const jsonData = JSON.stringify(metrics);
    
    // 如果支持压缩API，使用压缩
    if ('CompressionStream' in window) {
      try {
        const stream = new CompressionStream('gzip');
        const writer = stream.writable.getWriter();
        const reader = stream.readable.getReader();
        
        writer.write(new TextEncoder().encode(jsonData));
        writer.close();
        
        const chunks = [];
        let done = false;
        
        while (!done) {
          const { value, done: readerDone } = await reader.read();
          done = readerDone;
          if (value) chunks.push(value);
        }
        
        return new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], []));
      } catch (error) {
        console.warn('Compression failed, using uncompressed data:', error);
        return jsonData;
      }
    }
    
    return jsonData;
  }
  
  async transmitMetrics(data) {
    const endpoint = '/api/metrics';
    
    // 使用 sendBeacon 优先发送（适合页面卸载时）
    if (navigator.sendBeacon && typeof data === 'string') {
      const blob = new Blob([data], { type: 'application/json' });
      const sent = navigator.sendBeacon(endpoint, blob);
      if (sent) return;
    }
    
    // 回退到 fetch
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Encoding': typeof data !== 'string' ? 'gzip' : 'identity'
      },
      body: data
    });
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
  }
  
  bindEventListeners() {
    // 页面卸载时发送剩余指标
    window.addEventListener('beforeunload', () => {
      this.flushMetrics();
    });
    
    // 页面隐藏时发送指标
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') {
        this.flushMetrics();
      }
    });
  }
  
  // 获取当前性能统计
  getPerformanceStats() {
    return {
      metricsCollected: this.metricsBuffer.length,
      uptime: performance.now() - this.startTime,
      observersActive: this.observers.size,
      sessionId: this.getSessionId()
    };
  }
  
  // 清理资源
  destroy() {
    // 发送剩余指标
    this.flushMetrics();
    
    // 断开所有观察器
    for (const [name, observer] of this.observers) {
      try {
        observer.disconnect();
      } catch (error) {
        console.warn(`Failed to disconnect ${name} observer:`, error);
      }
    }
    
    this.observers.clear();
    this.metricsBuffer = [];
  }
}
```

### 2.2 数据压缩优化

```javascript
// 高级数据压缩管理器
class AdvancedCompressionManager {
  constructor(options = {}) {
    this.options = {
      compressionLevel: 6, // 压缩级别 1-9
      enableDictionary: true, // 启用字典压缩
      enableDelta: true, // 启用增量压缩
      enableDeduplication: true, // 启用去重
      maxDictionarySize: 1024 * 1024, // 最大字典大小 1MB
      ...options
    };
    
    this.compressionDictionary = new Map();
    this.deltaBase = null;
    this.seenHashes = new Set();
    
    this.init();
  }
  
  init() {
    this.buildCompressionDictionary();
  }
  
  async compress(data) {
    let processedData = data;
    
    // 1. 去重处理
    if (this.options.enableDeduplication) {
      processedData = this.deduplicateData(processedData);
    }
    
    // 2. 增量压缩
    if (this.options.enableDelta && this.deltaBase) {
      processedData = this.createDelta(processedData);
    }
    
    // 3. 字典压缩
    if (this.options.enableDictionary) {
      processedData = this.dictionaryCompress(processedData);
    }
    
    // 4. 标准压缩
    const compressed = await this.standardCompress(processedData);
    
    // 更新增量基准
    if (this.options.enableDelta) {
      this.updateDeltaBase(data);
    }
    
    return compressed;
  }
  
  deduplicateData(data) {
    if (typeof data !== 'object' || data === null) {
      return data;
    }
    
    if (Array.isArray(data)) {
      const uniqueItems = [];
      const seenHashes = new Set();
      
      for (const item of data) {
        const hash = this.hashObject(item);
        if (!seenHashes.has(hash)) {
          seenHashes.add(hash);
          uniqueItems.push(item);
        }
      }
      
      return uniqueItems;
    }
    
    // 对象去重（递归处理）
    const result = {};
    for (const [key, value] of Object.entries(data)) {
      result[key] = this.deduplicateData(value);
    }
    
    return result;
  }
  
  hashObject(obj) {
    const str = JSON.stringify(obj, Object.keys(obj).sort());
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // 转换为32位整数
    }
    return hash.toString();
  }
  
  createDelta(data) {
    if (!this.deltaBase || typeof data !== 'object') {
      return data;
    }
    
    const delta = this.computeDelta(this.deltaBase, data);
    return {
      type: 'delta',
      base: this.hashObject(this.deltaBase),
      changes: delta
    };
  }
  
  computeDelta(base, current) {
    const changes = {};
    
    // 检查新增和修改的字段
    for (const [key, value] of Object.entries(current)) {
      if (!(key in base)) {
        changes[key] = { type: 'add', value };
      } else if (JSON.stringify(base[key]) !== JSON.stringify(value)) {
        if (typeof value === 'object' && typeof base[key] === 'object') {
          const nestedChanges = this.computeDelta(base[key], value);
          if (Object.keys(nestedChanges).length > 0) {
            changes[key] = { type: 'modify', changes: nestedChanges };
          }
        } else {
          changes[key] = { type: 'modify', value };
        }
      }
    }
    
    // 检查删除的字段
    for (const key of Object.keys(base)) {
      if (!(key in current)) {
        changes[key] = { type: 'delete' };
      }
    }
    
    return changes;
  }
  
  updateDeltaBase(data) {
    // 只保留最近的数据作为基准
    this.deltaBase = JSON.parse(JSON.stringify(data));
  }
  
  buildCompressionDictionary() {
    // 构建常用字符串字典
    const commonStrings = [
      // 常用字段名
      'timestamp', 'message', 'level', 'error', 'stack', 'url', 'line', 'column',
      'userAgent', 'sessionId', 'userId', 'event', 'data', 'context', 'tags',
      'fingerprint', 'environment', 'release', 'platform', 'sdk', 'breadcrumbs',
      
      // 常用值
      'undefined', 'null', 'true', 'false', 'error', 'warning', 'info', 'debug',
      'production', 'development', 'staging', 'javascript', 'browser',
      
      // 常用错误类型
      'TypeError', 'ReferenceError', 'SyntaxError', 'RangeError', 'Error',
      'NetworkError', 'TimeoutError', 'AbortError',
      
      // 常用URL模式
      'https://', 'http://', 'www.', '.com', '.org', '.net', '/api/', '/static/',
      
      // 常用浏览器信息
      'Chrome', 'Firefox', 'Safari', 'Edge', 'Opera', 'Mobile', 'Desktop',
      'Windows', 'macOS', 'Linux', 'iOS', 'Android'
    ];
    
    // 为每个字符串分配短标识符
    commonStrings.forEach((str, index) => {
      const id = this.generateShortId(index);
      this.compressionDictionary.set(str, id);
    });
  }
  
  generateShortId(index) {
    // 生成短标识符（使用Base62编码）
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
    let result = '';
    let num = index;
    
    do {
      result = chars[num % 62] + result;
      num = Math.floor(num / 62);
    } while (num > 0);
    
    return `$${result}`; // 添加前缀避免冲突
  }
  
  dictionaryCompress(data) {
    const jsonStr = JSON.stringify(data);
    let compressed = jsonStr;
    
    // 使用字典替换常用字符串
    for (const [original, replacement] of this.compressionDictionary) {
      const regex = new RegExp(this.escapeRegExp(original), 'g');
      compressed = compressed.replace(regex, replacement);
    }
    
    return {
      type: 'dictionary',
      data: compressed,
      dictionary: Object.fromEntries(this.compressionDictionary)
    };
  }
  
  escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
  
  async standardCompress(data) {
    const jsonStr = typeof data === 'string' ? data : JSON.stringify(data);
    
    // 使用原生压缩API
    if ('CompressionStream' in window) {
      try {
        return await this.nativeCompress(jsonStr);
      } catch (error) {
        console.warn('Native compression failed:', error);
      }
    }
    
    // 回退到自定义压缩
    return this.customCompress(jsonStr);
  }
  
  async nativeCompress(data) {
    const stream = new CompressionStream('gzip');
    const writer = stream.writable.getWriter();
    const reader = stream.readable.getReader();
    
    writer.write(new TextEncoder().encode(data));
    writer.close();
    
    const chunks = [];
    let done = false;
    
    while (!done) {
      const { value, done: readerDone } = await reader.read();
      done = readerDone;
      if (value) chunks.push(value);
    }
    
    return {
      type: 'gzip',
      data: new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], [])),
      originalSize: data.length,
      compressedSize: chunks.reduce((acc, chunk) => acc + chunk.length, 0)
    };
  }
  
  customCompress(data) {
    // 简单的LZ77风格压缩
    let compressed = '';
    let i = 0;
    
    while (i < data.length) {
      let matchLength = 0;
      let matchDistance = 0;
      
      // 查找最长匹配
      for (let j = Math.max(0, i - 4096); j < i; j++) {
        let length = 0;
        while (length < 258 && i + length < data.length && 
               data[j + length] === data[i + length]) {
          length++;
        }
        
        if (length > matchLength) {
          matchLength = length;
          matchDistance = i - j;
        }
      }
      
      if (matchLength >= 3) {
        // 输出匹配
        compressed += `<${matchDistance},${matchLength}>`;
        i += matchLength;
      } else {
        // 输出字面量
        compressed += data[i];
        i++;
      }
    }
    
    return {
      type: 'lz77',
      data: compressed,
      originalSize: data.length,
      compressedSize: compressed.length
    };
  }
  
  async decompress(compressedData) {
    switch (compressedData.type) {
      case 'gzip':
        return await this.nativeDecompress(compressedData);
      case 'lz77':
        return this.customDecompress(compressedData);
      case 'dictionary':
        return this.dictionaryDecompress(compressedData);
      case 'delta':
        return this.applyDelta(compressedData);
      default:
        return compressedData.data;
    }
  }
  
  async nativeDecompress(compressedData) {
    const stream = new DecompressionStream('gzip');
    const writer = stream.writable.getWriter();
    const reader = stream.readable.getReader();
    
    writer.write(compressedData.data);
    writer.close();
    
    const chunks = [];
    let done = false;
    
    while (!done) {
      const { value, done: readerDone } = await reader.read();
      done = readerDone;
      if (value) chunks.push(value);
    }
    
    const decompressed = new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], []));
    return new TextDecoder().decode(decompressed);
  }
  
  customDecompress(compressedData) {
    const compressed = compressedData.data;
    let decompressed = '';
    let i = 0;
    
    while (i < compressed.length) {
      if (compressed[i] === '<') {
        // 解析匹配
        const endIndex = compressed.indexOf('>', i);
        const match = compressed.substring(i + 1, endIndex);
        const [distance, length] = match.split(',').map(Number);
        
        // 复制匹配的字符
        const startPos = decompressed.length - distance;
        for (let j = 0; j < length; j++) {
          decompressed += decompressed[startPos + j];
        }
        
        i = endIndex + 1;
      } else {
        // 字面量字符
        decompressed += compressed[i];
        i++;
      }
    }
    
    return decompressed;
  }
  
  dictionaryDecompress(compressedData) {
    let decompressed = compressedData.data;
    
    // 使用字典还原字符串
    for (const [original, replacement] of Object.entries(compressedData.dictionary)) {
      const regex = new RegExp(this.escapeRegExp(replacement), 'g');
      decompressed = decompressed.replace(regex, original);
    }
    
    return JSON.parse(decompressed);
  }
  
  applyDelta(deltaData) {
    // 需要基准数据来应用增量
    if (!this.deltaBase || this.hashObject(this.deltaBase) !== deltaData.base) {
      throw new Error('Delta base mismatch');
    }
    
    return this.mergeDelta(this.deltaBase, deltaData.changes);
  }
  
  mergeDelta(base, changes) {
    const result = JSON.parse(JSON.stringify(base));
    
    for (const [key, change] of Object.entries(changes)) {
      switch (change.type) {
        case 'add':
        case 'modify':
          if (change.value !== undefined) {
            result[key] = change.value;
          } else if (change.changes) {
            result[key] = this.mergeDelta(result[key] || {}, change.changes);
          }
          break;
        case 'delete':
          delete result[key];
          break;
      }
    }
    
    return result;
  }
  
  // 获取压缩统计信息
  getCompressionStats() {
    return {
      dictionarySize: this.compressionDictionary.size,
      hasDeltaBase: !!this.deltaBase,
      seenHashesCount: this.seenHashes.size
    };
  }
  
  // 清理资源
  cleanup() {
    this.compressionDictionary.clear();
    this.deltaBase = null;
    this.seenHashes.clear();
  }
}
```

## 3. 缓存优化策略

### 3.1 多层缓存管理器

```javascript
// 多层缓存管理器
class MultiTierCacheManager {
  constructor(options = {}) {
    this.options = {
      memoryLimit: 50 * 1024 * 1024, // 内存缓存限制 50MB
      localStorageLimit: 10 * 1024 * 1024, // LocalStorage限制 10MB
      indexedDBLimit: 100 * 1024 * 1024, // IndexedDB限制 100MB
      defaultTTL: 24 * 60 * 60 * 1000, // 默认TTL 24小时
      compressionThreshold: 1024, // 压缩阈值 1KB
      enableCompression: true,
      enableMetrics: true,
      ...options
    };
    
    this.memoryCache = new Map();
    this.cacheMetrics = {
      hits: 0,
      misses: 0,
      evictions: 0,
      compressions: 0
    };
    
    this.init();
  }
  
  async init() {
    // 初始化IndexedDB
    await this.initIndexedDB();
    
    // 启动清理任务
    this.startCleanupTasks();
    
    // 监控内存使用
    this.startMemoryMonitoring();
  }
  
  async initIndexedDB() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('SentryCacheDB', 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };
      
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        
        if (!db.objectStoreNames.contains('cache')) {
          const store = db.createObjectStore('cache', { keyPath: 'key' });
          store.createIndex('expiry', 'expiry', { unique: false });
          store.createIndex('size', 'size', { unique: false });
        }
      };
    });
  }
  
  async get(key) {
    // 1. 尝试内存缓存
    const memoryResult = this.getFromMemory(key);
    if (memoryResult !== null) {
      this.cacheMetrics.hits++;
      return memoryResult;
    }
    
    // 2. 尝试LocalStorage
    const localStorageResult = await this.getFromLocalStorage(key);
    if (localStorageResult !== null) {
      this.cacheMetrics.hits++;
      // 提升到内存缓存
      this.setInMemory(key, localStorageResult.value, localStorageResult.ttl);
      return localStorageResult.value;
    }
    
    // 3. 尝试IndexedDB
    const indexedDBResult = await this.getFromIndexedDB(key);
    if (indexedDBResult !== null) {
      this.cacheMetrics.hits++;
      // 提升到内存缓存
      this.setInMemory(key, indexedDBResult.value, indexedDBResult.ttl);
      return indexedDBResult.value;
    }
    
    this.cacheMetrics.misses++;
    return null;
  }
  
  async set(key, value, ttl = this.options.defaultTTL) {
    const expiry = Date.now() + ttl;
    const serializedValue = JSON.stringify(value);
    const size = new Blob([serializedValue]).size;
    
    // 决定存储层级
    if (size < 1024) {
      // 小数据存储在内存
      this.setInMemory(key, value, expiry);
    } else if (size < this.options.localStorageLimit / 10) {
      // 中等数据存储在LocalStorage
      await this.setInLocalStorage(key, value, expiry, size);
      this.setInMemory(key, value, expiry); // 同时缓存在内存
    } else {
      // 大数据存储在IndexedDB
      await this.setInIndexedDB(key, value, expiry, size);
    }
  }
  
  getFromMemory(key) {
    const item = this.memoryCache.get(key);
    if (!item) return null;
    
    if (Date.now() > item.expiry) {
      this.memoryCache.delete(key);
      return null;
    }
    
    // 更新访问时间（LRU）
    item.lastAccess = Date.now();
    return item.value;
  }
  
  setInMemory(key, value, expiry) {
    // 检查内存限制
    this.enforceMemoryLimit();
    
    this.memoryCache.set(key, {
      value,
      expiry,
      lastAccess: Date.now(),
      size: this.estimateSize(value)
    });
  }
  
  async getFromLocalStorage(key) {
    try {
      const item = localStorage.getItem(`sentry_cache_${key}`);
      if (!item) return null;
      
      const parsed = JSON.parse(item);
      if (Date.now() > parsed.expiry) {
        localStorage.removeItem(`sentry_cache_${key}`);
        return null;
      }
      
      let value = parsed.value;
      
      // 解压缩（如果需要）
      if (parsed.compressed) {
        value = await this.decompress(value);
      }
      
      return { value, ttl: parsed.expiry - Date.now() };
    } catch (error) {
      console.warn('LocalStorage cache read error:', error);
      return null;
    }
  }
  
  async setInLocalStorage(key, value, expiry, size) {
    try {
      let serializedValue = value;
      let compressed = false;
      
      // 压缩大数据
      if (this.options.enableCompression && size > this.options.compressionThreshold) {
        serializedValue = await this.compress(value);
        compressed = true;
        this.cacheMetrics.compressions++;
      }
      
      const item = {
        value: serializedValue,
        expiry,
        compressed,
        size
      };
      
      localStorage.setItem(`sentry_cache_${key}`, JSON.stringify(item));
    } catch (error) {
      if (error.name === 'QuotaExceededError') {
        // 清理过期项目并重试
        await this.cleanupLocalStorage();
        try {
          localStorage.setItem(`sentry_cache_${key}`, JSON.stringify(item));
        } catch (retryError) {
          console.warn('LocalStorage cache write failed after cleanup:', retryError);
        }
      } else {
        console.warn('LocalStorage cache write error:', error);
      }
    }
  }
  
  async getFromIndexedDB(key) {
    if (!this.db) return null;
    
    return new Promise((resolve) => {
      const transaction = this.db.transaction(['cache'], 'readonly');
      const store = transaction.objectStore('cache');
      const request = store.get(key);
      
      request.onsuccess = async () => {
        const result = request.result;
        if (!result) {
          resolve(null);
          return;
        }
        
        if (Date.now() > result.expiry) {
          // 异步删除过期项
          this.deleteFromIndexedDB(key);
          resolve(null);
          return;
        }
        
        let value = result.value;
        
        // 解压缩（如果需要）
        if (result.compressed) {
          value = await this.decompress(value);
        }
        
        resolve({ value, ttl: result.expiry - Date.now() });
      };
      
      request.onerror = () => {
        console.warn('IndexedDB cache read error:', request.error);
        resolve(null);
      };
    });
  }
  
  async setInIndexedDB(key, value, expiry, size) {
    if (!this.db) return;
    
    return new Promise(async (resolve) => {
      let serializedValue = value;
      let compressed = false;
      
      // 压缩大数据
      if (this.options.enableCompression && size > this.options.compressionThreshold) {
        serializedValue = await this.compress(value);
        compressed = true;
        this.cacheMetrics.compressions++;
      }
      
      const transaction = this.db.transaction(['cache'], 'readwrite');
      const store = transaction.objectStore('cache');
      
      const item = {
        key,
        value: serializedValue,
        expiry,
        compressed,
        size,
        created: Date.now()
      };
      
      const request = store.put(item);
      
      request.onsuccess = () => resolve();
      request.onerror = () => {
        console.warn('IndexedDB cache write error:', request.error);
        resolve();
      };
    });
  }
  
  async deleteFromIndexedDB(key) {
    if (!this.db) return;
    
    return new Promise((resolve) => {
      const transaction = this.db.transaction(['cache'], 'readwrite');
      const store = transaction.objectStore('cache');
      const request = store.delete(key);
      
      request.onsuccess = () => resolve();
      request.onerror = () => {
        console.warn('IndexedDB cache delete error:', request.error);
        resolve();
      };
    });
  }
  
  async compress(data) {
    const jsonStr = JSON.stringify(data);
    
    if ('CompressionStream' in window) {
      try {
        const stream = new CompressionStream('gzip');
        const writer = stream.writable.getWriter();
        const reader = stream.readable.getReader();
        
        writer.write(new TextEncoder().encode(jsonStr));
        writer.close();
        
        const chunks = [];
        let done = false;
        
        while (!done) {
          const { value, done: readerDone } = await reader.read();
          done = readerDone;
          if (value) chunks.push(value);
        }
        
        return Array.from(new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], [])));
      } catch (error) {
        console.warn('Compression failed:', error);
        return jsonStr;
      }
    }
    
    return jsonStr;
  }
  
  async decompress(data) {
    if (Array.isArray(data)) {
      // 解压缩二进制数据
      try {
        const stream = new DecompressionStream('gzip');
        const writer = stream.writable.getWriter();
        const reader = stream.readable.getReader();
        
        writer.write(new Uint8Array(data));
        writer.close();
        
        const chunks = [];
        let done = false;
        
        while (!done) {
          const { value, done: readerDone } = await reader.read();
          done = readerDone;
          if (value) chunks.push(value);
        }
        
        const decompressed = new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], []));
        const jsonStr = new TextDecoder().decode(decompressed);
        return JSON.parse(jsonStr);
      } catch (error) {
        console.warn('Decompression failed:', error);
        return data;
      }
    }
    
    return data;
  }
  
  enforceMemoryLimit() {
    const currentSize = this.getMemoryCacheSize();
    
    if (currentSize > this.options.memoryLimit) {
      // LRU淘汰
      const items = Array.from(this.memoryCache.entries())
        .map(([key, value]) => ({ key, ...value }))
        .sort((a, b) => a.lastAccess - b.lastAccess);
      
      let removedSize = 0;
      const targetSize = this.options.memoryLimit * 0.8; // 淘汰到80%
      
      for (const item of items) {
        if (currentSize - removedSize <= targetSize) break;
        
        this.memoryCache.delete(item.key);
        removedSize += item.size;
        this.cacheMetrics.evictions++;
      }
    }
  }
  
  getMemoryCacheSize() {
    let totalSize = 0;
    for (const item of this.memoryCache.values()) {
      totalSize += item.size;
    }
    return totalSize;
  }
  
  estimateSize(value) {
    return new Blob([JSON.stringify(value)]).size;
  }
  
  startCleanupTasks() {
    // 每小时清理一次过期项
    setInterval(() => {
      this.cleanupExpiredItems();
    }, 60 * 60 * 1000);
    
    // 每天清理一次存储空间
    setInterval(() => {
      this.cleanupStorage();
    }, 24 * 60 * 60 * 1000);
  }
  
  async cleanupExpiredItems() {
    // 清理内存缓存
    const now = Date.now();
    for (const [key, item] of this.memoryCache.entries()) {
      if (now > item.expiry) {
        this.memoryCache.delete(key);
      }
    }
    
    // 清理LocalStorage
    await this.cleanupLocalStorage();
    
    // 清理IndexedDB
    await this.cleanupIndexedDB();
  }
  
  async cleanupLocalStorage() {
    const keysToRemove = [];
    
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('sentry_cache_')) {
        try {
          const item = JSON.parse(localStorage.getItem(key));
          if (Date.now() > item.expiry) {
            keysToRemove.push(key);
          }
        } catch (error) {
          // 损坏的项目也删除
          keysToRemove.push(key);
        }
      }
    }
    
    keysToRemove.forEach(key => localStorage.removeItem(key));
  }
  
  async cleanupIndexedDB() {
    if (!this.db) return;
    
    return new Promise((resolve) => {
      const transaction = this.db.transaction(['cache'], 'readwrite');
      const store = transaction.objectStore('cache');
      const index = store.index('expiry');
      
      const range = IDBKeyRange.upperBound(Date.now());
      const request = index.openCursor(range);
      
      request.onsuccess = (event) => {
        const cursor = event.target.result;
        if (cursor) {
          cursor.delete();
          cursor.continue();
        } else {
          resolve();
        }
      };
      
      request.onerror = () => {
        console.warn('IndexedDB cleanup error:', request.error);
        resolve();
      };
    });
  }
  
  async cleanupStorage() {
    // 清理超出限制的存储
    await this.cleanupLocalStorageBySize();
    await this.cleanupIndexedDBBySize();
  }
  
  async cleanupLocalStorageBySize() {
    const items = [];
    let totalSize = 0;
    
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('sentry_cache_')) {
        try {
          const value = localStorage.getItem(key);
          const item = JSON.parse(value);
          const size = new Blob([value]).size;
          
          items.push({ key, size, expiry: item.expiry });
          totalSize += size;
        } catch (error) {
          // 忽略损坏的项目
        }
      }
    }
    
    if (totalSize > this.options.localStorageLimit) {
      // 按过期时间排序，删除最早过期的
      items.sort((a, b) => a.expiry - b.expiry);
      
      let removedSize = 0;
      const targetSize = this.options.localStorageLimit * 0.8;
      
      for (const item of items) {
        if (totalSize - removedSize <= targetSize) break;
        
        localStorage.removeItem(item.key);
        removedSize += item.size;
      }
    }
  }
  
  async cleanupIndexedDBBySize() {
    if (!this.db) return;
    
    return new Promise((resolve) => {
      const transaction = this.db.transaction(['cache'], 'readwrite');
      const store = transaction.objectStore('cache');
      const index = store.index('size');
      
      let totalSize = 0;
      const items = [];
      
      const request = index.openCursor();
      
      request.onsuccess = (event) => {
        const cursor = event.target.result;
        if (cursor) {
          const item = cursor.value;
          items.push(item);
          totalSize += item.size;
          cursor.continue();
        } else {
          // 检查是否超出限制
          if (totalSize > this.options.indexedDBLimit) {
            // 按创建时间排序，删除最旧的
            items.sort((a, b) => a.created - b.created);
            
            let removedSize = 0;
            const targetSize = this.options.indexedDBLimit * 0.8;
            
            const deleteTransaction = this.db.transaction(['cache'], 'readwrite');
            const deleteStore = deleteTransaction.objectStore('cache');
            
            for (const item of items) {
              if (totalSize - removedSize <= targetSize) break;
              
              deleteStore.delete(item.key);
              removedSize += item.size;
            }
          }
          
          resolve();
        }
      };
      
      request.onerror = () => {
        console.warn('IndexedDB size cleanup error:', request.error);
        resolve();
      };
    });
  }
  
  startMemoryMonitoring() {
    if ('memory' in performance) {
      setInterval(() => {
        const memoryInfo = performance.memory;
        const memoryUsage = memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit;
        
        // 如果内存使用率超过80%，主动清理缓存
        if (memoryUsage > 0.8) {
          this.enforceMemoryLimit();
        }
      }, 30000); // 每30秒检查一次
    }
  }
  
  // 获取缓存统计信息
  getCacheStats() {
    return {
      memory: {
        size: this.getMemoryCacheSize(),
        count: this.memoryCache.size,
        limit: this.options.memoryLimit
      },
      metrics: { ...this.cacheMetrics },
      hitRate: this.cacheMetrics.hits / (this.cacheMetrics.hits + this.cacheMetrics.misses) || 0
    };
  }
  
  // 清空所有缓存
  async clear() {
    // 清空内存缓存
    this.memoryCache.clear();
    
    // 清空LocalStorage缓存
    const keysToRemove = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('sentry_cache_')) {
        keysToRemove.push(key);
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key));
    
    // 清空IndexedDB缓存
    if (this.db) {
      return new Promise((resolve) => {
        const transaction = this.db.transaction(['cache'], 'readwrite');
        const store = transaction.objectStore('cache');
        const request = store.clear();
        
        request.onsuccess = () => resolve();
        request.onerror = () => {
          console.warn('IndexedDB clear error:', request.error);
          resolve();
        };
      });
    }
  }
}
```

## 4. 资源管理优化

### 4.1 内存管理优化器

```javascript
// 内存管理优化器
class MemoryOptimizer {
  constructor(options = {}) {
    this.options = {
      maxMemoryUsage: 100 * 1024 * 1024, // 最大内存使用 100MB
      gcThreshold: 0.8, // GC触发阈值
      monitorInterval: 30000, // 监控间隔 30秒
      enableWeakRef: true, // 启用WeakRef
      enableFinalizationRegistry: true, // 启用FinalizationRegistry
      ...options
    };
    
    this.memoryPools = new Map();
    this.objectRegistry = new Map();
    this.weakRefs = new Set();
    
    if (this.options.enableFinalizationRegistry && 'FinalizationRegistry' in window) {
      this.finalizationRegistry = new FinalizationRegistry((heldValue) => {
        this.handleObjectFinalization(heldValue);
      });
    }
    
    this.init();
  }
  
  init() {
    this.startMemoryMonitoring();
    this.setupMemoryPools();
  }
  
  setupMemoryPools() {
    // 创建不同类型的内存池
    this.memoryPools.set('events', new ObjectPool(() => ({}), 1000));
    this.memoryPools.set('breadcrumbs', new ObjectPool(() => ({}), 500));
    this.memoryPools.set('contexts', new ObjectPool(() => ({}), 200));
    this.memoryPools.set('spans', new ObjectPool(() => ({}), 300));
  }
  
  // 获取对象（从池中）
  acquire(type) {
    const pool = this.memoryPools.get(type);
    if (pool) {
      return pool.acquire();
    }
    return {};
  }
  
  // 释放对象（回到池中）
  release(type, obj) {
    const pool = this.memoryPools.get(type);
    if (pool && obj) {
      // 清理对象
      this.cleanObject(obj);
      pool.release(obj);
    }
  }
  
  cleanObject(obj) {
    // 清理对象的所有属性
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        delete obj[key];
      }
    }
  }
  
  // 注册对象用于内存跟踪
  registerObject(obj, metadata = {}) {
    const id = this.generateObjectId();
    
    this.objectRegistry.set(id, {
      object: obj,
      metadata,
      created: Date.now(),
      size: this.estimateObjectSize(obj)
    });
    
    // 使用WeakRef避免内存泄漏
    if (this.options.enableWeakRef && 'WeakRef' in window) {
      const weakRef = new WeakRef(obj);
      this.weakRefs.add(weakRef);
      
      // 注册到FinalizationRegistry
      if (this.finalizationRegistry) {
        this.finalizationRegistry.register(obj, id);
      }
    }
    
    return id;
  }
  
  generateObjectId() {
    return `obj_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  estimateObjectSize(obj) {
    try {
      return new Blob([JSON.stringify(obj)]).size;
    } catch (error) {
      // 对于循环引用等情况的估算
      return this.roughSizeEstimate(obj);
    }
  }
  
  roughSizeEstimate(obj, visited = new Set()) {
    if (obj === null || typeof obj !== 'object' || visited.has(obj)) {
      return 0;
    }
    
    visited.add(obj);
    let size = 0;
    
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        size += key.length * 2; // 键的大小
        
        const value = obj[key];
        switch (typeof value) {
          case 'string':
            size += value.length * 2;
            break;
          case 'number':
            size += 8;
            break;
          case 'boolean':
            size += 4;
            break;
          case 'object':
            size += this.roughSizeEstimate(value, visited);
            break;
          default:
            size += 0;
        }
      }
    }
    
    return size;
  }
  
  handleObjectFinalization(objectId) {
    // 对象被垃圾回收时的处理
    this.objectRegistry.delete(objectId);
  }
  
  startMemoryMonitoring() {
    setInterval(() => {
      this.checkMemoryUsage();
      this.cleanupWeakRefs();
    }, this.options.monitorInterval);
  }
  
  checkMemoryUsage() {
    if ('memory' in performance) {
      const memoryInfo = performance.memory;
      const usageRatio = memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit;
      
      if (usageRatio > this.options.gcThreshold) {
        this.triggerCleanup();
      }
      
      // 记录内存使用情况
      this.recordMemoryMetrics(memoryInfo);
    }
  }
  
  triggerCleanup() {
    // 清理过期的对象注册
    this.cleanupObjectRegistry();
    
    // 清理内存池
    this.cleanupMemoryPools();
    
    // 建议垃圾回收
    if ('gc' in window && typeof window.gc === 'function') {
      window.gc();
    }
  }
  
  cleanupObjectRegistry() {
    const now = Date.now();
    const maxAge = 5 * 60 * 1000; // 5分钟
    
    for (const [id, entry] of this.objectRegistry.entries()) {
      if (now - entry.created > maxAge) {
        this.objectRegistry.delete(id);
      }
    }
  }
  
  cleanupMemoryPools() {
    for (const pool of this.memoryPools.values()) {
      pool.cleanup();
    }
  }
  
  cleanupWeakRefs() {
    // 清理已经被回收的WeakRef
    for (const weakRef of this.weakRefs) {
      if (weakRef.deref() === undefined) {
        this.weakRefs.delete(weakRef);
      }
    }
  }
  
  recordMemoryMetrics(memoryInfo) {
    const metrics = {
      timestamp: Date.now(),
      usedJSHeapSize: memoryInfo.usedJSHeapSize,
      totalJSHeapSize: memoryInfo.totalJSHeapSize,
      jsHeapSizeLimit: memoryInfo.jsHeapSizeLimit,
      usageRatio: memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit,
      registeredObjects: this.objectRegistry.size,
      weakRefs: this.weakRefs.size,
      memoryPools: Object.fromEntries(
        Array.from(this.memoryPools.entries()).map(([key, pool]) => [
          key,
          {
            available: pool.available.length,
            inUse: pool.inUse.size,
            total: pool.available.length + pool.inUse.size
          }
        ])
      )
    };
    
    // 可以发送到监控系统
    this.onMemoryMetrics?.(metrics);
  }
  
  // 获取内存统计信息
  getMemoryStats() {
    const stats = {
      objectRegistry: this.objectRegistry.size,
      weakRefs: this.weakRefs.size,
      memoryPools: {}
    };
    
    for (const [type, pool] of this.memoryPools.entries()) {
      stats.memoryPools[type] = {
        available: pool.available.length,
        inUse: pool.inUse.size,
        total: pool.available.length + pool.inUse.size
      };
    }
    
    if ('memory' in performance) {
      const memoryInfo = performance.memory;
      stats.heap = {
        used: memoryInfo.usedJSHeapSize,
        total: memoryInfo.totalJSHeapSize,
        limit: memoryInfo.jsHeapSizeLimit,
        usageRatio: memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit
      };
    }
    
    return stats;
  }
  
  // 设置内存指标回调
  onMemoryMetrics(callback) {
    this.onMemoryMetrics = callback;
  }
}

// 对象池实现
class ObjectPool {
  constructor(factory, initialSize = 10) {
    this.factory = factory;
    this.available = [];
    this.inUse = new Set();
    
    // 预分配对象
    for (let i = 0; i < initialSize; i++) {
      this.available.push(this.factory());
    }
  }
  
  acquire() {
    let obj;
    
    if (this.available.length > 0) {
      obj = this.available.pop();
    } else {
      obj = this.factory();
    }
    
    this.inUse.add(obj);
    return obj;
  }
  
  release(obj) {
    if (this.inUse.has(obj)) {
      this.inUse.delete(obj);
      this.available.push(obj);
    }
  }
  
  cleanup() {
    // 如果可用对象太多，释放一些
    const maxAvailable = 50;
    if (this.available.length > maxAvailable) {
      this.available.splice(maxAvailable);
    }
  }
}
```

### 4.2 网络优化策略

```javascript
// 网络优化管理器
class NetworkOptimizer {
  constructor(options = {}) {
    this.options = {
      maxConcurrentRequests: 6, // 最大并发请求数
      requestTimeout: 30000, // 请求超时 30秒
      retryAttempts: 3, // 重试次数
      retryDelay: 1000, // 重试延迟
      enableCompression: true, // 启用压缩
      enableBatching: true, // 启用批处理
      batchSize: 10, // 批处理大小
      batchTimeout: 5000, // 批处理超时
      enablePriority: true, // 启用优先级
      enableConnectionPooling: true, // 启用连接池
      ...options
    };
    
    this.requestQueue = new PriorityQueue();
    this.activeRequests = new Set();
    this.connectionPool = new Map();
    this.batchBuffer = [];
    this.batchTimer = null;
    this.networkMetrics = {
      totalRequests: 0,
      successfulRequests: 0,
      failedRequests: 0,
      averageResponseTime: 0,
      totalBytes: 0
    };
    
    this.init();
  }
  
  init() {
    this.startBatchProcessor();
    this.monitorNetworkConditions();
  }
  
  async sendRequest(data, options = {}) {
    const requestOptions = {
      priority: 'normal', // low, normal, high, critical
      timeout: this.options.requestTimeout,
      retries: this.options.retryAttempts,
      compress: this.options.enableCompression,
      batch: this.options.enableBatching,
      ...options
    };
    
    if (requestOptions.batch && requestOptions.priority !== 'critical') {
      return this.addToBatch(data, requestOptions);
    }
    
    return this.executeRequest(data, requestOptions);
  }
  
  addToBatch(data, options) {
    return new Promise((resolve, reject) => {
      this.batchBuffer.push({
        data,
        options,
        resolve,
        reject,
        timestamp: Date.now()
      });
      
      // 如果批次已满，立即处理
      if (this.batchBuffer.length >= this.options.batchSize) {
        this.processBatch();
      }
    });
  }
  
  startBatchProcessor() {
    const processBatch = () => {
      if (this.batchBuffer.length > 0) {
        this.processBatch();
      }
      
      this.batchTimer = setTimeout(processBatch, this.options.batchTimeout);
    };
    
    processBatch();
  }
  
  async processBatch() {
    if (this.batchBuffer.length === 0) return;
    
    const batch = this.batchBuffer.splice(0, this.options.batchSize);
    
    try {
      const batchData = batch.map(item => item.data);
      const result = await this.executeBatchRequest(batchData);
      
      // 分发结果
      batch.forEach((item, index) => {
        if (result.results && result.results[index]) {
          item.resolve(result.results[index]);
        } else {
          item.reject(new Error('Batch request failed'));
        }
      });
    } catch (error) {
      // 批处理失败，回退到单独请求
      batch.forEach(item => {
        this.executeRequest(item.data, item.options)
          .then(item.resolve)
          .catch(item.reject);
      });
    }
  }
  
  async executeBatchRequest(batchData) {
    const compressedData = this.options.enableCompression 
      ? await this.compressData(batchData)
      : batchData;
    
    return this.makeHttpRequest({
      type: 'batch',
      data: compressedData,
      compressed: this.options.enableCompression
    });
  }
  
  async executeRequest(data, options) {
    // 检查并发限制
    if (this.activeRequests.size >= this.options.maxConcurrentRequests) {
      await this.waitForSlot();
    }
    
    const request = {
      id: this.generateRequestId(),
      data,
      options,
      startTime: Date.now(),
      attempts: 0
    };
    
    if (this.options.enablePriority) {
      return this.addToQueue(request);
    }
    
    return this.processRequest(request);
  }
  
  addToQueue(request) {
    return new Promise((resolve, reject) => {
      request.resolve = resolve;
      request.reject = reject;
      
      this.requestQueue.enqueue(request, this.getPriorityValue(request.options.priority));
      this.processQueue();
    });
  }
  
  getPriorityValue(priority) {
    const priorities = {
      'low': 1,
      'normal': 2,
      'high': 3,
      'critical': 4
    };
    return priorities[priority] || 2;
  }
  
  async processQueue() {
    while (!this.requestQueue.isEmpty() && 
           this.activeRequests.size < this.options.maxConcurrentRequests) {
      const request = this.requestQueue.dequeue();
      this.processRequest(request)
        .then(request.resolve)
        .catch(request.reject);
    }
  }
  
  async processRequest(request) {
    this.activeRequests.add(request);
    
    try {
      const result = await this.makeRequestWithRetry(request);
      this.recordSuccess(request, result);
      return result;
    } catch (error) {
      this.recordFailure(request, error);
      throw error;
    } finally {
      this.activeRequests.delete(request);
      this.processQueue(); // 处理队列中的下一个请求
    }
  }
  
  async makeRequestWithRetry(request) {
    let lastError;
    
    for (let attempt = 0; attempt <= request.options.retries; attempt++) {
      request.attempts = attempt + 1;
      
      try {
        return await this.makeHttpRequest(request.data, request.options);
      } catch (error) {
        lastError = error;
        
        // 如果是最后一次尝试，直接抛出错误
        if (attempt === request.options.retries) {
          throw error;
        }
        
        // 计算重试延迟（指数退避）
        const delay = this.options.retryDelay * Math.pow(2, attempt);
        await this.sleep(delay);
      }
    }
    
    throw lastError;
  }
  
  async makeHttpRequest(data, options = {}) {
    const url = this.getEndpointUrl();
    const connection = this.getConnection(url);
    
    const requestData = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeaders(),
        ...options.headers
      },
      body: JSON.stringify(data),
      signal: this.createAbortSignal(options.timeout)
    };
    
    // 压缩请求体
    if (options.compress && requestData.body.length > 1024) {
      requestData.body = await this.compressData(requestData.body);
      requestData.headers['Content-Encoding'] = 'gzip';
    }
    
    const startTime = performance.now();
    
    try {
      const response = await fetch(url, requestData);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const result = await response.json();
      const endTime = performance.now();
      
      // 记录网络指标
      this.updateNetworkMetrics({
        responseTime: endTime - startTime,
        bytes: new Blob([JSON.stringify(result)]).size,
        success: true
      });
      
      return result;
    } catch (error) {
      const endTime = performance.now();
      
      this.updateNetworkMetrics({
        responseTime: endTime - startTime,
        bytes: 0,
        success: false
      });
      
      throw error;
    } finally {
      this.releaseConnection(url, connection);
    }
  }
  
  getConnection(url) {
    if (!this.options.enableConnectionPooling) {
      return null;
    }
    
    const connections = this.connectionPool.get(url) || [];
    
    // 查找可用连接
    const availableConnection = connections.find(conn => !conn.inUse);
    
    if (availableConnection) {
      availableConnection.inUse = true;
      availableConnection.lastUsed = Date.now();
      return availableConnection;
    }
    
    // 创建新连接
    const newConnection = {
      id: this.generateConnectionId(),
      created: Date.now(),
      lastUsed: Date.now(),
      inUse: true,
      requests: 0
    };
    
    connections.push(newConnection);
    this.connectionPool.set(url, connections);
    
    return newConnection;
  }
  
  releaseConnection(url, connection) {
    if (!connection) return;
    
    connection.inUse = false;
    connection.requests++;
    
    // 清理过期连接
    this.cleanupConnections(url);
  }
  
  cleanupConnections(url) {
    const connections = this.connectionPool.get(url);
    if (!connections) return;
    
    const now = Date.now();
    const maxAge = 5 * 60 * 1000; // 5分钟
    const maxRequests = 100; // 最大请求数
    
    const activeConnections = connections.filter(conn => {
      return conn.inUse || 
             (now - conn.lastUsed < maxAge && conn.requests < maxRequests);
    });
    
    this.connectionPool.set(url, activeConnections);
  }
  
  async compressData(data) {
    const jsonStr = typeof data === 'string' ? data : JSON.stringify(data);
    
    if ('CompressionStream' in window) {
      try {
        const stream = new CompressionStream('gzip');
        const writer = stream.writable.getWriter();
        const reader = stream.readable.getReader();
        
        writer.write(new TextEncoder().encode(jsonStr));
        writer.close();
        
        const chunks = [];
        let done = false;
        
        while (!done) {
          const { value, done: readerDone } = await reader.read();
          done = readerDone;
          if (value) chunks.push(value);
        }
        
        return new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], []));
      } catch (error) {
        console.warn('Compression failed:', error);
        return jsonStr;
      }
    }
    
    return jsonStr;
  }
  
  createAbortSignal(timeout) {
    if ('AbortController' in window) {
      const controller = new AbortController();
      setTimeout(() => controller.abort(), timeout);
      return controller.signal;
    }
    return undefined;
  }
  
  async waitForSlot() {
    return new Promise((resolve) => {
      const checkSlot = () => {
        if (this.activeRequests.size < this.options.maxConcurrentRequests) {
          resolve();
        } else {
          setTimeout(checkSlot, 100);
        }
      };
      checkSlot();
    });
  }
  
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
  
  generateRequestId() {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  generateConnectionId() {
    return `conn_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  getEndpointUrl() {
    // 返回Sentry端点URL
    return 'https://sentry.io/api/0/projects/your-project/store/';
  }
  
  getAuthHeaders() {
    return {
      'Authorization': 'Bearer your-auth-token'
    };
  }
  
  recordSuccess(request, result) {
    this.networkMetrics.successfulRequests++;
    this.networkMetrics.totalRequests++;
  }
  
  recordFailure(request, error) {
    this.networkMetrics.failedRequests++;
    this.networkMetrics.totalRequests++;
  }
  
  updateNetworkMetrics(metrics) {
    this.networkMetrics.totalBytes += metrics.bytes;
    
    // 更新平均响应时间
    const totalRequests = this.networkMetrics.successfulRequests + this.networkMetrics.failedRequests;
    if (totalRequests > 0) {
      this.networkMetrics.averageResponseTime = 
        (this.networkMetrics.averageResponseTime * (totalRequests - 1) + metrics.responseTime) / totalRequests;
    }
  }
  
  monitorNetworkConditions() {
    if ('connection' in navigator) {
      const connection = navigator.connection;
      
      const updateNetworkSettings = () => {
        // 根据网络条件调整设置
        switch (connection.effectiveType) {
          case 'slow-2g':
          case '2g':
            this.options.maxConcurrentRequests = 2;
            this.options.batchSize = 5;
            this.options.requestTimeout = 60000;
            break;
          case '3g':
            this.options.maxConcurrentRequests = 4;
            this.options.batchSize = 8;
            this.options.requestTimeout = 45000;
            break;
          case '4g':
          default:
            this.options.maxConcurrentRequests = 6;
            this.options.batchSize = 10;
            this.options.requestTimeout = 30000;
            break;
        }
      };
      
      updateNetworkSettings();
      connection.addEventListener('change', updateNetworkSettings);
    }
  }
  
  // 获取网络统计信息
  getNetworkStats() {
    return {
      ...this.networkMetrics,
      activeRequests: this.activeRequests.size,
      queuedRequests: this.requestQueue.size(),
      batchedRequests: this.batchBuffer.length,
      connectionPools: Object.fromEntries(
        Array.from(this.connectionPool.entries()).map(([url, connections]) => [
          url,
          {
            total: connections.length,
            active: connections.filter(conn => conn.inUse).length,
            idle: connections.filter(conn => !conn.inUse).length
          }
        ])
      )
    };
  }
}

// 优先级队列实现
class PriorityQueue {
  constructor() {
    this.items = [];
  }
  
  enqueue(item, priority) {
    const queueItem = { item, priority };
    let added = false;
    
    for (let i = 0; i < this.items.length; i++) {
      if (queueItem.priority > this.items[i].priority) {
        this.items.splice(i, 0, queueItem);
        added = true;
        break;
      }
    }
    
    if (!added) {
      this.items.push(queueItem);
    }
  }
  
  dequeue() {
    return this.items.shift()?.item;
  }
  
  isEmpty() {
    return this.items.length === 0;
  }
  
  size() {
    return this.items.length;
  }
}
```

## 5. 性能监控仪表板

### 5.1 实时性能仪表板

```javascript
// 实时性能仪表板
class PerformanceDashboard {
  constructor(options = {}) {
    this.options = {
      updateInterval: 1000, // 更新间隔 1秒
      maxDataPoints: 100, // 最大数据点数
      enableRealtime: true, // 启用实时更新
      enableAlerts: true, // 启用告警
      thresholds: {
        responseTime: 3000, // 响应时间阈值 3秒
        errorRate: 0.05, // 错误率阈值 5%
        memoryUsage: 0.8, // 内存使用率阈值 80%
        cpuUsage: 0.7 // CPU使用率阈值 70%
      },
      ...options
    };
    
    this.metrics = {
      performance: [],
      errors: [],
      memory: [],
      network: [],
      vitals: []
    };
    
    this.charts = new Map();
    this.alerts = [];
    this.isRunning = false;
    
    this.init();
  }
  
  init() {
    this.createDashboard();
    this.setupCharts();
    this.bindEvents();
    
    if (this.options.enableRealtime) {
      this.startRealTimeUpdates();
    }
  }
  
  createDashboard() {
    const dashboard = document.createElement('div');
    dashboard.id = 'sentry-performance-dashboard';
    dashboard.innerHTML = `
      <div class="dashboard-header">
        <h2>Sentry 性能监控仪表板</h2>
        <div class="dashboard-controls">
          <button id="toggle-realtime">实时更新</button>
          <button id="export-data">导出数据</button>
          <button id="clear-data">清除数据</button>
        </div>
      </div>
      
      <div class="dashboard-grid">
        <!-- 关键指标卡片 -->
        <div class="metrics-cards">
          <div class="metric-card" id="response-time-card">
            <h3>平均响应时间</h3>
            <div class="metric-value" id="avg-response-time">0ms</div>
            <div class="metric-trend" id="response-time-trend"></div>
          </div>
          
          <div class="metric-card" id="error-rate-card">
            <h3>错误率</h3>
            <div class="metric-value" id="error-rate">0%</div>
            <div class="metric-trend" id="error-rate-trend"></div>
          </div>
          
          <div class="metric-card" id="memory-usage-card">
            <h3>内存使用率</h3>
            <div class="metric-value" id="memory-usage">0%</div>
            <div class="metric-trend" id="memory-usage-trend"></div>
          </div>
          
          <div class="metric-card" id="throughput-card">
            <h3>吞吐量</h3>
            <div class="metric-value" id="throughput">0 req/s</div>
            <div class="metric-trend" id="throughput-trend"></div>
          </div>
        </div>
        
        <!-- 图表区域 -->
        <div class="charts-container">
          <div class="chart-panel">
            <h3>响应时间趋势</h3>
            <canvas id="response-time-chart"></canvas>
          </div>
          
          <div class="chart-panel">
            <h3>错误分布</h3>
            <canvas id="error-distribution-chart"></canvas>
          </div>
          
          <div class="chart-panel">
            <h3>内存使用趋势</h3>
            <canvas id="memory-usage-chart"></canvas>
          </div>
          
          <div class="chart-panel">
            <h3>网络性能</h3>
            <canvas id="network-performance-chart"></canvas>
          </div>
        </div>
        
        <!-- Web Vitals -->
        <div class="vitals-panel">
          <h3>Core Web Vitals</h3>
          <div class="vitals-grid">
            <div class="vital-item">
              <span class="vital-label">LCP</span>
              <span class="vital-value" id="lcp-value">0ms</span>
              <span class="vital-status" id="lcp-status">good</span>
            </div>
            <div class="vital-item">
              <span class="vital-label">FID</span>
              <span class="vital-value" id="fid-value">0ms</span>
              <span class="vital-status" id="fid-status">good</span>
            </div>
            <div class="vital-item">
              <span class="vital-label">CLS</span>
              <span class="vital-value" id="cls-value">0</span>
              <span class="vital-status" id="cls-status">good</span>
            </div>
            <div class="vital-item">
              <span class="vital-label">FCP</span>
              <span class="vital-value" id="fcp-value">0ms</span>
              <span class="vital-status" id="fcp-status">good</span>
            </div>
            <div class="vital-item">
              <span class="vital-label">TTFB</span>
              <span class="vital-value" id="ttfb-value">0ms</span>
              <span class="vital-status" id="ttfb-status">good</span>
            </div>
          </div>
        </div>
        
        <!-- 告警面板 -->
        <div class="alerts-panel">
          <h3>性能告警</h3>
          <div id="alerts-list"></div>
        </div>
      </div>
    `;
    
    this.addDashboardStyles();
    document.body.appendChild(dashboard);
  }
  
  addDashboardStyles() {
    const styles = `
      <style>
        #sentry-performance-dashboard {
          position: fixed;
          top: 20px;
          right: 20px;
          width: 800px;
          height: 600px;
          background: #fff;
          border: 1px solid #ddd;
          border-radius: 8px;
          box-shadow: 0 4px 12px rgba(0,0,0,0.1);
          z-index: 10000;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          overflow: hidden;
        }
        
        .dashboard-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px;
          background: #f8f9fa;
          border-bottom: 1px solid #ddd;
        }
        
        .dashboard-header h2 {
          margin: 0;
          font-size: 18px;
          color: #333;
        }
        
        .dashboard-controls button {
          margin-left: 8px;
          padding: 6px 12px;
          border: 1px solid #ddd;
          background: #fff;
          border-radius: 4px;
          cursor: pointer;
          font-size: 12px;
        }
        
        .dashboard-controls button:hover {
          background: #f0f0f0;
        }
        
        .dashboard-grid {
          padding: 16px;
          height: calc(100% - 80px);
          overflow-y: auto;
        }
        
        .metrics-cards {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 12px;
          margin-bottom: 20px;
        }
        
        .metric-card {
          padding: 16px;
          background: #f8f9fa;
          border-radius: 6px;
          text-align: center;
        }
        
        .metric-card h3 {
          margin: 0 0 8px 0;
          font-size: 12px;
          color: #666;
          text-transform: uppercase;
        }
        
        .metric-value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
          margin-bottom: 4px;
        }
        
        .metric-trend {
          font-size: 10px;
          color: #666;
        }
        
        .charts-container {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;
          margin-bottom: 20px;
        }
        
        .chart-panel {
          background: #f8f9fa;
          border-radius: 6px;
          padding: 16px;
        }
        
        .chart-panel h3 {
          margin: 0 0 12px 0;
          font-size: 14px;
          color: #333;
        }
        
        .chart-panel canvas {
          width: 100%;
          height: 150px;
        }
        
        .vitals-panel {
          background: #f8f9fa;
          border-radius: 6px;
          padding: 16px;
          margin-bottom: 20px;
        }
        
        .vitals-panel h3 {
          margin: 0 0 12px 0;
          font-size: 14px;
          color: #333;
        }
        
        .vitals-grid {
          display: grid;
          grid-template-columns: repeat(5, 1fr);
          gap: 12px;
        }
        
        .vital-item {
          text-align: center;
          padding: 8px;
          background: #fff;
          border-radius: 4px;
        }
        
        .vital-label {
          display: block;
          font-size: 10px;
          color: #666;
          text-transform: uppercase;
          margin-bottom: 4px;
        }
        
        .vital-value {
          display: block;
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 4px;
        }
        
        .vital-status {
          display: block;
          font-size: 10px;
          padding: 2px 6px;
          border-radius: 10px;
          text-transform: uppercase;
        }
        
        .vital-status.good {
          background: #d4edda;
          color: #155724;
        }
        
        .vital-status.needs-improvement {
          background: #fff3cd;
          color: #856404;
        }
        
        .vital-status.poor {
          background: #f8d7da;
          color: #721c24;
        }
        
        .alerts-panel {
          background: #f8f9fa;
          border-radius: 6px;
          padding: 16px;
        }
        
        .alerts-panel h3 {
          margin: 0 0 12px 0;
          font-size: 14px;
          color: #333;
        }
        
        .alert-item {
          padding: 8px 12px;
          margin-bottom: 8px;
          border-radius: 4px;
          font-size: 12px;
        }
        
        .alert-item.warning {
          background: #fff3cd;
          color: #856404;
          border-left: 4px solid #ffc107;
        }
        
        .alert-item.error {
          background: #f8d7da;
          color: #721c24;
          border-left: 4px solid #dc3545;
        }
      </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', styles);
  }
  
  setupCharts() {
    // 这里可以集成Chart.js或其他图表库
    // 为了简化，我们使用简单的Canvas绘制
    this.setupResponseTimeChart();
    this.setupErrorDistributionChart();
    this.setupMemoryUsageChart();
    this.setupNetworkPerformanceChart();
  }
  
  setupResponseTimeChart() {
    const canvas = document.getElementById('response-time-chart');
    const ctx = canvas.getContext('2d');
    
    this.charts.set('responseTime', {
      canvas,
      ctx,
      data: [],
      draw: () => this.drawLineChart(ctx, this.metrics.performance, 'responseTime')
    });
  }
  
  setupErrorDistributionChart() {
    const canvas = document.getElementById('error-distribution-chart');
    const ctx = canvas.getContext('2d');
    
    this.charts.set('errorDistribution', {
      canvas,
      ctx,
      data: [],
      draw: () => this.drawPieChart(ctx, this.metrics.errors)
    });
  }
  
  setupMemoryUsageChart() {
    const canvas = document.getElementById('memory-usage-chart');
    const ctx = canvas.getContext('2d');
    
    this.charts.set('memoryUsage', {
      canvas,
      ctx,
      data: [],
      draw: () => this.drawLineChart(ctx, this.metrics.memory, 'usage')
    });
  }
  
  setupNetworkPerformanceChart() {
    const canvas = document.getElementById('network-performance-chart');
    const ctx = canvas.getContext('2d');
    
    this.charts.set('networkPerformance', {
      canvas,
      ctx,
      data: [],
      draw: () => this.drawBarChart(ctx, this.metrics.network)
    });
  }
  
  drawLineChart(ctx, data, property) {
    const canvas = ctx.canvas;
    const width = canvas.width;
    const height = canvas.height;
    
    ctx.clearRect(0, 0, width, height);
    
    if (data.length < 2) return;
    
    const maxValue = Math.max(...data.map(d => d[property] || 0));
    const minValue = Math.min(...data.map(d => d[property] || 0));
    const range = maxValue - minValue || 1;
    
    ctx.strokeStyle = '#007bff';
    ctx.lineWidth = 2;
    ctx.beginPath();
    
    data.forEach((point, index) => {
      const x = (index / (data.length - 1)) * width;
      const y = height - ((point[property] - minValue) / range) * height;
      
      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    
    ctx.stroke();
  }
  
  drawPieChart(ctx, data) {
    const canvas = ctx.canvas;
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(centerX, centerY) - 10;
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    if (data.length === 0) return;
    
    const total = data.reduce((sum, item) => sum + item.count, 0);
    let currentAngle = 0;
    
    const colors = ['#ff6384', '#36a2eb', '#ffce56', '#4bc0c0', '#9966ff'];
    
    data.forEach((item, index) => {
      const sliceAngle = (item.count / total) * 2 * Math.PI;
      
      ctx.fillStyle = colors[index % colors.length];
      ctx.beginPath();
      ctx.moveTo(centerX, centerY);
      ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle);
      ctx.closePath();
      ctx.fill();
      
      currentAngle += sliceAngle;
    });
  }
  
  drawBarChart(ctx, data) {
    const canvas = ctx.canvas;
    const width = canvas.width;
    const height = canvas.height;
    
    ctx.clearRect(0, 0, width, height);
    
    if (data.length === 0) return;
    
    const maxValue = Math.max(...data.map(d => d.value || 0));
    const barWidth = width / data.length;
    
    ctx.fillStyle = '#28a745';
    
    data.forEach((item, index) => {
      const barHeight = (item.value / maxValue) * height;
      const x = index * barWidth;
      const y = height - barHeight;
      
      ctx.fillRect(x, y, barWidth - 2, barHeight);
    });
  }
  
  bindEvents() {
    document.getElementById('toggle-realtime')?.addEventListener('click', () => {
      this.toggleRealTime();
    });
    
    document.getElementById('export-data')?.addEventListener('click', () => {
      this.exportData();
    });
    
    document.getElementById('clear-data')?.addEventListener('click', () => {
      this.clearData();
    });
  }
  
  startRealTimeUpdates() {
    if (this.isRunning) return;
    
    this.isRunning = true;
    this.updateInterval = setInterval(() => {
      this.updateMetrics();
      this.updateCharts();
      this.checkAlerts();
    }, this.options.updateInterval);
  }
  
  stopRealTimeUpdates() {
    if (!this.isRunning) return;
    
    this.isRunning = false;
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
    }
  }
  
  toggleRealTime() {
    if (this.isRunning) {
      this.stopRealTimeUpdates();
      document.getElementById('toggle-realtime').textContent = '开始实时更新';
    } else {
      this.startRealTimeUpdates();
      document.getElementById('toggle-realtime').textContent = '停止实时更新';
    }
  }
  
  updateMetrics() {
    // 收集当前性能指标
    const currentMetrics = this.collectCurrentMetrics();
    
    // 更新各类指标数据
    this.addMetricData('performance', currentMetrics.performance);
    this.addMetricData('memory', currentMetrics.memory);
    this.addMetricData('network', currentMetrics.network);
    this.addMetricData('vitals', currentMetrics.vitals);
    
    // 更新显示
    this.updateMetricCards(currentMetrics);
    this.updateVitalsDisplay(currentMetrics.vitals);
  }
  
  collectCurrentMetrics() {
    const now = Date.now();
    
    // 性能指标
    const navigation = performance.getEntriesByType('navigation')[0];
    const responseTime = navigation ? navigation.responseEnd - navigation.requestStart : 0;
    
    // 内存指标
    const memoryInfo = performance.memory || {};
    const memoryUsage = memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit || 0;
    
    // Web Vitals（模拟数据，实际应该从真实的Web Vitals API获取）
    const vitals = {
      lcp: this.getRandomValue(1000, 4000),
      fid: this.getRandomValue(50, 300),
      cls: this.getRandomValue(0, 0.5),
      fcp: this.getRandomValue(800, 3000),
      ttfb: this.getRandomValue(200, 1000)
    };
    
    return {
      timestamp: now,
      performance: {
        responseTime,
        timestamp: now
      },
      memory: {
        usage: memoryUsage,
        used: memoryInfo.usedJSHeapSize,
        total: memoryInfo.totalJSHeapSize,
        limit: memoryInfo.jsHeapSizeLimit,
        timestamp: now
      },
      network: {
        value: this.getRandomValue(10, 100), // 模拟网络吞吐量
        timestamp: now
      },
      vitals
    };
  }
  
  getRandomValue(min, max) {
    return Math.random() * (max - min) + min;
  }
  
  addMetricData(type, data) {
    if (!this.metrics[type]) {
      this.metrics[type] = [];
    }
    
    this.metrics[type].push(data);
    
    // 限制数据点数量
    if (this.metrics[type].length > this.options.maxDataPoints) {
      this.metrics[type].shift();
    }
  }
  
  updateMetricCards(metrics) {
    // 更新响应时间
    const avgResponseTime = this.calculateAverage(this.metrics.performance, 'responseTime');
    document.getElementById('avg-response-time').textContent = `${Math.round(avgResponseTime)}ms`;
    
    // 更新错误率（模拟）
    const errorRate = this.getRandomValue(0, 0.1);
    document.getElementById('error-rate').textContent = `${(errorRate * 100).toFixed(2)}%`;
    
    // 更新内存使用率
    const memoryUsage = metrics.memory.usage * 100;
    document.getElementById('memory-usage').textContent = `${memoryUsage.toFixed(1)}%`;
    
    // 更新吞吐量（模拟）
    const throughput = this.getRandomValue(10, 100);
    document.getElementById('throughput').textContent = `${Math.round(throughput)} req/s`;
  }
  
  updateVitalsDisplay(vitals) {
    // 更新LCP
    document.getElementById('lcp-value').textContent = `${Math.round(vitals.lcp)}ms`;
    document.getElementById('lcp-status').className = `vital-status ${this.getVitalStatus('lcp', vitals.lcp)}`;
    
    // 更新FID
    document.getElementById('fid-value').textContent = `${Math.round(vitals.fid)}ms`;
    document.getElementById('fid-status').className = `vital-status ${this.getVitalStatus('fid', vitals.fid)}`;
    
    // 更新CLS
    document.getElementById('cls-value').textContent = vitals.cls.toFixed(3);
    document.getElementById('cls-status').className = `vital-status ${this.getVitalStatus('cls', vitals.cls)}`;
    
    // 更新FCP
    document.getElementById('fcp-value').textContent = `${Math.round(vitals.fcp)}ms`;
    document.getElementById('fcp-status').className = `vital-status ${this.getVitalStatus('fcp', vitals.fcp)}`;
    
    // 更新TTFB
    document.getElementById('ttfb-value').textContent = `${Math.round(vitals.ttfb)}ms`;
    document.getElementById('ttfb-status').className = `vital-status ${this.getVitalStatus('ttfb', vitals.ttfb)}`;
  }
  
  getVitalStatus(metric, value) {
    const thresholds = {
      lcp: { good: 2500, poor: 4000 },
      fid: { good: 100, poor: 300 },
      cls: { good: 0.1, poor: 0.25 },
      fcp: { good: 1800, poor: 3000 },
      ttfb: { good: 800, poor: 1800 }
    };
    
    const threshold = thresholds[metric];
    if (!threshold) return 'good';
    
    if (value <= threshold.good) return 'good';
    if (value <= threshold.poor) return 'needs-improvement';
    return 'poor';
  }
  
  calculateAverage(data, property) {
    if (data.length === 0) return 0;
    
    const sum = data.reduce((total, item) => total + (item[property] || 0), 0);
    return sum / data.length;
  }
  
  updateCharts() {
    this.charts.forEach(chart => {
      chart.draw();
    });
  }
  
  checkAlerts() {
    if (!this.options.enableAlerts) return;
    
    const currentMetrics = this.metrics;
    const thresholds = this.options.thresholds;
    
    // 检查响应时间
    const avgResponseTime = this.calculateAverage(currentMetrics.performance, 'responseTime');
    if (avgResponseTime > thresholds.responseTime) {
      this.addAlert('warning', `响应时间过高: ${Math.round(avgResponseTime)}ms`);
    }
    
    // 检查内存使用率
    if (currentMetrics.memory.length > 0) {
      const latestMemory = currentMetrics.memory[currentMetrics.memory.length - 1];
      if (latestMemory.usage > thresholds.memoryUsage) {
        this.addAlert('error', `内存使用率过高: ${(latestMemory.usage * 100).toFixed(1)}%`);
      }
    }
  }
  
  addAlert(type, message) {
    const alert = {
      id: Date.now(),
      type,
      message,
      timestamp: new Date().toLocaleTimeString()
    };
    
    this.alerts.unshift(alert);
    
    // 限制告警数量
    if (this.alerts.length > 10) {
      this.alerts.pop();
    }
    
    this.updateAlertsDisplay();
  }
  
  updateAlertsDisplay() {
    const alertsList = document.getElementById('alerts-list');
    if (!alertsList) return;
    
    alertsList.innerHTML = this.alerts.map(alert => `
      <div class="alert-item ${alert.type}">
        <strong>${alert.timestamp}</strong> - ${alert.message}
      </div>
    `).join('');
  }
  
  exportData() {
    const data = {
      metrics: this.metrics,
      alerts: this.alerts,
      exportTime: new Date().toISOString()
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const a = document.createElement('a');
    a.href = url;
    a.download = `sentry-performance-data-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    
    URL.revokeObjectURL(url);
  }
  
  clearData() {
    this.metrics = {
      performance: [],
      errors: [],
      memory: [],
      network: [],
      vitals: []
    };
    
    this.alerts = [];
    this.updateCharts();
    this.updateAlertsDisplay();
  }
  
  // 销毁仪表板
  destroy() {
    this.stopRealTimeUpdates();
    
    const dashboard = document.getElementById('sentry-performance-dashboard');
    if (dashboard) {
      dashboard.remove();
    }
  }
}
```

## 6. 性能优化最佳实践

### 6.1 优化策略总结

```javascript
// 性能优化最佳实践指南
class SentryOptimizationGuide {
  static getBestPractices() {
    return {
      // 1. 采样策略优化
      sampling: {
        recommendations: [
          '根据应用规模调整采样率，避免数据过载',
          '对关键用户和错误使用更高的采样率',
          '实施智能采样，根据错误类型动态调整',
          '定期评估和调整采样策略的效果'
        ],
        implementation: {
          errorSampling: 1.0, // 错误事件100%采样
          performanceSampling: 0.1, // 性能事件10%采样
          userSampling: 0.2, // VIP用户20%采样
          adaptiveSampling: true // 启用自适应采样
        }
      },
      
      // 2. 数据传输优化
      dataTransmission: {
        recommendations: [
          '启用数据压缩减少传输大小',
          '使用批量传输减少请求频率',
          '实施智能重试机制处理网络问题',
          '根据网络条件调整传输策略'
        ],
        implementation: {
          compression: true,
          batchSize: 10,
          batchTimeout: 5000,
          retryAttempts: 3,
          adaptiveNetworking: true
        }
      },
      
      // 3. 内存管理优化
      memoryManagement: {
        recommendations: [
          '使用对象池减少垃圾回收压力',
          '实施内存监控和自动清理',
          '使用WeakRef避免内存泄漏',
          '定期清理过期数据和缓存'
        ],
        implementation: {
          objectPooling: true,
          memoryMonitoring: true,
          weakReferences: true,
          autoCleanup: true
        }
      },
      
      // 4. 缓存策略优化
      caching: {
        recommendations: [
          '实施多层缓存提高数据访问速度',
          '使用LRU算法管理缓存淘汰',
          '根据数据类型选择合适的存储方式',
          '定期清理过期缓存数据'
        ],
        implementation: {
          multiLayerCache: true,
          lruEviction: true,
          storageOptimization: true,
          cacheExpiration: true
        }
      },
      
      // 5. 网络优化
      networking: {
        recommendations: [
          '使用连接池减少连接开销',
          '实施请求优先级管理',
          '根据网络条件调整并发数',
          '使用CDN加速数据传输'
        ],
        implementation: {
          connectionPooling: true,
          requestPriority: true,
          adaptiveConcurrency: true,
          cdnAcceleration: true
        }
      }
    };
  }
  
  static getPerformanceMetrics() {
    return {
      // 关键性能指标
      kpis: {
        responseTime: {
          target: '< 3s',
          measurement: '平均响应时间',
          importance: 'high'
        },
        errorRate: {
          target: '< 1%',
          measurement: '错误率',
          importance: 'critical'
        },
        memoryUsage: {
          target: '< 80%',
          measurement: '内存使用率',
          importance: 'medium'
        },
        throughput: {
          target: '> 100 req/s',
          measurement: '吞吐量',
          importance: 'medium'
        }
      },
      
      // Web Vitals目标
      webVitals: {
        lcp: { good: 2500, poor: 4000, unit: 'ms' },
        fid: { good: 100, poor: 300, unit: 'ms' },
        cls: { good: 0.1, poor: 0.25, unit: 'score' },
        fcp: { good: 1800, poor: 3000, unit: 'ms' },
        ttfb: { good: 800, poor: 1800, unit: 'ms' }
      }
    };
  }
  
  static getOptimizationChecklist() {
    return [
      {
        category: '配置优化',
        items: [
          '✓ 设置合适的采样率',
          '✓ 配置错误过滤规则',
          '✓ 启用性能监控',
          '✓ 设置用户上下文'
        ]
      },
      {
        category: '传输优化',
        items: [
          '✓ 启用数据压缩',
          '✓ 配置批量传输',
          '✓ 实施重试机制',
          '✓ 优化网络设置'
        ]
      },
      {
        category: '内存优化',
        items: [
          '✓ 使用对象池',
          '✓ 监控内存使用',
          '✓ 清理过期数据',
          '✓ 避免内存泄漏'
        ]
      },
      {
        category: '缓存优化',
        items: [
          '✓ 实施多层缓存',
          '✓ 配置缓存策略',
          '✓ 监控缓存性能',
          '✓ 定期清理缓存'
        ]
      },
      {
        category: '监控优化',
        items: [
          '✓ 设置性能告警',
          '✓ 监控关键指标',
          '✓ 分析性能趋势',
          '✓ 优化仪表板'
        ]
      }
    ];
  }
}
```

## 7. 核心价值与实施建议

### 7.1 核心价值

1. **性能提升**：通过系统性的优化策略，显著提升Sentry监控系统的性能表现
2. **资源节约**：优化内存使用和网络传输，降低系统资源消耗
3. **用户体验**：减少监控对应用性能的影响，提升最终用户体验
4. **成本控制**：通过智能采样和数据压缩，有效控制监控成本
5. **可扩展性**：构建可扩展的监控架构，支持业务快速增长

### 7.2 实施建议

1. **分阶段实施**：
   - 第一阶段：基础性能监控和采样优化
   - 第二阶段：数据传输和缓存优化
   - 第三阶段：内存管理和网络优化
   - 第四阶段：高级监控和告警系统

2. **性能基准测试**：
   - 建立性能基准线
   - 定期进行性能测试
   - 监控优化效果
   - 持续改进策略

3. **团队培训**：
   - 性能优化最佳实践培训
   - 监控工具使用培训
   - 问题诊断和解决培训
   - 持续学习和改进

### 7.3 未来发展趋势

1. **AI驱动优化**：利用机器学习自动优化采样和缓存策略
2. **边缘计算**：在边缘节点进行数据预处理和过滤
3. **实时分析**：提供更实时的性能分析和告警
4. **智能预测**：基于历史数据预测性能问题
5. **自动化运维**：实现监控系统的自动化运维和优化

## 总结

前端Sentry性能优化与调优是一个系统性工程，需要从多个维度进行综合考虑和优化。通过实施智能采样策略、数据传输优化、内存管理优化、缓存策略优化和网络优化等措施，可以显著提升监控系统的性能表现，降低资源消耗，提升用户体验。

关键成功因素包括：
- 建立完善的性能监控体系
- 实施科学的优化策略
- 持续监控和改进
- 团队能力建设
- 技术创新应用

随着技术的不断发展，性能优化将更加智能化和自动化，为构建高效、可靠的前端监控系统提供强有力的支撑。

### 1.2 智能采样策略

```javascript
// 智能采样管理器
class IntelligentSamplingManager {
  constructor(options = {}) {
    this.options = {
      baseSampleRate: 0.1, // 基础采样率 10%
      errorSampleRate: 1.0, // 错误采样率 100%
      performanceSampleRate: 0.05, // 性能采样率 5%
      adaptiveSampling: true, // 启用自适应采样
      userTierSampling: true, // 启用用户分层采样
      ...options
    };
    
    this.currentSampleRate = this.options.baseSampleRate;
    this.samplingHistory = [];
    this.userTier = this.determineUserTier();
    
    this.init();
  }
  
  init() {
    if (this.options.adaptiveSampling) {
      this.startAdaptiveSampling();
    }
  }
  
  shouldSample(eventType, context = {}) {
    // 错误事件总是采样
    if (eventType === 'error' || eventType === 'exception') {
      return Math.random() < this.options.errorSampleRate;
    }
    
    // 性能事件使用专门的采样率
    if (eventType === 'performance' || eventType === 'web-vital') {
      return Math.random() < this.options.performanceSampleRate;
    }
    
    // 用户分层采样
    if (this.options.userTierSampling) {
      const tierRate = this.getTierSampleRate();
      if (Math.random() < tierRate) {
        return true;
      }
    }
    
    // 自适应采样
    if (this.options.adaptiveSampling) {
      return Math.random() < this.currentSampleRate;
    }
    
    // 基础采样
    return Math.random() < this.options.baseSampleRate;
  }
  
  determineUserTier() {
    // 根据用户特征确定用户层级
    const factors = {
      // 设备性能
      deviceMemory: navigator.deviceMemory || 4,
      // 网络连接
      connectionType: this.getConnectionType(),
      // 用户行为
      sessionDuration: this.getSessionDuration(),
      // 错误历史
      errorHistory: this.getErrorHistory()
    };
    
    let score = 0;
    
    // 设备内存评分
    if (factors.deviceMemory >= 8) score += 3;
    else if (factors.deviceMemory >= 4) score += 2;
    else score += 1;
    
    // 网络连接评分
    if (factors.connectionType === '4g' || factors.connectionType === 'wifi') score += 2;
    else score += 1;
    
    // 会话时长评分
    if (factors.sessionDuration > 300000) score += 2; // 5分钟以上
    else if (factors.sessionDuration > 60000) score += 1; // 1分钟以上
    
    // 错误历史评分（错误多的用户需要更多监控）
    if (factors.errorHistory > 5) score += 3;
    else if (factors.errorHistory > 2) score += 2;
    else score += 1;
    
    // 确定层级
    if (score >= 8) return 'premium';
    if (score >= 6) return 'standard';
    return 'basic';
  }
  
  getTierSampleRate() {
    const tierRates = {
      premium: 0.3,   // 高价值用户 30%
      standard: 0.15, // 标准用户 15%
      basic: 0.05     // 基础用户 5%
    };
    
    return tierRates[this.userTier] || this.options.baseSampleRate;
  }
  
  getConnectionType() {
    if ('connection' in navigator) {
      return navigator.connection.effectiveType || 'unknown';
    }
    return 'unknown';
  }
  
  getSessionDuration() {
    const sessionStart = sessionStorage.getItem('session_start');
    if (sessionStart) {
      return Date.now() - parseInt(sessionStart);
    }
    
    // 记录会话开始时间
    sessionStorage.setItem('session_start', Date.now().toString());
    return 0;
  }
  
  getErrorHistory() {
    const errorHistory = localStorage.getItem('sentry_error_history');
    if (errorHistory) {
      try {
        const history = JSON.parse(errorHistory);
        // 只统计最近24小时的错误
        const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000;
        return history.filter(timestamp => timestamp > oneDayAgo).length;
      } catch (error) {
        return 0;
      }
    }
    return 0;
  }
  
  recordError() {
    // 记录错误到历史
    const errorHistory = this.getErrorHistoryArray();
    errorHistory.push(Date.now());
    
    // 只保留最近100个错误记录
    if (errorHistory.length > 100) {
      errorHistory.splice(0, errorHistory.length - 100);
    }
    
    localStorage.setItem('sentry_error_history', JSON.stringify(errorHistory));
  }
  
  getErrorHistoryArray() {
    const errorHistory = localStorage.getItem('sentry_error_history');
    if (errorHistory) {
      try {
        return JSON.parse(errorHistory);
      } catch (error) {
        return [];
      }
    }
    return [];
  }
  
  startAdaptiveSampling() {
    // 每分钟调整一次采样率
    setInterval(() => {
      this.adjustSampleRate();
    }, 60000);
  }
  
  adjustSampleRate() {
    const systemLoad = this.getSystemLoad();
    const networkCondition = this.getNetworkCondition();
    const errorRate = this.getRecentErrorRate();
    
    let adjustmentFactor = 1.0;
    
    // 根据系统负载调整
    if (systemLoad > 0.8) {
      adjustmentFactor *= 0.5; // 高负载时减少采样
    } else if (systemLoad < 0.3) {
      adjustmentFactor *= 1.5; // 低负载时增加采样
    }
    
    // 根据网络条件调整
    if (networkCondition === 'slow') {
      adjustmentFactor *= 0.7; // 慢网络时减少采样
    } else if (networkCondition === 'fast') {
      adjustmentFactor *= 1.2; // 快网络时增加采样
    }
    
    // 根据错误率调整
    if (errorRate > 0.05) {
      adjustmentFactor *= 2.0; // 高错误率时增加采样
    }
    
    // 计算新的采样率
    const newSampleRate = Math.min(
      Math.max(
        this.options.baseSampleRate * adjustmentFactor,
        0.01 // 最小采样率 1%
      ),
      0.5 // 最大采样率 50%
    );
    
    this.currentSampleRate = newSampleRate;
    
    // 记录采样历史
    this.samplingHistory.push({
      timestamp: Date.now(),
      sampleRate: newSampleRate,
      systemLoad,
      networkCondition,
      errorRate,
      adjustmentFactor
    });
    
    // 只保留最近24小时的历史
    const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000;
    this.samplingHistory = this.samplingHistory.filter(
      record => record.timestamp > oneDayAgo
    );
  }
  
  getSystemLoad() {
    // 估算系统负载
    let load = 0;
    
    // 基于内存使用情况
    if ('memory' in performance) {
      const memoryInfo = performance.memory;
      const memoryUsage = memoryInfo.usedJSHeapSize / memoryInfo.jsHeapSizeLimit;
      load += memoryUsage * 0.4;
    }
    
    // 基于CPU使用情况（通过长任务检测）
    const longTaskCount = this.getLongTaskCount();
    if (longTaskCount > 5) {
      load += 0.3;
    } else if (longTaskCount > 2) {
      load += 0.2;
    }
    
    // 基于DOM复杂度
    const domComplexity = document.querySelectorAll('*').length;
    if (domComplexity > 5000) {
      load += 0.2;
    } else if (domComplexity > 2000) {
      load += 0.1;
    }
    
    return Math.min(load, 1.0);
  }
  
  getLongTaskCount() {
    // 获取最近的长任务数量
    const longTasks = performance.getEntriesByType('longtask');
    const recentTasks = longTasks.filter(
      task => task.startTime > performance.now() - 60000 // 最近1分钟
    );
    return recentTasks.length;
  }
  
  getNetworkCondition() {
    if ('connection' in navigator) {
      const connection = navigator.connection;
      const effectiveType = connection.effectiveType;
      
      if (effectiveType === '4g' && connection.downlink > 10) {
        return 'fast';
      } else if (effectiveType === '3g' || effectiveType === '2g') {
        return 'slow';
      }
    }
    
    return 'normal';
  }
  
  getRecentErrorRate() {
    const errorHistory = this.getErrorHistoryArray();
    const recentErrors = errorHistory.filter(
      timestamp => timestamp > Date.now() - 300000 // 最近5分钟
    );
    
    // 假设每分钟大约有100个事件
    const estimatedEvents = 5 * 100;
    return recentErrors.length / estimatedEvents;
  }
  
  // 获取采样统计信息
  getSamplingStats() {
    return {
      currentSampleRate: this.currentSampleRate,
      userTier: this.userTier,
      tierSampleRate: this.getTierSampleRate(),
      systemLoad: this.getSystemLoad(),
      networkCondition: this.getNetworkCondition(),
      recentErrorRate: this.getRecentErrorRate(),
      samplingHistory: this.samplingHistory.slice(-10) // 最近10条记录
    };
  }
}
```

## 2. 数据传输优化

### 2.1 批量传输管理器

```javascript
// 批量传输管理器
class BatchTransmissionManager {
  constructor(options = {}) {
    this.options = {
      batchSize: 50, // 批次大小
      maxBatchWait: 10000, // 最大等待时间 10秒
      maxRetries: 3, // 最大重试次数
      retryDelay: 1000, // 重试延迟
      compressionEnabled: true, // 启用压缩
      priorityQueue: true, // 启用优先级队列
      ...options
    };
    
    this.queue = [];
    this.priorityQueue = [];
    this.transmissionTimer = null;
    this.retryQueue = new Map();
    
    this.init();
  }
  
  init() {
    this.startBatchTimer();
    this.bindEventListeners();
  }
  
  addEvent(event, priority = 'normal') {
    // 添加元数据
    const enrichedEvent = {
      ...event,
      id: this.generateEventId(),
      timestamp: Date.now(),
      priority,
      retryCount: 0
    };
    
    // 根据优先级添加到不同队列
    if (priority === 'high' || priority === 'critical') {
      this.priorityQueue.push(enrichedEvent);
      
      // 高优先级事件立即发送
      if (priority === 'critical') {
        this.flushBatch(true);
      }
    } else {
      this.queue.push(enrichedEvent);
    }
    
    // 检查批次大小
    if (this.getTotalQueueSize() >= this.options.batchSize) {
      this.flushBatch();
    }
  }
  
  getTotalQueueSize() {
    return this.queue.length + this.priorityQueue.length;
  }
  
  generateEventId() {
    return `event_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  startBatchTimer() {
    this.transmissionTimer = setInterval(() => {
      if (this.getTotalQueueSize() > 0) {
        this.flushBatch();
      }
    }, this.options.maxBatchWait);
  }
  
  async flushBatch(force = false) {
    if (this.getTotalQueueSize() === 0 && !force) {
      return;
    }
    
    // 合并优先级队列和普通队列
    const batch = [
      ...this.priorityQueue.splice(0, this.options.batchSize),
      ...this.queue.splice(0, this.options.batchSize - this.priorityQueue.length)
    ];
    
    if (batch.length === 0) return;
    
    try {
      await this.transmitBatch(batch);
      console.log(`Successfully transmitted batch of ${batch.length} events`);
    } catch (error) {
      console.error('Batch transmission failed:', error);
      await this.handleTransmissionError(batch, error);
    }
  }
  
  async transmitBatch(batch) {
    // 准备传输数据
    const transmissionData = {
      events: batch,
      metadata: {
        batchId: this.generateBatchId(),
        timestamp: Date.now(),
        userAgent: navigator.userAgent,
        url: window.location.href,
        sessionId: this.getSessionId()
      }
    };
    
    // 压缩数据
    let payload = JSON.stringify(transmissionData);
    let headers = {
      'Content-Type': 'application/json'
    };
    
    if (this.options.compressionEnabled) {
      try {
        payload = await this.compressData(payload);
        headers['Content-Encoding'] = 'gzip';
      } catch (error) {
        console.warn('Compression failed, using uncompressed data:', error);
      }
    }
    
    // 发送数据
    const response = await this.sendWithRetry(payload, headers);
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    return response;
  }
  
  async compressData(data) {
    if ('CompressionStream' in window) {
      const stream = new CompressionStream('gzip');
      const writer = stream.writable.getWriter();
      const reader = stream.readable.getReader();
      
      writer.write(new TextEncoder().encode(data));
      writer.close();
      
      const chunks = [];
      let done = false;
      
      while (!done) {
        const { value, done: readerDone } = await reader.read();
        done = readerDone;
        if (value) chunks.push(value);
      }
      
      return new Uint8Array(chunks.reduce((acc, chunk) => [...acc, ...chunk], []));
    }
    
    // 回退到简单的字符串压缩
    return this.simpleCompress(data);
  }
  
  simpleCompress(data) {
    // 简单的重复字符压缩
    return data.replace(/(.)\1{2,}/g, (match, char) => {
      return `${char}*${match.length}`;
    });
  }
  
  async sendWithRetry(payload, headers, retryCount = 0) {
    try {
      const response = await fetch('/api/events/batch', {
        method: 'POST',
        headers,
        body: payload
      });
      
      return response;
    } catch (error) {
      if (retryCount < this.options.maxRetries) {
        const delay = this.options.retryDelay * Math.pow(2, retryCount); // 指数退避
        await this.sleep(delay);
        return this.sendWithRetry(payload, headers, retryCount + 1);
      }
      throw error;
    }
  }
  
  async handleTransmissionError(batch, error) {
    // 根据错误类型决定处理策略
    if (this.isRetryableError(error)) {
      // 可重试错误：加入重试队列
      for (const event of batch) {
        event.retryCount = (event.retryCount || 0) + 1;
        
        if (event.retryCount <= this.options.maxRetries) {
          this.scheduleRetry(event);
        } else {
          // 超过重试次数，记录到本地存储
          this.storeFailedEvent(event);
        }
      }
    } else {
      // 不可重试错误：直接丢弃或存储
      for (const event of batch) {
        this.storeFailedEvent(event);
      }
    }
  }
  
  isRetryableError(error) {
    // 网络错误、超时、5xx服务器错误等可重试
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      return true; // 网络错误
    }
    
    if (error.message.includes('HTTP 5')) {
      return true; // 5xx服务器错误
    }
    
    if (error.message.includes('timeout')) {
      return true; // 超时错误
    }
    
    return false;
  }
  
  scheduleRetry(event) {
    const retryDelay = this.options.retryDelay * Math.pow(2, event.retryCount);
    
    setTimeout(() => {
      // 重新添加到队列
      if (event.priority === 'high' || event.priority === 'critical') {
        this.priorityQueue.push(event);
      } else {
        this.queue.push(event);
      }
    }, retryDelay);
  }
  
  storeFailedEvent(event) {
    try {
      const failedEvents = this.getFailedEvents();
      failedEvents.push({
        ...event,
        failedAt: Date.now()
      });
      
      // 只保留最近1000个失败事件
      if (failedEvents.length > 1000) {
        failedEvents.splice(0, failedEvents.length - 1000);
      }
      
      localStorage.setItem('sentry_failed_events', JSON.stringify(failedEvents));
    } catch (error) {
      console.error('Failed to store failed event:', error);
    }
  }
  
  getFailedEvents() {
    try {
      const stored = localStorage.getItem('sentry_failed_events');
      return stored ? JSON.parse(stored) : [];
    } catch (error) {
      return [];
    }
  }
  
  // 重试发送失败的事件
  async retryFailedEvents() {
    const failedEvents = this.getFailedEvents();
    if (failedEvents.length === 0) return;
    
    console.log(`Retrying ${failedEvents.length} failed events`);
    
    // 清空本地存储
    localStorage.removeItem('sentry_failed_events');
    
    // 重新添加到队列
    for (const event of failedEvents) {
      event.retryCount = 0; // 重置重试计数
      this.addEvent(event, event.priority);
    }
  }
  
  generateBatchId() {
    return `batch_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  getSessionId() {
    let sessionId = sessionStorage.getItem('sentry_session_id');
    if (!sessionId) {
      sessionId = `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      sessionStorage.setItem('sentry_session_id', sessionId);
    }
    return sessionId;
  }
  
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
  
  bindEventListeners() {
    // 页面卸载时发送剩余事件
    window.addEventListener('beforeunload', () => {
      this.flushBatch(true);
    });
    
    // 页面隐藏时发送事件
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') {
        this.flushBatch(true);
      }
    });
    
    // 网络状态变化时重试失败事件
    window.addEventListener('online', () => {
      this.retryFailedEvents();
    });
  }
  
  // 获取传输统计信息
  getTransmissionStats() {
    return {
      queueSize: this.queue.length,
      priorityQueueSize: this.priorityQueue.length,
      failedEventsCount: this.getFailedEvents().length,
      retryQueueSize: this.retryQueue.size
    };
  }
  
  // 清理资源
  destroy() {
    if (this.transmissionTimer) {
      clearInterval(this.transmissionTimer);
    }
    
    // 发送剩余事件
    this.flushBatch(true);
  }
}
```