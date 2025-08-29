# 前端Sentry错误监控与调试最佳实践：构建高可用性Web应用

在现代Web开发中，错误监控和调试是确保应用稳定性和用户体验的关键环节。Sentry作为业界领先的错误监控平台，为前端开发者提供了强大的错误追踪、性能监控和调试工具。本文将深入探讨如何利用Sentry构建完善的前端错误监控体系，从基础配置到高级调试技巧，帮助开发者打造高可用性的Web应用。

## 一、Sentry错误监控基础架构

### 1.1 错误监控系统设计

```javascript
// errorMonitoringSystem.js
import * as Sentry from '@sentry/browser';
import { Integrations } from '@sentry/tracing';

// 错误监控系统核心类
export class ErrorMonitoringSystem {
  constructor(options = {}) {
    this.options = {
      dsn: process.env.REACT_APP_SENTRY_DSN,
      environment: process.env.NODE_ENV,
      release: process.env.REACT_APP_VERSION,
      enableTracing: true,
      enableUserFeedback: true,
      enableSourceMaps: true,
      enablePerformanceMonitoring: true,
      ...options
    };
    
    this.errorQueue = [];
    this.errorStats = {
      totalErrors: 0,
      uniqueErrors: new Set(),
      errorsByType: {},
      errorsByComponent: {},
      errorsByUser: {}
    };
    
    this.setupSentry();
    this.setupErrorHandlers();
    this.setupPerformanceMonitoring();
  }
  
  setupSentry() {
    Sentry.init({
      dsn: this.options.dsn,
      environment: this.options.environment,
      release: this.options.release,
      
      // 集成配置
      integrations: [
        new Integrations.BrowserTracing({
          // 自动追踪路由变化
          routingInstrumentation: Sentry.reactRouterV6Instrumentation(
            React.useEffect,
            useLocation,
            useNavigationType,
            createRoutesFromChildren,
            matchRoutes
          ),
        }),
        new Sentry.Replay({
          // 会话重放配置
          maskAllText: false,
          blockAllMedia: false,
        })
      ],
      
      // 性能监控
      tracesSampleRate: this.options.environment === 'production' ? 0.1 : 1.0,
      
      // 会话重放
      replaysSessionSampleRate: 0.1,
      replaysOnErrorSampleRate: 1.0,
      
      // 错误过滤
      beforeSend: (event, hint) => {
        return this.filterError(event, hint);
      },
      
      // 面包屑过滤
      beforeBreadcrumb: (breadcrumb, hint) => {
        return this.filterBreadcrumb(breadcrumb, hint);
      },
      
      // 初始作用域配置
      initialScope: {
        tags: {
          component: 'frontend',
          platform: this.detectPlatform(),
          browser: this.detectBrowser()
        },
        user: {
          id: this.getUserId(),
          email: this.getUserEmail()
        },
        level: 'info'
      }
    });
    
    console.log('Sentry initialized successfully');
  }
  
  setupErrorHandlers() {
    // 全局错误处理
    window.addEventListener('error', (event) => {
      this.handleGlobalError(event);
    });
    
    // Promise rejection处理
    window.addEventListener('unhandledrejection', (event) => {
      this.handleUnhandledRejection(event);
    });
    
    // React错误边界集成
    this.setupReactErrorBoundary();
  }
  
  setupPerformanceMonitoring() {
    if (this.options.enablePerformanceMonitoring) {
      // 监控长任务
      this.monitorLongTasks();
      
      // 监控内存使用
      this.monitorMemoryUsage();
      
      // 监控网络请求
      this.monitorNetworkRequests();
    }
  }
  
  // 错误过滤逻辑
  filterError(event, hint) {
    const error = hint.originalException;
    
    // 过滤掉已知的无害错误
    const ignoredErrors = [
      'ResizeObserver loop limit exceeded',
      'Script error.',
      'Network request failed',
      'Loading chunk'
    ];
    
    if (ignoredErrors.some(ignored => 
      event.message && event.message.includes(ignored)
    )) {
      return null;
    }
    
    // 过滤掉第三方脚本错误
    if (event.exception) {
      const frames = event.exception.values[0]?.stacktrace?.frames || [];
      const isThirdParty = frames.some(frame => 
        frame.filename && (
          frame.filename.includes('google-analytics') ||
          frame.filename.includes('facebook.net') ||
          frame.filename.includes('doubleclick.net')
        )
      );
      
      if (isThirdParty) {
        return null;
      }
    }
    
    // 添加自定义上下文
    this.enrichErrorContext(event);
    
    // 更新错误统计
    this.updateErrorStats(event);
    
    return event;
  }
  
  // 面包屑过滤
  filterBreadcrumb(breadcrumb, hint) {
    // 过滤敏感信息
    if (breadcrumb.category === 'http' && breadcrumb.data) {
      // 移除敏感的请求头和参数
      delete breadcrumb.data.authorization;
      delete breadcrumb.data.password;
      delete breadcrumb.data.token;
    }
    
    // 限制面包屑数量
    if (breadcrumb.category === 'console' && breadcrumb.level === 'debug') {
      return null;
    }
    
    return breadcrumb;
  }
  
  // 丰富错误上下文
  enrichErrorContext(event) {
    // 添加用户代理信息
    event.tags = {
      ...event.tags,
      userAgent: navigator.userAgent,
      viewport: `${window.innerWidth}x${window.innerHeight}`,
      url: window.location.href,
      timestamp: new Date().toISOString()
    };
    
    // 添加应用状态
    event.extra = {
      ...event.extra,
      appState: this.getAppState(),
      performanceMetrics: this.getPerformanceMetrics(),
      memoryUsage: this.getMemoryUsage(),
      networkStatus: navigator.onLine ? 'online' : 'offline'
    };
    
    // 添加用户交互历史
    event.breadcrumbs = [
      ...event.breadcrumbs,
      ...this.getUserInteractionHistory()
    ];
  }
  
  // 处理全局错误
  handleGlobalError(event) {
    const errorInfo = {
      message: event.message,
      filename: event.filename,
      lineno: event.lineno,
      colno: event.colno,
      error: event.error,
      timestamp: Date.now()
    };
    
    // 添加到错误队列
    this.errorQueue.push(errorInfo);
    
    // 发送到Sentry
    Sentry.captureException(event.error || new Error(event.message), {
      tags: {
        errorType: 'global',
        source: 'window.onerror'
      },
      extra: errorInfo
    });
    
    // 触发自定义错误处理
    this.triggerCustomErrorHandling(errorInfo);
  }
  
  // 处理未捕获的Promise rejection
  handleUnhandledRejection(event) {
    const errorInfo = {
      reason: event.reason,
      promise: event.promise,
      timestamp: Date.now()
    };
    
    this.errorQueue.push(errorInfo);
    
    Sentry.captureException(event.reason, {
      tags: {
        errorType: 'unhandledRejection',
        source: 'promise'
      },
      extra: errorInfo
    });
    
    this.triggerCustomErrorHandling(errorInfo);
  }
  
  // React错误边界设置
  setupReactErrorBoundary() {
    // 这个方法需要与React错误边界组件配合使用
    window.sentryErrorBoundaryHandler = (error, errorInfo) => {
      Sentry.withScope((scope) => {
        scope.setTag('errorBoundary', true);
        scope.setLevel('error');
        scope.setContext('errorInfo', errorInfo);
        scope.setContext('componentStack', {
          componentStack: errorInfo.componentStack
        });
        
        Sentry.captureException(error);
      });
    };
  }
  
  // 监控长任务
  monitorLongTasks() {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          if (entry.duration > 50) { // 超过50ms的任务
            Sentry.addBreadcrumb({
              category: 'performance',
              message: 'Long task detected',
              level: 'warning',
              data: {
                duration: entry.duration,
                startTime: entry.startTime,
                name: entry.name
              }
            });
            
            // 如果任务过长，发送警告
            if (entry.duration > 100) {
              Sentry.captureMessage(
                `Long task detected: ${entry.duration}ms`,
                'warning',
                {
                  tags: {
                    category: 'performance',
                    type: 'longTask'
                  },
                  extra: {
                    duration: entry.duration,
                    startTime: entry.startTime,
                    name: entry.name
                  }
                }
              );
            }
          }
        });
      });
      
      observer.observe({ entryTypes: ['longtask'] });
    }
  }
  
  // 监控内存使用
  monitorMemoryUsage() {
    if ('memory' in performance) {
      setInterval(() => {
        const memory = performance.memory;
        const memoryUsage = {
          used: memory.usedJSHeapSize,
          total: memory.totalJSHeapSize,
          limit: memory.jsHeapSizeLimit,
          percentage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
        };
        
        // 如果内存使用超过80%，发送警告
        if (memoryUsage.percentage > 80) {
          Sentry.captureMessage(
            `High memory usage: ${memoryUsage.percentage.toFixed(2)}%`,
            'warning',
            {
              tags: {
                category: 'performance',
                type: 'memory'
              },
              extra: memoryUsage
            }
          );
        }
      }, 30000); // 每30秒检查一次
    }
  }
  
  // 监控网络请求
  monitorNetworkRequests() {
    // 拦截fetch请求
    const originalFetch = window.fetch;
    window.fetch = async (...args) => {
      const startTime = performance.now();
      const url = args[0];
      
      try {
        const response = await originalFetch(...args);
        const duration = performance.now() - startTime;
        
        // 记录慢请求
        if (duration > 3000) {
          Sentry.addBreadcrumb({
            category: 'http',
            message: 'Slow network request',
            level: 'warning',
            data: {
              url,
              duration,
              status: response.status
            }
          });
        }
        
        // 记录失败请求
        if (!response.ok) {
          Sentry.captureMessage(
            `HTTP request failed: ${response.status} ${url}`,
            'error',
            {
              tags: {
                category: 'http',
                status: response.status
              },
              extra: {
                url,
                duration,
                status: response.status,
                statusText: response.statusText
              }
            }
          );
        }
        
        return response;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        Sentry.captureException(error, {
          tags: {
            category: 'http',
            type: 'networkError'
          },
          extra: {
            url,
            duration
          }
        });
        
        throw error;
      }
    };
  }
  
  // 获取应用状态
  getAppState() {
    return {
      route: window.location.pathname,
      timestamp: Date.now(),
      userAgent: navigator.userAgent,
      language: navigator.language,
      cookieEnabled: navigator.cookieEnabled,
      onLine: navigator.onLine
    };
  }
  
  // 获取性能指标
  getPerformanceMetrics() {
    if ('performance' in window && 'getEntriesByType' in performance) {
      const navigation = performance.getEntriesByType('navigation')[0];
      return {
        loadTime: navigation?.loadEventEnd - navigation?.loadEventStart,
        domContentLoaded: navigation?.domContentLoadedEventEnd - navigation?.domContentLoadedEventStart,
        firstPaint: this.getFirstPaint(),
        firstContentfulPaint: this.getFirstContentfulPaint()
      };
    }
    return {};
  }
  
  // 获取内存使用情况
  getMemoryUsage() {
    if ('memory' in performance) {
      const memory = performance.memory;
      return {
        used: memory.usedJSHeapSize,
        total: memory.totalJSHeapSize,
        limit: memory.jsHeapSizeLimit
      };
    }
    return {};
  }
  
  // 获取用户交互历史
  getUserInteractionHistory() {
    // 这里应该返回最近的用户交互记录
    // 实际实现中可以维护一个交互历史队列
    return [];
  }
  
  // 获取First Paint时间
  getFirstPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const firstPaint = paintEntries.find(entry => entry.name === 'first-paint');
    return firstPaint ? firstPaint.startTime : null;
  }
  
  // 获取First Contentful Paint时间
  getFirstContentfulPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const fcp = paintEntries.find(entry => entry.name === 'first-contentful-paint');
    return fcp ? fcp.startTime : null;
  }
  
  // 检测平台
  detectPlatform() {
    const userAgent = navigator.userAgent.toLowerCase();
    if (userAgent.includes('mobile')) return 'mobile';
    if (userAgent.includes('tablet')) return 'tablet';
    return 'desktop';
  }
  
  // 检测浏览器
  detectBrowser() {
    const userAgent = navigator.userAgent;
    if (userAgent.includes('Chrome')) return 'Chrome';
    if (userAgent.includes('Firefox')) return 'Firefox';
    if (userAgent.includes('Safari')) return 'Safari';
    if (userAgent.includes('Edge')) return 'Edge';
    return 'Unknown';
  }
  
  // 获取用户ID
  getUserId() {
    // 从localStorage、cookie或其他存储中获取用户ID
    return localStorage.getItem('userId') || 'anonymous';
  }
  
  // 获取用户邮箱
  getUserEmail() {
    // 从应用状态或存储中获取用户邮箱
    return localStorage.getItem('userEmail') || null;
  }
  
  // 更新错误统计
  updateErrorStats(event) {
    this.errorStats.totalErrors++;
    
    const errorKey = `${event.exception?.values[0]?.type || 'Unknown'}:${event.message}`;
    this.errorStats.uniqueErrors.add(errorKey);
    
    const errorType = event.exception?.values[0]?.type || 'Unknown';
    this.errorStats.errorsByType[errorType] = (this.errorStats.errorsByType[errorType] || 0) + 1;
    
    const userId = event.user?.id || 'anonymous';
    this.errorStats.errorsByUser[userId] = (this.errorStats.errorsByUser[userId] || 0) + 1;
  }
  
  // 触发自定义错误处理
  triggerCustomErrorHandling(errorInfo) {
    // 发送自定义事件
    window.dispatchEvent(new CustomEvent('appError', {
      detail: errorInfo
    }));
    
    // 可以在这里添加其他自定义逻辑
    // 比如显示用户友好的错误提示
    this.showUserFriendlyError(errorInfo);
  }
  
  // 显示用户友好的错误提示
  showUserFriendlyError(errorInfo) {
    // 这里可以集成toast通知或其他UI组件
    console.warn('An error occurred:', errorInfo);
  }
  
  // 手动报告错误
  reportError(error, context = {}) {
    Sentry.withScope((scope) => {
      // 设置上下文
      Object.entries(context.tags || {}).forEach(([key, value]) => {
        scope.setTag(key, value);
      });
      
      Object.entries(context.extra || {}).forEach(([key, value]) => {
        scope.setExtra(key, value);
      });
      
      if (context.user) {
        scope.setUser(context.user);
      }
      
      if (context.level) {
        scope.setLevel(context.level);
      }
      
      // 发送错误
      Sentry.captureException(error);
    });
  }
  
  // 手动报告消息
  reportMessage(message, level = 'info', context = {}) {
    Sentry.withScope((scope) => {
      Object.entries(context.tags || {}).forEach(([key, value]) => {
        scope.setTag(key, value);
      });
      
      Object.entries(context.extra || {}).forEach(([key, value]) => {
        scope.setExtra(key, value);
      });
      
      Sentry.captureMessage(message, level);
    });
  }
  
  // 设置用户信息
  setUser(user) {
    Sentry.setUser(user);
  }
  
  // 添加面包屑
  addBreadcrumb(breadcrumb) {
    Sentry.addBreadcrumb(breadcrumb);
  }
  
  // 获取错误统计
  getErrorStats() {
    return {
      ...this.errorStats,
      uniqueErrorCount: this.errorStats.uniqueErrors.size,
      errorQueue: this.errorQueue.slice(-10) // 最近10个错误
    };
  }
  
  // 清理错误队列
  clearErrorQueue() {
    this.errorQueue = [];
  }
  
  // 生成错误报告
  generateErrorReport() {
    return {
      timestamp: new Date().toISOString(),
      stats: this.getErrorStats(),
      environment: this.options.environment,
      release: this.options.release,
      userAgent: navigator.userAgent,
      url: window.location.href
    };
  }
}

// 创建全局错误监控实例
export const errorMonitoringSystem = new ErrorMonitoringSystem();

// 导出便捷方法
export const reportError = (error, context) => errorMonitoringSystem.reportError(error, context);
export const reportMessage = (message, level, context) => errorMonitoringSystem.reportMessage(message, level, context);
export const setUser = (user) => errorMonitoringSystem.setUser(user);
export const addBreadcrumb = (breadcrumb) => errorMonitoringSystem.addBreadcrumb(breadcrumb);
```

### 1.2 React错误边界组件

```jsx
// ErrorBoundary.jsx
import React from 'react';
import * as Sentry from '@sentry/browser';
import { errorMonitoringSystem } from './errorMonitoringSystem';

// React错误边界组件
export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      eventId: null
    };
  }
  
  static getDerivedStateFromError(error) {
    // 更新state以显示错误UI
    return {
      hasError: true,
      error
    };
  }
  
  componentDidCatch(error, errorInfo) {
    // 记录错误信息
    this.setState({
      error,
      errorInfo
    });
    
    // 发送错误到Sentry
    const eventId = Sentry.withScope((scope) => {
      scope.setTag('errorBoundary', true);
      scope.setTag('component', this.props.name || 'Unknown');
      scope.setLevel('error');
      
      // 设置错误上下文
      scope.setContext('errorBoundary', {
        componentName: this.props.name,
        componentStack: errorInfo.componentStack,
        errorBoundaryStack: errorInfo.errorBoundaryStack
      });
      
      // 设置组件props（过滤敏感信息）
      scope.setContext('componentProps', this.filterSensitiveProps(this.props));
      
      // 设置组件状态
      scope.setContext('componentState', this.filterSensitiveState(this.state));
      
      return Sentry.captureException(error);
    });
    
    this.setState({ eventId });
    
    // 调用全局错误处理
    if (window.sentryErrorBoundaryHandler) {
      window.sentryErrorBoundaryHandler(error, errorInfo);
    }
    
    // 记录到错误监控系统
    errorMonitoringSystem.addBreadcrumb({
      category: 'error-boundary',
      message: 'React Error Boundary caught an error',
      level: 'error',
      data: {
        componentName: this.props.name,
        errorMessage: error.message,
        errorStack: error.stack
      }
    });
    
    // 触发自定义错误处理
    if (this.props.onError) {
      this.props.onError(error, errorInfo, eventId);
    }
  }
  
  // 过滤敏感的props
  filterSensitiveProps(props) {
    const filtered = { ...props };
    const sensitiveKeys = ['password', 'token', 'apiKey', 'secret'];
    
    sensitiveKeys.forEach(key => {
      if (key in filtered) {
        filtered[key] = '[FILTERED]';
      }
    });
    
    return filtered;
  }
  
  // 过滤敏感的state
  filterSensitiveState(state) {
    const filtered = { ...state };
    const sensitiveKeys = ['password', 'token', 'apiKey', 'secret'];
    
    sensitiveKeys.forEach(key => {
      if (key in filtered) {
        filtered[key] = '[FILTERED]';
      }
    });
    
    return filtered;
  }
  
  // 重试机制
  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      eventId: null
    });
    
    // 记录重试操作
    Sentry.addBreadcrumb({
      category: 'user-action',
      message: 'User retried after error',
      level: 'info',
      data: {
        componentName: this.props.name,
        eventId: this.state.eventId
      }
    });
  }
  
  // 发送用户反馈
  handleFeedback = () => {
    if (this.state.eventId) {
      Sentry.showReportDialog({
        eventId: this.state.eventId,
        user: {
          email: 'user@example.com',
          name: 'User Name'
        }
      });
    }
  }
  
  render() {
    if (this.state.hasError) {
      // 自定义错误UI
      if (this.props.fallback) {
        return this.props.fallback(this.state.error, this.state.errorInfo, this.handleRetry);
      }
      
      // 默认错误UI
      return (
        <div className="error-boundary">
          <div className="error-boundary-content">
            <h2>出现了一个错误</h2>
            <p>很抱歉，应用遇到了一个意外错误。</p>
            
            {process.env.NODE_ENV === 'development' && (
              <details className="error-details">
                <summary>错误详情</summary>
                <pre>{this.state.error && this.state.error.toString()}</pre>
                <pre>{this.state.errorInfo.componentStack}</pre>
              </details>
            )}
            
            <div className="error-actions">
              <button 
                onClick={this.handleRetry}
                className="retry-button"
              >
                重试
              </button>
              
              <button 
                onClick={this.handleFeedback}
                className="feedback-button"
              >
                报告问题
              </button>
              
              <button 
                onClick={() => window.location.reload()}
                className="reload-button"
              >
                刷新页面
              </button>
            </div>
          </div>
        </div>
      );
    }
    
    return this.props.children;
  }
}

// 高阶组件包装器
export function withErrorBoundary(Component, errorBoundaryProps = {}) {
  const WrappedComponent = (props) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );
  
  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;
  
  return WrappedComponent;
}

// Hook版本的错误边界
export function useErrorHandler() {
  return React.useCallback((error, errorInfo) => {
    const eventId = Sentry.withScope((scope) => {
      scope.setTag('errorHandler', 'hook');
      scope.setLevel('error');
      
      if (errorInfo) {
        scope.setContext('errorInfo', errorInfo);
      }
      
      return Sentry.captureException(error);
    });
    
    // 记录到错误监控系统
    errorMonitoringSystem.addBreadcrumb({
      category: 'error-handler',
      message: 'Error caught by useErrorHandler hook',
      level: 'error',
      data: {
        errorMessage: error.message,
        eventId
      }
    });
    
    return eventId;
  }, []);
}

// 异步错误处理Hook
export function useAsyncError() {
  const [, setError] = React.useState();
  
  return React.useCallback((error) => {
    setError(() => {
      throw error;
    });
  }, []);
}
```

## 二、高级错误分析与调试

### 2.1 智能错误分析器

```javascript
// errorAnalyzer.js
import * as Sentry from '@sentry/browser';

// 智能错误分析器
export class ErrorAnalyzer {
  constructor() {
    this.errorPatterns = new Map();
    this.errorTrends = [];
    this.analysisCache = new Map();
    this.setupAnalysis();
  }
  
  setupAnalysis() {
    // 定期分析错误模式
    setInterval(() => {
      this.analyzeErrorPatterns();
      this.analyzeErrorTrends();
      this.generateInsights();
    }, 300000); // 每5分钟分析一次
  }
  
  // 分析错误模式
  analyzeErrorPatterns() {
    // 获取最近的错误数据
    const recentErrors = this.getRecentErrors();
    
    // 按错误类型分组
    const errorsByType = this.groupErrorsByType(recentErrors);
    
    // 分析每种错误类型的模式
    Object.entries(errorsByType).forEach(([errorType, errors]) => {
      const pattern = this.analyzeErrorTypePattern(errorType, errors);
      this.errorPatterns.set(errorType, pattern);
    });
    
    // 识别新的错误模式
    this.identifyNewPatterns(errorsByType);
  }
  
  // 分析错误趋势
  analyzeErrorTrends() {
    const now = Date.now();
    const oneHourAgo = now - 3600000;
    const oneDayAgo = now - 86400000;
    
    const hourlyErrors = this.getErrorsInTimeRange(oneHourAgo, now);
    const dailyErrors = this.getErrorsInTimeRange(oneDayAgo, now);
    
    const trend = {
      timestamp: now,
      hourlyCount: hourlyErrors.length,
      dailyCount: dailyErrors.length,
      hourlyRate: hourlyErrors.length / 60, // 每分钟错误数
      dailyRate: dailyErrors.length / 1440, // 每分钟错误数
      topErrors: this.getTopErrors(hourlyErrors, 5)
    };
    
    this.errorTrends.push(trend);
    
    // 保持最近24小时的趋势数据
    this.errorTrends = this.errorTrends.filter(
      t => now - t.timestamp < 86400000
    );
    
    // 检测异常趋势
    this.detectAnomalousPatterns(trend);
  }
  
  // 生成洞察
  generateInsights() {
    const insights = [];
    
    // 分析错误频率
    const frequencyInsights = this.analyzeErrorFrequency();
    insights.push(...frequencyInsights);
    
    // 分析用户影响
    const userImpactInsights = this.analyzeUserImpact();
    insights.push(...userImpactInsights);
    
    // 分析性能影响
    const performanceInsights = this.analyzePerformanceImpact();
    insights.push(...performanceInsights);
    
    // 发送洞察到Sentry
    if (insights.length > 0) {
      Sentry.addBreadcrumb({
        category: 'error-analysis',
        message: 'Error analysis insights generated',
        level: 'info',
        data: {
          insightCount: insights.length,
          insights: insights.slice(0, 3) // 前3个最重要的洞察
        }
      });
    }
    
    return insights;
  }
  
  // 分析错误类型模式
  analyzeErrorTypePattern(errorType, errors) {
    const pattern = {
      errorType,
      count: errors.length,
      frequency: this.calculateFrequency(errors),
      commonCauses: this.identifyCommonCauses(errors),
      affectedUsers: this.getAffectedUsers(errors),
      timeDistribution: this.analyzeTimeDistribution(errors),
      browserDistribution: this.analyzeBrowserDistribution(errors),
      urlDistribution: this.analyzeUrlDistribution(errors),
      severity: this.calculateSeverity(errors)
    };
    
    return pattern;
  }
  
  // 计算错误频率
  calculateFrequency(errors) {
    if (errors.length === 0) return 0;
    
    const timeSpan = Math.max(...errors.map(e => e.timestamp)) - 
                    Math.min(...errors.map(e => e.timestamp));
    
    return timeSpan > 0 ? errors.length / (timeSpan / 1000 / 60) : 0; // 每分钟错误数
  }
  
  // 识别常见原因
  identifyCommonCauses(errors) {
    const causes = {};
    
    errors.forEach(error => {
      // 分析错误消息中的关键词
      const keywords = this.extractKeywords(error.message);
      keywords.forEach(keyword => {
        causes[keyword] = (causes[keyword] || 0) + 1;
      });
      
      // 分析堆栈跟踪
      if (error.stackTrace) {
        const stackKeywords = this.extractStackKeywords(error.stackTrace);
        stackKeywords.forEach(keyword => {
          causes[keyword] = (causes[keyword] || 0) + 1;
        });
      }
    });
    
    // 返回最常见的原因
    return Object.entries(causes)
      .sort(([,a], [,b]) => b - a)
      .slice(0, 5)
      .map(([cause, count]) => ({ cause, count }));
  }
  
  // 获取受影响的用户
  getAffectedUsers(errors) {
    const users = new Set();
    errors.forEach(error => {
      if (error.userId) {
        users.add(error.userId);
      }
    });
    return users.size;
  }
  
  // 分析时间分布
  analyzeTimeDistribution(errors) {
    const hours = new Array(24).fill(0);
    
    errors.forEach(error => {
      const hour = new Date(error.timestamp).getHours();
      hours[hour]++;
    });
    
    return hours;
  }
  
  // 分析浏览器分布
  analyzeBrowserDistribution(errors) {
    const browsers = {};
    
    errors.forEach(error => {
      const browser = error.browser || 'Unknown';
      browsers[browser] = (browsers[browser] || 0) + 1;
    });
    
    return browsers;
  }
  
  // 分析URL分布
  analyzeUrlDistribution(errors) {
    const urls = {};
    
    errors.forEach(error => {
      const url = error.url || 'Unknown';
      urls[url] = (urls[url] || 0) + 1;
    });
    
    return Object.entries(urls)
      .sort(([,a], [,b]) => b - a)
      .slice(0, 10)
      .reduce((acc, [url, count]) => {
        acc[url] = count;
        return acc;
      }, {});
  }
  
  // 计算严重程度
  calculateSeverity(errors) {
    let severityScore = 0;
    
    errors.forEach(error => {
      // 基于错误类型的基础分数
      let score = this.getBaseScore(error.type);
      
      // 基于影响用户数的调整
      if (error.userId) {
        score += 1;
      }
      
      // 基于错误频率的调整
      if (error.frequency > 10) {
        score += 2;
      }
      
      severityScore += score;
    });
    
    const averageSeverity = severityScore / errors.length;
    
    if (averageSeverity >= 8) return 'critical';
    if (averageSeverity >= 6) return 'high';
    if (averageSeverity >= 4) return 'medium';
    return 'low';
  }
  
  // 获取错误类型的基础分数
  getBaseScore(errorType) {
    const scores = {
      'TypeError': 6,
      'ReferenceError': 7,
      'SyntaxError': 8,
      'NetworkError': 5,
      'SecurityError': 9,
      'ChunkLoadError': 4
    };
    
    return scores[errorType] || 3;
  }
  
  // 提取关键词
  extractKeywords(message) {
    if (!message) return [];
    
    const keywords = [];
    const patterns = [
      /Cannot read property '(\w+)'/,
      /Cannot access before initialization/,
      /(\w+) is not defined/,
      /Failed to fetch/,
      /Network request failed/,
      /Loading chunk (\d+) failed/
    ];
    
    patterns.forEach(pattern => {
      const match = message.match(pattern);
      if (match) {
        keywords.push(match[1] || match[0]);
      }
    });
    
    return keywords;
  }
  
  // 提取堆栈关键词
  extractStackKeywords(stackTrace) {
    if (!stackTrace) return [];
    
    const keywords = [];
    const lines = stackTrace.split('\n');
    
    lines.forEach(line => {
      // 提取函数名
      const functionMatch = line.match(/at (\w+)/);
      if (functionMatch) {
        keywords.push(functionMatch[1]);
      }
      
      // 提取文件名
      const fileMatch = line.match(/\/(\w+\.js)/);
      if (fileMatch) {
        keywords.push(fileMatch[1]);
      }
    });
    
    return keywords;
  }
  
  // 检测异常模式
  detectAnomalousPatterns(currentTrend) {
    if (this.errorTrends.length < 5) return;
    
    const recentTrends = this.errorTrends.slice(-5);
    const averageRate = recentTrends.reduce((sum, trend) => sum + trend.hourlyRate, 0) / recentTrends.length;
    
    // 检测错误率突增
    if (currentTrend.hourlyRate > averageRate * 2) {
      this.reportAnomaly({
        type: 'error_rate_spike',
        severity: 'high',
        currentRate: currentTrend.hourlyRate,
        averageRate,
        increase: ((currentTrend.hourlyRate / averageRate - 1) * 100).toFixed(2) + '%'
      });
    }
    
    // 检测新错误类型
    const currentErrorTypes = new Set(currentTrend.topErrors.map(e => e.type));
    const historicalErrorTypes = new Set();
    
    recentTrends.forEach(trend => {
      trend.topErrors.forEach(error => {
        historicalErrorTypes.add(error.type);
      });
    });
    
    const newErrorTypes = [...currentErrorTypes].filter(type => !historicalErrorTypes.has(type));
    
    if (newErrorTypes.length > 0) {
      this.reportAnomaly({
        type: 'new_error_types',
        severity: 'medium',
        newTypes: newErrorTypes
      });
    }
  }
  
  // 报告异常
  reportAnomaly(anomaly) {
    Sentry.captureMessage(
      `Error pattern anomaly detected: ${anomaly.type}`,
      anomaly.severity === 'high' ? 'error' : 'warning',
      {
        tags: {
          category: 'error-analysis',
          anomalyType: anomaly.type
        },
        extra: anomaly
      }
    );
  }
  
  // 分析错误频率洞察
  analyzeErrorFrequency() {
    const insights = [];
    
    this.errorPatterns.forEach((pattern, errorType) => {
      if (pattern.frequency > 5) { // 每分钟超过5个错误
        insights.push({
          type: 'high_frequency_error',
          severity: 'high',
          errorType,
          frequency: pattern.frequency,
          recommendation: `${errorType} 错误频率过高，建议立即调查`
        });
      }
    });
    
    return insights;
  }
  
  // 分析用户影响洞察
  analyzeUserImpact() {
    const insights = [];
    
    this.errorPatterns.forEach((pattern, errorType) => {
      if (pattern.affectedUsers > 10) {
        insights.push({
          type: 'high_user_impact',
          severity: 'high',
          errorType,
          affectedUsers: pattern.affectedUsers,
          recommendation: `${errorType} 影响了 ${pattern.affectedUsers} 个用户，需要优先处理`
        });
      }
    });
    
    return insights;
  }
  
  // 分析性能影响洞察
  analyzePerformanceImpact() {
    const insights = [];
    
    // 这里可以结合性能监控数据
    // 分析错误对性能的影响
    
    return insights;
  }
  
  // 获取最近的错误（模拟数据）
  getRecentErrors() {
    // 实际实现中应该从Sentry API或本地存储获取
    return [];
  }
  
  // 按类型分组错误
  groupErrorsByType(errors) {
    return errors.reduce((groups, error) => {
      const type = error.type || 'Unknown';
      if (!groups[type]) {
        groups[type] = [];
      }
      groups[type].push(error);
      return groups;
    }, {});
  }
  
  // 识别新模式
  identifyNewPatterns(errorsByType) {
    Object.entries(errorsByType).forEach(([errorType, errors]) => {
      if (!this.errorPatterns.has(errorType)) {
        // 发现新的错误类型
        Sentry.addBreadcrumb({
          category: 'error-analysis',
          message: 'New error pattern identified',
          level: 'info',
          data: {
            errorType,
            errorCount: errors.length
          }
        });
      }
    });
  }
  
  // 获取时间范围内的错误
  getErrorsInTimeRange(startTime, endTime) {
    // 实际实现中应该从错误存储中查询
    return [];
  }
  
  // 获取Top错误
  getTopErrors(errors, limit) {
    const errorCounts = {};
    
    errors.forEach(error => {
      const key = `${error.type}:${error.message}`;
      errorCounts[key] = (errorCounts[key] || 0) + 1;
    });
    
    return Object.entries(errorCounts)
      .sort(([,a], [,b]) => b - a)
      .slice(0, limit)
      .map(([key, count]) => {
        const [type, message] = key.split(':');
        return { type, message, count };
      });
  }
  
  // 获取分析报告
  getAnalysisReport() {
    return {
      timestamp: new Date().toISOString(),
      patterns: Object.fromEntries(this.errorPatterns),
      trends: this.errorTrends.slice(-24), // 最近24小时
      insights: this.generateInsights()
    };
  }
}

// 创建错误分析器实例
export const errorAnalyzer = new ErrorAnalyzer();
```

## 三、用户反馈与问题追踪

### 3.1 用户反馈系统

```javascript
// userFeedbackSystem.js
import * as Sentry from '@sentry/browser';

// 用户反馈系统
export class UserFeedbackSystem {
  constructor(options = {}) {
    this.options = {
      enableAutoFeedback: true,
      enableManualFeedback: true,
      feedbackThreshold: 3, // 错误次数阈值
      ...options
    };
    
    this.userSessions = new Map();
    this.feedbackQueue = [];
    this.setupFeedbackSystem();
  }
  
  setupFeedbackSystem() {
    // 监听错误事件
    window.addEventListener('appError', (event) => {
      this.handleErrorForFeedback(event.detail);
    });
    
    // 设置用户会话跟踪
    this.initializeUserSession();
    
    // 定期处理反馈队列
    setInterval(() => {
      this.processFeedbackQueue();
    }, 30000); // 每30秒处理一次
  }
  
  // 初始化用户会话
  initializeUserSession() {
    const sessionId = this.generateSessionId();
    const userId = this.getUserId();
    
    this.userSessions.set(userId, {
      sessionId,
      startTime: Date.now(),
      errorCount: 0,
      lastErrorTime: null,
      feedbackGiven: false,
      userAgent: navigator.userAgent,
      url: window.location.href
    });
  }
  
  // 处理错误以触发反馈
  handleErrorForFeedback(errorInfo) {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId);
    
    if (session) {
      session.errorCount++;
      session.lastErrorTime = Date.now();
      
      // 检查是否应该请求反馈
      if (this.shouldRequestFeedback(session)) {
        this.requestUserFeedback(errorInfo, session);
      }
    }
  }
  
  // 判断是否应该请求反馈
  shouldRequestFeedback(session) {
    return (
      this.options.enableAutoFeedback &&
      session.errorCount >= this.options.feedbackThreshold &&
      !session.feedbackGiven &&
      Date.now() - session.lastErrorTime < 60000 // 最近1分钟内的错误
    );
  }
  
  // 请求用户反馈
  requestUserFeedback(errorInfo, session) {
    // 创建反馈UI
    const feedbackModal = this.createFeedbackModal(errorInfo);
    document.body.appendChild(feedbackModal);
    
    // 标记已请求反馈
    session.feedbackGiven = true;
    
    // 记录反馈请求
    Sentry.addBreadcrumb({
      category: 'user-feedback',
      message: 'Feedback requested from user',
      level: 'info',
      data: {
        errorCount: session.errorCount,
        sessionId: session.sessionId
      }
    });
  }
  
  // 创建反馈模态框
  createFeedbackModal(errorInfo) {
    const modal = document.createElement('div');
    modal.className = 'feedback-modal';
    modal.innerHTML = `
      <div class="feedback-modal-overlay">
        <div class="feedback-modal-content">
          <div class="feedback-header">
            <h3>帮助我们改进</h3>
            <button class="feedback-close" onclick="this.closest('.feedback-modal').remove()">&times;</button>
          </div>
          
          <div class="feedback-body">
            <p>我们注意到您遇到了一些问题。您的反馈对我们很重要！</p>
            
            <div class="feedback-form">
              <div class="feedback-rating">
                <label>这个问题对您的影响程度：</label>
                <div class="rating-buttons">
                  <button class="rating-btn" data-rating="1">轻微</button>
                  <button class="rating-btn" data-rating="2">一般</button>
                  <button class="rating-btn" data-rating="3">严重</button>
                  <button class="rating-btn" data-rating="4">非常严重</button>
                </div>
              </div>
              
              <div class="feedback-description">
                <label for="feedback-text">请描述您遇到的问题：</label>
                <textarea 
                  id="feedback-text" 
                  placeholder="请详细描述您遇到的问题，这将帮助我们更好地解决它..."
                  rows="4"
                ></textarea>
              </div>
              
              <div class="feedback-contact">
                <label>
                  <input type="checkbox" id="feedback-contact-me"> 
                  如果需要，可以联系我获取更多信息
                </label>
                <input 
                  type="email" 
                  id="feedback-email" 
                  placeholder="您的邮箱地址（可选）"
                  style="display: none;"
                >
              </div>
            </div>
          </div>
          
          <div class="feedback-footer">
            <button class="feedback-cancel" onclick="this.closest('.feedback-modal').remove()">
              跳过
            </button>
            <button class="feedback-submit" onclick="window.userFeedbackSystem.submitFeedback(this)">
              提交反馈
            </button>
          </div>
        </div>
      </div>
    `;
    
    // 添加样式
    this.addFeedbackStyles();
    
    // 设置事件监听
    this.setupFeedbackEvents(modal);
    
    return modal;
  }
  
  // 添加反馈样式
  addFeedbackStyles() {
    if (document.getElementById('feedback-styles')) return;
    
    const style = document.createElement('style');
    style.id = 'feedback-styles';
    style.textContent = `
      .feedback-modal {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 10000;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }
      
      .feedback-modal-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 20px;
      }
      
      .feedback-modal-content {
        background: white;
        border-radius: 8px;
        max-width: 500px;
        width: 100%;
        max-height: 80vh;
        overflow-y: auto;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
      }
      
      .feedback-header {
        padding: 20px 20px 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #eee;
        margin-bottom: 20px;
      }
      
      .feedback-header h3 {
        margin: 0;
        color: #333;
        font-size: 18px;
      }
      
      .feedback-close {
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        color: #999;
        padding: 0;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      
      .feedback-body {
        padding: 0 20px;
      }
      
      .feedback-body p {
        color: #666;
        line-height: 1.5;
        margin-bottom: 20px;
      }
      
      .feedback-rating {
        margin-bottom: 20px;
      }
      
      .feedback-rating label {
        display: block;
        margin-bottom: 10px;
        font-weight: 500;
        color: #333;
      }
      
      .rating-buttons {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
      
      .rating-btn {
        padding: 8px 16px;
        border: 2px solid #ddd;
        background: white;
        border-radius: 20px;
        cursor: pointer;
        font-size: 14px;
        transition: all 0.2s;
      }
      
      .rating-btn:hover {
        border-color: #007bff;
        color: #007bff;
      }
      
      .rating-btn.selected {
        background: #007bff;
        border-color: #007bff;
        color: white;
      }
      
      .feedback-description {
        margin-bottom: 20px;
      }
      
      .feedback-description label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        color: #333;
      }
      
      .feedback-description textarea {
        width: 100%;
        padding: 12px;
        border: 2px solid #ddd;
        border-radius: 6px;
        font-family: inherit;
        font-size: 14px;
        resize: vertical;
        min-height: 80px;
      }
      
      .feedback-description textarea:focus {
        outline: none;
        border-color: #007bff;
      }
      
      .feedback-contact {
        margin-bottom: 20px;
      }
      
      .feedback-contact label {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        color: #666;
        font-size: 14px;
      }
      
      .feedback-contact input[type="email"] {
        width: 100%;
        padding: 8px 12px;
        border: 2px solid #ddd;
        border-radius: 4px;
        margin-top: 10px;
        font-size: 14px;
      }
      
      .feedback-footer {
        padding: 20px;
        border-top: 1px solid #eee;
        display: flex;
        justify-content: flex-end;
        gap: 12px;
      }
      
      .feedback-cancel,
      .feedback-submit {
        padding: 10px 20px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.2s;
      }
      
      .feedback-cancel {
        background: #f8f9fa;
        color: #666;
      }
      
      .feedback-cancel:hover {
        background: #e9ecef;
      }
      
      .feedback-submit {
        background: #007bff;
        color: white;
      }
      
      .feedback-submit:hover {
        background: #0056b3;
      }
      
      .feedback-submit:disabled {
        background: #ccc;
        cursor: not-allowed;
      }
    `;
    
    document.head.appendChild(style);
  }
  
  // 设置反馈事件
  setupFeedbackEvents(modal) {
    // 评分按钮事件
    const ratingButtons = modal.querySelectorAll('.rating-btn');
    ratingButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        ratingButtons.forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');
      });
    });
    
    // 联系方式复选框事件
    const contactCheckbox = modal.querySelector('#feedback-contact-me');
    const emailInput = modal.querySelector('#feedback-email');
    
    contactCheckbox.addEventListener('change', () => {
      emailInput.style.display = contactCheckbox.checked ? 'block' : 'none';
    });
    
    // 点击遮罩关闭
    modal.querySelector('.feedback-modal-overlay').addEventListener('click', (e) => {
      if (e.target === e.currentTarget) {
        modal.remove();
      }
    });
  }
  
  // 提交反馈
  submitFeedback(submitButton) {
    const modal = submitButton.closest('.feedback-modal');
    const selectedRating = modal.querySelector('.rating-btn.selected');
    const feedbackText = modal.querySelector('#feedback-text').value;
    const contactMe = modal.querySelector('#feedback-contact-me').checked;
    const email = modal.querySelector('#feedback-email').value;
    
    // 验证必填字段
    if (!selectedRating) {
      alert('请选择影响程度');
      return;
    }
    
    if (!feedbackText.trim()) {
      alert('请描述您遇到的问题');
      return;
    }
    
    // 禁用提交按钮
    submitButton.disabled = true;
    submitButton.textContent = '提交中...';
    
    // 构建反馈数据
    const feedbackData = {
      rating: parseInt(selectedRating.dataset.rating),
      description: feedbackText.trim(),
      contactMe,
      email: contactMe ? email : null,
      timestamp: Date.now(),
      sessionId: this.getCurrentSessionId(),
      userId: this.getUserId(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      errorContext: this.getErrorContext()
    };
    
    // 发送反馈到Sentry
    this.sendFeedbackToSentry(feedbackData)
      .then(() => {
        // 显示成功消息
        this.showFeedbackSuccess(modal);
        
        // 记录反馈提交
        Sentry.addBreadcrumb({
          category: 'user-feedback',
          message: 'User feedback submitted',
          level: 'info',
          data: {
            rating: feedbackData.rating,
            hasDescription: !!feedbackData.description,
            contactRequested: feedbackData.contactMe
          }
        });
      })
      .catch((error) => {
        // 显示错误消息
        this.showFeedbackError(modal, error);
        
        // 重新启用提交按钮
        submitButton.disabled = false;
        submitButton.textContent = '提交反馈';
      });
  }
  
  // 发送反馈到Sentry
  async sendFeedbackToSentry(feedbackData) {
    // 使用Sentry的用户反馈API
    const eventId = Sentry.captureMessage(
      'User feedback received',
      'info',
      {
        tags: {
          category: 'user-feedback',
          rating: feedbackData.rating
        },
        extra: feedbackData
      }
    );
    
    // 如果有Sentry的用户反馈功能，也可以使用
    if (feedbackData.contactMe && feedbackData.email) {
      Sentry.captureUserFeedback({
        event_id: eventId,
        name: 'User',
        email: feedbackData.email,
        comments: feedbackData.description
      });
    }
    
    // 添加到本地反馈队列
    this.feedbackQueue.push(feedbackData);
    
    return eventId;
  }
  
  // 显示反馈成功消息
  showFeedbackSuccess(modal) {
    const content = modal.querySelector('.feedback-modal-content');
    content.innerHTML = `
      <div class="feedback-success">
        <div style="text-align: center; padding: 40px 20px;">
          <div style="font-size: 48px; color: #28a745; margin-bottom: 20px;">✓</div>
          <h3 style="color: #333; margin-bottom: 10px;">感谢您的反馈！</h3>
          <p style="color: #666; margin-bottom: 20px;">您的反馈对我们很重要，我们会尽快处理相关问题。</p>
          <button 
            onclick="this.closest('.feedback-modal').remove()" 
            style="
              background: #007bff;
              color: white;
              border: none;
              padding: 10px 20px;
              border-radius: 6px;
              cursor: pointer;
              font-size: 14px;
            "
          >
            关闭
          </button>
        </div>
      </div>
    `;
    
    // 3秒后自动关闭
    setTimeout(() => {
      modal.remove();
    }, 3000);
  }
  
  // 显示反馈错误消息
  showFeedbackError(modal, error) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'feedback-error';
    errorDiv.style.cssText = `
      background: #f8d7da;
      color: #721c24;
      padding: 10px;
      border-radius: 4px;
      margin: 10px 20px;
      font-size: 14px;
    `;
    errorDiv.textContent = '提交反馈时出现错误，请稍后重试。';
    
    const body = modal.querySelector('.feedback-body');
    body.insertBefore(errorDiv, body.firstChild);
    
    // 5秒后移除错误消息
    setTimeout(() => {
      errorDiv.remove();
    }, 5000);
  }
  
  // 手动触发反馈
  showManualFeedback(context = {}) {
    if (!this.options.enableManualFeedback) return;
    
    const errorInfo = {
      type: 'manual',
      message: 'User manually requested feedback',
      ...context
    };
    
    const session = this.userSessions.get(this.getUserId()) || {};
    this.requestUserFeedback(errorInfo, session);
  }
  
  // 处理反馈队列
  processFeedbackQueue() {
    if (this.feedbackQueue.length === 0) return;
    
    // 分析反馈趋势
    const recentFeedback = this.feedbackQueue.filter(
      feedback => Date.now() - feedback.timestamp < 3600000 // 最近1小时
    );
    
    if (recentFeedback.length > 0) {
      const averageRating = recentFeedback.reduce(
        (sum, feedback) => sum + feedback.rating, 0
      ) / recentFeedback.length;
      
      // 如果平均评分过低，发送警告
      if (averageRating >= 3) {
        Sentry.captureMessage(
          `Low user satisfaction detected: average rating ${averageRating.toFixed(2)}`,
          'warning',
          {
            tags: {
              category: 'user-satisfaction',
              severity: 'high'
            },
            extra: {
              averageRating,
              feedbackCount: recentFeedback.length,
              timeRange: '1 hour'
            }
          }
        );
      }
    }
    
    // 清理旧的反馈数据
    this.feedbackQueue = this.feedbackQueue.filter(
      feedback => Date.now() - feedback.timestamp < 86400000 // 保留24小时
    );
  }
  
  // 获取当前会话ID
  getCurrentSessionId() {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId);
    return session ? session.sessionId : null;
  }
  
  // 获取用户ID
  getUserId() {
    return localStorage.getItem('userId') || 'anonymous';
  }
  
  // 生成会话ID
  generateSessionId() {
    return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  
  // 获取错误上下文
  getErrorContext() {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId);
    
    return {
      sessionId: session?.sessionId,
      errorCount: session?.errorCount || 0,
      lastErrorTime: session?.lastErrorTime,
      sessionDuration: session ? Date.now() - session.startTime : 0
    };
  }
  
  // 获取反馈统计
  getFeedbackStats() {
    const now = Date.now();
    const oneDay = 86400000;
    
    const recentFeedback = this.feedbackQueue.filter(
      feedback => now - feedback.timestamp < oneDay
    );
    
    const ratingDistribution = recentFeedback.reduce((dist, feedback) => {
      dist[feedback.rating] = (dist[feedback.rating] || 0) + 1;
      return dist;
    }, {});
    
    const averageRating = recentFeedback.length > 0 ?
      recentFeedback.reduce((sum, feedback) => sum + feedback.rating, 0) / recentFeedback.length :
      0;
    
    return {
      totalFeedback: recentFeedback.length,
      averageRating,
      ratingDistribution,
      contactRequests: recentFeedback.filter(f => f.contactMe).length
    };
  }
}

// 创建全局用户反馈系统实例
export const userFeedbackSystem = new UserFeedbackSystem();

// 将实例挂载到window对象，供HTML中的事件使用
window.userFeedbackSystem = userFeedbackSystem;

// 导出便捷方法
export const showFeedback = (context) => userFeedbackSystem.showManualFeedback(context);
export const getFeedbackStats = () => userFeedbackSystem.getFeedbackStats();
```

### 3.2 问题追踪与工作流集成

```javascript
// issueTrackingIntegration.js
import * as Sentry from '@sentry/browser';

// 问题追踪集成系统
export class IssueTrackingIntegration {
  constructor(options = {}) {
    this.options = {
      jiraUrl: process.env.REACT_APP_JIRA_URL,
      jiraToken: process.env.REACT_APP_JIRA_TOKEN,
      githubToken: process.env.REACT_APP_GITHUB_TOKEN,
      slackWebhook: process.env.REACT_APP_SLACK_WEBHOOK,
      enableAutoIssueCreation: true,
      issueThreshold: 5, // 错误次数阈值
      ...options
    };
    
    this.issueCache = new Map();
    this.setupIntegration();
  }
  
  setupIntegration() {
    // 监听Sentry错误事件
    Sentry.addGlobalEventProcessor((event) => {
      this.processEventForIssueTracking(event);
      return event;
    });
    
    // 定期检查问题状态
    setInterval(() => {
      this.checkIssueStatus();
    }, 300000); // 每5分钟检查一次
  }
  
  // 处理事件以进行问题追踪
  processEventForIssueTracking(event) {
    if (!this.options.enableAutoIssueCreation) return;
    
    const issueKey = this.generateIssueKey(event);
    const existingIssue = this.issueCache.get(issueKey);
    
    if (existingIssue) {
      // 更新现有问题
      existingIssue.count++;
      existingIssue.lastOccurrence = Date.now();
      existingIssue.events.push(event);
      
      // 检查是否需要升级问题
      if (existingIssue.count >= this.options.issueThreshold) {
        this.escalateIssue(existingIssue);
      }
    } else {
      // 创建新问题
      const newIssue = {
        key: issueKey,
        title: this.generateIssueTitle(event),
        description: this.generateIssueDescription(event),
        count: 1,
        firstOccurrence: Date.now(),
        lastOccurrence: Date.now(),
        events: [event],
        status: 'new',
        severity: this.calculateIssueSeverity(event),
        tags: this.extractIssueTags(event)
      };
      
      this.issueCache.set(issueKey, newIssue);
      
      // 如果是高严重性问题，立即创建工单
      if (newIssue.severity === 'critical' || newIssue.severity === 'high') {
        this.createIssueTicket(newIssue);
      }
    }
  }
  
  // 生成问题键
  generateIssueKey(event) {
    const errorType = event.exception?.values[0]?.type || 'Unknown';
    const errorMessage = event.message || event.exception?.values[0]?.value || 'Unknown';
    const stackTrace = event.exception?.values[0]?.stacktrace?.frames?.[0]?.filename || 'Unknown';
    
    // 创建基于错误特征的唯一键
    const key = `${errorType}:${errorMessage}:${stackTrace}`;
    return btoa(key).replace(/[^a-zA-Z0-9]/g, '').substr(0, 20);
  }
  
  // 生成问题标题
  generateIssueTitle(event) {
    const errorType = event.exception?.values[0]?.type || 'Error';
    const errorMessage = event.message || event.exception?.values[0]?.value || 'Unknown error';
    
    return `[${errorType}] ${errorMessage.substring(0, 100)}`;
  }
  
  // 生成问题描述
  generateIssueDescription(event) {
    const sections = [];
    
    // 基本信息
    sections.push('## 错误信息');
    sections.push(`**错误类型**: ${event.exception?.values[0]?.type || 'Unknown'}`);
    sections.push(`**错误消息**: ${event.message || event.exception?.values[0]?.value || 'Unknown'}`);
    sections.push(`**发生时间**: ${new Date(event.timestamp * 1000).toISOString()}`);
    sections.push('');
    
    // 用户信息
    if (event.user) {
      sections.push('## 用户信息');
      sections.push(`**用户ID**: ${event.user.id || 'Unknown'}`);
      sections.push(`**用户邮箱**: ${event.user.email || 'Unknown'}`);
      sections.push('');
    }
    
    // 环境信息
    sections.push('## 环境信息');
    sections.push(`**环境**: ${event.environment || 'Unknown'}`);
    sections.push(`**版本**: ${event.release || 'Unknown'}`);
    sections.push(`**URL**: ${event.request?.url || event.tags?.url || 'Unknown'}`);
    sections.push(`**用户代理**: ${event.request?.headers?.['User-Agent'] || event.tags?.userAgent || 'Unknown'}`);
    sections.push('');
    
    // 堆栈跟踪
    if (event.exception?.values[0]?.stacktrace) {
      sections.push('## 堆栈跟踪');
      sections.push('```');
      const frames = event.exception.values[0].stacktrace.frames || [];
      frames.forEach(frame => {
        sections.push(`  at ${frame.function || 'anonymous'} (${frame.filename}:${frame.lineno}:${frame.colno})`);
      });
      sections.push('```');
      sections.push('');
    }
    
    // 面包屑
    if (event.breadcrumbs && event.breadcrumbs.length > 0) {
      sections.push('## 用户操作历史');
      event.breadcrumbs.slice(-10).forEach(breadcrumb => {
        const time = new Date(breadcrumb.timestamp * 1000).toISOString();
        sections.push(`- **${time}** [${breadcrumb.category}] ${breadcrumb.message}`);
      });
      sections.push('');
    }
    
    // 额外信息
    if (event.extra && Object.keys(event.extra).length > 0) {
      sections.push('## 额外信息');
      Object.entries(event.extra).forEach(([key, value]) => {
        sections.push(`**${key}**: ${JSON.stringify(value, null, 2)}`);
      });
    }
    
    return sections.join('\n');
  }
  
  // 计算问题严重性
  calculateIssueSeverity(event) {
    let score = 0;
    
    // 基于错误类型的分数
    const errorType = event.exception?.values[0]?.type;
    const typeScores = {
      'SecurityError': 10,
      'SyntaxError': 8,
      'ReferenceError': 7,
      'TypeError': 6,
      'NetworkError': 4,
      'ChunkLoadError': 3
    };
    score += typeScores[errorType] || 2;
    
    // 基于环境的分数
    if (event.environment === 'production') {
      score += 3;
    }
    
    // 基于用户影响的分数
    if (event.user?.id) {
      score += 2;
    }
    
    // 基于标签的分数
    if (event.tags?.critical) {
      score += 5;
    }
    
    if (score >= 10) return 'critical';
    if (score >= 7) return 'high';
    if (score >= 4) return 'medium';
    return 'low';
  }
  
  // 提取问题标签
  extractIssueTags(event) {
    const tags = [];
    
    // 添加错误类型标签
    if (event.exception?.values[0]?.type) {
      tags.push(event.exception.values[0].type.toLowerCase());
    }
    
    // 添加环境标签
    if (event.environment) {
      tags.push(`env:${event.environment}`);
    }
    
    // 添加浏览器标签
    if (event.tags?.browser) {
      tags.push(`browser:${event.tags.browser.toLowerCase()}`);
    }
    
    // 添加平台标签
    if (event.tags?.platform) {
      tags.push(`platform:${event.tags.platform.toLowerCase()}`);
    }
    
    return tags;
  }
  
  // 升级问题
  escalateIssue(issue) {
    if (issue.status === 'escalated') return;
    
    issue.status = 'escalated';
    issue.escalatedAt = Date.now();
    
    // 创建工单
    this.createIssueTicket(issue);
    
    // 发送通知
    this.sendEscalationNotification(issue);
    
    // 记录升级事件
    Sentry.addBreadcrumb({
      category: 'issue-tracking',
      message: 'Issue escalated',
      level: 'warning',
      data: {
        issueKey: issue.key,
        count: issue.count,
        severity: issue.severity
      }
    });
  }
  
  // 创建工单
  async createIssueTicket(issue) {
    try {
      // 创建JIRA工单
      if (this.options.jiraUrl && this.options.jiraToken) {
        await this.createJiraIssue(issue);
      }
      
      // 创建GitHub Issue
      if (this.options.githubToken) {
        await this.createGitHubIssue(issue);
      }
      
      issue.ticketCreated = true;
      issue.ticketCreatedAt = Date.now();
      
    } catch (error) {
      console.error('Failed to create issue ticket:', error);
      
      Sentry.captureException(error, {
        tags: {
          category: 'issue-tracking',
          operation: 'create-ticket'
        },
        extra: {
          issueKey: issue.key,
          issueSeverity: issue.severity
        }
      });
    }
  }
  
  // 创建JIRA工单
  async createJiraIssue(issue) {
    const jiraIssue = {
      fields: {
        project: {
          key: 'FRONTEND' // 项目键
        },
        summary: issue.title,
        description: issue.description,
        issuetype: {
          name: 'Bug'
        },
        priority: {
          name: this.mapSeverityToPriority(issue.severity)
        },
        labels: issue.tags
      }
    };
    
    const response = await fetch(`${this.options.jiraUrl}/rest/api/2/issue`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.options.jiraToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(jiraIssue)
    });
    
    if (!response.ok) {
      throw new Error(`JIRA API error: ${response.status}`);
    }
    
    const result = await response.json();
    issue.jiraKey = result.key;
    
    return result;
  }
  
  // 创建GitHub Issue
  async createGitHubIssue(issue) {
    const githubIssue = {
      title: issue.title,
      body: issue.description,
      labels: ['bug', `severity:${issue.severity}`, ...issue.tags]
    };
    
    const response = await fetch(`https://api.github.com/repos/your-org/your-repo/issues`, {
      method: 'POST',
      headers: {
        'Authorization': `token ${this.options.githubToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(githubIssue)
    });
    
    if (!response.ok) {
      throw new Error(`GitHub API error: ${response.status}`);
    }
    
    const result = await response.json();
    issue.githubNumber = result.number;
    
    return result;
  }
  
  // 发送升级通知
  async sendEscalationNotification(issue) {
    if (this.options.slackWebhook) {
      await this.sendSlackNotification(issue);
    }
  }
  
  // 发送Slack通知
  async sendSlackNotification(issue) {
    const message = {
      text: `🚨 高优先级错误需要关注`,
      attachments: [
        {
          color: this.getSeverityColor(issue.severity),
          fields: [
            {
              title: '错误标题',
              value: issue.title,
              short: false
            },
            {
              title: '严重程度',
              value: issue.severity.toUpperCase(),
              short: true
            },
            {
              title: '发生次数',
              value: issue.count.toString(),
              short: true
            },
            {
              title: '首次发生',
              value: new Date(issue.firstOccurrence).toISOString(),
              short: true
            },
            {
              title: '最后发生',
              value: new Date(issue.lastOccurrence).toISOString(),
              short: true
            }
          ],
          actions: [
            {
              type: 'button',
              text: '查看Sentry',
              url: `https://sentry.io/organizations/your-org/issues/`
            }
          ]
        }
      ]
    };
    
    if (issue.jiraKey) {
      message.attachments[0].actions.push({
        type: 'button',
        text: '查看JIRA',
        url: `${this.options.jiraUrl}/browse/${issue.jiraKey}`
      });
    }
    
    if (issue.githubNumber) {
      message.attachments[0].actions.push({
        type: 'button',
        text: '查看GitHub',
        url: `https://github.com/your-org/your-repo/issues/${issue.githubNumber}`
      });
    }
    
    await fetch(this.options.slackWebhook, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(message)
    });
  }
  
  // 映射严重程度到优先级
  mapSeverityToPriority(severity) {
    const mapping = {
      'critical': 'Highest',
      'high': 'High',
      'medium': 'Medium',
      'low': 'Low'
    };
    return mapping[severity] || 'Medium';
  }
  
  // 获取严重程度颜色
  getSeverityColor(severity) {
    const colors = {
      'critical': '#ff0000',
      'high': '#ff6600',
      'medium': '#ffcc00',
      'low': '#00cc00'
    };
    return colors[severity] || '#cccccc';
  }
  
  // 检查问题状态
  checkIssueStatus() {
    this.issueCache.forEach(async (issue, key) => {
      // 检查是否有新的事件
      const timeSinceLastOccurrence = Date.now() - issue.lastOccurrence;
      
      // 如果24小时内没有新的错误，标记为可能已解决
      if (timeSinceLastOccurrence > 86400000 && issue.status !== 'resolved') {
        issue.status = 'possibly_resolved';
        issue.possiblyResolvedAt = Date.now();
        
        // 发送可能已解决的通知
        await this.sendResolutionNotification(issue);
      }
      
      // 清理超过7天的已解决问题
      if (issue.status === 'resolved' && timeSinceLastOccurrence > 604800000) {
        this.issueCache.delete(key);
      }
    });
  }
  
  // 发送解决通知
  async sendResolutionNotification(issue) {
    if (this.options.slackWebhook) {
      const message = {
        text: `✅ 错误可能已解决`,
        attachments: [
          {
            color: '#00cc00',
            fields: [
              {
                title: '错误标题',
                value: issue.title,
                short: false
              },
              {
                title: '最后发生时间',
                value: new Date(issue.lastOccurrence).toISOString(),
                short: true
              },
              {
                title: '总发生次数',
                value: issue.count.toString(),
                short: true
              }
            ]
          }
        ]
      };
      
      await fetch(this.options.slackWebhook, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(message)
      });
    }
  }
  
  // 手动创建问题
  createManualIssue(title, description, severity = 'medium') {
    const issue = {
      key: 'manual_' + Date.now(),
      title,
      description,
      count: 1,
      firstOccurrence: Date.now(),
      lastOccurrence: Date.now(),
      events: [],
      status: 'new',
      severity,
      tags: ['manual'],
      manual: true
    };
    
    this.issueCache.set(issue.key, issue);
    this.createIssueTicket(issue);
    
    return issue;
  }
  
  // 获取问题统计
  getIssueStats() {
    const stats = {
      total: this.issueCache.size,
      bySeverity: {},
      byStatus: {},
      recentIssues: []
    };
    
    this.issueCache.forEach(issue => {
      // 按严重程度统计
      stats.bySeverity[issue.severity] = (stats.bySeverity[issue.severity] || 0) + 1;
      
      // 按状态统计
      stats.byStatus[issue.status] = (stats.byStatus[issue.status] || 0) + 1;
      
      // 最近的问题
      if (Date.now() - issue.lastOccurrence < 86400000) {
        stats.recentIssues.push({
          key: issue.key,
          title: issue.title,
          severity: issue.severity,
          count: issue.count,
          lastOccurrence: issue.lastOccurrence
        });
      }
    });
    
    // 按最后发生时间排序
    stats.recentIssues.sort((a, b) => b.lastOccurrence - a.lastOccurrence);
    
    return stats;
  }
}

// 创建问题追踪集成实例
export const issueTrackingIntegration = new IssueTrackingIntegration();

// 导出便捷方法
export const createManualIssue = (title, description, severity) => 
  issueTrackingIntegration.createManualIssue(title, description, severity);
export const getIssueStats = () => issueTrackingIntegration.getIssueStats();
```

## 四、核心价值与实施建议

### 4.1 核心价值

1. **全面的错误监控**：从JavaScript错误到网络请求失败，全方位监控应用运行状态
2. **智能错误分析**：自动分析错误模式，识别趋势和异常，提供有价值的洞察
3. **用户体验优化**：通过用户反馈系统，了解错误对用户的真实影响
4. **高效问题解决**：集成工作流系统，自动创建工单，加速问题解决流程
5. **数据驱动决策**：基于详细的错误数据和分析，做出更明智的技术决策

### 4.2 实施建议

1. **分阶段实施**：
   - 第一阶段：基础错误监控和Sentry集成
   - 第二阶段：错误分析和用户反馈系统
   - 第三阶段：工作流集成和自动化

2. **团队协作**：
   - 建立错误处理标准和流程
   - 定期进行错误分析会议
   - 培训团队成员使用监控工具

3. **持续改进**：
   - 定期审查和优化错误过滤规则
   - 根据反馈调整监控策略
   - 持续完善错误处理机制

### 4.3 未来发展趋势

1. **AI驱动的错误预测**：利用机器学习预测潜在的错误和性能问题
2. **实时协作调试**：支持团队成员实时协作调试复杂问题
3. **智能错误修复建议**：基于错误模式自动生成修复建议
4. **跨平台统一监控**：统一Web、移动端和后端的错误监控体系

## 总结

前端Sentry错误监控与调试是构建高可用性Web应用的重要基础。通过建立完善的错误监控体系、智能的错误分析机制、有效的用户反馈系统和高效的问题追踪流程，我们可以显著提升应用的稳定性和用户体验。

关键在于将错误监控作为开发流程的重要组成部分，而不仅仅是事后的补救措施。通过持续的监控、分析和改进，我们可以构建更加健壮和可靠的前端应用，为用户提供更好的服务体验。