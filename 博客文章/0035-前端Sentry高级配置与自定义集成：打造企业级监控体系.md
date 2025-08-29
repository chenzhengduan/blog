# 前端Sentry高级配置与自定义集成：打造企业级监控体系

## 引言

在现代前端开发中，Sentry已经成为错误监控和性能分析的标准工具。然而，要在企业级环境中充分发挥Sentry的潜力，需要深入了解其高级配置选项和自定义集成能力。本文将详细探讨如何通过高级配置和自定义集成，构建适合企业需求的监控体系。

## 一、Sentry高级配置详解

### 1.1 环境配置与多环境管理

```javascript
// sentryConfig.js
import * as Sentry from '@sentry/browser';
import { Integrations } from '@sentry/tracing';

// 环境配置管理器
export class SentryEnvironmentManager {
  constructor() {
    this.environments = {
      development: {
        dsn: process.env.REACT_APP_SENTRY_DSN_DEV,
        debug: true,
        tracesSampleRate: 1.0,
        environment: 'development',
        beforeSend: this.developmentBeforeSend.bind(this)
      },
      staging: {
        dsn: process.env.REACT_APP_SENTRY_DSN_STAGING,
        debug: false,
        tracesSampleRate: 0.5,
        environment: 'staging',
        beforeSend: this.stagingBeforeSend.bind(this)
      },
      production: {
        dsn: process.env.REACT_APP_SENTRY_DSN_PROD,
        debug: false,
        tracesSampleRate: 0.1,
        environment: 'production',
        beforeSend: this.productionBeforeSend.bind(this)
      }
    };
  }
  
  // 初始化Sentry配置
  initializeSentry() {
    const environment = this.getCurrentEnvironment();
    const config = this.environments[environment];
    
    if (!config || !config.dsn) {
      console.warn(`Sentry DSN not configured for environment: ${environment}`);
      return;
    }
    
    Sentry.init({
      dsn: config.dsn,
      debug: config.debug,
      environment: config.environment,
      tracesSampleRate: config.tracesSampleRate,
      beforeSend: config.beforeSend,
      
      // 高级配置选项
      maxBreadcrumbs: this.getMaxBreadcrumbs(environment),
      attachStacktrace: true,
      sendDefaultPii: false,
      
      // 集成配置
      integrations: this.getIntegrations(environment),
      
      // 传输配置
      transport: this.getTransportConfig(environment),
      
      // 发布配置
      release: this.getRelease(),
      
      // 初始作用域配置
      initialScope: this.getInitialScope(environment)
    });
    
    // 设置环境特定的标签和上下文
    this.setupEnvironmentContext(environment);
  }
  
  getCurrentEnvironment() {
    if (process.env.NODE_ENV === 'development') {
      return 'development';
    }
    
    if (window.location.hostname.includes('staging')) {
      return 'staging';
    }
    
    return 'production';
  }
  
  getMaxBreadcrumbs(environment) {
    const limits = {
      development: 100,
      staging: 50,
      production: 30
    };
    return limits[environment] || 30;
  }
  
  getIntegrations(environment) {
    const baseIntegrations = [
      new Integrations.BrowserTracing({
        tracingOrigins: this.getTracingOrigins(environment),
        routingInstrumentation: Sentry.reactRouterV6Instrumentation(
          React.useEffect,
          useLocation,
          useNavigationType,
          createRoutesFromChildren,
          matchRoutes
        )
      })
    ];
    
    // 环境特定集成
    if (environment === 'development') {
      baseIntegrations.push(
        new Sentry.Integrations.Debug({
          stringify: true,
          debugger: true
        })
      );
    }
    
    if (environment === 'production') {
      baseIntegrations.push(
        new Sentry.Integrations.Dedupe(),
        new Sentry.Integrations.ExtraErrorData({
          depth: 3
        })
      );
    }
    
    return baseIntegrations;
  }
  
  getTracingOrigins(environment) {
    const origins = {
      development: ['localhost', /^https:\/\/api-dev\.company\.com/],
      staging: [/^https:\/\/api-staging\.company\.com/],
      production: [/^https:\/\/api\.company\.com/]
    };
    return origins[environment] || [];
  }
  
  getTransportConfig(environment) {
    // 自定义传输配置
    return {
      // 请求超时
      timeout: environment === 'production' ? 10000 : 5000,
      
      // 重试配置
      retries: environment === 'production' ? 3 : 1,
      
      // 批处理配置
      maxQueueSize: environment === 'production' ? 100 : 50
    };
  }
  
  getRelease() {
    // 从环境变量或构建信息获取版本
    return process.env.REACT_APP_VERSION || 
           process.env.REACT_APP_GIT_SHA || 
           'unknown';
  }
  
  getInitialScope(environment) {
    return {
      tags: {
        environment,
        component: 'frontend',
        version: this.getRelease()
      },
      contexts: {
        app: {
          name: 'MyApp',
          version: this.getRelease(),
          build_type: environment
        },
        device: {
          family: 'Browser'
        }
      }
    };
  }
  
  setupEnvironmentContext(environment) {
    Sentry.configureScope(scope => {
      // 设置用户信息（如果可用）
      const user = this.getCurrentUser();
      if (user) {
        scope.setUser({
          id: user.id,
          email: environment !== 'production' ? user.email : undefined,
          username: user.username
        });
      }
      
      // 设置额外的环境上下文
      scope.setContext('environment_info', {
        node_env: process.env.NODE_ENV,
        build_time: process.env.REACT_APP_BUILD_TIME,
        git_branch: process.env.REACT_APP_GIT_BRANCH,
        deployment_id: process.env.REACT_APP_DEPLOYMENT_ID
      });
      
      // 设置性能相关标签
      scope.setTag('browser', this.getBrowserInfo());
      scope.setTag('device_type', this.getDeviceType());
    });
  }
  
  // 开发环境beforeSend
  developmentBeforeSend(event, hint) {
    // 开发环境记录所有事件
    console.group('🐛 Sentry Event (Development)');
    console.log('Event:', event);
    console.log('Hint:', hint);
    console.groupEnd();
    
    return event;
  }
  
  // 预发布环境beforeSend
  stagingBeforeSend(event, hint) {
    // 过滤掉一些开发相关的错误
    if (event.exception) {
      const error = event.exception.values[0];
      if (error.value && error.value.includes('ResizeObserver loop limit exceeded')) {
        return null; // 忽略这个常见的无害错误
      }
    }
    
    return event;
  }
  
  // 生产环境beforeSend
  productionBeforeSend(event, hint) {
    // 生产环境的严格过滤
    if (event.level === 'info' || event.level === 'debug') {
      return null; // 生产环境不发送info和debug级别的事件
    }
    
    // 过滤掉已知的无害错误
    if (event.exception) {
      const error = event.exception.values[0];
      const ignoredErrors = [
        'ResizeObserver loop limit exceeded',
        'Non-Error promise rejection captured',
        'Script error.'
      ];
      
      if (ignoredErrors.some(ignored => error.value?.includes(ignored))) {
        return null;
      }
    }
    
    // 限制事件大小
    if (JSON.stringify(event).length > 100000) {
      event.extra = { _truncated: true, _reason: 'Event too large' };
    }
    
    return event;
  }
  
  getCurrentUser() {
    // 从应用状态或localStorage获取用户信息
    try {
      const userStr = localStorage.getItem('user');
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  }
  
  getBrowserInfo() {
    const ua = navigator.userAgent;
    if (ua.includes('Chrome')) return 'Chrome';
    if (ua.includes('Firefox')) return 'Firefox';
    if (ua.includes('Safari')) return 'Safari';
    if (ua.includes('Edge')) return 'Edge';
    return 'Unknown';
  }
  
  getDeviceType() {
    const width = window.innerWidth;
    if (width < 768) return 'mobile';
    if (width < 1024) return 'tablet';
    return 'desktop';
  }
}

// 创建环境管理器实例并初始化
export const environmentManager = new SentryEnvironmentManager();
environmentManager.initializeSentry();
```

### 1.2 自定义传输和数据处理

```javascript
// sentryTransport.js
import * as Sentry from '@sentry/browser';

// 自定义传输类
export class CustomSentryTransport {
  constructor(options = {}) {
    this.options = {
      url: options.url,
      headers: options.headers || {},
      timeout: options.timeout || 10000,
      retries: options.retries || 3,
      retryDelay: options.retryDelay || 1000,
      maxQueueSize: options.maxQueueSize || 100,
      batchSize: options.batchSize || 10,
      flushInterval: options.flushInterval || 5000,
      ...options
    };
    
    this.queue = [];
    this.isOnline = navigator.onLine;
    this.setupNetworkListeners();
    this.startBatchProcessor();
  }
  
  // 发送事件
  send(envelope) {
    return new Promise((resolve, reject) => {
      const event = {
        envelope,
        timestamp: Date.now(),
        retries: 0,
        resolve,
        reject
      };
      
      this.addToQueue(event);
    });
  }
  
  addToQueue(event) {
    // 检查队列大小
    if (this.queue.length >= this.options.maxQueueSize) {
      // 移除最旧的事件
      const oldEvent = this.queue.shift();
      oldEvent.reject(new Error('Queue overflow'));
    }
    
    this.queue.push(event);
    
    // 如果在线且队列达到批处理大小，立即处理
    if (this.isOnline && this.queue.length >= this.options.batchSize) {
      this.processBatch();
    }
  }
  
  startBatchProcessor() {
    setInterval(() => {
      if (this.isOnline && this.queue.length > 0) {
        this.processBatch();
      }
    }, this.options.flushInterval);
  }
  
  async processBatch() {
    if (this.queue.length === 0) return;
    
    const batch = this.queue.splice(0, this.options.batchSize);
    
    try {
      await this.sendBatch(batch);
      
      // 成功发送，解析所有Promise
      batch.forEach(event => event.resolve());
    } catch (error) {
      // 处理失败的事件
      this.handleFailedBatch(batch, error);
    }
  }
  
  async sendBatch(batch) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.options.timeout);
    
    try {
      const response = await fetch(this.options.url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-sentry-envelope',
          ...this.options.headers
        },
        body: this.serializeBatch(batch),
        signal: controller.signal
      });
      
      clearTimeout(timeoutId);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  }
  
  serializeBatch(batch) {
    // 将批次序列化为Sentry envelope格式
    return batch.map(event => this.serializeEnvelope(event.envelope)).join('\n');
  }
  
  serializeEnvelope(envelope) {
    // 简化的envelope序列化
    return JSON.stringify(envelope);
  }
  
  handleFailedBatch(batch, error) {
    batch.forEach(event => {
      if (event.retries < this.options.retries) {
        // 重试
        event.retries++;
        
        setTimeout(() => {
          this.addToQueue(event);
        }, this.options.retryDelay * Math.pow(2, event.retries)); // 指数退避
      } else {
        // 达到最大重试次数，拒绝Promise
        event.reject(error);
      }
    });
  }
  
  setupNetworkListeners() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      console.log('Network online, processing queued events');
      this.processBatch();
    });
    
    window.addEventListener('offline', () => {
      this.isOnline = false;
      console.log('Network offline, queuing events');
    });
  }
  
  // 获取队列状态
  getQueueStatus() {
    return {
      size: this.queue.length,
      isOnline: this.isOnline,
      oldestEvent: this.queue.length > 0 ? this.queue[0].timestamp : null
    };
  }
  
  // 清空队列
  clearQueue() {
    const clearedEvents = this.queue.splice(0);
    clearedEvents.forEach(event => {
      event.reject(new Error('Queue cleared'));
    });
    return clearedEvents.length;
  }
}

// 数据压缩传输
export class CompressedSentryTransport extends CustomSentryTransport {
  constructor(options = {}) {
    super(options);
    this.compressionEnabled = options.compression !== false;
  }
  
  async sendBatch(batch) {
    let body = this.serializeBatch(batch);
    let headers = { ...this.options.headers };
    
    // 如果启用压缩且数据足够大
    if (this.compressionEnabled && body.length > 1024) {
      try {
        body = await this.compressData(body);
        headers['Content-Encoding'] = 'gzip';
      } catch (error) {
        console.warn('Failed to compress data:', error);
      }
    }
    
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.options.timeout);
    
    try {
      const response = await fetch(this.options.url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-sentry-envelope',
          ...headers
        },
        body,
        signal: controller.signal
      });
      
      clearTimeout(timeoutId);
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  }
  
  async compressData(data) {
    // 使用CompressionStream API（如果可用）
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
    
    // 降级到原始数据
    return data;
  }
}

// 创建自定义传输实例
export const customTransport = new CompressedSentryTransport({
  url: process.env.REACT_APP_SENTRY_DSN,
  compression: true,
  batchSize: 5,
  flushInterval: 3000,
  maxQueueSize: 50
});
```

### 1.3 高级过滤和数据清理

```javascript
// sentryFilters.js
import * as Sentry from '@sentry/browser';

// 高级过滤管理器
export class SentryFilterManager {
  constructor() {
    this.errorFilters = new Map();
    this.performanceFilters = new Map();
    this.breadcrumbFilters = new Map();
    this.setupDefaultFilters();
  }
  
  setupDefaultFilters() {
    // 错误过滤器
    this.addErrorFilter('ignore_known_errors', (event) => {
      const knownErrors = [
        'ResizeObserver loop limit exceeded',
        'Script error.',
        'Non-Error promise rejection captured',
        'Network request failed',
        'Loading chunk',
        'ChunkLoadError'
      ];
      
      if (event.exception) {
        const errorMessage = event.exception.values[0]?.value || '';
        return !knownErrors.some(known => errorMessage.includes(known));
      }
      
      return true;
    });
    
    this.addErrorFilter('filter_by_url', (event) => {
      const ignoredUrls = [
        /chrome-extension:\/\//,
        /moz-extension:\/\//,
        /safari-extension:\/\//,
        /localhost:3000\/_next\/static/
      ];
      
      if (event.request?.url) {
        return !ignoredUrls.some(pattern => pattern.test(event.request.url));
      }
      
      return true;
    });
    
    this.addErrorFilter('filter_by_user_agent', (event) => {
      const ignoredUserAgents = [
        /bot/i,
        /crawler/i,
        /spider/i,
        /headless/i
      ];
      
      const userAgent = event.request?.headers?.['User-Agent'] || navigator.userAgent;
      return !ignoredUserAgents.some(pattern => pattern.test(userAgent));
    });
    
    // 性能过滤器
    this.addPerformanceFilter('filter_short_transactions', (event) => {
      if (event.type === 'transaction') {
        const duration = event.timestamp - event.start_timestamp;
        return duration > 100; // 忽略小于100ms的事务
      }
      return true;
    });
    
    this.addPerformanceFilter('filter_navigation_timing', (event) => {
      if (event.type === 'transaction' && event.transaction === 'pageload') {
        // 过滤异常的导航时间
        const spans = event.spans || [];
        const navigationSpan = spans.find(span => span.op === 'navigation');
        
        if (navigationSpan) {
          const duration = navigationSpan.timestamp - navigationSpan.start_timestamp;
          return duration < 60000; // 忽略超过60秒的页面加载
        }
      }
      return true;
    });
    
    // 面包屑过滤器
    this.addBreadcrumbFilter('filter_console_logs', (breadcrumb) => {
      if (breadcrumb.category === 'console') {
        // 只保留error和warn级别的控制台日志
        return ['error', 'warn'].includes(breadcrumb.level);
      }
      return true;
    });
    
    this.addBreadcrumbFilter('filter_ui_clicks', (breadcrumb) => {
      if (breadcrumb.category === 'ui.click') {
        // 过滤掉某些UI元素的点击
        const ignoredSelectors = [
          'button[data-testid="close-modal"]',
          '.advertisement',
          '.tracking-pixel'
        ];
        
        const target = breadcrumb.data?.target;
        return !ignoredSelectors.some(selector => target?.includes(selector));
      }
      return true;
    });
  }
  
  addErrorFilter(name, filterFn) {
    this.errorFilters.set(name, filterFn);
  }
  
  addPerformanceFilter(name, filterFn) {
    this.performanceFilters.set(name, filterFn);
  }
  
  addBreadcrumbFilter(name, filterFn) {
    this.breadcrumbFilters.set(name, filterFn);
  }
  
  // 应用所有过滤器
  applyFilters() {
    // 设置beforeSend过滤器
    Sentry.getCurrentHub().getClient().getOptions().beforeSend = (event, hint) => {
      return this.processEvent(event, hint);
    };
    
    // 设置beforeBreadcrumb过滤器
    Sentry.getCurrentHub().getClient().getOptions().beforeBreadcrumb = (breadcrumb, hint) => {
      return this.processBreadcrumb(breadcrumb, hint);
    };
  }
  
  processEvent(event, hint) {
    // 应用错误过滤器
    for (const [name, filter] of this.errorFilters) {
      try {
        if (!filter(event, hint)) {
          console.debug(`Event filtered by: ${name}`);
          return null;
        }
      } catch (error) {
        console.warn(`Error in filter ${name}:`, error);
      }
    }
    
    // 应用性能过滤器
    for (const [name, filter] of this.performanceFilters) {
      try {
        if (!filter(event, hint)) {
          console.debug(`Performance event filtered by: ${name}`);
          return null;
        }
      } catch (error) {
        console.warn(`Error in performance filter ${name}:`, error);
      }
    }
    
    // 数据清理
    return this.sanitizeEvent(event);
  }
  
  processBreadcrumb(breadcrumb, hint) {
    // 应用面包屑过滤器
    for (const [name, filter] of this.breadcrumbFilters) {
      try {
        if (!filter(breadcrumb, hint)) {
          console.debug(`Breadcrumb filtered by: ${name}`);
          return null;
        }
      } catch (error) {
        console.warn(`Error in breadcrumb filter ${name}:`, error);
      }
    }
    
    return this.sanitizeBreadcrumb(breadcrumb);
  }
  
  sanitizeEvent(event) {
    // 深度清理事件数据
    const sanitized = { ...event };
    
    // 清理异常堆栈中的敏感信息
    if (sanitized.exception) {
      sanitized.exception.values = sanitized.exception.values.map(exception => {
        if (exception.stacktrace?.frames) {
          exception.stacktrace.frames = exception.stacktrace.frames.map(frame => {
            // 清理文件路径中的敏感信息
            if (frame.filename) {
              frame.filename = this.sanitizeFilename(frame.filename);
            }
            
            // 清理变量中的敏感信息
            if (frame.vars) {
              frame.vars = this.sanitizeVariables(frame.vars);
            }
            
            return frame;
          });
        }
        return exception;
      });
    }
    
    // 清理请求数据
    if (sanitized.request) {
      sanitized.request = this.sanitizeRequest(sanitized.request);
    }
    
    // 清理额外数据
    if (sanitized.extra) {
      sanitized.extra = this.sanitizeObject(sanitized.extra);
    }
    
    // 限制事件大小
    const eventSize = JSON.stringify(sanitized).length;
    if (eventSize > 100000) {
      sanitized.extra = {
        _truncated: true,
        _originalSize: eventSize,
        _reason: 'Event size exceeded limit'
      };
    }
    
    return sanitized;
  }
  
  sanitizeBreadcrumb(breadcrumb) {
    const sanitized = { ...breadcrumb };
    
    // 清理面包屑数据
    if (sanitized.data) {
      sanitized.data = this.sanitizeObject(sanitized.data);
    }
    
    // 限制消息长度
    if (sanitized.message && sanitized.message.length > 1000) {
      sanitized.message = sanitized.message.substring(0, 1000) + '...';
    }
    
    return sanitized;
  }
  
  sanitizeFilename(filename) {
    // 移除绝对路径，只保留相对路径
    return filename.replace(/^.*[\/\\]/, '');
  }
  
  sanitizeVariables(vars) {
    const sensitiveKeys = ['password', 'token', 'secret', 'key', 'auth'];
    const sanitized = {};
    
    for (const [key, value] of Object.entries(vars)) {
      if (sensitiveKeys.some(sensitive => key.toLowerCase().includes(sensitive))) {
        sanitized[key] = '[Filtered]';
      } else if (typeof value === 'string' && value.length > 1000) {
        sanitized[key] = value.substring(0, 1000) + '...';
      } else {
        sanitized[key] = value;
      }
    }
    
    return sanitized;
  }
  
  sanitizeRequest(request) {
    const sanitized = { ...request };
    
    // 清理查询参数
    if (sanitized.query_string) {
      const params = new URLSearchParams(sanitized.query_string);
      const cleanParams = new URLSearchParams();
      
      for (const [key, value] of params) {
        if (key.toLowerCase().includes('token') || key.toLowerCase().includes('key')) {
          cleanParams.set(key, '[Filtered]');
        } else {
          cleanParams.set(key, value);
        }
      }
      
      sanitized.query_string = cleanParams.toString();
    }
    
    // 清理请求头
    if (sanitized.headers) {
      const sensitiveHeaders = ['authorization', 'cookie', 'x-api-key'];
      for (const header of sensitiveHeaders) {
        if (sanitized.headers[header]) {
          sanitized.headers[header] = '[Filtered]';
        }
      }
    }
    
    return sanitized;
  }
  
  sanitizeObject(obj, depth = 0) {
    if (depth > 5 || !obj || typeof obj !== 'object') {
      return obj;
    }
    
    const sanitized = {};
    
    for (const [key, value] of Object.entries(obj)) {
      if (typeof value === 'object' && value !== null) {
        sanitized[key] = this.sanitizeObject(value, depth + 1);
      } else if (typeof value === 'string' && value.length > 10000) {
        sanitized[key] = value.substring(0, 10000) + '...';
      } else {
        sanitized[key] = value;
      }
    }
    
    return sanitized;
  }
  
  // 获取过滤器统计
  getFilterStats() {
    return {
      errorFilters: this.errorFilters.size,
      performanceFilters: this.performanceFilters.size,
      breadcrumbFilters: this.breadcrumbFilters.size
    };
  }
  
  // 移除过滤器
  removeFilter(type, name) {
    switch (type) {
      case 'error':
        return this.errorFilters.delete(name);
      case 'performance':
        return this.performanceFilters.delete(name);
      case 'breadcrumb':
        return this.breadcrumbFilters.delete(name);
      default:
        return false;
    }
  }
}

// 创建过滤管理器实例
export const filterManager = new SentryFilterManager();
filterManager.applyFilters();
```

## 二、自定义集成开发

### 2.1 自定义集成基础框架

```javascript
// customIntegrations.js
import * as Sentry from '@sentry/browser';

// 自定义集成基类
export class BaseSentryIntegration {
  constructor(name, options = {}) {
    this.name = name;
    this.options = options;
    this.isSetup = false;
  }
  
  // 集成设置方法
  setupOnce(addGlobalEventProcessor, getCurrentHub) {
    if (this.isSetup) {
      console.warn(`Integration ${this.name} is already setup`);
      return;
    }
    
    this.hub = getCurrentHub;
    this.addGlobalEventProcessor = addGlobalEventProcessor;
    
    // 添加全局事件处理器
    addGlobalEventProcessor((event, hint) => {
      return this.processEvent(event, hint);
    });
    
    // 执行具体的设置逻辑
    this.setup();
    this.isSetup = true;
    
    console.log(`Integration ${this.name} setup completed`);
  }
  
  // 子类需要实现的设置方法
  setup() {
    throw new Error('setup method must be implemented by subclass');
  }
  
  // 事件处理方法
  processEvent(event, hint) {
    return event;
  }
  
  // 获取当前Hub
  getCurrentHub() {
    return this.hub();
  }
  
  // 捕获异常
  captureException(exception, context = {}) {
    const hub = this.getCurrentHub();
    hub.withScope(scope => {
      scope.setTag('integration', this.name);
      Object.keys(context).forEach(key => {
        scope.setContext(key, context[key]);
      });
      hub.captureException(exception);
    });
  }
  
  // 捕获消息
  captureMessage(message, level = 'info', context = {}) {
    const hub = this.getCurrentHub();
    hub.withScope(scope => {
      scope.setTag('integration', this.name);
      Object.keys(context).forEach(key => {
        scope.setContext(key, context[key]);
      });
      hub.captureMessage(message, level);
    });
  }
  
  // 添加面包屑
  addBreadcrumb(breadcrumb) {
    const hub = this.getCurrentHub();
    hub.addBreadcrumb({
      ...breadcrumb,
      data: {
        ...breadcrumb.data,
        integration: this.name
      }
    });
  }
}

// 用户行为追踪集成
export class UserBehaviorIntegration extends BaseSentryIntegration {
  constructor(options = {}) {
    super('UserBehavior', {
      trackClicks: true,
      trackScrolls: true,
      trackFormSubmissions: true,
      trackPageVisibility: true,
      debounceTime: 100,
      maxBreadcrumbs: 50,
      ...options
    });
    
    this.behaviorQueue = [];
    this.lastScrollTime = 0;
    this.isVisible = true;
  }
  
  setup() {
    if (this.options.trackClicks) {
      this.setupClickTracking();
    }
    
    if (this.options.trackScrolls) {
      this.setupScrollTracking();
    }
    
    if (this.options.trackFormSubmissions) {
      this.setupFormTracking();
    }
    
    if (this.options.trackPageVisibility) {
      this.setupVisibilityTracking();
    }
    
    // 定期处理行为队列
    this.startBehaviorProcessor();
  }
  
  setupClickTracking() {
    document.addEventListener('click', (event) => {
      const target = event.target;
      const tagName = target.tagName.toLowerCase();
      const className = target.className;
      const id = target.id;
      const text = target.textContent?.substring(0, 100) || '';
      
      this.addBehaviorEvent({
        type: 'click',
        target: {
          tagName,
          className,
          id,
          text,
          selector: this.getElementSelector(target)
        },
        position: {
          x: event.clientX,
          y: event.clientY
        },
        timestamp: Date.now()
      });
    }, { passive: true });
  }
  
  setupScrollTracking() {
    const throttledScrollHandler = this.throttle(() => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      const scrollHeight = document.documentElement.scrollHeight;
      const clientHeight = document.documentElement.clientHeight;
      const scrollPercentage = Math.round((scrollTop / (scrollHeight - clientHeight)) * 100);
      
      this.addBehaviorEvent({
        type: 'scroll',
        scrollTop,
        scrollPercentage,
        timestamp: Date.now()
      });
    }, this.options.debounceTime);
    
    window.addEventListener('scroll', throttledScrollHandler, { passive: true });
  }
  
  setupFormTracking() {
    document.addEventListener('submit', (event) => {
      const form = event.target;
      const formData = new FormData(form);
      const fields = {};
      
      // 收集表单字段（不包含敏感信息）
      for (const [key, value] of formData.entries()) {
        if (!this.isSensitiveField(key)) {
          fields[key] = typeof value === 'string' ? value.substring(0, 100) : '[File]';
        }
      }
      
      this.addBehaviorEvent({
        type: 'form_submit',
        formId: form.id,
        formAction: form.action,
        fieldCount: Object.keys(fields).length,
        fields,
        timestamp: Date.now()
      });
    });
  }
  
  setupVisibilityTracking() {
    document.addEventListener('visibilitychange', () => {
      this.isVisible = !document.hidden;
      
      this.addBehaviorEvent({
        type: 'visibility_change',
        visible: this.isVisible,
        timestamp: Date.now()
      });
    });
    
    // 页面焦点变化
    window.addEventListener('focus', () => {
      this.addBehaviorEvent({
        type: 'page_focus',
        timestamp: Date.now()
      });
    });
    
    window.addEventListener('blur', () => {
      this.addBehaviorEvent({
        type: 'page_blur',
        timestamp: Date.now()
      });
    });
  }
  
  addBehaviorEvent(event) {
    this.behaviorQueue.push(event);
    
    // 限制队列大小
    if (this.behaviorQueue.length > this.options.maxBreadcrumbs) {
      this.behaviorQueue.shift();
    }
  }
  
  startBehaviorProcessor() {
    setInterval(() => {
      this.processBehaviorQueue();
    }, 5000); // 每5秒处理一次
  }
  
  processBehaviorQueue() {
    if (this.behaviorQueue.length === 0) return;
    
    const events = this.behaviorQueue.splice(0);
    const summary = this.summarizeBehavior(events);
    
    this.addBreadcrumb({
      category: 'user-behavior',
      message: `User behavior summary: ${summary.totalEvents} events`,
      level: 'info',
      data: summary
    });
  }
  
  summarizeBehavior(events) {
    const summary = {
      totalEvents: events.length,
      timeRange: {
        start: events[0]?.timestamp,
        end: events[events.length - 1]?.timestamp
      },
      eventTypes: {},
      clickTargets: [],
      scrollActivity: {
        maxScroll: 0,
        scrollEvents: 0
      },
      formActivity: {
        submissions: 0,
        forms: new Set()
      }
    };
    
    events.forEach(event => {
      // 统计事件类型
      summary.eventTypes[event.type] = (summary.eventTypes[event.type] || 0) + 1;
      
      // 处理点击事件
      if (event.type === 'click') {
        summary.clickTargets.push({
          selector: event.target.selector,
          text: event.target.text
        });
      }
      
      // 处理滚动事件
      if (event.type === 'scroll') {
        summary.scrollActivity.scrollEvents++;
        summary.scrollActivity.maxScroll = Math.max(
          summary.scrollActivity.maxScroll,
          event.scrollPercentage
        );
      }
      
      // 处理表单事件
      if (event.type === 'form_submit') {
        summary.formActivity.submissions++;
        summary.formActivity.forms.add(event.formId);
      }
    });
    
    // 转换Set为数组
    summary.formActivity.forms = Array.from(summary.formActivity.forms);
    
    return summary;
  }
  
  getElementSelector(element) {
    if (element.id) {
      return `#${element.id}`;
    }
    
    if (element.className) {
      const classes = element.className.split(' ').filter(c => c).slice(0, 2);
      if (classes.length > 0) {
        return `${element.tagName.toLowerCase()}.${classes.join('.')}`;
      }
    }
    
    return element.tagName.toLowerCase();
  }
  
  isSensitiveField(fieldName) {
    const sensitiveFields = ['password', 'token', 'secret', 'ssn', 'credit'];
    return sensitiveFields.some(field => fieldName.toLowerCase().includes(field));
  }
  
  throttle(func, delay) {
    let timeoutId;
    let lastExecTime = 0;
    
    return function (...args) {
      const currentTime = Date.now();
      
      if (currentTime - lastExecTime > delay) {
        func.apply(this, args);
        lastExecTime = currentTime;
      } else {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
          func.apply(this, args);
          lastExecTime = Date.now();
        }, delay - (currentTime - lastExecTime));
      }
    };
  }
}

// 性能监控集成
export class AdvancedPerformanceIntegration extends BaseSentryIntegration {
  constructor(options = {}) {
    super('AdvancedPerformance', {
      trackLongTasks: true,
      trackMemoryUsage: true,
      trackResourceTiming: true,
      trackUserTiming: true,
      longTaskThreshold: 50,
      memoryCheckInterval: 30000,
      ...options
    });
    
    this.performanceObserver = null;
    this.memoryCheckInterval = null;
  }
  
  setup() {
    if (this.options.trackLongTasks) {
      this.setupLongTaskTracking();
    }
    
    if (this.options.trackMemoryUsage) {
      this.setupMemoryTracking();
    }
    
    if (this.options.trackResourceTiming) {
      this.setupResourceTimingTracking();
    }
    
    if (this.options.trackUserTiming) {
      this.setupUserTimingTracking();
    }
  }
  
  setupLongTaskTracking() {
    if ('PerformanceObserver' in window) {
      try {
        const observer = new PerformanceObserver((list) => {
          list.getEntries().forEach((entry) => {
            if (entry.duration > this.options.longTaskThreshold) {
              this.addBreadcrumb({
                category: 'performance',
                message: `Long task detected: ${entry.duration.toFixed(2)}ms`,
                level: 'warning',
                data: {
                  duration: entry.duration,
                  startTime: entry.startTime,
                  name: entry.name
                }
              });
            }
          });
        });
        
        observer.observe({ entryTypes: ['longtask'] });
        this.performanceObserver = observer;
      } catch (error) {
        console.warn('Long task tracking not supported:', error);
      }
    }
  }
  
  setupMemoryTracking() {
    if ('memory' in performance) {
      this.memoryCheckInterval = setInterval(() => {
        const memory = performance.memory;
        const memoryUsage = {
          used: memory.usedJSHeapSize,
          total: memory.totalJSHeapSize,
          limit: memory.jsHeapSizeLimit,
          usagePercentage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
        };
        
        // 如果内存使用率超过80%，记录警告
        if (memoryUsage.usagePercentage > 80) {
          this.addBreadcrumb({
            category: 'performance',
            message: `High memory usage: ${memoryUsage.usagePercentage.toFixed(2)}%`,
            level: 'warning',
            data: memoryUsage
          });
        }
      }, this.options.memoryCheckInterval);
    }
  }
  
  setupResourceTimingTracking() {
    if ('PerformanceObserver' in window) {
      try {
        const observer = new PerformanceObserver((list) => {
          list.getEntries().forEach((entry) => {
            // 只关注较慢的资源加载
            if (entry.duration > 1000) {
              this.addBreadcrumb({
                category: 'performance',
                message: `Slow resource load: ${entry.name}`,
                level: 'info',
                data: {
                  name: entry.name,
                  duration: entry.duration,
                  size: entry.transferSize,
                  type: entry.initiatorType
                }
              });
            }
          });
        });
        
        observer.observe({ entryTypes: ['resource'] });
      } catch (error) {
        console.warn('Resource timing tracking not supported:', error);
      }
    }
  }
  
  setupUserTimingTracking() {
    if ('PerformanceObserver' in window) {
      try {
        const observer = new PerformanceObserver((list) => {
          list.getEntries().forEach((entry) => {
            this.addBreadcrumb({
              category: 'performance',
              message: `User timing: ${entry.name}`,
              level: 'info',
              data: {
                name: entry.name,
                duration: entry.duration || 0,
                startTime: entry.startTime,
                entryType: entry.entryType
              }
            });
          });
        });
        
        observer.observe({ entryTypes: ['measure', 'mark'] });
      } catch (error) {
        console.warn('User timing tracking not supported:', error);
      }
    }
  }
  
  // 清理资源
  cleanup() {
    if (this.performanceObserver) {
      this.performanceObserver.disconnect();
    }
    
    if (this.memoryCheckInterval) {
      clearInterval(this.memoryCheckInterval);
    }
  }
}

// 创建集成实例
export const userBehaviorIntegration = new UserBehaviorIntegration({
  trackClicks: true,
  trackScrolls: true,
  debounceTime: 200
});

export const advancedPerformanceIntegration = new AdvancedPerformanceIntegration({
  trackLongTasks: true,
  trackMemoryUsage: true,
  longTaskThreshold: 100
});
```

### 2.2 业务特定集成

```javascript
// businessIntegrations.js
import { BaseSentryIntegration } from './customIntegrations';

// 电商业务集成
export class ECommerceIntegration extends BaseSentryIntegration {
  constructor(options = {}) {
    super('ECommerce', {
      trackPurchases: true,
      trackCartActions: true,
      trackProductViews: true,
      trackSearches: true,
      ...options
    });
    
    this.cartState = {
      items: [],
      total: 0,
      currency: 'USD'
    };
  }
  
  setup() {
    // 监听购买事件
    if (this.options.trackPurchases) {
      this.setupPurchaseTracking();
    }
    
    // 监听购物车操作
    if (this.options.trackCartActions) {
      this.setupCartTracking();
    }
    
    // 监听产品浏览
    if (this.options.trackProductViews) {
      this.setupProductViewTracking();
    }
    
    // 监听搜索行为
    if (this.options.trackSearches) {
      this.setupSearchTracking();
    }
  }
  
  setupPurchaseTracking() {
    // 监听购买完成事件
    window.addEventListener('purchase-completed', (event) => {
      const purchaseData = event.detail;
      
      this.addBreadcrumb({
        category: 'ecommerce',
        message: `Purchase completed: ${purchaseData.orderId}`,
        level: 'info',
        data: {
          orderId: purchaseData.orderId,
          total: purchaseData.total,
          currency: purchaseData.currency,
          itemCount: purchaseData.items.length,
          paymentMethod: purchaseData.paymentMethod
        }
      });
      
      // 设置用户上下文
      const hub = this.getCurrentHub();
      hub.configureScope(scope => {
        scope.setContext('lastPurchase', {
          orderId: purchaseData.orderId,
          total: purchaseData.total,
          timestamp: new Date().toISOString()
        });
        
        scope.setTag('customer_type', this.getCustomerType(purchaseData));
      });
    });
  }
  
  setupCartTracking() {
    // 监听添加到购物车
    window.addEventListener('add-to-cart', (event) => {
      const { product, quantity } = event.detail;
      
      this.cartState.items.push({ product, quantity });
      this.updateCartTotal();
      
      this.addBreadcrumb({
        category: 'ecommerce',
        message: `Added to cart: ${product.name}`,
        level: 'info',
        data: {
          productId: product.id,
          productName: product.name,
          price: product.price,
          quantity,
          cartTotal: this.cartState.total
        }
      });
    });
    
    // 监听从购物车移除
    window.addEventListener('remove-from-cart', (event) => {
      const { productId } = event.detail;
      
      this.cartState.items = this.cartState.items.filter(item => item.product.id !== productId);
      this.updateCartTotal();
      
      this.addBreadcrumb({
        category: 'ecommerce',
        message: `Removed from cart: ${productId}`,
        level: 'info',
        data: {
          productId,
          cartTotal: this.cartState.total
        }
      });
    });
  }
  
  setupProductViewTracking() {
    // 监听产品页面浏览
    window.addEventListener('product-view', (event) => {
      const product = event.detail;
      
      this.addBreadcrumb({
        category: 'ecommerce',
        message: `Viewed product: ${product.name}`,
        level: 'info',
        data: {
          productId: product.id,
          productName: product.name,
          category: product.category,
          price: product.price,
          inStock: product.inStock
        }
      });
      
      // 更新用户上下文
      const hub = this.getCurrentHub();
      hub.configureScope(scope => {
        scope.setContext('lastViewedProduct', {
          id: product.id,
          name: product.name,
          category: product.category,
          timestamp: new Date().toISOString()
        });
      });
    });
  }
  
  setupSearchTracking() {
    // 监听搜索事件
    window.addEventListener('product-search', (event) => {
      const { query, results, filters } = event.detail;
      
      this.addBreadcrumb({
        category: 'ecommerce',
        message: `Product search: "${query}"`,
        level: 'info',
        data: {
          query,
          resultCount: results.length,
          filters,
          hasResults: results.length > 0
        }
      });
      
      // 如果搜索无结果，记录为潜在问题
      if (results.length === 0) {
        this.captureMessage(
          `No search results for query: "${query}"`,
          'warning',
          {
            searchQuery: {
              query,
              filters,
              timestamp: new Date().toISOString()
            }
          }
        );
      }
    });
  }
  
  updateCartTotal() {
    this.cartState.total = this.cartState.items.reduce((total, item) => {
      return total + (item.product.price * item.quantity);
    }, 0);
  }
  
  getCustomerType(purchaseData) {
    if (purchaseData.total > 1000) {
      return 'high_value';
    } else if (purchaseData.total > 100) {
      return 'medium_value';
    } else {
      return 'low_value';
    }
  }
  
  // 公共API方法
  trackCustomEvent(eventName, data) {
    this.addBreadcrumb({
      category: 'ecommerce',
      message: `Custom event: ${eventName}`,
      level: 'info',
      data
    });
  }
  
  setCustomerContext(customer) {
    const hub = this.getCurrentHub();
    hub.configureScope(scope => {
      scope.setUser({
        id: customer.id,
        email: customer.email,
        username: customer.username
      });
      
      scope.setContext('customer', {
        tier: customer.tier,
        registrationDate: customer.registrationDate,
        totalOrders: customer.totalOrders,
        totalSpent: customer.totalSpent
      });
      
      scope.setTag('customer_tier', customer.tier);
    });
  }
}

// API监控集成
export class APIMonitoringIntegration extends BaseSentryIntegration {
  constructor(options = {}) {
    super('APIMonitoring', {
      trackAllRequests: true,
      trackSlowRequests: true,
      trackFailedRequests: true,
      slowRequestThreshold: 2000,
      excludeUrls: [],
      ...options
    });
    
    this.requestMetrics = new Map();
    this.originalFetch = null;
    this.originalXHR = null;
  }
  
  setup() {
    this.setupFetchInterception();
    this.setupXHRInterception();
  }
  
  setupFetchInterception() {
    if (!window.fetch) return;
    
    this.originalFetch = window.fetch;
    
    window.fetch = async (...args) => {
      const [url, options = {}] = args;
      
      if (this.shouldIgnoreUrl(url)) {
        return this.originalFetch.apply(window, args);
      }
      
      const requestId = this.generateRequestId();
      const startTime = Date.now();
      
      const requestData = {
        id: requestId,
        url: typeof url === 'string' ? url : url.url,
        method: options.method || 'GET',
        startTime,
        headers: options.headers || {}
      };
      
      this.trackRequestStart(requestData);
      
      try {
        const response = await this.originalFetch.apply(window, args);
        const endTime = Date.now();
        const duration = endTime - startTime;
        
        this.trackRequestEnd(requestId, {
          status: response.status,
          statusText: response.statusText,
          duration,
          success: response.ok
        });
        
        return response;
      } catch (error) {
        const endTime = Date.now();
        const duration = endTime - startTime;
        
        this.trackRequestEnd(requestId, {
          error: error.message,
          duration,
          success: false
        });
        
        throw error;
      }
    };
  }
  
  setupXHRInterception() {
    if (!window.XMLHttpRequest) return;
    
    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url, ...args) {
      this._sentryRequestData = {
        id: this.generateRequestId?.() || Math.random().toString(36),
        method,
        url,
        startTime: null
      };
      
      return originalOpen.apply(this, [method, url, ...args]);
    };
    
    XMLHttpRequest.prototype.send = function(...args) {
      if (this._sentryRequestData) {
        this._sentryRequestData.startTime = Date.now();
        
        const requestData = { ...this._sentryRequestData };
        this.trackRequestStart?.(requestData);
        
        const originalOnReadyStateChange = this.onreadystatechange;
        
        this.onreadystatechange = function() {
          if (this.readyState === 4) {
            const endTime = Date.now();
            const duration = endTime - requestData.startTime;
            
            this.trackRequestEnd?.(requestData.id, {
              status: this.status,
              statusText: this.statusText,
              duration,
              success: this.status >= 200 && this.status < 300
            });
          }
          
          if (originalOnReadyStateChange) {
            originalOnReadyStateChange.apply(this, arguments);
          }
        };
      }
      
      return originalSend.apply(this, args);
    };
  }
  
  trackRequestStart(requestData) {
    this.requestMetrics.set(requestData.id, requestData);
    
    if (this.options.trackAllRequests) {
      this.addBreadcrumb({
        category: 'http',
        message: `${requestData.method} ${requestData.url}`,
        level: 'info',
        data: {
          method: requestData.method,
          url: requestData.url,
          requestId: requestData.id
        }
      });
    }
  }
  
  trackRequestEnd(requestId, responseData) {
    const requestData = this.requestMetrics.get(requestId);
    if (!requestData) return;
    
    const fullData = { ...requestData, ...responseData };
    
    // 记录慢请求
    if (this.options.trackSlowRequests && responseData.duration > this.options.slowRequestThreshold) {
      this.addBreadcrumb({
        category: 'http',
        message: `Slow request: ${requestData.method} ${requestData.url}`,
        level: 'warning',
        data: {
          ...fullData,
          slow: true
        }
      });
    }
    
    // 记录失败请求
    if (this.options.trackFailedRequests && !responseData.success) {
      this.addBreadcrumb({
        category: 'http',
        message: `Failed request: ${requestData.method} ${requestData.url}`,
        level: 'error',
        data: fullData
      });
      
      // 对于严重的API错误，发送异常
      if (responseData.status >= 500) {
        this.captureMessage(
          `API Server Error: ${responseData.status} ${responseData.statusText}`,
          'error',
          {
            apiRequest: fullData
          }
        );
      }
    }
    
    this.requestMetrics.delete(requestId);
  }
  
  shouldIgnoreUrl(url) {
    const urlString = typeof url === 'string' ? url : url.url;
    return this.options.excludeUrls.some(pattern => {
      if (typeof pattern === 'string') {
        return urlString.includes(pattern);
      }
      if (pattern instanceof RegExp) {
        return pattern.test(urlString);
      }
      return false;
    });
  }
  
  generateRequestId() {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  // 获取API性能统计
  getAPIStats() {
    const stats = {
      totalRequests: 0,
      successfulRequests: 0,
      failedRequests: 0,
      averageResponseTime: 0,
      slowRequests: 0
    };
    
    // 这里可以从存储的指标中计算统计信息
    return stats;
  }
}

// 创建业务集成实例
export const ecommerceIntegration = new ECommerceIntegration({
  trackPurchases: true,
  trackCartActions: true,
  trackProductViews: true
});

export const apiMonitoringIntegration = new APIMonitoringIntegration({
  trackSlowRequests: true,
  slowRequestThreshold: 1500,
  excludeUrls: ['/health', '/metrics']
});
```

## 三、企业级监控体系构建

### 3.1 监控策略设计

```javascript
// monitoringStrategy.js
import * as Sentry from '@sentry/browser';

// 监控策略管理器
export class MonitoringStrategyManager {
  constructor() {
    this.strategies = new Map();
    this.activeStrategy = null;
    this.setupDefaultStrategies();
  }
  
  setupDefaultStrategies() {
    // 开发环境策略
    this.addStrategy('development', {
      errorSampling: 1.0,
      performanceSampling: 1.0,
      breadcrumbLimit: 100,
      logLevel: 'debug',
      enableDebugMode: true,
      captureConsole: true,
      filters: {
        ignoreErrors: [],
        ignoreUrls: []
      }
    });
    
    // 测试环境策略
    this.addStrategy('testing', {
      errorSampling: 1.0,
      performanceSampling: 0.5,
      breadcrumbLimit: 50,
      logLevel: 'info',
      enableDebugMode: false,
      captureConsole: false,
      filters: {
        ignoreErrors: ['ResizeObserver loop limit exceeded'],
        ignoreUrls: [/\/test\//]
      }
    });
    
    // 生产环境策略
    this.addStrategy('production', {
      errorSampling: 0.1,
      performanceSampling: 0.05,
      breadcrumbLimit: 30,
      logLevel: 'error',
      enableDebugMode: false,
      captureConsole: false,
      filters: {
        ignoreErrors: [
          'ResizeObserver loop limit exceeded',
          'Script error.',
          'Non-Error promise rejection captured'
        ],
        ignoreUrls: [
          /chrome-extension:/,
          /moz-extension:/,
          /\/analytics\//
        ]
      }
    });
  }
  
  addStrategy(name, config) {
    this.strategies.set(name, {
      name,
      ...config,
      createdAt: new Date().toISOString()
    });
  }
  
  activateStrategy(name) {
    const strategy = this.strategies.get(name);
    if (!strategy) {
      throw new Error(`Strategy '${name}' not found`);
    }
    
    this.activeStrategy = strategy;
    this.applyStrategy(strategy);
    
    console.log(`Activated monitoring strategy: ${name}`);
    return strategy;
  }
  
  applyStrategy(strategy) {
    // 更新Sentry配置
    const client = Sentry.getCurrentHub().getClient();
    if (client) {
      const options = client.getOptions();
      
      // 更新采样率
      options.sampleRate = strategy.errorSampling;
      options.tracesSampleRate = strategy.performanceSampling;
      
      // 更新面包屑限制
      options.maxBreadcrumbs = strategy.breadcrumbLimit;
      
      // 更新调试模式
      options.debug = strategy.enableDebugMode;
      
      // 应用过滤器
      this.applyFilters(strategy.filters);
    }
    
    // 配置控制台捕获
    if (strategy.captureConsole) {
      this.enableConsoleCapture();
    } else {
      this.disableConsoleCapture();
    }
  }
  
  applyFilters(filters) {
    Sentry.getCurrentHub().getClient().getOptions().beforeSend = (event, hint) => {
      // 应用错误过滤
      if (event.exception) {
        const errorMessage = event.exception.values[0]?.value || '';
        if (filters.ignoreErrors.some(pattern => {
          if (typeof pattern === 'string') {
            return errorMessage.includes(pattern);
          }
          if (pattern instanceof RegExp) {
            return pattern.test(errorMessage);
          }
          return false;
        })) {
          return null;
        }
      }
      
      // 应用URL过滤
      if (event.request?.url) {
        if (filters.ignoreUrls.some(pattern => {
          if (typeof pattern === 'string') {
            return event.request.url.includes(pattern);
          }
          if (pattern instanceof RegExp) {
            return pattern.test(event.request.url);
          }
          return false;
        })) {
          return null;
        }
      }
      
      return event;
    };
  }
  
  enableConsoleCapture() {
    const originalConsole = {
      log: console.log,
      warn: console.warn,
      error: console.error
    };
    
    console.log = (...args) => {
      Sentry.addBreadcrumb({
        category: 'console',
        message: args.join(' '),
        level: 'info'
      });
      originalConsole.log.apply(console, args);
    };
    
    console.warn = (...args) => {
      Sentry.addBreadcrumb({
        category: 'console',
        message: args.join(' '),
        level: 'warning'
      });
      originalConsole.warn.apply(console, args);
    };
    
    console.error = (...args) => {
      Sentry.addBreadcrumb({
        category: 'console',
        message: args.join(' '),
        level: 'error'
      });
      originalConsole.error.apply(console, args);
    };
  }
  
  disableConsoleCapture() {
    // 恢复原始console方法（简化实现）
    // 实际应用中需要保存原始方法的引用
  }
  
  getCurrentStrategy() {
    return this.activeStrategy;
  }
  
  getAvailableStrategies() {
    return Array.from(this.strategies.keys());
  }
  
  // 动态调整策略
  adjustStrategy(adjustments) {
    if (!this.activeStrategy) {
      throw new Error('No active strategy to adjust');
    }
    
    const updatedStrategy = {
      ...this.activeStrategy,
      ...adjustments,
      updatedAt: new Date().toISOString()
    };
    
    this.activeStrategy = updatedStrategy;
    this.applyStrategy(updatedStrategy);
    
    return updatedStrategy;
  }
}

// 创建策略管理器实例
export const strategyManager = new MonitoringStrategyManager();

// 根据环境自动激活策略
const environment = process.env.NODE_ENV || 'development';
strategyManager.activateStrategy(environment);
```

### 3.2 告警和通知系统

```javascript
// alertingSystem.js
import * as Sentry from '@sentry/browser';

// 告警系统
export class AlertingSystem {
  constructor(options = {}) {
    this.options = {
      webhookUrl: options.webhookUrl,
      slackToken: options.slackToken,
      emailEndpoint: options.emailEndpoint,
      thresholds: {
        errorRate: 0.05, // 5%错误率
        responseTime: 2000, // 2秒响应时间
        memoryUsage: 0.8, // 80%内存使用率
        ...options.thresholds
      },
      cooldownPeriod: 300000, // 5分钟冷却期
      ...options
    };
    
    this.alertHistory = new Map();
    this.metrics = {
      errors: [],
      performance: [],
      memory: []
    };
    
    this.setupMetricsCollection();
  }
  
  setupMetricsCollection() {
    // 收集错误指标
    Sentry.getCurrentHub().getClient().getOptions().beforeSend = (event, hint) => {
      this.recordError(event);
      return event;
    };
    
    // 收集性能指标
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          this.recordPerformance(entry);
        });
      });
      
      observer.observe({ entryTypes: ['navigation', 'resource'] });
    }
    
    // 定期检查指标
    setInterval(() => {
      this.checkThresholds();
    }, 60000); // 每分钟检查一次
  }
  
  recordError(event) {
    const now = Date.now();
    this.metrics.errors.push({
      timestamp: now,
      level: event.level,
      message: event.message,
      fingerprint: event.fingerprint
    });
    
    // 保留最近1小时的数据
    const oneHourAgo = now - 3600000;
    this.metrics.errors = this.metrics.errors.filter(e => e.timestamp > oneHourAgo);
  }
  
  recordPerformance(entry) {
    const now = Date.now();
    this.metrics.performance.push({
      timestamp: now,
      name: entry.name,
      duration: entry.duration,
      type: entry.entryType
    });
    
    // 保留最近1小时的数据
    const oneHourAgo = now - 3600000;
    this.metrics.performance = this.metrics.performance.filter(p => p.timestamp > oneHourAgo);
  }
  
  checkThresholds() {
    const now = Date.now();
    
    // 检查错误率
    const errorRate = this.calculateErrorRate();
    if (errorRate > this.options.thresholds.errorRate) {
      this.triggerAlert('high_error_rate', {
        currentRate: errorRate,
        threshold: this.options.thresholds.errorRate,
        timeWindow: '1 hour'
      });
    }
    
    // 检查响应时间
    const avgResponseTime = this.calculateAverageResponseTime();
    if (avgResponseTime > this.options.thresholds.responseTime) {
      this.triggerAlert('slow_response_time', {
        currentTime: avgResponseTime,
        threshold: this.options.thresholds.responseTime,
        timeWindow: '1 hour'
      });
    }
    
    // 检查内存使用率
    if ('memory' in performance) {
      const memoryUsage = performance.memory.usedJSHeapSize / performance.memory.jsHeapSizeLimit;
      if (memoryUsage > this.options.thresholds.memoryUsage) {
        this.triggerAlert('high_memory_usage', {
          currentUsage: memoryUsage,
          threshold: this.options.thresholds.memoryUsage,
          usedMemory: performance.memory.usedJSHeapSize,
          totalMemory: performance.memory.jsHeapSizeLimit
        });
      }
    }
  }
  
  calculateErrorRate() {
    const totalEvents = this.metrics.errors.length;
    if (totalEvents === 0) return 0;
    
    const errorEvents = this.metrics.errors.filter(e => e.level === 'error').length;
    return errorEvents / totalEvents;
  }
  
  calculateAverageResponseTime() {
    const navigationEntries = this.metrics.performance.filter(p => p.type === 'navigation');
    if (navigationEntries.length === 0) return 0;
    
    const totalTime = navigationEntries.reduce((sum, entry) => sum + entry.duration, 0);
    return totalTime / navigationEntries.length;
  }
  
  triggerAlert(alertType, data) {
    const alertKey = `${alertType}_${Math.floor(Date.now() / this.options.cooldownPeriod)}`;
    
    // 检查冷却期
    if (this.alertHistory.has(alertKey)) {
      return;
    }
    
    this.alertHistory.set(alertKey, {
      type: alertType,
      data,
      timestamp: Date.now()
    });
    
    // 发送告警
    this.sendAlert(alertType, data);
    
    // 清理旧的告警历史
    this.cleanupAlertHistory();
  }
  
  async sendAlert(alertType, data) {
    const alert = {
      type: alertType,
      severity: this.getAlertSeverity(alertType),
      message: this.formatAlertMessage(alertType, data),
      data,
      timestamp: new Date().toISOString(),
      environment: process.env.NODE_ENV,
      userAgent: navigator.userAgent,
      url: window.location.href
    };
    
    // 发送到多个通道
    const promises = [];
    
    if (this.options.webhookUrl) {
      promises.push(this.sendWebhookAlert(alert));
    }
    
    if (this.options.slackToken) {
      promises.push(this.sendSlackAlert(alert));
    }
    
    if (this.options.emailEndpoint) {
      promises.push(this.sendEmailAlert(alert));
    }
    
    try {
      await Promise.allSettled(promises);
      console.log(`Alert sent: ${alertType}`);
    } catch (error) {
      console.error('Failed to send alert:', error);
    }
  }
  
  getAlertSeverity(alertType) {
    const severityMap = {
      high_error_rate: 'critical',
      slow_response_time: 'warning',
      high_memory_usage: 'warning',
      api_failure: 'critical',
      user_experience_degraded: 'warning'
    };
    
    return severityMap[alertType] || 'info';
  }
  
  formatAlertMessage(alertType, data) {
    const messages = {
      high_error_rate: `High error rate detected: ${(data.currentRate * 100).toFixed(2)}% (threshold: ${(data.threshold * 100).toFixed(2)}%)`,
      slow_response_time: `Slow response time detected: ${data.currentTime.toFixed(2)}ms (threshold: ${data.threshold}ms)`,
      high_memory_usage: `High memory usage detected: ${(data.currentUsage * 100).toFixed(2)}% (threshold: ${(data.threshold * 100).toFixed(2)}%)`,
      api_failure: `API failure detected: ${data.endpoint} - ${data.error}`,
      user_experience_degraded: `User experience degraded: ${data.metric} - ${data.value}`
    };
    
    return messages[alertType] || `Alert: ${alertType}`;
  }
  
  async sendWebhookAlert(alert) {
    try {
      const response = await fetch(this.options.webhookUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(alert)
      });
      
      if (!response.ok) {
        throw new Error(`Webhook failed: ${response.status}`);
      }
    } catch (error) {
      console.error('Webhook alert failed:', error);
      throw error;
    }
  }
  
  async sendSlackAlert(alert) {
    const slackMessage = {
      text: alert.message,
      attachments: [
        {
          color: this.getSlackColor(alert.severity),
          fields: [
            {
              title: 'Environment',
              value: alert.environment,
              short: true
            },
            {
              title: 'Timestamp',
              value: alert.timestamp,
              short: true
            },
            {
              title: 'URL',
              value: alert.url,
              short: false
            }
          ]
        }
      ]
    };
    
    try {
      const response = await fetch('https://slack.com/api/chat.postMessage', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.options.slackToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(slackMessage)
      });
      
      if (!response.ok) {
        throw new Error(`Slack API failed: ${response.status}`);
      }
    } catch (error) {
      console.error('Slack alert failed:', error);
      throw error;
    }
  }
  
  async sendEmailAlert(alert) {
    try {
      const response = await fetch(this.options.emailEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          subject: `[${alert.severity.toUpperCase()}] ${alert.type}`,
          message: alert.message,
          data: alert.data,
          environment: alert.environment
        })
      });
      
      if (!response.ok) {
        throw new Error(`Email API failed: ${response.status}`);
      }
    } catch (error) {
      console.error('Email alert failed:', error);
      throw error;
    }
  }
  
  getSlackColor(severity) {
    const colors = {
      critical: 'danger',
      warning: 'warning',
      info: 'good'
    };
    
    return colors[severity] || 'good';
  }
  
  cleanupAlertHistory() {
    const cutoff = Date.now() - (this.options.cooldownPeriod * 10); // 保留10个冷却期的历史
    
    for (const [key, alert] of this.alertHistory) {
      if (alert.timestamp < cutoff) {
        this.alertHistory.delete(key);
      }
    }
  }
  
  // 手动触发告警
  manualAlert(type, message, data = {}) {
    this.triggerAlert(type, {
      message,
      manual: true,
      ...data
    });
  }
  
  // 获取告警统计
  getAlertStats() {
    const stats = {
      totalAlerts: this.alertHistory.size,
      alertsByType: {},
      recentAlerts: []
    };
    
    for (const alert of this.alertHistory.values()) {
      stats.alertsByType[alert.type] = (stats.alertsByType[alert.type] || 0) + 1;
      
      if (Date.now() - alert.timestamp < 3600000) { // 最近1小时
        stats.recentAlerts.push(alert);
      }
    }
    
    return stats;
  }
}

// 创建告警系统实例
export const alertingSystem = new AlertingSystem({
  webhookUrl: process.env.REACT_APP_ALERT_WEBHOOK,
  slackToken: process.env.REACT_APP_SLACK_TOKEN,
  emailEndpoint: process.env.REACT_APP_EMAIL_ENDPOINT,
  thresholds: {
    errorRate: 0.03,
    responseTime: 1500,
    memoryUsage: 0.85
  }
});
```

## 四、核心价值与实施建议

### 4.1 核心价值

1. **全面监控覆盖**
   - 错误监控：捕获和分析所有前端错误
   - 性能监控：实时跟踪应用性能指标
   - 用户行为监控：了解用户交互模式
   - API监控：监控后端服务调用

2. **智能化告警**
   - 基于阈值的自动告警
   - 多渠道通知支持
   - 告警去重和冷却机制
   - 上下文丰富的告警信息

3. **企业级特性**
   - 多环境配置管理
   - 数据隐私保护
   - 高可用性设计
   - 可扩展的集成架构

### 4.2 实施建议

1. **分阶段实施**
   - 第一阶段：基础错误监控
   - 第二阶段：性能监控集成
   - 第三阶段：自定义业务监控
   - 第四阶段：告警和自动化

2. **团队培训**
   - Sentry基础知识培训
   - 监控最佳实践分享
   - 告警响应流程制定
   - 定期监控数据回顾

3. **持续优化**
   - 定期评估监控效果
   - 优化过滤规则和阈值
   - 扩展自定义集成
   - 改进告警准确性

## 五、未来发展趋势

### 5.1 AI驱动的监控

- **智能异常检测**：使用机器学习识别异常模式
- **预测性监控**：基于历史数据预测潜在问题
- **自动化修复**：AI驱动的问题自动修复
- **智能告警**：减少误报，提高告警质量

### 5.2 边缘计算集成

- **边缘监控**：在CDN边缘节点部署监控
- **实时数据处理**：边缘计算实时处理监控数据
- **分布式监控**：跨地域的分布式监控架构
- **低延迟告警**：边缘计算实现毫秒级告警

### 5.3 隐私保护增强

- **零知识监控**：在不泄露敏感信息的情况下进行监控
- **本地化处理**：敏感数据本地处理，只上传聚合结果
- **加密传输**：端到端加密的监控数据传输
- **合规性自动化**：自动化的数据合规性检查

## 总结

通过本文介绍的高级配置和自定义集成方案，我们可以构建一个功能强大、灵活可扩展的企业级前端监控体系。这个体系不仅能够全面监控应用的健康状况，还能够提供智能化的告警和分析能力，帮助团队快速发现和解决问题，提升用户体验和应用质量。

关键要点：
- 合理的环境配置和多环境管理
- 自定义传输和数据处理优化
- 高级过滤和数据清理机制
- 业务特定的自定义集成
- 智能化的告警和通知系统
- 持续优化和团队协作

随着前端技术的不断发展，监控体系也需要持续演进，结合AI、边缘计算等新技术，为企业提供更加智能、高效的监控解决方案。