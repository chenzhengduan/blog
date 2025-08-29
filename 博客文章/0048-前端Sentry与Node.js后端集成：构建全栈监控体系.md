# 前端Sentry与Node.js后端集成：构建全栈监控体系

在现代Web应用开发中，前后端分离架构已成为主流。为了确保应用的稳定性和性能，我们需要建立一个覆盖前端和后端的全栈监控体系。本文将详细介绍如何将前端Sentry与Node.js后端进行深度集成，构建一个统一、高效的全栈监控解决方案。

## 1. 全栈监控架构设计

### 1.1 全栈监控集成器

```typescript
// 全栈监控集成器
import * as Sentry from '@sentry/node';
import * as SentryTracing from '@sentry/tracing';
import { Request, Response, NextFunction } from 'express';
import { createHash } from 'crypto';
import { performance } from 'perf_hooks';

interface FullStackConfig {
  frontend: {
    dsn: string;
    environment: string;
    release?: string;
    sampleRate?: number;
  };
  backend: {
    dsn: string;
    environment: string;
    release?: string;
    sampleRate?: number;
    tracesSampleRate?: number;
  };
  correlation: {
    enableTraceCorrelation?: boolean;
    enableUserCorrelation?: boolean;
    enableSessionCorrelation?: boolean;
  };
  database: {
    enableQueryMonitoring?: boolean;
    slowQueryThreshold?: number;
  };
  cache: {
    enableCacheMonitoring?: boolean;
    slowCacheThreshold?: number;
  };
}

interface TraceContext {
  traceId: string;
  spanId: string;
  parentSpanId?: string;
  userId?: string;
  sessionId?: string;
  requestId: string;
  timestamp: number;
}

interface RequestMetrics {
  requestId: string;
  method: string;
  url: string;
  statusCode?: number;
  duration?: number;
  userAgent?: string;
  ip?: string;
  userId?: string;
  sessionId?: string;
  traceId?: string;
  errors: any[];
  dbQueries: any[];
  cacheOperations: any[];
  memoryUsage?: any;
  cpuUsage?: any;
}

class FullStackMonitoringIntegrator {
  private config: FullStackConfig;
  private requestMetrics: Map<string, RequestMetrics> = new Map();
  private activeTransactions: Map<string, any> = new Map();
  private dbQueryCount = 0;
  private cacheOperationCount = 0;
  
  constructor(config: FullStackConfig) {
    this.config = config;
    this.initializeBackendSentry();
  }
  
  private initializeBackendSentry(): void {
    Sentry.init({
      dsn: this.config.backend.dsn,
      environment: this.config.backend.environment,
      release: this.config.backend.release,
      sampleRate: this.config.backend.sampleRate || 1.0,
      tracesSampleRate: this.config.backend.tracesSampleRate || 0.1,
      integrations: [
        new Sentry.Integrations.Http({ tracing: true }),
        new SentryTracing.Integrations.Express({ app: undefined }),
        new SentryTracing.Integrations.Postgres(),
        new SentryTracing.Integrations.Mysql(),
        new SentryTracing.Integrations.Mongo()
      ],
      beforeSend: (event) => {
        return this.enhanceEvent(event);
      },
      beforeSendTransaction: (transaction) => {
        return this.enhanceTransaction(transaction);
      }
    });
  }
  
  // Express中间件：请求追踪
  public requestTracking() {
    return (req: Request, res: Response, next: NextFunction) => {
      const startTime = performance.now();
      const requestId = this.generateRequestId();
      
      // 从前端传递的追踪信息
      const frontendTraceId = req.headers['x-trace-id'] as string;
      const frontendSpanId = req.headers['x-span-id'] as string;
      const userId = req.headers['x-user-id'] as string;
      const sessionId = req.headers['x-session-id'] as string;
      
      // 创建请求指标
      const metrics: RequestMetrics = {
        requestId,
        method: req.method,
        url: req.url,
        userAgent: req.headers['user-agent'],
        ip: this.getClientIP(req),
        userId,
        sessionId,
        traceId: frontendTraceId,
        errors: [],
        dbQueries: [],
        cacheOperations: []
      };
      
      this.requestMetrics.set(requestId, metrics);
      
      // 设置Sentry上下文
      Sentry.configureScope(scope => {
        scope.setTag('request-id', requestId);
        scope.setTag('frontend-trace-id', frontendTraceId);
        scope.setUser({ id: userId });
        scope.setContext('request', {
          id: requestId,
          method: req.method,
          url: req.url,
          userAgent: req.headers['user-agent'],
          ip: this.getClientIP(req)
        });
        
        if (this.config.correlation.enableTraceCorrelation && frontendTraceId) {
          scope.setTag('trace-correlation', 'enabled');
          scope.setContext('traceCorrelation', {
            frontendTraceId,
            frontendSpanId,
            backendRequestId: requestId
          });
        }
      });
      
      // 开始事务
      const transaction = Sentry.startTransaction({
        name: `${req.method} ${req.route?.path || req.url}`,
        op: 'http.server',
        tags: {
          'request-id': requestId,
          'frontend-trace-id': frontendTraceId
        },
        data: {
          url: req.url,
          method: req.method,
          query: req.query,
          headers: this.sanitizeHeaders(req.headers)
        }
      });
      
      this.activeTransactions.set(requestId, transaction);
      
      // 扩展req对象
      (req as any).requestId = requestId;
      (req as any).sentryTransaction = transaction;
      (req as any).metrics = metrics;
      
      // 监听响应结束
      res.on('finish', () => {
        this.handleRequestEnd(req, res, startTime);
      });
      
      next();
    };
  }
  
  // Express中间件：错误处理
  public errorHandler() {
    return (error: any, req: Request, res: Response, next: NextFunction) => {
      const requestId = (req as any).requestId;
      const metrics = this.requestMetrics.get(requestId);
      
      if (metrics) {
        metrics.errors.push({
          message: error.message,
          stack: error.stack,
          timestamp: Date.now()
        });
      }
      
      // 报告错误到Sentry
      Sentry.withScope(scope => {
        scope.setTag('error-type', 'request');
        scope.setTag('request-id', requestId);
        scope.setLevel('error');
        
        scope.setContext('requestError', {
          requestId,
          method: req.method,
          url: req.url,
          statusCode: res.statusCode,
          userAgent: req.headers['user-agent'],
          ip: this.getClientIP(req)
        });
        
        Sentry.captureException(error);
      });
      
      next(error);
    };
  }
  
  // 数据库查询监控
  public monitorDatabaseQuery(query: string, duration: number, error?: any): void {
    this.dbQueryCount++;
    
    const queryHash = this.hashQuery(query);
    const isSlowQuery = duration > (this.config.database.slowQueryThreshold || 1000);
    
    // 添加到当前请求指标
    const currentRequest = this.getCurrentRequestMetrics();
    if (currentRequest) {
      currentRequest.dbQueries.push({
        query: query.length > 200 ? query.substring(0, 200) + '...' : query,
        queryHash,
        duration,
        error: error?.message,
        timestamp: Date.now(),
        isSlowQuery
      });
    }
    
    // 报告慢查询
    if (isSlowQuery) {
      this.reportSlowDatabaseQuery(query, duration, error);
    }
    
    // 报告数据库错误
    if (error) {
      this.reportDatabaseError(query, error);
    }
    
    // 添加面包屑
    Sentry.addBreadcrumb({
      category: 'database',
      message: `Database query ${error ? 'failed' : 'completed'}`,
      level: error ? 'error' : isSlowQuery ? 'warning' : 'info',
      data: {
        queryHash,
        duration: Math.round(duration),
        isSlowQuery,
        hasError: !!error
      }
    });
  }
  
  // 缓存操作监控
  public monitorCacheOperation(operation: string, key: string, duration: number, hit?: boolean, error?: any): void {
    this.cacheOperationCount++;
    
    const isSlowOperation = duration > (this.config.cache.slowCacheThreshold || 100);
    
    // 添加到当前请求指标
    const currentRequest = this.getCurrentRequestMetrics();
    if (currentRequest) {
      currentRequest.cacheOperations.push({
        operation,
        key: key.length > 50 ? key.substring(0, 50) + '...' : key,
        duration,
        hit,
        error: error?.message,
        timestamp: Date.now(),
        isSlowOperation
      });
    }
    
    // 报告慢缓存操作
    if (isSlowOperation) {
      this.reportSlowCacheOperation(operation, key, duration);
    }
    
    // 报告缓存错误
    if (error) {
      this.reportCacheError(operation, key, error);
    }
    
    // 添加面包屑
    Sentry.addBreadcrumb({
      category: 'cache',
      message: `Cache ${operation} ${error ? 'failed' : 'completed'}`,
      level: error ? 'error' : isSlowOperation ? 'warning' : 'info',
      data: {
        operation,
        keyHash: this.hashString(key),
        duration: Math.round(duration),
        hit,
        isSlowOperation,
        hasError: !!error
      }
    });
  }
  
  // 用户行为关联
  public correlateUserAction(action: string, data: any, frontendTraceId?: string): void {
    if (!this.config.correlation.enableUserCorrelation) return;
    
    Sentry.withScope(scope => {
      scope.setTag('correlation-type', 'user-action');
      scope.setTag('action', action);
      
      if (frontendTraceId) {
        scope.setTag('frontend-trace-id', frontendTraceId);
      }
      
      scope.setContext('userAction', {
        action,
        data: this.sanitizeData(data),
        frontendTraceId,
        timestamp: new Date().toISOString()
      });
      
      Sentry.addBreadcrumb({
        category: 'user-action',
        message: `User action: ${action}`,
        level: 'info',
        data: {
          action,
          frontendTraceId,
          hasData: !!data
        }
      });
    });
  }
  
  // 性能指标收集
  public collectPerformanceMetrics(): any {
    const memoryUsage = process.memoryUsage();
    const cpuUsage = process.cpuUsage();
    
    return {
      memory: {
        rss: memoryUsage.rss,
        heapTotal: memoryUsage.heapTotal,
        heapUsed: memoryUsage.heapUsed,
        external: memoryUsage.external,
        arrayBuffers: memoryUsage.arrayBuffers
      },
      cpu: {
        user: cpuUsage.user,
        system: cpuUsage.system
      },
      uptime: process.uptime(),
      dbQueryCount: this.dbQueryCount,
      cacheOperationCount: this.cacheOperationCount,
      activeRequests: this.requestMetrics.size,
      timestamp: Date.now()
    };
  }
  
  private handleRequestEnd(req: Request, res: Response, startTime: number): void {
    const duration = performance.now() - startTime;
    const requestId = (req as any).requestId;
    const transaction = this.activeTransactions.get(requestId);
    const metrics = this.requestMetrics.get(requestId);
    
    if (metrics) {
      metrics.statusCode = res.statusCode;
      metrics.duration = duration;
      metrics.memoryUsage = process.memoryUsage();
      metrics.cpuUsage = process.cpuUsage();
    }
    
    // 完成事务
    if (transaction) {
      transaction.setHttpStatus(res.statusCode);
      transaction.setData('response.status_code', res.statusCode);
      transaction.setData('response.duration', duration);
      transaction.finish();
      this.activeTransactions.delete(requestId);
    }
    
    // 发送请求摘要
    this.sendRequestSummary(requestId);
    
    // 清理请求指标
    setTimeout(() => {
      this.requestMetrics.delete(requestId);
    }, 60000); // 1分钟后清理
  }
  
  private sendRequestSummary(requestId: string): void {
    const metrics = this.requestMetrics.get(requestId);
    if (!metrics) return;
    
    Sentry.addBreadcrumb({
      category: 'request',
      message: `Request ${requestId} summary`,
      level: 'info',
      data: {
        requestId,
        method: metrics.method,
        url: metrics.url,
        statusCode: metrics.statusCode,
        duration: Math.round(metrics.duration || 0),
        errorCount: metrics.errors.length,
        dbQueryCount: metrics.dbQueries.length,
        cacheOperationCount: metrics.cacheOperations.length,
        traceId: metrics.traceId
      }
    });
  }
  
  private getCurrentRequestMetrics(): RequestMetrics | undefined {
    // 简化实现：返回最新的请求指标
    const entries = Array.from(this.requestMetrics.entries());
    return entries.length > 0 ? entries[entries.length - 1][1] : undefined;
  }
  
  private generateRequestId(): string {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  private getClientIP(req: Request): string {
    return (req.headers['x-forwarded-for'] as string)?.split(',')[0] ||
           req.connection.remoteAddress ||
           req.socket.remoteAddress ||
           'unknown';
  }
  
  private hashQuery(query: string): string {
    return createHash('md5').update(query).digest('hex').substring(0, 8);
  }
  
  private hashString(str: string): string {
    return createHash('md5').update(str).digest('hex').substring(0, 8);
  }
  
  private sanitizeHeaders(headers: any): any {
    const sanitized = { ...headers };
    const sensitiveHeaders = ['authorization', 'cookie', 'x-api-key', 'x-auth-token'];
    
    sensitiveHeaders.forEach(header => {
      if (sanitized[header]) {
        sanitized[header] = '[FILTERED]';
      }
    });
    
    return sanitized;
  }
  
  private sanitizeData(data: any): any {
    if (!data || typeof data !== 'object') return data;
    
    const sanitized = { ...data };
    const sensitiveFields = ['password', 'token', 'secret', 'key', 'auth'];
    
    Object.keys(sanitized).forEach(key => {
      if (sensitiveFields.some(field => key.toLowerCase().includes(field))) {
        sanitized[key] = '[FILTERED]';
      }
    });
    
    return sanitized;
  }
  
  private enhanceEvent(event: any): any {
    // 增强错误事件
    const performanceMetrics = this.collectPerformanceMetrics();
    
    event.contexts = event.contexts || {};
    event.contexts.performance = performanceMetrics;
    
    return event;
  }
  
  private enhanceTransaction(transaction: any): any {
    // 增强事务数据
    const performanceMetrics = this.collectPerformanceMetrics();
    
    transaction.contexts = transaction.contexts || {};
    transaction.contexts.performance = performanceMetrics;
    
    return transaction;
  }
  
  private reportSlowDatabaseQuery(query: string, duration: number, error?: any): void {
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-database-query');
      scope.setLevel('warning');
      
      scope.setContext('slowDatabaseQuery', {
        query: query.substring(0, 500),
        queryHash: this.hashQuery(query),
        duration,
        threshold: this.config.database.slowQueryThreshold,
        hasError: !!error
      });
      
      Sentry.captureMessage(
        `Slow database query detected: ${Math.round(duration)}ms`,
        'warning'
      );
    });
  }
  
  private reportDatabaseError(query: string, error: any): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'database');
      scope.setLevel('error');
      
      scope.setContext('databaseError', {
        query: query.substring(0, 500),
        queryHash: this.hashQuery(query),
        errorMessage: error.message
      });
      
      Sentry.captureException(error);
    });
  }
  
  private reportSlowCacheOperation(operation: string, key: string, duration: number): void {
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-cache-operation');
      scope.setLevel('warning');
      
      scope.setContext('slowCacheOperation', {
        operation,
        key: key.substring(0, 100),
        keyHash: this.hashString(key),
        duration,
        threshold: this.config.cache.slowCacheThreshold
      });
      
      Sentry.captureMessage(
        `Slow cache operation detected: ${operation} took ${Math.round(duration)}ms`,
        'warning'
      );
    });
  }
  
  private reportCacheError(operation: string, key: string, error: any): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'cache');
      scope.setLevel('error');
      
      scope.setContext('cacheError', {
        operation,
        key: key.substring(0, 100),
        keyHash: this.hashString(key),
        errorMessage: error.message
      });
      
      Sentry.captureException(error);
    });
  }
}

export { FullStackMonitoringIntegrator, FullStackConfig, TraceContext, RequestMetrics };
```

## 2. 前后端追踪关联

### 2.1 前端追踪发送器

```typescript
// 前端追踪发送器
import * as Sentry from '@sentry/browser';
import { BrowserTracing } from '@sentry/tracing';

interface FrontendTraceConfig {
  enableBackendCorrelation: boolean;
  enableUserTracking: boolean;
  enableSessionTracking: boolean;
  apiBaseUrl: string;
  traceHeaderName: string;
  spanHeaderName: string;
  userHeaderName: string;
  sessionHeaderName: string;
}

class FrontendTraceSender {
  private config: FrontendTraceConfig;
  private currentUser: any = null;
  private sessionId: string;
  private traceQueue: any[] = [];
  private isOnline = navigator.onLine;
  
  constructor(config: FrontendTraceConfig) {
    this.config = config;
    this.sessionId = this.generateSessionId();
    this.setupNetworkInterception();
    this.setupOnlineStatusTracking();
  }
  
  private setupNetworkInterception(): void {
    // 拦截fetch请求
    const originalFetch = window.fetch;
    window.fetch = async (input: RequestInfo, init?: RequestInit) => {
      return this.interceptFetch(originalFetch, input, init);
    };
    
    // 拦截XMLHttpRequest
    const originalXHROpen = XMLHttpRequest.prototype.open;
    const originalXHRSend = XMLHttpRequest.prototype.send;
    
    XMLHttpRequest.prototype.open = function(method: string, url: string | URL, ...args: any[]) {
      (this as any)._method = method;
      (this as any)._url = url.toString();
      return originalXHROpen.apply(this, [method, url, ...args]);
    };
    
    XMLHttpRequest.prototype.send = function(body?: any) {
      this.addEventListener('loadstart', () => {
        const traceHeaders = this.getTraceHeaders();
        Object.keys(traceHeaders).forEach(key => {
          this.setRequestHeader(key, traceHeaders[key]);
        });
      }.bind(this));
      
      return originalXHRSend.apply(this, [body]);
    }.bind(this);
  }
  
  private async interceptFetch(originalFetch: any, input: RequestInfo, init?: RequestInit): Promise<Response> {
    const url = typeof input === 'string' ? input : input.url;
    
    // 只拦截API请求
    if (!this.isApiRequest(url)) {
      return originalFetch(input, init);
    }
    
    const startTime = performance.now();
    const traceHeaders = this.getTraceHeaders();
    
    // 添加追踪头
    const headers = new Headers(init?.headers);
    Object.keys(traceHeaders).forEach(key => {
      headers.set(key, traceHeaders[key]);
    });
    
    const enhancedInit = {
      ...init,
      headers
    };
    
    // 开始Span
    const transaction = Sentry.getCurrentHub().getScope()?.getTransaction();
    const span = transaction?.startChild({
      op: 'http.client',
      description: `${init?.method || 'GET'} ${url}`,
      data: {
        url,
        method: init?.method || 'GET',
        'http.request.headers': this.sanitizeHeaders(Object.fromEntries(headers.entries()))
      }
    });
    
    try {
      const response = await originalFetch(input, enhancedInit);
      const duration = performance.now() - startTime;
      
      // 记录响应信息
      if (span) {
        span.setHttpStatus(response.status);
        span.setData('http.response.status_code', response.status);
        span.setData('http.response.duration', duration);
      }
      
      // 发送追踪数据到后端
      this.sendTraceData({
        type: 'api-request',
        url,
        method: init?.method || 'GET',
        statusCode: response.status,
        duration,
        traceId: traceHeaders[this.config.traceHeaderName],
        spanId: traceHeaders[this.config.spanHeaderName],
        timestamp: Date.now(),
        success: response.ok
      });
      
      // 检查慢请求
      if (duration > 2000) {
        this.reportSlowApiRequest(url, duration, response.status);
      }
      
      // 检查错误响应
      if (!response.ok) {
        this.reportApiError(url, response.status, duration);
      }
      
      return response;
    } catch (error) {
      const duration = performance.now() - startTime;
      
      // 记录错误信息
      if (span) {
        span.setTag('error', true);
        span.setData('http.response.duration', duration);
      }
      
      // 发送错误追踪数据
      this.sendTraceData({
        type: 'api-error',
        url,
        method: init?.method || 'GET',
        duration,
        error: error.message,
        traceId: traceHeaders[this.config.traceHeaderName],
        spanId: traceHeaders[this.config.spanHeaderName],
        timestamp: Date.now(),
        success: false
      });
      
      throw error;
    } finally {
      if (span) {
        span.finish();
      }
    }
  }
  
  private getTraceHeaders(): Record<string, string> {
    const headers: Record<string, string> = {};
    
    if (this.config.enableBackendCorrelation) {
      const transaction = Sentry.getCurrentHub().getScope()?.getTransaction();
      const traceId = transaction?.traceId || this.generateTraceId();
      const spanId = this.generateSpanId();
      
      headers[this.config.traceHeaderName] = traceId;
      headers[this.config.spanHeaderName] = spanId;
    }
    
    if (this.config.enableUserTracking && this.currentUser) {
      headers[this.config.userHeaderName] = this.currentUser.id;
    }
    
    if (this.config.enableSessionTracking) {
      headers[this.config.sessionHeaderName] = this.sessionId;
    }
    
    return headers;
  }
  
  private isApiRequest(url: string): boolean {
    return url.startsWith(this.config.apiBaseUrl) || 
           url.startsWith('/api/') ||
           url.includes('/api/');
  }
  
  private sendTraceData(data: any): void {
    if (!this.isOnline) {
      this.traceQueue.push(data);
      return;
    }
    
    // 发送到后端的追踪端点
    fetch(`${this.config.apiBaseUrl}/monitoring/traces`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    }).catch(error => {
      // 发送失败，加入队列
      this.traceQueue.push(data);
    });
  }
  
  private setupOnlineStatusTracking(): void {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.flushTraceQueue();
    });
    
    window.addEventListener('offline', () => {
      this.isOnline = false;
    });
  }
  
  private flushTraceQueue(): void {
    if (this.traceQueue.length === 0) return;
    
    const traces = [...this.traceQueue];
    this.traceQueue = [];
    
    fetch(`${this.config.apiBaseUrl}/monitoring/traces/batch`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ traces })
    }).catch(error => {
      // 重新加入队列
      this.traceQueue.unshift(...traces);
    });
  }
  
  private reportSlowApiRequest(url: string, duration: number, statusCode: number): void {
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-api-request');
      scope.setLevel('warning');
      
      scope.setContext('slowApiRequest', {
        url,
        duration,
        statusCode,
        threshold: 2000
      });
      
      Sentry.captureMessage(
        `Slow API request: ${url} took ${Math.round(duration)}ms`,
        'warning'
      );
    });
  }
  
  private reportApiError(url: string, statusCode: number, duration: number): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'api');
      scope.setLevel('error');
      
      scope.setContext('apiError', {
        url,
        statusCode,
        duration
      });
      
      Sentry.captureMessage(
        `API request failed: ${url} returned ${statusCode}`,
        'error'
      );
    });
  }
  
  private generateTraceId(): string {
    return Array.from({ length: 32 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
  }
  
  private generateSpanId(): string {
    return Array.from({ length: 16 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
  }
  
  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
  
  private sanitizeHeaders(headers: Record<string, string>): Record<string, string> {
    const sanitized = { ...headers };
    const sensitiveHeaders = ['authorization', 'cookie', 'x-api-key'];
    
    sensitiveHeaders.forEach(header => {
      if (sanitized[header.toLowerCase()]) {
        sanitized[header.toLowerCase()] = '[FILTERED]';
      }
    });
    
    return sanitized;
  }
  
  // 设置当前用户
  public setUser(user: any): void {
    this.currentUser = user;
    Sentry.setUser(user);
  }
  
  // 清除用户
  public clearUser(): void {
    this.currentUser = null;
    Sentry.setUser(null);
  }
  
  // 手动发送用户行为
  public trackUserAction(action: string, data?: any): void {
    const traceHeaders = this.getTraceHeaders();
    
    this.sendTraceData({
      type: 'user-action',
      action,
      data,
      traceId: traceHeaders[this.config.traceHeaderName],
      spanId: traceHeaders[this.config.spanHeaderName],
      userId: this.currentUser?.id,
      sessionId: this.sessionId,
      timestamp: Date.now()
    });
    
    Sentry.addBreadcrumb({
      category: 'user-action',
      message: `User action: ${action}`,
      level: 'info',
      data: {
        action,
        traceId: traceHeaders[this.config.traceHeaderName],
        hasData: !!data
      }
    });
  }
}

export { FrontendTraceSender, FrontendTraceConfig };
```

## 3. 数据库监控集成

### 3.1 数据库监控器

```typescript
// 数据库监控器
import * as Sentry from '@sentry/node';
import { Pool, PoolClient } from 'pg';
import { Connection } from 'mysql2/promise';
import { MongoClient, Db } from 'mongodb';
import { performance } from 'perf_hooks';

interface DatabaseConfig {
  type: 'postgresql' | 'mysql' | 'mongodb';
  slowQueryThreshold: number;
  enableQueryLogging: boolean;
  enableConnectionPoolMonitoring: boolean;
  maxQueryLogLength: number;
}

interface QueryMetrics {
  query: string;
  queryHash: string;
  duration: number;
  rowCount?: number;
  error?: string;
  timestamp: number;
  connectionId?: string;
  database?: string;
  table?: string;
}

interface ConnectionPoolMetrics {
  totalConnections: number;
  activeConnections: number;
  idleConnections: number;
  waitingClients: number;
  maxConnections: number;
  timestamp: number;
}

class DatabaseMonitor {
  private config: DatabaseConfig;
  private queryMetrics: QueryMetrics[] = [];
  private connectionPoolMetrics: ConnectionPoolMetrics[] = [];
  private queryCount = 0;
  private slowQueryCount = 0;
  private errorCount = 0;
  
  constructor(config: DatabaseConfig) {
    this.config = config;
    this.startMetricsCollection();
  }
  
  // PostgreSQL监控
  public wrapPostgreSQLPool(pool: Pool): Pool {
    const originalQuery = pool.query.bind(pool);
    const originalConnect = pool.connect.bind(pool);
    
    // 包装查询方法
    pool.query = async (text: any, params?: any, callback?: any) => {
      const startTime = performance.now();
      const queryText = typeof text === 'string' ? text : text.text;
      
      try {
        const result = await originalQuery(text, params, callback);
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: queryText,
          queryHash: this.hashQuery(queryText),
          duration,
          rowCount: result.rowCount,
          timestamp: Date.now(),
          database: 'postgresql'
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: queryText,
          queryHash: this.hashQuery(queryText),
          duration,
          error: error.message,
          timestamp: Date.now(),
          database: 'postgresql'
        });
        
        throw error;
      }
    };
    
    // 包装连接方法
    pool.connect = async (callback?: any) => {
      const client = await originalConnect(callback);
      return this.wrapPostgreSQLClient(client);
    };
    
    // 监控连接池
    if (this.config.enableConnectionPoolMonitoring) {
      this.monitorPostgreSQLPool(pool);
    }
    
    return pool;
  }
  
  private wrapPostgreSQLClient(client: PoolClient): PoolClient {
    const originalQuery = client.query.bind(client);
    
    client.query = async (text: any, params?: any, callback?: any) => {
      const startTime = performance.now();
      const queryText = typeof text === 'string' ? text : text.text;
      
      try {
        const result = await originalQuery(text, params, callback);
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: queryText,
          queryHash: this.hashQuery(queryText),
          duration,
          rowCount: result.rowCount,
          timestamp: Date.now(),
          connectionId: (client as any).processID?.toString(),
          database: 'postgresql'
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: queryText,
          queryHash: this.hashQuery(queryText),
          duration,
          error: error.message,
          timestamp: Date.now(),
          connectionId: (client as any).processID?.toString(),
          database: 'postgresql'
        });
        
        throw error;
      }
    };
    
    return client;
  }
  
  // MySQL监控
  public wrapMySQLConnection(connection: Connection): Connection {
    const originalExecute = connection.execute.bind(connection);
    const originalQuery = connection.query.bind(connection);
    
    // 包装execute方法
    connection.execute = async (sql: string, values?: any) => {
      const startTime = performance.now();
      
      try {
        const result = await originalExecute(sql, values);
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: sql,
          queryHash: this.hashQuery(sql),
          duration,
          rowCount: Array.isArray(result[0]) ? result[0].length : 1,
          timestamp: Date.now(),
          connectionId: (connection as any).threadId?.toString(),
          database: 'mysql'
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: sql,
          queryHash: this.hashQuery(sql),
          duration,
          error: error.message,
          timestamp: Date.now(),
          connectionId: (connection as any).threadId?.toString(),
          database: 'mysql'
        });
        
        throw error;
      }
    };
    
    // 包装query方法
    connection.query = async (sql: string, values?: any) => {
      const startTime = performance.now();
      
      try {
        const result = await originalQuery(sql, values);
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: sql,
          queryHash: this.hashQuery(sql),
          duration,
          rowCount: Array.isArray(result[0]) ? result[0].length : 1,
          timestamp: Date.now(),
          connectionId: (connection as any).threadId?.toString(),
          database: 'mysql'
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordQuery({
          query: sql,
          queryHash: this.hashQuery(sql),
          duration,
          error: error.message,
          timestamp: Date.now(),
          connectionId: (connection as any).threadId?.toString(),
          database: 'mysql'
        });
        
        throw error;
      }
    };
    
    return connection;
  }
  
  // MongoDB监控
  public wrapMongoDatabase(db: Db): Db {
    const originalCollection = db.collection.bind(db);
    
    db.collection = (name: string, options?: any) => {
      const collection = originalCollection(name, options);
      return this.wrapMongoCollection(collection, name);
    };
    
    return db;
  }
  
  private wrapMongoCollection(collection: any, collectionName: string): any {
    const operations = ['find', 'findOne', 'insertOne', 'insertMany', 'updateOne', 'updateMany', 'deleteOne', 'deleteMany', 'aggregate'];
    
    operations.forEach(operation => {
      if (typeof collection[operation] === 'function') {
        const originalMethod = collection[operation].bind(collection);
        
        collection[operation] = async (...args: any[]) => {
          const startTime = performance.now();
          const query = JSON.stringify(args[0] || {});
          
          try {
            const result = await originalMethod(...args);
            const duration = performance.now() - startTime;
            
            // 获取结果计数
            let rowCount = 0;
            if (operation === 'find') {
              rowCount = await result.count();
            } else if (operation === 'findOne') {
              rowCount = result ? 1 : 0;
            } else if (result && result.insertedCount !== undefined) {
              rowCount = result.insertedCount;
            } else if (result && result.modifiedCount !== undefined) {
              rowCount = result.modifiedCount;
            } else if (result && result.deletedCount !== undefined) {
              rowCount = result.deletedCount;
            }
            
            this.recordQuery({
              query: `${operation}(${query})`,
              queryHash: this.hashQuery(`${operation}:${query}`),
              duration,
              rowCount,
              timestamp: Date.now(),
              database: 'mongodb',
              table: collectionName
            });
            
            return result;
          } catch (error) {
            const duration = performance.now() - startTime;
            
            this.recordQuery({
              query: `${operation}(${query})`,
              queryHash: this.hashQuery(`${operation}:${query}`),
              duration,
              error: error.message,
              timestamp: Date.now(),
              database: 'mongodb',
              table: collectionName
            });
            
            throw error;
          }
        };
      }
    });
    
    return collection;
  }
  
  private recordQuery(metrics: QueryMetrics): void {
    this.queryCount++;
    
    // 限制查询长度
    if (metrics.query.length > this.config.maxQueryLogLength) {
      metrics.query = metrics.query.substring(0, this.config.maxQueryLogLength) + '...';
    }
    
    // 添加到指标数组
    this.queryMetrics.push(metrics);
    
    // 保持最近1000条记录
    if (this.queryMetrics.length > 1000) {
      this.queryMetrics.shift();
    }
    
    // 检查慢查询
    if (metrics.duration > this.config.slowQueryThreshold) {
      this.slowQueryCount++;
      this.reportSlowQuery(metrics);
    }
    
    // 检查查询错误
    if (metrics.error) {
      this.errorCount++;
      this.reportQueryError(metrics);
    }
    
    // 记录查询日志
    if (this.config.enableQueryLogging) {
      this.logQuery(metrics);
    }
  }
  
  private monitorPostgreSQLPool(pool: Pool): void {
    setInterval(() => {
      const metrics: ConnectionPoolMetrics = {
        totalConnections: pool.totalCount,
        activeConnections: pool.totalCount - pool.idleCount,
        idleConnections: pool.idleCount,
        waitingClients: pool.waitingCount,
        maxConnections: pool.options.max || 10,
        timestamp: Date.now()
      };
      
      this.connectionPoolMetrics.push(metrics);
      
      // 保持最近100条记录
      if (this.connectionPoolMetrics.length > 100) {
        this.connectionPoolMetrics.shift();
      }
      
      // 检查连接池健康状态
      this.checkConnectionPoolHealth(metrics);
    }, 30000); // 每30秒检查一次
  }
  
  private checkConnectionPoolHealth(metrics: ConnectionPoolMetrics): void {
    const utilizationRate = metrics.activeConnections / metrics.maxConnections;
    
    // 连接池使用率过高
    if (utilizationRate > 0.8) {
      Sentry.withScope(scope => {
        scope.setTag('performance-issue', 'high-connection-pool-utilization');
        scope.setLevel('warning');
        
        scope.setContext('connectionPool', {
          utilizationRate: Math.round(utilizationRate * 100),
          activeConnections: metrics.activeConnections,
          maxConnections: metrics.maxConnections,
          waitingClients: metrics.waitingClients
        });
        
        Sentry.captureMessage(
          `High database connection pool utilization: ${Math.round(utilizationRate * 100)}%`,
          'warning'
        );
      });
    }
    
    // 等待客户端过多
    if (metrics.waitingClients > 5) {
      Sentry.withScope(scope => {
        scope.setTag('performance-issue', 'connection-pool-bottleneck');
        scope.setLevel('warning');
        
        scope.setContext('connectionPoolBottleneck', {
          waitingClients: metrics.waitingClients,
          activeConnections: metrics.activeConnections,
          maxConnections: metrics.maxConnections
        });
        
        Sentry.captureMessage(
          `Database connection pool bottleneck: ${metrics.waitingClients} clients waiting`,
          'warning'
        );
      });
    }
  }
  
  private reportSlowQuery(metrics: QueryMetrics): void {
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-database-query');
      scope.setTag('database', metrics.database || 'unknown');
      scope.setLevel('warning');
      
      scope.setContext('slowQuery', {
        queryHash: metrics.queryHash,
        duration: Math.round(metrics.duration),
        threshold: this.config.slowQueryThreshold,
        database: metrics.database,
        table: metrics.table,
        rowCount: metrics.rowCount,
        connectionId: metrics.connectionId
      });
      
      if (this.config.enableQueryLogging) {
        scope.setExtra('query', metrics.query);
      }
      
      Sentry.captureMessage(
        `Slow database query detected: ${Math.round(metrics.duration)}ms`,
        'warning'
      );
    });
  }
  
  private reportQueryError(metrics: QueryMetrics): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'database-query');
      scope.setTag('database', metrics.database || 'unknown');
      scope.setLevel('error');
      
      scope.setContext('queryError', {
        queryHash: metrics.queryHash,
        duration: Math.round(metrics.duration),
        database: metrics.database,
        table: metrics.table,
        connectionId: metrics.connectionId,
        errorMessage: metrics.error
      });
      
      if (this.config.enableQueryLogging) {
        scope.setExtra('query', metrics.query);
      }
      
      Sentry.captureMessage(
        `Database query error: ${metrics.error}`,
        'error'
      );
    });
  }
  
  private logQuery(metrics: QueryMetrics): void {
    Sentry.addBreadcrumb({
      category: 'database',
      message: `Database query ${metrics.error ? 'failed' : 'completed'}`,
      level: metrics.error ? 'error' : metrics.duration > this.config.slowQueryThreshold ? 'warning' : 'info',
      data: {
        queryHash: metrics.queryHash,
        duration: Math.round(metrics.duration),
        database: metrics.database,
        table: metrics.table,
        rowCount: metrics.rowCount,
        hasError: !!metrics.error
      }
    });
  }
  
  private hashQuery(query: string): string {
    // 简化查询以生成一致的哈希
    const normalized = query
      .replace(/\s+/g, ' ')
      .replace(/\d+/g, '?')
      .replace(/'[^']*'/g, '?')
      .replace(/"[^"]*"/g, '?')
      .toLowerCase()
      .trim();
    
    return require('crypto').createHash('md5').update(normalized).digest('hex').substring(0, 8);
  }
  
  private startMetricsCollection(): void {
    // 定期发送数据库指标摘要
    setInterval(() => {
      this.sendDatabaseMetricsSummary();
    }, 60000); // 每分钟发送一次
  }
  
  private sendDatabaseMetricsSummary(): void {
    const now = Date.now();
    const recentQueries = this.queryMetrics.filter(q => now - q.timestamp < 60000);
    
    if (recentQueries.length === 0) return;
    
    const summary = {
      totalQueries: recentQueries.length,
      slowQueries: recentQueries.filter(q => q.duration > this.config.slowQueryThreshold).length,
      errorQueries: recentQueries.filter(q => q.error).length,
      averageDuration: recentQueries.reduce((sum, q) => sum + q.duration, 0) / recentQueries.length,
      maxDuration: Math.max(...recentQueries.map(q => q.duration)),
      databases: [...new Set(recentQueries.map(q => q.database))],
      timestamp: now
    };
    
    Sentry.addBreadcrumb({
      category: 'database',
      message: 'Database metrics summary',
      level: 'info',
      data: summary
    });
  }
  
  // 获取数据库指标
  public getDatabaseMetrics(): any {
    const now = Date.now();
    const recentQueries = this.queryMetrics.filter(q => now - q.timestamp < 300000); // 最近5分钟
    const recentConnectionMetrics = this.connectionPoolMetrics.filter(c => now - c.timestamp < 300000);
    
    return {
      queries: {
        total: this.queryCount,
        recent: recentQueries.length,
        slow: this.slowQueryCount,
        errors: this.errorCount,
        averageDuration: recentQueries.length > 0 ? 
          recentQueries.reduce((sum, q) => sum + q.duration, 0) / recentQueries.length : 0
      },
      connectionPool: recentConnectionMetrics.length > 0 ? 
        recentConnectionMetrics[recentConnectionMetrics.length - 1] : null,
      timestamp: now
    };
  }
}

export { DatabaseMonitor, DatabaseConfig, QueryMetrics, ConnectionPoolMetrics };
```

## 4. 缓存监控集成

### 4.1 缓存监控器

```typescript
// 缓存监控器
import * as Sentry from '@sentry/node';
import { RedisClientType } from 'redis';
import { performance } from 'perf_hooks';

interface CacheConfig {
  type: 'redis' | 'memcached' | 'memory';
  slowOperationThreshold: number;
  enableOperationLogging: boolean;
  enableHitRateMonitoring: boolean;
  hitRateWindow: number; // 统计窗口（毫秒）
}

interface CacheMetrics {
  operation: string;
  key: string;
  keyHash: string;
  duration: number;
  hit?: boolean;
  size?: number;
  ttl?: number;
  error?: string;
  timestamp: number;
}

interface CacheStats {
  totalOperations: number;
  hits: number;
  misses: number;
  errors: number;
  slowOperations: number;
  hitRate: number;
  averageDuration: number;
  timestamp: number;
}

class CacheMonitor {
  private config: CacheConfig;
  private cacheMetrics: CacheMetrics[] = [];
  private operationCount = 0;
  private hitCount = 0;
  private missCount = 0;
  private errorCount = 0;
  private slowOperationCount = 0;
  
  constructor(config: CacheConfig) {
    this.config = config;
    this.startMetricsCollection();
  }
  
  // Redis监控
  public wrapRedisClient(client: RedisClientType): RedisClientType {
    const operations = ['get', 'set', 'del', 'exists', 'expire', 'ttl', 'incr', 'decr', 'hget', 'hset', 'hdel', 'lpush', 'rpush', 'lpop', 'rpop', 'sadd', 'srem', 'smembers'];
    
    operations.forEach(operation => {
      if (typeof (client as any)[operation] === 'function') {
        const originalMethod = (client as any)[operation].bind(client);
        
        (client as any)[operation] = async (...args: any[]) => {
          const startTime = performance.now();
          const key = args[0]?.toString() || 'unknown';
          
          try {
            const result = await originalMethod(...args);
            const duration = performance.now() - startTime;
            
            // 判断是否命中
            let hit: boolean | undefined;
            if (['get', 'hget', 'exists'].includes(operation)) {
              hit = result !== null && result !== undefined && result !== 0;
            }
            
            // 获取数据大小
            let size: number | undefined;
            if (result && typeof result === 'string') {
              size = Buffer.byteLength(result, 'utf8');
            } else if (result && typeof result === 'object') {
              size = Buffer.byteLength(JSON.stringify(result), 'utf8');
            }
            
            this.recordOperation({
              operation,
              key,
              keyHash: this.hashKey(key),
              duration,
              hit,
              size,
              timestamp: Date.now()
            });
            
            return result;
          } catch (error) {
            const duration = performance.now() - startTime;
            
            this.recordOperation({
              operation,
              key,
              keyHash: this.hashKey(key),
              duration,
              error: error.message,
              timestamp: Date.now()
            });
            
            throw error;
          }
        };
      }
    });
    
    return client;
  }
  
  // 内存缓存监控
  public wrapMemoryCache(cache: Map<string, any>): Map<string, any> {
    const originalGet = cache.get.bind(cache);
    const originalSet = cache.set.bind(cache);
    const originalDelete = cache.delete.bind(cache);
    const originalHas = cache.has.bind(cache);
    
    cache.get = (key: string) => {
      const startTime = performance.now();
      
      try {
        const result = originalGet(key);
        const duration = performance.now() - startTime;
        const hit = result !== undefined;
        
        this.recordOperation({
          operation: 'get',
          key,
          keyHash: this.hashKey(key),
          duration,
          hit,
          size: result ? Buffer.byteLength(JSON.stringify(result), 'utf8') : undefined,
          timestamp: Date.now()
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordOperation({
          operation: 'get',
          key,
          keyHash: this.hashKey(key),
          duration,
          error: error.message,
          timestamp: Date.now()
        });
        
        throw error;
      }
    };
    
    cache.set = (key: string, value: any) => {
      const startTime = performance.now();
      
      try {
        const result = originalSet(key, value);
        const duration = performance.now() - startTime;
        
        this.recordOperation({
          operation: 'set',
          key,
          keyHash: this.hashKey(key),
          duration,
          size: Buffer.byteLength(JSON.stringify(value), 'utf8'),
          timestamp: Date.now()
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordOperation({
          operation: 'set',
          key,
          keyHash: this.hashKey(key),
          duration,
          error: error.message,
          timestamp: Date.now()
        });
        
        throw error;
      }
    };
    
    cache.delete = (key: string) => {
      const startTime = performance.now();
      
      try {
        const result = originalDelete(key);
        const duration = performance.now() - startTime;
        
        this.recordOperation({
          operation: 'delete',
          key,
          keyHash: this.hashKey(key),
          duration,
          timestamp: Date.now()
        });
        
        return result;
      } catch (error) {
        const duration = performance.now() - startTime;
        
        this.recordOperation({
          operation: 'delete',
          key,
          keyHash: this.hashKey(key),
          duration,
          error: error.message,
          timestamp: Date.now()
        });
        
        throw error;
      }
    };
    
    return cache;
  }
  
  private recordOperation(metrics: CacheMetrics): void {
    this.operationCount++;
    
    // 限制key长度
    if (metrics.key.length > 100) {
      metrics.key = metrics.key.substring(0, 100) + '...';
    }
    
    // 添加到指标数组
    this.cacheMetrics.push(metrics);
    
    // 保持最近1000条记录
    if (this.cacheMetrics.length > 1000) {
      this.cacheMetrics.shift();
    }
    
    // 更新统计
    if (metrics.hit === true) {
      this.hitCount++;
    } else if (metrics.hit === false) {
      this.missCount++;
    }
    
    // 检查慢操作
    if (metrics.duration > this.config.slowOperationThreshold) {
      this.slowOperationCount++;
      this.reportSlowCacheOperation(metrics);
    }
    
    // 检查缓存错误
    if (metrics.error) {
      this.errorCount++;
      this.reportCacheError(metrics);
    }
    
    // 记录操作日志
    if (this.config.enableOperationLogging) {
      this.logCacheOperation(metrics);
    }
  }
  
  private reportSlowCacheOperation(metrics: CacheMetrics): void {
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-cache-operation');
      scope.setTag('cache-type', this.config.type);
      scope.setLevel('warning');
      
      scope.setContext('slowCacheOperation', {
        operation: metrics.operation,
        keyHash: metrics.keyHash,
        duration: Math.round(metrics.duration),
        threshold: this.config.slowOperationThreshold,
        size: metrics.size,
        hit: metrics.hit
      });
      
      Sentry.captureMessage(
        `Slow cache operation: ${metrics.operation} took ${Math.round(metrics.duration)}ms`,
        'warning'
      );
    });
  }
  
  private reportCacheError(metrics: CacheMetrics): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'cache');
      scope.setTag('cache-type', this.config.type);
      scope.setLevel('error');
      
      scope.setContext('cacheError', {
        operation: metrics.operation,
        keyHash: metrics.keyHash,
        duration: Math.round(metrics.duration),
        errorMessage: metrics.error
      });
      
      Sentry.captureMessage(
        `Cache operation error: ${metrics.operation} failed - ${metrics.error}`,
        'error'
      );
    });
  }
  
  private logCacheOperation(metrics: CacheMetrics): void {
    Sentry.addBreadcrumb({
      category: 'cache',
      message: `Cache ${metrics.operation} ${metrics.error ? 'failed' : 'completed'}`,
      level: metrics.error ? 'error' : metrics.duration > this.config.slowOperationThreshold ? 'warning' : 'info',
      data: {
        operation: metrics.operation,
        keyHash: metrics.keyHash,
        duration: Math.round(metrics.duration),
        hit: metrics.hit,
        size: metrics.size,
        hasError: !!metrics.error
      }
    });
  }
  
  private hashKey(key: string): string {
    return require('crypto').createHash('md5').update(key).digest('hex').substring(0, 8);
  }
  
  private startMetricsCollection(): void {
    // 定期发送缓存指标摘要
    setInterval(() => {
      this.sendCacheMetricsSummary();
    }, 60000); // 每分钟发送一次
    
    // 定期检查命中率
    if (this.config.enableHitRateMonitoring) {
      setInterval(() => {
        this.checkHitRate();
      }, this.config.hitRateWindow);
    }
  }
  
  private sendCacheMetricsSummary(): void {
    const stats = this.getCacheStats();
    
    if (stats.totalOperations === 0) return;
    
    Sentry.addBreadcrumb({
      category: 'cache',
      message: 'Cache metrics summary',
      level: 'info',
      data: stats
    });
  }
  
  private checkHitRate(): void {
    const now = Date.now();
    const windowStart = now - this.config.hitRateWindow;
    const recentOperations = this.cacheMetrics.filter(m => 
      m.timestamp >= windowStart && (m.hit === true || m.hit === false)
    );
    
    if (recentOperations.length < 10) return; // 样本太少
    
    const hits = recentOperations.filter(m => m.hit === true).length;
    const hitRate = (hits / recentOperations.length) * 100;
    
    // 命中率过低警告
    if (hitRate < 50) {
      Sentry.withScope(scope => {
        scope.setTag('performance-issue', 'low-cache-hit-rate');
        scope.setTag('cache-type', this.config.type);
        scope.setLevel('warning');
        
        scope.setContext('lowCacheHitRate', {
          hitRate: Math.round(hitRate),
          totalOperations: recentOperations.length,
          hits,
          misses: recentOperations.length - hits,
          windowDuration: this.config.hitRateWindow
        });
        
        Sentry.captureMessage(
          `Low cache hit rate detected: ${Math.round(hitRate)}%`,
          'warning'
        );
      });
    }
  }
  
  // 获取缓存统计
  public getCacheStats(): CacheStats {
    const now = Date.now();
    const recentOperations = this.cacheMetrics.filter(m => now - m.timestamp < 300000); // 最近5分钟
    
    const totalOperations = recentOperations.length;
    const hits = recentOperations.filter(m => m.hit === true).length;
    const misses = recentOperations.filter(m => m.hit === false).length;
    const errors = recentOperations.filter(m => m.error).length;
    const slowOperations = recentOperations.filter(m => m.duration > this.config.slowOperationThreshold).length;
    
    const hitRate = (hits + misses) > 0 ? (hits / (hits + misses)) * 100 : 0;
    const averageDuration = totalOperations > 0 ? 
      recentOperations.reduce((sum, m) => sum + m.duration, 0) / totalOperations : 0;
    
    return {
      totalOperations,
      hits,
      misses,
      errors,
      slowOperations,
      hitRate,
      averageDuration,
      timestamp: now
    };
  }
  
  // 手动记录缓存操作
  public recordCacheOperation(operation: string, key: string, duration: number, hit?: boolean, error?: any): void {
    this.recordOperation({
      operation,
      key,
      keyHash: this.hashKey(key),
      duration,
      hit,
      error: error?.message,
      timestamp: Date.now()
    });
  }
}

export { CacheMonitor, CacheConfig, CacheMetrics, CacheStats };
```

## 5. 实时监控与告警

### 5.1 实时监控器

```typescript
// 实时监控器
import * as Sentry from '@sentry/node';
import { EventEmitter } from 'events';
import { performance } from 'perf_hooks';
import WebSocket from 'ws';

interface RealTimeConfig {
  enableWebSocketMonitoring: boolean;
  enableEventStreamMonitoring: boolean;
  enableSystemMetricsMonitoring: boolean;
  metricsInterval: number;
  alertThresholds: {
    cpuUsage: number;
    memoryUsage: number;
    responseTime: number;
    errorRate: number;
  };
}

interface SystemMetrics {
  cpuUsage: number;
  memoryUsage: number;
  memoryTotal: number;
  uptime: number;
  loadAverage: number[];
  timestamp: number;
}

interface AlertRule {
  id: string;
  name: string;
  condition: (metrics: any) => boolean;
  severity: 'low' | 'medium' | 'high' | 'critical';
  cooldown: number; // 冷却时间（毫秒）
  lastTriggered?: number;
}

class RealTimeMonitor extends EventEmitter {
  private config: RealTimeConfig;
  private systemMetrics: SystemMetrics[] = [];
  private alertRules: AlertRule[] = [];
  private activeConnections = new Set<WebSocket>();
  private metricsTimer?: NodeJS.Timeout;
  
  constructor(config: RealTimeConfig) {
    super();
    this.config = config;
    this.setupDefaultAlertRules();
    this.startSystemMetricsCollection();
  }
  
  // WebSocket监控
  public monitorWebSocketServer(wss: WebSocket.Server): void {
    if (!this.config.enableWebSocketMonitoring) return;
    
    wss.on('connection', (ws: WebSocket, request) => {
      const connectionId = this.generateConnectionId();
      this.activeConnections.add(ws);
      
      // 记录连接建立
      Sentry.addBreadcrumb({
        category: 'websocket',
        message: 'WebSocket connection established',
        level: 'info',
        data: {
          connectionId,
          userAgent: request.headers['user-agent'],
          origin: request.headers.origin,
          ip: request.socket.remoteAddress
        }
      });
      
      // 监控消息
      ws.on('message', (data) => {
        this.recordWebSocketMessage(connectionId, 'received', data);
      });
      
      // 监控错误
      ws.on('error', (error) => {
        this.recordWebSocketError(connectionId, error);
      });
      
      // 监控关闭
      ws.on('close', (code, reason) => {
        this.activeConnections.delete(ws);
        this.recordWebSocketClose(connectionId, code, reason);
      });
      
      // 包装发送方法
      const originalSend = ws.send.bind(ws);
      ws.send = (data: any, options?: any, callback?: any) => {
        this.recordWebSocketMessage(connectionId, 'sent', data);
        return originalSend(data, options, callback);
      };
    });
  }
  
  // 事件流监控
  public monitorEventStream(eventEmitter: EventEmitter, eventNames: string[]): void {
    if (!this.config.enableEventStreamMonitoring) return;
    
    eventNames.forEach(eventName => {
      eventEmitter.on(eventName, (...args) => {
        this.recordEventStreamEvent(eventName, args);
      });
    });
  }
  
  // 系统指标收集
  private startSystemMetricsCollection(): void {
    if (!this.config.enableSystemMetricsMonitoring) return;
    
    this.metricsTimer = setInterval(() => {
      this.collectSystemMetrics();
    }, this.config.metricsInterval);
  }
  
  private async collectSystemMetrics(): Promise<void> {
    try {
      const metrics: SystemMetrics = {
        cpuUsage: await this.getCPUUsage(),
        memoryUsage: process.memoryUsage().heapUsed / 1024 / 1024, // MB
        memoryTotal: process.memoryUsage().heapTotal / 1024 / 1024, // MB
        uptime: process.uptime(),
        loadAverage: require('os').loadavg(),
        timestamp: Date.now()
      };
      
      this.systemMetrics.push(metrics);
      
      // 保持最近100条记录
      if (this.systemMetrics.length > 100) {
        this.systemMetrics.shift();
      }
      
      // 检查告警规则
      this.checkAlertRules(metrics);
      
      // 发送系统指标
      this.emit('systemMetrics', metrics);
      
      // 记录到Sentry
      Sentry.addBreadcrumb({
        category: 'system',
        message: 'System metrics collected',
        level: 'info',
        data: {
          cpuUsage: Math.round(metrics.cpuUsage * 100) / 100,
          memoryUsage: Math.round(metrics.memoryUsage),
          memoryTotal: Math.round(metrics.memoryTotal),
          uptime: Math.round(metrics.uptime)
        }
      });
      
    } catch (error) {
      Sentry.captureException(error);
    }
  }
  
  private async getCPUUsage(): Promise<number> {
    return new Promise((resolve) => {
      const startUsage = process.cpuUsage();
      const startTime = process.hrtime();
      
      setTimeout(() => {
        const endUsage = process.cpuUsage(startUsage);
        const endTime = process.hrtime(startTime);
        
        const totalTime = endTime[0] * 1000000 + endTime[1] / 1000; // 微秒
        const totalUsage = endUsage.user + endUsage.system; // 微秒
        
        const cpuPercent = (totalUsage / totalTime) * 100;
        resolve(cpuPercent);
      }, 100);
    });
  }
  
  // 告警规则管理
  private setupDefaultAlertRules(): void {
    this.alertRules = [
      {
        id: 'high-cpu-usage',
        name: 'High CPU Usage',
        condition: (metrics: SystemMetrics) => metrics.cpuUsage > this.config.alertThresholds.cpuUsage,
        severity: 'high',
        cooldown: 300000 // 5分钟
      },
      {
        id: 'high-memory-usage',
        name: 'High Memory Usage',
        condition: (metrics: SystemMetrics) => {
          const memoryPercent = (metrics.memoryUsage / metrics.memoryTotal) * 100;
          return memoryPercent > this.config.alertThresholds.memoryUsage;
        },
        severity: 'high',
        cooldown: 300000 // 5分钟
      },
      {
        id: 'high-load-average',
        name: 'High Load Average',
        condition: (metrics: SystemMetrics) => metrics.loadAverage[0] > require('os').cpus().length * 0.8,
        severity: 'medium',
        cooldown: 600000 // 10分钟
      }
    ];
  }
  
  public addAlertRule(rule: AlertRule): void {
    this.alertRules.push(rule);
  }
  
  public removeAlertRule(ruleId: string): void {
    this.alertRules = this.alertRules.filter(rule => rule.id !== ruleId);
  }
  
  private checkAlertRules(metrics: SystemMetrics): void {
    const now = Date.now();
    
    this.alertRules.forEach(rule => {
      // 检查冷却时间
      if (rule.lastTriggered && (now - rule.lastTriggered) < rule.cooldown) {
        return;
      }
      
      // 检查条件
      if (rule.condition(metrics)) {
        rule.lastTriggered = now;
        this.triggerAlert(rule, metrics);
      }
    });
  }
  
  private triggerAlert(rule: AlertRule, metrics: SystemMetrics): void {
    const alertData = {
      ruleId: rule.id,
      ruleName: rule.name,
      severity: rule.severity,
      metrics,
      timestamp: Date.now()
    };
    
    // 发送告警事件
    this.emit('alert', alertData);
    
    // 记录到Sentry
    Sentry.withScope(scope => {
      scope.setTag('alert-type', 'system-metrics');
      scope.setTag('alert-severity', rule.severity);
      scope.setLevel(rule.severity === 'critical' ? 'error' : 'warning');
      
      scope.setContext('alert', {
        ruleId: rule.id,
        ruleName: rule.name,
        severity: rule.severity,
        cpuUsage: metrics.cpuUsage,
        memoryUsage: metrics.memoryUsage,
        memoryTotal: metrics.memoryTotal,
        loadAverage: metrics.loadAverage
      });
      
      Sentry.captureMessage(
        `System alert triggered: ${rule.name}`,
        rule.severity === 'critical' ? 'error' : 'warning'
      );
    });
  }
  
  // WebSocket事件记录
  private recordWebSocketMessage(connectionId: string, direction: 'sent' | 'received', data: any): void {
    const size = Buffer.byteLength(JSON.stringify(data), 'utf8');
    
    Sentry.addBreadcrumb({
      category: 'websocket',
      message: `WebSocket message ${direction}`,
      level: 'info',
      data: {
        connectionId,
        direction,
        size,
        messageType: typeof data
      }
    });
  }
  
  private recordWebSocketError(connectionId: string, error: Error): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'websocket');
      scope.setLevel('error');
      
      scope.setContext('websocketError', {
        connectionId,
        errorMessage: error.message,
        errorStack: error.stack
      });
      
      Sentry.captureException(error);
    });
  }
  
  private recordWebSocketClose(connectionId: string, code: number, reason: Buffer): void {
    Sentry.addBreadcrumb({
      category: 'websocket',
      message: 'WebSocket connection closed',
      level: code === 1000 ? 'info' : 'warning',
      data: {
        connectionId,
        code,
        reason: reason.toString(),
        isNormalClosure: code === 1000
      }
    });
  }
  
  // 事件流记录
  private recordEventStreamEvent(eventName: string, args: any[]): void {
    Sentry.addBreadcrumb({
      category: 'event-stream',
      message: `Event emitted: ${eventName}`,
      level: 'info',
      data: {
        eventName,
        argsCount: args.length,
        timestamp: Date.now()
      }
    });
  }
  
  private generateConnectionId(): string {
    return require('crypto').randomBytes(8).toString('hex');
  }
  
  // 获取实时统计
  public getRealTimeStats(): any {
    const latestMetrics = this.systemMetrics[this.systemMetrics.length - 1];
    
    return {
      activeConnections: this.activeConnections.size,
      systemMetrics: latestMetrics,
      alertRules: this.alertRules.length,
      timestamp: Date.now()
    };
  }
  
  // 清理资源
  public destroy(): void {
    if (this.metricsTimer) {
      clearInterval(this.metricsTimer);
    }
    
    this.activeConnections.clear();
    this.removeAllListeners();
  }
}

export { RealTimeMonitor, RealTimeConfig, SystemMetrics, AlertRule };
```

## 6. 使用示例

### 6.1 完整集成示例

```typescript
// app.ts - 完整的全栈监控集成示例
import express from 'express';
import { Pool } from 'pg';
import { createClient } from 'redis';
import WebSocket from 'ws';
import { FullStackMonitoringIntegrator } from './monitoring/FullStackMonitoringIntegrator';
import { DatabaseMonitor } from './monitoring/DatabaseMonitor';
import { CacheMonitor } from './monitoring/CacheMonitor';
import { RealTimeMonitor } from './monitoring/RealTimeMonitor';

// 初始化监控系统
const monitoringIntegrator = new FullStackMonitoringIntegrator({
  sentryDsn: process.env.SENTRY_DSN!,
  environment: process.env.NODE_ENV || 'development',
  enablePerformanceMonitoring: true,
  enableDatabaseMonitoring: true,
  enableCacheMonitoring: true,
  enableUserTracking: true,
  slowRequestThreshold: 1000,
  errorRateThreshold: 0.05
});

// 初始化数据库监控
const databaseMonitor = new DatabaseMonitor({
  type: 'postgresql',
  slowQueryThreshold: 500,
  enableQueryLogging: true,
  enableConnectionPoolMonitoring: true,
  maxQueryLogLength: 1000
});

// 初始化缓存监控
const cacheMonitor = new CacheMonitor({
  type: 'redis',
  slowOperationThreshold: 100,
  enableOperationLogging: true,
  enableHitRateMonitoring: true,
  hitRateWindow: 300000 // 5分钟
});

// 初始化实时监控
const realTimeMonitor = new RealTimeMonitor({
  enableWebSocketMonitoring: true,
  enableEventStreamMonitoring: true,
  enableSystemMetricsMonitoring: true,
  metricsInterval: 30000, // 30秒
  alertThresholds: {
    cpuUsage: 80,
    memoryUsage: 85,
    responseTime: 2000,
    errorRate: 0.1
  }
});

// 创建Express应用
const app = express();

// 应用监控中间件
app.use(monitoringIntegrator.getRequestTrackingMiddleware());
app.use(monitoringIntegrator.getErrorHandlingMiddleware());

// 数据库连接
const dbPool = new Pool({
  host: process.env.DB_HOST,
  port: parseInt(process.env.DB_PORT || '5432'),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  max: 20
});

// 包装数据库连接池
const monitoredDbPool = databaseMonitor.wrapPostgreSQLPool(dbPool);

// Redis连接
const redisClient = createClient({
  url: process.env.REDIS_URL
});

// 包装Redis客户端
const monitoredRedisClient = cacheMonitor.wrapRedisClient(redisClient);

// WebSocket服务器
const wss = new WebSocket.Server({ port: 8080 });
realTimeMonitor.monitorWebSocketServer(wss);

// API路由示例
app.get('/api/users', async (req, res) => {
  try {
    // 数据库查询（自动监控）
    const result = await monitoredDbPool.query('SELECT * FROM users LIMIT 10');
    
    // 缓存操作（自动监控）
    await monitoredRedisClient.set('users:recent', JSON.stringify(result.rows), {
      EX: 300 // 5分钟过期
    });
    
    res.json(result.rows);
  } catch (error) {
    // 错误会自动被监控中间件捕获
    throw error;
  }
});

app.get('/api/user/:id', async (req, res) => {
  try {
    const userId = req.params.id;
    
    // 先检查缓存
    const cached = await monitoredRedisClient.get(`user:${userId}`);
    if (cached) {
      return res.json(JSON.parse(cached));
    }
    
    // 数据库查询
    const result = await monitoredDbPool.query('SELECT * FROM users WHERE id = $1', [userId]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    const user = result.rows[0];
    
    // 缓存结果
    await monitoredRedisClient.set(`user:${userId}`, JSON.stringify(user), {
      EX: 600 // 10分钟过期
    });
    
    res.json(user);
  } catch (error) {
    throw error;
  }
});

// 健康检查端点
app.get('/health', async (req, res) => {
  try {
    // 检查数据库连接
    await monitoredDbPool.query('SELECT 1');
    
    // 检查Redis连接
    await monitoredRedisClient.ping();
    
    // 获取系统指标
    const systemStats = realTimeMonitor.getRealTimeStats();
    const dbStats = databaseMonitor.getDatabaseMetrics();
    const cacheStats = cacheMonitor.getCacheStats();
    
    res.json({
      status: 'healthy',
      timestamp: new Date().toISOString(),
      system: systemStats,
      database: dbStats,
      cache: cacheStats
    });
  } catch (error) {
    res.status(500).json({
      status: 'unhealthy',
      error: error.message,
      timestamp: new Date().toISOString()
    });
  }
});

// 监控事件处理
realTimeMonitor.on('alert', (alertData) => {
  console.log('System alert triggered:', alertData);
  // 这里可以发送通知到Slack、邮件等
});

realTimeMonitor.on('systemMetrics', (metrics) => {
  // 可以将指标发送到其他监控系统
  console.log('System metrics:', {
    cpu: `${metrics.cpuUsage.toFixed(2)}%`,
    memory: `${metrics.memoryUsage.toFixed(0)}MB`,
    uptime: `${(metrics.uptime / 3600).toFixed(2)}h`
  });
});

// 启动服务器
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log('Full-stack monitoring enabled');
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log('Shutting down gracefully...');
  
  // 清理监控资源
  realTimeMonitor.destroy();
  
  // 关闭数据库连接
  dbPool.end();
  
  // 关闭Redis连接
  redisClient.quit();
  
  process.exit(0);
});
```

### 6.2 前端集成示例

```typescript
// frontend/monitoring.ts - 前端监控集成
import { FrontendTraceSender } from './monitoring/FrontendTraceSender';

// 初始化前端追踪
const traceSender = new FrontendTraceSender({
  backendUrl: 'http://localhost:3000',
  enableNetworkInterception: true,
  enableUserTracking: true,
  enableSessionTracking: true,
  enablePerformanceTracking: true,
  slowRequestThreshold: 2000,
  maxRetries: 3,
  retryDelay: 1000
});

// 发送用户行为数据
traceSender.trackUserAction('button_click', {
  buttonId: 'submit-form',
  formData: { /* 表单数据 */ }
});

// 发送页面性能数据
traceSender.trackPagePerformance({
  url: window.location.href,
  loadTime: performance.now(),
  domContentLoaded: 1500,
  firstContentfulPaint: 1200
});

// 发送自定义事件
traceSender.sendCustomEvent('feature_used', {
  featureName: 'advanced_search',
  userId: 'user123',
  timestamp: Date.now()
});
```

## 7. 最佳实践与总结

### 7.1 实施建议

1. **渐进式部署**
   - 从核心功能开始，逐步扩展监控范围
   - 先在测试环境验证，再部署到生产环境
   - 设置合理的采样率，避免性能影响

2. **数据隐私保护**
   - 过滤敏感信息（密码、令牌等）
   - 遵循GDPR等数据保护法规
   - 实施数据脱敏和匿名化

3. **性能优化**
   - 使用异步处理避免阻塞主线程
   - 实施批量发送减少网络开销
   - 设置合理的缓冲区大小

4. **告警策略**
   - 设置多级告警阈值
   - 实施告警聚合避免噪音
   - 建立告警升级机制

### 7.2 核心价值

1. **全链路可观测性**
   - 前后端统一的错误追踪
   - 完整的用户行为路径
   - 端到端的性能监控

2. **智能化运维**
   - 自动化问题检测
   - 预测性告警机制
   - 智能根因分析

3. **业务价值提升**
   - 提高系统稳定性
   - 优化用户体验
   - 降低运维成本

### 7.3 未来发展趋势

1. **AI驱动的监控**
   - 机器学习异常检测
   - 智能告警降噪
   - 自动化问题修复

2. **云原生监控**
   - 容器化监控
   - 服务网格集成
   - 多云环境支持

3. **业务监控融合**
   - 技术指标与业务指标结合
   - 用户体验量化
   - 业务影响评估

通过本文的全栈监控体系，我们可以构建一个完整、高效、智能的监控解决方案，为现代化应用提供全方位的可观测性支持。