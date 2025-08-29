# 前端Sentry性能监控深度实践：从Core Web Vitals到用户体验优化

## 引言

在现代Web应用开发中，性能监控已经成为保障用户体验的关键环节。Google的Core Web Vitals指标为我们提供了衡量用户体验的标准，而Sentry作为强大的监控平台，不仅能够捕获错误，更能深入监控应用性能。本文将深入探讨如何使用Sentry进行全面的前端性能监控，从Core Web Vitals的实时追踪到用户体验的持续优化。

## 一、Core Web Vitals监控实现

### 1.1 Core Web Vitals基础配置

```javascript
// webVitalsMonitor.js
import * as Sentry from '@sentry/browser';
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

// Web Vitals监控器
export class WebVitalsMonitor {
  constructor(options = {}) {
    this.options = {
      enableReporting: true,
      sampleRate: 1.0,
      thresholds: {
        LCP: { good: 2500, poor: 4000 },
        FID: { good: 100, poor: 300 },
        CLS: { good: 0.1, poor: 0.25 },
        FCP: { good: 1800, poor: 3000 },
        TTFB: { good: 800, poor: 1800 }
      },
      ...options
    };
    
    this.vitalsData = new Map();
    this.setupVitalsCollection();
  }
  
  setupVitalsCollection() {
    // 收集Largest Contentful Paint (LCP)
    getLCP((metric) => {
      this.handleVitalMetric('LCP', metric);
    });
    
    // 收集First Input Delay (FID)
    getFID((metric) => {
      this.handleVitalMetric('FID', metric);
    });
    
    // 收集Cumulative Layout Shift (CLS)
    getCLS((metric) => {
      this.handleVitalMetric('CLS', metric);
    });
    
    // 收集First Contentful Paint (FCP)
    getFCP((metric) => {
      this.handleVitalMetric('FCP', metric);
    });
    
    // 收集Time to First Byte (TTFB)
    getTTFB((metric) => {
      this.handleVitalMetric('TTFB', metric);
    });
  }
  
  handleVitalMetric(name, metric) {
    const value = metric.value;
    const rating = this.getRating(name, value);
    
    // 存储指标数据
    this.vitalsData.set(name, {
      name,
      value,
      rating,
      id: metric.id,
      delta: metric.delta,
      entries: metric.entries,
      timestamp: Date.now()
    });
    
    // 发送到Sentry
    if (this.options.enableReporting && Math.random() < this.options.sampleRate) {
      this.reportToSentry(name, metric, rating);
    }
    
    // 触发自定义事件
    this.dispatchVitalEvent(name, metric, rating);
  }
  
  getRating(name, value) {
    const thresholds = this.options.thresholds[name];
    if (!thresholds) return 'unknown';
    
    if (value <= thresholds.good) return 'good';
    if (value <= thresholds.poor) return 'needs-improvement';
    return 'poor';
  }
  
  reportToSentry(name, metric, rating) {
    // 作为性能指标发送
    Sentry.addBreadcrumb({
      category: 'web-vitals',
      message: `${name}: ${metric.value}ms (${rating})`,
      level: rating === 'poor' ? 'warning' : 'info',
      data: {
        metric: name,
        value: metric.value,
        rating,
        id: metric.id,
        delta: metric.delta
      }
    });
    
    // 发送自定义事件
    Sentry.captureMessage(
      `Web Vital: ${name}`,
      rating === 'poor' ? 'warning' : 'info',
      {
        tags: {
          webVital: name,
          rating: rating
        },
        extra: {
          value: metric.value,
          id: metric.id,
          delta: metric.delta,
          entries: metric.entries?.map(entry => ({
            name: entry.name,
            startTime: entry.startTime,
            duration: entry.duration
          }))
        }
      }
    );
    
    // 对于关键性能问题，发送错误级别事件
    if (rating === 'poor') {
      Sentry.captureException(
        new Error(`Poor ${name} performance: ${metric.value}ms`),
        {
          tags: {
            category: 'performance',
            webVital: name
          },
          level: 'warning'
        }
      );
    }
  }
  
  dispatchVitalEvent(name, metric, rating) {
    const event = new CustomEvent('webVitalMeasured', {
      detail: {
        name,
        metric,
        rating,
        timestamp: Date.now()
      }
    });
    
    window.dispatchEvent(event);
  }
  
  // 获取所有Web Vitals数据
  getAllVitals() {
    return Object.fromEntries(this.vitalsData);
  }
  
  // 获取性能评分
  getPerformanceScore() {
    const vitals = this.getAllVitals();
    const scores = {
      good: 3,
      'needs-improvement': 2,
      poor: 1,
      unknown: 0
    };
    
    let totalScore = 0;
    let count = 0;
    
    Object.values(vitals).forEach(vital => {
      totalScore += scores[vital.rating] || 0;
      count++;
    });
    
    return count > 0 ? Math.round((totalScore / count / 3) * 100) : 0;
  }
  
  // 生成性能报告
  generatePerformanceReport() {
    const vitals = this.getAllVitals();
    const score = this.getPerformanceScore();
    
    return {
      score,
      vitals,
      recommendations: this.generateRecommendations(vitals),
      timestamp: new Date().toISOString()
    };
  }
  
  generateRecommendations(vitals) {
    const recommendations = [];
    
    Object.entries(vitals).forEach(([name, data]) => {
      if (data.rating === 'poor') {
        switch (name) {
          case 'LCP':
            recommendations.push({
              metric: 'LCP',
              issue: 'Slow largest contentful paint',
              suggestions: [
                'Optimize images and use modern formats (WebP, AVIF)',
                'Implement lazy loading for images',
                'Use CDN for static assets',
                'Optimize server response times',
                'Remove unused CSS and JavaScript'
              ]
            });
            break;
          case 'FID':
            recommendations.push({
              metric: 'FID',
              issue: 'Poor first input delay',
              suggestions: [
                'Reduce JavaScript execution time',
                'Split large bundles using code splitting',
                'Use web workers for heavy computations',
                'Optimize third-party scripts',
                'Implement proper event delegation'
              ]
            });
            break;
          case 'CLS':
            recommendations.push({
              metric: 'CLS',
              issue: 'High cumulative layout shift',
              suggestions: [
                'Set explicit dimensions for images and videos',
                'Reserve space for dynamic content',
                'Avoid inserting content above existing content',
                'Use CSS transforms instead of changing layout properties',
                'Preload fonts to prevent font swapping'
              ]
            });
            break;
        }
      }
    });
    
    return recommendations;
  }
}

// 创建Web Vitals监控实例
export const webVitalsMonitor = new WebVitalsMonitor({
  enableReporting: true,
  sampleRate: 0.1, // 10%采样率
  thresholds: {
    LCP: { good: 2500, poor: 4000 },
    FID: { good: 100, poor: 300 },
    CLS: { good: 0.1, poor: 0.25 }
  }
});
```

### 1.2 高级性能指标监控

```javascript
// advancedPerformanceMonitor.js
import * as Sentry from '@sentry/browser';

// 高级性能监控器
export class AdvancedPerformanceMonitor {
  constructor(options = {}) {
    this.options = {
      enableResourceTiming: true,
      enableNavigationTiming: true,
      enableLongTasks: true,
      enableMemoryMonitoring: true,
      resourceThreshold: 1000, // 1秒
      longTaskThreshold: 50, // 50ms
      memoryCheckInterval: 30000, // 30秒
      ...options
    };
    
    this.performanceData = {
      resources: [],
      navigation: null,
      longTasks: [],
      memory: []
    };
    
    this.setupPerformanceObservers();
  }
  
  setupPerformanceObservers() {
    if (!('PerformanceObserver' in window)) {
      console.warn('PerformanceObserver not supported');
      return;
    }
    
    // 监控资源加载性能
    if (this.options.enableResourceTiming) {
      this.setupResourceTimingObserver();
    }
    
    // 监控导航性能
    if (this.options.enableNavigationTiming) {
      this.setupNavigationTimingObserver();
    }
    
    // 监控长任务
    if (this.options.enableLongTasks) {
      this.setupLongTaskObserver();
    }
    
    // 监控内存使用
    if (this.options.enableMemoryMonitoring) {
      this.setupMemoryMonitoring();
    }
  }
  
  setupResourceTimingObserver() {
    const observer = new PerformanceObserver((list) => {
      list.getEntries().forEach((entry) => {
        this.handleResourceEntry(entry);
      });
    });
    
    observer.observe({ entryTypes: ['resource'] });
  }
  
  setupNavigationTimingObserver() {
    const observer = new PerformanceObserver((list) => {
      list.getEntries().forEach((entry) => {
        this.handleNavigationEntry(entry);
      });
    });
    
    observer.observe({ entryTypes: ['navigation'] });
  }
  
  setupLongTaskObserver() {
    try {
      const observer = new PerformanceObserver((list) => {
        list.getEntries().forEach((entry) => {
          this.handleLongTaskEntry(entry);
        });
      });
      
      observer.observe({ entryTypes: ['longtask'] });
    } catch (error) {
      console.warn('Long task observer not supported:', error);
    }
  }
  
  setupMemoryMonitoring() {
    if (!('memory' in performance)) {
      console.warn('Memory API not supported');
      return;
    }
    
    setInterval(() => {
      this.collectMemoryMetrics();
    }, this.options.memoryCheckInterval);
  }
  
  handleResourceEntry(entry) {
    const duration = entry.responseEnd - entry.startTime;
    const isSlowResource = duration > this.options.resourceThreshold;
    
    const resourceData = {
      name: entry.name,
      type: this.getResourceType(entry),
      duration,
      size: entry.transferSize || 0,
      startTime: entry.startTime,
      responseEnd: entry.responseEnd,
      isSlowResource,
      timestamp: Date.now()
    };
    
    this.performanceData.resources.push(resourceData);
    
    // 报告慢资源
    if (isSlowResource) {
      this.reportSlowResource(resourceData);
    }
    
    // 限制存储的资源数量
    if (this.performanceData.resources.length > 100) {
      this.performanceData.resources = this.performanceData.resources.slice(-50);
    }
  }
  
  handleNavigationEntry(entry) {
    const navigationData = {
      type: entry.type,
      redirectCount: entry.redirectCount,
      domContentLoaded: entry.domContentLoadedEventEnd - entry.domContentLoadedEventStart,
      loadComplete: entry.loadEventEnd - entry.loadEventStart,
      domInteractive: entry.domInteractive - entry.navigationStart,
      firstPaint: this.getFirstPaint(),
      firstContentfulPaint: this.getFirstContentfulPaint(),
      timestamp: Date.now()
    };
    
    this.performanceData.navigation = navigationData;
    this.reportNavigationTiming(navigationData);
  }
  
  handleLongTaskEntry(entry) {
    const longTaskData = {
      duration: entry.duration,
      startTime: entry.startTime,
      name: entry.name,
      attribution: entry.attribution?.map(attr => ({
        name: attr.name,
        type: attr.entryType,
        startTime: attr.startTime,
        duration: attr.duration
      })),
      timestamp: Date.now()
    };
    
    this.performanceData.longTasks.push(longTaskData);
    this.reportLongTask(longTaskData);
    
    // 限制存储的长任务数量
    if (this.performanceData.longTasks.length > 50) {
      this.performanceData.longTasks = this.performanceData.longTasks.slice(-25);
    }
  }
  
  collectMemoryMetrics() {
    const memory = performance.memory;
    const memoryData = {
      usedJSHeapSize: memory.usedJSHeapSize,
      totalJSHeapSize: memory.totalJSHeapSize,
      jsHeapSizeLimit: memory.jsHeapSizeLimit,
      usagePercentage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100,
      timestamp: Date.now()
    };
    
    this.performanceData.memory.push(memoryData);
    
    // 检查内存使用率
    if (memoryData.usagePercentage > 80) {
      this.reportHighMemoryUsage(memoryData);
    }
    
    // 限制存储的内存数据数量
    if (this.performanceData.memory.length > 100) {
      this.performanceData.memory = this.performanceData.memory.slice(-50);
    }
  }
  
  getResourceType(entry) {
    if (entry.initiatorType) {
      return entry.initiatorType;
    }
    
    const url = entry.name;
    if (url.match(/\.(js|mjs)$/)) return 'script';
    if (url.match(/\.(css)$/)) return 'stylesheet';
    if (url.match(/\.(png|jpg|jpeg|gif|webp|svg)$/)) return 'image';
    if (url.match(/\.(woff|woff2|ttf|otf)$/)) return 'font';
    
    return 'other';
  }
  
  getFirstPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const fpEntry = paintEntries.find(entry => entry.name === 'first-paint');
    return fpEntry ? fpEntry.startTime : null;
  }
  
  getFirstContentfulPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const fcpEntry = paintEntries.find(entry => entry.name === 'first-contentful-paint');
    return fcpEntry ? fcpEntry.startTime : null;
  }
  
  reportSlowResource(resourceData) {
    Sentry.addBreadcrumb({
      category: 'performance',
      message: `Slow resource: ${resourceData.name}`,
      level: 'warning',
      data: {
        resource: resourceData.name,
        type: resourceData.type,
        duration: resourceData.duration,
        size: resourceData.size
      }
    });
    
    Sentry.captureMessage(
      `Slow resource loading: ${resourceData.name}`,
      'warning',
      {
        tags: {
          category: 'performance',
          resourceType: resourceData.type
        },
        extra: resourceData
      }
    );
  }
  
  reportNavigationTiming(navigationData) {
    Sentry.addBreadcrumb({
      category: 'navigation',
      message: `Page navigation completed`,
      level: 'info',
      data: {
        type: navigationData.type,
        domContentLoaded: navigationData.domContentLoaded,
        loadComplete: navigationData.loadComplete,
        domInteractive: navigationData.domInteractive
      }
    });
  }
  
  reportLongTask(longTaskData) {
    Sentry.addBreadcrumb({
      category: 'performance',
      message: `Long task detected: ${longTaskData.duration.toFixed(2)}ms`,
      level: 'warning',
      data: {
        duration: longTaskData.duration,
        startTime: longTaskData.startTime,
        name: longTaskData.name
      }
    });
    
    if (longTaskData.duration > 100) { // 超过100ms的长任务
      Sentry.captureMessage(
        `Long task blocking main thread: ${longTaskData.duration.toFixed(2)}ms`,
        'warning',
        {
          tags: {
            category: 'performance',
            longTask: true
          },
          extra: longTaskData
        }
      );
    }
  }
  
  reportHighMemoryUsage(memoryData) {
    Sentry.addBreadcrumb({
      category: 'performance',
      message: `High memory usage: ${memoryData.usagePercentage.toFixed(2)}%`,
      level: 'warning',
      data: {
        usedJSHeapSize: memoryData.usedJSHeapSize,
        totalJSHeapSize: memoryData.totalJSHeapSize,
        usagePercentage: memoryData.usagePercentage
      }
    });
    
    if (memoryData.usagePercentage > 90) {
      Sentry.captureMessage(
        `Critical memory usage: ${memoryData.usagePercentage.toFixed(2)}%`,
        'error',
        {
          tags: {
            category: 'performance',
            memoryIssue: true
          },
          extra: memoryData
        }
      );
    }
  }
  
  // 获取性能摘要
  getPerformanceSummary() {
    const resources = this.performanceData.resources;
    const longTasks = this.performanceData.longTasks;
    const memory = this.performanceData.memory;
    
    return {
      resources: {
        total: resources.length,
        slow: resources.filter(r => r.isSlowResource).length,
        averageDuration: resources.length > 0 
          ? resources.reduce((sum, r) => sum + r.duration, 0) / resources.length 
          : 0,
        totalSize: resources.reduce((sum, r) => sum + r.size, 0)
      },
      longTasks: {
        total: longTasks.length,
        totalDuration: longTasks.reduce((sum, t) => sum + t.duration, 0),
        averageDuration: longTasks.length > 0
          ? longTasks.reduce((sum, t) => sum + t.duration, 0) / longTasks.length
          : 0
      },
      memory: {
        current: memory.length > 0 ? memory[memory.length - 1] : null,
        peak: memory.length > 0 
          ? Math.max(...memory.map(m => m.usagePercentage))
          : 0
      },
      navigation: this.performanceData.navigation
    };
  }
  
  // 生成性能报告
  generateDetailedReport() {
    const summary = this.getPerformanceSummary();
    const recommendations = this.generatePerformanceRecommendations(summary);
    
    return {
      summary,
      recommendations,
      rawData: this.performanceData,
      timestamp: new Date().toISOString()
    };
  }
  
  generatePerformanceRecommendations(summary) {
    const recommendations = [];
    
    // 资源加载建议
    if (summary.resources.slow > 0) {
      recommendations.push({
        category: 'Resource Loading',
        priority: 'high',
        issue: `${summary.resources.slow} slow resources detected`,
        suggestions: [
          'Optimize large resources or split them into smaller chunks',
          'Use CDN for static assets',
          'Implement resource preloading for critical assets',
          'Consider lazy loading for non-critical resources'
        ]
      });
    }
    
    // 长任务建议
    if (summary.longTasks.total > 0) {
      recommendations.push({
        category: 'JavaScript Performance',
        priority: summary.longTasks.averageDuration > 100 ? 'high' : 'medium',
        issue: `${summary.longTasks.total} long tasks detected`,
        suggestions: [
          'Break up long-running JavaScript tasks',
          'Use requestIdleCallback for non-critical work',
          'Implement code splitting to reduce bundle size',
          'Consider using web workers for heavy computations'
        ]
      });
    }
    
    // 内存使用建议
    if (summary.memory.peak > 80) {
      recommendations.push({
        category: 'Memory Usage',
        priority: summary.memory.peak > 90 ? 'critical' : 'high',
        issue: `High memory usage detected (${summary.memory.peak.toFixed(2)}%)`,
        suggestions: [
          'Review and fix memory leaks',
          'Optimize data structures and algorithms',
          'Implement proper cleanup in component lifecycle',
          'Use object pooling for frequently created objects'
        ]
      });
    }
    
    return recommendations;
  }
}

// 创建高级性能监控实例
export const advancedPerformanceMonitor = new AdvancedPerformanceMonitor({
  enableResourceTiming: true,
  enableNavigationTiming: true,
  enableLongTasks: true,
  enableMemoryMonitoring: true,
  resourceThreshold: 800,
  longTaskThreshold: 50
});
```

## 二、用户体验监控

### 2.1 用户交互监控

```javascript
// userExperienceMonitor.js
import * as Sentry from '@sentry/browser';

// 用户体验监控器
export class UserExperienceMonitor {
  constructor(options = {}) {
    this.options = {
      trackClicks: true,
      trackScrolling: true,
      trackFormInteractions: true,
      trackPageVisibility: true,
      trackNetworkStatus: true,
      clickTimeout: 5000, // 5秒点击超时
      scrollThreshold: 100, // 滚动阈值
      ...options
    };
    
    this.userInteractions = [];
    this.pageMetrics = {
      timeOnPage: 0,
      scrollDepth: 0,
      clickCount: 0,
      formSubmissions: 0,
      errors: 0
    };
    
    this.setupUserExperienceTracking();
  }
  
  setupUserExperienceTracking() {
    if (this.options.trackClicks) {
      this.setupClickTracking();
    }
    
    if (this.options.trackScrolling) {
      this.setupScrollTracking();
    }
    
    if (this.options.trackFormInteractions) {
      this.setupFormTracking();
    }
    
    if (this.options.trackPageVisibility) {
      this.setupPageVisibilityTracking();
    }
    
    if (this.options.trackNetworkStatus) {
      this.setupNetworkStatusTracking();
    }
    
    this.setupPageMetrics();
  }
  
  setupClickTracking() {
    let clickStartTime = null;
    
    document.addEventListener('mousedown', (event) => {
      clickStartTime = performance.now();
    });
    
    document.addEventListener('click', (event) => {
      if (!clickStartTime) return;
      
      const clickDuration = performance.now() - clickStartTime;
      const target = event.target;
      
      const clickData = {
        type: 'click',
        element: this.getElementSelector(target),
        tagName: target.tagName,
        className: target.className,
        id: target.id,
        text: target.textContent?.slice(0, 100),
        duration: clickDuration,
        timestamp: Date.now(),
        coordinates: {
          x: event.clientX,
          y: event.clientY
        }
      };
      
      this.recordInteraction(clickData);
      this.pageMetrics.clickCount++;
      
      // 检测慢响应点击
      if (clickDuration > this.options.clickTimeout) {
        this.reportSlowClick(clickData);
      }
      
      clickStartTime = null;
    });
  }
  
  setupScrollTracking() {
    let scrollTimeout;
    let maxScrollDepth = 0;
    
    const trackScroll = () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight;
      
      const scrollDepth = Math.round((scrollTop + windowHeight) / documentHeight * 100);
      
      if (scrollDepth > maxScrollDepth) {
        maxScrollDepth = scrollDepth;
        this.pageMetrics.scrollDepth = maxScrollDepth;
        
        // 记录重要的滚动里程碑
        if (scrollDepth >= 25 && scrollDepth % 25 === 0) {
          this.recordInteraction({
            type: 'scroll',
            depth: scrollDepth,
            timestamp: Date.now()
          });
        }
      }
    };
    
    window.addEventListener('scroll', () => {
      clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(trackScroll, 100);
    });
  }
  
  setupFormTracking() {
    // 跟踪表单开始填写
    document.addEventListener('focusin', (event) => {
      if (this.isFormElement(event.target)) {
        this.recordInteraction({
          type: 'form_start',
          element: this.getElementSelector(event.target),
          formId: this.getFormId(event.target),
          timestamp: Date.now()
        });
      }
    });
    
    // 跟踪表单提交
    document.addEventListener('submit', (event) => {
      const form = event.target;
      const formData = new FormData(form);
      const fieldCount = Array.from(formData.keys()).length;
      
      this.recordInteraction({
        type: 'form_submit',
        formId: form.id || 'unknown',
        fieldCount,
        timestamp: Date.now()
      });
      
      this.pageMetrics.formSubmissions++;
    });
    
    // 跟踪表单错误
    document.addEventListener('invalid', (event) => {
      this.recordInteraction({
        type: 'form_error',
        element: this.getElementSelector(event.target),
        validationMessage: event.target.validationMessage,
        timestamp: Date.now()
      });
      
      this.pageMetrics.errors++;
    });
  }
  
  setupPageVisibilityTracking() {
    let pageStartTime = Date.now();
    let isVisible = !document.hidden;
    
    const updateTimeOnPage = () => {
      if (isVisible) {
        this.pageMetrics.timeOnPage += Date.now() - pageStartTime;
      }
      pageStartTime = Date.now();
    };
    
    document.addEventListener('visibilitychange', () => {
      updateTimeOnPage();
      isVisible = !document.hidden;
      
      this.recordInteraction({
        type: 'visibility_change',
        visible: isVisible,
        timestamp: Date.now()
      });
    });
    
    // 页面卸载时更新时间
    window.addEventListener('beforeunload', updateTimeOnPage);
  }
  
  setupNetworkStatusTracking() {
    if ('connection' in navigator) {
      const connection = navigator.connection;
      
      const trackConnection = () => {
        this.recordInteraction({
          type: 'network_change',
          effectiveType: connection.effectiveType,
          downlink: connection.downlink,
          rtt: connection.rtt,
          saveData: connection.saveData,
          timestamp: Date.now()
        });
      };
      
      connection.addEventListener('change', trackConnection);
      
      // 初始网络状态
      trackConnection();
    }
    
    // 在线/离线状态
    window.addEventListener('online', () => {
      this.recordInteraction({
        type: 'network_status',
        online: true,
        timestamp: Date.now()
      });
    });
    
    window.addEventListener('offline', () => {
      this.recordInteraction({
        type: 'network_status',
        online: false,
        timestamp: Date.now()
      });
    });
  }
  
  setupPageMetrics() {
    // 定期发送页面指标
    setInterval(() => {
      this.reportPageMetrics();
    }, 30000); // 每30秒
    
    // 页面卸载时发送最终指标
    window.addEventListener('beforeunload', () => {
      this.reportPageMetrics(true);
    });
  }
  
  recordInteraction(interactionData) {
    this.userInteractions.push(interactionData);
    
    // 发送到Sentry
    Sentry.addBreadcrumb({
      category: 'user-interaction',
      message: `User ${interactionData.type}`,
      level: 'info',
      data: interactionData
    });
    
    // 限制存储的交互数量
    if (this.userInteractions.length > 100) {
      this.userInteractions = this.userInteractions.slice(-50);
    }
  }
  
  reportSlowClick(clickData) {
    Sentry.captureMessage(
      `Slow click response: ${clickData.duration.toFixed(2)}ms`,
      'warning',
      {
        tags: {
          category: 'user-experience',
          slowClick: true
        },
        extra: clickData
      }
    );
  }
  
  reportPageMetrics(isFinal = false) {
    const metrics = {
      ...this.pageMetrics,
      url: window.location.href,
      userAgent: navigator.userAgent,
      viewport: {
        width: window.innerWidth,
        height: window.innerHeight
      },
      isFinal
    };
    
    Sentry.addBreadcrumb({
      category: 'page-metrics',
      message: `Page metrics ${isFinal ? 'final' : 'update'}`,
      level: 'info',
      data: metrics
    });
    
    if (isFinal) {
      Sentry.captureMessage(
        'Page session completed',
        'info',
        {
          tags: {
            category: 'user-experience',
            sessionEnd: true
          },
          extra: metrics
        }
      );
    }
  }
  
  getElementSelector(element) {
    if (element.id) {
      return `#${element.id}`;
    }
    
    if (element.className) {
      return `.${element.className.split(' ').join('.')}`;
    }
    
    return element.tagName.toLowerCase();
  }
  
  getFormId(element) {
    const form = element.closest('form');
    return form ? (form.id || 'unknown') : null;
  }
  
  isFormElement(element) {
    const formElements = ['INPUT', 'TEXTAREA', 'SELECT'];
    return formElements.includes(element.tagName);
  }
  
  // 获取用户体验评分
  getUserExperienceScore() {
    const metrics = this.pageMetrics;
    let score = 100;
    
    // 基于错误数量扣分
    score -= Math.min(metrics.errors * 10, 50);
    
    // 基于交互质量评分
    const slowClicks = this.userInteractions.filter(
      i => i.type === 'click' && i.duration > 1000
    ).length;
    score -= Math.min(slowClicks * 5, 25);
    
    // 基于参与度评分
    if (metrics.scrollDepth < 25) score -= 10;
    if (metrics.timeOnPage < 10000) score -= 15; // 少于10秒
    
    return Math.max(score, 0);
  }
  
  // 生成用户体验报告
  generateUXReport() {
    const score = this.getUserExperienceScore();
    const interactions = this.userInteractions;
    
    return {
      score,
      metrics: this.pageMetrics,
      interactions: {
        total: interactions.length,
        byType: this.groupInteractionsByType(interactions),
        timeline: interactions.slice(-20) // 最近20个交互
      },
      recommendations: this.generateUXRecommendations(score, this.pageMetrics),
      timestamp: new Date().toISOString()
    };
  }
  
  groupInteractionsByType(interactions) {
    return interactions.reduce((groups, interaction) => {
      const type = interaction.type;
      groups[type] = (groups[type] || 0) + 1;
      return groups;
    }, {});
  }
  
  generateUXRecommendations(score, metrics) {
    const recommendations = [];
    
    if (score < 70) {
      recommendations.push({
        priority: 'high',
        category: 'Overall UX',
        issue: 'Poor user experience score',
        suggestions: [
          'Review and optimize user interaction flows',
          'Reduce page load times and improve responsiveness',
          'Fix form validation and error handling',
          'Improve visual feedback for user actions'
        ]
      });
    }
    
    if (metrics.errors > 3) {
      recommendations.push({
        priority: 'high',
        category: 'Error Handling',
        issue: `High error count: ${metrics.errors}`,
        suggestions: [
          'Improve form validation and user guidance',
          'Add better error messages and recovery options',
          'Review and fix common user interaction issues'
        ]
      });
    }
    
    if (metrics.scrollDepth < 50) {
      recommendations.push({
        priority: 'medium',
        category: 'Content Engagement',
        issue: 'Low scroll depth indicates poor content engagement',
        suggestions: [
          'Review content quality and relevance',
          'Improve page layout and visual hierarchy',
          'Add engaging interactive elements',
          'Optimize content for better readability'
        ]
      });
    }
    
    return recommendations;
  }
}

// 创建用户体验监控实例
export const userExperienceMonitor = new UserExperienceMonitor({
  trackClicks: true,
  trackScrolling: true,
  trackFormInteractions: true,
  trackPageVisibility: true,
  trackNetworkStatus: true,
  clickTimeout: 3000
});
```

### 2.2 A/B测试与功能标志集成

```javascript
// abTestingIntegration.js
import * as Sentry from '@sentry/browser';

// A/B测试集成
export class ABTestingIntegration {
  constructor(options = {}) {
    this.options = {
      enableAutoTracking: true,
      trackConversions: true,
      trackUserSegments: true,
      ...options
    };
    
    this.activeTests = new Map();
    this.userSegments = new Set();
    this.conversions = [];
    
    this.setupABTestTracking();
  }
  
  setupABTestTracking() {
    // 监听A/B测试事件
    window.addEventListener('abTestAssigned', (event) => {
      this.handleTestAssignment(event.detail);
    });
    
    window.addEventListener('abTestConversion', (event) => {
      this.handleConversion(event.detail);
    });
    
    // 设置Sentry用户上下文
    this.updateSentryContext();
  }
  
  // 分配用户到A/B测试
  assignUserToTest(testId, variant, metadata = {}) {
    const assignment = {
      testId,
      variant,
      assignedAt: Date.now(),
      metadata
    };
    
    this.activeTests.set(testId, assignment);
    
    // 更新Sentry标签
    Sentry.setTag(`ab_test_${testId}`, variant);
    
    // 记录分配事件
    Sentry.addBreadcrumb({
      category: 'ab-testing',
      message: `Assigned to test ${testId}: ${variant}`,
      level: 'info',
      data: assignment
    });
    
    // 发送分配事件
    this.trackTestAssignment(assignment);
    
    return assignment;
  }
  
  // 处理测试分配
  handleTestAssignment(assignmentData) {
    const { testId, variant, metadata } = assignmentData;
    this.assignUserToTest(testId, variant, metadata);
  }
  
  // 跟踪转化事件
  trackConversion(testId, conversionType, value = null, metadata = {}) {
    const testAssignment = this.activeTests.get(testId);
    if (!testAssignment) {
      console.warn(`No active test found for ${testId}`);
      return;
    }
    
    const conversion = {
      testId,
      variant: testAssignment.variant,
      conversionType,
      value,
      metadata,
      timestamp: Date.now()
    };
    
    this.conversions.push(conversion);
    
    // 发送到Sentry
    Sentry.addBreadcrumb({
      category: 'ab-testing',
      message: `Conversion: ${testId} - ${conversionType}`,
      level: 'info',
      data: conversion
    });
    
    Sentry.captureMessage(
      `A/B Test Conversion: ${testId}`,
      'info',
      {
        tags: {
          abTest: testId,
          variant: testAssignment.variant,
          conversionType
        },
        extra: conversion
      }
    );
    
    return conversion;
  }
  
  // 处理转化事件
  handleConversion(conversionData) {
    const { testId, conversionType, value, metadata } = conversionData;
    this.trackConversion(testId, conversionType, value, metadata);
  }
  
  // 添加用户分段
  addUserSegment(segment) {
    this.userSegments.add(segment);
    Sentry.setTag('user_segment', Array.from(this.userSegments).join(','));
    
    Sentry.addBreadcrumb({
      category: 'user-segmentation',
      message: `Added to segment: ${segment}`,
      level: 'info',
      data: { segment }
    });
  }
  
  // 移除用户分段
  removeUserSegment(segment) {
    this.userSegments.delete(segment);
    Sentry.setTag('user_segment', Array.from(this.userSegments).join(','));
  }
  
  // 获取用户的测试变体
  getUserVariant(testId) {
    const assignment = this.activeTests.get(testId);
    return assignment ? assignment.variant : null;
  }
  
  // 检查用户是否在特定测试中
  isUserInTest(testId) {
    return this.activeTests.has(testId);
  }
  
  // 获取所有活跃测试
  getActiveTests() {
    return Object.fromEntries(this.activeTests);
  }
  
  // 更新Sentry上下文
  updateSentryContext() {
    const activeTests = this.getActiveTests();
    const segments = Array.from(this.userSegments);
    
    Sentry.setContext('ab_testing', {
      activeTests,
      userSegments: segments,
      totalTests: this.activeTests.size,
      totalConversions: this.conversions.length
    });
  }
  
  // 跟踪测试分配
  trackTestAssignment(assignment) {
    if (!this.options.enableAutoTracking) return;
    
    // 这里可以集成其他分析工具
    console.log('A/B Test Assignment:', assignment);
  }
  
  // 生成A/B测试报告
  generateTestingReport() {
    const activeTests = this.getActiveTests();
    const conversions = this.conversions;
    
    const report = {
      summary: {
        totalTests: this.activeTests.size,
        totalConversions: conversions.length,
        userSegments: Array.from(this.userSegments)
      },
      tests: Object.entries(activeTests).map(([testId, assignment]) => {
        const testConversions = conversions.filter(c => c.testId === testId);
        return {
          testId,
          variant: assignment.variant,
          assignedAt: assignment.assignedAt,
          conversions: testConversions.length,
          conversionTypes: [...new Set(testConversions.map(c => c.conversionType))]
        };
      }),
      conversions: conversions.slice(-20), // 最近20个转化
      timestamp: new Date().toISOString()
    };
    
    return report;
  }
}

// 功能标志集成
export class FeatureFlagIntegration {
  constructor(options = {}) {
    this.options = {
      enableAutoTracking: true,
      trackFlagEvaluations: true,
      ...options
    };
    
    this.featureFlags = new Map();
    this.flagEvaluations = [];
    
    this.setupFeatureFlagTracking();
  }
  
  setupFeatureFlagTracking() {
    // 监听功能标志事件
    window.addEventListener('featureFlagEvaluated', (event) => {
      this.handleFlagEvaluation(event.detail);
    });
  }
  
  // 评估功能标志
  evaluateFlag(flagKey, defaultValue = false, context = {}) {
    // 这里应该集成实际的功能标志服务
    const value = this.getFlagValue(flagKey, defaultValue, context);
    
    const evaluation = {
      flagKey,
      value,
      defaultValue,
      context,
      timestamp: Date.now()
    };
    
    this.flagEvaluations.push(evaluation);
    this.featureFlags.set(flagKey, value);
    
    // 更新Sentry标签
    Sentry.setTag(`feature_flag_${flagKey}`, value.toString());
    
    // 记录评估事件
    if (this.options.trackFlagEvaluations) {
      Sentry.addBreadcrumb({
        category: 'feature-flag',
        message: `Flag evaluated: ${flagKey} = ${value}`,
        level: 'info',
        data: evaluation
      });
    }
    
    return value;
  }
  
  // 处理标志评估事件
  handleFlagEvaluation(evaluationData) {
    const { flagKey, value, context } = evaluationData;
    this.evaluateFlag(flagKey, value, context);
  }
  
  // 获取标志值（模拟实现）
  getFlagValue(flagKey, defaultValue, context) {
    // 这里应该调用实际的功能标志服务API
    // 例如：LaunchDarkly, Split.io, ConfigCat等
    
    // 模拟逻辑
    const storedFlags = JSON.parse(localStorage.getItem('featureFlags') || '{}');
    return storedFlags[flagKey] !== undefined ? storedFlags[flagKey] : defaultValue;
  }
  
  // 批量评估标志
  evaluateFlags(flags) {
    const results = {};
    
    Object.entries(flags).forEach(([flagKey, defaultValue]) => {
      results[flagKey] = this.evaluateFlag(flagKey, defaultValue);
    });
    
    return results;
  }
  
  // 获取所有活跃标志
  getActiveFlags() {
    return Object.fromEntries(this.featureFlags);
  }
  
  // 更新Sentry上下文
  updateSentryContext() {
    const activeFlags = this.getActiveFlags();
    
    Sentry.setContext('feature_flags', {
      activeFlags,
      totalFlags: this.featureFlags.size,
      totalEvaluations: this.flagEvaluations.length
    });
  }
  
  // 生成功能标志报告
  generateFlagReport() {
    const activeFlags = this.getActiveFlags();
    const evaluations = this.flagEvaluations;
    
    return {
      summary: {
        totalFlags: this.featureFlags.size,
        totalEvaluations: evaluations.length,
        enabledFlags: Object.entries(activeFlags).filter(([_, value]) => value).length
      },
      flags: Object.entries(activeFlags).map(([flagKey, value]) => {
        const flagEvaluations = evaluations.filter(e => e.flagKey === flagKey);
        return {
          flagKey,
          currentValue: value,
          evaluationCount: flagEvaluations.length,
          lastEvaluated: flagEvaluations.length > 0 
            ? Math.max(...flagEvaluations.map(e => e.timestamp))
            : null
        };
      }),
      recentEvaluations: evaluations.slice(-20),
      timestamp: new Date().toISOString()
    };
  }
}

// 创建集成实例
export const abTestingIntegration = new ABTestingIntegration({
  enableAutoTracking: true,
  trackConversions: true,
  trackUserSegments: true
});

export const featureFlagIntegration = new FeatureFlagIntegration({
  enableAutoTracking: true,
  trackFlagEvaluations: true
});
```

## 三、性能优化策略

### 3.1 智能性能优化

```javascript
// performanceOptimizer.js
import * as Sentry from '@sentry/browser';

// 性能优化器
export class PerformanceOptimizer {
  constructor(options = {}) {
    this.options = {
      enableAutoOptimization: true,
      enableResourceOptimization: true,
      enableCodeSplitting: true,
      enableCaching: true,
      performanceThresholds: {
        LCP: 2500,
        FID: 100,
        CLS: 0.1,
        TTFB: 800
      },
      ...options
    };
    
    this.optimizations = [];
    this.performanceHistory = [];
    
    this.setupPerformanceOptimization();
  }
  
  setupPerformanceOptimization() {
    // 监听性能指标
    window.addEventListener('webVitalMeasured', (event) => {
      this.handlePerformanceMetric(event.detail);
    });
    
    // 定期分析性能
    setInterval(() => {
      this.analyzePerformance();
    }, 60000); // 每分钟分析一次
    
    if (this.options.enableAutoOptimization) {
      this.enableAutoOptimizations();
    }
  }
  
  handlePerformanceMetric(metricData) {
    const { name, metric, rating } = metricData;
    
    this.performanceHistory.push({
      name,
      value: metric.value,
      rating,
      timestamp: Date.now()
    });
    
    // 如果性能指标差，触发优化
    if (rating === 'poor') {
      this.triggerOptimization(name, metric);
    }
    
    // 限制历史数据大小
    if (this.performanceHistory.length > 200) {
      this.performanceHistory = this.performanceHistory.slice(-100);
    }
  }
  
  triggerOptimization(metricName, metric) {
    const optimizations = this.getOptimizationsForMetric(metricName);
    
    optimizations.forEach(optimization => {
      if (this.shouldApplyOptimization(optimization)) {
        this.applyOptimization(optimization, metricName, metric);
      }
    });
  }
  
  getOptimizationsForMetric(metricName) {
    const optimizationMap = {
      LCP: [
        'preloadCriticalResources',
        'optimizeImages',
        'enableResourceHints',
        'optimizeCriticalPath'
      ],
      FID: [
        'deferNonCriticalJS',
        'enableCodeSplitting',
        'optimizeEventHandlers',
        'useWebWorkers'
      ],
      CLS: [
        'setImageDimensions',
        'reserveSpaceForAds',
        'preloadFonts',
        'optimizeLayoutShifts'
      ],
      TTFB: [
        'enableCaching',
        'optimizeServerResponse',
        'useCDN',
        'enableCompression'
      ]
    };
    
    return optimizationMap[metricName] || [];
  }
  
  shouldApplyOptimization(optimization) {
    // 检查是否已经应用过此优化
    const recentOptimizations = this.optimizations.filter(
      opt => opt.type === optimization && Date.now() - opt.timestamp < 300000 // 5分钟内
    );
    
    return recentOptimizations.length === 0;
  }
  
  applyOptimization(optimization, metricName, metric) {
    const optimizationData = {
      type: optimization,
      metricName,
      metricValue: metric.value,
      timestamp: Date.now(),
      applied: false
    };
    
    try {
      switch (optimization) {
        case 'preloadCriticalResources':
          this.preloadCriticalResources();
          break;
        case 'optimizeImages':
          this.optimizeImages();
          break;
        case 'enableResourceHints':
          this.enableResourceHints();
          break;
        case 'deferNonCriticalJS':
          this.deferNonCriticalJS();
          break;
        case 'enableCodeSplitting':
          this.enableCodeSplitting();
          break;
        case 'setImageDimensions':
          this.setImageDimensions();
          break;
        case 'preloadFonts':
          this.preloadFonts();
          break;
        case 'enableCaching':
          this.enableCaching();
          break;
        default:
          console.warn(`Unknown optimization: ${optimization}`);
          return;
      }
      
      optimizationData.applied = true;
      
      // 记录优化应用
      Sentry.addBreadcrumb({
        category: 'performance-optimization',
        message: `Applied optimization: ${optimization}`,
        level: 'info',
        data: optimizationData
      });
      
    } catch (error) {
      optimizationData.error = error.message;
      
      Sentry.captureException(error, {
        tags: {
          category: 'performance-optimization',
          optimization
        },
        extra: optimizationData
      });
    }
    
    this.optimizations.push(optimizationData);
  }
  
  preloadCriticalResources() {
    // 预加载关键资源
    const criticalResources = this.identifyCriticalResources();
    
    criticalResources.forEach(resource => {
      if (!document.querySelector(`link[href="${resource.url}"]`)) {
        const link = document.createElement('link');
        link.rel = 'preload';
        link.href = resource.url;
        link.as = resource.type;
        
        if (resource.type === 'font') {
          link.crossOrigin = 'anonymous';
        }
        
        document.head.appendChild(link);
      }
    });
  }
  
  optimizeImages() {
    // 优化图片加载
    const images = document.querySelectorAll('img[data-src]');
    
    if ('IntersectionObserver' in window) {
      const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            img.removeAttribute('data-src');
            imageObserver.unobserve(img);
          }
        });
      });
      
      images.forEach(img => imageObserver.observe(img));
    }
  }
  
  enableResourceHints() {
    // 启用资源提示
    const externalDomains = this.getExternalDomains();
    
    externalDomains.forEach(domain => {
      if (!document.querySelector(`link[href="//${domain}"][rel="dns-prefetch"]`)) {
        const link = document.createElement('link');
        link.rel = 'dns-prefetch';
        link.href = `//${domain}`;
        document.head.appendChild(link);
      }
    });
  }
  
  deferNonCriticalJS() {
    // 延迟非关键JavaScript
    const scripts = document.querySelectorAll('script[data-defer]');
    
    scripts.forEach(script => {
      if (!script.hasAttribute('defer')) {
        script.defer = true;
      }
    });
    
    // 动态加载非关键脚本
    const nonCriticalScripts = this.identifyNonCriticalScripts();
    
    setTimeout(() => {
      nonCriticalScripts.forEach(scriptUrl => {
        this.loadScriptAsync(scriptUrl);
      });
    }, 1000);
  }
  
  enableCodeSplitting() {
    // 启用代码分割（需要构建工具支持）
    if ('import' in window) {
      // 动态导入非关键模块
      const nonCriticalModules = this.identifyNonCriticalModules();
      
      nonCriticalModules.forEach(modulePath => {
        import(modulePath).catch(error => {
          console.warn(`Failed to load module: ${modulePath}`, error);
        });
      });
    }
  }
  
  setImageDimensions() {
    // 为图片设置明确尺寸
    const images = document.querySelectorAll('img:not([width]):not([height])');
    
    images.forEach(img => {
      if (img.naturalWidth && img.naturalHeight) {
        img.width = img.naturalWidth;
        img.height = img.naturalHeight;
      }
    });
  }
  
  preloadFonts() {
    // 预加载字体
    const fontUrls = this.extractFontUrls();
    
    fontUrls.forEach(fontUrl => {
      if (!document.querySelector(`link[href="${fontUrl}"][rel="preload"]`)) {
        const link = document.createElement('link');
        link.rel = 'preload';
        link.href = fontUrl;
        link.as = 'font';
        link.crossOrigin = 'anonymous';
        document.head.appendChild(link);
      }
    });
  }
  
  enableCaching() {
    // 启用缓存策略
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js').catch(error => {
        console.warn('Service Worker registration failed:', error);
      });
    }
    
    // 设置缓存头
    this.setCacheHeaders();
  }
  
  identifyCriticalResources() {
    // 识别关键资源
    const criticalResources = [];
    
    // 关键CSS
    const criticalCSS = document.querySelectorAll('link[rel="stylesheet"][data-critical]');
    criticalCSS.forEach(link => {
      criticalResources.push({
        url: link.href,
        type: 'style'
      });
    });
    
    // 关键字体
    const criticalFonts = document.querySelectorAll('link[rel="preload"][as="font"][data-critical]');
    criticalFonts.forEach(link => {
      criticalResources.push({
        url: link.href,
        type: 'font'
      });
    });
    
    return criticalResources;
  }
  
  getExternalDomains() {
    // 获取外部域名
    const links = document.querySelectorAll('a[href^="http"], link[href^="http"], script[src^="http"]');
    const domains = new Set();
    
    links.forEach(element => {
      const url = element.href || element.src;
      try {
        const domain = new URL(url).hostname;
        if (domain !== window.location.hostname) {
          domains.add(domain);
        }
      } catch (error) {
        // 忽略无效URL
      }
    });
    
    return Array.from(domains);
  }
  
  identifyNonCriticalScripts() {
    // 识别非关键脚本
    return [
      '/js/analytics.js',
      '/js/social-widgets.js',
      '/js/chat-widget.js'
    ];
  }
  
  identifyNonCriticalModules() {
    // 识别非关键模块
    return [
      './modules/analytics.js',
      './modules/social-sharing.js',
      './modules/comments.js'
    ];
  }
  
  extractFontUrls() {
    // 提取字体URL
    const fontUrls = [];
    const stylesheets = document.querySelectorAll('link[rel="stylesheet"]');
    
    // 这里应该解析CSS文件中的字体URL
    // 简化实现
    fontUrls.push(
      '/fonts/main-font.woff2',
      '/fonts/heading-font.woff2'
    );
    
    return fontUrls;
  }
  
  loadScriptAsync(scriptUrl) {
    // 异步加载脚本
    const script = document.createElement('script');
    script.src = scriptUrl;
    script.async = true;
    document.head.appendChild(script);
  }
  
  setCacheHeaders() {
    // 设置缓存头（需要服务器支持）
    // 这里只是示例，实际需要在服务器端配置
    console.log('Cache headers should be configured on the server side');
  }
  
  analyzePerformance() {
    // 分析性能趋势
    const recentMetrics = this.performanceHistory.filter(
      metric => Date.now() - metric.timestamp < 300000 // 最近5分钟
    );
    
    if (recentMetrics.length === 0) return;
    
    const analysis = this.generatePerformanceAnalysis(recentMetrics);
    
    // 发送分析结果到Sentry
    Sentry.addBreadcrumb({
      category: 'performance-analysis',
      message: 'Performance analysis completed',
      level: 'info',
      data: analysis
    });
    
    // 如果发现性能问题，发送警告
    if (analysis.hasIssues) {
      Sentry.captureMessage(
        'Performance issues detected',
        'warning',
        {
          tags: {
            category: 'performance',
            autoAnalysis: true
          },
          extra: analysis
        }
      );
    }
  }
  
  generatePerformanceAnalysis(metrics) {
    const analysis = {
      totalMetrics: metrics.length,
      poorMetrics: metrics.filter(m => m.rating === 'poor').length,
      averageValues: {},
      trends: {},
      hasIssues: false
    };
    
    // 按指标类型分组
    const metricsByType = metrics.reduce((groups, metric) => {
      if (!groups[metric.name]) {
        groups[metric.name] = [];
      }
      groups[metric.name].push(metric);
      return groups;
    }, {});
    
    // 计算平均值和趋势
    Object.entries(metricsByType).forEach(([metricName, metricData]) => {
      const values = metricData.map(m => m.value);
      analysis.averageValues[metricName] = values.reduce((sum, val) => sum + val, 0) / values.length;
      
      // 简单趋势分析
      if (values.length >= 2) {
        const firstHalf = values.slice(0, Math.floor(values.length / 2));
        const secondHalf = values.slice(Math.floor(values.length / 2));
        
        const firstAvg = firstHalf.reduce((sum, val) => sum + val, 0) / firstHalf.length;
        const secondAvg = secondHalf.reduce((sum, val) => sum + val, 0) / secondHalf.length;
        
        analysis.trends[metricName] = secondAvg > firstAvg ? 'worsening' : 'improving';
        
        if (analysis.trends[metricName] === 'worsening') {
          analysis.hasIssues = true;
        }
      }
    });
    
    // 检查是否有过多的差评指标
    if (analysis.poorMetrics / analysis.totalMetrics > 0.3) {
      analysis.hasIssues = true;
    }
    
    return analysis;
  }
  
  enableAutoOptimizations() {
    // 启用自动优化
    console.log('Auto optimizations enabled');
    
    // 自动图片懒加载
    this.enableImageLazyLoading();
    
    // 自动资源预加载
    this.enableResourcePreloading();
    
    // 自动代码分割
    this.enableAutomaticCodeSplitting();
  }
  
  enableImageLazyLoading() {
    // 启用图片懒加载
    if ('loading' in HTMLImageElement.prototype) {
      // 原生懒加载支持
      const images = document.querySelectorAll('img:not([loading])');
      images.forEach(img => {
        img.loading = 'lazy';
      });
    } else {
      // 回退到Intersection Observer
      this.optimizeImages();
    }
  }
  
  enableResourcePreloading() {
    // 启用资源预加载
    const importantResources = this.identifyImportantResources();
    
    importantResources.forEach(resource => {
      const link = document.createElement('link');
      link.rel = 'preload';
      link.href = resource.url;
      link.as = resource.type;
      document.head.appendChild(link);
    });
  }
  
  enableAutomaticCodeSplitting() {
    // 启用自动代码分割
    if ('import' in window) {
      // 延迟加载非关键功能
      setTimeout(() => {
        this.loadNonCriticalFeatures();
      }, 2000);
    }
  }
  
  identifyImportantResources() {
    // 识别重要资源
    return [
      { url: '/css/critical.css', type: 'style' },
      { url: '/js/main.js', type: 'script' },
      { url: '/fonts/primary.woff2', type: 'font' }
    ];
  }
  
  loadNonCriticalFeatures() {
    // 加载非关键功能
    const features = [
      './features/analytics.js',
      './features/social-sharing.js',
      './features/comments.js'
    ];
    
    features.forEach(feature => {
      import(feature).catch(error => {
        console.warn(`Failed to load feature: ${feature}`, error);
      });
    });
  }
  
  // 获取优化报告
  getOptimizationReport() {
    const appliedOptimizations = this.optimizations.filter(opt => opt.applied);
    const failedOptimizations = this.optimizations.filter(opt => !opt.applied);
    
    return {
      summary: {
        totalOptimizations: this.optimizations.length,
        appliedOptimizations: appliedOptimizations.length,
        failedOptimizations: failedOptimizations.length,
        successRate: this.optimizations.length > 0 
          ? (appliedOptimizations.length / this.optimizations.length * 100).toFixed(2) + '%'
          : '0%'
      },
      optimizations: this.optimizations.slice(-20), // 最近20个优化
      performanceHistory: this.performanceHistory.slice(-50), // 最近50个性能指标
      timestamp: new Date().toISOString()
    };
  }
}

// 创建性能优化器实例
export const performanceOptimizer = new PerformanceOptimizer({
  enableAutoOptimization: true,
  enableResourceOptimization: true,
  enableCodeSplitting: true,
  enableCaching: true
});
```

### 3.2 实时性能监控仪表板

```javascript
// performanceDashboard.js
import * as Sentry from '@sentry/browser';
import { webVitalsMonitor } from './webVitalsMonitor.js';
import { advancedPerformanceMonitor } from './advancedPerformanceMonitor.js';
import { userExperienceMonitor } from './userExperienceMonitor.js';
import { performanceOptimizer } from './performanceOptimizer.js';

// 性能监控仪表板
export class PerformanceDashboard {
  constructor(options = {}) {
    this.options = {
      updateInterval: 5000, // 5秒更新间隔
      enableRealTimeUpdates: true,
      enableAlerts: true,
      alertThresholds: {
        LCP: 4000,
        FID: 300,
        CLS: 0.25,
        errorRate: 0.05
      },
      ...options
    };
    
    this.dashboardData = {
      webVitals: {},
      performance: {},
      userExperience: {},
      optimizations: {},
      alerts: []
    };
    
    this.setupDashboard();
  }
  
  setupDashboard() {
    if (this.options.enableRealTimeUpdates) {
      this.startRealTimeUpdates();
    }
    
    this.createDashboardUI();
    this.setupEventListeners();
  }
  
  startRealTimeUpdates() {
    setInterval(() => {
      this.updateDashboardData();
      this.updateDashboardUI();
      this.checkAlerts();
    }, this.options.updateInterval);
  }
  
  updateDashboardData() {
    // 更新Web Vitals数据
    this.dashboardData.webVitals = {
      ...webVitalsMonitor.getAllVitals(),
      score: webVitalsMonitor.getPerformanceScore()
    };
    
    // 更新高级性能数据
    this.dashboardData.performance = advancedPerformanceMonitor.getPerformanceSummary();
    
    // 更新用户体验数据
    this.dashboardData.userExperience = {
      score: userExperienceMonitor.getUserExperienceScore(),
      metrics: userExperienceMonitor.pageMetrics
    };
    
    // 更新优化数据
    this.dashboardData.optimizations = performanceOptimizer.getOptimizationReport();
  }
  
  createDashboardUI() {
    // 创建仪表板UI
    const dashboardContainer = document.createElement('div');
    dashboardContainer.id = 'performance-dashboard';
    dashboardContainer.innerHTML = `
      <div class="dashboard-header">
        <h2>性能监控仪表板</h2>
        <button id="toggle-dashboard">收起</button>
      </div>
      <div class="dashboard-content">
        <div class="metrics-grid">
          <div class="metric-card" id="web-vitals-card">
            <h3>Core Web Vitals</h3>
            <div id="web-vitals-content"></div>
          </div>
          <div class="metric-card" id="performance-card">
            <h3>性能指标</h3>
            <div id="performance-content"></div>
          </div>
          <div class="metric-card" id="ux-card">
            <h3>用户体验</h3>
            <div id="ux-content"></div>
          </div>
          <div class="metric-card" id="optimizations-card">
            <h3>优化状态</h3>
            <div id="optimizations-content"></div>
          </div>
        </div>
        <div class="alerts-section" id="alerts-section">
          <h3>性能警告</h3>
          <div id="alerts-content"></div>
        </div>
      </div>
    `;
    
    // 添加样式
    const style = document.createElement('style');
    style.textContent = this.getDashboardStyles();
    document.head.appendChild(style);
    
    // 添加到页面
    document.body.appendChild(dashboardContainer);
  }
  
  getDashboardStyles() {
    return `
      #performance-dashboard {
        position: fixed;
        top: 20px;
        right: 20px;
        width: 400px;
        max-height: 80vh;
        background: white;
        border: 1px solid #ddd;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        font-size: 14px;
        overflow: hidden;
      }
      
      .dashboard-header {
        background: #f8f9fa;
        padding: 12px 16px;
        border-bottom: 1px solid #ddd;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .dashboard-header h2 {
        margin: 0;
        font-size: 16px;
        color: #333;
      }
      
      #toggle-dashboard {
        background: #007bff;
        color: white;
        border: none;
        padding: 4px 8px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
      }
      
      .dashboard-content {
        padding: 16px;
        max-height: calc(80vh - 60px);
        overflow-y: auto;
      }
      
      .metrics-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
        margin-bottom: 16px;
      }
      
      .metric-card {
        background: #f8f9fa;
        padding: 12px;
        border-radius: 6px;
        border: 1px solid #e9ecef;
      }
      
      .metric-card h3 {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #495057;
      }
      
      .metric-value {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 4px;
      }
      
      .metric-value.good { color: #28a745; }
      .metric-value.needs-improvement { color: #ffc107; }
      .metric-value.poor { color: #dc3545; }
      
      .metric-label {
        font-size: 12px;
        color: #6c757d;
      }
      
      .alerts-section {
        border-top: 1px solid #ddd;
        padding-top: 16px;
      }
      
      .alerts-section h3 {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #495057;
      }
      
      .alert-item {
        background: #fff3cd;
        border: 1px solid #ffeaa7;
        border-radius: 4px;
        padding: 8px;
        margin-bottom: 8px;
        font-size: 12px;
      }
      
      .alert-item.error {
        background: #f8d7da;
        border-color: #f5c6cb;
      }
      
      .alert-item.warning {
        background: #fff3cd;
        border-color: #ffeaa7;
      }
      
      .dashboard-collapsed .dashboard-content {
        display: none;
      }
    `;
  }
  
  setupEventListeners() {
    // 切换仪表板显示
    document.getElementById('toggle-dashboard').addEventListener('click', () => {
      const dashboard = document.getElementById('performance-dashboard');
      dashboard.classList.toggle('dashboard-collapsed');
      
      const button = document.getElementById('toggle-dashboard');
      button.textContent = dashboard.classList.contains('dashboard-collapsed') ? '展开' : '收起';
    });
  }
  
  updateDashboardUI() {
    this.updateWebVitalsUI();
    this.updatePerformanceUI();
    this.updateUXUI();
    this.updateOptimizationsUI();
    this.updateAlertsUI();
  }
  
  updateWebVitalsUI() {
    const container = document.getElementById('web-vitals-content');
    const vitals = this.dashboardData.webVitals;
    
    let html = '';
    
    if (vitals.LCP) {
      html += `
        <div class="metric-value ${vitals.LCP.rating}">${Math.round(vitals.LCP.value)}ms</div>
        <div class="metric-label">LCP</div>
      `;
    }
    
    if (vitals.FID) {
      html += `
        <div class="metric-value ${vitals.FID.rating}">${Math.round(vitals.FID.value)}ms</div>
        <div class="metric-label">FID</div>
      `;
    }
    
    if (vitals.CLS) {
      html += `
        <div class="metric-value ${vitals.CLS.rating}">${vitals.CLS.value.toFixed(3)}</div>
        <div class="metric-label">CLS</div>
      `;
    }
    
    if (vitals.score !== undefined) {
      html += `
        <div class="metric-value ${this.getScoreRating(vitals.score)}">${vitals.score}</div>
        <div class="metric-label">总分</div>
      `;
    }
    
    container.innerHTML = html;
  }
  
  updatePerformanceUI() {
    const container = document.getElementById('performance-content');
    const perf = this.dashboardData.performance;
    
    let html = '';
    
    if (perf.resources) {
      html += `
        <div class="metric-value">${perf.resources.total}</div>
        <div class="metric-label">资源总数</div>
        <div class="metric-value ${perf.resources.slow > 0 ? 'poor' : 'good'}">${perf.resources.slow}</div>
        <div class="metric-label">慢资源</div>
      `;
    }
    
    if (perf.longTasks) {
      html += `
        <div class="metric-value ${perf.longTasks.total > 0 ? 'poor' : 'good'}">${perf.longTasks.total}</div>
        <div class="metric-label">长任务</div>
      `;
    }
    
    container.innerHTML = html;
  }
  
  updateUXUI() {
    const container = document.getElementById('ux-content');
    const ux = this.dashboardData.userExperience;
    
    let html = '';
    
    if (ux.score !== undefined) {
      html += `
        <div class="metric-value ${this.getScoreRating(ux.score)}">${ux.score}</div>
        <div class="metric-label">UX评分</div>
      `;
    }
    
    if (ux.metrics) {
      html += `
        <div class="metric-value">${ux.metrics.clickCount}</div>
        <div class="metric-label">点击次数</div>
        <div class="metric-value">${ux.metrics.scrollDepth}%</div>
        <div class="metric-label">滚动深度</div>
      `;
    }
    
    container.innerHTML = html;
  }
  
  updateOptimizationsUI() {
    const container = document.getElementById('optimizations-content');
    const opts = this.dashboardData.optimizations;
    
    let html = '';
    
    if (opts.summary) {
      html += `
        <div class="metric-value">${opts.summary.appliedOptimizations}</div>
        <div class="metric-label">已应用优化</div>
        <div class="metric-value">${opts.summary.successRate}</div>
        <div class="metric-label">成功率</div>
      `;
    }
    
    container.innerHTML = html;
  }
  
  updateAlertsUI() {
    const container = document.getElementById('alerts-content');
    const alerts = this.dashboardData.alerts;
    
    if (alerts.length === 0) {
      container.innerHTML = '<div class="metric-label">暂无警告</div>';
      return;
    }
    
    let html = '';
    alerts.slice(-5).forEach(alert => {
      html += `
        <div class="alert-item ${alert.level}">
          <strong>${alert.title}</strong><br>
          ${alert.message}
        </div>
      `;
    });
    
    container.innerHTML = html;
  }
  
  getScoreRating(score) {
    if (score >= 80) return 'good';
    if (score >= 60) return 'needs-improvement';
    return 'poor';
  }
  
  checkAlerts() {
    const newAlerts = [];
    const vitals = this.dashboardData.webVitals;
    const thresholds = this.options.alertThresholds;
    
    // 检查Web Vitals警告
    if (vitals.LCP && vitals.LCP.value > thresholds.LCP) {
      newAlerts.push({
        id: 'lcp-alert',
        level: 'warning',
        title: 'LCP过慢',
        message: `LCP: ${Math.round(vitals.LCP.value)}ms (阈值: ${thresholds.LCP}ms)`,
        timestamp: Date.now()
      });
    }
    
    if (vitals.FID && vitals.FID.value > thresholds.FID) {
      newAlerts.push({
        id: 'fid-alert',
        level: 'warning',
        title: 'FID过高',
        message: `FID: ${Math.round(vitals.FID.value)}ms (阈值: ${thresholds.FID}ms)`,
        timestamp: Date.now()
      });
    }
    
    if (vitals.CLS && vitals.CLS.value > thresholds.CLS) {
      newAlerts.push({
        id: 'cls-alert',
        level: 'warning',
        title: 'CLS过高',
        message: `CLS: ${vitals.CLS.value.toFixed(3)} (阈值: ${thresholds.CLS})`,
        timestamp: Date.now()
      });
    }
    
    // 添加新警告
    newAlerts.forEach(alert => {
      const existingAlert = this.dashboardData.alerts.find(a => a.id === alert.id);
      if (!existingAlert) {
        this.dashboardData.alerts.push(alert);
        this.sendAlertToSentry(alert);
      }
    });
    
    // 清理过期警告
    this.dashboardData.alerts = this.dashboardData.alerts.filter(
      alert => Date.now() - alert.timestamp < 300000 // 5分钟内
    );
  }
  
  sendAlertToSentry(alert) {
    Sentry.captureMessage(
      `Performance Alert: ${alert.title}`,
      alert.level === 'error' ? 'error' : 'warning',
      {
        tags: {
          category: 'performance-alert',
          alertType: alert.id
        },
        extra: alert
      }
    );
  }
  
  // 生成完整报告
  generateCompleteReport() {
    return {
      timestamp: new Date().toISOString(),
      webVitals: webVitalsMonitor.generatePerformanceReport(),
      performance: advancedPerformanceMonitor.generateDetailedReport(),
      userExperience: userExperienceMonitor.generateUXReport(),
      optimizations: performanceOptimizer.getOptimizationReport(),
      alerts: this.dashboardData.alerts,
      summary: this.generateSummary()
    };
  }
  
  generateSummary() {
    const vitals = this.dashboardData.webVitals;
    const ux = this.dashboardData.userExperience;
    const perf = this.dashboardData.performance;
    
    return {
      overallScore: Math.round((vitals.score + ux.score) / 2),
      criticalIssues: this.dashboardData.alerts.filter(a => a.level === 'error').length,
      warnings: this.dashboardData.alerts.filter(a => a.level === 'warning').length,
      optimizationsApplied: this.dashboardData.optimizations.summary?.appliedOptimizations || 0,
      recommendations: this.generateTopRecommendations()
    };
  }
  
  generateTopRecommendations() {
    const recommendations = [];
    const vitals = this.dashboardData.webVitals;
    const perf = this.dashboardData.performance;
    
    // 基于当前数据生成建议
    if (vitals.LCP && vitals.LCP.rating === 'poor') {
      recommendations.push('优化LCP：压缩图片、使用CDN、优化服务器响应时间');
    }
    
    if (perf.longTasks && perf.longTasks.total > 0) {
      recommendations.push('减少长任务：拆分大型JavaScript包、使用代码分割');
    }
    
    if (perf.resources && perf.resources.slow > 0) {
      recommendations.push('优化资源加载：启用压缩、使用现代图片格式、实施懒加载');
    }
    
    return recommendations.slice(0, 3); // 返回前3个建议
  }
}

// 创建性能监控仪表板实例
export const performanceDashboard = new PerformanceDashboard({
  updateInterval: 5000,
  enableRealTimeUpdates: true,
  enableAlerts: true,
  alertThresholds: {
    LCP: 3000,
    FID: 200,
    CLS: 0.2,
    errorRate: 0.03
  }
});
```

## 四、核心价值与实施建议

### 4.1 核心价值

1. **全面性能监控**
   - 覆盖Core Web Vitals、资源加载、长任务等关键指标
   - 实时监控用户体验和交互质量
   - 提供完整的性能分析和优化建议

2. **智能优化策略**
   - 基于性能数据自动触发优化措施
   - 支持多种优化技术的自动应用
   - 提供优化效果的实时反馈

3. **用户体验洞察**
   - 深入分析用户行为和交互模式
   - 识别用户体验痛点和改进机会
   - 提供个性化的优化建议

### 4.2 实施建议

1. **分阶段实施**
   - 第一阶段：基础监控（Core Web Vitals + 错误监控）
   - 第二阶段：高级监控（资源性能 + 用户体验）
   - 第三阶段：智能优化（自动优化 + 实时仪表板）

2. **团队协作**
   - 建立性能监控团队和流程
   - 定期审查性能数据和优化效果
   - 制定性能预算和目标

3. **持续改进**
   - 定期更新监控策略和优化算法
   - 跟踪行业最佳实践和新技术
   - 建立性能文化和意识

## 五、未来发展趋势

### 5.1 AI驱动的性能优化

- **机器学习预测**：基于历史数据预测性能问题
- **智能资源调度**：动态调整资源加载策略
- **个性化优化**：根据用户行为定制优化方案

### 5.2 边缘计算集成

- **边缘性能监控**：在CDN节点部署监控
- **就近优化处理**：在边缘节点执行优化
- **全球性能统一**：提供一致的全球用户体验

### 5.3 实时协作优化

- **团队实时协作**：多人同时监控和优化
- **自动化工作流**：集成CI/CD的性能优化
- **智能告警系统**：基于上下文的智能通知

## 总结

通过Sentry的深度性能监控实践，我们可以构建一个全面、智能、实时的前端性能监控体系。从Core Web Vitals的精确追踪到用户体验的深度分析，从智能优化策略的自动应用到实时监控仪表板的直观展示，这套解决方案为前端性能优化提供了完整的工具链和方法论。

关键在于将监控、分析、优化形成闭环，通过数据驱动的方式持续改进用户体验。随着Web技术的不断发展，性能监控也将朝着更加智能化、自动化的方向演进，为用户提供更加流畅、高效的Web体验。