# 前端Sentry与微服务架构集成：分布式监控解决方案

在微服务架构日益普及的今天，前端应用往往需要与多个后端服务进行交互。如何在这种分布式环境中实现有效的错误监控和性能追踪，成为了开发团队面临的重要挑战。本文将深入探讨如何将前端Sentry与微服务架构进行深度集成，构建完整的分布式监控解决方案。

## 1. 微服务监控架构设计

### 1.1 分布式追踪管理器

```javascript
// 分布式追踪管理器
class DistributedTracingManager {
  constructor(options = {}) {
    this.options = {
      serviceName: options.serviceName || 'frontend-app',
      environment: options.environment || 'production',
      traceIdHeader: options.traceIdHeader || 'x-trace-id',
      spanIdHeader: options.spanIdHeader || 'x-span-id',
      parentSpanIdHeader: options.parentSpanIdHeader || 'x-parent-span-id',
      samplingRate: options.samplingRate || 0.1,
      maxSpanDuration: options.maxSpanDuration || 30000, // 30秒
      ...options
    };
    
    this.activeSpans = new Map();
    this.traceContext = new Map();
    this.spanIdGenerator = this.createSpanIdGenerator();
    
    this.initializeTracing();
  }
  
  initializeTracing() {
    // 初始化Sentry性能监控
    if (typeof Sentry !== 'undefined') {
      Sentry.addGlobalEventProcessor((event) => {
        if (event.type === 'transaction') {
          this.enrichTransactionWithTraceData(event);
        }
        return event;
      });
    }
    
    // 拦截网络请求，添加追踪头
    this.interceptNetworkRequests();
    
    // 监听页面导航事件
    this.setupNavigationTracking();
  }
  
  // 创建新的追踪
  startTrace(operationName, data = {}) {
    const traceId = this.generateTraceId();
    const spanId = this.generateSpanId();
    
    const span = {
      traceId,
      spanId,
      parentSpanId: null,
      operationName,
      startTime: Date.now(),
      endTime: null,
      duration: null,
      status: 'active',
      tags: {
        service: this.options.serviceName,
        environment: this.options.environment,
        ...data.tags
      },
      logs: [],
      data: { ...data }
    };
    
    this.activeSpans.set(spanId, span);
    this.traceContext.set(traceId, {
      rootSpanId: spanId,
      activeSpanId: spanId,
      spans: [span]
    });
    
    // 开始Sentry事务
    if (typeof Sentry !== 'undefined') {
      const transaction = Sentry.startTransaction({
        name: operationName,
        op: 'navigation',
        tags: span.tags,
        data: span.data
      });
      
      span.sentryTransaction = transaction;
    }
    
    return span;
  }
  
  // 创建子Span
  startChildSpan(parentSpanId, operationName, data = {}) {
    const parentSpan = this.activeSpans.get(parentSpanId);
    if (!parentSpan) {
      console.warn('Parent span not found:', parentSpanId);
      return null;
    }
    
    const spanId = this.generateSpanId();
    const span = {
      traceId: parentSpan.traceId,
      spanId,
      parentSpanId,
      operationName,
      startTime: Date.now(),
      endTime: null,
      duration: null,
      status: 'active',
      tags: {
        ...parentSpan.tags,
        ...data.tags
      },
      logs: [],
      data: { ...data }
    };
    
    this.activeSpans.set(spanId, span);
    
    // 更新追踪上下文
    const traceContext = this.traceContext.get(parentSpan.traceId);
    if (traceContext) {
      traceContext.spans.push(span);
      traceContext.activeSpanId = spanId;
    }
    
    // 创建Sentry子Span
    if (parentSpan.sentryTransaction) {
      const childSpan = parentSpan.sentryTransaction.startChild({
        op: data.operation || 'custom',
        description: operationName,
        tags: span.tags,
        data: span.data
      });
      
      span.sentrySpan = childSpan;
    }
    
    return span;
  }
  
  // 结束Span
  finishSpan(spanId, status = 'ok', data = {}) {
    const span = this.activeSpans.get(spanId);
    if (!span) {
      console.warn('Span not found:', spanId);
      return;
    }
    
    span.endTime = Date.now();
    span.duration = span.endTime - span.startTime;
    span.status = status;
    
    // 合并额外数据
    Object.assign(span.data, data);
    
    // 结束Sentry Span
    if (span.sentrySpan) {
      span.sentrySpan.setStatus(status);
      span.sentrySpan.setData(span.data);
      span.sentrySpan.finish();
    }
    
    // 如果是根Span，结束整个事务
    if (span.sentryTransaction && !span.parentSpanId) {
      span.sentryTransaction.setStatus(status);
      span.sentryTransaction.setData(span.data);
      span.sentryTransaction.finish();
    }
    
    // 从活跃Span中移除
    this.activeSpans.delete(spanId);
    
    // 发送追踪数据到后端
    this.sendTraceData(span);
    
    return span;
  }
  
  // 添加Span日志
  addSpanLog(spanId, level, message, data = {}) {
    const span = this.activeSpans.get(spanId);
    if (!span) {
      console.warn('Span not found:', spanId);
      return;
    }
    
    const logEntry = {
      timestamp: Date.now(),
      level,
      message,
      data
    };
    
    span.logs.push(logEntry);
    
    // 添加到Sentry Span
    if (span.sentrySpan) {
      span.sentrySpan.setData(`log_${span.logs.length}`, logEntry);
    }
  }
  
  // 设置Span标签
  setSpanTag(spanId, key, value) {
    const span = this.activeSpans.get(spanId);
    if (!span) {
      console.warn('Span not found:', spanId);
      return;
    }
    
    span.tags[key] = value;
    
    // 更新Sentry Span标签
    if (span.sentrySpan) {
      span.sentrySpan.setTag(key, value);
    }
  }
  
  // 获取当前追踪上下文
  getCurrentTraceContext() {
    const activeSpans = Array.from(this.activeSpans.values());
    if (activeSpans.length === 0) {
      return null;
    }
    
    // 返回最近创建的Span的追踪上下文
    const latestSpan = activeSpans.reduce((latest, span) => 
      span.startTime > latest.startTime ? span : latest
    );
    
    return {
      traceId: latestSpan.traceId,
      spanId: latestSpan.spanId,
      parentSpanId: latestSpan.parentSpanId
    };
  }
  
  // 拦截网络请求
  interceptNetworkRequests() {
    // 拦截fetch请求
    const originalFetch = window.fetch;
    window.fetch = async (url, options = {}) => {
      const traceContext = this.getCurrentTraceContext();
      
      if (traceContext) {
        options.headers = {
          ...options.headers,
          [this.options.traceIdHeader]: traceContext.traceId,
          [this.options.spanIdHeader]: traceContext.spanId,
          [this.options.parentSpanIdHeader]: traceContext.parentSpanId || ''
        };
      }
      
      // 创建HTTP请求Span
      const requestSpan = traceContext ? 
        this.startChildSpan(traceContext.spanId, `HTTP ${options.method || 'GET'} ${url}`, {
          operation: 'http.client',
          tags: {
            'http.method': options.method || 'GET',
            'http.url': url,
            'component': 'fetch'
          }
        }) : null;
      
      try {
        const response = await originalFetch(url, options);
        
        if (requestSpan) {
          this.setSpanTag(requestSpan.spanId, 'http.status_code', response.status);
          this.finishSpan(requestSpan.spanId, response.ok ? 'ok' : 'error', {
            responseSize: response.headers.get('content-length'),
            responseType: response.headers.get('content-type')
          });
        }
        
        return response;
      } catch (error) {
        if (requestSpan) {
          this.addSpanLog(requestSpan.spanId, 'error', error.message, { error });
          this.finishSpan(requestSpan.spanId, 'error', { error: error.message });
        }
        throw error;
      }
    };
    
    // 拦截XMLHttpRequest
    const originalXHROpen = XMLHttpRequest.prototype.open;
    const originalXHRSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
      this._traceData = {
        method,
        url,
        startTime: Date.now()
      };
      
      return originalXHROpen.call(this, method, url, async, user, password);
    };
    
    XMLHttpRequest.prototype.send = function(data) {
      const traceContext = this.getCurrentTraceContext();
      
      if (traceContext && this._traceData) {
        this.setRequestHeader(this.options.traceIdHeader, traceContext.traceId);
        this.setRequestHeader(this.options.spanIdHeader, traceContext.spanId);
        if (traceContext.parentSpanId) {
          this.setRequestHeader(this.options.parentSpanIdHeader, traceContext.parentSpanId);
        }
        
        // 创建HTTP请求Span
        const requestSpan = this.startChildSpan(traceContext.spanId, 
          `HTTP ${this._traceData.method} ${this._traceData.url}`, {
          operation: 'http.client',
          tags: {
            'http.method': this._traceData.method,
            'http.url': this._traceData.url,
            'component': 'xhr'
          }
        });
        
        this._requestSpan = requestSpan;
        
        // 监听请求完成
        this.addEventListener('loadend', () => {
          if (requestSpan) {
            this.setSpanTag(requestSpan.spanId, 'http.status_code', this.status);
            this.finishSpan(requestSpan.spanId, 
              this.status >= 200 && this.status < 400 ? 'ok' : 'error', {
              responseSize: this.getResponseHeader('content-length'),
              responseType: this.getResponseHeader('content-type')
            });
          }
        });
        
        this.addEventListener('error', () => {
          if (requestSpan) {
            this.addSpanLog(requestSpan.spanId, 'error', 'Network error');
            this.finishSpan(requestSpan.spanId, 'error', { error: 'Network error' });
          }
        });
      }
      
      return originalXHRSend.call(this, data);
    }.bind(this);
  }
  
  // 设置页面导航追踪
  setupNavigationTracking() {
    // 监听页面加载
    window.addEventListener('load', () => {
      const navigationSpan = this.startTrace('page_load', {
        operation: 'navigation',
        tags: {
          'navigation.type': 'load',
          'page.url': window.location.href,
          'page.title': document.title
        }
      });
      
      // 收集导航时序信息
      if (window.performance && window.performance.timing) {
        const timing = window.performance.timing;
        const navigationData = {
          domainLookup: timing.domainLookupEnd - timing.domainLookupStart,
          connect: timing.connectEnd - timing.connectStart,
          request: timing.responseStart - timing.requestStart,
          response: timing.responseEnd - timing.responseStart,
          domProcessing: timing.domComplete - timing.domLoading,
          loadComplete: timing.loadEventEnd - timing.loadEventStart
        };
        
        setTimeout(() => {
          this.finishSpan(navigationSpan.spanId, 'ok', navigationData);
        }, 100);
      }
    });
    
    // 监听路由变化（SPA）
    if (window.history) {
      const originalPushState = window.history.pushState;
      const originalReplaceState = window.history.replaceState;
      
      window.history.pushState = function(...args) {
        const navigationSpan = this.startTrace('route_change', {
          operation: 'navigation',
          tags: {
            'navigation.type': 'pushState',
            'page.url': args[2] || window.location.href
          }
        });
        
        setTimeout(() => {
          this.finishSpan(navigationSpan.spanId, 'ok');
        }, 100);
        
        return originalPushState.apply(this, args);
      }.bind(this);
      
      window.history.replaceState = function(...args) {
        const navigationSpan = this.startTrace('route_change', {
          operation: 'navigation',
          tags: {
            'navigation.type': 'replaceState',
            'page.url': args[2] || window.location.href
          }
        });
        
        setTimeout(() => {
          this.finishSpan(navigationSpan.spanId, 'ok');
        }, 100);
        
        return originalReplaceState.apply(this, args);
      }.bind(this);
    }
  }
  
  // 发送追踪数据到后端
  async sendTraceData(span) {
    try {
      const traceData = {
        traceId: span.traceId,
        spanId: span.spanId,
        parentSpanId: span.parentSpanId,
        operationName: span.operationName,
        startTime: span.startTime,
        endTime: span.endTime,
        duration: span.duration,
        status: span.status,
        tags: span.tags,
        logs: span.logs,
        data: span.data,
        service: this.options.serviceName,
        environment: this.options.environment
      };
      
      // 发送到追踪收集服务
      await fetch('/api/traces', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(traceData)
      });
    } catch (error) {
      console.error('Failed to send trace data:', error);
    }
  }
  
  // 丰富Sentry事务数据
  enrichTransactionWithTraceData(event) {
    const traceContext = this.getCurrentTraceContext();
    if (traceContext) {
      event.tags = {
        ...event.tags,
        traceId: traceContext.traceId,
        spanId: traceContext.spanId
      };
      
      event.extra = {
        ...event.extra,
        traceContext
      };
    }
    
    return event;
  }
  
  // 生成追踪ID
  generateTraceId() {
    return Array.from({ length: 32 }, () => 
      Math.floor(Math.random() * 16).toString(16)
    ).join('');
  }
  
  // 生成Span ID
  generateSpanId() {
    return Array.from({ length: 16 }, () => 
      Math.floor(Math.random() * 16).toString(16)
    ).join('');
  }
  
  // 创建Span ID生成器
  createSpanIdGenerator() {
    let counter = 0;
    return () => {
      counter = (counter + 1) % 0xFFFFFFFF;
      return counter.toString(16).padStart(8, '0') + 
             Math.floor(Math.random() * 0xFFFFFFFF).toString(16).padStart(8, '0');
    };
  }
  
  // 获取所有活跃的Span
  getActiveSpans() {
    return Array.from(this.activeSpans.values());
  }
  
  // 获取追踪统计信息
  getTracingStats() {
    const activeSpans = this.getActiveSpans();
    const traceContexts = Array.from(this.traceContext.values());
    
    return {
      activeSpans: activeSpans.length,
      activeTraces: traceContexts.length,
      avgSpanDuration: activeSpans.length > 0 ? 
        activeSpans.reduce((sum, span) => sum + (Date.now() - span.startTime), 0) / activeSpans.length : 0,
      oldestSpan: activeSpans.length > 0 ? 
        Math.min(...activeSpans.map(span => span.startTime)) : null
    };
  }
  
  // 清理过期的Span
  cleanupExpiredSpans() {
    const now = Date.now();
    const expiredSpans = [];
    
    for (const [spanId, span] of this.activeSpans) {
      if (now - span.startTime > this.options.maxSpanDuration) {
        expiredSpans.push(spanId);
      }
    }
    
    expiredSpans.forEach(spanId => {
      const span = this.activeSpans.get(spanId);
      if (span) {
        this.addSpanLog(spanId, 'warn', 'Span expired due to timeout');
        this.finishSpan(spanId, 'timeout', { reason: 'expired' });
      }
    });
  }
}
```

### 2.2 服务依赖映射器

```javascript
// 服务依赖映射器
class ServiceDependencyMapper {
  constructor(options = {}) {
    this.options = {
      services: options.services || [],
      autoDiscovery: options.autoDiscovery !== false,
      discoveryInterval: options.discoveryInterval || 300000, // 5分钟
      dependencyTimeout: options.dependencyTimeout || 86400000, // 24小时
      ...options
    };
    
    this.dependencyGraph = new Map();
    this.callFlow = new Map();
    this.serviceMetrics = new Map();
    
    this.initializeDependencyMapping();
  }
  
  initializeDependencyMapping() {
    // 初始化已知服务
    this.options.services.forEach(service => {
      this.registerService(service);
    });
    
    // 启动自动发现
    if (this.options.autoDiscovery) {
      this.startAutoDiscovery();
    }
    
    // 拦截网络请求以发现依赖关系
    this.interceptNetworkCalls();
  }
  
  // 注册服务
  registerService(service) {
    const serviceInfo = {
      name: service.name,
      type: service.type || 'api',
      version: service.version || '1.0.0',
      endpoints: service.endpoints || [],
      dependencies: new Set(service.dependencies || []),
      dependents: new Set(),
      metrics: {
        totalCalls: 0,
        avgResponseTime: 0,
        errorRate: 0,
        lastSeen: Date.now()
      },
      health: {
        status: 'unknown',
        lastCheck: null,
        uptime: 0
      },
      tags: service.tags || {}
    };
    
    this.dependencyGraph.set(service.name, serviceInfo);
    
    // 建立依赖关系
    service.dependencies?.forEach(depName => {
      this.addDependency(service.name, depName);
    });
  }
  
  // 添加依赖关系
  addDependency(serviceName, dependencyName) {
    const service = this.dependencyGraph.get(serviceName);
    const dependency = this.dependencyGraph.get(dependencyName);
    
    if (service) {
      service.dependencies.add(dependencyName);
    }
    
    if (dependency) {
      dependency.dependents.add(serviceName);
    }
    
    // 记录调用流
    const flowKey = `${serviceName}->${dependencyName}`;
    if (!this.callFlow.has(flowKey)) {
      this.callFlow.set(flowKey, {
        from: serviceName,
        to: dependencyName,
        callCount: 0,
        avgLatency: 0,
        errorCount: 0,
        lastCall: null,
        protocols: new Set()
      });
    }
  }
  
  // 记录服务调用
  recordServiceCall(fromService, toService, callData) {
    const flowKey = `${fromService}->${toService}`;
    let flow = this.callFlow.get(flowKey);
    
    if (!flow) {
      // 自动发现新的依赖关系
      this.addDependency(fromService, toService);
      flow = this.callFlow.get(flowKey);
    }
    
    if (flow) {
      flow.callCount++;
      flow.lastCall = Date.now();
      
      // 更新平均延迟
      if (callData.duration) {
        flow.avgLatency = (
          (flow.avgLatency * (flow.callCount - 1)) + callData.duration
        ) / flow.callCount;
      }
      
      // 记录错误
      if (!callData.success) {
        flow.errorCount++;
      }
      
      // 记录协议
      if (callData.protocol) {
        flow.protocols.add(callData.protocol);
      }
    }
    
    // 更新服务指标
    this.updateServiceMetrics(toService, callData);
  }
  
  // 更新服务指标
  updateServiceMetrics(serviceName, callData) {
    const service = this.dependencyGraph.get(serviceName);
    if (!service) return;
    
    service.metrics.totalCalls++;
    service.metrics.lastSeen = Date.now();
    
    if (callData.duration) {
      service.metrics.avgResponseTime = (
        (service.metrics.avgResponseTime * (service.metrics.totalCalls - 1)) + callData.duration
      ) / service.metrics.totalCalls;
    }
    
    if (!callData.success) {
      service.metrics.errorRate = (
        (service.metrics.errorRate * (service.metrics.totalCalls - 1)) + 1
      ) / service.metrics.totalCalls;
    } else {
      service.metrics.errorRate = (
        service.metrics.errorRate * (service.metrics.totalCalls - 1)
      ) / service.metrics.totalCalls;
    }
  }
  
  // 拦截网络调用
  interceptNetworkCalls() {
    const originalFetch = window.fetch;
    
    window.fetch = async (url, options = {}) => {
      const startTime = Date.now();
      const fromService = 'frontend'; // 前端服务名
      const toService = this.extractServiceFromUrl(url);
      
      try {
        const response = await originalFetch(url, options);
        const endTime = Date.now();
        
        if (toService) {
          this.recordServiceCall(fromService, toService, {
            duration: endTime - startTime,
            success: response.ok,
            protocol: 'http',
            method: options.method || 'GET',
            url: url,
            statusCode: response.status
          });
        }
        
        return response;
      } catch (error) {
        const endTime = Date.now();
        
        if (toService) {
          this.recordServiceCall(fromService, toService, {
            duration: endTime - startTime,
            success: false,
            protocol: 'http',
            method: options.method || 'GET',
            url: url,
            error: error.message
          });
        }
        
        throw error;
      }
    };
  }
  
  // 从URL提取服务名
  extractServiceFromUrl(url) {
    try {
      const urlObj = new URL(url);
      const hostname = urlObj.hostname;
      
      // 尝试从已知服务中匹配
      for (const [serviceName, service] of this.dependencyGraph) {
        if (service.endpoints.some(endpoint => 
          typeof endpoint === 'string' ? url.includes(endpoint) : endpoint.test(url)
        )) {
          return serviceName;
        }
      }
      
      // 基于主机名推断服务名
      const parts = hostname.split('.');
      if (parts.length > 0) {
        const serviceName = parts[0].replace(/[^a-zA-Z0-9-]/g, '');
        
        // 自动注册新发现的服务
        if (!this.dependencyGraph.has(serviceName)) {
          this.registerService({
            name: serviceName,
            type: 'api',
            endpoints: [hostname]
          });
        }
        
        return serviceName;
      }
    } catch (error) {
      console.warn('Failed to extract service from URL:', url, error);
    }
    
    return null;
  }
  
  // 启动自动发现
  startAutoDiscovery() {
    setInterval(() => {
      this.performServiceDiscovery();
    }, this.options.discoveryInterval);
  }
  
  // 执行服务发现
  async performServiceDiscovery() {
    // 清理过期的依赖关系
    this.cleanupExpiredDependencies();
    
    // 发现新的服务
    await this.discoverNewServices();
    
    // 更新服务健康状态
    await this.updateServiceHealth();
  }
  
  // 发现新服务
  async discoverNewServices() {
    // 这里可以集成服务注册中心（如Consul、Eureka等）
    // 或者通过其他方式发现新服务
    
    try {
      // 示例：从服务注册中心获取服务列表
      const response = await fetch('/api/services/discovery');
      if (response.ok) {
        const services = await response.json();
        
        services.forEach(service => {
          if (!this.dependencyGraph.has(service.name)) {
            this.registerService(service);
          }
        });
      }
    } catch (error) {
      console.warn('Service discovery failed:', error);
    }
  }
  
  // 更新服务健康状态
  async updateServiceHealth() {
    const healthChecks = [];
    
    for (const [serviceName, service] of this.dependencyGraph) {
      if (service.endpoints.length > 0) {
        healthChecks.push(this.checkServiceHealth(serviceName, service));
      }
    }
    
    await Promise.allSettled(healthChecks);
  }
  
  // 检查服务健康状态
  async checkServiceHealth(serviceName, service) {
    const startTime = Date.now();
    
    try {
      // 尝试访问服务的健康检查端点
      const healthEndpoint = service.endpoints.find(ep => 
        typeof ep === 'string' && ep.includes('/health')
      ) || service.endpoints[0];
      
      if (!healthEndpoint) return;
      
      const response = await fetch(healthEndpoint, {
        method: 'GET',
        timeout: 5000
      });
      
      const endTime = Date.now();
      
      service.health = {
        status: response.ok ? 'healthy' : 'unhealthy',
        lastCheck: startTime,
        responseTime: endTime - startTime,
        uptime: response.ok ? service.health.uptime + 1 : 0
      };
      
    } catch (error) {
      const endTime = Date.now();
      
      service.health = {
        status: 'unhealthy',
        lastCheck: startTime,
        responseTime: endTime - startTime,
        uptime: 0,
        error: error.message
      };
    }
  }
  
  // 清理过期依赖
  cleanupExpiredDependencies() {
    const now = Date.now();
    const expiredFlows = [];
    
    for (const [flowKey, flow] of this.callFlow) {
      if (flow.lastCall && now - flow.lastCall > this.options.dependencyTimeout) {
        expiredFlows.push(flowKey);
      }
    }
    
    expiredFlows.forEach(flowKey => {
      this.callFlow.delete(flowKey);
    });
  }
  
  // 获取服务依赖图
  getDependencyGraph() {
    const nodes = [];
    const edges = [];
    
    // 构建节点
    for (const [serviceName, service] of this.dependencyGraph) {
      nodes.push({
        id: serviceName,
        name: serviceName,
        type: service.type,
        version: service.version,
        health: service.health.status,
        metrics: service.metrics,
        tags: service.tags
      });
    }
    
    // 构建边
    for (const [flowKey, flow] of this.callFlow) {
      edges.push({
        id: flowKey,
        from: flow.from,
        to: flow.to,
        callCount: flow.callCount,
        avgLatency: flow.avgLatency,
        errorRate: flow.errorCount / flow.callCount,
        protocols: Array.from(flow.protocols),
        lastCall: flow.lastCall
      });
    }
    
    return { nodes, edges };
  }
  
  // 分析关键路径
  analyzeCriticalPath(targetService) {
    const paths = [];
    const visited = new Set();
    
    const findPaths = (currentService, path, depth) => {
      if (depth > 10 || visited.has(currentService)) return; // 防止无限循环
      
      visited.add(currentService);
      path.push(currentService);
      
      if (currentService === targetService) {
        paths.push([...path]);
      } else {
        const service = this.dependencyGraph.get(currentService);
        if (service) {
          service.dependencies.forEach(dep => {
            findPaths(dep, path, depth + 1);
          });
        }
      }
      
      path.pop();
      visited.delete(currentService);
    };
    
    // 从前端开始查找路径
    findPaths('frontend', [], 0);
    
    // 计算路径权重（基于延迟和错误率）
    return paths.map(path => {
      let totalLatency = 0;
      let totalErrorRate = 0;
      let pathWeight = 0;
      
      for (let i = 0; i < path.length - 1; i++) {
        const flowKey = `${path[i]}->${path[i + 1]}`;
        const flow = this.callFlow.get(flowKey);
        
        if (flow) {
          totalLatency += flow.avgLatency;
          totalErrorRate += flow.errorCount / flow.callCount;
          pathWeight += flow.callCount;
        }
      }
      
      return {
        path,
        totalLatency,
        avgErrorRate: totalErrorRate / (path.length - 1),
        weight: pathWeight,
        criticality: this.calculatePathCriticality(totalLatency, totalErrorRate, pathWeight)
      };
    }).sort((a, b) => b.criticality - a.criticality);
  }
  
  // 计算路径关键性
  calculatePathCriticality(latency, errorRate, weight) {
    // 基于延迟、错误率和调用频率计算关键性分数
    const latencyScore = Math.min(latency / 1000, 10); // 延迟分数（秒）
    const errorScore = errorRate * 100; // 错误率分数
    const weightScore = Math.log(weight + 1); // 调用频率分数
    
    return (latencyScore * 0.4) + (errorScore * 0.4) + (weightScore * 0.2);
  }
  
  // 检测服务瓶颈
  detectBottlenecks() {
    const bottlenecks = [];
    
    for (const [serviceName, service] of this.dependencyGraph) {
      const bottleneckScore = this.calculateBottleneckScore(service);
      
      if (bottleneckScore > 0.7) {
        bottlenecks.push({
          service: serviceName,
          score: bottleneckScore,
          issues: this.identifyServiceIssues(service),
          recommendations: this.generateBottleneckRecommendations(service)
        });
      }
    }
    
    return bottlenecks.sort((a, b) => b.score - a.score);
  }
  
  // 计算瓶颈分数
  calculateBottleneckScore(service) {
    let score = 0;
    
    // 基于响应时间
    if (service.metrics.avgResponseTime > 2000) {
      score += 0.3;
    } else if (service.metrics.avgResponseTime > 1000) {
      score += 0.2;
    }
    
    // 基于错误率
    if (service.metrics.errorRate > 0.1) {
      score += 0.4;
    } else if (service.metrics.errorRate > 0.05) {
      score += 0.2;
    }
    
    // 基于健康状态
    if (service.health.status === 'unhealthy') {
      score += 0.3;
    }
    
    // 基于依赖数量（高依赖可能是瓶颈）
    if (service.dependents.size > 5) {
      score += 0.2;
    }
    
    return Math.min(score, 1.0);
  }
  
  // 识别服务问题
  identifyServiceIssues(service) {
    const issues = [];
    
    if (service.metrics.avgResponseTime > 2000) {
      issues.push('High response time');
    }
    
    if (service.metrics.errorRate > 0.1) {
      issues.push('High error rate');
    }
    
    if (service.health.status === 'unhealthy') {
      issues.push('Service unhealthy');
    }
    
    if (service.dependents.size > 5) {
      issues.push('High dependency load');
    }
    
    return issues;
  }
  
  // 生成瓶颈建议
  generateBottleneckRecommendations(service) {
    const recommendations = [];
    
    if (service.metrics.avgResponseTime > 2000) {
      recommendations.push('Optimize service performance or add caching');
    }
    
    if (service.metrics.errorRate > 0.1) {
      recommendations.push('Investigate and fix error sources');
    }
    
    if (service.dependents.size > 5) {
      recommendations.push('Consider load balancing or service scaling');
    }
    
    if (service.health.status === 'unhealthy') {
      recommendations.push('Check service health and restart if necessary');
    }
    
    return recommendations;
  }
  
  // 生成依赖报告
  generateDependencyReport() {
    const graph = this.getDependencyGraph();
    const bottlenecks = this.detectBottlenecks();
    
    return {
      timestamp: new Date().toISOString(),
      summary: {
        totalServices: graph.nodes.length,
        totalDependencies: graph.edges.length,
        healthyServices: graph.nodes.filter(node => node.health === 'healthy').length,
        criticalBottlenecks: bottlenecks.filter(b => b.score > 0.8).length
      },
      services: graph.nodes,
      dependencies: graph.edges,
      bottlenecks,
      recommendations: this.generateSystemRecommendations(graph, bottlenecks)
    };
  }
  
  // 生成系统建议
  generateSystemRecommendations(graph, bottlenecks) {
    const recommendations = [];
    
    if (bottlenecks.length > 0) {
      recommendations.push('Address identified service bottlenecks to improve overall system performance');
    }
    
    const unhealthyServices = graph.nodes.filter(node => node.health === 'unhealthy');
    if (unhealthyServices.length > 0) {
      recommendations.push(`${unhealthyServices.length} services are unhealthy and need attention`);
    }
    
    const highLatencyEdges = graph.edges.filter(edge => edge.avgLatency > 1000);
    if (highLatencyEdges.length > 0) {
      recommendations.push('Optimize high-latency service communications');
    }
    
    return recommendations;
  }
}
```

## 3. 微服务性能分析

### 3.1 性能分析器

```javascript
// 微服务性能分析器
class MicroservicePerformanceAnalyzer {
  constructor(options = {}) {
    this.options = {
      analysisInterval: options.analysisInterval || 60000, // 1分钟
      retentionPeriod: options.retentionPeriod || 86400000, // 24小时
      performanceThresholds: {
        responseTime: options.performanceThresholds?.responseTime || 1000,
        errorRate: options.performanceThresholds?.errorRate || 0.05,
        throughput: options.performanceThresholds?.throughput || 100,
        ...options.performanceThresholds
      },
      ...options
    };
    
    this.performanceData = new Map();
    this.analysisResults = new Map();
    this.alerts = [];
    
    this.initializeAnalyzer();
  }
  
  initializeAnalyzer() {
    // 启动性能数据收集
    this.startPerformanceCollection();
    
    // 启动定期分析
    this.startPeriodicAnalysis();
    
    // 集成Sentry性能监控
    this.integrateSentryPerformance();
  }
  
  // 启动性能数据收集
  startPerformanceCollection() {
    // 收集Web Vitals
    this.collectWebVitals();
    
    // 收集资源性能
    this.collectResourcePerformance();
    
    // 收集用户交互性能
    this.collectUserInteractionPerformance();
  }
  
  // 收集Web Vitals
  collectWebVitals() {
    if (typeof window !== 'undefined' && 'web-vitals' in window) {
      // 使用web-vitals库收集核心指标
      import('web-vitals').then(({ getCLS, getFID, getFCP, getLCP, getTTFB }) => {
        getCLS(this.recordVital.bind(this, 'CLS'));
        getFID(this.recordVital.bind(this, 'FID'));
        getFCP(this.recordVital.bind(this, 'FCP'));
        getLCP(this.recordVital.bind(this, 'LCP'));
        getTTFB(this.recordVital.bind(this, 'TTFB'));
      });
    } else {
      // 手动收集性能指标
      this.collectManualWebVitals();
    }
  }
  
  // 手动收集Web Vitals
  collectManualWebVitals() {
    if (typeof window === 'undefined') return;
    
    // 收集FCP (First Contentful Paint)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        if (entry.name === 'first-contentful-paint') {
          this.recordVital('FCP', { value: entry.startTime });
        }
      });
    }).observe({ entryTypes: ['paint'] });
    
    // 收集LCP (Largest Contentful Paint)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      this.recordVital('LCP', { value: lastEntry.startTime });
    }).observe({ entryTypes: ['largest-contentful-paint'] });
    
    // 收集FID (First Input Delay)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        this.recordVital('FID', { value: entry.processingStart - entry.startTime });
      });
    }).observe({ entryTypes: ['first-input'] });
    
    // 收集CLS (Cumulative Layout Shift)
    let clsValue = 0;
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        if (!entry.hadRecentInput) {
          clsValue += entry.value;
        }
      });
      this.recordVital('CLS', { value: clsValue });
    }).observe({ entryTypes: ['layout-shift'] });
  }
  
  // 记录Web Vital指标
  recordVital(name, metric) {
    const timestamp = Date.now();
    const serviceKey = 'frontend';
    
    if (!this.performanceData.has(serviceKey)) {
      this.performanceData.set(serviceKey, {
        webVitals: [],
        resources: [],
        interactions: [],
        customMetrics: []
      });
    }
    
    const serviceData = this.performanceData.get(serviceKey);
    serviceData.webVitals.push({
      name,
      value: metric.value,
      timestamp,
      id: metric.id || this.generateMetricId(),
      rating: this.rateWebVital(name, metric.value)
    });
    
    // 保持数据量在合理范围内
    if (serviceData.webVitals.length > 1000) {
      serviceData.webVitals.splice(0, serviceData.webVitals.length - 1000);
    }
    
    // 发送到Sentry
    this.sendVitalToSentry(name, metric);
  }
  
  // 评估Web Vital指标
  rateWebVital(name, value) {
    const thresholds = {
      FCP: { good: 1800, poor: 3000 },
      LCP: { good: 2500, poor: 4000 },
      FID: { good: 100, poor: 300 },
      CLS: { good: 0.1, poor: 0.25 },
      TTFB: { good: 800, poor: 1800 }
    };
    
    const threshold = thresholds[name];
    if (!threshold) return 'unknown';
    
    if (value <= threshold.good) return 'good';
    if (value <= threshold.poor) return 'needs-improvement';
    return 'poor';
  }
  
  // 收集资源性能
  collectResourcePerformance() {
    if (typeof window === 'undefined') return;
    
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        this.recordResourcePerformance(entry);
      });
    }).observe({ entryTypes: ['resource'] });
  }
  
  // 记录资源性能
  recordResourcePerformance(entry) {
    const serviceKey = this.extractServiceFromUrl(entry.name) || 'frontend';
    
    if (!this.performanceData.has(serviceKey)) {
      this.performanceData.set(serviceKey, {
        webVitals: [],
        resources: [],
        interactions: [],
        customMetrics: []
      });
    }
    
    const serviceData = this.performanceData.get(serviceKey);
    serviceData.resources.push({
      name: entry.name,
      type: entry.initiatorType,
      duration: entry.duration,
      size: entry.transferSize || entry.encodedBodySize,
      timestamp: Date.now(),
      timing: {
        dns: entry.domainLookupEnd - entry.domainLookupStart,
        connect: entry.connectEnd - entry.connectStart,
        request: entry.responseStart - entry.requestStart,
        response: entry.responseEnd - entry.responseStart
      }
    });
    
    // 保持数据量在合理范围内
    if (serviceData.resources.length > 500) {
      serviceData.resources.splice(0, serviceData.resources.length - 500);
    }
  }
  
  // 收集用户交互性能
  collectUserInteractionPerformance() {
    if (typeof window === 'undefined') return;
    
    // 监听点击事件
    document.addEventListener('click', (event) => {
      this.recordInteraction('click', event);
    });
    
    // 监听输入事件
    document.addEventListener('input', (event) => {
      this.recordInteraction('input', event);
    });
    
    // 监听滚动事件（节流）
    let scrollTimeout;
    document.addEventListener('scroll', (event) => {
      clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        this.recordInteraction('scroll', event);
      }, 100);
    });
  }
  
  // 记录用户交互
  recordInteraction(type, event) {
    const timestamp = Date.now();
    const serviceKey = 'frontend';
    
    if (!this.performanceData.has(serviceKey)) {
      this.performanceData.set(serviceKey, {
        webVitals: [],
        resources: [],
        interactions: [],
        customMetrics: []
      });
    }
    
    const serviceData = this.performanceData.get(serviceKey);
    serviceData.interactions.push({
      type,
      timestamp,
      target: event.target?.tagName || 'unknown',
      x: event.clientX || 0,
      y: event.clientY || 0
    });
    
    // 保持数据量在合理范围内
    if (serviceData.interactions.length > 200) {
      serviceData.interactions.splice(0, serviceData.interactions.length - 200);
    }
  }
  
  // 启动定期分析
  startPeriodicAnalysis() {
    setInterval(() => {
      this.performAnalysis();
    }, this.options.analysisInterval);
    
    // 立即执行一次分析
    setTimeout(() => this.performAnalysis(), 5000);
  }
  
  // 执行性能分析
  performAnalysis() {
    const analysisTimestamp = Date.now();
    
    for (const [serviceKey, data] of this.performanceData) {
      const analysis = this.analyzeServicePerformance(serviceKey, data);
      this.analysisResults.set(serviceKey, {
        ...analysis,
        timestamp: analysisTimestamp
      });
      
      // 检查性能告警
      this.checkPerformanceAlerts(serviceKey, analysis);
    }
    
    // 清理过期数据
    this.cleanupExpiredData();
  }
  
  // 分析服务性能
  analyzeServicePerformance(serviceKey, data) {
    const now = Date.now();
    const oneHourAgo = now - 3600000;
    
    // 分析Web Vitals
    const recentVitals = data.webVitals.filter(v => v.timestamp > oneHourAgo);
    const vitalsAnalysis = this.analyzeWebVitals(recentVitals);
    
    // 分析资源性能
    const recentResources = data.resources.filter(r => r.timestamp > oneHourAgo);
    const resourcesAnalysis = this.analyzeResources(recentResources);
    
    // 分析用户交互
    const recentInteractions = data.interactions.filter(i => i.timestamp > oneHourAgo);
    const interactionsAnalysis = this.analyzeInteractions(recentInteractions);
    
    // 计算综合性能分数
    const performanceScore = this.calculatePerformanceScore({
      vitals: vitalsAnalysis,
      resources: resourcesAnalysis,
      interactions: interactionsAnalysis
    });
    
    return {
      service: serviceKey,
      performanceScore,
      vitals: vitalsAnalysis,
      resources: resourcesAnalysis,
      interactions: interactionsAnalysis,
      recommendations: this.generatePerformanceRecommendations(serviceKey, {
        vitals: vitalsAnalysis,
        resources: resourcesAnalysis,
        interactions: interactionsAnalysis
      })
    };
  }
  
  // 分析Web Vitals
  analyzeWebVitals(vitals) {
    const vitalsByName = {};
    
    vitals.forEach(vital => {
      if (!vitalsByName[vital.name]) {
        vitalsByName[vital.name] = [];
      }
      vitalsByName[vital.name].push(vital.value);
    });
    
    const analysis = {};
    
    Object.keys(vitalsByName).forEach(name => {
      const values = vitalsByName[name];
      if (values.length > 0) {
        analysis[name] = {
          count: values.length,
          avg: values.reduce((sum, v) => sum + v, 0) / values.length,
          min: Math.min(...values),
          max: Math.max(...values),
          p75: this.percentile(values, 0.75),
          p95: this.percentile(values, 0.95),
          rating: this.rateWebVital(name, values.reduce((sum, v) => sum + v, 0) / values.length)
        };
      }
    });
    
    return analysis;
  }
  
  // 分析资源性能
  analyzeResources(resources) {
    if (resources.length === 0) {
      return {
        totalRequests: 0,
        avgDuration: 0,
        totalSize: 0,
        byType: {}
      };
    }
    
    const totalDuration = resources.reduce((sum, r) => sum + r.duration, 0);
    const totalSize = resources.reduce((sum, r) => sum + (r.size || 0), 0);
    
    const byType = {};
    resources.forEach(resource => {
      if (!byType[resource.type]) {
        byType[resource.type] = {
          count: 0,
          totalDuration: 0,
          totalSize: 0
        };
      }
      
      byType[resource.type].count++;
      byType[resource.type].totalDuration += resource.duration;
      byType[resource.type].totalSize += resource.size || 0;
    });
    
    // 计算每种类型的平均值
    Object.keys(byType).forEach(type => {
      const typeData = byType[type];
      typeData.avgDuration = typeData.totalDuration / typeData.count;
      typeData.avgSize = typeData.totalSize / typeData.count;
    });
    
    return {
      totalRequests: resources.length,
      avgDuration: totalDuration / resources.length,
      totalSize,
      avgSize: totalSize / resources.length,
      byType
    };
  }
  
  // 分析用户交互
  analyzeInteractions(interactions) {
    if (interactions.length === 0) {
      return {
        totalInteractions: 0,
        byType: {},
        interactionRate: 0
      };
    }
    
    const byType = {};
    interactions.forEach(interaction => {
      if (!byType[interaction.type]) {
        byType[interaction.type] = 0;
      }
      byType[interaction.type]++;
    });
    
    // 计算交互率（每分钟交互次数）
    const timeSpan = Math.max(
      interactions[interactions.length - 1].timestamp - interactions[0].timestamp,
      60000 // 至少1分钟
    );
    const interactionRate = (interactions.length / timeSpan) * 60000;
    
    return {
      totalInteractions: interactions.length,
      byType,
      interactionRate
    };
  }
  
  // 计算性能分数
  calculatePerformanceScore(analysis) {
    let score = 100;
    
    // Web Vitals影响（权重50%）
    if (analysis.vitals.LCP) {
      if (analysis.vitals.LCP.rating === 'poor') score -= 20;
      else if (analysis.vitals.LCP.rating === 'needs-improvement') score -= 10;
    }
    
    if (analysis.vitals.FID) {
      if (analysis.vitals.FID.rating === 'poor') score -= 15;
      else if (analysis.vitals.FID.rating === 'needs-improvement') score -= 7;
    }
    
    if (analysis.vitals.CLS) {
      if (analysis.vitals.CLS.rating === 'poor') score -= 15;
      else if (analysis.vitals.CLS.rating === 'needs-improvement') score -= 7;
    }
    
    // 资源性能影响（权重30%）
    if (analysis.resources.avgDuration > 2000) {
      score -= 15;
    } else if (analysis.resources.avgDuration > 1000) {
      score -= 8;
    }
    
    // 交互性能影响（权重20%）
    if (analysis.interactions.interactionRate < 1) {
      score -= 10; // 交互率过低可能表示用户体验问题
    }
    
    return Math.max(0, Math.round(score));
  }
  
  // 生成性能建议
  generatePerformanceRecommendations(serviceKey, analysis) {
    const recommendations = [];
    
    // Web Vitals建议
    if (analysis.vitals.LCP?.rating === 'poor') {
      recommendations.push('Optimize Largest Contentful Paint by reducing server response times and optimizing critical resources');
    }
    
    if (analysis.vitals.FID?.rating === 'poor') {
      recommendations.push('Improve First Input Delay by reducing JavaScript execution time and optimizing event handlers');
    }
    
    if (analysis.vitals.CLS?.rating === 'poor') {
      recommendations.push('Reduce Cumulative Layout Shift by setting dimensions for images and avoiding dynamic content insertion');
    }
    
    // 资源性能建议
    if (analysis.resources.avgDuration > 2000) {
      recommendations.push('Optimize resource loading by implementing caching, compression, and CDN usage');
    }
    
    if (analysis.resources.totalSize > 5000000) { // 5MB
      recommendations.push('Reduce total resource size by optimizing images, minifying assets, and implementing code splitting');
    }
    
    // 交互性能建议
    if (analysis.interactions.interactionRate < 1) {
      recommendations.push('Improve user engagement by optimizing UI responsiveness and reducing interaction barriers');
    }
    
    return recommendations;
  }
  
  // 检查性能告警
  checkPerformanceAlerts(serviceKey, analysis) {
    const alerts = [];
    
    // 检查性能分数
    if (analysis.performanceScore < 50) {
      alerts.push({
        type: 'critical',
        service: serviceKey,
        message: `Performance score is critically low: ${analysis.performanceScore}`,
        timestamp: Date.now()
      });
    } else if (analysis.performanceScore < 70) {
      alerts.push({
        type: 'warning',
        service: serviceKey,
        message: `Performance score is below threshold: ${analysis.performanceScore}`,
        timestamp: Date.now()
      });
    }
    
    // 检查Web Vitals
    Object.keys(analysis.vitals).forEach(vitalName => {
      const vital = analysis.vitals[vitalName];
      if (vital.rating === 'poor') {
        alerts.push({
          type: 'warning',
          service: serviceKey,
          message: `${vitalName} is performing poorly: ${vital.avg.toFixed(2)}`,
          timestamp: Date.now()
        });
      }
    });
    
    // 发送告警
    alerts.forEach(alert => {
      this.sendPerformanceAlert(alert);
    });
    
    this.alerts.push(...alerts);
    
    // 保持告警历史在合理范围内
    if (this.alerts.length > 100) {
      this.alerts.splice(0, this.alerts.length - 100);
    }
  }
  
  // 发送性能告警
  sendPerformanceAlert(alert) {
    if (typeof Sentry !== 'undefined') {
      Sentry.withScope(scope => {
        scope.setTag('alert_type', alert.type);
        scope.setTag('service', alert.service);
        scope.setLevel(alert.type === 'critical' ? 'error' : 'warning');
        
        Sentry.captureMessage(`Performance Alert: ${alert.message}`, alert.type === 'critical' ? 'error' : 'warning');
      });
    }
  }
  
  // 集成Sentry性能监控
  integrateSentryPerformance() {
    if (typeof Sentry !== 'undefined') {
      // 添加性能数据到Sentry事务
      Sentry.addGlobalEventProcessor((event) => {
        if (event.type === 'transaction') {
          const serviceKey = 'frontend';
          const analysis = this.analysisResults.get(serviceKey);
          
          if (analysis) {
            event.extra = {
              ...event.extra,
              performanceAnalysis: {
                score: analysis.performanceScore,
                vitals: analysis.vitals,
                timestamp: analysis.timestamp
              }
            };
          }
        }
        
        return event;
      });
    }
  }
  
  // 发送Web Vital到Sentry
  sendVitalToSentry(name, metric) {
    if (typeof Sentry !== 'undefined') {
      Sentry.addBreadcrumb({
        category: 'web-vital',
        message: `${name}: ${metric.value}`,
        level: 'info',
        data: {
          name,
          value: metric.value,
          rating: this.rateWebVital(name, metric.value)
        }
      });
    }
  }
  
  // 清理过期数据
  cleanupExpiredData() {
    const cutoffTime = Date.now() - this.options.retentionPeriod;
    
    for (const [serviceKey, data] of this.performanceData) {
      data.webVitals = data.webVitals.filter(v => v.timestamp > cutoffTime);
      data.resources = data.resources.filter(r => r.timestamp > cutoffTime);
      data.interactions = data.interactions.filter(i => i.timestamp > cutoffTime);
    }
    
    // 清理过期告警
    this.alerts = this.alerts.filter(alert => alert.timestamp > cutoffTime);
  }
  
  // 工具方法
  percentile(values, p) {
    const sorted = values.slice().sort((a, b) => a - b);
    const index = Math.ceil(sorted.length * p) - 1;
    return sorted[index];
  }
  
  extractServiceFromUrl(url) {
    try {
      const urlObj = new URL(url);
      const hostname = urlObj.hostname;
      const parts = hostname.split('.');
      return parts[0];
    } catch {
      return null;
    }
  }
  
  generateMetricId() {
    return 'metric_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  
  // 获取性能报告
  getPerformanceReport() {
    const report = {
      timestamp: new Date().toISOString(),
      services: {},
      summary: {
        totalServices: this.analysisResults.size,
        avgPerformanceScore: 0,
        criticalAlerts: this.alerts.filter(a => a.type === 'critical').length,
        warningAlerts: this.alerts.filter(a => a.type === 'warning').length
      }
    };
    
    let totalScore = 0;
    for (const [serviceKey, analysis] of this.analysisResults) {
      report.services[serviceKey] = analysis;
      totalScore += analysis.performanceScore;
    }
    
    if (this.analysisResults.size > 0) {
      report.summary.avgPerformanceScore = totalScore / this.analysisResults.size;
    }
    
    return report;
  }
}
```

## 4. 最佳实践与总结

### 4.1 实施建议

1. **渐进式集成**
   - 从核心服务开始，逐步扩展监控范围
   - 建立统一的追踪标准和命名规范
   - 实施分阶段的监控策略

2. **数据关联策略**
   - 使用统一的追踪ID关联分布式调用
   - 建立服务间的依赖关系映射
   - 实现错误的自动关联和分析

3. **性能优化重点**
   - 关注关键路径的性能指标
   - 识别和解决服务瓶颈
   - 实施智能告警和自动化响应

### 4.2 核心价值

- **全链路追踪**：实现从前端到后端的完整调用链监控
- **智能关联**：自动识别和关联分布式系统中的错误模式
- **性能洞察**：深入分析微服务架构的性能瓶颈
- **预防性监控**：提前发现潜在问题，减少系统故障
- **运维效率**：提升问题定位和解决的效率

### 4.3 未来发展趋势

- **AI驱动分析**：利用机器学习进行异常检测和预测
- **自适应监控**：根据系统负载自动调整监控策略
- **云原生集成**：与Kubernetes、Service Mesh深度集成
- **实时决策**：基于监控数据进行实时的系统优化

通过构建完整的前端Sentry与微服务架构集成方案，我们可以实现高效、智能的分布式监控，为微服务架构的稳定运行提供强有力的技术保障，助力企业构建更加可靠和高性能的分布式系统。

### 1.2 微服务错误关联器

```javascript
// 微服务错误关联器
class MicroserviceErrorCorrelator {
  constructor(options = {}) {
    this.options = {
      services: options.services || [],
      correlationWindow: options.correlationWindow || 300000, // 5分钟
      maxCorrelationDepth: options.maxCorrelationDepth || 5,
      errorThreshold: options.errorThreshold || 0.05,
      ...options
    };
    
    this.errorBuffer = new Map();
    this.serviceTopology = new Map();
    this.correlationRules = new Map();
    
    this.initializeCorrelation();
  }
  
  initializeCorrelation() {
    // 初始化服务拓扑
    this.buildServiceTopology();
    
    // 设置错误关联规则
    this.setupCorrelationRules();
    
    // 启动错误关联处理
    this.startErrorCorrelation();
    
    // 集成Sentry错误处理
    this.integrateSentryErrorHandling();
  }
  
  // 构建服务拓扑
  buildServiceTopology() {
    this.options.services.forEach(service => {
      this.serviceTopology.set(service.name, {
        name: service.name,
        type: service.type || 'api',
        dependencies: service.dependencies || [],
        endpoints: service.endpoints || [],
        healthCheck: service.healthCheck,
        priority: service.priority || 'medium',
        tags: service.tags || {}
      });
    });
  }
  
  // 设置错误关联规则
  setupCorrelationRules() {
    // 基于HTTP状态码的关联规则
    this.correlationRules.set('http_status', {
      name: 'HTTP Status Code Correlation',
      condition: (error1, error2) => {
        return error1.tags?.['http.status_code'] === error2.tags?.['http.status_code'] &&
               Math.abs(error1.timestamp - error2.timestamp) < 60000; // 1分钟内
      },
      weight: 0.8
    });
    
    // 基于追踪ID的关联规则
    this.correlationRules.set('trace_id', {
      name: 'Trace ID Correlation',
      condition: (error1, error2) => {
        return error1.tags?.traceId === error2.tags?.traceId;
      },
      weight: 1.0
    });
    
    // 基于用户ID的关联规则
    this.correlationRules.set('user_id', {
      name: 'User ID Correlation',
      condition: (error1, error2) => {
        return error1.user?.id === error2.user?.id &&
               Math.abs(error1.timestamp - error2.timestamp) < 300000; // 5分钟内
      },
      weight: 0.6
    });
    
    // 基于错误类型的关联规则
    this.correlationRules.set('error_type', {
      name: 'Error Type Correlation',
      condition: (error1, error2) => {
        return error1.exception?.type === error2.exception?.type &&
               Math.abs(error1.timestamp - error2.timestamp) < 120000; // 2分钟内
      },
      weight: 0.7
    });
    
    // 基于服务依赖的关联规则
    this.correlationRules.set('service_dependency', {
      name: 'Service Dependency Correlation',
      condition: (error1, error2) => {
        const service1 = this.serviceTopology.get(error1.tags?.service);
        const service2 = this.serviceTopology.get(error2.tags?.service);
        
        if (!service1 || !service2) return false;
        
        return service1.dependencies.includes(service2.name) ||
               service2.dependencies.includes(service1.name);
      },
      weight: 0.9
    });
  }
  
  // 记录错误事件
  recordError(error) {
    const errorEvent = {
      id: this.generateErrorId(),
      timestamp: Date.now(),
      message: error.message,
      stack: error.stack,
      type: error.constructor.name,
      tags: error.tags || {},
      user: error.user,
      extra: error.extra || {},
      fingerprint: error.fingerprint,
      level: error.level || 'error',
      service: error.tags?.service || 'frontend',
      traceId: error.tags?.traceId,
      spanId: error.tags?.spanId,
      correlations: []
    };
    
    // 添加到错误缓冲区
    this.errorBuffer.set(errorEvent.id, errorEvent);
    
    // 执行错误关联
    this.correlateError(errorEvent);
    
    // 清理过期错误
    this.cleanupExpiredErrors();
    
    return errorEvent;
  }
  
  // 执行错误关联
  correlateError(newError) {
    const correlations = [];
    
    // 遍历错误缓冲区中的其他错误
    for (const [errorId, existingError] of this.errorBuffer) {
      if (errorId === newError.id) continue;
      
      // 检查时间窗口
      if (Math.abs(newError.timestamp - existingError.timestamp) > this.options.correlationWindow) {
        continue;
      }
      
      // 应用关联规则
      let totalWeight = 0;
      let matchedRules = [];
      
      for (const [ruleName, rule] of this.correlationRules) {
        if (rule.condition(newError, existingError)) {
          totalWeight += rule.weight;
          matchedRules.push(ruleName);
        }
      }
      
      // 如果权重超过阈值，建立关联
      if (totalWeight > 0.5) {
        const correlation = {
          errorId: existingError.id,
          weight: totalWeight,
          rules: matchedRules,
          timestamp: Date.now(),
          type: this.determineCorrelationType(matchedRules)
        };
        
        correlations.push(correlation);
        
        // 双向关联
        existingError.correlations.push({
          errorId: newError.id,
          weight: totalWeight,
          rules: matchedRules,
          timestamp: Date.now(),
          type: correlation.type
        });
      }
    }
    
    newError.correlations = correlations;
    
    // 如果发现关联错误，触发分析
    if (correlations.length > 0) {
      this.analyzeErrorPattern(newError);
    }
  }
  
  // 分析错误模式
  analyzeErrorPattern(error) {
    const pattern = {
      id: this.generatePatternId(),
      rootError: error,
      relatedErrors: [],
      services: new Set(),
      users: new Set(),
      timeRange: {
        start: error.timestamp,
        end: error.timestamp
      },
      severity: 'medium',
      category: 'unknown'
    };
    
    // 收集相关错误
    const visited = new Set([error.id]);
    const queue = [error];
    
    while (queue.length > 0 && pattern.relatedErrors.length < this.options.maxCorrelationDepth) {
      const currentError = queue.shift();
      
      currentError.correlations.forEach(correlation => {
        if (!visited.has(correlation.errorId)) {
          const relatedError = this.errorBuffer.get(correlation.errorId);
          if (relatedError) {
            pattern.relatedErrors.push(relatedError);
            pattern.services.add(relatedError.service);
            if (relatedError.user?.id) {
              pattern.users.add(relatedError.user.id);
            }
            
            // 更新时间范围
            pattern.timeRange.start = Math.min(pattern.timeRange.start, relatedError.timestamp);
            pattern.timeRange.end = Math.max(pattern.timeRange.end, relatedError.timestamp);
            
            visited.add(correlation.errorId);
            queue.push(relatedError);
          }
        }
      });
    }
    
    // 分析模式特征
    pattern.category = this.categorizeErrorPattern(pattern);
    pattern.severity = this.calculatePatternSeverity(pattern);
    
    // 生成模式报告
    const report = this.generatePatternReport(pattern);
    
    // 发送到Sentry
    this.sendPatternToSentry(pattern, report);
    
    return pattern;
  }
  
  // 错误模式分类
  categorizeErrorPattern(pattern) {
    const services = Array.from(pattern.services);
    const errors = [pattern.rootError, ...pattern.relatedErrors];
    
    // 级联失败模式
    if (services.length > 1) {
      const hasServiceDependency = services.some(service1 => 
        services.some(service2 => {
          const topology1 = this.serviceTopology.get(service1);
          const topology2 = this.serviceTopology.get(service2);
          return topology1?.dependencies.includes(service2) ||
                 topology2?.dependencies.includes(service1);
        })
      );
      
      if (hasServiceDependency) {
        return 'cascade_failure';
      }
    }
    
    // 批量用户错误模式
    if (pattern.users.size > 5) {
      return 'batch_user_error';
    }
    
    // 网络错误模式
    const networkErrors = errors.filter(error => 
      error.tags?.['http.status_code'] >= 500 ||
      error.message.includes('network') ||
      error.message.includes('timeout')
    );
    
    if (networkErrors.length / errors.length > 0.7) {
      return 'network_issue';
    }
    
    // 数据库错误模式
    const dbErrors = errors.filter(error => 
      error.message.includes('database') ||
      error.message.includes('sql') ||
      error.tags?.component === 'database'
    );
    
    if (dbErrors.length / errors.length > 0.7) {
      return 'database_issue';
    }
    
    // 认证错误模式
    const authErrors = errors.filter(error => 
      error.tags?.['http.status_code'] === 401 ||
      error.tags?.['http.status_code'] === 403 ||
      error.message.includes('auth')
    );
    
    if (authErrors.length / errors.length > 0.7) {
      return 'authentication_issue';
    }
    
    return 'general_error';
  }
  
  // 计算模式严重程度
  calculatePatternSeverity(pattern) {
    let score = 0;
    
    // 基于错误数量
    score += Math.min(pattern.relatedErrors.length * 10, 50);
    
    // 基于影响的服务数量
    score += pattern.services.size * 15;
    
    // 基于影响的用户数量
    score += Math.min(pattern.users.size * 2, 30);
    
    // 基于时间跨度
    const duration = pattern.timeRange.end - pattern.timeRange.start;
    if (duration < 60000) { // 1分钟内
      score += 20; // 快速传播
    }
    
    // 基于错误类型
    const criticalErrors = [pattern.rootError, ...pattern.relatedErrors]
      .filter(error => error.level === 'fatal' || error.tags?.['http.status_code'] >= 500);
    score += criticalErrors.length * 5;
    
    if (score >= 80) return 'critical';
    if (score >= 50) return 'high';
    if (score >= 20) return 'medium';
    return 'low';
  }
  
  // 生成模式报告
  generatePatternReport(pattern) {
    const report = {
      id: pattern.id,
      timestamp: Date.now(),
      category: pattern.category,
      severity: pattern.severity,
      summary: this.generatePatternSummary(pattern),
      details: {
        rootError: {
          message: pattern.rootError.message,
          service: pattern.rootError.service,
          timestamp: pattern.rootError.timestamp
        },
        affectedServices: Array.from(pattern.services),
        affectedUsers: pattern.users.size,
        errorCount: pattern.relatedErrors.length + 1,
        timeRange: {
          start: new Date(pattern.timeRange.start).toISOString(),
          end: new Date(pattern.timeRange.end).toISOString(),
          duration: pattern.timeRange.end - pattern.timeRange.start
        }
      },
      recommendations: this.generateRecommendations(pattern)
    };
    
    return report;
  }
  
  // 生成模式摘要
  generatePatternSummary(pattern) {
    const serviceCount = pattern.services.size;
    const errorCount = pattern.relatedErrors.length + 1;
    const userCount = pattern.users.size;
    
    switch (pattern.category) {
      case 'cascade_failure':
        return `Cascade failure detected across ${serviceCount} services with ${errorCount} related errors`;
      case 'batch_user_error':
        return `Batch user error affecting ${userCount} users with ${errorCount} related errors`;
      case 'network_issue':
        return `Network connectivity issue with ${errorCount} related errors across ${serviceCount} services`;
      case 'database_issue':
        return `Database connectivity/performance issue with ${errorCount} related errors`;
      case 'authentication_issue':
        return `Authentication/authorization issue affecting ${userCount} users`;
      default:
        return `Error pattern detected with ${errorCount} related errors across ${serviceCount} services`;
    }
  }
  
  // 生成建议
  generateRecommendations(pattern) {
    const recommendations = [];
    
    switch (pattern.category) {
      case 'cascade_failure':
        recommendations.push('Check service dependencies and implement circuit breakers');
        recommendations.push('Review service health checks and auto-scaling policies');
        recommendations.push('Consider implementing bulkhead pattern for service isolation');
        break;
      case 'network_issue':
        recommendations.push('Check network connectivity between services');
        recommendations.push('Review load balancer configuration and health checks');
        recommendations.push('Implement retry policies with exponential backoff');
        break;
      case 'database_issue':
        recommendations.push('Check database connection pool configuration');
        recommendations.push('Review database performance metrics and slow queries');
        recommendations.push('Consider implementing database connection retry logic');
        break;
      case 'authentication_issue':
        recommendations.push('Check authentication service status and configuration');
        recommendations.push('Review token expiration and refresh logic');
        recommendations.push('Verify user session management');
        break;
      default:
        recommendations.push('Review error logs for common patterns');
        recommendations.push('Check service health and performance metrics');
        recommendations.push('Consider implementing additional monitoring and alerting');
    }
    
    return recommendations;
  }
  
  // 发送模式到Sentry
  sendPatternToSentry(pattern, report) {
    if (typeof Sentry !== 'undefined') {
      Sentry.withScope(scope => {
        scope.setTag('error_pattern', pattern.category);
        scope.setTag('pattern_severity', pattern.severity);
        scope.setLevel(pattern.severity === 'critical' ? 'fatal' : 'error');
        
        scope.setContext('error_pattern', {
          id: pattern.id,
          category: pattern.category,
          severity: pattern.severity,
          errorCount: pattern.relatedErrors.length + 1,
          serviceCount: pattern.services.size,
          userCount: pattern.users.size,
          duration: pattern.timeRange.end - pattern.timeRange.start
        });
        
        scope.setContext('pattern_report', report);
        
        Sentry.captureMessage(`Error Pattern Detected: ${report.summary}`, 'error');
      });
    }
  }
  
  // 启动错误关联处理
  startErrorCorrelation() {
    // 定期清理过期错误
    setInterval(() => {
      this.cleanupExpiredErrors();
    }, 60000); // 每分钟清理一次
  }
  
  // 集成Sentry错误处理
  integrateSentryErrorHandling() {
    if (typeof Sentry !== 'undefined') {
      Sentry.addGlobalEventProcessor((event) => {
        if (event.exception) {
          // 记录错误到关联器
          const error = {
            message: event.exception.values?.[0]?.value || 'Unknown error',
            stack: event.exception.values?.[0]?.stacktrace,
            tags: event.tags,
            user: event.user,
            extra: event.extra,
            fingerprint: event.fingerprint,
            level: event.level
          };
          
          this.recordError(error);
        }
        
        return event;
      });
    }
  }
  
  // 清理过期错误
  cleanupExpiredErrors() {
    const now = Date.now();
    const expiredErrors = [];
    
    for (const [errorId, error] of this.errorBuffer) {
      if (now - error.timestamp > this.options.correlationWindow) {
        expiredErrors.push(errorId);
      }
    }
    
    expiredErrors.forEach(errorId => {
      this.errorBuffer.delete(errorId);
    });
  }
  
  // 确定关联类型
  determineCorrelationType(matchedRules) {
    if (matchedRules.includes('trace_id')) return 'trace';
    if (matchedRules.includes('service_dependency')) return 'service';
    if (matchedRules.includes('user_id')) return 'user';
    if (matchedRules.includes('http_status')) return 'http';
    return 'general';
  }
  
  // 生成错误ID
  generateErrorId() {
    return 'err_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  
  // 生成模式ID
  generatePatternId() {
    return 'pattern_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  
  // 获取错误统计
  getErrorStats() {
    const errors = Array.from(this.errorBuffer.values());
    const now = Date.now();
    
    return {
      totalErrors: errors.length,
      recentErrors: errors.filter(error => now - error.timestamp < 3600000).length, // 1小时内
      correlatedErrors: errors.filter(error => error.correlations.length > 0).length,
      serviceDistribution: this.getServiceDistribution(errors),
      categoryDistribution: this.getCategoryDistribution(errors)
    };
  }
  
  // 获取服务分布
  getServiceDistribution(errors) {
    const distribution = {};
    errors.forEach(error => {
      const service = error.service || 'unknown';
      distribution[service] = (distribution[service] || 0) + 1;
    });
    return distribution;
  }
  
  // 获取类别分布
  getCategoryDistribution(errors) {
    const distribution = {};
    errors.forEach(error => {
      const category = error.type || 'unknown';
      distribution[category] = (distribution[category] || 0) + 1;
    });
    return distribution;
  }
}
```

## 2. 服务间通信监控

### 2.1 API调用监控器

```javascript
// API调用监控器
class APICallMonitor {
  constructor(options = {}) {
    this.options = {
      endpoints: options.endpoints || [],
      timeout: options.timeout || 30000,
      retryAttempts: options.retryAttempts || 3,
      circuitBreakerThreshold: options.circuitBreakerThreshold || 5,
      circuitBreakerTimeout: options.circuitBreakerTimeout || 60000,
      healthCheckInterval: options.healthCheckInterval || 30000,
      ...options
    };
    
    this.callStats = new Map();
    this.circuitBreakers = new Map();
    this.healthStatus = new Map();
    
    this.initializeMonitoring();
  }
  
  initializeMonitoring() {
    // 初始化端点统计
    this.options.endpoints.forEach(endpoint => {
      this.callStats.set(endpoint.name, {
        totalCalls: 0,
        successfulCalls: 0,
        failedCalls: 0,
        avgResponseTime: 0,
        lastCallTime: null,
        recentCalls: [],
        errors: []
      });
      
      this.circuitBreakers.set(endpoint.name, {
        state: 'closed', // closed, open, half-open
        failureCount: 0,
        lastFailureTime: null,
        nextAttemptTime: null
      });
      
      this.healthStatus.set(endpoint.name, {
        status: 'unknown',
        lastCheck: null,
        responseTime: null,
        error: null
      });
    });
    
    // 启动健康检查
    this.startHealthChecks();
    
    // 拦截API调用
    this.interceptAPICalls();
  }
  
  // 拦截API调用
  interceptAPICalls() {
    const originalFetch = window.fetch;
    
    window.fetch = async (url, options = {}) => {
      const endpoint = this.findEndpointByUrl(url);
      
      if (endpoint) {
        return this.monitoredFetch(endpoint, url, options, originalFetch);
      }
      
      return originalFetch(url, options);
    };
  }
  
  // 监控的fetch调用
  async monitoredFetch(endpoint, url, options, originalFetch) {
    const startTime = Date.now();
    const callId = this.generateCallId();
    
    // 检查熔断器状态
    if (!this.canMakeCall(endpoint.name)) {
      const error = new Error(`Circuit breaker is open for ${endpoint.name}`);
      this.recordFailure(endpoint.name, startTime, error, callId);
      throw error;
    }
    
    // 添加监控头
    options.headers = {
      ...options.headers,
      'X-Call-ID': callId,
      'X-Timestamp': startTime.toString(),
      'X-Client': 'frontend-monitor'
    };
    
    try {
      // 设置超时
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), this.options.timeout);
      
      options.signal = controller.signal;
      
      const response = await originalFetch(url, options);
      clearTimeout(timeoutId);
      
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      // 记录成功调用
      this.recordSuccess(endpoint.name, startTime, duration, response, callId);
      
      // 发送调用数据到Sentry
      this.sendCallDataToSentry(endpoint.name, {
        callId,
        url,
        method: options.method || 'GET',
        status: response.status,
        duration,
        success: true
      });
      
      return response;
    } catch (error) {
      const endTime = Date.now();
      const duration = endTime - startTime;
      
      // 记录失败调用
      this.recordFailure(endpoint.name, startTime, error, callId, duration);
      
      // 发送错误数据到Sentry
      this.sendCallDataToSentry(endpoint.name, {
        callId,
        url,
        method: options.method || 'GET',
        duration,
        success: false,
        error: error.message
      });
      
      throw error;
    }
  }
  
  // 记录成功调用
  recordSuccess(endpointName, startTime, duration, response, callId) {
    const stats = this.callStats.get(endpointName);
    if (!stats) return;
    
    stats.totalCalls++;
    stats.successfulCalls++;
    stats.lastCallTime = startTime;
    
    // 更新平均响应时间
    stats.avgResponseTime = (
      (stats.avgResponseTime * (stats.totalCalls - 1)) + duration
    ) / stats.totalCalls;
    
    // 添加到最近调用记录
    stats.recentCalls.push({
      callId,
      timestamp: startTime,
      duration,
      status: response.status,
      success: true
    });
    
    // 保持最近100次调用记录
    if (stats.recentCalls.length > 100) {
      stats.recentCalls.shift();
    }
    
    // 重置熔断器
    this.resetCircuitBreaker(endpointName);
  }
  
  // 记录失败调用
  recordFailure(endpointName, startTime, error, callId, duration = 0) {
    const stats = this.callStats.get(endpointName);
    if (!stats) return;
    
    stats.totalCalls++;
    stats.failedCalls++;
    stats.lastCallTime = startTime;
    
    // 添加错误记录
    stats.errors.push({
      callId,
      timestamp: startTime,
      error: error.message,
      stack: error.stack
    });
    
    // 保持最近50个错误记录
    if (stats.errors.length > 50) {
      stats.errors.shift();
    }
    
    // 添加到最近调用记录
    stats.recentCalls.push({
      callId,
      timestamp: startTime,
      duration,
      success: false,
      error: error.message
    });
    
    // 保持最近100次调用记录
    if (stats.recentCalls.length > 100) {
      stats.recentCalls.shift();
    }
    
    // 更新熔断器
    this.updateCircuitBreaker(endpointName);
  }
  
  // 检查是否可以发起调用
  canMakeCall(endpointName) {
    const breaker = this.circuitBreakers.get(endpointName);
    if (!breaker) return true;
    
    const now = Date.now();
    
    switch (breaker.state) {
      case 'closed':
        return true;
      case 'open':
        if (now >= breaker.nextAttemptTime) {
          breaker.state = 'half-open';
          return true;
        }
        return false;
      case 'half-open':
        return true;
      default:
        return true;
    }
  }
  
  // 更新熔断器状态
  updateCircuitBreaker(endpointName) {
    const breaker = this.circuitBreakers.get(endpointName);
    if (!breaker) return;
    
    breaker.failureCount++;
    breaker.lastFailureTime = Date.now();
    
    if (breaker.state === 'half-open') {
      // 半开状态下失败，重新打开熔断器
      breaker.state = 'open';
      breaker.nextAttemptTime = Date.now() + this.options.circuitBreakerTimeout;
    } else if (breaker.failureCount >= this.options.circuitBreakerThreshold) {
      // 失败次数达到阈值，打开熔断器
      breaker.state = 'open';
      breaker.nextAttemptTime = Date.now() + this.options.circuitBreakerTimeout;
      
      // 发送熔断器打开通知
      this.notifyCircuitBreakerOpen(endpointName);
    }
  }
  
  // 重置熔断器
  resetCircuitBreaker(endpointName) {
    const breaker = this.circuitBreakers.get(endpointName);
    if (!breaker) return;
    
    if (breaker.state === 'half-open') {
      // 半开状态下成功，关闭熔断器
      breaker.state = 'closed';
      breaker.failureCount = 0;
      
      // 发送熔断器关闭通知
      this.notifyCircuitBreakerClosed(endpointName);
    } else if (breaker.state === 'closed') {
      // 重置失败计数
      breaker.failureCount = Math.max(0, breaker.failureCount - 1);
    }
  }
  
  // 启动健康检查
  startHealthChecks() {
    setInterval(() => {
      this.performHealthChecks();
    }, this.options.healthCheckInterval);
    
    // 立即执行一次健康检查
    this.performHealthChecks();
  }
  
  // 执行健康检查
  async performHealthChecks() {
    const promises = this.options.endpoints.map(endpoint => 
      this.checkEndpointHealth(endpoint)
    );
    
    await Promise.allSettled(promises);
  }
  
  // 检查端点健康状态
  async checkEndpointHealth(endpoint) {
    const startTime = Date.now();
    
    try {
      const response = await fetch(endpoint.healthCheck || endpoint.url, {
        method: 'GET',
        headers: {
          'X-Health-Check': 'true'
        },
        timeout: 10000 // 10秒超时
      });
      
      const endTime = Date.now();
      const responseTime = endTime - startTime;
      
      this.healthStatus.set(endpoint.name, {
        status: response.ok ? 'healthy' : 'unhealthy',
        lastCheck: startTime,
        responseTime,
        error: null
      });
      
    } catch (error) {
      const endTime = Date.now();
      const responseTime = endTime - startTime;
      
      this.healthStatus.set(endpoint.name, {
        status: 'unhealthy',
        lastCheck: startTime,
        responseTime,
        error: error.message
      });
    }
  }
  
  // 发送调用数据到Sentry
  sendCallDataToSentry(endpointName, callData) {
    if (typeof Sentry !== 'undefined') {
      Sentry.addBreadcrumb({
        category: 'api.call',
        message: `API call to ${endpointName}`,
        level: callData.success ? 'info' : 'error',
        data: callData
      });
      
      // 如果是错误调用，发送事件
      if (!callData.success) {
        Sentry.withScope(scope => {
          scope.setTag('api.endpoint', endpointName);
          scope.setTag('api.method', callData.method);
          scope.setContext('api_call', callData);
          
          Sentry.captureMessage(`API call failed: ${endpointName}`, 'warning');
        });
      }
    }
  }
  
  // 通知熔断器打开
  notifyCircuitBreakerOpen(endpointName) {
    if (typeof Sentry !== 'undefined') {
      Sentry.withScope(scope => {
        scope.setTag('circuit_breaker', 'open');
        scope.setTag('endpoint', endpointName);
        scope.setLevel('warning');
        
        Sentry.captureMessage(`Circuit breaker opened for ${endpointName}`, 'warning');
      });
    }
  }
  
  // 通知熔断器关闭
  notifyCircuitBreakerClosed(endpointName) {
    if (typeof Sentry !== 'undefined') {
      Sentry.withScope(scope => {
        scope.setTag('circuit_breaker', 'closed');
        scope.setTag('endpoint', endpointName);
        scope.setLevel('info');
        
        Sentry.captureMessage(`Circuit breaker closed for ${endpointName}`, 'info');
      });
    }
  }
  
  // 根据URL查找端点
  findEndpointByUrl(url) {
    return this.options.endpoints.find(endpoint => {
      if (typeof endpoint.url === 'string') {
        return url.includes(endpoint.url);
      } else if (endpoint.url instanceof RegExp) {
        return endpoint.url.test(url);
      }
      return false;
    });
  }
  
  // 生成调用ID
  generateCallId() {
    return 'call_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  
  // 获取端点统计
  getEndpointStats(endpointName) {
    const stats = this.callStats.get(endpointName);
    const breaker = this.circuitBreakers.get(endpointName);
    const health = this.healthStatus.get(endpointName);
    
    if (!stats) return null;
    
    return {
      name: endpointName,
      totalCalls: stats.totalCalls,
      successRate: stats.totalCalls > 0 ? stats.successfulCalls / stats.totalCalls : 0,
      avgResponseTime: stats.avgResponseTime,
      lastCallTime: stats.lastCallTime,
      circuitBreakerState: breaker?.state || 'unknown',
      healthStatus: health?.status || 'unknown',
      recentErrors: stats.errors.slice(-10) // 最近10个错误
    };
  }
  
  // 获取所有端点统计
  getAllEndpointStats() {
    return this.options.endpoints.map(endpoint => 
      this.getEndpointStats(endpoint.name)
    ).filter(stats => stats !== null);
  }
  
  // 获取系统健康状态
  getSystemHealth() {
    const allStats = this.getAllEndpointStats();
    const healthyEndpoints = allStats.filter(stats => 
      stats.healthStatus === 'healthy' && stats.circuitBreakerState === 'closed'
    );
    
    return {
      totalEndpoints: allStats.length,
      healthyEndpoints: healthyEndpoints.length,
      systemHealthScore: allStats.length > 0 ? healthyEndpoints.length / allStats.length : 0,
      avgResponseTime: allStats.length > 0 ? 
        allStats.reduce((sum, stats) => sum + stats.avgResponseTime, 0) / allStats.length : 0,
      avgSuccessRate: allStats.length > 0 ? 
        allStats.reduce((sum, stats) => sum + stats.successRate, 0) / allStats.length : 0
    };
  }
}
```