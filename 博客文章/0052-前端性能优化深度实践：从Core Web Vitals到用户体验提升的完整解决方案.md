# 前端性能优化深度实践：从Core Web Vitals到用户体验提升的完整解决方案

在现代Web开发中，性能优化已经成为决定用户体验和业务成功的关键因素。随着Google将Core Web Vitals纳入搜索排名算法，前端性能优化的重要性更是达到了前所未有的高度。本文将深入探讨前端性能优化的核心理念、实践方法和监控体系，帮助开发者构建高性能的Web应用。

## 1. 性能优化基础架构

### 1.1 性能监控与分析系统

```javascript
// 性能监控管理器
class PerformanceMonitoringManager {
  constructor(options = {}) {
    this.options = {
      enableRUM: true, // Real User Monitoring
      enableSynthetic: true, // 合成监控
      sampleRate: 0.1, // 采样率
      reportingEndpoint: '/api/performance',
      thresholds: {
        LCP: 2500, // Largest Contentful Paint
        FID: 100,  // First Input Delay
        CLS: 0.1,  // Cumulative Layout Shift
        FCP: 1800, // First Contentful Paint
        TTFB: 800  // Time to First Byte
      },
      ...options
    };
    
    this.metrics = new Map();
    this.observers = new Map();
    this.reportQueue = [];
    this.isReporting = false;
    
    this.init();
  }
  
  // 初始化性能监控
  async init() {
    try {
      // 初始化Core Web Vitals监控
      await this.initCoreWebVitals();
      
      // 初始化自定义性能指标
      await this.initCustomMetrics();
      
      // 初始化资源监控
      await this.initResourceMonitoring();
      
      // 初始化用户交互监控
      await this.initUserInteractionMonitoring();
      
      // 启动定期报告
      this.startPeriodicReporting();
      
      console.log('性能监控系统初始化完成');
    } catch (error) {
      console.error('性能监控系统初始化失败:', error);
    }
  }
  
  // 初始化Core Web Vitals监控
  async initCoreWebVitals() {
    // LCP监控
    this.observeLCP();
    
    // FID监控
    this.observeFID();
    
    // CLS监控
    this.observeCLS();
    
    // FCP监控
    this.observeFCP();
    
    // TTFB监控
    this.observeTTFB();
  }
  
  // 监控Largest Contentful Paint
  observeLCP() {
    if (!('PerformanceObserver' in window)) return;
    
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      
      this.recordMetric('LCP', {
        value: lastEntry.startTime,
        element: lastEntry.element,
        url: lastEntry.url,
        timestamp: Date.now()
      });
    });
    
    observer.observe({ entryTypes: ['largest-contentful-paint'] });
    this.observers.set('LCP', observer);
  }
  
  // 监控First Input Delay
  observeFID() {
    if (!('PerformanceObserver' in window)) return;
    
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        this.recordMetric('FID', {
          value: entry.processingStart - entry.startTime,
          eventType: entry.name,
          timestamp: Date.now()
        });
      });
    });
    
    observer.observe({ entryTypes: ['first-input'] });
    this.observers.set('FID', observer);
  }
  
  // 监控Cumulative Layout Shift
  observeCLS() {
    if (!('PerformanceObserver' in window)) return;
    
    let clsValue = 0;
    let sessionValue = 0;
    let sessionEntries = [];
    
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        // 只计算非用户输入引起的布局偏移
        if (!entry.hadRecentInput) {
          const firstSessionEntry = sessionEntries[0];
          const lastSessionEntry = sessionEntries[sessionEntries.length - 1];
          
          // 如果条目与上一个条目的时间间隔小于1秒且
          // 与会话中第一个条目的时间间隔小于5秒，则将条目包含在当前会话中
          if (sessionValue &&
              entry.startTime - lastSessionEntry.startTime < 1000 &&
              entry.startTime - firstSessionEntry.startTime < 5000) {
            sessionValue += entry.value;
            sessionEntries.push(entry);
          } else {
            sessionValue = entry.value;
            sessionEntries = [entry];
          }
          
          // 如果当前会话值大于当前CLS值，则更新CLS
          if (sessionValue > clsValue) {
            clsValue = sessionValue;
            
            this.recordMetric('CLS', {
              value: clsValue,
              entries: sessionEntries.map(e => ({
                element: e.sources?.[0]?.node,
                value: e.value,
                startTime: e.startTime
              })),
              timestamp: Date.now()
            });
          }
        }
      });
    });
    
    observer.observe({ entryTypes: ['layout-shift'] });
    this.observers.set('CLS', observer);
  }
  
  // 监控First Contentful Paint
  observeFCP() {
    if (!('PerformanceObserver' in window)) return;
    
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        if (entry.name === 'first-contentful-paint') {
          this.recordMetric('FCP', {
            value: entry.startTime,
            timestamp: Date.now()
          });
        }
      });
    });
    
    observer.observe({ entryTypes: ['paint'] });
    this.observers.set('FCP', observer);
  }
  
  // 监控Time to First Byte
  observeTTFB() {
    if (!('PerformanceObserver' in window)) return;
    
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        if (entry.entryType === 'navigation') {
          this.recordMetric('TTFB', {
            value: entry.responseStart - entry.requestStart,
            timestamp: Date.now()
          });
        }
      });
    });
    
    observer.observe({ entryTypes: ['navigation'] });
    this.observers.set('TTFB', observer);
  }
  
  // 初始化自定义性能指标
  async initCustomMetrics() {
    // 页面加载时间
    this.measurePageLoadTime();
    
    // 资源加载时间
    this.measureResourceLoadTime();
    
    // JavaScript执行时间
    this.measureJavaScriptExecutionTime();
    
    // 内存使用情况
    this.measureMemoryUsage();
    
    // 网络连接质量
    this.measureNetworkQuality();
  }
  
  // 测量页面加载时间
  measurePageLoadTime() {
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0];
      
      this.recordMetric('PageLoadTime', {
        domContentLoaded: navigation.domContentLoadedEventEnd - navigation.domContentLoadedEventStart,
        loadComplete: navigation.loadEventEnd - navigation.loadEventStart,
        totalTime: navigation.loadEventEnd - navigation.navigationStart,
        timestamp: Date.now()
      });
    });
  }
  
  // 测量资源加载时间
  measureResourceLoadTime() {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        if (entry.entryType === 'resource') {
          this.recordMetric('ResourceLoadTime', {
            name: entry.name,
            type: this.getResourceType(entry),
            duration: entry.duration,
            size: entry.transferSize,
            cached: entry.transferSize === 0 && entry.decodedBodySize > 0,
            timestamp: Date.now()
          });
        }
      });
    });
    
    observer.observe({ entryTypes: ['resource'] });
    this.observers.set('ResourceLoadTime', observer);
  }
  
  // 测量JavaScript执行时间
  measureJavaScriptExecutionTime() {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        if (entry.entryType === 'measure' && entry.name.startsWith('js-execution')) {
          this.recordMetric('JavaScriptExecutionTime', {
            name: entry.name,
            duration: entry.duration,
            timestamp: Date.now()
          });
        }
      });
    });
    
    observer.observe({ entryTypes: ['measure'] });
    this.observers.set('JavaScriptExecutionTime', observer);
  }
  
  // 测量内存使用情况
  measureMemoryUsage() {
    if ('memory' in performance) {
      setInterval(() => {
        this.recordMetric('MemoryUsage', {
          usedJSHeapSize: performance.memory.usedJSHeapSize,
          totalJSHeapSize: performance.memory.totalJSHeapSize,
          jsHeapSizeLimit: performance.memory.jsHeapSizeLimit,
          timestamp: Date.now()
        });
      }, 30000); // 每30秒记录一次
    }
  }
  
  // 测量网络连接质量
  measureNetworkQuality() {
    if ('connection' in navigator) {
      const connection = navigator.connection;
      
      this.recordMetric('NetworkQuality', {
        effectiveType: connection.effectiveType,
        downlink: connection.downlink,
        rtt: connection.rtt,
        saveData: connection.saveData,
        timestamp: Date.now()
      });
      
      // 监听网络变化
      connection.addEventListener('change', () => {
        this.recordMetric('NetworkQuality', {
          effectiveType: connection.effectiveType,
          downlink: connection.downlink,
          rtt: connection.rtt,
          saveData: connection.saveData,
          timestamp: Date.now()
        });
      });
    }
  }
  
  // 初始化资源监控
  async initResourceMonitoring() {
    this.resourceMonitor = new ResourceMonitor({
      onResourceLoad: (resource) => {
        this.analyzeResourcePerformance(resource);
      },
      onResourceError: (error) => {
        this.recordResourceError(error);
      }
    });
    
    await this.resourceMonitor.start();
  }
  
  // 初始化用户交互监控
  async initUserInteractionMonitoring() {
    this.interactionMonitor = new UserInteractionMonitor({
      onInteraction: (interaction) => {
        this.recordUserInteraction(interaction);
      },
      onLongTask: (task) => {
        this.recordLongTask(task);
      }
    });
    
    await this.interactionMonitor.start();
  }
  
  // 记录性能指标
  recordMetric(name, data) {
    if (!this.shouldSample()) return;
    
    const metric = {
      name,
      data,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: Date.now(),
      sessionId: this.getSessionId()
    };
    
    // 存储到本地
    if (!this.metrics.has(name)) {
      this.metrics.set(name, []);
    }
    this.metrics.get(name).push(metric);
    
    // 添加到报告队列
    this.reportQueue.push(metric);
    
    // 检查阈值
    this.checkThresholds(name, data);
    
    // 触发实时报告（如果需要）
    if (this.shouldReportImmediately(name, data)) {
      this.reportImmediately(metric);
    }
  }
  
  // 检查是否应该采样
  shouldSample() {
    return Math.random() < this.options.sampleRate;
  }
  
  // 检查阈值
  checkThresholds(name, data) {
    const threshold = this.options.thresholds[name];
    if (!threshold) return;
    
    const value = data.value || data.duration;
    if (value > threshold) {
      this.recordThresholdViolation(name, value, threshold);
    }
  }
  
  // 记录阈值违规
  recordThresholdViolation(metricName, value, threshold) {
    const violation = {
      type: 'threshold_violation',
      metricName,
      value,
      threshold,
      severity: this.calculateSeverity(value, threshold),
      timestamp: Date.now()
    };
    
    console.warn(`性能阈值违规: ${metricName} = ${value}ms (阈值: ${threshold}ms)`);
    
    // 立即报告严重违规
    if (violation.severity === 'critical') {
      this.reportImmediately(violation);
    }
  }
  
  // 计算严重程度
  calculateSeverity(value, threshold) {
    const ratio = value / threshold;
    
    if (ratio >= 3) return 'critical';
    if (ratio >= 2) return 'high';
    if (ratio >= 1.5) return 'medium';
    return 'low';
  }
  
  // 判断是否需要立即报告
  shouldReportImmediately(name, data) {
    // Core Web Vitals超过阈值时立即报告
    const coreMetrics = ['LCP', 'FID', 'CLS'];
    if (coreMetrics.includes(name)) {
      const threshold = this.options.thresholds[name];
      const value = data.value;
      return value > threshold * 2; // 超过阈值2倍时立即报告
    }
    
    return false;
  }
  
  // 立即报告
  async reportImmediately(metric) {
    try {
      await fetch(this.options.reportingEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          type: 'immediate',
          metrics: [metric]
        })
      });
    } catch (error) {
      console.error('立即报告失败:', error);
    }
  }
  
  // 启动定期报告
  startPeriodicReporting() {
    setInterval(() => {
      this.reportMetrics();
    }, 30000); // 每30秒报告一次
    
    // 页面卸载时报告
    window.addEventListener('beforeunload', () => {
      this.reportMetrics(true);
    });
    
    // 页面隐藏时报告
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') {
        this.reportMetrics(true);
      }
    });
  }
  
  // 报告性能指标
  async reportMetrics(isBeacon = false) {
    if (this.isReporting || this.reportQueue.length === 0) return;
    
    this.isReporting = true;
    
    try {
      const metricsToReport = [...this.reportQueue];
      this.reportQueue = [];
      
      const payload = {
        type: 'batch',
        metrics: metricsToReport,
        meta: {
          url: window.location.href,
          userAgent: navigator.userAgent,
          timestamp: Date.now(),
          sessionId: this.getSessionId()
        }
      };
      
      if (isBeacon && 'sendBeacon' in navigator) {
        // 使用sendBeacon确保数据能够发送
        navigator.sendBeacon(
          this.options.reportingEndpoint,
          JSON.stringify(payload)
        );
      } else {
        await fetch(this.options.reportingEndpoint, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        });
      }
      
      console.log(`已报告 ${metricsToReport.length} 个性能指标`);
    } catch (error) {
      console.error('性能指标报告失败:', error);
      // 将失败的指标重新加入队列
      this.reportQueue.unshift(...metricsToReport);
    } finally {
      this.isReporting = false;
    }
  }
  
  // 获取会话ID
  getSessionId() {
    let sessionId = sessionStorage.getItem('performance_session_id');
    if (!sessionId) {
      sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      sessionStorage.setItem('performance_session_id', sessionId);
    }
    return sessionId;
  }
  
  // 获取资源类型
  getResourceType(entry) {
    if (entry.initiatorType) {
      return entry.initiatorType;
    }
    
    const url = entry.name;
    if (url.match(/\.(js|mjs)$/)) return 'script';
    if (url.match(/\.(css)$/)) return 'stylesheet';
    if (url.match(/\.(png|jpg|jpeg|gif|svg|webp)$/)) return 'image';
    if (url.match(/\.(woff|woff2|ttf|eot)$/)) return 'font';
    
    return 'other';
  }
  
  // 分析资源性能
  analyzeResourcePerformance(resource) {
    const analysis = {
      name: resource.name,
      type: this.getResourceType(resource),
      loadTime: resource.duration,
      size: resource.transferSize,
      cached: resource.transferSize === 0 && resource.decodedBodySize > 0,
      compression: this.calculateCompressionRatio(resource),
      timing: {
        dns: resource.domainLookupEnd - resource.domainLookupStart,
        tcp: resource.connectEnd - resource.connectStart,
        ssl: resource.secureConnectionStart > 0 ? resource.connectEnd - resource.secureConnectionStart : 0,
        ttfb: resource.responseStart - resource.requestStart,
        download: resource.responseEnd - resource.responseStart
      }
    };
    
    this.recordMetric('ResourcePerformance', analysis);
    
    // 检查资源性能问题
    this.checkResourceIssues(analysis);
  }
  
  // 计算压缩比
  calculateCompressionRatio(resource) {
    if (resource.decodedBodySize === 0) return 0;
    return (resource.decodedBodySize - resource.encodedBodySize) / resource.decodedBodySize;
  }
  
  // 检查资源问题
  checkResourceIssues(analysis) {
    const issues = [];
    
    // 检查加载时间
    if (analysis.loadTime > 3000) {
      issues.push({
        type: 'slow_resource',
        message: `资源加载时间过长: ${analysis.loadTime}ms`,
        severity: 'high'
      });
    }
    
    // 检查文件大小
    if (analysis.size > 1024 * 1024) { // 1MB
      issues.push({
        type: 'large_resource',
        message: `资源文件过大: ${(analysis.size / 1024 / 1024).toFixed(2)}MB`,
        severity: 'medium'
      });
    }
    
    // 检查压缩
    if (analysis.compression < 0.3 && analysis.size > 10240) { // 10KB
      issues.push({
        type: 'poor_compression',
        message: `资源压缩率过低: ${(analysis.compression * 100).toFixed(1)}%`,
        severity: 'low'
      });
    }
    
    if (issues.length > 0) {
      this.recordMetric('ResourceIssues', {
        resource: analysis.name,
        issues,
        timestamp: Date.now()
      });
    }
  }
  
  // 记录资源错误
  recordResourceError(error) {
    this.recordMetric('ResourceError', {
      url: error.target?.src || error.target?.href,
      type: error.target?.tagName,
      message: error.message,
      timestamp: Date.now()
    });
  }
  
  // 记录用户交互
  recordUserInteraction(interaction) {
    this.recordMetric('UserInteraction', {
      type: interaction.type,
      target: interaction.target,
      duration: interaction.duration,
      timestamp: Date.now()
    });
  }
  
  // 记录长任务
  recordLongTask(task) {
    this.recordMetric('LongTask', {
      duration: task.duration,
      startTime: task.startTime,
      attribution: task.attribution,
      timestamp: Date.now()
    });
  }
  
  // 获取性能摘要
  getPerformanceSummary() {
    const summary = {
      coreWebVitals: {},
      customMetrics: {},
      resourceStats: {},
      issues: []
    };
    
    // Core Web Vitals摘要
    ['LCP', 'FID', 'CLS', 'FCP', 'TTFB'].forEach(metric => {
      const data = this.metrics.get(metric);
      if (data && data.length > 0) {
        const values = data.map(d => d.data.value).filter(v => v !== undefined);
        if (values.length > 0) {
          summary.coreWebVitals[metric] = {
            current: values[values.length - 1],
            average: values.reduce((a, b) => a + b, 0) / values.length,
            p95: this.calculatePercentile(values, 95),
            threshold: this.options.thresholds[metric],
            status: this.getMetricStatus(metric, values[values.length - 1])
          };
        }
      }
    });
    
    return summary;
  }
  
  // 计算百分位数
  calculatePercentile(values, percentile) {
    const sorted = values.sort((a, b) => a - b);
    const index = Math.ceil((percentile / 100) * sorted.length) - 1;
    return sorted[index];
  }
  
  // 获取指标状态
  getMetricStatus(metric, value) {
    const threshold = this.options.thresholds[metric];
    if (!threshold) return 'unknown';
    
    if (value <= threshold) return 'good';
    if (value <= threshold * 1.5) return 'needs_improvement';
    return 'poor';
  }
  
  // 销毁监控器
  destroy() {
    // 停止所有观察器
    this.observers.forEach(observer => {
      observer.disconnect();
    });
    this.observers.clear();
    
    // 最后一次报告
    this.reportMetrics(true);
    
    // 清理资源
    this.metrics.clear();
    this.reportQueue = [];
    
    console.log('性能监控系统已销毁');
  }
}
```

### 1.3 用户交互监控器

```javascript
// 用户交互监控器
class UserInteractionMonitor {
  constructor(options = {}) {
    this.options = {
      trackClicks: true,
      trackScrolls: true,
      trackKeyboard: true,
      trackTouch: true,
      trackLongTasks: true,
      scrollThreshold: 25, // 滚动阈值（像素）
      debounceTime: 100, // 防抖时间（毫秒）
      ...options
    };
    
    this.interactions = [];
    this.longTasks = [];
    this.scrollPosition = 0;
    this.lastScrollTime = 0;
    
    this.onInteraction = options.onInteraction || (() => {});
    this.onLongTask = options.onLongTask || (() => {});
  }
  
  async start() {
    // 监控点击事件
    if (this.options.trackClicks) {
      this.trackClickEvents();
    }
    
    // 监控滚动事件
    if (this.options.trackScrolls) {
      this.trackScrollEvents();
    }
    
    // 监控键盘事件
    if (this.options.trackKeyboard) {
      this.trackKeyboardEvents();
    }
    
    // 监控触摸事件
    if (this.options.trackTouch) {
      this.trackTouchEvents();
    }
    
    // 监控长任务
    if (this.options.trackLongTasks) {
      this.trackLongTasks();
    }
    
    console.log('用户交互监控器已启动');
  }
  
  // 监控点击事件
  trackClickEvents() {
    document.addEventListener('click', (event) => {
      const interaction = {
        type: 'click',
        target: this.getElementSelector(event.target),
        timestamp: Date.now(),
        coordinates: {
          x: event.clientX,
          y: event.clientY
        },
        viewport: {
          width: window.innerWidth,
          height: window.innerHeight
        }
      };
      
      this.recordInteraction(interaction);
    }, { passive: true });
  }
  
  // 监控滚动事件
  trackScrollEvents() {
    const throttledScrollHandler = this.throttle(() => {
      const currentPosition = window.pageYOffset;
      const scrollDelta = Math.abs(currentPosition - this.scrollPosition);
      
      if (scrollDelta >= this.options.scrollThreshold) {
        const interaction = {
          type: 'scroll',
          timestamp: Date.now(),
          position: currentPosition,
          delta: scrollDelta,
          direction: currentPosition > this.scrollPosition ? 'down' : 'up',
          viewport: {
            width: window.innerWidth,
            height: window.innerHeight
          },
          documentHeight: document.documentElement.scrollHeight
        };
        
        this.recordInteraction(interaction);
        this.scrollPosition = currentPosition;
      }
    }, this.options.debounceTime);
    
    window.addEventListener('scroll', throttledScrollHandler, { passive: true });
  }
  
  // 监控键盘事件
  trackKeyboardEvents() {
    document.addEventListener('keydown', (event) => {
      const interaction = {
        type: 'keydown',
        key: event.key,
        code: event.code,
        target: this.getElementSelector(event.target),
        timestamp: Date.now(),
        modifiers: {
          ctrl: event.ctrlKey,
          alt: event.altKey,
          shift: event.shiftKey,
          meta: event.metaKey
        }
      };
      
      this.recordInteraction(interaction);
    }, { passive: true });
  }
  
  // 监控触摸事件
  trackTouchEvents() {
    let touchStartTime = 0;
    let touchStartPosition = { x: 0, y: 0 };
    
    document.addEventListener('touchstart', (event) => {
      touchStartTime = Date.now();
      const touch = event.touches[0];
      touchStartPosition = {
        x: touch.clientX,
        y: touch.clientY
      };
    }, { passive: true });
    
    document.addEventListener('touchend', (event) => {
      const touchEndTime = Date.now();
      const duration = touchEndTime - touchStartTime;
      
      const interaction = {
        type: 'touch',
        target: this.getElementSelector(event.target),
        timestamp: touchEndTime,
        duration,
        startPosition: touchStartPosition,
        touchCount: event.changedTouches.length
      };
      
      this.recordInteraction(interaction);
    }, { passive: true });
    
    // 监控滑动手势
    document.addEventListener('touchmove', this.throttle((event) => {
      const touch = event.touches[0];
      const currentPosition = {
        x: touch.clientX,
        y: touch.clientY
      };
      
      const deltaX = currentPosition.x - touchStartPosition.x;
      const deltaY = currentPosition.y - touchStartPosition.y;
      const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      
      if (distance > 10) { // 最小滑动距离
        const interaction = {
          type: 'swipe',
          target: this.getElementSelector(event.target),
          timestamp: Date.now(),
          startPosition: touchStartPosition,
          currentPosition,
          delta: { x: deltaX, y: deltaY },
          distance
        };
        
        this.recordInteraction(interaction);
      }
    }, 100), { passive: true });
  }
  
  // 监控长任务
  trackLongTasks() {
    if (!('PerformanceObserver' in window)) return;
    
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        const longTask = {
          duration: entry.duration,
          startTime: entry.startTime,
          name: entry.name,
          attribution: entry.attribution ? entry.attribution.map(attr => ({
            name: attr.name,
            entryType: attr.entryType,
            startTime: attr.startTime,
            duration: attr.duration
          })) : [],
          timestamp: Date.now()
        };
        
        this.recordLongTask(longTask);
      });
    });
    
    observer.observe({ entryTypes: ['longtask'] });
  }
  
  // 记录交互
  recordInteraction(interaction) {
    this.interactions.push(interaction);
    this.onInteraction(interaction);
    
    // 限制存储的交互数量
    if (this.interactions.length > 1000) {
      this.interactions = this.interactions.slice(-500);
    }
  }
  
  // 记录长任务
  recordLongTask(longTask) {
    this.longTasks.push(longTask);
    this.onLongTask(longTask);
    
    // 限制存储的长任务数量
    if (this.longTasks.length > 100) {
      this.longTasks = this.longTasks.slice(-50);
    }
  }
  
  // 获取元素选择器
  getElementSelector(element) {
    if (!element || element === document) return 'document';
    
    // 尝试使用ID
    if (element.id) {
      return `#${element.id}`;
    }
    
    // 尝试使用类名
    if (element.className && typeof element.className === 'string') {
      const classes = element.className.split(' ').filter(c => c.trim());
      if (classes.length > 0) {
        return `${element.tagName.toLowerCase()}.${classes[0]}`;
      }
    }
    
    // 使用标签名和位置
    const tagName = element.tagName.toLowerCase();
    const parent = element.parentElement;
    
    if (parent) {
      const siblings = Array.from(parent.children).filter(child => 
        child.tagName.toLowerCase() === tagName
      );
      const index = siblings.indexOf(element);
      return `${tagName}:nth-of-type(${index + 1})`;
    }
    
    return tagName;
  }
  
  // 节流函数
  throttle(func, limit) {
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
  
  // 获取交互统计
  getInteractionStats() {
    const stats = {
      total: this.interactions.length,
      byType: {},
      longTasksCount: this.longTasks.length,
      averageLongTaskDuration: 0
    };
    
    // 按类型统计交互
    this.interactions.forEach(interaction => {
      if (!stats.byType[interaction.type]) {
        stats.byType[interaction.type] = 0;
      }
      stats.byType[interaction.type]++;
    });
    
    // 计算长任务平均持续时间
    if (this.longTasks.length > 0) {
      const totalDuration = this.longTasks.reduce((sum, task) => sum + task.duration, 0);
      stats.averageLongTaskDuration = totalDuration / this.longTasks.length;
    }
    
    return stats;
  }
  
  // 获取最近的交互
  getRecentInteractions(timeWindow = 60000) { // 默认1分钟
    const cutoff = Date.now() - timeWindow;
    return this.interactions.filter(interaction => interaction.timestamp > cutoff);
  }
  
  // 获取最近的长任务
  getRecentLongTasks(timeWindow = 60000) {
    const cutoff = Date.now() - timeWindow;
    return this.longTasks.filter(task => task.timestamp > cutoff);
  }
  
  // 清理数据
  clear() {
    this.interactions = [];
    this.longTasks = [];
  }
}
```

## 2. 性能优化策略实现

### 2.1 资源优化管理器

```javascript
// 资源优化管理器
class ResourceOptimizationManager {
  constructor(options = {}) {
    this.options = {
      enableImageOptimization: true,
      enableScriptOptimization: true,
      enableStyleOptimization: true,
      enableFontOptimization: true,
      enableCaching: true,
      enableCompression: true,
      enableLazyLoading: true,
      enablePreloading: true,
      ...options
    };
    
    this.optimizations = new Map();
    this.loadingQueue = new Set();
    this.preloadedResources = new Set();
    
    this.init();
  }
  
  async init() {
    try {
      // 初始化图片优化
      if (this.options.enableImageOptimization) {
        await this.initImageOptimization();
      }
      
      // 初始化脚本优化
      if (this.options.enableScriptOptimization) {
        await this.initScriptOptimization();
      }
      
      // 初始化样式优化
      if (this.options.enableStyleOptimization) {
        await this.initStyleOptimization();
      }
      
      // 初始化字体优化
      if (this.options.enableFontOptimization) {
        await this.initFontOptimization();
      }
      
      // 初始化懒加载
      if (this.options.enableLazyLoading) {
        await this.initLazyLoading();
      }
      
      // 初始化预加载
      if (this.options.enablePreloading) {
        await this.initPreloading();
      }
      
      console.log('资源优化管理器初始化完成');
    } catch (error) {
      console.error('资源优化管理器初始化失败:', error);
    }
  }
  
  // 初始化图片优化
  async initImageOptimization() {
    this.imageOptimizer = new ImageOptimizer({
      enableWebP: true,
      enableAVIF: true,
      enableResponsive: true,
      enableLazyLoading: true,
      quality: 85,
      onOptimization: (result) => {
        this.recordOptimization('image', result);
      }
    });
    
    await this.imageOptimizer.start();
  }
  
  // 初始化脚本优化
  async initScriptOptimization() {
    this.scriptOptimizer = new ScriptOptimizer({
      enableMinification: true,
      enableTreeShaking: true,
      enableCodeSplitting: true,
      enableAsyncLoading: true,
      onOptimization: (result) => {
        this.recordOptimization('script', result);
      }
    });
    
    await this.scriptOptimizer.start();
  }
  
  // 初始化样式优化
  async initStyleOptimization() {
    this.styleOptimizer = new StyleOptimizer({
      enableMinification: true,
      enableCriticalCSS: true,
      enableUnusedCSS: true,
      enableInlining: true,
      onOptimization: (result) => {
        this.recordOptimization('style', result);
      }
    });
    
    await this.styleOptimizer.start();
  }
  
  // 初始化字体优化
  async initFontOptimization() {
    this.fontOptimizer = new FontOptimizer({
      enableSubsetting: true,
      enablePreloading: true,
      enableFallbacks: true,
      enableDisplay: 'swap',
      onOptimization: (result) => {
        this.recordOptimization('font', result);
      }
    });
    
    await this.fontOptimizer.start();
  }
  
  // 初始化懒加载
  async initLazyLoading() {
    this.lazyLoader = new LazyLoader({
      rootMargin: '50px',
      threshold: 0.1,
      enableImages: true,
      enableIframes: true,
      enableScripts: true,
      onLoad: (element) => {
        this.recordOptimization('lazy_load', {
          element: element.tagName,
          src: element.src || element.dataset.src
        });
      }
    });
    
    await this.lazyLoader.start();
  }
  
  // 初始化预加载
  async initPreloading() {
    this.preloader = new ResourcePreloader({
      enableDNSPrefetch: true,
      enablePreconnect: true,
      enablePreload: true,
      enablePrefetch: true,
      onPreload: (resource) => {
        this.recordOptimization('preload', resource);
      }
    });
    
    await this.preloader.start();
  }
  
  // 记录优化结果
  recordOptimization(type, result) {
    if (!this.optimizations.has(type)) {
      this.optimizations.set(type, []);
    }
    
    this.optimizations.get(type).push({
      ...result,
      timestamp: Date.now()
    });
  }
  
  // 获取优化统计
  getOptimizationStats() {
    const stats = {
      total: 0,
      byType: {},
      savings: {
        bytes: 0,
        requests: 0,
        time: 0
      }
    };
    
    this.optimizations.forEach((optimizations, type) => {
      stats.byType[type] = {
        count: optimizations.length,
        savings: {
          bytes: 0,
          requests: 0,
          time: 0
        }
      };
      
      optimizations.forEach(opt => {
        if (opt.bytesSaved) {
          stats.byType[type].savings.bytes += opt.bytesSaved;
          stats.savings.bytes += opt.bytesSaved;
        }
        
        if (opt.requestsSaved) {
          stats.byType[type].savings.requests += opt.requestsSaved;
          stats.savings.requests += opt.requestsSaved;
        }
        
        if (opt.timeSaved) {
          stats.byType[type].savings.time += opt.timeSaved;
          stats.savings.time += opt.timeSaved;
        }
      });
      
      stats.total += optimizations.length;
    });
    
    return stats;
  }
}
```

### 2.2 图片优化器

```javascript
// 图片优化器
class ImageOptimizer {
  constructor(options = {}) {
    this.options = {
      enableWebP: true,
      enableAVIF: true,
      enableResponsive: true,
      enableLazyLoading: true,
      quality: 85,
      formats: ['avif', 'webp', 'jpg', 'png'],
      breakpoints: [320, 640, 768, 1024, 1280, 1920],
      ...options
    };
    
    this.supportedFormats = new Set();
    this.processedImages = new Map();
    
    this.onOptimization = options.onOptimization || (() => {});
  }
  
  async start() {
    // 检测浏览器支持的格式
    await this.detectSupportedFormats();
    
    // 处理现有图片
    this.processExistingImages();
    
    // 监控新图片
    this.observeNewImages();
    
    console.log('图片优化器已启动');
  }
  
  // 检测支持的格式
  async detectSupportedFormats() {
    const formats = {
      webp: 'data:image/webp;base64,UklGRjoAAABXRUJQVlA4IC4AAACyAgCdASoCAAIALmk0mk0iIiIiIgBoSygABc6WWgAA/veff/0PP8bA//LwYAAA',
      avif: 'data:image/avif;base64,AAAAIGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZk1BMUIAAADybWV0YQAAAAAAAAAoaGRscgAAAAAAAAAAcGljdAAAAAAAAAAAAAAAAGxpYmF2aWYAAAAADnBpdG0AAAAAAAEAAAAeaWxvYwAAAABEAAABAAEAAAABAAABGgAAAB0AAAAoaWluZgAAAAAAAQAAABppbmZlAgAAAAABAABhdjAxQ29sb3IAAAAAamlwcnAAAABLaXBjbwAAABRpc3BlAAAAAAAAAAIAAAACAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQ0MAAAAABNjb2xybmNseAACAAIAAYAAAAAXaXBtYQAAAAAAAAABAAEEAQKDBAAAACVtZGF0EgAKCBgABogQEAwgMg8f8D///8WfhwB8+ErK42A='
    };
    
    for (const [format, dataUrl] of Object.entries(formats)) {
      try {
        const supported = await this.testImageFormat(dataUrl);
        if (supported) {
          this.supportedFormats.add(format);
        }
      } catch (error) {
        console.warn(`格式 ${format} 检测失败:`, error);
      }
    }
    
    console.log('支持的图片格式:', Array.from(this.supportedFormats));
  }
  
  // 测试图片格式支持
  testImageFormat(dataUrl) {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = dataUrl;
    });
  }
  
  // 处理现有图片
  processExistingImages() {
    const images = document.querySelectorAll('img[src]');
    
    images.forEach(img => {
      this.optimizeImage(img);
    });
  }
  
  // 监控新图片
  observeNewImages() {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach(mutation => {
        mutation.addedNodes.forEach(node => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            if (node.tagName === 'IMG') {
              this.optimizeImage(node);
            } else {
              const images = node.querySelectorAll?.('img[src]');
              images?.forEach(img => this.optimizeImage(img));
            }
          }
        });
      });
    });
    
    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
  }
  
  // 优化图片
  async optimizeImage(img) {
    const src = img.src;
    if (!src || this.processedImages.has(src)) return;
    
    this.processedImages.set(src, true);
    
    try {
      const optimization = {
        originalSrc: src,
        element: img,
        optimizations: []
      };
      
      // 格式优化
      if (this.options.enableWebP || this.options.enableAVIF) {
        const formatOptimization = await this.optimizeFormat(img, src);
        if (formatOptimization) {
          optimization.optimizations.push(formatOptimization);
        }
      }
      
      // 响应式优化
      if (this.options.enableResponsive) {
        const responsiveOptimization = await this.optimizeResponsive(img, src);
        if (responsiveOptimization) {
          optimization.optimizations.push(responsiveOptimization);
        }
      }
      
      // 懒加载优化
      if (this.options.enableLazyLoading) {
        const lazyOptimization = this.optimizeLazyLoading(img);
        if (lazyOptimization) {
          optimization.optimizations.push(lazyOptimization);
        }
      }
      
      this.onOptimization(optimization);
    } catch (error) {
      console.error('图片优化失败:', error);
    }
  }
  
  // 格式优化
  async optimizeFormat(img, src) {
    const bestFormat = this.getBestSupportedFormat();
    if (!bestFormat || bestFormat === this.getImageFormat(src)) {
      return null;
    }
    
    const optimizedSrc = this.generateOptimizedUrl(src, {
      format: bestFormat,
      quality: this.options.quality
    });
    
    // 预加载优化后的图片
    const preloadSuccess = await this.preloadImage(optimizedSrc);
    if (preloadSuccess) {
      img.src = optimizedSrc;
      
      return {
        type: 'format',
        from: this.getImageFormat(src),
        to: bestFormat,
        estimatedSavings: this.estimateFormatSavings(src, bestFormat)
      };
    }
    
    return null;
  }
  
  // 响应式优化
  async optimizeResponsive(img, src) {
    const srcset = this.generateSrcSet(src);
    const sizes = this.generateSizes(img);
    
    if (srcset && sizes) {
      img.srcset = srcset;
      img.sizes = sizes;
      
      return {
        type: 'responsive',
        srcset,
        sizes,
        breakpoints: this.options.breakpoints
      };
    }
    
    return null;
  }
  
  // 懒加载优化
  optimizeLazyLoading(img) {
    if (this.isInViewport(img)) {
      return null; // 已在视口内，不需要懒加载
    }
    
    const originalSrc = img.src;
    img.dataset.src = originalSrc;
    img.src = this.generatePlaceholder(img);
    img.loading = 'lazy';
    
    return {
      type: 'lazy_loading',
      placeholder: img.src,
      originalSrc
    };
  }
  
  // 获取最佳支持格式
  getBestSupportedFormat() {
    const formatPriority = ['avif', 'webp', 'jpg', 'png'];
    
    for (const format of formatPriority) {
      if (this.supportedFormats.has(format)) {
        return format;
      }
    }
    
    return null;
  }
  
  // 获取图片格式
  getImageFormat(src) {
    const extension = src.split('.').pop().toLowerCase();
    const formatMap = {
      'jpg': 'jpg',
      'jpeg': 'jpg',
      'png': 'png',
      'webp': 'webp',
      'avif': 'avif'
    };
    
    return formatMap[extension] || 'unknown';
  }
  
  // 生成优化后的URL
  generateOptimizedUrl(src, options) {
    // 这里应该根据实际的图片服务来生成URL
    // 例如：Cloudinary, ImageKit, 或自定义服务
    const params = new URLSearchParams();
    
    if (options.format) {
      params.set('f', options.format);
    }
    
    if (options.quality) {
      params.set('q', options.quality);
    }
    
    if (options.width) {
      params.set('w', options.width);
    }
    
    if (options.height) {
      params.set('h', options.height);
    }
    
    return `${src}?${params.toString()}`;
  }
  
  // 生成srcset
  generateSrcSet(src) {
    return this.options.breakpoints
      .map(width => {
        const optimizedUrl = this.generateOptimizedUrl(src, { width });
        return `${optimizedUrl} ${width}w`;
      })
      .join(', ');
  }
  
  // 生成sizes
  generateSizes(img) {
    // 简化的sizes生成逻辑
    const containerWidth = img.parentElement?.offsetWidth || window.innerWidth;
    
    if (containerWidth <= 640) {
      return '100vw';
    } else if (containerWidth <= 1024) {
      return '50vw';
    } else {
      return '33vw';
    }
  }
  
  // 预加载图片
  preloadImage(src) {
    return new Promise((resolve) => {
      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = src;
    });
  }
  
  // 检查是否在视口内
  isInViewport(element) {
    const rect = element.getBoundingClientRect();
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= window.innerHeight &&
      rect.right <= window.innerWidth
    );
  }
  
  // 生成占位符
  generatePlaceholder(img) {
    const width = img.width || 300;
    const height = img.height || 200;
    
    // 生成简单的SVG占位符
    const svg = `
      <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
        <rect width="100%" height="100%" fill="#f0f0f0"/>
        <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999">Loading...</text>
      </svg>
    `;
    
    return `data:image/svg+xml;base64,${btoa(svg)}`;
  }
  
  // 估算格式节省
  estimateFormatSavings(src, newFormat) {
    const currentFormat = this.getImageFormat(src);
    
    // 简化的节省估算
    const savingsMap = {
      'png->webp': 0.3,
      'jpg->webp': 0.25,
      'png->avif': 0.5,
      'jpg->avif': 0.4,
      'webp->avif': 0.2
    };
    
    const key = `${currentFormat}->${newFormat}`;
    const savingsRatio = savingsMap[key] || 0;
    
    return {
      ratio: savingsRatio,
      estimatedBytes: 0 // 需要实际测量
    };
  }
}
```

### 2.3 缓存策略管理器

```javascript
// 缓存策略管理器
class CacheStrategyManager {
  constructor(options = {}) {
    this.options = {
      enableServiceWorker: true,
      enableMemoryCache: true,
      enableLocalStorage: true,
      enableIndexedDB: true,
      enableHTTPCache: true,
      cacheVersion: '1.0.0',
      maxMemoryCacheSize: 50 * 1024 * 1024, // 50MB
      maxLocalStorageSize: 5 * 1024 * 1024,  // 5MB
      ...options
    };
    
    this.memoryCache = new Map();
    this.memoryCacheSize = 0;
    this.cacheStrategies = new Map();
    
    this.init();
  }
  
  async init() {
    try {
      // 初始化Service Worker缓存
      if (this.options.enableServiceWorker) {
        await this.initServiceWorkerCache();
      }
      
      // 初始化内存缓存
      if (this.options.enableMemoryCache) {
        this.initMemoryCache();
      }
      
      // 初始化本地存储缓存
      if (this.options.enableLocalStorage) {
        this.initLocalStorageCache();
      }
      
      // 初始化IndexedDB缓存
      if (this.options.enableIndexedDB) {
        await this.initIndexedDBCache();
      }
      
      // 设置缓存策略
      this.setupCacheStrategies();
      
      console.log('缓存策略管理器初始化完成');
    } catch (error) {
      console.error('缓存策略管理器初始化失败:', error);
    }
  }
  
  // 初始化Service Worker缓存
  async initServiceWorkerCache() {
    if (!('serviceWorker' in navigator)) {
      console.warn('Service Worker不支持');
      return;
    }
    
    try {
      const registration = await navigator.serviceWorker.register('/sw.js');
      console.log('Service Worker注册成功:', registration);
      
      // 监听Service Worker消息
      navigator.serviceWorker.addEventListener('message', (event) => {
        if (event.data.type === 'CACHE_UPDATE') {
          console.log('缓存更新:', event.data.payload);
        }
      });
    } catch (error) {
      console.error('Service Worker注册失败:', error);
    }
  }
  
  // 初始化内存缓存
  initMemoryCache() {
    this.memoryCache = new Map();
    this.memoryCacheSize = 0;
    
    // 定期清理过期缓存
    setInterval(() => {
      this.cleanExpiredMemoryCache();
    }, 60000); // 每分钟清理一次
  }
  
  // 初始化本地存储缓存
  initLocalStorageCache() {
    try {
      // 检查localStorage可用性
      const testKey = '__cache_test__';
      localStorage.setItem(testKey, 'test');
      localStorage.removeItem(testKey);
      
      // 清理过期的localStorage缓存
      this.cleanExpiredLocalStorageCache();
    } catch (error) {
      console.warn('localStorage不可用:', error);
    }
  }
  
  // 初始化IndexedDB缓存
  async initIndexedDBCache() {
    if (!('indexedDB' in window)) {
      console.warn('IndexedDB不支持');
      return;
    }
    
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('PerformanceCache', 1);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.indexedDB = request.result;
        resolve(this.indexedDB);
      };
      
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        
        // 创建对象存储
        if (!db.objectStoreNames.contains('resources')) {
          const store = db.createObjectStore('resources', { keyPath: 'url' });
          store.createIndex('timestamp', 'timestamp', { unique: false });
          store.createIndex('type', 'type', { unique: false });
        }
      };
    });
  }
  
  // 设置缓存策略
  setupCacheStrategies() {
    // 静态资源缓存策略
    this.cacheStrategies.set('static', {
      name: 'Cache First',
      match: (url) => {
        return /\.(js|css|png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/i.test(url);
      },
      strategy: 'cacheFirst',
      maxAge: 30 * 24 * 60 * 60 * 1000, // 30天
      storage: 'serviceWorker'
    });
    
    // API数据缓存策略
    this.cacheStrategies.set('api', {
      name: 'Network First',
      match: (url) => {
        return url.includes('/api/') || url.includes('/data/');
      },
      strategy: 'networkFirst',
      maxAge: 5 * 60 * 1000, // 5分钟
      storage: 'memory'
    });
    
    // 图片缓存策略
    this.cacheStrategies.set('images', {
      name: 'Stale While Revalidate',
      match: (url) => {
        return /\.(png|jpg|jpeg|gif|webp|avif)$/i.test(url);
      },
      strategy: 'staleWhileRevalidate',
      maxAge: 7 * 24 * 60 * 60 * 1000, // 7天
      storage: 'indexedDB'
    });
    
    // 字体缓存策略
    this.cacheStrategies.set('fonts', {
      name: 'Cache First',
      match: (url) => {
        return /\.(woff|woff2|ttf|eot)$/i.test(url);
      },
      strategy: 'cacheFirst',
      maxAge: 365 * 24 * 60 * 60 * 1000, // 1年
      storage: 'serviceWorker'
    });
  }
  
  // 获取缓存策略
  getCacheStrategy(url) {
    for (const [key, strategy] of this.cacheStrategies) {
      if (strategy.match(url)) {
        return strategy;
      }
    }
    
    // 默认策略
    return {
      name: 'Network Only',
      strategy: 'networkOnly',
      storage: 'none'
    };
  }
  
  // 缓存资源
  async cacheResource(url, data, options = {}) {
    const strategy = this.getCacheStrategy(url);
    const cacheData = {
      url,
      data,
      timestamp: Date.now(),
      maxAge: options.maxAge || strategy.maxAge || 0,
      type: options.type || 'unknown',
      size: this.calculateSize(data)
    };
    
    switch (strategy.storage) {
      case 'memory':
        return this.cacheToMemory(cacheData);
      case 'localStorage':
        return this.cacheToLocalStorage(cacheData);
      case 'indexedDB':
        return this.cacheToIndexedDB(cacheData);
      case 'serviceWorker':
        return this.cacheToServiceWorker(cacheData);
      default:
        return false;
    }
  }
  
  // 获取缓存资源
  async getCachedResource(url) {
    const strategy = this.getCacheStrategy(url);
    
    switch (strategy.storage) {
      case 'memory':
        return this.getFromMemory(url);
      case 'localStorage':
        return this.getFromLocalStorage(url);
      case 'indexedDB':
        return this.getFromIndexedDB(url);
      case 'serviceWorker':
        return this.getFromServiceWorker(url);
      default:
        return null;
    }
  }
  
  // 内存缓存
  cacheToMemory(cacheData) {
    // 检查缓存大小限制
    if (this.memoryCacheSize + cacheData.size > this.options.maxMemoryCacheSize) {
      this.evictMemoryCache(cacheData.size);
    }
    
    this.memoryCache.set(cacheData.url, cacheData);
    this.memoryCacheSize += cacheData.size;
    
    return true;
  }
  
  // 从内存获取缓存
  getFromMemory(url) {
    const cached = this.memoryCache.get(url);
    
    if (!cached) return null;
    
    // 检查是否过期
    if (this.isExpired(cached)) {
      this.memoryCache.delete(url);
      this.memoryCacheSize -= cached.size;
      return null;
    }
    
    return cached.data;
  }
  
  // 本地存储缓存
  cacheToLocalStorage(cacheData) {
    try {
      const key = `cache_${btoa(cacheData.url)}`;
      const value = JSON.stringify(cacheData);
      
      // 检查存储大小
      if (value.length > this.options.maxLocalStorageSize) {
        console.warn('数据过大，无法存储到localStorage');
        return false;
      }
      
      localStorage.setItem(key, value);
      return true;
    } catch (error) {
      console.error('localStorage缓存失败:', error);
      return false;
    }
  }
  
  // 从本地存储获取缓存
  getFromLocalStorage(url) {
    try {
      const key = `cache_${btoa(url)}`;
      const value = localStorage.getItem(key);
      
      if (!value) return null;
      
      const cached = JSON.parse(value);
      
      // 检查是否过期
      if (this.isExpired(cached)) {
        localStorage.removeItem(key);
        return null;
      }
      
      return cached.data;
    } catch (error) {
      console.error('localStorage读取失败:', error);
      return null;
    }
  }
  
  // IndexedDB缓存
  async cacheToIndexedDB(cacheData) {
    if (!this.indexedDB) return false;
    
    return new Promise((resolve, reject) => {
      const transaction = this.indexedDB.transaction(['resources'], 'readwrite');
      const store = transaction.objectStore('resources');
      
      const request = store.put(cacheData);
      
      request.onsuccess = () => resolve(true);
      request.onerror = () => reject(request.error);
    });
  }
  
  // 从IndexedDB获取缓存
  async getFromIndexedDB(url) {
    if (!this.indexedDB) return null;
    
    return new Promise((resolve, reject) => {
      const transaction = this.indexedDB.transaction(['resources'], 'readonly');
      const store = transaction.objectStore('resources');
      
      const request = store.get(url);
      
      request.onsuccess = () => {
        const cached = request.result;
        
        if (!cached) {
          resolve(null);
          return;
        }
        
        // 检查是否过期
        if (this.isExpired(cached)) {
          // 删除过期缓存
          const deleteTransaction = this.indexedDB.transaction(['resources'], 'readwrite');
          const deleteStore = deleteTransaction.objectStore('resources');
          deleteStore.delete(url);
          
          resolve(null);
          return;
        }
        
        resolve(cached.data);
      };
      
      request.onerror = () => reject(request.error);
    });
  }
  
  // Service Worker缓存
  async cacheToServiceWorker(cacheData) {
    if (!('serviceWorker' in navigator) || !navigator.serviceWorker.controller) {
      return false;
    }
    
    navigator.serviceWorker.controller.postMessage({
      type: 'CACHE_RESOURCE',
      payload: cacheData
    });
    
    return true;
  }
  
  // 从Service Worker获取缓存
  async getFromServiceWorker(url) {
    if (!('serviceWorker' in navigator) || !navigator.serviceWorker.controller) {
      return null;
    }
    
    return new Promise((resolve) => {
      const messageChannel = new MessageChannel();
      
      messageChannel.port1.onmessage = (event) => {
        resolve(event.data.result);
      };
      
      navigator.serviceWorker.controller.postMessage({
        type: 'GET_CACHED_RESOURCE',
        payload: { url }
      }, [messageChannel.port2]);
      
      // 超时处理
      setTimeout(() => resolve(null), 1000);
    });
  }
  
  // 检查是否过期
  isExpired(cached) {
    if (!cached.maxAge) return false;
    return Date.now() - cached.timestamp > cached.maxAge;
  }
  
  // 计算数据大小
  calculateSize(data) {
    if (typeof data === 'string') {
      return new Blob([data]).size;
    } else if (data instanceof ArrayBuffer) {
      return data.byteLength;
    } else {
      return new Blob([JSON.stringify(data)]).size;
    }
  }
  
  // 清理过期内存缓存
  cleanExpiredMemoryCache() {
    for (const [url, cached] of this.memoryCache) {
      if (this.isExpired(cached)) {
        this.memoryCache.delete(url);
        this.memoryCacheSize -= cached.size;
      }
    }
  }
  
  // 清理过期localStorage缓存
  cleanExpiredLocalStorageCache() {
    try {
      const keys = Object.keys(localStorage);
      
      keys.forEach(key => {
        if (key.startsWith('cache_')) {
          try {
            const cached = JSON.parse(localStorage.getItem(key));
            if (this.isExpired(cached)) {
              localStorage.removeItem(key);
            }
          } catch (error) {
            // 删除无效的缓存项
            localStorage.removeItem(key);
          }
        }
      });
    } catch (error) {
      console.error('清理localStorage缓存失败:', error);
    }
  }
  
  // 内存缓存淘汰
  evictMemoryCache(requiredSize) {
    // LRU淘汰策略
    const entries = Array.from(this.memoryCache.entries())
      .sort((a, b) => a[1].timestamp - b[1].timestamp);
    
    let freedSize = 0;
    
    for (const [url, cached] of entries) {
      this.memoryCache.delete(url);
      this.memoryCacheSize -= cached.size;
      freedSize += cached.size;
      
      if (freedSize >= requiredSize) {
        break;
      }
    }
  }
  
  // 获取缓存统计
  getCacheStats() {
    return {
      memory: {
        size: this.memoryCacheSize,
        count: this.memoryCache.size,
        maxSize: this.options.maxMemoryCacheSize
      },
      localStorage: {
        available: this.isLocalStorageAvailable(),
        maxSize: this.options.maxLocalStorageSize
      },
      indexedDB: {
        available: !!this.indexedDB
      },
      serviceWorker: {
        available: 'serviceWorker' in navigator,
        active: !!(navigator.serviceWorker && navigator.serviceWorker.controller)
      }
    };
  }
  
  // 检查localStorage可用性
  isLocalStorageAvailable() {
    try {
      const testKey = '__test__';
      localStorage.setItem(testKey, 'test');
      localStorage.removeItem(testKey);
      return true;
    } catch (error) {
      return false;
    }
  }
  
  // 清理所有缓存
  async clearAllCaches() {
    // 清理内存缓存
    this.memoryCache.clear();
    this.memoryCacheSize = 0;
    
    // 清理localStorage缓存
    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith('cache_')) {
          localStorage.removeItem(key);
        }
      });
    } catch (error) {
      console.error('清理localStorage失败:', error);
    }
    
    // 清理IndexedDB缓存
    if (this.indexedDB) {
      try {
        const transaction = this.indexedDB.transaction(['resources'], 'readwrite');
        const store = transaction.objectStore('resources');
        await store.clear();
      } catch (error) {
        console.error('清理IndexedDB失败:', error);
      }
    }
    
    // 清理Service Worker缓存
    if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
      navigator.serviceWorker.controller.postMessage({
        type: 'CLEAR_ALL_CACHES'
      });
    }
  }
}
```

### 2.4 代码分割与懒加载管理器

```javascript
// 代码分割与懒加载管理器
class CodeSplittingManager {
  constructor(options = {}) {
    this.options = {
      enableRouteBasedSplitting: true,
      enableComponentBasedSplitting: true,
      enableVendorSplitting: true,
      enableDynamicImports: true,
      preloadThreshold: 0.5, // 预加载阈值
      chunkSizeThreshold: 244 * 1024, // 244KB
      ...options
    };
    
    this.loadedChunks = new Set();
    this.loadingChunks = new Map();
    this.preloadedChunks = new Set();
    this.chunkDependencies = new Map();
    
    this.init();
  }
  
  async init() {
    try {
      // 初始化路由分割
      if (this.options.enableRouteBasedSplitting) {
        this.initRouteBasedSplitting();
      }
      
      // 初始化组件分割
      if (this.options.enableComponentBasedSplitting) {
        this.initComponentBasedSplitting();
      }
      
      // 初始化供应商分割
      if (this.options.enableVendorSplitting) {
        this.initVendorSplitting();
      }
      
      // 监控性能
      this.setupPerformanceMonitoring();
      
      console.log('代码分割管理器初始化完成');
    } catch (error) {
      console.error('代码分割管理器初始化失败:', error);
    }
  }
  
  // 初始化路由分割
  initRouteBasedSplitting() {
    this.routeChunks = new Map();
    
    // 注册路由块
    this.registerRouteChunk = (route, chunkLoader) => {
      this.routeChunks.set(route, {
        loader: chunkLoader,
        loaded: false,
        loading: false,
        preloaded: false
      });
    };
    
    // 监听路由变化
    if (typeof window !== 'undefined') {
      window.addEventListener('popstate', () => {
        this.handleRouteChange();
      });
      
      // 拦截pushState和replaceState
      this.interceptHistoryMethods();
    }
  }
  
  // 初始化组件分割
  initComponentBasedSplitting() {
    this.componentChunks = new Map();
    
    // 创建懒加载组件
    this.createLazyComponent = (componentLoader, options = {}) => {
      const componentId = this.generateComponentId();
      
      this.componentChunks.set(componentId, {
        loader: componentLoader,
        loaded: false,
        loading: false,
        preloaded: false,
        options
      });
      
      return this.createLazyWrapper(componentId);
    };
  }
  
  // 初始化供应商分割
  initVendorSplitting() {
    this.vendorChunks = new Map();
    
    // 常见的供应商库
    const commonVendors = [
      'react', 'react-dom', 'vue', 'angular',
      'lodash', 'moment', 'axios', 'rxjs'
    ];
    
    commonVendors.forEach(vendor => {
      this.vendorChunks.set(vendor, {
        loaded: false,
        loading: false,
        preloaded: false
      });
    });
  }
  
  // 动态导入模块
  async dynamicImport(moduleSpecifier, options = {}) {
    const chunkId = this.getChunkId(moduleSpecifier);
    
    // 检查是否已加载
    if (this.loadedChunks.has(chunkId)) {
      return this.getLoadedModule(chunkId);
    }
    
    // 检查是否正在加载
    if (this.loadingChunks.has(chunkId)) {
      return this.loadingChunks.get(chunkId);
    }
    
    // 开始加载
    const loadPromise = this.loadChunk(moduleSpecifier, options);
    this.loadingChunks.set(chunkId, loadPromise);
    
    try {
      const module = await loadPromise;
      this.loadedChunks.add(chunkId);
      this.loadingChunks.delete(chunkId);
      
      // 记录加载性能
      this.recordChunkLoadPerformance(chunkId, module);
      
      return module;
    } catch (error) {
      this.loadingChunks.delete(chunkId);
      console.error(`块加载失败: ${moduleSpecifier}`, error);
      throw error;
    }
  }
  
  // 预加载块
  async preloadChunk(moduleSpecifier, priority = 'low') {
    const chunkId = this.getChunkId(moduleSpecifier);
    
    // 检查是否已预加载或加载
    if (this.preloadedChunks.has(chunkId) || this.loadedChunks.has(chunkId)) {
      return;
    }
    
    this.preloadedChunks.add(chunkId);
    
    try {
      // 使用link预加载
      if (this.supportsModulePreload()) {
        this.createModulePreloadLink(moduleSpecifier, priority);
      } else {
        // 降级到prefetch
        this.createPrefetchLink(moduleSpecifier);
      }
    } catch (error) {
      console.error(`块预加载失败: ${moduleSpecifier}`, error);
    }
  }
  
  // 加载块
  async loadChunk(moduleSpecifier, options = {}) {
    const startTime = performance.now();
    
    try {
      // 使用动态import
      const module = await import(/* webpackChunkName: "[request]" */ moduleSpecifier);
      
      const loadTime = performance.now() - startTime;
      
      // 记录加载时间
      this.recordLoadTime(moduleSpecifier, loadTime);
      
      return module;
    } catch (error) {
      // 重试机制
      if (options.retry !== false && (options.retryCount || 0) < 3) {
        console.warn(`块加载失败，正在重试: ${moduleSpecifier}`);
        
        await this.delay(1000 * Math.pow(2, options.retryCount || 0));
        
        return this.loadChunk(moduleSpecifier, {
          ...options,
          retryCount: (options.retryCount || 0) + 1
        });
      }
      
      throw error;
    }
  }
  
  // 智能预加载
  async intelligentPreload() {
    try {
      // 基于用户行为预测
      const predictions = await this.predictUserBehavior();
      
      // 基于网络状态调整预加载策略
      const networkInfo = this.getNetworkInfo();
      
      if (networkInfo.effectiveType === '4g' && !networkInfo.saveData) {
        // 高速网络，积极预加载
        predictions.high.forEach(chunk => {
          this.preloadChunk(chunk, 'high');
        });
        
        predictions.medium.forEach(chunk => {
          this.preloadChunk(chunk, 'medium');
        });
      } else if (networkInfo.effectiveType === '3g') {
        // 中速网络，只预加载高优先级
        predictions.high.forEach(chunk => {
          this.preloadChunk(chunk, 'high');
        });
      }
      // 低速网络或省流量模式，不预加载
    } catch (error) {
      console.error('智能预加载失败:', error);
    }
  }
  
  // 预测用户行为
  async predictUserBehavior() {
    const currentRoute = this.getCurrentRoute();
    const userHistory = this.getUserNavigationHistory();
    const timeOfDay = new Date().getHours();
    
    // 简化的预测逻辑
    const predictions = {
      high: [],
      medium: [],
      low: []
    };
    
    // 基于历史导航模式
    const commonNextRoutes = this.analyzeNavigationPatterns(currentRoute, userHistory);
    
    commonNextRoutes.forEach(route => {
      if (route.probability > 0.7) {
        predictions.high.push(route.chunk);
      } else if (route.probability > 0.4) {
        predictions.medium.push(route.chunk);
      } else {
        predictions.low.push(route.chunk);
      }
    });
    
    // 基于时间模式
    const timeBasedPredictions = this.getTimeBasedPredictions(timeOfDay);
    predictions.medium.push(...timeBasedPredictions);
    
    return predictions;
  }
  
  // 创建懒加载包装器
  createLazyWrapper(componentId) {
    return {
      componentId,
      load: async () => {
        const chunk = this.componentChunks.get(componentId);
        if (!chunk) {
          throw new Error(`组件块未找到: ${componentId}`);
        }
        
        return this.dynamicImport(chunk.loader);
      },
      preload: () => {
        const chunk = this.componentChunks.get(componentId);
        if (chunk) {
          this.preloadChunk(chunk.loader);
        }
      }
    };
  }
  
  // 处理路由变化
  handleRouteChange() {
    const currentRoute = this.getCurrentRoute();
    const routeChunk = this.routeChunks.get(currentRoute);
    
    if (routeChunk && !routeChunk.loaded && !routeChunk.loading) {
      this.dynamicImport(routeChunk.loader);
    }
    
    // 预加载相关路由
    this.preloadRelatedRoutes(currentRoute);
  }
  
  // 预加载相关路由
  preloadRelatedRoutes(currentRoute) {
    const relatedRoutes = this.getRelatedRoutes(currentRoute);
    
    relatedRoutes.forEach(route => {
      const routeChunk = this.routeChunks.get(route);
      if (routeChunk && !routeChunk.preloaded) {
        this.preloadChunk(routeChunk.loader);
        routeChunk.preloaded = true;
      }
    });
  }
  
  // 创建模块预加载链接
  createModulePreloadLink(moduleSpecifier, priority) {
    const link = document.createElement('link');
    link.rel = 'modulepreload';
    link.href = moduleSpecifier;
    
    if (priority === 'high') {
      link.fetchPriority = 'high';
    }
    
    document.head.appendChild(link);
  }
  
  // 创建预取链接
  createPrefetchLink(moduleSpecifier) {
    const link = document.createElement('link');
    link.rel = 'prefetch';
    link.href = moduleSpecifier;
    
    document.head.appendChild(link);
  }
  
  // 检查模块预加载支持
  supportsModulePreload() {
    const link = document.createElement('link');
    return link.relList && link.relList.supports && link.relList.supports('modulepreload');
  }
  
  // 获取网络信息
  getNetworkInfo() {
    if ('connection' in navigator) {
      return {
        effectiveType: navigator.connection.effectiveType,
        saveData: navigator.connection.saveData,
        downlink: navigator.connection.downlink
      };
    }
    
    return {
      effectiveType: '4g',
      saveData: false,
      downlink: 10
    };
  }
  
  // 获取块ID
  getChunkId(moduleSpecifier) {
    return btoa(moduleSpecifier).replace(/[^a-zA-Z0-9]/g, '');
  }
  
  // 生成组件ID
  generateComponentId() {
    return `component_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  // 获取当前路由
  getCurrentRoute() {
    return window.location.pathname;
  }
  
  // 延迟函数
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
  
  // 记录加载时间
  recordLoadTime(moduleSpecifier, loadTime) {
    console.log(`块加载时间: ${moduleSpecifier} - ${loadTime.toFixed(2)}ms`);
    
    // 发送到分析服务
    if (typeof gtag !== 'undefined') {
      gtag('event', 'chunk_load_time', {
        event_category: 'Performance',
        event_label: moduleSpecifier,
        value: Math.round(loadTime)
      });
    }
  }
  
  // 记录块加载性能
  recordChunkLoadPerformance(chunkId, module) {
    const performance = {
      chunkId,
      loadTime: Date.now(),
      moduleSize: this.estimateModuleSize(module),
      dependencies: this.extractDependencies(module)
    };
    
    console.log('块加载性能:', performance);
  }
  
  // 估算模块大小
  estimateModuleSize(module) {
    try {
      return JSON.stringify(module).length;
    } catch (error) {
      return 0;
    }
  }
  
  // 提取依赖
  extractDependencies(module) {
    // 简化的依赖提取
    return Object.keys(module).filter(key => typeof module[key] === 'function');
  }
  
  // 获取加载统计
  getLoadingStats() {
    return {
      loadedChunks: this.loadedChunks.size,
      loadingChunks: this.loadingChunks.size,
      preloadedChunks: this.preloadedChunks.size,
      routeChunks: this.routeChunks.size,
      componentChunks: this.componentChunks.size,
      vendorChunks: this.vendorChunks.size
    };
  }
}
```

## 3. 最佳实践与总结

### 3.1 性能优化实施策略

#### 分阶段优化方案

1. **基础优化阶段**
   - 实施Core Web Vitals监控
   - 优化关键渲染路径
   - 实现基础缓存策略
   - 压缩和优化静态资源

2. **进阶优化阶段**
   - 实现智能预加载
   - 优化JavaScript执行
   - 实现图片懒加载和格式优化
   - 建立性能预算

3. **高级优化阶段**
   - 实现微前端架构
   - 建立完整的监控体系
   - 实现自动化性能测试
   - 建立性能文化

#### 性能监控指标体系

```javascript
// 性能指标定义
const PERFORMANCE_METRICS = {
  // Core Web Vitals
  LCP: { threshold: { good: 2500, poor: 4000 }, weight: 0.3 },
  FID: { threshold: { good: 100, poor: 300 }, weight: 0.3 },
  CLS: { threshold: { good: 0.1, poor: 0.25 }, weight: 0.3 },
  
  // 其他重要指标
  FCP: { threshold: { good: 1800, poor: 3000 }, weight: 0.1 },
  TTFB: { threshold: { good: 800, poor: 1800 }, weight: 0.1 },
  
  // 自定义指标
  TTI: { threshold: { good: 3800, poor: 7300 }, weight: 0.1 },
  TBT: { threshold: { good: 200, poor: 600 }, weight: 0.1 },
  SI: { threshold: { good: 3400, poor: 5800 }, weight: 0.1 }
};

// 性能评分计算
function calculatePerformanceScore(metrics) {
  let totalScore = 0;
  let totalWeight = 0;
  
  Object.entries(PERFORMANCE_METRICS).forEach(([key, config]) => {
    if (metrics[key] !== undefined) {
      const score = calculateMetricScore(metrics[key], config.threshold);
      totalScore += score * config.weight;
      totalWeight += config.weight;
    }
  });
  
  return totalWeight > 0 ? Math.round(totalScore / totalWeight) : 0;
}

function calculateMetricScore(value, threshold) {
  if (value <= threshold.good) {
    return 100;
  } else if (value <= threshold.poor) {
    const ratio = (value - threshold.good) / (threshold.poor - threshold.good);
    return Math.round(100 - (ratio * 50));
  } else {
    return Math.max(0, Math.round(50 - ((value - threshold.poor) / threshold.poor) * 50));
  }
}
```

### 3.2 核心价值与收益

#### 业务价值
- **用户体验提升**: 页面加载速度提升30-50%，用户满意度显著改善
- **转化率优化**: 每100ms的加载时间减少可提升1%的转化率
- **SEO优化**: Core Web Vitals成为Google排名因素，提升搜索可见性
- **成本降低**: 减少服务器负载和带宽消耗

#### 技术价值
- **可维护性**: 建立标准化的性能监控和优化流程
- **可扩展性**: 支持大规模应用的性能管理
- **可观测性**: 全面的性能数据收集和分析能力
- **自动化**: 减少手动性能优化工作量

### 3.3 未来发展趋势

1. **AI驱动的性能优化**
   - 智能资源预加载
   - 自适应性能策略
   - 预测性能问题

2. **边缘计算优化**
   - CDN智能路由
   - 边缘侧渲染
   - 就近数据处理

3. **新兴Web标准**
   - Web Assembly性能优化
   - HTTP/3协议优势
   - 新的浏览器API

4. **用户体验指标演进**
   - 更精确的用户体验量化
   - 个性化性能标准
   - 实时性能反馈

## 结语

前端性能优化是一个持续演进的过程，需要从监控、分析、优化、验证等多个维度建立完整的体系。通过实施本文介绍的Core Web Vitals监控、资源优化、缓存策略、代码分割等技术方案，可以显著提升应用的性能表现和用户体验。

关键在于建立数据驱动的优化文化，持续监控性能指标，及时发现和解决性能问题，为用户提供快速、流畅的Web体验。

---

**相关文章推荐：**
- [前端监控体系建设：从错误追踪到性能优化的完整方案](./0041-前端监控体系建设：从错误追踪到性能优化的完整方案.md)
- [前端微前端架构深度实践：从qiankun到Module Federation的技术演进](./0051-前端微前端架构深度实践：从qiankun到Module Federation的技术演进.md)
- [前端Sentry监控体系总结：构建企业级可观测性平台](./0050-前端Sentry监控体系总结：构建企业级可观测性平台.md)

### 1.2 资源监控器

```javascript
// 资源监控器
class ResourceMonitor {
  constructor(options = {}) {
    this.options = {
      monitorImages: true,
      monitorScripts: true,
      monitorStylesheets: true,
      monitorFonts: true,
      monitorXHR: true,
      monitorFetch: true,
      ...options
    };
    
    this.resourceCache = new Map();
    this.loadingResources = new Set();
    this.failedResources = new Set();
    
    this.onResourceLoad = options.onResourceLoad || (() => {});
    this.onResourceError = options.onResourceError || (() => {});
  }
  
  async start() {
    // 监控现有资源
    this.monitorExistingResources();
    
    // 监控新资源
    this.monitorNewResources();
    
    // 监控网络请求
    if (this.options.monitorXHR) {
      this.monitorXHRRequests();
    }
    
    if (this.options.monitorFetch) {
      this.monitorFetchRequests();
    }
    
    console.log('资源监控器已启动');
  }
  
  // 监控现有资源
  monitorExistingResources() {
    const resources = performance.getEntriesByType('resource');
    
    resources.forEach(resource => {
      this.processResource(resource);
    });
  }
  
  // 监控新资源
  monitorNewResources() {
    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        if (entry.entryType === 'resource') {
          this.processResource(entry);
        }
      });
    });
    
    observer.observe({ entryTypes: ['resource'] });
    
    // 监控DOM中的资源元素
    this.monitorDOMResources();
  }
  
  // 监控DOM资源
  monitorDOMResources() {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach(mutation => {
        mutation.addedNodes.forEach(node => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            this.checkResourceElement(node);
          }
        });
      });
    });
    
    observer.observe(document.body, {
      childList: true,
      subtree: true
    });
    
    // 检查现有元素
    this.checkExistingResourceElements();
  }
  
  // 检查现有资源元素
  checkExistingResourceElements() {
    const selectors = [
      'img[src]',
      'script[src]',
      'link[rel="stylesheet"]',
      'link[rel="preload"]',
      'link[rel="prefetch"]'
    ];
    
    selectors.forEach(selector => {
      document.querySelectorAll(selector).forEach(element => {
        this.checkResourceElement(element);
      });
    });
  }
  
  // 检查资源元素
  checkResourceElement(element) {
    const tagName = element.tagName.toLowerCase();
    let src = null;
    
    switch (tagName) {
      case 'img':
        if (this.options.monitorImages) {
          src = element.src;
          this.attachResourceListeners(element, src);
        }
        break;
      case 'script':
        if (this.options.monitorScripts && element.src) {
          src = element.src;
          this.attachResourceListeners(element, src);
        }
        break;
      case 'link':
        if (this.options.monitorStylesheets && element.rel === 'stylesheet') {
          src = element.href;
          this.attachResourceListeners(element, src);
        }
        break;
    }
  }
  
  // 附加资源监听器
  attachResourceListeners(element, src) {
    if (this.loadingResources.has(src)) return;
    
    this.loadingResources.add(src);
    
    element.addEventListener('load', () => {
      this.loadingResources.delete(src);
      this.handleResourceLoad(element, src);
    });
    
    element.addEventListener('error', (error) => {
      this.loadingResources.delete(src);
      this.failedResources.add(src);
      this.handleResourceError(element, src, error);
    });
  }
  
  // 处理资源加载
  handleResourceLoad(element, src) {
    const resourceData = {
      element,
      src,
      type: element.tagName.toLowerCase(),
      loadTime: Date.now(),
      size: this.getResourceSize(src)
    };
    
    this.resourceCache.set(src, resourceData);
    this.onResourceLoad(resourceData);
  }
  
  // 处理资源错误
  handleResourceError(element, src, error) {
    const errorData = {
      element,
      src,
      type: element.tagName.toLowerCase(),
      error,
      timestamp: Date.now()
    };
    
    this.onResourceError(errorData);
  }
  
  // 获取资源大小
  getResourceSize(src) {
    const resource = performance.getEntriesByName(src)[0];
    return resource ? resource.transferSize : 0;
  }
  
  // 处理资源
  processResource(resource) {
    const resourceData = {
      name: resource.name,
      type: this.getResourceType(resource),
      duration: resource.duration,
      size: resource.transferSize,
      decodedSize: resource.decodedBodySize,
      encodedSize: resource.encodedBodySize,
      cached: resource.transferSize === 0 && resource.decodedBodySize > 0,
      timing: {
        redirect: resource.redirectEnd - resource.redirectStart,
        dns: resource.domainLookupEnd - resource.domainLookupStart,
        tcp: resource.connectEnd - resource.connectStart,
        ssl: resource.secureConnectionStart > 0 ? resource.connectEnd - resource.secureConnectionStart : 0,
        request: resource.responseStart - resource.requestStart,
        response: resource.responseEnd - resource.responseStart
      }
    };
    
    this.resourceCache.set(resource.name, resourceData);
    this.onResourceLoad(resourceData);
  }
  
  // 获取资源类型
  getResourceType(resource) {
    if (resource.initiatorType) {
      return resource.initiatorType;
    }
    
    const url = resource.name;
    if (url.match(/\.(js|mjs)$/)) return 'script';
    if (url.match(/\.(css)$/)) return 'stylesheet';
    if (url.match(/\.(png|jpg|jpeg|gif|svg|webp|avif)$/)) return 'image';
    if (url.match(/\.(woff|woff2|ttf|eot|otf)$/)) return 'font';
    if (url.match(/\.(mp4|webm|ogg|avi)$/)) return 'video';
    if (url.match(/\.(mp3|wav|ogg|aac)$/)) return 'audio';
    
    return 'other';
  }
  
  // 监控XHR请求
  monitorXHRRequests() {
    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
      this._method = method;
      this._url = url;
      this._startTime = Date.now();
      
      return originalOpen.apply(this, arguments);
    };
    
    XMLHttpRequest.prototype.send = function(data) {
      const xhr = this;
      
      xhr.addEventListener('loadstart', () => {
        xhr._startTime = Date.now();
      });
      
      xhr.addEventListener('loadend', () => {
        const duration = Date.now() - xhr._startTime;
        
        this.processXHRRequest({
          method: xhr._method,
          url: xhr._url,
          status: xhr.status,
          duration,
          responseSize: xhr.responseText ? xhr.responseText.length : 0,
          success: xhr.status >= 200 && xhr.status < 300
        });
      });
      
      return originalSend.apply(this, arguments);
    };
  }
  
  // 监控Fetch请求
  monitorFetchRequests() {
    const originalFetch = window.fetch;
    
    window.fetch = async function(input, init = {}) {
      const startTime = Date.now();
      const url = typeof input === 'string' ? input : input.url;
      const method = init.method || 'GET';
      
      try {
        const response = await originalFetch(input, init);
        const duration = Date.now() - startTime;
        
        this.processFetchRequest({
          method,
          url,
          status: response.status,
          duration,
          success: response.ok,
          responseSize: response.headers.get('content-length') || 0
        });
        
        return response;
      } catch (error) {
        const duration = Date.now() - startTime;
        
        this.processFetchRequest({
          method,
          url,
          status: 0,
          duration,
          success: false,
          error: error.message
        });
        
        throw error;
      }
    }.bind(this);
  }
  
  // 处理XHR请求
  processXHRRequest(requestData) {
    this.onResourceLoad({
      ...requestData,
      type: 'xhr'
    });
  }
  
  // 处理Fetch请求
  processFetchRequest(requestData) {
    this.onResourceLoad({
      ...requestData,
      type: 'fetch'
    });
  }
  
  // 获取资源统计
  getResourceStats() {
    const stats = {
      total: this.resourceCache.size,
      byType: {},
      totalSize: 0,
      cached: 0,
      failed: this.failedResources.size,
      averageLoadTime: 0
    };
    
    let totalLoadTime = 0;
    
    this.resourceCache.forEach(resource => {
      // 按类型统计
      if (!stats.byType[resource.type]) {
        stats.byType[resource.type] = {
          count: 0,
          size: 0,
          averageLoadTime: 0
        };
      }
      
      stats.byType[resource.type].count++;
      stats.byType[resource.type].size += resource.size || 0;
      
      // 总大小
      stats.totalSize += resource.size || 0;
      
      // 缓存统计
      if (resource.cached) {
        stats.cached++;
      }
      
      // 加载时间
      if (resource.duration) {
        totalLoadTime += resource.duration;
        stats.byType[resource.type].averageLoadTime += resource.duration;
      }
    });
    
    // 计算平均加载时间
    if (stats.total > 0) {
      stats.averageLoadTime = totalLoadTime / stats.total;
      
      Object.keys(stats.byType).forEach(type => {
        const typeStats = stats.byType[type];
        if (typeStats.count > 0) {
          typeStats.averageLoadTime = typeStats.averageLoadTime / typeStats.count;
        }
      });
    }
    
    return stats;
  }
  
  // 获取失败的资源
  getFailedResources() {
    return Array.from(this.failedResources);
  }
  
  // 清理缓存
  clearCache() {
    this.resourceCache.clear();
    this.failedResources.clear();
  }
}
```