# 前端Sentry与Angular生态集成：构建企业级监控体系

在现代企业级前端开发中，Angular以其强大的架构设计和丰富的生态系统成为了许多大型项目的首选框架。然而，随着应用复杂度的增加，如何有效监控Angular应用的运行状态、快速定位问题并优化性能成为了开发团队面临的重要挑战。本文将深入探讨如何将Sentry与Angular生态系统进行深度集成，构建一个全面、高效的企业级监控体系。

## 1. Angular监控架构设计

### 1.1 Angular监控集成器

```typescript
// Angular监控集成器
import { Injectable, ErrorHandler, NgZone, ApplicationRef } from '@angular/core';
import { Router, NavigationEnd, NavigationError } from '@angular/router';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import * as Sentry from '@sentry/angular';
import { Observable, fromEvent, merge } from 'rxjs';
import { filter, debounceTime, map } from 'rxjs/operators';

interface AngularMonitoringConfig {
  dsn: string;
  environment: string;
  release?: string;
  enablePerformanceMonitoring?: boolean;
  enableRouterMonitoring?: boolean;
  enableHttpMonitoring?: boolean;
  enableComponentMonitoring?: boolean;
  enableFormMonitoring?: boolean;
  sampleRate?: number;
  tracesSampleRate?: number;
  blacklistedRoutes?: string[];
  blacklistedComponents?: string[];
  sensitiveDataKeys?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AngularMonitoringIntegrator {
  private config: AngularMonitoringConfig;
  private performanceObserver: PerformanceObserver | null = null;
  private componentMetrics = new Map<string, any>();
  private routeMetrics = new Map<string, any>();
  private httpMetrics = new Map<string, any>();
  private formMetrics = new Map<string, any>();
  private isInitialized = false;
  
  constructor(
    private router: Router,
    private ngZone: NgZone,
    private appRef: ApplicationRef
  ) {}
  
  // 初始化监控
  initialize(config: AngularMonitoringConfig): void {
    if (this.isInitialized) {
      console.warn('Angular monitoring already initialized');
      return;
    }
    
    this.config = {
      enablePerformanceMonitoring: true,
      enableRouterMonitoring: true,
      enableHttpMonitoring: true,
      enableComponentMonitoring: true,
      enableFormMonitoring: true,
      sampleRate: 1.0,
      tracesSampleRate: 0.1,
      blacklistedRoutes: [],
      blacklistedComponents: [],
      sensitiveDataKeys: ['password', 'token', 'secret', 'key'],
      ...config
    };
    
    // 初始化Sentry
    this.initializeSentry();
    
    // 设置各种监控
    if (this.config.enableRouterMonitoring) {
      this.setupRouterMonitoring();
    }
    
    if (this.config.enablePerformanceMonitoring) {
      this.setupPerformanceMonitoring();
    }
    
    if (this.config.enableComponentMonitoring) {
      this.setupComponentMonitoring();
    }
    
    if (this.config.enableFormMonitoring) {
      this.setupFormMonitoring();
    }
    
    // 设置全局错误处理
    this.setupGlobalErrorHandling();
    
    // 设置用户反馈
    this.setupUserFeedback();
    
    this.isInitialized = true;
    
    console.log('Angular monitoring initialized successfully');
  }
  
  // 初始化Sentry
  private initializeSentry(): void {
    Sentry.init({
      dsn: this.config.dsn,
      environment: this.config.environment,
      release: this.config.release,
      sampleRate: this.config.sampleRate,
      tracesSampleRate: this.config.tracesSampleRate,
      integrations: [
        new Sentry.BrowserTracing({
          routingInstrumentation: Sentry.routingInstrumentation,
          tracingOrigins: ['localhost', /^\//],
        }),
      ],
      beforeSend: (event) => this.beforeSendEvent(event),
      beforeSendTransaction: (transaction) => this.beforeSendTransaction(transaction)
    });
  }
  
  // 设置路由监控
  private setupRouterMonitoring(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd || event instanceof NavigationError)
    ).subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.recordRouteNavigation(event);
      } else if (event instanceof NavigationError) {
        this.recordRouteError(event);
      }
    });
  }
  
  // 记录路由导航
  private recordRouteNavigation(event: NavigationEnd): void {
    const routePath = this.sanitizeRoutePath(event.urlAfterRedirects);
    
    if (this.isRouteBlacklisted(routePath)) {
      return;
    }
    
    const routeMetric = {
      path: routePath,
      timestamp: Date.now(),
      loadTime: performance.now(),
      previousRoute: this.getPreviousRoute()
    };
    
    this.routeMetrics.set(routePath, routeMetric);
    
    // 发送到Sentry
    Sentry.addBreadcrumb({
      category: 'navigation',
      message: `Navigated to ${routePath}`,
      level: 'info',
      data: {
        from: routeMetric.previousRoute,
        to: routePath,
        loadTime: routeMetric.loadTime
      }
    });
    
    // 开始路由事务
    const transaction = Sentry.startTransaction({
      name: `Route: ${routePath}`,
      op: 'navigation'
    });
    
    Sentry.getCurrentHub().configureScope(scope => {
      scope.setTag('route', routePath);
      scope.setContext('route', {
        path: routePath,
        timestamp: routeMetric.timestamp,
        loadTime: routeMetric.loadTime
      });
    });
    
    // 在下一个导航时结束事务
    const subscription = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      take(1)
    ).subscribe(() => {
      transaction.finish();
      subscription.unsubscribe();
    });
  }
  
  // 记录路由错误
  private recordRouteError(event: NavigationError): void {
    const routePath = this.sanitizeRoutePath(event.url);
    
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'navigation');
      scope.setTag('route', routePath);
      scope.setLevel('error');
      
      scope.setContext('navigationError', {
        url: event.url,
        error: event.error?.toString(),
        timestamp: new Date().toISOString()
      });
      
      Sentry.captureException(event.error || new Error(`Navigation failed to ${routePath}`));
    });
  }
  
  // 设置性能监控
  private setupPerformanceMonitoring(): void {
    // Web Vitals监控
    this.monitorWebVitals();
    
    // 资源加载监控
    this.monitorResourceLoading();
    
    // 内存使用监控
    this.monitorMemoryUsage();
    
    // 长任务监控
    this.monitorLongTasks();
  }
  
  // 监控Web Vitals
  private monitorWebVitals(): void {
    if ('PerformanceObserver' in window) {
      // LCP (Largest Contentful Paint)
      const lcpObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        
        Sentry.addBreadcrumb({
          category: 'performance',
          message: 'LCP measured',
          level: 'info',
          data: {
            value: lastEntry.startTime,
            rating: this.getLCPRating(lastEntry.startTime)
          }
        });
      });
      
      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });
      
      // FID (First Input Delay)
      const fidObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          Sentry.addBreadcrumb({
            category: 'performance',
            message: 'FID measured',
            level: 'info',
            data: {
              value: entry.processingStart - entry.startTime,
              rating: this.getFIDRating(entry.processingStart - entry.startTime)
            }
          });
        });
      });
      
      fidObserver.observe({ entryTypes: ['first-input'] });
      
      // CLS (Cumulative Layout Shift)
      let clsValue = 0;
      const clsObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          if (!entry.hadRecentInput) {
            clsValue += entry.value;
          }
        });
        
        Sentry.addBreadcrumb({
          category: 'performance',
          message: 'CLS measured',
          level: 'info',
          data: {
            value: clsValue,
            rating: this.getCLSRating(clsValue)
          }
        });
      });
      
      clsObserver.observe({ entryTypes: ['layout-shift'] });
    }
  }
  
  // 监控资源加载
  private monitorResourceLoading(): void {
    if ('PerformanceObserver' in window) {
      const resourceObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        
        entries.forEach(entry => {
          const resource = entry as PerformanceResourceTiming;
          
          // 只监控关键资源
          if (this.isImportantResource(resource.name)) {
            const loadTime = resource.responseEnd - resource.startTime;
            
            if (loadTime > 1000) { // 超过1秒的资源
              Sentry.addBreadcrumb({
                category: 'performance',
                message: 'Slow resource loading',
                level: 'warning',
                data: {
                  url: resource.name,
                  loadTime,
                  size: resource.transferSize,
                  type: this.getResourceType(resource.name)
                }
              });
            }
          }
        });
      });
      
      resourceObserver.observe({ entryTypes: ['resource'] });
    }
  }
  
  // 监控内存使用
  private monitorMemoryUsage(): void {
    if ('memory' in performance) {
      setInterval(() => {
        const memory = (performance as any).memory;
        const usedMemory = memory.usedJSHeapSize;
        const totalMemory = memory.totalJSHeapSize;
        const memoryUsagePercent = (usedMemory / totalMemory) * 100;
        
        if (memoryUsagePercent > 80) {
          Sentry.addBreadcrumb({
            category: 'performance',
            message: 'High memory usage detected',
            level: 'warning',
            data: {
              usedMemory,
              totalMemory,
              usagePercent: memoryUsagePercent
            }
          });
        }
      }, 30000); // 每30秒检查一次
    }
  }
  
  // 监控长任务
  private monitorLongTasks(): void {
    if ('PerformanceObserver' in window) {
      const longTaskObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        
        entries.forEach(entry => {
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
        });
      });
      
      longTaskObserver.observe({ entryTypes: ['longtask'] });
    }
  }
  
  // 设置组件监控
  private setupComponentMonitoring(): void {
    // 这里需要通过装饰器或其他方式来监控组件
    // 具体实现会在后面的组件监控器中详细说明
  }
  
  // 设置表单监控
  private setupFormMonitoring(): void {
    // 监控表单错误和提交
    this.ngZone.runOutsideAngular(() => {
      fromEvent(document, 'submit').pipe(
        debounceTime(100)
      ).subscribe((event: Event) => {
        this.recordFormSubmission(event);
      });
      
      fromEvent(document, 'invalid').pipe(
        debounceTime(100)
      ).subscribe((event: Event) => {
        this.recordFormValidationError(event);
      });
    });
  }
  
  // 记录表单提交
  private recordFormSubmission(event: Event): void {
    const form = event.target as HTMLFormElement;
    const formData = new FormData(form);
    const sanitizedData = this.sanitizeFormData(formData);
    
    Sentry.addBreadcrumb({
      category: 'form',
      message: 'Form submitted',
      level: 'info',
      data: {
        formId: form.id || 'unknown',
        formName: form.name || 'unknown',
        fieldCount: sanitizedData.size,
        action: form.action,
        method: form.method
      }
    });
  }
  
  // 记录表单验证错误
  private recordFormValidationError(event: Event): void {
    const field = event.target as HTMLInputElement;
    
    Sentry.addBreadcrumb({
      category: 'form',
      message: 'Form validation error',
      level: 'warning',
      data: {
        fieldName: field.name || 'unknown',
        fieldType: field.type,
        validationMessage: field.validationMessage,
        formId: field.form?.id || 'unknown'
      }
    });
  }
  
  // 设置全局错误处理
  private setupGlobalErrorHandling(): void {
    // 这里会与Angular的ErrorHandler集成
    // 具体实现在AngularErrorHandler中
  }
  
  // 设置用户反馈
  private setupUserFeedback(): void {
    // 监听用户反馈事件
    this.ngZone.runOutsideAngular(() => {
      fromEvent(window, 'beforeunload').subscribe(() => {
        this.generateSessionSummary();
      });
    });
  }
  
  // 生成会话摘要
  private generateSessionSummary(): void {
    const summary = {
      routeMetrics: Array.from(this.routeMetrics.values()),
      componentMetrics: Array.from(this.componentMetrics.values()),
      httpMetrics: Array.from(this.httpMetrics.values()),
      formMetrics: Array.from(this.formMetrics.values()),
      sessionDuration: Date.now() - this.getSessionStartTime(),
      timestamp: new Date().toISOString()
    };
    
    Sentry.addBreadcrumb({
      category: 'session',
      message: 'Session summary',
      level: 'info',
      data: summary
    });
  }
  
  // 辅助方法
  private sanitizeRoutePath(path: string): string {
    // 移除查询参数和片段
    return path.split('?')[0].split('#')[0];
  }
  
  private isRouteBlacklisted(path: string): boolean {
    return this.config.blacklistedRoutes?.some(route => 
      path.includes(route)
    ) || false;
  }
  
  private getPreviousRoute(): string {
    // 从路由历史中获取上一个路由
    return 'unknown'; // 简化实现
  }
  
  private getLCPRating(value: number): string {
    if (value <= 2500) return 'good';
    if (value <= 4000) return 'needs-improvement';
    return 'poor';
  }
  
  private getFIDRating(value: number): string {
    if (value <= 100) return 'good';
    if (value <= 300) return 'needs-improvement';
    return 'poor';
  }
  
  private getCLSRating(value: number): string {
    if (value <= 0.1) return 'good';
    if (value <= 0.25) return 'needs-improvement';
    return 'poor';
  }
  
  private isImportantResource(url: string): boolean {
    const importantExtensions = ['.js', '.css', '.woff', '.woff2', '.png', '.jpg', '.svg'];
    return importantExtensions.some(ext => url.includes(ext));
  }
  
  private getResourceType(url: string): string {
    if (url.includes('.js')) return 'script';
    if (url.includes('.css')) return 'stylesheet';
    if (url.includes('.woff') || url.includes('.woff2')) return 'font';
    if (url.includes('.png') || url.includes('.jpg') || url.includes('.svg')) return 'image';
    return 'other';
  }
  
  private sanitizeFormData(formData: FormData): Map<string, any> {
    const sanitized = new Map();
    
    for (const [key, value] of formData.entries()) {
      if (this.config.sensitiveDataKeys?.some(sensitive => 
        key.toLowerCase().includes(sensitive.toLowerCase())
      )) {
        sanitized.set(key, '[FILTERED]');
      } else {
        sanitized.set(key, typeof value === 'string' ? value.slice(0, 100) : '[FILE]');
      }
    }
    
    return sanitized;
  }
  
  private getSessionStartTime(): number {
    // 简化实现，实际应该在应用启动时记录
    return Date.now() - 3600000; // 假设1小时前开始
  }
  
  private beforeSendEvent(event: Sentry.Event): Sentry.Event | null {
    // 过滤敏感数据
    if (event.extra) {
      event.extra = this.sanitizeEventData(event.extra);
    }
    
    if (event.contexts) {
      event.contexts = this.sanitizeEventData(event.contexts);
    }
    
    return event;
  }
  
  private beforeSendTransaction(transaction: Sentry.Transaction): Sentry.Transaction | null {
    // 过滤不重要的事务
    if (transaction.name?.includes('heartbeat') || 
        transaction.name?.includes('ping')) {
      return null;
    }
    
    return transaction;
  }
  
  private sanitizeEventData(data: any): any {
    if (typeof data !== 'object' || data === null) {
      return data;
    }
    
    const sanitized = { ...data };
    
    for (const [key, value] of Object.entries(sanitized)) {
      if (this.config.sensitiveDataKeys?.some(sensitive => 
        key.toLowerCase().includes(sensitive.toLowerCase())
      )) {
        sanitized[key] = '[FILTERED]';
      } else if (typeof value === 'object') {
        sanitized[key] = this.sanitizeEventData(value);
      }
    }
    
    return sanitized;
  }
}

export { AngularMonitoringIntegrator, AngularMonitoringConfig };
```

### 1.2 Angular错误处理器

```typescript
// Angular错误处理器
import { ErrorHandler, Injectable, NgZone } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import * as Sentry from '@sentry/angular';

interface ErrorContext {
  component?: string;
  action?: string;
  userId?: string;
  sessionId?: string;
  route?: string;
  userAgent?: string;
  timestamp?: string;
  [key: string]: any;
}

@Injectable()
export class AngularErrorHandler implements ErrorHandler {
  private errorCount = 0;
  private maxErrorsPerSession = 50;
  private errorCache = new Set<string>();
  private lastErrorTime = 0;
  private errorRateThreshold = 5; // 每分钟最多5个错误
  
  constructor(private ngZone: NgZone) {}
  
  handleError(error: any): void {
    // 检查错误率限制
    if (!this.shouldReportError()) {
      return;
    }
    
    // 增加错误计数
    this.errorCount++;
    
    // 在Angular Zone外执行，避免影响应用性能
    this.ngZone.runOutsideAngular(() => {
      this.processError(error);
    });
    
    // 仍然输出到控制台
    console.error('Angular Error:', error);
  }
  
  private shouldReportError(): boolean {
    const now = Date.now();
    const timeSinceLastError = now - this.lastErrorTime;
    
    // 检查错误总数限制
    if (this.errorCount >= this.maxErrorsPerSession) {
      return false;
    }
    
    // 检查错误频率限制
    if (timeSinceLastError < 60000 / this.errorRateThreshold) {
      return false;
    }
    
    this.lastErrorTime = now;
    return true;
  }
  
  private processError(error: any): void {
    const errorInfo = this.extractErrorInfo(error);
    const errorHash = this.generateErrorHash(errorInfo);
    
    // 避免重复报告相同错误
    if (this.errorCache.has(errorHash)) {
      return;
    }
    
    this.errorCache.add(errorHash);
    
    // 清理缓存（保持最近100个错误）
    if (this.errorCache.size > 100) {
      const firstError = this.errorCache.values().next().value;
      this.errorCache.delete(firstError);
    }
    
    // 发送到Sentry
    this.sendToSentry(error, errorInfo);
  }
  
  private extractErrorInfo(error: any): any {
    const errorInfo: any = {
      timestamp: new Date().toISOString(),
      url: window.location.href,
      userAgent: navigator.userAgent
    };
    
    if (error instanceof HttpErrorResponse) {
      // HTTP错误
      errorInfo.type = 'http';
      errorInfo.status = error.status;
      errorInfo.statusText = error.statusText;
      errorInfo.url = error.url;
      errorInfo.method = error.headers?.get('X-HTTP-Method') || 'unknown';
      errorInfo.message = error.message;
    } else if (error instanceof Error) {
      // JavaScript错误
      errorInfo.type = 'javascript';
      errorInfo.name = error.name;
      errorInfo.message = error.message;
      errorInfo.stack = error.stack;
    } else if (typeof error === 'string') {
      // 字符串错误
      errorInfo.type = 'string';
      errorInfo.message = error;
    } else {
      // 其他类型错误
      errorInfo.type = 'unknown';
      errorInfo.message = String(error);
    }
    
    // 尝试获取Angular特定信息
    if (error.ngDebugContext) {
      errorInfo.component = error.ngDebugContext.component?.constructor?.name;
      errorInfo.element = error.ngDebugContext.element?.tagName;
    }
    
    return errorInfo;
  }
  
  private generateErrorHash(errorInfo: any): string {
    const hashString = `${errorInfo.type}-${errorInfo.name}-${errorInfo.message}`;
    return btoa(hashString).slice(0, 16);
  }
  
  private sendToSentry(error: any, errorInfo: any): void {
    Sentry.withScope(scope => {
      // 设置错误级别
      scope.setLevel(this.getErrorLevel(errorInfo));
      
      // 设置标签
      scope.setTag('error-handler', 'angular');
      scope.setTag('error-type', errorInfo.type);
      
      if (errorInfo.component) {
        scope.setTag('component', errorInfo.component);
      }
      
      if (errorInfo.status) {
        scope.setTag('http-status', errorInfo.status.toString());
      }
      
      // 设置上下文
      scope.setContext('errorInfo', {
        ...errorInfo,
        errorCount: this.errorCount,
        sessionId: this.getSessionId()
      });
      
      scope.setContext('browser', {
        userAgent: navigator.userAgent,
        language: navigator.language,
        platform: navigator.platform,
        cookieEnabled: navigator.cookieEnabled,
        onLine: navigator.onLine
      });
      
      scope.setContext('page', {
        url: window.location.href,
        referrer: document.referrer,
        title: document.title,
        timestamp: errorInfo.timestamp
      });
      
      // 设置用户信息（如果可用）
      const userInfo = this.getUserInfo();
      if (userInfo) {
        scope.setUser(userInfo);
      }
      
      // 添加面包屑
      scope.addBreadcrumb({
        category: 'error',
        message: `Error handled by Angular ErrorHandler`,
        level: 'error',
        data: {
          type: errorInfo.type,
          component: errorInfo.component,
          errorCount: this.errorCount
        }
      });
      
      // 捕获异常
      if (error instanceof Error) {
        Sentry.captureException(error);
      } else {
        Sentry.captureMessage(errorInfo.message || 'Unknown error', 'error');
      }
    });
  }
  
  private getErrorLevel(errorInfo: any): Sentry.SeverityLevel {
    if (errorInfo.type === 'http') {
      if (errorInfo.status >= 500) {
        return 'error';
      } else if (errorInfo.status >= 400) {
        return 'warning';
      }
      return 'info';
    }
    
    if (errorInfo.type === 'javascript') {
      if (errorInfo.name === 'TypeError' || errorInfo.name === 'ReferenceError') {
        return 'error';
      }
      return 'warning';
    }
    
    return 'error';
  }
  
  private getSessionId(): string {
    // 简化实现，实际应该从会话管理服务获取
    return sessionStorage.getItem('sessionId') || 'unknown';
  }
  
  private getUserInfo(): any {
    // 简化实现，实际应该从用户服务获取
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        return {
          id: user.id,
          email: user.email,
          username: user.username
        };
      }
    } catch (e) {
      // 忽略解析错误
    }
    return null;
  }
  
  // 手动报告错误的方法
  reportError(error: Error, context?: ErrorContext): void {
    Sentry.withScope(scope => {
      if (context) {
        // 设置标签
        Object.keys(context).forEach(key => {
          if (typeof context[key] === 'string') {
            scope.setTag(key, context[key]);
          }
        });
        
        // 设置上下文
        scope.setContext('manualReport', context);
      }
      
      scope.setTag('error-source', 'manual');
      Sentry.captureException(error);
    });
  }
  
  // 报告消息的方法
  reportMessage(message: string, level: Sentry.SeverityLevel = 'info', context?: ErrorContext): void {
    Sentry.withScope(scope => {
      if (context) {
        Object.keys(context).forEach(key => {
          if (typeof context[key] === 'string') {
            scope.setTag(key, context[key]);
          }
        });
        
        scope.setContext('manualReport', context);
      }
      
      scope.setTag('message-source', 'manual');
      Sentry.captureMessage(message, level);
    });
  }
}

export { AngularErrorHandler, ErrorContext };
```

## 2. HTTP拦截器监控

### 2.1 HTTP监控拦截器

```typescript
// HTTP监控拦截器
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import * as Sentry from '@sentry/angular';
import { Observable, throwError } from 'rxjs';
import { tap, catchError, finalize } from 'rxjs/operators';

interface HttpMetrics {
  url: string;
  method: string;
  startTime: number;
  endTime?: number;
  duration?: number;
  status?: number;
  size?: number;
  success: boolean;
  error?: string;
  retryCount?: number;
}

@Injectable()
export class HttpMonitoringInterceptor implements HttpInterceptor {
  private activeRequests = new Map<string, HttpMetrics>();
  private requestCounter = 0;
  private slowRequestThreshold = 3000; // 3秒
  private errorRequestThreshold = 5; // 连续5个错误请求
  private consecutiveErrors = 0;
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // 生成请求ID
    const requestId = this.generateRequestId();
    const startTime = performance.now();
    
    // 创建请求指标
    const metrics: HttpMetrics = {
      url: this.sanitizeUrl(req.url),
      method: req.method,
      startTime,
      success: false
    };
    
    this.activeRequests.set(requestId, metrics);
    
    // 开始Sentry事务
    const transaction = Sentry.startTransaction({
      name: `${req.method} ${this.sanitizeUrl(req.url)}`,
      op: 'http.client'
    });
    
    // 添加请求头用于追踪
    const tracedReq = req.clone({
      setHeaders: {
        'X-Request-ID': requestId,
        'X-Sentry-Trace': transaction.toTraceparent()
      }
    });
    
    return next.handle(tracedReq).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          this.handleSuccessResponse(requestId, event, transaction);
        }
      }),
      catchError(error => {
        this.handleErrorResponse(requestId, error, transaction);
        return throwError(error);
      }),
      finalize(() => {
        this.finalizeRequest(requestId, transaction);
      })
    );
  }
  
  private generateRequestId(): string {
    return `req_${++this.requestCounter}_${Date.now()}`;
  }
  
  private sanitizeUrl(url: string): string {
    // 移除敏感参数
    const urlObj = new URL(url, window.location.origin);
    const sensitiveParams = ['token', 'key', 'password', 'secret'];
    
    sensitiveParams.forEach(param => {
      if (urlObj.searchParams.has(param)) {
        urlObj.searchParams.set(param, '[FILTERED]');
      }
    });
    
    return urlObj.toString();
  }
  
  private handleSuccessResponse(requestId: string, response: HttpResponse<any>, transaction: Sentry.Transaction): void {
    const metrics = this.activeRequests.get(requestId);
    if (!metrics) return;
    
    const endTime = performance.now();
    const duration = endTime - metrics.startTime;
    
    // 更新指标
    metrics.endTime = endTime;
    metrics.duration = duration;
    metrics.status = response.status;
    metrics.success = true;
    
    // 计算响应大小
    if (response.body) {
      try {
        metrics.size = new Blob([JSON.stringify(response.body)]).size;
      } catch (e) {
        metrics.size = 0;
      }
    }
    
    // 重置连续错误计数
    this.consecutiveErrors = 0;
    
    // 检查慢请求
    if (duration > this.slowRequestThreshold) {
      this.reportSlowRequest(metrics, transaction);
    }
    
    // 添加成功面包屑
    Sentry.addBreadcrumb({
      category: 'http',
      message: `${metrics.method} ${metrics.url}`,
      level: 'info',
      data: {
        status: metrics.status,
        duration: Math.round(duration),
        size: metrics.size
      }
    });
    
    // 设置事务状态
    transaction.setHttpStatus(response.status);
    transaction.setData('response.size', metrics.size);
  }
  
  private handleErrorResponse(requestId: string, error: HttpErrorResponse, transaction: Sentry.Transaction): void {
    const metrics = this.activeRequests.get(requestId);
    if (!metrics) return;
    
    const endTime = performance.now();
    const duration = endTime - metrics.startTime;
    
    // 更新指标
    metrics.endTime = endTime;
    metrics.duration = duration;
    metrics.status = error.status;
    metrics.success = false;
    metrics.error = error.message;
    
    // 增加连续错误计数
    this.consecutiveErrors++;
    
    // 检查连续错误
    if (this.consecutiveErrors >= this.errorRequestThreshold) {
      this.reportConsecutiveErrors();
    }
    
    // 报告HTTP错误
    this.reportHttpError(metrics, error, transaction);
    
    // 设置事务状态
    transaction.setHttpStatus(error.status);
    transaction.setTag('error', true);
  }
  
  private finalizeRequest(requestId: string, transaction: Sentry.Transaction): void {
    const metrics = this.activeRequests.get(requestId);
    if (metrics && metrics.duration) {
      // 设置事务数据
      transaction.setData('request.method', metrics.method);
      transaction.setData('request.url', metrics.url);
      transaction.setData('request.duration', metrics.duration);
      
      // 结束事务
      transaction.finish();
    }
    
    // 清理活跃请求
    this.activeRequests.delete(requestId);
  }
  
  private reportSlowRequest(metrics: HttpMetrics, transaction: Sentry.Transaction): void {
    Sentry.withScope(scope => {
      scope.setTag('performance-issue', 'slow-request');
      scope.setTag('http-method', metrics.method);
      scope.setLevel('warning');
      
      scope.setContext('slowRequest', {
        url: metrics.url,
        method: metrics.method,
        duration: metrics.duration,
        threshold: this.slowRequestThreshold,
        status: metrics.status,
        size: metrics.size
      });
      
      Sentry.captureMessage(
        `Slow HTTP request: ${metrics.method} ${metrics.url} took ${Math.round(metrics.duration!)}ms`,
        'warning'
      );
    });
  }
  
  private reportHttpError(metrics: HttpMetrics, error: HttpErrorResponse, transaction: Sentry.Transaction): void {
    Sentry.withScope(scope => {
      scope.setTag('error-type', 'http');
      scope.setTag('http-method', metrics.method);
      scope.setTag('http-status', metrics.status?.toString() || 'unknown');
      scope.setLevel('error');
      
      scope.setContext('httpError', {
        url: metrics.url,
        method: metrics.method,
        status: metrics.status,
        statusText: error.statusText,
        duration: metrics.duration,
        consecutiveErrors: this.consecutiveErrors,
        headers: this.sanitizeHeaders(error.headers)
      });
      
      // 添加错误面包屑
      Sentry.addBreadcrumb({
        category: 'http',
        message: `HTTP Error: ${metrics.method} ${metrics.url}`,
        level: 'error',
        data: {
          status: metrics.status,
          duration: Math.round(metrics.duration!),
          error: metrics.error
        }
      });
      
      Sentry.captureException(error);
    });
  }
  
  private reportConsecutiveErrors(): void {
    Sentry.withScope(scope => {
      scope.setTag('error-pattern', 'consecutive-http-errors');
      scope.setLevel('error');
      
      scope.setContext('consecutiveErrors', {
        count: this.consecutiveErrors,
        threshold: this.errorRequestThreshold,
        timestamp: new Date().toISOString()
      });
      
      Sentry.captureMessage(
        `Detected ${this.consecutiveErrors} consecutive HTTP errors`,
        'error'
      );
    });
  }
  
  private sanitizeHeaders(headers: any): any {
    if (!headers) return null;
    
    const sanitized: any = {};
    const sensitiveHeaders = ['authorization', 'cookie', 'x-api-key', 'x-auth-token'];
    
    headers.keys().forEach((key: string) => {
      const lowerKey = key.toLowerCase();
      if (sensitiveHeaders.includes(lowerKey)) {
        sanitized[key] = '[FILTERED]';
      } else {
        sanitized[key] = headers.get(key);
      }
    });
    
    return sanitized;
  }
  
  // 获取HTTP指标摘要
  getHttpMetricsSummary(): any {
    const activeCount = this.activeRequests.size;
    const totalRequests = this.requestCounter;
    
    return {
      activeRequests: activeCount,
      totalRequests,
      consecutiveErrors: this.consecutiveErrors,
      timestamp: new Date().toISOString()
    };
  }
}

export { HttpMonitoringInterceptor, HttpMetrics };
```

## 3. Angular组件监控

### 3.1 组件监控装饰器

```typescript
// 组件监控装饰器
import { Component, OnInit, OnDestroy, AfterViewInit, DoCheck } from '@angular/core';
import * as Sentry from '@sentry/angular';

interface ComponentMetrics {
  name: string;
  initTime?: number;
  renderTime?: number;
  destroyTime?: number;
  changeDetectionCount: number;
  errorCount: number;
  memoryUsage?: number;
  propsChanges: number;
  lastUpdate: number;
}

interface MonitoringOptions {
  trackPerformance?: boolean;
  trackChanges?: boolean;
  trackErrors?: boolean;
  trackMemory?: boolean;
  sampleRate?: number;
  blacklistedProps?: string[];
}

// 组件监控装饰器
export function MonitorComponent(options: MonitoringOptions = {}) {
  return function <T extends { new(...args: any[]): {} }>(constructor: T) {
    const defaultOptions: MonitoringOptions = {
      trackPerformance: true,
      trackChanges: true,
      trackErrors: true,
      trackMemory: false,
      sampleRate: 1.0,
      blacklistedProps: ['password', 'token', 'secret'],
      ...options
    };
    
    // 如果采样率不满足，跳过监控
    if (Math.random() > defaultOptions.sampleRate!) {
      return constructor;
    }
    
    return class extends constructor implements OnInit, OnDestroy, AfterViewInit, DoCheck {
      private _componentMetrics: ComponentMetrics;
      private _startTime: number;
      private _changeDetectionStartTime: number;
      private _previousProps: any = {};
      private _memoryObserver?: PerformanceObserver;
      
      constructor(...args: any[]) {
        super(...args);
        
        this._startTime = performance.now();
        this._componentMetrics = {
          name: constructor.name,
          changeDetectionCount: 0,
          errorCount: 0,
          propsChanges: 0,
          lastUpdate: Date.now()
        };
        
        // 开始组件事务
        const transaction = Sentry.startTransaction({
          name: `Component: ${constructor.name}`,
          op: 'component.lifecycle'
        });
        
        Sentry.getCurrentHub().configureScope(scope => {
          scope.setTag('component', constructor.name);
          scope.setContext('component', {
            name: constructor.name,
            startTime: this._startTime
          });
        });
      }
      
      ngOnInit() {
        if (super.ngOnInit) {
          super.ngOnInit();
        }
        
        if (defaultOptions.trackPerformance) {
          this._componentMetrics.initTime = performance.now() - this._startTime;
          
          Sentry.addBreadcrumb({
            category: 'component',
            message: `${constructor.name} initialized`,
            level: 'info',
            data: {
              initTime: this._componentMetrics.initTime
            }
          });
        }
        
        if (defaultOptions.trackMemory) {
          this.setupMemoryMonitoring();
        }
      }
      
      ngAfterViewInit() {
        if (super.ngAfterViewInit) {
          super.ngAfterViewInit();
        }
        
        if (defaultOptions.trackPerformance) {
          this._componentMetrics.renderTime = performance.now() - this._startTime;
          
          // 检查渲染性能
          if (this._componentMetrics.renderTime > 100) {
            this.reportSlowRender();
          }
        }
      }
      
      ngDoCheck() {
        if (super.ngDoCheck) {
          super.ngDoCheck();
        }
        
        if (defaultOptions.trackChanges) {
          this._changeDetectionStartTime = performance.now();
          this._componentMetrics.changeDetectionCount++;
          
          // 检查变更检测频率
          if (this._componentMetrics.changeDetectionCount % 100 === 0) {
            this.reportFrequentChangeDetection();
          }
          
          // 跟踪属性变化
          this.trackPropsChanges();
        }
      }
      
      ngOnDestroy() {
        if (super.ngOnDestroy) {
          super.ngOnDestroy();
        }
        
        if (defaultOptions.trackPerformance) {
          this._componentMetrics.destroyTime = performance.now();
        }
        
        // 清理内存监控
        if (this._memoryObserver) {
          this._memoryObserver.disconnect();
        }
        
        // 发送组件生命周期摘要
        this.sendComponentSummary();
      }
      
      private setupMemoryMonitoring(): void {
        if ('memory' in performance) {
          const checkMemory = () => {
            const memory = (performance as any).memory;
            this._componentMetrics.memoryUsage = memory.usedJSHeapSize;
          };
          
          checkMemory();
          setInterval(checkMemory, 5000); // 每5秒检查一次
        }
      }
      
      private trackPropsChanges(): void {
        try {
          const currentProps = this.extractProps();
          const changes = this.compareProps(this._previousProps, currentProps);
          
          if (changes.length > 0) {
            this._componentMetrics.propsChanges++;
            this._componentMetrics.lastUpdate = Date.now();
            
            Sentry.addBreadcrumb({
              category: 'component',
              message: `${constructor.name} props changed`,
              level: 'debug',
              data: {
                changes: changes.slice(0, 5), // 只记录前5个变化
                changeCount: changes.length
              }
            });
          }
          
          this._previousProps = currentProps;
        } catch (error) {
          // 忽略属性提取错误
        }
      }
      
      private extractProps(): any {
        const props: any = {};
        
        // 提取组件属性（简化实现）
        Object.keys(this).forEach(key => {
          if (!key.startsWith('_') && 
              !key.startsWith('ng') && 
              typeof (this as any)[key] !== 'function') {
            
            const value = (this as any)[key];
            
            // 过滤敏感属性
            if (defaultOptions.blacklistedProps?.some(prop => 
              key.toLowerCase().includes(prop.toLowerCase())
            )) {
              props[key] = '[FILTERED]';
            } else {
              props[key] = this.sanitizeValue(value);
            }
          }
        });
        
        return props;
      }
      
      private compareProps(prev: any, current: any): string[] {
        const changes: string[] = [];
        
        // 检查新增和修改的属性
        Object.keys(current).forEach(key => {
          if (prev[key] !== current[key]) {
            changes.push(key);
          }
        });
        
        // 检查删除的属性
        Object.keys(prev).forEach(key => {
          if (!(key in current)) {
            changes.push(`-${key}`);
          }
        });
        
        return changes;
      }
      
      private sanitizeValue(value: any): any {
        if (value === null || value === undefined) {
          return value;
        }
        
        if (typeof value === 'function') {
          return '[Function]';
        }
        
        if (typeof value === 'object') {
          try {
            const serialized = JSON.stringify(value);
            return serialized.length > 200 ? '[Object]' : value;
          } catch {
            return '[Object]';
          }
        }
        
        if (typeof value === 'string' && value.length > 100) {
          return value.slice(0, 100) + '...';
        }
        
        return value;
      }
      
      private reportSlowRender(): void {
        Sentry.withScope(scope => {
          scope.setTag('performance-issue', 'slow-render');
          scope.setTag('component', constructor.name);
          scope.setLevel('warning');
          
          scope.setContext('slowRender', {
            component: constructor.name,
            renderTime: this._componentMetrics.renderTime,
            initTime: this._componentMetrics.initTime
          });
          
          Sentry.captureMessage(
            `Slow component render: ${constructor.name} took ${Math.round(this._componentMetrics.renderTime!)}ms`,
            'warning'
          );
        });
      }
      
      private reportFrequentChangeDetection(): void {
        Sentry.withScope(scope => {
          scope.setTag('performance-issue', 'frequent-change-detection');
          scope.setTag('component', constructor.name);
          scope.setLevel('warning');
          
          scope.setContext('frequentChangeDetection', {
            component: constructor.name,
            changeDetectionCount: this._componentMetrics.changeDetectionCount,
            propsChanges: this._componentMetrics.propsChanges
          });
          
          Sentry.captureMessage(
            `Frequent change detection: ${constructor.name} has ${this._componentMetrics.changeDetectionCount} cycles`,
            'warning'
          );
        });
      }
      
      private sendComponentSummary(): void {
        Sentry.addBreadcrumb({
          category: 'component',
          message: `${constructor.name} lifecycle summary`,
          level: 'info',
          data: {
            ...this._componentMetrics,
            totalLifetime: this._componentMetrics.destroyTime ? 
              this._componentMetrics.destroyTime - this._startTime : undefined
          }
        });
      }
    };
  };
}

// 组件错误边界装饰器
export function ErrorBoundary(fallbackComponent?: any) {
  return function <T extends { new(...args: any[]): {} }>(constructor: T) {
    return class extends constructor {
      constructor(...args: any[]) {
        super(...args);
        
        // 设置错误处理
        this.setupErrorHandling();
      }
      
      private setupErrorHandling(): void {
        // 捕获未处理的Promise错误
        window.addEventListener('unhandledrejection', (event) => {
          this.handleError(event.reason, 'unhandledrejection');
        });
        
        // 捕获全局错误
        window.addEventListener('error', (event) => {
          this.handleError(event.error, 'global');
        });
      }
      
      private handleError(error: any, source: string): void {
        Sentry.withScope(scope => {
          scope.setTag('error-boundary', constructor.name);
          scope.setTag('error-source', source);
          scope.setLevel('error');
          
          scope.setContext('errorBoundary', {
            component: constructor.name,
            source,
            timestamp: new Date().toISOString()
          });
          
          Sentry.captureException(error);
        });
        
        // 如果提供了fallback组件，可以在这里处理UI降级
        if (fallbackComponent) {
          // 实现UI降级逻辑
        }
      }
    };
  };
}

export { ComponentMetrics, MonitoringOptions };
```

### 3.2 使用示例

```typescript
// 使用组件监控装饰器
import { Component, OnInit, OnDestroy } from '@angular/core';
import { MonitorComponent, ErrorBoundary } from './component-monitoring';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
@MonitorComponent({
  trackPerformance: true,
  trackChanges: true,
  trackMemory: true,
  sampleRate: 0.1, // 10%采样率
  blacklistedProps: ['password', 'token']
})
@ErrorBoundary()
export class UserProfileComponent implements OnInit, OnDestroy {
  user: any;
  loading = false;
  error: string | null = null;
  
  ngOnInit() {
    this.loadUserProfile();
  }
  
  ngOnDestroy() {
    // 清理逻辑
  }
  
  private async loadUserProfile() {
    try {
      this.loading = true;
      // 模拟API调用
      this.user = await this.userService.getProfile();
    } catch (error) {
      this.error = 'Failed to load user profile';
      throw error; // 让错误边界捕获
    } finally {
      this.loading = false;
    }
  }
}
```

## 4. Angular服务监控

### 4.1 服务监控装饰器

```typescript
// 服务监控装饰器
import { Injectable } from '@angular/core';
import * as Sentry from '@sentry/angular';

interface ServiceMetrics {
  name: string;
  methodCalls: Map<string, number>;
  methodErrors: Map<string, number>;
  methodDurations: Map<string, number[]>;
  lastActivity: number;
  totalCalls: number;
  totalErrors: number;
}

interface ServiceMonitoringOptions {
  trackMethods?: boolean;
  trackPerformance?: boolean;
  trackErrors?: boolean;
  slowMethodThreshold?: number;
  sampleRate?: number;
  blacklistedMethods?: string[];
}

// 服务监控装饰器
export function MonitorService(options: ServiceMonitoringOptions = {}) {
  return function <T extends { new(...args: any[]): {} }>(constructor: T) {
    const defaultOptions: ServiceMonitoringOptions = {
      trackMethods: true,
      trackPerformance: true,
      trackErrors: true,
      slowMethodThreshold: 1000,
      sampleRate: 1.0,
      blacklistedMethods: ['toString', 'valueOf', 'constructor'],
      ...options
    };
    
    // 如果采样率不满足，跳过监控
    if (Math.random() > defaultOptions.sampleRate!) {
      return constructor;
    }
    
    return class extends constructor {
      private _serviceMetrics: ServiceMetrics;
      
      constructor(...args: any[]) {
        super(...args);
        
        this._serviceMetrics = {
          name: constructor.name,
          methodCalls: new Map(),
          methodErrors: new Map(),
          methodDurations: new Map(),
          lastActivity: Date.now(),
          totalCalls: 0,
          totalErrors: 0
        };
        
        // 代理所有方法
        this.proxyMethods();
        
        // 设置Sentry上下文
        Sentry.getCurrentHub().configureScope(scope => {
          scope.setTag('service', constructor.name);
        });
      }
      
      private proxyMethods(): void {
        const prototype = Object.getPrototypeOf(this);
        const methodNames = Object.getOwnPropertyNames(prototype)
          .filter(name => {
            return typeof (this as any)[name] === 'function' &&
                   name !== 'constructor' &&
                   !name.startsWith('_') &&
                   !defaultOptions.blacklistedMethods?.includes(name);
          });
        
        methodNames.forEach(methodName => {
          const originalMethod = (this as any)[methodName];
          
          (this as any)[methodName] = (...args: any[]) => {
            return this.wrapMethod(methodName, originalMethod, args);
          };
        });
      }
      
      private wrapMethod(methodName: string, originalMethod: Function, args: any[]): any {
        const startTime = performance.now();
        
        // 更新调用统计
        this._serviceMetrics.totalCalls++;
        this._serviceMetrics.lastActivity = Date.now();
        
        const currentCalls = this._serviceMetrics.methodCalls.get(methodName) || 0;
        this._serviceMetrics.methodCalls.set(methodName, currentCalls + 1);
        
        // 开始方法事务
        const transaction = Sentry.startTransaction({
          name: `${constructor.name}.${methodName}`,
          op: 'service.method'
        });
        
        try {
          const result = originalMethod.apply(this, args);
          
          // 处理Promise结果
          if (result && typeof result.then === 'function') {
            return result
              .then((value: any) => {
                this.recordMethodSuccess(methodName, startTime, transaction);
                return value;
              })
              .catch((error: any) => {
                this.recordMethodError(methodName, error, startTime, transaction);
                throw error;
              });
          } else {
            this.recordMethodSuccess(methodName, startTime, transaction);
            return result;
          }
        } catch (error) {
          this.recordMethodError(methodName, error, startTime, transaction);
          throw error;
        }
      }
      
      private recordMethodSuccess(methodName: string, startTime: number, transaction: Sentry.Transaction): void {
        const duration = performance.now() - startTime;
        
        // 记录方法持续时间
        const durations = this._serviceMetrics.methodDurations.get(methodName) || [];
        durations.push(duration);
        
        // 保持最近100次调用的记录
        if (durations.length > 100) {
          durations.shift();
        }
        
        this._serviceMetrics.methodDurations.set(methodName, durations);
        
        // 检查慢方法
        if (duration > defaultOptions.slowMethodThreshold!) {
          this.reportSlowMethod(methodName, duration);
        }
        
        // 添加面包屑
        Sentry.addBreadcrumb({
          category: 'service',
          message: `${constructor.name}.${methodName} completed`,
          level: 'info',
          data: {
            duration: Math.round(duration),
            callCount: this._serviceMetrics.methodCalls.get(methodName)
          }
        });
        
        // 完成事务
        transaction.setData('method.duration', duration);
        transaction.finish();
      }
      
      private recordMethodError(methodName: string, error: any, startTime: number, transaction: Sentry.Transaction): void {
        const duration = performance.now() - startTime;
        
        // 更新错误统计
        this._serviceMetrics.totalErrors++;
        const currentErrors = this._serviceMetrics.methodErrors.get(methodName) || 0;
        this._serviceMetrics.methodErrors.set(methodName, currentErrors + 1);
        
        // 报告服务错误
        this.reportServiceError(methodName, error, duration);
        
        // 完成事务
        transaction.setTag('error', true);
        transaction.setData('method.duration', duration);
        transaction.finish();
      }
      
      private reportSlowMethod(methodName: string, duration: number): void {
        Sentry.withScope(scope => {
          scope.setTag('performance-issue', 'slow-service-method');
          scope.setTag('service', constructor.name);
          scope.setTag('method', methodName);
          scope.setLevel('warning');
          
          scope.setContext('slowMethod', {
            service: constructor.name,
            method: methodName,
            duration,
            threshold: defaultOptions.slowMethodThreshold,
            callCount: this._serviceMetrics.methodCalls.get(methodName),
            averageDuration: this.getAverageMethodDuration(methodName)
          });
          
          Sentry.captureMessage(
            `Slow service method: ${constructor.name}.${methodName} took ${Math.round(duration)}ms`,
            'warning'
          );
        });
      }
      
      private reportServiceError(methodName: string, error: any, duration: number): void {
        Sentry.withScope(scope => {
          scope.setTag('error-type', 'service');
          scope.setTag('service', constructor.name);
          scope.setTag('method', methodName);
          scope.setLevel('error');
          
          scope.setContext('serviceError', {
            service: constructor.name,
            method: methodName,
            duration,
            errorCount: this._serviceMetrics.methodErrors.get(methodName),
            totalCalls: this._serviceMetrics.methodCalls.get(methodName),
            errorRate: this.getMethodErrorRate(methodName)
          });
          
          Sentry.captureException(error);
        });
      }
      
      private getAverageMethodDuration(methodName: string): number {
        const durations = this._serviceMetrics.methodDurations.get(methodName) || [];
        if (durations.length === 0) return 0;
        
        const sum = durations.reduce((acc, duration) => acc + duration, 0);
        return sum / durations.length;
      }
      
      private getMethodErrorRate(methodName: string): number {
        const errors = this._serviceMetrics.methodErrors.get(methodName) || 0;
        const calls = this._serviceMetrics.methodCalls.get(methodName) || 0;
        
        return calls > 0 ? (errors / calls) * 100 : 0;
      }
      
      // 获取服务指标摘要
      getServiceMetrics(): any {
        const methodStats = Array.from(this._serviceMetrics.methodCalls.entries()).map(([method, calls]) => ({
          method,
          calls,
          errors: this._serviceMetrics.methodErrors.get(method) || 0,
          averageDuration: this.getAverageMethodDuration(method),
          errorRate: this.getMethodErrorRate(method)
        }));
        
        return {
          service: constructor.name,
          totalCalls: this._serviceMetrics.totalCalls,
          totalErrors: this._serviceMetrics.totalErrors,
          errorRate: this._serviceMetrics.totalCalls > 0 ? 
            (this._serviceMetrics.totalErrors / this._serviceMetrics.totalCalls) * 100 : 0,
          lastActivity: this._serviceMetrics.lastActivity,
          methodStats,
          timestamp: new Date().toISOString()
        };
      }
    };
  };
}

export { ServiceMetrics, ServiceMonitoringOptions };
```

### 4.2 使用示例

```typescript
// 使用服务监控装饰器
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MonitorService } from './service-monitoring';

@Injectable({
  providedIn: 'root'
})
@MonitorService({
  trackPerformance: true,
  trackErrors: true,
  slowMethodThreshold: 2000,
  sampleRate: 0.5 // 50%采样率
})
export class UserService {
  constructor(private http: HttpClient) {}
  
  async getProfile(userId: string): Promise<any> {
    // 这个方法会被自动监控
    const response = await this.http.get(`/api/users/${userId}`).toPromise();
    return response;
  }
  
  async updateProfile(userId: string, data: any): Promise<any> {
    // 这个方法也会被自动监控
    const response = await this.http.put(`/api/users/${userId}`, data).toPromise();
    return response;
  }
  
  private validateUser(user: any): boolean {
    // 私有方法不会被监控（因为以_开头的方法被排除）
    return user && user.id && user.email;
  }
}
```

## 5. 最佳实践与总结

### 5.1 实施建议

1. **渐进式集成**
   - 从核心模块开始集成Sentry
   - 逐步添加组件和服务监控
   - 根据业务需求调整监控粒度

2. **性能优化**
   - 合理设置采样率，避免性能影响
   - 使用黑名单过滤不重要的组件和方法
   - 定期清理监控数据，防止内存泄漏

3. **错误处理策略**
   - 设置多层错误边界
   - 实现优雅的错误降级
   - 提供用户友好的错误提示

4. **数据安全**
   - 过滤敏感数据和参数
   - 限制数据传输大小
   - 遵循企业数据保护政策

### 5.2 核心价值

1. **企业级监控**
   - 深度集成Angular架构
   - 支持大规模应用监控
   - 提供详细的性能分析

2. **开发效率**
   - 自动化错误收集和分析
   - 快速定位问题根源
   - 简化调试和维护流程

3. **业务稳定性**
   - 主动发现和预防问题
   - 提升应用可靠性
   - 优化用户体验

### 5.3 未来发展趋势

1. **智能化监控**
   - AI驱动的异常检测
   - 自动化性能优化建议
   - 智能错误分类和处理

2. **深度集成**
   - 与Angular DevTools深度集成
   - 支持更多Angular生态工具
   - 提供可视化监控面板

3. **微前端支持**
   - 跨应用监控和追踪
   - 统一的错误处理机制
   - 分布式性能分析

通过本文介绍的Angular生态集成方案，开发者可以构建一个全面、高效的企业级前端监控体系，为Angular应用提供强大的错误监控、性能分析和用户体验优化能力。这不仅能够帮助开发团队快速定位和解决问题，还能为企业级应用的稳定运行提供可靠保障。