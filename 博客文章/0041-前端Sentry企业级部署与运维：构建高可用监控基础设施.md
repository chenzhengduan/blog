# 前端Sentry企业级部署与运维：构建高可用监控基础设施

在企业级应用中，前端监控系统的稳定性和可用性直接影响到业务的正常运行。本文将深入探讨如何构建企业级的Sentry部署架构，实现高可用、可扩展的前端监控基础设施。

## 1. 企业级架构设计

### 1.1 高可用架构设计

```javascript
// 企业级Sentry配置管理器
class EnterpriseSentryConfig {
  constructor(options = {}) {
    this.options = {
      environment: process.env.NODE_ENV || 'production',
      region: process.env.SENTRY_REGION || 'us-east-1',
      enableFailover: true,
      enableLoadBalancing: true,
      enableCaching: true,
      enableCompression: true,
      ...options
    };
    
    this.endpoints = this.initializeEndpoints();
    this.loadBalancer = new SentryLoadBalancer(this.endpoints);
    this.failoverManager = new FailoverManager(this.endpoints);
    this.cacheManager = new CacheManager();
    this.compressionManager = new CompressionManager();
  }
  
  initializeEndpoints() {
    const baseConfig = {
      production: {
        primary: {
          dsn: process.env.SENTRY_DSN_PRIMARY,
          region: 'us-east-1',
          priority: 1,
          healthCheck: '/health'
        },
        secondary: {
          dsn: process.env.SENTRY_DSN_SECONDARY,
          region: 'us-west-2',
          priority: 2,
          healthCheck: '/health'
        },
        tertiary: {
          dsn: process.env.SENTRY_DSN_TERTIARY,
          region: 'eu-west-1',
          priority: 3,
          healthCheck: '/health'
        }
      },
      staging: {
        primary: {
          dsn: process.env.SENTRY_DSN_STAGING,
          region: 'us-east-1',
          priority: 1,
          healthCheck: '/health'
        }
      },
      development: {
        primary: {
          dsn: process.env.SENTRY_DSN_DEV,
          region: 'local',
          priority: 1,
          healthCheck: '/health'
        }
      }
    };
    
    return baseConfig[this.options.environment] || baseConfig.development;
  }
  
  // 获取Sentry配置
  getSentryConfig() {
    const activeEndpoint = this.loadBalancer.getActiveEndpoint();
    
    return {
      dsn: activeEndpoint.dsn,
      environment: this.options.environment,
      release: this.getReleaseVersion(),
      
      // 性能配置
      sampleRate: this.getSampleRate(),
      tracesSampleRate: this.getTracesSampleRate(),
      
      // 传输配置
      transport: this.createCustomTransport(),
      
      // 集成配置
      integrations: this.getIntegrations(),
      
      // 错误处理
      beforeSend: this.createBeforeSend(),
      beforeBreadcrumb: this.createBeforeBreadcrumb(),
      
      // 标签和上下文
      initialScope: {
        tags: this.getDefaultTags(),
        contexts: this.getDefaultContexts()
      }
    };
  }
  
  createCustomTransport() {
    return (options) => {
      return {
        send: async (request) => {
          try {
            // 1. 压缩数据
            if (this.options.enableCompression) {
              request = await this.compressionManager.compress(request);
            }
            
            // 2. 缓存检查
            if (this.options.enableCaching) {
              const cached = await this.cacheManager.get(request);
              if (cached) {
                return cached;
              }
            }
            
            // 3. 负载均衡
            const endpoint = this.loadBalancer.getActiveEndpoint();
            
            // 4. 发送请求
            const response = await this.sendWithRetry(request, endpoint);
            
            // 5. 缓存响应
            if (this.options.enableCaching && response.status === 200) {
              await this.cacheManager.set(request, response);
            }
            
            return response;
            
          } catch (error) {
            // 故障转移
            if (this.options.enableFailover) {
              return await this.failoverManager.handleFailure(request, error);
            }
            throw error;
          }
        }
      };
    };
  }
  
  async sendWithRetry(request, endpoint, maxRetries = 3) {
    let lastError;
    
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        const response = await fetch(endpoint.url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'User-Agent': this.getUserAgent(),
            'X-Sentry-Auth': this.getAuthHeader(endpoint),
            ...this.getCustomHeaders()
          },
          body: JSON.stringify(request)
        });
        
        if (response.ok) {
          return response;
        }
        
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        
      } catch (error) {
        lastError = error;
        
        // 记录重试
        console.warn(`Sentry request attempt ${attempt} failed:`, error.message);
        
        // 指数退避
        if (attempt < maxRetries) {
          const delay = Math.pow(2, attempt) * 1000 + Math.random() * 1000;
          await this.delay(delay);
        }
      }
    }
    
    throw lastError;
  }
  
  getSampleRate() {
    const rates = {
      production: 0.1,
      staging: 0.5,
      development: 1.0
    };
    
    return rates[this.options.environment] || 0.1;
  }
  
  getTracesSampleRate() {
    const rates = {
      production: 0.01,
      staging: 0.1,
      development: 0.5
    };
    
    return rates[this.options.environment] || 0.01;
  }
  
  getReleaseVersion() {
    return process.env.SENTRY_RELEASE || 
           process.env.npm_package_version || 
           'unknown';
  }
  
  getDefaultTags() {
    return {
      environment: this.options.environment,
      region: this.options.region,
      version: this.getReleaseVersion(),
      deployment: process.env.DEPLOYMENT_ID || 'unknown',
      cluster: process.env.CLUSTER_NAME || 'default'
    };
  }
  
  getDefaultContexts() {
    return {
      deployment: {
        id: process.env.DEPLOYMENT_ID,
        timestamp: process.env.DEPLOYMENT_TIMESTAMP,
        branch: process.env.GIT_BRANCH,
        commit: process.env.GIT_COMMIT
      },
      infrastructure: {
        region: this.options.region,
        cluster: process.env.CLUSTER_NAME,
        node: process.env.NODE_NAME,
        pod: process.env.POD_NAME
      }
    };
  }
  
  getUserAgent() {
    return `SentryEnterprise/1.0 (${this.options.environment}; ${this.options.region})`;
  }
  
  getAuthHeader(endpoint) {
    const timestamp = Math.floor(Date.now() / 1000);
    const key = this.extractKeyFromDsn(endpoint.dsn);
    
    return `Sentry sentry_version=7, sentry_timestamp=${timestamp}, sentry_key=${key}`;
  }
  
  getCustomHeaders() {
    return {
      'X-Request-ID': this.generateRequestId(),
      'X-Client-Version': this.getReleaseVersion(),
      'X-Environment': this.options.environment
    };
  }
  
  extractKeyFromDsn(dsn) {
    const match = dsn.match(/https:\/\/([^@]+)@/);
    return match ? match[1] : '';
  }
  
  generateRequestId() {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
```

## 3. 缓存与压缩优化

### 3.1 缓存管理器

```javascript
// 缓存管理器
class CacheManager {
  constructor(options = {}) {
    this.options = {
      maxSize: 100, // 最大缓存条目数
      ttl: 300000, // 5分钟TTL
      enableCompression: true,
      storageType: 'memory', // 'memory', 'localStorage', 'indexedDB'
      ...options
    };
    
    this.cache = new Map();
    this.accessTimes = new Map();
    this.storage = this.initializeStorage();
    
    // 定期清理过期缓存
    this.startCleanupTimer();
  }
  
  initializeStorage() {
    switch (this.options.storageType) {
      case 'localStorage':
        return new LocalStorageAdapter();
      case 'indexedDB':
        return new IndexedDBAdapter();
      default:
        return new MemoryStorageAdapter();
    }
  }
  
  async get(request) {
    const key = this.generateKey(request);
    
    try {
      // 首先检查内存缓存
      if (this.cache.has(key)) {
        const cached = this.cache.get(key);
        
        if (this.isValid(cached)) {
          this.accessTimes.set(key, Date.now());
          return cached.data;
        } else {
          this.cache.delete(key);
          this.accessTimes.delete(key);
        }
      }
      
      // 检查持久化存储
      const stored = await this.storage.get(key);
      if (stored && this.isValid(stored)) {
        // 恢复到内存缓存
        this.cache.set(key, stored);
        this.accessTimes.set(key, Date.now());
        return stored.data;
      }
      
      return null;
      
    } catch (error) {
      console.warn('Cache get error:', error);
      return null;
    }
  }
  
  async set(request, response) {
    const key = this.generateKey(request);
    
    try {
      const cacheEntry = {
        data: response,
        timestamp: Date.now(),
        ttl: this.options.ttl,
        size: this.calculateSize(response)
      };
      
      // 检查缓存大小限制
      await this.ensureCapacity();
      
      // 存储到内存缓存
      this.cache.set(key, cacheEntry);
      this.accessTimes.set(key, Date.now());
      
      // 异步存储到持久化存储
      this.storage.set(key, cacheEntry).catch(error => {
        console.warn('Cache persistence error:', error);
      });
      
    } catch (error) {
      console.warn('Cache set error:', error);
    }
  }
  
  generateKey(request) {
    // 基于请求内容生成缓存键
    const keyData = {
      url: request.url,
      method: request.method,
      headers: request.headers,
      body: request.body
    };
    
    return this.hashObject(keyData);
  }
  
  hashObject(obj) {
    const str = JSON.stringify(obj, Object.keys(obj).sort());
    let hash = 0;
    
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // 转换为32位整数
    }
    
    return hash.toString(36);
  }
  
  isValid(cacheEntry) {
    if (!cacheEntry || !cacheEntry.timestamp) {
      return false;
    }
    
    const age = Date.now() - cacheEntry.timestamp;
    return age < cacheEntry.ttl;
  }
  
  calculateSize(data) {
    try {
      return JSON.stringify(data).length;
    } catch {
      return 0;
    }
  }
  
  async ensureCapacity() {
    if (this.cache.size < this.options.maxSize) {
      return;
    }
    
    // LRU淘汰策略
    const entries = Array.from(this.accessTimes.entries())
      .sort((a, b) => a[1] - b[1]); // 按访问时间排序
    
    const toRemove = entries.slice(0, Math.floor(this.options.maxSize * 0.2)); // 移除20%
    
    for (const [key] of toRemove) {
      this.cache.delete(key);
      this.accessTimes.delete(key);
      await this.storage.delete(key);
    }
  }
  
  startCleanupTimer() {
    setInterval(() => {
      this.cleanup();
    }, 60000); // 每分钟清理一次
  }
  
  async cleanup() {
    const now = Date.now();
    const expiredKeys = [];
    
    for (const [key, entry] of this.cache.entries()) {
      if (!this.isValid(entry)) {
        expiredKeys.push(key);
      }
    }
    
    for (const key of expiredKeys) {
      this.cache.delete(key);
      this.accessTimes.delete(key);
      await this.storage.delete(key);
    }
    
    console.log(`Cleaned up ${expiredKeys.length} expired cache entries`);
  }
  
  async clear() {
    this.cache.clear();
    this.accessTimes.clear();
    await this.storage.clear();
  }
  
  getStats() {
    const totalSize = Array.from(this.cache.values())
      .reduce((sum, entry) => sum + (entry.size || 0), 0);
    
    return {
      size: this.cache.size,
      maxSize: this.options.maxSize,
      totalSize,
      hitRate: this.calculateHitRate(),
      storageType: this.options.storageType
    };
  }
  
  calculateHitRate() {
    // 简化的命中率计算
    return this.cache.size > 0 ? 0.85 : 0; // 模拟85%命中率
  }
}

// 内存存储适配器
class MemoryStorageAdapter {
  constructor() {
    this.storage = new Map();
  }
  
  async get(key) {
    return this.storage.get(key) || null;
  }
  
  async set(key, value) {
    this.storage.set(key, value);
  }
  
  async delete(key) {
    this.storage.delete(key);
  }
  
  async clear() {
    this.storage.clear();
  }
}

// LocalStorage适配器
class LocalStorageAdapter {
  constructor() {
    this.prefix = 'sentry_cache_';
  }
  
  async get(key) {
    try {
      const stored = localStorage.getItem(this.prefix + key);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }
  
  async set(key, value) {
    try {
      localStorage.setItem(this.prefix + key, JSON.stringify(value));
    } catch (error) {
      console.warn('LocalStorage set error:', error);
    }
  }
  
  async delete(key) {
    localStorage.removeItem(this.prefix + key);
  }
  
  async clear() {
    const keys = Object.keys(localStorage)
      .filter(key => key.startsWith(this.prefix));
    
    keys.forEach(key => localStorage.removeItem(key));
  }
}

// IndexedDB适配器
class IndexedDBAdapter {
  constructor() {
    this.dbName = 'SentryCache';
    this.storeName = 'cache';
    this.version = 1;
    this.db = null;
    
    this.initDB();
  }
  
  async initDB() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.version);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve(this.db);
      };
      
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains(this.storeName)) {
          db.createObjectStore(this.storeName);
        }
      };
    });
  }
  
  async get(key) {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readonly');
      const store = transaction.objectStore(this.storeName);
      const request = store.get(key);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve(request.result || null);
    });
  }
  
  async set(key, value) {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readwrite');
      const store = transaction.objectStore(this.storeName);
      const request = store.put(value, key);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve();
    });
  }
  
  async delete(key) {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readwrite');
      const store = transaction.objectStore(this.storeName);
      const request = store.delete(key);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve();
    });
  }
  
  async clear() {
    if (!this.db) await this.initDB();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db.transaction([this.storeName], 'readwrite');
      const store = transaction.objectStore(this.storeName);
      const request = store.clear();
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve();
    });
  }
}
```

### 3.2 压缩管理器

```javascript
// 压缩管理器
class CompressionManager {
  constructor(options = {}) {
    this.options = {
      algorithm: 'gzip', // 'gzip', 'deflate', 'brotli'
      level: 6, // 压缩级别 1-9
      threshold: 1024, // 最小压缩阈值（字节）
      ...options
    };
    
    this.stats = {
      totalRequests: 0,
      compressedRequests: 0,
      originalSize: 0,
      compressedSize: 0
    };
  }
  
  async compress(request) {
    this.stats.totalRequests++;
    
    const originalSize = this.calculateSize(request);
    this.stats.originalSize += originalSize;
    
    // 检查是否需要压缩
    if (originalSize < this.options.threshold) {
      return request;
    }
    
    try {
      const compressed = await this.performCompression(request);
      const compressedSize = this.calculateSize(compressed);
      
      // 检查压缩效果
      const compressionRatio = compressedSize / originalSize;
      
      if (compressionRatio < 0.9) { // 压缩率超过10%才使用
        this.stats.compressedRequests++;
        this.stats.compressedSize += compressedSize;
        
        return {
          ...compressed,
          headers: {
            ...compressed.headers,
            'Content-Encoding': this.options.algorithm,
            'X-Original-Size': originalSize.toString(),
            'X-Compressed-Size': compressedSize.toString()
          }
        };
      }
      
      return request;
      
    } catch (error) {
      console.warn('Compression failed:', error);
      return request;
    }
  }
  
  async performCompression(request) {
    const data = JSON.stringify(request);
    
    switch (this.options.algorithm) {
      case 'gzip':
        return await this.gzipCompress(data);
      case 'deflate':
        return await this.deflateCompress(data);
      case 'brotli':
        return await this.brotliCompress(data);
      default:
        throw new Error(`Unsupported compression algorithm: ${this.options.algorithm}`);
    }
  }
  
  async gzipCompress(data) {
    // 使用Web Streams API进行gzip压缩
    if (typeof CompressionStream !== 'undefined') {
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
      
      const compressed = new Uint8Array(
        chunks.reduce((acc, chunk) => acc + chunk.length, 0)
      );
      
      let offset = 0;
      for (const chunk of chunks) {
        compressed.set(chunk, offset);
        offset += chunk.length;
      }
      
      return {
        body: compressed,
        compressed: true
      };
    }
    
    // 降级到简单的字符串压缩
    return this.simpleCompress(data);
  }
  
  async deflateCompress(data) {
    if (typeof CompressionStream !== 'undefined') {
      // 类似gzip的实现
      const stream = new CompressionStream('deflate');
      // ... 实现细节类似gzip
    }
    
    return this.simpleCompress(data);
  }
  
  async brotliCompress(data) {
    // Brotli压缩实现
    // 注意：浏览器对Brotli的支持有限
    return this.simpleCompress(data);
  }
  
  simpleCompress(data) {
    // 简单的字符串压缩算法（LZ77变体）
    const compressed = this.lz77Compress(data);
    
    return {
      body: compressed,
      compressed: true,
      algorithm: 'simple'
    };
  }
  
  lz77Compress(data) {
    const windowSize = 4096;
    const lookaheadSize = 18;
    const result = [];
    let position = 0;
    
    while (position < data.length) {
      let matchLength = 0;
      let matchDistance = 0;
      
      // 查找最长匹配
      const searchStart = Math.max(0, position - windowSize);
      
      for (let i = searchStart; i < position; i++) {
        let length = 0;
        
        while (
          length < lookaheadSize &&
          position + length < data.length &&
          data[i + length] === data[position + length]
        ) {
          length++;
        }
        
        if (length > matchLength) {
          matchLength = length;
          matchDistance = position - i;
        }
      }
      
      if (matchLength >= 3) {
        // 输出匹配
        result.push({
          type: 'match',
          distance: matchDistance,
          length: matchLength
        });
        position += matchLength;
      } else {
        // 输出字面量
        result.push({
          type: 'literal',
          value: data[position]
        });
        position++;
      }
    }
    
    return JSON.stringify(result);
  }
  
  calculateSize(data) {
    if (typeof data === 'string') {
      return new TextEncoder().encode(data).length;
    }
    
    if (data instanceof Uint8Array) {
      return data.length;
    }
    
    try {
      return JSON.stringify(data).length;
    } catch {
      return 0;
    }
  }
  
  getCompressionStats() {
    const compressionRatio = this.stats.originalSize > 0 ? 
      this.stats.compressedSize / this.stats.originalSize : 0;
    
    const compressionRate = this.stats.totalRequests > 0 ? 
      this.stats.compressedRequests / this.stats.totalRequests : 0;
    
    return {
      totalRequests: this.stats.totalRequests,
      compressedRequests: this.stats.compressedRequests,
      compressionRate: compressionRate,
      originalSize: this.stats.originalSize,
      compressedSize: this.stats.compressedSize,
      compressionRatio: compressionRatio,
      spaceSaved: this.stats.originalSize - this.stats.compressedSize,
      algorithm: this.options.algorithm,
      level: this.options.level
    };
  }
  
  resetStats() {
    this.stats = {
      totalRequests: 0,
      compressedRequests: 0,
      originalSize: 0,
      compressedSize: 0
    };
  }
}
```

## 4. 负载均衡指标监控

### 4.1 负载均衡指标收集器

```javascript
// 负载均衡指标收集器
class LoadBalancerMetrics {
  constructor() {
    this.metrics = new Map();
    this.connectionCounts = new Map();
    this.responseTimeHistory = new Map();
    this.errorCounts = new Map();
    this.startTime = Date.now();
    
    // 定期清理历史数据
    this.startCleanupTimer();
  }
  
  recordSelection(endpoint) {
    const dsn = endpoint.dsn;
    
    if (!this.metrics.has(dsn)) {
      this.initializeMetrics(dsn);
    }
    
    const metrics = this.metrics.get(dsn);
    metrics.selections++;
    metrics.lastSelected = Date.now();
    
    // 增加连接计数
    this.connectionCounts.set(dsn, this.getActiveConnections(dsn) + 1);
  }
  
  recordResponse(dsn, responseTime, success = true) {
    if (!this.metrics.has(dsn)) {
      this.initializeMetrics(dsn);
    }
    
    const metrics = this.metrics.get(dsn);
    metrics.totalRequests++;
    
    if (success) {
      metrics.successfulRequests++;
    } else {
      metrics.failedRequests++;
      this.recordError(dsn);
    }
    
    // 记录响应时间
    this.recordResponseTime(dsn, responseTime);
    
    // 减少连接计数
    const currentConnections = this.getActiveConnections(dsn);
    this.connectionCounts.set(dsn, Math.max(0, currentConnections - 1));
  }
  
  recordError(dsn) {
    const now = Date.now();
    const errors = this.errorCounts.get(dsn) || [];
    
    errors.push(now);
    
    // 只保留最近1小时的错误记录
    const oneHourAgo = now - 3600000;
    const recentErrors = errors.filter(timestamp => timestamp > oneHourAgo);
    
    this.errorCounts.set(dsn, recentErrors);
  }
  
  recordResponseTime(dsn, responseTime) {
    if (!this.responseTimeHistory.has(dsn)) {
      this.responseTimeHistory.set(dsn, []);
    }
    
    const history = this.responseTimeHistory.get(dsn);
    history.push({
      timestamp: Date.now(),
      responseTime
    });
    
    // 只保留最近1000条记录
    if (history.length > 1000) {
      history.shift();
    }
  }
  
  initializeMetrics(dsn) {
    this.metrics.set(dsn, {
      selections: 0,
      totalRequests: 0,
      successfulRequests: 0,
      failedRequests: 0,
      lastSelected: null,
      createdAt: Date.now()
    });
    
    this.connectionCounts.set(dsn, 0);
    this.responseTimeHistory.set(dsn, []);
    this.errorCounts.set(dsn, []);
  }
  
  getActiveConnections(dsn) {
    return this.connectionCounts.get(dsn) || 0;
  }
  
  getAverageResponseTime(dsn, timeWindow = 300000) { // 5分钟窗口
    const history = this.responseTimeHistory.get(dsn) || [];
    const now = Date.now();
    
    const recentHistory = history.filter(record => 
      (now - record.timestamp) <= timeWindow
    );
    
    if (recentHistory.length === 0) return 0;
    
    const totalTime = recentHistory.reduce((sum, record) => 
      sum + record.responseTime, 0
    );
    
    return totalTime / recentHistory.length;
  }
  
  getErrorRate(dsn, timeWindow = 300000) { // 5分钟窗口
    const errors = this.errorCounts.get(dsn) || [];
    const now = Date.now();
    
    const recentErrors = errors.filter(timestamp => 
      (now - timestamp) <= timeWindow
    );
    
    const metrics = this.metrics.get(dsn);
    if (!metrics || metrics.totalRequests === 0) return 0;
    
    // 计算最近时间窗口内的错误率
    const recentRequests = this.getRecentRequestCount(dsn, timeWindow);
    
    return recentRequests > 0 ? recentErrors.length / recentRequests : 0;
  }
  
  getRecentRequestCount(dsn, timeWindow) {
    const history = this.responseTimeHistory.get(dsn) || [];
    const now = Date.now();
    
    return history.filter(record => 
      (now - record.timestamp) <= timeWindow
    ).length;
  }
  
  getThroughput(dsn, timeWindow = 60000) { // 1分钟窗口
    const recentRequests = this.getRecentRequestCount(dsn, timeWindow);
    return (recentRequests / timeWindow) * 1000; // 每秒请求数
  }
  
  getPercentile(dsn, percentile, timeWindow = 300000) {
    const history = this.responseTimeHistory.get(dsn) || [];
    const now = Date.now();
    
    const recentHistory = history
      .filter(record => (now - record.timestamp) <= timeWindow)
      .map(record => record.responseTime)
      .sort((a, b) => a - b);
    
    if (recentHistory.length === 0) return 0;
    
    const index = Math.ceil((percentile / 100) * recentHistory.length) - 1;
    return recentHistory[Math.max(0, index)];
  }
  
  getStats() {
    const stats = {
      overview: {
        totalEndpoints: this.metrics.size,
        uptime: Date.now() - this.startTime,
        totalSelections: 0,
        totalRequests: 0,
        totalErrors: 0
      },
      endpoints: {}
    };
    
    for (const [dsn, metrics] of this.metrics.entries()) {
      stats.overview.totalSelections += metrics.selections;
      stats.overview.totalRequests += metrics.totalRequests;
      stats.overview.totalErrors += metrics.failedRequests;
      
      stats.endpoints[dsn] = {
        selections: metrics.selections,
        totalRequests: metrics.totalRequests,
        successfulRequests: metrics.successfulRequests,
        failedRequests: metrics.failedRequests,
        activeConnections: this.getActiveConnections(dsn),
        averageResponseTime: this.getAverageResponseTime(dsn),
        errorRate: this.getErrorRate(dsn),
        throughput: this.getThroughput(dsn),
        percentiles: {
          p50: this.getPercentile(dsn, 50),
          p90: this.getPercentile(dsn, 90),
          p95: this.getPercentile(dsn, 95),
          p99: this.getPercentile(dsn, 99)
        },
        lastSelected: metrics.lastSelected
      };
    }
    
    return stats;
  }
  
  startCleanupTimer() {
    // 每小时清理一次历史数据
    setInterval(() => {
      this.cleanupHistoryData();
    }, 3600000);
  }
  
  cleanupHistoryData() {
    const now = Date.now();
    const maxAge = 24 * 60 * 60 * 1000; // 24小时
    
    // 清理响应时间历史
    for (const [dsn, history] of this.responseTimeHistory.entries()) {
      const filtered = history.filter(record => 
        (now - record.timestamp) <= maxAge
      );
      this.responseTimeHistory.set(dsn, filtered);
    }
    
    // 清理错误计数
    for (const [dsn, errors] of this.errorCounts.entries()) {
      const filtered = errors.filter(timestamp => 
        (now - timestamp) <= maxAge
      );
      this.errorCounts.set(dsn, filtered);
    }
  }
  
  exportMetrics() {
    return {
      timestamp: Date.now(),
      metrics: Object.fromEntries(this.metrics),
      connectionCounts: Object.fromEntries(this.connectionCounts),
      responseTimeHistory: Object.fromEntries(this.responseTimeHistory),
      errorCounts: Object.fromEntries(this.errorCounts)
    };
  }
  
  importMetrics(data) {
    if (data.metrics) {
      this.metrics = new Map(Object.entries(data.metrics));
    }
    if (data.connectionCounts) {
      this.connectionCounts = new Map(Object.entries(data.connectionCounts));
    }
    if (data.responseTimeHistory) {
      this.responseTimeHistory = new Map(Object.entries(data.responseTimeHistory));
    }
    if (data.errorCounts) {
      this.errorCounts = new Map(Object.entries(data.errorCounts));
    }
  }
}
```

## 5. 企业级部署策略

### 5.1 容器化部署管理器

```javascript
// 容器化部署管理器
class ContainerDeploymentManager {
  constructor(options = {}) {
    this.options = {
      platform: 'kubernetes', // 'kubernetes', 'docker', 'docker-swarm'
      namespace: 'sentry-monitoring',
      replicas: 3,
      resources: {
        requests: {
          cpu: '100m',
          memory: '128Mi'
        },
        limits: {
          cpu: '500m',
          memory: '512Mi'
        }
      },
      ...options
    };
    
    this.deploymentConfig = this.generateDeploymentConfig();
    this.serviceConfig = this.generateServiceConfig();
    this.configMapConfig = this.generateConfigMapConfig();
  }
  
  generateDeploymentConfig() {
    return {
      apiVersion: 'apps/v1',
      kind: 'Deployment',
      metadata: {
        name: 'sentry-frontend-monitor',
        namespace: this.options.namespace,
        labels: {
          app: 'sentry-frontend-monitor',
          version: 'v1.0.0',
          component: 'monitoring'
        }
      },
      spec: {
        replicas: this.options.replicas,
        selector: {
          matchLabels: {
            app: 'sentry-frontend-monitor'
          }
        },
        template: {
          metadata: {
            labels: {
              app: 'sentry-frontend-monitor',
              version: 'v1.0.0'
            },
            annotations: {
              'prometheus.io/scrape': 'true',
              'prometheus.io/port': '3000',
              'prometheus.io/path': '/metrics'
            }
          },
          spec: {
            containers: [
              {
                name: 'sentry-monitor',
                image: 'your-registry/sentry-frontend-monitor:latest',
                ports: [
                  {
                    containerPort: 3000,
                    name: 'http'
                  },
                  {
                    containerPort: 9090,
                    name: 'metrics'
                  }
                ],
                env: [
                  {
                    name: 'NODE_ENV',
                    value: 'production'
                  },
                  {
                    name: 'SENTRY_DSN_PRIMARY',
                    valueFrom: {
                      secretKeyRef: {
                        name: 'sentry-secrets',
                        key: 'dsn-primary'
                      }
                    }
                  },
                  {
                    name: 'SENTRY_DSN_SECONDARY',
                    valueFrom: {
                      secretKeyRef: {
                        name: 'sentry-secrets',
                        key: 'dsn-secondary'
                      }
                    }
                  }
                ],
                resources: this.options.resources,
                livenessProbe: {
                  httpGet: {
                    path: '/health',
                    port: 3000
                  },
                  initialDelaySeconds: 30,
                  periodSeconds: 10
                },
                readinessProbe: {
                  httpGet: {
                    path: '/ready',
                    port: 3000
                  },
                  initialDelaySeconds: 5,
                  periodSeconds: 5
                },
                volumeMounts: [
                  {
                    name: 'config',
                    mountPath: '/app/config'
                  },
                  {
                    name: 'cache',
                    mountPath: '/app/cache'
                  }
                ]
              }
            ],
            volumes: [
              {
                name: 'config',
                configMap: {
                  name: 'sentry-monitor-config'
                }
              },
              {
                name: 'cache',
                emptyDir: {
                  sizeLimit: '1Gi'
                }
              }
            ],
            affinity: {
              podAntiAffinity: {
                preferredDuringSchedulingIgnoredDuringExecution: [
                  {
                    weight: 100,
                    podAffinityTerm: {
                      labelSelector: {
                        matchExpressions: [
                          {
                            key: 'app',
                            operator: 'In',
                            values: ['sentry-frontend-monitor']
                          }
                        ]
                      },
                      topologyKey: 'kubernetes.io/hostname'
                    }
                  }
                ]
              }
            }
          }
        }
      }
    };
  }
  
  generateServiceConfig() {
    return {
      apiVersion: 'v1',
      kind: 'Service',
      metadata: {
        name: 'sentry-frontend-monitor-service',
        namespace: this.options.namespace,
        labels: {
          app: 'sentry-frontend-monitor'
        }
      },
      spec: {
        selector: {
          app: 'sentry-frontend-monitor'
        },
        ports: [
          {
            name: 'http',
            port: 80,
            targetPort: 3000,
            protocol: 'TCP'
          },
          {
            name: 'metrics',
            port: 9090,
            targetPort: 9090,
            protocol: 'TCP'
          }
        ],
        type: 'ClusterIP'
      }
    };
  }
  
  generateConfigMapConfig() {
    return {
      apiVersion: 'v1',
      kind: 'ConfigMap',
      metadata: {
        name: 'sentry-monitor-config',
        namespace: this.options.namespace
      },
      data: {
        'config.json': JSON.stringify({
          monitoring: {
            enabled: true,
            interval: 30000,
            healthCheck: {
              enabled: true,
              interval: 10000,
              timeout: 5000
            }
          },
          loadBalancer: {
            strategy: 'round-robin',
            healthCheckInterval: 30000,
            failureThreshold: 3
          },
          cache: {
            enabled: true,
            maxSize: 1000,
            ttl: 300000,
            storageType: 'memory'
          },
          compression: {
            enabled: true,
            algorithm: 'gzip',
            threshold: 1024
          },
          alerts: {
            slack: {
              enabled: true,
              webhook: process.env.SLACK_WEBHOOK_URL
            },
            email: {
              enabled: true,
              endpoint: process.env.EMAIL_API_ENDPOINT
            }
          }
        }, null, 2)
      }
    };
  }
  
  generateDockerfile() {
    return `
# 多阶段构建
FROM node:18-alpine AS builder

WORKDIR /app

# 复制依赖文件
COPY package*.json ./
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产镜像
FROM node:18-alpine AS production

# 创建非root用户
RUN addgroup -g 1001 -S nodejs
RUN adduser -S sentry -u 1001

WORKDIR /app

# 复制构建产物
COPY --from=builder --chown=sentry:nodejs /app/dist ./dist
COPY --from=builder --chown=sentry:nodejs /app/node_modules ./node_modules
COPY --from=builder --chown=sentry:nodejs /app/package.json ./package.json

# 创建必要目录
RUN mkdir -p /app/cache /app/logs && chown -R sentry:nodejs /app

# 切换到非root用户
USER sentry

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD node healthcheck.js

# 暴露端口
EXPOSE 3000 9090

# 启动应用
CMD ["node", "dist/server.js"]
`;
  }
  
  generateDockerCompose() {
    return `
version: '3.8'

services:
  sentry-monitor:
    build:
      context: .
      dockerfile: Dockerfile
    image: sentry-frontend-monitor:latest
    container_name: sentry-monitor
    restart: unless-stopped
    ports:
      - "3000:3000"
      - "9090:9090"
    environment:
      - NODE_ENV=production
      - SENTRY_DSN_PRIMARY=\${SENTRY_DSN_PRIMARY}
      - SENTRY_DSN_SECONDARY=\${SENTRY_DSN_SECONDARY}
      - SLACK_WEBHOOK_URL=\${SLACK_WEBHOOK_URL}
      - EMAIL_API_ENDPOINT=\${EMAIL_API_ENDPOINT}
    volumes:
      - ./config:/app/config:ro
      - cache_volume:/app/cache
      - logs_volume:/app/logs
    networks:
      - sentry_network
    healthcheck:
      test: ["CMD", "node", "healthcheck.js"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.1'
          memory: 128M
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
        window: 120s

  nginx:
    image: nginx:alpine
    container_name: sentry-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    networks:
      - sentry_network
    depends_on:
      - sentry-monitor

  prometheus:
    image: prom/prometheus:latest
    container_name: sentry-prometheus
    restart: unless-stopped
    ports:
      - "9091:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    networks:
      - sentry_network
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'

  grafana:
    image: grafana/grafana:latest
    container_name: sentry-grafana
    restart: unless-stopped
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=\${GRAFANA_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
      - ./grafana/datasources:/etc/grafana/provisioning/datasources:ro
    networks:
      - sentry_network
    depends_on:
      - prometheus

volumes:
  cache_volume:
  logs_volume:
  prometheus_data:
  grafana_data:

networks:
  sentry_network:
    driver: bridge
`;
  }
  
  async deploy() {
    console.log('Starting deployment...');
    
    try {
      // 1. 创建命名空间
      await this.createNamespace();
      
      // 2. 创建ConfigMap
      await this.applyConfig(this.configMapConfig);
      
      // 3. 创建Service
      await this.applyConfig(this.serviceConfig);
      
      // 4. 创建Deployment
      await this.applyConfig(this.deploymentConfig);
      
      // 5. 等待部署完成
      await this.waitForDeployment();
      
      console.log('Deployment completed successfully');
      
    } catch (error) {
      console.error('Deployment failed:', error);
      throw error;
    }
  }
  
  async createNamespace() {
    const namespaceConfig = {
      apiVersion: 'v1',
      kind: 'Namespace',
      metadata: {
        name: this.options.namespace,
        labels: {
          name: this.options.namespace,
          purpose: 'sentry-monitoring'
        }
      }
    };
    
    await this.applyConfig(namespaceConfig);
  }
  
  async applyConfig(config) {
    // 这里应该调用Kubernetes API或使用kubectl
    console.log(`Applying config: ${config.kind}/${config.metadata.name}`);
    
    // 模拟API调用
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log(`✓ ${config.kind}/${config.metadata.name} applied`);
        resolve();
      }, 1000);
    });
  }
  
  async waitForDeployment() {
    console.log('Waiting for deployment to be ready...');
    
    // 模拟等待部署就绪
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log('✓ Deployment is ready');
        resolve();
      }, 5000);
    });
  }
  
  generateHelmChart() {
    return {
      'Chart.yaml': `
apiVersion: v2
name: sentry-frontend-monitor
description: Enterprise Sentry Frontend Monitoring
type: application
version: 1.0.0
appVersion: "1.0.0"
`,
      'values.yaml': `
replicaCount: 3

image:
  repository: your-registry/sentry-frontend-monitor
  pullPolicy: IfNotPresent
  tag: "latest"

service:
  type: ClusterIP
  port: 80
  targetPort: 3000

resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 100m
    memory: 128Mi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80

config:
  monitoring:
    enabled: true
    interval: 30000
  loadBalancer:
    strategy: "round-robin"
  cache:
    enabled: true
    maxSize: 1000
    ttl: 300000

secrets:
  sentryDsnPrimary: ""
  sentryDsnSecondary: ""
  slackWebhook: ""
  emailApiEndpoint: ""
`,
      'templates/deployment.yaml': `
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "sentry-frontend-monitor.fullname" . }}
  labels:
    {{- include "sentry-frontend-monitor.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "sentry-frontend-monitor.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "sentry-frontend-monitor.selectorLabels" . | nindent 8 }}
    spec:
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: 3000
              protocol: TCP
            - name: metrics
              containerPort: 9090
              protocol: TCP
          env:
            - name: NODE_ENV
              value: "production"
            - name: SENTRY_DSN_PRIMARY
              valueFrom:
                secretKeyRef:
                  name: {{ include "sentry-frontend-monitor.fullname" . }}-secrets
                  key: sentry-dsn-primary
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
`
    };
  }
}
```

### 5.2 监控仪表板

```javascript
// 企业级监控仪表板
class EnterpriseMonitoringDashboard {
  constructor(options = {}) {
    this.options = {
      refreshInterval: 30000, // 30秒刷新
      enableRealtime: true,
      enableAlerts: true,
      theme: 'dark',
      ...options
    };
    
    this.metricsCollector = new MetricsCollector();
    this.alertManager = new AlertManager();
    this.chartManager = new ChartManager();
    
    this.init();
  }
  
  init() {
    this.createDashboardLayout();
    this.setupEventListeners();
    this.startDataCollection();
    
    if (this.options.enableRealtime) {
      this.enableRealtimeUpdates();
    }
  }
  
  createDashboardLayout() {
    const dashboardHTML = `
      <div class="enterprise-dashboard" data-theme="${this.options.theme}">
        <!-- 顶部状态栏 -->
        <header class="dashboard-header">
          <div class="status-indicators">
            <div class="status-item" id="overall-status">
              <span class="status-icon">●</span>
              <span class="status-text">System Status</span>
            </div>
            <div class="status-item" id="endpoint-status">
              <span class="status-count">0</span>
              <span class="status-label">Active Endpoints</span>
            </div>
            <div class="status-item" id="error-rate">
              <span class="status-count">0%</span>
              <span class="status-label">Error Rate</span>
            </div>
            <div class="status-item" id="response-time">
              <span class="status-count">0ms</span>
              <span class="status-label">Avg Response Time</span>
            </div>
          </div>
          
          <div class="dashboard-controls">
            <button id="refresh-btn" class="control-btn">
              <i class="icon-refresh"></i> Refresh
            </button>
            <button id="export-btn" class="control-btn">
              <i class="icon-download"></i> Export
            </button>
            <button id="settings-btn" class="control-btn">
              <i class="icon-settings"></i> Settings
            </button>
          </div>
        </header>
        
        <!-- 主要指标网格 -->
        <div class="metrics-grid">
          <!-- 系统概览 -->
          <div class="metric-card system-overview">
            <h3>System Overview</h3>
            <div class="overview-stats">
              <div class="stat-item">
                <span class="stat-value" id="total-requests">0</span>
                <span class="stat-label">Total Requests</span>
              </div>
              <div class="stat-item">
                <span class="stat-value" id="success-rate">0%</span>
                <span class="stat-label">Success Rate</span>
              </div>
              <div class="stat-item">
                <span class="stat-value" id="uptime">0h</span>
                <span class="stat-label">Uptime</span>
              </div>
            </div>
          </div>
          
          <!-- 端点健康状态 -->
          <div class="metric-card endpoint-health">
            <h3>Endpoint Health</h3>
            <div id="endpoint-list" class="endpoint-list"></div>
          </div>
          
          <!-- 响应时间趋势 -->
          <div class="metric-card response-time-chart">
            <h3>Response Time Trend</h3>
            <canvas id="response-time-canvas"></canvas>
          </div>
          
          <!-- 错误率趋势 -->
          <div class="metric-card error-rate-chart">
            <h3>Error Rate Trend</h3>
            <canvas id="error-rate-canvas"></canvas>
          </div>
          
          <!-- 负载分布 -->
          <div class="metric-card load-distribution">
            <h3>Load Distribution</h3>
            <canvas id="load-distribution-canvas"></canvas>
          </div>
          
          <!-- 缓存性能 -->
          <div class="metric-card cache-performance">
            <h3>Cache Performance</h3>
            <div class="cache-stats">
              <div class="cache-stat">
                <span class="cache-value" id="cache-hit-rate">0%</span>
                <span class="cache-label">Hit Rate</span>
              </div>
              <div class="cache-stat">
                <span class="cache-value" id="cache-size">0</span>
                <span class="cache-label">Cache Size</span>
              </div>
              <div class="cache-stat">
                <span class="cache-value" id="compression-ratio">0%</span>
                <span class="cache-label">Compression</span>
              </div>
            </div>
          </div>
          
          <!-- 告警历史 -->
          <div class="metric-card alert-history">
            <h3>Recent Alerts</h3>
            <div id="alert-list" class="alert-list"></div>
          </div>
          
          <!-- 性能指标 -->
          <div class="metric-card performance-metrics">
            <h3>Performance Metrics</h3>
            <div class="performance-grid">
              <div class="perf-item">
                <span class="perf-label">P50</span>
                <span class="perf-value" id="p50-time">0ms</span>
              </div>
              <div class="perf-item">
                <span class="perf-label">P90</span>
                <span class="perf-value" id="p90-time">0ms</span>
              </div>
              <div class="perf-item">
                <span class="perf-label">P95</span>
                <span class="perf-value" id="p95-time">0ms</span>
              </div>
              <div class="perf-item">
                <span class="perf-label">P99</span>
                <span class="perf-value" id="p99-time">0ms</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 详细日志 -->
        <div class="logs-section">
          <h3>System Logs</h3>
          <div id="log-container" class="log-container"></div>
        </div>
      </div>
    `;
    
    document.body.innerHTML = dashboardHTML;
    this.addDashboardStyles();
  }
  
  addDashboardStyles() {
    const styles = `
      <style>
        .enterprise-dashboard {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          background: var(--bg-primary);
          color: var(--text-primary);
          min-height: 100vh;
          padding: 20px;
        }
        
        .enterprise-dashboard[data-theme="dark"] {
          --bg-primary: #1a1a1a;
          --bg-secondary: #2d2d2d;
          --bg-tertiary: #404040;
          --text-primary: #ffffff;
          --text-secondary: #cccccc;
          --accent-primary: #007acc;
          --accent-success: #28a745;
          --accent-warning: #ffc107;
          --accent-danger: #dc3545;
          --border-color: #404040;
        }
        
        .enterprise-dashboard[data-theme="light"] {
          --bg-primary: #ffffff;
          --bg-secondary: #f8f9fa;
          --bg-tertiary: #e9ecef;
          --text-primary: #212529;
          --text-secondary: #6c757d;
          --accent-primary: #007bff;
          --accent-success: #28a745;
          --accent-warning: #ffc107;
          --accent-danger: #dc3545;
          --border-color: #dee2e6;
        }
        
        .dashboard-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          background: var(--bg-secondary);
          padding: 20px;
          border-radius: 8px;
          margin-bottom: 20px;
          border: 1px solid var(--border-color);
        }
        
        .status-indicators {
          display: flex;
          gap: 30px;
        }
        
        .status-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 5px;
        }
        
        .status-icon {
          font-size: 20px;
          color: var(--accent-success);
        }
        
        .status-count {
          font-size: 24px;
          font-weight: bold;
          color: var(--text-primary);
        }
        
        .status-label, .status-text {
          font-size: 12px;
          color: var(--text-secondary);
          text-transform: uppercase;
        }
        
        .dashboard-controls {
          display: flex;
          gap: 10px;
        }
        
        .control-btn {
          background: var(--accent-primary);
          color: white;
          border: none;
          padding: 10px 15px;
          border-radius: 5px;
          cursor: pointer;
          font-size: 14px;
          display: flex;
          align-items: center;
          gap: 5px;
          transition: background-color 0.2s;
        }
        
        .control-btn:hover {
          background: var(--accent-primary);
          opacity: 0.8;
        }
        
        .metrics-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
          gap: 20px;
          margin-bottom: 20px;
        }
        
        .metric-card {
          background: var(--bg-secondary);
          border: 1px solid var(--border-color);
          border-radius: 8px;
          padding: 20px;
          min-height: 200px;
        }
        
        .metric-card h3 {
          margin: 0 0 15px 0;
          color: var(--text-primary);
          font-size: 16px;
          font-weight: 600;
        }
        
        .overview-stats {
          display: flex;
          justify-content: space-around;
          align-items: center;
          height: 100%;
        }
        
        .stat-item {
          text-align: center;
        }
        
        .stat-value {
          display: block;
          font-size: 32px;
          font-weight: bold;
          color: var(--accent-primary);
          margin-bottom: 5px;
        }
        
        .stat-label {
          font-size: 12px;
          color: var(--text-secondary);
          text-transform: uppercase;
        }
        
        .endpoint-list {
          max-height: 150px;
          overflow-y: auto;
        }
        
        .endpoint-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-bottom: 1px solid var(--border-color);
        }
        
        .endpoint-name {
          font-weight: 500;
          color: var(--text-primary);
        }
        
        .endpoint-status {
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 11px;
          font-weight: bold;
          text-transform: uppercase;
        }
        
        .endpoint-status.healthy {
          background: var(--accent-success);
          color: white;
        }
        
        .endpoint-status.unhealthy {
          background: var(--accent-danger);
          color: white;
        }
        
        .cache-stats {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 15px;
          height: 100%;
          align-items: center;
        }
        
        .cache-stat {
          text-align: center;
        }
        
        .cache-value {
          display: block;
          font-size: 24px;
          font-weight: bold;
          color: var(--accent-primary);
          margin-bottom: 5px;
        }
        
        .cache-label {
          font-size: 12px;
          color: var(--text-secondary);
          text-transform: uppercase;
        }
        
        .alert-list {
          max-height: 150px;
          overflow-y: auto;
        }
        
        .alert-item {
          padding: 8px;
          margin-bottom: 8px;
          border-radius: 4px;
          font-size: 12px;
        }
        
        .alert-item.warning {
          background: rgba(255, 193, 7, 0.1);
          border-left: 3px solid var(--accent-warning);
        }
        
        .alert-item.error {
          background: rgba(220, 53, 69, 0.1);
          border-left: 3px solid var(--accent-danger);
        }
        
        .performance-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 15px;
          height: 100%;
          align-items: center;
        }
        
        .perf-item {
          text-align: center;
        }
        
        .perf-label {
          display: block;
          font-size: 12px;
          color: var(--text-secondary);
          margin-bottom: 5px;
        }
        
        .perf-value {
          font-size: 20px;
          font-weight: bold;
          color: var(--accent-primary);
        }
        
        .logs-section {
          background: var(--bg-secondary);
          border: 1px solid var(--border-color);
          border-radius: 8px;
          padding: 20px;
        }
        
        .logs-section h3 {
          margin: 0 0 15px 0;
          color: var(--text-primary);
        }
        
        .log-container {
          background: var(--bg-primary);
          border: 1px solid var(--border-color);
          border-radius: 4px;
          padding: 15px;
          height: 200px;
          overflow-y: auto;
          font-family: 'Courier New', monospace;
          font-size: 12px;
          line-height: 1.4;
        }
        
        .log-entry {
          margin-bottom: 5px;
          color: var(--text-secondary);
        }
        
        .log-entry.error {
          color: var(--accent-danger);
        }
        
        .log-entry.warning {
          color: var(--accent-warning);
        }
        
        .log-entry.info {
          color: var(--accent-primary);
        }
        
        canvas {
          width: 100%;
          height: 150px;
        }
      </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', styles);
  }
  
  setupEventListeners() {
    // 刷新按钮
    document.getElementById('refresh-btn').addEventListener('click', () => {
      this.refreshData();
    });
    
    // 导出按钮
    document.getElementById('export-btn').addEventListener('click', () => {
      this.exportData();
    });
    
    // 设置按钮
    document.getElementById('settings-btn').addEventListener('click', () => {
      this.showSettings();
    });
  }
  
  startDataCollection() {
    // 立即加载数据
    this.refreshData();
    
    // 定期刷新
    setInterval(() => {
      this.refreshData();
    }, this.options.refreshInterval);
  }
  
  async refreshData() {
    try {
      const metrics = await this.metricsCollector.collectAllMetrics();
      this.updateDashboard(metrics);
    } catch (error) {
      console.error('Failed to refresh data:', error);
      this.showError('Failed to load metrics data');
    }
  }
  
  updateDashboard(metrics) {
    // 更新状态指示器
    this.updateStatusIndicators(metrics);
    
    // 更新系统概览
    this.updateSystemOverview(metrics);
    
    // 更新端点健康状态
    this.updateEndpointHealth(metrics);
    
    // 更新图表
    this.updateCharts(metrics);
    
    // 更新缓存性能
    this.updateCachePerformance(metrics);
    
    // 更新性能指标
    this.updatePerformanceMetrics(metrics);
    
    // 更新告警历史
    this.updateAlertHistory(metrics);
    
    // 更新日志
    this.updateLogs(metrics);
  }
  
  updateStatusIndicators(metrics) {
    const overallStatus = this.calculateOverallStatus(metrics);
    const statusIcon = document.querySelector('#overall-status .status-icon');
    const statusText = document.querySelector('#overall-status .status-text');
    
    statusIcon.style.color = overallStatus.healthy ? 'var(--accent-success)' : 'var(--accent-danger)';
    statusText.textContent = overallStatus.healthy ? 'System Healthy' : 'System Issues';
    
    document.getElementById('endpoint-status').querySelector('.status-count').textContent = 
      metrics.loadBalancer?.overview?.totalEndpoints || 0;
    
    document.getElementById('error-rate').querySelector('.status-count').textContent = 
      `${(metrics.errorRate * 100).toFixed(1)}%`;
    
    document.getElementById('response-time').querySelector('.status-count').textContent = 
      `${metrics.averageResponseTime.toFixed(0)}ms`;
  }
  
  calculateOverallStatus(metrics) {
    const errorRate = metrics.errorRate || 0;
    const responseTime = metrics.averageResponseTime || 0;
    
    const healthy = errorRate < 0.05 && responseTime < 1000; // 5%错误率，1秒响应时间
    
    return { healthy };
  }
  
  enableRealtimeUpdates() {
    // WebSocket连接用于实时更新
    if (typeof WebSocket !== 'undefined') {
      const ws = new WebSocket('ws://localhost:3000/metrics');
      
      ws.onmessage = (event) => {
        const metrics = JSON.parse(event.data);
        this.updateDashboard(metrics);
      };
      
      ws.onerror = (error) => {
        console.warn('WebSocket error:', error);
      };
    }
  }
  
  exportData() {
    const data = {
      timestamp: new Date().toISOString(),
      metrics: this.metricsCollector.getLastMetrics(),
      alerts: this.alertManager.getAlertStats()
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json'
    });
    
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `sentry-metrics-${Date.now()}.json`;
    a.click();
    
    URL.revokeObjectURL(url);
  }
  
  showSettings() {
    // 显示设置对话框
    console.log('Settings dialog would open here');
  }
  
  showError(message) {
    // 显示错误消息
    console.error(message);
  }
}
```

### 1.2 负载均衡器

```javascript
// Sentry负载均衡器
class SentryLoadBalancer {
  constructor(endpoints) {
    this.endpoints = endpoints;
    this.healthChecker = new HealthChecker(endpoints);
    this.metrics = new LoadBalancerMetrics();
    this.strategy = 'round-robin'; // 'round-robin', 'least-connections', 'weighted'
    this.currentIndex = 0;
    
    // 启动健康检查
    this.startHealthChecking();
  }
  
  getActiveEndpoint() {
    const healthyEndpoints = this.getHealthyEndpoints();
    
    if (healthyEndpoints.length === 0) {
      throw new Error('No healthy Sentry endpoints available');
    }
    
    let selectedEndpoint;
    
    switch (this.strategy) {
      case 'round-robin':
        selectedEndpoint = this.roundRobinSelection(healthyEndpoints);
        break;
      case 'least-connections':
        selectedEndpoint = this.leastConnectionsSelection(healthyEndpoints);
        break;
      case 'weighted':
        selectedEndpoint = this.weightedSelection(healthyEndpoints);
        break;
      default:
        selectedEndpoint = healthyEndpoints[0];
    }
    
    // 记录选择
    this.metrics.recordSelection(selectedEndpoint);
    
    return selectedEndpoint;
  }
  
  roundRobinSelection(endpoints) {
    const endpoint = endpoints[this.currentIndex % endpoints.length];
    this.currentIndex++;
    return endpoint;
  }
  
  leastConnectionsSelection(endpoints) {
    return endpoints.reduce((least, current) => {
      const leastConnections = this.metrics.getActiveConnections(least.dsn);
      const currentConnections = this.metrics.getActiveConnections(current.dsn);
      
      return currentConnections < leastConnections ? current : least;
    });
  }
  
  weightedSelection(endpoints) {
    // 基于优先级和健康状态的加权选择
    const weights = endpoints.map(endpoint => {
      const healthScore = this.healthChecker.getHealthScore(endpoint.dsn);
      const priorityWeight = 1 / endpoint.priority;
      return {
        endpoint,
        weight: healthScore * priorityWeight
      };
    });
    
    const totalWeight = weights.reduce((sum, item) => sum + item.weight, 0);
    const random = Math.random() * totalWeight;
    
    let currentWeight = 0;
    for (const item of weights) {
      currentWeight += item.weight;
      if (random <= currentWeight) {
        return item.endpoint;
      }
    }
    
    return endpoints[0];
  }
  
  getHealthyEndpoints() {
    return Object.values(this.endpoints).filter(endpoint => 
      this.healthChecker.isHealthy(endpoint.dsn)
    );
  }
  
  startHealthChecking() {
    // 每30秒检查一次健康状态
    setInterval(() => {
      this.healthChecker.checkAllEndpoints();
    }, 30000);
    
    // 立即执行一次检查
    this.healthChecker.checkAllEndpoints();
  }
  
  // 获取负载均衡统计信息
  getStats() {
    return {
      strategy: this.strategy,
      totalEndpoints: Object.keys(this.endpoints).length,
      healthyEndpoints: this.getHealthyEndpoints().length,
      metrics: this.metrics.getStats(),
      health: this.healthChecker.getHealthStats()
    };
  }
}
```

### 1.3 健康检查器

```javascript
// 健康检查器
class HealthChecker {
  constructor(endpoints) {
    this.endpoints = endpoints;
    this.healthStatus = new Map();
    this.healthHistory = new Map();
    this.checkInterval = 30000; // 30秒
    this.timeout = 5000; // 5秒超时
    
    // 初始化健康状态
    Object.values(endpoints).forEach(endpoint => {
      this.healthStatus.set(endpoint.dsn, {
        healthy: true,
        lastCheck: null,
        lastError: null,
        responseTime: 0,
        consecutiveFailures: 0
      });
      
      this.healthHistory.set(endpoint.dsn, []);
    });
  }
  
  async checkAllEndpoints() {
    const checkPromises = Object.values(this.endpoints).map(endpoint => 
      this.checkEndpoint(endpoint)
    );
    
    await Promise.allSettled(checkPromises);
  }
  
  async checkEndpoint(endpoint) {
    const startTime = Date.now();
    
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), this.timeout);
      
      const response = await fetch(this.getHealthCheckUrl(endpoint), {
        method: 'GET',
        signal: controller.signal,
        headers: {
          'User-Agent': 'SentryHealthChecker/1.0'
        }
      });
      
      clearTimeout(timeoutId);
      
      const responseTime = Date.now() - startTime;
      const isHealthy = response.ok;
      
      this.updateHealthStatus(endpoint.dsn, {
        healthy: isHealthy,
        lastCheck: new Date(),
        lastError: isHealthy ? null : `HTTP ${response.status}`,
        responseTime,
        consecutiveFailures: isHealthy ? 0 : this.getConsecutiveFailures(endpoint.dsn) + 1
      });
      
      this.recordHealthHistory(endpoint.dsn, {
        timestamp: new Date(),
        healthy: isHealthy,
        responseTime,
        status: response.status
      });
      
    } catch (error) {
      const responseTime = Date.now() - startTime;
      
      this.updateHealthStatus(endpoint.dsn, {
        healthy: false,
        lastCheck: new Date(),
        lastError: error.message,
        responseTime,
        consecutiveFailures: this.getConsecutiveFailures(endpoint.dsn) + 1
      });
      
      this.recordHealthHistory(endpoint.dsn, {
        timestamp: new Date(),
        healthy: false,
        responseTime,
        error: error.message
      });
    }
  }
  
  getHealthCheckUrl(endpoint) {
    // 从DSN中提取基础URL并添加健康检查路径
    const dsnMatch = endpoint.dsn.match(/https:\/\/[^@]+@([^/]+)/);
    if (dsnMatch) {
      return `https://${dsnMatch[1]}${endpoint.healthCheck || '/health'}`;
    }
    
    throw new Error(`Invalid DSN format: ${endpoint.dsn}`);
  }
  
  updateHealthStatus(dsn, status) {
    this.healthStatus.set(dsn, {
      ...this.healthStatus.get(dsn),
      ...status
    });
  }
  
  recordHealthHistory(dsn, record) {
    const history = this.healthHistory.get(dsn) || [];
    history.push(record);
    
    // 保留最近100条记录
    if (history.length > 100) {
      history.shift();
    }
    
    this.healthHistory.set(dsn, history);
  }
  
  isHealthy(dsn) {
    const status = this.healthStatus.get(dsn);
    if (!status) return false;
    
    // 连续失败超过3次认为不健康
    return status.healthy && status.consecutiveFailures < 3;
  }
  
  getHealthScore(dsn) {
    const status = this.healthStatus.get(dsn);
    if (!status) return 0;
    
    const history = this.healthHistory.get(dsn) || [];
    const recentHistory = history.slice(-10); // 最近10次检查
    
    if (recentHistory.length === 0) return status.healthy ? 1 : 0;
    
    const successRate = recentHistory.filter(h => h.healthy).length / recentHistory.length;
    const avgResponseTime = recentHistory.reduce((sum, h) => sum + h.responseTime, 0) / recentHistory.length;
    
    // 综合成功率和响应时间计算健康分数
    const responseTimeScore = Math.max(0, 1 - (avgResponseTime / 5000)); // 5秒为基准
    
    return (successRate * 0.7) + (responseTimeScore * 0.3);
  }
  
  getConsecutiveFailures(dsn) {
    const status = this.healthStatus.get(dsn);
    return status ? status.consecutiveFailures : 0;
  }
  
  getHealthStats() {
    const stats = {};
    
    for (const [dsn, status] of this.healthStatus.entries()) {
      const history = this.healthHistory.get(dsn) || [];
      const recentHistory = history.slice(-10);
      
      stats[dsn] = {
        healthy: this.isHealthy(dsn),
        healthScore: this.getHealthScore(dsn),
        lastCheck: status.lastCheck,
        lastError: status.lastError,
        responseTime: status.responseTime,
        consecutiveFailures: status.consecutiveFailures,
        successRate: recentHistory.length > 0 ? 
          recentHistory.filter(h => h.healthy).length / recentHistory.length : 0,
        avgResponseTime: recentHistory.length > 0 ? 
          recentHistory.reduce((sum, h) => sum + h.responseTime, 0) / recentHistory.length : 0
      };
    }
    
    return stats;
  }
}
```

## 6. 运维最佳实践

### 6.1 自动化运维管理器

```javascript
// 自动化运维管理器
class AutomatedOpsManager {
  constructor(options = {}) {
    this.options = {
      enableAutoScaling: true,
      enableAutoHealing: true,
      enableBackup: true,
      backupInterval: 24 * 60 * 60 * 1000, // 24小时
      healthCheckInterval: 30000, // 30秒
      scalingThresholds: {
        cpu: 80, // CPU使用率阈值
        memory: 85, // 内存使用率阈值
        responseTime: 2000, // 响应时间阈值(ms)
        errorRate: 0.05 // 错误率阈值(5%)
      },
      ...options
    };
    
    this.metricsCollector = new MetricsCollector();
    this.scalingManager = new AutoScalingManager();
    this.healingManager = new AutoHealingManager();
    this.backupManager = new BackupManager();
    this.alertManager = new AlertManager();
    
    this.init();
  }
  
  init() {
    this.startHealthMonitoring();
    
    if (this.options.enableAutoScaling) {
      this.startAutoScaling();
    }
    
    if (this.options.enableAutoHealing) {
      this.startAutoHealing();
    }
    
    if (this.options.enableBackup) {
      this.startBackupSchedule();
    }
  }
  
  startHealthMonitoring() {
    setInterval(async () => {
      try {
        const health = await this.checkSystemHealth();
        await this.processHealthStatus(health);
      } catch (error) {
        console.error('Health monitoring error:', error);
        await this.alertManager.sendAlert({
          type: 'error',
          message: 'Health monitoring failed',
          error: error.message,
          timestamp: new Date().toISOString()
        });
      }
    }, this.options.healthCheckInterval);
  }
  
  async checkSystemHealth() {
    const metrics = await this.metricsCollector.collectAllMetrics();
    
    return {
      timestamp: new Date().toISOString(),
      overall: this.calculateOverallHealth(metrics),
      components: {
        loadBalancer: this.checkLoadBalancerHealth(metrics),
        cache: this.checkCacheHealth(metrics),
        endpoints: this.checkEndpointsHealth(metrics),
        performance: this.checkPerformanceHealth(metrics)
      },
      metrics
    };
  }
  
  calculateOverallHealth(metrics) {
    const thresholds = this.options.scalingThresholds;
    
    const checks = [
      metrics.cpu < thresholds.cpu,
      metrics.memory < thresholds.memory,
      metrics.averageResponseTime < thresholds.responseTime,
      metrics.errorRate < thresholds.errorRate
    ];
    
    const healthyChecks = checks.filter(check => check).length;
    const healthScore = (healthyChecks / checks.length) * 100;
    
    return {
      score: healthScore,
      status: healthScore >= 80 ? 'healthy' : healthScore >= 60 ? 'warning' : 'critical',
      issues: this.identifyHealthIssues(metrics, thresholds)
    };
  }
  
  identifyHealthIssues(metrics, thresholds) {
    const issues = [];
    
    if (metrics.cpu >= thresholds.cpu) {
      issues.push({
        type: 'cpu',
        severity: 'high',
        message: `CPU usage ${metrics.cpu}% exceeds threshold ${thresholds.cpu}%`,
        value: metrics.cpu,
        threshold: thresholds.cpu
      });
    }
    
    if (metrics.memory >= thresholds.memory) {
      issues.push({
        type: 'memory',
        severity: 'high',
        message: `Memory usage ${metrics.memory}% exceeds threshold ${thresholds.memory}%`,
        value: metrics.memory,
        threshold: thresholds.memory
      });
    }
    
    if (metrics.averageResponseTime >= thresholds.responseTime) {
      issues.push({
        type: 'response_time',
        severity: 'medium',
        message: `Response time ${metrics.averageResponseTime}ms exceeds threshold ${thresholds.responseTime}ms`,
        value: metrics.averageResponseTime,
        threshold: thresholds.responseTime
      });
    }
    
    if (metrics.errorRate >= thresholds.errorRate) {
      issues.push({
        type: 'error_rate',
        severity: 'high',
        message: `Error rate ${(metrics.errorRate * 100).toFixed(2)}% exceeds threshold ${(thresholds.errorRate * 100).toFixed(2)}%`,
        value: metrics.errorRate,
        threshold: thresholds.errorRate
      });
    }
    
    return issues;
  }
  
  async processHealthStatus(health) {
    // 记录健康状态
    await this.logHealthStatus(health);
    
    // 处理健康问题
    if (health.overall.issues.length > 0) {
      await this.handleHealthIssues(health.overall.issues);
    }
    
    // 触发自动扩缩容
    if (this.options.enableAutoScaling) {
      await this.evaluateScaling(health);
    }
    
    // 触发自动修复
    if (this.options.enableAutoHealing) {
      await this.evaluateHealing(health);
    }
  }
  
  async handleHealthIssues(issues) {
    for (const issue of issues) {
      await this.alertManager.sendAlert({
        type: 'health_issue',
        severity: issue.severity,
        message: issue.message,
        details: issue,
        timestamp: new Date().toISOString()
      });
      
      // 根据问题类型执行相应的处理
      switch (issue.type) {
        case 'cpu':
          await this.handleCpuIssue(issue);
          break;
        case 'memory':
          await this.handleMemoryIssue(issue);
          break;
        case 'response_time':
          await this.handleResponseTimeIssue(issue);
          break;
        case 'error_rate':
          await this.handleErrorRateIssue(issue);
          break;
      }
    }
  }
  
  async handleCpuIssue(issue) {
    console.log('Handling CPU issue:', issue.message);
    
    // 可能的处理策略：
    // 1. 触发自动扩容
    // 2. 优化缓存策略
    // 3. 限制请求频率
    
    if (this.options.enableAutoScaling) {
      await this.scalingManager.scaleUp({
        reason: 'high_cpu',
        currentValue: issue.value,
        threshold: issue.threshold
      });
    }
  }
  
  async handleMemoryIssue(issue) {
    console.log('Handling memory issue:', issue.message);
    
    // 可能的处理策略：
    // 1. 清理缓存
    // 2. 触发垃圾回收
    // 3. 重启服务实例
    
    // 清理缓存
    await this.clearCache();
    
    // 如果内存使用率仍然很高，考虑扩容
    if (issue.value > 90) {
      await this.scalingManager.scaleUp({
        reason: 'high_memory',
        currentValue: issue.value,
        threshold: issue.threshold
      });
    }
  }
  
  async clearCache() {
    try {
      // 清理应用缓存
      if (typeof window !== 'undefined' && window.caches) {
        const cacheNames = await window.caches.keys();
        await Promise.all(
          cacheNames.map(cacheName => window.caches.delete(cacheName))
        );
      }
      
      // 清理内存缓存
      if (global.gc) {
        global.gc();
      }
      
      console.log('Cache cleared successfully');
    } catch (error) {
      console.error('Failed to clear cache:', error);
    }
  }
  
  startAutoScaling() {
    console.log('Auto-scaling enabled');
    // 自动扩缩容逻辑已在健康检查中处理
  }
  
  startAutoHealing() {
    console.log('Auto-healing enabled');
    
    // 监听服务故障
    setInterval(async () => {
      try {
        await this.checkAndHealServices();
      } catch (error) {
        console.error('Auto-healing error:', error);
      }
    }, this.options.healthCheckInterval * 2); // 检查频率稍低
  }
  
  async checkAndHealServices() {
    const services = await this.getServiceStatus();
    
    for (const service of services) {
      if (!service.healthy) {
        await this.healService(service);
      }
    }
  }
  
  async getServiceStatus() {
    // 模拟获取服务状态
    return [
      {
        name: 'sentry-monitor',
        healthy: Math.random() > 0.1, // 90%健康率
        lastCheck: new Date().toISOString()
      },
      {
        name: 'load-balancer',
        healthy: Math.random() > 0.05, // 95%健康率
        lastCheck: new Date().toISOString()
      }
    ];
  }
  
  async healService(service) {
    console.log(`Healing service: ${service.name}`);
    
    try {
      // 尝试重启服务
      await this.restartService(service.name);
      
      // 等待服务恢复
      await this.waitForServiceRecovery(service.name);
      
      await this.alertManager.sendAlert({
        type: 'healing_success',
        message: `Service ${service.name} healed successfully`,
        service: service.name,
        timestamp: new Date().toISOString()
      });
      
    } catch (error) {
      await this.alertManager.sendAlert({
        type: 'healing_failed',
        message: `Failed to heal service ${service.name}`,
        service: service.name,
        error: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }
  
  async restartService(serviceName) {
    console.log(`Restarting service: ${serviceName}`);
    
    // 在实际环境中，这里会调用容器编排系统的API
    // 例如：kubectl restart deployment/${serviceName}
    // 或者：docker restart ${serviceName}
    
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log(`Service ${serviceName} restarted`);
        resolve();
      }, 2000);
    });
  }
  
  async waitForServiceRecovery(serviceName, maxWaitTime = 60000) {
    const startTime = Date.now();
    
    while (Date.now() - startTime < maxWaitTime) {
      try {
        const isHealthy = await this.checkServiceHealth(serviceName);
        if (isHealthy) {
          console.log(`Service ${serviceName} recovered`);
          return true;
        }
      } catch (error) {
        console.log(`Service ${serviceName} still recovering...`);
      }
      
      await new Promise(resolve => setTimeout(resolve, 5000)); // 等待5秒
    }
    
    throw new Error(`Service ${serviceName} failed to recover within ${maxWaitTime}ms`);
  }
  
  async checkServiceHealth(serviceName) {
    // 模拟健康检查
    return Math.random() > 0.3; // 70%成功率
  }
  
  startBackupSchedule() {
    console.log('Backup schedule started');
    
    // 立即执行一次备份
    this.performBackup();
    
    // 定期备份
    setInterval(() => {
      this.performBackup();
    }, this.options.backupInterval);
  }
  
  async performBackup() {
    try {
      console.log('Starting backup...');
      
      const backupData = await this.collectBackupData();
      const backupId = await this.backupManager.createBackup(backupData);
      
      await this.alertManager.sendAlert({
        type: 'backup_success',
        message: `Backup completed successfully`,
        backupId,
        timestamp: new Date().toISOString()
      });
      
      console.log(`Backup completed: ${backupId}`);
      
    } catch (error) {
      await this.alertManager.sendAlert({
        type: 'backup_failed',
        message: `Backup failed: ${error.message}`,
        error: error.message,
        timestamp: new Date().toISOString()
      });
      
      console.error('Backup failed:', error);
    }
  }
  
  async collectBackupData() {
    return {
      timestamp: new Date().toISOString(),
      configuration: await this.getSystemConfiguration(),
      metrics: await this.metricsCollector.getHistoricalMetrics(),
      alerts: await this.alertManager.getAlertHistory(),
      logs: await this.getSystemLogs()
    };
  }
  
  async getSystemConfiguration() {
    return {
      loadBalancer: this.options,
      scaling: this.scalingManager.getConfiguration(),
      monitoring: this.metricsCollector.getConfiguration()
    };
  }
  
  async getSystemLogs() {
    // 模拟获取系统日志
    return [
      {
        timestamp: new Date().toISOString(),
        level: 'info',
        message: 'System backup initiated'
      }
    ];
  }
  
  async logHealthStatus(health) {
    const logEntry = {
      timestamp: health.timestamp,
      type: 'health_check',
      status: health.overall.status,
      score: health.overall.score,
      issues: health.overall.issues.length,
      components: Object.keys(health.components).reduce((acc, key) => {
        acc[key] = health.components[key].status || 'unknown';
        return acc;
      }, {})
    };
    
    console.log('Health status:', logEntry);
    
    // 在实际环境中，这里会写入日志系统
    // 例如：ELK Stack, Fluentd, 或云日志服务
  }
  
  // 获取运维统计信息
  async getOpsStats() {
    return {
      uptime: this.calculateUptime(),
      totalAlerts: await this.alertManager.getTotalAlerts(),
      successfulBackups: await this.backupManager.getSuccessfulBackups(),
      healingActions: await this.healingManager.getHealingStats(),
      scalingEvents: await this.scalingManager.getScalingStats()
    };
  }
  
  calculateUptime() {
    // 计算系统运行时间
    const startTime = this.startTime || Date.now();
    const uptime = Date.now() - startTime;
    
    return {
      milliseconds: uptime,
      seconds: Math.floor(uptime / 1000),
      minutes: Math.floor(uptime / (1000 * 60)),
      hours: Math.floor(uptime / (1000 * 60 * 60)),
      days: Math.floor(uptime / (1000 * 60 * 60 * 24))
    };
  }
}
```

### 6.2 监控告警系统

```javascript
// 高级监控告警系统
class AdvancedMonitoringSystem {
  constructor(options = {}) {
    this.options = {
      alertChannels: ['slack', 'email', 'webhook'],
      alertRules: {
        errorRate: {
          warning: 0.02, // 2%
          critical: 0.05 // 5%
        },
        responseTime: {
          warning: 1000, // 1秒
          critical: 2000 // 2秒
        },
        availability: {
          warning: 0.99, // 99%
          critical: 0.95 // 95%
        }
      },
      escalationRules: {
        warning: {
          delay: 5 * 60 * 1000, // 5分钟
          channels: ['slack']
        },
        critical: {
          delay: 1 * 60 * 1000, // 1分钟
          channels: ['slack', 'email', 'webhook']
        }
      },
      ...options
    };
    
    this.alertManager = new AlertManager();
    this.metricsCollector = new MetricsCollector();
    this.notificationManager = new NotificationManager();
    
    this.activeAlerts = new Map();
    this.alertHistory = [];
    
    this.init();
  }
  
  init() {
    this.startMonitoring();
    this.setupAlertProcessing();
  }
  
  startMonitoring() {
    setInterval(async () => {
      try {
        const metrics = await this.metricsCollector.collectAllMetrics();
        await this.evaluateAlerts(metrics);
      } catch (error) {
        console.error('Monitoring error:', error);
      }
    }, 30000); // 每30秒检查一次
  }
  
  async evaluateAlerts(metrics) {
    const alerts = [];
    
    // 检查错误率
    const errorRateAlert = this.checkErrorRate(metrics.errorRate);
    if (errorRateAlert) alerts.push(errorRateAlert);
    
    // 检查响应时间
    const responseTimeAlert = this.checkResponseTime(metrics.averageResponseTime);
    if (responseTimeAlert) alerts.push(responseTimeAlert);
    
    // 检查可用性
    const availabilityAlert = this.checkAvailability(metrics.availability);
    if (availabilityAlert) alerts.push(availabilityAlert);
    
    // 检查资源使用率
    const resourceAlerts = this.checkResourceUsage(metrics);
    alerts.push(...resourceAlerts);
    
    // 处理告警
    for (const alert of alerts) {
      await this.processAlert(alert);
    }
    
    // 检查告警恢复
    await this.checkAlertRecovery(metrics);
  }
  
  checkErrorRate(errorRate) {
    const rules = this.options.alertRules.errorRate;
    
    if (errorRate >= rules.critical) {
      return {
        id: 'error_rate_critical',
        type: 'error_rate',
        severity: 'critical',
        message: `Critical: Error rate ${(errorRate * 100).toFixed(2)}% exceeds threshold ${(rules.critical * 100).toFixed(2)}%`,
        value: errorRate,
        threshold: rules.critical,
        timestamp: new Date().toISOString()
      };
    } else if (errorRate >= rules.warning) {
      return {
        id: 'error_rate_warning',
        type: 'error_rate',
        severity: 'warning',
        message: `Warning: Error rate ${(errorRate * 100).toFixed(2)}% exceeds threshold ${(rules.warning * 100).toFixed(2)}%`,
        value: errorRate,
        threshold: rules.warning,
        timestamp: new Date().toISOString()
      };
    }
    
    return null;
  }
  
  checkResponseTime(responseTime) {
    const rules = this.options.alertRules.responseTime;
    
    if (responseTime >= rules.critical) {
      return {
        id: 'response_time_critical',
        type: 'response_time',
        severity: 'critical',
        message: `Critical: Response time ${responseTime.toFixed(0)}ms exceeds threshold ${rules.critical}ms`,
        value: responseTime,
        threshold: rules.critical,
        timestamp: new Date().toISOString()
      };
    } else if (responseTime >= rules.warning) {
      return {
        id: 'response_time_warning',
        type: 'response_time',
        severity: 'warning',
        message: `Warning: Response time ${responseTime.toFixed(0)}ms exceeds threshold ${rules.warning}ms`,
        value: responseTime,
        threshold: rules.warning,
        timestamp: new Date().toISOString()
      };
    }
    
    return null;
  }
  
  checkAvailability(availability) {
    const rules = this.options.alertRules.availability;
    
    if (availability <= rules.critical) {
      return {
        id: 'availability_critical',
        type: 'availability',
        severity: 'critical',
        message: `Critical: Availability ${(availability * 100).toFixed(2)}% below threshold ${(rules.critical * 100).toFixed(2)}%`,
        value: availability,
        threshold: rules.critical,
        timestamp: new Date().toISOString()
      };
    } else if (availability <= rules.warning) {
      return {
        id: 'availability_warning',
        type: 'availability',
        severity: 'warning',
        message: `Warning: Availability ${(availability * 100).toFixed(2)}% below threshold ${(rules.warning * 100).toFixed(2)}%`,
        value: availability,
        threshold: rules.warning,
        timestamp: new Date().toISOString()
      };
    }
    
    return null;
  }
  
  checkResourceUsage(metrics) {
    const alerts = [];
    
    // CPU使用率
    if (metrics.cpu >= 90) {
      alerts.push({
        id: 'cpu_critical',
        type: 'cpu',
        severity: 'critical',
        message: `Critical: CPU usage ${metrics.cpu.toFixed(1)}% is very high`,
        value: metrics.cpu,
        threshold: 90,
        timestamp: new Date().toISOString()
      });
    } else if (metrics.cpu >= 80) {
      alerts.push({
        id: 'cpu_warning',
        type: 'cpu',
        severity: 'warning',
        message: `Warning: CPU usage ${metrics.cpu.toFixed(1)}% is high`,
        value: metrics.cpu,
        threshold: 80,
        timestamp: new Date().toISOString()
      });
    }
    
    // 内存使用率
    if (metrics.memory >= 95) {
      alerts.push({
        id: 'memory_critical',
        type: 'memory',
        severity: 'critical',
        message: `Critical: Memory usage ${metrics.memory.toFixed(1)}% is very high`,
        value: metrics.memory,
        threshold: 95,
        timestamp: new Date().toISOString()
      });
    } else if (metrics.memory >= 85) {
      alerts.push({
        id: 'memory_warning',
        type: 'memory',
        severity: 'warning',
        message: `Warning: Memory usage ${metrics.memory.toFixed(1)}% is high`,
        value: metrics.memory,
        threshold: 85,
        timestamp: new Date().toISOString()
      });
    }
    
    return alerts;
  }
  
  async processAlert(alert) {
    const existingAlert = this.activeAlerts.get(alert.id);
    
    if (existingAlert) {
      // 更新现有告警
      existingAlert.count = (existingAlert.count || 1) + 1;
      existingAlert.lastSeen = alert.timestamp;
      existingAlert.value = alert.value;
    } else {
      // 新告警
      alert.count = 1;
      alert.firstSeen = alert.timestamp;
      alert.lastSeen = alert.timestamp;
      
      this.activeAlerts.set(alert.id, alert);
      this.alertHistory.push(alert);
      
      // 发送告警通知
      await this.sendAlertNotification(alert);
    }
  }
  
  async sendAlertNotification(alert) {
    const escalationRule = this.options.escalationRules[alert.severity];
    
    if (escalationRule) {
      // 延迟发送（避免误报）
      setTimeout(async () => {
        // 检查告警是否仍然活跃
        if (this.activeAlerts.has(alert.id)) {
          for (const channel of escalationRule.channels) {
            await this.notificationManager.send(channel, {
              alert,
              template: this.getAlertTemplate(alert, channel)
            });
          }
        }
      }, escalationRule.delay);
    }
  }
  
  getAlertTemplate(alert, channel) {
    const templates = {
      slack: {
        text: `🚨 ${alert.severity.toUpperCase()} Alert`,
        attachments: [
          {
            color: alert.severity === 'critical' ? 'danger' : 'warning',
            fields: [
              {
                title: 'Message',
                value: alert.message,
                short: false
              },
              {
                title: 'Type',
                value: alert.type,
                short: true
              },
              {
                title: 'Value',
                value: alert.value,
                short: true
              },
              {
                title: 'Timestamp',
                value: alert.timestamp,
                short: true
              }
            ]
          }
        ]
      },
      email: {
        subject: `[${alert.severity.toUpperCase()}] Sentry Monitoring Alert`,
        html: `
          <h2>Alert Notification</h2>
          <p><strong>Severity:</strong> ${alert.severity}</p>
          <p><strong>Type:</strong> ${alert.type}</p>
          <p><strong>Message:</strong> ${alert.message}</p>
          <p><strong>Value:</strong> ${alert.value}</p>
          <p><strong>Threshold:</strong> ${alert.threshold}</p>
          <p><strong>Timestamp:</strong> ${alert.timestamp}</p>
        `
      },
      webhook: {
        alert_id: alert.id,
        severity: alert.severity,
        type: alert.type,
        message: alert.message,
        value: alert.value,
        threshold: alert.threshold,
        timestamp: alert.timestamp
      }
    };
    
    return templates[channel] || templates.webhook;
  }
  
  async checkAlertRecovery(metrics) {
    const recoveredAlerts = [];
    
    for (const [alertId, alert] of this.activeAlerts) {
      if (this.isAlertRecovered(alert, metrics)) {
        recoveredAlerts.push(alert);
        this.activeAlerts.delete(alertId);
      }
    }
    
    // 发送恢复通知
    for (const alert of recoveredAlerts) {
      await this.sendRecoveryNotification(alert);
    }
  }
  
  isAlertRecovered(alert, metrics) {
    switch (alert.type) {
      case 'error_rate':
        return metrics.errorRate < alert.threshold * 0.8; // 恢复阈值
      case 'response_time':
        return metrics.averageResponseTime < alert.threshold * 0.8;
      case 'availability':
        return metrics.availability > alert.threshold * 1.02;
      case 'cpu':
        return metrics.cpu < alert.threshold * 0.8;
      case 'memory':
        return metrics.memory < alert.threshold * 0.8;
      default:
        return false;
    }
  }
  
  async sendRecoveryNotification(alert) {
    const recoveryAlert = {
      ...alert,
      id: `${alert.id}_recovery`,
      severity: 'info',
      message: `Recovered: ${alert.type} is back to normal`,
      timestamp: new Date().toISOString()
    };
    
    for (const channel of this.options.alertChannels) {
      await this.notificationManager.send(channel, {
        alert: recoveryAlert,
        template: this.getRecoveryTemplate(recoveryAlert, channel)
      });
    }
  }
  
  getRecoveryTemplate(alert, channel) {
    const templates = {
      slack: {
        text: `✅ Alert Recovered`,
        attachments: [
          {
            color: 'good',
            fields: [
              {
                title: 'Message',
                value: alert.message,
                short: false
              },
              {
                title: 'Type',
                value: alert.type,
                short: true
              },
              {
                title: 'Recovery Time',
                value: alert.timestamp,
                short: true
              }
            ]
          }
        ]
      },
      email: {
        subject: `[RECOVERED] Sentry Monitoring Alert`,
        html: `
          <h2>Alert Recovery Notification</h2>
          <p><strong>Type:</strong> ${alert.type}</p>
          <p><strong>Message:</strong> ${alert.message}</p>
          <p><strong>Recovery Time:</strong> ${alert.timestamp}</p>
        `
      }
    };
    
    return templates[channel] || { message: alert.message };
  }
  
  // 获取告警统计
  getAlertStats() {
    return {
      active: this.activeAlerts.size,
      total: this.alertHistory.length,
      byType: this.getAlertsByType(),
      bySeverity: this.getAlertsBySeverity(),
      recentAlerts: this.alertHistory.slice(-10)
    };
  }
  
  getAlertsByType() {
    const byType = {};
    for (const alert of this.alertHistory) {
      byType[alert.type] = (byType[alert.type] || 0) + 1;
    }
    return byType;
  }
  
  getAlertsBySeverity() {
    const bySeverity = {};
    for (const alert of this.alertHistory) {
      bySeverity[alert.severity] = (bySeverity[alert.severity] || 0) + 1;
    }
    return bySeverity;
  }
}
```

## 7. 核心价值与实施建议

### 7.1 核心价值

1. **高可用性保障**
   - 多层次故障转移机制
   - 自动化故障检测与恢复
   - 零停机时间部署策略

2. **企业级扩展性**
   - 容器化部署支持
   - 自动扩缩容机制
   - 微服务架构适配

3. **智能化运维**
   - 自动化监控告警
   - 预测性维护
   - 智能故障诊断

4. **全面的可观测性**
   - 实时性能监控
   - 详细的指标收集
   - 可视化运维仪表板

### 7.2 实施建议

1. **分阶段部署**
   ```javascript
   // 阶段1：基础监控
   const phase1 = {
     components: ['基础负载均衡', '健康检查', '简单告警'],
     duration: '2-4周',
     success_criteria: ['基本监控功能', '告警通知正常']
   };
   
   // 阶段2：高级功能
   const phase2 = {
     components: ['自动扩缩容', '故障转移', '缓存优化'],
     duration: '4-6周',
     success_criteria: ['自动化运维', '性能优化']
   };
   
   // 阶段3：企业级特性
   const phase3 = {
     components: ['容器化部署', '监控仪表板', '高级告警'],
     duration: '6-8周',
     success_criteria: ['企业级部署', '完整可观测性']
   };
   ```

2. **团队培训计划**
   - 运维团队：容器化部署、监控告警
   - 开发团队：性能优化、故障排查
   - 管理团队：监控仪表板、运维报告

3. **监控指标体系**
   - SLA指标：可用性、响应时间、错误率
   - 业务指标：用户体验、转化率
   - 技术指标：资源使用率、性能指标

### 7.3 未来发展趋势

1. **AI驱动的运维**
   - 智能异常检测
   - 自动化根因分析
   - 预测性扩容

2. **云原生架构**
   - Serverless部署
   - 边缘计算支持
   - 多云部署策略

3. **可观测性增强**
   - 分布式链路追踪
   - 实时日志分析
   - 业务指标关联

## 8. 总结

前端Sentry企业级部署与运维体系为现代Web应用提供了完整的监控基础设施解决方案。通过高可用架构设计、自动化运维管理、智能监控告警等核心功能，确保了系统的稳定性和可靠性。

这套体系不仅提供了技术层面的保障，更重要的是建立了完整的运维流程和最佳实践，帮助团队构建真正企业级的前端监控系统。随着技术的不断发展，这套体系将继续演进，为企业数字化转型提供坚实的技术支撑。

## 2. 故障转移与恢复

### 2.1 故障转移管理器

```javascript
// 故障转移管理器
class FailoverManager {
  constructor(endpoints) {
    this.endpoints = endpoints;
    this.failureThreshold = 3; // 连续失败阈值
    this.recoveryCheckInterval = 60000; // 1分钟
    this.circuitBreaker = new Map();
    this.alertManager = new AlertManager();
    
    this.initializeCircuitBreakers();
    this.startRecoveryChecking();
  }
  
  initializeCircuitBreakers() {
    Object.values(this.endpoints).forEach(endpoint => {
      this.circuitBreaker.set(endpoint.dsn, {
        state: 'CLOSED', // CLOSED, OPEN, HALF_OPEN
        failureCount: 0,
        lastFailureTime: null,
        nextRetryTime: null
      });
    });
  }
  
  async handleFailure(request, error) {
    console.error('Primary endpoint failed:', error.message);
    
    // 更新断路器状态
    this.updateCircuitBreaker(this.getCurrentEndpoint().dsn, false);
    
    // 尝试故障转移
    const fallbackEndpoints = this.getFallbackEndpoints();
    
    for (const endpoint of fallbackEndpoints) {
      try {
        const response = await this.sendToEndpoint(request, endpoint);
        
        // 发送成功，发送告警
        this.alertManager.sendFailoverAlert({
          originalEndpoint: this.getCurrentEndpoint().dsn,
          fallbackEndpoint: endpoint.dsn,
          error: error.message,
          timestamp: new Date()
        });
        
        return response;
        
      } catch (fallbackError) {
        console.warn(`Fallback endpoint ${endpoint.dsn} also failed:`, fallbackError.message);
        this.updateCircuitBreaker(endpoint.dsn, false);
      }
    }
    
    // 所有端点都失败，发送严重告警
    this.alertManager.sendCriticalAlert({
      message: 'All Sentry endpoints failed',
      originalError: error.message,
      timestamp: new Date()
    });
    
    // 返回空响应或缓存响应
    return this.getEmergencyResponse();
  }
  
  async sendToEndpoint(request, endpoint) {
    const circuitState = this.circuitBreaker.get(endpoint.dsn);
    
    // 检查断路器状态
    if (circuitState.state === 'OPEN') {
      if (Date.now() < circuitState.nextRetryTime) {
        throw new Error('Circuit breaker is OPEN');
      } else {
        // 尝试半开状态
        circuitState.state = 'HALF_OPEN';
      }
    }
    
    try {
      const response = await fetch(this.buildEndpointUrl(endpoint), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Sentry-Auth': this.getAuthHeader(endpoint)
        },
        body: JSON.stringify(request),
        timeout: 10000 // 10秒超时
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      // 成功，重置断路器
      this.updateCircuitBreaker(endpoint.dsn, true);
      
      return response;
      
    } catch (error) {
      this.updateCircuitBreaker(endpoint.dsn, false);
      throw error;
    }
  }
  
  updateCircuitBreaker(dsn, success) {
    const circuit = this.circuitBreaker.get(dsn);
    if (!circuit) return;
    
    if (success) {
      // 成功，重置计数器
      circuit.failureCount = 0;
      circuit.state = 'CLOSED';
      circuit.lastFailureTime = null;
      circuit.nextRetryTime = null;
    } else {
      // 失败，增加计数器
      circuit.failureCount++;
      circuit.lastFailureTime = Date.now();
      
      if (circuit.failureCount >= this.failureThreshold) {
        // 打开断路器
        circuit.state = 'OPEN';
        circuit.nextRetryTime = Date.now() + this.getBackoffDelay(circuit.failureCount);
        
        console.warn(`Circuit breaker opened for ${dsn}`);
      }
    }
  }
  
  getBackoffDelay(failureCount) {
    // 指数退避，最大5分钟
    const baseDelay = 30000; // 30秒
    const maxDelay = 300000; // 5分钟
    
    return Math.min(baseDelay * Math.pow(2, failureCount - this.failureThreshold), maxDelay);
  }
  
  getFallbackEndpoints() {
    // 按优先级排序，排除当前失败的端点
    const currentDsn = this.getCurrentEndpoint().dsn;
    
    return Object.values(this.endpoints)
      .filter(endpoint => endpoint.dsn !== currentDsn)
      .sort((a, b) => a.priority - b.priority)
      .filter(endpoint => {
        const circuit = this.circuitBreaker.get(endpoint.dsn);
        return circuit.state !== 'OPEN' || Date.now() >= circuit.nextRetryTime;
      });
  }
  
  getCurrentEndpoint() {
    // 返回当前主要端点
    return Object.values(this.endpoints).find(endpoint => endpoint.priority === 1);
  }
  
  buildEndpointUrl(endpoint) {
    // 从DSN构建完整的端点URL
    const dsnMatch = endpoint.dsn.match(/https:\/\/([^@]+)@([^/]+)\/(.+)/);
    if (dsnMatch) {
      const [, key, host, projectId] = dsnMatch;
      return `https://${host}/api/${projectId}/store/`;
    }
    
    throw new Error(`Invalid DSN format: ${endpoint.dsn}`);
  }
  
  getAuthHeader(endpoint) {
    const timestamp = Math.floor(Date.now() / 1000);
    const key = this.extractKeyFromDsn(endpoint.dsn);
    
    return `Sentry sentry_version=7, sentry_timestamp=${timestamp}, sentry_key=${key}`;
  }
  
  extractKeyFromDsn(dsn) {
    const match = dsn.match(/https:\/\/([^@]+)@/);
    return match ? match[1] : '';
  }
  
  getEmergencyResponse() {
    // 返回模拟的成功响应
    return {
      status: 200,
      statusText: 'OK (Emergency Mode)',
      ok: true,
      json: () => Promise.resolve({ id: 'emergency-mode' })
    };
  }
  
  startRecoveryChecking() {
    setInterval(() => {
      this.checkRecovery();
    }, this.recoveryCheckInterval);
  }
  
  async checkRecovery() {
    for (const [dsn, circuit] of this.circuitBreaker.entries()) {
      if (circuit.state === 'OPEN' && Date.now() >= circuit.nextRetryTime) {
        try {
          // 尝试健康检查
          const endpoint = Object.values(this.endpoints).find(e => e.dsn === dsn);
          if (endpoint) {
            await this.performHealthCheck(endpoint);
            console.log(`Endpoint ${dsn} recovered`);
            
            // 发送恢复告警
            this.alertManager.sendRecoveryAlert({
              endpoint: dsn,
              timestamp: new Date()
            });
          }
        } catch (error) {
          console.warn(`Recovery check failed for ${dsn}:`, error.message);
          this.updateCircuitBreaker(dsn, false);
        }
      }
    }
  }
  
  async performHealthCheck(endpoint) {
    const response = await fetch(this.getHealthCheckUrl(endpoint), {
      method: 'GET',
      timeout: 5000
    });
    
    if (!response.ok) {
      throw new Error(`Health check failed: ${response.status}`);
    }
    
    this.updateCircuitBreaker(endpoint.dsn, true);
  }
  
  getHealthCheckUrl(endpoint) {
    const dsnMatch = endpoint.dsn.match(/https:\/\/[^@]+@([^/]+)/);
    if (dsnMatch) {
      return `https://${dsnMatch[1]}/health`;
    }
    
    throw new Error(`Invalid DSN format: ${endpoint.dsn}`);
  }
  
  // 获取故障转移统计信息
  getFailoverStats() {
    const stats = {
      circuitBreakers: {},
      totalFailovers: 0,
      activeFailovers: 0
    };
    
    for (const [dsn, circuit] of this.circuitBreaker.entries()) {
      stats.circuitBreakers[dsn] = {
        state: circuit.state,
        failureCount: circuit.failureCount,
        lastFailureTime: circuit.lastFailureTime,
        nextRetryTime: circuit.nextRetryTime
      };
      
      if (circuit.state === 'OPEN') {
        stats.activeFailovers++;
      }
    }
    
    return stats;
  }
}
```

### 2.2 告警管理器

```javascript
// 告警管理器
class AlertManager {
  constructor(options = {}) {
    this.options = {
      enableSlack: true,
      enableEmail: true,
      enablePagerDuty: false,
      slackWebhook: process.env.SLACK_WEBHOOK_URL,
      emailEndpoint: process.env.EMAIL_API_ENDPOINT,
      pagerDutyKey: process.env.PAGERDUTY_API_KEY,
      ...options
    };
    
    this.alertHistory = [];
    this.rateLimiter = new Map();
  }
  
  async sendFailoverAlert(details) {
    const alertKey = `failover_${details.originalEndpoint}`;
    
    // 速率限制：同一端点的故障转移告警5分钟内只发送一次
    if (this.isRateLimited(alertKey, 300000)) {
      return;
    }
    
    const alert = {
      type: 'failover',
      severity: 'warning',
      title: 'Sentry Endpoint Failover',
      message: `Primary Sentry endpoint failed, switched to fallback`,
      details: {
        originalEndpoint: details.originalEndpoint,
        fallbackEndpoint: details.fallbackEndpoint,
        error: details.error,
        timestamp: details.timestamp
      }
    };
    
    await this.sendAlert(alert);
    this.recordAlert(alert);
  }
  
  async sendCriticalAlert(details) {
    const alert = {
      type: 'critical_failure',
      severity: 'critical',
      title: 'All Sentry Endpoints Failed',
      message: 'All configured Sentry endpoints are unavailable',
      details: {
        originalError: details.originalError,
        timestamp: details.timestamp
      }
    };
    
    await this.sendAlert(alert);
    this.recordAlert(alert);
  }
  
  async sendRecoveryAlert(details) {
    const alert = {
      type: 'recovery',
      severity: 'info',
      title: 'Sentry Endpoint Recovered',
      message: 'Previously failed Sentry endpoint has recovered',
      details: {
        endpoint: details.endpoint,
        timestamp: details.timestamp
      }
    };
    
    await this.sendAlert(alert);
    this.recordAlert(alert);
  }
  
  async sendAlert(alert) {
    const promises = [];
    
    if (this.options.enableSlack) {
      promises.push(this.sendSlackAlert(alert));
    }
    
    if (this.options.enableEmail) {
      promises.push(this.sendEmailAlert(alert));
    }
    
    if (this.options.enablePagerDuty && alert.severity === 'critical') {
      promises.push(this.sendPagerDutyAlert(alert));
    }
    
    await Promise.allSettled(promises);
  }
  
  async sendSlackAlert(alert) {
    if (!this.options.slackWebhook) {
      console.warn('Slack webhook not configured');
      return;
    }
    
    const color = this.getSeverityColor(alert.severity);
    const payload = {
      text: alert.title,
      attachments: [
        {
          color: color,
          title: alert.title,
          text: alert.message,
          fields: [
            {
              title: 'Severity',
              value: alert.severity.toUpperCase(),
              short: true
            },
            {
              title: 'Type',
              value: alert.type,
              short: true
            },
            {
              title: 'Timestamp',
              value: new Date().toISOString(),
              short: true
            }
          ],
          footer: 'Sentry Enterprise Monitor',
          ts: Math.floor(Date.now() / 1000)
        }
      ]
    };
    
    // 添加详细信息
    if (alert.details) {
      Object.entries(alert.details).forEach(([key, value]) => {
        if (value && typeof value === 'string') {
          payload.attachments[0].fields.push({
            title: key.charAt(0).toUpperCase() + key.slice(1),
            value: value,
            short: false
          });
        }
      });
    }
    
    try {
      const response = await fetch(this.options.slackWebhook, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });
      
      if (!response.ok) {
        throw new Error(`Slack API error: ${response.statusText}`);
      }
      
    } catch (error) {
      console.error('Failed to send Slack alert:', error);
    }
  }
  
  async sendEmailAlert(alert) {
    if (!this.options.emailEndpoint) {
      console.warn('Email endpoint not configured');
      return;
    }
    
    const emailData = {
      to: process.env.ALERT_EMAIL_RECIPIENTS?.split(',') || ['admin@company.com'],
      subject: `[${alert.severity.toUpperCase()}] ${alert.title}`,
      html: this.generateEmailHTML(alert),
      priority: alert.severity === 'critical' ? 'high' : 'normal'
    };
    
    try {
      const response = await fetch(this.options.emailEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${process.env.EMAIL_API_TOKEN}`
        },
        body: JSON.stringify(emailData)
      });
      
      if (!response.ok) {
        throw new Error(`Email API error: ${response.statusText}`);
      }
      
    } catch (error) {
      console.error('Failed to send email alert:', error);
    }
  }
  
  async sendPagerDutyAlert(alert) {
    if (!this.options.pagerDutyKey) {
      console.warn('PagerDuty key not configured');
      return;
    }
    
    const payload = {
      routing_key: this.options.pagerDutyKey,
      event_action: 'trigger',
      dedup_key: `sentry_${alert.type}_${Date.now()}`,
      payload: {
        summary: alert.title,
        source: 'Sentry Enterprise Monitor',
        severity: alert.severity,
        component: 'sentry-monitoring',
        group: 'infrastructure',
        class: 'monitoring',
        custom_details: alert.details
      }
    };
    
    try {
      const response = await fetch('https://events.pagerduty.com/v2/enqueue', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });
      
      if (!response.ok) {
        throw new Error(`PagerDuty API error: ${response.statusText}`);
      }
      
    } catch (error) {
      console.error('Failed to send PagerDuty alert:', error);
    }
  }
  
  generateEmailHTML(alert) {
    return `
      <html>
        <body style="font-family: Arial, sans-serif; margin: 20px;">
          <div style="border-left: 4px solid ${this.getSeverityColor(alert.severity)}; padding-left: 20px;">
            <h2 style="color: #333;">${alert.title}</h2>
            <p style="font-size: 16px; color: #666;">${alert.message}</p>
            
            <table style="border-collapse: collapse; width: 100%; margin-top: 20px;">
              <tr>
                <td style="border: 1px solid #ddd; padding: 8px; font-weight: bold;">Severity</td>
                <td style="border: 1px solid #ddd; padding: 8px;">${alert.severity.toUpperCase()}</td>
              </tr>
              <tr>
                <td style="border: 1px solid #ddd; padding: 8px; font-weight: bold;">Type</td>
                <td style="border: 1px solid #ddd; padding: 8px;">${alert.type}</td>
              </tr>
              <tr>
                <td style="border: 1px solid #ddd; padding: 8px; font-weight: bold;">Timestamp</td>
                <td style="border: 1px solid #ddd; padding: 8px;">${new Date().toISOString()}</td>
              </tr>
              ${Object.entries(alert.details || {}).map(([key, value]) => `
                <tr>
                  <td style="border: 1px solid #ddd; padding: 8px; font-weight: bold;">${key}</td>
                  <td style="border: 1px solid #ddd; padding: 8px;">${value}</td>
                </tr>
              `).join('')}
            </table>
          </div>
        </body>
      </html>
    `;
  }
  
  getSeverityColor(severity) {
    const colors = {
      info: '#36a64f',
      warning: '#ff9500',
      error: '#ff0000',
      critical: '#8b0000'
    };
    
    return colors[severity] || colors.info;
  }
  
  isRateLimited(key, windowMs) {
    const now = Date.now();
    const lastSent = this.rateLimiter.get(key);
    
    if (!lastSent || (now - lastSent) > windowMs) {
      this.rateLimiter.set(key, now);
      return false;
    }
    
    return true;
  }
  
  recordAlert(alert) {
    this.alertHistory.push({
      ...alert,
      sentAt: new Date()
    });
    
    // 保留最近1000条告警记录
    if (this.alertHistory.length > 1000) {
      this.alertHistory.shift();
    }
  }
  
  getAlertStats() {
    const now = Date.now();
    const last24h = this.alertHistory.filter(alert => 
      (now - new Date(alert.sentAt).getTime()) < 24 * 60 * 60 * 1000
    );
    
    const stats = {
      total: this.alertHistory.length,
      last24h: last24h.length,
      bySeverity: {},
      byType: {}
    };
    
    last24h.forEach(alert => {
      stats.bySeverity[alert.severity] = (stats.bySeverity[alert.severity] || 0) + 1;
      stats.byType[alert.type] = (stats.byType[alert.type] || 0) + 1;
    });
    
    return stats;
  }
}
```