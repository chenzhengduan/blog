# 前端Sentry数据分析与可视化：构建智能化监控仪表板

在现代前端开发中，仅仅收集错误数据是不够的，更重要的是如何有效地分析和可视化这些数据，从中获得有价值的洞察。本文将深入探讨如何基于Sentry构建智能化的监控仪表板，实现数据的深度分析和可视化展示。

## 一、Sentry数据分析基础架构

### 1.1 数据收集与预处理系统

```javascript
// dataCollectionSystem.js
import * as Sentry from '@sentry/browser';
import { BrowserTracing } from '@sentry/tracing';

// 数据收集与预处理系统
export class DataCollectionSystem {
  constructor(options = {}) {
    this.options = {
      enableAdvancedMetrics: true,
      enableUserBehaviorTracking: true,
      enablePerformanceAnalytics: true,
      dataRetentionDays: 30,
      batchSize: 100,
      flushInterval: 5000, // 5秒
      ...options
    };
    
    this.dataBuffer = [];
    this.metricsCache = new Map();
    this.userSessions = new Map();
    this.performanceMetrics = new Map();
    
    this.setupDataCollection();
    this.startDataProcessing();
  }
  
  setupDataCollection() {
    // 增强Sentry配置
    Sentry.init({
      dsn: process.env.REACT_APP_SENTRY_DSN,
      environment: process.env.NODE_ENV,
      integrations: [
        new BrowserTracing({
          // 自定义性能监控
          beforeNavigate: context => {
            return {
              ...context,
              // 添加自定义标签
              tags: {
                ...context.tags,
                navigationSource: this.getNavigationSource(),
                userSegment: this.getUserSegment(),
                deviceType: this.getDeviceType()
              }
            };
          }
        })
      ],
      tracesSampleRate: 0.1,
      
      // 自定义事件处理器
      beforeSend: (event) => {
        return this.preprocessEvent(event);
      },
      
      // 自定义面包屑处理器
      beforeBreadcrumb: (breadcrumb) => {
        return this.preprocessBreadcrumb(breadcrumb);
      }
    });
    
    // 设置全局事件监听器
    this.setupGlobalEventListeners();
  }
  
  // 预处理事件数据
  preprocessEvent(event) {
    // 添加自定义数据
    event.extra = {
      ...event.extra,
      sessionId: this.getCurrentSessionId(),
      userJourney: this.getUserJourney(),
      performanceContext: this.getPerformanceContext(),
      businessContext: this.getBusinessContext()
    };
    
    // 添加自定义标签
    event.tags = {
      ...event.tags,
      errorCategory: this.categorizeError(event),
      impactLevel: this.calculateImpactLevel(event),
      userType: this.getUserType(),
      featureFlag: this.getActiveFeatureFlags()
    };
    
    // 缓存事件数据用于分析
    this.cacheEventData(event);
    
    return event;
  }
  
  // 预处理面包屑数据
  preprocessBreadcrumb(breadcrumb) {
    // 增强面包屑数据
    breadcrumb.data = {
      ...breadcrumb.data,
      sessionTime: Date.now() - this.getSessionStartTime(),
      pageLoadTime: this.getPageLoadTime(),
      userInteractionCount: this.getUserInteractionCount()
    };
    
    // 缓存用户行为数据
    this.cacheUserBehavior(breadcrumb);
    
    return breadcrumb;
  }
  
  // 设置全局事件监听器
  setupGlobalEventListeners() {
    // 监听页面性能
    if ('PerformanceObserver' in window) {
      this.setupPerformanceObserver();
    }
    
    // 监听用户交互
    this.setupUserInteractionTracking();
    
    // 监听网络状态
    this.setupNetworkMonitoring();
    
    // 监听内存使用
    this.setupMemoryMonitoring();
  }
  
  // 设置性能观察器
  setupPerformanceObserver() {
    // 监听导航性能
    const navObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        this.collectNavigationMetrics(entry);
      });
    });
    navObserver.observe({ entryTypes: ['navigation'] });
    
    // 监听资源加载性能
    const resourceObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        this.collectResourceMetrics(entry);
      });
    });
    resourceObserver.observe({ entryTypes: ['resource'] });
    
    // 监听长任务
    const longTaskObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        this.collectLongTaskMetrics(entry);
      });
    });
    longTaskObserver.observe({ entryTypes: ['longtask'] });
    
    // 监听布局偏移
    const clsObserver = new PerformanceObserver((list) => {
      list.getEntries().forEach(entry => {
        this.collectLayoutShiftMetrics(entry);
      });
    });
    clsObserver.observe({ entryTypes: ['layout-shift'] });
  }
  
  // 收集导航性能指标
  collectNavigationMetrics(entry) {
    const metrics = {
      type: 'navigation',
      timestamp: Date.now(),
      url: entry.name,
      duration: entry.duration,
      domContentLoaded: entry.domContentLoadedEventEnd - entry.domContentLoadedEventStart,
      loadComplete: entry.loadEventEnd - entry.loadEventStart,
      firstPaint: this.getFirstPaint(),
      firstContentfulPaint: this.getFirstContentfulPaint(),
      largestContentfulPaint: this.getLargestContentfulPaint(),
      firstInputDelay: this.getFirstInputDelay(),
      cumulativeLayoutShift: this.getCumulativeLayoutShift(),
      timeToInteractive: this.getTimeToInteractive()
    };
    
    this.addToDataBuffer('performance', metrics);
    
    // 发送到Sentry
    Sentry.addBreadcrumb({
      category: 'performance',
      message: 'Navigation performance collected',
      level: 'info',
      data: metrics
    });
  }
  
  // 收集资源加载指标
  collectResourceMetrics(entry) {
    const metrics = {
      type: 'resource',
      timestamp: Date.now(),
      name: entry.name,
      duration: entry.duration,
      size: entry.transferSize,
      resourceType: this.getResourceType(entry.name),
      initiatorType: entry.initiatorType,
      responseStart: entry.responseStart,
      responseEnd: entry.responseEnd
    };
    
    this.addToDataBuffer('resource', metrics);
  }
  
  // 收集长任务指标
  collectLongTaskMetrics(entry) {
    const metrics = {
      type: 'longtask',
      timestamp: Date.now(),
      duration: entry.duration,
      startTime: entry.startTime,
      attribution: entry.attribution
    };
    
    this.addToDataBuffer('longtask', metrics);
    
    // 如果长任务超过阈值，发送警告
    if (entry.duration > 100) {
      Sentry.captureMessage(
        `Long task detected: ${entry.duration}ms`,
        'warning',
        {
          tags: {
            category: 'performance',
            type: 'longtask'
          },
          extra: metrics
        }
      );
    }
  }
  
  // 收集布局偏移指标
  collectLayoutShiftMetrics(entry) {
    if (!entry.hadRecentInput) {
      const metrics = {
        type: 'layout-shift',
        timestamp: Date.now(),
        value: entry.value,
        sources: entry.sources?.map(source => ({
          node: source.node?.tagName,
          currentRect: source.currentRect,
          previousRect: source.previousRect
        }))
      };
      
      this.addToDataBuffer('layout-shift', metrics);
    }
  }
  
  // 设置用户交互追踪
  setupUserInteractionTracking() {
    const interactionEvents = ['click', 'scroll', 'keydown', 'touchstart'];
    
    interactionEvents.forEach(eventType => {
      document.addEventListener(eventType, (event) => {
        this.trackUserInteraction(eventType, event);
      }, { passive: true });
    });
  }
  
  // 追踪用户交互
  trackUserInteraction(type, event) {
    const interaction = {
      type: 'user-interaction',
      timestamp: Date.now(),
      eventType: type,
      target: this.getElementSelector(event.target),
      coordinates: type === 'click' ? { x: event.clientX, y: event.clientY } : null,
      sessionTime: Date.now() - this.getSessionStartTime()
    };
    
    this.addToDataBuffer('interaction', interaction);
    
    // 更新用户会话数据
    this.updateUserSession(interaction);
  }
  
  // 设置网络监控
  setupNetworkMonitoring() {
    // 监听在线/离线状态
    window.addEventListener('online', () => {
      this.addToDataBuffer('network', {
        type: 'network-status',
        timestamp: Date.now(),
        status: 'online'
      });
    });
    
    window.addEventListener('offline', () => {
      this.addToDataBuffer('network', {
        type: 'network-status',
        timestamp: Date.now(),
        status: 'offline'
      });
    });
    
    // 监听连接信息
    if ('connection' in navigator) {
      const connection = navigator.connection;
      
      const collectConnectionInfo = () => {
        this.addToDataBuffer('network', {
          type: 'connection-info',
          timestamp: Date.now(),
          effectiveType: connection.effectiveType,
          downlink: connection.downlink,
          rtt: connection.rtt,
          saveData: connection.saveData
        });
      };
      
      connection.addEventListener('change', collectConnectionInfo);
      collectConnectionInfo(); // 初始收集
    }
  }
  
  // 设置内存监控
  setupMemoryMonitoring() {
    if ('memory' in performance) {
      setInterval(() => {
        const memInfo = performance.memory;
        this.addToDataBuffer('memory', {
          type: 'memory-usage',
          timestamp: Date.now(),
          usedJSHeapSize: memInfo.usedJSHeapSize,
          totalJSHeapSize: memInfo.totalJSHeapSize,
          jsHeapSizeLimit: memInfo.jsHeapSizeLimit,
          usagePercentage: (memInfo.usedJSHeapSize / memInfo.jsHeapSizeLimit) * 100
        });
      }, 30000); // 每30秒收集一次
    }
  }
  
  // 添加数据到缓冲区
  addToDataBuffer(category, data) {
    this.dataBuffer.push({
      category,
      data,
      timestamp: Date.now()
    });
    
    // 如果缓冲区满了，立即处理
    if (this.dataBuffer.length >= this.options.batchSize) {
      this.processDataBuffer();
    }
  }
  
  // 开始数据处理
  startDataProcessing() {
    // 定期处理数据缓冲区
    setInterval(() => {
      this.processDataBuffer();
    }, this.options.flushInterval);
    
    // 定期清理过期数据
    setInterval(() => {
      this.cleanupExpiredData();
    }, 3600000); // 每小时清理一次
  }
  
  // 处理数据缓冲区
  processDataBuffer() {
    if (this.dataBuffer.length === 0) return;
    
    const batch = this.dataBuffer.splice(0, this.options.batchSize);
    
    // 分析数据
    this.analyzeDataBatch(batch);
    
    // 存储到本地缓存
    this.storeDataBatch(batch);
    
    // 发送聚合数据到Sentry
    this.sendAggregatedData(batch);
  }
  
  // 分析数据批次
  analyzeDataBatch(batch) {
    const analysis = {
      performance: this.analyzePerformanceData(batch),
      userBehavior: this.analyzeUserBehaviorData(batch),
      errors: this.analyzeErrorData(batch),
      network: this.analyzeNetworkData(batch)
    };
    
    // 检测异常模式
    this.detectAnomalies(analysis);
    
    // 更新实时指标
    this.updateRealTimeMetrics(analysis);
  }
  
  // 分析性能数据
  analyzePerformanceData(batch) {
    const performanceData = batch.filter(item => 
      ['performance', 'resource', 'longtask', 'layout-shift'].includes(item.category)
    );
    
    if (performanceData.length === 0) return null;
    
    const analysis = {
      averageLoadTime: 0,
      slowestResources: [],
      longTaskCount: 0,
      layoutShiftScore: 0,
      performanceScore: 0
    };
    
    // 计算平均加载时间
    const navigationData = performanceData.filter(item => 
      item.category === 'performance' && item.data.type === 'navigation'
    );
    
    if (navigationData.length > 0) {
      analysis.averageLoadTime = navigationData.reduce(
        (sum, item) => sum + item.data.duration, 0
      ) / navigationData.length;
    }
    
    // 找出最慢的资源
    const resourceData = performanceData.filter(item => item.category === 'resource');
    analysis.slowestResources = resourceData
      .sort((a, b) => b.data.duration - a.data.duration)
      .slice(0, 5)
      .map(item => ({
        name: item.data.name,
        duration: item.data.duration,
        size: item.data.size
      }));
    
    // 计算长任务数量
    analysis.longTaskCount = performanceData.filter(
      item => item.category === 'longtask'
    ).length;
    
    // 计算布局偏移分数
    const layoutShiftData = performanceData.filter(
      item => item.category === 'layout-shift'
    );
    analysis.layoutShiftScore = layoutShiftData.reduce(
      (sum, item) => sum + item.data.value, 0
    );
    
    // 计算综合性能分数
    analysis.performanceScore = this.calculatePerformanceScore(analysis);
    
    return analysis;
  }
  
  // 分析用户行为数据
  analyzeUserBehaviorData(batch) {
    const behaviorData = batch.filter(item => item.category === 'interaction');
    
    if (behaviorData.length === 0) return null;
    
    const analysis = {
      totalInteractions: behaviorData.length,
      interactionTypes: {},
      averageSessionTime: 0,
      mostInteractedElements: {},
      userEngagement: 0
    };
    
    // 统计交互类型
    behaviorData.forEach(item => {
      const type = item.data.eventType;
      analysis.interactionTypes[type] = (analysis.interactionTypes[type] || 0) + 1;
    });
    
    // 统计最多交互的元素
    behaviorData.forEach(item => {
      const target = item.data.target;
      analysis.mostInteractedElements[target] = 
        (analysis.mostInteractedElements[target] || 0) + 1;
    });
    
    // 计算平均会话时间
    const sessionTimes = behaviorData.map(item => item.data.sessionTime);
    if (sessionTimes.length > 0) {
      analysis.averageSessionTime = sessionTimes.reduce((sum, time) => sum + time, 0) / sessionTimes.length;
    }
    
    // 计算用户参与度
    analysis.userEngagement = this.calculateUserEngagement(analysis);
    
    return analysis;
  }
  
  // 检测异常模式
  detectAnomalies(analysis) {
    const anomalies = [];
    
    // 检测性能异常
    if (analysis.performance) {
      if (analysis.performance.averageLoadTime > 5000) {
        anomalies.push({
          type: 'performance',
          severity: 'high',
          message: `Average load time is ${analysis.performance.averageLoadTime}ms`,
          data: analysis.performance
        });
      }
      
      if (analysis.performance.longTaskCount > 10) {
        anomalies.push({
          type: 'performance',
          severity: 'medium',
          message: `High number of long tasks: ${analysis.performance.longTaskCount}`,
          data: analysis.performance
        });
      }
      
      if (analysis.performance.layoutShiftScore > 0.25) {
        anomalies.push({
          type: 'performance',
          severity: 'medium',
          message: `High cumulative layout shift: ${analysis.performance.layoutShiftScore}`,
          data: analysis.performance
        });
      }
    }
    
    // 检测用户行为异常
    if (analysis.userBehavior) {
      if (analysis.userBehavior.userEngagement < 0.3) {
        anomalies.push({
          type: 'user-behavior',
          severity: 'medium',
          message: `Low user engagement: ${analysis.userBehavior.userEngagement}`,
          data: analysis.userBehavior
        });
      }
    }
    
    // 发送异常报告
    if (anomalies.length > 0) {
      this.reportAnomalies(anomalies);
    }
  }
  
  // 报告异常
  reportAnomalies(anomalies) {
    anomalies.forEach(anomaly => {
      Sentry.captureMessage(
        `Anomaly detected: ${anomaly.message}`,
        anomaly.severity === 'high' ? 'error' : 'warning',
        {
          tags: {
            category: 'anomaly-detection',
            type: anomaly.type,
            severity: anomaly.severity
          },
          extra: anomaly.data
        }
      );
    });
  }
  
  // 计算性能分数
  calculatePerformanceScore(analysis) {
    let score = 100;
    
    // 基于加载时间的扣分
    if (analysis.averageLoadTime > 3000) {
      score -= Math.min(30, (analysis.averageLoadTime - 3000) / 100);
    }
    
    // 基于长任务的扣分
    score -= Math.min(20, analysis.longTaskCount * 2);
    
    // 基于布局偏移的扣分
    score -= Math.min(25, analysis.layoutShiftScore * 100);
    
    return Math.max(0, Math.round(score));
  }
  
  // 计算用户参与度
  calculateUserEngagement(analysis) {
    let engagement = 0;
    
    // 基于交互数量
    engagement += Math.min(0.4, analysis.totalInteractions / 100);
    
    // 基于会话时间
    engagement += Math.min(0.3, analysis.averageSessionTime / 300000); // 5分钟为满分
    
    // 基于交互多样性
    const typeCount = Object.keys(analysis.interactionTypes).length;
    engagement += Math.min(0.3, typeCount / 4);
    
    return Math.round(engagement * 100) / 100;
  }
  
  // 获取元素选择器
  getElementSelector(element) {
    if (!element) return 'unknown';
    
    let selector = element.tagName.toLowerCase();
    
    if (element.id) {
      selector += `#${element.id}`;
    } else if (element.className) {
      selector += `.${element.className.split(' ').join('.')}`;
    }
    
    return selector;
  }
  
  // 获取资源类型
  getResourceType(url) {
    const extension = url.split('.').pop()?.toLowerCase();
    
    const typeMap = {
      'js': 'script',
      'css': 'stylesheet',
      'png': 'image',
      'jpg': 'image',
      'jpeg': 'image',
      'gif': 'image',
      'svg': 'image',
      'woff': 'font',
      'woff2': 'font',
      'ttf': 'font',
      'eot': 'font'
    };
    
    return typeMap[extension] || 'other';
  }
  
  // 获取导航来源
  getNavigationSource() {
    if (document.referrer) {
      try {
        const referrerDomain = new URL(document.referrer).hostname;
        const currentDomain = window.location.hostname;
        return referrerDomain === currentDomain ? 'internal' : 'external';
      } catch {
        return 'unknown';
      }
    }
    return 'direct';
  }
  
  // 获取用户细分
  getUserSegment() {
    // 这里可以根据业务逻辑实现用户细分
    const userId = localStorage.getItem('userId');
    if (!userId) return 'anonymous';
    
    // 示例：基于用户ID的简单细分
    const userType = localStorage.getItem('userType') || 'regular';
    return userType;
  }
  
  // 获取设备类型
  getDeviceType() {
    const userAgent = navigator.userAgent;
    
    if (/tablet|ipad|playbook|silk/i.test(userAgent)) {
      return 'tablet';
    }
    if (/mobile|iphone|ipod|android|blackberry|opera|mini|windows\sce|palm|smartphone|iemobile/i.test(userAgent)) {
      return 'mobile';
    }
    return 'desktop';
  }
  
  // 获取当前会话ID
  getCurrentSessionId() {
    let sessionId = sessionStorage.getItem('sentrySessionId');
    if (!sessionId) {
      sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      sessionStorage.setItem('sentrySessionId', sessionId);
    }
    return sessionId;
  }
  
  // 获取用户旅程
  getUserJourney() {
    const journey = JSON.parse(localStorage.getItem('userJourney') || '[]');
    return journey.slice(-10); // 返回最近10个页面
  }
  
  // 获取性能上下文
  getPerformanceContext() {
    return {
      connectionType: navigator.connection?.effectiveType || 'unknown',
      deviceMemory: navigator.deviceMemory || 'unknown',
      hardwareConcurrency: navigator.hardwareConcurrency || 'unknown',
      cookieEnabled: navigator.cookieEnabled,
      language: navigator.language
    };
  }
  
  // 获取业务上下文
  getBusinessContext() {
    return {
      currentPage: window.location.pathname,
      referrer: document.referrer,
      timestamp: Date.now(),
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      screenResolution: `${screen.width}x${screen.height}`,
      viewportSize: `${window.innerWidth}x${window.innerHeight}`
    };
  }
  
  // 分类错误
  categorizeError(event) {
    const errorType = event.exception?.values[0]?.type;
    const errorMessage = event.message || event.exception?.values[0]?.value || '';
    
    // 网络错误
    if (errorType === 'NetworkError' || errorMessage.includes('fetch')) {
      return 'network';
    }
    
    // 语法错误
    if (errorType === 'SyntaxError') {
      return 'syntax';
    }
    
    // 引用错误
    if (errorType === 'ReferenceError') {
      return 'reference';
    }
    
    // 类型错误
    if (errorType === 'TypeError') {
      return 'type';
    }
    
    // 权限错误
    if (errorType === 'SecurityError') {
      return 'security';
    }
    
    // 资源加载错误
    if (errorMessage.includes('Loading chunk') || errorMessage.includes('ChunkLoadError')) {
      return 'chunk-load';
    }
    
    return 'other';
  }
  
  // 计算影响级别
  calculateImpactLevel(event) {
    let score = 0;
    
    // 基于错误类型
    const errorCategory = this.categorizeError(event);
    const categoryScores = {
      'security': 10,
      'network': 7,
      'chunk-load': 6,
      'syntax': 8,
      'reference': 7,
      'type': 5,
      'other': 3
    };
    score += categoryScores[errorCategory] || 3;
    
    // 基于环境
    if (event.environment === 'production') {
      score += 3;
    }
    
    // 基于用户类型
    const userType = this.getUserType();
    if (userType === 'premium') {
      score += 2;
    }
    
    if (score >= 10) return 'critical';
    if (score >= 7) return 'high';
    if (score >= 4) return 'medium';
    return 'low';
  }
  
  // 获取用户类型
  getUserType() {
    return localStorage.getItem('userType') || 'regular';
  }
  
  // 获取活跃的功能标志
  getActiveFeatureFlags() {
    const flags = JSON.parse(localStorage.getItem('featureFlags') || '{}');
    return Object.keys(flags).filter(key => flags[key]).join(',');
  }
  
  // 缓存事件数据
  cacheEventData(event) {
    const key = `event_${Date.now()}`;
    this.metricsCache.set(key, {
      type: 'error',
      data: event,
      timestamp: Date.now()
    });
  }
  
  // 缓存用户行为
  cacheUserBehavior(breadcrumb) {
    const key = `behavior_${Date.now()}`;
    this.metricsCache.set(key, {
      type: 'behavior',
      data: breadcrumb,
      timestamp: Date.now()
    });
  }
  
  // 更新用户会话
  updateUserSession(interaction) {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId) || {
      startTime: Date.now(),
      interactions: [],
      pageViews: 0,
      errors: 0
    };
    
    session.interactions.push(interaction);
    session.lastActivity = Date.now();
    
    this.userSessions.set(userId, session);
  }
  
  // 获取用户ID
  getUserId() {
    return localStorage.getItem('userId') || 'anonymous';
  }
  
  // 获取会话开始时间
  getSessionStartTime() {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId);
    return session ? session.startTime : Date.now();
  }
  
  // 获取页面加载时间
  getPageLoadTime() {
    const navigation = performance.getEntriesByType('navigation')[0];
    return navigation ? navigation.loadEventEnd - navigation.loadEventStart : 0;
  }
  
  // 获取用户交互次数
  getUserInteractionCount() {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId);
    return session ? session.interactions.length : 0;
  }
  
  // 获取First Paint
  getFirstPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const fpEntry = paintEntries.find(entry => entry.name === 'first-paint');
    return fpEntry ? fpEntry.startTime : 0;
  }
  
  // 获取First Contentful Paint
  getFirstContentfulPaint() {
    const paintEntries = performance.getEntriesByType('paint');
    const fcpEntry = paintEntries.find(entry => entry.name === 'first-contentful-paint');
    return fcpEntry ? fcpEntry.startTime : 0;
  }
  
  // 获取Largest Contentful Paint
  getLargestContentfulPaint() {
    return new Promise((resolve) => {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        resolve(lastEntry ? lastEntry.startTime : 0);
        observer.disconnect();
      });
      observer.observe({ entryTypes: ['largest-contentful-paint'] });
      
      // 超时处理
      setTimeout(() => {
        observer.disconnect();
        resolve(0);
      }, 5000);
    });
  }
  
  // 获取First Input Delay
  getFirstInputDelay() {
    return new Promise((resolve) => {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const firstEntry = entries[0];
        resolve(firstEntry ? firstEntry.processingStart - firstEntry.startTime : 0);
        observer.disconnect();
      });
      observer.observe({ entryTypes: ['first-input'] });
      
      // 超时处理
      setTimeout(() => {
        observer.disconnect();
        resolve(0);
      }, 5000);
    });
  }
  
  // 获取Cumulative Layout Shift
  getCumulativeLayoutShift() {
    return new Promise((resolve) => {
      let clsValue = 0;
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (!entry.hadRecentInput) {
            clsValue += entry.value;
          }
        }
      });
      observer.observe({ entryTypes: ['layout-shift'] });
      
      // 在页面卸载时返回结果
      window.addEventListener('beforeunload', () => {
        observer.disconnect();
        resolve(clsValue);
      });
      
      // 超时处理
      setTimeout(() => {
        observer.disconnect();
        resolve(clsValue);
      }, 5000);
    });
  }
  
  // 获取Time to Interactive
  getTimeToInteractive() {
    // 简化的TTI计算
    const navigation = performance.getEntriesByType('navigation')[0];
    if (!navigation) return 0;
    
    return navigation.domContentLoadedEventEnd - navigation.fetchStart;
  }
  
  // 存储数据批次
  storeDataBatch(batch) {
    try {
      const existingData = JSON.parse(localStorage.getItem('sentryAnalyticsData') || '[]');
      const newData = [...existingData, ...batch];
      
      // 限制存储大小
      const maxItems = 1000;
      if (newData.length > maxItems) {
        newData.splice(0, newData.length - maxItems);
      }
      
      localStorage.setItem('sentryAnalyticsData', JSON.stringify(newData));
    } catch (error) {
      console.warn('Failed to store analytics data:', error);
    }
  }
  
  // 发送聚合数据
  sendAggregatedData(batch) {
    const aggregated = this.aggregateData(batch);
    
    if (Object.keys(aggregated).length > 0) {
      Sentry.addBreadcrumb({
        category: 'analytics',
        message: 'Aggregated data collected',
        level: 'info',
        data: aggregated
      });
    }
  }
  
  // 聚合数据
  aggregateData(batch) {
    const aggregated = {
      performance: {},
      interactions: {},
      network: {},
      memory: {}
    };
    
    batch.forEach(item => {
      switch (item.category) {
        case 'performance':
          this.aggregatePerformanceData(aggregated.performance, item.data);
          break;
        case 'interaction':
          this.aggregateInteractionData(aggregated.interactions, item.data);
          break;
        case 'network':
          this.aggregateNetworkData(aggregated.network, item.data);
          break;
        case 'memory':
          this.aggregateMemoryData(aggregated.memory, item.data);
          break;
      }
    });
    
    return aggregated;
  }
  
  // 聚合性能数据
  aggregatePerformanceData(target, data) {
    if (data.type === 'navigation') {
      target.navigationCount = (target.navigationCount || 0) + 1;
      target.totalDuration = (target.totalDuration || 0) + data.duration;
      target.averageDuration = target.totalDuration / target.navigationCount;
    }
  }
  
  // 聚合交互数据
  aggregateInteractionData(target, data) {
    target.totalInteractions = (target.totalInteractions || 0) + 1;
    target.byType = target.byType || {};
    target.byType[data.eventType] = (target.byType[data.eventType] || 0) + 1;
  }
  
  // 聚合网络数据
  aggregateNetworkData(target, data) {
    if (data.type === 'connection-info') {
      target.connectionType = data.effectiveType;
      target.downlink = data.downlink;
      target.rtt = data.rtt;
    }
  }
  
  // 聚合内存数据
  aggregateMemoryData(target, data) {
    if (data.type === 'memory-usage') {
      target.currentUsage = data.usagePercentage;
      target.maxUsage = Math.max(target.maxUsage || 0, data.usagePercentage);
    }
  }
  
  // 清理过期数据
  cleanupExpiredData() {
    const cutoffTime = Date.now() - (this.options.dataRetentionDays * 24 * 60 * 60 * 1000);
    
    // 清理内存缓存
    this.metricsCache.forEach((value, key) => {
      if (value.timestamp < cutoffTime) {
        this.metricsCache.delete(key);
      }
    });
    
    // 清理本地存储
    try {
      const storedData = JSON.parse(localStorage.getItem('sentryAnalyticsData') || '[]');
      const filteredData = storedData.filter(item => item.timestamp > cutoffTime);
      localStorage.setItem('sentryAnalyticsData', JSON.stringify(filteredData));
    } catch (error) {
      console.warn('Failed to cleanup stored data:', error);
    }
  }
  
  // 获取分析数据
  getAnalyticsData(timeRange = '24h') {
    const timeRanges = {
      '1h': 3600000,
      '24h': 86400000,
      '7d': 604800000,
      '30d': 2592000000
    };
    
    const cutoffTime = Date.now() - (timeRanges[timeRange] || timeRanges['24h']);
    
    try {
      const storedData = JSON.parse(localStorage.getItem('sentryAnalyticsData') || '[]');
      return storedData.filter(item => item.timestamp > cutoffTime);
    } catch (error) {
      console.warn('Failed to get analytics data:', error);
      return [];
    }
  }
  
  // 获取实时指标
  getRealTimeMetrics() {
    const userId = this.getUserId();
    const session = this.userSessions.get(userId);
    
    return {
      sessionDuration: session ? Date.now() - session.startTime : 0,
      interactionCount: session ? session.interactions.length : 0,
      pageViews: session ? session.pageViews : 0,
      errorCount: session ? session.errors : 0,
      memoryUsage: this.getCurrentMemoryUsage(),
      connectionType: navigator.connection?.effectiveType || 'unknown'
    };
  }
  
  // 获取当前内存使用
  getCurrentMemoryUsage() {
    if ('memory' in performance) {
      const memInfo = performance.memory;
      return {
        used: memInfo.usedJSHeapSize,
        total: memInfo.totalJSHeapSize,
        limit: memInfo.jsHeapSizeLimit,
        percentage: (memInfo.usedJSHeapSize / memInfo.jsHeapSizeLimit) * 100
      };
    }
    return null;
  }
}

// 创建全局数据收集系统实例
export const dataCollectionSystem = new DataCollectionSystem();

// 导出便捷方法
export const getAnalyticsData = (timeRange) => dataCollectionSystem.getAnalyticsData(timeRange);
export const getRealTimeMetrics = () => dataCollectionSystem.getRealTimeMetrics();
```

### 1.2 数据分析引擎

```javascript
// dataAnalysisEngine.js
import * as Sentry from '@sentry/browser';

// 数据分析引擎
export class DataAnalysisEngine {
  constructor(options = {}) {
    this.options = {
      enableTrendAnalysis: true,
      enableAnomalyDetection: true,
      enablePredictiveAnalysis: false,
      analysisInterval: 300000, // 5分钟
      trendWindow: 86400000, // 24小时
      anomalyThreshold: 2, // 标准差倍数
      ...options
    };
    
    this.analysisCache = new Map();
    this.trendData = new Map();
    this.baselineMetrics = new Map();
    
    this.startAnalysisEngine();
  }
  
  startAnalysisEngine() {
    // 定期执行分析
    setInterval(() => {
      this.performAnalysis();
    }, this.options.analysisInterval);
    
    // 初始化基线指标
    this.initializeBaselines();
  }
  
  // 执行分析
  async performAnalysis() {
    try {
      const data = this.getAnalysisData();
      
      if (data.length === 0) return;
      
      const analysis = {
        timestamp: Date.now(),
        errorAnalysis: await this.analyzeErrors(data),
        performanceAnalysis: await this.analyzePerformance(data),
        userBehaviorAnalysis: await this.analyzeUserBehavior(data),
        trendAnalysis: this.options.enableTrendAnalysis ? await this.analyzeTrends(data) : null,
        anomalyAnalysis: this.options.enableAnomalyDetection ? await this.detectAnomalies(data) : null
      };
      
      // 缓存分析结果
      this.cacheAnalysisResult(analysis);
      
      // 发送分析结果到Sentry
      this.sendAnalysisToSentry(analysis);
      
      // 触发告警
      this.checkAlerts(analysis);
      
    } catch (error) {
      console.error('Analysis failed:', error);
      Sentry.captureException(error, {
        tags: {
          category: 'data-analysis',
          operation: 'perform-analysis'
        }
      });
    }
  }
  
  // 获取分析数据
  getAnalysisData() {
    try {
      const storedData = JSON.parse(localStorage.getItem('sentryAnalyticsData') || '[]');
      const cutoffTime = Date.now() - this.options.trendWindow;
      return storedData.filter(item => item.timestamp > cutoffTime);
    } catch (error) {
      console.warn('Failed to get analysis data:', error);
      return [];
    }
  }
  
  // 分析错误
  async analyzeErrors(data) {
    const errorData = data.filter(item => item.category === 'error');
    
    if (errorData.length === 0) {
      return {
        totalErrors: 0,
        errorRate: 0,
        topErrors: [],
        errorTrends: {},
        impactAnalysis: {}
      };
    }
    
    const analysis = {
      totalErrors: errorData.length,
      errorRate: this.calculateErrorRate(errorData, data),
      topErrors: this.getTopErrors(errorData),
      errorTrends: this.getErrorTrends(errorData),
      impactAnalysis: this.analyzeErrorImpact(errorData),
      categoryDistribution: this.getErrorCategoryDistribution(errorData),
      severityDistribution: this.getErrorSeverityDistribution(errorData),
      environmentDistribution: this.getErrorEnvironmentDistribution(errorData)
    };
    
    return analysis;
  }
  
  // 计算错误率
  calculateErrorRate(errorData, allData) {
    const totalEvents = allData.length;
    return totalEvents > 0 ? (errorData.length / totalEvents) * 100 : 0;
  }
  
  // 获取顶级错误
  getTopErrors(errorData) {
    const errorCounts = new Map();
    
    errorData.forEach(item => {
      const errorKey = this.getErrorKey(item.data);
      const count = errorCounts.get(errorKey) || 0;
      errorCounts.set(errorKey, count + 1);
    });
    
    return Array.from(errorCounts.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(([error, count]) => ({ error, count }));
  }
  
  // 获取错误键
  getErrorKey(errorData) {
    const errorType = errorData.exception?.values[0]?.type || 'Unknown';
    const errorMessage = errorData.message || errorData.exception?.values[0]?.value || 'Unknown';
    return `${errorType}: ${errorMessage.substring(0, 100)}`;
  }
  
  // 获取错误趋势
  getErrorTrends(errorData) {
    const hourlyTrends = new Map();
    const now = Date.now();
    
    // 初始化24小时的数据
    for (let i = 23; i >= 0; i--) {
      const hour = new Date(now - i * 3600000).getHours();
      hourlyTrends.set(hour, 0);
    }
    
    // 统计每小时的错误数
    errorData.forEach(item => {
      const hour = new Date(item.timestamp).getHours();
      const count = hourlyTrends.get(hour) || 0;
      hourlyTrends.set(hour, count + 1);
    });
    
    return Object.fromEntries(hourlyTrends);
  }
  
  // 分析错误影响
  analyzeErrorImpact(errorData) {
    const impactLevels = {
      critical: 0,
      high: 0,
      medium: 0,
      low: 0
    };
    
    const affectedUsers = new Set();
    const affectedPages = new Set();
    
    errorData.forEach(item => {
      const impact = item.data.tags?.impactLevel || 'low';
      impactLevels[impact] = (impactLevels[impact] || 0) + 1;
      
      if (item.data.user?.id) {
        affectedUsers.add(item.data.user.id);
      }
      
      if (item.data.request?.url) {
        affectedPages.add(item.data.request.url);
      }
    });
    
    return {
      impactLevels,
      affectedUsers: affectedUsers.size,
      affectedPages: affectedPages.size,
      totalImpactScore: this.calculateTotalImpactScore(impactLevels)
    };
  }
  
  // 计算总影响分数
  calculateTotalImpactScore(impactLevels) {
    const weights = { critical: 10, high: 7, medium: 4, low: 1 };
    return Object.entries(impactLevels).reduce(
      (total, [level, count]) => total + (count * (weights[level] || 1)), 0
    );
  }
  
  // 获取错误分类分布
  getErrorCategoryDistribution(errorData) {
    const distribution = {};
    
    errorData.forEach(item => {
      const category = item.data.tags?.errorCategory || 'other';
      distribution[category] = (distribution[category] || 0) + 1;
    });
    
    return distribution;
  }
  
  // 获取错误严重性分布
  getErrorSeverityDistribution(errorData) {
    const distribution = {};
    
    errorData.forEach(item => {
      const severity = item.data.level || 'error';
      distribution[severity] = (distribution[severity] || 0) + 1;
    });
    
    return distribution;
  }
  
  // 获取错误环境分布
  getErrorEnvironmentDistribution(errorData) {
    const distribution = {};
    
    errorData.forEach(item => {
      const environment = item.data.environment || 'unknown';
      distribution[environment] = (distribution[environment] || 0) + 1;
    });
    
    return distribution;
  }
  
  // 分析性能
  async analyzePerformance(data) {
    const performanceData = data.filter(item => 
      ['performance', 'resource', 'longtask', 'layout-shift', 'memory'].includes(item.category)
    );
    
    if (performanceData.length === 0) {
      return {
        overallScore: 0,
        coreWebVitals: {},
        resourceAnalysis: {},
        memoryAnalysis: {},
        performanceTrends: {}
      };
    }
    
    const analysis = {
      overallScore: this.calculateOverallPerformanceScore(performanceData),
      coreWebVitals: this.analyzeCoreWebVitals(performanceData),
      resourceAnalysis: this.analyzeResources(performanceData),
      memoryAnalysis: this.analyzeMemoryUsage(performanceData),
      performanceTrends: this.getPerformanceTrends(performanceData),
      longTaskAnalysis: this.analyzeLongTasks(performanceData),
      layoutShiftAnalysis: this.analyzeLayoutShifts(performanceData)
    };
    
    return analysis;
  }
  
  // 计算整体性能分数
  calculateOverallPerformanceScore(performanceData) {
    const navigationData = performanceData.filter(
      item => item.category === 'performance' && item.data.type === 'navigation'
    );
    
    if (navigationData.length === 0) return 0;
    
    let totalScore = 0;
    let scoreCount = 0;
    
    navigationData.forEach(item => {
      const data = item.data;
      let score = 100;
      
      // FCP评分 (0-2.5s: 100分, 2.5-4s: 50分, >4s: 0分)
      if (data.firstContentfulPaint > 4000) {
        score -= 30;
      } else if (data.firstContentfulPaint > 2500) {
        score -= 15;
      }
      
      // LCP评分 (0-2.5s: 100分, 2.5-4s: 50分, >4s: 0分)
      if (data.largestContentfulPaint > 4000) {
        score -= 30;
      } else if (data.largestContentfulPaint > 2500) {
        score -= 15;
      }
      
      // FID评分 (0-100ms: 100分, 100-300ms: 50分, >300ms: 0分)
      if (data.firstInputDelay > 300) {
        score -= 25;
      } else if (data.firstInputDelay > 100) {
        score -= 12;
      }
      
      // CLS评分 (0-0.1: 100分, 0.1-0.25: 50分, >0.25: 0分)
      if (data.cumulativeLayoutShift > 0.25) {
        score -= 15;
      } else if (data.cumulativeLayoutShift > 0.1) {
        score -= 7;
      }
      
      totalScore += Math.max(0, score);
      scoreCount++;
    });
    
    return scoreCount > 0 ? Math.round(totalScore / scoreCount) : 0;
  }
  
  // 分析Core Web Vitals
  analyzeCoreWebVitals(performanceData) {
    const navigationData = performanceData.filter(
      item => item.category === 'performance' && item.data.type === 'navigation'
    );
    
    if (navigationData.length === 0) {
      return {
        lcp: { average: 0, p75: 0, p95: 0, status: 'unknown' },
        fid: { average: 0, p75: 0, p95: 0, status: 'unknown' },
        cls: { average: 0, p75: 0, p95: 0, status: 'unknown' },
        fcp: { average: 0, p75: 0, p95: 0, status: 'unknown' }
      };
    }
    
    const lcpValues = navigationData.map(item => item.data.largestContentfulPaint).filter(v => v > 0);
    const fidValues = navigationData.map(item => item.data.firstInputDelay).filter(v => v > 0);
    const clsValues = navigationData.map(item => item.data.cumulativeLayoutShift).filter(v => v >= 0);
    const fcpValues = navigationData.map(item => item.data.firstContentfulPaint).filter(v => v > 0);
    
    return {
      lcp: this.calculateMetricStats(lcpValues, [2500, 4000]),
      fid: this.calculateMetricStats(fidValues, [100, 300]),
      cls: this.calculateMetricStats(clsValues, [0.1, 0.25]),
      fcp: this.calculateMetricStats(fcpValues, [1800, 3000])
    };
  }
  
  // 计算指标统计
  calculateMetricStats(values, thresholds) {
    if (values.length === 0) {
      return { average: 0, p75: 0, p95: 0, status: 'unknown' };
    }
    
    const sorted = values.sort((a, b) => a - b);
    const average = values.reduce((sum, val) => sum + val, 0) / values.length;
    const p75 = this.getPercentile(sorted, 75);
    const p95 = this.getPercentile(sorted, 95);
    
    let status = 'good';
    if (p75 > thresholds[1]) {
      status = 'poor';
    } else if (p75 > thresholds[0]) {
      status = 'needs-improvement';
    }
    
    return {
      average: Math.round(average),
      p75: Math.round(p75),
      p95: Math.round(p95),
      status
    };
  }
  
  // 获取百分位数
  getPercentile(sortedArray, percentile) {
    const index = (percentile / 100) * (sortedArray.length - 1);
    const lower = Math.floor(index);
    const upper = Math.ceil(index);
    const weight = index % 1;
    
    if (upper >= sortedArray.length) return sortedArray[sortedArray.length - 1];
    
    return sortedArray[lower] * (1 - weight) + sortedArray[upper] * weight;
  }
  
  // 分析资源
  analyzeResources(performanceData) {
    const resourceData = performanceData.filter(item => item.category === 'resource');
    
    if (resourceData.length === 0) {
      return {
        totalResources: 0,
        totalSize: 0,
        averageLoadTime: 0,
        slowestResources: [],
        resourceTypes: {}
      };
    }
    
    const totalSize = resourceData.reduce((sum, item) => sum + (item.data.size || 0), 0);
    const totalDuration = resourceData.reduce((sum, item) => sum + item.data.duration, 0);
    const averageLoadTime = totalDuration / resourceData.length;
    
    // 按类型分组
    const resourceTypes = {};
    resourceData.forEach(item => {
      const type = item.data.resourceType || 'other';
      if (!resourceTypes[type]) {
        resourceTypes[type] = { count: 0, totalSize: 0, totalDuration: 0 };
      }
      resourceTypes[type].count++;
      resourceTypes[type].totalSize += item.data.size || 0;
      resourceTypes[type].totalDuration += item.data.duration;
    });
    
    // 计算每种类型的平均值
    Object.keys(resourceTypes).forEach(type => {
      const typeData = resourceTypes[type];
      typeData.averageSize = typeData.totalSize / typeData.count;
      typeData.averageDuration = typeData.totalDuration / typeData.count;
    });
    
    // 找出最慢的资源
    const slowestResources = resourceData
      .sort((a, b) => b.data.duration - a.data.duration)
      .slice(0, 5)
      .map(item => ({
        name: item.data.name,
        duration: item.data.duration,
        size: item.data.size,
        type: item.data.resourceType
      }));
    
    return {
      totalResources: resourceData.length,
      totalSize,
      averageLoadTime,
      slowestResources,
      resourceTypes
    };
  }
  
  // 分析内存使用
  analyzeMemoryUsage(performanceData) {
    const memoryData = performanceData.filter(item => item.category === 'memory');
    
    if (memoryData.length === 0) {
      return {
        currentUsage: 0,
        maxUsage: 0,
        averageUsage: 0,
        trend: 'stable'
      };
    }
    
    const usageValues = memoryData.map(item => item.data.usagePercentage);
    const currentUsage = usageValues[usageValues.length - 1];
    const maxUsage = Math.max(...usageValues);
    const averageUsage = usageValues.reduce((sum, val) => sum + val, 0) / usageValues.length;
    
    // 计算趋势
    let trend = 'stable';
    if (usageValues.length > 1) {
      const recent = usageValues.slice(-5);
      const older = usageValues.slice(-10, -5);
      
      if (recent.length > 0 && older.length > 0) {
        const recentAvg = recent.reduce((sum, val) => sum + val, 0) / recent.length;
        const olderAvg = older.reduce((sum, val) => sum + val, 0) / older.length;
        
        if (recentAvg > olderAvg + 5) {
          trend = 'increasing';
        } else if (recentAvg < olderAvg - 5) {
          trend = 'decreasing';
        }
      }
    }
    
    return {
      currentUsage: Math.round(currentUsage),
      maxUsage: Math.round(maxUsage),
      averageUsage: Math.round(averageUsage),
      trend
    };
  }
  
  // 获取性能趋势
  getPerformanceTrends(performanceData) {
    const hourlyTrends = new Map();
    const now = Date.now();
    
    // 初始化24小时的数据
    for (let i = 23; i >= 0; i--) {
      const hour = new Date(now - i * 3600000).getHours();
      hourlyTrends.set(hour, {
        loadTime: [],
        resourceCount: 0,
        longTasks: 0,
        layoutShifts: 0
      });
    }
    
    // 统计每小时的性能数据
    performanceData.forEach(item => {
      const hour = new Date(item.timestamp).getHours();
      const hourData = hourlyTrends.get(hour);
      
      if (!hourData) return;
      
      switch (item.category) {
        case 'performance':
          if (item.data.type === 'navigation') {
            hourData.loadTime.push(item.data.duration);
          }
          break;
        case 'resource':
          hourData.resourceCount++;
          break;
        case 'longtask':
          hourData.longTasks++;
          break;
        case 'layout-shift':
          hourData.layoutShifts++;
          break;
      }
    });
    
    // 计算每小时的平均值
    const trends = {};
    hourlyTrends.forEach((data, hour) => {
      trends[hour] = {
        averageLoadTime: data.loadTime.length > 0 
          ? data.loadTime.reduce((sum, time) => sum + time, 0) / data.loadTime.length 
          : 0,
        resourceCount: data.resourceCount,
        longTasks: data.longTasks,
        layoutShifts: data.layoutShifts
      };
    });
    
    return trends;
  }
  
  // 分析长任务
  analyzeLongTasks(performanceData) {
    const longTaskData = performanceData.filter(item => item.category === 'longtask');
    
    if (longTaskData.length === 0) {
      return {
        totalCount: 0,
        averageDuration: 0,
        maxDuration: 0,
        impactScore: 0
      };
    }
    
    const durations = longTaskData.map(item => item.data.duration);
    const totalDuration = durations.reduce((sum, duration) => sum + duration, 0);
    const averageDuration = totalDuration / durations.length;
    const maxDuration = Math.max(...durations);
    
    // 计算影响分数 (基于数量和持续时间)
    const impactScore = Math.min(100, (longTaskData.length * 5) + (averageDuration / 10));
    
    return {
      totalCount: longTaskData.length,
      averageDuration: Math.round(averageDuration),
      maxDuration: Math.round(maxDuration),
      impactScore: Math.round(impactScore)
    };
  }
  
  // 分析布局偏移
  analyzeLayoutShifts(performanceData) {
    const layoutShiftData = performanceData.filter(item => item.category === 'layout-shift');
    
    if (layoutShiftData.length === 0) {
      return {
        totalShifts: 0,
        cumulativeScore: 0,
        averageShift: 0,
        impactLevel: 'good'
      };
    }
    
    const shiftValues = layoutShiftData.map(item => item.data.value);
    const cumulativeScore = shiftValues.reduce((sum, value) => sum + value, 0);
    const averageShift = cumulativeScore / shiftValues.length;
    
    // 确定影响级别
    let impactLevel = 'good';
    if (cumulativeScore > 0.25) {
      impactLevel = 'poor';
    } else if (cumulativeScore > 0.1) {
      impactLevel = 'needs-improvement';
    }
    
    return {
      totalShifts: layoutShiftData.length,
      cumulativeScore: Math.round(cumulativeScore * 1000) / 1000,
      averageShift: Math.round(averageShift * 1000) / 1000,
      impactLevel
    };
  }
  
  // 分析用户行为
  async analyzeUserBehavior(data) {
    const behaviorData = data.filter(item => item.category === 'interaction');
    
    if (behaviorData.length === 0) {
      return {
        totalInteractions: 0,
        engagementScore: 0,
        sessionAnalysis: {},
        interactionPatterns: {},
        userJourney: []
      };
    }
    
    const analysis = {
      totalInteractions: behaviorData.length,
      engagementScore: this.calculateEngagementScore(behaviorData),
      sessionAnalysis: this.analyzeUserSessions(behaviorData),
      interactionPatterns: this.analyzeInteractionPatterns(behaviorData),
      userJourney: this.analyzeUserJourney(behaviorData),
      deviceAnalysis: this.analyzeDeviceUsage(behaviorData),
      timeAnalysis: this.analyzeTimePatterns(behaviorData)
    };
    
    return analysis;
  }
  
  // 计算参与度分数
  calculateEngagementScore(behaviorData) {
    let score = 0;
    
    // 基于交互数量 (最多40分)
    const interactionScore = Math.min(40, behaviorData.length * 2);
    score += interactionScore;
    
    // 基于交互多样性 (最多30分)
    const interactionTypes = new Set(behaviorData.map(item => item.data.eventType));
    const diversityScore = Math.min(30, interactionTypes.size * 7.5);
    score += diversityScore;
    
    // 基于会话时长 (最多30分)
    const sessionTimes = behaviorData.map(item => item.data.sessionTime);
    const maxSessionTime = Math.max(...sessionTimes);
    const timeScore = Math.min(30, maxSessionTime / 10000); // 10秒为1分
    score += timeScore;
    
    return Math.round(score);
  }
  
  // 分析用户会话
  analyzeUserSessions(behaviorData) {
    const sessions = new Map();
    
    behaviorData.forEach(item => {
      const sessionId = item.data.sessionId || 'default';
      if (!sessions.has(sessionId)) {
        sessions.set(sessionId, {
          interactions: [],
          startTime: item.timestamp,
          endTime: item.timestamp
        });
      }
      
      const session = sessions.get(sessionId);
      session.interactions.push(item);
      session.endTime = Math.max(session.endTime, item.timestamp);
    });
    
    const sessionAnalysis = {
      totalSessions: sessions.size,
      averageSessionDuration: 0,
      averageInteractionsPerSession: 0,
      longestSession: 0,
      shortestSession: Infinity
    };
    
    let totalDuration = 0;
    let totalInteractions = 0;
    
    sessions.forEach(session => {
      const duration = session.endTime - session.startTime;
      totalDuration += duration;
      totalInteractions += session.interactions.length;
      
      sessionAnalysis.longestSession = Math.max(sessionAnalysis.longestSession, duration);
      sessionAnalysis.shortestSession = Math.min(sessionAnalysis.shortestSession, duration);
    });
    
    sessionAnalysis.averageSessionDuration = Math.round(totalDuration / sessions.size);
    sessionAnalysis.averageInteractionsPerSession = Math.round(totalInteractions / sessions.size);
    
    if (sessionAnalysis.shortestSession === Infinity) {
      sessionAnalysis.shortestSession = 0;
    }
    
    return sessionAnalysis;
  }
  
  // 分析交互模式
  analyzeInteractionPatterns(behaviorData) {
    const patterns = {
      byType: {},
      byTarget: {},
      byTime: {},
      sequences: []
    };
    
    // 按类型统计
    behaviorData.forEach(item => {
      const type = item.data.eventType;
      patterns.byType[type] = (patterns.byType[type] || 0) + 1;
    });
    
    // 按目标元素统计
    behaviorData.forEach(item => {
      const target = item.data.target;
      patterns.byTarget[target] = (patterns.byTarget[target] || 0) + 1;
    });
    
    // 按时间段统计
    behaviorData.forEach(item => {
      const hour = new Date(item.timestamp).getHours();
      patterns.byTime[hour] = (patterns.byTime[hour] || 0) + 1;
    });
    
    // 分析交互序列
    const sortedData = behaviorData.sort((a, b) => a.timestamp - b.timestamp);
    for (let i = 0; i < sortedData.length - 1; i++) {
      const current = sortedData[i];
      const next = sortedData[i + 1];
      const timeDiff = next.timestamp - current.timestamp;
      
      if (timeDiff < 5000) { // 5秒内的连续交互
        patterns.sequences.push({
          from: current.data.eventType,
          to: next.data.eventType,
          interval: timeDiff
        });
      }
    }
    
    return patterns;
  }
  
  // 分析用户旅程
  analyzeUserJourney(behaviorData) {
    const journey = [];
    const sortedData = behaviorData.sort((a, b) => a.timestamp - b.timestamp);
    
    let currentPage = null;
    let pageStartTime = null;
    
    sortedData.forEach(item => {
      const page = this.extractPageFromInteraction(item);
      
      if (page !== currentPage) {
        if (currentPage) {
          journey.push({
            page: currentPage,
            startTime: pageStartTime,
            endTime: item.timestamp,
            duration: item.timestamp - pageStartTime,
            interactions: sortedData.filter(d => 
              d.timestamp >= pageStartTime && 
              d.timestamp < item.timestamp &&
              this.extractPageFromInteraction(d) === currentPage
            ).length
          });
        }
        
        currentPage = page;
        pageStartTime = item.timestamp;
      }
    });
    
    // 添加最后一个页面
    if (currentPage && pageStartTime) {
      const lastTimestamp = sortedData[sortedData.length - 1].timestamp;
      journey.push({
        page: currentPage,
        startTime: pageStartTime,
        endTime: lastTimestamp,
        duration: lastTimestamp - pageStartTime,
        interactions: sortedData.filter(d => 
          d.timestamp >= pageStartTime &&
          this.extractPageFromInteraction(d) === currentPage
        ).length
      });
    }
    
    return journey;
  }
  
  // 从交互中提取页面信息
  extractPageFromInteraction(interaction) {
    // 这里可以根据实际情况实现页面提取逻辑
    return interaction.data.page || window.location.pathname;
  }
  
  // 分析设备使用情况
  analyzeDeviceUsage(behaviorData) {
    const deviceTypes = {};
    const browsers = {};
    const screenSizes = {};
    
    behaviorData.forEach(item => {
      const deviceType = item.data.deviceType || 'unknown';
      const browser = item.data.browser || 'unknown';
      const screenSize = item.data.screenSize || 'unknown';
      
      deviceTypes[deviceType] = (deviceTypes[deviceType] || 0) + 1;
      browsers[browser] = (browsers[browser] || 0) + 1;
      screenSizes[screenSize] = (screenSizes[screenSize] || 0) + 1;
    });
    
    return {
      deviceTypes,
      browsers,
      screenSizes
    };
  }
  
  // 分析时间模式
  analyzeTimePatterns(behaviorData) {
    const hourlyActivity = new Array(24).fill(0);
    const dailyActivity = {};
    
    behaviorData.forEach(item => {
      const date = new Date(item.timestamp);
      const hour = date.getHours();
      const day = date.toDateString();
      
      hourlyActivity[hour]++;
      dailyActivity[day] = (dailyActivity[day] || 0) + 1;
    });
    
    // 找出最活跃的时间段
    const peakHour = hourlyActivity.indexOf(Math.max(...hourlyActivity));
    const peakDay = Object.keys(dailyActivity).reduce((a, b) => 
      dailyActivity[a] > dailyActivity[b] ? a : b
    );
    
    return {
       hourlyActivity,
       dailyActivity,
       peakHour,
       peakDay,
       totalActiveDays: Object.keys(dailyActivity).length
     };
   }
}

// 使用示例
const dataAnalyzer = new SentryDataAnalyzer();

// 启动数据收集
dataAnalyzer.startDataCollection();

// 定期执行分析
setInterval(async () => {
  const analysis = await dataAnalyzer.performAnalysis();
  console.log('分析结果:', analysis);
}, 60000); // 每分钟分析一次
```

## 3. 智能化监控仪表板实现

### 3.1 实时监控仪表板组件

```javascript
// 智能监控仪表板
class IntelligentMonitoringDashboard {
  constructor(containerId, options = {}) {
    this.container = document.getElementById(containerId);
    this.options = {
      refreshInterval: 30000, // 30秒刷新间隔
      theme: 'light',
      autoResize: true,
      ...options
    };
    
    this.charts = new Map();
    this.widgets = new Map();
    this.dataAnalyzer = new SentryDataAnalyzer();
    this.alertManager = new AlertManager();
    
    this.init();
  }
  
  // 初始化仪表板
  async init() {
    this.createLayout();
    this.setupEventListeners();
    await this.loadInitialData();
    this.startRealTimeUpdates();
    
    // 启用自动调整大小
    if (this.options.autoResize) {
      window.addEventListener('resize', this.handleResize.bind(this));
    }
  }
  
  // 创建布局
  createLayout() {
    this.container.innerHTML = `
      <div class="dashboard-header">
        <h1>智能监控仪表板</h1>
        <div class="dashboard-controls">
          <select id="timeRange">
            <option value="1h">最近1小时</option>
            <option value="6h">最近6小时</option>
            <option value="24h" selected>最近24小时</option>
            <option value="7d">最近7天</option>
          </select>
          <button id="refreshBtn">刷新</button>
          <button id="exportBtn">导出</button>
        </div>
      </div>
      
      <div class="dashboard-grid">
        <!-- 关键指标卡片 -->
        <div class="metrics-row">
          <div class="metric-card" id="errorRate">
            <h3>错误率</h3>
            <div class="metric-value">-</div>
            <div class="metric-trend">-</div>
          </div>
          <div class="metric-card" id="performanceScore">
            <h3>性能评分</h3>
            <div class="metric-value">-</div>
            <div class="metric-trend">-</div>
          </div>
          <div class="metric-card" id="userSatisfaction">
            <h3>用户满意度</h3>
            <div class="metric-value">-</div>
            <div class="metric-trend">-</div>
          </div>
          <div class="metric-card" id="activeUsers">
            <h3>活跃用户</h3>
            <div class="metric-value">-</div>
            <div class="metric-trend">-</div>
          </div>
        </div>
        
        <!-- 图表区域 -->
        <div class="charts-row">
          <div class="chart-container">
            <h3>错误趋势</h3>
            <canvas id="errorTrendChart"></canvas>
          </div>
          <div class="chart-container">
            <h3>性能指标</h3>
            <canvas id="performanceChart"></canvas>
          </div>
        </div>
        
        <div class="charts-row">
          <div class="chart-container">
            <h3>用户行为热图</h3>
            <div id="userHeatmap"></div>
          </div>
          <div class="chart-container">
            <h3>实时告警</h3>
            <div id="alertsList"></div>
          </div>
        </div>
        
        <!-- 详细分析区域 -->
        <div class="analysis-row">
          <div class="analysis-panel">
            <h3>智能分析建议</h3>
            <div id="intelligentInsights"></div>
          </div>
          <div class="analysis-panel">
            <h3>性能瓶颈识别</h3>
            <div id="bottleneckAnalysis"></div>
          </div>
        </div>
      </div>
    `;
    
    this.addStyles();
  }
  
  // 添加样式
  addStyles() {
    const styles = `
      <style>
        .dashboard-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 20px;
          background: #f8f9fa;
          border-bottom: 1px solid #e9ecef;
        }
        
        .dashboard-controls {
          display: flex;
          gap: 10px;
        }
        
        .dashboard-controls select,
        .dashboard-controls button {
          padding: 8px 16px;
          border: 1px solid #ddd;
          border-radius: 4px;
          background: white;
          cursor: pointer;
        }
        
        .dashboard-grid {
          padding: 20px;
        }
        
        .metrics-row {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 20px;
          margin-bottom: 30px;
        }
        
        .metric-card {
          background: white;
          padding: 20px;
          border-radius: 8px;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
          text-align: center;
        }
        
        .metric-card h3 {
          margin: 0 0 10px 0;
          color: #666;
          font-size: 14px;
          text-transform: uppercase;
        }
        
        .metric-value {
          font-size: 32px;
          font-weight: bold;
          color: #333;
          margin-bottom: 5px;
        }
        
        .metric-trend {
          font-size: 12px;
          color: #666;
        }
        
        .metric-trend.positive {
          color: #28a745;
        }
        
        .metric-trend.negative {
          color: #dc3545;
        }
        
        .charts-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
          margin-bottom: 30px;
        }
        
        .chart-container {
          background: white;
          padding: 20px;
          border-radius: 8px;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .chart-container h3 {
          margin: 0 0 20px 0;
          color: #333;
        }
        
        .analysis-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
        }
        
        .analysis-panel {
          background: white;
          padding: 20px;
          border-radius: 8px;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .analysis-panel h3 {
          margin: 0 0 15px 0;
          color: #333;
        }
        
        .insight-item {
          padding: 10px;
          margin-bottom: 10px;
          border-left: 4px solid #007bff;
          background: #f8f9fa;
          border-radius: 4px;
        }
        
        .insight-item.warning {
          border-left-color: #ffc107;
        }
        
        .insight-item.error {
          border-left-color: #dc3545;
        }
        
        .alert-item {
          padding: 12px;
          margin-bottom: 8px;
          border-radius: 4px;
          border-left: 4px solid #dc3545;
          background: #f8d7da;
        }
        
        .alert-item.warning {
          border-left-color: #ffc107;
          background: #fff3cd;
        }
        
        .alert-item.info {
          border-left-color: #17a2b8;
          background: #d1ecf1;
        }
        
        @media (max-width: 768px) {
          .charts-row,
          .analysis-row {
            grid-template-columns: 1fr;
          }
          
          .metrics-row {
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          }
        }
      </style>
    `;
    
    document.head.insertAdjacentHTML('beforeend', styles);
  }
  
  // 设置事件监听器
  setupEventListeners() {
    // 时间范围选择
    document.getElementById('timeRange').addEventListener('change', (e) => {
      this.updateTimeRange(e.target.value);
    });
    
    // 刷新按钮
    document.getElementById('refreshBtn').addEventListener('click', () => {
      this.refreshData();
    });
    
    // 导出按钮
    document.getElementById('exportBtn').addEventListener('click', () => {
      this.exportData();
    });
  }
  
  // 加载初始数据
  async loadInitialData() {
    try {
      const analysis = await this.dataAnalyzer.performAnalysis();
      this.updateDashboard(analysis);
    } catch (error) {
      console.error('加载初始数据失败:', error);
      this.showError('数据加载失败，请稍后重试');
    }
  }
  
  // 启动实时更新
  startRealTimeUpdates() {
    this.updateInterval = setInterval(async () => {
      try {
        const analysis = await this.dataAnalyzer.performAnalysis();
        this.updateDashboard(analysis);
      } catch (error) {
        console.error('实时更新失败:', error);
      }
    }, this.options.refreshInterval);
  }
  
  // 更新仪表板
  updateDashboard(analysis) {
    this.updateMetricCards(analysis);
    this.updateCharts(analysis);
    this.updateInsights(analysis);
    this.updateAlerts(analysis);
  }
  
  // 更新指标卡片
  updateMetricCards(analysis) {
    const { errorAnalysis, performanceAnalysis, userBehaviorAnalysis } = analysis;
    
    // 错误率
    const errorRate = errorAnalysis.errorRate;
    this.updateMetricCard('errorRate', {
      value: `${errorRate.toFixed(2)}%`,
      trend: this.calculateTrend(errorRate, 'error'),
      status: errorRate > 5 ? 'error' : errorRate > 2 ? 'warning' : 'good'
    });
    
    // 性能评分
    const performanceScore = this.calculatePerformanceScore(performanceAnalysis);
    this.updateMetricCard('performanceScore', {
      value: performanceScore,
      trend: this.calculateTrend(performanceScore, 'performance'),
      status: performanceScore < 60 ? 'error' : performanceScore < 80 ? 'warning' : 'good'
    });
    
    // 用户满意度
    const userSatisfaction = userBehaviorAnalysis.engagementScore;
    this.updateMetricCard('userSatisfaction', {
      value: `${userSatisfaction}/100`,
      trend: this.calculateTrend(userSatisfaction, 'satisfaction'),
      status: userSatisfaction < 60 ? 'error' : userSatisfaction < 80 ? 'warning' : 'good'
    });
    
    // 活跃用户
    const activeUsers = userBehaviorAnalysis.sessionAnalysis.totalSessions || 0;
    this.updateMetricCard('activeUsers', {
      value: activeUsers.toLocaleString(),
      trend: this.calculateTrend(activeUsers, 'users'),
      status: 'info'
    });
  }
  
  // 更新单个指标卡片
  updateMetricCard(cardId, data) {
    const card = document.getElementById(cardId);
    const valueElement = card.querySelector('.metric-value');
    const trendElement = card.querySelector('.metric-trend');
    
    valueElement.textContent = data.value;
    trendElement.textContent = data.trend.text;
    trendElement.className = `metric-trend ${data.trend.direction}`;
    
    // 更新卡片状态颜色
    card.className = `metric-card ${data.status}`;
  }
  
  // 计算趋势
  calculateTrend(currentValue, type) {
    // 这里应该与历史数据比较，简化实现
    const change = Math.random() * 20 - 10; // 模拟变化
    const direction = change > 0 ? 'positive' : change < 0 ? 'negative' : 'neutral';
    
    let text = '';
    if (Math.abs(change) < 1) {
      text = '保持稳定';
    } else {
      text = `${change > 0 ? '↑' : '↓'} ${Math.abs(change).toFixed(1)}%`;
    }
    
    return { direction, text };
  }
  
  // 计算性能评分
  calculatePerformanceScore(performanceAnalysis) {
    const { coreWebVitals, resourceAnalysis, longTaskAnalysis } = performanceAnalysis;
    
    let score = 100;
    
    // LCP评分 (最大内容绘制)
    if (coreWebVitals.lcp > 4000) {
      score -= 30;
    } else if (coreWebVitals.lcp > 2500) {
      score -= 15;
    }
    
    // FID评分 (首次输入延迟)
    if (coreWebVitals.fid > 300) {
      score -= 25;
    } else if (coreWebVitals.fid > 100) {
      score -= 10;
    }
    
    // CLS评分 (累积布局偏移)
    if (coreWebVitals.cls > 0.25) {
      score -= 20;
    } else if (coreWebVitals.cls > 0.1) {
      score -= 10;
    }
    
    // 长任务影响
    score -= Math.min(15, longTaskAnalysis.impactScore / 10);
    
    return Math.max(0, Math.round(score));
  }
  
  // 更新图表
  updateCharts(analysis) {
    this.updateErrorTrendChart(analysis.errorAnalysis);
    this.updatePerformanceChart(analysis.performanceAnalysis);
    this.updateUserHeatmap(analysis.userBehaviorAnalysis);
  }
  
  // 更新错误趋势图表
  updateErrorTrendChart(errorAnalysis) {
    const canvas = document.getElementById('errorTrendChart');
    const ctx = canvas.getContext('2d');
    
    // 简化的图表实现
    const data = errorAnalysis.trends || [];
    this.renderLineChart(ctx, data, {
      title: '错误趋势',
      color: '#dc3545',
      yAxisLabel: '错误数量'
    });
  }
  
  // 更新性能图表
  updatePerformanceChart(performanceAnalysis) {
    const canvas = document.getElementById('performanceChart');
    const ctx = canvas.getContext('2d');
    
    const data = [
      { label: 'LCP', value: performanceAnalysis.coreWebVitals.lcp },
      { label: 'FID', value: performanceAnalysis.coreWebVitals.fid },
      { label: 'CLS', value: performanceAnalysis.coreWebVitals.cls * 1000 }
    ];
    
    this.renderBarChart(ctx, data, {
      title: '核心性能指标',
      colors: ['#007bff', '#28a745', '#ffc107']
    });
  }
  
  // 更新用户行为热图
  updateUserHeatmap(userBehaviorAnalysis) {
    const container = document.getElementById('userHeatmap');
    const timeAnalysis = userBehaviorAnalysis.timeAnalysis;
    
    if (!timeAnalysis) {
      container.innerHTML = '<p>暂无用户行为数据</p>';
      return;
    }
    
    const hourlyData = timeAnalysis.hourlyActivity;
    const maxActivity = Math.max(...hourlyData);
    
    let heatmapHTML = '<div class="heatmap-grid">';
    
    for (let hour = 0; hour < 24; hour++) {
      const activity = hourlyData[hour] || 0;
      const intensity = maxActivity > 0 ? activity / maxActivity : 0;
      const opacity = Math.max(0.1, intensity);
      
      heatmapHTML += `
        <div class="heatmap-cell" 
             style="background-color: rgba(0, 123, 255, ${opacity})"
             title="${hour}:00 - ${activity} 次交互">
          ${hour}
        </div>
      `;
    }
    
    heatmapHTML += '</div>';
    
    // 添加热图样式
    const heatmapStyles = `
      <style>
        .heatmap-grid {
          display: grid;
          grid-template-columns: repeat(12, 1fr);
          gap: 2px;
          margin-top: 10px;
        }
        
        .heatmap-cell {
          aspect-ratio: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 4px;
          font-size: 12px;
          color: white;
          font-weight: bold;
          cursor: pointer;
          transition: transform 0.2s;
        }
        
        .heatmap-cell:hover {
          transform: scale(1.1);
        }
      </style>
    `;
    
    if (!document.querySelector('.heatmap-styles')) {
      document.head.insertAdjacentHTML('beforeend', heatmapStyles.replace('<style>', '<style class="heatmap-styles">'));
    }
    
    container.innerHTML = heatmapHTML;
  }
  
  // 渲染折线图
  renderLineChart(ctx, data, options) {
    const canvas = ctx.canvas;
    const width = canvas.width;
    const height = canvas.height;
    
    ctx.clearRect(0, 0, width, height);
    
    if (!data || data.length === 0) {
      ctx.fillStyle = '#666';
      ctx.font = '14px Arial';
      ctx.textAlign = 'center';
      ctx.fillText('暂无数据', width / 2, height / 2);
      return;
    }
    
    // 简化的折线图实现
    const padding = 40;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;
    
    const maxValue = Math.max(...data.map(d => d.value));
    const minValue = Math.min(...data.map(d => d.value));
    const valueRange = maxValue - minValue || 1;
    
    ctx.strokeStyle = options.color;
    ctx.lineWidth = 2;
    ctx.beginPath();
    
    data.forEach((point, index) => {
      const x = padding + (index / (data.length - 1)) * chartWidth;
      const y = padding + chartHeight - ((point.value - minValue) / valueRange) * chartHeight;
      
      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    
    ctx.stroke();
  }
  
  // 渲染柱状图
  renderBarChart(ctx, data, options) {
    const canvas = ctx.canvas;
    const width = canvas.width;
    const height = canvas.height;
    
    ctx.clearRect(0, 0, width, height);
    
    if (!data || data.length === 0) {
      ctx.fillStyle = '#666';
      ctx.font = '14px Arial';
      ctx.textAlign = 'center';
      ctx.fillText('暂无数据', width / 2, height / 2);
      return;
    }
    
    const padding = 40;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;
    const barWidth = chartWidth / data.length * 0.8;
    const barSpacing = chartWidth / data.length * 0.2;
    
    const maxValue = Math.max(...data.map(d => d.value));
    
    data.forEach((item, index) => {
      const barHeight = (item.value / maxValue) * chartHeight;
      const x = padding + index * (barWidth + barSpacing);
      const y = padding + chartHeight - barHeight;
      
      ctx.fillStyle = options.colors[index % options.colors.length];
      ctx.fillRect(x, y, barWidth, barHeight);
      
      // 绘制标签
      ctx.fillStyle = '#333';
      ctx.font = '12px Arial';
      ctx.textAlign = 'center';
      ctx.fillText(item.label, x + barWidth / 2, height - 10);
       ctx.fillText(item.value.toFixed(0), x + barWidth / 2, y - 5);
     });
   }
   
   // 更新智能分析建议
   updateInsights(analysis) {
     const container = document.getElementById('intelligentInsights');
     const insights = this.generateInsights(analysis);
     
     let insightsHTML = '';
     insights.forEach(insight => {
       insightsHTML += `
         <div class="insight-item ${insight.type}">
           <strong>${insight.title}</strong>
           <p>${insight.description}</p>
           ${insight.action ? `<small>建议: ${insight.action}</small>` : ''}
         </div>
       `;
     });
     
     container.innerHTML = insightsHTML || '<p>暂无分析建议</p>';
   }
   
   // 生成智能分析建议
   generateInsights(analysis) {
     const insights = [];
     const { errorAnalysis, performanceAnalysis, userBehaviorAnalysis } = analysis;
     
     // 错误率分析
     if (errorAnalysis.errorRate > 5) {
       insights.push({
         type: 'error',
         title: '错误率过高',
         description: `当前错误率为 ${errorAnalysis.errorRate.toFixed(2)}%，超过了5%的警戒线`,
         action: '建议立即检查最近的代码部署，排查高频错误原因'
       });
     } else if (errorAnalysis.errorRate > 2) {
       insights.push({
         type: 'warning',
         title: '错误率偏高',
         description: `当前错误率为 ${errorAnalysis.errorRate.toFixed(2)}%，需要关注`,
         action: '建议分析错误类型分布，优化容错处理'
       });
     }
     
     // 性能分析
     const performanceScore = this.calculatePerformanceScore(performanceAnalysis);
     if (performanceScore < 60) {
       insights.push({
         type: 'error',
         title: '性能严重不足',
         description: `性能评分仅为 ${performanceScore}，用户体验较差`,
         action: '建议进行性能优化：代码分割、资源压缩、CDN加速等'
       });
     } else if (performanceScore < 80) {
       insights.push({
         type: 'warning',
         title: '性能有待提升',
         description: `性能评分为 ${performanceScore}，还有优化空间`,
         action: '建议优化Core Web Vitals指标，特别关注LCP和FID'
       });
     }
     
     // 长任务分析
     if (performanceAnalysis.longTaskAnalysis.totalCount > 10) {
       insights.push({
         type: 'warning',
         title: '长任务过多',
         description: `检测到 ${performanceAnalysis.longTaskAnalysis.totalCount} 个长任务`,
         action: '建议使用Web Workers或时间切片技术优化长任务'
       });
     }
     
     // 布局偏移分析
     if (performanceAnalysis.layoutShiftAnalysis.impactLevel === 'poor') {
       insights.push({
         type: 'error',
         title: '布局偏移严重',
         description: `CLS评分为 ${performanceAnalysis.layoutShiftAnalysis.cumulativeScore}`,
         action: '建议为图片和广告设置固定尺寸，避免动态内容插入'
       });
     }
     
     // 用户参与度分析
     if (userBehaviorAnalysis.engagementScore < 60) {
       insights.push({
         type: 'warning',
         title: '用户参与度较低',
         description: `用户参与度评分为 ${userBehaviorAnalysis.engagementScore}`,
         action: '建议优化用户界面和交互体验，增加用户粘性'
       });
     }
     
     // 会话分析
     const sessionAnalysis = userBehaviorAnalysis.sessionAnalysis;
     if (sessionAnalysis.averageSessionDuration < 30000) { // 30秒
       insights.push({
         type: 'info',
         title: '会话时长较短',
         description: `平均会话时长为 ${Math.round(sessionAnalysis.averageSessionDuration / 1000)} 秒`,
         action: '建议分析用户流失点，优化页面内容和导航'
       });
     }
     
     return insights;
   }
   
   // 更新告警列表
   updateAlerts(analysis) {
     const container = document.getElementById('alertsList');
     const alerts = this.generateAlerts(analysis);
     
     let alertsHTML = '';
     alerts.forEach(alert => {
       alertsHTML += `
         <div class="alert-item ${alert.severity}">
           <div class="alert-header">
             <strong>${alert.title}</strong>
             <span class="alert-time">${alert.time}</span>
           </div>
           <p>${alert.message}</p>
         </div>
       `;
     });
     
     container.innerHTML = alertsHTML || '<p>暂无告警信息</p>';
   }
   
   // 生成告警信息
   generateAlerts(analysis) {
     const alerts = [];
     const now = new Date();
     
     // 基于分析结果生成告警
     const { errorAnalysis, performanceAnalysis } = analysis;
     
     if (errorAnalysis.errorRate > 10) {
       alerts.push({
         severity: 'error',
         title: '严重错误告警',
         message: `错误率达到 ${errorAnalysis.errorRate.toFixed(2)}%，需要立即处理`,
         time: now.toLocaleTimeString()
       });
     }
     
     if (performanceAnalysis.coreWebVitals.lcp > 4000) {
       alerts.push({
         severity: 'warning',
         title: 'LCP性能告警',
         message: `最大内容绘制时间为 ${performanceAnalysis.coreWebVitals.lcp}ms，超过4秒阈值`,
         time: now.toLocaleTimeString()
       });
     }
     
     if (performanceAnalysis.longTaskAnalysis.totalCount > 20) {
       alerts.push({
         severity: 'warning',
         title: '长任务告警',
         message: `检测到 ${performanceAnalysis.longTaskAnalysis.totalCount} 个长任务，可能影响用户体验`,
         time: now.toLocaleTimeString()
       });
     }
     
     return alerts.slice(0, 5); // 只显示最近5条告警
   }
   
   // 更新时间范围
   updateTimeRange(range) {
     this.currentTimeRange = range;
     this.refreshData();
   }
   
   // 刷新数据
   async refreshData() {
     try {
       const analysis = await this.dataAnalyzer.performAnalysis();
       this.updateDashboard(analysis);
       this.showSuccess('数据已刷新');
     } catch (error) {
       console.error('刷新数据失败:', error);
       this.showError('数据刷新失败');
     }
   }
   
   // 导出数据
   exportData() {
     try {
       const data = {
         timestamp: new Date().toISOString(),
         timeRange: this.currentTimeRange,
         analysis: this.lastAnalysis
       };
       
       const blob = new Blob([JSON.stringify(data, null, 2)], {
         type: 'application/json'
       });
       
       const url = URL.createObjectURL(blob);
       const a = document.createElement('a');
       a.href = url;
       a.download = `sentry-analysis-${Date.now()}.json`;
       document.body.appendChild(a);
       a.click();
       document.body.removeChild(a);
       URL.revokeObjectURL(url);
       
       this.showSuccess('数据已导出');
     } catch (error) {
       console.error('导出数据失败:', error);
       this.showError('数据导出失败');
     }
   }
   
   // 处理窗口大小调整
   handleResize() {
     // 重新渲染图表
     if (this.lastAnalysis) {
       this.updateCharts(this.lastAnalysis);
     }
   }
   
   // 显示成功消息
   showSuccess(message) {
     this.showNotification(message, 'success');
   }
   
   // 显示错误消息
   showError(message) {
     this.showNotification(message, 'error');
   }
   
   // 显示通知
   showNotification(message, type) {
     const notification = document.createElement('div');
     notification.className = `notification ${type}`;
     notification.textContent = message;
     
     notification.style.cssText = `
       position: fixed;
       top: 20px;
       right: 20px;
       padding: 12px 20px;
       border-radius: 4px;
       color: white;
       font-weight: bold;
       z-index: 1000;
       animation: slideIn 0.3s ease-out;
       background: ${type === 'success' ? '#28a745' : '#dc3545'};
     `;
     
     document.body.appendChild(notification);
     
     setTimeout(() => {
       notification.style.animation = 'slideOut 0.3s ease-in';
       setTimeout(() => {
         document.body.removeChild(notification);
       }, 300);
     }, 3000);
   }
   
   // 销毁仪表板
   destroy() {
     if (this.updateInterval) {
       clearInterval(this.updateInterval);
     }
     
     if (this.options.autoResize) {
       window.removeEventListener('resize', this.handleResize.bind(this));
     }
     
     this.container.innerHTML = '';
   }
 }
 
 // 告警管理器
 class AlertManager {
   constructor(options = {}) {
     this.options = {
       maxAlerts: 100,
       retentionTime: 24 * 60 * 60 * 1000, // 24小时
       ...options
     };
     
     this.alerts = [];
     this.subscribers = [];
   }
   
   // 添加告警
   addAlert(alert) {
     const alertWithId = {
       id: Date.now() + Math.random(),
       timestamp: Date.now(),
       ...alert
     };
     
     this.alerts.unshift(alertWithId);
     
     // 限制告警数量
     if (this.alerts.length > this.options.maxAlerts) {
       this.alerts = this.alerts.slice(0, this.options.maxAlerts);
     }
     
     // 通知订阅者
     this.notifySubscribers(alertWithId);
     
     // 清理过期告警
     this.cleanupExpiredAlerts();
   }
   
   // 获取告警列表
   getAlerts(filter = {}) {
     let filteredAlerts = [...this.alerts];
     
     if (filter.severity) {
       filteredAlerts = filteredAlerts.filter(alert => alert.severity === filter.severity);
     }
     
     if (filter.since) {
       filteredAlerts = filteredAlerts.filter(alert => alert.timestamp >= filter.since);
     }
     
     if (filter.limit) {
       filteredAlerts = filteredAlerts.slice(0, filter.limit);
     }
     
     return filteredAlerts;
   }
   
   // 订阅告警
   subscribe(callback) {
     this.subscribers.push(callback);
     
     // 返回取消订阅函数
     return () => {
       const index = this.subscribers.indexOf(callback);
       if (index > -1) {
         this.subscribers.splice(index, 1);
       }
     };
   }
   
   // 通知订阅者
   notifySubscribers(alert) {
     this.subscribers.forEach(callback => {
       try {
         callback(alert);
       } catch (error) {
         console.error('告警通知失败:', error);
       }
     });
   }
   
   // 清理过期告警
   cleanupExpiredAlerts() {
     const now = Date.now();
     this.alerts = this.alerts.filter(alert => 
       now - alert.timestamp < this.options.retentionTime
     );
   }
   
   // 清除所有告警
   clearAlerts() {
     this.alerts = [];
   }
 }
 
 // 使用示例
 const dashboard = new IntelligentMonitoringDashboard('dashboard-container', {
   refreshInterval: 30000,
   theme: 'light',
   autoResize: true
 });
 
 // 监听告警
 const alertManager = new AlertManager();
 alertManager.subscribe(alert => {
   console.log('新告警:', alert);
   
   // 可以在这里添加更多的告警处理逻辑
   // 比如发送邮件、推送通知等
 });
 ```
 
 ## 4. 核心价值与实施建议
 
 ### 4.1 核心价值
 
 **数据驱动决策**
 - 基于真实用户数据进行产品优化
 - 量化用户体验指标
 - 识别性能瓶颈和改进机会
 
 **智能化监控**
 - 自动异常检测和告警
 - 智能分析建议生成
 - 实时性能监控和优化
 
 **可视化洞察**
 - 直观的数据展示和趋势分析
 - 多维度数据关联分析
 - 自定义仪表板和报告
 
 ### 4.2 实施建议
 
 **分阶段实施**
 1. **基础阶段**: 实现数据收集和基本分析
 2. **进阶阶段**: 添加智能分析和可视化
 3. **高级阶段**: 构建完整的监控体系
 
 **团队协作**
 - 开发团队负责数据收集和技术实现
 - 产品团队参与指标定义和分析
 - 运维团队负责监控和告警处理
 
 **持续改进**
 - 定期评估和优化监控策略
 - 根据业务需求调整分析维度
 - 持续完善告警规则和阈值
 
 ## 5. 未来发展趋势
 
 ### 5.1 AI驱动的智能分析
 
 **机器学习预测**
 - 基于历史数据预测性能趋势
 - 智能异常检测和根因分析
 - 自动化性能优化建议
 
 **自然语言处理**
 - 智能错误分类和聚合
 - 自动生成分析报告
 - 语音交互式数据查询
 
 ### 5.2 边缘计算集成
 
 **边缘数据处理**
 - 就近处理用户数据，减少延迟
 - 实时性能监控和优化
 - 分布式数据分析架构
 
 **智能缓存策略**
 - 基于用户行为的智能缓存
 - 动态资源优化和分发
 - 边缘计算性能提升
 
 ### 5.3 隐私保护增强
 
 **数据脱敏技术**
 - 自动识别和保护敏感信息
 - 差分隐私数据分析
 - 联邦学习模型训练
 
 **合规性自动化**
 - 自动化GDPR合规检查
 - 数据生命周期管理
 - 隐私影响评估工具
 
 ## 总结
 
 前端Sentry数据分析与可视化是构建现代Web应用监控体系的重要组成部分。通过系统化的数据收集、智能化的分析处理和直观的可视化展示，我们可以：
 
 1. **全面了解应用状态**: 从错误监控到性能分析，从用户行为到业务指标
 2. **快速识别问题**: 通过智能告警和异常检测，及时发现和解决问题
 3. **数据驱动优化**: 基于真实数据进行产品决策和性能优化
 4. **提升用户体验**: 通过持续监控和改进，不断提升用户满意度
 
 随着技术的不断发展，前端监控将更加智能化、自动化和个性化。掌握这些技术和方法，将帮助我们构建更加稳定、高效和用户友好的Web应用。