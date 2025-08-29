# 前端Sentry实战指南：从错误监控到性能优化的完整解决方案

## 前言

Sentry作为业界领先的应用监控平台，为前端开发者提供了强大的错误追踪、性能监控和用户体验分析能力。本文将深入探讨Sentry在前端项目中的实战应用，从基础配置到高级优化，帮助开发者构建完善的前端监控体系。

## 一、Sentry基础配置与集成

### 1.1 项目初始化配置

```javascript
// sentry.config.js
import * as Sentry from '@sentry/browser';
import { Integrations } from '@sentry/tracing';

// Sentry初始化配置
Sentry.init({
  dsn: process.env.REACT_APP_SENTRY_DSN,
  environment: process.env.NODE_ENV,
  release: process.env.REACT_APP_VERSION,
  
  // 性能监控配置
  integrations: [
    new Integrations.BrowserTracing({
      // 路由变化追踪
      routingInstrumentation: Sentry.reactRouterV6Instrumentation(
        React.useEffect,
        useLocation,
        useNavigationType,
        createRoutesFromChildren,
        matchRoutes
      ),
      // 自动追踪用户交互
      tracingOrigins: ['localhost', 'your-api-domain.com', /^\//],
      // 自动追踪网络请求
      beforeNavigate: context => {
        return {
          ...context,
          name: location.pathname
        };
      }
    }),
    // 重放集成
    new Sentry.Replay({
      maskAllText: false,
      blockAllMedia: false,
      maskAllInputs: true,
      // 采样率配置
      sessionSampleRate: 0.1,
      errorSampleRate: 1.0
    })
  ],
  
  // 性能监控采样率
  tracesSampleRate: process.env.NODE_ENV === 'production' ? 0.1 : 1.0,
  
  // 错误过滤
  beforeSend(event, hint) {
    // 过滤掉开发环境的错误
    if (process.env.NODE_ENV === 'development') {
      console.log('Sentry Event:', event);
    }
    
    // 过滤掉特定错误
    if (event.exception) {
      const error = hint.originalException;
      
      // 过滤网络错误
      if (error && error.name === 'NetworkError') {
        return null;
      }
      
      // 过滤第三方脚本错误
      if (event.exception.values[0].stacktrace?.frames) {
        const frames = event.exception.values[0].stacktrace.frames;
        const isThirdParty = frames.some(frame => 
          frame.filename && !frame.filename.includes(window.location.origin)
        );
        
        if (isThirdParty) {
          return null;
        }
      }
    }
    
    return event;
  },
  
  // 面包屑过滤
  beforeBreadcrumb(breadcrumb, hint) {
    // 过滤敏感信息
    if (breadcrumb.category === 'console' && breadcrumb.level === 'log') {
      return null;
    }
    
    // 过滤频繁的UI事件
    if (breadcrumb.category === 'ui.click' && 
        breadcrumb.message?.includes('button')) {
      return breadcrumb;
    }
    
    return breadcrumb;
  }
});

// 设置用户上下文
export const setSentryUser = (user) => {
  Sentry.setUser({
    id: user.id,
    email: user.email,
    username: user.username,
    // 不要包含敏感信息
    extra: {
      plan: user.plan,
      role: user.role
    }
  });
};

// 设置标签
export const setSentryTags = (tags) => {
  Sentry.setTags(tags);
};

// 设置上下文
export const setSentryContext = (key, context) => {
  Sentry.setContext(key, context);
};
```

### 1.2 React应用集成

```javascript
// App.js
import React from 'react';
import * as Sentry from '@sentry/react';
import { BrowserRouter } from 'react-router-dom';
import './sentry.config';

// Sentry错误边界组件
const SentryErrorBoundary = Sentry.withErrorBoundary(
  ({ children }) => children,
  {
    fallback: ({ error, resetError }) => (
      <div className="error-boundary">
        <h2>出现了一个错误</h2>
        <p>{error.message}</p>
        <button onClick={resetError}>重试</button>
      </div>
    ),
    beforeCapture: (scope, error, errorInfo) => {
      scope.setTag('errorBoundary', true);
      scope.setContext('errorInfo', errorInfo);
    }
  }
);

// 性能监控HOC
const withSentryProfiling = (WrappedComponent, componentName) => {
  return Sentry.withProfiler(WrappedComponent, { name: componentName });
};

function App() {
  React.useEffect(() => {
    // 设置应用启动时的上下文
    Sentry.setContext('app', {
      startTime: new Date().toISOString(),
      userAgent: navigator.userAgent,
      viewport: {
        width: window.innerWidth,
        height: window.innerHeight
      }
    });
  }, []);

  return (
    <SentryErrorBoundary>
      <BrowserRouter>
        <div className="App">
          {/* 应用内容 */}
        </div>
      </BrowserRouter>
    </SentryErrorBoundary>
  );
}

export default Sentry.withSentryRouting(App);
```

### 1.3 Vue应用集成

```javascript
// main.js
import { createApp } from 'vue';
import * as Sentry from '@sentry/vue';
import { BrowserTracing } from '@sentry/tracing';
import router from './router';
import App from './App.vue';

const app = createApp(App);

Sentry.init({
  app,
  dsn: process.env.VUE_APP_SENTRY_DSN,
  environment: process.env.NODE_ENV,
  
  integrations: [
    new BrowserTracing({
      routingInstrumentation: Sentry.vueRouterInstrumentation(router),
      tracingOrigins: ['localhost', 'your-api-domain.com', /^\//]
    })
  ],
  
  tracesSampleRate: 0.1,
  
  // Vue特定配置
  trackComponents: true,
  timeout: 2000,
  hooks: ['activate', 'mount', 'update'],
  
  beforeSend(event, hint) {
    // Vue错误处理
    if (event.exception) {
      const error = hint.originalException;
      
      // 过滤Vue警告
      if (error && error.message && error.message.includes('[Vue warn]')) {
        return null;
      }
    }
    
    return event;
  }
});

app.use(router);
app.mount('#app');
```

## 二、错误监控与追踪

### 2.1 自定义错误处理

```javascript
// errorHandler.js
import * as Sentry from '@sentry/browser';

// 错误分类枚举
export const ErrorTypes = {
  NETWORK: 'network',
  VALIDATION: 'validation',
  BUSINESS: 'business',
  SYSTEM: 'system',
  USER: 'user'
};

// 错误严重级别
export const ErrorSeverity = {
  LOW: 'low',
  MEDIUM: 'medium',
  HIGH: 'high',
  CRITICAL: 'critical'
};

// 自定义错误类
export class CustomError extends Error {
  constructor(message, type, severity = ErrorSeverity.MEDIUM, context = {}) {
    super(message);
    this.name = 'CustomError';
    this.type = type;
    this.severity = severity;
    this.context = context;
    this.timestamp = new Date().toISOString();
  }
}

// 错误处理器
export class ErrorHandler {
  static captureError(error, context = {}) {
    // 设置错误上下文
    Sentry.withScope(scope => {
      // 设置错误级别
      if (error.severity) {
        scope.setLevel(error.severity);
      }
      
      // 设置错误类型标签
      if (error.type) {
        scope.setTag('errorType', error.type);
      }
      
      // 设置额外上下文
      scope.setContext('errorContext', {
        ...error.context,
        ...context,
        userAgent: navigator.userAgent,
        url: window.location.href,
        timestamp: error.timestamp || new Date().toISOString()
      });
      
      // 设置指纹以便错误分组
      if (error.type && error.message) {
        scope.setFingerprint([error.type, error.message]);
      }
      
      // 捕获错误
      Sentry.captureException(error);
    });
  }
  
  static captureMessage(message, level = 'info', context = {}) {
    Sentry.withScope(scope => {
      scope.setContext('messageContext', context);
      Sentry.captureMessage(message, level);
    });
  }
  
  // API错误处理
  static handleAPIError(error, request) {
    const apiError = new CustomError(
      `API Error: ${error.message}`,
      ErrorTypes.NETWORK,
      this.getAPIErrorSeverity(error.status),
      {
        url: request.url,
        method: request.method,
        status: error.status,
        statusText: error.statusText,
        requestData: request.data,
        responseData: error.response?.data
      }
    );
    
    this.captureError(apiError);
  }
  
  // 业务逻辑错误处理
  static handleBusinessError(message, context = {}) {
    const businessError = new CustomError(
      message,
      ErrorTypes.BUSINESS,
      ErrorSeverity.MEDIUM,
      context
    );
    
    this.captureError(businessError);
  }
  
  // 用户操作错误处理
  static handleUserError(message, action, context = {}) {
    const userError = new CustomError(
      `User Error: ${message}`,
      ErrorTypes.USER,
      ErrorSeverity.LOW,
      {
        action,
        ...context
      }
    );
    
    this.captureError(userError);
  }
  
  static getAPIErrorSeverity(status) {
    if (status >= 500) return ErrorSeverity.CRITICAL;
    if (status >= 400) return ErrorSeverity.HIGH;
    return ErrorSeverity.MEDIUM;
  }
}

// 全局错误处理
window.addEventListener('error', (event) => {
  const error = new CustomError(
    event.message,
    ErrorTypes.SYSTEM,
    ErrorSeverity.HIGH,
    {
      filename: event.filename,
      lineno: event.lineno,
      colno: event.colno,
      stack: event.error?.stack
    }
  );
  
  ErrorHandler.captureError(error);
});

// 未处理的Promise拒绝
window.addEventListener('unhandledrejection', (event) => {
  const error = new CustomError(
    `Unhandled Promise Rejection: ${event.reason}`,
    ErrorTypes.SYSTEM,
    ErrorSeverity.HIGH,
    {
      reason: event.reason,
      promise: event.promise
    }
  );
  
  ErrorHandler.captureError(error);
});
```

### 2.2 API请求监控

```javascript
// apiMonitor.js
import * as Sentry from '@sentry/browser';
import { ErrorHandler } from './errorHandler';

// API监控装饰器
export function withAPIMonitoring(apiClient) {
  // 请求拦截器
  apiClient.interceptors.request.use(
    (config) => {
      // 开始性能追踪
      const transaction = Sentry.startTransaction({
        name: `${config.method?.toUpperCase()} ${config.url}`,
        op: 'http.client'
      });
      
      // 将transaction附加到请求配置
      config.metadata = {
        transaction,
        startTime: Date.now()
      };
      
      // 设置请求头用于分布式追踪
      const traceparent = transaction.toTraceparent();
      if (traceparent) {
        config.headers['sentry-trace'] = traceparent;
      }
      
      return config;
    },
    (error) => {
      ErrorHandler.handleAPIError(error, error.config);
      return Promise.reject(error);
    }
  );
  
  // 响应拦截器
  apiClient.interceptors.response.use(
    (response) => {
      const { transaction, startTime } = response.config.metadata || {};
      
      if (transaction) {
        // 设置响应状态
        transaction.setHttpStatus(response.status);
        transaction.setData('response.size', JSON.stringify(response.data).length);
        transaction.setData('response.time', Date.now() - startTime);
        
        // 完成事务
        transaction.finish();
      }
      
      // 记录成功的API调用
      Sentry.addBreadcrumb({
        category: 'http',
        message: `${response.config.method?.toUpperCase()} ${response.config.url}`,
        level: 'info',
        data: {
          status: response.status,
          duration: Date.now() - startTime
        }
      });
      
      return response;
    },
    (error) => {
      const { transaction, startTime } = error.config?.metadata || {};
      
      if (transaction) {
        // 设置错误状态
        transaction.setHttpStatus(error.response?.status || 0);
        transaction.setData('error', error.message);
        transaction.setData('response.time', Date.now() - startTime);
        
        // 完成事务
        transaction.finish();
      }
      
      // 处理API错误
      ErrorHandler.handleAPIError(error, error.config);
      
      return Promise.reject(error);
    }
  );
  
  return apiClient;
}

// API性能监控
export class APIPerformanceMonitor {
  constructor() {
    this.metrics = new Map();
    this.thresholds = {
      slow: 1000, // 1秒
      verySlow: 3000 // 3秒
    };
  }
  
  recordAPICall(url, method, duration, status) {
    const key = `${method} ${url}`;
    
    if (!this.metrics.has(key)) {
      this.metrics.set(key, {
        calls: 0,
        totalDuration: 0,
        errors: 0,
        slowCalls: 0,
        verySlowCalls: 0
      });
    }
    
    const metric = this.metrics.get(key);
    metric.calls++;
    metric.totalDuration += duration;
    
    if (status >= 400) {
      metric.errors++;
    }
    
    if (duration > this.thresholds.verySlow) {
      metric.verySlowCalls++;
      
      // 记录非常慢的API调用
      Sentry.withScope(scope => {
        scope.setTag('performance', 'very_slow_api');
        scope.setContext('apiPerformance', {
          url,
          method,
          duration,
          threshold: this.thresholds.verySlow
        });
        
        Sentry.captureMessage(
          `Very slow API call: ${method} ${url} took ${duration}ms`,
          'warning'
        );
      });
    } else if (duration > this.thresholds.slow) {
      metric.slowCalls++;
    }
  }
  
  getMetrics() {
    const result = {};
    
    for (const [key, metric] of this.metrics) {
      result[key] = {
        ...metric,
        averageDuration: metric.totalDuration / metric.calls,
        errorRate: metric.errors / metric.calls,
        slowRate: metric.slowCalls / metric.calls,
        verySlowRate: metric.verySlowCalls / metric.calls
      };
    }
    
    return result;
  }
  
  reportMetrics() {
    const metrics = this.getMetrics();
    
    Sentry.withScope(scope => {
      scope.setContext('apiMetrics', metrics);
      Sentry.captureMessage('API Performance Metrics', 'info');
    });
  }
}

// 创建全局API监控实例
export const apiMonitor = new APIPerformanceMonitor();

// 定期报告API性能指标
setInterval(() => {
  apiMonitor.reportMetrics();
}, 5 * 60 * 1000); // 每5分钟报告一次
```

## 三、性能监控与优化

### 3.1 Core Web Vitals监控

```javascript
// performanceMonitor.js
import * as Sentry from '@sentry/browser';
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

// Web Vitals监控器
export class WebVitalsMonitor {
  constructor() {
    this.vitals = {};
    this.thresholds = {
      LCP: { good: 2500, poor: 4000 },
      FID: { good: 100, poor: 300 },
      CLS: { good: 0.1, poor: 0.25 },
      FCP: { good: 1800, poor: 3000 },
      TTFB: { good: 800, poor: 1800 }
    };
    
    this.initializeMonitoring();
  }
  
  initializeMonitoring() {
    // 监控Largest Contentful Paint
    getLCP((metric) => {
      this.recordVital('LCP', metric);
    });
    
    // 监控First Input Delay
    getFID((metric) => {
      this.recordVital('FID', metric);
    });
    
    // 监控Cumulative Layout Shift
    getCLS((metric) => {
      this.recordVital('CLS', metric);
    });
    
    // 监控First Contentful Paint
    getFCP((metric) => {
      this.recordVital('FCP', metric);
    });
    
    // 监控Time to First Byte
    getTTFB((metric) => {
      this.recordVital('TTFB', metric);
    });
  }
  
  recordVital(name, metric) {
    this.vitals[name] = metric;
    
    const threshold = this.thresholds[name];
    const rating = this.getRating(metric.value, threshold);
    
    // 发送到Sentry
    Sentry.withScope(scope => {
      scope.setTag('vital', name);
      scope.setTag('rating', rating);
      scope.setContext('webVital', {
        name,
        value: metric.value,
        rating,
        id: metric.id,
        delta: metric.delta,
        entries: metric.entries?.map(entry => ({
          name: entry.name,
          startTime: entry.startTime,
          duration: entry.duration
        }))
      });
      
      // 如果性能指标较差，记录为警告
      if (rating === 'poor') {
        Sentry.captureMessage(
          `Poor ${name} performance: ${metric.value}`,
          'warning'
        );
      }
    });
    
    // 记录性能指标
    Sentry.setMeasurement(name, metric.value, 'millisecond');
  }
  
  getRating(value, threshold) {
    if (value <= threshold.good) return 'good';
    if (value <= threshold.poor) return 'needs-improvement';
    return 'poor';
  }
  
  getVitals() {
    return this.vitals;
  }
  
  reportVitals() {
    Sentry.withScope(scope => {
      scope.setContext('allWebVitals', this.vitals);
      Sentry.captureMessage('Web Vitals Report', 'info');
    });
  }
}

// 资源加载监控
export class ResourceMonitor {
  constructor() {
    this.resources = [];
    this.initializeMonitoring();
  }
  
  initializeMonitoring() {
    // 监控资源加载
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        this.recordResource(entry);
      }
    });
    
    observer.observe({ entryTypes: ['resource'] });
    
    // 监控长任务
    const longTaskObserver = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        this.recordLongTask(entry);
      }
    });
    
    if ('PerformanceObserver' in window && 'PerformanceLongTaskTiming' in window) {
      longTaskObserver.observe({ entryTypes: ['longtask'] });
    }
  }
  
  recordResource(entry) {
    const resource = {
      name: entry.name,
      type: this.getResourceType(entry),
      size: entry.transferSize,
      duration: entry.duration,
      startTime: entry.startTime,
      responseEnd: entry.responseEnd
    };
    
    this.resources.push(resource);
    
    // 检查慢资源
    if (entry.duration > 1000) { // 超过1秒
      Sentry.withScope(scope => {
        scope.setTag('performance', 'slow_resource');
        scope.setContext('slowResource', resource);
        
        Sentry.captureMessage(
          `Slow resource loading: ${entry.name} took ${entry.duration}ms`,
          'warning'
        );
      });
    }
    
    // 检查大文件
    if (entry.transferSize > 1024 * 1024) { // 超过1MB
      Sentry.withScope(scope => {
        scope.setTag('performance', 'large_resource');
        scope.setContext('largeResource', resource);
        
        Sentry.captureMessage(
          `Large resource: ${entry.name} is ${(entry.transferSize / 1024 / 1024).toFixed(2)}MB`,
          'info'
        );
      });
    }
  }
  
  recordLongTask(entry) {
    const longTask = {
      name: entry.name,
      duration: entry.duration,
      startTime: entry.startTime,
      attribution: entry.attribution?.map(attr => ({
        name: attr.name,
        type: attr.containerType,
        src: attr.containerSrc,
        id: attr.containerId
      }))
    };
    
    Sentry.withScope(scope => {
      scope.setTag('performance', 'long_task');
      scope.setContext('longTask', longTask);
      
      Sentry.captureMessage(
        `Long task detected: ${entry.duration}ms`,
        'warning'
      );
    });
  }
  
  getResourceType(entry) {
    if (entry.initiatorType) {
      return entry.initiatorType;
    }
    
    const url = new URL(entry.name);
    const extension = url.pathname.split('.').pop()?.toLowerCase();
    
    const typeMap = {
      'js': 'script',
      'css': 'link',
      'png': 'img',
      'jpg': 'img',
      'jpeg': 'img',
      'gif': 'img',
      'svg': 'img',
      'woff': 'font',
      'woff2': 'font',
      'ttf': 'font'
    };
    
    return typeMap[extension] || 'other';
  }
  
  getResourceMetrics() {
    const metrics = {
      totalResources: this.resources.length,
      totalSize: this.resources.reduce((sum, r) => sum + (r.size || 0), 0),
      averageLoadTime: this.resources.reduce((sum, r) => sum + r.duration, 0) / this.resources.length,
      byType: {}
    };
    
    // 按类型分组
    for (const resource of this.resources) {
      if (!metrics.byType[resource.type]) {
        metrics.byType[resource.type] = {
          count: 0,
          totalSize: 0,
          totalDuration: 0
        };
      }
      
      const typeMetric = metrics.byType[resource.type];
      typeMetric.count++;
      typeMetric.totalSize += resource.size || 0;
      typeMetric.totalDuration += resource.duration;
    }
    
    // 计算平均值
    for (const type in metrics.byType) {
      const typeMetric = metrics.byType[type];
      typeMetric.averageSize = typeMetric.totalSize / typeMetric.count;
      typeMetric.averageDuration = typeMetric.totalDuration / typeMetric.count;
    }
    
    return metrics;
  }
}

// 创建监控实例
export const webVitalsMonitor = new WebVitalsMonitor();
export const resourceMonitor = new ResourceMonitor();

// 页面卸载时报告性能数据
window.addEventListener('beforeunload', () => {
  webVitalsMonitor.reportVitals();
  
  const resourceMetrics = resourceMonitor.getResourceMetrics();
  Sentry.withScope(scope => {
    scope.setContext('resourceMetrics', resourceMetrics);
    Sentry.captureMessage('Resource Performance Metrics', 'info');
  });
});
```

### 3.2 用户体验监控

```javascript
// userExperienceMonitor.js
import * as Sentry from '@sentry/browser';

// 用户体验监控器
export class UserExperienceMonitor {
  constructor() {
    this.interactions = [];
    this.pageViews = [];
    this.userSessions = new Map();
    this.frustrationEvents = [];
    
    this.initializeMonitoring();
  }
  
  initializeMonitoring() {
    // 监控用户交互
    this.setupInteractionTracking();
    
    // 监控页面可见性
    this.setupVisibilityTracking();
    
    // 监控用户挫败事件
    this.setupFrustrationTracking();
    
    // 监控页面性能
    this.setupPagePerformanceTracking();
  }
  
  setupInteractionTracking() {
    // 点击事件
    document.addEventListener('click', (event) => {
      this.recordInteraction('click', event);
    });
    
    // 表单提交
    document.addEventListener('submit', (event) => {
      this.recordInteraction('submit', event);
    });
    
    // 输入事件
    document.addEventListener('input', (event) => {
      this.recordInteraction('input', event);
    }, { passive: true });
    
    // 滚动事件
    let scrollTimeout;
    document.addEventListener('scroll', () => {
      clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        this.recordInteraction('scroll', {
          scrollY: window.scrollY,
          scrollX: window.scrollX
        });
      }, 100);
    }, { passive: true });
  }
  
  recordInteraction(type, event) {
    const interaction = {
      type,
      timestamp: Date.now(),
      target: this.getElementInfo(event.target || event),
      page: window.location.pathname,
      viewport: {
        width: window.innerWidth,
        height: window.innerHeight
      }
    };
    
    this.interactions.push(interaction);
    
    // 添加面包屑
    Sentry.addBreadcrumb({
      category: 'ui.' + type,
      message: `User ${type} on ${interaction.target.tagName}`,
      level: 'info',
      data: interaction
    });
    
    // 检查快速连续点击（可能表示挫败）
    if (type === 'click') {
      this.checkRapidClicks(interaction);
    }
  }
  
  getElementInfo(element) {
    if (!element || !element.tagName) {
      return { tagName: 'unknown' };
    }
    
    return {
      tagName: element.tagName.toLowerCase(),
      id: element.id,
      className: element.className,
      text: element.textContent?.substring(0, 50),
      attributes: {
        type: element.type,
        name: element.name,
        value: element.value?.substring(0, 50)
      }
    };
  }
  
  checkRapidClicks(interaction) {
    const recentClicks = this.interactions
      .filter(i => i.type === 'click' && Date.now() - i.timestamp < 2000)
      .slice(-5);
    
    if (recentClicks.length >= 3) {
      // 检查是否在同一元素上
      const sameElement = recentClicks.every(click => 
        click.target.tagName === interaction.target.tagName &&
        click.target.id === interaction.target.id
      );
      
      if (sameElement) {
        this.recordFrustrationEvent('rapid_clicks', {
          element: interaction.target,
          clickCount: recentClicks.length,
          timespan: Date.now() - recentClicks[0].timestamp
        });
      }
    }
  }
  
  setupVisibilityTracking() {
    let pageStartTime = Date.now();
    let isVisible = !document.hidden;
    
    document.addEventListener('visibilitychange', () => {
      const now = Date.now();
      
      if (document.hidden && isVisible) {
        // 页面变为不可见
        const viewTime = now - pageStartTime;
        this.recordPageView(viewTime);
        isVisible = false;
      } else if (!document.hidden && !isVisible) {
        // 页面变为可见
        pageStartTime = now;
        isVisible = true;
      }
    });
    
    // 页面卸载时记录
    window.addEventListener('beforeunload', () => {
      if (isVisible) {
        const viewTime = Date.now() - pageStartTime;
        this.recordPageView(viewTime);
      }
    });
  }
  
  recordPageView(duration) {
    const pageView = {
      url: window.location.href,
      duration,
      timestamp: Date.now(),
      referrer: document.referrer,
      userAgent: navigator.userAgent
    };
    
    this.pageViews.push(pageView);
    
    // 发送到Sentry
    Sentry.withScope(scope => {
      scope.setContext('pageView', pageView);
      
      if (duration < 5000) { // 少于5秒可能表示用户快速离开
        scope.setTag('engagement', 'low');
        Sentry.captureMessage('Short page view', 'info');
      } else if (duration > 300000) { // 超过5分钟表示高参与度
        scope.setTag('engagement', 'high');
        Sentry.captureMessage('Long page view', 'info');
      }
    });
  }
  
  setupFrustrationTracking() {
    // 监控错误点击（点击不可点击元素）
    document.addEventListener('click', (event) => {
      const element = event.target;
      const isClickable = element.tagName === 'BUTTON' ||
                         element.tagName === 'A' ||
                         element.onclick ||
                         element.getAttribute('role') === 'button' ||
                         getComputedStyle(element).cursor === 'pointer';
      
      if (!isClickable && element.tagName !== 'INPUT') {
        this.recordFrustrationEvent('dead_click', {
          element: this.getElementInfo(element),
          coordinates: { x: event.clientX, y: event.clientY }
        });
      }
    });
    
    // 监控愤怒点击（快速重复点击）
    let clickCount = 0;
    let clickTimer;
    
    document.addEventListener('click', () => {
      clickCount++;
      clearTimeout(clickTimer);
      
      clickTimer = setTimeout(() => {
        if (clickCount >= 5) {
          this.recordFrustrationEvent('rage_clicks', {
            count: clickCount,
            timespan: 1000
          });
        }
        clickCount = 0;
      }, 1000);
    });
  }
  
  recordFrustrationEvent(type, data) {
    const frustrationEvent = {
      type,
      data,
      timestamp: Date.now(),
      page: window.location.pathname,
      userAgent: navigator.userAgent
    };
    
    this.frustrationEvents.push(frustrationEvent);
    
    // 发送到Sentry
    Sentry.withScope(scope => {
      scope.setTag('frustration', type);
      scope.setContext('frustrationEvent', frustrationEvent);
      
      Sentry.captureMessage(
        `User frustration detected: ${type}`,
        'warning'
      );
    });
  }
  
  setupPagePerformanceTracking() {
    // 监控页面加载性能
    window.addEventListener('load', () => {
      setTimeout(() => {
        const navigation = performance.getEntriesByType('navigation')[0];
        
        if (navigation) {
          const performanceData = {
            domContentLoaded: navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart,
            loadComplete: navigation.loadEventEnd - navigation.loadEventStart,
            domInteractive: navigation.domInteractive - navigation.navigationStart,
            firstPaint: this.getFirstPaint(),
            firstContentfulPaint: this.getFirstContentfulPaint()
          };
          
          Sentry.withScope(scope => {
            scope.setContext('pagePerformance', performanceData);
            
            // 检查性能问题
            if (performanceData.domContentLoaded > 3000) {
              scope.setTag('performance', 'slow_dom');
              Sentry.captureMessage('Slow DOM content loaded', 'warning');
            }
            
            if (performanceData.loadComplete > 5000) {
              scope.setTag('performance', 'slow_load');
              Sentry.captureMessage('Slow page load', 'warning');
            }
          });
        }
      }, 0);
    });
  }
  
  getFirstPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const firstPaint = paintEntries.find(entry => entry.name === 'first-paint');
    return firstPaint ? firstPaint.startTime : null;
  }
  
  getFirstContentfulPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const fcp = paintEntries.find(entry => entry.name === 'first-contentful-paint');
    return fcp ? fcp.startTime : null;
  }
  
  // 获取用户体验指标
  getUserExperienceMetrics() {
    return {
      interactions: this.interactions.length,
      pageViews: this.pageViews.length,
      frustrationEvents: this.frustrationEvents.length,
      averagePageViewDuration: this.pageViews.reduce((sum, pv) => sum + pv.duration, 0) / this.pageViews.length,
      mostCommonFrustration: this.getMostCommonFrustration(),
      engagementLevel: this.calculateEngagementLevel()
    };
  }
  
  getMostCommonFrustration() {
    const frustrationCounts = {};
    
    for (const event of this.frustrationEvents) {
      frustrationCounts[event.type] = (frustrationCounts[event.type] || 0) + 1;
    }
    
    return Object.keys(frustrationCounts).reduce((a, b) => 
      frustrationCounts[a] > frustrationCounts[b] ? a : b
    );
  }
  
  calculateEngagementLevel() {
    const avgPageViewDuration = this.pageViews.reduce((sum, pv) => sum + pv.duration, 0) / this.pageViews.length;
    const interactionRate = this.interactions.length / this.pageViews.length;
    const frustrationRate = this.frustrationEvents.length / this.interactions.length;
    
    if (avgPageViewDuration > 60000 && interactionRate > 5 && frustrationRate < 0.1) {
      return 'high';
    } else if (avgPageViewDuration > 30000 && interactionRate > 2 && frustrationRate < 0.2) {
      return 'medium';
    } else {
      return 'low';
    }
  }
}

// 创建用户体验监控实例
export const uxMonitor = new UserExperienceMonitor();

// 定期报告用户体验指标
setInterval(() => {
  const metrics = uxMonitor.getUserExperienceMetrics();
  
  Sentry.withScope(scope => {
    scope.setContext('userExperienceMetrics', metrics);
    Sentry.captureMessage('User Experience Metrics', 'info');
  });
}, 10 * 60 * 1000); // 每10分钟报告一次
```

## 四、高级功能与最佳实践

### 4.1 自定义仪表板与告警

```javascript
// sentryDashboard.js
import * as Sentry from '@sentry/browser';

// 自定义仪表板数据收集器
export class SentryDashboardCollector {
  constructor() {
    this.metrics = {
      errors: [],
      performance: [],
      userActivity: [],
      businessMetrics: []
    };
    
    this.alertRules = new Map();
    this.initializeCollection();
  }
  
  initializeCollection() {
    // 定期收集指标
    setInterval(() => {
      this.collectMetrics();
      this.checkAlertRules();
    }, 60000); // 每分钟收集一次
  }
  
  collectMetrics() {
    // 收集错误指标
    this.collectErrorMetrics();
    
    // 收集性能指标
    this.collectPerformanceMetrics();
    
    // 收集用户活动指标
    this.collectUserActivityMetrics();
    
    // 收集业务指标
    this.collectBusinessMetrics();
  }
  
  collectErrorMetrics() {
    const errorMetric = {
      timestamp: Date.now(),
      errorCount: this.getErrorCount(),
      errorRate: this.getErrorRate(),
      topErrors: this.getTopErrors(),
      affectedUsers: this.getAffectedUsers()
    };
    
    this.metrics.errors.push(errorMetric);
    
    // 保持最近24小时的数据
    const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000;
    this.metrics.errors = this.metrics.errors.filter(m => m.timestamp > oneDayAgo);
  }
  
  collectPerformanceMetrics() {
    const performanceMetric = {
      timestamp: Date.now(),
      averageLoadTime: this.getAverageLoadTime(),
      p95LoadTime: this.getP95LoadTime(),
      slowPages: this.getSlowPages(),
      coreWebVitals: this.getCoreWebVitals()
    };
    
    this.metrics.performance.push(performanceMetric);
    
    // 保持最近24小时的数据
    const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000;
    this.metrics.performance = this.metrics.performance.filter(m => m.timestamp > oneDayAgo);
  }
  
  collectUserActivityMetrics() {
    const userActivityMetric = {
      timestamp: Date.now(),
      activeUsers: this.getActiveUsers(),
      pageViews: this.getPageViews(),
      bounceRate: this.getBounceRate(),
      averageSessionDuration: this.getAverageSessionDuration()
    };
    
    this.metrics.userActivity.push(userActivityMetric);
  }
  
  collectBusinessMetrics() {
    const businessMetric = {
      timestamp: Date.now(),
      conversionRate: this.getConversionRate(),
      revenueImpact: this.getRevenueImpact(),
      featureUsage: this.getFeatureUsage(),
      userSatisfaction: this.getUserSatisfaction()
    };
    
    this.metrics.businessMetrics.push(businessMetric);
  }
  
  // 告警规则管理
  addAlertRule(name, condition, action) {
    this.alertRules.set(name, {
      condition,
      action,
      lastTriggered: null,
      cooldown: 5 * 60 * 1000 // 5分钟冷却期
    });
  }
  
  checkAlertRules() {
    for (const [name, rule] of this.alertRules) {
      // 检查冷却期
      if (rule.lastTriggered && Date.now() - rule.lastTriggered < rule.cooldown) {
        continue;
      }
      
      // 检查条件
      if (rule.condition(this.metrics)) {
        rule.action(name, this.metrics);
        rule.lastTriggered = Date.now();
      }
    }
  }
  
  // 预定义告警规则
  setupDefaultAlertRules() {
    // 错误率告警
    this.addAlertRule(
      'high_error_rate',
      (metrics) => {
        const recentErrors = metrics.errors.slice(-5); // 最近5分钟
        const avgErrorRate = recentErrors.reduce((sum, m) => sum + m.errorRate, 0) / recentErrors.length;
        return avgErrorRate > 0.05; // 错误率超过5%
      },
      (name, metrics) => {
        Sentry.withScope(scope => {
          scope.setTag('alert', name);
          scope.setLevel('error');
          scope.setContext('alertData', {
            errorRate: metrics.errors.slice(-1)[0].errorRate,
            threshold: 0.05
          });
          
          Sentry.captureMessage('High error rate detected', 'error');
        });
      }
    );
    
    // 性能告警
    this.addAlertRule(
      'slow_performance',
      (metrics) => {
        const recentPerf = metrics.performance.slice(-5);
        const avgLoadTime = recentPerf.reduce((sum, m) => sum + m.averageLoadTime, 0) / recentPerf.length;
        return avgLoadTime > 3000; // 平均加载时间超过3秒
      },
      (name, metrics) => {
        Sentry.withScope(scope => {
          scope.setTag('alert', name);
          scope.setLevel('warning');
          scope.setContext('alertData', {
            averageLoadTime: metrics.performance.slice(-1)[0].averageLoadTime,
            threshold: 3000
          });
          
          Sentry.captureMessage('Slow performance detected', 'warning');
        });
      }
    );
    
    // 用户体验告警
    this.addAlertRule(
      'high_bounce_rate',
      (metrics) => {
        const recentActivity = metrics.userActivity.slice(-10); // 最近10分钟
        const avgBounceRate = recentActivity.reduce((sum, m) => sum + m.bounceRate, 0) / recentActivity.length;
        return avgBounceRate > 0.7; // 跳出率超过70%
      },
      (name, metrics) => {
        Sentry.withScope(scope => {
          scope.setTag('alert', name);
          scope.setLevel('warning');
          scope.setContext('alertData', {
            bounceRate: metrics.userActivity.slice(-1)[0].bounceRate,
            threshold: 0.7
          });
          
          Sentry.captureMessage('High bounce rate detected', 'warning');
        });
      }
    );
  }
  
  // 生成仪表板数据
  generateDashboardData() {
    return {
      summary: this.generateSummary(),
      trends: this.generateTrends(),
      alerts: this.getActiveAlerts(),
      recommendations: this.generateRecommendations()
    };
  }
  
  generateSummary() {
    const latestMetrics = {
      errors: this.metrics.errors.slice(-1)[0],
      performance: this.metrics.performance.slice(-1)[0],
      userActivity: this.metrics.userActivity.slice(-1)[0],
      businessMetrics: this.metrics.businessMetrics.slice(-1)[0]
    };
    
    return {
      errorRate: latestMetrics.errors?.errorRate || 0,
      averageLoadTime: latestMetrics.performance?.averageLoadTime || 0,
      activeUsers: latestMetrics.userActivity?.activeUsers || 0,
      conversionRate: latestMetrics.businessMetrics?.conversionRate || 0,
      healthScore: this.calculateHealthScore(latestMetrics)
    };
  }
  
  calculateHealthScore(metrics) {
    let score = 100;
    
    // 错误率影响
    if (metrics.errors?.errorRate > 0.01) {
      score -= Math.min(50, metrics.errors.errorRate * 1000);
    }
    
    // 性能影响
    if (metrics.performance?.averageLoadTime > 2000) {
      score -= Math.min(30, (metrics.performance.averageLoadTime - 2000) / 100);
    }
    
    // 用户体验影响
    if (metrics.userActivity?.bounceRate > 0.5) {
      score -= Math.min(20, (metrics.userActivity.bounceRate - 0.5) * 40);
    }
    
    return Math.max(0, Math.round(score));
  }
}

// 创建仪表板收集器实例
export const dashboardCollector = new SentryDashboardCollector();
dashboardCollector.setupDefaultAlertRules();
```

### 4.2 A/B测试与功能标志集成

```javascript
// sentryABTesting.js
import * as Sentry from '@sentry/browser';

// A/B测试与Sentry集成
export class SentryABTestingIntegration {
  constructor() {
    this.experiments = new Map();
    this.userVariants = new Map();
    this.conversionEvents = [];
  }
  
  // 注册A/B测试实验
  registerExperiment(experimentId, config) {
    this.experiments.set(experimentId, {
      id: experimentId,
      name: config.name,
      variants: config.variants,
      trafficAllocation: config.trafficAllocation || 1.0,
      conversionGoals: config.conversionGoals || [],
      startDate: config.startDate || new Date(),
      endDate: config.endDate,
      status: 'active'
    });
    
    // 在Sentry中设置实验标签
    Sentry.setTag(`experiment_${experimentId}`, 'registered');
  }
  
  // 为用户分配变体
  assignUserToVariant(userId, experimentId) {
    const experiment = this.experiments.get(experimentId);
    if (!experiment || experiment.status !== 'active') {
      return null;
    }
    
    // 检查是否已分配
    const existingVariant = this.userVariants.get(`${userId}_${experimentId}`);
    if (existingVariant) {
      return existingVariant;
    }
    
    // 检查流量分配
    const hash = this.hashUserId(userId + experimentId);
    if (hash > experiment.trafficAllocation) {
      return null; // 用户不参与实验
    }
    
    // 分配变体
    const variantIndex = Math.floor(hash * experiment.variants.length / experiment.trafficAllocation);
    const variant = experiment.variants[variantIndex];
    
    // 记录分配
    this.userVariants.set(`${userId}_${experimentId}`, variant);
    
    // 在Sentry中设置用户变体
    Sentry.setTag(`variant_${experimentId}`, variant.id);
    Sentry.setContext('abTest', {
      experimentId,
      variantId: variant.id,
      variantName: variant.name,
      assignmentTime: new Date().toISOString()
    });
    
    // 记录分配事件
    Sentry.addBreadcrumb({
      category: 'ab-test',
      message: `User assigned to variant ${variant.id} in experiment ${experimentId}`,
      level: 'info',
      data: {
        experimentId,
        variantId: variant.id,
        userId
      }
    });
    
    return variant;
  }
  
  // 记录转化事件
  recordConversion(userId, experimentId, goalId, value = 1) {
    const variant = this.userVariants.get(`${userId}_${experimentId}`);
    if (!variant) {
      return; // 用户未参与实验
    }
    
    const conversionEvent = {
      userId,
      experimentId,
      variantId: variant.id,
      goalId,
      value,
      timestamp: Date.now()
    };
    
    this.conversionEvents.push(conversionEvent);
    
    // 发送到Sentry
    Sentry.withScope(scope => {
      scope.setTag('conversion', goalId);
      scope.setTag(`experiment_${experimentId}`, variant.id);
      scope.setContext('conversion', conversionEvent);
      
      Sentry.captureMessage(
        `Conversion recorded: ${goalId} for variant ${variant.id}`,
        'info'
      );
    });
  }
  
  // 记录实验相关错误
  recordExperimentError(experimentId, variantId, error, context = {}) {
    Sentry.withScope(scope => {
      scope.setTag('experiment_error', experimentId);
      scope.setTag('variant', variantId);
      scope.setContext('experimentError', {
        experimentId,
        variantId,
        context,
        timestamp: new Date().toISOString()
      });
      
      Sentry.captureException(error);
    });
  }
  
  // 分析实验结果
  analyzeExperimentResults(experimentId) {
    const experiment = this.experiments.get(experimentId);
    if (!experiment) {
      return null;
    }
    
    const results = {
      experimentId,
      variants: {},
      summary: {
        totalUsers: 0,
        totalConversions: 0,
        overallConversionRate: 0
      }
    };
    
    // 初始化变体结果
    experiment.variants.forEach(variant => {
      results.variants[variant.id] = {
        id: variant.id,
        name: variant.name,
        users: 0,
        conversions: 0,
        conversionRate: 0,
        errors: 0,
        errorRate: 0
      };
    });
    
    // 统计用户分配
    for (const [key, variant] of this.userVariants) {
      if (key.includes(experimentId)) {
        results.variants[variant.id].users++;
        results.summary.totalUsers++;
      }
    }
    
    // 统计转化
    this.conversionEvents
      .filter(event => event.experimentId === experimentId)
      .forEach(event => {
        results.variants[event.variantId].conversions += event.value;
        results.summary.totalConversions += event.value;
      });
    
    // 计算转化率
    Object.values(results.variants).forEach(variant => {
      if (variant.users > 0) {
        variant.conversionRate = variant.conversions / variant.users;
      }
    });
    
    if (results.summary.totalUsers > 0) {
      results.summary.overallConversionRate = results.summary.totalConversions / results.summary.totalUsers;
    }
    
    // 发送分析结果到Sentry
    Sentry.withScope(scope => {
      scope.setContext('experimentResults', results);
      Sentry.captureMessage(
        `Experiment analysis: ${experimentId}`,
        'info'
      );
    });
    
    return results;
  }
  
  // 哈希函数
  hashUserId(input) {
    let hash = 0;
    for (let i = 0; i < input.length; i++) {
      const char = input.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // 转换为32位整数
    }
    return Math.abs(hash) / 2147483647;
  }
}

// 功能标志集成
export class SentryFeatureFlagIntegration {
  constructor() {
    this.flags = new Map();
    this.userFlags = new Map();
  }
  
  // 注册功能标志
  registerFlag(flagId, config) {
    this.flags.set(flagId, {
      id: flagId,
      name: config.name,
      description: config.description,
      enabled: config.enabled || false,
      rolloutPercentage: config.rolloutPercentage || 0,
      targetUsers: config.targetUsers || [],
      conditions: config.conditions || []
    });
    
    // 在Sentry中设置功能标志标签
    Sentry.setTag(`flag_${flagId}`, config.enabled ? 'enabled' : 'disabled');
  }
  
  // 检查用户是否启用功能
  isFeatureEnabled(userId, flagId) {
    const flag = this.flags.get(flagId);
    if (!flag) {
      return false;
    }
    
    // 检查全局开关
    if (!flag.enabled) {
      return false;
    }
    
    // 检查目标用户
    if (flag.targetUsers.includes(userId)) {
      this.recordFlagUsage(userId, flagId, true, 'target_user');
      return true;
    }
    
    // 检查条件
    for (const condition of flag.conditions) {
      if (this.evaluateCondition(userId, condition)) {
        this.recordFlagUsage(userId, flagId, true, 'condition_match');
        return true;
      }
    }
    
    // 检查百分比推出
    const hash = this.hashUserId(userId + flagId);
    const enabled = hash < flag.rolloutPercentage / 100;
    
    this.recordFlagUsage(userId, flagId, enabled, 'rollout_percentage');
    return enabled;
  }
  
  recordFlagUsage(userId, flagId, enabled, reason) {
    const usage = {
      userId,
      flagId,
      enabled,
      reason,
      timestamp: Date.now()
    };
    
    // 在Sentry中记录功能标志使用
    Sentry.withScope(scope => {
      scope.setTag('feature_flag', flagId);
      scope.setTag('flag_enabled', enabled);
      scope.setContext('flagUsage', usage);
      
      Sentry.addBreadcrumb({
        category: 'feature-flag',
        message: `Feature flag ${flagId} ${enabled ? 'enabled' : 'disabled'} for user`,
        level: 'info',
        data: usage
      });
    });
  }
  
  hashUserId(input) {
    let hash = 0;
    for (let i = 0; i < input.length; i++) {
      const char = input.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return Math.abs(hash) / 2147483647;
  }
}

// 创建集成实例
export const abTestingIntegration = new SentryABTestingIntegration();
export const featureFlagIntegration = new SentryFeatureFlagIntegration();
```

### 4.3 数据隐私与合规

```javascript
// sentryPrivacy.js
import * as Sentry from '@sentry/browser';

// 数据隐私管理器
export class SentryPrivacyManager {
  constructor() {
    this.sensitiveFields = new Set([
      'password', 'token', 'secret', 'key', 'auth',
      'ssn', 'credit', 'card', 'phone', 'email'
    ]);
    
    this.piiPatterns = [
      /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/g, // Email
      /\b\d{3}-\d{2}-\d{4}\b/g, // SSN
      /\b\d{4}[\s-]?\d{4}[\s-]?\d{4}[\s-]?\d{4}\b/g, // Credit Card
      /\b\d{3}[\s-]?\d{3}[\s-]?\d{4}\b/g // Phone
    ];
    
    this.setupPrivacyFilters();
  }
  
  setupPrivacyFilters() {
    // 设置beforeSend过滤器
    Sentry.getCurrentHub().getClient()?.getOptions().beforeSend = (event, hint) => {
      return this.sanitizeEvent(event, hint);
    };
    
    // 设置beforeBreadcrumb过滤器
    Sentry.getCurrentHub().getClient()?.getOptions().beforeBreadcrumb = (breadcrumb, hint) => {
      return this.sanitizeBreadcrumb(breadcrumb, hint);
    };
  }
  
  sanitizeEvent(event, hint) {
    // 清理异常信息
    if (event.exception) {
      event.exception.values = event.exception.values.map(exception => {
        if (exception.value) {
          exception.value = this.sanitizeText(exception.value);
        }
        
        if (exception.stacktrace?.frames) {
          exception.stacktrace.frames = exception.stacktrace.frames.map(frame => {
            if (frame.vars) {
              frame.vars = this.sanitizeObject(frame.vars);
            }
            return frame;
          });
        }
        
        return exception;
      });
    }
    
    // 清理请求数据
    if (event.request) {
      event.request = this.sanitizeRequest(event.request);
    }
    
    // 清理用户数据
    if (event.user) {
      event.user = this.sanitizeUser(event.user);
    }
    
    // 清理额外数据
    if (event.extra) {
      event.extra = this.sanitizeObject(event.extra);
    }
    
    // 清理上下文
    if (event.contexts) {
      event.contexts = this.sanitizeObject(event.contexts);
    }
    
    return event;
  }
  
  sanitizeBreadcrumb(breadcrumb, hint) {
    // 清理面包屑数据
    if (breadcrumb.data) {
      breadcrumb.data = this.sanitizeObject(breadcrumb.data);
    }
    
    if (breadcrumb.message) {
      breadcrumb.message = this.sanitizeText(breadcrumb.message);
    }
    
    return breadcrumb;
  }
  
  sanitizeObject(obj) {
    if (!obj || typeof obj !== 'object') {
      return obj;
    }
    
    const sanitized = {};
    
    for (const [key, value] of Object.entries(obj)) {
      // 检查键名是否包含敏感信息
      if (this.isSensitiveField(key)) {
        sanitized[key] = '[Filtered]';
        continue;
      }
      
      if (typeof value === 'string') {
        sanitized[key] = this.sanitizeText(value);
      } else if (typeof value === 'object' && value !== null) {
        sanitized[key] = this.sanitizeObject(value);
      } else {
        sanitized[key] = value;
      }
    }
    
    return sanitized;
  }
  
  sanitizeText(text) {
    if (typeof text !== 'string') {
      return text;
    }
    
    let sanitized = text;
    
    // 替换PII模式
    this.piiPatterns.forEach(pattern => {
      sanitized = sanitized.replace(pattern, '[PII Filtered]');
    });
    
    return sanitized;
  }
  
  sanitizeRequest(request) {
    const sanitized = { ...request };
    
    // 清理查询参数
    if (sanitized.query_string) {
      sanitized.query_string = this.sanitizeQueryString(sanitized.query_string);
    }
    
    // 清理请求头
    if (sanitized.headers) {
      sanitized.headers = this.sanitizeHeaders(sanitized.headers);
    }
    
    // 清理请求体
    if (sanitized.data) {
      sanitized.data = this.sanitizeObject(sanitized.data);
    }
    
    return sanitized;
  }
  
  sanitizeUser(user) {
    const sanitized = { ...user };
    
    // 保留用户ID用于错误分组，但清理其他敏感信息
    if (sanitized.email) {
      sanitized.email = this.hashEmail(sanitized.email);
    }
    
    if (sanitized.username && sanitized.username.includes('@')) {
      sanitized.username = this.hashEmail(sanitized.username);
    }
    
    // 清理额外用户数据
    if (sanitized.extra) {
      sanitized.extra = this.sanitizeObject(sanitized.extra);
    }
    
    return sanitized;
  }
  
  isSensitiveField(fieldName) {
    const lowerField = fieldName.toLowerCase();
    return Array.from(this.sensitiveFields).some(sensitive => 
      lowerField.includes(sensitive)
    );
  }
  
  hashEmail(email) {
    // 简单的邮箱哈希，保留域名用于分析
    const [local, domain] = email.split('@');
    if (!domain) return '[Email Filtered]';
    
    const hashedLocal = this.simpleHash(local);
    return `${hashedLocal}@${domain}`;
  }
  
  simpleHash(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return Math.abs(hash).toString(36).substring(0, 8);
  }
  
  sanitizeQueryString(queryString) {
    if (!queryString) return queryString;
    
    const params = new URLSearchParams(queryString);
    const sanitized = new URLSearchParams();
    
    for (const [key, value] of params) {
      if (this.isSensitiveField(key)) {
        sanitized.set(key, '[Filtered]');
      } else {
        sanitized.set(key, this.sanitizeText(value));
      }
    }
    
    return sanitized.toString();
  }
  
  sanitizeHeaders(headers) {
    const sanitized = {};
    const sensitiveHeaders = ['authorization', 'cookie', 'x-api-key', 'x-auth-token'];
    
    for (const [key, value] of Object.entries(headers)) {
      if (sensitiveHeaders.includes(key.toLowerCase())) {
        sanitized[key] = '[Filtered]';
      } else {
        sanitized[key] = value;
      }
    }
    
    return sanitized;
  }
}

// GDPR合规管理器
export class GDPRComplianceManager {
  constructor() {
    this.userConsents = new Map();
    this.dataRetentionPeriod = 30 * 24 * 60 * 60 * 1000; // 30天
  }
  
  // 记录用户同意
  recordUserConsent(userId, consentType, granted) {
    const consent = {
      userId,
      type: consentType,
      granted,
      timestamp: Date.now(),
      ipAddress: this.getClientIP(),
      userAgent: navigator.userAgent
    };
    
    this.userConsents.set(`${userId}_${consentType}`, consent);
    
    // 在Sentry中记录同意状态
    Sentry.setTag(`consent_${consentType}`, granted ? 'granted' : 'denied');
    
    if (granted) {
      Sentry.setContext('gdprConsent', {
        type: consentType,
        timestamp: new Date(consent.timestamp).toISOString()
      });
    }
  }
  
  // 检查用户同意状态
  hasUserConsent(userId, consentType) {
    const consent = this.userConsents.get(`${userId}_${consentType}`);
    return consent && consent.granted;
  }
  
  // 处理数据删除请求
  handleDataDeletionRequest(userId) {
    // 停止为该用户收集数据
    Sentry.configureScope(scope => {
      scope.clear();
      scope.setTag('data_deletion_requested', true);
    });
    
    // 记录删除请求
    Sentry.withScope(scope => {
      scope.setContext('dataDeletion', {
        userId,
        requestTime: new Date().toISOString(),
        status: 'requested'
      });
      
      Sentry.captureMessage(
        `Data deletion requested for user ${userId}`,
        'info'
      );
    });
    
    return {
      status: 'accepted',
      message: 'Data deletion request has been recorded and will be processed.',
      requestId: this.generateRequestId()
    };
  }
  
  // 数据导出请求
  handleDataExportRequest(userId) {
    const userData = this.collectUserData(userId);
    
    Sentry.withScope(scope => {
      scope.setContext('dataExport', {
        userId,
        requestTime: new Date().toISOString(),
        dataSize: JSON.stringify(userData).length
      });
      
      Sentry.captureMessage(
        `Data export requested for user ${userId}`,
        'info'
      );
    });
    
    return {
      status: 'completed',
      data: userData,
      exportId: this.generateRequestId()
    };
  }
  
  collectUserData(userId) {
    // 收集用户相关的所有数据
    return {
      userId,
      consents: Array.from(this.userConsents.values())
        .filter(consent => consent.userId === userId),
      collectionPeriod: {
        start: new Date(Date.now() - this.dataRetentionPeriod).toISOString(),
        end: new Date().toISOString()
      }
    };
  }
  
  generateRequestId() {
    return 'req_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  
  getClientIP() {
    // 注意：在实际应用中，IP地址通常由服务器端提供
    return 'client-ip-not-available';
  }
}

// 创建隐私管理实例
export const privacyManager = new SentryPrivacyManager();
export const gdprManager = new GDPRComplianceManager();
```

## 五、实施最佳实践

### 5.1 团队协作与工作流

```javascript
// sentryWorkflow.js
import * as Sentry from '@sentry/browser';

// 团队协作工作流管理器
export class SentryWorkflowManager {
  constructor() {
    this.issueAssignments = new Map();
    this.escalationRules = [];
    this.notificationChannels = new Map();
  }
  
  // 设置问题分配规则
  setupIssueAssignment() {
    // 基于错误类型的自动分配
    this.addAssignmentRule({
      condition: (issue) => issue.tags?.errorType === 'network',
      assignee: 'backend-team@company.com',
      priority: 'high'
    });
    
    this.addAssignmentRule({
      condition: (issue) => issue.tags?.errorType === 'validation',
      assignee: 'frontend-team@company.com',
      priority: 'medium'
    });
    
    this.addAssignmentRule({
      condition: (issue) => issue.tags?.performance === 'slow_api',
      assignee: 'performance-team@company.com',
      priority: 'high'
    });
  }
  
  addAssignmentRule(rule) {
    this.issueAssignments.set(rule.condition.toString(), rule);
  }
  
  // 设置升级规则
  setupEscalationRules() {
    this.escalationRules = [
      {
        condition: (issue) => issue.level === 'error' && issue.count > 100,
        action: 'escalate_to_senior',
        delay: 30 * 60 * 1000 // 30分钟
      },
      {
        condition: (issue) => issue.level === 'fatal' && issue.count > 10,
        action: 'escalate_to_manager',
        delay: 15 * 60 * 1000 // 15分钟
      },
      {
        condition: (issue) => issue.tags?.performance === 'critical',
        action: 'page_oncall',
        delay: 5 * 60 * 1000 // 5分钟
      }
    ];
  }
  
  // 处理新问题
  handleNewIssue(issue) {
    // 自动分配
    const assignment = this.findAssignment(issue);
    if (assignment) {
      this.assignIssue(issue, assignment);
    }
    
    // 设置升级定时器
    this.scheduleEscalation(issue);
    
    // 发送通知
    this.sendNotification(issue, 'new_issue');
  }
  
  findAssignment(issue) {
    for (const [conditionStr, rule] of this.issueAssignments) {
      if (rule.condition(issue)) {
        return rule;
      }
    }
    return null;
  }
  
  assignIssue(issue, assignment) {
    // 在实际应用中，这里会调用Sentry API
    console.log(`Assigning issue ${issue.id} to ${assignment.assignee}`);
    
    Sentry.withScope(scope => {
      scope.setContext('issueAssignment', {
        issueId: issue.id,
        assignee: assignment.assignee,
        priority: assignment.priority,
        timestamp: new Date().toISOString()
      });
      
      Sentry.captureMessage(
        `Issue auto-assigned to ${assignment.assignee}`,
        'info'
      );
    });
  }
  
  scheduleEscalation(issue) {
    this.escalationRules.forEach(rule => {
      if (rule.condition(issue)) {
        setTimeout(() => {
          this.executeEscalation(issue, rule);
        }, rule.delay);
      }
    });
  }
  
  executeEscalation(issue, rule) {
    console.log(`Escalating issue ${issue.id}: ${rule.action}`);
    
    Sentry.withScope(scope => {
      scope.setTag('escalation', rule.action);
      scope.setContext('escalation', {
        issueId: issue.id,
        action: rule.action,
        timestamp: new Date().toISOString()
      });
      
      Sentry.captureMessage(
        `Issue escalated: ${rule.action}`,
        'warning'
      );
    });
  }
  
  sendNotification(issue, type) {
    const notification = {
      type,
      issue,
      timestamp: new Date().toISOString(),
      channels: this.getNotificationChannels(issue, type)
    };
    
    // 发送到各个通知渠道
    notification.channels.forEach(channel => {
      this.sendToChannel(channel, notification);
    });
  }
  
  getNotificationChannels(issue, type) {
    const channels = [];
    
    // 基于严重程度选择通知渠道
    if (issue.level === 'fatal') {
      channels.push('slack', 'email', 'sms');
    } else if (issue.level === 'error') {
      channels.push('slack', 'email');
    } else {
      channels.push('slack');
    }
    
    return channels;
  }
  
  sendToChannel(channel, notification) {
    // 实际实现中会调用相应的API
    console.log(`Sending notification to ${channel}:`, notification);
  }
}

// 创建工作流管理器实例
export const workflowManager = new SentryWorkflowManager();
workflowManager.setupIssueAssignment();
workflowManager.setupEscalationRules();
```

### 5.2 性能优化建议

```javascript
// sentryOptimization.js
import * as Sentry from '@sentry/browser';

// Sentry性能优化管理器
export class SentryOptimizationManager {
  constructor() {
    this.performanceMetrics = [];
    this.optimizationRules = [];
    this.setupOptimizationRules();
  }
  
  setupOptimizationRules() {
    // 采样率优化
    this.addOptimizationRule({
      name: 'dynamic_sampling',
      condition: () => this.getErrorRate() > 0.01,
      action: () => this.adjustSamplingRate(),
      priority: 'high'
    });
    
    // 数据量优化
    this.addOptimizationRule({
      name: 'data_reduction',
      condition: () => this.getAverageEventSize() > 50000,
      action: () => this.enableDataReduction(),
      priority: 'medium'
    });
    
    // 网络优化
    this.addOptimizationRule({
      name: 'network_optimization',
      condition: () => this.getNetworkLatency() > 1000,
      action: () => this.optimizeNetworkSettings(),
      priority: 'medium'
    });
  }
  
  addOptimizationRule(rule) {
    this.optimizationRules.push(rule);
  }
  
  // 动态调整采样率
  adjustSamplingRate() {
    const currentErrorRate = this.getErrorRate();
    let newSampleRate;
    
    if (currentErrorRate > 0.05) {
      newSampleRate = 0.05; // 高错误率时降低采样
    } else if (currentErrorRate > 0.01) {
      newSampleRate = 0.1;
    } else {
      newSampleRate = 0.2;
    }
    
    // 更新Sentry配置
    const client = Sentry.getCurrentHub().getClient();
    if (client) {
      client.getOptions().tracesSampleRate = newSampleRate;
    }
    
    console.log(`Adjusted sample rate to ${newSampleRate} based on error rate ${currentErrorRate}`);
  }
  
  // 启用数据减少策略
  enableDataReduction() {
    // 减少面包屑数量
    const client = Sentry.getCurrentHub().getClient();
    if (client) {
      client.getOptions().maxBreadcrumbs = 50; // 默认100
    }
    
    // 启用更严格的数据过滤
    this.enableStrictFiltering();
    
    console.log('Enabled data reduction strategies');
  }
  
  enableStrictFiltering() {
    // 更严格的beforeSend过滤
    const originalBeforeSend = Sentry.getCurrentHub().getClient()?.getOptions().beforeSend;
    
    Sentry.getCurrentHub().getClient().getOptions().beforeSend = (event, hint) => {
      // 应用原始过滤器
      if (originalBeforeSend) {
        event = originalBeforeSend(event, hint);
        if (!event) return null;
      }
      
      // 额外的严格过滤
      if (event.extra) {
        // 限制extra数据大小
        const extraStr = JSON.stringify(event.extra);
        if (extraStr.length > 10000) {
          event.extra = { _truncated: true, _originalSize: extraStr.length };
        }
      }
      
      // 限制堆栈跟踪深度
      if (event.exception?.values) {
        event.exception.values.forEach(exception => {
          if (exception.stacktrace?.frames && exception.stacktrace.frames.length > 20) {
            exception.stacktrace.frames = exception.stacktrace.frames.slice(-20);
          }
        });
      }
      
      return event;
    };
  }
  
  // 优化网络设置
  optimizeNetworkSettings() {
    // 启用请求压缩
    const client = Sentry.getCurrentHub().getClient();
    if (client) {
      // 在实际实现中，这里会配置传输选项
      console.log('Optimized network settings for high latency');
    }
  }
  
  // 监控性能指标
  monitorPerformance() {
    setInterval(() => {
      const metrics = {
        timestamp: Date.now(),
        errorRate: this.getErrorRate(),
        eventSize: this.getAverageEventSize(),
        networkLatency: this.getNetworkLatency(),
        memoryUsage: this.getMemoryUsage()
      };
      
      this.performanceMetrics.push(metrics);
      
      // 保持最近1小时的数据
      const oneHourAgo = Date.now() - 60 * 60 * 1000;
      this.performanceMetrics = this.performanceMetrics.filter(m => m.timestamp > oneHourAgo);
      
      // 检查优化规则
      this.checkOptimizationRules();
    }, 5 * 60 * 1000); // 每5分钟检查一次
  }
  
  checkOptimizationRules() {
    this.optimizationRules.forEach(rule => {
      if (rule.condition()) {
        console.log(`Applying optimization rule: ${rule.name}`);
        rule.action();
        
        // 记录优化操作
        Sentry.withScope(scope => {
          scope.setTag('optimization', rule.name);
          scope.setContext('optimization', {
            rule: rule.name,
            priority: rule.priority,
            timestamp: new Date().toISOString()
          });
          
          Sentry.captureMessage(
            `Applied optimization: ${rule.name}`,
            'info'
          );
        });
      }
    });
  }
  
  getErrorRate() {
    // 模拟错误率计算
    const recentMetrics = this.performanceMetrics.slice(-12); // 最近1小时
    if (recentMetrics.length === 0) return 0;
    
    return recentMetrics.reduce((sum, m) => sum + (m.errorRate || 0), 0) / recentMetrics.length;
  }
  
  getAverageEventSize() {
    // 模拟事件大小计算
    return 25000; // 25KB
  }
  
  getNetworkLatency() {
    // 模拟网络延迟计算
    return Math.random() * 2000; // 0-2秒
  }
  
  getMemoryUsage() {
    // 获取内存使用情况
    if (performance.memory) {
      return {
        used: performance.memory.usedJSHeapSize,
        total: performance.memory.totalJSHeapSize,
        limit: performance.memory.jsHeapSizeLimit
      };
    }
    return null;
  }
}

// 创建优化管理器实例
export const optimizationManager = new SentryOptimizationManager();
optimizationManager.monitorPerformance();
```

## 六、总结与未来发展

### 6.1 核心价值总结

1. **全面的错误监控**：从JavaScript错误到网络请求失败，Sentry提供了完整的错误追踪能力
2. **深入的性能分析**：Core Web Vitals监控、资源加载分析、用户体验追踪
3. **智能的问题管理**：自动分组、智能告警、团队协作工作流
4. **数据隐私保护**：GDPR合规、PII过滤、用户同意管理
5. **持续的优化改进**：动态采样、性能优化、成本控制

### 6.2 实施建议

1. **分阶段实施**：从基础错误监控开始，逐步添加性能监控和高级功能
2. **团队培训**：确保团队成员了解Sentry的功能和最佳实践
3. **定期审查**：定期检查监控配置、告警规则和数据质量
4. **成本优化**：合理设置采样率、过滤规则，控制数据量和成本
5. **隐私合规**：确保数据处理符合相关法规要求

### 6.3 未来发展趋势

1. **AI驱动的错误分析**：利用机器学习自动识别错误模式和根本原因
2. **预测性监控**：基于历史数据预测潜在问题
3. **更深入的用户体验分析**：结合用户行为数据提供更全面的体验洞察
4. **自动化修复建议**：AI生成的错误修复建议和代码补丁
5. **跨平台统一监控**：Web、移动端、后端的统一监控视图

Sentry作为前端监控的重要工具，不仅帮助开发者快速发现和解决问题，更重要的是提供了数据驱动的产品优化方向。通过合理配置和使用Sentry，团队可以构建更稳定、更高性能的前端应用，提升用户体验和业务价值。